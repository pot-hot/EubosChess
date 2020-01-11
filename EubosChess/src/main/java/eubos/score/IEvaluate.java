package eubos.score;

import eubos.search.SearchContext;

public interface IEvaluate {
	short evaluatePosition();
	boolean isQuiescent();
	boolean isThreeFoldRepetition(Long hashCode);
	SearchContext getSearchContext();
}