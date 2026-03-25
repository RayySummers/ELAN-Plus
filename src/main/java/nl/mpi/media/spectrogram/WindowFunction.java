package nl.mpi.media.spectrogram;

/**
 * This class provides several windowing functions to be applied to intervals
 * of a waveform before a Fourier transform. Not all functions here are 
 * applicable/suitable for generation of audio spectrograms.
 * The methods return an array of the requested size which can be applied 
 * to multiple sequences of windows (waveform intervals).
 * <p>
 * This class also contains one or more methods for creating an array for a 
 * Window and Level Filter for image contrast manipulation.
 * <p>
 * <strong>Warning:</strong> methods in this class do not check the validity of
 * parameters, e.g. it is assumed that the size of a window is {@code >0} and
 * that other parameters are within the documented range. 
 * 
 * @author Han Sloetjes
 * @see <a href="https://en.wikipedia.org/wiki/Window_function">Window functions</a>
 * 
 */
public class WindowFunction {
	/**
	 * The constants of this enumerated type identify available implementations
	 * of window functions.
	 */
	public enum WF_NAME {
		/** the Hann or Hanning function */
		HANN("hann", "Hann"),
		/** the Hamming function */
		HAMMING("hamming", "Hamming"),
		/** the Blackman function */
		BLACKMAN("blackman", "Blackman"),
		/** the Nutall function */
		NUTALL("nutall", "Nutall"),
		/** the Blackman-Harris function */
		BLACKMAN_HARRIS("black-har", "Blackman-Harris"),
		/** the Kaiser-Bessel function */
		KAISER_BESSEL("kais-bes", "Kaiser-Bessel"),
		/** the Flattop function */
		FLAT_TOP("flattop", "Flat Top"),
		/** the Bartlett function */
		BARTLETT("bartlett", "Bartlett"),
		/** the Triangular function */
		TRIANGULAR("triangular", "Triangular"),
		/** the Gaussian function */
		GAUSSIAN("gaussian", "Gaussian"),
		/** the Tukey function */
		TUKEY("tukey", "Tukey"),
		/** the Gaussian-Tukey function */
		GAUSSIAN_TUKEY("gaus-tuk", "Gaussian-Tukey"),
		/** the Welch function */
		WELCH("welch", "Welch"),
		/** the Rectangular function */
		RECTANGULAR("rectangular", "Rectangular (none)");

		private final String id;
		private final String dispName;
		
		/**
		 * @param id short name
		 * @param name display name
		 */
		private WF_NAME(String id, String dispName) {
			this.dispName = dispName;
			this.id = id;
		}

		@Override
		public String toString() {
			return dispName;
		}
		
		/**
		 * Returns the {@code id}.
		 * 
		 * @return the {@code id} of the function
		 */
		public final String getId() {
			return id;
		}
		
		/**
		 * Returns the display name of the function.
		 * 
		 * @return the display name
		 */
		public final String getDisplayName() {
			return dispName;
		}
	}

	/**
	 * Private constructor, all methods are static.
	 */
	private WindowFunction() {
	}
	
	/**
	 * Returns a window array of the specified size applying the function 
	 * identified by the specified name.
	 * 
	 * @param winName one of the functions of the {@link WF_NAME} enumeration
	 * @param size the size of the window, usually a power of 2
	 * @return an array produced by the window function, usually zero-ended
	 */
	public static double[] windowForName(WF_NAME winName, int size) {
		if (winName == null) {
			return null;
		}
		switch (winName) {
		case HANN:
			return hann(size);
		case HAMMING:
			return hamming(size);
		case BLACKMAN:
			return blackman(size);
		case BLACKMAN_HARRIS:
			return blackmanHarris(size);
		case NUTALL:
			return nutall(size);
		case KAISER_BESSEL:
			return kaiserBessel(size);
		case FLAT_TOP:
			return flatTop(size);
		case BARTLETT:
			return bartlett(size);
		case TRIANGULAR:
			return triangular(size);
		case GAUSSIAN:
			return gaussian(size);
		case TUKEY:
			return tukey(size);
		case GAUSSIAN_TUKEY:
			return gausTukey(size);
		case WELCH:
			return welch(size);
		case RECTANGULAR:
			return null;
			default:
				return hann(size);
		}
	}
	
