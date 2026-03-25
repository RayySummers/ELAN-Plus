package nl.mpi.media.spectrogram;

import java.util.Arrays;

/**
 * A class to transform audio samples from the time domain to the frequency
 * domain by means of a Fast Fourier Transform (FFT). Optionally a weighting
 * window can be applied. 
 * <br>
 * Additionally an experimental method allows to apply an inverse transform to
 * sub-ranges of the frequency bins produced by an FFT transform. It would 
 * allow to produce waveform visualizations of specific ranges of frequencies.
 * This is currently not used. 
 */
public class Frequency {
	private static FFT fft = new FFT();
	
	/**
	 * Conditionally loads and applies a weighting window function to the
	 * samples when passing sliding windows of the samples to the Fourier 
	 * transform. 
	 * 
	 * @param samples the loaded audio samples, not {@code null}
	 * @param numSamplesToUse the number of samples to use for the transform,
	 * less than or equal to the number of loaded samples 
	 * @param specSettings the user's spectrogram settings object, not 
	 * {@code null}
	 * 
	 * @return a two-dimensional array of "frequency bins", each array of bins
	 * representing one column, one window of the samples, each bin 
	 * representing the power or intensity of a range of frequencies
	 * 
	 * @see {@link FFT}
	 */
	public static double[][] getFrequencies(int[] samples, int numSamplesToUse, 
			SpectrogramSettings specSettings) {
		int sampleFrequency = (int) specSettings.getSampleFrequency();
		int samplerPosMinSample = (int) -(Math.pow(2, specSettings.getBitsPerSample()) / 2); 
		int samplerPosMaxSample = (int) (Math.pow(2, specSettings.getBitsPerSample()) / 2) - 1;
		if (specSettings.getNumSamplesPerWindow() == 0 || specSettings.getNumSamplesPerStride() == 0 || 
				specSettings.getSampleFrequency() != sampleFrequency) {		
				specSettings.setSampleFrequency(sampleFrequency);
		}
	    int samplesPerWindow = specSettings.getNumSamplesPerWindow();
	    int samplesPerStride = specSettings.getNumSamplesPerStride();
	    double[] weightingWindow = WindowFunction.windowForName(
					WindowFunction.getWFName(specSettings.getWindowFunction()), 
					samplesPerWindow);	
		
		int numWindows = (numSamplesToUse - samplesPerWindow) / samplesPerStride;
		if (numWindows <= 0) {
			return null;
		}

		while (numWindows * samplesPerStride < numSamplesToUse - samplesPerWindow) {
			numWindows++;
		}

		double[][] columnArray = new double[numWindows][];
		boolean power = (specSettings.getAmplUnit() == SpectrogramSettings.AMPL_UNIT.POWER);
		boolean rootPower = (specSettings.getAmplUnit() == SpectrogramSettings.AMPL_UNIT.ROOT_POWER);

		double ma = (double) samplerPosMaxSample;
		for (int ri = 0, w = 0; ri < numSamplesToUse - samplesPerWindow && w < columnArray.length; 
				ri += samplesPerStride, w++) {
			double[] ra = new double[samplesPerWindow];
			// fill array
			for (int k = 0; k < samplesPerWindow; k++) {
				// raw values and window applied, change this if normalized input values are required
				if (weightingWindow != null) {
					if (specSettings.isNormalizedInputData()) {
						ra[k] = (samples[k + ri] / ma) * weightingWindow[k];
					} else {
						ra[k] = samples[k + ri] * weightingWindow[k];
					}
				} else {
					if (specSettings.isNormalizedInputData()) {
						ra[k] = samples[k + ri] / ma;
					} else {
						ra[k] = samples[k + ri];
					}
				}
			}
			
			double[] fa = fft.jFFTLROpt(ra, true, true, true, true, power, rootPower, false);
			columnArray[w] = fa;
		}
		
		if (power) {
			specSettings.setAdaptiveMinimum(10 * Math.log10(FFT.meps));
		} else if (rootPower) {
			specSettings.setAdaptiveMinimum(20 * Math.log10(FFT.meps));
		} else {
			specSettings.setAdaptiveMinimum(samplerPosMinSample);
		}
		
		return columnArray;
	}
	
