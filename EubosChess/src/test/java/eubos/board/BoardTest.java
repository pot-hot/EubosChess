package eubos.board;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.fluxchess.jcpi.models.GenericPosition;
import com.fluxchess.jcpi.models.IllegalNotationException;

import eubos.position.Move;
import eubos.position.Position;
import eubos.position.PositionManager;
import eubos.search.DrawChecker;

public class BoardTest {
	
	private Board classUnderTest;
	private Map<Integer, Integer> pieceMap;
	private static final int testSq = Position.a1;
	
	@Before
	public void setUp() throws Exception {
		pieceMap = new HashMap<Integer, Integer>();
		classUnderTest = new Board(pieceMap, Piece.Colour.white);
	}

	@Test
	public void testBoard() {
		assertTrue(classUnderTest!=null);
	}

	@Test
	public void testSetEnPassantTargetSq() {
		classUnderTest.setEnPassantTargetSq( testSq );
	}
	
	@Test
	public void testGetEnPassantTargetSq_uninitialised() {
		int square = classUnderTest.getEnPassantTargetSq();
		assertTrue(square == Position.NOPOSITION);
	}

	@Test
	public void testGetEnPassantTargetSq_initialised() {
		classUnderTest.setEnPassantTargetSq(testSq);
		int square = classUnderTest.getEnPassantTargetSq();
		assertTrue(square == Position.a1);
	}	

	@Test
	public void testSetPieceAtSquare_and_squareIsEmpty() {
		assertTrue(classUnderTest.squareIsEmpty(testSq));
		classUnderTest.setPieceAtSquare(testSq, Piece.WHITE_PAWN);
		assertFalse(classUnderTest.squareIsEmpty(testSq));
	}

	@Test
	public void testPickUpPieceAtSquare_Exists() throws InvalidPieceException {
		assertTrue(classUnderTest.squareIsEmpty(testSq));
		classUnderTest.setPieceAtSquare(testSq, Piece.WHITE_PAWN);
		assertFalse(classUnderTest.squareIsEmpty(testSq));
		int pickedUpPiece = classUnderTest.pickUpPieceAtSquare(testSq);
		assertTrue(classUnderTest.squareIsEmpty(testSq));
		assertEquals(Piece.WHITE_PAWN, pickedUpPiece);
	}
	
	@Test
	public void testPickUpPieceAtSquare_DoesntExist() throws InvalidPieceException {
		assertEquals(Piece.NONE, classUnderTest.pickUpPieceAtSquare(testSq));
	}	

	@Test
	public void testGetPieceAtSquare_Exists() {
		assertTrue(classUnderTest.squareIsEmpty(testSq));
		classUnderTest.setPieceAtSquare(testSq, Piece.WHITE_PAWN);
		assertFalse(classUnderTest.squareIsEmpty(testSq));
		int gotPiece = classUnderTest.getPieceAtSquare(testSq);
		assertFalse(classUnderTest.squareIsEmpty(testSq));
		assertTrue(gotPiece==Piece.BLACK_PAWN || gotPiece==Piece.WHITE_PAWN);
	}
	
	@Test
	public void testGetPieceAtSquare_DoesntExist() {
		assertTrue(classUnderTest.getPieceAtSquare(testSq)==Piece.NONE);
	}
	
	@Test
	public void testCaptureAtSquare() {
		assertTrue(classUnderTest.pickUpPieceAtSquare(testSq)==Piece.NONE);
	}
	
	@Test
	public void testGetAsFenString() {
		classUnderTest.setPieceAtSquare(testSq, Piece.WHITE_PAWN);
		assertEquals("8/8/8/8/8/8/8/P7",classUnderTest.getAsFenString());
	}
	
	@Test
	public void testGetAsFenString1() {
		classUnderTest.setPieceAtSquare(testSq, Piece.WHITE_PAWN);
		classUnderTest.setPieceAtSquare(Position.c1, Piece.WHITE_KING);
		assertEquals("8/8/8/8/8/8/8/P1K5",classUnderTest.getAsFenString());
	}
	
	@Test
	public void testGetAsFenString2() {
		classUnderTest.setPieceAtSquare(Position.h1, Piece.WHITE_PAWN);
		classUnderTest.setPieceAtSquare(Position.g1, Piece.WHITE_KING);
		assertEquals("8/8/8/8/8/8/8/6KP",classUnderTest.getAsFenString());
	}
	
	@Test
	public void testGetAsFenString3() {
		classUnderTest.setPieceAtSquare(Position.h1, Piece.BLACK_PAWN);
		classUnderTest.setPieceAtSquare(Position.g1, Piece.BLACK_KING);
		assertEquals("8/8/8/8/8/8/8/6kp",classUnderTest.getAsFenString());
	}
	
	@Test
	public void testGetAsFenString4() {
		classUnderTest.setPieceAtSquare(Position.h8, Piece.BLACK_PAWN);
		classUnderTest.setPieceAtSquare(Position.g8, Piece.BLACK_KING);
		assertEquals("6kp/8/8/8/8/8/8/8",classUnderTest.getAsFenString());
	}
	