	/**
	 * Returns a window array of the specified size applying the function 
	 * identified by the specified name, applying additional parameters.
	 * Only a few functions accept additional parameters, for most functions
	 * the call is forwarded to {@link #windowForName(WF_NAME, int)}.
	 * 
	 * @param winName one of the functions of the {@link WF_NAME} enumeration.
	 * Currently only the Gaussian and Tukey functions accept additional 
	 * parameters ({@code sigma} or {@code alpha} respectively).
	 * @param size one of the functions of the {@link WF_NAME} enumeration
	 * @param p1 first parameter ({@code sigma} or {@code alpha})
	 * @param p2 second parameter ({@code sigma} or {@code alpha})
	 * 
	 * @return an array produced by the window function, usually zero-ended
	 */
	public static double[] windowForName(WF_NAME winName, int size, double p1, double p2) {
		if (winName == null) {
			return null;
		}
		switch (winName) {
		case GAUSSIAN:
			return gaussian(size, p1, true);
		case TUKEY:
			return tukey(size, p1);
		case GAUSSIAN_TUKEY:
			return gausTukey(size, p1, p2);
			default:
				return windowForName(winName, size);
		}
	}
	
	/**
	 * Returns one of the {@link WF_NAME} types for the specified display name
	 * or {@code null} if none is found. 
	 * 
	 * @param displayName the display name
	 * @return the corresponding {@link WF_NAME} type
	 */
	public static WF_NAME getWFName(String displayName) {
		for (WF_NAME wfn : WF_NAME.values()) {
			if (wfn.getDisplayName().equals(displayName)) {
				return wfn;
			}
		}
		return null;
	}

	/**
	 * The Hann window function, forwards to {@link #cosineSum(int, double, double)}
	 * with parameters {@code 0.5} and {@code 0.5}.
	 * 
	 * @param size the size of the window
	 * @return the window array
	 * @see <a href="https://en.wikipedia.org/wiki/Hann_function">Hann window</a>
	 */
	public static double[] hann(int size) {
		return cosineSum(size, 0.5d, 0.5d);
		/*
		double[] ar = new double[size];
		int s = size - 1;// -1 to get a symmetric window without a value 1.0
		
		for (int n = 0; n < size; n++) {
			ar[n] = 0.5d - 0.5d * Math.cos((2 * Math.PI * n) / s); // Wikipedia and e.g. the Python numpy.hanning implementation
			//ar[n] = 0.5d * (1 - Math.cos((2 * Math.PI * n) / s));    // equivalent to the above and the following
			//ar[n] = Math.sin((Math.PI * n) / s) * Math.sin((Math.PI * n) / s); // Math.pow(Math.sin((Math.PI * n) / s), 2)
		}
		print(ar);
		return ar;
		*/
	}
	
	/**
	 * The Hamming window function, forwards to {@link #cosineSum(int, double, double)}
	 * passing values, approximately {@code a0=0.54} and {@code a1=0.46}.
	 * 
	 * @param size the size of the window
	 * @return the window array
	 * @see <a href="https://en.wikipedia.org/wiki/Window_function#Hann_and_Hamming_windows">Hann and Hamming windows</a>
	 */
	public static double[] hamming(int size) {
		return cosineSum(size, 0.53836d, 0.46164d);
	}
	
	/**
	 * The Blackman window function, forwards to {@link #cosineSum(int, double, double, double, double)}
	 * passing approximately the <b>exact Blackman</b> values 
	 * {@code a0=0.42659}, {@code a1=0.49656} and {@code a2=0.076849}.
	 * 
	 * @param size the size of the window
	 * @return the window array
	 */
	public static double[] blackman(int size) {
		return cosineSum(size,  0.42659d, 0.49656d, 0.076849d, 0.0);
	}
	
	/**
	 * The Nutall window function, forwards to {@link #cosineSum(int, double, double, double, double)}
	 * passing the following values {@code a0=0.355768}, {@code a1=0.487396}, 
	 * {@code a2=0.144232} and {@code a3=0.012604}.
	 * 
	 * @param size the size of the window
	 * @return the window array
	 */
	public static double[] nutall(int size) {
		return cosineSum(size,  0.355768d, 0.487396d, 0.144232d, 0.012604d);
	}
	
	/**
	 * The Blackman-Harris window function, forwards to {@link #cosineSum(int, double, double, double, double)}
	 * passing the values 
	 * {@code a0=0.35875}, {@code a1=0.48829}, {@code a2=0.14128} and {@code a3=0.01168}.
	 * 
	 * @param size the size of the window
	 * @return the window array
	 */
	public static double[] blackmanHarris(int size) {
		return cosineSum(size,  0.35875d, 0.48829d, 0.14128d, 0.01168d);
	}
	
