package eubos.search.transposition;

import java.util.List;

import com.fluxchess.jcpi.models.GenericMove;

import eubos.position.MoveList;
import eubos.position.Move;
import eubos.search.Score;
import eubos.search.Score.ScoreType;

public class Transposition {
	private byte depthSearchedInPly;
	private short score;
	private MoveList ml;
	private int bestMove;
	private ScoreType scoreType;
	private List<Integer> pv;

	public Transposition(byte depth, short score, ScoreType scoreType, MoveList ml, GenericMove bestMove) {
		// Only used by tests
		this(depth, score, scoreType, ml, Move.toMove(bestMove, null, Move.TYPE_NONE), null);
	}
	
	public Transposition(byte depth, short score, ScoreType scoreType, MoveList ml, int bestMove, List<Integer> pv) {
		setMoveList(ml);
		setDepthSearchedInPly(depth);
		setScore(score);
		setScoreType(scoreType);
		setBestMove(bestMove);
		setPv(pv);
	}
	
	public Transposition(byte depth, Score score, MoveList ml, int bestMove, List<Integer> pv) {
		this(depth, score.getScore(), score.getType(), ml, bestMove, pv);
	}

	public MoveList getMoveList() {
		return ml;
	}
	
	public ScoreType getScoreType() {
		return scoreType;
	}

	public void setScoreType(ScoreType scoreType) {
		this.scoreType = scoreType;
	}

	public short getScore() {
		return score;
	}

	public void setScore(short score) {
		this.score = score;
	}

	public byte getDepthSearchedInPly() {
		return depthSearchedInPly;
	}

	public void setDepthSearchedInPly(byte depthSearchedInPly) {
		this.depthSearchedInPly = depthSearchedInPly;
	}

	public int getBestMove() {
		return bestMove;
	}
	
	public void setBestMove(int bestMove) {
		if (!Move.areEqual(this.bestMove, bestMove)) {
			this.bestMove = bestMove;
			if (bestMove != 0) {
				this.ml.reorderWithNewBestMove(bestMove);
			}
		}
	}
	
	void setMoveList(MoveList new_ml) {
		this.ml = new_ml;		
	}
	
	public List<Integer> getPv() {
		return pv;
	}

	public void setPv(List<Integer> pv) {
		if (pv != null && pv.size() > depthSearchedInPly) {
			pv.subList(depthSearchedInPly, pv.size()).clear();
		}
		this.pv = pv;
	}
	
	public String report() {
		String onward_pv = "";
		if (pv != null) {
			for (int move : pv) {
				onward_pv += String.format("%s, ", Move.toString(move));
			}
		}
		String output = String.format("trans best=%s, dep=%d, sc=%d, type=%s, pv=%s", 
				Move.toString(bestMove),
				depthSearchedInPly,
				score,
				scoreType,
				onward_pv);
		return output; //ml: " + ml + " ref:" + Integer.toHexString(System.identityHashCode(ml));
	}
	
	public void update(
			byte new_Depth, 
			short new_score, 
			ScoreType new_bound, 
			MoveList new_ml, 
			int new_bestMove, 
			List<Integer> pv) {
		// order is important because setBestMove uses ml, also setPv uses depth
		setMoveList(new_ml);
		setDepthSearchedInPly(new_Depth);
		setScoreType(new_bound);
		setScore(new_score);
		setBestMove(new_bestMove);
		setPv(pv);
	}
}
