package eubos.board;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

import com.fluxchess.jcpi.models.GenericMove;
import com.fluxchess.jcpi.models.GenericPosition;

import eubos.pieces.King;
import eubos.pieces.Pawn;
import eubos.pieces.Piece;
import eubos.pieces.Piece.Colour;

public class MoveGeneratorTest {
	
	protected LinkedList<Piece> pl;
	protected MoveGenerator classUnderTest;
	protected GenericMove expectedMove;
	
	@Before
	public void setUp() {
		pl = new LinkedList<Piece>();
	}
	
	private void performTest( boolean assertSense ) {
		try {
			GenericMove selectedMove = classUnderTest.findBestMove();
			if ( assertSense )
				assertTrue(selectedMove.equals(expectedMove));
			else
				assertFalse(selectedMove.equals(expectedMove));
		}
		catch ( NoLegalMoveException e ) {
			assert( false );
		}
	}
	
	@Test
	public void test_findBestMove_DoNotMoveIntoCheck() {
		pl.add(new King( Colour.black, GenericPosition.a8 ));
		pl.add(new Pawn( Colour.white, GenericPosition.c6 ));
		BoardManager bm = new BoardManager( new Board( pl ));
		classUnderTest = new MoveGenerator( bm, Colour.black );
		expectedMove = new GenericMove( GenericPosition.a8, GenericPosition.b7 );
		performTest(false);
	}
	
	@Test
	public void test_findBestMove_CaptureToEscapeCheck() throws NoLegalMoveException {
		pl.add(new King( Colour.white, GenericPosition.a1 ));
		pl.add(new Pawn( Colour.black, GenericPosition.b1 ));
		pl.add(new Pawn( Colour.black, GenericPosition.a2 ));
		pl.add(new Pawn( Colour.black, GenericPosition.b2 ));
		pl.add(new Pawn( Colour.black, GenericPosition.c2 ));
		pl.add(new Pawn( Colour.black, GenericPosition.b3 ));
		// pawn at b2 can be captured to escape check
		BoardManager bm = new BoardManager( new Board( pl ));
		classUnderTest = new MoveGenerator( bm, Colour.white );
		expectedMove = new GenericMove( GenericPosition.a1, GenericPosition.b2 );
		performTest(true);			
	}
	
	@Test
	public void test_findBestMove_MoveToEscapeCheck() throws NoLegalMoveException {
		pl.add(new King( Colour.white, GenericPosition.a1 ));
		pl.add(new Pawn( Colour.black, GenericPosition.b2 ));
		pl.add(new Pawn( Colour.black, GenericPosition.b3 ));
		pl.add(new Pawn( Colour.black, GenericPosition.c3 ));
		// king can move out of check to b1
		BoardManager bm = new BoardManager( new Board( pl ));
		classUnderTest = new MoveGenerator( bm, Colour.white );
		expectedMove = new GenericMove( GenericPosition.a1, GenericPosition.b1 );
		performTest(true);
	}
	
	@Test(expected=NoLegalMoveException.class)
	public void test_findBestMove_NoLegalMove() throws NoLegalMoveException {
		pl.add(new King( Colour.white, GenericPosition.a1 ));
		pl.add(new Pawn( Colour.black, GenericPosition.b1 ));
		pl.add(new Pawn( Colour.black, GenericPosition.a2 ));
		pl.add(new Pawn( Colour.black, GenericPosition.b2 ));
		pl.add(new Pawn( Colour.black, GenericPosition.c2 ));
		pl.add(new Pawn( Colour.black, GenericPosition.b3 ));
		pl.add(new Pawn( Colour.black, GenericPosition.c3 ));
		BoardManager bm = new BoardManager( new Board( pl ));
		classUnderTest = new MoveGenerator( bm, Colour.white );
		classUnderTest.findBestMove();
	}
}