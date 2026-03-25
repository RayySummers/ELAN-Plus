package nl.mpi.media.spectrogram;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferDouble;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;

/**
 * Utility class for spectrogram image creation. The process depends on several
 * user supplied preference settings and, possibly, execution time dependent 
 * considerations.<p>
 * This class stores state and should only be used by a single viewer/displayer
 * instance. 
 * 
 * @author Han Sloetjes
 *
 */
public class ImageCreator {
	private SpectrogramSettings settings;
	private ColorModel directColorModel;
	private ColorModel comColorModel;
	
	//## variables and methods for rgb, 3 bank, double buffer images ##
	// rgb components for Color 1
	double cr1 = 0.0d, cg1 = 0d, cb1 = 0d;
	// rgb components for Color 2
	double cr2 = 1d, cg2 = 1.0d, cb2 = 1.0d;
	// the extent per color component
	double cre = Math.abs(cr1 - cr2), cge = Math.abs(cg1 - cg2), cbe = Math.abs(cb1 -cb2);
	//## variables for 1 bank (packed pixel), rgb, int buffer images #####
	// color masks, based on default ARGB order
	static final int AM = 0xff000000;
	static final int RM = 0x00ff0000;// 0xff0000
	static final int GM = 0x0000ff00;// 0xff00
	static final int BM = 0x000000ff;// 0xff
	// the actual order used for the pixel samples
	int[] masks = new int[]{RM, GM, BM};
	
	private int[] lut = null;
	private Color col1 = Color.WHITE;
	private Color col2 = Color.BLACK;
	
	// minimal percentage of overlap when mapping frequency bins to pixels 
	private double minOv = 0.4;
	
	/**
	 * 	Constructor, creates a settings object with default settings.
	 */
	public ImageCreator() {
		settings = new SpectrogramSettings();
		initialize();
	}

	/**
	 * Constructor.
	 * 
	 * @param settings the settings instance to use
	 */
	public ImageCreator(SpectrogramSettings settings) {
		super();
		if (settings != null) {
			this.settings = settings;
		} else {
			this.settings = new SpectrogramSettings();
		}
		initialize();
	}
	
	/**
	 * Creates a few re-usable data structures.
	 */
	private void initialize() {
		directColorModel = new DirectColorModel(
				ColorSpace.getInstance(ColorSpace.CS_sRGB), 24, // 24 bits is sufficient
				masks[0], masks[1], masks[2], 
				0,// 0 for alpha means no transparency
			    true, // isAlphaPremultiplied
				DataBuffer.TYPE_INT);
		comColorModel = new ComponentColorModel(
				ColorSpace.getInstance(ColorSpace.CS_sRGB), 
				false, false, // hasAlpha, isAlphaPremultiplied
				Transparency.OPAQUE, DataBuffer.TYPE_DOUBLE);
		// maybe sample models can be re-used?
		// maybe data buffers can be re-used?
		// maybe rasters can be re-used?
	}

	/**
	 * Returns the {@code SpectrogramSettings}.
	 * 
	 * @return the settings instance
	 */
	public SpectrogramSettings getSettings() {
		return settings;
	}
	
	/**
	 * Creates a lookup table based on two colors, foreground and background.
	 * The transition between the 2 colors is performed via conversion to 
	 * HSV and back again. The shortest 'Hue' route from background to 
	 * foreground is used at the moment.
	 */
	private void updateColorMap( ) {
		boolean updateMap = false;
		if (col1 == null || !col1.equals(settings.getColor1())) {
			if (settings.getColor1() != null) {
				col1 = settings.getColor1();
				updateMap = true;
			}
		}
		if (col2 == null || !col2.equals(settings.getColor2())) {
			if (settings.getColor2() != null) {
				col2 = settings.getColor2();
				updateMap = true;
			}	
		}
		if (!updateMap && lut == null) {
			return;
		}
		
		int size = 256 * 256;
		lut = new int[size];
		float[] hsv1 = Color.RGBtoHSB(col1.getRed(), col1.getGreen(), col1.getBlue(), null);
		float[] hsv2 = Color.RGBtoHSB(col2.getRed(), col2.getGreen(), col2.getBlue(), null);
		float satRange = Math.abs(hsv1[1] - hsv2[1]);
		float satStep = hsv2[1] < hsv1[1] ? satRange / size : -(satRange / size);
		float valRange = Math.abs(hsv1[2] - hsv2[2]);
		float valStep = hsv2[2] < hsv1[2] ? valRange / size : -(valRange / size);
		// for hue try to find the shortest route from foreground to background
		float hueRange = Math.abs(hsv1[0] - hsv2[0]);
		float hueStep = hsv2[0] < hsv1[0] ? hueRange / size : -(hueRange / size);
		if (hueRange > 0.5f) {// taking the shortest direction along the color circle
			float revHueRange = 1 - hueRange;
			if (hsv1[0] > hsv2[0]) {
				hsv2[0] = hsv2[0] + 1;
				hueStep = -(revHueRange / size);
			} else {
				hsv1[0] = hsv1[0] + 1;
				hueStep = revHueRange / size;
			}
		}
		
		// stepping from background color (0) to foreground color (1)
		for (int i = 0; i < size; i++) {
			lut[i] = Color.HSBtoRGB(hsv2[0] + (i * hueStep), hsv2[1] + (i * satStep), hsv2[2] + (i * valStep));
		}
	}
	
