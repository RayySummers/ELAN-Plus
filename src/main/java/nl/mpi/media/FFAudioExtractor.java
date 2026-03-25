package nl.mpi.media;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

/**
 * An audio extractor based on FFmpeg.
 */
public class FFAudioExtractor implements AudioExtraction {
	private int formatTag = -1;
	private static boolean ffLibLoaded = false;
	private static boolean ffNativeLogLoaded = false;
	
	final static System.Logger LOG = System.getLogger("NativeLogger");
	/* the media file path or URL */
	private String mediaPath;
	/* the id (address) of the native counterpart */
	private long id;
	/* cache some fields that are unlikely to change during the lifetime of the extractor */
	private int    sampleFrequency = 0;
	private int    numberOfChannels = 0;
	private int    bitsPerSample = 0;
	private long   mediaDuration = 0;
	private double mediaDurationSec = 0.0d;
	private long   sampleBufferDuration = 0;
	private double sampleBufferDurationSec = 0.0d;
	private long   sampleBufferSize = 0;// is this a constant in all implementations?
	private double curMediaPosition;
	//private ReentrantLock samplerLock = new ReentrantLock();
	//private int failedLoadsCount = 0;
	
	// reuse the ByteBuffer
	private ByteBuffer curByteBuffer;
	private int curBufferSize = 4 * 1024 * 1024;// start with 4Mb
	private final ReentrantLock bufferLock = new ReentrantLock();
	// loading using a byte array instead of a ByteBuffer
	private static boolean useByteArray = true;
	
	static {
		// ffmpeg libs
		try {
			//System.loadLibrary("avformat");
			System.loadLibrary("avutil");
			System.loadLibrary("swresample");
			System.loadLibrary("avcodec");
			System.loadLibrary("avformat");
		} catch (UnsatisfiedLinkError ule) {
			if (LOG.isLoggable(System.Logger.Level.WARNING)) {
				LOG.log(System.Logger.Level.WARNING, "Could not load a native FFMPEG library ([dll/dylib/so]): " + ule.getMessage());
			}
		} catch (Throwable t) {
			if (LOG.isLoggable(System.Logger.Level.WARNING)) {
				LOG.log(System.Logger.Level.WARNING, "Could not load a native FFMPEG library ([dll/dylib/so]): " + t.getMessage());
			}
		}
		try {
			System.loadLibrary("JNIUtil");
			ffNativeLogLoaded = true;
		} catch (UnsatisfiedLinkError ule) {
			if (LOG.isLoggable(System.Logger.Level.WARNING)) {
				LOG.log(System.Logger.Level.WARNING, "Could not load native utility library (libJNIUtil.[dll/dylib/so]): " + ule.getMessage());
			}
		} catch (Throwable t) {
			if (LOG.isLoggable(System.Logger.Level.WARNING)) {
				LOG.log(System.Logger.Level.WARNING, "Could not load native utility library (libJNIUtil.[dll/dylib/so]): " + t.getMessage());
			}
		}
		try {
			// load native AudioExtractor
			System.loadLibrary("FFAudioExtractor");
			ffLibLoaded = true;
		} catch (Throwable t) {
			//t.printStackTrace();
			if (LOG.isLoggable(System.Logger.Level.WARNING)) {
				LOG.log(System.Logger.Level.WARNING, "Error loading native library: " + t.getMessage());
			}
		}
		// here add separate set and get methods for debug mode for FFAudioExtractor
		if (ffLibLoaded && ffNativeLogLoaded) {
			try {
				FFAudioExtractor.initLog("nl/mpi/jni/NativeLogger", "nlog");
				if (LOG.isLoggable(System.Logger.Level.DEBUG)) {
					FFAudioExtractor.setDebugMode(true);
				}
			} catch (Throwable t) {
				if (LOG.isLoggable(System.Logger.Level.WARNING)) {
					LOG.log(System.Logger.Level.WARNING, "Error while configuring the FFAudioExtractor: " + t.getMessage());
				}
			}
		}
		
		String arrayLoad = System.getProperty("AudioExtractor.UseByteArray");
		if (arrayLoad != null) {
			if (!arrayLoad.equalsIgnoreCase("true") ) {
				useByteArray = false;
			}
		}
	}
	
	/**
	 * Creates a new extractor instance.
	 * 
	 * @param mediaPath the path to the media file
	 * @throws UnsupportedMediaException if the media file is not supported
	 */
	public FFAudioExtractor(String mediaPath) throws UnsupportedMediaException {
		super();
		if (!ffLibLoaded) {
			throw new UnsupportedMediaException("A native library was not found or could not be loaded");
		}
		this.mediaPath = mediaPath;
		
		id = initNativeFF(mediaPath);
		
		if (id > 0) {
			if (LOG.isLoggable(System.Logger.Level.DEBUG)) {
				LOG.log(System.Logger.Level.DEBUG, "The native FFAudioExtractor was initialized successfully");
			}
		} else {
			// failure to initialize
			if (LOG.isLoggable(System.Logger.Level.WARNING)) {
				LOG.log(System.Logger.Level.WARNING, "The native FFAudioExtractor could not be initialized");
			}
			throw new UnsupportedMediaException("The native FFAudioExtractor could not be initialized.");
		}
	}
	