	/*
	 * Creates audio samples for ranges of frequencies. The spectrogram settings
	 * object contains the preferred or user specified visible range of frequencies
	 * and the number of ranges parameter determines for how many sub-ranges of
	 * those frequencies audio samples should be (re)constructed.
	 * E.g. if the visible range is 0 - 5000 Hz and 4 sub-ranges should be created,
	 * 4 waveforms for ranges approximately 0-1250, 1250-2500, 2500-3750 and 3750-5000 Hz
	 * should be produced. 
	 */
	public static int[][] getSampleRanges(int[] audioSamples, SpectrogramSettings specSettings, int numRanges) {
		// check parameters, e.g. mRanges <= 4
		// preparations, variables concerning the input data and settings 
		int sampleFrequency = (int) specSettings.getSampleFrequency();
		if (specSettings.getNumSamplesPerWindow() == 0 || specSettings.getNumSamplesPerStride() == 0 || 
				specSettings.getSampleFrequency() != sampleFrequency) {		
				specSettings.setSampleFrequency(sampleFrequency);
		}
	    int totalSamplesPerWindow = specSettings.getNumSamplesPerWindow();
	    //int totalSamplesPerStride = specSettings.getNumSamplesPerStride();
	    // ignoring stride (i.e. the stride is the same size as the window) seems to result in better sound quality in the sub ranges
	    // 75% overlap sounds even better (with the default Hann window)
	    int totalSamplesPerStride = totalSamplesPerWindow / 4;
	    double[] weightingWindow = WindowFunction.windowForName(
					WindowFunction.getWFName(specSettings.getWindowFunction()), 
					totalSamplesPerWindow);	
		
		int numWindows = (audioSamples.length - totalSamplesPerWindow) / totalSamplesPerStride;
		if (numWindows <= 0) {
			return null;
		}
		
		double posMaxFreq = specSettings.getPossibleMaxFrequency();
		double freqsPerBin = posMaxFreq / totalSamplesPerWindow; // number of bins = total samples per window
		
		// variables concerning the selection of displayed frequencies, usually an interval
		// within the range of possible frequencies
		double visibleRange = specSettings.getMaxDisplayFrequency() - specSettings.getMinDisplayFrequency();
		double selectionRatio = visibleRange / posMaxFreq;
		double selectionSamplesPerWindow = totalSamplesPerWindow * selectionRatio;
		double selectionSamplesPerStride = totalSamplesPerStride * selectionRatio;
		int subRangeSamplesPerWindow = (int) Math.round(selectionSamplesPerWindow / numRanges);
		int subRangeSamplesPerStride = (int) Math.round(selectionSamplesPerStride / numRanges);

		double freqPerRangeDouble = visibleRange / numRanges;
		int outSamplesPerRange = (int) (freqPerRangeDouble / freqsPerBin);
		int numBinsPerRange = outSamplesPerRange;
		// adjust the number of bins per range to a power of two
		int power2 = 2;
		while (power2 < numBinsPerRange) {	
			power2 <<= 1;//power2 *= 2;
		}
		// currently always take the size bigger than the calculated number of bins per range,
		// which is not necessarily the closest (to avoid that frequencies are 'lost', gaps between the ranges)
		numBinsPerRange = power2;
		// rounded down
		int firstBinIndex = (int) (specSettings.getMinDisplayFrequency() / freqsPerBin);
		int numSamplesPerRange = (numWindows - 1) * subRangeSamplesPerStride + subRangeSamplesPerWindow;
		int[][] sampleRanges = new int[numRanges][numSamplesPerRange];
		double[] realArray = new double[totalSamplesPerWindow];
		double[] imArray = new double[totalSamplesPerWindow];
		//  step 1: iterate windows of the audio samples and transform them to (bins of) frequencies
		for (int w = 0; w < numWindows; w++) {
			Arrays.fill(realArray, 0d);
			Arrays.fill(imArray, 0d);
			int strideIndex = w * totalSamplesPerStride;
			int numsamplesToCopy = totalSamplesPerWindow;
			if (audioSamples.length - strideIndex < totalSamplesPerWindow) {
				numsamplesToCopy = audioSamples.length - strideIndex;
			}
			// in case of a weighting window, apply it here
			for (int i = 0; i < numsamplesToCopy; i++) {
				if (weightingWindow != null) {
					realArray[i] = audioSamples[strideIndex + i] * weightingWindow[i];
				} else {
					realArray[i] = audioSamples[strideIndex + i];
				}
			}		
			
			fft.forwardFTIP(realArray, imArray);
			
			// step 2: check the frequency ranges based on the settings (visible frequency range
			// and the number of requested ranges. Create 'slices' of real and imaginary arrays
			// corresponding (more or less) to those ranges, based on indexes of the bins
			double[] realRange = new double[numBinsPerRange];
			double[] imRange = new double[numBinsPerRange];
		    double[] inverseWindow = WindowFunction.windowForName(
						WindowFunction.getWFName(specSettings.getWindowFunction()), 
						numBinsPerRange);

			for (int range = 0; range < numRanges; range++) {
				Arrays.fill(realRange, 0d);
				Arrays.fill(imRange, 0d);
				int startIndex = firstBinIndex + range * numBinsPerRange;
				int numBinsToCopy = numBinsPerRange;
				if (realArray.length - startIndex < numBinsToCopy) {
					numBinsToCopy = realArray.length - startIndex;
				}
				System.arraycopy(realArray, startIndex, realRange, 0, numBinsToCopy);
				System.arraycopy(imArray, startIndex, imRange, 0, numBinsToCopy);
				// perform inverse transform
				fft.inverseFTIP(realRange, imRange);
				
				// add the real values as int samples to the correct sample range at the correct position
				int insertStartIndex = w * subRangeSamplesPerStride;
				// correct start index for the window stride, which determines the starting point 
				// of where to insert the audio samples
				for (int s = 0; s < realRange.length && insertStartIndex + s < sampleRanges[range].length; s++) {
					double value = realRange[s];
					if (inverseWindow != null) {
						value *= inverseWindow[s];
					}
					sampleRanges[range][insertStartIndex + s] = 
							(sampleRanges[range][insertStartIndex + s] + (int) Math.round(value)) / 2;
				}
			}
		}
		
		return sampleRanges;
	}
	
