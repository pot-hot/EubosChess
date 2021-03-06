package eubos.search;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.fluxchess.jcpi.models.GenericMove;
import com.fluxchess.jcpi.models.IllegalNotationException;

import eubos.board.InvalidPieceException;
import eubos.position.Move;
import eubos.position.PositionManager;
import eubos.search.transposition.FixedSizeTranspositionTable;
import eubos.search.transposition.ITransposition;
import eubos.search.transposition.TranspositionEvaluation;
import eubos.search.transposition.TranspositionTableAccessor;
import eubos.search.transposition.TranspositionEvaluation.TranspositionTableStatus;

public class TranspositionTableAccessorTest {

	FixedSizeTranspositionTable transTable;
	PositionManager pm;
	ScoreTracker st;
	PrincipalContinuation pc;
	List<GenericMove> lastPc;
	
	private  static final int SEARCH_DEPTH_IN_PLY = 4;
	
	byte currPly; 
	
	TranspositionTableAccessor sut;
	TranspositionEvaluation eval;
	
	@Before
	public void setUp() throws Exception {
		transTable = new FixedSizeTranspositionTable();
		SearchDebugAgent sda = new SearchDebugAgent(0, true);
		st = new ScoreTracker(SEARCH_DEPTH_IN_PLY, true, sda);
		st.setProvisionalScoreAtPly((byte) 0);
		pm = new PositionManager();
		sut = new TranspositionTableAccessor(transTable, pm, st, sda);
		currPly = 0;
	}

	@Test
	public void testEval_WhenEmpty_insufficientNoData() {
		eval = sut.getTransposition(currPly, SEARCH_DEPTH_IN_PLY);
		assertEquals(TranspositionTableStatus.insufficientNoData, eval.status);
	}
	
	@Test
	public void testEval_StoreRetrieve_sufficientTerminalNode() throws InvalidPieceException, IllegalNotationException {
		List<GenericMove> pc = new ArrayList<GenericMove>();
		pc.add(new GenericMove("e2e4"));
		
		sut.setTransposition(null, (byte)1, (short)105, Score.exact, Move.toMove(pc.get(0), pm.getTheBoard()));
		
		eval = sut.getTransposition(currPly, 1);
		
		assertEquals(TranspositionTableStatus.sufficientTerminalNode, eval.status);
	}
	
	@Test
	public void testEval_StoreRetrieve_sufficientSeedMoveList() throws InvalidPieceException, IllegalNotationException {
		List<GenericMove> pc = new ArrayList<GenericMove>();
		pc.add(new GenericMove("e2e4"));
		
		sut.setTransposition(null, (byte)1, (short)105, Score.exact, Move.toMove(pc.get(0)));
		
		eval = sut.getTransposition(currPly, 2);
		
		assertEquals(TranspositionTableStatus.sufficientSeedMoveList, eval.status);
	}
	
	@Test
	@Ignore
	public void testEval_StoreRetrieve_whenNoMoveList_insufficientNoData() throws InvalidPieceException, IllegalNotationException {
		List<GenericMove> pc = new ArrayList<GenericMove>();
		
		sut.setTransposition(null, (byte)1, (short)105, Score.exact, Move.toMove(pc.get(0)));
		
		eval = sut.getTransposition(currPly, 2);
		
		assertEquals(TranspositionTableStatus.insufficientNoData, eval.status);
	}
	
	@Test
	public void testEval_StoreRetrieve_whenUpperBound_AndScoreIsLower_inSufficientRefutation() throws InvalidPieceException, IllegalNotationException {
		List<GenericMove> pc = new ArrayList<GenericMove>();
		pc.add(new GenericMove("e2e4"));

		currPly = 3;
		sut.setTransposition(null, (byte)1, (short)18, Score.upperBound, Move.toMove(pc.get(0)));
		
		// Set up score tracker according to diagram
		st.setBackedUpScoreAtPly((byte)0, (short)12);
		st.setProvisionalScoreAtPly((byte)1);
		st.setProvisionalScoreAtPly((byte)2);
		st.setProvisionalScoreAtPly((byte)3);
		eval = sut.getTransposition(currPly, 1);
		
		assertEquals(TranspositionTableStatus.sufficientSeedMoveList, eval.status);
	}
	
