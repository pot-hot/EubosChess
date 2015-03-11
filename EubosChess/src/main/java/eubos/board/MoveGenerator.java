package eubos.board;

import java.util.LinkedList;
import java.util.Random;
import java.util.Iterator;

import com.fluxchess.jcpi.models.GenericMove;

import eubos.pieces.Piece;
import eubos.pieces.King;

public class MoveGenerator implements IMoveGenerator {
	
	private BoardManager bm;
	private Piece.Colour onMove;
	
	public MoveGenerator( BoardManager bm, Piece.Colour sideToMove ) {
		this.bm = bm;
		this.onMove = sideToMove;
	}

	// TODO: for now find a random legal move for the side indicated
	public GenericMove findBestMove() throws NoLegalMoveException {
		// Generate the entire move list
		GenericMove bestMove = null;
		LinkedList<GenericMove> entireMoveList = new LinkedList<GenericMove>();
		// For each piece of the side to move on the board...
		Iterator<Piece> iter_p = bm.getTheBoard().iterateColour(onMove);
		while ( iter_p.hasNext() ) {
			// ...append the piece's legal moves to the entire move list
			entireMoveList.addAll( iter_p.next().generateMoves( bm ));
		}
		// once the move list has been generated, remove any moves that would place
		// the king in check from consideration.
		Iterator<GenericMove> iter_ml = entireMoveList.iterator();
		while ( iter_ml.hasNext() ) {
			GenericMove currMove = iter_ml.next();
			bm.performMove( currMove );
			if (inCheck()) {
				iter_ml.remove();
			}
			bm.undoPreviousMove();
		}
		if ( !entireMoveList.isEmpty()) {
			// For the time-being, return a valid move at random
			Random randomIndex = new Random();
			Integer indexToGet = randomIndex.nextInt(entireMoveList.size());
			bestMove = entireMoveList.get(indexToGet);			
		} else {
			throw new NoLegalMoveException();
		}
		return bestMove;
	}
	
	private boolean inCheck() {
		// loop through all the opposite colour pieces and see if any of them are currently attacking the king.
		boolean inCheck = false;
		King ownKing = bm.getKing(onMove);
		if ( ownKing != null ) {
			Iterator<Piece> iterPotentialAttackers = bm.getTheBoard().iterateColour(Piece.Colour.getOpposite(onMove));
			while (iterPotentialAttackers.hasNext()) {
				Piece currPiece = iterPotentialAttackers.next();
				if (currPiece.attacks(ownKing.getSquare())) {
					inCheck = true;
					break;
				}
			}
		}
		return inCheck;
	}
}