	/**
	 * The Kaiser-Bessel window function.
	 * 
	 * @param size the size of the window
	 * @return the window array
	 */
	public static double[] kaiserBessel(int size) {
		double[] ar = new double[size];
		int s = size - 1;
		
		for (int n = 0; n < size; n++) {
			ar[n] = 1d - 1.24d * Math.cos((2 * Math.PI * n) / s) + 0.244d * Math.cos((4 * Math.PI * n) / s) 
			- 0.00305d * Math.cos((6 * Math.PI * n) / s); // Bruel & Kjaar, bv0031.pdf
		}
		//print(ar);
		return ar;
	}
	
	/**
	 * The flat-top window function.
	 * 
	 * @param size the size of the window
	 * @return the window array
	 */
	public static double[] flatTop(int size) {
		double[] ar = new double[size];
		int s = size - 1;
		
		for (int n = 0; n < size; n++) {
			//ar[n] =  1d - 1.93d * Math.cos((2 * Math.PI * n) / s) + 1.29d * Math.cos((4 * Math.PI * n) / s) - 
			//		0.388d * Math.cos((6 * Math.PI * n) / s) + 0.0322d * Math.cos((8 * Math.PI * n) / s);// Bruel & Kjaar, bv0031.pdf
			// the cosine-sum with 5 parameters
			ar[n] =  0.211557895d - 0.41663158d * Math.cos((2 * Math.PI * n) / s) + 0.277263158d * Math.cos((4 * Math.PI * n) / s) - 
					0.083578947d * Math.cos((6 * Math.PI * n) / s) + 0.006947368d * Math.cos((8 * Math.PI * n) / s);
		}
		//print(ar);
		return ar;
	}
	
	/**
	 * The short cosine-sum form of windows that can be used for several implementations.
	 * 
	 * Algorithm: {@code a0 - a1 * cos((2*pi*n) / N) }
	 * 
	 * @param size the size of the window
	 * @param a0 the first constant
	 * @param a1 the second constant
	 * @return the window array
	 * @see <a href="https://en.wikipedia.org/wiki/Window_function#Cosine-sum_windows">Cosine-sum windows</a>
	 */
	public static double[] cosineSum(int size, double a0, double a1) {
		double[] ar = new double[size];
		int s = size - 1;// -1 to get a symmetric window without a value 1.0
		
		for (int n = 0; n < size; n++) {
			ar[n] = a0 - a1 * Math.cos((2 * Math.PI * n) / s);
		}
		//print(ar);
		return ar;
	}
	
	/**
	 * The full cosine-sum form of windows that can be used for several implementations.
	 * 
	 * Algorithm: {@code a0 - a1 * cos((2*pi*n) / N) + a2 * cos((4*pi*n) / N) - a3 * cos((6*pi*n) / N)}
	 * 
	 * @param size the size of the window
	 * @param a0 the first constant
	 * @param a1 the second constant
	 * @param a2 the third constant
	 * @param a3 the fourth constant
	 * @return the window array
	 * 
	 * @see #cosineSum(int, double, double)
	 */
	public static double[] cosineSum(int size, double a0, double a1, double a2, double a3) {
		double[] ar = new double[size];
		int s = size - 1;// -1 to get a symmetric window without a value 1.0
		
		for (int n = 0; n < size; n++) {
			ar[n] = a0 - a1 * Math.cos((2 * Math.PI * n) / s) + 
					a2 * Math.cos((4 * Math.PI * n) / s) - 
					a3 * Math.cos((6 * Math.PI * n) / s);
		}
		//print(ar);
		return ar;
	}
	
	/**
	 * The Bartlett window function.
	 * 
	 * @param size the size of the window
	 * @return the window array
	 */
	public static double[] bartlett(int size) {
		double[] ar = new double[size];
		double s = size - 1;// -1 to get a symmetric window without a value 1.0
		double L = s;
		
		for (int n = 0; n < size; n++) {
			ar[n] = 1d - Math.abs((n - s / 2) / (L / 2));
		}
		//print(ar);
		return ar;
	}
	
	/**
	 * The triangular window function.
	 * 
	 * @param size the size of the window
	 * @return the window array
	 */
	public static double[] triangular(int size) {
		double[] ar = new double[size];
		double s = size - 1;// -1 to get a symmetric window without a value 1.0
		double L = size; // s + 1
		
		for (int n = 0; n < size; n++) {
			ar[n] = 1d - Math.abs((n - s / 2) / (L / 2));
		}
		//print(ar);
		return ar;
	}
	
