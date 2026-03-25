package mpi.eudico.client.annotator;

/**
 * Defines methods for a Selection user, which has direct access to the 
 * {@code Selection} instance and receives notifications.
 */
public interface SelectionUser extends SelectionListener {
    /**
     * Sets the {@code Selection} object.
     *
     * @param selection the {@code Selection}
     */
    public void setSelectionObject(Selection selection);

    /**
     * Notification of a change in the selection.
     */
    @Override
	public void updateSelection();

    /**
     * Sets the begin and end time of the selection.
     *
     * @param begin the begin time
     * @param end the end time
     */
    public void setSelection(long begin, long end);

    /**
     * Returns the begin time of the selection.
     *
     * @return the selection begin time
     */
    public long getSelectionBeginTime();

    /**
     * Returns the selection end time
     *
     * @return the selection end time
     */
    public long getSelectionEndTime();
}
