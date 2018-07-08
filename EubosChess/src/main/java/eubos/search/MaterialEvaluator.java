package eubos.search;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import com.fluxchess.jcpi.models.GenericPosition;

import eubos.board.Board;
import eubos.board.pieces.Bishop;
import eubos.board.pieces.King;
import eubos.board.pieces.Knight;
import eubos.board.pieces.Pawn;
import eubos.board.pieces.Piece;
import eubos.board.pieces.Queen;
import eubos.board.pieces.Rook;

public class MaterialEvaluator {
	
	public static final int KING_VALUE = 300000;
	static final int QUEEN_VALUE = 900;
	static final int ROOK_VALUE = 500;
	static final int BISHOP_VALUE = 320;
	static final int KNIGHT_VALUE = 300;
	static final int PAWN_VALUE = 100;
	
	private static final Map<GenericPosition, Integer> PAWN_WEIGHTINGS;
    static {
    	Map<GenericPosition, Integer> aMap = new HashMap<GenericPosition, Integer>();
        aMap.put(GenericPosition.a1, 0);aMap.put(GenericPosition.b1, 0);aMap.put(GenericPosition.c1, 0);aMap.put(GenericPosition.d1, 0);aMap.put(GenericPosition.e1, 0);aMap.put(GenericPosition.f1, 0);aMap.put(GenericPosition.g1, 0);aMap.put(GenericPosition.h1, 0);
        aMap.put(GenericPosition.a2, 0);aMap.put(GenericPosition.b2, 0);aMap.put(GenericPosition.c2, 0);aMap.put(GenericPosition.d2, 0);aMap.put(GenericPosition.e2, 0);aMap.put(GenericPosition.f2, 0);aMap.put(GenericPosition.g2, 0);aMap.put(GenericPosition.h2, 0);
        aMap.put(GenericPosition.a3, 0);aMap.put(GenericPosition.b3, 0);aMap.put(GenericPosition.c3, 0);aMap.put(GenericPosition.d3, 5);aMap.put(GenericPosition.e3, 5);aMap.put(GenericPosition.f3, 0);aMap.put(GenericPosition.g3, 0);aMap.put(GenericPosition.h3, 0);
        aMap.put(GenericPosition.a4, 0);aMap.put(GenericPosition.b4, 0);aMap.put(GenericPosition.c4, 5);aMap.put(GenericPosition.d4, 10);aMap.put(GenericPosition.e4, 10);aMap.put(GenericPosition.f4, 5);aMap.put(GenericPosition.g4, 0);aMap.put(GenericPosition.h4, 0);
        aMap.put(GenericPosition.a5, 0);aMap.put(GenericPosition.b5, 0);aMap.put(GenericPosition.c5, 5);aMap.put(GenericPosition.d5, 10);aMap.put(GenericPosition.e5, 10);aMap.put(GenericPosition.f5, 5);aMap.put(GenericPosition.g5, 0);aMap.put(GenericPosition.h5, 0);
		aMap.put(GenericPosition.a6, 0);aMap.put(GenericPosition.b6, 0);aMap.put(GenericPosition.c6, 0);aMap.put(GenericPosition.d6, 5);aMap.put(GenericPosition.e6, 5);aMap.put(GenericPosition.f6, 0);aMap.put(GenericPosition.g6, 0);aMap.put(GenericPosition.h6, 0);
		aMap.put(GenericPosition.a7, 0);aMap.put(GenericPosition.b7, 0);aMap.put(GenericPosition.c7, 0);aMap.put(GenericPosition.d7, 0);aMap.put(GenericPosition.e7, 0);aMap.put(GenericPosition.f7, 0);aMap.put(GenericPosition.g7, 0);aMap.put(GenericPosition.h7, 0);
		aMap.put(GenericPosition.a8, 0);aMap.put(GenericPosition.b8, 0);aMap.put(GenericPosition.c8, 0);aMap.put(GenericPosition.d8, 0);aMap.put(GenericPosition.e8, 0);aMap.put(GenericPosition.f8, 0);aMap.put(GenericPosition.g8, 0);aMap.put(GenericPosition.h8, 0);
        PAWN_WEIGHTINGS = Collections.unmodifiableMap(aMap);
    }
	
