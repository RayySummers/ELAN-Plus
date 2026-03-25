package mpi.search.content.query.model;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;


/**
 * Super class for AnchorConstraint and DepedentConstraint
 *
 * @author Alexander Klassmann
 * @version November 2004
 */
public abstract class AbstractConstraint implements Constraint {
    /** Holds TEMPORAL or STRUCTURAL */
    protected String mode = Constraint.TEMPORAL;

    /** Holds ANY or NONE */
    protected String quantifier = Constraint.ANY;

    /** An array of tier names */
    protected String[] tierNames = new String[0];

    /** Holds parent node */
    private Constraint parent = null;

    /** An attributes map */
    private Map<String, String> attributes;

    /** The pattern string */
    private String patternString = "";

    /** The unit string */
    private String unit;

    /** Holds node children of this node */
    private List<Constraint> children = new ArrayList<Constraint>();

    /** The case sensitive flag */
    private boolean isCaseSensitive = false;

    /** The regular expression flag */
    private boolean isRegEx = false;

    /** The lower boundary */
    private long lowerBoundary = Long.MIN_VALUE;

    /** The upper boundary */
    private long upperBoundary = Long.MAX_VALUE;

    /**
     * Creates a new AbstractConstraint object.
     */
    public AbstractConstraint() {
    }

    /**
     * Constructor.
     *
     * @param tierNames constraint number within a query
     * @param patternString string/regular expression to be searched
     * @param lowerBoundary negative number (of units) (e.g. 0, -1, -2, ... -X)
     * @param upperBoundary positive number (of units) (e.g. 0, 1, 2 ... +X)
     * @param unit search unit in which should be searched (in respect to
     *        referential constraint)
     * @param isRegEx string or regular expression ?
     * @param isCaseSensitive case sensitive string search ?
     * @param attributes should contain (as strings) attribute names (key) and
     *        values (value)
     */
    public AbstractConstraint(String[] tierNames, String patternString,
        long lowerBoundary, long upperBoundary, String unit, boolean isRegEx,
        boolean isCaseSensitive, Map<String, String> attributes) {
        this.tierNames = tierNames;
        this.patternString = patternString;
        this.lowerBoundary = lowerBoundary;
        this.upperBoundary = upperBoundary;
        this.unit = unit;
        this.isRegEx = isRegEx;
        this.isCaseSensitive = isCaseSensitive;
        this.attributes = attributes;
    }

    /**
     * @return {@code true} if child constraints are allowed, {@code false} 
     * otherwise
     */
    @Override
	public boolean getAllowsChildren() {
        return true;
    }

    /**
     * Sets attributes.
     *
     * @param h the attributes map
     */
    @Override
	public void setAttributes(Map<String, String> h) {
        attributes = h;
    }

    /**
     * @see mpi.search.content.query.model.Constraint#getAttributes()
     */
    @Override
	public Map<String, String> getAttributes() {
        return attributes;
    }

    /**
     * @param b the new value of the case sensitive flag
     */
    @Override
	public void setCaseSensitive(boolean b) {
        isCaseSensitive = b;
    }

    /**
     * @see mpi.search.content.query.model.Constraint#isCaseSensitive()
     */
    @Override
	public boolean isCaseSensitive() {
        return isCaseSensitive;
    }

    /**
     * @param i the index
     *
     * @return the constraint at the specified index
     */
    @Override
	public Constraint getChildAt(int i) {
        return children.get(i);
    }

    /**
     * @return the number of child constraints
     */
    @Override
	public int getChildCount() {
        return children.size();
    }

    /**
     * @return {@code true} if this constraint is editable
     */
    @Override
	public boolean isEditable() {
        return true;
    }

    /**
     * @return the id of this constraint 
     */
    @Override
	public String getId() {
        return (parent != null) ? (parent.getId() + "." +
        parent.getIndex(this)) : "C";
    }

    /**
     * @param node the node to get the index for
     *
     * @return the index of the node
     */
    @Override
	public int getIndex(TreeNode node) {
        return children.indexOf(node);
    }

    /**
     * @return {@code true} if this constraint has no child constraints
     */
    @Override
	public boolean isLeaf() {
        return (getChildCount() == 0);
    }

