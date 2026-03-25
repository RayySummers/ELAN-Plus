package mpi.search.model;

/**
 * Defines asearch listener. 
 * 
 * @author Alexander Klassmann
 * @version Jul 28, 2004
 * @version April, 2005
 */
public interface SearchListener{
	
	/**
	 * Handle exceptions from search thread.
	 * 
	 * @param e the exception
	 */
	public void handleException(Exception e);
	
	/**
	 * Notifies tool that search has started.
	 */
	public void executionStarted();
	    
	/**
	 * Notifies tool that search has stopped. 
	 */
	public void executionStopped();
}
