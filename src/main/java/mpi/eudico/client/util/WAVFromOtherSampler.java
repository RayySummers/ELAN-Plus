package mpi.eudico.client.util;

import java.io.IOException;
import java.util.Arrays;

import nl.mpi.media.AudioExtraction;
import nl.mpi.media.AudioExtractor;
import nl.mpi.media.FFAudioExtractor;
import nl.mpi.media.UnsupportedMediaException;

/**
 * A {@code WAVE} sampler for other files than local, uncompressed PCM 
 * {@code .wav} files. Tries to make use of functionality of a native
 * media framework to decode the audio of a video file or of an audio file of a 
 * type other than (local, uncompressed) {@code .wav} files. 
 * This sampler uses an {@link AudioExtractor} instance which builds on 
 * {@code JNI} to interact with the native decoder.
 *  
 * @author Han Sloetjes
 */
public class WAVFromOtherSampler implements WAVSamplesProvider {
	private String mediaUrlString;
	private AudioExtraction audioExtractor;
	
    private int possibleMaxSample;
    private int possibleMinSample;
    private int[] maxSamplePerChannel;
    private int[] minSamplePerChannel;
    private int[][] intArrayPerChannel;
    
    private double seekTime;
	
    /**
     * Constructs a sampler for the specified media file, if it is supported.
     * <p>
     * This sampler currently applies the same strategy to get the samples of 
     * an interval as the older, existing {@code WAVSampler}:
     * <ul>
     * <li>seek or set the reader position to the start of the interval 
     * ({@link #seekTime(float)} or {@link #seekSample(long)})
     * <li>call {@link #readInterval(int, int)}
     * <li>get the data by calls to {@link #getChannelArray(int)} for each 
     * channel
     * </ul>
     * 
     * Therefore this sampler should not be used from different threads 
     * concurrently.
     *  
     * @param mediaPath the media file path or URL
     * 
     * @throws IOException any I/O related exception 
     * @throws UnsupportedMediaException if the media is not supported by the 
     * framework or if the framework could not be initialized etc. 
     */
	public WAVFromOtherSampler(String mediaPath) throws IOException, UnsupportedMediaException {
		this(mediaPath, null);
	}
	
    /**
     * Constructs a sampler for the specified media file, if it is supported
     * by the preferred framework.
     * <p>
     * This sampler currently applies the same strategy to get the samples of 
     * an interval as the older, existing {@code WAVSampler}:
     * <ul>
     * <li>seek or set the reader position to the start of the interval 
     * ({@link #seekTime(float)} or {@link #seekSample(long)})
     * <li>call {@link #readInterval(int, int)}
     * <li>get the data by calls to {@link #getChannelArray(int)} for each 
     * channel
     * </ul>
     * 
     * Therefore this sampler should not be used from different threads 
     * concurrently.
     *  
     * @param mediaPath the media file path or URL
     * @param prefFramework the framework to use for extracting the audio
     * samples
     * 
     * @throws IOException any I/O related exception 
     * @throws UnsupportedMediaException if the media is not supported by the 
     * preferred framework or if the framework could not be initialized etc. 
     */
	public WAVFromOtherSampler(String mediaPath, String prefFramework) throws IOException, UnsupportedMediaException {		
		// check type and create an AudioExtractor if possible
        String urlString = mediaPath;
        if (urlString.startsWith("file:") &&
                !urlString.startsWith("file:///")) {// ?? or remove 2 of the 3 slashes
            urlString = urlString.substring(5);
            // or 
//    		if (urlString.startsWith("///")) {
//    			// remove two slashes, /C:/etc/etc seems to work on Windows 
//    			urlString = urlString.substring(2);
//    		}
        }
        mediaUrlString = urlString;
        if (prefFramework != null && prefFramework.equals("FFmpeg")) {//PlayerFactory.FFMPEG
        	audioExtractor = new FFAudioExtractor(mediaUrlString);
        } else {
        	audioExtractor = new AudioExtractor(mediaUrlString);
        }
		
		initTest();
	}
	
