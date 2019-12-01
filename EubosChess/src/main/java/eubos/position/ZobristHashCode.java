package eubos.position;

import java.util.Random;
import java.util.Stack;

import com.fluxchess.jcpi.models.GenericFile;
import com.fluxchess.jcpi.models.GenericMove;
import com.fluxchess.jcpi.models.GenericPosition;
import com.fluxchess.jcpi.models.GenericChessman;
import com.fluxchess.jcpi.models.IllegalNotationException;
import com.fluxchess.jcpi.models.IntFile;
import com.fluxchess.jcpi.models.IntRank;

import eubos.board.pieces.Bishop;
import eubos.board.pieces.King;
import eubos.board.pieces.Knight;
import eubos.board.pieces.Piece;
import eubos.board.pieces.Piece.PieceType;
import eubos.board.pieces.Queen;
import eubos.board.pieces.Rook;
import eubos.board.pieces.Piece.Colour;
import eubos.position.CaptureData;

public class ZobristHashCode {
	
	public long hashCode;
	
	private static final int NUM_COLOURS = 2;
	private static final int NUM_PIECES = 6;
	private static final int NUM_SQUARES = 64;
	// One entry for each piece at each square for each colour.
	private static final int INDEX_WHITE = 0;
	private static final int INDEX_BLACK = (NUM_PIECES*NUM_SQUARES);
	// One entry indicating that the side to move is black
	private static final int INDEX_SIDE_TO_MOVE = (NUM_COLOURS*NUM_PIECES*NUM_SQUARES);
	// Four entries for castling rights
	private static final int INDEX_WHITE_KSC = INDEX_SIDE_TO_MOVE+1;
	private static final int INDEX_WHITE_QSC = INDEX_WHITE_KSC+1;
	private static final int INDEX_BLACK_KSC = INDEX_WHITE_QSC+1;
	private static final int INDEX_BLACK_QSC = INDEX_BLACK_KSC+1;
    // Right entries for the en passant file, if en passant move is legal
	private static final int INDEX_ENP_A = INDEX_BLACK_QSC+1;
	private static final int INDEX_ENP_B = INDEX_ENP_A+1;
	private static final int INDEX_ENP_C = INDEX_ENP_B+1;
	private static final int INDEX_ENP_D = INDEX_ENP_C+1;
	private static final int INDEX_ENP_E = INDEX_ENP_D+1;
	private static final int INDEX_ENP_F = INDEX_ENP_E+1;
	private static final int INDEX_ENP_G = INDEX_ENP_F+1;
	private static final int INDEX_ENP_H = INDEX_ENP_G+1;
	private static final int LENGTH_TABLE = INDEX_ENP_H+1;
	
	private static final int INDEX_PAWN = 0;
	private static final int INDEX_KNIGHT = 1;
	private static final int INDEX_BISHOP = 2;
	private static final int INDEX_ROOK = 3;
	private static final int INDEX_QUEEN = 4;
	private static final int INDEX_KING = 5;
	
	private IPositionAccessors pos;
	
	private Stack<GenericFile> prevEnPassantFile = null;
	private int prevCastlingMask = 0;
		
	static private final long prnLookupTable[] = new long[LENGTH_TABLE];
	static {
		// Set up the pseudo random number lookup table that shall be used
		Random randGen = new Random();
		for (int index = 0; index < prnLookupTable.length; index++) 
				// TODO: investigate using a better PRN generator here...
				prnLookupTable[index] = randGen.nextLong();
	};

	public ZobristHashCode(IPositionAccessors pm) {
		pos = pm;
		prevEnPassantFile = new Stack<GenericFile>();
		generate();
	}
	
