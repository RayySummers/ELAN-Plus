package nl.mpi.media.spectrogram;

import java.util.Arrays;
/**
 * A class providing different (re-)implementations of the Fast Fourier 
 * Transform. There are several (re)implementations in (pure) Java and several
 * native implementations via JNI.
 * 
 * @author Han Sloetjes
 */
public class FFT {
	/** machine epsilon approximation */
	public static final double meps = 1e-14d;// machine epsilon approximation
	/* currently the Java implementation is used, the native implementation 
	   only seems to be slightly faster 
	static {
		try {
			System.loadLibrary("FFT");
		} catch (UnsatisfiedLinkError ule) {
			System.out.println("Library not loaded: " + ule.getMessage());
		}
	}
	*/
	
	/**
	 * No arg constructor.
	 */
	public FFT() {
		
	}
	
	//#################
	// LibRow based FFT re-implementations in Java
	//#################
	// LibRow based  this variant uses separate double arrays instead of a Complex class
	
	/**
	 * In place forward Fourier Transform based on arrays of doubles. 
	 * Both arrays are modified during the transform.
	 * 
	 * @param realArray the array of the real part of the complex number
	 * @param imArray the array of the imaginary part of the complex number
	 * @return true if no errors occurred
	 */
	public boolean forwardFTIP(double[] realArray, double[] imArray) {
		// the length of the array should be a power of 2
		if (realArray == null || realArray.length == 0 || (realArray.length & realArray.length - 1) != 0) {
			return false;
		}
		if (imArray == null) {
			imArray = new double[realArray.length];
			Arrays.fill(imArray, 0.0d);
		}
		
		rearrangeIP(realArray);
		performIP(realArray, imArray, false);
		
		return true;
	}
	
	/**
	 * In place inverse Fourier Transform based on arrays of doubles. 
	 * Both arrays are modified during the transform.
	 * 
	 * @param realArray the array of the real part of the complex number
	 * @param imArray the array of the imaginary part of the complex number
	 * @return {@code true} if no errors occurred
	 */
	public boolean inverseFTIP(double[] realArray, double[] imArray) {
		// the length of the array should be a power of 2
		if (realArray == null || realArray.length == 0 || (realArray.length & realArray.length - 1) != 0) {
			return false;
		}
		if (imArray == null) {
			imArray = new double[realArray.length];
			Arrays.fill(imArray, 0.0d);
		}
		
		rearrangeIP(realArray, imArray);
		performIP(realArray, imArray, true);
		// scale by default
		scaleIP(realArray, imArray);
		
		return true;
	}
	
	/**
	 * In place forward Fourier Transform based on arrays of doubles. 
	 * Both arrays are modified during the transform. The array containing the
	 * real part will contain normalized, absolute values of the complex 
	 * numbers (the square root of the sum of the power of two of both parts).
	 * 
	 * @param realArray the array of the real part of the complex number
	 * @param imArray the array of the imaginary part of the complex number
	 * @return {@code true} if no errors occurred
	 */
	public boolean forwardFTNormIP(double[] realArray, double[] imArray) {
		// the length of the array should be a power of 2
		if (realArray == null || realArray.length == 0 || (realArray.length & realArray.length - 1) != 0) {
			return false;
		}
		if (imArray == null) {
			imArray = new double[realArray.length];
			Arrays.fill(imArray, 0.0d);
		}
		
		rearrangeIP(realArray);
		performIP(realArray, imArray, false);
		
		for (int i = 0; i < realArray.length; i++) {
			realArray[i] = Math.sqrt(realArray[i] * realArray[i] + imArray[i] * imArray[i]);
			//realArray[i] = Math.sqrt(Math.pow(realArray[i], 2) + Math.pow(imArray[i], 2));
		}
		
		return true;
	}
	