	@Test
	public void testOpenFile_isOpen() {
		classUnderTest.setPieceAtSquare(Position.h8, Piece.BLACK_PAWN);
		assertEquals(14, classUnderTest.getNumRankFileSquaresAvailable(Position.h8));
	}
	
	@Test
	public void testOpenFile_isClosed() {
		classUnderTest.setPieceAtSquare(Position.h7, Piece.BLACK_PAWN);
		classUnderTest.setPieceAtSquare(Position.h2, Piece.WHITE_ROOK);
		assertEquals(9, classUnderTest.getNumRankFileSquaresAvailable(Position.h2));
	}
	
	@Test
	public void testOpenFile_isOpen1() {
		classUnderTest.setPieceAtSquare(Position.d7, Piece.BLACK_PAWN);
		classUnderTest.setPieceAtSquare(Position.e2, Piece.WHITE_ROOK);
		assertEquals(14, classUnderTest.getNumRankFileSquaresAvailable(Position.e2));
	}
	
	@Test
	public void testisHalfOpenFile_isHalfOpen() {
		classUnderTest.setPieceAtSquare(Position.e7, Piece.BLACK_PAWN);
		classUnderTest.setPieceAtSquare(Position.e2, Piece.WHITE_ROOK);
		assertTrue(classUnderTest.isOnHalfOpenFile(GenericPosition.e2, Piece.WHITE_ROOK));
	}
	
	@Test
	public void testisHalfOpenFile_isNotHalfOpen() {
		classUnderTest.setPieceAtSquare(Position.e7, Piece.BLACK_PAWN);
		classUnderTest.setPieceAtSquare(Position.e2, Piece.WHITE_PAWN);
		classUnderTest.setPieceAtSquare(Position.e1, Piece.WHITE_ROOK);
		assertFalse(classUnderTest.isOnHalfOpenFile(GenericPosition.e1, Piece.WHITE_ROOK));
	}
	
	@Test
	@Ignore
	public void testisHalfOpenFile_isNotHalfOpen1() {
		classUnderTest.setPieceAtSquare(Position.e7, Piece.WHITE_PAWN);
		classUnderTest.setPieceAtSquare(Position.e2, Piece.BLACK_PAWN);
		classUnderTest.setPieceAtSquare(Position.e1, Piece.WHITE_ROOK);
		assertTrue(classUnderTest.isOnHalfOpenFile(GenericPosition.e1, Piece.WHITE_ROOK));
	}
	
	@Test
	public void testisOnOpenDiagonal_Yes() {
		classUnderTest.setPieceAtSquare(Position.d5, Piece.BLACK_BISHOP);
		classUnderTest.setPieceAtSquare(Position.e5, Piece.WHITE_PAWN);
		assertEquals(13,classUnderTest.getNumDiagonalSquaresAvailable(Position.d5));
	}
	
	@Test
	public void testisOnOpenDiagonal_No() {
		classUnderTest.setPieceAtSquare(Position.d5, Piece.BLACK_BISHOP);
		classUnderTest.setPieceAtSquare(Position.e6, Piece.WHITE_PAWN);
		assertEquals(0,classUnderTest.getNumDiagonalSquaresAvailable(Position.d5));
	}
	
	@Test
	public void testisOnOpenDiagonal_Yes1() {
		classUnderTest.setPieceAtSquare(Position.d5, Piece.BLACK_BISHOP);
		classUnderTest.setPieceAtSquare(Position.e5, Piece.WHITE_PAWN);
		classUnderTest.setPieceAtSquare(Position.d6, Piece.WHITE_PAWN);
		classUnderTest.setPieceAtSquare(Position.d4, Piece.WHITE_PAWN);
		classUnderTest.setPieceAtSquare(Position.c5, Piece.WHITE_PAWN);
		assertEquals(13,classUnderTest.getNumDiagonalSquaresAvailable(Position.d5));
	}
	
	@Test
	public void testisOnOpenDiagonal_No1() {
		classUnderTest.setPieceAtSquare(Position.a1, Piece.BLACK_BISHOP);
		classUnderTest.setPieceAtSquare(Position.h8, Piece.WHITE_PAWN);
		assertEquals(6,classUnderTest.getNumDiagonalSquaresAvailable(Position.a1));
	}
	
	@Test
	public void testisOnOpenDiagonal_Yes2() {
		classUnderTest.setPieceAtSquare(Position.a1, Piece.BLACK_BISHOP);
		classUnderTest.setPieceAtSquare(Position.a8, Piece.WHITE_PAWN);
		assertEquals(7,classUnderTest.getNumDiagonalSquaresAvailable(Position.a1));
	}
	
