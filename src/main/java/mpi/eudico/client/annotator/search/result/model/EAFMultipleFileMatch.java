package mpi.eudico.client.annotator.search.result.model;

import mpi.search.content.result.model.AbstractContentMatch;

/**
 * A match object for matches in multiple file search.
 * 
 * Created on Aug 17, 2004
 * @author Alexander Klassmann
 * @version Aug 17, 2004
 */
@SuppressWarnings("serial")
public class EAFMultipleFileMatch extends AbstractContentMatch {
	final private String value;
	private String id;

	/**
	 * Creates a match instance for the specified value.
	 *  
	 * @param value the match value
	 */
	public EAFMultipleFileMatch(String value){
		this.value = value;
	}
	
	/**
	 * Creates a match instance with the specified id and value.
	 * 
	 * @param id the id of the match
	 * @param value the value of the match
	 */
	public EAFMultipleFileMatch(String id, String value){
		this.id = id;
		this.value = value;
	}

	/**
	 * Setter method for file name
	 * @param s the file name
	 */
	public void setFileName(String s){
		fileName = s;
	}
	
	/**
	 * Setter method for tier name
	 * @param s the tier name 
	 */
	public void setTierName(String s){
		tierName = s;
	}
	
	/**
	 * Setter method for left context
	 * @param s the left context value
	 */
	public void setLeftContext(String s){
		leftContext = s;
	}
	
	/**
	 * Setter method for right context
	 * @param s the right context value
	 */
	public void setRightContext(String s){
		rightContext = s;
	}
	
	/**
	 * Setter method for begin time
	 * @param time the begin time
	 */
	public void setBeginTimeBoundary(long time){
		beginTime = time;
	}
	
    /**
     * Setter method for end time
     * @param time the end time
     */
	public void setEndTimeBoundary(long time){
		endTime = time;
	}

	@Override
	public String getValue() {
		return value;
	}
	
	/**
	 * Sets which fragments of the value string are matching the query.
	 *  
	 * @param substringIndices a two dimensional array containing begin and end
	 *  indices of matching substrings
	 */
	public void setMatchedSubstringIndices(int[][] substringIndices){
		this.matchedSubstringIndices = substringIndices;
	}
	
	/**
	 * Setter method for id
	 * @param id the id to be set
	 */
	public void setId(String id) {
		this.id = id;	
	}
	
	/**
	 * Getter method for id
	 * @return the id
	 */
	public String getId(){
		return id;
	}
	//add children and parent context mod.Coralie Villes
	@Override
	public String getChildrenContext() {
		return null;
	}

	@Override
	public String getParentContext() {
		return null;
	}
}