	/**
	 * Calculates the minimum and maximum frequency of a number of sub-ranges of 
	 * the specified input frequency range. The result is not necessarily a 
	 * simple division of the input range, the calculation takes into account
	 * that the subranges have to be based on a power of two number of frequency
	 * bins. The current implementation determines the lower boundary of each
	 * sub-range by subdivision of the input range and the upper boundary based
	 * on the size and number of frequency bins. The subranges can overlap.
	 * E.g. if the input range is 0-5000Hz and the number of  subranges is 4, 
	 * the values returned could be something like 
	 * [[0,1500],[1250,2750],[2500,4000],[3750,5250]].
	 *    
	 * @param minFrequency the lower frequency boundary of the input range 
	 * @param maxFrequency the upper frequency boundary of the input range
	 * @param freqPerBin the number of frequencies in each frequency bin
	 * @param numRanges the number of requested subranges, probably 2 or 4
	 * 
	 * @return a two-dimensional array, each element is an array of size 2,
	 * the lower and upper boundary of a sub-range
	 */
	public static int[][] getSubRangeBoundaries(double minFrequency, double maxFrequency, 
			double freqPerBin, int numRanges) {
		int[][] boundaries = new int[numRanges][2];
		double totalRange = maxFrequency - minFrequency;
		double freqPerRange = totalRange / numRanges;
		int numBinsPerRange = (int) (freqPerRange / freqPerBin);
		// make it a power of 2
		// adjust the number of bins per range to a power of two
		int power2 = 2;
		while (power2 < numBinsPerRange) {	
			power2 <<= 1;//power2 *= 2;
		}
		numBinsPerRange = power2;
		double adjustedFreqPerRange = numBinsPerRange * freqPerBin;
		
		for (int i = 0; i < numRanges; i++) {
			boundaries[i][0] = (int) (minFrequency + (i * freqPerRange));
			boundaries[i][1] = boundaries[i][0] + (int) adjustedFreqPerRange;
		}
		
		return boundaries;
	}
}
