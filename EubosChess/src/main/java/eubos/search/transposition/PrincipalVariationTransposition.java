package eubos.search.transposition;

import java.util.List;

import com.fluxchess.jcpi.models.GenericMove;

import eubos.main.EubosEngineMain;
import eubos.position.Move;
import eubos.search.Score;

public class PrincipalVariationTransposition implements ITransposition {

	private byte depthSearchedInPly;
	private short score;
	private int bestMove;
	private byte scoreType;
	private List<Integer> pv;
	private short accessCount;

	public PrincipalVariationTransposition(byte depth, short score, byte scoreType, GenericMove bestMove) {
		// Only used by tests
		this(depth, score, scoreType, Move.toMove(bestMove, null, Move.TYPE_REGULAR_NONE), null);
	}
	
	public PrincipalVariationTransposition(byte depth, short score, byte scoreType, int bestMove, List<Integer> pv) {
		setDepthSearchedInPly(depth);
		setScore(score);
		setType(scoreType);
		setBestMove(bestMove);
		setAccessCount((short)0);
	}
	
	public PrincipalVariationTransposition(byte depth, Score score, int bestMove, List<Integer> pv) {
		this(depth, score.getScore(), score.getType(), bestMove, pv);
	}

	@Override
	public byte getType() {
		return scoreType;
	}

	@Override
	public void setType(byte scoreType) {
		this.scoreType = scoreType;
	}

	@Override
	public short getScore() {
		return score;
	}

	@Override
	public void setScore(short score) {
		this.score = score;
	}

	@Override
	public byte getDepthSearchedInPly() {
		return depthSearchedInPly;
	}

	@Override
	public void setDepthSearchedInPly(byte depthSearchedInPly) {
		this.depthSearchedInPly = depthSearchedInPly;
	}

	@Override
	public int getBestMove() {
		return bestMove;
	}
	
	@Override
	public void setBestMove(int bestMove) {
		if (!Move.areEqual(this.bestMove, bestMove)) {
			this.bestMove = bestMove;
		}
	}
	
	public List<Integer> getPv() {
		return pv;
	}
	
	private void truncateOnwardPvToSearchDepth(List<Integer> pv) {
		if (pv != null && pv.size() > depthSearchedInPly) {
			pv.subList(depthSearchedInPly, pv.size()).clear();
		}
	}

	public void setPv(List<Integer> pv) {
		if (EubosEngineMain.UCI_INFO_ENABLED) {
			truncateOnwardPvToSearchDepth(pv);
			this.pv = pv;
		}
	}
	
	@Override
	public String report() {
		String onward_pv = "";
		if (EubosEngineMain.UCI_INFO_ENABLED) {
			if (pv != null) {
				for (int move : pv) {
					onward_pv += String.format("%s, ", Move.toString(move));
				}
			}
		}
		String output = String.format("trans best=%s, dep=%d, sc=%d, type=%s, pv=%s", 
				Move.toString(bestMove),
				depthSearchedInPly,
				score,
				scoreType,
				onward_pv);
		return output;
	}
	
	@Override
	public void update(
			byte new_Depth, 
			short new_score, 
			byte new_bound, 
			int new_bestMove, 
			List<Integer> pv) {
		// order is important because setPv uses depth
		setDepthSearchedInPly(new_Depth);
		setType(new_bound);
		setScore(new_score);
		setBestMove(new_bestMove);
		setPv(pv);
	}
	
	public short getAccessCount() {
		return accessCount;
	}
	
	public void setAccessCount(short accessCount) {
		this.accessCount = accessCount;
	}
}
