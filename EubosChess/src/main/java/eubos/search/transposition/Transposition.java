package eubos.search.transposition;

import com.fluxchess.jcpi.models.GenericMove;

import eubos.position.MoveList;

public class Transposition {
	private byte depthSearchedInPly;
	private short score;
	private MoveList ml;
	private GenericMove bestMove;

	public enum ScoreType { 
		exact, upperBound, lowerBound;
	};
	private ScoreType scoreType;

	public Transposition(byte depth, short score, ScoreType scoreType, MoveList ml, GenericMove bestMove) {
		setDepthSearchedInPly(depth);
		setScore(score);
		setScoreType(scoreType);
		setBestMove(bestMove);
		this.ml = ml;
	}

	public MoveList getMoveList() {
		if (ml != null) {
			ml.reorderWithNewBestMove(bestMove);
		}
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

	public GenericMove getBestMove() {
		return bestMove;
	}

	public void setBestMove(GenericMove bestMove) {
		this.bestMove = bestMove;
	}
	
	public String report() {
		return "trans best:"+bestMove+" dep:"+depthSearchedInPly+" sc:"+score+" type:"+scoreType;
	}
	
	public void update(Transposition updateFrom) {
		this.setBestMove(updateFrom.getBestMove());
	    this.setDepthSearchedInPly(updateFrom.getDepthSearchedInPly());
	    this.setScoreType(updateFrom.getScoreType());
	    this.setScore(updateFrom.getScore());
	}
}