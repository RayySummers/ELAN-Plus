package nl.mpi.jmmf;

/**
 * A class to hold header information for (the data of) a Device Independent Bitmap structure.
 * Analogous to BITMAPINFOHEADER and used in JNI calls to Microsoft Media Foundation objects.
 * 
 * @author Han Sloetjes
 */
public class DIBInfoHeader {
	/**
	 * Creates a new header instance.
	 */
	public DIBInfoHeader() {
		super();
	}
	
	/** the number of bytes required by the header structure */
	public long size;
	/** bitmap width in pixels */
	public int width;
	/** bitmap height in pixels */
	public int height;
	/** number of planes */
	public int planes;
	/** number of bits per pixel */
	public int bitCount;
	/** compression type */
	public int compression;
	/** image size in bytes */
	public long sizeImage;
	/** the horizontal resolution, in pixels per meter */
	public long xPelsPerMeter;
	/** the vertical resolution, in pixels per meter */
	public long yPelsPerMeter;
	/** the number of color indices in the color table */
	public boolean clrUsed;
	/** the number of color indices that are considered important */
	public boolean clrImportant;
}