	/**
	 * Creates a spectrogram image based on the provided frequency windows and
	 * dimensions, taking into account the settings stored in the shared 
	 * {@link SpectrogramSettings} instance.
	 *  
	 * @param freqWindows the frequency windows
	 * @param imageWidth the width of the image
	 * @param imageHeight the height of the image
	 * @param renderPerf currently there is no difference here in optimization
	 * for speed or quality
	 * 
	 * @return a new image or {@code null}
	 */
	public BufferedImage createSpecImage(double[][] freqWindows, int imageWidth, int imageHeight, 
			SpectrogramSettings.PERFORMANCE renderPerf) {
		switch (renderPerf) {
		case SPEED:
			// the AWT method delivers slightly different layout/size and, as is, sometimes
			// causes incomplete rendering. Could switch to BufferedImage altogether
			// and maybe ignore adaptive rendering when speed is required?
			//return createSpectrogramImageAWT(freqWindows, imageWidth, imageHeight);

			return createSpectrogramImageSmooth(freqWindows, imageWidth, imageHeight);
		case QUALITY:
			return createSpectrogramImageSmooth(freqWindows, imageWidth, imageHeight);
		}
		return null;
	}
	
	/**
	 * Creates a spectrogram image with two separate channels based on the provided 
	 * frequency windows and dimensions, taking into account the settings stored in
	 * the shared {@link SpectrogramSettings} instance.
	 *  
	 * @param freqWindows the frequency windows, an array of size two
	 * @param imageWidth the width of the image
	 * @param imageHeight the height of the image
	 * @param renderPerf currently there is no difference here in optimization
	 * for speed or quality
	 * 
	 * @return a new image or {@code null}
	 */
	public BufferedImage createStereoSpecImage(double[][][] freqWindows, int imageWidth, int imageHeight, 
			SpectrogramSettings.PERFORMANCE renderPerf) {
		if (freqWindows.length == 1) {
			return createSpecImage(freqWindows[0], imageWidth, imageHeight, renderPerf);
		}
		
		switch (renderPerf) {
		case SPEED:
			// fall through, currently there's only one implementation
		case QUALITY:
			// fall  through
			default:
			int topH = (int) Math.ceil(imageHeight / 2.0d);
			int bottomH = imageHeight - topH;
			BufferedImage topBI = createSpectrogramImageSmooth(freqWindows[0], imageWidth, topH);
			BufferedImage bottomBI = createSpectrogramImageSmooth(freqWindows[1], imageWidth, bottomH);
			BufferedImage combinedBI = new BufferedImage(imageWidth, imageHeight, topBI.getType());
			// there might be better ways to combine the images 
			Graphics2D g2d = combinedBI.createGraphics();
			g2d.drawImage(topBI, 0, 0, null);
			g2d.drawImage(bottomBI, 0, topH, null);
			
			return combinedBI;
		}
	}
	
//############## DataBufferDouble #################################	
//## for rgb, 3-bank, double buffer images ##	
	// this method has not been updated and is not used at the moment
	@SuppressWarnings("unused")
	private BufferedImage createSpectrogram2(double[][] freqWindows, int posMaxFreq, int maxDisplayFreq) {
		int numBins = freqWindows[0].length;
		int freqsPerBin = posMaxFreq / numBins;
		System.out.println(String.format("Image: freqPerBin: %d, num bins: %d, pos max freq: %d, req max: %d", 
				freqsPerBin, numBins, posMaxFreq, maxDisplayFreq));
		int h = maxDisplayFreq / freqsPerBin;// start with one pixel per bin
		int w = freqWindows.length;// start with one pixel per 'window'
		System.out.println(String.format("Image: w: %d, h: %d, num pixels: %d", w, h, (w*h)));
		// scanline stride in data buffers is row-based (a scanline is a row of pixels), 
		// the origin of an image, (0, 0) is the left top
		// our input is column oriented, the origin is at the left bottom

		// determine the max and min value in visible area
		double min = Double.MAX_VALUE;
		double max = 0.0d;
		for (int i = 0; i < freqWindows.length; i++) {
			for (int j = 0; j < h; j++) {
				double d = freqWindows[i][j];
				if (d < min) min = d;
				if (d > max) max = d;
			}
		}

		double valRange = max - min;
		double absMin = min < 0 ? -min : min;
		System.out.println(String.format("Image range: min: %.6f, max: %.6f, range: %.6f", min, max, valRange));
		DataBufferDouble dbd = new DataBufferDouble(w * h, 3);
		double[] rdata = dbd.getData(0);
		double[] gdata = dbd.getData(1);
		double[] bdata = dbd.getData(2);
		// contrast correction 1
//		double c = 0.6d; // change this for variation
//		double cf = (1.05d * c) / (1.05d - c);
		
		// linear transform
//		double alpha = 2.0d; // 0 < a < 1 decrease, a > 1 increase contrast 
//		double beta = -0.2d; // 0 < b < 1 increase brightness
		
		for (int i = 0; i < w; i++) {
			// i corresponds to the column of the pixel
			double[] col = freqWindows[i];
			for (int j = 0; j < h; j++) {
				// j corresponds to the row of a pixel, starting at the bottom (y = h)
				//double d = (col[j] / EXT);
				double d = 1 - (absMin + col[j]) / valRange;
//				d = Math.sin(Math.PI /3 * d);
//				d = 1 - d;
				//d = cf * (d - 0.5d) + 0.5d;
				//d = alpha * d + beta; // linear contrast adjustment
				// d should be between 0 and 1
				if (d > 1) d = 1.0d;
				else if (d < 0) d = 0.0d;
				
				//data[i + (h - 1 - j) * w] = d;
				rdata[i + (h - 1 - j) * w] = cr1 >= cr2 ? cr1 - (cre * d) : cr1 + (cre * d);
				gdata[i + (h - 1 - j) * w] = cg1 >= cg2 ? cg1 - (cge * d) : cg1 + (cge * d);
				bdata[i + (h - 1 - j) * w] = cb1 >= cb2 ? cb1 - (cbe * d) : cb1 + (cbe * d);
			}
		}
		
		ColorModel colorModel = new ComponentColorModel(
				ColorSpace.getInstance(ColorSpace.CS_sRGB), 
				false, false, // hasAlpha, isAlphaPremultiplied
				Transparency.OPAQUE, DataBuffer.TYPE_DOUBLE);
		BandedSampleModel sampleModel = new BandedSampleModel(DataBuffer.TYPE_DOUBLE, w, h, 3);
		WritableRaster raster = Raster.createWritableRaster(sampleModel, dbd, null);
		
		// the native library of this transform op fails to load (on windows)
//		AffineTransformOp scaleOp = new AffineTransformOp(AffineTransform.getScaleInstance(3.0, 3.0), 
//				AffineTransformOp.TYPE_BICUBIC);
//		BufferedImage bi1 = new BufferedImage(colorModel, grayRaster, true, null);
//		BufferedImage bi2 = scaleOp.createCompatibleDestImage(bi1, colorModel);
//		scaleOp.filter(bi1, bi2);
//		return bi2;
//		return scaleOp.filter(new BufferedImage(colorModel, grayRaster, true, null), null);
		return new BufferedImage(colorModel, raster, true, null);
	}
//############## end DataBufferDouble ################################

//############## DataBufferInt #######################################
//## for 1 bank (packed pixel), rgb, int buffer and model images #####
	/**
	 * Creates a spectrogram image for the specified frequency data and image
	 * width and height. Depending on the ratio between number of windows and
	 * the width of the image and between number of bins and the height of the
	 * image, it forwards to a specialized version that performs some form of
	 * interpolation while scaling up or down. Settings in the shared
	 * {@link SpectrogramSettings} instance apply.
	 * 
	 * @param freqWindows the frequency windows
	 * @param imageWidth the width of the image in pixels
	 * @param imageHeight the height of the image in pixels
	 * 
	 * @return a new image or {@code null}
	 */
	public BufferedImage createSpectrogramImageSmooth(double[][] freqWindows, /*double minFreq, double posMaxFreq,
			double msPerColumn, double msPerPixel, */
			int imageWidth, int imageHeight) {
		double maxDisplayFreq = settings.getMaxDisplayFrequency();
		double minDisplayFreq = settings.getMinDisplayFrequency();
		double posMaxFreq = settings.getPossibleMaxFrequency();
		int numBins = freqWindows[0].length;
		double freqsPerBin = posMaxFreq / numBins;
		int minBin = (int) (minDisplayFreq / freqsPerBin);// index
		int maxBin = (int) Math.ceil(maxDisplayFreq / freqsPerBin);// index, rounded up
		maxBin = maxBin >= numBins ? numBins - 1 : maxBin;
		int numDisplayBins = maxBin - minBin + 1;
		// no scaling required
		if (imageWidth == freqWindows.length && imageHeight == numDisplayBins) {
			return createSpectrogramImageNoScaling(freqWindows, imageWidth, imageHeight);
		}
		boolean vertScaleUp = imageHeight > numDisplayBins;
		boolean horScaleUp = settings.getStrideDurationSec() > settings.getPixelDurationSec();
		
		if (horScaleUp) {
			if (vertScaleUp) {
				return createSpectrogramImageLinearHV(freqWindows, imageWidth, imageHeight);
			} else {
				return createSpectrogramImageLinearH(freqWindows, imageWidth, imageHeight);
			}
		} else {
			if (vertScaleUp) {
				return createSpectrogramImageLinearV(freqWindows, imageWidth, imageHeight);
			} else {
				return createSpectrogramImage(freqWindows, imageWidth, imageHeight);
			}
		}
	}
	
	
	private BufferedImage createSpectrogramImage(double[][] freqWindows,  
			int imageWidth, int imageHeight) {
		// check the range of frequencies that have to be displayed, the number of frequencies per bin
		// and the ratio between those values. Based on that, bin values might need to be concatenated
		// or repeated in pixels 'vertically'.
		
		double maxDisplayFreq = settings.getMaxDisplayFrequency();
		double minDisplayFreq = settings.getMinDisplayFrequency();
		double posMaxFreq = settings.getPossibleMaxFrequency();
		int numBins = freqWindows[0].length;
		double freqsPerBin = posMaxFreq / numBins;
		int minBin = (int) (minDisplayFreq / freqsPerBin);
		int maxBin = (int) Math.ceil(maxDisplayFreq / freqsPerBin);// index, rounded up
		maxBin = maxBin >= numBins ? numBins - 1 : maxBin;
		int numDisplayBins = maxBin - minBin + 1;
		// the value range can either be given, via settings or detected from the passed windows
		double[] minmax = getMinMax(freqWindows, minBin, maxBin);
		double valueRange = minmax[1] - minmax[0];
		// contrast corrections passed via settings, as well as the colors
		
		// the passed image width should be consistent with msperpixel, length of the windows array
		// and the stride duration. Assume it is.
		// create a databuffer
		int w = imageWidth;
		int h = imageHeight;
		// the number of bands depends on colour settings
		//DataBufferInt dataBufferInt = new DataBufferInt(w * h, 1);
		//int[] data = dataBufferInt.getData();
		double[] dar = new double[w * h];

		int[][] xMap = getCorrespondingIndices(w, freqWindows.length, settings.getPixelDurationSec(), 
				settings.getActualStrideDurationSec());
		int[][] yMap = getBinMap(h, numDisplayBins);
		
		for (int i = 0; i < w; i++) {
			// i is the index of a column of pixels in the image, the x coordinate 
			for (int j = 0; j < h; j++) {
				// j is the index of a row of pixels, the y coordinate, 
				// starting at the 'bottom', the lowest frequencies (lowest indices), 
				// corresponding to the pixels at the bottom of the image, with the 
				// highest y coordinate values  
				// which bins contribute to this pixel?
				double value = 0d;
				double binCount = 0;
				// get from xMap and yMap
				int[] xc = xMap[i];
				for (int wi = 0; wi < xc.length; wi++) {
					if (xc[wi] != 0 || wi == 0) {
						int[] yc = yMap[j];
						for (int k = 0; k < yc.length; k++) {
							if (yc[k] != 0 || k == 0) {
								int bindex = yc[k] + minBin;// check against numBin?
								if(xc[wi] < freqWindows.length && freqWindows[xc[wi]] != null &&
										yc[k] < freqWindows[xc[wi]].length ) {// check null	or is this test superfluous?						
									value += freqWindows[xc[wi]][bindex];
									binCount++;
								}
							}
						}
					}
				}
				value /= binCount;//the average value
				value = (value - minmax[0]) / valueRange;// relative value

				dar[i + (h - 1 - j) * w] = value > 1 ? 1 : (value < 0 ? 0 : value);
			}
		}

		return toImage(dar, w, h);
	}

