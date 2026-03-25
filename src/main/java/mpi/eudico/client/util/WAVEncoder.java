package mpi.eudico.client.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utility class to encode audio samples, mono or stereo, as a WAV byte array
 * (including a header). The byte array can be used in memory or be saved to 
 * a {@code .wav} file.
 */
public class WAVEncoder {
	
	/**
	 * Writes mono, one channel, audio samples to a {@code .wav} file. This
	 * method does not check if the file already exists and does not prompt the
	 * user to ask if an existing file should be overwritten.
	 * 
	 * @param destinationPath the file path to write the wave byte array to
	 * @param samples the (decoded) audio samples as an array of integers
	 * @param numberOfBitsPerSample the number of bits to use per sample
	 * @param numberOfSamplesPerSecond the number of samples per second to
	 * encode, the sample frequency
	 * @throws IOException any IO exception that might be thrown by {@code Files.write}
	 */
	public static void toWaveFile(Path destinationPath, int[] samples, 
			int numberOfBitsPerSample, int numberOfSamplesPerSecond) throws IOException {
		Files.write(destinationPath, toWaveArray(samples, 
				numberOfBitsPerSample, numberOfSamplesPerSecond));
	}
	
	/**
	 * Writes stereo, two channels, audio samples to a {@code .wav} file. Does
	 * not check if the file already exists and can be overwritten.
	 * 
	 * @param destinationPath the file path to write the wave byte array to 
	 * @param samples the (decoded) audio samples as integers, the first channel
	 * in array at index 0, the second at index 1, other arrays, if present,
	 * are ignored
	 * @param numberOfBitsPerSample the number of bits to use per sample
	 * @param numberOfSamplesPerSecond the number of samples per second to
	 * encode, the sample frequency
	 * @throws IOException any IO exception that might be thrown by {@code Files.write}
	 */
	public static void toWaveFile(Path destinationPath, int[][] samples, 
			int numberOfBitsPerSample, int numberOfSamplesPerSecond) throws IOException {
		Files.write(destinationPath, toWaveArray(samples, 
				numberOfBitsPerSample, numberOfSamplesPerSecond));
	}
	
	/**
	 * Encodes audio samples to a byte array, including a {@code wave} header.
	 * @param samples the samples of a single channel as an array of integers
	 * @param numberOfBitsPerSample the number of bits to use per sample while
	 * encoding
	 * @param numberOfSamplesPerSecond the number of samples per second to
	 * encode, the sample frequency
	 * @return the encoded {@code .wav} stream as an array of bytes 
	 */
	public static byte[] toWaveArray(int[] samples, int numberOfBitsPerSample, int numberOfSamplesPerSecond) {
		byte[] header = createHeader(samples.length, numberOfSamplesPerSecond, 1, numberOfBitsPerSample);
		byte[] data = toByteArray(samples, numberOfBitsPerSample);
		byte[] waveArray = new byte[header.length + data.length];
		System.arraycopy(header, 0, waveArray, 0, header.length);
		System.arraycopy(data, 0, waveArray, header.length, data.length);
		
		return waveArray;
	}
	
	/**
	 * Encodes audio samples to a byte array, including a {@code wave} header.
	 * It is assumed the input samples are 32-bit values which will be 
	 * converted to 16-bit values
	 * 
	 * @param samples the samples of a single channel as an array of integers
	 * @param numberOfSamplesPerSecond the number of samples per second to
	 * encode, the sample frequency
	 * @return the encoded {@code .wav} stream as an array of bytes 
	 */
	public static byte[] toWaveArray32To16Bit(int[] samples, int numberOfSamplesPerSecond) {
		byte[] header = createHeader(samples.length, numberOfSamplesPerSecond, 1, 16);
		byte[] data = toByteArray32To16(samples);
		byte[] waveArray = new byte[header.length + data.length];
		System.arraycopy(header, 0, waveArray, 0, header.length);
		System.arraycopy(data, 0, waveArray, header.length, data.length);
		
		return waveArray;
	}
	
	/**
	 * Encodes stereo channels audio samples to a byte array, including a 
	 * {@code wave} header.
	 * 
	 * @param samples the samples of two channels as arrays of integers
	 * @param numberOfBitsPerSample the number of bits to use per sample while
	 * encoding
	 * @param numberOfSamplesPerSecond the number of samples per second to
	 * encode, the sample frequency
	 * @return the encoded {@code .wav} stream as an array of bytes 
	 */
	public static byte[] toWaveArray(int[][] samples, int numberOfBitsPerSample, int numberOfSamplesPerSecond) {
		if (samples.length > 1 && samples[1] == null) {
			return toWaveArray(samples[0], numberOfBitsPerSample, numberOfSamplesPerSecond);
		}
		byte[] header = createHeader(samples.length * samples[0].length, numberOfSamplesPerSecond, 
				samples.length, numberOfBitsPerSample);
		byte[] data = toByteArray(samples, numberOfBitsPerSample);
		byte[] waveArray = new byte[header.length + data.length];
		System.arraycopy(header, 0, waveArray, 0, header.length);
		System.arraycopy(data, 0, waveArray, header.length, data.length);
		
		return waveArray;
	}
	
