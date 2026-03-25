package mpi.search.result.model;


/**
 * Defines a result change listener.
 * 
 * @author Alexander Klassmann
 * @version Jul 29, 2004
 */
public interface ResultChangeListener {
	
	/**
	 * Notification of a change in the results.
	 *  
	 * @param e the result event
	 */
	public void resultChanged(ResultEvent e);
	
}
