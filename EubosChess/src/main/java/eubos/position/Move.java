package eubos.position;

import com.fluxchess.jcpi.models.GenericMove;
import com.fluxchess.jcpi.models.IntChessman;

import eubos.board.Board;
import eubos.board.Piece;

/* This class represents a move as a integer primitive value. */
public final class Move {
	
	private static final int ORIGINPOSITION_SHIFT = 0;
	private static final int ORIGINPOSITION_MASK = Position.MASK << ORIGINPOSITION_SHIFT;
	
	private static final int TARGETPOSITION_SHIFT = ORIGINPOSITION_SHIFT+7;
	private static final int TARGETPOSITION_MASK = Position.MASK << TARGETPOSITION_SHIFT;
	
	private static final int PROMOTION_SHIFT = TARGETPOSITION_SHIFT+7;
	private static final int PROMOTION_MASK = IntChessman.MASK << PROMOTION_SHIFT;
	
	private static final int ORIGIN_PIECE_SHIFT = PROMOTION_SHIFT+3;
	private static final int ORIGIN_PIECE_MASK = Piece.PIECE_WHOLE_MASK << ORIGIN_PIECE_SHIFT;
	
	public static final int TYPE_NONE = 0;
	
	public static final int TYPE_REGULAR_BIT = 0;
	public static final int TYPE_CASTLE_BIT = 1;
	public static final int TYPE_CHECK_BIT = 2;
	public static final int TYPE_CAPTURE_PAWN_BIT = 3;
	public static final int TYPE_CAPTURE_PIECE_BIT = 4;
	public static final int TYPE_CAPTURE_ROOK_BIT = 5;
	public static final int TYPE_CAPTURE_QUEEN_BIT = 6;
	public static final int TYPE_PROMOTION_PIECE_BIT = 7;
	public static final int TYPE_PROMOTION_ROOK_BIT = 8;
	public static final int TYPE_PROMOTION_QUEEN_BIT = 9;
	public static final int TYPE_WIDTH = TYPE_PROMOTION_QUEEN_BIT + 1;
	
	public static final int TYPE_PROMOTION_QUEEN_MASK = (0x1 << TYPE_PROMOTION_QUEEN_BIT);
	public static final int TYPE_PROMOTION_ROOK_MASK = (0x1 << TYPE_PROMOTION_ROOK_BIT);
	public static final int TYPE_PROMOTION_PIECE_MASK = (0x1 << TYPE_PROMOTION_PIECE_BIT);
	public static final int TYPE_CAPTURE_QUEEN_MASK = (0x1 << TYPE_CAPTURE_QUEEN_BIT);
	public static final int TYPE_CAPTURE_ROOK_MASK = (0x1 << TYPE_CAPTURE_ROOK_BIT);
	public static final int TYPE_CAPTURE_PIECE_MASK = (0x1 << TYPE_CAPTURE_PIECE_BIT);
	public static final int TYPE_CAPTURE_PAWN_MASK = (0x1 << TYPE_CAPTURE_PAWN_BIT);
	public static final int TYPE_CHECK_MASK = (0x1 << TYPE_CHECK_BIT);
	public static final int TYPE_CASTLE_MASK = (0x1 << TYPE_CASTLE_BIT);
	public static final int TYPE_REGULAR_MASK = (0x1 << TYPE_REGULAR_BIT);
	
	private static final int TYPE_SHIFT = ORIGIN_PIECE_SHIFT+4;
	private static final int TYPE_MASK = ((1<<TYPE_WIDTH)-1) << TYPE_SHIFT;
	
	//private static final int TARGET_PIECE_SHIFT = 25;
	//private static final int TARGET_PIECE_MASK = Piece.PIECE_WHOLE_MASK << TARGET_PIECE_SHIFT;
	
	public static final int NULL_MOVE =
			valueOf(TYPE_NONE, Position.a1, Piece.NONE, Position.a1, Piece.NONE, IntChessman.NOCHESSMAN);
	
	private Move() {
	}
	
	public static int valueOf(int originPosition, int originPiece, int targetPosition, int targetPiece) {
		return Move.valueOf(Move.TYPE_NONE, originPosition, originPiece, targetPosition, targetPiece, IntChessman.NOCHESSMAN);
	}

