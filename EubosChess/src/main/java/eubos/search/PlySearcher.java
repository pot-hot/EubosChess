package eubos.search;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fluxchess.jcpi.models.GenericMove;

import eubos.board.InvalidPieceException;
import eubos.board.pieces.Piece.Colour;
import eubos.position.IChangePosition;
import eubos.position.IGenerateMoveList;
import eubos.position.IPositionAccessors;
import eubos.position.IScoreMate;
import eubos.position.MateScoreGenerator;
import eubos.search.Transposition.ScoreType;
import eubos.search.TranspositionTableAccessor.TranspositionEval;
import eubos.search.TranspositionTableAccessor.TranspositionTableStatus;
import eubos.position.IEvaluate;

public class PlySearcher {
	
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
	private PrincipalContinuationUpdateHelper pcUpdater;
	
	byte currPly = 0;
	byte depthSearchedPly = 0;
	
	PlySearcher(
			FixedSizeTranspositionTable hashMap,
			PrincipalContinuation pc,
			SearchMetrics sm,
			SearchMetricsReporter sr,
			byte searchDepthPly,
			IChangePosition pm,
			IGenerateMoveList mlgen,
			IPositionAccessors pos,
			List<GenericMove> lastPc,
			IEvaluate pe) {
		currPly = 0;
		depthSearchedPly = 0;
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
		this.pe = pe;
		this.st = new ScoreTracker(searchDepthPly*3, initialOnMove == Colour.white);
		this.tt = new TranspositionTableAccessor(hashMap, pos, st, lastPc);
		this.sg = new MateScoreGenerator(pos, searchDepthPly*3);
		this.pcUpdater = new PrincipalContinuationUpdateHelper(initialOnMove, pc, sm, sr);
	}
	
	public synchronized void terminateFindMove() { 
		terminate = true; }
	private synchronized boolean isTerminated() { return terminate; }	
	
	protected void doPrincipalContinuationupdateOnScoreBackup(
			GenericMove currMove, short positionScore)
			throws InvalidPieceException {
		pc.update(currPly, currMove);
		if (currPly == 0) {
			// If backed up to the root node, report the principal continuation
			constructPc();
			pcUpdater.report(positionScore, searchDepthPly);
		}
	}
	
