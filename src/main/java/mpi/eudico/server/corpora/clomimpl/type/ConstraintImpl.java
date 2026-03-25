package mpi.eudico.server.corpora.clomimpl.type;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.TimeSlot;
import mpi.eudico.server.corpora.clomimpl.abstr.RefAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;


/**
 * Abstract Constraint class, provides basic implementation of most methods
 * of the {@code Constraint} interface.
 */
public abstract class ConstraintImpl implements Constraint {
    /** a list for nested constraints */
    protected List<Constraint> nestedConstraints;

    /**
     * Creates a new ConstraintImpl instance
     */
    public ConstraintImpl() {
        nestedConstraints = new ArrayList<Constraint>();
    }

    /**
     * Iterates over the nested constraints and calls their implementation of
     * the {@code ConstraintImpl#forceTimes(long[], TierImpl)} method.
     *
     * @param segment the time interval
     * @param forTier the target tier
     */
    @Override
	public void forceTimes(long[] segment, TierImpl forTier) {
        for (Constraint c : nestedConstraints) {
            c.forceTimes(segment, forTier);
        }
    }

    /**
     * Iterates over the nested constraints and calls their implementation of
     * the {@code ConstraintImpl#getBeginTimeForRefAnnotation(RefAnnotation)} method.
     *
     */
    @Override
	public long getBeginTimeForRefAnnotation(RefAnnotation theAnnot) {
        long t = 0;

        for (Constraint c : nestedConstraints) {
            t = c.getBeginTimeForRefAnnotation(theAnnot);
        }

        return t;
    }

    /**
     * Iterates over the nested constraints and calls their implementation of
     * the {@code ConstraintImpl#getEndTimeForRefAnnotation(RefAnnotation)} method.
     *
     */
    @Override
	public long getEndTimeForRefAnnotation(RefAnnotation theAnnot) {
        long t = 0;

        for (Constraint c : nestedConstraints) {
            t = c.getEndTimeForRefAnnotation(theAnnot);
        }

        return t; // default
    }

    /**
     * Iterates over the nested constraints and calls their implementation of
     * the {@code ConstraintImpl#getTimeSlotsForNewAnnotation(long, long, TierImpl)} 
     * method.
     * The implementation is such that only the last {@code ConstraintImpl}'s 
     * implementation matters; that list is returned.
     *
     */
    @Override
	public List<TimeSlot> getTimeSlotsForNewAnnotation(long begin, long end,
        TierImpl forTier) {
        List<TimeSlot> slots = new ArrayList<TimeSlot>();

        for (Constraint c : nestedConstraints) {
            slots = c.getTimeSlotsForNewAnnotation(begin, end, forTier);
        }

        return slots;
    }

    /**
     * Stub, no default implementation. 
     *
     * @param theTier the target tier
     */
    @Override
	public void enforceOnWholeTier(TierImpl theTier) {
        //	Iterator cIter = nestedConstraints.iterator();
        //	while (cIter.hasNext()) {
        //		((Constraint) cIter.next()).enforceOnWholeTier(theTier);
        //	}	
    }

    /**
     *
     * @return false
     */
    @Override
	public boolean supportsInsertion() {
        return false;
    }

    /**
     * Empty method, just returns {@code null}.
     * 
     * @return {@code null} by default
     */
    @Override
	public Annotation insertBefore(Annotation beforeAnn, TierImpl theTier) {
        return null;
    }

    /**
     * Empty method, just returns {@code null}.
     *
     * @return {@code null} by default
     */
    @Override
	public Annotation insertAfter(Annotation afterAnn, TierImpl theTier) {
        return null;
    }

    /**
     * Empty method body.
     */
    @Override
	public void detachAnnotation(Annotation theAnn, TierImpl theTier) {
        // default: do nothing
    }

    /**
     * Adds a {@code Constraint} to the list of nested constraints.
     *
     * @param theConstraint the constraint to add to the list of nested 
     * constraints
     */
    @Override
	public void addConstraint(Constraint theConstraint) {
        nestedConstraints.add(theConstraint);
    }
	
	/**
	 * Overrides <code>Object</code>'s equals method by checking number and type
	 * of the nested Constraints of the other object to be equal to the number 
	 * and type of the nested Constraints in this object.
	 * 
	 * @param obj the reference object with which to compare
	 * @return true if this object is the same as the obj argument; false otherwise
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			// null is never equal
			return false;
		}
		if (obj == this) {
			// same object reference 
			return true;
		}
		if (!(obj instanceof ConstraintImpl)) {
			// it should be a ConstraintImpl object
			return false;
		}
		
		ConstraintImpl other = (ConstraintImpl) obj;
		
		if (other.getStereoType() != this.getStereoType()) {
			return false;
		}
		
		if (nestedConstraints.size() != other.nestedConstraints.size()) {
			return false;
		}
		
		boolean allConstraintsEqual = true;
		
		loop:
		for (int i = 0; i < nestedConstraints.size(); i++) {
			ConstraintImpl ci = (ConstraintImpl) nestedConstraints.get(i);
			for (int j = 0; j < other.nestedConstraints.size(); j++) {
				if (ci.equals(other.nestedConstraints.get(j))) {
					continue loop;	
				}
			}
			// if we get here constraints are unequal
			allConstraintsEqual = false;
			break;
		}
		
		return allConstraintsEqual;
	}
	
	
	@Override
	public int hashCode() {
		return Objects.hash(getStereoType(), nestedConstraints);
	}

	@Override
	public ConstraintImpl clone() throws CloneNotSupportedException {
		ConstraintImpl copy = (ConstraintImpl)super.clone();
		// deep-copy List
		ArrayList<Constraint> newList = new ArrayList<Constraint>();
		for (Constraint c : copy.nestedConstraints) {
			newList.add(c.clone());
		}
		copy.nestedConstraints = newList;
		
		return copy;
	}
	
    /**
     * Returns the stereotype name of the specified type constant.
     * 
     * @param typeConstant one of the stereotype constants, TIME_SUBDIVISION etc.
     * @return a String representation of one of the public stereotype
     */
    public static String getStereoTypeName(int typeConstant) {
    	switch(typeConstant) {
    	case Constraint.TIME_SUBDIVISION:
    		return Constraint.publicStereoTypes[0];
    	case Constraint.INCLUDED_IN:
    		return Constraint.publicStereoTypes[1];
    	case Constraint.SYMBOLIC_SUBDIVISION:
    		return Constraint.publicStereoTypes[2];
    	case Constraint.SYMBOLIC_ASSOCIATION:
    		return Constraint.publicStereoTypes[3];
    	default:
    		return "No Constraint";
    	}
    }
}
