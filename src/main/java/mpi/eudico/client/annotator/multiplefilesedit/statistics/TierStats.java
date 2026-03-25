package mpi.eudico.client.annotator.multiplefilesedit.statistics;

import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder for tier statistics for multiple files.
 * 
 * @author Han Sloetjes
 */
public 	class TierStats {
	private String tierName;
	/** a list of durations */
	public List<Long> durations;// should every individual duration be in the list or only the unique durations?
	/** the file count */
	public int numFiles;
	/** the annotation count */
	public int numAnnotations;
	/** the minimal duration */
	public long minDur;
	/** the maximal duration */
	public long maxDur;
	/** the total duration */
	public long totalDur;
	/** the latency or first occurrence */
	public long latency;
	
	/**
	 * Creates a new instance for the specified tier.
	 * 
	 * @param tierName the tier name
	 */
	public TierStats(String tierName) {
		super();
		this.tierName = tierName;
		durations = new ArrayList<Long>();
	}
	
	/**
	 * Returns the tier name.
	 * 
	 * @return the tier name
	 */
	public String getTierName() {
		return tierName;
	}
	
}
