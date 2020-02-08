package eubos.board;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import eubos.board.Piece.Colour;
import eubos.position.Move;
import eubos.position.Position;

import com.fluxchess.jcpi.models.IntFile;
import com.fluxchess.jcpi.models.GenericFile;
import com.fluxchess.jcpi.models.GenericPosition;

public class Board implements Iterable<Integer> {

	private static final int INDEX_PAWN = 0;
	private static final int INDEX_KNIGHT = 1;
	private static final int INDEX_BISHOP = 2;
	private static final int INDEX_ROOK = 3;
	private static final int INDEX_QUEEN = 4;
	private static final int INDEX_KING = 5;
	
	private BitBoard allPieces = null;
	private BitBoard whitePieces = null;
	private BitBoard blackPieces = null;
	private BitBoard[] pieces = new BitBoard[6];
	
	
	
	private static final Map<Integer, List<BitBoard>> RankFileMask_Lut = new HashMap<Integer, List<BitBoard>>();
	static {
		Direction [] rankFile = { Direction.left, Direction.up, Direction.right, Direction.down };
		for (int square : Position.values) {
			List<BitBoard> array = new ArrayList<BitBoard>();
			for (int index=1; index<8; index++) {
				createMask(square, array, index, rankFile);
			}
			RankFileMask_Lut.put(square, array);
		}
	}
	static private void createMask(int square, List<BitBoard> array, int index, Direction [] directions) {
		Long currMask = 0L;
		for (Direction dir: directions) {
			currMask = setAllInDirection(dir, square, currMask, index);
		}
		// Only add the mask if it isn't the same as previous (i.e. no more squares to add)
		BitBoard toAdd = new BitBoard(currMask);
		toAdd.setNumBits();
		if (array.size()-1 >= 0) {
			if (currMask != array.get(array.size()-1).getValue())
				array.add(toAdd);
		} else {
			array.add(toAdd);
		}
	}
	static private Long setAllInDirection(Direction dir, int fromSq, Long currMask, int index) {
		int newSquare = fromSq;
		for (int i=0; i < index; i++) {
			if (newSquare != Position.NOPOSITION)
				newSquare = Direction.getDirectMoveSq(dir, newSquare);
			if (newSquare != Position.NOPOSITION)
				currMask |= BitBoard.positionToMask_Lut[newSquare].getValue();
		}
		return currMask;
	}
	
	private static final Map<Integer, BitBoard> directAttacksOnPosition_Lut = new HashMap<Integer, BitBoard>();
	static {
		Direction [] allDirect = { Direction.left, Direction.up, Direction.right, Direction.down, Direction.downLeft, Direction.upLeft, Direction.upRight, Direction.downRight };
		for (int square : Position.values) {
			Long allAttacksMask = 0L;
			for (Direction dir: allDirect) {
				allAttacksMask = setAllInDirection(dir, square, allAttacksMask, 8);
			}
			directAttacksOnPosition_Lut.put(square, new BitBoard(allAttacksMask));
		}
	}
	
	private static final Map<Integer, List<BitBoard>> DiagonalMask_Lut = new HashMap<Integer, List<BitBoard>>();
	static {
		Direction [] diagonals = { Direction.downLeft, Direction.upLeft, Direction.upRight, Direction.downRight };
		for (int square : Position.values) {
			List<BitBoard> array = new ArrayList<BitBoard>();
			for (int index=1; index<8; index++) {
				createMask(square, array, index, diagonals);
			}
			DiagonalMask_Lut.put(square, array);
		}
	}
	
	private static final Map<GenericFile, BitBoard> FileMask_Lut = new EnumMap<GenericFile, BitBoard>(GenericFile.class);
	static {
		for (GenericFile file : GenericFile.values()) {
			long mask = 0;
			int f=IntFile.valueOf(file);
			for (int r = 0; r<8; r++) {
				mask  |= 1L << r*8+f;
			}
			FileMask_Lut.put(file, new BitBoard(mask));
		}
	}
	
