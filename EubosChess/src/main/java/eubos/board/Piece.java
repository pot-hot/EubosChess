package eubos.board;

import java.util.List;

import com.fluxchess.jcpi.models.IntChessman;
import com.fluxchess.jcpi.models.IntRank;

import eubos.main.EubosEngineMain;
import eubos.position.Move;
import eubos.position.Position;

public abstract class Piece {
	public enum Colour { 
		white, black;
		
		public static Colour getOpposite( Colour arg ) { return (arg == white) ? black : white; }
		public static boolean isWhite( Colour arg ) { return arg == white; }
		public static boolean isBlack( Colour arg ) { return arg == black; }
	};
	
	public static final int NONE = 0x0;
	public static final int KING = 0x1;
	public static final int QUEEN = 0x2;
	public static final int ROOK = 0x3;
	public static final int BISHOP = 0x4;
	public static final int KNIGHT = 0x5;
	public static final int PAWN = 0x6;
	
	public static final int BLACK = 0x8;
	
	public static final int PIECE_NO_COLOUR_MASK = 0x7;
	public static final int PIECE_WHOLE_MASK = 0xf;

	public static final int WHITE_QUEEN = QUEEN;
	public static final int WHITE_BISHOP = BISHOP;
	public static final int WHITE_KING = KING;
	public static final int WHITE_KNIGHT = KNIGHT;
	public static final int WHITE_ROOK = ROOK;
	public static final int WHITE_PAWN = PAWN;
	
	public static final int BLACK_QUEEN = (BLACK|QUEEN);
	public static final int BLACK_BISHOP = (BLACK|BISHOP);
	public static final int BLACK_KING = (BLACK|KING);
	public static final int BLACK_KNIGHT = (BLACK|KNIGHT);
	public static final int BLACK_ROOK = (BLACK|ROOK);
	public static final int BLACK_PAWN = (BLACK|PAWN);
	
	public static boolean isPawn(int arg) { return (arg & PIECE_NO_COLOUR_MASK) == PAWN; }
	public static boolean isKing(int arg) { return (arg & PIECE_NO_COLOUR_MASK) == KING; }
	public static boolean isQueen(int arg) { return (arg & PIECE_NO_COLOUR_MASK) == QUEEN; }
	public static boolean isRook(int arg) { return (arg & PIECE_NO_COLOUR_MASK) == ROOK; }
	public static boolean isBishop(int arg) { return (arg & PIECE_NO_COLOUR_MASK) == BISHOP; }
	public static boolean isKnight(int arg) { return (arg & PIECE_NO_COLOUR_MASK) == KNIGHT; }
	
	public static boolean isOppositeColour(Colour ownColour, int toCheck) {
		if (EubosEngineMain.ASSERTS_ENABLED)
			assert (toCheck & PIECE_NO_COLOUR_MASK) != NONE;
		return Colour.isWhite(ownColour) ? isBlack(toCheck) : isWhite(toCheck);
	}
	public static boolean isOppositeColourOrNone(Colour ownColour, int toCheck) {
		boolean retVal = true;
		if (toCheck != Piece.NONE) {
			retVal = Colour.isWhite(ownColour) ? isBlack(toCheck) : isWhite(toCheck);
		}
		return retVal;
	}
	public static boolean isWhite(int arg) {
		return (arg&BLACK) == 0;
	}
	public static boolean isBlack(int arg) {
		return (arg&BLACK) == BLACK;
	}
	public static Colour getOpposite(int arg) {
		return isWhite(arg) ? Colour.black : Colour.white;
	} 
	
	public static int convertChessmanToPiece(int chessman, boolean isWhite) {
		int eubosPiece = Piece.NONE;
		if (chessman==IntChessman.KNIGHT)
			eubosPiece = isWhite ? Piece.WHITE_KNIGHT : Piece.BLACK_KNIGHT;
		else if (chessman==IntChessman.BISHOP)
			eubosPiece = isWhite ? Piece.WHITE_BISHOP : Piece.BLACK_BISHOP;
		else if (chessman==IntChessman.ROOK)
			eubosPiece = isWhite? Piece.WHITE_ROOK : Piece.BLACK_ROOK;
		else if (chessman==IntChessman.QUEEN)
			eubosPiece = isWhite ? Piece.WHITE_QUEEN : Piece.BLACK_QUEEN;
		return eubosPiece;
	}
	
