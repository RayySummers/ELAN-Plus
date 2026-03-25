package mpi.eudico.client.annotator.transcriptionMode;

import mpi.eudico.util.TimeFormatter;

/**
 * A placeholder object for table cells that don't contain or represent
 * an actual annotation in the Transcription Mode table.
 * The information can be used for rendering, for tool tips and for
 * determining whether an annotation can be created in that cell.
 * 
 * @author Han Sloetjes
 */
public class AnnotationCellPlaceholder {
	// all fields are public, direct access
	/** boolean can create variable */
	public boolean canCreate = true;
	/** tier name */
	public String tierName;
	/** begin time */
	public long bt;
	/** end time */
	public long et;
	
	/**
	 * Constructor
	 */
	public AnnotationCellPlaceholder() {
		super();
	}

	/**
	 * Constructor to initialize the instance variables
	 * @param canCreate boolean parameter to determine to create or not
	 * @param tierName name of the tier
	 * @param bt begin time of annotation
	 * @param et end time of annotation
	 */
	public AnnotationCellPlaceholder(boolean canCreate, String tierName,
			long bt, long et) {
		super();
		this.canCreate = canCreate;
		this.tierName = tierName;
		this.bt = bt;
		this.et = et;
	}

	/**
	 * Returns the time interval as a string.
	 *  
	 * @return a string representation of the time interval represented by this cell
	 */
	public String getTimeInterval() {
		return TimeFormatter.toString(bt) + " - " + TimeFormatter.toString(et);
	}
	
	/**
	 * Returns a parameter string with the values of all members.
	 * 
	 * @return a string representation of all fields in this object
	 */
	public String paramString() {
		StringBuilder sb = new StringBuilder();
		if (canCreate) {
			sb.append("CAN_CREATE");
		} else {
			sb.append("CANNOT_CREATE");
		}
		sb.append(';');
		sb.append(tierName);
		sb.append(';');
		sb.append(TimeFormatter.toString(bt));
		sb.append(" - ");
		sb.append(TimeFormatter.toString(et));
		
		return sb.toString();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return paramString();
	}	
	
}
