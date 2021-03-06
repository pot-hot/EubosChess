package eubos.search.transposition;

import java.util.List;

import eubos.search.ScoreTracker;
import eubos.search.transposition.TranspositionEvaluation.TranspositionTableStatus;

public interface ITransposition {

	byte getType();

	short getScore();

	byte getDepthSearchedInPly();

	int getBestMove();

	String report();

	boolean checkUpdate(byte new_Depth, short new_score, byte new_bound, int new_bestMove, List<Integer> pv);
	
	boolean checkUpdateToExact(byte depth, short new_score, int new_bestMove);
	
	short getAccessCount();
	
	void setAccessCount(short accessCount);

	List<Integer> getPv();
	
	TranspositionTableStatus evaluateSuitability(byte currPly, int depthRequiredPly, ScoreTracker st);
}