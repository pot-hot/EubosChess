package eubos.search;

import java.util.LinkedList;

import com.fluxchess.jcpi.models.GenericMove;

class PrincipalContinuation {

	private GenericMove pc[][];
	private int searchDepthPly;

	PrincipalContinuation(int searchDepth) {
		pc = new GenericMove[searchDepth][searchDepth];
		searchDepthPly = searchDepth;
	}
	
	GenericMove getBestMove() {
		return pc[0][0];
	}
	
	GenericMove getBestMove(int currPly) {
		return pc[currPly][currPly];
	}

	String toStringAfter(int currPly) {
		GenericMove currMove = pc[currPly][currPly];
		String output = ""+currMove;
		for ( int nextPly = currPly+1; nextPly < searchDepthPly; nextPly++) {
			currMove = pc[currPly][nextPly];
			if (currMove == null)
				break;
			output+=(", "+currMove);
		}
		return output;
	}
	
	LinkedList<GenericMove> toPvList() {
		LinkedList<GenericMove> mv;
		mv = new LinkedList<GenericMove>();
		for (int currPly=0; currPly < searchDepthPly; currPly++) {
			GenericMove currMove = pc[0][currPly]; 
			if (currMove != null) {
				mv.add(currMove);
			}
		}
		return mv;
	}

	void update(int currPly, GenericMove currMove) {
		// Update Principal Continuation
		pc[currPly][currPly]=currMove;
		for (int nextPly=currPly+1; nextPly < searchDepthPly; nextPly++) {
			pc[currPly][nextPly]=pc[currPly+1][nextPly];
		}
		SearchDebugAgent.printPrincipalContinuation(currPly,this);
	}

	void clearAfter(int currPly ) {
		// Clear the principal continuation after the indicated ply depth
		for (int nextPly=currPly+1; nextPly < searchDepthPly; nextPly++) {
			for (int i=0; i<searchDepthPly; i++)
				// All plies need to be cleared.
				pc[i][nextPly]=null;
		}
	}	
}