	private BufferedImage createSpectrogramImageLinearV(double[][] freqWindows, 
			int imageWidth, int imageHeight) {
		// check the range of frequencies that have to be displayed, the number of frequencies per bin
		// and the ratio between those values. Based on that, bin values might need to be concatenated
		// or repeated in pixels 'vertically'.
		
		double maxDisplayFreq = settings.getMaxDisplayFrequency();
		double minDisplayFreq = settings.getMinDisplayFrequency();
		double posMaxFreq = settings.getPossibleMaxFrequency();
		int numBins = freqWindows[0].length;
		double freqsPerBin = posMaxFreq / numBins;
		int minBin = (int) (minDisplayFreq / freqsPerBin);// index
		int maxBin = (int) Math.ceil(maxDisplayFreq / freqsPerBin);// index, rounded up
		maxBin = maxBin >= numBins ? numBins - 1 : maxBin;
		int numDisplayBins = maxBin - minBin + 1;
		// the value range can either be given, via settings or detected from the passed windows
		double[] minmax = getMinMax(freqWindows, minBin, maxBin);
		double valueRange = minmax[1] - minmax[0];
		// contrast corrections passed via settings, as well as the colors
		
		// the passed image width should be consistent with msperpixel, length of the windows array
		// and the stride duration. Assume it is.
		// create a databuffer
		int w = imageWidth;
		int h = imageHeight;
		// the number of bands depends on colour settings
		//DataBufferInt dataBufferInt = new DataBufferInt(w * h, 1);
		//int[] data = dataBufferInt.getData();
		double[] dar = new double[w * h];
		
		int[][] xMap = getCorrespondingIndices(w, freqWindows.length, settings.getPixelDurationSec(), 
				settings.getActualStrideDurationSec());
		int[] yMap = getBinMapSizingUp(h, numDisplayBins);
		
		for (int i = 0; i < w; i++) {
			// i is the index of a column of pixels in the image, the x coordinate 
			// get from xMap and yMap
			int[] xc = xMap[i];
			
			int curBinIndex = -1;
			int curPixYIndex = 0;
			double beginValue = 0d;
			double endValue = 0d;
			double yValueStep = 0d;;// interpolation value

			for (int j = 0; j < h; j++) {
				// j is the index of a row of pixels, the y coordinate, 
				// starting at the 'bottom', the lowest frequencies (lowest indices), 
				// corresponding to the pixels at the bottom of the image, with the 
				// highest y coordinate values 
				// which bins contribute to this pixel?
				double value = 0d;
				double binCount = 0;

				for (int wi = 0; wi < xc.length; wi++) {
					if (xc[wi] != 0 || wi == 0) {
						// get from yMap
						int yc = yMap[j] + minBin;// check against numBin?
						beginValue = freqWindows[xc[wi]][yc];
						
						if (yc != curBinIndex) {
							curBinIndex = yc;
							curPixYIndex = j;
							yValueStep = 0d;
							for (int pInd = j + 1; pInd < h; pInd++) {
								if (yMap[pInd] + minBin != curBinIndex) {
									endValue = freqWindows[xc[wi]][yMap[pInd] + minBin];
									yValueStep = (endValue - beginValue) / (pInd - j);
									break;
								}
							}
						} else {
							value += ((j - curPixYIndex) * yValueStep);//increment
						}
						value += beginValue;
						binCount++;
					}
				}
				value /= binCount;//the average value
				value = (value - minmax[0]) / valueRange;// relative value

				dar[i + (h - 1 - j) * w] = value > 1 ? 1 : (value < 0 ? 0 : value);
			}
		}

		return toImage(dar, w, h);
	}
	
	
	private BufferedImage createSpectrogramImageLinearHV(double[][] freqWindows,  
			int imageWidth, int imageHeight) {
		// check the range of frequencies that have to be displayed, the number of frequencies per bin
		// and the ratio between those values. Based on that, bin values might need to be concatenated
		// or repeated in pixels 'vertically'.
		
		double maxDisplayFreq = settings.getMaxDisplayFrequency();
		double minDisplayFreq = settings.getMinDisplayFrequency();
		double posMaxFreq = settings.getPossibleMaxFrequency();
		int numBins = freqWindows[0].length;
		double freqsPerBin = posMaxFreq / numBins;
		int minBin = (int) (minDisplayFreq / freqsPerBin);// index
		int maxBin = (int) Math.ceil(maxDisplayFreq / freqsPerBin);// index, rounded up
		maxBin = maxBin >= numBins ? numBins - 1 : maxBin;
		int numDisplayBins = maxBin - minBin + 1;
		// the value range can either be given, via settings or detected from the passed windows
		double[] minmax = getMinMax(freqWindows, minBin, maxBin);
		double valueRange = minmax[1] - minmax[0];
		// contrast corrections passed via settings, as well as the colors
		
		// the passed image width should be consistent with msperpixel, length of the windows array
		// and the stride duration. Assume it is.
		// create a databuffer
		int w = imageWidth;
		int h = imageHeight;
		// the number of bands depends on colour settings
		//DataBufferInt dataBufferInt = new DataBufferInt(w * h, 1);
		//int[] data = dataBufferInt.getData();
		double[] dar = new double[w * h];
		int[] xMap = getWindowMapSizingUp(w, freqWindows.length, settings.getPixelDurationSec(), 
				settings.getActualStrideDurationSec());
		int[] yMap = getBinMapSizingUp(h, numDisplayBins);

		int curWinIndex = -1;
		int nextWinIndex = 0;
		int curPixXIndex = 0;
		int nextPixXIndex = 0;

		for (int i = 0; i < w; i++) {
			// i is the index of a column of pixels in the image, the x coordinate 
			// get from xMap and yMap
			// xc is the index of the frequency window
			int xc = xMap[i];
			if (xc != curWinIndex) {
				curWinIndex = xc;
				curPixXIndex = i;
				for (int wInd = i + 1; wInd < w; wInd++) {
					if (xMap[wInd] != curWinIndex) {
						nextWinIndex = xMap[wInd];
						nextPixXIndex = wInd;
						break;
					}
				}
			}
			
			int curBinIndex = -1;
			int curPixYIndex = 0;
			double beginValue = 0d;
			double endValue = 0d;
			double yValueStep = 0d;;// interpolation value

			for (int j = 0; j < h; j++) {
				// j is the index of a row of pixels, the y coordinate, 
				// starting at the 'bottom', the lowest frequencies (lowest indices), 
				// corresponding to the pixels at the bottom of the image, with the 
				// highest y coordinate values  
				// which bins contribute to this pixel?
				double value = 0d;
				double binCount = 0;

				// get from yMap
				int yc = yMap[j] + minBin;// check against numBin?
				// get initial value for this pixel
				beginValue = freqWindows[xc][yc];
				// interpolate horizontally
				if (curWinIndex != nextWinIndex) {
					double xValueStep = (freqWindows[nextWinIndex][yc] - freqWindows[curWinIndex][yc]) / 
							(nextPixXIndex - curPixXIndex);
					value += (i - curPixXIndex) * xValueStep;
				}
				
				// interpolate vertically
				if (yc != curBinIndex) {
					curBinIndex = yc;
					curPixYIndex = j;
					yValueStep = 0d;
					for (int pInd = j + 1; pInd < h; pInd++) {
						if (yMap[pInd] + minBin != curBinIndex) {
							endValue = freqWindows[xc][yMap[pInd] + minBin];
							yValueStep = (endValue - beginValue) / (pInd - j);
							break;
						}
					}
				} else {
					value += ((j - curPixYIndex) * yValueStep);//increment
				}
				value += beginValue;
				binCount++;			
				
				value /= binCount;//the average value
				value = (value - minmax[0]) / valueRange;// relative value

				dar[i + (h - 1 - j) * w] = value > 1 ? 1 : (value < 0 ? 0 : value);
			}
		}

		return toImage(dar, w, h);
	}
	
	
	private BufferedImage createSpectrogramImageLinearH(double[][] freqWindows, 
			int imageWidth, int imageHeight) {
		// check the range of frequencies that have to be displayed, the number of frequencies per bin
		// and the ratio between those values. Based on that, bin values might need to be concatenated
		// or repeated in pixels 'vertically'.
		
		double maxDisplayFreq = settings.getMaxDisplayFrequency();
		double minDisplayFreq = settings.getMinDisplayFrequency();
		double posMaxFreq = settings.getPossibleMaxFrequency();
		int numBins = freqWindows[0].length;
		double freqsPerBin = posMaxFreq / numBins;
		int minBin = (int) (minDisplayFreq / freqsPerBin);// index
		int maxBin = (int) Math.ceil(maxDisplayFreq / freqsPerBin);// index, rounded up
		maxBin = maxBin >= numBins ? numBins - 1: maxBin;
		int numDisplayBins = maxBin - minBin + 1;
		// the value range can either be given, via settings or detected from the passed windows
		double[] minmax = getMinMax(freqWindows, minBin, maxBin);
		double valueRange = minmax[1] - minmax[0];
		// contrast corrections passed via settings, as well as the colors
		
		// the passed image width should be consistent with msperpixel, length of the windows array
		// and the stride duration. Assume it is.
		// create a databuffer
		int w = imageWidth;
		int h = imageHeight;
		// the number of bands depends on colour settings
		//DataBufferInt dataBufferInt = new DataBufferInt(w * h, 1);
		//int[] data = dataBufferInt.getData();
		double[] dar = new double[w * h];
		// horizontal and vertical mapping arrays
		int[] xMap = getWindowMapSizingUp(w, freqWindows.length, settings.getPixelDurationSec(), 
				settings.getActualStrideDurationSec());
		int[][] yMap = getBinMap(h, numDisplayBins);

		int curWinIndex = -1;
		int nextWinIndex = 0;
		int curPixXIndex = 0;
		int nextPixXIndex = 0;
		
		for (int i = 0; i < w; i++) {
			// i is the index of a column of pixels in the image, the x coordinate 
			// get from xMap and yMap
			// xc is the index of the frequency window
			int xc = xMap[i];
			if (xc != curWinIndex) {
				curWinIndex = xc;
				curPixXIndex = i;
				for (int wInd = i + 1; wInd < w; wInd++) {
					if (xMap[wInd] != curWinIndex) {
						nextWinIndex = xMap[wInd];
						nextPixXIndex = wInd;
						break;
					}
				}
			}

			for (int j = 0; j < h; j++) {
				// j is the index of a row of pixels, the y coordinate, 
				// starting at the 'bottom', the lowest frequencies (lowest indices), 
				// corresponding to the pixels at the bottom of the image, with the 
				// highest y coordinate values 
				// which bins contribute to this pixel?

				double value = 0d;
				double binCount = 0;

				// get from yMap
				int[] yc = yMap[j];
				for (int k = 0; k < yc.length; k++) {
					if (yc[k] != 0 || k == 0) {	
						int bindex = yc[k] + minBin;// check against numBin?
						value += freqWindows[xc][bindex];
						binCount++;
						
						// interpolate horizontally
						if (curWinIndex != nextWinIndex) {
							double xValueStep = (freqWindows[nextWinIndex][bindex] - freqWindows[curWinIndex][bindex]) / 
									(nextPixXIndex - curPixXIndex);
							value += (i - curPixXIndex) * xValueStep;
						}
					}
				}		
				
				value /= binCount;//the average value
				value = (value - minmax[0]) / valueRange;// relative value

				dar[i + (h - 1 - j) * w] = value > 1 ? 1 : (value < 0 ? 0 : value);
			}
		}

		return toImage(dar, w, h);
	}

