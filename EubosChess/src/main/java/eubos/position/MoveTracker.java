package eubos.position;

class MoveTracker {
	
	private static final int CAPACITY = 400;
	private TrackedMove[] stack;
	private int index = 0;
	
	MoveTracker() {
		stack = new TrackedMove[CAPACITY];
		for (int i = 0; i < CAPACITY; i++) {
			stack[i] = new TrackedMove(Move.NULL_MOVE);
		}
		index = 0;
	}
	
	public void push(int move, int cap, int enPassant, int castlingFlags) {
		if (index < CAPACITY) {
			stack[index].setCaptureData(cap);
			stack[index].setEnPassantTarget(enPassant);
			stack[index].setMove(move);
			stack[index].setCastlingFlags(castlingFlags);
			index += 1;
		}
	}
	
	public TrackedMove pop() {
		TrackedMove tm = null;
		if (!isEmpty()) {
			index -= 1;
			tm = stack[index];
		}
		return tm;
	}
	
	boolean lastMoveWasCapture() {
		boolean wasCapture = false;
		if ( !this.isEmpty()) {
			wasCapture = Move.isCapture(stack[index-1].getMove());
		}
		return wasCapture;
	}
	
	boolean lastMoveWasCastle() {
		boolean wasCastle = false;
		if ( !this.isEmpty()) {
			wasCastle = Move.isCastle(stack[index-1].getMove());
		}
		return wasCastle;
	}

	public int getCaptureData() {
		int captured = 0;
		if ( !isEmpty()) {
			captured = stack[index-1].getCaptureData();
		}
		return captured;
	}

	public boolean lastMoveWasPromotion() {
		boolean wasPromotion = false;
		if ( !this.isEmpty()) {
			wasPromotion = Move.isPromotion(stack[index-1].getMove());
		}
		return wasPromotion;
	}

	public boolean lastMoveWasCheck() {
		boolean wasCheck = false;
		if ( !isEmpty()) {
			wasCheck = Move.isCheck(stack[index-1].getMove());
		}
		return wasCheck;
	}
	
	public boolean isEmpty() {
		return index == 0;
	}
}