	// Generate a hash code for a position from scratch
	private long generate() {
		// add pieces
		hashCode = 0;
		for (Piece currPiece : pos.getTheBoard()) {
			hashCode ^= getPrnForPiece(currPiece.getSquare(), currPiece);
		}
		// add castling
		prevCastlingMask = pos.getCastlingAvaillability();	
		if ((prevCastlingMask & PositionManager.WHITE_KINGSIDE)==PositionManager.WHITE_KINGSIDE)
			hashCode ^= prnLookupTable[INDEX_WHITE_KSC];
		if ((prevCastlingMask & PositionManager.WHITE_QUEENSIDE)==PositionManager.WHITE_QUEENSIDE)
			hashCode ^= prnLookupTable[INDEX_WHITE_QSC];
		if ((prevCastlingMask & PositionManager.BLACK_KINGSIDE)==PositionManager.BLACK_KINGSIDE)
			hashCode ^= prnLookupTable[INDEX_BLACK_KSC];
		if ((prevCastlingMask & PositionManager.BLACK_QUEENSIDE)==PositionManager.BLACK_QUEENSIDE)
			hashCode ^= prnLookupTable[INDEX_BLACK_QSC];
		// add on move
		if (!pos.onMoveIsWhite()) {
			doOnMove();
		}
		// add en passant
		GenericPosition enPassant = pos.getTheBoard().getEnPassantTargetSq();
		if (enPassant!=null) {
			prevEnPassantFile.push(enPassant.file);
			int enPassantFile = IntFile.valueOf(enPassant.file);
			hashCode ^= prnLookupTable[(INDEX_ENP_A+enPassantFile)];
		}
		return hashCode;
	}

	protected long getPrnForPiece(GenericPosition pos, Piece currPiece) {
		// compute prnLookup index to use, based on piece type, colour and square.
		int lookupIndex=INDEX_WHITE;
		int atFile = IntFile.valueOf(pos.file);
		int atRank = IntRank.valueOf(pos.rank);
		int pieceType = INDEX_PAWN; // Default
		if (currPiece instanceof Knight) {
			pieceType = INDEX_KNIGHT;
		} else if (currPiece instanceof Bishop) {
			pieceType = INDEX_BISHOP;
		} else if (currPiece instanceof Rook) {
			pieceType = INDEX_ROOK;
		} else if (currPiece instanceof Queen) {
			pieceType = INDEX_QUEEN;
		} else if (currPiece instanceof King) {
			pieceType = INDEX_KING;
		}
		lookupIndex = atFile + atRank * 8 + pieceType * NUM_SQUARES;
		if (currPiece.isBlack())
			lookupIndex += INDEX_BLACK;
		
		return prnLookupTable[lookupIndex];
	}
	
	protected long getPrnForPieceAlt(GenericPosition pos, PieceType currPiece) {
		// compute prnLookup index to use, based on piece type, colour and square.
		int lookupIndex=INDEX_WHITE;
		int atFile = IntFile.valueOf(pos.file);
		int atRank = IntRank.valueOf(pos.rank);
		int pieceType = INDEX_PAWN; // Default
		if (currPiece.equals(PieceType.WhiteKnight) || currPiece.equals(PieceType.BlackKnight)) {
			pieceType = INDEX_KNIGHT;
		} else if (currPiece.equals(PieceType.WhiteBishop) || currPiece.equals(PieceType.BlackBishop)) {
			pieceType = INDEX_BISHOP;
		} else if (currPiece.equals(PieceType.WhiteRook) || currPiece.equals(PieceType.BlackRook)) {
			pieceType = INDEX_ROOK;
		} else if (currPiece.equals(PieceType.WhiteQueen) || currPiece.equals(PieceType.BlackQueen)) {
			pieceType = INDEX_QUEEN;
		} else if (currPiece.equals(PieceType.WhiteKing) || currPiece.equals(PieceType.BlackKing)) {
			pieceType = INDEX_KING;
		}
		lookupIndex = atFile + atRank * 8 + pieceType * NUM_SQUARES;
		if (currPiece.equals(PieceType.BlackKnight) || currPiece.equals(PieceType.BlackBishop) || currPiece.equals(PieceType.BlackRook) ||
			currPiece.equals(PieceType.BlackQueen) || currPiece.equals(PieceType.BlackKing) || currPiece.equals(PieceType.BlackPawn))
			lookupIndex += INDEX_BLACK;
		
		return prnLookupTable[lookupIndex];
	}
	
