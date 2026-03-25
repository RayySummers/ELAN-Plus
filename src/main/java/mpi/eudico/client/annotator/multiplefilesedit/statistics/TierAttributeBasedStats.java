package mpi.eudico.client.annotator.multiplefilesedit.statistics;

import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder for information based on an attribute of tiers for multiple files.
 * To be used for annotator, participant, linguistic type, content language.
 *
 * @author Han Sloetjes
 */
public 	class TierAttributeBasedStats {
	private String attributeValue;
	/** a list of duration values */
	public List<Long> durations;// should every individual duration be in the list or only the unique durations?
	//public int numFiles;
	/** the tier count */
	public int numTiers;
	/** the annotation count */
	public int numAnnotations;
	/** the minimal duration  */
	public long minDur;
	/** the maximal duration */
	public long maxDur;
	/** the total duration */
	public long totalDur;
	/** the latency or first occurrence */
	public long latency;
	
	private List<String> fileNames;
	private List<String> tierNames;
	
	/**
	 * Constructor.
	 * 
	 * @param attributeValue the value of the tier attribute
	 */
	public TierAttributeBasedStats(String attributeValue) {
		super();
		this.attributeValue = attributeValue;
		fileNames = new ArrayList<String>();
		tierNames = new ArrayList<String>();
		durations = new ArrayList<Long>();
	}
	
	/**
	 * Returns the attribute value the statistics are created for.
	 * 
	 * @return the tier attribute value
	 */
	public String getAttributeValue() {
		return attributeValue;
	}
	
	/**
	 * Adds a file name if it isn't already in the list.
	 * 
	 * @param fileName the file name to add
	 */
	public void addFileName(String fileName) {
		if (!fileNames.contains(fileName)) {
			fileNames.add(fileName);
		}
	}
	
	/**
	 * Adds a tier name to the list.
	 * 
	 * @param name the tier name
	 */
	public void addTierName(String name) {
		if (!tierNames.contains(name)) {
			tierNames.add(name);
		}
	}
	
	/**
	 * Returns the number of unique file names.
	 * 
	 * @return the file count
	 */
	public int getNumFiles() {
		return fileNames.size();
	}
	
	/**
	 * Returns the number of unique tiers.
	 * 
	 * @return the tier count
	 */
	public int getNumUniqueTiers() {
		return tierNames.size();
	}
}
