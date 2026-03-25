package mpi.eudico.client.annotator.recognizer.data;

/**
 * Data container for a segment boundary.
 * <p>
 * Note: this class has a natural ordering that is inconsistent with equals.
 * 
 * @author albertr
 *
 */
public class Boundary implements Comparable<Boundary> {
	/** the time identifier */
	public long time;
	/** the label identifier */
	public String label;
	
	/**
	 * Constructor
	 * @param time the boundary time
	 * @param label the label to the boundary
	 */
	public Boundary(long time, String label) {
		this.time = time;
		this.label = label;
	}
	
	@Override
	public int compareTo(Boundary otherBoundary) {
		return (int)(time - otherBoundary.time);
	}
}