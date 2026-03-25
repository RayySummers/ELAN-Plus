package mpi.eudico.server.corpora.clomimpl.abstr;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.ExternalReference;
import mpi.eudico.server.corpora.clom.Property;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.event.ACMEditEvent;
import mpi.eudico.server.corpora.event.ParentAnnotationListener;
import mpi.eudico.server.corpora.util.ACMEditableObject;


/**
 * An abstract class for an annotation, the base class for time-aligned
 * and reference annotations.
 */
public abstract class AbstractAnnotation implements Annotation {
    private Tier tier;
    private String value = "";
    private String id = null;
    private boolean markedDeleted = false;
    private List<Annotation> parentAnnotListeners; // see addParentAnnotationListener()
    /** field used to, potentially, store different types of external references from an annotation
     * e.g. a reference to a (ISO DCR) Data Category 
     * */
    private ExternalReference extRef;
	/**
	 *  field used to refer to the id of an entry in a Controlled Vocabulary
	 */
	private String cvEntryId;

    /**
     * Creates a new AbstractAnnotation instance
     */
    public AbstractAnnotation() {
        parentAnnotListeners = new ArrayList<Annotation>();
    }

    /**
     *
     * @return the time value of the begin of this annotation
     */
    @Override
	public abstract long getBeginTimeBoundary();

    /**
     *
     * @return the time value of the end of this annotation
     */
    @Override
	public abstract long getEndTimeBoundary();

    /**
     *
     * @return the text value of this annotation, can be empty
     */
    @Override
	public String getValue() {
        return value;
    }

    /**
     * Sets the value of this annotation. Even if the value is a number, it is 
     * always set and stored as a sting.
     *
     * @param theValue the new text value of this annotation
     */
    @Override
	public void setValue(String theValue) {
        value = theValue;

        modified(ACMEditEvent.CHANGE_ANNOTATION_VALUE, null);
    }

    /**
     * Returns the annotation id.
     * If it does not have one yet, it is invented. By default the id has the 
     * form of an "a" followed by a positive integer, e.g. "a1".
     * 
     * @return the annotation id
     */
    @Override
	public String getId() {
    	if (id == null || id.isEmpty()) {
    		TierImpl ti = (TierImpl)tier;
    		TranscriptionImpl tr = ti.getTranscription();
    		// This Property's value is an Integer.
    		Property p = tr.getDocProperty("lastUsedAnnotationId");
    		Integer lastUsedAnnId = 0;
			if (p == null) {
				p = new PropertyImpl("lastUsedAnnotationId", null);
				tr.addDocProperty(p);
			} else if (p.getValue() != null) {
				try {
					lastUsedAnnId = (Integer)p.getValue();
				} catch (ClassCastException nfe) {
					System.out.println("Could not retrieve the last used annotation id.");
				}
			}
			id = "a" + ++lastUsedAnnId;
			p.setValue(lastUsedAnnId);
    	}
    		
    	return id;
    }

    /**
     * Returns the annotation id.
     * If it does not have one yet, null is returned.
     * 
     * @return the annotation id, or null
     */
	public String getIdLazily() {
		return id;
	}

	/**
     * Sets the annotation id.
     * 
     * @param s the annotation id
     */
    @Override
	public void setId(String s){
    	id = s;
    }
    
    /**
     * Returns the external reference object. This can be a compound object containing 
     * multiple reference objects.
     * 
     * @return the external reference object
     */
    public ExternalReference getExtRef() {
    	return extRef;
    }
    
    /**
     * Sets the external reference object. This can be a compound object containing multiple 
     * external references.
     * 
     * @param extRef the external reference object
     */
    public void setExtRef(ExternalReference extRef) {
    	this.extRef = extRef;
    	// is this event necessary?
    	modified(ACMEditEvent.CHANGE_ANNOTATION_EXTERNAL_REFERENCE, extRef);// or null as the modification?
    }
    
	/**
	 * @return the id of an entry in a Controlled Vocabulary, or null if the 
	 * annotation is not linked to an entry
	 */
	@Override
	public String getCVEntryId() {
		return cvEntryId;
	}

	/**
	 * @param cVEntryId the id of an entry in a Controlled Vocabulary this 
	 * annotation is linked to
	 */
	@Override
	public void setCVEntryId(String cVEntryId) {
		cvEntryId = cVEntryId;
	}
    