	// produces an image without scaling and the interpolation that comes with it 
	private BufferedImage createSpectrogramImageNoScaling(double[][] freqWindows, /*double minFreq, double posMaxFreq, */
			int imageWidth, int imageHeight) {
		// check the range of frequencies that have to be displayed, the number of frequencies per bin
		// and the ratio between those values. 
		
		double maxDisplayFreq = settings.getMaxDisplayFrequency();
		double minDisplayFreq = settings.getMinDisplayFrequency();
		double posMaxFreq = settings.getPossibleMaxFrequency();
		int numBins = freqWindows[0].length;
		double freqsPerBin = posMaxFreq / numBins;
		int minBin = (int) (minDisplayFreq / freqsPerBin);// index
		int maxBin = (int) Math.ceil(maxDisplayFreq / freqsPerBin);// index, rounded up
		maxBin = maxBin >= freqWindows[0].length ? freqWindows[0].length - 1 : maxBin;
		int numDisplayBins = maxBin - minBin + 1;
		int w = freqWindows.length;
		int h = numDisplayBins;

		if (w != imageWidth && h != imageHeight) {
			// log error
		}
		// the value range can either be given, via settings or detected from the passed windows
		double[] minmax = getMinMax(freqWindows, minBin, maxBin);
		double valueRange = minmax[1] - minmax[0];
		// contrast corrections passed via settings, as well as the colors

		double[] dar = new double[w * h];
		
		for (int i = 0; i < w; i++) {
			// i is the index of a column of pixels in the image, the x coordinate 
			for (int j = minBin; j <= maxBin; j++) {
				// j is the index of a row of pixels, the y coordinate, 
				// starting at the 'bottom', the lowest frequencies (lowest indices), 
				// corresponding to the pixels at the bottom of the image, with the 
				// highest y coordinate values  
				double value = (freqWindows[i][j] - minmax[0]) / valueRange;// relative value

				dar[i + (h - 1 - j) * w] = value > 1 ? 1 : (value < 0 ? 0 : value);
			}
		}

		return toImage(dar, w, h);
	}
	
