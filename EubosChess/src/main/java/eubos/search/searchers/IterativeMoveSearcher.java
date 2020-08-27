package eubos.search.searchers;

import java.util.List;

import com.fluxchess.jcpi.commands.ProtocolBestMoveCommand;

import eubos.board.InvalidPieceException;
import eubos.board.Piece.Colour;
import eubos.main.EubosEngineMain;
import eubos.position.IChangePosition;
import eubos.position.IPositionAccessors;
import eubos.score.IEvaluate;
import eubos.search.NoLegalMoveException;
import eubos.search.SearchDebugAgent;
import eubos.search.SearchResult;
import eubos.search.generators.MiniMaxMoveGenerator;
import eubos.search.transposition.FixedSizeTranspositionTable;

public class IterativeMoveSearcher extends AbstractMoveSearcher {
	
	public static final int AVG_MOVES_PER_GAME = 50;
	long gameTimeRemaining;
	short initialScore;
	boolean searchStopped = false;

	public IterativeMoveSearcher(EubosEngineMain eubos, 
			FixedSizeTranspositionTable hashMap, 
			IChangePosition inputPm,  
			IPositionAccessors pos, 
			long time,
			IEvaluate pe ) {
		super(eubos,inputPm,pos, new MiniMaxMoveGenerator( eubos, hashMap, inputPm, pos, pe ));
		initialScore = pe.evaluatePosition();
		if (Colour.isBlack(pos.getOnMove())) {
			initialScore = (short)-initialScore;
		}
		EubosEngineMain.logger.info(
				String.format("Starting initialScore=%d gameTimeRemaining=%d", initialScore, time));
		gameTimeRemaining = time;
		this.setName("IterativeMoveSearcher");
	}
	
	@Override
	public void halt() {
		mg.terminateFindMove();
		searchStopped = true; 
	}
	
	@Override
	public void run() {
		byte currentDepth = 1;
		SearchResult res = new SearchResult(null, false);
		List<Integer> pc = null;
		IterativeMoveSearchStopper stopper = new IterativeMoveSearchStopper(initialScore);
		stopper.start();
		while (!searchStopped) {
			try {
				res = mg.findMove(currentDepth, pc);
			} catch( NoLegalMoveException e ) {
				EubosEngineMain.logger.info(
						String.format("IterativeMoveSearcher out of legal moves for %s", pos.getOnMove()));
				searchStopped = true;
			} catch(InvalidPieceException e ) {
				EubosEngineMain.logger.info(
						String.format("IterativeMoveSearcher can't find piece at %s", e.getAtPosition()));
				searchStopped = true;
			}
			if (res != null && res.foundMate) {
				EubosEngineMain.logger.info("IterativeMoveSearcher found mate");
				break;
			}				
			if (stopper.extraTime) {
				// don't start a new iteration, we just allow time to complete the current ply
				searchStopped = true;
				EubosEngineMain.logger.info(
						String.format("findMove stopped, not time for a new iteration, ran for %d ms", stopper.timeRanFor));
			}
			pc = mg.pc.toPvList(0);
			currentDepth++;
			if (currentDepth == Byte.MAX_VALUE) {
				break;
			}
		}
		EubosEngineMain.logger.info(
			String.format("IterativeMoveSearcher ended best=%s gameTimeRemaining=%d", res.bestMove, gameTimeRemaining));
		stopper.end();
		eubosEngine.sendBestMoveCommand(new ProtocolBestMoveCommand( res.bestMove, null ));
		mg.terminateSearchMetricsReporter();
		SearchDebugAgent.close();
		//System.gc();
	}

	class IterativeMoveSearchStopper extends Thread {
		
		private boolean stopperActive = false;
		boolean extraTime = false;
		private int checkPoint = 0;
		long timeRanFor = 0;
		long timeIntoWait = 0;
		long timeOutOfWait = 0;
		
		IterativeMoveSearchStopper(short initialScore) {
		}
		
		public void run() {
			long timeQuanta = 0;
			stopperActive = true;
			boolean hasWaitedOnce = false;
			boolean terminateNow = false;
			do {
				timeQuanta = calculateSearchTimeQuanta();
				if (hasWaitedOnce) {
					/* Consider extending time for Search according to following... */
					short currentScore = mg.sm.getCpScore();
					switch (checkPoint) {
					case 0:
						if (currentScore > (initialScore + 500))
							terminateNow = true;
						break;
					case 1:
						if (currentScore >= (initialScore - 25)) {
							terminateNow = true;
						}
						extraTime = true;
						break;
					case 2:
						break;
					case 3:
						if (currentScore >= (initialScore - 300))
							terminateNow = true;
						break;
					case 4:
					case 5:
					case 6:
						break;
					case 7:
					default:
						terminateNow = true;
					
						break;
					}
					if (terminateNow) {
						mg.terminateFindMove();
						searchStopped = true;
						stopperActive = false;
					} else {
						checkPoint++;
					}
					
					EubosEngineMain.logger.info(String.format(
							"IterativeMoveSearchStopper checkPoint=%d searchStopped=%s ranFor=%d ", checkPoint, searchStopped, timeRanFor));
				}
				
				timeIntoWait = System.currentTimeMillis();
				try {
					synchronized (this) {
						this.wait(Math.max(timeQuanta, 1));
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				timeOutOfWait = System.currentTimeMillis();
				long duration = Math.max((timeOutOfWait - timeIntoWait), 1);
				gameTimeRemaining -= duration;
				timeRanFor += duration;
				hasWaitedOnce = true;
				
			} while (stopperActive);
		}
		
		public void end() {
			stopperActive = false;
			synchronized (this) {
				this.notify();
			}
		}
		
		private long calculateSearchTimeQuanta() {
			int moveHypothesis = (AVG_MOVES_PER_GAME - pos.getMoveNumber());
			int movesRemaining = Math.max(moveHypothesis, 10);
			long msPerMove = gameTimeRemaining/movesRemaining;
			long timeQuanta = msPerMove/2;
			return timeQuanta;
		}
	}
}