	/**
	 * Encodes stereo channels audio samples to a byte array, including a 
	 * {@code wave} header. It is assumed the input samples are 32-bit
	 * values which will be converted to 16-bit values
	 * 
	 * @param samples the samples of two channels as arrays of integers
	 * @param numberOfSamplesPerSecond the number of samples per second to
	 * encode, the sample frequency
	 * @return the encoded {@code .wav} stream as an array of bytes 
	 */
	public static byte[] toWaveArray32To16Bit(int[][] samples, int numberOfSamplesPerSecond) {
		if (samples.length > 1 && samples[1] == null) {
			return toWaveArray32To16Bit(samples[0], numberOfSamplesPerSecond);
		}
		byte[] header = createHeader(samples.length * samples[0].length, numberOfSamplesPerSecond, 
				samples.length, 16);
		byte[] data = toByteArray32To16(samples);
		byte[] waveArray = new byte[header.length + data.length];
		System.arraycopy(header, 0, waveArray, 0, header.length);
		System.arraycopy(data, 0, waveArray, header.length, data.length);
		
		return waveArray;
	}

	/**
	 * Encodes audio samples to an array of bytes.
	 * 
	 * @param samples the samples of a single channel as an array of integers
	 * @param numberOfBitsPerSample the number of bits to use per sample while
	 * encoding (8, 16, 24 or 32)
	 * 
	 * @return the audio samples encoded as an array of bytes
	 */
	public static byte[] toByteArray(int[] samples, int numberOfBitsPerSample) {
		if (samples == null) {
			return null;
		}		

		// bits per sample: 8, 16, 24 or 32
		if (numberOfBitsPerSample % 8 != 0 || numberOfBitsPerSample > 32) {
			return null;
		}
		// signed/unsigned correction for 8bit values
		byte signCorrection = numberOfBitsPerSample == 8 ? (byte) 128 : (byte) 0;
		// 1 int to 1, 2, 3, or 4 bytes
		int sizeFactor = numberOfBitsPerSample / 8;
		// one channel only 
		byte[] byteArray = new byte[samples.length * sizeFactor];
		int bytePos = 0;
		for (int i = 0; i < samples.length; i++) {
			// convert one sample from this channel
			for (int j = 0; j < sizeFactor; j++) {
				byteArray[bytePos++] = (byte) (((samples[i] >> (j * 8)) & 0xff) - signCorrection);
			}
		}
		
		return byteArray;
	}
	
	/**
	 * Encodes audio samples to an array of bytes.
	 * 
	 * @param samples the samples of a single channel as an array of integers
	 * @param numberOfBitsPerSample the number of bits to use per sample while
	 * encoding (8, 16, 24 or 32)
	 * 
	 * @return the audio samples encoded as an array of bytes
	 */
	public static byte[] toByteArray32To16(int[] samples) {
		if (samples == null) {
			return null;
		}
		
		// 1 int to 1, 2, 3, or 4 bytes
		int sizeFactor = 2; // 16 / 8;
		// a factor for recalculation of the sample values 
		// (maybe double precision should be preferred?)
		float valueFactor = (float) Short.MAX_VALUE / Integer.MAX_VALUE;
		
		// for randomly rounding new values up or down maybe
		// Random r = new Random();
		// r.nextBoolean() 
		// can be used instead of Math.random()
		
		// one channel only 
		byte[] byteArray = new byte[samples.length * sizeFactor];
		int bytePos = 0;
		for (int i = 0; i < samples.length; i++) {
			// convert one sample from this channel
			for (int j = 0; j < sizeFactor; j++) {
				float sample = samples[i] * valueFactor;
				// randomly round up or down
				int rounded = Math.random() > 0.5d ?
					(int) Math.ceil(sample) :
					(int) Math.floor(sample);
				byteArray[bytePos++] = (byte) (((rounded >> (j * 8)) & 0xff));
			}
		}
		
		return byteArray;
	}
	
