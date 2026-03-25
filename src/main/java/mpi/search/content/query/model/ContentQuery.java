package mpi.search.content.query.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

import mpi.search.content.model.CorpusType;
import mpi.search.content.result.model.ContentResult;
import mpi.search.query.model.Query;
import mpi.search.result.model.Result;


/**
 * Annotation content query class.
 *
 * @author klasal
 */
public class ContentQuery extends Query {
    private final ContentResult result = new ContentResult();
    private AnchorConstraint anchorConstraint;
    private final CorpusType type;
    private final File[] files;

    /**
     * Creates a new ContentQuery object.
     *
     * @param rootConstraint the root or anchor constraint
     * @param type the corpus type
     */
    public ContentQuery(AnchorConstraint rootConstraint, CorpusType type) {
        this(rootConstraint, type, null);
    }

    /**
     * Creates a new Query instance
     *
     * @param rootConstraint the root or anchor constraint
     * @param type the corpus type
     * @param files the files to query
     */
    public ContentQuery(AnchorConstraint rootConstraint, CorpusType type,
        File[] files) {
        this.anchorConstraint = rootConstraint;
        this.type = type;
        this.files = files;
    }

    /**
     * Sets the anchor constraint.
     * 
     * @param rootConstraint the root or anchor constraint
     */
    public final void setAnchorConstraint(AnchorConstraint rootConstraint) {
        this.anchorConstraint = rootConstraint;
    }

    /**
     * Returns the anchor constraint.
     * 
     * @return the root or anchor constraint
     */
    public final AnchorConstraint getAnchorConstraint() {
        return anchorConstraint;
    }

    /**
     * Returns a list of all constraints.
     * 
     * @return a list containing the constraints
     */
    public final List<Constraint> getConstraints() {
        List<Constraint> constraintList = new ArrayList<Constraint>();
        addChildren(constraintList, anchorConstraint);

        return constraintList;
    }

    /**
     * Returns a list of files to query.
     * 
     * @return the files to query
     */
    public File[] getFiles() {
        return files;
    }

    /**
     * Returns whether the anchor constraint is a {@code RestrictedAnchorConstraint}.
     * 
     * @return {@code true} if the anchor constraint is a {@code RestrictedAnchorConstraint}
     */
    public final boolean isRestricted() {
        return anchorConstraint instanceof RestrictedAnchorConstraint;
    }

    /**
     *
     * @return the result of the query execution
     */
    @Override
	public Result getResult() {
        return result;
    }

    /**
     * Returns the corpus type.
     * 
     * @return the corpus type
     */
    public final CorpusType getType() {
        return type;
    }

    /**
     * Returns false, if there is one constraint with an empty search
     * expression.
     *
     * @return {@code true} if all constraints are properly specified
     */
    public final boolean isWellSpecified() {
        boolean wellSpecified = true;

        List<Constraint> constraintList = getConstraints();

        for (int i = 0; i < constraintList.size(); i++) {
            if (constraintList.get(i) != null) {
                if (constraintList.get(i).isRegEx() &&
                        constraintList.get(i).getPattern()
                             .equals("")) {
                    wellSpecified = false;
                }
            }
        }

        return wellSpecified;
    }

    /**
     * @param object the object to compare with
     *
     * @return {@code true} if the specified object is equal to this object,
     * {@code false} otherwise
     */
    @Override
	public boolean equals(Object object) {
        if (!(object instanceof ContentQuery)) {
            return false;
        }

        return getConstraints().equals(((ContentQuery) object).getConstraints());
    }

    /**
     * Only the constraints are checked in {@code equals()}, this is reflected
     * in has code calculation here.
     */
    @Override
	public int hashCode() {
		return Objects.hash(getConstraints());
	}

	/**
     * Translates the query to human readable text. (Debugging)
     *
     * @return the query as human readable text
     */
    @Override
	public String toString() {
        StringBuilder sb = new StringBuilder();
        List<Constraint> constraintList = getConstraints();

        for (int i = 0; i < constraintList.size(); i++) {
            sb.append(constraintList.get(i).toString());
        }

        return sb.toString();
    }

    private final void addChildren(List<Constraint> list, Constraint node) {
        list.add(node);

        for (Enumeration<Constraint> e = node.children(); e.hasMoreElements();) {
            addChildren(list, e.nextElement());
        }
    }
}