	public Board( Map<Integer, Integer> pieceMap ) {
		allPieces = new BitBoard();
		whitePieces = new BitBoard();
		blackPieces = new BitBoard();
		for (int i=0; i<=INDEX_KING; i++) {
			pieces[i] = new BitBoard();
		}
		for ( Entry<Integer, Integer> nextPiece : pieceMap.entrySet() ) {
			setPieceAtSquare( nextPiece.getKey(), nextPiece.getValue() );
		}
	}
	
	public List<Integer> getRegularPieceMoves(Piece.Colour side) {
		BitBoard bitBoardToIterate = Colour.isWhite(side) ? whitePieces : blackPieces;
		ArrayList<Integer> movesList = new ArrayList<Integer>();
		for (int bit_index: bitBoardToIterate) {
			int atSquare = BitBoard.bitToPosition_Lut[bit_index];
			BitBoard pieceToPickUp = new BitBoard(1L<<bit_index);
			if (blackPieces.and(pieceToPickUp).isNonZero()) {
				if (pieces[INDEX_KING].isSet(bit_index)) {
					movesList.addAll(Piece.king_generateMoves(this, atSquare, Colour.black));
				} else if (pieces[INDEX_QUEEN].isSet(bit_index)) {
					movesList.addAll(Piece.queen_generateMoves(this, atSquare, Colour.black));
				} else if (pieces[INDEX_ROOK].isSet(bit_index)) {
					movesList.addAll(Piece.rook_generateMoves(this, atSquare, Colour.black));
				} else if (pieces[INDEX_BISHOP].isSet(bit_index)) {
					movesList.addAll(Piece.bishop_generateMoves(this, atSquare, Colour.black));
				} else if (pieces[INDEX_KNIGHT].isSet(bit_index)) {
					movesList.addAll(Piece.knight_generateMoves(this, atSquare, Colour.black));
				} else if (pieces[INDEX_PAWN].isSet(bit_index)) {
					movesList.addAll(Piece.pawn_generateMoves(this, atSquare, Colour.black));
				}
			} else if (whitePieces.and(pieceToPickUp).isNonZero()) {
				if (pieces[INDEX_KING].isSet(bit_index)) {
					movesList.addAll(Piece.king_generateMoves(this, atSquare, Colour.white));
				} else if (pieces[INDEX_QUEEN].isSet(bit_index)) {
					movesList.addAll(Piece.queen_generateMoves(this, atSquare, Colour.white));
				} else if (pieces[INDEX_ROOK].isSet(bit_index)) {
					movesList.addAll(Piece.rook_generateMoves(this, atSquare, Colour.white));
				} else if (pieces[INDEX_BISHOP].isSet(bit_index)) {
					movesList.addAll(Piece.bishop_generateMoves(this, atSquare, Colour.white));
				} else if (pieces[INDEX_KNIGHT].isSet(bit_index)) {
					movesList.addAll(Piece.knight_generateMoves(this, atSquare, Colour.white));
				} else if (pieces[INDEX_PAWN].isSet(bit_index)) {
					movesList.addAll(Piece.pawn_generateMoves(this, atSquare, Colour.white));
				}
			} else {
				assert false;
			}
		}
		return movesList;
	}
		
	private int enPassantTargetSq = Position.NOPOSITION;
	public int getEnPassantTargetSq() {
		return enPassantTargetSq;
	}
	public void setEnPassantTargetSq(int enPassantTargetSq) {
		// TODO: add bounds checking - only certain en passant squares can be legal.
		this.enPassantTargetSq = enPassantTargetSq;
	}
	
	public boolean squareIsEmpty( int atPos ) {
		return !allPieces.isSet(BitBoard.positionToBit_Lut[atPos]);		
	}
	
	public boolean squareIsAttacked( int atPos, Piece.Colour ownColour ) {
		return SquareAttackEvaluator.isAttacked(this, atPos, ownColour);
	}
	
