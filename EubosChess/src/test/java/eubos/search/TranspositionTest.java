package eubos.search;

import org.junit.*;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.fluxchess.jcpi.models.GenericMove;
import com.fluxchess.jcpi.models.IllegalNotationException;

import eubos.main.EubosEngineMain;
import eubos.position.Move;
import eubos.position.MoveList;
import eubos.position.PositionManager;
import eubos.search.transposition.PrincipalVariationTransposition;

public class TranspositionTest {

	private PrincipalVariationTransposition sut;
	
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test_truncationOfPv() throws IllegalNotationException {
		if (EubosEngineMain.UCI_INFO_ENABLED) {
			byte depth = 1;
			short score = 0;
			byte scoreType = Score.lowerBound;
			MoveList ml = new MoveList(new PositionManager());
			GenericMove bestMove = new GenericMove("e2e4");
			sut = new PrincipalVariationTransposition(depth, score, scoreType, ml, bestMove);
			
			int[] pvArray = { Move.NULL_MOVE, Move.NULL_MOVE, Move.NULL_MOVE };
			List<Integer> pv = Arrays.stream(pvArray).boxed().collect(Collectors.toList());
			
			sut.setPv(pv);
			List<Integer> truncated = sut.getPv();
			assertEquals(depth, truncated.size());
		}
	}
	
	@Test
	public void test_PvDoesntNeedTruncation() throws IllegalNotationException {
		if (EubosEngineMain.UCI_INFO_ENABLED) {
			byte depth = 1;
			short score = 0;
			byte scoreType = Score.lowerBound;
			MoveList ml = new MoveList(new PositionManager());
			GenericMove bestMove = new GenericMove("e2e4");
			sut = new PrincipalVariationTransposition(depth, score, scoreType, ml, bestMove);
			
			int[] pvArray = { Move.toMove(bestMove) };
			List<Integer> pv = Arrays.stream(pvArray).boxed().collect(Collectors.toList());
			
			sut.setPv(pv);
			List<Integer> truncated = sut.getPv();
			
			assertEquals(depth, truncated.size());
			assertEquals(bestMove, Move.toGenericMove(truncated.get(0)));
		}
	}
	
	@Test
	public void test_PvValuesExpected() throws IllegalNotationException {
		if (EubosEngineMain.UCI_INFO_ENABLED) {
			byte depth = 1;
			short score = 0;
			byte scoreType = Score.lowerBound;
			MoveList ml = new MoveList(new PositionManager());
			GenericMove bestMove = new GenericMove("e2e4");
			sut = new PrincipalVariationTransposition(depth, score, scoreType, ml, bestMove);
			
			int[] pvArray = { Move.toMove(bestMove), Move.toMove(new GenericMove("d7d5")), Move.toMove(new GenericMove("d2d4")) };
			List<Integer> pv = Arrays.stream(pvArray).boxed().collect(Collectors.toList());
			
			sut.setPv(pv);
			List<Integer> truncated = sut.getPv();
			
			assertEquals(depth, truncated.size());
			assertEquals(bestMove, Move.toGenericMove(truncated.get(0)));
		}
	}

}
