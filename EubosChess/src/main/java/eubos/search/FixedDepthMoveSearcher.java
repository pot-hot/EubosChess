package eubos.search;

import java.util.LinkedList;

import com.fluxchess.jcpi.commands.ProtocolBestMoveCommand;
import com.fluxchess.jcpi.models.GenericMove;

import eubos.board.InvalidPieceException;
import eubos.main.EubosEngineMain;
import eubos.position.IChangePosition;
import eubos.position.IGenerateMoveList;
import eubos.position.IPositionAccessors;

public class FixedDepthMoveSearcher extends AbstractMoveSearcher {
	
	private int searchDepth = 1;
	
	public FixedDepthMoveSearcher( EubosEngineMain eubos, IChangePosition inputPm, IGenerateMoveList mlgen, IPositionAccessors pos, int searchDepth ) {
		super(eubos,inputPm,pos, new MiniMaxMoveGenerator( eubos, inputPm, mlgen, pos ));
		this.searchDepth = searchDepth;
	}
	
	@Override
	public void halt() {
		mg.terminateFindMove();
	}
	
	@Override
	public void run() {
		GenericMove selectedMove = null;
		LinkedList<GenericMove> pc = null;
		for (int depth=1; depth<searchDepth; depth++) {
			try {
				selectedMove = mg.findMove(depth, pc);
			} catch( NoLegalMoveException e ) {
				System.out.println( "Eubos has run out of legal moves for side " + pos.getOnMove().toString() );
			} catch(InvalidPieceException e ) {
				System.out.println( 
						"Serious error: Eubos can't find a piece on the board whilst searching findMove(), at "
								+ e.getAtPosition().toString() );
			}
			pc = mg.pc.toPvList();
		}
		eubosEngine.sendBestMoveCommand(new ProtocolBestMoveCommand( selectedMove, null ));
	}
}