	private static final Map<GenericPosition, Integer> KNIGHT_WEIGHTINGS;
    static {
    	Map<GenericPosition, Integer> bMap = new HashMap<GenericPosition, Integer>();
        bMap.put(GenericPosition.a1, 0);bMap.put(GenericPosition.b1, 0);bMap.put(GenericPosition.c1, 0);bMap.put(GenericPosition.d1, 0);bMap.put(GenericPosition.e1, 0);bMap.put(GenericPosition.f1, 0);bMap.put(GenericPosition.g1, 0);bMap.put(GenericPosition.h1, 0);
		bMap.put(GenericPosition.a2, 0);bMap.put(GenericPosition.b2, 0);bMap.put(GenericPosition.c2, 0);bMap.put(GenericPosition.d2, 0);bMap.put(GenericPosition.e2, 0);bMap.put(GenericPosition.f2, 0);bMap.put(GenericPosition.g2, 0);bMap.put(GenericPosition.h2, 0);
		bMap.put(GenericPosition.a3, 0);bMap.put(GenericPosition.b3, 0);bMap.put(GenericPosition.c3, 10);bMap.put(GenericPosition.d3, 10);bMap.put(GenericPosition.e3, 10);bMap.put(GenericPosition.f3, 10);bMap.put(GenericPosition.g3, 0);bMap.put(GenericPosition.h3, 0);
		bMap.put(GenericPosition.a4, 0);bMap.put(GenericPosition.b4, 0);bMap.put(GenericPosition.c4, 10);bMap.put(GenericPosition.d4, 20);bMap.put(GenericPosition.e4, 20);bMap.put(GenericPosition.f4, 10);bMap.put(GenericPosition.g4, 0);bMap.put(GenericPosition.h4, 0);
		bMap.put(GenericPosition.a5, 0);bMap.put(GenericPosition.b5, 0);bMap.put(GenericPosition.c5, 10);bMap.put(GenericPosition.d5, 20);bMap.put(GenericPosition.e5, 20);bMap.put(GenericPosition.f5, 10);bMap.put(GenericPosition.g5, 0);bMap.put(GenericPosition.h5, 0);
		bMap.put(GenericPosition.a6, 0);bMap.put(GenericPosition.b6, 0);bMap.put(GenericPosition.c6, 10);bMap.put(GenericPosition.d6, 10);bMap.put(GenericPosition.e6, 10);bMap.put(GenericPosition.f6, 10);bMap.put(GenericPosition.g6, 0);bMap.put(GenericPosition.h6, 0);
		bMap.put(GenericPosition.a7, 0);bMap.put(GenericPosition.b7, 0);bMap.put(GenericPosition.c7, 0);bMap.put(GenericPosition.d7, 0);bMap.put(GenericPosition.e7, 0);bMap.put(GenericPosition.f7, 0);bMap.put(GenericPosition.g7, 0);bMap.put(GenericPosition.h7, 0);
		bMap.put(GenericPosition.a8, 0);bMap.put(GenericPosition.b8, 0);bMap.put(GenericPosition.c8, 0);bMap.put(GenericPosition.d8, 0);bMap.put(GenericPosition.e8, 0);bMap.put(GenericPosition.f8, 0);bMap.put(GenericPosition.g8, 0);bMap.put(GenericPosition.h8, 0);
        KNIGHT_WEIGHTINGS = Collections.unmodifiableMap(bMap);
    }	

