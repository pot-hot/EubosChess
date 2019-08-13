package eubos.position;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fluxchess.jcpi.models.GenericMove;

import eubos.board.InvalidPieceException;
import eubos.board.pieces.Piece;
import eubos.board.pieces.Piece.Colour;

class MoveListGenerator {
	
	private PositionManager pm;
	
	MoveListGenerator( PositionManager pm ) {
		this.pm = pm;
	}
	
	private List<GenericMove> getAllPossibleMoves() {
		List<GenericMove> entireMoveList = new ArrayList<GenericMove>();
		// For each piece of the "on Move" colour, add it's legal moves to the entire move list
		Iterator<Piece> iter_p = pm.getTheBoard().iterateColour(pm.getOnMove());
		while ( iter_p.hasNext() ) {
			Piece currPiece = iter_p.next();
			entireMoveList.addAll( currPiece.generateMoves( pm.getTheBoard() ));
		}
		pm.castling.addCastlingMoves(entireMoveList);
		return entireMoveList;
	}
	
	List<GenericMove> createMoveList() throws InvalidPieceException {
		List<GenericMove> entireMoveList = getAllPossibleMoves();
		List<GenericMove> newMoveList = new ArrayList<GenericMove>();
		Colour onMove = pm.getOnMove();
		
		Iterator<GenericMove> iter_ml = entireMoveList.iterator();
		while ( iter_ml.hasNext() ) {
			GenericMove currMove = iter_ml.next();
			pm.performMove(currMove);
			// Scratch any moves resulting in the king being in check
			if (pm.isKingInCheck(onMove))
				iter_ml.remove();
			// Groom the movelist so that the moves expected to be best are searched first.
			// This is to get max benefit form alpha beta algorithm
			else if (pm.lastMoveWasCaptureOrCastle()) {
				newMoveList.add(0, currMove);
			} else {
				newMoveList.add(currMove);
			}
			pm.unperformMove();
		}
		entireMoveList = newMoveList;
		return entireMoveList;
	}
	
	List<GenericMove> getCheckAndCaptureMovesOnly() throws InvalidPieceException {
		List<GenericMove> entireMoveList = getAllPossibleMoves();
		Iterator<GenericMove> iter_ml = entireMoveList.iterator();
		List<GenericMove> justCheckAndCaptures = new ArrayList<GenericMove>();
		Colour colourOfOppositeKing = Colour.getOpposite(pm.getOnMove());
		Colour ownColour = Colour.getOpposite(colourOfOppositeKing);
		while ( iter_ml.hasNext() ) {
			GenericMove currMove = iter_ml.next();
			pm.performMove(currMove);
			// Scratch any moves resulting in the king being in check
			if (pm.isKingInCheck(ownColour))
				iter_ml.remove();
			// Add only checks and captures
			if (pm.lastMoveWasCapture()) {
				justCheckAndCaptures.add(currMove);
			} else if (pm.isKingInCheck(colourOfOppositeKing)) {
				justCheckAndCaptures.add(currMove);
			}
			pm.unperformMove();
		}
		return justCheckAndCaptures;
	}
}