	@Override
	public int getSampleFrequency() {
		if (sampleFrequency == 0) {
			sampleFrequency = getSampleFrequencyFF(id); 
		}
		
		return sampleFrequency;
	}

	@Override
	public int getBitsPerSample() {
		if (bitsPerSample == 0) {
			bitsPerSample = getBitsPerSampleFF(id);
			
			if (bitsPerSample == 0) {
				// calculate based on other properties
			}
		}
		
		return bitsPerSample;
	}

	@Override
	public int getNumberOfChannels() {
		if (numberOfChannels == 0) {
			numberOfChannels = getNumberOfChannelsFF(id); 
		}
		
		return  numberOfChannels;
	}

	@Override
	public int getFormatTag() {
		if (formatTag < 0) {
			formatTag = this.getFormatTagFF(id);
		}
		
		return formatTag;
	}

	@Override
	public long getDuration() {
		if (mediaDuration == 0) {
			mediaDuration = getDurationMsFF(id);
		}
		
		return mediaDuration;
	}

	@Override
	public double getDurationSec() {
		if (mediaDurationSec == 0) {
			mediaDurationSec = getDurationSecFF(id);
		}
		
		return mediaDurationSec;
	}

	@Override
	public long getSampleBufferSize() {
		if (sampleBufferSize <= 1) {
			sampleBufferSize = getSampleBufferSizeFF(id);
		}
		
		return sampleBufferSize;
	}

	@Override
	public long getSampleBufferDurationMs() {
		if (sampleBufferDuration == 0) {
			sampleBufferDuration = getSampleBufferDurationMsFF(id);
		}
		
		return sampleBufferDuration;
	}

	@Override
	public double getSampleBufferDurationSec() {
		if (sampleBufferDurationSec == 0.0) {
			sampleBufferDurationSec = getSampleBufferDurationSecFF(id);
		}
		
		return sampleBufferDurationSec;
	}

	@Override
	public byte[] getSamples(double fromTime, double toTime) {
		if (id == 0) {
			return null;
		}
		if (toTime <= fromTime) {
			return null;
		}
		if (fromTime >= getDurationSec()) {
			return null;
		}
		if (toTime > getDurationSec()) {
			toTime = getDurationSec();
		}
		double timeSpan = toTime - fromTime;
		int numBytes = 0;
		double bufferDur = getSampleBufferDurationSec();
		
		if (bufferDur > 0) {
			double numBuffers = timeSpan / bufferDur;
			int nb = (int) Math.round((numBuffers * getSampleBufferSize() * getNumberOfChannels()));
			numBytes = nb + (int) getSampleBufferSize();// add some bytes extra?
		} else {
			// log error?
			numBytes = (int) (timeSpan * (getSampleFrequency() * 
					(getBitsPerSample() / 8) * getNumberOfChannels()));
			numBytes += 1024;
		}
		

		if (useByteArray) {
			return getSamplesByteArray(fromTime, toTime, numBytes);
		}
		
		bufferLock.lock();
		try {
			//ByteBuffer byteBuffer = ByteBuffer.allocateDirect(numBytes);
			ByteBuffer byteBuffer = getByteBuffer(numBytes);
			int numRead = getSamplesFF(id, fromTime, toTime, byteBuffer);
			curMediaPosition = toTime + getSampleBufferDurationSec();// an approximation of the current read position of the source reader
			byte[] ba = new byte[numRead];
			byteBuffer.get(ba, 0, numRead);
			
			return ba;
		} finally {
			bufferLock.unlock();
		}
	}

	@Override
	public byte[] getSample(double forTime) {
		if (id == 0) {
			return null;
		}
		
		if (useByteArray) {
			return getSampleByteArray(forTime, (int) getSampleBufferSize());
		}
		
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect((int) getSampleBufferSize());
		
		int numBytes = getSampleFF(id, forTime, byteBuffer);
		curMediaPosition = forTime + getSampleBufferDurationSec();
		
		byte[] ba = new byte[numBytes];
		byteBuffer.get(ba, 0, numBytes);
		return ba;
	}

	@Override
	public double getPositionSec() {
		return curMediaPosition;
	}

	@Override
	public void setPositionSec(double seekPositionSec) {
		if (id == 0) {
			return;
		}
		curMediaPosition = seekPositionSec;
		setPositionSecFF(id, seekPositionSec);
	}

