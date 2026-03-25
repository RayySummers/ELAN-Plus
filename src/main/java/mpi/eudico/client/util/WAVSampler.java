/* This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package mpi.eudico.client.util;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

/**
 * A class that provides samples from a local WAV file. Samples can be read
 * from random positions, a {@code RandomAccessFile} is used for seeking and
 * retrieving samples.
 * <p>
 * This class is not thread safe. In the current implementation retrieving samples
 * from a random position requires at least three method calls:
 * <ul>
 * <li>{@link WAVSampler#seekSample(long)} (or {@link #seekTime(float)})
 * <li>{@link #readInterval(int, int)}
 * <li>{@link #getChannelArray(int)}
 * </ul>
 * The retrieved bytes are converted to {@code int} arrays, one for each channel
 * (currently maximal 2).
 * <p>
 * The above three methods maybe could or should be replaced by a single method
 * combining the parameters of the first two and returning an {@code int[][]} 
 * array.
 * <p>
 * Most of the public methods of this class are now defined in an interface which
 * allows implementations building on existing frameworks to e.g. extract audio 
 * from a video file, other audio file types and/or from remote files. 
 *
 * @author Alexander Klassmann
 * @author Han Sloetjes
 */
public class WAVSampler implements WAVSamplesProvider {
	
    private RandomAccessFile soundFile;
    private String filePath;
    private WAVHeader wavHeader;
    private byte[] buffer;
    private int[] intArrayLeft;
    private int[] intArrayRight;
    private float duration;
    private double durationSec;
    private int headerSize;
    private int maxSampleFirst;
    private int maxSampleSecond;
    private int minSampleFirst;
    private int minSampleSecond;
    private int possibleMaxSample;
    private int possibleMinSample;
    private int sampleFrequency;
    private long nrOfSamples;
    private short bitsPerSample;
    private short compressionCode;
    private short nrOfChannels;
    private short sampleSize;// or BlockAlign, NumChannels * BitsPerSample/8
    private static boolean debugMode = false;

    /**
     * Construct the Samples object only for {@code .wav} files, otherwise an 
     * IOException is thrown.
     *
     * @param fileName the absolute path of the WAV file
     *
     * @throws IOException if the file appears not be a WAV file, any other IO
     * related exception
     */
    public WAVSampler(String fileName) throws IOException {
        buffer = new byte[4096];

        if (fileName.toLowerCase().endsWith(".wav")) {
            soundFile = new RandomAccessFile(fileName, "r");
            wavHeader = new WAVHeader(soundFile);
            filePath = fileName;
            
            if (wavHeader.getHeaderSize() == 0) {
            	soundFile.close();
            	throw new IOException("Invalid wav file format");
            }
            
            sampleFrequency = wavHeader.getFrequency();
            sampleSize = wavHeader.getSampleSize();
            nrOfSamples = wavHeader.getDataLength() / sampleSize;
            nrOfChannels = wavHeader.getNumberOfChannels();
            bitsPerSample = (short) ((sampleSize * 8) / nrOfChannels);
            duration = ((float) 1000 * nrOfSamples) / sampleFrequency;
            durationSec = nrOfSamples / (double) sampleFrequency;
            possibleMinSample = (int) (-Math.pow(2, bitsPerSample - 1));
            possibleMaxSample = (int) (-1 + Math.pow(2, bitsPerSample - 1));
            headerSize = wavHeader.getHeaderSize();
            compressionCode = wavHeader.getCompressionCode();

            if (compressionCode == WAVHeader.WAVE_FORMAT_ALAW) {
                possibleMinSample *= 64;
                possibleMaxSample *= 64;
            }           		
            
//			System.out.println("Information from header of wav-file:");
//			System.out.println("NrOfChannels    : " + nrOfChannels);
//			System.out.println("Sample frequency: " + sampleFrequency);
//			System.out.println("Bits per sample : " + bitsPerSample);
//			System.out.println("nrOfSamples     : " + nrOfSamples);
//			System.out.println("Duration (sec)  : " + 
//					String.format("%.3f", getTimeAtSample(nrOfSamples) / (float)1000));
//			System.out.println("WAVE Format     : " +
//			    ((compressionCode < WAVHeader.formatDescriptions.length)
//			    ? WAVHeader.formatDescriptions[compressionCode] : ("" + compressionCode)));
//			
//			if (!wavHeader.getInfo().equals("")) {
//			    System.out.println("\nMeta info tail:" + wavHeader.getInfo());
//			}
            
            soundFile.seek(headerSize);
        }
        else {
            throw new IOException("Unsupported file format");
        }
    }

