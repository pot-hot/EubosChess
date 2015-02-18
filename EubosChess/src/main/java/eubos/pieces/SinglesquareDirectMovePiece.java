package eubos.pieces;

import java.util.LinkedList;

import com.fluxchess.jcpi.models.GenericMove;

import eubos.board.Board;

public abstract class SinglesquareDirectMovePiece extends DirectMovePiece {
	public abstract LinkedList<GenericMove> generateMoveList(Board theBoard);
}
