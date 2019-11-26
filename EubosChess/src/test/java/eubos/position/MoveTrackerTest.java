package eubos.position;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.fluxchess.jcpi.models.GenericMove;
import com.fluxchess.jcpi.models.GenericPosition;
import com.fluxchess.jcpi.models.IllegalNotationException;

import eubos.board.pieces.Piece.PieceType;
import eubos.position.MoveTracker;
import eubos.position.CaptureData;
import eubos.position.TrackedMove;

public class MoveTrackerTest {
	
	private MoveTracker classUnderTest;

	@Before
	public void setUp() throws Exception {
		classUnderTest = new MoveTracker();
	}

	@Test
	public void testMoveTracker() {
		assertTrue(classUnderTest!=null);
	}

	@Test
	public void testLastMoveWasCapture_NoTrackedMoves() {
		assertFalse(classUnderTest.lastMoveWasCapture());
	}

	@Test
	public void testLastMoveWasCapture_NotACapture() throws IllegalNotationException {
		TrackedMove tm = new TrackedMove(new GenericMove("a2a4"));
		classUnderTest.add(tm);		
		assertFalse(classUnderTest.lastMoveWasCapture());
	}

	@Test
	public void testLastMoveWasCapture_WasACapture() throws IllegalNotationException {
		CaptureData cap = new CaptureData(PieceType.BlackPawn, GenericPosition.b3);
		TrackedMove tm = new TrackedMove(new GenericMove("a2b3"), cap, null, "K");
		classUnderTest.add(tm);		
		assertTrue(classUnderTest.lastMoveWasCapture());
	}
}
