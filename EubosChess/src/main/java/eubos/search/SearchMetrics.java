package eubos.search;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.fluxchess.jcpi.commands.ProtocolInformationCommand;
import com.fluxchess.jcpi.models.GenericMove;

import eubos.position.Move;
import eubos.score.MaterialEvaluator;

public class SearchMetrics {
	private AtomicLong nodesSearched;
	private long time;
	private short hashFull;
	private List<Integer> pv;
	private boolean pvValid = false;
	private short cpScore;
	private int depth;
	private int partialDepth;
	private long initialTimestamp;
	
	public SearchMetrics(int searchDepth) {
		nodesSearched = new AtomicLong(0);
		time = 0;
		cpScore = 0;
		pvValid = false;
		depth = searchDepth;
		partialDepth = 0;
		hashFull = 0;
		initialTimestamp = System.currentTimeMillis();
	}

	public SearchMetrics() {
		this(1);
	}
	
	public synchronized void setPeriodicInfoCommand(ProtocolInformationCommand info) {
		incrementTime();
		info.setNodes(getNodesSearched());
		info.setNps(getNodesPerSecond());
		info.setTime(getTime());
		info.setHash(getHashFull());
	}
	
	public synchronized void setPvInfoCommand(ProtocolInformationCommand info) {
		incrementTime();
		info.setNodes(getNodesSearched());
		info.setNps(getNodesPerSecond());
		info.setHash(getHashFull());
		info.setMoveList(getPrincipalVariation());
		info.setTime(getTime());
		int score = getCpScore();
		int depth = getDepth();
		if (java.lang.Math.abs(score)<MaterialEvaluator.MATERIAL_VALUE_KING) {
			info.setCentipawns(score);
		} else {
			int mateMove = (score > 0) ? Short.MAX_VALUE - score : Short.MIN_VALUE - score;
			info.setMate(mateMove);
		}
		info.setDepth(depth);
		info.setMaxDepth(getPartialDepth());
	}
	
	void incrementNodesSearched() { nodesSearched.incrementAndGet(); }
	long getNodesSearched() { return nodesSearched.get(); }
	
	void incrementTime() {
		long currentTimestamp = System.currentTimeMillis();
		time = currentTimestamp - initialTimestamp;
	}
	long getTime() { return time; }
	
	int getNodesPerSecond() {
		int nps = 0;
		if (time != 0) {
			nps = (int)(nodesSearched.get()*1000/time);
		}
		return nps;
	}
	
	public synchronized void setPrincipalVariation(List<Integer> pc) {
		if (!pc.isEmpty()) {
			pvValid = true;
			pv = pc;
		}
	}
	synchronized List<GenericMove> getPrincipalVariation() {
		List<GenericMove> thePv = null;
		if (pvValid) {
			thePv = new ArrayList<GenericMove>();
			for (int move : this.pv) {
				thePv.add(Move.toGenericMove(move));
			}
		}
		return thePv;
	}
	
	public synchronized short getCpScore() { return cpScore; }
	synchronized void setCpScore(short cpScore) { this.cpScore = cpScore; }
	
	synchronized int getDepth() { return depth; }
	public synchronized void setDepth(int depth) { this.depth = depth; }
	
	synchronized int getPartialDepth() { return partialDepth; }
	synchronized void setPartialDepth(int depth ) { this.partialDepth = depth; }
	
	short getHashFull() { return hashFull;	}
	public void setHashFull(short hashFull) { this.hashFull = hashFull; }
}