	public int getPieceAtSquare( int atPos ) {
		int type = Piece.NONE;
		int bit_index = BitBoard.positionToBit_Lut[atPos];
		BitBoard pieceToPickUp = new BitBoard(1L<<bit_index);
		if (allPieces.and(pieceToPickUp).isNonZero()) {	
			if (blackPieces.and(pieceToPickUp).isNonZero()) {
				type |= Piece.BLACK;
			} else assert whitePieces.and(pieceToPickUp).isNonZero();
			if (pieces[INDEX_KING].isSet(bit_index)) {
				type |= Piece.KING;
			} else if (pieces[INDEX_QUEEN].isSet(bit_index)) {
				type |= Piece.QUEEN;
			} else if (pieces[INDEX_ROOK].isSet(bit_index)) {
				type |= Piece.ROOK;
			} else if (pieces[INDEX_BISHOP].isSet(bit_index)) {
				type |= Piece.BISHOP;
			} else if (pieces[INDEX_KNIGHT].isSet(bit_index)) {
				type |= Piece.KNIGHT;
			} else if (pieces[INDEX_PAWN].isSet(bit_index)) {
				type |= Piece.PAWN;
			}
		}
		return type;
	}
	
	public void setPieceAtSquare( int atPos, int pieceToPlace ) {
		assert pieceToPlace != Piece.NONE;
		int bit_index = BitBoard.positionToBit_Lut[atPos];
		if (Piece.isKing(pieceToPlace)) {
			pieces[INDEX_KING].set(bit_index);
		} else if (Piece.isQueen(pieceToPlace)) {
			pieces[INDEX_QUEEN].set(bit_index);
		} else if (Piece.isRook(pieceToPlace)) {
			pieces[INDEX_ROOK].set(bit_index);
		} else if (Piece.isBishop(pieceToPlace)) {
			pieces[INDEX_BISHOP].set(bit_index);
		} else if (Piece.isKnight(pieceToPlace)) {
			pieces[INDEX_KNIGHT].set(bit_index);
		} else if (Piece.isPawn(pieceToPlace)) {
			pieces[INDEX_PAWN].set(bit_index);
		} else {
			assert false;
		}
		if (Piece.isBlack(pieceToPlace)) {
			blackPieces.set(bit_index);
		} else {
			whitePieces.set(bit_index);
		}
		allPieces.set(bit_index);
	}
	
	public boolean isKingInCheck(Piece.Colour side) {
		boolean inCheck = false;
		BitBoard getFromBoard = Colour.isWhite(side) ? whitePieces : blackPieces;
		BitBoard kingMask = getFromBoard.and(pieces[INDEX_KING]);
		if (kingMask.isNonZero()) {
			// The conditional is needed because some unit test positions don't have a king...
			int kingSquare = BitBoard.maskToPosition_Lut.get(kingMask.getValue());
			inCheck = squareIsAttacked(kingSquare, side);
		}
		return inCheck;
	}
	
	public int pickUpPieceAtSquare( int atPos ) {
		int type = Piece.NONE;
		int bit_index = BitBoard.positionToBit_Lut[atPos];
		BitBoard pieceToPickUp = new BitBoard(1L<<bit_index);
		if (allPieces.and(pieceToPickUp).isNonZero()) {	
			if (blackPieces.and(pieceToPickUp).isNonZero()) {
				blackPieces.clear(bit_index);
				type |= Piece.BLACK;
			} else {
				assert whitePieces.and(pieceToPickUp).isNonZero();
				whitePieces.clear(bit_index);
			}
			if (pieces[INDEX_KING].isSet(bit_index)) {
				pieces[INDEX_KING].clear(bit_index);
				type |= Piece.KING;
			} else if (pieces[INDEX_QUEEN].isSet(bit_index)) {
				pieces[INDEX_QUEEN].clear(bit_index);
				type |= Piece.QUEEN;
			} else if (pieces[INDEX_ROOK].isSet(bit_index)) {
				pieces[INDEX_ROOK].clear(bit_index);
				type |= Piece.ROOK;
			} else if (pieces[INDEX_BISHOP].isSet(bit_index)) {
				pieces[INDEX_BISHOP].clear(bit_index);
				type |= Piece.BISHOP;
			} else if (pieces[INDEX_KNIGHT].isSet(bit_index)) {
				pieces[INDEX_KNIGHT].clear(bit_index);
				type |= Piece.KNIGHT;
			} else if (pieces[INDEX_PAWN].isSet(bit_index)) {
				pieces[INDEX_PAWN].clear(bit_index);
				type |= Piece.PAWN;
			}
			allPieces.clear(bit_index);
		}
		return type;
	}
	
