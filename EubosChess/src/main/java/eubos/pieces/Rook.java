package eubos.pieces;

import java.util.LinkedList;
import java.util.List;

import com.fluxchess.jcpi.models.GenericMove;
import com.fluxchess.jcpi.models.GenericPosition;

import eubos.board.Board;
import eubos.board.BoardManager;
import eubos.board.Direction;

public class Rook extends PieceMultisquareDirectMove {

	public Rook( Colour Colour, GenericPosition at ) {
		colour = Colour;
		onSquare = at;
	}
	
	@Override
	public List<GenericMove> generateMoves(BoardManager bm) {
		LinkedList<GenericMove> moveList = new LinkedList<GenericMove>();
		Board theBoard = bm.getTheBoard();
		addMoves(moveList, theBoard, getAllSqs(Direction.down, theBoard));
		addMoves(moveList, theBoard, getAllSqs(Direction.up, theBoard));
		addMoves(moveList, theBoard, getAllSqs(Direction.left, theBoard));
		addMoves(moveList, theBoard, getAllSqs(Direction.right, theBoard));
		return moveList;	
	}

	@Override
	public boolean attacks(BoardManager bm, GenericPosition [] pos) {
		Board theBoard = bm.getTheBoard();
		List<GenericPosition> targetSqs = getAllSqs(Direction.down, theBoard);
		targetSqs.addAll(getAllSqs(Direction.up, theBoard));
		targetSqs.addAll(getAllSqs(Direction.left, theBoard));
		targetSqs.addAll(getAllSqs(Direction.right, theBoard));
		return (evaluateIfAttacks( pos, targetSqs ));
	}
}
