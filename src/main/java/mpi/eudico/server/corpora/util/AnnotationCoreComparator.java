package mpi.eudico.server.corpora.util;

import java.io.Serializable;
import java.util.Comparator;

import mpi.eudico.server.corpora.clom.AnnotationCore;

/**
 * Compares two {@link AnnotationCore} instances, first based on their begin
 * time values and then on their end time values.
 * This comparator ignores the textual value of the {@link AnnotationCore}
 * objects, therefore:
 * 
 * Note: this comparator imposes orderings that are inconsistent with equals.
 */
public class AnnotationCoreComparator implements Comparator<AnnotationCore>, Serializable {
	private static final long serialVersionUID = -8693481302488330427L;

	/**
	 * Constructs a new {@code AnnotationCore} comparator. 
	 */
	public AnnotationCoreComparator() {
		super();
	}

	/**
	 * Compares two {@code AnnotationCore} instances.
	 * 
	 * @return -1 if the begin time of the first AnnotationCore is less than the 
	 * begin time of the second, or, if equal, if the end time of the first is 
	 * less than the end time of the second, 1 if the begin time of the second 
	 * is less than that of the first, or, if equal, if the end time of the second 
	 * is less than that of the first, 0 if begin and end times are equal
	 */
	@Override
	public int compare(AnnotationCore o1, AnnotationCore o2) {
        long begin1 = o1.getBeginTimeBoundary();
        long begin2 = o2.getBeginTimeBoundary();

        // Compare begin time
        if (begin1 < begin2) {
            return -1;
        } else if (begin1 > begin2) {
            return 1;
        }

        // Begin time equal, compare end time
        long end1 = o1.getEndTimeBoundary();
        long end2 = o2.getEndTimeBoundary();

        if (end1 < end2) {
            return -1;
        } else if (end1 > end2) {
            return 1;
        }

        return 0;
	}

}