    /**
     * @param l the lower distance boundary
     */
    @Override
	public void setLowerBoundary(long l) {
        lowerBoundary = l;
    }

    /**
     * @see mpi.search.content.query.model.Constraint#getLowerBoundary()
     */
    @Override
	public long getLowerBoundary() {
        return lowerBoundary;
    }

    /**
     * @see mpi.search.content.query.model.Constraint#getLowerBoundaryAsString()
     */
    @Override
	public String getLowerBoundaryAsString() {
        return (lowerBoundary == Long.MIN_VALUE) ? "-X" : ("" + lowerBoundary);
    }

    /**
     * @see mpi.search.content.query.model.Constraint#getMode()
     */
    @Override
	public String getMode() {
        return mode;
    }

    /**
     * This method is here to satisfy the MutableTreeNode interface.
     * <p>
     * In practice all nodes in the tree are Constraints.
     * <p>
     * This method may still be called from the java libraries,
     * or with parent == null.
     * 
     * @param parent the parent tree node
     */
    @Override
	public void setParent(MutableTreeNode parent) {
        this.setParent((Constraint) parent);
    }

    @Override
	public void setParent(Constraint parent) {
        this.parent = parent;
    }

    /**
     * @return the parent
     */
    @Override
	public Constraint getParent() {
        return parent;
    }

    /**
     * @param s the search pattern 
     */
    @Override
	public void setPattern(String s) {
        patternString = s;
    }

    /**
     * @see mpi.search.content.query.model.Constraint#getPattern()
     */
    @Override
	public String getPattern() {
        return patternString;
    }

    /**
     * @return the quantifier like ("ANY" or "NONE") 
     */
    @Override
	public String getQuantifier() {
        return quantifier;
    }

    /**
     * @param b the regular expression flag
     */
    @Override
	public void setRegEx(boolean b) {
        isRegEx = b;
    }

    /**
     * @see mpi.search.content.query.model.Constraint#isRegEx()
     */
    @Override
	public boolean isRegEx() {
        return isRegEx;
    }

    /**
     * Sets the single tier name.
     * 
     * @param s the tier name
     */
    public void setTierName(String s) {
        tierNames = new String[] { s };
    }

    /**
     * for Corex compatibility
     *
     * @return first element of tierNames[]
     */
    @Override
	public String getTierName() {
        return (tierNames.length > 0) ? tierNames[0] : null;
    }

    /**
     * Sets the tier names 
     *
     * @param s an array of tier names
     */
    @Override
	public void setTierNames(String[] s) {
        tierNames = s;
    }

    /**
     * @return the tier names
     */
    @Override
	public String[] getTierNames() {
        return tierNames;
    }

    /**
     * @param s the unit string
     */
    @Override
	public void setUnit(String s) {
        unit = s;
    }

    /**
     * @see mpi.search.content.query.model.Constraint#getUnit()
     */
    @Override
	public String getUnit() {
        return unit;
    }

    /**
     * @param l the upper distance boundary
     */
    @Override
	public void setUpperBoundary(long l) {
        upperBoundary = l;
    }

    /**
     * @see mpi.search.content.query.model.Constraint#getUpperBoundary()
     */
    @Override
	public long getUpperBoundary() {
        return upperBoundary;
    }

    /**
     * @see mpi.search.content.query.model.Constraint#getUpperBoundaryAsString()
     */
    @Override
	public String getUpperBoundaryAsString() {
        return (upperBoundary == Long.MAX_VALUE) ? "+X" : ("" + upperBoundary);
    }

    /**
     * dummy function; DefaultTreeModel uses it; has further no implication
     *
     * @param object the user object
     */
    @Override
	public void setUserObject(Object object) {
    }

    /**
     * @see mpi.search.content.query.model.Constraint#addAttribute(String,
     *      String)
     */
    @Override
	public void addAttribute(String name, String value) {
        attributes.put(name, value);
    }

    /**
     * Implements javax.swing.tree.TreeNode.children().
     * Since this uses the old-style Enumeration, use the
     * collection-to-enumeration adapter.
     * <p>
     * 
     * {@inheritDoc}
     */
    @Override
	public Enumeration<Constraint> children() {
    	return Collections.enumeration(children);
    }

