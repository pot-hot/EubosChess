package eubos.position;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import com.fluxchess.jcpi.models.GenericMove;

import eubos.board.Board;
import eubos.board.InvalidPieceException;
import eubos.board.Piece;
import eubos.board.Piece.Colour;

public class MoveList implements Iterable<Integer> {
	
	private List<Integer> normal_search_moves;
	private List<Integer> extended_search_moves;
	
    static class MoveTypeComparator implements Comparator<Integer> {
        @Override public int compare(Integer move1, Integer move2) {
            boolean gt = Move.getType(move1) < Move.getType(move2);
            boolean eq = Move.getType(move1) == Move.getType(move2);
            return gt ? 1 : (eq ? 0 : -1);
        }
    }
    
    public static final int [] MATERIAL = {0, Board.MATERIAL_VALUE_KING, Board.MATERIAL_VALUE_QUEEN, Board.MATERIAL_VALUE_ROOK, 
    		Board.MATERIAL_VALUE_BISHOP, Board.MATERIAL_VALUE_KNIGHT, Board.MATERIAL_VALUE_PAWN }; 
    
    private static final MoveMvvLvaComparator mvvLvaComparator = new MoveMvvLvaComparator();
    
    private static class MoveMvvLvaComparator implements Comparator<Integer> {
        @Override public int compare(Integer move1, Integer move2) {
        	int type1 = Move.getType(move1);
        	int type2 = Move.getType(move2);
            if (type1 < type2) {
            	return 1;
            } else if (type1 == type2) {
            	if ((type1 & Move.TYPE_CAPTURE_MASK) == 0) {
            		// Only valid for captures
            		return 0;
            	}
            	// mvv lva used for tie breaking move type comparison, if it is a capture
            	int victim1 = MATERIAL[Move.getTargetPiece(move1)&Piece.PIECE_NO_COLOUR_MASK];
            	int attacker1 = MATERIAL[Move.getOriginPiece(move1)&Piece.PIECE_NO_COLOUR_MASK];
            	int victim2 = MATERIAL[Move.getTargetPiece(move2)&Piece.PIECE_NO_COLOUR_MASK];
            	int attacker2 = MATERIAL[Move.getOriginPiece(move2)&Piece.PIECE_NO_COLOUR_MASK];
            	int mvvLvaRankingForMove1 = victim1-attacker1;
            	int mvvLvaRankingForMove2 = victim2-attacker2;
            	
            	if (mvvLvaRankingForMove1 < mvvLvaRankingForMove2) {
            		return 1;
            	} else if (mvvLvaRankingForMove1 == mvvLvaRankingForMove2) {
            		return 0;
            	} else {
            		return -1;
            	}
            } else {
            	return -1;
            }
        }
    }
	
	public MoveList(PositionManager pm) {
		this(pm, Move.NULL_MOVE);
	}
	
	public MoveList(PositionManager pm, int bestMove) {
		Colour onMove = pm.getOnMove();
		boolean validBestKillerMove = bestMove != Move.NULL_MOVE;
		boolean needToEscapeMate = false;
		if (pm.lastMoveWasCheck() || (pm.noLastMove() && pm.isKingInCheck(onMove))) {
			needToEscapeMate = true;
		}
		normal_search_moves = pm.generateMoves();
		
		ListIterator<Integer> it = normal_search_moves.listIterator();
		while (it.hasNext()) {
			int currMove = it.next();
			try {
				boolean possibleDiscoveredOrMoveIntoCheck = false;
				if (pm.getTheBoard().moveCouldLeadToOwnKingDiscoveredCheck(currMove) || Piece.isKing(Move.getOriginPiece(currMove))) {
					possibleDiscoveredOrMoveIntoCheck = true;
				}
				pm.performMove(currMove, false);
				if ((possibleDiscoveredOrMoveIntoCheck || needToEscapeMate) && pm.isKingInCheck(onMove)) {
					// Scratch any moves resulting in the king being in check, including moves that don't escape mate!
					it.remove();
				} else {
					// Set the check flag for any moves attacking the opposing king
					boolean isCheck = pm.getTheBoard().moveCouldPotentiallyCheckOtherKing(currMove) && pm.isKingInCheck(pm.getOnMove());
					boolean isBestKiller = validBestKillerMove && Move.areEqualForBestKiller(currMove, bestMove);
					if (isCheck) {
						currMove = Move.setCheck(currMove);
					}
					if (isBestKiller) {
						currMove = Move.setBestKiller(currMove);
					}
					if (isCheck || isBestKiller) {
						it.set(currMove);
					}
				}
				pm.unperformMove(false);
			} catch(InvalidPieceException e) {
				assert false;
			}
		}
		// Sort the list
		Collections.sort(normal_search_moves, mvvLvaComparator);
	}
	
	@Override
	public Iterator<Integer> iterator() {
		return normal_search_moves.iterator();
	}
	
	public Iterator<Integer> getStandardIterator(boolean extended) {
		Iterator<Integer> it;
		if (extended) {
			// Lazy creation of extended move list
			extended_search_moves = new ArrayList<Integer>(normal_search_moves.size());
			for (int currMove : normal_search_moves) {
				if (Move.isCapture(currMove) || Move.isCheck(currMove) || Move.isQueenPromotion(currMove)) {
					extended_search_moves.add(currMove);
				}
			}
			it = extended_search_moves.iterator();
		} else {
			it = normal_search_moves.iterator();
		}
		return it; 
	}
		
	public boolean isMateOccurred() {
		return (normal_search_moves.size() == 0);
	}
	
	public GenericMove getRandomMove() {
		GenericMove bestMove = null;
		if (!isMateOccurred()) {
			Random randomIndex = new Random();
			Integer indexToGet = randomIndex.nextInt(normal_search_moves.size());
			bestMove = Move.toGenericMove(normal_search_moves.get(indexToGet));		
		}
		return bestMove;
	}
	
	@Override
	public String toString() {
		String retVal = "";
		for (int move : this.normal_search_moves) {
			retVal += Move.toString(move);
			retVal += ", ";
		}
		return retVal;
	}

	public boolean hasMultipleRegularMoves() {
		int count = 0;
		for (int move : normal_search_moves) {
			if (Move.isRegular(move)) {
				count++;
				if (count == 2 )
					return true;
			}
		}
		return false;
	}

	public int getBestMove() {
		if (normal_search_moves.size() != 0) {
			return normal_search_moves.get(0);
		} else {
			return Move.NULL_MOVE;
		}
	}
	
	public int getSafestMove() {
		if (normal_search_moves.size() != 0) {
			/* The logic is to avoid checks, which will be in the highest prio indexes of the ml */
			return normal_search_moves.get(normal_search_moves.size()-1);
		} else {
			return Move.NULL_MOVE;
		}
	}
	
	// Test API
	boolean contains(int move) {
		for (int reg_move : normal_search_moves) {
			if (move == reg_move)
				return true;
		}
		return false;
	}
}
