package mpi.eudico.client.annotator.recognizer.data;


import java.util.*;

/**
 * Utility class used by SignalViewer to find Segment boundaries fast
 * 
 * There are no nested segments allowed so each segment must end before the beginning of the next segment.
 * This implementation does not check the above constraint. 
 * 
 * @author albertr
 *
 */
public class BoundarySegmentation {
	private static long INFINITY = Long.MAX_VALUE;
	private TreeSet<Boundary> boundaries;
	
	/**
	 * Constructor, builds a set of boundaries based on the segmentation.
	 * 
	 * @param segmentation the segmentation to convert into boundary objects
	 */
	public BoundarySegmentation(Segmentation segmentation) {
		boundaries = new TreeSet<Boundary>();
		List<RSelection> segments = segmentation.getSegments();
		for (int i = 0; i < segments.size(); i++) {
			Segment segment = (Segment) segments.get(i);
			Boundary begin = new Boundary(segment.beginTime, segment.label);
			// need to remove an eventual end boundary with the same time of the previous segment
			boundaries.remove(begin); 
			// because add only adds if the element is not already present
			boundaries.add(begin);
			Boundary end = new Boundary(segment.endTime, "");
			boundaries.add(end);
		}
	}
	
	/**
	 * Returns the boundary time before the specified time.
	 * 
	 * @param time the click time
	 * @return the before time
	 */
	public long boundaryTimeBefore(long time) {
		Boundary bound = new Boundary(time, "");
		SortedSet<Boundary> head = boundaries.headSet(bound);
		if (head.size() > 0) {
			return head.last().time;
		} else {
			return 0;
		}
	}
	
	/**
	 * Returns the boundary time after the specified time.
	 * 
	 * @param time the click time
	 * @return the after time
	 */
	public long boundaryTimeAfter(long time) {
		Boundary bound = new Boundary(time, "");
		SortedSet<Boundary> tail = boundaries.tailSet(bound);
		if (tail.size() > 0) {
			return tail.first().time;
		} else {
			return INFINITY;
		}
	}
	
	/**
	 * Returns the last boundary between two specified time values.
	 * 
	 * @param beginTime the begin time of the interval to search
	 * @param endTime the end time of the interval to search
	 * 
	 * @return {@code null} if there is no boundary between beginTime and endTime, 
	 * the last {@code Boundary} object otherwise (can be "")
	 */
	public Boundary boundaryBetween(long beginTime, long endTime) {
		if (beginTime >= endTime) {
			return null;
		}
		
		Boundary from = new Boundary(beginTime, "");
		Boundary to = new Boundary(endTime, "");
		SortedSet<Boundary> sub = boundaries.subSet(from, to);
		if (sub.size() == 0) {
			return null;
		} else {
			return sub.last();
		}
	}
}
