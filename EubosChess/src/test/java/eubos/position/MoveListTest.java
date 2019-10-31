package eubos.position;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import com.fluxchess.jcpi.models.GenericMove;
import com.fluxchess.jcpi.models.IllegalNotationException;

import eubos.board.InvalidPieceException;
import eubos.position.PositionManager;

public class MoveListTest {

	public static final boolean EXTENDED = true;
	public static final boolean NORMAL = false;
	
	protected MoveList classUnderTest;
	
	private void setup(String fen) {
		PositionManager pm = new PositionManager( fen );
		classUnderTest = new MoveList(pm);
	}
	
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testLegalMoveListGenerator() {
		classUnderTest = new MoveList(new PositionManager());
	}

	@Test
	public void testCreateMoveList() {
		setup("8/8/8/8/8/1pp5/ppp5/Kp6 w - - - -"); // is_stalemate
		assertFalse(classUnderTest.iterator().hasNext());		
	}
	
	@Test
	public void testCreateMoveList_CapturesFirstThenChecks() throws InvalidPieceException, IllegalNotationException {
		setup("8/3k3B/8/1p6/2P5/8/4K3/8 w - - 0 1 ");
		Iterator<GenericMove> it = classUnderTest.iterator();
		assertEquals(new GenericMove("c4b5"), it.next());
		assertEquals(new GenericMove("h7f5"), it.next());
	}
	
	@Test
	public void testCreateMoveList_ChecksFirst() throws InvalidPieceException, IllegalNotationException {
		setup( "8/3k3B/8/8/8/8/4K3/8 w - - 0 1");
		Iterator<GenericMove> it = classUnderTest.iterator();
		assertEquals(new GenericMove("h7f5"), it.next());
	}
	
	@Test
	public void testCreateMoveList_CastlesFirstThenChecks() throws InvalidPieceException, IllegalNotationException {
		setup("8/3k3B/8/1p6/8/8/8/4K2R w K - 0 1");
		Iterator<GenericMove> it = classUnderTest.iterator();
		assertEquals(new GenericMove("e1g1"), it.next());
		assertEquals(new GenericMove("h7f5"), it.next());
	}
	
	@Test
	public void test_setBestMove() throws IllegalNotationException {
		GenericMove expected = new GenericMove("g3f2"); 
		setup("8/8/4n1p1/1R3p1p/3k3P/2rB2K1/2P3P1/8 w - - 15 51");
		Iterator<GenericMove> it = classUnderTest.iterator();
		assertNotEquals(expected, it.next());
		classUnderTest.reorderWithNewBestMove(expected);
		it = classUnderTest.iterator();
		assertEquals(expected, it.next());
	}
	
	@Test
	public void test_whenNoChecksCapturesOrPromotions() throws IllegalNotationException { 
		setup("8/3p4/8/8/8/5k2/1P6/7K w - - 0 1");
		Iterator<GenericMove> iter = classUnderTest.getIterator(EXTENDED);
		assertFalse(iter.hasNext());
		iter = classUnderTest.getIterator(NORMAL);
		assertTrue(iter.hasNext());
	}
	
	@Test
	public void test_whenChangedBestCapture_BothIteratorsAreUpdated() throws IllegalNotationException {
		setup("8/1B6/8/3q1r2/4P3/8/8/8 w - - 0 1");
		Iterator<GenericMove> it = classUnderTest.getIterator(NORMAL);
		GenericMove first = it.next();
		
		GenericMove newBestCapture = new GenericMove("e4f5");
		assertNotEquals(first, newBestCapture);
		
		classUnderTest.reorderWithNewBestMove(newBestCapture);
		
		Iterator<GenericMove> iter = classUnderTest.getIterator(NORMAL);
		assertTrue(iter.hasNext());
		assertEquals(newBestCapture, iter.next());
		
		iter = classUnderTest.getIterator(EXTENDED);
		assertTrue(iter.hasNext());
		assertEquals(newBestCapture, iter.next());
	}
	
	@Test
	public void test_whenCheckAndCapturePossible() throws IllegalNotationException {
		setup("8/K7/8/8/4B1R1/8/6q1/7k w - - 0 1 ");
		Iterator<GenericMove> it = classUnderTest.iterator();
		assertEquals(new GenericMove("e4g2"), it.next()); // Check and capture
		assertEquals(new GenericMove("g4g2"), it.next()); // capture
		assertEquals(new GenericMove("g4h4"), it.next()); // check
	}
	
	@Test
	public void test_whenPromotionAndPromoteWithCaptureAndCheckPossible() throws IllegalNotationException {
		setup("q1n5/1P6/8/8/8/8/1K6/7k w - - 0 1 ");
		Iterator<GenericMove> it = classUnderTest.iterator();
		assertEquals(new GenericMove("b7a8q"), it.next()); // Promotion with check and capture
		assertEquals(new GenericMove("b7c8q"), it.next()); // Promotion and capture
		assertEquals(new GenericMove("b7b8q"), it.next()); // Promotion
	}
	
