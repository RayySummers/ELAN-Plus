package mpi.eudico.server.corpora.clom;

/**
 * An object to anchor a media event to an actual media time value or to a 
 * virtual media position based on the index of a {@code TimeSlot} in a sequence
 * of slots.<p> 
 * Several other tools use the term {@code Anchor} for this concept.   
 */
public interface TimeSlot extends Comparable<TimeSlot> {
    /** a constant to indicate that a slot is not connected to an actual media
     *  time but serves as an anchor to position events between other events */
    public static final int TIME_UNALIGNED = -1;

    /** a constant to indicate that a slot is not yet added to and correctly
     * positioned in a {@link TimeOrder} instance */
    public static final int NOT_INDEXED = -1;

    /**
     * The index of a {@code TimeSlot} usually corresponds to the index it has
     * in a list or set, but especially during ongoing modifications this may
     * not be the case.
     *
     * @return the index of this slot in a sequence of slots as determined by
     * a {@link TimeOrder} object, or {@link #TIME_UNALIGNED} if it isn't 
     * indexed yet 
     */
    public int getIndex();

    /**
     * When a slot has been positioned in a list or set its index is set or 
     * updated by the enclosing {@link TimeOrder} object.
     *
     * @param theIndex the new index value 
     */
    public void setIndex(int theIndex);

    /**
     * Returns the media time of the slot.
     * 
     * @return the media time of this slot or {@link #TIME_UNALIGNED}
     */
    public long getTime();

    /**
     * Sets the time value of this slot. This might lead to an update of 
     * its index in the list of slots.
     *
     * @param theTime the new media time for this slot
     */
    public void setTime(long theTime);

    /**
     * Updates the time value of this slot and updates its position in
     * the list or set it is part of.
     * Only to be called by TimeOrder !! (This method should actually not be 
     * public.)
     * 
     * @param theTime the new time value of this slot and updates its position in
     * the list or set it is part of 
     */
    public void updateTime(long theTime);

    /**
     * Returns whether this slot holds an actual media time.
     * 
     * @return {@code true} if this slot holds an actual media time, {@code false}
     * otherwise
     */
    public boolean isTimeAligned();

    /**
     * Returns whether this {@code TimeSlot} is positioned after the specified
     * time slot.
     * 
     * @param timeSlot the slot to compare this one with
     *
     * @return {@code true} if this slot is positioned after the specified slot
     * (has a higher index), {@code false} otherwise
     */
    public boolean isAfter(TimeSlot timeSlot);
}