	// add jFFTLR which returns an array of both real and imaginary parts 
	/**
	 * In place forward Fourier Transform based on a single array of doubles 
	 * containing the real part of the complex numbers. The array is modified 
	 * during the transform. <p>
	 * This uses the Java re-implementation of the LibRow FFT.
	 * 
	 * @param realArray the array of the real part of the complex number

	 * @return an array twice the size of the input array, with interleaved real 
	 * and imaginary parts of the complex numbers (i.e. the real part at even
	 * index {@code n} and the imaginary part odd index {@code n + 1})
	 */
	public double[] jFFTLR(double[] realArray) {
		// the length of the array should be a power of 2
		if (realArray == null || realArray.length == 0 || (realArray.length & realArray.length - 1) != 0) {
			return null;
		}

		double[] imArray = new double[realArray.length];
		Arrays.fill(imArray, 0.0d);
		
		rearrangeIP(realArray);
		performIP(realArray, imArray, false);
		
		double[] outArray = new double[realArray.length * 2];
		for (int i = 0, j = 0; i < realArray.length; i++, j += 2) {
			outArray[j] = realArray[i];
			outArray[j + 1] = imArray[i];
		}
		
		return outArray;
	}
	
	/**
	 * In place forward Fourier Transform based on a single array of doubles 
	 * containing the real part of the complex numbers. The array is modified 
	 * during the transform. The result array contains absolute values of
	 * the complex numbers.
	 * <p>
	 * This uses the Java re-implementation of the LibRow FFT.
	 * 
	 * @param realArray the array of the real part of the complex number

	 * @return an array containing absolute values of the complex numbers 
	 * ({@code Math.sqrt(re*re + im*im)}). The size of the array is half the
	 * size of the input array plus one (n / 2 + 1); the complex conjugates
	 * have been removed.
	 */
	public double[] jFFTLRAbs(double[] realArray) {
		// the length of the array should be a power of 2
		if (realArray == null || realArray.length == 0 || (realArray.length & realArray.length - 1) != 0) {
			return null;
		}

		double[] imArray = new double[realArray.length];
		Arrays.fill(imArray, 0.0d);
		
		rearrangeIP(realArray);
		performIP(realArray, imArray, false);
		
		double[] outArray = new double[realArray.length / 2 + 1];
		for (int i = 0; i <= realArray.length / 2; i++) {
			outArray[i] = Math.sqrt(realArray[i] * realArray[i] + imArray[i] * imArray[i]);
		}
		
		return outArray;
	}
	
	/**
	 * In place forward Fourier Transform based on a single array of doubles 
	 * containing the real part of the complex numbers. The array is modified 
	 * during the transform. The result array contains normalized values of
	 * the complex numbers.
	 * <p>
	 * This uses the Java re-implementation of the LibRow FFT.
	 * 
	 * @param realArray the array of the real part of the complex number

	 * @return an array containing normalized values of the complex numbers 
	 * ({@code re*re + im*im}). The size of the array is half the size of the input array plus one
	 * (n / 2 + 1); the complex conjugates have been removed.
	 */
	public double[] jFFTLRNorm(double[] realArray) {
		// the length of the array should be a power of 2
		if (realArray == null || realArray.length == 0 || (realArray.length & realArray.length - 1) != 0) {
			return null;
		}

		double[] imArray = new double[realArray.length];
		Arrays.fill(imArray, 0.0d);
		
		rearrangeIP(realArray);
		performIP(realArray, imArray, false);
		
		double[] outArray = new double[realArray.length / 2 + 1];
		for (int i = 0; i <= realArray.length / 2; i++) {
			outArray[i] = realArray[i] * realArray[i] + imArray[i] * imArray[i];
		}
		
		return outArray;
	}
	
