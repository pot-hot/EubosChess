package eubos.search.transposition;

import java.util.List;

import eubos.position.IPositionAccessors;
import eubos.search.ScoreTracker;
import eubos.search.SearchDebugAgent;

public class TranspositionTableAccessor implements ITranspositionAccessor {
	
	private FixedSizeTranspositionTable hashMap;
	private IPositionAccessors pos;
	private ScoreTracker st;
	private SearchDebugAgent sda;
	
	public TranspositionTableAccessor(
			FixedSizeTranspositionTable transTable,
			IPositionAccessors pos,
			ScoreTracker st,
			SearchDebugAgent sda) {
		hashMap = transTable;
		this.pos = pos;
		this.st = st;
		this.sda = sda;
	}
	
	public TranspositionEvaluation getTransposition(byte currPly, int depthRequiredPly) {
		TranspositionEvaluation eval = new TranspositionEvaluation();
		eval.trans = hashMap.getTransposition(pos.getHash());
		if (eval.trans != null) {
			eval.status = eval.trans.evaluateSuitability(currPly, depthRequiredPly, st);
		}
		return eval;
	}
	
	public ITransposition setTransposition(ITransposition trans, byte new_Depth, short new_score, byte new_bound, int new_bestMove) {
		return setTransposition(trans, new_Depth, new_score, new_bound, new_bestMove, null);
	}
	
	public ITransposition setTransposition(ITransposition trans, byte new_Depth, short new_score, byte new_bound, int new_bestMove, List<Integer> pv) {
		boolean is_created = false;
		if (trans == null) {
			// Needed, because we want to merge this transposition with that of other threads, not to lose their effort.
			// Read, modify, write, otherwise we blindly update the transposition table, potentially overwriting other thread's Transposition object.
			trans = hashMap.getTransposition(pos.getHash());
			if (trans == null) {
				trans = createTranpositionAddToTable(new_Depth, new_score, new_bound, new_bestMove, pv);
				is_created = true;
			}
		}
		if (!is_created) {
			boolean is_updated = trans.checkUpdate(new_Depth, new_score, new_bound, new_bestMove, pv);
			if (is_updated) {
				sda.printTransUpdate(trans, pos.getHash());
			}
		}
		return trans;
	}
	
	private ITransposition createTranpositionAddToTable(byte new_Depth, short new_score, byte new_bound, int new_bestMove, List<Integer> pv) {
		ITransposition new_trans;
		sda.printCreateTrans(pos.getHash());
		if (USE_PRINCIPAL_VARIATION_TRANSPOSITIONS) {
			new_trans = new PrincipalVariationTransposition(new_Depth, new_score, new_bound, new_bestMove, pv);
		} else {
			new_trans= new Transposition(new_Depth, new_score, new_bound, new_bestMove, null);
		}
		hashMap.putTransposition(pos.getHash(), new_trans);
		sda.printTransUpdate(new_trans, pos.getHash());
		return new_trans;
	}
}