	/**
	 * The Gaussian window function.
	 * 
	 * @param size the size of the window
	 * @param sigma a value {@code >0} and {@code <= 0.5}
	 * @param multiply if {@code true} each value is multiplied by itself
	 * to make the window almost zero ended 
	 * @return the window array
	 * @see <a href="https://en.wikipedia.org/wiki/Window_function#Gaussian_window">Gaussian window</a>
	 */
	public static double[] gaussian(int size, double sigma, boolean multiply) {
		double[] ar = new double[size];
		int s = size - 1;
		
		for (int n = 0; n < size; n++) {
			// the following approaches yield almost the same result
			//ar[n] =  Math.pow(Math.E, -0.5d * Math.pow((n - s / 2d) / (sigma * s / 2d), 2d));
			ar[n] = Math.exp(-0.5d * Math.pow((n - s / 2d) / (sigma * s / 2d), 2d));
			if (multiply) {
				ar[n] *= ar[n];
			}
		}
		//print(ar);
		return ar;
	}
	
	// approximate confined gaussian window?
	/*
	int L = size;
	int N = s;
	double sigT = 0.1d;
	for (int n = 0; n < size; n++) {
		ar2[n] = gf(n, N, L, sigT) - 
				(gf(-0.5d, N, L, sigT) * (gf(n + L, N, L, sigT) + gf(n - L, N, L, sigT))) / 
				(gf(-0.5d + L, N, L, sigT) + gf(-0.5d - L, N, L, sigT));
	}
	*/
	/*
	private static double gf(double n, int N, int L, double sigma) {
		return Math.exp(-Math.pow((n - N/2) / (2 * L * sigma), 2d));
	}
	*/
	
	/**
	 * The Gaussian window function with a default {@code sigma} of {@code 0.25}.
	 * 
	 * @param size the size of the window
	 * @return the window array
	 */
	public static double[] gaussian(int size) {
		return gaussian(size, 0.25d, true);
	}
	
	/**
	 * The Gaussian window function, made zero ended by tapering (the first and
	 * last 10% of the samples).
	 * 
	 * @param size the size of the window
	 * @param sigma a value {@code >0} and {@code <= 0.5}
	 * @return the window array
	 * @see <a href="https://en.wikipedia.org/wiki/Window_function#Gaussian_window">Gaussian window</a>
	 */
	public static double[] gaussianTapered(int size, double sigma) {
		double[] ar = new double[size];
		double[] tw = planckTaper(size);
		int s = size - 1;
		
		for (int n = 0; n < size; n++) {
			// the following approaches yield almost the same result
			//ar[n] =  Math.pow(Math.E, -0.5d * Math.pow((n - s / 2d) / (sigma * s / 2d), 2d));
			ar[n] = Math.exp(-0.5d * Math.pow((n - s / 2d) / (sigma * s / 2d), 2d));
			ar[n] = ar[n] * tw[n];
		}
		//print(ar);
		return ar;
	}
	
	/**
	 * The Gaussian window function, made zero ended by tapering (the first and
	 * last 10% of the samples) and with a default {@code sigma} of {@code 0.25}.
	 * 
	 * @param size the size of the window
	 * @return the window array
	 * @see <a href="https://en.wikipedia.org/wiki/Window_function#Gaussian_window">Gaussian window</a>
	 */
	public static double[] gaussianTapered(int size) {
		return gaussianTapered(size, 0.25d);
	}
	
	/**
	 * The Tukey window.
	 * 
	 * @param size the size of the window
	 * @param alpha the {@code alpha} value {@code a > 0} and {@code a <= 1}
	 * @return the window array
	 * @see <a href="https://en.wikipedia.org/wiki/Window_function#Tukey_window">Tukey window</a>
	 */
	public static double[] tukey(int size, double alpha) {
		double[] ar = new double[size];
		int s = size - 1;
		double an =  alpha * s;
		int han = (int) (an / 2);
		
		for (int n = 0; n < han; n++) {
			ar[n] = 0.5d * (1 - Math.cos((2 * Math.PI * n) / an)) ;
		}
		for (int n = han; n <= s / 2; n++) {
			ar[n] = 1d;
		}
		for (int n = 0; n <= s / 2; n++) {
			ar[s - n] = ar[n];
		}
		//print(ar);
		return ar;
	}
	
