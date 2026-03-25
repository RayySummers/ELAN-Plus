package mpi.search.model;

import mpi.search.query.model.Query;
/**
 * Defines a search engine.
 * 
 * @author klasal
 */
public interface SearchEngine {
	/**
	 * Starts execution of the specified query.
	 * 
	 * @param query the query to execute
	 * @throws Exception any exception that can occur during execution
	 */
	public void performSearch(Query query) throws Exception;
	
}