	/**
	 * Returns the value of the external reference of a type.
	 * Added by: Micha Hulsbosch
	 * 
	 * @param type the type of reference, one of the constants in {@link ExternalReference}
	 * @return the value of an external reference or null
	 */
	public String getExtRefValue(int type) {
		if (extRef != null) {
			String value = null; 
			ExternalReference ef = extRef;
			ArrayList<ExternalReference> extRefList = new ArrayList<ExternalReference>();
			extRefList.add(ef);
			int i = 0;
			while (i < extRefList.size()) {
				if (extRefList.get(i).getReferenceType() == type) {
					value = extRefList.get(i).getValue();
					break;
				} else if (extRefList.get(i).getReferenceType() == ExternalReference.REFERENCE_GROUP) {
					extRefList.addAll(((ExternalReferenceGroup) extRefList.get(i)).getAllReferences());
				}
				i++;
			}
			return value;
		}
		return null;
	}

	/**
	 * Removes a certain ExternalReference
	 * @param er the external reference to remove
	 */
	public void removeExtRef(ExternalReferenceImpl er) {
		if (extRef == null) {
			// No need to do anything
		} else if (extRef.equals(er)) {
			extRef = null;
		} else if (extRef instanceof ExternalReferenceGroup) {
			removeExtRefFromGroup((ExternalReferenceGroup) extRef, er);
		}
		modified(ACMEditEvent.CHANGE_ANNOTATION_EXTERNAL_REFERENCE, extRef);
	}
	
	/**
	 * Removes an external reference from a group of references.
	 * 
	 * @param extRefGrp the group to remove it from
	 * @param er the external reference to remove
	 */
	private void removeExtRefFromGroup(ExternalReferenceGroup extRefGrp, ExternalReference er) {
		List<ExternalReference> extRefList = extRefGrp.getAllReferences();
		for(int i = 0; i < extRefList.size();  i++) {
			final ExternalReference externalReference = extRefList.get(i);
			if (externalReference instanceof ExternalReferenceGroup) {
				removeExtRefFromGroup((ExternalReferenceGroup) externalReference, er);
			} else if (externalReference != null
					&& externalReference.equals(er)) {
				extRefGrp.removeReference(er);
			}
		}
	}

	/**
	 * Adds a certain ExternalReference 
	 * @param er the external reference to add
	 */
	public void addExtRef(ExternalReference er) {
		if (extRef == null) {
			extRef = er;
		} else if (extRef instanceof ExternalReferenceGroup) {
				((ExternalReferenceGroup) extRef).addReference(er);
		} else {
			extRef = ExternalReferenceGroup.create(extRef, er);
		}
		modified(ACMEditEvent.CHANGE_ANNOTATION_EXTERNAL_REFERENCE, extRef);
	}

	/**
	 * Returns a list containing the external references
	 * 
	 * @return a list containing the external references (not external reference
	 * groups!)
	 */
	public List<ExternalReference> getExtRefs() {
		List<ExternalReference> extRefs = new ArrayList<ExternalReference>();

		if (extRef != null) {
			if (extRef instanceof ExternalReferenceGroup) {
				addExtRefToList((ExternalReferenceGroup) extRef, extRefs);
			} else {
				extRefs.add(extRef);
			}
		}

		return extRefs;
	}

	/**
	 * Adds all references of a group to a list 
	 * @param extRefGrp the group to take from
	 * @param extRefs the list to add to
	 */
	private void addExtRefToList(ExternalReferenceGroup extRefGrp,
			List<ExternalReference> extRefs) {
		for (ExternalReference er : extRefGrp.getAllReferences()) {
			if (er != null) {
				if (er instanceof ExternalReferenceGroup) {
					addExtRefToList((ExternalReferenceGroup) er, extRefs);
				} else {
					extRefs.add(er);
				}
			}
		}
	}
	
    /**
     * Updates the value of this annotation and issues a modified event.
     * Equivalent to {@link #setValue(String)}.
     *
     * @param theValue the new textual value of this annotation
     * @see #setValue(String)
     */
    @Override
	public void updateValue(String theValue) {
        value = theValue;

        modified(ACMEditEvent.CHANGE_ANNOTATION_VALUE, null);
    }

    /**
     * Annotations are 'part of' or 'contained in' a tier. The tier is the 
     * parent node when a transcription is seen as a tree. 
     *
     * @return the tier this annotation is part of, this maybe null if an 
     * annotation hasn't been added to a tier yet or when it has been removed
     */
    @Override
	public Tier getTier() {
        return tier;
    }

    /**
     * Sets the tier this annotation belongs to
     *
     * @param theTier the tier holding this annotation
     */
    protected void setTier(Tier theTier) {
        tier = theTier;
    }