	public int countDoubledPawnsForSide(Colour side) {
		int doubledCount = 0;
		BitBoard pawns = Colour.isWhite(side) ? getWhitePawns() : getBlackPawns();
		for (GenericFile file : GenericFile.values()) {
			BitBoard mask = FileMask_Lut.get(file);
			long fileMask = pawns.and(mask).getValue();
			int numPawnsInFile = Long.bitCount(fileMask);
			if (numPawnsInFile > 1) {
				doubledCount += numPawnsInFile-1;
			}
		}
		return doubledCount;
	}
	
	public boolean isPassedPawn(int atPos, Colour side) {
		boolean isPassed = true;
		BitBoard mask = PassedPawn_Lut.get(side.ordinal()).get(atPos);
		BitBoard otherSidePawns = Colour.isWhite(side) ? getBlackPawns() : getWhitePawns();
		if (mask.and(otherSidePawns).isNonZero()) {
			isPassed  = false;
		}
		return isPassed;
	}
	
	private static final List<Map<Integer, BitBoard>> PassedPawn_Lut = new ArrayList<Map<Integer, BitBoard>>(2); 
	static {
		Map<Integer, BitBoard> white_map = new HashMap<Integer, BitBoard>();
		PassedPawn_Lut.add(Colour.white.ordinal(), white_map);
		for (int atPos : Position.values) {
			white_map.put(atPos, buildPassedPawnFileMask(Position.getFile(atPos), Position.getRank(atPos), true));
		}
		Map<Integer, BitBoard> black_map = new HashMap<Integer, BitBoard>();
		PassedPawn_Lut.add(Colour.black.ordinal(), black_map);
		for (int atPos : Position.values) {
			black_map.put(atPos, buildPassedPawnFileMask(Position.getFile(atPos), Position.getRank(atPos), false));
		}
	}
	private static BitBoard buildPassedPawnFileMask(int f, int r, boolean isWhite) {
		long mask = 0;
		boolean hasPrevFile = IntFile.toGenericFile(f).hasPrev();
		boolean hasNextFile = IntFile.toGenericFile(f).hasNext();
		if (isWhite) {
			for (r=r+1; r < 7; r++) {
				mask = addRankForPassedPawnMask(mask, r, f, hasPrevFile,
						hasNextFile);
			}
		} else {
			for (r=r-1; r > 0; r--) {
				mask = addRankForPassedPawnMask(mask, r, f, hasPrevFile,
						hasNextFile);	
			}
		}
		return new BitBoard(mask);
	}
	private static long addRankForPassedPawnMask(long mask, int r, int f,
			boolean hasPrevFile, boolean hasNextFile) {
		if (hasPrevFile) {
			mask |= 1L << r*8+(f-1);
		}
		mask |= 1L << r*8+f;
		if (hasNextFile) {
			mask |= 1L << r*8+(f+1);
		}
		return mask;
	}
	