	public static int valueOf(int type, int originPosition, int originPiece, int targetPosition, int targetPiece, int promotion) {
		int move = 0;

		// Encode move classification
		assert (type & ~(Move.TYPE_MASK >>> TYPE_SHIFT)) == 0;
		move |= type << TYPE_SHIFT;

		// Encode origin position
		assert (originPosition & 0x88) == 0;
		move |= originPosition << ORIGINPOSITION_SHIFT;
		
		// Encode Origin Piece
		assert (originPiece & ~Piece.PIECE_WHOLE_MASK) == 0;
		move |= originPiece << ORIGIN_PIECE_SHIFT;

		// Encode target position
		assert (targetPosition & 0x88) == 0;
		move |= targetPosition << TARGETPOSITION_SHIFT;

		// Encode Target Piece
		/*
		assert (targetPiece & ~Piece.PIECE_WHOLE_MASK) == 0;
		move |= targetPiece << TARGET_PIECE_SHIFT;
		*/
		
		// Encode promotion
		assert (IntChessman.isValid(promotion) && IntChessman.isValidPromotion(promotion))
		|| promotion == IntChessman.NOCHESSMAN;
		move |= promotion << PROMOTION_SHIFT;

		return move;
	}
	
	public static int toMove(GenericMove move, Board theBoard, int type) {
		int intMove = 0;
		int targetPosition = Position.valueOf(move.to);
		int originPosition = Position.valueOf(move.from);
		int promotion = IntChessman.NOCHESSMAN;
		int originPiece = Piece.NONE;
		int targetPiece = Piece.NONE;
		if (theBoard != null) {
			originPiece = theBoard.getPieceAtSquare(originPosition);
			targetPiece = theBoard.getPieceAtSquare(targetPosition);
		}
		if (move.promotion != null) {
			promotion = IntChessman.valueOf(move.promotion);
			intMove = Move.valueOf(Move.TYPE_PROMOTION_PIECE_MASK, originPosition, originPiece, targetPosition, targetPiece, promotion);
		} else {
			intMove = Move.valueOf(type, originPosition, originPiece, targetPosition, targetPiece, promotion);
		}
		return intMove;
	}
	
	public static int toMove(GenericMove move, Board theBoard) {
		return Move.toMove(move, theBoard, Move.TYPE_NONE);
	}
	
	public static int toMove(GenericMove move) {
		return Move.toMove(move, null, Move.TYPE_NONE);
	}
	
	public static boolean isPromotion(int move) {
		return (getType(move) & (Move.TYPE_PROMOTION_QUEEN_MASK | Move.TYPE_PROMOTION_ROOK_MASK | Move.TYPE_PROMOTION_PIECE_MASK)) != 0;			
	}
	
	public static boolean isCapture(int move) {
		return (getType(move) & (Move.TYPE_CAPTURE_QUEEN_MASK | Move.TYPE_CAPTURE_ROOK_MASK | Move.TYPE_CAPTURE_PIECE_MASK | Move.TYPE_CAPTURE_PAWN_MASK)) != 0;			
	}
	
	public static boolean isCheck(int move) {
		return (getType(move) & Move.TYPE_CHECK_MASK) != 0;
	}
	
	public static boolean isRegular(int move) {
		return (getType(move) & (Move.TYPE_REGULAR_MASK | Move.TYPE_CASTLE_MASK)) != 0;
	}

	public static GenericMove toGenericMove(int move) {
		if (move == Move.NULL_MOVE)
			return null;
		
		int originPosition = getOriginPosition(move);
		int targetPosition = getTargetPosition(move);

		if (isPromotion(move)) {
			return new GenericMove(
					Position.toGenericPosition(originPosition),
					Position.toGenericPosition(targetPosition),
					IntChessman.toGenericChessman(getPromotion(move)));
		} else {
			return new GenericMove(
					Position.toGenericPosition(originPosition),
					Position.toGenericPosition(targetPosition));
		}
	}
	
	public static boolean areEqual(int move1, int move2) {
		boolean areEqual = false;
		if (Move.getOriginPosition(move1)==Move.getOriginPosition(move2) &&
			Move.getTargetPosition(move1)==Move.getTargetPosition(move2) &&
			Move.getPromotion(move1)==Move.getPromotion(move2)) {
			areEqual = true;
		}
		return areEqual;
	}

	public static int getType(int move) {
		int type = (move & TYPE_MASK) >>> TYPE_SHIFT;

		assert (type & ~(Move.TYPE_MASK >>> TYPE_SHIFT)) == 0;

		return type;
	}