    /**
     * Overridden to make clone public.  Returns a shallow copy of this node;
     * the new node has no parent or children and has a reference to the same
     * user object, if any.
     *
     * @return a copy of this node
     */
    @Override
	public Object clone() {
        AbstractConstraint newConstraint = null;

        try {
            newConstraint = (AbstractConstraint) super.clone();

            newConstraint.setTierNames(getTierNames());
            newConstraint.setPattern(getPattern());
            newConstraint.setCaseSensitive(isCaseSensitive());
            newConstraint.setRegEx(isRegEx());
            newConstraint.setUnit(getUnit());
            newConstraint.setLowerBoundary(getLowerBoundary());
            newConstraint.setUpperBoundary(getUpperBoundary());
            newConstraint.setAttributes(getAttributes());
            newConstraint.children = new ArrayList<Constraint>();
            newConstraint.parent = null;
        } catch (CloneNotSupportedException e) {
        }

        return newConstraint;
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
	public boolean equals(Object object) {
    	if (object == this) {
    		return true;
    	}
    	
        if (!(object instanceof AbstractConstraint)) {
            return false;
        }

        AbstractConstraint constraint = (AbstractConstraint) object;

        if (constraint.isCaseSensitive() != isCaseSensitive()) {
            return false;
        }

        if (constraint.isRegEx() != isRegEx()) {
            return false;
        }

        if (!constraint.getPattern().equals(getPattern())) {
            return false;
        }

        if (constraint.getLowerBoundary() != getLowerBoundary()) {
            return false;
        }

        if (constraint.getUpperBoundary() != getUpperBoundary()) {
            return false;
        }

        if (((constraint.getUnit() == null) && (getUnit() != null)) ||
                ((constraint.getUnit() != null) &&
                !constraint.getUnit().equals(getUnit()))) {
            return false;
        }

        if (((constraint.getAttributes() == null) && (getAttributes() != null)) 
        		|| ((constraint.getAttributes() != null) &&
                !constraint.getAttributes().equals(getAttributes()))) {
            return false;
        }

        return true;
    }
    
    /**
     * Constraint objects are (possibly) shown and edited in a JTree. If any of the modifiable
     * fields of this class would be involved in the hash code calculation and would be changed
     * in the user interface, the constraint would no longer be retrievable from the tree's 
     * TreePath has table. It disappears from the tree visualisation.
     */
    @Override
	public int hashCode() {
		return super.hashCode();
	}

	/**
     * This method is here to satisfy the MutableTreeNode interface.
     * <p>
     * In practice all nodes in the tree are Constraints.
     * <p>
     * This method may still be called from the java libraries.
     *
     * @param child the node to insert
     * @param index the index where to insert
     */
    @Override // MutableTreeNode
	public void insert(MutableTreeNode child, int index) {
    	insert((Constraint)child, index);
    }

    @Override // Constraint
	public void insert(Constraint child, int index) {
   		children.add(index, child);    	
        child.setParent(this);
    }
	
    /**
     * Removes the constraint at the specified index
     *
     * @param index the index to remove
     */
    @Override
	public void remove(int index) {
        Constraint child = children.get(index);
        children.remove(index);
        child.setParent(null);
    }

    /**
     * @param node the node to remove
     */
    @Override
	public void remove(MutableTreeNode node) {
        children.remove(node);
        node.setParent(null);
    }

    /**
     * Removes this node from its parent.
     */
    @Override
	public void removeFromParent() {
        if (parent != null) {
            parent.remove(this);
        }
    }

    /**
     * only for debugging
     *
     * @return a parameter string
     */
    @Override
	public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Quantifier:\t" + quantifier + "\n");

        for (String tierName : tierNames) {
            sb.append("Tier name:\t" + tierName + "\n");
        }

        sb.append("Pattern:\t" + patternString + "\n");
        sb.append("Unit:\t" + unit + "\n");
        sb.append("Lower boundary:\t" + lowerBoundary + "\n");
        sb.append("Upper boundary:\t" + upperBoundary + "\n");

        return sb.toString();
    }
}