	/**
	 * Extended in place forward Fourier Transform based on a single array of 
	 * doubles containing the real part of the complex numbers. The array is 
	 * modified during the transform. The contents of the result array depends
	 * on parameters passed to the method.
	 * <p>
	 * This uses the Java re-implementation of the LibRow FFT.
	 * 
	 * @param realArray the array of the real part of the complex number
	 * @param normalize if {@code true} the complex numbers are normalized
	 * @param absolute if {@code true} absolute values of the complex numbers
	 *        are returned. The {@code normalize} parameter is ignored then. 
	 * @param trim if {@code true} the complex conjugates are removed and the
	 *        returned array is half the size of the input plus one
	 * @param divideBySize if {@code true} the values are scaled, divided by 
	 *        the size of the array 
	 * @param power if {@code true} the values are converted to the 
	 *        {@code 10 * Math.log10(n)} power value
	 * @param rootPower if {@code true} the values are converted to the 
	 *        {@code 20 * Math.log10(n)} root power value. This overrules the
	 *        {@code power} parameter.
	 * @param zeroPowerLimit if {@code true} the power values will be limited 
	 *        to {@code >= 0} (the lower threshold to prevent negative values).
	 *        Only meaningful in combination with {@code power} or 
	 *        {@code rootPower} parameters.
	 *         
	 * @return a result array, size and contents depends on the settings
	 */
	public double[] jFFTLROpt(double[] realArray, boolean normalize, 
			boolean absolute, boolean trim, boolean divideBySize, boolean power, 
			boolean rootPower, boolean zeroPowerLimit) {
		// the length of the array should be a power of 2
		if (realArray == null || realArray.length == 0 || (realArray.length & realArray.length - 1) != 0) {
			System.out.println("Invalid input, returning null array");
			return null;
		}
		
		double[] imArray = new double[realArray.length];
		Arrays.fill(imArray, 0.0d);
		
		rearrangeIP(realArray);
		performIP(realArray, imArray, false);
		
		double[] outArray = null;

		// if trim omit the second, symmetric half of the array
		if (trim) {
			outArray = new double[realArray.length / 2 + 1];
		} else {
			outArray = realArray;
		}
		
		int size = outArray.length;
		for (int i = 0; i < size; i++) {
			if (normalize && !absolute) {
				outArray[i] = realArray[i] * realArray[i] + imArray[i] * imArray[i];
			} else if (absolute) {
				outArray[i] = Math.sqrt(realArray[i] * realArray[i] + imArray[i] * imArray[i]);
			}
			
			if (divideBySize) {
				outArray[i] = outArray[i] / (size - 1);
			}
			
			if (rootPower) {
				if (!zeroPowerLimit)
					outArray[i] = 20 * Math.log10(outArray[i] + meps);
				else
					outArray[i] = 20 * Math.log10(Math.max(outArray[i], 1.0));
			} else if (power) {
				if (!zeroPowerLimit)
					outArray[i] = 10 * Math.log10(outArray[i] + meps);
				else
					outArray[i] = 10 * Math.log10(Math.max(outArray[i], 1.0));
			}
		}
		
		return outArray;
	}
	
	/**
	 * Re-arrange in place (based on LibRow implementation).
	 * @param realArray the input array of the real part of the complex numbers
	 */
	private void rearrangeIP(double[] realArray) {
		int t = 0;// target
		int len = realArray.length;
		
		for (int i = 0; i < len; i++) {
			if (t > i) {
				double d = realArray[t];
				realArray[t] = realArray[i];
				realArray[i] = d;
			}		
		
			int m = len;
			while ((t & (m >>= 1)) != 0) { 
				t &= ~m;
			}
			t |= m;
		}
	}
	
	/**
	 * Re-arrange in place, both real and imaginary part (based on LibRow implementation).
	 * @param realArray the input array of the real part of the complex numbers
	 * @param imArray the input array of the imaginary part of the complex numbers
	 */
	private void rearrangeIP(double[] realArray, double[] imArray) {
		int t = 0;// target
		int len = realArray.length;
		
		for (int i = 0; i < len; i++) {
			if (t > i) {
				double d = realArray[t];
				realArray[t] = realArray[i];
				realArray[i] = d;
				double di = imArray[t];
				imArray[t] = imArray[i];
				imArray[i] = di;
			}		
		
			int m = len;
			while ((t & (m >>= 1)) != 0) { 
				t &= ~m;
			}
			t |= m;
		}
	}
	
