package mpi.eudico.server.corpora.clomimpl.abstr;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;


/**
 * A class for non-time-alignable reference annotations. This type of annotation
 * is required to have a parent annotation, the tier this type of annotation is 
 * part of, always should have a parent tier.
 * 
 * @author MK (02/06/24)
 */
public class RefAnnotation extends AbstractAnnotation {
    private List<Annotation> references; // should contain minimally 1 reference
    private RefAnnotation next; // for chains of RefAnnots that have same parent
    private RefAnnotation previous; // for chains of RefAnnots that have same parent

    /**
     * <p>
     * MK:02/06/24<br> A RefAnnotation refers to at least one "parent
     * Annotation", called "theReference".  The current implementation allows
     * the "parent Annotation" to be null, but the parent has to set before 
     * this annotation can be used reliably. During loading of a file the 
     * dependent annotation might be created before the parent annotation,
     * the reference to the parent is then set later, in a second step.<br>
     * The returned RefAnnotation has no value, it has to be set with
     * setValue().
     * </p>
     *
     * @param theReference the parent Annotation. Can be null when created in the
     *        process of loading a file and the parent isn't known yet 
     * 
     * @param theTier tier of the returned RefAnnotation.
     */
    public RefAnnotation(Annotation theReference, Tier theTier) {
        super(); // creates a parent listeners list

        references = new ArrayList<Annotation>();

        if (theReference != null) {
            addReference(theReference);
        }

        this.setTier(theTier);
    }

    /**
     * {@code RefAnnotation}s are not time alignable so the returned time value
     * here is either the inherited begin time of the parent (or higher ancestor)
     * or a virtual, interpolated time value based on its position in a sequence
     * of {@code RefAnnotation}s.
     *
     * @return the begin time value in milliseconds  
     */
    @Override
	public long getBeginTimeBoundary() {
        long beginTimeBoundary = Long.MAX_VALUE;
        Constraint c = ((TierImpl) getTier()).getLinguisticType()
                        .getConstraints();

        if (c != null) {
            beginTimeBoundary = c.getBeginTimeForRefAnnotation(this);
        } else {// the tier a RefAnnotation is part of should always have a Constraint object
            long beginB = 0;

            Iterator<Annotation> refIter = references.iterator();

            while (refIter.hasNext()) {
                beginB = refIter.next().getBeginTimeBoundary();

                beginTimeBoundary = Math.min(beginTimeBoundary, beginB);
            }
        }

        return beginTimeBoundary;
    }

    /**
     * {@code RefAnnotation}s are not time alignable so the returned time value
     * here is either the inherited end time of the parent (or higher ancestor)
     * or a virtual, interpolated time value based on its position in a sequence
     * of {@code RefAnnotation}s.
     *
     * @return the end time value in milliseconds  
     */
    @Override
	public long getEndTimeBoundary() {
        long endTimeBoundary = 0;

        Constraint c = ((TierImpl) getTier()).getLinguisticType()
                        .getConstraints();

        if (c != null) {
            endTimeBoundary = c.getEndTimeForRefAnnotation(this);
        } else {
            long endB = 0;

            Iterator<Annotation> refIter = references.iterator();

            while (refIter.hasNext()) {
                endB = refIter.next().getEndTimeBoundary();

                endTimeBoundary = Math.max(endTimeBoundary, endB);
            }
        }

        return endTimeBoundary;
    }

    /**
     * Traverses the annotation hierarchy tree up until a time-alignable 
     * ancestor is encountered.
     *
     * @return the closest time-alignable ancestor annotation
     */
    public AlignableAnnotation getFirstAlignableRoot() {
        Annotation parent = references.get(0);

        if (parent instanceof AlignableAnnotation) {
            return (AlignableAnnotation) parent;
        } else {
            return ((RefAnnotation) parent).getFirstAlignableRoot();
        }
    }

    /**
     * For some, probably historic, reason, the parent is set through this 
     * method which adds it to a list of references (parents?).
     * <p>
     * No checks are performed, any passed Annotation is added to the list
     * and this annotation is registered as parent listener of all of them.
     * 
     * @param theReference the parent Annotation of this Annotation, not null
     */
    public void addReference(Annotation theReference) {
        //System.out.println("add ref: " + theReference.getValue() + " to: " + getValue());
        references.add(theReference);

        // register as listener with reference
        theReference.addParentAnnotationListener(this);
    }

