package mpi.eudico.server.corpora.clomimpl.type;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clomimpl.abstr.RefAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;


/**
 * A constraint which determines that there can be zero or one dependent 
 * annotation (per dependent tier) associated with a parent annotation.
 * This creates a one-to-one relation e.g. for tagging.
 */
public class SymbolicAssociation extends ConstraintImpl {
    /**
     * Creates a new SymbolicAssociation instance
     */
    public SymbolicAssociation() {
        super();
    }

    /**
     * @return the constant {@link Constraint#SYMBOLIC_ASSOCIATION}
     */
    @Override
	public int getStereoType() {
        return Constraint.SYMBOLIC_ASSOCIATION;
    }

    /**
     * Not relevant for this constraint.
     *
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
     * @return the begin time of the first of the reference annotations, 
     * the parent annotation
     */
    @Override
	public long getBeginTimeForRefAnnotation(RefAnnotation theAnnot) {
        //	System.out.println("begin for ref annot: " + theAnnot.getValue() + " on tier: " + theAnnot.getTier().getName());

        long beginTB = 0;

        if (theAnnot.getReferences().size() > 0) {
            Annotation ref = theAnnot.getReferences().get(0);
            beginTB = ref.getBeginTimeBoundary();
        }

        return beginTB;
    }

    /**
     * @param theAnnot the {@code RefAnnotation} to retrieve a time for
     *
     * @return the end time of the first of the reference annotations,
     * the parent annotation
     */
    @Override
	public long getEndTimeForRefAnnotation(RefAnnotation theAnnot) {
        long endTB = 0;

        if (theAnnot.getReferences().size() > 0) {
            Annotation ref = theAnnot.getReferences().get(0);
            endTB = ref.getEndTimeBoundary();
        }

        return endTB;
    }
}