	public static int convertPieceToChessman(int piece) {
		int chessman = IntChessman.NOCHESSMAN;
		if (Piece.isKnight(piece))
			chessman = IntChessman.KNIGHT;
		else if (Piece.isBishop(piece))
			chessman = IntChessman.BISHOP;
		else if (Piece.isRook(piece))
			chessman = IntChessman.ROOK;
		else if (Piece.isQueen(piece))
			chessman = IntChessman.QUEEN;
		else if (Piece.isKing(piece))
			chessman = IntChessman.KING;
		else if (Piece.isPawn(piece))
			chessman = IntChessman.PAWN;		
		return chessman;
	}
	
	public static char toFenChar(int piece) {
		char chessman = 0;
		if (piece==Piece.WHITE_PAWN)
			chessman = 'P';
		else if (piece==Piece.WHITE_KNIGHT)
			chessman = 'N';
		else if (piece==Piece.WHITE_BISHOP)
			chessman = 'B';
		else if (piece==Piece.WHITE_ROOK)
			chessman = 'R';
		else if (piece==Piece.WHITE_QUEEN)
			chessman = 'Q';
		else if (piece==Piece.WHITE_KING)
			chessman = 'K';
		else if (piece==Piece.BLACK_PAWN)
			chessman = 'p';
		else if (piece==Piece.BLACK_KNIGHT)
			chessman = 'n';
		else if (piece==Piece.BLACK_BISHOP)
			chessman = 'b';
		else if (piece==Piece.BLACK_ROOK)
			chessman = 'r';
		else if (piece==Piece.BLACK_QUEEN)
			chessman = 'q';
		else if (piece==Piece.BLACK_KING)
			chessman = 'k';
		return chessman;
	}
	
	static final int[][] WhiteKnightMove_Lut = new int[128][];
	static {
		for (int square : Position.values) {
			WhiteKnightMove_Lut[square] = createKnightMovesFromOriginPosition(square, true);
		}
	}
	static final int[][] BlackKnightMove_Lut = new int[128][];
	static {
		for (int square : Position.values) {
			BlackKnightMove_Lut[square] = createKnightMovesFromOriginPosition(square, false);
		}
	}
	static int [] createKnightMovesFromOriginPosition(int originPosition, boolean isWhite) {
		int originPiece = isWhite ? Piece.WHITE_KNIGHT : Piece.BLACK_KNIGHT;
		int count = 0;
		int [] moves = new int[8];
		// Create reference moves (with target none)
		for (Direction dir: Direction.values()) {
			int targetPosition = Direction.getIndirectMoveSq(dir, originPosition);
			if (targetPosition != Position.NOPOSITION) {
				moves[count] = Move.valueOf(originPosition, originPiece, targetPosition, Piece.NONE);
				count++;
			}
		}
		// Copy to correctly sized array
		int [] ref_moves = new int[count];
		for (int i=0; i < count; i++) {
			ref_moves[i] = moves[i];
		}
		return ref_moves;
	}
	
	static final int[][] WhiteKingMove_Lut = new int[128][];
	static {
		for (int square : Position.values) {
			WhiteKingMove_Lut[square] = createKingMovesFromOriginPosition(square, true);
		}
	}
	static final int[][] BlackKingMove_Lut = new int[128][];
	static {
		for (int square : Position.values) {
			BlackKingMove_Lut[square] = createKingMovesFromOriginPosition(square, false);
		}
	}
	static int [] createKingMovesFromOriginPosition(int originPosition, boolean isWhite) {
		Direction [] allDirect = { Direction.up, Direction.upRight, Direction.right, Direction.downRight, Direction.down, Direction.downLeft, Direction.left, Direction.upLeft };
		int originPiece = isWhite ? Piece.WHITE_KING : Piece.BLACK_KING;
		int count = 0;
		int [] moves = new int[8];
		// Create reference moves (with target none)
		for (Direction dir: allDirect) {
			int targetPosition = Direction.getDirectMoveSq(dir, originPosition);
			if (targetPosition != Position.NOPOSITION) {
				moves[count] = Move.valueOf(originPosition, originPiece, targetPosition, Piece.NONE);
				count++;
			}
		}
		// Copy to correctly sized array
		int [] ref_moves = new int[count];
		for (int i=0; i < count; i++) {
			ref_moves[i] = moves[i];
		}
		return ref_moves;
	}

