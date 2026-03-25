package mpi.eudico.server.corpora.clomimpl.theme;

/**
 * A line in a Theme data file has this form:
 * 
 * time TAB partName,(b|e),value1,value2,...
 * 
 * where partName correlates to a tier name or participant name, b indicates a begin time 
 * point, e an end time point, and value1 etc. to one or more annotation values. 
 * <p>
 * Note: this class has a natural ordering that is inconsistent with equals.
 * 
 * @author Han Sloetjes
 */
public class ThemeLine implements Comparable<ThemeLine> {
	long time;
	String beginOrEnd;
	String partName;
	String label;
	
	/**
	 * Creates a new Them line instance.
	 * 
	 * @param time the time point
	 * @param beginOrEnd the begin or end time marker
	 * @param partName the participant or actor
	 * @param label the annotation label
	 */
	public ThemeLine(long time, String beginOrEnd, String partName, String label) {
		this.time = time;
		this.beginOrEnd = beginOrEnd;
		this.partName = partName;
		this.label = label;
	}
	
	
//	public String getValuePart() {
//		if (beginOrEnd != null && partName != null) {
//			StringBuilder sb = new StringBuilder(partName);
//			sb.append(",");
//			sb.append(beginOrEnd);
//			sb.append(label);
//			sb.append(",");
//		}
//		return "";
//	}
	
	/**
	 * Compares two Theme lines based on their time value.
	 */
	@Override
	public int compareTo(ThemeLine other) {
		if (time < other.time) {
			return -1;
		}
		if (time > other.time) {
			return 1;
		}
		
		return 0;
	}	

}
