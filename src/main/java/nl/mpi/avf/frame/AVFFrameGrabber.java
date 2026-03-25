package nl.mpi.avf.frame;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class for retrieving an image of a video frame or the bytes of a (decoded) video frame
 * or for saving an image using native code. 
 * The native code is based on the macOS AVFoundation.
 *  
 * @author Han Sloetjes
 */
public class AVFFrameGrabber {
	private final static Logger LOG = Logger.getLogger("NativeLogger");
	static boolean nativeLibLoaded = false;
	static boolean nativeLogLoaded = false;
	private static boolean isAarch64 = false;
	
	static {
		try {
			System.loadLibrary("JNIUtil");
			nativeLogLoaded = true;
		} catch (UnsatisfiedLinkError ule) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "Could not load native utility library (libJNIUtil.dylib): " + ule.getMessage());
			}
		} catch (Throwable t) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "Could not load native utility library (libJNIUtil.dylib): " + t.getMessage());
			}
		}
		try {
			// load libAVFFrameGrabber.dylib
			System.loadLibrary("AVFFrameGrabber");
			nativeLibLoaded = true;
		} catch (SecurityException se) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "Could not load native library (libAVFFrameGrabber.dylib): " + se.getMessage());
			}
		} catch (UnsatisfiedLinkError ule) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "Could not load native library (libAVFFrameGrabber.dylib): " + ule.getMessage());
			}
		} catch (Throwable t) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "Could not load native library (libAVFFrameGrabber.dylib): " + t.getMessage());
			}
		}

		// configure native logging
		if (nativeLogLoaded && nativeLibLoaded) {
			try {
				AVFFrameGrabber.initLog("nl/mpi/jni/NativeLogger", "nlog");
			} catch (Throwable thr) {
				if (LOG.isLoggable(Level.WARNING)) {
					LOG.log(Level.WARNING, "Error while configuring native logging: " + thr.getMessage());
				}
			}
		}
		
		String osArch = System.getProperty("os.arch");
		if (osArch != null && osArch.length() > 0) {
			// Apple Silicon or M1 identifies as "aarch64" (in XCode and elsewhere as "arm64")
			// Intel architecture identifies as "x86_64"
			if (osArch.toLowerCase().matches("a.+64")) {
				isAarch64 = true;
			}
		}
	}
	
	private String mediaPath;
	private long id;
	private boolean fieldsInited = false;
	// the following fields are given a value in native code, a change in field name
	// should be combined with a change in the native code
	private int numBytesPerFrame;
	private int numBytesPerRow;
	private int numBitsPerPixel;
	// component meaning R, G, B and/or A
	private int numBitsPerPixelComponent;
	private int imageWidth;
	private int imageHeight;
	private String colorModelCG;
	private String alphaInfo;
	private String bitmapInfo;
	private int videoWidth;
	private int videoHeight;
	private long videoDuration;
	
	/** for use with possibly repeated calls to {@link #getVideoFrameImage(long)} */
	private ByteBuffer byteBuffer;
	private ColorModel colorModel;
