package mpi.search.content.query.model;

import java.util.Enumeration;
import java.util.Map;

import javax.swing.tree.MutableTreeNode;

/**
 * Defines a constraint which is also a tree node.
 *
 * @author Alexander Klassmann
 * @version Aug 18, 2004
 */
public interface Constraint extends MutableTreeNode, Cloneable{
	/** a constant for all tiers */
	public static final String ALL_TIERS = "Search.Constraint.AllTiers";
	/** a constant for a custom set of tiers as defined by the user */
	public static final String CUSTOM_TIER_SET = "Search.Constraint.CustomTiers";
	/** a constant for a temporal constraint */
    public static final String TEMPORAL = "Search.Constraint.Temporal";

    /** a constant for a structural constraint */
    public static final String STRUCTURAL = "Search.Constraint.Structural";

    /** a constant for a constraint modes */
    public static final String[] MODES = { STRUCTURAL, TEMPORAL };

    /** a constant for quantifier any */
    public static final String ANY = "Search.Constraint.Any";

    /** a constant for quantifier none */
    public static final String NONE = "Search.Constraint.None";

    /** a constant for the quantifiers */
    public static final String[] QUANTIFIERS = new String[] { ANY, NONE };

    /** a constant for time relation is inside */
    public static final String IS_INSIDE = "Search.Constraint.Inside";

    /** a constant for time relation overlap */
    public static final String OVERLAP = "Search.Constraint.Overlap";

    /** a constant for time relation not inside */
    public static final String NOT_INSIDE = "Search.Constraint.NotInside";

    /** a constant for time relation no overlap */
    public static final String NO_OVERLAP = "Search.Constraint.NoOverlap";

    /** a constant for time relation left overlap */
    public static final String LEFT_OVERLAP = "Search.Constraint.LeftOverlap";

    /** a constant for time relation right overlap */
    public static final String RIGHT_OVERLAP = "Search.Constraint.RightOverlap";

    /** a constant for time relation within distance */
    public static final String WITHIN_OVERALL_DISTANCE = "Search.Constraint.WithinOverallDistance";

    /** a constant for time relation within distance of left boundary */
    public static final String WITHIN_DISTANCE_TO_LEFT_BOUNDARY = "Search.Constraint.WithinLeftDistance";

    /** a constant for time relation within distance of right boundary*/
    public static final String WITHIN_DISTANCE_TO_RIGHT_BOUNDARY = "Search.Constraint.WithinRightDistance";

    /** a constant for time relation before left distance */
    public static final String BEFORE_LEFT_DISTANCE = "Search.Constraint.BeforeLeftDistance";

    /** a constant for time relation after right distance */
    public static final String AFTER_RIGHT_DISTANCE = "Search.Constraint.AfterRightDistance";

    /** a constant for anchor time relations */
    public static final String[] ANCHOR_CONSTRAINT_TIME_RELATIONS = {
            IS_INSIDE, OVERLAP, NOT_INSIDE, NO_OVERLAP
        };

    /** a constant for dependent time relations */
    public static final String[] DEPENDENT_CONSTRAINT_TIME_RELATIONS = {
            IS_INSIDE, OVERLAP, LEFT_OVERLAP, RIGHT_OVERLAP,
            WITHIN_OVERALL_DISTANCE, WITHIN_DISTANCE_TO_LEFT_BOUNDARY,
            WITHIN_DISTANCE_TO_RIGHT_BOUNDARY, BEFORE_LEFT_DISTANCE,
            AFTER_RIGHT_DISTANCE
        };

    /**
     * Returns a map of attribute names and values.
     * 
     * @return a map of attributes
     */
    public Map<String, String> getAttributes();

    /**
     * Sets whether the search is case sensitive.
     * 
     * @param b the case sensitive flag
     */
    public void setCaseSensitive(boolean b);

    /**
     * Returns whether the search is case sensitive.
     *  
     * @return {@code true} if the search is executed case sensitive
     */
    public boolean isCaseSensitive();

    /**
     * Set the lower boundary as time value or number of annotations.
     * 
     * @param l the lower distance boundary, expressed as a time value or as 
     * a number of annotations 
     */
    public void setLowerBoundary(long l);

    /**
     * Returns the lower distance boundary.
     * 
     * @return the lower distance boundary, expressed as a time value or as 
     * a number of annotations 
     */
    public long getLowerBoundary();

    /**
     * Returns the lower boundary as a string.
     * 
     * @return the lower distance boundary as a string
     */
    public String getLowerBoundaryAsString();

    /**
     * Returns the search mode.
     * 
     * @return the search mode
     */
    public String getMode();

    /**
     * Returns the id.
     * 
     * @return the id
     */
    public String getId();

    /**
     * Sets the query pattern.
     * 
     * @param s the new search pattern
     */
    public void setPattern(String s);

    /**
     * Returns the query pattern.
     * 
     * @return the search pattern
     */
    public String getPattern();

    /**
     * Returns a Quantifier like ("ANY" or "NONE")
     *
     * @return the quantifier
     */
    public String getQuantifier();

    /**
     * Sets whether the query is a regular expression.
     * 
     * @param b the new regular expression flag
     */
    public void setRegEx(boolean b);

    /**
     * Returns whether the query is a regular expression.
     * 
     * @return the regular expression flag
     */
    public boolean isRegEx();

    /**
     * Sets the tier names.
     * 
     * @param s the names of the tiers to query
     */
    public void setTierNames(String[] s);

    /**
     * Returns the single tier name.
     * 
     * @return the single tier name of this constraint
     */
    public String getTierName();

    /**
     * Returns the tier names.
     * 
     * @return the array of tier names to search
     */
    public String[] getTierNames();
    
    /**
     * Sets the search unit.
     * 
     * @param s the new search unit
     */
    public void setUnit(String s);

    /**
     * Returns the search unit.
     * 
     * @return the search unit
     */
    public String getUnit();

    /**
     * Sets the upper distance boundary.
     * 
     * @param l the upper distance boundary, expressed as a time value or as 
     * a number of annotations 
     */
    public void setUpperBoundary(long l);

    /**
     * Returns the upper distance boundary.
     * 
     * @return the upper distance boundary, expressed as a time value or as 
     * a number of annotations 
     */
    public long getUpperBoundary();

    /**
     * Returns the upper distance boundary as a string.
     * 
     * @return the upper distance boundary as string
     */
    public String getUpperBoundaryAsString();
    
    /**
     * Sets the map of attributes.
     * 
     * @param h the new attributes map
     */
    public void setAttributes(Map<String, String> h);

    /**
     * Adds a single attribute, name and value
     *
     * @param name the attribute name
     * @param value the attribute value
     */
    public void addAttribute(String name, String value);
    
    /**
     * Returns a clone of this object.
     * 
     * @return Object the cloned object
     */
    public Object clone();
    
    /**
     * Returns whether the constraint is editable.
     * 
     * @return boolean returns true or false based on isEditable
     */
    public boolean isEditable();
    
    // specialize from MutableTreeNode
    @Override
	public Enumeration<Constraint> children();
    /**
     * Inserts a dependent constraint at the specified index.
     * 
     * @param child the child constraint
     * @param index the index where to insert
     */
	public void insert(Constraint child, int index);
	
	/**
	 * Sets the parent constraint.
	 * 
	 * @param parent the parent constraint 
	 */
	public void setParent(Constraint parent);
	
	
    @Override
	public Constraint getChildAt(int i);
}
