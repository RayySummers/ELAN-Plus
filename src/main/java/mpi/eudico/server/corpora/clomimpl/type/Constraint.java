package mpi.eudico.server.corpora.clomimpl.type;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.TimeSlot;
import mpi.eudico.server.corpora.clomimpl.abstr.RefAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;

import java.sql.Time;
import java.util.List;


/**
 * This interface defines constraints between tiers and (therefore) between
 * annotations on tiers.
 */
public interface Constraint extends Cloneable {
    /** Constant for the Time Subdivision constraint, which determines that 
     * annotations on a dependent tier are inside the boundaries of the parent
     * annotation and are time alignable. */
    public static final int TIME_SUBDIVISION = 0;

    /** Constant for the Included In constraint, which determines that 
     * annotations on a dependent tier are inside the boundaries of the parent
     * annotation, are time alignable and gaps are allowed between the 
     * dependent annotations. */
    public static final int INCLUDED_IN = 1;

    /** Constant for a constraint which determines that annotations on a 
     * dependent tier are inside the boundaries of the parent and gaps are not
     * allowed. I.e. the interval of the parent is fully covered by annotations
     * on the dependent tier. */
    public static final int NO_GAP_WITHIN_PARENT = 2;

    /** Constant for the Symbolic Subdivision constraint, which determines that 
     * annotations on a dependent tier are inside the boundaries of the parent
     * annotation and are not time alignable. */
    public static final int SYMBOLIC_SUBDIVISION = 3;

    /** Constant for the Symbolic Association constraint, which determines that 
     * there can be 0 or 1 annotation on the dependent tier for a parent
     * annotation. The dependent annotation is not time alignable. */
    public static final int SYMBOLIC_ASSOCIATION = 4;

    /** Constant for the Multiple References constraint, which determines that
     * an annotation can have multiple references to other annotations. */
    public static final int MULTIPLE_REFS = 5;

    /** A String array containing textual versions of the constraint constants */
    public static final String[] stereoTypes = {
        "Time Subdivision", "Included In", "No Gap Within Parent",
        "Symbolic Subdivision", "Symbolic Association", "Multiple References"
    };

    /** A String array containing textual versions of those constraint 
     * constants that are to be shown to a user in a user interface and stored 
     * in annotation documents. */
    public static final String[] publicStereoTypes = {
        "Time Subdivision", "Included In", "Symbolic Subdivision", "Symbolic Association"
    };

    /**
     * Depending on the constraint type, annotations are forced inside
     * the begin and end time of the segment or are deleted.
     *
     * @param segment an array of size 2, the begin and end time of a segment
     * @param forTier the tier instance to apply the constraint to 
     */
    public void forceTimes(long[] segment, TierImpl forTier);

    /**
     * Calculates or retrieves the begin time for a reference annotation.
     *
     * @param theAnnot the {@code RefAnnotation} to get the begin for
     *
     * @return the begin time as a {@code long} value
     */
    public long getBeginTimeForRefAnnotation(RefAnnotation theAnnot);

    /**
     * Calculates or retrieves the end time for a reference annotation.
     *
     * @param theAnnot the {@code RefAnnotation} to get the end for
     *
     * @return the end time as a {@code long} value
     */
    public long getEndTimeForRefAnnotation(RefAnnotation theAnnot);

    /**
     * Finds or creates {@link TimeSlot}s for a new annotation (if applicable
     * for the constraint).
     *
     * @param begin the requested begin time
     * @param end the requested end time
     * @param forTier the target tier for the new annotation
     *
     * @return a list of {@code TimeSlot}s, usually size 2
     */
    public List<TimeSlot> getTimeSlotsForNewAnnotation(long begin, long end,
        TierImpl forTier);

    /**
     * Enforces constraints on an entire tier.
     *
     * @param theTier the target tier
     */
    public void enforceOnWholeTier(TierImpl theTier);

    /**
     * Indicates if a constraint supports insertion of an annotation on a tier 
     * relative to an existing annotation on that tier.
     * insertBefore()
     * and insertAfter().
     * @return true if annotations can be added via @{@code #insertBefore(Annotation, TierImpl)}
     * and {@code #insertAfter(Annotation, TierImpl)}
     * 
     * @see #insertAfter(Annotation, TierImpl)
     * @see #insertBefore(Annotation, TierImpl)
     */
    public boolean supportsInsertion();

    /**
     * Inserts an annotation before the specified annotation if the 
     * constraint supports this.
     *
     * @param beforeAnn the reference annotation
     * @param theTier the target tier
     *
     * @return a new annotation positioned before the reference annotation
     * or {@code null}
     */
    public Annotation insertBefore(Annotation beforeAnn, TierImpl theTier);

    /**
     * Inserts an annotation after the specified annotation if the 
     * constraint supports this.
     *
     * @param afterAnn the reference annotation
     * @param theTier the target tier
     *
     * @return a new annotation positioned after the reference annotation
     * or {@code null}
     */
    public Annotation insertAfter(Annotation afterAnn, TierImpl theTier);

    /**
     * Detaches annotation theAnn from tier theTier making sure that remaining
     * annotations on tier still meet the Constraint. Assumes that all
     * references and ParentAnnotationListener registrations are already
     * cleaned up.
     * 
     * @param theAnn the annotation to detach
     * @param theTier the source tier
     */
    public void detachAnnotation(Annotation theAnn, TierImpl theTier);

    /**
     * Returns the stereotype of a {@code Constraint} implementation,
     * one of the constants from {@link #TIME_SUBDIVISION} to 
     * {@link #MULTIPLE_REFS} (but more likely one corresponding to the 
     * public String stereotypes.
     * 
     * @return the stereotype of the constraint
     */
    public int getStereoType();

    // constraints are nested
    /**
     * Adds a {@code Constraint} to this constraint. This allows for nested or 
     * accumulated constraints.
     * 
     * @param theConstraint the {@code Constraint} to add to this one
     */
    public void addConstraint(Constraint theConstraint);
    
    /**
     * {@code Cloneable} method
     * @return a deep copy of this {@code Constraint} instance
     * @throws CloneNotSupportedException thrown if an implementor does not
     * support cloning, should not happen
     */
    public Constraint clone() throws CloneNotSupportedException;
}
