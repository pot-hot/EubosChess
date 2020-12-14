package eubos.search.searchers;

import java.util.ArrayList;
import java.util.List;

import com.fluxchess.jcpi.commands.ProtocolBestMoveCommand;
import com.fluxchess.jcpi.models.GenericMove;

import eubos.board.InvalidPieceException;
import eubos.main.EubosEngineMain;
import eubos.position.Move;
import eubos.search.DrawChecker;
import eubos.search.NoLegalMoveException;
import eubos.search.SearchResult;
import eubos.search.generators.MiniMaxMoveGenerator;
import eubos.search.transposition.FixedSizeTranspositionTable;
import eubos.search.transposition.ITransposition;

public class MultithreadedIterativeMoveSearcher extends IterativeMoveSearcher {
	
	private static final int STAGGERED_START_TIME_FOR_THREADS = 25;
	private static final boolean ENABLE_MOVE_LIST_ORDERING_IN_WORKER_THREADS = true;
	
	protected IterativeMoveSearchStopper stopper;
	protected int threads = 0;
	private long rootPositionHash = 0;
	private FixedSizeTranspositionTable tt;
	
	protected List<MultithreadedSearchWorkerThread> workers;
	protected List<MiniMaxMoveGenerator> moveGenerators;
	
	public MultithreadedIterativeMoveSearcher(EubosEngineMain eubos, 
			FixedSizeTranspositionTable hashMap, 
			String fen,  
			DrawChecker dc, 
			long time,
			long increment,
			int threads) {
		super(eubos, hashMap, fen, dc, time, increment);
		this.setName("MultithreadedIterativeMoveSearcher");
		this.threads = threads;
		this.tt = hashMap;
		rootPositionHash = mg.pos.getHash();
		workers = new ArrayList<MultithreadedSearchWorkerThread>(threads);
		createMoveGenerators(hashMap, fen, dc, threads);
		stopper = new IterativeMoveSearchStopper();
	}

	private void createMoveGenerators(FixedSizeTranspositionTable hashMap, String fen, DrawChecker dc, int threads) {
		moveGenerators = new ArrayList<MiniMaxMoveGenerator>(threads);
		// The first move generator shall be that constructed by the abstract MoveSearcher
		moveGenerators.add(mg);
		// Create subsequent move generators using cloned DrawCheckers
		for (int i=1; i < threads; i++) {
			MiniMaxMoveGenerator thisMg = new MiniMaxMoveGenerator(hashMap, fen, new DrawChecker(dc.getState()), sr);
			moveGenerators.add(thisMg);
			if (!ENABLE_MOVE_LIST_ORDERING_IN_WORKER_THREADS) {
				thisMg.disableMoveListOrdering();
			}
		}
	}
	
	@Override
	public void halt() {
		haltAllWorkers();
		searchStopped = true; 
	}
	
	private void haltAllWorkers() {
		for (MultithreadedSearchWorkerThread worker : workers) {
			worker.halt();
		}		
	}

	@Override
	public void run() {
		stopper.start();
		// Create workers and let them run
		for (int i=0; i < threads; i++) {
			MultithreadedSearchWorkerThread worker = new MultithreadedSearchWorkerThread(moveGenerators.get(i));
			workers.add(worker);
			worker.start();
			if (STAGGERED_START_TIME_FOR_THREADS > 0) {
				try {
					Thread.sleep(STAGGERED_START_TIME_FOR_THREADS);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
		
		enableSearchMetricsReporter(true);
		while (isAtLeastOneWorkerStillAlive()) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		enableSearchMetricsReporter(false);
		
		stopper.end();
		sendBestMove();
		terminateSearchMetricsReporter();
	}

	private void sendBestMove() {
		GenericMove bestMove;
		ITransposition trans = tt.getTransposition(this.rootPositionHash);
		if (trans != null) {
			EubosEngineMain.logger.info(String.format("best is trans=%s", trans.report()));
			bestMove = Move.toGenericMove(trans.getBestMove());
		} else {
			EubosEngineMain.logger.warning("Can't find bestMove in Transposition Table");
			bestMove = workers.get(0).result.bestMove;
		}
		eubosEngine.sendBestMoveCommand(new ProtocolBestMoveCommand( bestMove, null ));
	}
	
	private boolean isAtLeastOneWorkerStillAlive() {
		boolean isAtLeastOneWorkerStillAlive = false;
		for (MultithreadedSearchWorkerThread worker : workers) {
			if (worker.isAlive()) {
				isAtLeastOneWorkerStillAlive = true;
				break;
			}
		}
		return isAtLeastOneWorkerStillAlive;
	}

	class MultithreadedSearchWorkerThread extends Thread {
		
		private MiniMaxMoveGenerator myMg;
		public SearchResult result;
		
		public MultithreadedSearchWorkerThread( MiniMaxMoveGenerator moveGen ) {
			this.myMg = moveGen;
		}
		
		public void run() {
			byte currentDepth = 1;
			List<Integer> pc = null;
			result = new SearchResult(null, false);
		
			while (!searchStopped) {
				try {
					result = myMg.findMove(currentDepth, pc, sr);
				} catch( NoLegalMoveException e ) {
					EubosEngineMain.logger.info(
							String.format("out of legal moves"));
					searchStopped = true;
				} catch(InvalidPieceException e ) {
					EubosEngineMain.logger.info(
							String.format("can't find piece at %s", e.getAtPosition()));
					searchStopped = true;
				}
				if (result != null && result.foundMate) {
					EubosEngineMain.logger.info(String.format("%s found mate", this));
					break;
				}				
				if (stopper.extraTime && !searchStopped) {
					// don't start a new iteration, we only allow time to complete the current ply
					searchStopped = true;
					if (DEBUG_LOGGING) {
						EubosEngineMain.logger.info(String.format(
							"%s findMove stopped, not time for a new iteration, ran for %d ms", this, stopper.timeRanFor));
					}
				}
				pc = myMg.pc.toPvList(0);
				currentDepth++;
				if (currentDepth == Byte.MAX_VALUE) {
					break;
				}
			}
			// The result can be read by reading the result member of this object or by reading the shared transposition table
		}
		
		public void halt() {
			myMg.terminateFindMove(); 
		}	
	}
}
