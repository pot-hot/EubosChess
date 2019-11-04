package eubos.search;

public class TranspositionEvaluation {
	
	public TranspositionEvaluation() {
		status = TranspositionTableStatus.insufficientNoData;
		trans = null;
	}
	
	public enum TranspositionTableStatus {
		insufficientNoData,
		sufficientTerminalNode,
		sufficientTerminalNodeBeta,
		sufficientRefutation,
		sufficientSeedMoveList		
	};
	
	public TranspositionTableStatus status;
	public Transposition trans;
}
