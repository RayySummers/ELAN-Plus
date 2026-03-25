package mpi.eudico.server.corpora.clomimpl.type;

import java.util.Iterator;
import java.util.List;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;

/**
 * A constraint which determines that there can be multiple dependent 
 * annotations inside the interval of a parent annotation and that the
 * dependent annotations can be time aligned.
 * 
 * See also {@link NoTimeGapWithinParent}
 */
public class TimeSubdivision extends ConstraintImpl {
    /**
     * Creates a new TimeSubdivision instance
     */
    public TimeSubdivision() {
        super();

        addConstraint(new NoTimeGapWithinParent());
    }

    /**
     * @return the constant {@link Constraint#TIME_SUBDIVISION}
     */
    @Override
	public int getStereoType() {
        return Constraint.TIME_SUBDIVISION;
    }

    /**
     * Finds overlapping annotations on the specified tier and modifies begin and
     * or end time of the segment. The tier is the parent tier of a tier for which
     * the segment has to be checked and modified.
     *
     * @param segment a time segment, representing an intended dependent 
     * annotation 
     * @param forTier the tier the annotations are on
     */
    @Override
	public void forceTimes(long[] segment, TierImpl forTier) {
        //	System.out.println("TimeSubdivision.forceTimes called");	
        if (forTier != null) {
            Annotation annAtBegin = forTier.getAnnotationAtTime(segment[0]);
            Annotation annAtEnd = forTier.getAnnotationAtTime(segment[1]);

            if ((annAtBegin != null) && (annAtEnd == null)) {
                segment[1] = annAtBegin.getEndTimeBoundary();
            } else if ((annAtBegin == null) && (annAtEnd != null)) {
                segment[0] = annAtEnd.getBeginTimeBoundary();
            } else if ((annAtBegin != null) && (annAtEnd != null) &&
                    (annAtBegin != annAtEnd)) {
                segment[0] = annAtEnd.getBeginTimeBoundary();
            } else if ((annAtBegin == null) && (annAtEnd == null)) {
                // if annotations in between, constrain to first of them
            	List<Annotation> annotsInBetween = forTier.getOverlappingAnnotations(segment[0],
                        segment[1]);

                if (annotsInBetween.size() > 0) {
                    AlignableAnnotation a = (AlignableAnnotation) annotsInBetween.get(0);
                    segment[0] = a.getBegin().getTime();
                    segment[1] = a.getEnd().getTime();
                } else {
                    segment[0] = segment[1];
                }
            }
        }
    }

    /**
     * Forces all annotations on the specified tier inside the parent
     * annotation on the parent tier.
     * 
     * @param theTier the tier to check 
     */
    @Override
    public void enforceOnWholeTier(TierImpl theTier) {
        System.out.println("enforcing on tier: " + theTier.getName());

        // force times (later to "IncludedIn" constraint?)

        for (AlignableAnnotation ann : theTier.getAlignableAnnotations()) {

            //	long[] segment = {ann.getBegin().getTime(), ann.getEnd().getTime()};
            long[] segment = {
                ann.getBeginTimeBoundary(), ann.getEndTimeBoundary()
            };

            TierImpl parent = theTier.getParentTier();
            forceTimes(segment, parent);

            //	if (segment[0] == segment[1]) {
            //			((TierImpl) theTier).removeAnnotation(ann);
            //	}
            //	else {
            ann.getBegin().setTime(segment[0]);
            ann.getEnd().setTime(segment[1]);

            //	}						
        }

        // no gaps within parent segment, pass to NoTimeGapWithinParent constraint
        Iterator cIter = nestedConstraints.iterator();

        while (cIter.hasNext()) {
            ((Constraint) cIter.next()).enforceOnWholeTier(theTier);
        }
    }

    /**
     * @return {@code true}
     */
    @Override
	public boolean supportsInsertion() {
        return true;
    }

    /**
     * Assumes there is a nested constraint of type {@link NoTimeGapWithinParent}
     * (at index 0) and delegates the work to the {@code insertBefore(Annotation, Tier)}
     * method of that object. 
     *
     * @param beforeAnn the annotation to insert before
     * @param theTier the tier the annotation is on
     *
     * @return a new annotation or null
     */
    @Override
    public Annotation insertBefore(Annotation beforeAnn, TierImpl theTier) {
        // assumption: first added Constraint is a NoTimeGapWithinParent
        return ((NoTimeGapWithinParent) nestedConstraints.get(0)).insertBefore(beforeAnn,
            theTier);
    }

    /**
     * Assumes there is a nested constraint of type {@link NoTimeGapWithinParent}
     * (at index 0) and delegates the work to the {@code insertAfter(Annotation, Tier)}
     * method of that object.
     *
     * @param afterAnn the annotation to insert after
     * @param theTier the tier the annotation is on
     *
     * @return a new annotation or null
     */
    @Override
    public Annotation insertAfter(Annotation afterAnn, TierImpl theTier) {
        // assumption: first added Constraint is a NoTimeGapWithinParent
        return ((NoTimeGapWithinParent) nestedConstraints.get(0)).insertAfter(afterAnn,
            theTier);
    }

    /**
     * Assumes there is a nested constraint of type {@link NoTimeGapWithinParent}
     * (at index 0) and delegates the work to the {@code detachAnnotation(Annotation, TierImpl)}
     * method of that object.
     *
     * @param theAnn the annotation to detach
     * @param theTier the tier the annotation is on
     */
    @Override
    public void detachAnnotation(Annotation theAnn, TierImpl theTier) {
        // assumption: first added Constraint is a NoTimeGapWithinParent
        ((NoTimeGapWithinParent) nestedConstraints.get(0)).detachAnnotation(theAnn,
            theTier);
    }
}