	public String getAsFenString() {
		int currPiece = Piece.NONE;
		int spaceCounter = 0;
		StringBuilder fen = new StringBuilder();
		for (int rank=7; rank>=0; rank--) {
			for (int file=0; file<8; file++) {
				currPiece = this.getPieceAtSquare(Position.valueOf(file,rank));
				if (currPiece != Piece.NONE) {
					if (spaceCounter != 0)
						fen.append(spaceCounter);
					fen.append(Piece.toFenChar(currPiece));
					spaceCounter=0;					
				} else {
					spaceCounter++;
				}
			}
			if (spaceCounter != 0)
				fen.append(spaceCounter);
			if (rank != 0)
				fen.append('/');
			spaceCounter=0;
		}
		return fen.toString();
	}
	
	class allPiecesOnBoardIterator implements Iterator<Integer> {

		private LinkedList<Integer> iterList = null;

		allPiecesOnBoardIterator() throws InvalidPieceException {
			iterList = new LinkedList<Integer>();
			buildIterList(allPieces);
		}

		allPiecesOnBoardIterator( Piece.Colour colourToIterate ) throws InvalidPieceException {
			iterList = new LinkedList<Integer>();
			buildIterList(Colour.isWhite(colourToIterate) ? whitePieces : blackPieces);
		}
		
		allPiecesOnBoardIterator( int typeToIterate ) throws InvalidPieceException {
			iterList = new LinkedList<Integer>();
			BitBoard bitBoardToIterate;
			if (typeToIterate == Piece.WHITE_PAWN) {
				bitBoardToIterate = getWhitePawns();
			} else if (typeToIterate == Piece.BLACK_PAWN) {
				bitBoardToIterate = getBlackPawns();
			} else {
				bitBoardToIterate = new BitBoard();
			}
			buildIterList(bitBoardToIterate);
		}

		private void buildIterList(BitBoard bitBoardToIterate) {
			for (int bit_index: bitBoardToIterate) {
				iterList.add(BitBoard.bitToPosition_Lut[bit_index]);
			}
		}	

		public boolean hasNext() {
			if (!iterList.isEmpty()) {
				return true;
			} else {
				return false;
			}
		}

		public Integer next() {
			return iterList.remove();
		}

		@Override
		public void remove() {
			iterList.remove();
		}
	}

	public Iterator<Integer> iterator() {
		// default iterator returns all the pieces on the board
		try {
			return new allPiecesOnBoardIterator( );
		} catch (InvalidPieceException e) {
			return null;
		}
	}

	public Iterator<Integer> iterateColour( Piece.Colour colourToIterate ) {
		try {
			return new allPiecesOnBoardIterator( colourToIterate );
		} catch (InvalidPieceException e) {
			return null;
		}
	}
	
	public BitBoard getMaskForType(int type) {
		BitBoard mask = null;
		switch(type) {
		case Piece.WHITE_KING:
			mask = getWhiteKing();
			break;
		case Piece.WHITE_QUEEN:
			mask = getWhiteQueens();
			break;
		case Piece.WHITE_ROOK:
			mask = getWhiteRooks();
			break;
		case Piece.WHITE_BISHOP:
			mask = getWhiteBishops();
			break;
		case Piece.WHITE_KNIGHT:
			mask = getWhiteKnights();
			break;
		case Piece.WHITE_PAWN:
			mask = getWhitePawns();
			break;
		case Piece.BLACK_KING:
			mask = getBlackKing();
			break;
		case Piece.BLACK_QUEEN:
			mask = getBlackQueens();
			break;
		case Piece.BLACK_ROOK:
			mask = getBlackRooks();
			break;
		case Piece.BLACK_BISHOP:
			mask = getBlackBishops();
			break;
		case Piece.BLACK_KNIGHT:
			mask = getBlackKnights();
			break;
		case Piece.BLACK_PAWN:
			mask = getBlackPawns();
			break;
		case Piece.NONE:
		default:
			assert false;
			break;
		}
		return mask;
	}
		
	public BitBoard getBlackPawns() {
		return blackPieces.and(pieces[INDEX_PAWN]);
	}
	
	public BitBoard getBlackKnights() {
		return blackPieces.and(pieces[INDEX_KNIGHT]);
	}
	