	short normalSearchPly() throws InvalidPieceException {
		if (isTerminated())
			return 0;
		
		List<GenericMove> ml = null;
		byte depthRequiredPly = initialiseSearchAtPly();
		
		TranspositionEval eval = tt.getTransposition(currPly, depthRequiredPly);
		switch (eval.status) {
		
		case sufficientTerminalNode:
		case sufficientRefutation:
			depthSearchedPly = eval.trans.getDepthSearchedInPly();
			pc.clearTreeBeyondPly(currPly);
			if (doScoreBackup(eval.trans.getScore())) {
				doPrincipalContinuationupdateOnScoreBackup(eval.trans.getBestMove(), eval.trans.getScore());
			}
			sm.incrementNodesSearched();
			break;
			
		case sufficientSeedMoveList:
			SearchDebugAgent.printHashIsSeedMoveList(currPly, eval.trans.getBestMove(), pos.getHash());
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
		handleEarlyTermination();
		
		return st.getBackedUpScoreAtPly(currPly);
	}

	private void handleEarlyTermination() {
		if (currPly == 0 && isTerminated()) {
			// Set best move to previous iteration search result.
			if (lastPc != null) {
				pc.update(0, lastPc.get(0));
			}
		}
	}

	private void searchMoves(List<GenericMove> ml, Transposition trans) throws InvalidPieceException {
		if (isMateOccurred(ml)) {
			short mateScore = sg.scoreMate(currPly, (pos.getOnMove() == Colour.white), initialOnMove);
			st.setBackedUpScoreAtPly(currPly, mateScore);
		} else {
			pc.update(currPly, ml.get(0));
			short provisionalScoreAtPly = st.getProvisionalScoreAtPly(currPly);
			Iterator<GenericMove> move_iter = ml.iterator();
			
			boolean everBackedUp = false;
			boolean refutationFound = false;
			ScoreType plyBound = (pos.getOnMove().equals(Colour.white)) ? ScoreType.lowerBound : ScoreType.upperBound;
			short plyScore = (plyBound == ScoreType.lowerBound) ? Short.MIN_VALUE : Short.MAX_VALUE;
			
			long currHashAtStart = pos.getHash();
			SearchDebugAgent.printTransNull(currPly, currHashAtStart);
			
			while(move_iter.hasNext() && !isTerminated()) {
				
				// Debug bad hash bug
				long currHashAtMoveN = pos.getHash();
				if (currHashAtMoveN != currHashAtStart) {
					SearchDebugAgent.printTransNull(currPly, currHashAtMoveN);
					SearchDebugAgent.printTransNull(currPly, currHashAtStart);
				}
				
				GenericMove currMove = move_iter.next();
				if (currPly == 0) {
					// When we start to search a move at the root node, clear the principal continuation data
					pc.clearRowsBeyondPly(currPly);
					reportMove(currMove);
				}
				
				short positionScore = applyMoveAndScore(currMove);
				
				if (doScoreBackup(positionScore)) {
					everBackedUp = true;
					plyScore = positionScore;
					doPrincipalContinuationupdateOnScoreBackup(currMove, positionScore);
					Transposition newTrans = new Transposition(depthSearchedPly, positionScore, plyBound, ml, currMove);
					trans = tt.setTransposition(sm, currPly, trans, newTrans);
				} else {
					// Always clear the principal continuation when we didn't back up the score
					pc.clearRowsBeyondPly(currPly);
					// Update the position hash if the move is better than that previously stored at this position
					if (shouldUpdatePositionBoundScoreAndBestMove(plyBound, plyScore, positionScore)) {
						plyScore = positionScore;
						Transposition newTrans = new Transposition(depthSearchedPly, plyScore, plyBound, ml, currMove);
						trans = tt.setTransposition(sm, currPly, trans, newTrans);
					}
				}
				
				if (st.isAlphaBetaCutOff( currPly, provisionalScoreAtPly, positionScore)) {
					refutationFound = true;
					SearchDebugAgent.printRefutationFound(currPly);
					break;	
				}
			}
			if (everBackedUp && !refutationFound) {
				// Needed to set exact score instead of upper/lower bound score now we finished search at this ply
				Transposition newTrans = new Transposition(depthSearchedPly, st.getBackedUpScoreAtPly(currPly), ScoreType.exact, ml, pc.getBestMove(currPly));
				trans = tt.setTransposition(sm, currPly, trans, newTrans);
			}
			depthSearchedPly++; // backing up, increment depth searched
		}
	}

	protected boolean shouldUpdatePositionBoundScoreAndBestMove(
			ScoreType plyBound, short plyScore, short positionScore) {
		boolean doUpdate = false;
		if (plyBound == ScoreType.lowerBound) {
			if (positionScore > plyScore)
				doUpdate = true;
		} else {
			if (positionScore < plyScore)
				doUpdate = true;
		}
		return doUpdate;
	}
	
	void constructPc() throws InvalidPieceException {
		byte plies = 0;
		int numMoves = 0;
		List<GenericMove> constructed_pc = new ArrayList<GenericMove>(searchDepthPly);
		for (plies = 0; plies < searchDepthPly; plies++) {
			GenericMove pcMove = pc.getBestMove(plies);
			if (pcMove != null) {
				// apply move from principal continuation
				constructed_pc.add(pcMove);
				pm.performMove(pcMove);
				numMoves++;
			} else {
				/* Apply move and find best move from hash */
				TranspositionEval eval = tt.getTransposition(plies, 0);
				if (eval.status != TranspositionTableStatus.insufficientNoData && eval != null && eval.trans != null) {
					GenericMove currMove = eval.trans.getBestMove();
					constructed_pc.add(currMove);
					pm.performMove(currMove);
					numMoves++;
				}
			}
		}
		for (plies = 0; plies < numMoves; plies++) {
			pm.unperformMove();
		}
		pc.update(0, constructed_pc);
	}
	
	private byte initialiseSearchAtPly() {
		byte depthRequiredPly = (byte)(searchDepthPly - currPly);
		st.setProvisionalScoreAtPly(currPly);
		SearchDebugAgent.printStartPlyInfo(currPly, depthRequiredPly, st.getProvisionalScoreAtPly(currPly), pos);
		return depthRequiredPly;
	}

	private boolean doScoreBackup(short positionScore) {
		boolean backupRequired = false;
		if (st.isBackUpRequired(currPly, positionScore)) {
			st.setBackedUpScoreAtPly(currPly, positionScore);
			backupRequired = true;
		}
		return backupRequired;
	}
	
	private void reportMove(GenericMove currMove) {
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

	enum SearchState {
		normalSearchTerminalNode,
		normalSearchNode,
		extendedSearchNode,
		extendedSearchTerminalNode
	};
	
	private short assessNewPosition(GenericMove prevMove) throws InvalidPieceException {
		short positionScore = 0;
		switch ( isTerminalNode() ) {
		case normalSearchTerminalNode:
		case extendedSearchTerminalNode:
			positionScore = scoreTerminalNode();
			depthSearchedPly = 1; // We searched to find this score
			break;
		case normalSearchNode:
			positionScore = normalSearchPly();
			break;
		case extendedSearchNode:
			positionScore = extendedSearchPly();
			depthSearchedPly = 1; // Not sure this is needed
			break;
		default:
			break;
		}
		return positionScore;
	}

	private short scoreTerminalNode() {
		return pe.evaluatePosition();
	}
	
	private SearchState isTerminalNode() {
		SearchState nodeState = SearchState.normalSearchNode;
		if (currPly < searchDepthPly) {
			nodeState = SearchState.normalSearchNode;
		} else if (currPly == searchDepthPly) {
			if (pe.isQuiescent()) {
				nodeState = SearchState.normalSearchTerminalNode;
			} else {
				nodeState = SearchState.extendedSearchNode; 
			}
		} else { // if (currPly > searchDepthPly) // extended search
			if (pe.isQuiescent() || (currPly > Math.min((searchDepthPly + 6), ((searchDepthPly*3)-1))) /* todo ARBITRARY!!!! */) {
				nodeState = SearchState.extendedSearchTerminalNode;
			} else {
				nodeState = SearchState.extendedSearchNode; 
			}
		}
		return nodeState;
	}
	
	private short extendedSearchPly() throws InvalidPieceException {
		if (isTerminated())
			return 0;
		
		st.setProvisionalScoreAtPly(currPly);
		
		ScoreType plyBound = (pos.getOnMove().equals(Colour.white)) ? ScoreType.lowerBound : ScoreType.upperBound;
		short plyScore = (plyBound == ScoreType.lowerBound) ? Short.MIN_VALUE : Short.MAX_VALUE;
		List<GenericMove> ml = null;
		
		TranspositionEval eval = tt.getTransposition(currPly, 100);
		switch (eval.status) {
		
		case sufficientTerminalNode:
		case sufficientRefutation:
		case sufficientSeedMoveList:
			SearchDebugAgent.printHashIsSeedMoveList(currPly, eval.trans.getBestMove(), pos.getHash());
			ml = eval.trans.getMoveList();
			searchCheckAndCaptureMoves( ml );
			break;

		case insufficientNoData:
			ml = getMoveList();
			
			// To store movelist
			if (ml.size() != 0) {
				Transposition newTrans = new Transposition((byte)0, plyScore, plyBound, ml, ml.get(0));
				tt.setTransposition(sm, currPly, null, newTrans);
			}
			
			searchCheckAndCaptureMoves( ml );
			break;
			
		default:
			break;
		}
		return st.getBackedUpScoreAtPly(currPly);
	}
	
	private void searchCheckAndCaptureMoves(List<GenericMove> ml) throws InvalidPieceException {
		if (isMateOccurred(ml)) {
			short mateScore = sg.scoreMate(currPly, (pos.getOnMove() == Colour.white), initialOnMove);
			st.setBackedUpScoreAtPly(currPly, mateScore);
		} else {
			short provisionalScoreAtPly = st.getProvisionalScoreAtPly(currPly);
			Iterator<GenericMove> move_iter = ml.iterator();
			boolean isCaptureMove = false;
			while(move_iter.hasNext() && !isTerminated()) {
				GenericMove currMove = move_iter.next();
			
				pm.performMove(currMove);
				isCaptureMove = pos.lastMoveWasCheckOrCapture();
				pm.unperformMove();
				
				if (isCaptureMove) {
					short positionScore = applyMoveAndScore(currMove);
					doScoreBackup(positionScore);
					
					if (st.isAlphaBetaCutOff( currPly, provisionalScoreAtPly, positionScore)) {
						SearchDebugAgent.printRefutationFound(currPly);
						break;	
					}
				}
			}
		}
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
}