	private static final Map<GenericPosition, Integer>BISHOP_WEIGHTINGS;
    static {
    	Map<GenericPosition, Integer> cMap = new HashMap<GenericPosition, Integer>();
		cMap.put(GenericPosition.a1, 5);cMap.put(GenericPosition.b1, 0);cMap.put(GenericPosition.c1, 0);cMap.put(GenericPosition.d1, 0);cMap.put(GenericPosition.e1, 0);cMap.put(GenericPosition.f1, 0);cMap.put(GenericPosition.g1, 0);cMap.put(GenericPosition.h1, 5);
		cMap.put(GenericPosition.a2, 0);cMap.put(GenericPosition.b2, 5);cMap.put(GenericPosition.c2, 0);cMap.put(GenericPosition.d2, 0);cMap.put(GenericPosition.e2, 0);cMap.put(GenericPosition.f2, 0);cMap.put(GenericPosition.g2, 5);cMap.put(GenericPosition.h2, 0);
		cMap.put(GenericPosition.a3, 0);cMap.put(GenericPosition.b3, 0);cMap.put(GenericPosition.c3, 5);cMap.put(GenericPosition.d3, 0);cMap.put(GenericPosition.e3, 0);cMap.put(GenericPosition.f3, 5);cMap.put(GenericPosition.g3, 0);cMap.put(GenericPosition.h3, 0);
		cMap.put(GenericPosition.a4, 0);cMap.put(GenericPosition.b4, 0);cMap.put(GenericPosition.c4, 0);cMap.put(GenericPosition.d4, 5);cMap.put(GenericPosition.e4, 5);cMap.put(GenericPosition.f4, 0);cMap.put(GenericPosition.g4, 0);cMap.put(GenericPosition.h4, 0);
		cMap.put(GenericPosition.a5, 0);cMap.put(GenericPosition.b5, 0);cMap.put(GenericPosition.c5, 0);cMap.put(GenericPosition.d5, 5);cMap.put(GenericPosition.e5, 5);cMap.put(GenericPosition.f5, 0);cMap.put(GenericPosition.g5, 0);cMap.put(GenericPosition.h5, 0);
		cMap.put(GenericPosition.a6, 0);cMap.put(GenericPosition.b6, 0);cMap.put(GenericPosition.c6, 5);cMap.put(GenericPosition.d6, 0);cMap.put(GenericPosition.e6, 0);cMap.put(GenericPosition.f6, 5);cMap.put(GenericPosition.g6, 0);cMap.put(GenericPosition.h6, 0);
		cMap.put(GenericPosition.a7, 0);cMap.put(GenericPosition.b7, 5);cMap.put(GenericPosition.c7, 0);cMap.put(GenericPosition.d7, 0);cMap.put(GenericPosition.e7, 0);cMap.put(GenericPosition.f7, 0);cMap.put(GenericPosition.g7, 5);cMap.put(GenericPosition.h7, 0);
		cMap.put(GenericPosition.a8, 5);cMap.put(GenericPosition.b8, 0);cMap.put(GenericPosition.c8, 0);cMap.put(GenericPosition.d8, 0);cMap.put(GenericPosition.e8, 0);cMap.put(GenericPosition.f8, 0);cMap.put(GenericPosition.g8, 0);cMap.put(GenericPosition.h8, 5);
        BISHOP_WEIGHTINGS = Collections.unmodifiableMap(cMap);
    }    
	public int evaluate(Board theBoard) {
		Iterator<Piece> iter_p = theBoard.iterator();
		int materialEvaluation = 0;
		while ( iter_p.hasNext() ) {
			Piece currPiece = iter_p.next();
			int currValue = 0;
			if ( currPiece instanceof Pawn ) {
				currValue = PAWN_VALUE;
				currValue += PAWN_WEIGHTINGS.get(currPiece.getSquare());
			}
			else if ( currPiece instanceof Rook )
				currValue = ROOK_VALUE;
			else if ( currPiece instanceof Bishop ) {
				currValue = BISHOP_VALUE;
				currValue += BISHOP_WEIGHTINGS.get(currPiece.getSquare());
			}
			else if ( currPiece instanceof Knight ) {
				currValue = KNIGHT_VALUE;
				currValue += KNIGHT_WEIGHTINGS.get(currPiece.getSquare());
			}
			else if ( currPiece instanceof Queen )
				currValue = QUEEN_VALUE;
			else if ( currPiece instanceof King )
				currValue = KING_VALUE;
			currValue = encourageDeployment(currPiece, currValue);
			if (currPiece.isBlack()) currValue = -currValue;
			materialEvaluation += currValue;
		}
		return materialEvaluation;
	}
	
	private int encourageDeployment(Piece currPiece, int currValue) {
		if (!(currPiece instanceof King)) {
			if (currPiece.hasEverMoved()) {
				currValue += 1;
			}
		}
		return currValue;
	}
}
