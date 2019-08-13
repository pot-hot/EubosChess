package eubos.position;

import java.util.LinkedList;
import java.util.List;

import com.fluxchess.jcpi.models.GenericMove;

public class Transposition {
	private GenericMove bestMove;
	private byte depthSearchedInPly;
	private short score;
	private List<GenericMove> ml;
	
	public List<GenericMove> getMoveList() {
		if (ml != null && bestMove != null) {
			List<GenericMove> ordered_ml = new LinkedList<GenericMove>();
			ordered_ml.addAll(ml);
			ordered_ml.remove(bestMove);
			ordered_ml.add(0, bestMove);
			return ordered_ml;
		}
		return ml;
	}

	public void setMoveList(List<GenericMove> ml) {
		this.ml = ml;
	}

	public enum ScoreType { 
		exact, upperBound, lowerBound;
	};
	private ScoreType scoreType;	
	
	public Transposition(GenericMove best, byte depth, short score, ScoreType scoreType, List<GenericMove> ml) {
		setBestMove(best);
		this.setDepthSearchedInPly(depth);
		this.setScore(score);
		this.setScoreType(scoreType);
		this.ml = ml;
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

	public GenericMove getBestMove() {
		return bestMove;
	}

	public void setBestMove(GenericMove bestMove) {
		this.bestMove = bestMove;
	}
}