	@Test
	public void test_ChangingBestMove() {
		setup("8/8/5P2/4P3/3P4/2P5/8/8 w - - 0 1");
		Iterator<GenericMove> it = classUnderTest.iterator();
		GenericMove [] original_moves = new GenericMove[4];
		for (int i=0; i<original_moves.length; i++)
			original_moves[i] = it.next();
		
		// Set new best move
		classUnderTest.reorderWithNewBestMove(original_moves[3]);
		it = classUnderTest.iterator();
		GenericMove [] reordered_moves = new GenericMove[4];
		for (int i=0; i<reordered_moves.length; i++)
			reordered_moves[i] = it.next();
		assertEquals(reordered_moves[0], original_moves[3]);
		assertEquals(reordered_moves[3], original_moves[0]);
		
		// Revert change
		classUnderTest.reorderWithNewBestMove(reordered_moves[3]);
		it = classUnderTest.iterator();
		reordered_moves = new GenericMove[4];
		for (int i=0; i<reordered_moves.length; i++)
			reordered_moves[i] = it.next();
		assertEquals(reordered_moves[0], original_moves[0]);
		assertEquals(reordered_moves[3], original_moves[3]);
		
		// Set second move as best
		classUnderTest.reorderWithNewBestMove(reordered_moves[1]);
		it = classUnderTest.iterator();
		reordered_moves = new GenericMove[4];
		for (int i=0; i<reordered_moves.length; i++)
			reordered_moves[i] = it.next();
		assertEquals(reordered_moves[0], original_moves[1]);
		assertEquals(reordered_moves[1], original_moves[0]);
		
		// Revert change
		classUnderTest.reorderWithNewBestMove(reordered_moves[1]);
		it = classUnderTest.iterator();
		reordered_moves = new GenericMove[4];
		for (int i=0; i<reordered_moves.length; i++)
			reordered_moves[i] = it.next();
		assertEquals(reordered_moves[0], original_moves[0]);
		assertEquals(reordered_moves[1], original_moves[1]);
		assertEquals(reordered_moves[2], original_moves[2]);
		assertEquals(reordered_moves[3], original_moves[3]);
		
		// Set third move as best
		classUnderTest.reorderWithNewBestMove(reordered_moves[2]);
		it = classUnderTest.iterator();
		reordered_moves = new GenericMove[4];
		for (int i=0; i<reordered_moves.length; i++)
			reordered_moves[i] = it.next();
		assertEquals(reordered_moves[0], original_moves[2]);
		assertEquals(reordered_moves[1], original_moves[1]);
		assertEquals(reordered_moves[2], original_moves[0]);
		assertEquals(reordered_moves[3], original_moves[3]);
		
		// Set fourth move as best
		classUnderTest.reorderWithNewBestMove(original_moves[3]);
		it = classUnderTest.iterator();
		reordered_moves = new GenericMove[4];
		for (int i=0; i<reordered_moves.length; i++)
			reordered_moves[i] = it.next();
		assertEquals(reordered_moves[0], original_moves[3]);
		assertEquals(reordered_moves[1], original_moves[1]);
		assertEquals(reordered_moves[2], original_moves[2]);
		assertEquals(reordered_moves[3], original_moves[0]);
		
		// Set fourth move as best
		classUnderTest.reorderWithNewBestMove(original_moves[3]);
		it = classUnderTest.iterator();
		reordered_moves = new GenericMove[4];
		for (int i=0; i<reordered_moves.length; i++)
			reordered_moves[i] = it.next();
		assertEquals(reordered_moves[0], original_moves[0]);
		assertEquals(reordered_moves[1], original_moves[1]);
		assertEquals(reordered_moves[2], original_moves[2]);
		assertEquals(reordered_moves[3], original_moves[3]);
		
		// Set fourth move as best
		classUnderTest.reorderWithNewBestMove(original_moves[2]);
		it = classUnderTest.iterator();
		reordered_moves = new GenericMove[4];
		for (int i=0; i<reordered_moves.length; i++)
			reordered_moves[i] = it.next();
		assertEquals(reordered_moves[0], original_moves[2]);
		assertEquals(reordered_moves[1], original_moves[1]);
		assertEquals(reordered_moves[2], original_moves[0]);
		assertEquals(reordered_moves[3], original_moves[3]);	
	}
	
	@Test
	public void test_ChangingBestMove_WhenSeeded() throws IllegalNotationException {
		PositionManager pm = new PositionManager( "8/8/5P2/4P3/3P4/2P5/8/8 w - - 0 1" );
		classUnderTest = new MoveList(pm, new GenericMove("f6f7")); // seed with last move as best
		Iterator<GenericMove> it = classUnderTest.iterator();
		GenericMove [] original_moves = new GenericMove[4];
		for (int i=0; i<original_moves.length; i++)
			original_moves[i] = it.next();
		System.out.println("original - seeded last as best");
		System.out.println(Arrays.toString(original_moves));
		
		// Set new best move as second move
		classUnderTest.reorderWithNewBestMove(original_moves[2]);
		it = classUnderTest.iterator();
		GenericMove [] reordered_moves = new GenericMove[4];
		for (int i=0; i<reordered_moves.length; i++)
			reordered_moves[i] = it.next();
		
		System.out.println("3rd as best");
		System.out.println(Arrays.toString(reordered_moves));
		assertEquals(original_moves[2], reordered_moves[0]);
		assertEquals(original_moves[0], reordered_moves[2]);
		
		// Revert change
		System.out.println("revert");
		classUnderTest.reorderWithNewBestMove(reordered_moves[2]);
		it = classUnderTest.iterator();
		reordered_moves = new GenericMove[4];
		for (int i=0; i<reordered_moves.length; i++)
			reordered_moves[i] = it.next();
		System.out.println(Arrays.toString(reordered_moves));
		assertEquals(reordered_moves[0], original_moves[0]);
		assertEquals(reordered_moves[2], original_moves[2]);
		assertEquals(reordered_moves[3], original_moves[3]);
	}
}
