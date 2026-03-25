package mpi.search.result.model;

/**
 * Defines a handler for search results.
 * 
 * @author klasal
 */
public interface ResultHandler {
	/**
	 * Do something with a match
	 *
	 * @param match Match that should be handled
	 * @param parameter an optional parameter string that specifies what should be
	 *        done with the match.
	 */
	public void handleMatch(Match match, String parameter);

}