	/**
	 * The Tukey window with {@code alpha} value is {@code 0.5}.
	 * @param size the size of the window
	 * @return the window array
	 * @see #tukey(int, double)
	 */
	public static double[] tukey(int size) {
		return tukey(size, 0.5d);
	}
	
	/**
	 * Creates a Gaussian window and applies a Tukey window to it to make it 
	 * zero-ended.
	 * 
	 * @param size the size of the window
	 * @param sigma a value {@code >0} and {@code <= 0.5}
	 * @param alpha the {@code alpha} value {@code a > 0} and {@code a <= 1}
	 * @return the window array
	 * @see #gaussian(int, double, boolean)
	 * @see #tukey(int, double)
	 */
	public static double[] gausTukey(int size, double sigma, double alpha) {
		double[] tw = tukey(size, alpha);
		double[] gw = gaussian(size, sigma, false);
		for (int n = 0; n < size; n++) {
			gw[n] = gw[n] * tw[n];
		}
		//print(gw);
		return gw;
	}
	
	/**
	 * Creates a Gaussian window and applies a Tukey window to it to make it 
	 * zero-ended, using default sigma and alpha values.
	 * 
	 * @param size the size of the window
	 * @return the window array
	 * @see  #gaussian(int, double, boolean)
	 * @see #tukey(int, double)
	 */
	public static double[] gausTukey(int size) {
		double[] tw = tukey(size);
		double[] gw = gaussian(size);
		for (int n = 0; n < size; n++) {
			gw[n] = gw[n] * tw[n];
		}
		//print(gw);
		return gw;
	}
	
	
	/**
	 * A tapering window that varies smoothly from 0 to 1 and v.v. in the first
	 * and last 10% of {@code size} samples.
	 *  
	 * @param size the size of the window
	 * @return the tapering window array
	 * @see <a href="https://en.wikipedia.org/wiki/Window_function#Planck-taper_window">Planck-taper window</a>
	 */
	public static double[] planckTaper(int size) {
		int eps = size / 10;
		double[] pt = new double[size];
		int s = size - 1;
		
		for (int i = 1; i < eps; i++) {
			pt[i] = Math.pow(1 + Math.exp((eps / i) - (eps/(eps - i))), -1d);
		}
		for (int i = eps; i < size / 2; i++) {
			pt[i] = 1d;
		}
		pt[0] = 0d;
		for (int i = 0; i < size / 2; i++) {
			pt[s - i] = pt[i];
		}
		
		return pt;
	}
	
	/**
	 * Creates a Welch window.
	 * 
	 * @param size the size of the window
	 * @return the window array
	 * @see <a href="https://en.wikipedia.org/wiki/Window_function#Welch_window">Welch window</a>Welch_window
	 */
	public static double[] welch(int size) {
		double[] ar = new double[size];
		double s = size - 1;// -1 to get a symmetric window without a value 1.0
		double hs = s / 2;
		
		for (int n = 0; n < size; n++) {
			ar[n] = 1d - Math.pow(((n - hs) / hs), 2);
		}
		//print(ar);
		return ar;
	}
	
	/**
	 * Creates a linear window and level filter array for pixel component 
	 * values between {@code 0} and {@code 255}. The array indexes between
	 * {@code from} and {@code to} are linearly interpolated from {@code 0}
	 * to {@code 255}. The array can be used as a lookup table for contrast 
	 * correction.
	 * 
	 * @param from all elements from index {@code 0} to index {@code from} are
	 * set to {@code 0}
	 * @param to all elements from index {@code to} to index {@code 255} are
	 * set to {@code 255}
	 * @return an array of size 256 which can be used as lookup table
	 */
	public static int[] windowLevelFilter(int from, int to) {
		int size = 256;
		int[] map = new int[size];
		for (int i = 0; i < from; i++) {
			map[i] = 0;
		}
		for (int i = to + 1; i < size; i++) {
			map[i] = 255;
		}
		double stepSize = 255d /(to - from);
		for (int i = 0, j = from; j <= to; j++, i++) {
			map[j] = (int) Math.round(i * stepSize);
		}
		
		return map;
	}
	
	/**
	 * Prints the entire sequence of a window to standard out.
	 * 
	 * @param window the window array
	 */
	@SuppressWarnings("unused")
	private static void print(double[] window) {
		String format = "%.12f   ";
		System.out.println(String.format("Window size: %d", window.length));
		for (int i = 0, j = 0; i < window.length; i++) {
			System.out.print(String.format(format, window[i]));
			if (++j % 10 == 0) System.out.println();
		}
		System.out.println();
	}
}