	/**
	 * Returns the file path or URL as a string.
	 * 
	 * @return the media path as a string
	 */
	public String getMediaLocation() {
		 if (filePath != null) {
			 return filePath;
		 }
		 
		return "";
	}
	
	
    /**
     * Returns the total duration of the samples in milliseconds.
     *
     * @return the duration of the WAV file
     */
    @Override
	public float getDuration() {
        return duration;
    }

    @Override
	public double getDurationSeconds() {
		return durationSec;
	}

	@Override
	public int getNumberOfChannels() {
		return (int) nrOfChannels;
	}

	@Override
	public int getBitsPerSample() {
		return (int) bitsPerSample;
	}

	@Override
	public int[] getChannelArray(int channelIndex) {
		switch (channelIndex) {
		case 0:
			return intArrayLeft;
		case 1:
			return intArrayRight;
			default:
				return null;
		}
	}

	/**
     * Gets the array that stores the data of the first channel of an interval
     * that has been read.
     *
     * @return an array of {@code int}
     *
     * @see #readInterval(int, int)
     */
	public int[] getFirstChannelArray() {
        return intArrayLeft;
    }
    
    /**
     * Gets the array that stores the data of the second channel of an interval
     * that has been read.
     *
     * @return an array of {@code int}
     *
     * @see #readInterval(int, int)
     */
	public int[] getSecondChannelArray() {
        return intArrayRight;
    }

    @Override
	public int getMaxSample(int channelIndex) {
		switch (channelIndex) {
		case 0:
			return maxSampleFirst;
		case 1:
			return maxSampleSecond;
			default:
				return 0;
		}
	}

	/**
     * Returns the maximum value of the read samples of the first channel.
     *
     * @return the maximum value of the read samples #1
     */
	public int getMaxSampleFirst() {
        return maxSampleFirst;
    }

    /**
     * Returns the maximum value of the read samples of the second channel.
     *
     * @return the maximum value of the read samples #2
     */
	public int getMaxSampleSecond() {
        return maxSampleSecond;
    }

    @Override
	public int getMinSample(int channelIndex) {
		switch (channelIndex) {
		case 0:
			return minSampleFirst;
		case 1:
			return minSampleSecond;
			default:
				return 0;
		}
	}

	/**
     * Returns the minimum value of the read samples of the first channel.
     *
     * @return the minimum value of the read samples #1
     */
	public int getMinSampleFirst() {
        return minSampleFirst;
    }

    /**
     * Returns the minimum value of the read samples of the second channel.
     *
     * @return the minimal value of the read samples #2
     */
	public int getMinSampleSecond() {
        return minSampleSecond;
    }

    /**
     * Returns the total number of samples in the file.
     * 
     * @return the total number of samples
     */
    @Override
	public long getNrOfSamples() {
        return nrOfSamples;
    }

    /**
     * Returns the maximal possible value of the samples.
     *
     * @return the possible maximum sample value
     */
    @Override
	public int getPossibleMaxSample() {
        return possibleMaxSample;
    }

    /**
     * Returns the minimal possible value of the samples.
     *
     * @return the possible minimum sample value
     */
    @Override
	public int getPossibleMinSample() {
        return possibleMinSample;
    }