	// Used to update the Zobrist hash code whenever a position changes due to a move being performed
	public void update(GenericMove move, CaptureData captureTarget, GenericFile enPassantFile) {
		PieceType piece = doBasicMove(move);
		
		doCapturedPiece(captureTarget);
		
		doEnPassant(move, enPassantFile);
		
		try {
			doSecondaryMove(move, piece);
		} catch (IllegalNotationException e) {
			e.printStackTrace();
		}
		
		doCastlingFlags();
		
		doOnMove();
	}

	private PieceType convertChessmanToPiece(GenericChessman chessman, GenericMove move) {
		PieceType eubosPiece = null;
		if (chessman.equals(GenericChessman.KNIGHT))
			eubosPiece = (pos.getOnMove().equals(Colour.black)) ? PieceType.WhiteKnight : PieceType.BlackKnight;
		else if (chessman.equals(GenericChessman.BISHOP))
			eubosPiece = (pos.getOnMove().equals(Colour.black)) ? PieceType.WhiteBishop : PieceType.BlackBishop;
		else if (chessman.equals(GenericChessman.ROOK))
			eubosPiece = (pos.getOnMove().equals(Colour.black)) ? PieceType.WhiteRook : PieceType.BlackRook;
		else if (chessman.equals(GenericChessman.QUEEN))
			eubosPiece = (pos.getOnMove().equals(Colour.black)) ? PieceType.WhiteQueen : PieceType.BlackQueen;
		return eubosPiece;
	}
	
	protected PieceType doBasicMove(GenericMove move) {
		PieceType piece = pos.getTheBoard().getPieceAtSquare(move.to);
		GenericChessman promotedChessman = move.promotion;
		if (promotedChessman == null) {
			// Basic move only
			hashCode ^= getPrnForPieceAlt(move.to, piece);
			hashCode ^= getPrnForPieceAlt(move.from, piece);
		} else {
			// Promotion
			if (piece==PieceType.WhitePawn||piece==PieceType.BlackPawn) {
				// is undoing promotion
				PieceType promotedToPiece = convertChessmanToPiece(promotedChessman, move);
				hashCode ^= getPrnForPieceAlt(move.to, piece);
				hashCode ^= getPrnForPieceAlt(move.from, promotedToPiece);
				piece = promotedToPiece;
			} else if (piece==PieceType.WhiteKnight || piece==PieceType.BlackKnight ||
					piece==PieceType.WhiteBishop || piece==PieceType.BlackBishop || 
				    piece==PieceType.WhiteRook || piece==PieceType.BlackRook || 
				    piece==PieceType.WhiteQueen || piece==PieceType.BlackQueen ) {
				// is a promotion
				PieceType unpromotedPawn = (pos.getOnMove().equals(Colour.white)) ? PieceType.WhitePawn : PieceType.BlackPawn;
				hashCode ^= getPrnForPieceAlt(move.to, piece);
				hashCode ^= getPrnForPieceAlt(move.from, unpromotedPawn);
			}
		}
		return piece;
	}

	protected void doCapturedPiece(CaptureData captureTarget) {
		if (captureTarget.target != PieceType.NONE)
			hashCode ^= getPrnForPieceAlt(captureTarget.square, captureTarget.target);
	}

	private void setTargetFile(GenericFile enPasFile) {
		if (!prevEnPassantFile.isEmpty()) {
			clearTargetFile();
		}
		prevEnPassantFile.push(enPasFile);
		hashCode ^= prnLookupTable[(INDEX_ENP_A+IntFile.valueOf(enPasFile))];
	}
	
	private void clearTargetFile() {
		hashCode ^= prnLookupTable[(INDEX_ENP_A+IntFile.valueOf(prevEnPassantFile.pop()))];
	}
	
	protected void doEnPassant(GenericMove move, GenericFile enPassantFile) {
		if (enPassantFile != null) {
			setTargetFile(enPassantFile);
		} else if (!prevEnPassantFile.isEmpty()) {
			clearTargetFile();
		} else {
			// no action needed
		}
	}

	protected void doOnMove() {
	    hashCode ^= prnLookupTable[INDEX_SIDE_TO_MOVE];
	}

