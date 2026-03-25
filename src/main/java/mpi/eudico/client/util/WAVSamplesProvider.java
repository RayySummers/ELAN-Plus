package mpi.eudico.client.util;

/**
 * Interface for classes that are able to extract bytes from an audio track
 * and provide them e.g. to classes that create a visualization of the audio.
 *
 */
public interface WAVSamplesProvider {
	
	/**
	 * Returns the file path or URL as a string.
	 * 
	 * @return the media path as a string
	 */
	public String getMediaLocation();

	/**
	 * Returns the total duration of the samples in milliseconds.
	 *
	 * @return the duration of the media file
	 */
	public float getDuration();
	
	/**
	 * Returns the total duration in seconds.
	 * 
	 * @return the media duration in seconds
	 */
	public double getDurationSeconds();
	
	/**
	 * Returns the number of channels in the audio track.
	 * 
	 * @return the number of channels
	 */
	public int getNumberOfChannels();
	
	/**
	 * Returns the number of bits used to store a single sample. 
	 * 
	 * @return the number of bits per sample
	 */
	public int getBitsPerSample();
	
	/**
	 * Returns the array of (converted, decoded) sample data of a single channel.
	 * 
	 * @param channelIndex the zero-based channel index
	 * @return an array of {@code int} or {@code null}
	 * 
	 * @see #readInterval(int, int)
	 */
	public int[] getChannelArray(int channelIndex);
	
	/**
	 * Returns the maximum value of the read samples of a specific channel.
	 * The channel is identified by a zero-based index.
	 * 
	 * @param channelIndex the zero-based index of the channel
	 * @return the maximum value of the samples currently read into memory
	 */
	public int getMaxSample(int channelIndex);
	
	/**
	 * Returns the minimum value of the read samples of a specific channel.
	 * The channel is identified by a zero-based index.
	 * 
	 * @param channelIndex the zero-based index of the channel
	 * @return the minimum value of the samples currently read into memory
	 */
	public int getMinSample(int channelIndex);	

	/**
	 * Returns the total number of samples in the file.
	 * 
	 * @return the total number of samples
	 */
	public long getNrOfSamples();

	/**
	 * Returns the maximal possible value of the samples.
	 *
	 * @return the possible maximum sample value
	 */
	public int getPossibleMaxSample();

	/**
	 * Returns the minimal possible value of the samples.
	 *
	 * @return the possible minimum sample value
	 */
	public int getPossibleMinSample();

	/**
	 * Returns the sample frequency (number of samples per second) of the file.
	 *
	 * @return the sample frequency
	 */
	public int getSampleFrequency();

	/**
	 * Returns the index of the current sample, the location of the sample
	 * pointer (if applicable). 
	 *
	 * @return the location of the sample pointer
	 */
	public long getSamplePointer();

	/**
	 * Returns the time corresponding to a given sample index.
	 *
	 * @param sample the index or position of the sample
	 *
	 * @return the time in milliseconds
	 */
	public long getTimeAtSample(long sample);

	/**
	 * Returns the time in milliseconds corresponding to the current sample
	 * index (the current location of pointer).
	 *
	 * @return the time in milliseconds at the pointer's position
	 */
	public float getTimePointer();
	
	/**
	 * Returns the time in seconds corresponding to the current sample index 
	 * (the current position of the pointer).
	 * 
	 * @return the time in seconds at the current position of the pointer
	 */
	public double getTimePointerSeconds();

	/**
	 * Closes the provider and releases resources.
	 */
	public void close();

	/**
	 * Reads the requested number of samples in one run and stores the extracted
	 * values in one (mono or merged) or two (stereo) arrays of {@code int} (or
	 * more if more than two channels are supported). 
	 * These arrays can then be accessed to further process the samples.<br>
	 * <b>Note:</b> if the loading of two channels is requested, a second array
	 * of the proper size will always be created, even if the {@code .wav} file 
	 * does not actually contain more than one channel.
	 *
	 * @param requestedNrOfSamplesToRead the number of samples to read
	 * @param nrOfChannelsToLoad the number of channels to get
	 *
	 * @return the number of <b>samples</b> that have been read, less than or equal to
	 *         requestedNrOfSamplesToRead
	 *
	 * @see #getChannelArray(int)
	 */
	public int readInterval(int requestedNrOfSamplesToRead, int nrOfChannelsToLoad);

	/**
	 * Seeks to the {@code n-th} sample. This sets the start position for 
	 * the next read action.
	 *
	 * @param n the sample index to seek
	 */
	public void seekSample(long n);

	/**
	 * Seek the sample that corresponds to the specified time in milliseconds.
	 *
	 * @param time the time in milliseconds
	 */
	public void seekTime(float time);
	
	/**
	 * Seek the sample that corresponds to the specified time in seconds.
	 *
	 * @param timeSec the time in seconds
	 */
	public void seekTimeSeconds(double timeSec);
	
	/**
	 * Returns the type of compression, the audio format of the header chunk 
	 * (in case of a WAVE file). 
	 * 
	 * @return the compression code, 1 (PCM, uncompressed) by default
	 */
	public short getCompressionCode();
	
	/**
	 * Returns a string representation of a specific compression type.
	 * 
	 * @param compr the compression code
	 * 
	 * @return a string representation of the compression type
	 */
	public String getCompressionString(short compr);
	
	/**
	 * Enables or disables debug level messages.
	 * 
	 * @param enable if {@code true} more information messages will be logged 
	 */
	public void setDebugMode(boolean enable);
}