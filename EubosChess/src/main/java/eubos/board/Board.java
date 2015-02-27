package eubos.board;

import java.util.*;

import eubos.pieces.*;

import com.fluxchess.jcpi.models.*;

public class Board {
	private Piece[][] theBoard = new Piece[8][8];
	private GenericMove previousMove = null;

	public Board() {
		setupNewGame();
	}

	public Board( LinkedList<Piece> pieceList ) {
		for ( Piece nextPiece : pieceList ) {
			setPieceAtSquare( nextPiece );
		}
	}

	public GenericMove findBestMove() throws IllegalNotationException {
		// TODO: for now find a random legal move for the side indicated
		// first generate the entire move list
		GenericMove bestMove = null;
		LinkedList<GenericMove> entireMoveList = new LinkedList<GenericMove>();
		for (int i: IntFile.values) {
			for (int j: IntRank.values) {
				Piece nextPiece = theBoard[i][j];
				if (nextPiece != null && nextPiece.isBlack() ) {
					// append this piece's legal moves to the entire move list
					entireMoveList.addAll(nextPiece.generateMoveList(this));
				}
			}
		}
		if ( !entireMoveList.isEmpty()) {
			// secondly return a move at random
			Random randomIndex = new Random();
			Integer indexToGet = randomIndex.nextInt(entireMoveList.size());
			bestMove = entireMoveList.get(indexToGet);			
		}
		// TODO: This exception is when there is no valid move - it is temporary,
		// when implementation is complete this case would actually mean stalemate.
		else throw new IllegalNotationException();
		return bestMove;
	}

	public void performMove( GenericMove move ) {
		// Move the piece
		Piece pieceToMove = pickUpPieceAtSquare( move.from );
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
			// Update the piece's square.
			pieceToMove.setSquare( move.to );
			setPieceAtSquare( pieceToMove );
			previousMove = move;
		} else {
			// TODO throw an exception in this case?
		}
	}

	private void setupNewGame() {
		setupBackRanks();
		setupPawns();
	}

	private void setupPawns() {
		GenericPosition[] allFiles = new GenericPosition[] { 
				GenericPosition.a2,
				GenericPosition.b2,
				GenericPosition.c2,
				GenericPosition.d2,
				GenericPosition.e2,
				GenericPosition.f2,
				GenericPosition.g2,
				GenericPosition.h2 };
		for ( GenericPosition startPos : allFiles ) {
			setPieceAtSquare( new Pawn( Piece.PieceColour.white, startPos ));
		}
		allFiles = new GenericPosition[] { 
				GenericPosition.a7,
				GenericPosition.b7,
				GenericPosition.c7,
				GenericPosition.d7,
				GenericPosition.e7,
				GenericPosition.f7,
				GenericPosition.g7,
				GenericPosition.h7 };		
		for ( GenericPosition startPos : allFiles ) {
			setPieceAtSquare( new Pawn( Piece.PieceColour.black, startPos ));
		}
	}

	private void setupBackRanks() {
		// White
		setPieceAtSquare( new Rook  ( Piece.PieceColour.white, GenericPosition.a1 ));
		setPieceAtSquare( new Knight( Piece.PieceColour.white, GenericPosition.b1 ));
		setPieceAtSquare( new Bishop( Piece.PieceColour.white, GenericPosition.c1 ));
		setPieceAtSquare( new Queen ( Piece.PieceColour.white, GenericPosition.d1 ));
		setPieceAtSquare( new King  ( Piece.PieceColour.white, GenericPosition.e1 ));
		setPieceAtSquare( new Bishop( Piece.PieceColour.white, GenericPosition.f1 ));
		setPieceAtSquare( new Knight( Piece.PieceColour.white, GenericPosition.g1 ));
		setPieceAtSquare( new Rook  ( Piece.PieceColour.white, GenericPosition.h1 ));
		// Black
		setPieceAtSquare( new Rook  ( Piece.PieceColour.black, GenericPosition.a8 ));
		setPieceAtSquare( new Knight( Piece.PieceColour.black, GenericPosition.b8 ));
		setPieceAtSquare( new Bishop( Piece.PieceColour.black, GenericPosition.c8 ));
		setPieceAtSquare( new Queen ( Piece.PieceColour.black, GenericPosition.d8 ));
		setPieceAtSquare( new King  ( Piece.PieceColour.black, GenericPosition.e8 ));
		setPieceAtSquare( new Bishop( Piece.PieceColour.black, GenericPosition.f8 ));
		setPieceAtSquare( new Knight( Piece.PieceColour.black, GenericPosition.g8 ));
		setPieceAtSquare( new Rook  ( Piece.PieceColour.black, GenericPosition.h8 ));
	}

	private void setPieceAtSquare( Piece pieceToPlace ) {
		GenericPosition atPos = pieceToPlace.getSquare();
		int file, rank;
		file = IntFile.valueOf(atPos.file);
		rank = IntRank.valueOf(atPos.rank);
		theBoard[file][rank] = pieceToPlace;	
	}

	public Piece getPieceAtSquare( GenericPosition atPos ) {
		int file, rank;
		file = IntFile.valueOf(atPos.file);
		rank = IntRank.valueOf(atPos.rank);
		return ( theBoard[file][rank] );
	}
	
	private Piece pickUpPieceAtSquare( GenericPosition atPos ) {
		int file, rank;
		file = IntFile.valueOf(atPos.file);
		rank = IntRank.valueOf(atPos.rank);
		Piece pieceToPickUp = theBoard[file][rank];
		theBoard[file][rank] = null;
		return ( pieceToPickUp );
	}	
	
	public boolean isSquareEmpty( GenericPosition atPos ) {
		int file, rank;
		file = IntFile.valueOf(atPos.file);
		rank = IntRank.valueOf(atPos.rank);
		return ( theBoard[file][rank] == null );		
	}

	public boolean isSquareWhitePiece(GenericPosition atPos) {
		int file, rank;
		file = IntFile.valueOf(atPos.file);
		rank = IntRank.valueOf(atPos.rank);
		boolean retVal = false;
		Piece piece = theBoard[file][rank];
		if (piece != null){
			retVal = theBoard[file][rank].isWhite();
		}
		return retVal;
	}
		
	public GenericMove getPreviousMove() { return previousMove; }
}
