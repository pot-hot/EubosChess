package eubos.search;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fluxchess.jcpi.models.GenericMove;

import eubos.board.InvalidPieceException;
import eubos.board.pieces.King;
import eubos.board.pieces.Piece.Colour;
import eubos.position.IChangePosition;
import eubos.position.IGenerateMoveList;
import eubos.position.IPositionAccessors;
import eubos.position.IScoreMate;
import eubos.position.Transposition;
import eubos.position.Transposition.ScoreType;
import eubos.search.TranspositionTableAccessor.TranspositionEval;
import eubos.position.IEvaluate;

public class PlySearcher {
	
	public static final int PLIES_PER_MOVE = 2;
	public static final boolean ENABLE_SEARCH_EXTENSION_FOR_RECAPTURES = true;
	
	private IChangePosition pm;
	private IGenerateMoveList mlgen;
	IPositionAccessors pos;
	
	ScoreTracker st;
	private IEvaluate pe;
	private IScoreMate sg;
	PrincipalContinuation pc;
	private SearchMetrics sm;
	private SearchMetricsReporter sr;
	
	private boolean terminate = false;
	
	private Colour initialOnMove;	
	private List<GenericMove> lastPc;
	private byte searchDepthPly;
	private TranspositionTableAccessor tt;
	
	byte currPly = 0;
	byte depthSearchedPly = 0;
	
	PlySearcher(
			FixedSizeTranspositionTable hashMap,
			IEvaluate pe,
			IScoreMate sg,
			PrincipalContinuation pc,
			SearchMetrics sm,
			SearchMetricsReporter sr,
			byte searchDepthPly,
			IChangePosition pm,
			IGenerateMoveList mlgen,
			IPositionAccessors pos,
			List<GenericMove> lastPc) {
		currPly = 0;
		depthSearchedPly = 0;
		this.pe = pe;
		this.sg = sg;
		this.pc = pc;
		this.sm = sm;
		this.sr = sr;
		this.pm = pm;
		this.pos = pos;
		this.mlgen = mlgen;
		this.lastPc = lastPc;
		this.searchDepthPly = searchDepthPly;
		// Register initialOnMove
		initialOnMove = pos.getOnMove();
		this.st = new ScoreTracker(searchDepthPly, initialOnMove == Colour.white);
		this.tt = new TranspositionTableAccessor(hashMap, pos, st, pc, lastPc);
	}
	
	synchronized void terminateFindMove() { 
		terminate = true; }
	private synchronized boolean isTerminated() { return terminate; }	
	
	short searchPly() throws InvalidPieceException {
		return searchPly(null);
	}
	
	short searchPly(GenericMove commandedMove) throws InvalidPieceException {
		List<GenericMove> ml = null;
		byte depthRequiredPly = initialiseSearchAtPly();
		
		if (commandedMove == null) {
			TranspositionEval eval = tt.evaluateTranspositionData(currPly, depthRequiredPly);
			switch (eval.status) {
			
			case sufficientTerminalNode:
			case sufficientRefutation:
				depthSearchedPly = eval.trans.getDepthSearchedInPly();
				pc.initialiseOnTranspositionHit(currPly, eval.trans.getBestMove());
				doScoreBackup(eval.trans.getBestMove(), eval.trans.getScore());
				sm.incrementNodesSearched();
				break;
				
			case sufficientSeedMoveList:
				SearchDebugAgent.printHashIsSeedMoveList(currPly, eval.trans.getBestMove());
				ml = eval.trans.getMoveList();
				// Intentional drop through
			case insufficientNoData:
				if (ml == null)
					ml = getMoveList();
				searchMoves( ml, eval.trans);
				break;
				
			default:
				break;
			}
		} else {
			ml = new ArrayList<GenericMove>();
			ml.add(commandedMove);
			searchMoves( ml, null);
		}
		
		return st.getBackedUpScoreAtPly(currPly);
	}