	/**
	 * Tests if some required audio properties have been detected properly by
	 * the native framework.
	 * 
	 * @throws UnsupportedMediaException if a property has not been properly detected
	 */
	private void initTest() throws UnsupportedMediaException {
		if (audioExtractor != null) {
			if (audioExtractor.getBitsPerSample() <= 0) {
				throw new UnsupportedMediaException("The native AudioExtractor could not detect the bits per sample property.");
			}
			if (audioExtractor.getSampleFrequency() <= 0) {
				throw new UnsupportedMediaException("The native AudioExtractor could not detect the sample frequency property.");
			}
			if (audioExtractor.getDuration() <= 0) {
				throw new UnsupportedMediaException("The native AudioExtractor could not detect the media duration property.");
			}
			if (getPossibleMaxSample() == 0) {
				throw new UnsupportedMediaException("The possible maximum sample value could not be calculated");
			}
			if (getPossibleMinSample() == 0) {
				throw new UnsupportedMediaException("The possible minimum sample value could not be calculated");
			}
		}
	}

	@Override
	public String getMediaLocation() {
		return mediaUrlString;
	}

	@Override
	public float getDuration() {
		return (float) audioExtractor.getDuration();
	}

	@Override
	public double getDurationSeconds() {
		return audioExtractor.getDurationSec();
	}

	@Override
	public int getNumberOfChannels() {
		return audioExtractor.getNumberOfChannels();
	}

	@Override
	public int getBitsPerSample() {
		return audioExtractor.getBitsPerSample();
	}

	@Override
	public int[] getChannelArray(int channelIndex) {
		if (intArrayPerChannel != null && channelIndex < intArrayPerChannel.length) {
			return intArrayPerChannel[channelIndex];
		}
		return null;
	}

	@Override
	public int getMaxSample(int channelIndex) {
		if (maxSamplePerChannel == null) {
			if (intArrayPerChannel != null && channelIndex < intArrayPerChannel.length) {
				maxSamplePerChannel = new int[intArrayPerChannel.length];
				for (int c = 0; c < intArrayPerChannel.length; c++) {
					int max = Integer.MIN_VALUE;
					for (int i = 0; i < intArrayPerChannel[c].length; i++) {
						if (intArrayPerChannel[c][i] > max) max = intArrayPerChannel[c][i];
					}
					if (max == Integer.MIN_VALUE) max = 0;
					maxSamplePerChannel[c] = max;
				}
			}
		}
		
		if (maxSamplePerChannel != null && channelIndex < maxSamplePerChannel.length) {
			return maxSamplePerChannel[channelIndex];
		}
		
		return 0;
	}

	@Override
	public int getMinSample(int channelIndex) {
		if (minSamplePerChannel == null) {
			if (intArrayPerChannel != null && channelIndex < intArrayPerChannel.length) {
				minSamplePerChannel = new int[intArrayPerChannel.length];
				for (int c = 0; c < intArrayPerChannel.length; c++) {
					int min = Integer.MAX_VALUE;
					for (int i = 0; i < intArrayPerChannel[c].length; i++) {
						if (intArrayPerChannel[c][i] < min) min = intArrayPerChannel[c][i];
					}
					if (min == Integer.MAX_VALUE) min = 0;
					minSamplePerChannel[c] = min;
				}
			}
		}
		
		if (minSamplePerChannel != null && channelIndex < minSamplePerChannel.length) {
			return minSamplePerChannel[channelIndex];
		}
		
		return 0;
	}

	/**
	 * Returns an approximation of the number of samples in the file, the 
	 * product of the duration in seconds and the sample frequency.
	 * 
	 * @return (an approximation of) the number of samples in the file
	 */
	@Override
	public long getNrOfSamples() {
		// probably revise
		return (long) (audioExtractor.getDurationSec() * 
				audioExtractor.getSampleFrequency());
	}

	@Override
	public int getPossibleMaxSample() {
		if (possibleMaxSample == 0) {
			possibleMaxSample = (int) (-1 + Math.pow(2, 
					audioExtractor.getBitsPerSample() - 1));
		}
		return possibleMaxSample;
	}