	@Test
	public void testCouldLeadToCheck_Yes() {
		classUnderTest.setPieceAtSquare(Position.d2, Piece.WHITE_PAWN);
		classUnderTest.setPieceAtSquare(Position.d1, Piece.WHITE_KING);
		int move = Move.valueOf(Position.d2, Piece.WHITE_PAWN, Position.d4, Piece.NONE);
		assertTrue(classUnderTest.moveCouldLeadToOwnKingDiscoveredCheck(move));
	}
	
	@Test
	public void testCouldLeadToCheck_Yes1() {
		classUnderTest.setPieceAtSquare(Position.e2, Piece.WHITE_PAWN);
		classUnderTest.setPieceAtSquare(Position.d1, Piece.WHITE_KING);
		int move = Move.valueOf(Position.e2, Piece.WHITE_PAWN, Position.d3, Piece.NONE);
		assertTrue(classUnderTest.moveCouldLeadToOwnKingDiscoveredCheck(move));
	}
	
	@Test
	public void testCouldLeadToCheck_No() {
		classUnderTest.setPieceAtSquare(Position.e3, Piece.WHITE_PAWN);
		classUnderTest.setPieceAtSquare(Position.d1, Piece.WHITE_KING);
		int move = Move.valueOf(Position.e3, Piece.WHITE_PAWN, Position.e4, Piece.NONE);
		assertFalse(classUnderTest.moveCouldLeadToOwnKingDiscoveredCheck(move));
	}
	
	@Test
	public void testCouldLeadToCheck_No1() {
		classUnderTest.setPieceAtSquare(Position.d1, Piece.WHITE_KNIGHT);
		classUnderTest.setPieceAtSquare(Position.e4, Piece.WHITE_KING);
		int move = Move.valueOf(Position.d1, Piece.WHITE_KNIGHT, Position.c3, Piece.NONE);
		assertFalse(classUnderTest.moveCouldLeadToOwnKingDiscoveredCheck(move));
	}
	
	protected void setUpPosition(String fen) {
		PositionManager pm = new PositionManager(fen, new DrawChecker());
		classUnderTest = pm.getTheBoard();
	}
	
	@Test
	public void test_isInsufficientMaterial_JustKings() throws InvalidPieceException, IllegalNotationException {
		setUpPosition("8/8/8/8/8/8/k/7K w - - 0 1");
		assertTrue(classUnderTest.isInsufficientMaterial());
	}
	
	@Test
	public void test_isInsufficientMaterial_RookOnBoard() throws InvalidPieceException, IllegalNotationException {
		setUpPosition("8/R7/8/8/8/8/k/7K w - - 0 1");
		assertFalse(classUnderTest.isInsufficientMaterial());
	}
	
	@Test
	public void test_isInsufficientMaterial_QueenOnBoard() throws InvalidPieceException, IllegalNotationException {
		setUpPosition("8/Q7/8/8/8/8/k/7K w - - 0 1");
		assertFalse(classUnderTest.isInsufficientMaterial());
	}
	
	@Test
	public void test_isInsufficientMaterial_TwoKnights() throws InvalidPieceException, IllegalNotationException {
		setUpPosition("8/K7/8/K7/8/8/k/7K w - - 0 1");
		assertTrue(classUnderTest.isInsufficientMaterial());
	}
	
	@Test
	public void test_isInsufficientMaterial_BishopKnight() throws InvalidPieceException, IllegalNotationException {
		setUpPosition("8/B7/8/4N3/8/8/k/7K w - - 0 1");
		assertFalse(classUnderTest.isInsufficientMaterial());
	}
	
	@Test
	public void test_isInsufficientMaterial_BishopKnightDifferentSides() throws InvalidPieceException, IllegalNotationException {
		setUpPosition("8/B7/8/4n3/8/8/k/7K w - - 0 1");
		assertTrue(classUnderTest.isInsufficientMaterial());
	}
	
	@Test
	public void test_isInsufficientMaterial_TwoBishops() throws InvalidPieceException, IllegalNotationException {
		setUpPosition("BB6/8/8/8/8/8/k/7K w - - 0 1");
		assertFalse(classUnderTest.isInsufficientMaterial());
	}
	
	@Test
	public void test_isInsufficientMaterial_TwoBishopsDifferentSides() throws InvalidPieceException, IllegalNotationException {
		setUpPosition("Bb6/8/8/8/8/8/k/7K w - - 0 1");
		assertTrue(classUnderTest.isInsufficientMaterial());
	}
	
	@Test
	public void test_isInsufficientMaterial_SingleBishop() throws InvalidPieceException, IllegalNotationException {
		setUpPosition("B7/8/8/8/8/8/k/7K w - - 0 1");
		assertTrue(classUnderTest.isInsufficientMaterial());
	}
	
	@Test
	public void test_isInsufficientMaterial_PawnOnBoard() throws InvalidPieceException, IllegalNotationException {
		setUpPosition("8/P/8/8/8/8/k/7K w - - 0 1");
		assertFalse(classUnderTest.isInsufficientMaterial());
	}
}
