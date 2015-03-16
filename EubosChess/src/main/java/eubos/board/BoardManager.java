package eubos.board;

import java.util.Iterator;
import java.util.Stack;

import com.fluxchess.jcpi.models.GenericMove;
import com.fluxchess.jcpi.models.GenericPosition;

import eubos.pieces.Bishop;
import eubos.pieces.Knight;
import eubos.pieces.Pawn;
import eubos.pieces.Piece;
import eubos.pieces.Piece.Colour;
import eubos.pieces.Queen;
import eubos.pieces.Rook;
import eubos.pieces.King;

public class BoardManager implements IBoardManager {

	public class TrackedMove {
		private GenericMove move = null;
		private Piece capturedPiece = null;

		public TrackedMove( GenericMove inMove ) { move = inMove; }
		public TrackedMove( GenericMove inMove, Piece capture ) {  move = inMove; capturedPiece = capture;}
		public boolean isCapture() { return ((capturedPiece != null) ? true : false); }

		public GenericMove getMove() {
			return move;
		}
		public void setMove(GenericMove move) {
			this.move = move;
		}
		public Piece getCapturedPiece() {
			return capturedPiece;
		}
		public void setCapturedPiece(Piece capturedPiece) {
			this.capturedPiece = capturedPiece;
		}
	}

	private Stack<TrackedMove> previousMoves;
	private Board theBoard;
	private King whiteKing;
	private King blackKing;
	private boolean whiteHasCastled;
	private boolean blackHasCastled;
	
	public Board getTheBoard() {
		return theBoard;
	}

	public BoardManager() { 
		previousMoves = new Stack<TrackedMove>();
		theBoard = new Board();
		setKing( (King) theBoard.getPieceAtSquare(GenericPosition.e1));
		setKing( (King) theBoard.getPieceAtSquare(GenericPosition.e8));
		whiteHasCastled = false;
		blackHasCastled = false;
	}
	
	public BoardManager( Board startingPosition ) {
		previousMoves = new Stack<TrackedMove>();
		theBoard = startingPosition;
		Iterator<Piece> iterAllPieces = theBoard.iterator();
		while (iterAllPieces.hasNext()) {
			Piece currPiece = iterAllPieces.next();
			if ( currPiece instanceof King ) {
				setKing( (King)currPiece );
			}
		}
		whiteHasCastled = false;
		blackHasCastled = false;
	}
	
	public King getKing( Piece.Colour colour ) {
		return ((colour == Piece.Colour.white) ? whiteKing : blackKing);
	}
	
	public void setKing(King king) {
		if (king.isWhite())
			whiteKing = king;
		else 
			blackKing = king;
	}
	
	public boolean isWhiteHasCastled() {
		return whiteHasCastled;
	}

	public void setWhiteHasCastled(boolean whiteHasCastled) {
		this.whiteHasCastled = whiteHasCastled;
	}

	public boolean isBlackHasCastled() {
		return blackHasCastled;
	}

	public void setBlackHasCastled(boolean blackHasCastled) {
		this.blackHasCastled = blackHasCastled;
	}	
	
	public boolean hasCastled( Piece.Colour onMove ) {
		if ( onMove == Colour.white ) {
			return isWhiteHasCastled();
		} else {
			return isBlackHasCastled();
		}
	}
	
	private static final GenericPosition [] kscWhiteCheckSqs = {GenericPosition.e1, GenericPosition.f1, GenericPosition.g1};
	private static final GenericPosition [] kscBlackCheckSqs = {GenericPosition.e8, GenericPosition.f8, GenericPosition.g8};
	
	public GenericMove addKingSideCastle( Piece.Colour onMove ) {
		if ( onMove == Colour.white ) {
			// Target rook should not have moved and be on it initial square
			Piece kscTarget = theBoard.getPieceAtSquare( GenericPosition.h1 );
			if ( !(kscTarget instanceof Rook) || kscTarget.hasEverMoved())
				return null;
			// All the intervening squares between King and Rook should be empty
			if ( !theBoard.squareIsEmpty(GenericPosition.f1) || !theBoard.squareIsEmpty(GenericPosition.g1))
				return null;
			Iterator<Piece> iterPotentialAttackers = theBoard.iterateColour(Piece.Colour.getOpposite(onMove));
			while (iterPotentialAttackers.hasNext()) {
				// None of the intervening squares between King and Rook should be attacked
				// the king cannot be in check at the start or end of the move
				Piece currPiece = iterPotentialAttackers.next();
				if (currPiece.attacks(kscWhiteCheckSqs)) {
					return null;
				}
			}
			return new GenericMove(GenericPosition.e1,GenericPosition.g1);
		} else {
			Piece kscTarget = theBoard.getPieceAtSquare( GenericPosition.h8 );
			if ( !(kscTarget instanceof Rook) || kscTarget.hasEverMoved())
				return null;
			if ( !theBoard.squareIsEmpty(GenericPosition.f8) || !theBoard.squareIsEmpty(GenericPosition.g8))
				return null;
			Iterator<Piece> iterPotentialAttackers = theBoard.iterateColour(Piece.Colour.getOpposite(onMove));
			while (iterPotentialAttackers.hasNext()) {
				Piece currPiece = iterPotentialAttackers.next();
				if (currPiece.attacks(kscBlackCheckSqs)) {
					return null;
				}
			}
			return new GenericMove(GenericPosition.e8,GenericPosition.g8);
		}
	}
		
	public GenericMove addQueenSideCastle( Piece.Colour onMove ) {
		return null;
	}
	
	public void undoPreviousMove() {
		if ( !previousMoves.isEmpty()) {
			TrackedMove tm = previousMoves.pop();
			GenericMove moveToUndo = tm.getMove();
			if ( moveToUndo.promotion != null ) {
				Piece.Colour colourToCreate = theBoard.getPieceAtSquare(moveToUndo.to).getColour();
				theBoard.setPieceAtSquare( new Pawn( colourToCreate, moveToUndo.to ));
			}
			performMove( new GenericMove( moveToUndo.to, moveToUndo.from ) );
			if ( tm.isCapture()) {
				theBoard.setPieceAtSquare(tm.getCapturedPiece());
			}
		}
	}
	
	public GenericMove getPreviousMove() {
		GenericMove lastMove = null;
		if ( !previousMoves.isEmpty()) {
			TrackedMove tm = previousMoves.peek();
			lastMove = tm.getMove();
		}
		return lastMove;
	}
	
	public void performMove( GenericMove move ) {
		// Move the piece
		Piece pieceToMove = theBoard.pickUpPieceAtSquare( move.from );
		if ( pieceToMove != null ) {
			// Handle pawn promotion moves
			if ( move.promotion != null ) {
				switch( move.promotion ) {
				case QUEEN:
					pieceToMove = new Queen(pieceToMove.getColour(), null );
					break;
				case KNIGHT:
					pieceToMove = new Knight(pieceToMove.getColour(), null );
					break;
				case BISHOP:
					pieceToMove = new Bishop(pieceToMove.getColour(), null );
					break;
				case ROOK:
					pieceToMove = new Rook(pieceToMove.getColour(), null );
					break;
				default:
					break;
				}
			}
			// Store this move in the previous moves list
			Piece captureTarget = theBoard.getPieceAtSquare( move.to );
			previousMoves.push( new TrackedMove( move, captureTarget ));
			// Update the piece's square.
			// TODO duplicated information here - sub optimal...
			pieceToMove.setSquare( move.to );
			theBoard.setPieceAtSquare( pieceToMove );
		} else {
			// TODO throw an exception in this case?
		}
	}
}