	// this way of producing an image of the requested size is 5 to 6 times faster (than the LinearHV variant) 
	Image createSpectrogramImageAWT(double[][] freqWindows, /*double minFreq, double posMaxFreq, */
			int imageWidth, int imageHeight) {
		// check the range of frequencies that have to be displayed, the number of frequencies per bin
		// and the ratio between those values. Based on that, bin values might need to be concatenated
		// or repeated in pixels 'vertically'.
		
		double maxDisplayFreq = settings.getMaxDisplayFrequency();
		double minDisplayFreq = settings.getMinDisplayFrequency();
		double posMaxFreq = settings.getPossibleMaxFrequency();
		int numBins = freqWindows[0].length;
		double freqsPerBin = posMaxFreq / numBins;
		int minBin = (int) (minDisplayFreq / freqsPerBin);// index
		int maxBin = (int) Math.ceil(maxDisplayFreq / freqsPerBin);// index, rounded up
		maxBin = maxBin >= freqWindows[0].length ? freqWindows[0].length - 1 : maxBin;
		int numDisplayBins = maxBin - minBin + 1;
		int w = freqWindows.length;
		int h = numDisplayBins;
		// the value range can either be given, via settings or detected from the passed windows
		double[] minmax = getMinMax(freqWindows, minBin, maxBin);
		double valueRange = minmax[1] - minmax[0];
		// contrast corrections passed via settings, as well as the colors
		
		// create a databuffer

		// the number of bands depends on colour settings
		//DataBufferInt dataBufferInt = new DataBufferInt(w * h, 1);
		//int[] data = dataBufferInt.getData();
		double[] dar = new double[w * h];
		
		for (int i = 0; i < w; i++) {
			// i is the index of a column of pixels in the image, the x coordinate 
			for (int j = minBin; j <= maxBin; j++) {
				// j is the index of a row of pixels, the y coordinate, 
				// starting at the 'bottom', the lowest frequencies (lowest indices), 
				// corresponding to the pixels at the bottom of the image, with the 
				// highest y coordinate values  
				double value = (freqWindows[i][j] - minmax[0]) / valueRange;// relative value

				dar[i + (h - 1 - j) * w] = value > 1 ? 1 : (value < 0 ? 0 : value); // value corresponding to reversed gray
			}
		}

		BufferedImage bi = toImage(dar, w, h);

		if (w == imageWidth && h == imageHeight) {
			return bi;
		}
		return bi.getScaledInstance(imageWidth, imageHeight, Image.SCALE_SMOOTH);
	}
	