	static List<Integer> king_generateMoves(List<Integer> moveList, Board theBoard, int atSquare, Piece.Colour ownSide) {
		int [] ref_moves = Colour.isWhite(ownSide) ? WhiteKingMove_Lut[atSquare] : BlackKingMove_Lut[atSquare];
		for (int new_move : ref_moves) {
			int targetPiece = theBoard.getPieceAtSquare(Move.getTargetPosition(new_move));
			if (targetPiece == Piece.NONE) {
				moveList.add(new_move);
			} else if (Piece.isOppositeColour(ownSide, targetPiece)) {
				// assign capture into actual move to add to movelist
				new_move = Move.setCapture(new_move, targetPiece);
				moveList.add(new_move);
			} else {
				// Indicates blocked by own piece.
			}
		}
		return moveList;	
	}
	
	static List<Integer> knight_generateMoves(List<Integer> moveList, Board theBoard, int atSquare, Piece.Colour ownSide) {
		int [] ref_moves = Colour.isWhite(ownSide) ? WhiteKnightMove_Lut[atSquare] : BlackKnightMove_Lut[atSquare];
		for (int new_move : ref_moves) {
			int targetPiece = theBoard.getPieceAtSquare(Move.getTargetPosition(new_move));
			if (targetPiece == Piece.NONE) {
				moveList.add(new_move);
			} else if (Piece.isOppositeColour(ownSide, targetPiece)) {
				// assign capture into actual move to add to movelist
				new_move = Move.setCapture(new_move, targetPiece);
				moveList.add(new_move);
			} else {
				// Indicates blocked by own piece.
			}
		}
		return moveList;		
	}
	
	static List<Integer> rook_generateMoves(List<Integer> moveList, Board theBoard, int atSquare, Piece.Colour ownSide) {
		int ownPiece = (Colour.isBlack(ownSide)) ? Piece.BLACK_ROOK : Piece.WHITE_ROOK;
		multidirect_addMoves(ownPiece, atSquare, moveList, theBoard, Direction.down);
		multidirect_addMoves(ownPiece, atSquare, moveList, theBoard, Direction.up);
		multidirect_addMoves(ownPiece, atSquare, moveList, theBoard, Direction.left);
		multidirect_addMoves(ownPiece, atSquare, moveList, theBoard, Direction.right);
		return moveList;	
	}
	
	static List<Integer> queen_generateMoves(List<Integer> moveList, Board theBoard, int atSquare, Piece.Colour ownSide) {
		int ownPiece = (Colour.isBlack(ownSide)) ? Piece.BLACK_QUEEN : Piece.WHITE_QUEEN;
		multidirect_addMoves(ownPiece, atSquare, moveList, theBoard, Direction.downLeft);
		multidirect_addMoves(ownPiece, atSquare, moveList, theBoard, Direction.upLeft);
		multidirect_addMoves(ownPiece, atSquare, moveList, theBoard, Direction.downRight);
		multidirect_addMoves(ownPiece, atSquare, moveList, theBoard, Direction.upRight);
		multidirect_addMoves(ownPiece, atSquare, moveList, theBoard, Direction.down);
		multidirect_addMoves(ownPiece, atSquare, moveList, theBoard, Direction.up);
		multidirect_addMoves(ownPiece, atSquare, moveList, theBoard, Direction.left);
		multidirect_addMoves(ownPiece, atSquare, moveList, theBoard, Direction.right);
		return moveList;	
	}
	
