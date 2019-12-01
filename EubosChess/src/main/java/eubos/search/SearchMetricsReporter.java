package eubos.search;

import com.fluxchess.jcpi.commands.ProtocolInformationCommand;

import eubos.main.EubosEngineMain;
import eubos.position.MaterialEvaluator;

public class SearchMetricsReporter extends Thread {
	
	private boolean sendInfo = false;
	private volatile boolean reporterActive;
	private SearchMetrics sm;
	private EubosEngineMain eubosEngine;
	private static final int UPDATE_RATE_MS = 500;
	
	public SearchMetricsReporter( EubosEngineMain eubos, SearchMetrics inputSm ) {
		sm = inputSm;
		reporterActive = true;
		eubosEngine = eubos;
		this.setName("SearchMetricsReporter");
	}
	
	public void run() {
		long timestampIntoWait = 0;
		long timestampOutOfWait = 0;
		do {
			timestampIntoWait = System.currentTimeMillis();
			try {
				synchronized (this) {
					wait(UPDATE_RATE_MS);
				}
			} catch (InterruptedException e) {
				reporterActive = false;
				break;
			}
			timestampOutOfWait = System.currentTimeMillis();
			sm.incrementTime((int)(timestampOutOfWait - timestampIntoWait));
			reportNodeData();
		} while (reporterActive);
	}
	
	public void end() {
		reporterActive = false;
		synchronized (this) {
			this.notify();
		}
	}
	
	public void reportNodeData() {
		ProtocolInformationCommand info = new ProtocolInformationCommand();
		info.setNodes(sm.getNodesSearched());
		info.setNps(sm.getNodesPerSecond());
		info.setTime(sm.getTime());
		info.setHash(sm.getHashFull());
		if (info.getTime() > 10) { 
			eubosEngine.sendInfoCommand(info);
		}
	}
	
	void reportPrincipalVariation() {
		if (sendInfo) {
			ProtocolInformationCommand info = new ProtocolInformationCommand();
			info.setMoveList(sm.getPrincipalVariation());
			info.setTime(sm.getTime());
			int score = sm.getCpScore();
			int depth = sm.getDepth();
			if (java.lang.Math.abs(score)<MaterialEvaluator.MATERIAL_VALUE_KING) {
				info.setCentipawns(score);
			} else {
				int mateMove = (score > 0) ? Short.MAX_VALUE - score : Short.MIN_VALUE - score;
				info.setMate(mateMove);
			}
			info.setDepth(depth);
			info.setMaxDepth(sm.getPartialDepth());
			eubosEngine.sendInfoCommand(info);
		}
	}
	
	void reportCurrentMove() {
		if (sendInfo) {
			ProtocolInformationCommand info = new ProtocolInformationCommand();
			info.setCurrentMove(sm.getCurrentMove());
			info.setCurrentMoveNumber(sm.getCurrentMoveNumber());
			eubosEngine.sendInfoCommand(info);
		}
	}

	public void setSendInfo(boolean enable) {
		sendInfo = enable;		
	}
}