	@Test
	@Ignore
	public void testEval_StoreRetrieve_whenLowerBound_AndScoreIsHigher_sufficientRefutation() throws InvalidPieceException, IllegalNotationException {
		/* Example from second limb of search tree, fig9.15, pg.178, How Computers Play Chess, Newborn and Levy */
		List<GenericMove> pc = new ArrayList<GenericMove>();
		pc.add(new GenericMove("e2e4"));

		currPly = 3;
		sut.setTransposition(null, (byte)1, (short)18, Score.upperBound, Move.toMove(pc.get(0), pm.getTheBoard()));
		
		// Set up score tracker according to diagram
		st.setBackedUpScoreAtPly((byte)0, Score.valueOf((short)12, Score.upperBound));
		st.setProvisionalScoreAtPly((byte)1);
		st.setProvisionalScoreAtPly((byte)2);
		st.setProvisionalScoreAtPly((byte)3);
		st.setBackedUpScoreAtPly((byte)3, Score.valueOf((short)40, Score.exact));
		st.setBackedUpScoreAtPly((byte)2, Score.valueOf((short)40, Score.exact));
		st.setProvisionalScoreAtPly((byte)3);
		eval = sut.getTransposition(currPly, 1);
		
		assertEquals(TranspositionTableStatus.sufficientRefutation, eval.status);
	}
	
	@Test
	public void testUpdateWorks_whenNew() throws IllegalNotationException {
		List<GenericMove> pc = new ArrayList<GenericMove>();
		pc.add(new GenericMove("e2e4"));

		currPly = 2;
		sut.setTransposition(null, (byte)1, (short)105, Score.lowerBound, Move.toMove(pc.get(0)));
	}
	
	@Test
	public void testUpdateWorks_whenExistingUpdated() throws IllegalNotationException {
		GenericMove move1 = new GenericMove("e2e4");
		GenericMove move2 = new GenericMove("d2d4");
		List<GenericMove> pc = new ArrayList<GenericMove>();
		pc.add(move1);

		currPly = 2;
		ITransposition stored_trans = sut.setTransposition(null, (byte)1, (short)105, Score.lowerBound, Move.toMove(move1, pm.getTheBoard()));
		
		stored_trans = sut.setTransposition(stored_trans, (byte)1, (short)110, Score.exact, Move.toMove(move2, pm.getTheBoard()));
		
		assertEquals(Score.exact, stored_trans.getType());
		assertEquals(110, stored_trans.getScore());
		
		// check move list order is updated
		assertTrue(Move.areEqual(Move.toMove(move2), stored_trans.getBestMove()));
		
		// Check eval returns expected hash data
		eval = sut.getTransposition(currPly, 1);
		assertEquals(stored_trans, eval.trans);
		assertTrue(Move.areEqual(Move.toMove(move2), eval.trans.getBestMove()));
	}
	
	@Test
	public void testUpdateWorks_whenExistingUpdated_ArenaError() throws IllegalNotationException {
		pm = new PositionManager("8/8/p6p/1p3kp1/1P6/P4PKP/5P2/8 w - - 0 1"); //Endgame pos
		sut = new TranspositionTableAccessor(transTable, pm, st, new SearchDebugAgent(0, true));
		GenericMove move1 = new GenericMove("h3h4");
		GenericMove move2 = new GenericMove("f3f4");
		
		List<GenericMove> pc = new ArrayList<GenericMove>();
		pc.add(move1);
		
		currPly = 0;
		ITransposition stored_trans = sut.setTransposition(null, (byte)9, (short)25, Score.lowerBound, Move.toMove(move1));
		
		stored_trans = sut.setTransposition(stored_trans, (byte)9, (short)72, Score.lowerBound, Move.toMove(move2));
		
		assertEquals(Score.lowerBound, stored_trans.getType());
		assertEquals(72, stored_trans.getScore());
		
		// check move list order is updated
		assertTrue(Move.areEqual(Move.toMove(move2), stored_trans.getBestMove()));
		
		// Check eval returns expected hash data
		eval = sut.getTransposition(currPly, 1);
		assertEquals(stored_trans, eval.trans);
		assertTrue(Move.areEqual(Move.toMove(move2), eval.trans.getBestMove()));
	}
}
