package mpi.eudico.client.annotator.player;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.util.SystemReporting;
import mpi.eudico.client.util.WAVFromOtherSampler;
import mpi.eudico.client.util.WAVHeader;
import mpi.eudico.client.util.WAVSampler;
import mpi.eudico.client.util.WAVSamplesProvider;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import nl.mpi.media.UnsupportedMediaException;
import nl.mpi.util.FileUtility;

/**
 * A factory class for the creation of a {@code WAVSamplesProvider} for a media
 * file. Traditionally only {@code WAVE} audio files were supported, for which
 * a {@code WAVSampler} instance was created.
 * Visualization of a waveform for the audio track of a video file or of a 
 * different type of audio file, may now be possible by using a native media
 * framework's functionality.  
 * 
 * @author Han Sloetjes
 * @version Jan 2021
 */
public class WAVSamplerFactory {
	private final static Logger LOG = Logger.getLogger("ClientLogger");
	private static boolean useNativeAudioExtractor = true;
	
	static {
		// could consider a preference setting too
		String prop = System.getProperty("AudioExtractor.UseNativeExtractor");
		if (prop != null) {
			useNativeAudioExtractor = Boolean.parseBoolean(prop);
		}
		
	}
	
	/**
	 * Private constructor.
	 */
	private WAVSamplerFactory() {
	}
	
	/**
	 * Sets whether or not a native audio extractor should be used or not.
	 * Currently assumes there is only one possible native extractor per
	 * platform.
	 * 
	 * @param useNativeExtractor the value for this flag 
	 */
	public static void setUseNativeExtractor(boolean useNativeExtractor) {
		useNativeAudioExtractor = useNativeExtractor;
	}

	/**
	 * Tries to create a sampler for the specified file. 
	 * The default for local {@code .wav} files still is the {@code WAVSampler},
	 * for other files a native framework is tried. 
	 *  
	 * @param mediaPath the location of a media file
	 * @return a {@code WAVSamplesProvider} or {@code null} if no sampler 
	 * supports the file
	 */
	public static WAVSamplesProvider createWAVSamplesProvider(String mediaPath) {
		if (mediaPath == null) {
			return null;
		}
		boolean isLocal = false;
		if (!FileUtility.isRemoteFile(mediaPath)) {
			isLocal = true;
			mediaPath = FileUtility.urlToAbsPath(mediaPath);
		}
		
		String lcExt = FileUtility.getExtension(mediaPath, "wav").toLowerCase();
		if (isLocal && (lcExt.equals("wav") || lcExt.equals("wave"))) {
			
			// try the RandomAccessFile based WAVSampler, if the compression type
			// is supported and the number of channels is < 3				
			WAVHeader header = new WAVHeader(mediaPath);
			int compression = header.getCompressionCode();
			if (compression == WAVHeader.WAVE_FORMAT_PCM || compression == WAVHeader.WAVE_FORMAT_ALAW ||
					compression == WAVHeader.WAVE_FORMAT_EXTENSIBLE || 
					compression == WAVHeader.WAVE_FORMAT_IEEE_FLOAT) {
				if (header.getNumberOfChannels() < 3) {
					try {
						return new WAVSampler(mediaPath);
					} catch (IOException ioe) {
						if (LOG.isLoggable(Level.WARNING)) {
							LOG.log(Level.WARNING, ioe.getMessage());
						}
					}
				}
			}
		} 
		
		// other cases (remote file or wrong wav format or some Exception occurred), try the native media framework
		
		if (!WAVSamplerFactory.useNativeAudioExtractor) {
			return null;
		}
		
		String prefFrameWork = getPreferredProvider();
		if (PlayerFactory.NONE.equals(prefFrameWork)) {
			return null;
		}

		try {
			return new WAVFromOtherSampler(mediaPath, prefFrameWork);
		} catch (UnsupportedMediaException ume) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, ume.getMessage());
			}
		} catch (IOException ioe) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, ioe.getMessage());
			}			
		}
		
		return null;
	}
	
	/**
	 * Tries to create a sampler for the media file identified by the 
	 * specified media descriptor. 
	 * The default for local {@code .wav} files still is the {@code WAVSampler},
	 * for other files a native framework is tried. 
	 *  
	 * @param medDescriptor the media descriptor containing the location of a 
	 * media file
	 * @return a {@code WAVSamplesProvider} or {@code null} if no sampler 
	 * supports the file
	 */
	public static WAVSamplesProvider createWAVSamplesProvider(MediaDescriptor medDescriptor) {
		if (medDescriptor == null || medDescriptor.mediaURL == null) {
			return null;
		}
		String medPath = medDescriptor.mediaURL;
		boolean isLocal = false;
		if (!FileUtility.isRemoteFile(medPath)) {
			isLocal = true;
			medPath = FileUtility.urlToAbsPath(medPath);
		}
		
		// a local .wav file
		if (MediaDescriptor.WAV_MIME_TYPE.equals(medDescriptor.mimeType)) {
			
			if (isLocal) {
				// try the RandomAccessFile based WAVSampler, if the compression type
				// is supported and the number of channels is < 3				
				WAVHeader header = new WAVHeader(medPath);
				int compression = header.getCompressionCode();
				if (compression == WAVHeader.WAVE_FORMAT_PCM || compression == WAVHeader.WAVE_FORMAT_ALAW ||
						compression == WAVHeader.WAVE_FORMAT_EXTENSIBLE || 
						compression == WAVHeader.WAVE_FORMAT_IEEE_FLOAT) {
					if (header.getNumberOfChannels() < 3) {
						try {
							return new WAVSampler(medPath);
						} catch (IOException ioe) {
							if (LOG.isLoggable(Level.WARNING)) {
								LOG.log(Level.WARNING, ioe.getMessage());
							}
						}
					}
				}
			} 
		}
			
		// in all other cases (remote file or wrong wav format or some Exception occurred), 
		// try the native media framework
		if (!WAVSamplerFactory.useNativeAudioExtractor) {
			return null;
		}
		
		String prefFrameWork = getPreferredProvider();
		if (PlayerFactory.NONE.equals(prefFrameWork)) {
			return null;
		}
		
		try {
			return new WAVFromOtherSampler(medPath, prefFrameWork);
		} catch (UnsupportedMediaException ume) {// unsupported exception
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, ume.getMessage());
			}
		} catch (IOException ioe) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, ioe.getMessage());
			}
		}

		return null;
	}
	
	private static String getPreferredProvider() {
		String stringPref = Preferences.getString("AudioExtractionFramework", null);
		// check
		if (stringPref != null) {
			return stringPref;
		} else {
			//return a platform specific default
			/*
			if (SystemReporting.isWindows()) {
				return PlayerFactory.JMMF;
			} else if (SystemReporting.isMacOS()) {
				return PlayerFactory.FFMPEG; // PlayerFactory.JAVF
			} else {
				return PlayerFactory.FFMPEG;
			}
			*/
			// Aug 2023: change the default to "none"
			return PlayerFactory.NONE;
		}
	}
}
