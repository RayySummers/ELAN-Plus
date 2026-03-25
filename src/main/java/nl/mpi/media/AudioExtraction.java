package nl.mpi.media;

/**
 * An interface for classes that extract audio data from a video file or from
 * an audio file other than a WAVE file.
 * The main purpose is to support visualization of a waveform or spectrogram
 * based on the audio of such files.
 * 
 * @author Han Sloetjes
 */
public interface AudioExtraction {

	/**
	 * Returns the sample frequency of the (decoded) audio.
	 * 
	 * @return the sample frequency, the number of samples per second
	 */
	int getSampleFrequency();

	/**
	 * Returns the number of bits per sample.
	 * 
	 * @return the number of bits for a single sample
	 */
	int getBitsPerSample();

	/**
	 * Returns the number of channels in the audio stream.
	 * 
	 * @return the number of audio channels
	 */
	int getNumberOfChannels();

	/**
	 * Returns the format tag corresponding to the format of the returned data.
	 * Should be one of the WAV header format tags; {@code 0 "Unknown"} and 
	 * {@code 1 "PCM (uncompressed)"} are expected and handled the same as 
	 * uncompressed WAVE files. 
	 * <p>
	 * Some frameworks (e.g FFmpeg) might return {@code 3 "IEEE float"} for 
	 * some types of input.
	 * 
	 * @return {@code 1} by default, assuming the native framework delivers
	 * uncompressed PCM data as integers
	 */
	int getFormatTag();

	/**
	 * Returns the duration of the audio stream in milliseconds.
	 * 
	 * @return the duration in milliseconds
	 */
	long getDuration();

	/**
	 * Returns the duration of the audio stream in seconds.
	 * 
	 * @return the duration in seconds
	 */
	double getDurationSec();

	/**
	 * Returns the size of the default buffer size of the native framework.
	 * This may not be specified for a framework and 0 or 1 might be returned.
	 * Otherwise an attempt can be made to request a multiple of this size.
	 * 
	 * @return the default size of the native buffer, used for a single read
	 * and decode action
	 */
	long getSampleBufferSize();

	/**
	 * Returns the duration in milliseconds represented by a single buffer of 
	 * audio samples, as employed by the native decoder/source reader.
	 * 
	 * @return the duration of one buffer of audio samples
	 */
	long getSampleBufferDurationMs();

	/**
	 * Returns the duration in seconds represented by a single buffer of 
	 * audio samples, as employed by the native decoder/source reader.
	 * 
	 * @return the duration of one buffer of audio samples in seconds
	 */
	double getSampleBufferDurationSec();

	/**
	 * Initializes a direct {@code ByteBuffer} of sufficient size for the bytes
	 * of the specified time span. There is currently no built-in limit to the
	 * size of the buffer. The caller is responsible for deciding whether an 
	 * interval can be loaded in a single action or should be split into 
	 * multiple calls.
	 * In the current implementation a new {@code ByteBuffer} is created for
	 * every call, making this method thread safe, but this might change to
	 * re-using an existing buffer if it is large enough or the interval.
	 *   
	 * @param fromTime the time of the first sample in seconds
	 * @param toTime the time of the last sample in seconds
	 * @return an array containing the decoded samples for the specified interval
	 */
	byte[] getSamples(double fromTime, double toTime);

	/**
	 * Returns the sample for the specified time plus the following samples that
	 * fit in the default native buffer.
	 *   
	 * @param forTime the time to get the sample for 
	 * @return an array of bytes, starting with the sample at the requested time
	 */
	byte[] getSample(double forTime);

	/**
	 * Returns (an approximation of) the current read position, in seconds.
	 * 
	 * @return the read position in seconds
	 */
	double getPositionSec();

	/**
	 * Sets the position of the audio source reader, in seconds.
	 * 
	 * @param seekPositionSec the seek position for the reader
	 */
	void setPositionSec(double seekPositionSec);

	/**
	 * Releases the native resources from memory when done with this class.
	 */
	void release();

}