/**
 * 
 */
package mpi.eudico.util;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Comparator for Integer objects. 
 * (I would expect there was a standard class for comparing int's?)
 * 
 * @param <T> the type of objects to compare, Integers
 * 
 * @author Han Sloetjes
 */
public class IntComparator<T> implements Comparator<Integer>, Serializable {
	private static final long serialVersionUID = 3482775626094930625L;

	/**
	 * Constructor.
	 */
	public IntComparator() {
		super();
	}

	@Override
	public int compare(Integer o1, Integer o2) {
		if (o1 == null) {
			throw new NullPointerException("The first Integer objects is null");
		}
		if (o2 == null) {
			throw new NullPointerException("The second Integer objects is null");
		}
		return o1.compareTo(o2);
	}

}
