package eubos.search;

import eubos.board.Board;
import eubos.board.Piece;
import eubos.board.Piece.Colour;
import eubos.position.IPositionAccessors;
import eubos.score.PiecewiseEvaluation;
import eubos.score.ReferenceScore;

public class SearchContext {
	PiecewiseEvaluation initial;
	IPositionAccessors pos;
	Piece.Colour initialOnMove;
	SearchGoal goal;
	
	static final short SIMPLIFY_THRESHOLD = 100;
	static final short DRAW_THRESHOLD = -150;
	
	static final short SIMPLIFICATION_BONUS = 75;
	static final short AVOID_DRAW_HANDICAP = -150;
	static final short ACHIEVES_DRAW_BONUS = Board.MATERIAL_VALUE_KING/2;
	
	static final boolean ALWAYS_TRY_FOR_WIN = false;
	
	private enum SearchGoal {
		try_for_mate,
		try_for_win,
		simplify,
		try_for_draw
	};
	
	public SearchContext(IPositionAccessors pos, PiecewiseEvaluation initialMaterial, ReferenceScore refScore) {
		this.pos = pos;
		// Make a copy of the initial Material Evaluation and store it here
		initial = new PiecewiseEvaluation(initialMaterial.getWhite(), initialMaterial.getBlack(), initialMaterial.getPosition());
		initialOnMove = pos.getOnMove();
		setGoal(refScore);
	}

	private void setGoal(ReferenceScore refScore) {
		goal = SearchGoal.try_for_win;
		if (!ALWAYS_TRY_FOR_WIN) {
			if (pos.getTheBoard().isInsufficientMaterial(Colour.getOpposite(initialOnMove))) {
				// When opponent can't win
				goal = SearchGoal.try_for_mate;
				// consider cleaning hash table when we first get this goal?
				// otherwise we can get a problem where prior hash scores exist which can have e.g. try_for_draw scores in them, causing errors!
				// see this position - 8/1P6/2K2k2/8/4n3/8/8/8 w - - - 75
				
			} else if (pos.getTheBoard().isInsufficientMaterial(initialOnMove) ||
					   (refScore != null && refScore.getReference().score < DRAW_THRESHOLD)) {
				// When we can't win
				goal = SearchGoal.try_for_draw;
				
			} else if (refScore != null && 
					   (!pos.getTheBoard().isEndgame && refScore.getReference().score > SIMPLIFY_THRESHOLD) ||
					   (pos.getTheBoard().isEndgame && refScore.getReference().score < 0)) {
				// When simplification possible, but don't oversimplify in the endgame, if winning. Can simplify endgame if losing.
				goal = SearchGoal.simplify;
			} else {
				// Default; try_for_win
			}
		}
	}
	
	public boolean isTryForDraw() {
		return goal == SearchGoal.try_for_draw; 
	}
		
	public class SearchContextEvaluation {
		public boolean isDraw;
		public short score;
		
		public SearchContextEvaluation() {
			isDraw = false;
			score = 0;
		}
	}
	
	public SearchContextEvaluation computeSearchGoalBonus(PiecewiseEvaluation current) {
		Piece.Colour opponent = Colour.getOpposite(initialOnMove);
		SearchContextEvaluation eval = new SearchContextEvaluation();
		
		boolean threeFold = pos.isThreefoldRepetitionPossible();
		boolean insufficient = pos.getTheBoard().isInsufficientMaterial();
		if (threeFold || insufficient) {
			eval.isDraw  = true;
		}
		
		// If we just moved, score as according to our goal
		if (pos.getOnMove().equals(opponent)) {
			switch(goal) {
			case simplify: 
			    if (!eval.isDraw) {
			    	if (isPositionSimplified(current)) {
				    	eval.score = SIMPLIFICATION_BONUS;
				    	eval.score = adjustScoreIfBlack(eval.score);
			    	}
				    // Add on positional weightings
				    eval.score += current.getPosition();
				}
				break;
			case try_for_draw:
				if (insufficient) {
					eval.score = ACHIEVES_DRAW_BONUS;
					eval.score = adjustScoreIfBlack(eval.score);
				} else if (!eval.isDraw) {
				    // Add on positional weightings
				    eval.score += current.getPosition();
				} else {
					// do nothing if a draw
				}
				break;
			case try_for_win:
				if (!eval.isDraw) {
					// Add on positional weightings
					eval.score += current.getPosition();
			    }
				break;
			case try_for_mate:
				// Don't add on positional factors to save aimless faffing about board
				break;
			default:
				break;
			}
		} else {
			eval.score += current.getPosition();
		}
		return eval;
	}
	
	private boolean isPositionSimplified(PiecewiseEvaluation current) {
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
	
	public short getScoreForStalemate() {
		short mateScore = isTryForDraw() ? ACHIEVES_DRAW_BONUS : -ACHIEVES_DRAW_BONUS;
		return adjustScoreIfBlack(mateScore);	
	}
	
	private short adjustScoreIfBlack(short score) {
		if (Colour.isBlack(initialOnMove)) {
			score = (short) -score;
		}
		return score;
	}

	public String getGoal() {
		return goal.toString();
	}
}
