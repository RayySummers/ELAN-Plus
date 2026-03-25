package mpi.search.content.result.model;

import mpi.eudico.server.corpora.clom.AnnotationCore;
import mpi.search.result.model.Match;

/**
 * Interface defining methods for retrieving information from a 
 * content search match.
 * 
 * Created on Jul 22, 2004
 * @author Alexander Klassmann
 * @version Jul 22, 2004
 */
public interface ContentMatch extends AnnotationCore, Match {
	/**
	 * Returns the file name.
	 * 
	 * @return the file name in which the match is found
	 */
	public String getFileName();

	/**
	 * Returns the tier name.
	 * @return the name of the tier the match (annotation) is on
	 */
	public String getTierName();
	
	/**
	 * Returns the left context of this match.
	 * 
	 * @return the left context of the match as a string
	 */
	public String getLeftContext();
	
	/**
	 * Returns the right context of this match.
	 * 
	 * @return the right context of the match as a string
	 */
	public String getRightContext();
	
	//mod. Coralie Villes
	/**
	 * Returns the parent context of this match.
	 * 
	 * Author: Coralie Villes
	 * @return the parent context of the match
	 */
	public String getParentContext();
	
	/**
	 * Returns the dependents' context of the match.
	 * 
	 * @return the children context of the match
	 */
	public String getChildrenContext();
	
	/**
	 * There can be multiple matches in a string, each identified by the begin
	 * token index and end token index.
	 * 
	 * @return a two dimensional array identifying the matched substrings
	 */
	public int[][] getMatchedSubstringIndices();
	
	/**
	 * Returns the index of this match.
	 * 
	 * @return the index of the match
	 */
	public int getIndex();
	
}