	protected void doCastlingFlags() {
		int currentCastlingFlags = pos.getCastlingAvaillability();
		int delta = currentCastlingFlags ^ this.prevCastlingMask;
		if (delta != 0)
		{
			if ((delta & PositionManager.WHITE_KINGSIDE)==PositionManager.WHITE_KINGSIDE)
			{
				hashCode ^= prnLookupTable[INDEX_WHITE_KSC];
			}
			if ((delta & PositionManager.WHITE_QUEENSIDE)==PositionManager.WHITE_QUEENSIDE) {
				hashCode ^= prnLookupTable[INDEX_WHITE_QSC];
			}
			if ((delta & PositionManager.BLACK_KINGSIDE)==PositionManager.BLACK_KINGSIDE) {
				hashCode ^= prnLookupTable[INDEX_BLACK_KSC];
			}
			if ((delta & PositionManager.BLACK_QUEENSIDE)==PositionManager.BLACK_QUEENSIDE) {
				hashCode ^= prnLookupTable[INDEX_BLACK_QSC];
			}
		}
		this.prevCastlingMask = currentCastlingFlags;
	}

	protected void doSecondaryMove(GenericMove move, PieceType piece)
			throws IllegalNotationException {
		if ( piece==PieceType.WhiteKing ) {
			if (move.equals(CastlingManager.wksc)) {
				piece = pos.getTheBoard().getPieceAtSquare(GenericPosition.f1);
				hashCode ^= getPrnForPieceAlt(GenericPosition.f1, piece); // to
				hashCode ^= getPrnForPieceAlt(GenericPosition.h1, piece); // from
			}
			else if (move.equals(CastlingManager.wqsc)) {
				piece = pos.getTheBoard().getPieceAtSquare(GenericPosition.d1);
				hashCode ^= getPrnForPieceAlt(GenericPosition.d1, piece); // to
				hashCode ^= getPrnForPieceAlt(GenericPosition.a1, piece); // from
			}
			else if (move.equals(CastlingManager.undo_wksc))
			{
				piece = pos.getTheBoard().getPieceAtSquare(GenericPosition.h1);
				hashCode ^= getPrnForPieceAlt(GenericPosition.h1, piece); // to
				hashCode ^= getPrnForPieceAlt(GenericPosition.f1, piece); // from
			}
			else if (move.equals(CastlingManager.undo_wqsc)) {
				piece = pos.getTheBoard().getPieceAtSquare(GenericPosition.a1);
				hashCode ^= getPrnForPieceAlt(GenericPosition.a1, piece); // to
				hashCode ^= getPrnForPieceAlt(GenericPosition.d1, piece); // from
			}
		} else if (piece==PieceType.BlackKing) {
			if (move.equals(CastlingManager.bksc)) {
				piece = pos.getTheBoard().getPieceAtSquare(GenericPosition.f8);
				hashCode ^= getPrnForPieceAlt(GenericPosition.f8, piece); // to
				hashCode ^= getPrnForPieceAlt(GenericPosition.h8, piece); // from
			}
			else if (move.equals(CastlingManager.bqsc)) {
				piece = pos.getTheBoard().getPieceAtSquare(GenericPosition.d8);
				hashCode ^= getPrnForPieceAlt(GenericPosition.d8, piece); // to
				hashCode ^= getPrnForPieceAlt(GenericPosition.a8, piece); // from
			}
			else if (move.equals(CastlingManager.undo_bksc)) {
				piece = pos.getTheBoard().getPieceAtSquare(GenericPosition.h8);
				hashCode ^= getPrnForPieceAlt(GenericPosition.h8, piece); // to
				hashCode ^= getPrnForPieceAlt(GenericPosition.f8, piece); // from
			}
			else if (move.equals(CastlingManager.undo_bqsc)) {
				piece = pos.getTheBoard().getPieceAtSquare(GenericPosition.a8);
				hashCode ^= getPrnForPieceAlt(GenericPosition.a8, piece); // to
				hashCode ^= getPrnForPieceAlt(GenericPosition.d8, piece); // from
			}
		} else {
			// not actually a castle move
		}
	}
}