	@Override
	public void release() {
		if (id > 0) {
			releaseFF(id);
		}
	}
	
	/**
	 * Checks if the current buffer is {@code null} or is smaller than the
	 * requested buffer size. Creates a new buffer in both cases and 
	 * returns the current buffer in all other cases.
	 * 
	 * @param minSize the minimal size for the buffer
	 * @return the current or a new new buffer
	 */
	private ByteBuffer getByteBuffer(int minSize) {
		if (curByteBuffer == null) {
			if (minSize > curBufferSize) {
				curBufferSize = minSize;
			}
			curByteBuffer = ByteBuffer.allocateDirect(curBufferSize);
			return curByteBuffer;
		} else {
			if (minSize > curBufferSize) {
				curBufferSize = minSize;
				// the old buffer is dereferenced and can be garbage collected
				curByteBuffer = ByteBuffer.allocateDirect(curBufferSize);
			} else {
				curByteBuffer.clear();
			}
			return curByteBuffer;
		}
	}

	/**
	 * Retrieves audio samples using a {@code byte[]} instead of a 
	 * {@code ByteBuffer}. The array is created here and passed to the native
	 * library to be filled with the audio bytes.
	 *   
	 * @param fromTime the time of the first sample in seconds
	 * @param toTime the time of the last sample in seconds
	 * @param minBufSize the minimum required size of the array
	 * 
	 * @return an array containing the decoded samples for the specified interval
	 */
	private synchronized byte[] getSamplesByteArray(double fromTime, double toTime, int minBufSize) {
		byte[] byteArray = new byte[minBufSize];
		int numRead = getSamplesFFBA(id, fromTime, toTime, byteArray);
		curMediaPosition = toTime + getSampleBufferDurationSec();// an approximation of the current read position of the source reader
		// maybe do this only if there is a big difference in size?
		if (minBufSize - numRead > 2 * sampleBufferSize) {
			return Arrays.copyOfRange(byteArray, 0, numRead);
		} else {
			return byteArray;
		}
		
	}
	
	/**
	 * Retrieves the audio sample for the specified time plus the following 
	 * samples that fit in the default native buffer. Uses a {@code byte[]} 
	 * instead of a {@code ByteBuffer}. The array is created here and passed
	 * to the native library to be filled with the audio bytes.
	 *   
	 * @param forTime the time to get the sample for
	 * @param minBufSize the minimum required size of the array
	 * 
	 * @return an array of bytes, starting with the sample at the requested time
	 */
	private synchronized byte[] getSampleByteArray(double forTime, int minBufSize) {
		byte[] byteArray = new byte[minBufSize];
		getSampleFFBA(id, forTime, byteArray);
		curMediaPosition = forTime + getSampleBufferDurationSec();// an approximation of the current read position of the source reader

		return byteArray;
	}
	
	// global native debug setting
	/**
	 * Sets the debug mode.
	 * 
	 * @param debugMode if {@code true} native debug messages are activated
	 */
	public static native void setDebugMode(boolean debugMode);
	/**
	 * Returns whether native debugging is active.
	 * 
	 * @return {@code true} if native debug mode is active
	 */
	public static native boolean isDebugMode();
	/**
	 * Tells the native counterpart which class and method to use for
	 * logging messages to the Java logger.
	 * 
	 * @param clDescriptor the class descriptor, 
	 * 		e.g. {@code nl/mpi/jni/NativeLog}
	 * 	   
	 * @param methodName the name of the {@code static void} method to call, 
	 * e.g. {@code nlog}, a method which accepts one {@code String}
	 */
	public static native void initLog(String clDescriptor, String methodName);
	
	// could use the same method names as in the super class, but this can 
	// maybe reduce confusion
	private native long initNativeFF(String mediaPath);
	private native int getSampleFrequencyFF(long id);
	private native int getBitsPerSampleFF(long id);
	private native int getNumberOfChannelsFF(long id);
	private native int getFormatTagFF(long id);
	private native long getDurationMsFF(long id);
	private native double getDurationSecFF(long id);
	private native long getSampleBufferSizeFF(long id);
	private native long getSampleBufferDurationMsFF(long id);
	private native double getSampleBufferDurationSecFF(long id);
	private native int getSamplesFF(long id, double fromTime, double toTime, ByteBuffer buffer);
	private native int getSamplesFFBA(long id, double fromTime, double toTime, byte[] byteArray);
	private native int getSampleFF(long id, double fromTime, ByteBuffer buffer);
	private native int getSampleFFBA(long id, double fromTime, byte[] byteArray);
	private native boolean setPositionSecFF(long id, double seekTime);
	private native void releaseFF(long id);
}