	public static int getOriginPosition(int move) {
		int originPosition = (move & ORIGINPOSITION_MASK) >>> ORIGINPOSITION_SHIFT;
		assert (originPosition & 0x88) == 0;

		return originPosition;
	}
	
	private static int setOriginPosition(int move, int originPosition) {
		// Zero out origin position
		move &= ~ORIGINPOSITION_MASK;

		// Encode origin position
		assert (originPosition & 0x88) == 0;
		move |= originPosition << ORIGINPOSITION_SHIFT;

		return move;
	}

	public static int getTargetPosition(int move) {
		int targetPosition = (move & TARGETPOSITION_MASK) >>> TARGETPOSITION_SHIFT;
		assert (targetPosition & 0x88) == 0;

		return targetPosition;
	}

	public static int setTargetPosition(int move, int targetPosition) {
		// Zero out target position
		move &= ~TARGETPOSITION_MASK;

		// Encode target position
		assert (targetPosition & 0x88) == 0;
		move |= targetPosition << TARGETPOSITION_SHIFT;

		return move;
	}

	public static int getPromotion(int move) {
		int promotion = (move & PROMOTION_MASK) >>> PROMOTION_SHIFT;
		if (move != 0) {
			assert (IntChessman.isValid(promotion) && IntChessman.isValidPromotion(promotion))
			|| promotion == IntChessman.NOCHESSMAN;
		}
		return promotion;
	}

	public static int setPromotion(int move, int promotion) {
		// Zero out promotion chessman
		move &= ~PROMOTION_MASK;

		// Encode promotion
		assert (IntChessman.isValid(promotion) && IntChessman.isValidPromotion(promotion))
		|| promotion == IntChessman.NOCHESSMAN;
		move |= promotion << PROMOTION_SHIFT;

		return move;
	}
	
	public static int getOriginPiece(int move) {
		int piece = (move & ORIGIN_PIECE_MASK) >>> ORIGIN_PIECE_SHIFT;
		//assert (piece & Piece.PIECE_NO_COLOUR_MASK) != Piece.PIECE_NONE;
		
		return piece;
	}

	public static int setOriginPiece(int move, int piece) {
		//assert (piece & Piece.PIECE_NO_COLOUR_MASK) != Piece.PIECE_NONE;
		
		move &= ~ORIGIN_PIECE_MASK;
		move |= piece << ORIGIN_PIECE_SHIFT;
		return move;
	}
	
	/*public static int getTargetPiece(int move) {
		int piece = (move & TARGET_PIECE_MASK) >>> TARGET_PIECE_SHIFT;
		//assert (piece & Piece.PIECE_NO_COLOUR_MASK) != Piece.PIECE_NONE;
		
		return piece;
	}

	public static int setTargetPiece(int move, int piece) {
		//assert (piece & Piece.PIECE_NO_COLOUR_MASK) != Piece.PIECE_NONE;
		
		move &= ~TARGET_PIECE_MASK;
		move |= piece << TARGET_PIECE_SHIFT;
		return move;
	}*/
	
	public static String toString(int move) {
		StringBuilder string = new StringBuilder();
		if (move != Move.NULL_MOVE) {
			string.append(toGenericMove(move).toString());

			if (isPromotion(move)) {
				string.append(":");
				string.append(IntChessman.toGenericChessman(getPromotion(move)));
			}
			
			if (getOriginPiece(move) != Piece.NONE) {
				string.append(":");
				string.append(Piece.toFenChar(getOriginPiece(move)));
			}
		}
		return string.toString();
	}

	public static int setType(int move, int type) {
		// Zero out type
		move &= ~TYPE_MASK;
		
		assert ((type<<TYPE_SHIFT) & ~Move.TYPE_MASK) == 0;
		
		return move |= type << TYPE_SHIFT;
	}
	
	public static int reverse(int move) {
		int reversedMove = move;
		reversedMove = Move.setTargetPosition(reversedMove, Move.getOriginPosition(move));
		reversedMove = Move.setOriginPosition(reversedMove, Move.getTargetPosition(move));
		return reversedMove;
	}

	public static boolean isQueenPromotion(int move) {
		return ((getType(move) & Move.TYPE_PROMOTION_QUEEN_MASK) != 0);
	}

}
