package eubos.pieces;

import java.util.LinkedList;

import com.fluxchess.jcpi.models.GenericMove;
import com.fluxchess.jcpi.models.GenericPosition;

import eubos.board.Board;
import eubos.board.BoardManager;
import eubos.board.Direction;

public class Bishop extends PieceMultisquareDirectMove {

	public Bishop( Colour Colour, GenericPosition at ) {
		colour = Colour;
		onSquare = at;
	}
	
	@Override
	public LinkedList<GenericMove> generateMoves(BoardManager bm) {
		LinkedList<GenericMove> moveList = new LinkedList<GenericMove>();
		Board theBoard = bm.getTheBoard();
		addMoves(moveList, theBoard, getAllSqs(Direction.downLeft, theBoard));
		addMoves(moveList, theBoard, getAllSqs(Direction.upLeft, theBoard));
		addMoves(moveList, theBoard, getAllSqs(Direction.downRight, theBoard));
		addMoves(moveList, theBoard, getAllSqs(Direction.upRight, theBoard));
		return moveList;
	}
}
