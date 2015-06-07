package eubos.board;

import java.util.ArrayList;
import java.util.List;

import com.fluxchess.jcpi.models.GenericPosition;

import eubos.pieces.Bishop;
import eubos.pieces.King;
import eubos.pieces.Knight;
import eubos.pieces.Pawn;
import eubos.pieces.Piece;
import eubos.pieces.Queen;
import eubos.pieces.Rook;

public class SquareAttackEvaluator {
	
	private Piece.Colour attackingColour;
	private GenericPosition attackedSq;
	private Board theBoard;
	
	public SquareAttackEvaluator( Board bd, GenericPosition atPos, Piece.Colour ownColour ) {
		attackingColour = Piece.Colour.getOpposite(ownColour);
		attackedSq = atPos;
		theBoard = bd;
	}
	
	public boolean isAttacked() {
		boolean attacked = false;
		// do/while loop is to allow the function to return attacked=true at earliest possibility
		do {
			// Check for pawn attacks
			attacked = attackedByPawn(Direction.getDirectMoveSq(Direction.upRight,attackedSq));
			if (attacked) break;
			attacked = attackedByPawn(Direction.getDirectMoveSq(Direction.upLeft,attackedSq));
			if (attacked) break;
			attacked = attackedByPawn(Direction.getDirectMoveSq(Direction.downRight,attackedSq));
			if (attacked) break;
			attacked = attackedByPawn(Direction.getDirectMoveSq(Direction.downLeft,attackedSq));
			if (attacked) break;
			// Check for king presence (to avoid moving into check by the enemy king)
			attacked = checkForKingAttacks();
			if (attacked) break;
			// Check for knight attacks
			attacked = checkForKnightAttacks();
			if (attacked) break;
			// check for diagonal attacks
			attacked = checkForAttackerOnDiagonal(getAllSqs(Direction.downLeft));
			if (attacked) break;
			attacked = checkForAttackerOnDiagonal(getAllSqs(Direction.upLeft));
			if (attacked) break;
			attacked = checkForAttackerOnDiagonal(getAllSqs(Direction.downRight));
			if (attacked) break;
			attacked = checkForAttackerOnDiagonal(getAllSqs(Direction.upRight));
			if (attacked) break;
			// check for rank or file attacks
			attacked = checkForAttackerOnRankFile(getAllSqs(Direction.down));
			if (attacked) break;
			attacked = checkForAttackerOnRankFile(getAllSqs(Direction.up));
			if (attacked) break;
			attacked = checkForAttackerOnRankFile(getAllSqs(Direction.left));
			if (attacked) break;
			attacked = checkForAttackerOnRankFile(getAllSqs(Direction.right));
		} while (false);
		return attacked;	
	}

	private boolean checkForKnightAttacks() {
		boolean attacked = false;
		GenericPosition atPos;
		Piece currPiece;
		for (Direction dir: Direction.values()) {
			atPos = Direction.getIndirectMoveSq(dir, attackedSq);
			if (atPos != null) {
				currPiece = theBoard.getPieceAtSquare(atPos);
				if ( currPiece != null && currPiece instanceof Knight && currPiece.getColour()==attackingColour) {
					attacked = true;
					break;
				}
			}
		}
		return attacked;
	}

	private boolean checkForKingAttacks() {
		boolean attacked = false;
		GenericPosition atPos;
		Piece currPiece;
		for (Direction dir: Direction.values()) {
			atPos = Direction.getDirectMoveSq(dir, attackedSq);
			if (atPos != null) {
				currPiece = theBoard.getPieceAtSquare(atPos);
				if ( currPiece != null && currPiece instanceof King && currPiece.getColour()==attackingColour) {
					attacked = true;
					break;
				}
			}
		}
		return attacked;
	}	

	private List<GenericPosition> getAllSqs(Direction dir) {
		GenericPosition atPos = attackedSq;
		ArrayList<GenericPosition> targetSquares = new ArrayList<GenericPosition>();
		while ((atPos = Direction.getDirectMoveSq(dir, atPos)) != null) {
			targetSquares.add(atPos);
		}
		return targetSquares;
	}

	private boolean checkForAttackerOnDiagonal(List<GenericPosition> targetSqs) {
		boolean attacked = false;
		for (GenericPosition attackerSq: targetSqs) {
			Piece currPiece = theBoard.getPieceAtSquare(attackerSq);
			if (currPiece != null ) {
				if (((currPiece instanceof Bishop) || (currPiece instanceof Queen)) && currPiece.getColour()==attackingColour) {
					// Indicates attacked
					attacked = true;
				} // else blocked by own piece or non-attacking enemy
				break;
			}
		}
		return attacked;
	}

	private boolean checkForAttackerOnRankFile(List<GenericPosition> targetSqs) {
		boolean attacked = false;
		for (GenericPosition attackerSq: targetSqs) {
			Piece currPiece = theBoard.getPieceAtSquare(attackerSq);
			if (currPiece != null ) {
				if (((currPiece instanceof Rook) || (currPiece instanceof Queen)) && currPiece.getColour()==attackingColour) {
					// Indicates attacked
					attacked = true;
				} // else blocked by own piece or non-attacking enemy
				break;
			}
		}
		return attacked;
	}	

	private boolean attackedByPawn(GenericPosition attackerSq) {
		Piece currPiece;
		boolean attacked = false;
		if (attackerSq != null) {
			currPiece = theBoard.getPieceAtSquare(attackerSq);
			if ( currPiece != null && currPiece instanceof Pawn && currPiece.getColour()==attackingColour) {
				attacked = true;
			}
		}
		return attacked;
	}
}
