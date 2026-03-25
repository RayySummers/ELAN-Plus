package mpi.eudico.server.corpora.clom;

import java.util.Iterator;
import java.util.List;


/**
 * TimeOrder encapsulates the ordering of TimeSlots in a Transcription. It is
 * considered to be part of the Transcription.  The TimeOrder is used when
 * comparing TimeSlots in the TimeSlot's compareTo method. Given a constructed
 * TimeOrder, it is then sufficient to add TimeSlots to a TreeSet, they will
 * be ordered according to the TimeOrder automatically.
 *
 * @author Hennie Brugman
 */
public interface TimeOrder {
    /**
     * Adds a TimeSlot to the TimeOrder at current position. The TimeSlot can
     * be either time-aligned or not time-aligned.
     *
     * @param theTimeSlot the TimeSlot to be inserted.
     */
    public void insertTimeSlot(TimeSlot theTimeSlot);

    /**
     * Adds a TimeSlot to the TimeOrder at current position. The TimeSlot can
     * be either time-aligned or not time-aligned. The TimeSlot is inserted
     * after 'afterSlot' and before 'beforeSlot'.
     *
     * @param theTimeSlot the TimeSlot to be inserted.
     * @param afterSlot the slot to insert the new slot after
     * @param beforeSlot the slot to insert before
     */
    public void insertTimeSlot(TimeSlot theTimeSlot, TimeSlot afterSlot,
        TimeSlot beforeSlot);

    /**
     * Removes a time slot from the list.
     *
     * @param theSlot the time slot to remove
     */
    public void removeTimeSlot(TimeSlot theSlot);

    /**
     * A utility method to print the current state of TimeOrder to standard
     * output.
     */
    public void printTimeOrder();

    /**
     * Returns true if timeSlot1 starts before timeSlot2, according to the
     * order specified by the TimeOrder. Each TimeSlot can be either
     * time-aligned or  not time-aligned.
     *
     * @param timeSlot1 first TimeSlot to be compared.
     * @param timeSlot2 second TimeSlot to be compared.
     *
     * @return true if timeSlot1 starts before timeSlot2.
     */
    public boolean isBefore(TimeSlot timeSlot1, TimeSlot timeSlot2);

    /**
     * Returns the {@code TimeSlot} before the specified time slot.
     * 
     * @param timeSlot the time slot to find the preceding one for
     * @return the TimeSlot before the given one.
     */
	public TimeSlot getPredecessorOf(TimeSlot timeSlot);

	/**
	 * Returns the number of {@code TimeSlot} objects in the {@code TimeOrder}.
	 * 
     * @return the number of elements in TimeOrder
     */
    public int size();

    /**
     * Remove all time slots that are not referenced by any Annotation. If this
     * is not done the TimeOrder will grow continuously from generation to
     * generation of the XML document.
     */
    public void pruneTimeSlots();

    /**
     * Returns the actual time of a slot, or a proposed, interpolated time for 
     * unaligned time slots.
     *
     * @param theSlot the slot to get the (proposed) time for
     * @return either the actual time of the slot, or, in case of unaligned slots, 
     * a proposal for a time.
     */
    public long proposeTimeFor(TimeSlot theSlot);

    /**
     * Returns a {@code TimeSlot} iterator.
     * 
     * @return an iterator for the slots in this time order
     */
    public Iterator<TimeSlot> iterator();

    /**
     * Updates the time value of a time slot and changes its position in the 
     * list if necessary to maintain the ordering principle.
     *
     * @param ts the time slot to update
     * @param newTime the new time for the slot
     */
    public void modifyTimeSlot(TimeSlot ts, long newTime);

	/**
	 * Increase the time value of part of the time slots with a certain 
	 * amount of ms.
	 *  
	 * @param fromTime only slots with time values greater than this value will be changed 
	 * @param shift the amount of ms to add to the time value
	 * @param lastFixedSlot the last slot that should not be changed 
	 * (the end time slot of a source annotation)
	 * @param otherFixedSlots optional other slots that should not be changed
	 *  
	 */
    public void shift(long fromTime, long shift, TimeSlot lastFixedSlot, 
		List<TimeSlot> otherFixedSlots);
    
    /**
     * Adds the specified amount of ms to the time value of aligned slots.
     * 
     * @param shiftValue the amount of ms to add to the time values
     * @throws IllegalArgumentException when any slot would get a negative time value
     */ 
    public void shiftAll(long shiftValue) throws IllegalArgumentException;

}