    /**
     * Returns the sample frequency (number of samples per second) of the file.
     *
     * @return the sample frequency
     */
    @Override
	public int getSampleFrequency() {
        return sampleFrequency;
    }

    /**
     * Returns the index of the current sample, the location of the sample
     * pointer. 
     *
     * @return the location of the sample pointer
     */
    @Override
	public long getSamplePointer() {
        long pointer = 0;

        try {
            pointer = (soundFile.getFilePointer() - headerSize) / sampleSize;
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return pointer;
    }


    /**
     * Returns time corresponding to a given sample index.
     *
     * @param sample the index or position of the sample
     *
     * @return the time in milliseconds
     */
    @Override
	public long getTimeAtSample(long sample) {
        return (long) (((sample * 1000f) / sampleFrequency) + .5);
    }

    /**
     * Returns the time in milliseconds corresponding to the current sample
     * index (the current location of pointer).
     *
     * @return the time in milliseconds at the pointer's position
     */
    @Override
	public float getTimePointer() {
        return ((float) 1000 * getSamplePointer()) / sampleFrequency;
    }

    @Override
	public double getTimePointerSeconds() {
		return getSamplePointer() / (double) sampleFrequency;
	}

	/**
     * Gets the <code>WavHeader</code>.
     *
     * @return the <code>WavHeader</code> object of this <code>WavSampler</code>
     */
	public WAVHeader getWavHeader() {
        return wavHeader;
    }

    /**
     * Closes the RandomAccessFile sets the {@code WAVHeader} to null.
     */
    @Override
	public void close() {
        wavHeader = null;

        if (soundFile != null) {
            try {
                soundFile.close();
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        soundFile = null;
    }

    /**
     * Reads the requested number of samples in one run and stores the extracted
     * values in one (mono or merged) or two (stereo) arrays of {@code int}. 
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
     * @see #getFirstChannelArray()
     * @see #getSecondChannelArray()
     */
    @Override
	public int readInterval(int requestedNrOfSamplesToRead, int nrOfChannelsToLoad) {
        int actualRead = 0;
        boolean stereoOutput = false;

        int actualNrOfSamplesToRead = requestedNrOfSamplesToRead;

        int samplesAvailable = (int) (getNrOfSamples() - getSamplePointer());

        if (requestedNrOfSamplesToRead > samplesAvailable) {
            actualNrOfSamplesToRead = samplesAvailable;
        }

        if ((intArrayLeft == null) || (intArrayLeft.length < actualNrOfSamplesToRead)) {
            intArrayLeft = new int[actualNrOfSamplesToRead];
        }

        Arrays.fill(intArrayLeft,  0);

        if (nrOfChannelsToLoad == 2) {
            stereoOutput = true;

            if ((intArrayRight == null) || (intArrayRight.length < actualNrOfSamplesToRead)) {
                intArrayRight = new int[actualNrOfSamplesToRead];
            }

            Arrays.fill(intArrayRight, 0);
        }
        else {
            nrOfChannelsToLoad = 1;
            intArrayRight = null;
        }

        if (buffer.length < (actualNrOfSamplesToRead * sampleSize)) {
            buffer = new byte[actualNrOfSamplesToRead * sampleSize];
        }

        // actual reading
        try {
            actualRead = soundFile.read(buffer, 0, actualNrOfSamplesToRead * sampleSize);

            //System.out.println("Requested (samples): " + requestedNrOfSamplesToRead);
            //System.out.println("Reading (samples):   " + actualNrOfSamplesToRead);
            //System.out.println("Reading (bytes):     " + actualNrOfSamplesToRead * sampleSize);
            //System.out.println("Bytes read:          " + actualRead);
        }
        catch (IOException ioe) {
//            System.out.println("IO Error while reading samples.");

            return actualRead; //=0
        }

        // 8 bit mono
        if (sampleSize == 1) {
            for (int s = 0; s < actualNrOfSamplesToRead; s++) {
                intArrayLeft[s] = convert((buffer[s] & 0xFF) - 128);
            }
        }
        else if (sampleSize == 2) {
        	// 16 bit mono
            if (nrOfChannels == 1) {
                int b = 0;
                int b1;
                int b2;

                for (int s = 0; s < actualNrOfSamplesToRead; s++) {
                    b1 = buffer[b] & 0xFF;
                    b2 = buffer[b + 1];
                    intArrayLeft[s] = convert(b1 | (b2 << 8));
                    b += 2;
                }
            }
            // 8 bit stereo
            else {
                int b = 0;
                int b1;

                for (int s = 0; s < actualNrOfSamplesToRead; s++) {
                    // channel 1
                    b1 = (buffer[b] & 0xFF) - 128;
                    intArrayLeft[s] = convert(b1);

                    // channel 2
                    b1 = (buffer[b + 1] & 0xFF) - 128;

                    if (stereoOutput) {
                        intArrayRight[s] = convert(b1);
                    }
                    else {
                        intArrayLeft[s] = (intArrayLeft[s] + convert(b1)) / 2;
                    }

                    b += 2;
                }
            }
        }
        // 24 bit mono
        else if ((sampleSize == 3) && (nrOfChannels == 1)) {
            int b = 0;
            int b1;
            int b2;
            int b3;

            for (int s = 0; s < actualNrOfSamplesToRead; s++) {
                b1 = buffer[b] & 0xFF;
                b2 = buffer[b + 1] & 0xFF;
                b3 = buffer[b + 2];

                intArrayLeft[s] = convert(b1 | (b2 << 8) | (b3 << 16));
                b += 3;
            }
        }
        // 16 bit stereo
        else if ((sampleSize == 4) && (nrOfChannels == 2)) {
            int b = 0;
            int b1;
            int b2;

            for (int s = 0; s < actualNrOfSamplesToRead; s++) {
                // channel 1
                b1 = buffer[b] & 0xFF;
                b2 = buffer[b + 1];
                intArrayLeft[s] = convert(b1 | (b2 << 8));

                // channel 2
                b1 = buffer[b + 2] & 0xFF;
                b2 = buffer[b + 3];

                if (stereoOutput) {
                    intArrayRight[s] = convert(b1 | (b2 << 8));
                }
                else {
                    intArrayLeft[s] = (intArrayLeft[s] + (convert(b1 | (b2 << 8)))) / 2;
                }

                b += 4;
            }
        }
        //24 bit stereo
        else if ((sampleSize == 6) && (nrOfChannels == 2)) {
            int b = 0;
            int b1;
            int b2;
            int b3;

            for (int s = 0; s < actualNrOfSamplesToRead; s++) {
                // channel 1
                b1 = buffer[b] & 0xFF;
                b2 = buffer[b + 1] & 0xFF;
                b3 = buffer[b + 2];

                intArrayLeft[s] = convert(b1 | (b2 << 8) | (b3 << 16));

                // channel 2
                b1 = buffer[b + 3] & 0xFF;
                b2 = buffer[b + 4] & 0xFF;
                b3 = buffer[b + 5];

                if (stereoOutput) {
                    intArrayRight[s] = convert(b1 | (b2 << 8) | (b3 << 16));
                }
                else {
                    intArrayLeft[s] = (intArrayLeft[s] + convert(b1 | (b2 << 8) | (b3 << 16))) / 2;
                }

                b += 6;
            }
        }
        //32 bit mono
        else if ((sampleSize == 4) && (nrOfChannels == 1)) {
            int b = 0;
            int b1;
            int b2;
            int b3;
            int b4;

            for (int s = 0; s < actualNrOfSamplesToRead; s++) {
                b1 = buffer[b] & 0xFF;
                b2 = buffer[b + 1] & 0xFF;
                b3 = buffer[b + 2] & 0xFF;
                b4 = buffer[b + 3];

                intArrayLeft[s] = convert(b1 | (b2 << 8) | (b3 << 16) | (b4 << 24));
                b += 4;
            }
        }
        //32 bit stereo
        else if ((sampleSize == 8) && (nrOfChannels == 2)) {       	
            int b = 0;
            int b1;
            int b2;
            int b3;
            int b4;

            for (int s = 0; s < actualNrOfSamplesToRead; s++) {
                // channel 1
                b1 = buffer[b] & 0xFF;
                b2 = buffer[b + 1] & 0xFF;
                b3 = buffer[b + 2] & 0XFF;
                b4 = buffer[b + 3];

                intArrayLeft[s] = convert(b1 | (b2 << 8) | (b3 << 16) | (b4 << 24));

                // channel 2
                b1 = buffer[b + 4] & 0xFF;
                b2 = buffer[b + 5] & 0xFF;
                b3 = buffer[b + 6] & 0xFF;
                b4 = buffer[b + 7];

                if (stereoOutput) {
                    intArrayRight[s] = convert(b1 | (b2 << 8) | (b3 << 16) | (b4 << 24));
                }
                else {
                    intArrayLeft[s] = (intArrayLeft[s] + convert(b1 | (b2 << 8) | (b3 << 16) | (b4 << 24))) / 2;
                }

                b += 8;
            }
        }
        
        //System.out.println("Returning (samples):     " + actualRead / sampleSize);
        //System.out.println("IntArrayLeft (samples):  " + intArrayLeft.length + " thread: " +Thread.currentThread());
        //System.out.println("IntArrayRight (samples): " + intArrayRight.length + " WavSampler: " + this);
        return actualRead / sampleSize;
    }

    /**
     * Seeks to the {@code n-th} sample.
     *
     * @param n the sample index to seek
     */
    @Override
	public void seekSample(long n) {
        if ((n < 0) || (n > nrOfSamples)) {
//            System.out.println("Cannot seek sample " + n + ".");
        }
        else {
            try {
                soundFile.seek(headerSize + (n * sampleSize));
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Seek the sample that corresponds to the specified time in milliseconds.
     *
     * @param time the time in milliseconds
     */
    @Override
	public void seekTime(float time) {
        if (time < 0) {
//            System.out.println("Cannot seek sample for time `" + time + "' !");
        }
        else {
            seekSample((long) ((time * sampleFrequency) / 1000));
        }
    }

    @Override
	public void seekTimeSeconds(double timeSec) {
		if (timeSec >= 0) {
			seekSample((long) (timeSec * sampleFrequency));
		}
	}

	@Override
	public short getCompressionCode() {
		return wavHeader.getCompressionCode();
	}

	@Override
	public String getCompressionString(short compr) {
		return wavHeader.getCompressionString(compr);
	}

	private int convert(int orig) {
        switch (compressionCode) {
        case WAVHeader.WAVE_FORMAT_ALAW: //alaw

            byte val = toUnsigned((byte) orig);
            int t;
            int seg;

            val ^= 0x55;
            t = (val & 0xf) << 4;
            seg = (toUnsigned(val) & 0x70) >> 4;

            switch (seg) {
            case 0:
                t += 8;

                break;

            case 1:
                t += 0x108;

                break;

            default:
                t += 0x108;
                t <<= (seg - 1);
            }

            return (((val & 0x80) != 0) ? t : (-t));
            
        case WAVHeader.WAVE_FORMAT_IEEE_FLOAT:
        	
        	float f = Float.intBitsToFloat(orig);
           	return (int) (f * possibleMaxSample);
           	
        default:
            return orig;
        }
    }

    private byte toUnsigned(byte signed) {
        return (byte) (signed + 128);
    }

	@Override
	public void setDebugMode(boolean enable) {
		// stub, to be implemented
		debugMode = enable;
	}
}
