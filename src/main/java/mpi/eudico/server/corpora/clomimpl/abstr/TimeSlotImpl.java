package mpi.eudico.server.corpora.clomimpl.abstr;

import mpi.eudico.server.corpora.clom.TimeOrder;
import mpi.eudico.server.corpora.clom.TimeSlot;


/**
 * Implementation of a {@link TimeSlot} which maintains a reference to the
 * {@link TimeOrder} it is part of.
 * <p>
 * Note: this class has a natural ordering that is inconsistent with equals.
 * 
 * @author MK (02/06/19)
 */
public class TimeSlotImpl implements TimeSlot {
    /** the time value */
    long time;

    /** if true, the time value should be >= 0 */
    boolean isAligned;

    /** the position of the slot in the ordered list of slots */
    int index;

    /** the {@code TimeOrder} this slot is part of */
    TimeOrder timeOrder;
    
	/** field for a calculated, proposed time for an unaligned slot */
	long proposedTime;

    /**
     * Creates a new unaligned TimeSlotImpl instance
     *
     * @param theTO the {@code TimeOrder} this slot is part of
     */
    public TimeSlotImpl(TimeOrder theTO) {
        time = TIME_UNALIGNED;
        isAligned = false;
        index = NOT_INDEXED;
        timeOrder = theTO;
		proposedTime = TIME_UNALIGNED;
    }

    /**
     * Creates a new TimeSlotImpl instance
     *
     * @param theTime the time for the slot
     * @param theTO the {@code TimeOrder} this slot is part of
     */
    public TimeSlotImpl(long theTime, TimeOrder theTO) {
        time = theTime;
        isAligned = true;
        index = NOT_INDEXED;
        timeOrder = theTO;
		proposedTime = TIME_UNALIGNED;
    }

    /**
     * @return the current stored value of the {@code index} field
     */
    @Override
	public int getIndex() {
        return index;
    }

    /**
     * Sets the value of the {@code index} field
     *
     * @param theIndex the new index of this slot, usually corresponding to the
     * index of the slot in the list maintained by {@link TimeOrder}
     */
    @Override
	public void setIndex(int theIndex) {
        index = theIndex;
    }

    /**
     * 
     * @return the current time value of this slot
     */
    @Override
	public long getTime() {
        return time;
    }

    /**
     * Sets the time value of this slot, calls {@link TimeOrder#modifyTimeSlot(TimeSlot, long)}
     * to allow the {@code TimeOrder} to maintain consistency
     *
     * @param theTime the new time value
     */
    @Override
	public void setTime(long theTime) {
        // time = theTime;

        timeOrder.modifyTimeSlot(this, theTime);

        if (theTime >= 0) {
            isAligned = true;
        } else {
            isAligned = false;
        }
    }

    /**
     * Only to be called by TimeOrder !! Intended to be package private.
     *
     * @param theTime the new time value
     */
    @Override
	public void updateTime(long theTime) {
        time = theTime;
    }

    /**
     *
     * @return the value of the {@code isAligned} flag
     */
    @Override
	public boolean isTimeAligned() {
        return isAligned;
    }

    /**
     * Returns true if this timeSlot comes after the parameter timeSlot
     *
     * @param timeSlot the timeSlot against which is to be checked if this
     *        timeSlot comes after it.
     *
     * @return true if this timeSlot comes after the specified {@code timeSlot}
     */
    @Override
	public boolean isAfter(TimeSlot timeSlot) {
        /*        if (isTimeAligned() && timeSlot.isTimeAligned() && time > timeSlot.getTime()) {
           return true;
           }
           if (isTimeAligned() && timeSlot.isTimeAligned() && time == timeSlot.getTime()) {
               if (index > timeSlot.getIndex()) {
                   return true;
               }
           }*/
        if (index > timeSlot.getIndex()) {
            return true;
        }

        return false;
    }
    
    /**
     * Returns the precalculated time for unaligned time slots.
     * To disable this feature let this method always return -1.
     * 
     * @return the pre-calculated proposed time
     */
	public long getProposedTime() {
		//return -1;
		return proposedTime;
	}
	
	/**
	 * Sets the new proposed time after a change in related aligned time slots.
	 * @param proposedTime the new proposed time
	 */   
	public void setProposedTime(long proposedTime) {
		this.proposedTime = proposedTime;
	}

	/**
	 * Compares two slots based on the value of their {@code index} field.
	 * @return -1 if this slot comes before (is at a lower index) the {@code obj} 
	 * slot, 1 if the other slot has a lower index value, 0 if their index values
	 * are equal (should not happen)
	 */
    @Override
	public int compareTo(TimeSlot obj) {
        int ret = 1;

        if (this.getIndex() > obj.getIndex()) {
            ret = 1;
        }
        // NOTE: 0 case necessary, because TreeSet.remove uses compareTo
        // to test equality (?!)
        else if (this.getIndex() == obj.getIndex()) {
            ret = 0;
        } else {
            ret = -1;
        }

        return ret;
    }
}
