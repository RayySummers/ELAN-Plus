package mpi.dcr;

import java.io.Serializable;
import java.util.Comparator;


/**
 * Compares two DCSmall objects! by comparing the identifier fields.
 *
 * @author Han Sloetjes
 */
public class DCIdentifierComparator implements Comparator<DCSmall>, Serializable {	

	private static final long serialVersionUID = -9161470594181420031L;

	/**
     * Creates a new DCIdentifierComparator instance
     */
    public DCIdentifierComparator() {
        super();
    }

    /**
     * Compares two DCSmall objects! by comparing the identifier fields.
     * To do: check class (ClassCastException), check nulls
     *
     * @param dc1 the first DCSmall object
     * @param dc2 the second DCSmall object
     *
     * @return the comparison of the (string) identifiers of the data categories
     */
    @Override
	public int compare(DCSmall dc1, DCSmall dc2) {
        return dc1.getIdentifier().compareTo(dc2.getIdentifier());
    }
}
