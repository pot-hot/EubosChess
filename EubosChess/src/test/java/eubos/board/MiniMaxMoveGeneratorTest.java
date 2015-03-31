package eubos.board;

import static org.junit.Assert.*;

import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

import com.fluxchess.jcpi.models.GenericChessman;
import com.fluxchess.jcpi.models.GenericMove;
import com.fluxchess.jcpi.models.GenericPosition;

import eubos.pieces.Bishop;
import eubos.pieces.King;
import eubos.pieces.Knight;
import eubos.pieces.Pawn;
import eubos.pieces.Piece;
import eubos.pieces.Queen;
import eubos.pieces.Rook;
import eubos.pieces.Piece.Colour;

public class MiniMaxMoveGeneratorTest {
	protected LinkedList<Piece> pl;
	protected MiniMaxMoveGenerator classUnderTest;
	protected GenericMove expectedMove;
	
	@Before
	public void setUp() {
		pl = new LinkedList<Piece>();
	}
	
	private void doFindMoveTest( boolean expectMove ) {
		try {
			GenericMove selectedMove = classUnderTest.findMove();
			if ( expectMove )
				assertTrue(selectedMove.equals(expectedMove));
			else
				assertFalse(selectedMove.equals(expectedMove));
		}
		catch ( NoLegalMoveException e ) {
			fail();
		}
	}

	@Test
	public void test_findMove_WhitePawnCapture() {
		System.out.println("\ntest_findMove_WhitePawnCapture()");
		// 8 ........
		// 7 ........
		// 6 ...P..P.
		// 5 ..p.....
		// 4 ........
		// 3 ........
		// 2 ........
		// 1 ........
		//   abcdefgh
		BoardManager bm = new BoardManager( "8/8/3p2p1/2P5/8/8/8/8 w - - - -" );
		classUnderTest = new MiniMaxMoveGenerator( bm, Colour.white );
		expectedMove = new GenericMove( GenericPosition.c5, GenericPosition.d6 );
		doFindMoveTest(true);
	}	
	
	@Test
	public void test_findMove_BlackPawnCapture() {
		System.out.println("\ntest_findMove_BlackPawnCapture()");
		// 8 ........
		// 7 ...P....
		// 6 ..p.....
		// 5 ........
		// 4 ........
		// 3 ........
		// 2 .....p..
		// 1 ........
		//   abcdefgh
		BoardManager bm = new BoardManager( "8/3p4/2P5/8/8/8/5P2/8 b - - - -" );
		classUnderTest = new MiniMaxMoveGenerator( bm, Colour.black );
		expectedMove = new GenericMove( GenericPosition.d7, GenericPosition.c6 );
		doFindMoveTest(true);
	}
	
	@Test
	public void test_findMove_CaptureToEscapeCheck() throws NoLegalMoveException {
		System.out.println("\ntest_findMove_CaptureToEscapeCheck()");
		// 8 ........
		// 7 ........
		// 6 ........
		// 5 ........
		// 4 ........
		// 3 .P......
		// 2 PPP.....
		// 1 kP......
		//   abcdefgh
		BoardManager bm = new BoardManager( "8/8/8/8/8/1p6/ppp5/Kp6 w - - - -" );
		classUnderTest = new MiniMaxMoveGenerator( bm, Colour.white );
		expectedMove = new GenericMove( GenericPosition.a1, GenericPosition.b2 );
		doFindMoveTest(true);			
	}
	
	@Test
	public void test_findMove_MoveToEscapeCheck() throws NoLegalMoveException {
		System.out.println("\ntest_findMove_MoveToEscapeCheck()");
		// 8 ........
		// 7 ........
		// 6 ........
		// 5 ........
		// 4 ........
		// 3 .PP.....
		// 2 .P......
		// 1 k.......
		//   abcdefgh
		BoardManager bm = new BoardManager( "8/8/8/8/8/1pp5/1p6/K7 w - - - -" );
		classUnderTest = new MiniMaxMoveGenerator( bm, Colour.white );
		expectedMove = new GenericMove( GenericPosition.a1, GenericPosition.b1 );
		doFindMoveTest(true);
	}
	
	@Test(expected=NoLegalMoveException.class)
	public void test_findMove_NoLegalMove() throws NoLegalMoveException {
		System.out.println("\ntest_findMove_NoLegalMove()");
		// 8 ........
		// 7 ........
		// 6 ........
		// 5 ........
		// 4 ........
		// 3 .PP.....
		// 2 PPP.....
		// 1 kP......
		//   abcdefgh
		BoardManager bm = new BoardManager( "8/8/8/8/8/1pp5/ppp5/Kp6 w - - - -" );
		classUnderTest = new MiniMaxMoveGenerator( bm, Colour.white );
		classUnderTest.findMove();
	}
	