	/**
	 * Encodes stereo channel audio samples to an array of bytes.
	 * 
	 * @param samples the samples of two channels as arrays of integers
	 * @param numberOfBitsPerSample the number of bits to use per sample while
	 * encoding (8, 16, 24 or 32)
	 * 
	 * @return the audio samples encoded as an array of bytes
	 */
	public static byte[] toByteArray(int[][] samples, final int numberOfBitsPerSample) {
		if (samples == null) {
			return null;
		}
		// if only one channel, on array is present
		if (samples[1] == null) {
			return toByteArray(samples[0], numberOfBitsPerSample);
		}
		
		// bits per sample: 8, 16, 24 or 32
		if (numberOfBitsPerSample % 8 != 0 || numberOfBitsPerSample > 32) {
			return null;
		}
		// signed/unsigned correction for 8bit values
		byte signCorrection = numberOfBitsPerSample == 8 ? (byte) 128 : (byte) 0;
		// 1 int to 1, 2, 3, or 4 bytes
		int sizeFactor = numberOfBitsPerSample / 8;
		// two channels (currently the maximum), multiply the size by two
		byte[] byteArray = new byte[samples[0].length * sizeFactor * 2];
		int bytePos = 0;
		for (int i = 0; i < samples[0].length; i++) {
			// convert one sample from the first channel
			for (int j = 0; j < sizeFactor; j++) {
				byteArray[bytePos++] = (byte) (((samples[0][i] >> (j * 8)) & 0xff) - signCorrection);
			}
			// then one sample from the second channel
			for (int j = 0; j < sizeFactor; j++) {
				byteArray[bytePos++] = (byte) (((samples[1][i] >> (j * 8)) & 0xff) - signCorrection);
			}
		}
		
		return byteArray;
	}
	
	/**
	 * Encodes stereo channel audio samples to an array of bytes, where the
	 * input samples represent 32-bit values which will be converted to output
	 * encoded as 16-bit values (2 bytes per sample).
	 * 
	 * @param samples the samples of two channels as arrays of integers
	 * 
	 * @return the audio samples encoded as an array of bytes, 16-bit per sample
	 */
	public static byte[] toByteArray32To16(int[][] samples) {
		if (samples == null) {
			return null;
		}
		
		if (samples[1] == null) {
			//return toByteArray32To16(samples[0]);
			return null;
		}

		// 1 int in 2 bytes
		int sizeFactor = 2; // 16 / 8
		// a factor for recalculation of the sample values 
		// (maybe double precision should be preferred?)
		float valueFactor = (float) Short.MAX_VALUE / Integer.MAX_VALUE;
		
		// for randomly rounding new values up or down maybe
		// Random r = new Random();
		// r.nextBoolean() 
		// can be used instead of Math.random()
		
		// two channels (currently the maximum), multiply the size by two
		byte[] byteArray = new byte[samples[0].length * sizeFactor * 2];
		int bytePos = 0;
		for (int i = 0; i < samples[0].length; i++) {
			// convert one sample from the first channel
			for (int j = 0; j < sizeFactor; j++) {
				float sample = samples[0][i] * valueFactor;
				// randomly round up or down
				int rounded = Math.random() > 0.5d ?
					(int) Math.ceil(sample) :
					(int) Math.floor(sample);
				byteArray[bytePos++] = (byte) (((rounded >> (j * 8)) & 0xff));
			}
			// then one sample from the second channel
			for (int j = 0; j < sizeFactor; j++) {
				float sample1 = samples[1][i] * valueFactor;
				int rounded1 = Math.random() > 0.5d ?
						(int) Math.ceil(sample1) :
						(int) Math.floor(sample1);
				byteArray[bytePos++] = (byte) (((rounded1 >> (j * 8)) & 0xff));
			}
		}
		
		return byteArray;
	}
	
