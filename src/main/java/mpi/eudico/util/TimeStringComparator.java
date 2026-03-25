package mpi.eudico.util;

import java.io.Serializable;
import java.util.Comparator;

/**
 * A comparator that compares Strings that represent time values.
 * The time format is unknown, therefore the comparator tries to convert the
 * values first to Long values (milliseconds), then to Double values (sec.ms)
 * and finally compares the values as any other string values.
 * Empty and null values are "greater than" other strings (appear at the end
 * of a sorted list).
 * 
 * @author Han Sloetjes
 */
public class TimeStringComparator implements Comparator<String>, Serializable {
	private static final long serialVersionUID = 2754694892049247910L;

	/**
	 * Constructor.
	 */
	public TimeStringComparator() {
		super();
	}
	
	/**
	 * Compare two time strings by first trying to convert them to millisecond
	 * Long values, then to sec.ms Double values and finally assuming the 
	 * strings are in hh:mm:ss.ms or hh:mm:ss:ff format that can be compared
	 * as any other string. Empty or {@code null} strings are always after
	 * other strings.
	 * 
	 */
	@Override
	public int compare(String o1, String o2) {
		if (o1 == null && o2 == null) {
			return 0;
		}
		if ( (o1 == null || o1.isEmpty()) && (o2 != null && !o2.isEmpty())) {
			return 1;
		}
		if ( (o1 != null && !o1.isEmpty()) && (o2 == null || o2.isEmpty()) ) {
			return -1;
		}
		// try milliseconds, only return if both are in that format
		try {
			Long l1 = Long.parseLong(o1);
			Long l2 = Long.parseLong(o2);
			
			return l1.compareTo(l2);
		} catch (NumberFormatException nfe) {}
		// try sec.ms
		try {
			Double d1 = Double.parseDouble(o1);
			Double d2 = Double.parseDouble(o2);
			
			return d1.compareTo(d2);
		} catch (NumberFormatException nfe) {}
		
		// treat hh:mm:ss.mss and hh:mm:ss:ff as ordinary strings
		return o1.compareTo(o2);
	}

}