//	private WritableRaster raster;
	private byte[] byteArray;
	private boolean useByteArray = false;// use of ByteBuffer is the default 
	private static boolean debugMode = false; 
	
	/**
	 * Constructor, initializes a native AVFoundation Asset for the specified
	 * media (video) file. The native asset is stored in memory for repeated  
	 * frame grabbing without requiring initialization each time.
	 * 
	 * @param mediaPath the path or url of the video file
	 */
	public AVFFrameGrabber(String mediaPath) {
		this.mediaPath = mediaPath;
		if (!nativeLibLoaded) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "The AVFoundation based FrameGrabber library could not be loaded");
			}
			return;
		}
		if (mediaPath.startsWith("file:///")) {
			this.mediaPath = mediaPath.substring(5);
		}
		// try to convert the (URL) string to a path string 
		/*
		try {
			URI mediaURI = new URI(this.mediaPath);
			this.mediaPath = mediaURI.getPath();
		} catch (URISyntaxException use) {
			if (LOG.isLoggable(Level.FINE)) {
				LOG.fine("Cannot create URI for path:  " + use.getMessage());
			}
		}
		*/
		id = initNativeAsset(this.mediaPath);
		
		// test some fields 
		if (id > 0 /*&& videoWidth > 0*/) {
			fieldsInited = true;
		} else {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.warning("The AVFoundation based FrameGrabber could not be initialized");
			}
		}
	}
	
	/**
	 * Creates and returns an image object for the specified time.
	 * 
	 * @param sampleTime the time to get the image for
	 * @return a BufferedImage of the natural size of the video frame, or null if 
	 * an error occurred
	 */
	public synchronized BufferedImage getVideoFrameImage(long sampleTime) {
		if (!fieldsInited) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.warning("No Image: the FrameGrabber was not correctly initialized");
			}
			return null;
		}
		//long time = System.currentTimeMillis();
		if(numBytesPerFrame == 0) {
			numBytesPerFrame = numBytesPerRow * imageHeight;
		}
		int numBytes = 0;
		
		if (useByteArray) {
			if (byteArray == null) {
				byteArray = new byte[numBytesPerFrame];
			}
			numBytes = grabVideoFrameBA(id, sampleTime, byteArray);
		} else {
			if (byteBuffer == null) {
				byteBuffer = ByteBuffer.allocateDirect(numBytesPerFrame);
			} else {
				//reset position to 0
				byteBuffer.position(0);
			}
			numBytes = grabVideoFrame(id, sampleTime, byteBuffer);
		}
 
		if (numBytes > 0) {
			try {
				byte[] dataBytes = null;
				if (useByteArray) {
					dataBytes = byteArray;
				} else {
					if (byteBuffer.hasArray()) {// returns false
						dataBytes = byteBuffer.array();
					} else {
						// this repeated creation of a new byte array seems to be non-optimal 
						dataBytes = new byte[numBytes];
						byteBuffer.get(dataBytes, 0, numBytes);
					}
				}				
				
				// maybe the raster can be reused after creation by calling 
				// setDataElements(int x, int y, int w, int h, Object inData)
				// this seems to result in scrambled images
				//raster.setDataElements(0, 0, imageWidth, imageHeight, dataBytes);
				DataBufferByte dataBufferByte = new DataBufferByte(dataBytes, numBytes);
				WritableRaster raster = Raster.createInterleavedRaster(dataBufferByte, 
					imageWidth, imageHeight, numBytesPerRow, numBitsPerPixel / numBitsPerPixelComponent, 
					getBandOffsets(), null);
				
				if (colorModel == null) {
					colorModel = createCorrespondingColorModel();
				}
				//System.out.println("T: " +(System.currentTimeMillis() - time));
				/*BufferedImage img =*/ return new BufferedImage(colorModel, raster, true, null);
			} catch (Throwable t) {
				if (LOG.isLoggable(Level.WARNING)) {
					LOG.warning("Error converting frame bytes to Java image: " + t.getMessage());
				}
				//t.printStackTrace();
			}
		} else {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.warning("Error retrieving frame bytes from the AVFoundation image generator ");
			}
		}
		
		return null;
	}
	
	/**
	 * The actual ColorModel to create should depend on the characteristics of the native CGImage
	 * @return a color model that corresponds to or is compatible with the properties of the 
	 * CoreGraphics CGImage
	 */
	private ColorModel createCorrespondingColorModel() {
		// especially the color space, the hasAlpha and isAlphaPremultiplied parameters should 
		// depend on the properties of the native image 
		ComponentColorModel comModel = new ComponentColorModel(
				ColorSpace.getInstance(ColorSpace.CS_sRGB), 
				true, true, // hasAlpha, isAlphaPremultiplied
				Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
		return comModel;
	}
	
	/**
	 * Note: the actual band offsets should be determined by the colorModelCG 
	 * and/or the alphaInfo.
	 * 
	 * @return the band offsets for interpreting the bytes array
	 */
	private int[] getBandOffsets() {
		if (!isAarch64) {
			return new int[]{1, 2, 3, 0};//original: x86_64
		} else {
			return new int[]{2, 1, 0, 3};
		}
		
		//return new int[]{1, 2, 0, 3};
		//return new int[]{1, 3, 2, 0};
		//return new int[]{1, 3, 0, 2};
		//return new int[]{1, 0, 2, 3};
		//return new int[]{1, 0, 3, 2};
		
		//return new int[]{2, 1, 0, 3};// <== arm64/aarch64
		//return new int[]{2, 1, 3, 0};
		//return new int[]{2, 0, 1, 3};
		//return new int[]{2, 0, 3, 1};
		//return new int[]{2, 3, 0, 1};
		//return new int[]{2, 3, 1, 0};
		
		//return new int[]{3, 1, 0, 2};
		//return new int[]{3, 1, 2, 0};
		//return new int[]{3, 2, 0, 1};
		//return new int[]{3, 2, 1, 0};
		//return new int[]{3, 0, 1, 2};
		//return new int[]{3, 0, 2, 1};
		
		//return new int[]{0, 1, 2, 3};
		//return new int[]{0, 1, 3, 2};
		//return new int[]{0, 2, 1, 3};
		//return new int[]{0, 2, 3, 1};
		//return new int[]{0, 3, 1, 2};
		//return new int[]{0, 3, 2, 1};	
	}

	/**
	 * Grabs the bytes of a video frame and stores them in the ByteBuffer, shared between 
	 * Java and the native interface.
	 * 
	 * @param sampleTime the time to get the frame for
	 * @param buffer the buffer to copy the bytes into, the buffer must be of sufficient size
	 * @return the actual number of copied bytes
	 */
	public int grabVideoFrame(long sampleTime, ByteBuffer buffer) {
		if (!fieldsInited) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.warning("No pixel bytes: the FrameGrabber was not correctly initialized");
			}
			return 0;
		}
		
		return grabVideoFrame(id, sampleTime, buffer);
	}
	
	/**
	 * Grabs the bytes of a video frame and stores them in the byte[].
	 * 
	 * @param sampleTime the time to get the frame for
	 * @param byteArray the byte array to copy the bytes into, the array must be of sufficient size
	 * @return the actual number of copied bytes
	 */
	public int grabVideoFrameByteArray(long sampleTime, byte[] byteArray) {
		if (!fieldsInited) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.warning("No pixel bytes: the FrameGrabber was not correctly initialized");
			}
			return 0;
		}
		
		return grabVideoFrameBA(id, sampleTime, byteArray);
	}
	
	/**
	 * Saves the video frame in the specified location using native code. If the file 
	 * already exists it will be overwritten without warning.
	 * 
	 * @param imageURL the location to store the image. The type of the image is based on
	 * the file extension, .png in case of unknown or unsupported image format. Other 
	 * supported formats are .jpg and .bmp.
	 * @param sampleTime the time for which to save the frame image
	 * 
	 * @return true if the file was saved successfully, false otherwise
	 */
	public boolean saveFrameNativeAVF(String imageURL, long sampleTime) {
		if (!fieldsInited) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.warning("Could not save the image: the FrameGrabber was not correctly initialized");
			}
			return false;
		}
		
		return saveFrameNativeAVF(id, imageURL, sampleTime);
	}
	
	/**
	 * Releases the native resources from memory when done with this class.
	 */
	public void release() {
		if (id > 0) {
			release(id);
		}		
	}
	
	/**
	 * Enables or disables debug level log messages.
	 *  
	 * @param enable if {@code true} debug level messages will be logged
	 */
	public static void enableDebugMode(boolean enable) {
		debugMode = enable;
		setDebugMode(debugMode);
	}

	/**
	 * Informs the native counterpart of the class and the method to use for
	 * logging messages to the Java logger.
	 * 
	 * @param clDescriptor the class descriptor, 
	 * 		e.g. {@code nl/mpi/jni/NativeLog}
	 * 	   
	 * @param methodName the name of the {@code static void} method to call, 
	 * e.g. {@code nlog}, a method which accepts one {@code String}
	 */
	static native void initLog(String clDescriptor, String methodName);
	
	/**
	 * Initializes the native Asset and ImageGenerator and generates an id for it.
	 * @param mediaPath the path or url of the video
	 * @return an id which serves as a key for storing and retrieving the generator 
	 * in and from a map or dictionary.
	 */
	private native long initNativeAsset(String mediaPath);
	
	private native int grabVideoFrame(long id, long sampleTime, ByteBuffer buffer);
	
	private native int grabVideoFrameBA(long id, long sampleTime, byte[] byteArray);
	
	private native boolean saveFrameNativeAVF(long id, String imageURL, long sampleTime);
	
	private native void release(long id);
	
	private static native void setDebugMode(boolean debugMode); 

	// getters for details of the image if the caller loads the bytes into a ByteBuffer
	/**
	 * Returns the number of bytes per frame.
	 * 
	 * @return the number of bytes necessary for storing the pixels of a single video frame
	 */
	public int getNumBytesPerFrame() {
		return numBytesPerFrame;
	}

	/**
	 * Returns the number of bytes per pixel row.
	 * 
	 * @return the number of bytes per row 
	 */
	public int getNumBytesPerRow() {
		return numBytesPerRow;
	}

	/**
	 * Returns the number of bits per pixel.
	 * 
	 * @return the number of bits necessary for storing a single pixel
	 */
	public int getNumBitsPerPixel() {
		return numBitsPerPixel;
	}

	/**
	 * Returns the number of bits per pixel component (color or alpha component).
	 * 
	 * @return the number of bits per pixel component, e.g. the R (or G or B etc.) component
	 */
	public int getNumBitsPerPixelComponent() {
		return numBitsPerPixelComponent;
	}

	/**
	 * Returns the image width in pixels.
	 * 
	 * @return the width in pixels of a frame image, this can be different from the videoWidth 
	 */
	public int getImageWidth() {
		return imageWidth;
	}

	/**
	 * Returns the image height in pixels.
	 * 
	 * @return the height in pixels of a frame image, this can be different from the videoHeight 
	 */
	public int getImageHeight() {
		return imageHeight;
	}

	/**
	 * Returns the Core Graphics color model.
	 * @return a String representation of the color model of the image, one of the constants of
	 * CGColorSpaceModel, part of the CoreGraphics Framework
	 */
	public String getColorModelCG() {
		return colorModelCG;
	}

	/**
	 * Returns the width of the video.
	 * 
	 * @return the width of the video, can be different from the decoded frame image width
	 */
	public int getVideoWidth() {
		return videoWidth;
	}

	/**
	 * Returns the height of the video.
	 * 
	 * @return the height of the video, can be different from the decoded frame image height
	 */
	public int getVideoHeight() {
		return videoHeight;
	}

	/**
	 * Returns the video duration in milliseconds.
	 * 
	 * @return the duration of the video in milliseconds
	 */
	public long getVideoDuration() {
		return videoDuration;
	}

	/**
	 * Returns whether {@code byte} array is preferred over the use of 
	 * {@code ByteBuffer} when retrieving the image bytes.
	 * 
	 * @return whether or not a byte array should be used when grabbing an image,
	 * instead of a nio.ByteBuffer 
	 * 
	 * @see #getVideoFrameImage(long)
	 */
	public boolean isUseByteArray() {
		return useByteArray;
	}

	/**
	 * Sets the flag that determines whether a nio.ByteBuffer is used to receive the pixels
	 * from the native AVFoundation or a byte array
	 * 
	 * @param useByteArray the new value of this flag
	 * @see #getVideoFrameImage(long)
	 */
	public void setUseByteArray(boolean useByteArray) {
		this.useByteArray = useByteArray;
	}
	
}
