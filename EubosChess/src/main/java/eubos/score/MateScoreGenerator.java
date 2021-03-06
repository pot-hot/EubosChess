package eubos.score;

import eubos.board.Piece.Colour;
import eubos.position.IPositionAccessors;

public class MateScoreGenerator implements IScoreMate {
	
	private IPositionAccessors pos;
	private Colour initialOnMove;
	private IEvaluate pe;

	public static final int PLIES_PER_MOVE = 2;
	
	public MateScoreGenerator(IPositionAccessors pos, IEvaluate pe) {
		this.pos = pos;
		initialOnMove = pos.getOnMove();
		this.pe = pe;
	}
	
	private boolean isInitialOnMove(byte currPly) {
		return (currPly%2) == 0;
	}
	
	private short getWhiteIsMatedScore(short mateMoveNum) {
		return (short) (Short.MIN_VALUE + mateMoveNum);
	}
	
	private short getBlackIsMatedScore(short mateMoveNum) {
		return (short) (Short.MAX_VALUE - mateMoveNum);
	}
	
	public short scoreMate(byte currPly) {
		short mateScore = 0;
		if (pos.isKingInCheck()) {
			// Checkmate
			if (Colour.isWhite(initialOnMove)) {
				mateScore = isInitialOnMove(currPly) ? getWhiteIsMatedScore(currPly) : getBlackIsMatedScore(currPly);
			} else { // initial on move is black
				mateScore = isInitialOnMove(currPly) ? getBlackIsMatedScore(currPly) : getWhiteIsMatedScore(currPly);
			}
		} else {
			// Stalemate
			mateScore = pe.getScoreForStalemate();
		}
		return mateScore;
	}
}