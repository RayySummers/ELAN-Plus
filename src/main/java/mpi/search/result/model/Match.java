package mpi.search.result.model;

import java.io.Serializable;


/**
 * Defines a search match.
 * 
 * @author klasal
 */
public interface Match extends Serializable {
	/**
	 * Returns the value of the match.
	 * 
	 * @return the value
	 */
    public String getValue();
}
