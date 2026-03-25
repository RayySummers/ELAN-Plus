package mpi.eudico.client.annotator.recognizer.data;

import java.util.Objects;

/**
 * Base class for selection data, identifying a segment by a begin and an end time.
 * Almost identical to mpi.eudico.util.TimeInterval, but to allow convenient extension
 * implementation, this class is part of the recognizer package.
 * 
 * There are specialized versions for audio and for video recognizers.
 * 
 * @author albertr
 * 
 * @version 2.0 Jan 2010 HS: the channel field has been moved to the subclass AudioSegment
 * Changed name for clearer distinction of existing Selection types.  
 */
public class RSelection {
	/** the begin time of the selection */
	public long beginTime; // begin time in milliseconds
	/** the end time of the selection */
	public long endTime;   // end time in milliseconds
	// channel has been moved to AudioSegment
	//public int channel;    // channel number to AudioSegment
	
	/**
	 * Constructor. 
	 * Note: no checks are performed on the value of begin time and end time.
	 * 
	 * @param beginTime the begin time
	 * @param endTime the end time
	 */
	public RSelection(long beginTime, long endTime) {
		super();
		this.beginTime = beginTime;
		this.endTime = endTime;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof RSelection) {
			return (this.beginTime == ((RSelection) obj).beginTime && 
					this.endTime == ((RSelection) obj).endTime);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(beginTime, endTime);
	}

}