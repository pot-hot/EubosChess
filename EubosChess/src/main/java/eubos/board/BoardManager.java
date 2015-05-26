package eubos.board;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;

import com.fluxchess.jcpi.models.GenericFile;
import com.fluxchess.jcpi.models.GenericMove;
import com.fluxchess.jcpi.models.GenericPosition;
import com.fluxchess.jcpi.models.GenericRank;

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
		private GenericPosition enPassantTarget = null; 

		public TrackedMove( GenericMove inMove ) { move = inMove; }
		public TrackedMove( GenericMove inMove, Piece capture, GenericPosition enP ) {
			move = inMove; 
			capturedPiece = capture;
			enPassantTarget = enP;
		}
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
		public GenericPosition getEnPassantTarget() {
			return enPassantTarget;
		}
		public void setEnPassantTarget(GenericPosition enPassantTarget) {
			this.enPassantTarget = enPassantTarget;
		}
	}

	public class fenParser {
		LinkedList<Piece> pl;
		
		public fenParser( BoardManager bm, String fenString ) {
			pl = new LinkedList<Piece>();
			String[] tokens = fenString.split(" ");
			String piecePlacement = tokens[0];
			String colourOnMove = tokens[1];
			castling = new CastlingManager(bm, tokens[2]);
			String enPassanttargetSq = tokens[3];
//			String halfMoveClock = tokens[4];
//			String moveNumber = tokens[5];
			parsePiecePlacement(piecePlacement);
			parseOnMove(colourOnMove);
			parseEnPassant(enPassanttargetSq);
		}
		private void parseOnMove(String colourOnMove) {
			if (colourOnMove.equals("w"))
				onMove = Colour.white;
			else if (colourOnMove.equals("b"))
				onMove = Colour.black;
		}
		private void parsePiecePlacement(String piecePlacement) {
			GenericRank r = GenericRank.R8;
			GenericFile f = GenericFile.Fa;
			for ( char c: piecePlacement.toCharArray() ){
				switch(c)
				{
				case 'r':
					pl.add(new Rook( Colour.black, GenericPosition.valueOf(f,r)));
					f = advanceFile(f);
					break;
				case 'R':
					pl.add(new Rook( Colour.white, GenericPosition.valueOf(f,r)));
					f = advanceFile(f);
					break;
				case 'n':
					pl.add(new Knight( Colour.black, GenericPosition.valueOf(f,r)));
					f = advanceFile(f);
					break;
				case 'N':
					pl.add(new Knight( Colour.white, GenericPosition.valueOf(f,r)));
					f = advanceFile(f);
					break;
				case 'b':
					pl.add(new Bishop( Colour.black, GenericPosition.valueOf(f,r)));
					f = advanceFile(f);
					break;
				case 'B':
					pl.add(new Bishop( Colour.white, GenericPosition.valueOf(f,r)));
					f = advanceFile(f);
					break;
				case 'q':
					pl.add(new Queen( Colour.black, GenericPosition.valueOf(f,r)));
					f = f.next();
					break;
				case 'Q':
					pl.add(new Queen( Colour.white, GenericPosition.valueOf(f,r)));
					f = advanceFile(f);
					break;
				case 'k':
					pl.add(new King( Colour.black, GenericPosition.valueOf(f,r)));
					f = advanceFile(f);
					break;
				case 'K':
					pl.add(new King( Colour.white, GenericPosition.valueOf(f,r)));
					f = advanceFile(f);
					break;
				case 'p':
					pl.add(new Pawn( Colour.black, GenericPosition.valueOf(f,r)));
					f = advanceFile(f);
					break;
				case 'P':
					pl.add(new Pawn( Colour.white, GenericPosition.valueOf(f,r)));
					f = advanceFile(f);
					break;
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
					int loop = new Integer(c-'0');
					for ( int i=0; i<loop; i++ ) {
						f = advanceFile(f);
					}
				case '8':
					break;
				case '/':
					r = r.prev();
					f = GenericFile.Fa;
					break;
				}
			}
		}
		private void parseEnPassant(String targetSq) {
			if (!targetSq.contentEquals("-")) {
				enPassantManager = new EnPassantManager(GenericPosition.valueOf(targetSq));
			} else {
				enPassantManager = new EnPassantManager(null);
			}
		}
		private GenericFile advanceFile(GenericFile f) {
			if ( f != GenericFile.Fh )
				f = f.next();
			return f;
		}
		public Board create() {
			return new Board( pl );
		}
	}

	private Stack<TrackedMove> previousMoves;
	
	// No public setter, because onMove is only changed by performing a move on the board.
	private Colour onMove;
	public Colour getOnMove() {
		return onMove;
	}

	private King whiteKing;
	private King blackKing;
	public King getKing( Colour colour ) {
		return ((colour == Colour.white) ? whiteKing : blackKing);
	}
	public void setKing(King king) {
		if (king.isWhite())
			whiteKing = king;
		else 
			blackKing = king;
	}

	private CastlingManager castling;
	public CastlingManager getCastlingManager() {
		return castling;
	}

	private Board theBoard;
	public Board getTheBoard() {
		return theBoard;
	}
	
	private EnPassantManager enPassantManager = new EnPassantManager( null );
	public EnPassantManager getEnPassantManager() {
		return enPassantManager;
	}

	public BoardManager() {
		this("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
	}
	
	public BoardManager( Board startingPosition, Piece.Colour colourToMove ) {
		previousMoves = new Stack<TrackedMove>();
		theBoard = startingPosition;
		Iterator<Piece> iterAllPieces = theBoard.iterator();
		while (iterAllPieces.hasNext()) {
			Piece currPiece = iterAllPieces.next();
			if ( currPiece instanceof King ) {
				setKing( (King)currPiece );
			}
		}
		castling = new CastlingManager(this);
		onMove = colourToMove;
	}
	
	public BoardManager( String fenString ) {
		previousMoves = new Stack<TrackedMove>();
		fenParser fp = new fenParser( this, fenString );
		theBoard = fp.create();
		//enPassantManager = fp.createEnPassantManager();
		Iterator<Piece> iterAllPieces = theBoard.iterator();
		while (iterAllPieces.hasNext()) {
			Piece currPiece = iterAllPieces.next();
			if ( currPiece instanceof King ) {
				setKing( (King)currPiece );
			}
		}
	}
	
	public void undoPreviousMove() throws InvalidPieceException {
		if ( !previousMoves.isEmpty()) {
			enPassantManager.setEnPassantTargetSq(null);
			TrackedMove tm = previousMoves.pop();
			GenericMove moveToUndo = tm.getMove();
			if ( moveToUndo.promotion != null ) {
				Piece.Colour colourToCreate = theBoard.getPieceAtSquare(moveToUndo.to).getColour();
				theBoard.setPieceAtSquare( new Pawn( colourToCreate, moveToUndo.to ));
			}
			unperformMove( new GenericMove( moveToUndo.to, moveToUndo.from ) );
			if ( tm.isCapture()) {
				theBoard.setPieceAtSquare(tm.getCapturedPiece());
			}
			enPassantManager.setEnPassantTargetSq(tm.getEnPassantTarget());
			// Update onMove
			onMove = Piece.Colour.getOpposite(onMove);
		}
	}
	
	public boolean lastMoveWasCapture() {
		boolean wasCapture = false;
		if ( !previousMoves.isEmpty()) {
			wasCapture = previousMoves.peek().isCapture();
		}
		return wasCapture;
	}
	
	public void performMove( GenericMove move ) throws InvalidPieceException {
		// Move the piece
		Piece pieceToMove = theBoard.pickUpPieceAtSquare( move.from );
		if ( pieceToMove != null ) {
			// Flag if move is an en passant capture
			boolean enPassantCapture = enPassantManager.isEnPassantCapture(move, pieceToMove);
			// Save previous en passant square and initialise for this move
			GenericPosition prevEnPassantTargetSq = enPassantManager.getEnPassantTargetSq();
			enPassantManager.setEnPassantTargetSq(null);
			// Handle pawn promotion moves
			pieceToMove = checkForPawnPromotions(move, pieceToMove);
			// Handle castling secondary rook moves...
			if (pieceToMove instanceof King)
				castling.performSecondaryCastlingMove(move);
			// Handle any initial 2 square pawn moves that are subject to en passant rule
			enPassantManager.checkToSetEnPassantTargetSq(move, pieceToMove);
			// Store this move in the previous moves list
			savePreviousMove(move, pieceToMove, enPassantCapture, prevEnPassantTargetSq);
			// Update the piece's square.
			updateSquarePieceOccupies(move.to, pieceToMove);
			// Update onMove
			onMove = Colour.getOpposite(onMove);
		} else {
			throw new InvalidPieceException(move.from);
		}
	}

	private void savePreviousMove(GenericMove move, Piece pieceToMove,
			boolean enPassantCapture, GenericPosition prevEnPassantTargetSq) 
					throws InvalidPieceException {
		Piece captureTarget = theBoard.captureAtSquare(move.to);
		GenericRank rank;
		if (enPassantCapture) {
			if (pieceToMove.isWhite()) {
				rank = GenericRank.R5;
			} else {
				rank = GenericRank.R4;
			}
			GenericPosition capturePos = GenericPosition.valueOf(move.to.file,rank);
			captureTarget = theBoard.captureAtSquare(capturePos);
		}
		previousMoves.push( new TrackedMove(move, captureTarget, prevEnPassantTargetSq));
	}

	private void unperformMove( GenericMove move ) throws InvalidPieceException {
		// Move the piece
		Piece pieceToMove = theBoard.pickUpPieceAtSquare( move.from );
		if ( pieceToMove != null ) {
			// Handle castling secondary rook moves...
			if (pieceToMove instanceof King)
				castling.unperformSecondaryCastlingMove(move);
			updateSquarePieceOccupies(move.to, pieceToMove);
		} else {
			throw new InvalidPieceException(move.from);
		}
	}

	void updateSquarePieceOccupies(GenericPosition newSq, Piece pieceToMove) {
		pieceToMove.setSquare(newSq);
		theBoard.setPieceAtSquare(pieceToMove);
	}

	private Piece checkForPawnPromotions(GenericMove move, Piece pieceToMove) {
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
		return pieceToMove;
	}
}
