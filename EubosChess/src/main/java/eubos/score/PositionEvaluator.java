package eubos.score;

import java.util.PrimitiveIterator;

import com.fluxchess.jcpi.models.IntFile;

import eubos.board.Board;
import eubos.board.Piece;
import eubos.board.SquareAttackEvaluator;
import eubos.board.Piece.Colour;
import eubos.position.CaptureData;
import eubos.position.Position;
import eubos.position.PositionManager;
import eubos.search.DrawChecker;
import eubos.search.SearchContext;

public class PositionEvaluator implements IEvaluate {

	PositionManager pm;
	private SearchContext sc;
	private DrawChecker dc;
	
	public static final int HAS_CASTLED_BOOST_CENTIPAWNS = 50;
	public static final int DOUBLED_PAWN_HANDICAP = 50;
	public static final int PASSED_PAWN_BOOST = 30;
	public static final int ROOK_FILE_PASSED_PAWN_BOOST = 20;
	
	public static final boolean DISABLE_QUIESCENCE_CHECK = false; 
	
	public PositionEvaluator(PositionManager pm, DrawChecker dc) {	
		this.pm = pm;
		sc = new SearchContext(pm, MaterialEvaluator.evaluate(pm.getTheBoard(), false), dc);
		this.dc = dc;
	}
	
	public boolean isQuiescent() {
		if (DISABLE_QUIESCENCE_CHECK)
			return true;
		if (pm.lastMoveWasCheck()) {
			return false;
		} else if (pm.lastMoveWasPromotion() || pm.isPromotionPossible()) {
			return false;
		} else if (pm.lastMoveWasCapture()) {
			// we could keep a capture list, so we know where we are in the exchange series?
			// we can get access to the captured piece in the current codebase, but we need to know the whole capture sequence to do swap off?
			CaptureData captured = pm.getCapturedPiece();
			if (captured != null)
			{
				if (SquareAttackEvaluator.isAttacked(
						pm.getTheBoard(),
						captured.getSquare(),
						Colour.getOpposite(pm.getOnMove())))
					return false;
			}
		}
		return true;
	}
	
	public short evaluatePosition() {
		MaterialEvaluation mat = MaterialEvaluator.evaluate(pm.getTheBoard(), sc.isEndgame());
		short score = mat.getDelta();
		score += sc.computeSearchGoalBonus(mat);
		score += evaluatePawnStructure();
		return score;
	}
	
	int encourageCastling() {
		int castleScoreBoost = 0;
		Colour onMoveWas = Colour.getOpposite(pm.getOnMove());
		if (pm.hasCastled(onMoveWas)) {
			castleScoreBoost = HAS_CASTLED_BOOST_CENTIPAWNS;
		}
		if (Colour.isBlack(onMoveWas)) {
			castleScoreBoost = -castleScoreBoost;
		}
		return castleScoreBoost;
	}
	
	int evaluatePawnStructure() {
		int pawnEvaluationScore = evaluatePawnsForColour(pm.getOnMove());
		pawnEvaluationScore += evaluatePawnsForColour(Colour.getOpposite(pm.getOnMove()));
		return pawnEvaluationScore;
	}

	private int evaluatePawnsForColour(Colour onMoveWas) {
		Board board = pm.getTheBoard();
		int passedPawnBoost = 0;
		int pawnHandicap = -board.countDoubledPawnsForSide(onMoveWas)*DOUBLED_PAWN_HANDICAP;
		int ownPawns = Colour.isWhite(onMoveWas) ? Piece.WHITE_PAWN : Piece.BLACK_PAWN;
		PrimitiveIterator.OfInt iter = board.iterateType(ownPawns);
		while (iter.hasNext()) {
			int pawn = iter.nextInt();
			if (board.isPassedPawn(pawn, onMoveWas)) {
				if (Position.getFile(pawn) == IntFile.Fa || Position.getFile(pawn) == IntFile.Fh) {
					passedPawnBoost += ROOK_FILE_PASSED_PAWN_BOOST;
				} else {
					passedPawnBoost += PASSED_PAWN_BOOST;
				}
			}
		}
		if (Colour.isBlack(onMoveWas)) {
			pawnHandicap = -pawnHandicap;
			passedPawnBoost = -passedPawnBoost;
		}
		return pawnHandicap + passedPawnBoost;
	}

	public MaterialEvaluation getMaterialEvaluation() {
		return MaterialEvaluator.evaluate(pm.getTheBoard(), sc.isEndgame());
	}

	@Override
	public boolean isThreeFoldRepetition(Long hashCode) {
		return dc.isPositionDraw(hashCode);
	}
	
	@Override
	public boolean couldLeadToThreeFoldRepetiton(Long hashCode) {
		return dc.isPositionOpponentCouldClaimDraw(hashCode);
	}
	
	public SearchContext getSearchContext() {
		return this.sc;
	}
	
	public boolean isInsufficientMaterial() {
		Board board = pm.getTheBoard();
		return board.isInsufficientMaterial();			
	}
}