	@Override
	public int getPossibleMinSample() {
		if (possibleMinSample == 0) {
			possibleMinSample = (int) (-Math.pow(2, 
					audioExtractor.getBitsPerSample() - 1));
		}
		return possibleMinSample;
	}

	@Override
	public int getSampleFrequency() {
		return audioExtractor.getSampleFrequency();
	}

	@Override
	public long getSamplePointer() {
		return (long) (audioExtractor.getPositionSec() * audioExtractor.getSampleFrequency());
	}

	@Override
	public long getTimeAtSample(long sample) {
		// maybe revise?
		return (long) (((sample * 1000f) / audioExtractor.getSampleFrequency()) + .5);
	}

	@Override
	public float getTimePointer() {
		// for now return seekTime, it may not be possible to get the position from the decoder?
		return (float) (audioExtractor.getPositionSec() * 1000);
	}

	@Override
	public double getTimePointerSeconds() {
		return audioExtractor.getPositionSec();
	}

	@Override
	public void close() {
		try {
			audioExtractor.release();
		} catch (Throwable t) {
			
		}

	}

	@Override
	public int readInterval(int requestedNrOfSamplesToRead, int nrOfChannelsToLoad) {
		int actualRead = 0;
        boolean stereoOutput = false;

        int actualNrOfSamplesToRead = requestedNrOfSamplesToRead;

        int samplesAvailable = (int) (getNrOfSamples() - getSamplePointer());

        if (requestedNrOfSamplesToRead > samplesAvailable) {
            actualNrOfSamplesToRead = samplesAvailable;
        }
        int encNrOfChannels = getNumberOfChannels();
        int actualNrOfChannels = nrOfChannelsToLoad < encNrOfChannels ? 
        		nrOfChannelsToLoad : encNrOfChannels;
        // if 1 channel is requested and more (2) are available, the channels are merged 
        stereoOutput = nrOfChannelsToLoad > 1;

        if ((intArrayPerChannel == null) || intArrayPerChannel.length < nrOfChannelsToLoad || 
        		(intArrayPerChannel[0].length < actualNrOfSamplesToRead)) {
        	intArrayPerChannel = new int[actualNrOfChannels][actualNrOfSamplesToRead];
        } else {
        	for(int j = 0; j < intArrayPerChannel.length; j++) {
        		Arrays.fill(intArrayPerChannel[j], 0);
        	}
        }
        
        short sampleSize = (short) (encNrOfChannels * (getBitsPerSample() / 8)  /* % 8*/);
        if (sampleSize == 0) {
        	sampleSize = 1;
        }
        byte[] buffer = null;

        // actual reading
        buffer = audioExtractor.getSamples(seekTime, 
        		seekTime + actualNrOfSamplesToRead / (double) audioExtractor.getSampleFrequency());
//        System.out.println("Get samples from: " + seekTime + " to: " + (seekTime + actualNrOfSamplesToRead / (double) audioExtractor.getSampleFrequency()));
//        System.out.println("Requested (samples): " + requestedNrOfSamplesToRead);
//        System.out.println("Reading (samples):   " + actualNrOfSamplesToRead);
//        System.out.println("Reading (bytes):     " + actualNrOfSamplesToRead * sampleSize);
        //System.out.println("Bytes read:          " + actualRead);
        if (buffer == null || buffer.length == 0) {
        	// log
        	return actualRead;
        } else {
        	actualRead = buffer.length;
        }
        

        // 8 bit mono
        if (sampleSize == 1) {
            for (int s = 0; s < actualNrOfSamplesToRead; s++) {
                intArrayPerChannel[0][s] = buffer[s] & 0xFF;
            }
        }
        else if (sampleSize == 2) {
        	// 16 bit mono
            if (actualNrOfChannels == 1) {
                int b = 0;
                int b1;
                int b2;

                for (int s = 0; s < actualNrOfSamplesToRead && b < buffer.length - 1; s++) {
                    b1 = buffer[b] & 0xFF;
                    b2 = buffer[b + 1];
                    intArrayPerChannel[0][s] = b1 | (b2 << 8);
                    b += 2;
                }
            }
            // 8 bit stereo
            else {
                int b = 0;
                int b1;

                for (int s = 0; s < actualNrOfSamplesToRead && b < buffer.length - 1; s++) {
                    // channel 1
                    b1 = buffer[b] & 0xFF;
                    intArrayPerChannel[0][s] = b1;

                    // channel 2
                    b1 = buffer[b + 1] & 0xFF;

                    if (stereoOutput) {
                        intArrayPerChannel[1][s] = b1;
                    }
                    else {
                    	intArrayPerChannel[0][s] = (intArrayPerChannel[0][s] + b1) / 2;
                    }

                    b += 2;
                }
            }
        }
        // 24 bit mono
        else if ((sampleSize == 3) && (encNrOfChannels == 1)) {
            int b = 0;
            int b1;
            int b2;
            int b3;

            for (int s = 0; s < actualNrOfSamplesToRead && b < buffer.length - 2; s++) {
                b1 = buffer[b] & 0xFF;
                b2 = buffer[b + 1] & 0xFF;
                b3 = buffer[b + 2];
                intArrayPerChannel[0][s] = b1 | (b2 << 8) | (b3 << 16);
                b += 3;
            }
        }
        // 16 bit stereo
        else if ((sampleSize == 4) && (encNrOfChannels == 2)) {
            int b = 0;
            int b1;
            int b2;

            for (int s = 0; s < actualNrOfSamplesToRead && b < buffer.length - 3; s++) {
                // channel 1
                b1 = buffer[b] & 0xFF;
                b2 = buffer[b + 1];
                intArrayPerChannel[0][s] = b1 | (b2 << 8);

                // channel 2
                b1 = buffer[b + 2] & 0xFF;
                b2 = buffer[b + 3];

                if (stereoOutput) {
                    intArrayPerChannel[1][s] = b1 | (b2 << 8);
                }
                else {
                    intArrayPerChannel[0][s] = (intArrayPerChannel[0][s] + (b1 | (b2 << 8))) / 2;
                }

                b += 4;
            }
        }
        //24 bit stereo
        else if ((sampleSize == 6) && (encNrOfChannels == 2)) {
            int b = 0;
            int b1;
            int b2;
            int b3;

            for (int s = 0; s < actualNrOfSamplesToRead && b < buffer.length - 5; s++) {
                // channel 1
                b1 = buffer[b] & 0xFF;
                b2 = buffer[b + 1] & 0xFF;
                b3 = buffer[b + 2];

                intArrayPerChannel[0][s] = b1 | (b2 << 8) | (b3 << 16);

                // channel 2
                b1 = buffer[b + 3] & 0xFF;
                b2 = buffer[b + 4] & 0xFF;
                b3 = buffer[b + 5];

                if (stereoOutput) {
                    intArrayPerChannel[1][s] = b1 | (b1 << 8) | (b3 << 16);
                }
                else {
                    intArrayPerChannel[0][s] = (intArrayPerChannel[0][s] + (b1 | (b2 << 8) | (b3 << 16))) / 2;
                }

                b += 6;
            }
        }
        //32 bit mono
        else if ((sampleSize == 4) && (encNrOfChannels == 1)) {
            int b = 0;
            int b1;
            int b2;
            int b3;
            int b4;

            for (int s = 0; s < actualNrOfSamplesToRead && b < buffer.length - 3; s++) {
                b1 = buffer[b] & 0xFF;
                b2 = buffer[b + 1] & 0xFF;
                b3 = buffer[b + 2] & 0xFF;
                b4 = buffer[b + 3];

                intArrayPerChannel[0][s] = b1 | (b2 << 8) | (b3 << 16) | (b4 << 24);
                b += 4;
            }
        }
        //32 bit stereo
        else if ((sampleSize == 8) && (encNrOfChannels == 2)) {
            int b = 0;
            int b1;
            int b2;
            int b3;
            int b4;

            for (int s = 0; s < actualNrOfSamplesToRead && b < buffer.length - 7; s++) {
                // channel 1
                b1 = buffer[b] & 0xFF;
                b2 = buffer[b + 1] & 0xFF;
                b3 = buffer[b + 2] & 0XFF;
                b4 = buffer[b + 3];

                intArrayPerChannel[0][s] = convert(b1 | (b2 << 8) | (b3 << 16) | (b4 << 24));

                // channel 2
                b1 = buffer[b + 4] & 0xFF;
                b2 = buffer[b + 5] & 0xFF;
                b3 = buffer[b + 6] & 0xFF;
                b4 = buffer[b + 7];

                if (stereoOutput) {
                	intArrayPerChannel[1][s]  = convert(b1 | (b2 << 8) | (b3 << 16) | (b4 << 24));
                }
                else {
                	intArrayPerChannel[0][s] = (intArrayPerChannel[0][s] + convert(b1 | (b2 << 8) | (b3 << 16) | (b4 << 24))) / 2;
                }

                b += 8;
            }
        }
        
        //System.out.println("Returning (samples):     " + actualRead / sampleSize);
        //System.out.println("IntArrayLeft (samples):  " + intArrayLeft.length + " thread: " +Thread.currentThread());
        //System.out.println("IntArrayRight (samples): " + intArrayRight.length + " WavSampler: " + this);
        return actualRead / sampleSize;
	}