    /**
     * Marks this annotation for deletion in the process of removing it
     * from the tier. Notifies dependent annotations, which will be removed
     * in turn, and the parent annotation (if there).
     *
     * @param deleted if true the annotation is considered to be deleted or to be
     * going to be deleted from tier
     */
    @Override
	public void markDeleted(boolean deleted) {
        // System.out.println("ann: " + getValue() + " marked deleted");
        markedDeleted = deleted;

        notifyParentListeners();
        unregisterWithParent();
    }

    /**
     *
     * @return the marked for deletion flag
     */
    @Override
	public boolean isMarkedDeleted() {
        return markedDeleted;
    }

    /**
     * If this annotation has a parent annotation, removes this annotation
     * as a parent annotation listener from the parent.
     */
    public void unregisterWithParent() {
        if (hasParentAnnotation()) {
            Annotation p = getParentAnnotation();

            if (p != null) {
                p.removeParentAnnotationListener(this);
            }
        }
    }

    /**
     * Initiates the issuing of a modification event, using this annotation as
     * the source of the event.
     *
     * @param operation the type of modification (added, removed, changed etc.)
     * @param modification the (optional) new value of the modification
     *
     * @see ACMEditEvent
     */
    @Override
	public void modified(int operation, Object modification) {
        handleModification(this, operation, modification);
    }

    /**
     * Forwards the call to the tier, if it is not null.
     *
     * @param source the source of the event
     * @param operation the type of modification
     * @param modification the new value of the modification
     */
    @Override
	public void handleModification(ACMEditableObject source, int operation,
        Object modification) {
        if (tier != null) {
            tier.handleModification(source, operation, modification);
        }
    }

    /**
     * Each Annotation has 0..n  ACMListeners. Children of this annotation do
     * not account as ACMListeners.
	 * <p>
     * FIXME: The result of getParentListeners() is known to be Annotations (or even more specific!)
     * and is used as such without checking.
     * Even the callback RefAnnotation.parentAnnotationChanged() "knows" this.
     * So enforce that here already.
     * <br>
     * A better way would be to fix the ParentAnnotation interface, its use, and/or
     * the users of getParentListeners() (which is not part of the interface).
     * 
     * Added by: MK 03/07/02
     * @param listener the child Annotation
     */
    @Override
	public void addParentAnnotationListener(ParentAnnotationListener listener) {
        addParentAnnotationListener((Annotation)listener);
    }

    /**
     * A correction on or workaround for the inconsistency between interface
     * definition and usage in implementors. See above.
     * @param child the child annotation (=parent annotation listener)
     */
	protected void addParentAnnotationListener(Annotation child) {
        if (!parentAnnotListeners.contains(child)) {
            parentAnnotListeners.add(child);
        }
    }

	/**
     * Removes a parent annotation listener (child annotation) from the list of
     * listeners.
     *
     * @param l the listener (child) to remove
     * @see #addParentAnnotationListener(ParentAnnotationListener)
     */
    @Override
	public void removeParentAnnotationListener(ParentAnnotationListener l) {
        parentAnnotListeners.remove(l);
    }

    /**
     * Notifies listeners that an event occurred. The type of event is 
     * unspecified.
     */
    @Override
	public void notifyParentListeners() {
        List<ParentAnnotationListener> copiedList = new ArrayList<ParentAnnotationListener>(parentAnnotListeners);
        Iterator<ParentAnnotationListener> i = copiedList.iterator();

        while (i.hasNext()) {
            ParentAnnotationListener listener = i.next();
            listener.parentAnnotationChanged(new EventObject(this));
        }
    }

    /**
     * Each Annotation has 0..n child Annotations.<br>
     * 
     * <p>
     * FIXME: this returns a list of ParentAnnotationListeners, but in fact
     * is used (and known to contain) Annotations or even AbstractAnnotations.
     * This fact is even abused in this very file, see for example in 
     * {@link #getChildrenOnTier(Tier)}.
     * <p>
     * According to the interface, this knowledge should not be used.
     * 
     * MK 03/07/02
     * @return children-Annotations of 'this' parent, or empty ArrayList.
     */
    public List<Annotation> getParentListeners() {
        return parentAnnotListeners;
    }

    /**
     * gets a List with all dependent children on the specified tier. If
     * there are no children, an empty list is returned.
     *
     * Added by: AK 05/07/2002
     * @param tier the tier to get the child annotations for
     *
     * @return a List of Annotations
     */
    @Override
	public final List<Annotation> getChildrenOnTier(Tier tier) {
        List<Annotation> children = new ArrayList<Annotation>();
        Annotation annotation = null;

        for (Annotation annotation2 : parentAnnotListeners) {
            annotation = annotation2;

            if (annotation.getTier() == tier) {
                children.add(annotation);
            }
        }

        return children;
    }