	void searchMoves(List<GenericMove> ml, Transposition trans) throws InvalidPieceException {
		if (isMateOccurred(ml)) {
			short mateScore = sg.scoreMate(currPly, (pos.getOnMove() == Colour.white), initialOnMove);
			st.setBackedUpScoreAtPly(currPly, mateScore);
		} else {
			pc.update(currPly, ml.get(0));
			short provisionalScoreAtPly = st.getProvisionalScoreAtPly(currPly);
			Iterator<GenericMove> move_iter = ml.iterator();
			
			while(move_iter.hasNext() && !isTerminated()) {
				GenericMove currMove = move_iter.next();
				if (currPly == 0)
					reportMove(currMove);
				
				short positionScore = applyMoveAndScore(currMove);
				
				doScoreBackup(currMove, positionScore);
				
				updateTranspositionTable(move_iter, ml, st.getBackedUpScoreAtPly(currPly), trans);
				
				if (st.isAlphaBetaCutOff( currPly, provisionalScoreAtPly, positionScore)) {
					SearchDebugAgent.printRefutationFound(currPly);
					break;	
				}
			}
		}
	}
	
	private byte initialiseSearchAtPly() {
		byte depthRequiredPly = (byte)(searchDepthPly - currPly);
		st.setProvisionalScoreAtPly(currPly);
		if (currPly == 0) {
			SearchDebugAgent.printNewIterationBanner(depthRequiredPly);
		}
		SearchDebugAgent.printSearchPly(currPly, st.getProvisionalScoreAtPly(currPly), pos.getOnMove());
		SearchDebugAgent.printFen(currPly, pos);
		return (depthRequiredPly < 0) ? 0 : depthRequiredPly;
	}

	protected void doScoreBackup(GenericMove currMove, short positionScore) {
		if (st.isBackUpRequired(currPly, positionScore)) {
			// New best score found at this node, back up and update the principal continuation.
			st.setBackedUpScoreAtPly(currPly, positionScore);
			pc.update(currPly, currMove);
			if (currPly == 0)
				new PrincipalContinuationUpdateHelper(positionScore).report();
		}
	}

	protected void updateTranspositionTable(Iterator<GenericMove> move_iter, List<GenericMove> ml, short positionScore, Transposition trans) {
		GenericMove bestMove = (depthSearchedPly == 0) ? null : pc.getBestMove(currPly);
		ScoreType bound = ScoreType.exact;
		if (move_iter.hasNext()) {
			// We haven't searched all the moves yet so this is a bound score
			bound = (pos.getOnMove() == Colour.white) ? ScoreType.lowerBound : ScoreType.upperBound;
		}
		tt.storeTranspositionScore(currPly, depthSearchedPly, bestMove, positionScore, bound, ml, trans);
	}
	
	void reportMove(GenericMove currMove) {
		sm.setCurrentMove(currMove);
		sm.incrementCurrentMoveNumber();
		sr.reportCurrentMove();
	}
		
	private List<GenericMove> getMoveList() throws InvalidPieceException {
		List<GenericMove> ml = null;
		if ((lastPc != null) && (lastPc.size() > currPly)) {
			// Seeded move list is possible
			ml = mlgen.getMoveList(lastPc.get(currPly));
		} else {
			ml = mlgen.getMoveList();
		}
		return ml;
	}

	private boolean isMateOccurred(List<GenericMove> ml) {
		return ml.isEmpty();
	}
	
	private short applyMoveAndScore(GenericMove currMove) throws InvalidPieceException {
		
		doPerformMove(currMove);
		short positionScore = assessNewPosition(currMove);
		doUnperformMove(currMove);
		
		sm.incrementNodesSearched();
		
		return positionScore;
	}

