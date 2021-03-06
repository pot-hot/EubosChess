package eubos.search;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import eubos.position.IPositionAccessors;
import eubos.position.Move;
import eubos.search.transposition.ITransposition;

public class SearchDebugAgent {

	public static final boolean DEBUG_ENABLED = false;
	
	private String indent = "";
	private FileWriter fw;
	private String filenameBase = "";
	private int currPly = 0;
	
	public SearchDebugAgent(int moveNumber, boolean isWhite) {
		if (DEBUG_ENABLED) {
			try {
				LocalDateTime dateTime = LocalDateTime.now();
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH-mm-ss.SSSS");
				filenameBase = (dateTime.format(formatter));
				fw = new FileWriter(new File(String.format("%s_move%d_%s.txt", filenameBase, moveNumber, isWhite?"w":"b")));
			} catch (IOException e) {
			}
		}
	}
	
	public void close() {
		if (DEBUG_ENABLED) {
			try {
				if (fw != null)
					fw.close();
			} catch (IOException e) {
			}
		}
	}
	
	private void printOutput(String output) {
		try {
			fw.write(output+'\n');
			fw.flush();
		} catch (IOException e) {
		}
	}
	
	private void computeIndent() {
		indent="";
		for (int i=0; i<currPly; i++) {
			indent += "\t";
		}
	}
	
	public void nextPly() {
		if (DEBUG_ENABLED) {
			currPly++;
			computeIndent();
		}
	}
	
	public void prevPly() {
		if (DEBUG_ENABLED) {
			currPly--;
			computeIndent();
		}
	}

	void printPerformMove(int currMove) {
		if (DEBUG_ENABLED) {
			printOutput(String.format("%sdo(%s) @%d", indent, Move.toString(currMove), currPly));
		}
	}

	void printUndoMove(int currMove) {
		if (DEBUG_ENABLED) {
			printOutput(String.format("%sundo(%s) @%d", indent, Move.toString(currMove), currPly));
		}
	}

	void printBackUpScore(int currPly, int prevScore, int positionScore) {
		if (DEBUG_ENABLED) {
			printOutput(String.format("%sbackedUp was:%s now:%s @%d",
				indent, Score.toString(prevScore), Score.toString(positionScore), currPly));
		}
	}

	void printPrincipalContinuation(PrincipalContinuation pc) {
		if (DEBUG_ENABLED) {
			printOutput(String.format("%spc:%s", indent, pc.toStringAt(currPly)));
		}
	}
	
	void printMateFound() {
		if (DEBUG_ENABLED) {
			printOutput(String.format("%spossible mate @%d", indent, +currPly));
		}
	}
	
	void printRefutationFound() {
		if (DEBUG_ENABLED) {
			printOutput(String.format("%sref @%d", indent, currPly));
		}
	}
	
	void printAlphaBetaCutOffLimit(int score) {
		if (DEBUG_ENABLED) {
		}
	}

	public void printHashIsTerminalNode(ITransposition trans, long hash) {
		if (DEBUG_ENABLED) {
			printOutput(String.format("%shash:%d term:%s object:%s @%d", indent, hash, trans.report(), trans.toString(), currPly));
		}
	}

	public void printHashIsRefutation(long hash, ITransposition trans) {
		if (DEBUG_ENABLED) {
			printOutput(String.format("%shash:%d ref:%s object:%s @%d", indent, hash, trans.report(), trans.toString(), currPly));
		}
		
	}

	public void printHashIsSeedMoveList(long hash, ITransposition trans) {
		if (DEBUG_ENABLED) {
			printOutput(String.format("%shash:%d seed:%s object:%s @%d", indent, hash, trans.report(), trans.toString(), currPly));
		}
	}

	public void printAlphaBetaComparison(int prevPlyScore, short positionScore) {
		if (DEBUG_ENABLED) {
			printOutput(String.format("%sab cmp prev:%s curr:%d @%d",
				indent, Score.toString(prevPlyScore), positionScore, currPly));
		}
	}
	
	public void printAlphaBetaComparison(short prevPlyScore, short positionScore) {
		if (DEBUG_ENABLED) {
			printOutput(String.format("%sab cmp prev:%s curr:%d @%d",
				indent, prevPlyScore, positionScore, currPly));
		}
	}

	public void printTransUpdate(ITransposition trans, long hashCode) {
		if (DEBUG_ENABLED) {
			printOutput(String.format("%s%s hash:%d object:%s", indent, trans.report(), hashCode, trans.toString()));
		}		
	}
	
	public void printCreateTrans(long hashCode) {
		if (DEBUG_ENABLED) {
			printOutput(String.format("%strans create, hash:%d", indent, hashCode));
		}		
	}
	
	public void printExactTrans(long hashCode, ITransposition trans) {
		if (DEBUG_ENABLED) {
			printOutput(String.format("%strans now exact, hash:%d trans:%s", indent, hashCode, trans.report()));
		}		
	}

	public void printStartPlyInfo(ScoreTracker st, IPositionAccessors pos, byte originalSearchDepthRequiredInPly) {
		if (DEBUG_ENABLED) {
			LocalDateTime dateTime = LocalDateTime.now();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH-mm-ss.SSSSSS");
			String timestamp = dateTime.format(formatter);
			if (currPly == 0) {
				printOutput(String.format("\n\n\n NEW ITERATION %d @time %s\n\n\n", originalSearchDepthRequiredInPly, timestamp));
			}			
			printOutput(String.format("%ssearch @:%d prov:%d @time %s", indent, currPly, st.getBackedUpScoreAtPly((byte)currPly), timestamp));
			printOutput(String.format("%sfen:%s", indent, pos.getFen()));
		}
	}

	public void printRepeatedPositionHash(long hash, String fen) {
		if (DEBUG_ENABLED) {
			printOutput(String.format("%s3-fold in hash hit rep @%d hash:%d fen:%s", indent, currPly, hash, fen));
		}
	}

	public void printTransDepthCheck(int currentDepth, int newDepth) {
		if (DEBUG_ENABLED) {
			printOutput(String.format("%strans set @%d depth curr:%d depth new:%d", indent, currPly, currentDepth, newDepth));
		}		
	}

	public void printTransBoundScoreCheck(byte currentBound, short score, short score2) {
		if (DEBUG_ENABLED) {
			printOutput(String.format("%strans set @%d bound:%d curr score:%d new score:%d", indent, currPly, currentBound, score, score2));
		}	
	}

	public void inExtendedSearchAlternatives(int currMove, short score) {
		if (DEBUG_ENABLED) {
			printOutput(String.format("%sextSearch @%d move:%s alt score:%d", indent, currPly, Move.toString(currMove), score));
		}
	}

	public void printExtSearchNoMoves(int theScore) {
		if (DEBUG_ENABLED) {
			printOutput(String.format("%sextSearch NoMoves term @%d score:%s", indent, currPly, Score.getScore(theScore)));
		}
	}

	public void printRepeatedPositionSearch(long hash, String fen) {
		if (DEBUG_ENABLED) {
			printOutput(String.format("%s3-fold in search rep @%d hash:%d fen:%s", indent, currPly, hash, fen));
		}
	}

	public void printTimeStamp() {
		if (DEBUG_ENABLED) {
			LocalDateTime dateTime = LocalDateTime.now();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH-mm-ss.SSSSSS");
			printOutput(String.format("%s@time %s", indent, dateTime.format(formatter)));	
		}		
	}
}