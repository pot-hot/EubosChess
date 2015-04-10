package eubos.board;

import java.util.Iterator;
import java.util.LinkedList;

import com.fluxchess.jcpi.models.GenericMove;

import eubos.pieces.Bishop;
import eubos.pieces.King;
import eubos.pieces.Knight;
import eubos.pieces.Pawn;
import eubos.pieces.Piece;
import eubos.pieces.Piece.Colour;
import eubos.pieces.Queen;
import eubos.pieces.Rook;

public class MiniMaxMoveGenerator extends MoveGenerator implements
		IMoveGenerator {
	
	private int searchDepthPly;
	private int scores[];
	private PrincipalContinuation pc;
	private static final boolean isDebugOn = false;
	private Piece.Colour initialOnMove;
	private boolean mateFound = false;
	private boolean stalemateFound = false;
	
	public MiniMaxMoveGenerator( BoardManager bm, int searchDepth ) {
		super( bm );
		scores = new int[searchDepth];
		searchDepthPly = searchDepth;
		pc = new PrincipalContinuation(searchDepth);
	}
	
	@Override
	public GenericMove findMove() throws NoLegalMoveException {
		// Register initialOnMove
		initialOnMove = bm.onMove;
		// Descend the plies in the search tree, to full depth, updating board and scoring positions
		searchPly(0);
		// Report the principal continuation and select the best move
		SearchDebugAgent debug = new SearchDebugAgent(0, true);
		debug.printPrincipalContinuation(0, pc);
		GenericMove bestMove = pc.getBestMove();
		if (bestMove==null) {
			throw new NoLegalMoveException();
		}
		return bestMove;
	}

	private int searchPly(int currPly) {
		SearchDebugAgent debug = new SearchDebugAgent(currPly, isDebugOn);
		debug.printSearchPly(currPly,bm.onMove);
		int alphaBetaCutOff = initNodeScoreAlphaBeta(debug, currPly);
		// Generate all moves at this position and test if the previous move in the
		// search tree led to either checkmate or stalemate.
		LinkedList<GenericMove> ml = generateMovesAtPosition();
		if (mateFound) {
			backupScoreForCheckmate(currPly);
			debug.printMateFound(currPly);
			pc.clearAfter(currPly-1);
			mateFound = false;
		} else if (stalemateFound) {
			backupScoreForStalemate(currPly);
			stalemateFound = false;
		}		
		Iterator<GenericMove> move_iter = ml.iterator();
		// Iterate through all the moves for this ply; there will be none if a mate was detected...
		while( move_iter.hasNext()) {
			int positionScore = 0;
			// 1) Apply the next move in the list
			GenericMove currMove = move_iter.next();
			debug.printPerformMove(currPly, currMove);
			bm.performMove(currMove);
			// 2) Either recurse or evaluate position and check for back-up of score
			if ( isTerminalNode(currPly) ) {
				positionScore = evaluatePosition(bm.getTheBoard());
			} else {
				positionScore = searchPly(currPly+1);
			}
			// 3) Having assessed the position, undo the move
			debug.printUndoMove(currPly, currMove);
			bm.undoPreviousMove();
			// 4a) Back-up the position score and update the principal continuation...
			if (backUpIsRequired(currPly, positionScore)) {
				scores[currPly]=positionScore;
				debug.printBackUpScore(currPly, positionScore);
				pc.update(currPly, currMove);
				debug.printPrincipalContinuation(currPly,pc);
			// 4b) ...or test for an Alpha Beta algorithm cut-off
			} else if (testForAlphaBetaCutOff( alphaBetaCutOff, positionScore, currPly )) {
				debug.printRefutationFound(currPly);
				break;
			}
		}
		return scores[currPly];
	}

	private boolean backUpIsRequired(int currPly, int positionScore) {
		boolean backUpScore = false;
		if (bm.onMove == Colour.white) {
			// if white, maximise score
			if (positionScore > scores[currPly])
				backUpScore = true;
		} else {
			// if black, minimise score 
			if (positionScore < scores[currPly])
				backUpScore = true;
		}
		return backUpScore;
	}
	
	private boolean testForAlphaBetaCutOff(int cutOffValue, int positionScore, int currPly) {
		if ((cutOffValue != Integer.MAX_VALUE) && (cutOffValue != Integer.MIN_VALUE)) {
			if ((bm.onMove == Colour.white && positionScore >= scores[currPly-1]) ||
					(bm.onMove == Colour.black && positionScore <= scores[currPly-1])) {
				return true;
			}
		}
		return false;
	}

	private void backupScoreForStalemate(int currPly) {
		// Avoid stalemates by giving them a large penalty score.
		scores[currPly] = -300000;
		if (initialOnMove==Colour.black)
			scores[currPly] = -scores[currPly];
	}

	private void backupScoreForCheckmate(int currPly) {
		// Favour earlier mates (i.e. Mate-in-one over mate-in-three) by giving them a larger score.
		scores[currPly] = (searchDepthPly-currPly)*300000;
		if (initialOnMove==Colour.black)
			scores[currPly] = -scores[currPly];
	}

	private int initNodeScoreAlphaBeta(SearchDebugAgent debug, int currPly) {
		// Initialise score at this node
		if (currPly==0 || currPly==1) {
			if (bm.onMove==Colour.white) {
				scores[currPly] = Integer.MIN_VALUE;
			} else {
				scores[currPly] = Integer.MAX_VALUE;
			}
		} else {
			// alpha beta algorithm: bring down score from 2 levels up tree
			debug.printAlphaBetaCutOffLimit(currPly, scores[currPly-2]);
			scores[currPly] = scores[currPly-2];
		}
		return scores[currPly];
	}

	private LinkedList<GenericMove> generateMovesAtPosition() {
		LinkedList<GenericMove> entireMoveList = new LinkedList<GenericMove>();
		// Test if the King is in check at the start of the turn
		King ownKing = bm.getKing(bm.onMove);
		boolean kingIsInCheck = inCheck(ownKing);
		// For each piece of the "on Move" colour, add it's legal moves to the entire move list
		Iterator<Piece> iter_p = bm.getTheBoard().iterateColour(bm.onMove);
		while ( iter_p.hasNext() ) {
			Piece currPiece = iter_p.next();
			entireMoveList.addAll( currPiece.generateMoves( bm ));
		}
		addCastlingMoves(entireMoveList);
		// Scratch any moves resulting in the king being in check
		Iterator<GenericMove> iter_ml = entireMoveList.iterator();
		while ( iter_ml.hasNext() ) {
			GenericMove currMove = iter_ml.next();
			bm.performMove( currMove );
			if (inCheck(ownKing)) {
				iter_ml.remove();
			}
			bm.undoPreviousMove();
		}
		if (entireMoveList.isEmpty()) {
			if (kingIsInCheck && initialOnMove==Piece.Colour.getOpposite(bm.onMove)) {
				// Indicates checkmate! Perform an immediate backup of score and abort the 
				// search of any moves deeper than the previous node in the search tree. 
				// However, search the rest of the tree, as this may yield earlier forced mates.
				mateFound = true;
			} else {
				// Indicates a stalemate position.
				stalemateFound = true;
			}
		}
		return entireMoveList;
	}

	private int evaluatePosition(Board theBoard ) {
		// First effort does only the most simple calculation based on material
		Iterator<Piece> iter_p = theBoard.iterator();
		int materialEvaluation = 0;
		while ( iter_p.hasNext() ) {
			Piece currPiece = iter_p.next();
			int currValue = 0;
			if ( currPiece instanceof Pawn ) 
				currValue = 100;
			else if ( currPiece instanceof Rook )
				currValue = 500;
			else if ( currPiece instanceof Bishop )
				currValue = 320;
			else if ( currPiece instanceof Knight )
				currValue = 300;
			else if ( currPiece instanceof Queen )
				currValue = 900;
			else if ( currPiece instanceof King )
				currValue = 300000;
			if (currPiece.isBlack()) currValue = -currValue;
			materialEvaluation += currValue;
		}
		return materialEvaluation;
	}

	private boolean isTerminalNode(int currPly) {
		boolean isTerminalNode = false;
		if (currPly == (searchDepthPly-1)) {
			isTerminalNode = true;
		}
		return isTerminalNode;
	}
}