	public BitBoard getBlackBishops() {
		return blackPieces.and(pieces[INDEX_BISHOP]);
	}
	
	public BitBoard getBlackRooks() {
		return blackPieces.and(pieces[INDEX_ROOK]);
	}
	
	public BitBoard getBlackQueens() {
		return blackPieces.and(pieces[INDEX_QUEEN]);
	}
	
	public BitBoard getBlackKing() {
		return blackPieces.and(pieces[INDEX_KING]);
	}
	
	public BitBoard getWhitePawns() {
		return whitePieces.and(pieces[INDEX_PAWN]);
	}
	
	public BitBoard getWhiteBishops() {
		return whitePieces.and(pieces[INDEX_BISHOP]);
	}
	
	public BitBoard getWhiteRooks() {
		return whitePieces.and(pieces[INDEX_ROOK]);
	}
	
	public BitBoard getWhiteQueens() {
		return whitePieces.and(pieces[INDEX_QUEEN]);
	}
	
	public BitBoard getWhiteKnights() {
		return whitePieces.and(pieces[INDEX_KNIGHT]);
	}
	
	public BitBoard getWhiteKing() {
		return whitePieces.and(pieces[INDEX_KING]);
	}
	
	public Iterator<Integer> iterateType( int typeToIterate ) {
		try {
			return new allPiecesOnBoardIterator( typeToIterate );
		} catch (InvalidPieceException e) {
			return null;
		}
	}

	public int getNumRankFileSquaresAvailable(int atPos) {
		return getSquaresAvaillableFromPosition(atPos, RankFileMask_Lut);
	}
	
	public int getNumDiagonalSquaresAvailable(int atPos) {
		return getSquaresAvaillableFromPosition(atPos, DiagonalMask_Lut);
	}
	
	private int getSquaresAvaillableFromPosition(int atPos, Map<Integer, List<BitBoard>> maskMap ) {
		int squaresCount = 0;
		int bit = BitBoard.positionToBit_Lut[atPos];
		List<BitBoard> list = maskMap.get(atPos);
		for (BitBoard levelMask : list) {
			if (checkSingleMask(bit, levelMask))
				squaresCount = levelMask.getNumBits();
		}
		return squaresCount;
	}

	private boolean checkSingleMask(int bit, BitBoard levelMask) {
		levelMask.clear(bit);
		return allPieces.and(levelMask).getValue() == 0;
	}
	
	public boolean isOnHalfOpenFile(GenericPosition atPos, int type) {
		boolean isHalfOpen = false;
		BitBoard fileMask = new BitBoard(FileMask_Lut.get(atPos.file).getValue());
		BitBoard otherSide = Piece.getOpposite(type) == Colour.white ? whitePieces : blackPieces;
		BitBoard pawnMask = otherSide.and(pieces[INDEX_PAWN]);
		boolean opponentPawnOnFile = pawnMask.and(fileMask).isNonZero();
		if (opponentPawnOnFile) {
			BitBoard ownSide = Piece.isWhite(type) ? whitePieces : blackPieces;
			pawnMask = ownSide.and(pieces[INDEX_PAWN]);
			// and no pawns of own side
			isHalfOpen = !pawnMask.and(fileMask).isNonZero();
		}
		return isHalfOpen;
	}
	
	public boolean moveCouldLeadToDiscoveredCheck(Integer move) {
		int piece = Move.getOriginPiece(move);
		BitBoard king = (Piece.isWhite(piece)) ? getWhiteKing() : getBlackKing();
		
		if (king == null || king.getValue() == 0)  return false;
		
		int atSquare = Move.getOriginPosition(move);
		// establish if the square is on a multisquare slider mask from the king position
		BitBoard square = BitBoard.positionToMask_Lut[atSquare];
		int kingPosition = BitBoard.maskToPosition_Lut.get(king.getValue());
		BitBoard attackingSquares = directAttacksOnPosition_Lut.get(kingPosition);
		return square.and(attackingSquares).isNonZero();
	}
}
