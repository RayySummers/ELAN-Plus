package mpi.eudico.server.corpora.clomimpl.type;

import java.util.List;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clomimpl.abstr.RefAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;


/**
 * A constraint which determines that there can be a sequence of dependent
 * annotations for a parent annotation. The dependent annotations are not
 * time aligned but are ordered. They form a chain or a graph from the parent's
 * begin point to its end point.  
 */
public class SymbolicSubdivision extends ConstraintImpl {
    /**
     * Creates a new SymbolicSubdivision instance
     */
    public SymbolicSubdivision() {
        super();
    }

    /**
     * @return the constant {@link Constraint#SYMBOLIC_SUBDIVISION}
     */
    @Override
	public int getStereoType() {
        return Constraint.SYMBOLIC_SUBDIVISION;
    }

    /**
     * Not relevant for this constraint.
     */
    @Override
	public void forceTimes(long[] segment, TierImpl forTier) {
        //		if (forTier != null) {
        //			segment[1] = segment[0];
        //		}
    }

    /**
     * @param theAnnot the {@code RefAnnotation} to retrieve a time for
     *
     * @return follows the chain left and right, calculates a segment duration
     * based on the parents' begin and end time and then calculates a time 
     * based on the number of annotations in the chain.  
     */
    @Override
	public long getBeginTimeForRefAnnotation(RefAnnotation theAnnot) {
        long[] segment = { 0, 0 };
        int[] elmtsLeftAndRight = { 0, 0 };

        getSegmentForChainOf(theAnnot, segment, elmtsLeftAndRight);

        long duration = segment[1] - segment[0];
        double durationPerAnnot = (double) duration / (double) (elmtsLeftAndRight[0] +
            elmtsLeftAndRight[1] + 1);

        return (segment[0] + (long) (elmtsLeftAndRight[0] * durationPerAnnot));
    }

    /**
     * @param theAnnot the {@code RefAnnotation} to retrieve a time for
     *
     * @return follows the chain left and right, calculates a segment duration
     * based on the parents' begin and end time and then calculates a time 
     * based on the number of annotations in the chain.
     */
    @Override
	public long getEndTimeForRefAnnotation(RefAnnotation theAnnot) {
        long[] segment = { 0, 0 };
        int[] elmtsLeftAndRight = { 0, 0 };

        getSegmentForChainOf(theAnnot, segment, elmtsLeftAndRight);

        long duration = segment[1] - segment[0];
        double durationPerAnnot = (double) duration / (double) (elmtsLeftAndRight[0] +
            elmtsLeftAndRight[1] + 1);

        return (segment[0] +
        (long) ((elmtsLeftAndRight[0] + 1) * durationPerAnnot));
    }

    /**
     * @return {@code true}
     */
    @Override
	public boolean supportsInsertion() {
        return true;
    }

    /**
     * Creates a new annotation and inserts it in the chain before the
     * specified annotation.
     *
     * @param beforeAnn the annotation (a {@link RefAnnotation}) to insert before
     * @param theTier the tier the annotations are on
     *
     * @return the new annotation
     */
    @Override
	public Annotation insertBefore(Annotation beforeAnn, TierImpl theTier) {
        Annotation parentAnn = ((RefAnnotation) beforeAnn).getReferences()
                                             .get(0);
        RefAnnotation newAnn = new RefAnnotation(parentAnn, theTier);

        if (((RefAnnotation) beforeAnn).hasPrevious()) {
            RefAnnotation prevAnn = ((RefAnnotation) beforeAnn).getPrevious();

            prevAnn.setNext(newAnn);
        }

        newAnn.setNext((RefAnnotation) beforeAnn);

        theTier.addAnnotation(newAnn);

        return newAnn;
    }

    /**
     * Creates a new annotation and inserts it in the chain after the
     * specified annotation.
     *
     * @param afterAnn the annotation (a {@link RefAnnotation}) to insert after
     * @param theTier the tier the annotations are on
     *
     * @return the new annotation
     */
    @Override
	public Annotation insertAfter(Annotation afterAnn, TierImpl theTier) {
        final RefAnnotation afterRefAnn = (RefAnnotation) afterAnn;
		Annotation parentAnn = afterRefAnn.getReferences().get(0);
        RefAnnotation newAnn = new RefAnnotation(parentAnn, theTier);

        //MK:02/06/24 insert into "next" chain 
        if (afterRefAnn.hasNext()) {
            RefAnnotation nextAnn = afterRefAnn.getNext();

            newAnn.setNext(nextAnn);
        }

        afterRefAnn.setNext(newAnn);

        theTier.addAnnotation(newAnn);

        return newAnn;
    }

    private void getSegmentForChainOf(RefAnnotation theAnnot, long[] segment,
        int[] elmtsLeftAndRight) {
        RefAnnotation firstOfChain = getFirstOfChain(theAnnot, elmtsLeftAndRight);
        RefAnnotation lastOfChain = getLastOfChain(theAnnot, elmtsLeftAndRight);

        List<Annotation> refsOfFirst = firstOfChain.getReferences();

        if (refsOfFirst.size() > 0) {
            Annotation beginRef = refsOfFirst.get(0);
            segment[0] = beginRef.getBeginTimeBoundary();
        }

        List<Annotation> refsOfLast = lastOfChain.getReferences();

        if (refsOfLast.size() > 0) {
            Annotation endRef = lastOfChain.getReferences().get(0);
            segment[1] = endRef.getEndTimeBoundary();
        }
    }

    private RefAnnotation getFirstOfChain(RefAnnotation theAnnot,
        int[] elmtsLeftAndRight) {
        RefAnnotation first = theAnnot;

        int leftElementCount = 0;

        while (first.hasPrevious()) {
            first = first.getPrevious();
            leftElementCount++;
        }

        elmtsLeftAndRight[0] = leftElementCount;

        return first;
    }

    private RefAnnotation getLastOfChain(RefAnnotation theAnnot,
        int[] elmtsLeftAndRight) {
        RefAnnotation last = theAnnot;

        int rightElementCount = 0;

        while (last.hasNext()) {
            last = last.getNext();
            rightElementCount++;
        }

        elmtsLeftAndRight[1] = rightElementCount;

        return last;
    }

    /**
     * Detach annotation theAnn from tier theTier by reconnecting remaining
     * Annotations on the tier. Assumes that all references and
     * ParentAnnotationListener registrations are already cleaned up.
     *
     * @param theAnn the annotation to detach
     * @param theTier ignored in the implementation 
     */
    @Override
	public void detachAnnotation(Annotation theAnn, TierImpl theTier) {
        RefAnnotation a = (RefAnnotation) theAnn; // cast is safe for case of SymbolicSubdivision

        RefAnnotation prev = a.getPrevious();
        RefAnnotation next = a.getNext();

        // reconnect
        if (prev != null) {
            prev.setNext(next);
        } else if (next != null) {
            next.setPrevious(null);
        }
    }
}