	/*
	 * Base template for a WAVE header
	 */
	private static final String HEADER_TEMPLATE = "RIFF....WAVEfmt ....................data...."; 
	/*
	 * WAV HEADER description, for a minimal uncompressed PCM wave file, 44 bytes
	 * 
	 * byte		value	group
	 * 0		R		-		4 bytes/chars, "RIFF"
	 * 1		I		|
	 * 2		F		|
	 * 3		F		-
	 * 4		s1		-		4 bytes, together an int specifying the size of the file - 8 (the size of the first 2 fields)
	 * 5		s2		|
	 * 6		s3		|
	 * 7		s4		-
	 * 8		W		-		4 bytes/chars, "WAVE"
	 * 9		A		|
	 * 10		V		|
	 * 11		E		_
	 * 12		f		-		4 individual bytes/chars, the chunk ID, "fmt "
	 * 13		m		|
	 * 14		t		|
	 * 15		 	 	-
	 * 16		cs1		-		4 bytes, together an int specifying the chunk data size, 16, 18 or 40 (use 16 for PCM)
	 * 17		cs2		|		
	 * 18		cs3		|		
	 * 19		cs4		-		
	 * 20		wFormat	-		2 bytes, together a short, 1 for PCM in the first byte (wFormatTag, AudioFormat)
	 * 21		wFormat	-		
	 * 22		nChann	-		2 bytes, together a short, number of channels, 1 for mono, 2 for stereo etc.
	 * 23		nChann	-
	 * 24		sRate	-		4 bytes, together an int, the sample rate, number of samples per second, e.g. 44100
	 * 25		sRate	|
	 * 26		sRate	|
	 * 27		sRate	-
	 * 28		bRate	-		4 bytes, together an int, average bytes per second (== SampleRate * NumChannels * BitsPerSample/8)
	 * 29		bRate	|
	 * 30		bRate	|
	 * 31		bRate	-
	 * 32		bAlign	-		2 bytes, together a short, the block alignment, the number of bytes for 1 sample of all channels (NumChannels * BitsPerSample/8)
	 * 33		bAlign	-
	 * 34		bitsS	-		2 bytes, together a short, the bits per sample, e.g. 8-bit, 16-bit, 24-bit or 32-bit
	 * 35		bitsS	-
	 * 36		d		-		4 bytes, "data" chunk id
	 * 37		a		|
	 * 38		t		|
	 * 39		a		-
	 * 40		dSize	-		4 bytes, together an int, the size of the actual audio data in bytes (should be NumSamples * NumChannels * BitsPerSample/8)
	 * 41		dSize	|
	 * 42		dSize	|
	 * 43		dSize	-
	 * 
	 * end of simple header, the actual audio data follow here.
	 * All int's and shorts are encoded little-endian, the char/strings big-endian 
	 * 
	 * 
	 */
	
	/**
	 * Creates a WAVE header encoded as an array of bytes.
	 * 
	 * @param numberOfSamplesInDataArray the size of the {@code data} array
	 * @param numberOfSamplesPerSecond the sample frequency
	 * @param numberOfChannels the number of audio channels
	 * @param numberOfBitsPerSample the number of bits to use per sample
	 * 
	 * @return the WAVE header as an array of bytes
	 */
	public static byte[] createHeader(int numberOfSamplesInDataArray, int numberOfSamplesPerSecond, 
			int numberOfChannels, int numberOfBitsPerSample) {
		byte[] header = HEADER_TEMPLATE.getBytes();
		int numberOfBytesPerSample = numberOfBitsPerSample / 8;
		int blockAlignmentSize = numberOfChannels * numberOfBytesPerSample; 
		int numberOfBytesPerSecond = numberOfSamplesPerSecond * blockAlignmentSize;
		int numberOfBytesInDataArray = numberOfSamplesInDataArray * numberOfBytesPerSample;
		int riffChunkSize = numberOfBytesInDataArray + 44 - 8; 
		
		// fill in the numerical fields
		// 0 - 3 "RIFF"
		header[4] = (byte) (riffChunkSize & 0xff);
		header[5] = (byte) ((riffChunkSize >> 8) & 0xff);
		header[6] = (byte) ((riffChunkSize >> 16) & 0xff);
		header[7] = (byte) ((riffChunkSize >> 24) & 0xff);
		// 8 - 15 "WAVEfmt "
		header[16] = (byte) 16;
		header[17] = (byte) 0;
		header[18] = (byte) 0;
		header[19] = (byte) 0;
		header[20] = (byte) 1;
		header[21] = (byte) 0;
		header[22] = (byte) numberOfChannels;
		header[23] = (byte) 0;
		header[24] = (byte) (numberOfSamplesPerSecond & 0xff);
		header[25] = (byte) ((numberOfSamplesPerSecond >> 8) & 0xff);
		header[26] = (byte) ((numberOfSamplesPerSecond >> 16) & 0xff);
		header[27] = (byte) ((numberOfSamplesPerSecond >> 24) & 0xff);
		header[28] = (byte) (numberOfBytesPerSecond & 0xff);
		header[29] = (byte) ((numberOfBytesPerSecond >> 8) & 0xff);
		header[30] = (byte) ((numberOfBytesPerSecond >> 16) & 0xff);
		header[31] = (byte) ((numberOfBytesPerSecond >> 24) & 0xff);
		header[32] = (byte) (blockAlignmentSize & 0xff);
		header[33] = (byte) ((blockAlignmentSize >> 8) & 0xff);
		header[34] = (byte) numberOfBitsPerSample;
		header[35] = (byte) 0;
		// 36 - 39 "data"
		header[40] = (byte) (numberOfBytesInDataArray & 0xff);
		header[41] = (byte) ((numberOfBytesInDataArray >> 8) & 0xff);
		header[42] = (byte) ((numberOfBytesInDataArray >> 16) & 0xff);
		header[43] = (byte) ((numberOfBytesInDataArray >> 24) & 0xff);
		
		return header;
	}
}
