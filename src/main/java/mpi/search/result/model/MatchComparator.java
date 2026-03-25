package mpi.search.result.model;

import java.io.Serializable;
import java.util.Comparator;


/**
 * A comparator for search matches.
 * 
 * @author klasal
 */
public class MatchComparator implements Comparator<Match>, Serializable {
    static final long serialVersionUID = 6283402238321154001L;

	/**
	 * Creates a new comparator.
	 */
    public MatchComparator() {
		super();
	}

	/**
     *
     * @param o1 first match
     * @param o2 second match
     *
     * @return comparison of values
     * 
     * @see String#compareTo(String)
     */
    @Override
	public int compare(Match o1, Match o2) {

        return o1.getValue().compareTo(o2.getValue());
    }
}