	// creates an image from a double array with values between 0 and 1
	private BufferedImage toImage(double[] valueArray, int w, int h) {
		final SpectrogramSettings.COLOR_SCHEME colScheme = settings.getColorScheme();
		if (settings.getDataBufferType() == DataBuffer.TYPE_INT) {
			DataBufferInt dbi = new DataBufferInt(valueArray.length, 1);
			int[] data = dbi.getData();
			
			switch (colScheme) {
			case REVERSED_GRAY:
				for (int i = 0; i < valueArray.length; i++) {
					int b = (int) (valueArray[i] * 255);
					data[i] = 0 << 24 | b << 16 | b << 8 | b;
				}
				break;
			case BI_COLOR:
				// do the following once, when settings change?
				updateColorMap();
				int size = lut.length - 1;

				for (int i = 0; i < valueArray.length; i++) {
					//double d = valueArray[i];
					data[i] = lut[(int) (valueArray[i] * size)];
				}
				break;
			case GRAY:
				// default scheme
				default:
					for (int i = 0; i < valueArray.length; i++) {
						int b = (int)(255d - (valueArray[i] * 255));
						data[i] = 0 << 24 | b << 16 | b << 8 | b;
					}
			}
			
			SinglePixelPackedSampleModel sampleModel = new SinglePixelPackedSampleModel(
					DataBuffer.TYPE_INT, w, h, masks);
			WritableRaster raster = Raster.createWritableRaster(sampleModel, 
					dbi, null);
			return new BufferedImage(directColorModel, raster, true, null);
		} else {// type double
			DataBufferDouble dbd = new DataBufferDouble(w * h, 3);
			double[] rdata = dbd.getData(0);
			double[] gdata = dbd.getData(1);
			double[] bdata = dbd.getData(2);
			
			switch (colScheme) {
			case REVERSED_GRAY:
				for (int i = 0; i < valueArray.length; i++) {
					double d = valueArray[i];
					rdata[i] = d;
					gdata[i] = d;
					bdata[i] = d;
				}
				break;
			case BI_COLOR:
				// this still needs to be adapted to color updates
				for (int i = 0; i < valueArray.length; i++) {
					double d = valueArray[i];
					rdata[i] = cr1 >= cr2 ? cr1 - (cre * d) : cr1 + (cre * d);
					gdata[i] = cg1 >= cg2 ? cg1 - (cge * d) : cg1 + (cge * d);
					bdata[i] = cb1 >= cb2 ? cb1 - (cbe * d) : cb1 + (cbe * d);
				}
				break;
			case GRAY:
				// default case
				default:
					for (int i = 0; i < valueArray.length; i++) {
						double d = 1d - valueArray[i];
						rdata[i] = d;
						gdata[i] = d;
						bdata[i] = d;
					}
			}
			
			BandedSampleModel sampleModel = new BandedSampleModel(DataBuffer.TYPE_DOUBLE, w, h, 3);
			WritableRaster raster = Raster.createWritableRaster(sampleModel, dbd, null);
			
			return new BufferedImage(comColorModel, raster, true, null);
		}

	}
	
