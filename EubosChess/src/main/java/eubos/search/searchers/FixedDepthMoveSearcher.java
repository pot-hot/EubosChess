package eubos.search.searchers;

import java.util.List;

import com.fluxchess.jcpi.commands.ProtocolBestMoveCommand;

import eubos.main.EubosEngineMain;
import eubos.position.IChangePosition;
import eubos.position.IPositionAccessors;
import eubos.search.SearchDebugAgent;
import eubos.search.SearchResult;
import eubos.search.transposition.FixedSizeTranspositionTable;

public class FixedDepthMoveSearcher extends AbstractMoveSearcher {
	
	private byte searchDepth = 1;
	boolean searchStopped = false;
	
	public FixedDepthMoveSearcher( EubosEngineMain eubos, 
			FixedSizeTranspositionTable hashMap, 
			IChangePosition inputPm,  
			IPositionAccessors pos, 
			byte searchDepth) {
		super(eubos,inputPm,pos, hashMap);
		this.searchDepth = searchDepth;
		this.setName("FixedDepthMoveSearcher");
	}
	
	@Override
	public void halt() {
		searchStopped = true;
		mg.terminateFindMove();
	}
	
	@Override
	public void run() {
		SearchResult res = new SearchResult(null, false);
		List<Integer> pc = null;
		for (byte depth=1; depth<=searchDepth && !searchStopped; depth++) {
			res = doFindMove(res.bestMove, pc, depth);
			pc = mg.pc.toPvList(0);
		}
		eubosEngine.sendBestMoveCommand(new ProtocolBestMoveCommand( res.bestMove, null ));
		mg.terminateSearchMetricsReporter();
		SearchDebugAgent.close();
	}
}