    /**
     * A request to remove the parent from the list of references or parents
     * and to unregister as parent listener. If the list of references is empty
     * this annotation is marked for deletion.
     *
     * @param theReference the parent annotation of this annotation
     */
    public void removeReference(Annotation theReference) {
        //System.out.println("remove ref: " + theReference.getValue() + " from: " + getValue());
        // unregister as listener with reference
        theReference.removeParentAnnotationListener(this);

        references.remove(theReference);

        if (references.size() == 0) { // not referring to any annotation anymore
            markDeleted(true);
        }
    }

    /**
     * <p>
     * MK:02/06/24<br> The returned List contains {@link Annotation} objects,
     * which are the parent Annotation(s) of 'this' RefAnnotation.
     * It is a runtime error if 'this' RefAnnotation has no "parent
     * Annotation".
     * </p>
     *
     * @return a list of "parent Annotation"(s?) of 'this' RefAnnotation.
     */
    public List<Annotation> getReferences() {
        return references;
    }

    /**
     * {@code RefAnnotation}s can be part of a sequence and/or network of 
     * annotations. This method returns the "next" annotation, the annotation
     * to "the right" of this annotation, if any.
     *
     * @return the {@code next} annotation in the sequence or null
     */
    public RefAnnotation getNext() {
        return next;
    }

    /**
     * Sets the annotation "next" to this one, i.e. next in the graph ("to the 
     * right"). Registers this one as "previous"
     *
     * @param a the new {@code next} annotation
     */
    public void setNext(RefAnnotation a) {
        //	System.out.println(getValue() + " has as next: " + a.getValue());
        next = a;

        if (a != null) {
            a.setPrevious(this);
        }
    }

    /**
     * Returns whether this annotation has a {@code next} annotation.
     * 
     * @return true if the {@code next} annotation is not null, false otherwise
     */
    public boolean hasNext() {
        if (next != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns the {@code previous} annotation.
     * 
     * @return the {@code previous} annotation or null
     */
    public RefAnnotation getPrevious() {
        return previous;
    }

    /**
     * Calling {@link #setNext(RefAnnotation)} should be preferred over this
     * method, it takes care of setting the {@code previous} member of the 
     * passed annotation.
     * This method can be used to set {@code previous} to null (e.g. as part of
     * an edit action). 
     * 
     * @see #setNext(RefAnnotation)
     * @param a the previous annotation or null
     */
    public void setPrevious(RefAnnotation a) {
        previous = a;
    }

    /**
     * Returns whether this annotation has a {@code previous} annotation.
     * 
     * @return true if this annotation has a {@code previous} annotation, false
     * otherwise
     */
    public boolean hasPrevious() {
        if (previous != null) {
            return true;
        } else {
            return false;
        }
    }

    // ParentAnnotationListener implementation
    /**
     * If the source of the event has been marked deleted, it is removed from the 
     * list of references and this annotation is unregistered as parent listener.
     * 
     * @param e the {@link EventObject} containing the source of the event
     */
    @Override
	public void parentAnnotationChanged(EventObject e) {
        if (e.getSource() instanceof Annotation) {
            if (((Annotation) e.getSource()).isMarkedDeleted()) {
                removeReference(((Annotation) e.getSource()));
            }
        }
    }

    /**
     * Checks if this RefAnnotation has a parent Annotation.
     * This should always return true unless maybe in the process of loading a 
     * document and still constructing the network of annotations or of 
     * editing/removing of annotations.
     * 
     * @return true if this annotation has a parent annotation,
     * false otherwise 
     */
    @Override
	public boolean hasParentAnnotation() {
        boolean hasParent = false;

        if (references.size() > 0) {
            hasParent = true;
        }

        return hasParent;
    }

    /**
     * Parent-child relationship for RefAnnotations is defined by an explicit
     * reference.
     *
     * @return the parent annotation or null (in specific situations)
     */
    @Override
	public Annotation getParentAnnotation() {
        Annotation p = null;

        if (hasParentAnnotation()) {
            p = references.get(0);
        }

        return p;
    }
}
