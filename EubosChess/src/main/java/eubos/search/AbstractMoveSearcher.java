package eubos.search;

import java.util.LinkedList;

import com.fluxchess.jcpi.models.GenericMove;

import eubos.board.InvalidPieceException;
import eubos.main.EubosEngineMain;
import eubos.position.IChangePosition;
import eubos.position.IPositionAccessors;

class SearchResult {
	public GenericMove bestMove;
	public boolean foundMate;
	
	public SearchResult(GenericMove bestMove, boolean foundMate) {
		this.bestMove = bestMove;
		this.foundMate = foundMate;
	}
}

public abstract class AbstractMoveSearcher extends Thread {

	protected EubosEngineMain eubosEngine;
	protected IChangePosition pm;
	protected IPositionAccessors pos;
	protected MiniMaxMoveGenerator mg;

	public AbstractMoveSearcher(EubosEngineMain eng, IChangePosition pm, IPositionAccessors pos, MiniMaxMoveGenerator mg) {
		super();
		this.eubosEngine = eng;
		this.pm = pm;
		this.pos = pos;
		this.mg = mg;
	}

	public AbstractMoveSearcher(Runnable target) {
		super(target);
	}

	public AbstractMoveSearcher(String name) {
		super(name);
	}

	public AbstractMoveSearcher(ThreadGroup group, Runnable target) {
		super(group, target);
	}

	public abstract void run();

	public abstract void halt();

	protected SearchResult doFindMove(GenericMove selectedMove, LinkedList<GenericMove> pc, int depth) {
		SearchResult res = null;
		try {
			res = mg.findMove(depth, pc);
		} catch( NoLegalMoveException e ) {
			System.out.println( "Eubos has run out of legal moves for side " + pos.getOnMove().toString() );
		} catch(InvalidPieceException e ) {
			System.out.println( 
					"Serious error: Eubos can't find a piece on the board whilst searching findMove(), at "
							+ e.getAtPosition().toString() );
		}
		return res;
	}

	public AbstractMoveSearcher(ThreadGroup group, String name) {
		super(group, name);
	}

	public AbstractMoveSearcher(Runnable target, String name) {
		super(target, name);
	}

	public AbstractMoveSearcher(ThreadGroup group, Runnable target, String name) {
		super(group, target, name);
	}

	public AbstractMoveSearcher(ThreadGroup group, Runnable target, String name,
			long stackSize) {
		super(group, target, name, stackSize);
	}

}