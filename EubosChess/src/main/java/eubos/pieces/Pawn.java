package eubos.pieces;

import java.util.LinkedList;

import com.fluxchess.jcpi.models.GenericChessman;
import com.fluxchess.jcpi.models.GenericMove;
import com.fluxchess.jcpi.models.GenericPosition;
import com.fluxchess.jcpi.models.GenericRank;

import eubos.board.Board;
import eubos.board.BoardManager;
import eubos.board.Direction;

public class Pawn extends PieceSinglesquareDirectMove {

	public Pawn( Colour Colour, GenericPosition at ) {
		colour = Colour;
		onSquare = at;
	}
	
	private boolean hasNeverMoved() {
		return ( !everMoved && isAtInitialPosition());
	}

	public boolean isAtInitialPosition() {
		if ( isBlack() ) {
			return (onSquare.rank.equals( GenericRank.R7 ));
		} else {
			return (onSquare.rank.equals( GenericRank.R2 ));
		}
	}

	private GenericPosition genOneSqTarget() {
		if ( isBlack() ) {
			return getOneSq(Direction.down);
		} else {
			return getOneSq(Direction.up);
		}
	}	
	
	private GenericPosition genTwoSqTarget() {
		GenericPosition moveTo = null;
		if ( hasNeverMoved() ) {
			if ( isBlack()) {
				moveTo = Direction.getDirectMoveSq(Direction.down, Direction.getDirectMoveSq(Direction.down, onSquare));
			} else {
				moveTo = Direction.getDirectMoveSq(Direction.up, Direction.getDirectMoveSq(Direction.up, onSquare));
			}
		}
		return moveTo;
	}
	
	private GenericPosition genLeftCaptureTarget() {
		if ( isBlack() ) {
			return getOneSq(Direction.downRight);
		} else {
			return getOneSq(Direction.upLeft);
		}
	}
	
	private GenericPosition genRightCaptureTarget() {
		if ( isBlack() ) {
			return getOneSq(Direction.downLeft);
		} else {
			return getOneSq(Direction.upRight);
		}		
	}
	
	private boolean isCapturable(BoardManager bm, GenericPosition captureAt ) {
		boolean isCapturable = false;
		Piece queryPiece = bm.getTheBoard().getPieceAtSquare( captureAt );
		if ( queryPiece != null ) {
			isCapturable = isOppositeColour( queryPiece );
		} else if (captureAt == bm.getEnPassantManager().getEnPassantTargetSq()) {
			isCapturable = true;
		}
		return isCapturable;
	}
	
	private boolean checkPromotionPossible( GenericPosition targetSquare ) {
		return (( isBlack() && targetSquare.rank == GenericRank.R1 ) || 
				( isWhite() && targetSquare.rank == GenericRank.R8 ));
	}
	
	private void checkPromotionAddMove(LinkedList<GenericMove> moveList,
			GenericPosition targetSquare) {
		if ( checkPromotionPossible( targetSquare )) {
			moveList.add( new GenericMove( onSquare, targetSquare, GenericChessman.QUEEN ));
			moveList.add( new GenericMove( onSquare, targetSquare, GenericChessman.KNIGHT ));
			moveList.add( new GenericMove( onSquare, targetSquare, GenericChessman.BISHOP ));
			moveList.add( new GenericMove( onSquare, targetSquare, GenericChessman.ROOK ));
		} else {
			moveList.add( new GenericMove( onSquare, targetSquare ) );
		}
	}	
	
	@Override
	public LinkedList<GenericMove> generateMoves(BoardManager bm) {
		Board theBoard = bm.getTheBoard();
		LinkedList<GenericMove> moveList = new LinkedList<GenericMove>();
		// Check for standard one and two square moves
		GenericPosition moveTo = genOneSqTarget();
		if ( moveTo != null && theBoard.squareIsEmpty( moveTo )) {
			checkPromotionAddMove(moveList, moveTo);
			moveTo = genTwoSqTarget();
			if ( moveTo != null && theBoard.squareIsEmpty( moveTo )) {
				moveList.add( new GenericMove( onSquare, moveTo ) );
			}	
		}
		// Check for capture moves, includes en passant
		GenericPosition captureAt = genLeftCaptureTarget();
		if ( captureAt != null && isCapturable(bm,captureAt)) {
			checkPromotionAddMove(moveList, captureAt);
		}
		captureAt = genRightCaptureTarget();
		if ( captureAt != null && isCapturable(bm,captureAt)) {
			checkPromotionAddMove(moveList, captureAt);
		}
		return moveList;
	}
	
	@Override
	public boolean attacks( BoardManager bm, GenericPosition [] pos ) {
		boolean isAnyAttacked = false;
		for ( GenericPosition sqToCheck : pos ) {
			if (sqToCheck.equals(genRightCaptureTarget()) || sqToCheck.equals(genLeftCaptureTarget()))
				isAnyAttacked = true;
		}
		return isAnyAttacked;
	}
}