	private short assessNewPosition(GenericMove prevMove) throws InvalidPieceException {
		short positionScore;
		// Either recurse or evaluate a terminal position
		if ( isTerminalNode() ) {
			if (ENABLE_SEARCH_EXTENSION_FOR_RECAPTURES) {
				
				positionScore = scoreTerminalNode();
				
				if (pos.lastMoveWasCapture() && currPly < (this.searchDepthPly+1)) {
					List<GenericMove> extra_ml = mlgen.getMoveList();
					Colour onMove = pos.getOnMove();
					// look to see if there is a recapture possible
					// where a recapture is a capture on the old move target square
					Iterator<GenericMove> move_iter = extra_ml.iterator();
					boolean anyMoveWasCapture = false;
					while(move_iter.hasNext()) {
						GenericMove nextMove = move_iter.next();
						// DEFECT: this is wrong for en passant captures
						if (nextMove.to.equals(prevMove.to)) {
							
							anyMoveWasCapture = true;
							
							short forcedCapturePositionScore = searchPly(nextMove);
							// was blacks move last
							if ((onMove==Colour.white) && (forcedCapturePositionScore > positionScore)) {
								// recaptures are worse for black, so need to take account
								positionScore = forcedCapturePositionScore;
							}
							// was whites last move
							if ((onMove==Colour.black) && (forcedCapturePositionScore < positionScore)) {
								// recaptures are worse for white, so need to take account
								positionScore = forcedCapturePositionScore;
							}
						}
					}
					// If none of the moves are a recapture leave the score as is
					if (anyMoveWasCapture) {
						//depthSearchedPly++;
						depthSearchedPly =1;
					} else {
						depthSearchedPly=1;
					}
				} else {
					depthSearchedPly = 1;
				}
			} else {
				positionScore = scoreTerminalNode();
				depthSearchedPly = 1;
			}
		} else {
			positionScore = searchPly();
			depthSearchedPly++;
		}
		return positionScore;
	}

	private short scoreTerminalNode() {
		return pe.evaluatePosition(pos);
	}
	
	private boolean isTerminalNode() {
		boolean isTerminalNode = false;
		if (currPly >= searchDepthPly) {
			isTerminalNode = true;
		}
		return isTerminalNode;
	}	

	private void doPerformMove(GenericMove currMove) throws InvalidPieceException {
		SearchDebugAgent.printPerformMove(currPly, currMove);
		pm.performMove(currMove);
		currPly++;
	}
	
	private void doUnperformMove(GenericMove currMove) throws InvalidPieceException {
		pm.unperformMove();
		currPly--;
		SearchDebugAgent.printUndoMove(currPly, currMove);
	}	
		
	// Principal continuation focused, search report focused inner class
	class PrincipalContinuationUpdateHelper
	{
		short positionScore;
		
		PrincipalContinuationUpdateHelper(short score) {
			positionScore = score;
		}

		void report() {
			assignPrincipalVariationToSearchMetrics();
			assignCentipawnScoreToSearchMetrics();
			sr.reportPrincipalVariation();
		}	
		
		private void assignPrincipalVariationToSearchMetrics() {
			truncatePrincipalContinuation();
			sm.setPrincipalVariation(pc.toPvList());
		}	

		private void assignCentipawnScoreToSearchMetrics() {
			if (initialOnMove.equals(Colour.black))
				positionScore = (short) -positionScore; // Negated due to UCI spec (from engine pov)
			sm.setCpScore(positionScore);
		}
		
		private void truncatePrincipalContinuation() {
			if (isScoreIndicatesMate()) {
				// If the positionScore indicates a mate, truncate the pc accordingly
				int matePly = calculatePlyMateOccurredOn();
				pc.clearAfter(matePly);
			}
		}
		
		private boolean isScoreIndicatesMate() {
			return Math.abs(positionScore) >= King.MATERIAL_VALUE;
		}
		
		private int calculatePlyMateOccurredOn() {
			int matePly = Math.abs(positionScore)/King.MATERIAL_VALUE;
			matePly *= PLIES_PER_MOVE;
			matePly = searchDepthPly - matePly;
			if (isOwnMate()) {
				if ((searchDepthPly&1) != 0x1)
					matePly += 1;
			} else {
				if ((searchDepthPly&1) == 0x1)
					matePly -= 1;	
			}
			return (matePly > 0) ? matePly : 0;
		}
		
		private boolean isOwnMate() {
			return ((initialOnMove==Colour.white && positionScore<0) ||
			        (initialOnMove==Colour.black && positionScore>0));
		}
	}	
}
