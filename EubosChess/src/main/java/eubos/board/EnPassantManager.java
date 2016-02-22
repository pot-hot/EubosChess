package eubos.board;

import com.fluxchess.jcpi.models.GenericMove;
import com.fluxchess.jcpi.models.GenericPosition;
import com.fluxchess.jcpi.models.GenericRank;

import eubos.pieces.Pawn;
import eubos.pieces.Piece;

class EnPassantManager {
	
	private GenericPosition enPassantTargetSq;
	private Board theBoard;

	EnPassantManager(GenericPosition atPos, Board board) {
		theBoard = board;
		setEnPassantTargetSq(atPos);
	}

	GenericPosition getEnPassantTargetSq() {
		return enPassantTargetSq;
	}

	void setEnPassantTargetSq(GenericPosition enPassantTargetSq) {
		// TODO: Currently data is duplicated in the Board!
		this.enPassantTargetSq = enPassantTargetSq;
		theBoard.setEnPassantTargetSq(enPassantTargetSq);
	}

	boolean isEnPassantCapture(GenericMove move, Piece pieceToMove) {
		boolean enPassantCapture = false;
		if ( enPassantTargetSq != null && pieceToMove instanceof Pawn && move.to == enPassantTargetSq) {
			enPassantCapture = true;
		}
		return enPassantCapture;
	}
	
	void checkToSetEnPassantTargetSq(GenericMove move, Piece pieceToMove) {
		if ( pieceToMove instanceof Pawn ) {
			Pawn pawnPiece = (Pawn) pieceToMove;
			if ( pawnPiece.isAtInitialPosition()) {
				if ( pawnPiece.isWhite()) {
					if (move.to.rank == GenericRank.R4) {
						GenericPosition enPassantWhite = GenericPosition.valueOf(move.to.file,GenericRank.R3);
						setEnPassantTargetSq(enPassantWhite);
					}
				} else {
					if (move.to.rank == GenericRank.R5) {
						GenericPosition enPassantBlack = GenericPosition.valueOf(move.to.file,GenericRank.R6);
						setEnPassantTargetSq(enPassantBlack);
					}						
				}
			}
		}
	}
}