	/**
	 * Returns the minimum and maximum value, the value range.
	 * 
	 * @param freqWindows the frequency data, in case of adaptive rendering the 
	 * values are extracted from this array, otherwise they are taken from the
	 * settings object
	 * @param fromBin the first bin to include, depends on the display settings
	 * @param toBin the last bin to include, depends on the display settings
	 * @return an array with two elements 
	 */
	private double[] getMinMax(double[][] freqWindows, int fromBin, int toBin) {
		if (!settings.isAdaptiveContrast()) {
			double range = Math.abs(settings.getUpperValueLimit() - settings.getLowerValueLimit());
			return new double[] {
					settings.getLowerValueLimit() + (range * settings.getLowerValueAdjustment() / 100),//percentage 
					settings.getUpperValueLimit() + (range * settings.getUpperValueAdjustment() / 100)};
		} else {
			double min = Double.MAX_VALUE;
			double max = -1000.0d;
			double meps = settings.getAdaptiveMinimum();
			for (int i = 0; i < freqWindows.length; i++) {
				for (int j = fromBin; j <= toBin; j++) {
					double d = freqWindows[i][j];
					if(d <= meps) continue;
					if (d < min) min = d;
					if (d > max) max = d;
				}
			}

			return new double[] {min, max};
		}
	}
	
// methods for scaling and mapping of bins to pixels	
	private boolean match(boolean firstSmaller, double rb, double re, double ob, double oe) {
		if (oe < rb || re < ob) return false;
		double ov1 = rb <= ob ? ob : rb;
		double ov2 = re <= oe ? re : oe;
		if (firstSmaller) {
			return (ov2 - ov1) >= minOv * (re - rb);
		} else {
			return (ov2 - ov1) >= minOv * (oe - ob);
		}
	}
	