    // Comparable<Annotation> interface method
    /**
     * Compares an annotation with this annotation based on begin and/or end time
     * or the index in the list of annotations. The implementation includes  
     * several non-obvious decisions e.g. when an {@link AlignableAnnotation}
     * is compared to a {@link RefAnnotation}.
     *  
     * @return -1 if this annotation is 'before' the other annotation, 0 if they
     * are equal and 1 if the other annotation is 'before' this annotation
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
	public int compareTo(Annotation obj) {
        // if not both RefAnnotations, delegate comparison to first alignable root.
        // if both RefAnnotations, call compareRefAnnotations()
        int ret = 0;

        Annotation a1 = this;
        Annotation a2 = obj;

        // comparison is on basis of embedding in 'network' of Annotations. When an Annotation
        // is marked deleted, it is already detached from the network.
        if (a1.isMarkedDeleted() || a2.isMarkedDeleted()) {
            return compareOtherwise(a1, a2);
        }

        int numOfRefAnnotations = 0;

        if (this instanceof RefAnnotation) {
            numOfRefAnnotations += 1;
            a1 = ((RefAnnotation) this).getFirstAlignableRoot();
        }

        if (obj instanceof RefAnnotation) {
            numOfRefAnnotations += 1;
            a2 = ((RefAnnotation) obj).getFirstAlignableRoot();
        }

        if (a1 != a2) { // two different RefAnnotations can have same alignable parent         
            ret = ((AlignableAnnotation) a1).getBegin().compareTo(((AlignableAnnotation) a2).getBegin());

            if (ret == 0) {
                ret = -1;
            }
             // otherwise shared begin timeslot would lead to equality
        } else { // same alignable parent

            // if one Alignable and one RefAnnotation, the Alignable one comes before the Ref one
            if (numOfRefAnnotations == 1) {
                if (this instanceof AlignableAnnotation) {
                    ret = -1;
                } else {
                    ret = 1;
                }
            } else if (numOfRefAnnotations == 2) { // two RefAnnotations with the same parent. 
                ret = compareRefAnnotations((RefAnnotation) this,
                        (RefAnnotation) obj);
            }
        }

        return ret;
    }

    // see comment inside the method
    private int compareOtherwise(Annotation a1, Annotation a2) {
        if (a1.equals(a2)) {
            return 0;
        } else {
            // no refs to parents, fall back on index on Tier
            Tier t1 = a1.getTier();
            Tier t2 = a2.getTier();

            if (t1 == t2) {
                List<? extends Annotation> v = null;

                v = ((TierImpl) t1).getAnnotations();

                if (v.indexOf(a1) < v.indexOf(a2)) {
                    return -1;
                } else {
                    return 1;
                }
            } else {
                return -1; // FIXME: fails symmetry requirement for compareTo()
            }
        }
    }

    /**
     * Compares two reference annotations. 
     *
     * @param a1 the first annotation
     * @param a2 the second annotation
     *
     * @return -1 if a1 is before a2, 1 if a2 is before a1, 0 otherwise
     */
    public int compareRefAnnotations(RefAnnotation a1, RefAnnotation a2) {
        // if on same tier, compare via "chain of reference".
        // else, take first and go up reference tree until on same tier or until alignable parent,
        // take second and repeat
        int ret = 0;

        if ((a1.getTier() == a2.getTier()) &&
                (a1.getReferences().get(0) == a2.getReferences()
                                                            .get(0))) { // and have same parent
            ret = compareUsingRefChain(a1, a2);
        } else {
            Annotation parent = (a1.getReferences().get(0));

            if (parent instanceof RefAnnotation) {
                ret = compareRefAnnotations((RefAnnotation) parent, a2);
            }

            if (ret == 0) { // still undecided
                parent = (a2.getReferences().get(0));

                if (parent instanceof RefAnnotation) {
                    ret = compareRefAnnotations(a1, (RefAnnotation) parent);
                }
            }
        }

        return ret;
    }

    /**
     * Compares two annotations that are on the same tier by following the
     * chain of RefAnnotations.
     *
     * @param a1 the first annotation
     * @param a2 the second annotation
     *
     * @return -1 if a1 is before a2, 0 if they are equal (the same object), 
     * 1 if a2 is before a1
     */
    public int compareUsingRefChain(RefAnnotation a1, RefAnnotation a2) {
        // a1 and a2 are on the same tier, and have the same alignable parent
        int ret = 1; // default: a1 comes after a2
        RefAnnotation nextR = a1.getNext();

        if (a1 == a2) {
            ret = 0;
        } else {
            while (nextR != null) {
                if (nextR == a2) { // a2 after a1
                    ret = -1;

                    break;
                }

                nextR = nextR.getNext();
            }
        }

        return ret;
    }
}