	@Override
	public void seekSample(long n) {
		// instead of real seeking, the requested sample value could be stored until the next call to readInterval
		// similarly thread unsafe as in WAVSampler
		
		setSeekTime( n / (double) audioExtractor.getSampleFrequency() );
	}

	@Override
	public void seekTime(float time) {
		setSeekTime((double) time / 1000d);
	}

	@Override
	public void seekTimeSeconds(double timeSec) {
		setSeekTime(timeSec);
	}
	
	private void setSeekTime(double t) {
		if (t < 0) {
			seekTime = 0.0d;
		} else if (t > audioExtractor.getDurationSec()) {
			seekTime = audioExtractor.getDurationSec();// ?? 0?
		} else {
			seekTime = t;
		}
		audioExtractor.setPositionSec(seekTime);
	}

	/**
	 * After decoding the extractor usually produces uncompressed PCM data,
	 * but sometimes IEEE float or an other format.
	 * 
	 * @return the {@code format tag} or compression code
	 */
	@Override
	public short getCompressionCode() {
		return (short)audioExtractor.getFormatTag();
	}

	/**
	 * Returns "Unknown", "PCM (uncompressed)", "IEEE float" or other format
	 * names that might be produced by the audio extractor.
	 * 
	 * @param compr the compression code or format tag
	 * @return a string representation of the format
	 */
	@Override
	public String getCompressionString(short compr) {
		if (compr == WAVHeader.WAVE_FORMAT_PCM) {
			return WAVHeader.formatDescriptions[WAVHeader.WAVE_FORMAT_PCM];
		} else if (compr == WAVHeader.WAVE_FORMAT_IEEE_FLOAT) {
			return WAVHeader.formatDescriptions[WAVHeader.WAVE_FORMAT_IEEE_FLOAT];
		}
		return WAVHeader.formatDescriptions[0];
	}
	
	/**
	 * Converts a sample value depending on the format tag or compression code.
	 * 
	 * @param sample the input sample
	 * @return a converted value in case of IEEE float input, otherwise the 
	 * input value  is returned
	 */
	private int convert(int sample) {
    	if (getCompressionCode() == WAVHeader.WAVE_FORMAT_IEEE_FLOAT) {
    		float f = Float.intBitsToFloat(sample);
    		return (int) (f * possibleMaxSample);
    	}
    	
		return sample;
	}

	@Override
	public void setDebugMode(boolean enable) {
		if (audioExtractor != null) {
			AudioExtractor.setDebugMode(enable);
		}
	}

}
