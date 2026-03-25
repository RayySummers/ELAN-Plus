package mpi.eudico.server.corpora.util;

import java.io.Serializable;
import java.util.Comparator;

import mpi.eudico.server.corpora.clom.AnnotationCore;

/**
 * Compares two annotations based on their value.
 * Empty or {@code null} values are "greater than" non-empty values (appear
 * after the other values).
 * 
 * @author Han Sloetjes
 *
 * @param <T> expected types are AnnotationCore and String
 */
public class AnnotationValueComparator<T> implements Comparator<T>, Serializable {
	private static final long serialVersionUID = 4331378582466468232L;

	/**
	 * Constructor.
	 */
	public AnnotationValueComparator() {
		super();
	}

	/**
	 * Compares the textual values of annotations based on natural string
	 * ordering, but placing empty values after non-empty values.
	 */
	@Override
	public int compare(T o1, T o2) {
		if (o1 == null && o2 == null) {
			return 0;
		}
		if (o1 == null) { // o2 != null
			return 1;
		}
		if (o2 == null) { // o1 != null
			return -1;
		}
		
		String s1 = null;
		String s2 = null;
		if (o1 instanceof AnnotationCore) {
			s1 = ((AnnotationCore) o1).getValue();
		} else if (o1 instanceof String) {
			s1 = (String) o1;
		} else {
			s1 = o1.toString();
		}
		
		if (o2 instanceof AnnotationCore) {
			s2 = ((AnnotationCore) o2).getValue();
		} else if (o2 instanceof String) {
			s2 = (String) o2;
		} else {
			s2 = o2.toString();
		}
		
		if (s1.isEmpty() && !s2.isEmpty()) {
			return 1;
		}
		if (!s1.isEmpty() && s2.isEmpty()) {
			return -1;
		}
		
		return s1.compareTo(s2);
	}

}