	/**
	 * The actual transform, based on LibRow implementation.
	 * 
	 * @param realArray the array of containing the real part of complex numbers
	 * @param imArray array containing the imaginary part of complex numbers
	 * @param inverse if {@code true} the inverse transform will be applied
	 */
	private void performIP(double[] realArray, double[] imArray, boolean inverse) {
		final double pi = inverse ? Math.PI : -Math.PI;
		int len = realArray.length;
		
		// i is step
		for (int i = 1; i < len; i <<= 1) {// 1, 2, 4, 8, ... powers of 2
			// j is jump
			final int j = i << 1; // 2, 4, 8, ...
			// angle increment
			double delta = pi / (double) i;
			// sine (delta / 2)
			double s = Math.sin(delta * 0.5d);
			// multiplier trigonometric recurrence
			//ComplexDouble multip = new ComplexDouble(-2.0d * s * s, Math.sin(delta));
			double multipre = -2.0d * s * s;
			double multipim = Math.sin(delta);
			// transform factor
			//ComplexDouble fact = new ComplexDouble(1.0d, 0.0d);
			double factre = 1.0d;
			double factim = 0.0d;
			// groups of different transform factors
			for (int g = 0; g < i; g++) {
				// within group
				for (int p = g; p < len; p += j) {
					// match position
					int mp = p + i;
					// second term for two point transform
					//ComplexDouble prod = ComplexDouble.mult(fact, inArray[mp]);
					double prodre = factre * realArray[mp] - factim * imArray[mp]; // cd1.real * cd2.real - cd1.img * cd2.img
					double prodim =  factre * imArray[mp] + factim * realArray[mp];// cd1.real * cd2.img + cd1.img * cd2.real
					// transform matching pos
					//inArray[mp] = ComplexDouble.subt(inArray[p], prod);
					realArray[mp] = realArray[p] - prodre;
					imArray[mp] = imArray[p] - prodim;
					// transform pair
					//inArray[p].add(prod);
					realArray[p] = realArray[p] + prodre;
					imArray[p] = imArray[p] + prodim;
				}
				
				//fact = ComplexDouble.add(ComplexDouble.mult(multip, fact), fact);
				// or
				//fact.add(ComplexDouble.mult(multip, fact));
				// Factor = Multiplier * Factor + Factor;
				// multiply first
				double factretemp = multipre * factre - multipim * factim;
				double factimtemp = multipre * factim + multipim * factre;
				// then add
				factre = factretemp + factre;
				factim = factimtemp + factim;
			}
		}
	}

	//======================================
	/**
	 * Java implementation of the in-place scale function of LibRow, called 
	 * for the inverse transform.
	 * @param ra the array of the real part of complex numbers
	 */
	public void scaleRealIP(double[] ra) {
		int n = ra.length;
		//double rad = 1 / Math.sqrt(n);
		double rad = 1 / (double) n;
		
    	for (int i = 0; i < n; i++) {
    		ra[i] = ra[i] * rad;
    	}
		
	}
    
	/**
	 * Java implementation of the in-place scale function of LibRow, called 
	 * for the inverse transform.
	 * @param ra the array of the real part of complex numbers
	 * @param im the array of the imaginary part of complex numbers
	 */
	public void scaleIP(double[] ra, double[] im) {
		int n = ra.length;
		double rad = 1 / (double) n;
		
    	for (int i = 0; i < n; i++) {
    		ra[i] = ra[i] * rad;
    		im[i] = im[i] * rad;
    	}
		
	}
	
	//#####################################
	// native implementation of LibRow
	//#####################################
	/**
	 * Native FFT based on LibRow code.
	 * 
	 * @param samples the input samples
	 * @return the result of the transform 
	 */
	public native double[] nFFTLR(double[] samples);//nForwardFTBothD
	/**
	 * Native FFT based on LibRow code returning absolute values.
	 * 
	 * @param samples the input samples
	 * @return the result of the transform
	 */
	public native double[] nFFTLRAbs(double[] samples);
	/**
	 * Native FFT based on LibRow code returning normalized values.
	 * 
	 * @param samples the input samples
	 * @return the result of the transform
	 */
	public native double[] nFFTLRNorm(double[] samples);
	/**
	 * Native FFT based on LibRow with options to customize the output.
	 * 
	 * @param samples the input samples
	 * @param normalize if {@code true} the result values will be normalized
	 * @param absolute if {@code true} absolute values will be returned
	 * @param trim if {@code true} the complex conjugates (roughly half of
	 * the array) will be removed
	 * @param divideBySize if {@code true} the values are scaled back, divided
	 * by the size of the array
	 * @param power if {@code true} the values are converted to the 
	 *        {@code 10 * Math.log10(n)} power value
	 * @param rootPower if {@code true} the values are converted to the 
	 *        {@code 20 * Math.log10(n)} root power value. This overrules the
	 *        {@code power} parameter.
	 * @param zeroPowerLimit if {@code true} the power values will be limited 
	 *        to {@code >= 0} (the lower threshold to prevent negative values).
	 *        Only meaningful in combination with {@code power} or 
	 *        {@code rootPower} parameters.
	 * @return the resulting array
	 */
	public native double[] nFFTLROpt(double[] samples, boolean normalize, 
			boolean absolute, boolean trim, boolean divideBySize, boolean power, 
			boolean rootPower, boolean zeroPowerLimit);
	// could add 2 x inverse variants
	// could add scaling?
	
