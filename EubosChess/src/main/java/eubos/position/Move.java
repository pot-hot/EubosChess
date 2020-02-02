package eubos.position;

import com.fluxchess.jcpi.models.GenericMove;
import com.fluxchess.jcpi.models.IntChessman;

/**
 * This class represents a move as a int value. The fields are represented by
 * the following bits.
 * <p/>
 *  0 -  3: the type (required)
 *  4 - 10: the origin position (required)
 * 11 - 17: the target position (required)
 * 18 - 20: the promotion chessman (optional)
 */
public final class Move {
	
	public static final int TYPE_PROMOTION_AND_CAPTURE_WITH_CHECK = 0;
	public static final int TYPE_PROMOTION_AND_CAPTURE = 1;
	public static final int TYPE_PROMOTION = 2;
	public static final int TYPE_KBR_PROMOTION = 3;
	public static final int TYPE_CAPTURE_WITH_CHECK = 4;
	public static final int TYPE_CAPTURE_QUEEN = 5;
	public static final int TYPE_CAPTURE_ROOK = 6;
	public static final int TYPE_CAPTURE_PIECE = 7;
	public static final int TYPE_CAPTURE_PAWN = 8;
	public static final int TYPE_CASTLE = 9;
	public static final int TYPE_CHECK = 10;
	public static final int TYPE_REGULAR = 11;
	public static final int TYPE_NONE = 12;
	
	private static final int TYPE_SHIFT = 0;
	private static final int TYPE_MASK = 0xF << TYPE_SHIFT;
	
	private static final int ORIGINPOSITION_SHIFT = 4;
	private static final int ORIGINPOSITION_MASK = Position.MASK << ORIGINPOSITION_SHIFT;
	private static final int TARGETPOSITION_SHIFT = 11;
	private static final int TARGETPOSITION_MASK = Position.MASK << TARGETPOSITION_SHIFT;
	private static final int PROMOTION_SHIFT = 18;
	private static final int PROMOTION_MASK = IntChessman.MASK << PROMOTION_SHIFT;

	private Move() {
	}
	
	public static int valueOf(int originPosition, int targetPosition)
	{
		return Move.valueOf(Move.TYPE_NONE, originPosition, targetPosition, IntChessman.NOCHESSMAN);
	}
	
	public static int valueOf(int originPosition, int targetPosition, int promotion)
	{
		return Move.valueOf(Move.TYPE_NONE, originPosition, targetPosition, promotion);
	}

	public static int valueOf(int type, int originPosition, int targetPosition, int promotion) {
		int move = 0;

		// Encode move classification
		assert (type >= Move.TYPE_PROMOTION_AND_CAPTURE_WITH_CHECK || type <= Move.TYPE_NONE);
		move |= type << TYPE_SHIFT;

		// Encode origin position
		assert (originPosition & 0x88) == 0;
		move |= originPosition << ORIGINPOSITION_SHIFT;

		// Encode target position
		assert (targetPosition & 0x88) == 0;
		move |= targetPosition << TARGETPOSITION_SHIFT;

		// Encode promotion
		assert (IntChessman.isValid(promotion) && IntChessman.isValidPromotion(promotion))
		|| promotion == IntChessman.NOCHESSMAN;
		move |= promotion << PROMOTION_SHIFT;

		return move;
	}
	
	public static int toMove(GenericMove move, int type) {
		int intMove = 0;
		int targetPosition = Position.valueOf(move.to);
		int originPosition = Position.valueOf(move.from);
		int promotion = 0;
		if (move.promotion != null) {
			promotion = IntChessman.valueOf(move.promotion);
			intMove = Move.valueOf(Move.TYPE_KBR_PROMOTION, originPosition, targetPosition, promotion);
		} else {
			promotion = IntChessman.NOCHESSMAN;
			intMove = Move.valueOf(type, originPosition, targetPosition, promotion);
		}
		return intMove;
	}

	public static GenericMove toGenericMove(int move) {
		if (move == 0)
			return null;
		
		int type = getType(move);
		int originPosition = getOriginPosition(move);
		int targetPosition = getTargetPosition(move);

		if (type > Move.TYPE_KBR_PROMOTION) {
			return new GenericMove(
					Position.toGenericPosition(originPosition),
					Position.toGenericPosition(targetPosition));
		} else {
			return new GenericMove(
					Position.toGenericPosition(originPosition),
					Position.toGenericPosition(targetPosition),
					IntChessman.toGenericChessman(getPromotion(move)));
		}
	}
	
	public static boolean areEqual(int move1, int move2) {
		boolean areEqual = false;
		//if (Move.getOriginPosition(move1)==Move.getOriginPosition(move2) &&
		//	Move.getTargetPosition(move1)==Move.getTargetPosition(move2) &&
		//	Move.getPromotion(move1)==Move.getPromotion(move2)) {
		//	areEqual = true;
		//}
		move1 &= ~TYPE_MASK;
		move2 &= ~TYPE_MASK;
		if (move1==move2) {
			areEqual = true;
		}
		return areEqual;
	}

	public static int getType(int move) {
		int type = (move & TYPE_MASK) >>> TYPE_SHIFT;

		assert (type >= Move.TYPE_PROMOTION_AND_CAPTURE_WITH_CHECK || type <= Move.TYPE_NONE);

		return type;
	}

	public static int getOriginPosition(int move) {
		int originPosition = (move & ORIGINPOSITION_MASK) >>> ORIGINPOSITION_SHIFT;
		assert (originPosition & 0x88) == 0;

		return originPosition;
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
		assert (IntChessman.isValid(promotion) && IntChessman.isValidPromotion(promotion))
		|| promotion == IntChessman.NOCHESSMAN;

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

	public static String toString(int move) {
		String string = "";
		if (move != 0) {
			string += toGenericMove(move).toString();

			if (getType(move) <= Move.TYPE_KBR_PROMOTION) {
				string += ":";
				string += IntChessman.toGenericChessman(getPromotion(move));
			}
		}
		return string;
	}

	public static int toMove(GenericMove move) {
		return Move.toMove(move, Move.TYPE_NONE);
	}

	public static int setType(int move, int type) {
		// Zero out type
		move &= ~TYPE_MASK;
		
		assert (type >= Move.TYPE_PROMOTION_AND_CAPTURE_WITH_CHECK || type <= Move.TYPE_NONE);
		
		return move |= type << TYPE_SHIFT;
	}

}