	// third approach
	private int[][] getCorrespondingIndices(int numPixels, int numWindows, double pixelDur, double windowDur) {
		int max = (int) Math.max(2, Math.ceil(pixelDur / windowDur));
		int[][] map = new int[numPixels][max];
		boolean refSmaller = (pixelDur <= windowDur);
		
		double rb = 0, re = 0, ob = 0, oe = windowDur;
		int fw = 0;

		// outer loop represents the pixels loop
		for (int i = 0; i < numPixels; i++) {
			rb = re;
			re += pixelDur;
			// inner loop represents the frequency windows loop
			boolean oneMatch = false;
			for (; fw < numWindows; fw++) {
				if (match(refSmaller, rb, re, ob, oe)) {
					oneMatch = true;
					//add to the array for pixel i
					for (int j = 0; j < max; j++) {
						if (map[i][j] == 0) {
							map[i][j] = fw;
							break;
						}
					}
					ob = oe;
					oe += windowDur;
				} else {
					if (oneMatch) {
						break;
					} else {
						ob = oe;
						oe += windowDur;
					}
				}
			}
			// one window step back
			oe = ob;
			ob -= windowDur;
			fw--;
		}
				
		return map;
	}
	
	private int[][] getBinMap(int h, int numDisplayBins) {
		double ratio = h / (double) numDisplayBins;
		//int max = ratio > 1 ? 2 : (int) Math.max(2, Math.ceil(ratio));
		int max = (int) Math.max(2, Math.ceil(ratio));
		int[][] map = new int[h][max];		
		
		/*
		int blendNum = (int) (ratio / 2);
		if (ratio > 1) {
			int ri = (int) ratio;
			int curBin = 0;
			double curAdv = ratio;
			for (int i = 0; i < map.length; i++) {
				map[i][0] = curBin;
				if (i > 0 && i == ri) {
					if (curBin < numDisplayBins - 1) {
						curBin++;
						map[i][1] = curBin;
						curAdv += ratio;
						ri = (int) curAdv;
						// blend left and right
						int curInd = 2;
						for (int j = 0; j < blendNum; j++) {
							if (curBin - 1 - j >= 0 && curBin - 1 -j != i) { 
								map[i][curInd++] = curBin - 1 - j;
								// adjust arrays to the left
							}
						}
						for (int j = 0; j < blendNum; j++) {
							if (curBin + 1 + j <= numDisplayBins - 1 && curInd < max) { 
								map[i][curInd++] = curBin + 1 + j;
								//adjust arrays to the right
							}
						}
					}
				}
			}
			
			return map;
		}
		*/
		// this creates blocks with only one interpolated pixel
		if (ratio > 1) {
			int ri = (int) ratio;
			int curBin = 0;
			double curAdv = ratio;
			for (int i = 0; i < map.length; i++) {
				map[i][0] = curBin;
				if (i > 0 && i == ri) {
					if (curBin < numDisplayBins - 1) {
						curBin++;
						map[i][1] = curBin;
						curAdv += ratio;
						ri = (int) curAdv;
					}
				}
			}
			
			return map;
		}
		
		double s = 0, e = 0, bb = 0, be = ratio;
		int ind = 0;
		
		for (int i = 0; i < h; i++) {
			s = e;
			e++;
			for (int j = 0; j < max;) {
				if (match(ratio > 1, s, e, bb, be)) {
					map[i][j] = ind;
					j++;
				} else if (j > 0){
					break;
				} 
				bb = be;
				be += ratio;
				ind++;
			}
		}
		
		return map;
	}
	
	private int[] getBinMapSizingUp(int h, int numDisplayBins) {
		double ratio = h / (double) numDisplayBins;
		if (ratio < 1) {
			// report, throw exception?
			return null;
		}
		int[] map = new int[h];		

		int ri = (int) ratio;
		int curBin = 0;
		double curAdv = ratio;
		for (int i = 0; i < map.length; i++) {
			map[i] = curBin;
			if (i > 0 && i == ri) {
				if (curBin < numDisplayBins - 1) {
					curBin++;
					map[i] = curBin;
					curAdv += ratio;
					ri = (int) curAdv;
				}
			}
		}
		
		return map;
	}
	
	private int[] getWindowMapSizingUp(int numPixels, int numWindows, double pixelDur, double windowDur) {
		double ratio = pixelDur / windowDur;
		if (ratio > 1) {
			// report, throw exception?
			return null;
		}
		int[] map = new int[numPixels];
		double pb = 0, pe = 0, wb = 0, we = windowDur;
		int winInd = 0;
		
		for (int i = 0; i < numPixels; i++) {
			pb = pe;
			pe += pixelDur;
			
			for (; winInd < numWindows; winInd++) {
				if (overlap1(pb, pe, wb, we) >= 0.5d) {
					map[i] = winInd;
					break;
				} else {
					wb = we;
					we += windowDur;
				}
			}
			// one window step back?
//			we = wb;
//			wb -= windowDur;
//			winInd--;
		}
		return map;
	}
	
	private double overlap1(double rb, double re, double ob, double oe) {
		if (oe < rb || re < ob) return 0d;
		double ov1 = rb <= ob ? ob : rb;
		double ov2 = re <= oe ? re : oe;
		
		return (ov2 - ov1) / (re - rb);
	}
	
}