	//#####################################
	// Rosetta based Java, C and C++ implementation
	//##################################### 
	/**
	 * Rosetta based Java implementation of Fast Fourier Transform.
	 * Incomplete!
	 * @param realArray the real part of the complex numbers
	 * @param normalize if {@code true} the result values will be normalized
	 * @param absolute if {@code true} absolute values will be returned.
	 * @param trim if {@code true} the complex conjugates (roughly half of
	 * the array) will be removed
	 * @param divideBySize if {@code true} the values are scaled back, divided
	 * by the size of the array
	 * @param power if {@code true} the values of the resulting array are the 
	 * power of two of the primary results
	 * @return the resulting array
	 */
	public double[] jFFTRos(double[] realArray, boolean normalize, 
			boolean absolute, boolean trim, boolean divideBySize, boolean power) {
		// the length of the array should be a power of 2
		if (realArray == null || realArray.length == 0 || (realArray.length & realArray.length - 1) != 0) {
			return null;
		}
		Complex[] cmp = new Complex[realArray.length];
		for (int i = 0; i < realArray.length; i++) {
			cmp[i] = new Complex(realArray[i], 0.0d);
		}
		
		fft(cmp);
		
		for (int i = 0; i < realArray.length; i++) {
			realArray[i] = cmp[i].re;
		}
		return realArray;
	}
	
	private static int bitReverse(int n, int bits) {
        int reversedN = n;
        int count = bits - 1;
 
        n >>= 1;
        while (n > 0) {
            reversedN = (reversedN << 1) | (n & 1);
            count--;
            n >>= 1;
        }
 
        return ((reversedN << count) & ((1 << bits) - 1));
    }
 
    private void fft(Complex[] buffer) {
 
        int bits = (int) (Math.log(buffer.length) / Math.log(2));
        for (int j = 1; j < buffer.length / 2; j++) {
 
            int swapPos = bitReverse(j, bits);
            Complex temp = buffer[j];
            buffer[j] = buffer[swapPos];
            buffer[swapPos] = temp;
        }
 
        for (int N = 2; N <= buffer.length; N <<= 1) {
            for (int i = 0; i < buffer.length; i += N) {
                for (int k = 0; k < N / 2; k++) {
 
                    int evenIndex = i + k;
                    int oddIndex = i + k + (N / 2);
                    Complex even = buffer[evenIndex];
                    Complex odd = buffer[oddIndex];
 
                    double term = (-2 * Math.PI * k) / (double) N;
                    Complex exp = (new Complex(Math.cos(term), Math.sin(term)).mult(odd));
 
                    buffer[evenIndex] = even.add(exp);
                    buffer[oddIndex] = even.sub(exp);
                }
            }
        }
    }
    
    /**
     * A class representing complex numbers, containing the real and imaginary
     * parts and methods for addition, subtraction and multiplication of 
     * complex numbers.
     *
     */
    class Complex {
    	/** the real part */
        public final double re;
        /** the imaginary part */
        public final double im;
     
        /**
         * Creates a new complex number instance.
         */
        public Complex() {
            this(0, 0);
        }
     
        /**
         * Creates a new complex number instance.
         * 
         * @param r the real part
         * @param i the imaginary part
         */
        public Complex(double r, double i) {
            re = r;
            im = i;
        }
        
        /**
         * Adds a complex number to this complex and returns a new
         * complex number.
         * 
         * @param b the complex number to add to this
         * 
         * @return the resulting complex number
         */
        public Complex add(Complex b) {
            return new Complex(this.re + b.re, this.im + b.im);
        }
     
        /**
         * Subtracts a complex number from this complex and returns new
         * complex number.
         * 
         * @param b the complex to subtract from this
         * 
         * @return the resulting complex number
         */
        public Complex sub(Complex b) {
            return new Complex(this.re - b.re, this.im - b.im);
        }
     
        /**
         * Multiplies this complex with another complex number and returns a
         * new complex number.
         * 
         * @param b the complex to multiply with this
         * 
         * @return the resulting complex number
         */
        public Complex mult(Complex b) {
            return new Complex(this.re * b.re - this.im * b.im,
                    this.re * b.im + this.im * b.re);
        }
     