	static List<Integer> bishop_generateMoves(List<Integer> moveList, Board theBoard, int atSquare, Piece.Colour ownSide) {
		int ownPiece = (Colour.isBlack(ownSide)) ? Piece.BLACK_BISHOP : Piece.WHITE_BISHOP;
		multidirect_addMoves(ownPiece, atSquare, moveList, theBoard, Direction.downLeft);
		multidirect_addMoves(ownPiece, atSquare, moveList, theBoard, Direction.upLeft);
		multidirect_addMoves(ownPiece, atSquare, moveList, theBoard, Direction.downRight);
		multidirect_addMoves(ownPiece, atSquare, moveList, theBoard, Direction.upRight);
		return moveList;	
	}

	private static void multidirect_addMoves(int ownPiece, int atSquare, List<Integer> moveList, Board theBoard, Direction dir) {
		int targetSquare = atSquare;
		while ( true ) {
			targetSquare = Direction.getDirectMoveSqRaw(dir, targetSquare);
			if ((targetSquare & 0x88) == 0) { // If bit set indicates the square is off the board
				int targetPiece = theBoard.getPieceAtSquare(targetSquare);
				if (targetPiece == Piece.NONE) {
					// Slider move
					moveList.add(Move.valueOfRegular(atSquare, ownPiece, targetSquare));
					continue;
				}
				else if ((ownPiece&Piece.BLACK) != (targetPiece&Piece.BLACK)) {
					// Indicates a capture
					moveList.add(Move.valueOfCapture(atSquare, ownPiece, targetSquare, targetPiece));
				} else {
					// Indicates blocked by own piece.
				}
			}
			return;
		}
	}
		
	private static boolean pawn_isAtInitialPosition(int atSquare, Piece.Colour ownSide) {
		if (Colour.isBlack(ownSide)) {
			return (Position.getRank(atSquare) == IntRank.R7);
		} else {
			return (Position.getRank(atSquare) == IntRank.R2);
		}
	}

	private static int pawn_genOneSqTarget(int atSquare, Piece.Colour ownSide) {
		if (Colour.isBlack(ownSide)) {
			return Direction.getDirectMoveSq(Direction.down, atSquare);
		} else {
			return Direction.getDirectMoveSq(Direction.up, atSquare);
		}
	}	
	
	private static int pawn_genTwoSqTarget(int atSquare, Piece.Colour ownSide) {
		int moveTo = Position.NOPOSITION;
		if ( pawn_isAtInitialPosition(atSquare, ownSide) ) {
			if (Colour.isBlack(ownSide)) {
				moveTo = Direction.getDirectMoveSq(Direction.down, Direction.getDirectMoveSq(Direction.down, atSquare));
			} else {
				moveTo = Direction.getDirectMoveSq(Direction.up, Direction.getDirectMoveSq(Direction.up, atSquare));
			}
		}
		return moveTo;
	}
	
	private static int pawn_genLeftCaptureTarget(int atSquare, Piece.Colour ownSide) {
		if (Colour.isBlack(ownSide)) {
			return Direction.getDirectMoveSq(Direction.downRight, atSquare);
		} else {
			return Direction.getDirectMoveSq(Direction.upLeft, atSquare);
		}
	}
	
	private static int pawn_genRightCaptureTarget(int atSquare, Piece.Colour ownSide) {
		if (Colour.isBlack(ownSide)) {
			return Direction.getDirectMoveSq(Direction.downLeft, atSquare);
		} else {
			return Direction.getDirectMoveSq(Direction.upRight, atSquare);
		}		
	}
	
	private static int pawn_isCapturable(Piece.Colour ownSide, Board theBoard, int captureAt ) {
		int capturePiece = Piece.NONE;
		int queryPiece = theBoard.getPieceAtSquare(captureAt);
		if ( queryPiece != Piece.NONE ) {
			if (Piece.isOppositeColour( ownSide, queryPiece )) {
				capturePiece = queryPiece;
			}
		}
		return capturePiece;
	}
	
	private static boolean pawn_checkPromotionPossible(Piece.Colour ownSide, int targetSquare ) {
		return (( Colour.isBlack(ownSide) && Position.getRank(targetSquare) == IntRank.R1) || 
				( Colour.isWhite(ownSide) && Position.getRank(targetSquare) == IntRank.R8));
	}
	
