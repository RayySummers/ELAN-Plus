package mpi.eudico.server.corpora.clomimpl.type;

import java.util.Iterator;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clomimpl.abstr.RefAnnotation;


/**
 * A Constraint which allows multiple references from a single annotation.
 * 
 * Note: this is not actually used (yet).
 */
public class MultipleRefs extends ConstraintImpl {
    /**
     * Creates a new MultipleRefs instance
     */
    public MultipleRefs() {
        super();
    }

    /**
     * @return the constant {@link Constraint#MULTIPLE_REFS}
     */
    @Override
	public int getStereoType() {
        return Constraint.MULTIPLE_REFS;
    }

    /**
     * @param theAnnot the {@code RefAnnotation} to retrieve a time for
     *
     * @return the calculated or retrieved begin time (which is the minimum 
     * of the begin times of all referenced annotations)
     */
    @Override
	public long getBeginTimeForRefAnnotation(RefAnnotation theAnnot) {
        long beginTimeBoundary = Long.MAX_VALUE;
        long beginB = 0;

        Iterator<Annotation> refIter = theAnnot.getReferences().iterator();

        while (refIter.hasNext()) {
            beginB = refIter.next().getBeginTimeBoundary();

            beginTimeBoundary = Math.min(beginTimeBoundary, beginB);
        }

        return beginTimeBoundary;
    }

    /**
     * @param theAnnot the {@code RefAnnotation} to retrieve a time for
     *
     * @return the calculated or retrieved end time (which is the maximum of
     * the end times of all referenced annotations)
     */
    @Override
	public long getEndTimeForRefAnnotation(RefAnnotation theAnnot) {
        long endTimeBoundary = 0;
        long endB = 0;

        Iterator<Annotation> refIter = theAnnot.getReferences().iterator();

        while (refIter.hasNext()) {
            endB = refIter.next().getEndTimeBoundary();

            endTimeBoundary = Math.max(endTimeBoundary, endB);
        }

        return endTimeBoundary;
    }
}
