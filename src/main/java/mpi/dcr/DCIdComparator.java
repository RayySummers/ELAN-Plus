package mpi.dcr;

import java.io.Serializable;
import java.util.Comparator;


/**
 * Compares two DCSmall objects! by comparing the idAsInteger fields.
 *
 * @author Han Sloetjes
 */
public class DCIdComparator implements Comparator<DCSmall>, Serializable {

	private static final long serialVersionUID = 1515538628259897510L;

	/**
     * Creates a new DCIdComparator instance
     */
    public DCIdComparator() {
        super();
    }

    /**
     * Compares to objects containing a summary of the information on a data
     * category. To do: check class (ClassCastException), check nulls
     *
     * @param dc1 the first DCSmall object to compare
     * @param dc2 the second DCSmall object to compare
     *
     * @return -1 if the id of the first object is less than the id of the
     *         second object
     */
    @Override
	public int compare(DCSmall dc1, DCSmall dc2) {
        return dc1.getIdAsInteger().compareTo(dc2.getIdAsInteger());
    }
}