	@Test
	public void test_findMove_ArenaFailKingMove() throws NoLegalMoveException {
		System.out.println("\ntest_findMove_ArenaFailKingMove()");
		// 8 ..b.q...
		// 7 ......K.
		// 6 ..q.....
		// 5 p.....b.
		// 4 ....p...
		// 3 .p...npn
		// 2 ........
		// 1 ..kr...r
		//   abcdefgh
		BoardManager bm = new BoardManager( "2B1Q3/6k1/2Q5/P5B1/4P3/1P3NPN/8/2KR3R b - - - -" );
		classUnderTest = new MiniMaxMoveGenerator( bm, Colour.black );
		expectedMove = new GenericMove( GenericPosition.g7, GenericPosition.h7 );
		doFindMoveTest(true);
	}
	
	@Test
	public void test_findMove_ArenaFailKingInCheck() throws NoLegalMoveException {
		System.out.println("\ntest_findMove_ArenaFailKingInCheck()");
		// 8 ...NKBNR
		// 7 ...P.PPP
		// 6 ........
		// 5 .b.P....
		// 4 r..n....
		// 3 ........
		// 2 ......pp
		// 1 ....r.k.
		//   abcdefgh
		BoardManager bm = new BoardManager( "3nkbnr/3p1ppp/8/1B1p4/R2N4/8/6PP/4R1K1 b - - - -" );
		classUnderTest = new MiniMaxMoveGenerator( bm, Colour.black );
		expectedMove = new GenericMove( GenericPosition.g8, GenericPosition.e7 );
		doFindMoveTest(true);
	}	
	
	@Test
	public void test_findMove_ChooseHighestValueCapture() throws NoLegalMoveException {
		System.out.println("\ntest_findMove_ChooseHighestValueCapture()");
		// 8 ........
		// 7 .....Q..
		// 6 ...Pp...
		// 5 ..p.....
		// 4 .B......
		// 3 p.......
		// 2 ........
		// 1 ........
		//   abcdefgh
		BoardManager bm = new BoardManager( "8/5q2/3pP3/2P5/1b6/P7/8/8 w - - - -" );
		classUnderTest = new MiniMaxMoveGenerator( bm, Colour.white );
		expectedMove = new GenericMove( GenericPosition.e6, GenericPosition.f7 );
		doFindMoveTest(true);
	}
	
	@Test
	//@Ignore
	public void test_findMove_ChooseHighestValueCaptureAndPromotion() throws NoLegalMoveException {
		System.out.println("\ntest_findMove_ChooseHighestValueCaptureAndPromotion()");
		// 8 .....Q..
		// 7 ....p...
		// 6 ...P....
		// 5 ..p.....
		// 4 .B......
		// 3 p.......
		// 2 ........
		// 1 ........
		//   abcdefgh
		BoardManager bm = new BoardManager( "5q2/4P3/3p4/2P5/1b6/P7/8/8 w - - - -" );
		classUnderTest = new MiniMaxMoveGenerator( bm, Colour.white );
		expectedMove = new GenericMove( GenericPosition.e7, GenericPosition.f8, GenericChessman.QUEEN );
		doFindMoveTest(true);
	}
	
	@Test
	public void test_findMove_pawnPromotion()  throws NoLegalMoveException {
		System.out.println("\ntest_findMove_pawnPromotion()");
		// 8 ........
		// 7 ....p...
		// 6 ...P....
		// 5 ........
		// 4 ........
		// 3 ........
		// 2 ........
		// 1 ........
		//   abcdefgh
		BoardManager bm = new BoardManager( "8/4P3/3p4/8/8/8/8/8 w - - - -" );
		classUnderTest = new MiniMaxMoveGenerator( bm, Colour.white );
		expectedMove = new GenericMove( GenericPosition.e7, GenericPosition.e8, GenericChessman.QUEEN );
		doFindMoveTest(true);
	}
	
	@Test
	public void test_findMove_pinnedPawn1()  throws NoLegalMoveException {
		System.out.println("\ntest_findMove_pinnedPawn()");
		// 8 ....K...
		// 7 ........
		// 6 ....P...
		// 5 .....b..
		// 4 ........
		// 3 ........
		// 2 ........
		// 1 ....r...
		//   abcdefgh
		BoardManager bm = new BoardManager( "4k3/8/4p3/5b2/8/8/8/4R3 b - - - -" );
		classUnderTest = new MiniMaxMoveGenerator( bm, Colour.black );
		expectedMove = new GenericMove( GenericPosition.e6, GenericPosition.f5 );
		doFindMoveTest(false);
	}

	@Test
	public void test_findMove_pinnedPawn2()  throws NoLegalMoveException {
		// Observed to produce an illegal move exception in Arena testing, 29th March 2015.
		System.out.println("\ntest_findMove_pinnedPawn2()");
		// 8 .NBQK..R
		// 7 ...P...P
		// 6 R.PBPN..
		// 5 .P...b..
		// 4 ...p....
		// 3 pqp..n..
		// 2 .....ppp
		// 1 r...r.k.
		//   abcdefgh
		BoardManager bm = new BoardManager( "1nbqk2r/3p3p/r1pbpn2/1p3B2/3P4/PQP2N2/5PPP/R3R1K1 w - - - -" );
		classUnderTest = new MiniMaxMoveGenerator( bm, Colour.black );
		expectedMove = new GenericMove( GenericPosition.e6, GenericPosition.f5 );
		doFindMoveTest(false);
	}
}
