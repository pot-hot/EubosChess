package eubos.search.generators;

import java.util.List;

import com.fluxchess.jcpi.models.GenericMove;

import eubos.board.InvalidPieceException;
import eubos.main.EubosEngineMain;
import eubos.position.IChangePosition;
import eubos.position.IEvaluate;
import eubos.position.IPositionAccessors;
import eubos.search.NoLegalMoveException;
import eubos.search.PlySearcher;
import eubos.search.PrincipalContinuation;
import eubos.search.ScoreTracker;
import eubos.search.SearchMetrics;
import eubos.search.SearchMetricsReporter;
import eubos.search.SearchResult;
import eubos.search.transposition.FixedSizeTranspositionTable;
import eubos.search.transposition.TranspositionTableAccessor;

public class MiniMaxMoveGenerator implements
		IMoveGenerator {

	private IChangePosition pm;
	private IPositionAccessors pos;
	public PrincipalContinuation pc;
	private SearchMetrics sm;
	private SearchMetricsReporter sr;
	private boolean sendInfo = false;
	private EubosEngineMain callback;
	private PlySearcher ps;
	private FixedSizeTranspositionTable tt;
	private TranspositionTableAccessor tta;
	private ScoreTracker st;
	private IEvaluate pe;
	private short score;

	// Used for unit tests
	MiniMaxMoveGenerator( FixedSizeTranspositionTable hashMap,
			IChangePosition pm,
			IPositionAccessors pos,
			IEvaluate pe) {
		this.pm = pm;
		this.pos = pos;
		this.pe = pe;
		tt = hashMap;
		sm = new SearchMetrics();
		score = 0;
	}

	// Used with Arena, Lichess
	public MiniMaxMoveGenerator( EubosEngineMain eubos,
			FixedSizeTranspositionTable hashMap,
			IChangePosition pm,
			IPositionAccessors pos,
			IEvaluate pe) {
		this(hashMap, pm, pos, pe);
		sm = new SearchMetrics();
		callback = eubos;
		sendInfo = true;
		score = 0;
	}
	
	public short getScore() { return score; }
	
	private void initialiseSearchDepthDependentObjects(int searchDepth) {
		pc = new PrincipalContinuation(searchDepth*3);
		sm.setDepth(searchDepth);
		sm.clearCurrentMoveNumber();
		sm.setPrincipalVariation(pc.toPvList());
		sr = new SearchMetricsReporter(callback,sm);	
		if (sendInfo)
			sr.setSendInfo(true);
		st = new ScoreTracker(searchDepth*3, pos.onMoveIsWhite());
		tta = new TranspositionTableAccessor(tt, pos, st);
	}
	
	@Override
	public SearchResult findMove() throws NoLegalMoveException, InvalidPieceException {
		return this.findMove((byte)1, null);
	}
	
	@Override
	public SearchResult findMove(byte searchDepth) throws NoLegalMoveException, InvalidPieceException {
		return this.findMove(searchDepth, null);
	}
	
	@Override
	public SearchResult findMove(byte searchDepth, List<GenericMove> lastPc) throws NoLegalMoveException, InvalidPieceException {
		boolean foundMate = false;
		initialiseSearchDepthDependentObjects(searchDepth);
		ps = new PlySearcher(tta, st, pc, sm, sr, searchDepth, pm, pos, lastPc, pe);
		// Start the search reporter task
		if (sendInfo)
			sr.start();
		// Descend the plies in the search tree, to full depth, updating board and scoring positions
		try {
			score = ps.searchPly();
		} catch (AssertionError e) {
			e.printStackTrace();
			//this.terminateFindMove();
			System.exit(0);
		}
		if (Math.abs(score) >= eubos.board.pieces.King.MATERIAL_VALUE) {
			foundMate = true;
		}
		if (sendInfo) {
			sr.end();
			sr.reportNodeData();
		}
		// Select the best move
		GenericMove bestMove = pc.getBestMove();
		if (bestMove==null) {
			throw new NoLegalMoveException();
		}
		return new SearchResult(bestMove,foundMate);
	}
	
	public synchronized void terminateFindMove() {
		if (ps != null)
			ps.terminateFindMove();
		}
}
