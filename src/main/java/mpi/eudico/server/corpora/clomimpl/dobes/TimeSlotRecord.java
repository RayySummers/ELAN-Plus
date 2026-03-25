package mpi.eudico.server.corpora.clomimpl.dobes;

/**
 * A record for temporary storage of id and time value while an annotation
 * file is being parsed.
 * <br>
 * Note: the record classes could be moved to another package
 * @author Han Sloetjes
 *
 */
public class TimeSlotRecord {
	private int id;
	private long value = -1;
	
	/**
	 * Creates a new record instance.
	 */
	public TimeSlotRecord() {
		super();
	}

	/**
	 * Creates a new record instance.
	 * 
	 * @param id the id of the time slot
	 * @param value the time value of the slot
	 */
	public TimeSlotRecord(int id, long value) {
		super();
		this.id = id;
		this.value = value;
	}

	/**
	 * Returns the id.
	 * 
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets the id of the slot.
	 * 
	 * @param id the id of the slot
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Returns the time value of the slot.
	 * 
	 * @return the time value
	 */
	public long getValue() {
		return value;
	}

	/**
	 * Sets the time value of the slot.
	 * 
	 * @param value the time value 
	 */
	public void setValue(long value) {
		this.value = value;
	}

}