	private static void pawn_checkPromotionAddMove(int ownPiece, Board theBoard, int atSquare, Piece.Colour ownSide, List<Integer> moveList,
			int targetSquare, int targetPiece) {
		if ( pawn_checkPromotionPossible( ownSide, targetSquare )) {
			// Add in order of prioritisation
			moveList.add( Move.valueOf(Move.TYPE_PROMOTION_MASK, atSquare, ownPiece, targetSquare, targetPiece, Piece.QUEEN ));
			moveList.add( Move.valueOf(Move.TYPE_PROMOTION_MASK, atSquare, ownPiece, targetSquare, targetPiece, Piece.ROOK ));
			moveList.add( Move.valueOf(Move.TYPE_PROMOTION_MASK, atSquare, ownPiece, targetSquare, targetPiece, Piece.BISHOP ));
			moveList.add( Move.valueOf(Move.TYPE_PROMOTION_MASK, atSquare, ownPiece, targetSquare, targetPiece, Piece.KNIGHT ));
		} else {
			moveList.add( Move.valueOf(atSquare, ownPiece, targetSquare, targetPiece));
		}
	}	
	
	static List<Integer> pawn_generateMoves(List<Integer> moveList, Board theBoard, int atSquare, Piece.Colour ownSide) {
		int ownPiece = (Colour.isBlack(ownSide)) ? Piece.BLACK_PAWN : Piece.WHITE_PAWN;
		int capturePiece = Piece.NONE;
		// Check for standard one and two square moves
		int moveTo = pawn_genOneSqTarget(atSquare, ownSide);
		if ( moveTo != Position.NOPOSITION && theBoard.squareIsEmpty( moveTo )) {
			pawn_checkPromotionAddMove(ownPiece, theBoard, atSquare, ownSide, moveList, moveTo, Piece.NONE);
			moveTo = pawn_genTwoSqTarget(atSquare, ownSide);
			if ( moveTo != Position.NOPOSITION && theBoard.squareIsEmpty( moveTo )) {
				// Can't be a promotion
				moveList.add( Move.valueOf(atSquare, ownPiece, moveTo , Piece.NONE));
			}	
		}
		// Check for capture moves, includes en passant
		int captureAt = pawn_genLeftCaptureTarget(atSquare, ownSide);
		if ( captureAt != Position.NOPOSITION ) {
			capturePiece = pawn_isCapturable(ownSide, theBoard, captureAt);
			if (capturePiece != Piece.NONE) {
				pawn_checkPromotionAddMove(ownPiece, theBoard, atSquare, ownSide, moveList, captureAt, capturePiece);
			} else if (captureAt == theBoard.getEnPassantTargetSq()) {
				capturePiece = Colour.isBlack(ownSide) ? Piece.WHITE_PAWN : Piece.BLACK_PAWN;
				// promotion can't be possible if en passant capture
				moveList.add( Move.valueOf(Move.TYPE_EN_PASSANT_CAPTURE_MASK, atSquare, ownPiece, captureAt, capturePiece, Piece.NONE));
			}
		}
		captureAt = pawn_genRightCaptureTarget(atSquare, ownSide);
		if ( captureAt != Position.NOPOSITION ) {
			capturePiece = pawn_isCapturable(ownSide, theBoard, captureAt);
			if (capturePiece != Piece.NONE) {
				pawn_checkPromotionAddMove(ownPiece, theBoard, atSquare, ownSide, moveList, captureAt, capturePiece);
			} else if (captureAt == theBoard.getEnPassantTargetSq()) {
				capturePiece = Colour.isBlack(ownSide) ? Piece.WHITE_PAWN : Piece.BLACK_PAWN;
				// promotion can't be possible if en passant capture
				moveList.add( Move.valueOf(Move.TYPE_EN_PASSANT_CAPTURE_MASK, atSquare, ownPiece, captureAt, capturePiece, Piece.NONE));
			}
		}
		return moveList;
	}		
}
