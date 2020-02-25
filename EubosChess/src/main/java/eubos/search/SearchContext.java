package eubos.search;

import eubos.board.Piece;
import eubos.board.Piece.Colour;
import eubos.position.IPositionAccessors;
import eubos.score.MaterialEvaluation;
import eubos.score.MaterialEvaluator;

public class SearchContext {
	MaterialEvaluation initial;
	IPositionAccessors pos;
	Piece.Colour initialOnMove;
	SearchGoal goal;
	DrawChecker dc;
	boolean isEndgame;
	
	static final short SIMPLIFY_THRESHOLD = 100;
	static final short DRAW_THRESHOLD = -200;
	
	static final short SIMPLIFICATION_BONUS = 75;
	static final short AVOID_DRAW_HANDICAP = -400;
	static final short ACHIEVES_DRAW_BONUS = MaterialEvaluator.MATERIAL_VALUE_KING/2;
	
	static final boolean ALWAYS_TRY_FOR_WIN = false;
	
	private enum SearchGoal {
		try_for_win,
		simplify,
		try_for_draw
	};
	
	public SearchContext(IPositionAccessors pos, MaterialEvaluation initialMaterial, DrawChecker dc) {
		this.pos = pos;
		this.dc = dc;
		initial = initialMaterial;
		initialOnMove = pos.getOnMove();
		boolean queensOffBoard = pos.getTheBoard().getWhiteQueens().isZero() && pos.getTheBoard().getBlackQueens().isZero();
		boolean materialQuantityThreshholdReached = initialMaterial.getWhite() <= 5200 && initialMaterial.getBlack() <= 5200;
		if (queensOffBoard || materialQuantityThreshholdReached) {
			isEndgame = true;
		}
		setGoal();
	}

	private void setGoal() {
		if (ALWAYS_TRY_FOR_WIN) {
			goal = SearchGoal.try_for_win;
		} else if ((Colour.isWhite(initialOnMove) && initial.getDelta() > SIMPLIFY_THRESHOLD) ||
			(Colour.isBlack(initialOnMove) && initial.getDelta() < -SIMPLIFY_THRESHOLD )) {
			goal = SearchGoal.simplify;
		} else if ((Colour.isWhite(initialOnMove) && initial.getDelta() < DRAW_THRESHOLD) ||
				(Colour.isBlack(initialOnMove) && initial.getDelta() > -DRAW_THRESHOLD )) {
			goal = SearchGoal.try_for_draw;
		} else {
			goal = SearchGoal.try_for_win;
		}
	}
	
	public boolean isTryForDraw() {
		return goal == SearchGoal.try_for_draw; 
	}
	
	public short computeSearchGoalBonus(MaterialEvaluation current) {
		short bonus = 0;
		// If we just moved, score as according to our game plan
		if (pos.getOnMove().equals(Colour.getOpposite(initialOnMove))) {
			switch(goal) {
			case simplify:
				if (dc.isPositionDraw(pos.getHash())) {
					bonus += AVOID_DRAW_HANDICAP;
				} else if (isPositionSimplified(current)) {
					bonus += SIMPLIFICATION_BONUS;
				}
				break;
			case try_for_win:
				if (dc.isPositionDraw(pos.getHash())) {
					bonus += AVOID_DRAW_HANDICAP;
				}
				break;
			case try_for_draw:
				if (dc.isPositionDraw(pos.getHash())) {
					bonus += ACHIEVES_DRAW_BONUS;
				}
				break;
			default:
				break;
			}
			if (Colour.isBlack(initialOnMove)) {
				bonus = (short)-bonus;
			}
		} else {
			switch(goal) {
			case simplify:
			case try_for_win:
				if (dc.isPositionDraw(pos.getHash())) {
					// Assume opponent wants a draw.
					bonus += ACHIEVES_DRAW_BONUS;
				}
				break;
			case try_for_draw:
				// If we are trying for a draw and evaluating opponents move, score a draw as bad for them
				if (dc.isPositionDraw(pos.getHash())) {
					bonus += AVOID_DRAW_HANDICAP;
				}
				break;
			default:
				break;
			}
			// we are evaluating after the move, so if opponent is black, invert score
			if (Colour.isWhite(pos.getOnMove())) {
				bonus = (short)-bonus;
			}
		}
		return bonus;
	}
	
	private boolean isPositionSimplified(MaterialEvaluation current) {
		boolean isSimplification = false;
		if (Colour.isWhite(initialOnMove)) {
			if ((initial.getDelta() <= current.getDelta()) && initial.getWhite() > current.getWhite()) {
				isSimplification = true;
			}
		} else {
			if ((initial.getDelta() >= current.getDelta()) && initial.getBlack() > current.getBlack()) {
				isSimplification = true;
			}			
		}
		return isSimplification;
	}

	public boolean isEndgame() {
		// Could make this update for when we enter endgame as a consequence of moves applied during the search.
		return isEndgame;
	}
}