        @Override
        public String toString() {
            return String.format("(%f,%f)", re, im);
        }
    }
    
    /**
     * Native FFT based on Rosetta code.
     * 
     * @param samples the input samples
     * @return the result of the transform
     */
    public native double[] nFFTRos(double[] samples);// doesn't perform well
    /**
     * Alternative native FFT based on Rosetta code.
     * 
     * @param samples the input samples
     * @return the result of the transform
     */
    public native double[] nFFTRosPlus(double[] samples);
    //##################
    // end Rosetta based
    //##################
    
    //##################
    // Java implementation of FFT by Allan van Hulst
    // Slightly modified for the purpose of comparison
    //##################
    /**
     * FFT implementation in Java with two input arrays, for the real and
     * imaginary parts.
     * 
     * @param inputReal the real array, will be modified
     * @param inputImagx the imaginary part array, same size as the real array,
     * will be modified
     * @param DIRECT direct flag, determines the sign of a constant in the
     * calculations  
     * @return the modified input real array 
     */
    public static double [] fft (double [] inputReal, double [] inputImagx, boolean DIRECT) 
    {
      int n = inputReal.length;

      double ld = Math.log(n) / Math.log(2.0);

      if (((int) ld) - ld != 0) 
        {
          System.out.println("The number of elements is not a power of 2.");
          return null;
        }
      
      // instead of requiring an input array for the imaginary part
	  double[] inputImag = new double[n];
	  Arrays.fill(inputImag, 0.0d);
	  
      int nu = (int) ld;
      int n2 = n / 2;
      int nu1 = nu - 1;
      // turn it into an in place function instead of copying the arrays
//      double [] xReal = new double [n];
//      double [] xImag = new double [n];
	  double [] xReal = inputReal;
	  double [] xImag = inputImag; 
      double tReal, tImag, p, arg, c, s;

      double constant;
      if (DIRECT)
          constant = -2 * Math.PI;
      else
          constant = 2 * Math.PI;
      /* without copying
      for (int i = 0; i < n; i++) 
        {
          xReal[i] = inputReal[i];
          xImag[i] = inputImag[i];
        }
	*/
      int k = 0;
      for (int l = 1; l <= nu; l++) 
        {
          while (k < n) 
            {
              for (int i = 1; i <= n2; i++) 
                {
                  p = bitreverseReference(k >> nu1, nu);
                  
                  arg = constant * p / n;
                  c = Math.cos(arg);
                  s = Math.sin(arg);
                  tReal = xReal[k + n2] * c + xImag[k + n2] * s;
                  tImag = xImag[k + n2] * c - xReal[k + n2] * s;
                  xReal[k + n2] = xReal[k] - tReal;
                  xImag[k + n2] = xImag[k] - tImag;
                  xReal[k] += tReal;
                  xImag[k] += tImag;
                  k++;
                }

              k += n2;
            }
          k = 0;
          nu1--;
          n2 /= 2;
        }

      k = 0;
      int r;
      while (k < n) 
        {
          r = bitreverseReference(k, nu);
          if (r > k) 
            {
              tReal = xReal[k];
              tImag = xImag[k];
              xReal[k] = xReal[r];
              xImag[k] = xImag[r];
              xReal[r] = tReal;
              xImag[r] = tImag;
            }

          k++;
        }
      // to have results equal to other fft implementation, return this
      return xReal;
      
      // the following multiplication leads to different outcomes as other fft implementations
      // the processing after this method can still lead to the same or similar results
      /*
      double [] newArray = new double [xReal.length * 2];
      double radice = 1 / Math.sqrt(n);

      for (int i = 0; i < newArray.length; i += 2) {
          int i2 = i / 2;
          newArray[i] = xReal[i2] * radice;
          newArray[i + 1] = xImag[i2] * radice;
      }

      return newArray;
      */
    }

    private static int bitreverseReference (int j, int nu) 
    {
      int j2, j1 = j, k = 0;

      for (int i = 1 ; i <= nu ; i++) 
        {
          j2 = j1 / 2;
          k  = 2 * k + j1 - 2 * j2;
          j1 = j2;
        }

      return k;
    }

    
}
