package eubos.position;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.fluxchess.jcpi.models.IllegalNotationException;
import com.fluxchess.jcpi.models.GenericMove;

import eubos.board.Piece;

public class MoveTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test_good() throws IllegalNotationException {
		int move1 = Move.valueOf(Move.TYPE_REGULAR_NONE, Position.e1, Piece.WHITE_KING, Position.g1, Piece.NONE, Piece.NONE);
		int move2 = Move.toMove(new GenericMove("e1g1"));
		assertTrue(Move.areEqual(move1, move2));
	}
	
	@Test
	public void test_good1() throws IllegalNotationException {
		int move1 = Move.valueOf(Move.TYPE_REGULAR_NONE, Position.e1, Piece.WHITE_KING, Position.g1, Piece.NONE, Piece.NONE);
		int move2 = Move.toMove(new GenericMove("e1g1"));
		assertTrue(Move.areEqual(move1, move2));
	}
	
	@Test
	public void testbad() throws IllegalNotationException {
		int move1 = Move.valueOf(Move.TYPE_REGULAR_NONE, Position.e1, Piece.WHITE_KING, Position.g1, Piece.NONE, Piece.KNIGHT);
		int move2 = Move.toMove(new GenericMove("e1g1"));
		assertFalse(Move.areEqual(move1, move2));
	}
	
	@Test
	public void testbad1() throws IllegalNotationException {
		int move1 = Move.valueOf(Move.TYPE_REGULAR_NONE, Position.g1, Piece.WHITE_KING, Position.e1, Piece.NONE, Piece.NONE);
		int move2 = Move.toMove(new GenericMove("e1g1"));
		assertFalse(Move.areEqual(move1, move2));
	}
}
