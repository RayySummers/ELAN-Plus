package mpi.eudico.client.annotator.search.result.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.TreeNode;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.search.content.result.model.AbstractContentMatch;
/**
 * A match class for searches in ELAN/EAF files.
 * 
 * Created on Jul 22, 2004
 * @author Alexander Klassmann
 * @version Jul 22, 2004
 */
@SuppressWarnings("serial")
public class ElanMatch extends AbstractContentMatch
                       implements AnnotationMatch, TreeNode {
	final private Annotation annotation;
	final private ElanMatch parentMatch;
	// id of constraint which this match belongs to; used to distinguish between matches of sibling constraints
	final private String constraintId;
	final private List<ElanMatch> children = new ArrayList<ElanMatch>();
	private Annotation leftContextAnnotation;
	private Annotation rightContextAnnotation;
	private Annotation parentContextAnnotation;
	
	/**
	 * Creates an ElanMatch instance.
	 * 
	 * @param parentMatch the parent match
	 * @param annotation the annotation containing the match
	 * @param constraintId the constraint identifier
	 * @param indexWithinTier the index of the annotation in the tier
	 * @param substringIndices the indices of the matching substrings
	 */
	public ElanMatch(ElanMatch parentMatch , Annotation annotation, String constraintId, int indexWithinTier, int[][] substringIndices){
		this(parentMatch, annotation, constraintId, indexWithinTier, "", "", substringIndices);
	}
	
	/**
	 * Creates an ElanMatch instance.
	 * 
	 * @param parentMatch the parent match
	 * @param annotation the annotation containing the match
	 * @param constraintId the constraint identifier
	 * @param indexWithinTier the index of the annotation in the tier
	 * @param leftContext the left context of this match
	 * @param rightContext the right context of this match
	 * @param substringIndices the indices of the matching substrings
	 */
	public ElanMatch(
			ElanMatch parentMatch,
		Annotation annotation,
		String constraintId,
		int indexWithinTier,
		String leftContext,
		String rightContext,
		int[][] substringIndices) {
			
		this.parentMatch = parentMatch;
		this.annotation = annotation;
		this.constraintId = constraintId;
		setIndex(indexWithinTier);

		setLeftContext(leftContext);
		setRightContext(rightContext);
		setMatchedSubstringIndices(substringIndices);
	}
	
	/**

	*/
	
	/**
	 * Creates a new ElanMatch instance with parent and children information.
	 * 
	 * Author: Coralie Villes
	 *  
	 * @param parentMatch the parent match
	 * @param annotation the annotation containing the match
	 * @param constraintId the constraint identifier
	 * @param indexWithinTier the index of the annotation in the tier
	 * @param leftContext the left context of this match
	 * @param rightContext the right context of this match
	 * @param substringIndices the indices of the matching substrings
	 * @param parentContext the parent context of this match
	 * @param childrenContext the children context of this match
	 */
    public ElanMatch(ElanMatch parentMatch, Annotation annotation,
            String constraintId, int indexWithinTier, String leftContext,
            String rightContext, int[][] substringIndices, String parentContext,
            String childrenContext) {
        this.parentMatch = parentMatch;
        this.annotation = annotation;
        this.constraintId = constraintId;
        setIndex(indexWithinTier);

        setLeftContext(leftContext);
        setRightContext(rightContext);
        setMatchedSubstringIndices(substringIndices);
        setParentContext(parentContext);
        setChildrenContext(childrenContext);
    }

    /**
     * Adds a single child sub-match.
     * 
     * @param subMatch the child match
     */
	public void addChild(ElanMatch subMatch){
		children.add(subMatch);
	}
	
	/**
	 * Adds a collection of child matches.
	 * 
	 * @param subMatches the sub-matches to add
	 */
	public void addChildren(Collection<ElanMatch> subMatches){
		children.addAll(subMatches);
	}
	
	/**
	 * Sets the name of the file containing the match.
	 *  
	 * @param fileName the name of the file
	 */
	public void setFileName(String fileName){
	    this.fileName = fileName;
	}
	
	/**
	 * Returns the constraint id.
	 * 
	 * @return the constraint id
	 */
	public String getConstraintId(){
		return constraintId;
	}
	
	/**
	 * Returns the name of the tier the matching annotation is on.
	 * 
	 * @return the name of the tier or the empty string
	 */
	@Override
	public String getTierName() {
		String name = "";
		try {
			name = annotation.getTier().getName();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return name;
	}

	/**
	 * Returns the value of the matching annotation.
	 * 
	 * @return the annotation value
	 */
	@Override
	public String getValue() {
		return annotation.getValue();
	}

	@Override
	public long getBeginTimeBoundary() {
		return annotation.getBeginTimeBoundary();
	}

	@Override
	public long getEndTimeBoundary() {
		return annotation.getEndTimeBoundary();
	}

	/**
	 * Returns the matching annotation.
	 * 
	 * @return the matching annotation (or the annotation containing the match)
	 */
	public Annotation getAnnotation() {
		return annotation;
	}
	
	@Override
    public String getParentContext() {
        return parentContext;
    }
    
	/**
	 * Question: push this up to the ContentMatch interface?
	 * Or perhaps to a separate interface.
	 */
	@Override
    public Annotation getParentContextAnnotation() {
        return parentContextAnnotation;
    }
    
	/**
	 * Question: push this up to the ContentMatch interface?
	 * Or perhaps to a separate interface.
	 */
	@Override
    public Annotation getLeftContextAnnotation() {
        return leftContextAnnotation;
    }
    
	/**
	 * Question: push this up to the ContentMatch interface?
	 * Or perhaps to a separate interface.
	 */
	@Override
    public Annotation getRightContextAnnotation() {
        return rightContextAnnotation;
    }
    
	@Override
    public String getChildrenContext() {
        return childrenContext;
    }

	/**
	 * Sets the left context string of the match.
	 * 
	 * @param context the left context string
	 */
	public void setLeftContext(String context) {
		leftContext = context;
	}

	/**
	 * Sets the left context annotation of the match.
	 * 
	 * @param context the left context annotation
	 */
	public void setLeftContext(Annotation context) {
		leftContextAnnotation = context;
		setLeftContext(context.getValue());
	}

	/**
	 * Sets the right context string of the match.
	 * 
	 * @param context the right context string
	 */
	public void setRightContext(String context) {
		rightContext = context;
	}

	/**
	 * Sets the right context annotation of the match.
	 * 
	 * @param context the right context annotation
	 */
	public void setRightContext(Annotation context) {
		rightContextAnnotation = context;
		setRightContext(context.getValue());
	}

	/**
	 * Sets the parent context string of this match.
	 * 
	 * @param context the parent context string
	 */
    public void setParentContext(String context){
    	parentContext = context;
    }
    
    /**
     * Sets the parent context annotation of the match.
     * 
     * @param context the parent context annotation
     */
	public void setParentContext(Annotation context) {
		parentContextAnnotation = context;
		setParentContext(context.getValue());
	}

	/**
	 * Sets the children context string of the match.
	 * 
	 * @param context the children context string
	 */
    public void setChildrenContext(String context) {
    	childrenContext = context;
	}
    
    /**
     * Sets the substring indices of the matching parts.
     * 
     * @param substringIndices the matching substring indices
     */
	public void setMatchedSubstringIndices(int[][] substringIndices) {
		this.matchedSubstringIndices = substringIndices;
	}

	@Override
	public Enumeration<ElanMatch> children(){
		return Collections.enumeration(children);
	}
	
	/**
	 * @return {@code true}
	 */
	@Override
	public boolean getAllowsChildren(){
		return true;
	}
	
	@Override
	public TreeNode getChildAt(int index){
		return children.get(index);
	}
	
	@Override
	public int getChildCount(){
		return children.size();
	}
	
	@Override
	public int getIndex(TreeNode node){
		return children.indexOf(node);
	}
	
	@Override
	public TreeNode getParent(){
		return parentMatch;
	}
	
	@Override
	public boolean isLeaf(){
		return children.size() == 0;
	}
	
	@Override
	public String toString(){
		return annotation.getValue();
		/*
		StringBuilder sb = new StringBuilder();
		TreeNode loopNode = parentMatch;
		while(loopNode != null){
			sb.append("\t");
			loopNode = loopNode.getParent();
		}
		sb.append(annotation.getValue()+"\n");
		for(int i=0; i<children.size(); i++){
			sb.append(children.get(i));
		}
		return sb.toString();*/
	}
	
	/**
	 * Returns an {@code HTML} representation of the match.
	 * 
	 * @return {@code HTML} representation of the match tree
	 */
	public String toHTML(){
		StringBuilder sb = new StringBuilder("<HTML><BODY>");
		TreeNode loopNode = parentMatch;
		while(loopNode != null){
			loopNode = loopNode.getParent();
		}
		sb.append(annotation.getValue()+"<ul>");
		for(int i=0; i<children.size(); i++){
			sb.append(children.get(i));
		}
		sb.append("</ul>");
		sb.append("</BODY></HTML>");
		return sb.toString();
	}
}
