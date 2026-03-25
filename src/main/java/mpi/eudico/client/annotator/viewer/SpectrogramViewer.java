package mpi.eudico.client.annotator.viewer;

import static mpi.eudico.client.annotator.util.ClientLogger.LOG;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.gui.SpectrogramSettingsDialog;
import mpi.eudico.client.annotator.spectrogram.SpectrogramSettingsIO;
import mpi.eudico.client.mediacontrol.ControllerEvent;
import mpi.eudico.client.mediacontrol.StopEvent;
import mpi.eudico.client.util.WAVHeader;
import mpi.eudico.client.util.WAVSampler;
import mpi.eudico.client.util.WAVSamplesProvider;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.util.TimeFormatter;
import nl.mpi.media.spectrogram.FFT;
import nl.mpi.media.spectrogram.Frequency;
import nl.mpi.media.spectrogram.ImageCreator;
import nl.mpi.media.spectrogram.SpectrogramSettings;
import nl.mpi.media.spectrogram.SpectrogramSettings.FREQ_CHANNEL;
import nl.mpi.util.FileUtility;

/**
 * A viewer that displays an audio spectrogram of a WAV file or the audio track
 * of a video file (if the platform supports that). A Fourier transform is 
 * applied to turn the audio signal into a spectrum of frequencies. 
 * <p>
 * This is a {@link TimeScaleBasedViewer} which currently does not include a 
 * time ruler in its visualization. Several options are available to modify the
 * appearance of the spectrogram. 
 *  
 * @author Han Sloetjes
 * @version 1, 01-2022 
 */
@SuppressWarnings("serial")
public class SpectrogramViewer extends DefaultTimeScaleBasedViewer implements ItemListener {
    /** store the path to the media file */
    private String mediaFilePath;
    /*  */
    private MediaDescriptor mediaDescriptor;
    private WAVSamplesProvider mediaSampler;
    private SpectrogramSettings specSettings;
    
    private FFT fft;
    private double[] weightingWindow;
    private ImageCreator imgCreator;
    private BufferedImage specImage;
    private IntervalCache curCache = null;
    private BasicStroke dashLine = new BasicStroke(0.75f, BasicStroke.CAP_BUTT, 
    		BasicStroke.JOIN_BEVEL, 1f, new float[] {8, 10}, 0f);
    // some error feedback
    private String errorKey = null;
    
    private double[] normalizedRange = new double[]{-110, 0};// value range after FFT
    private double[][] amplitudeRanges;// ranges after FFT, different depending on bits per sample
    private final ReentrantLock shiftLoadLock = new ReentrantLock();
    // ui
    private JMenuItem settingsMI;
    private JMenuItem detailMI;
    private JMenu channelMenu;
    private JRadioButtonMenuItem channel1MI;
    private JRadioButtonMenuItem channel2MI;
    private JRadioButtonMenuItem channel12MI;
    private JRadioButtonMenuItem channel12SeparateMI;
    private boolean prefsInited = false;
    private boolean globalPrefsInited = false;
    private String tooltipFormat = "<html><table><tr><td>T</td><td>%.2f &nbsp;[ %s ]</td></tr><tr><td>Hz</td><td>\u00b1 %.0f - %.0f</td></tr></table></html>";
	// in order to prevent too rapid succession of updates (e.g. in ticker mode)
    // ignore or delay calls that require recreation of 
    // a timed queue or similar would probably be best
    private long lastUpdate = 0L; // the system time of the last call for an update
	private boolean refreshPending = false; // if one or more calls have been ignored, update later
	
	/**
	 * No-arg constructor, currently not used.
	 */
	public SpectrogramViewer() {
		super();
		initViewer();
	}

	/**
	 * Constructor, initializes the viewer for the specified media file.
	 * 
	 * @param mediaPath the file path or URL of the source media  
	 */
	public SpectrogramViewer(String mediaPath) {
		this();
		setMedia(mediaPath);
	}
	
	/**
	 * Constructor, initializes the viewer for the specified {@link MediaDescriptor}.
	 * 
	 * @param mediaDescriptor the descriptor containing the location of the 
	 * media source
	 */
	public SpectrogramViewer(MediaDescriptor mediaDescriptor) {
		this(mediaDescriptor.mediaURL);
    	this.mediaDescriptor = mediaDescriptor;
    	setMediaTimeOffset(mediaDescriptor.timeOrigin);
	}
	
	/**
	 * Constructor, initializes the viewer for the specified {@link WAVSamplesProvider}.
	 * 
	 * @param sampler the provider of the audio samples for the spectrogram
	 */
	public SpectrogramViewer(WAVSamplesProvider sampler) {
		this();
		setMediaSampler(sampler, null);
		
    	if (LOG.isLoggable(Level.INFO)) {
			LOG.log(Level.INFO, "MediaSampler URL for SpectrogramViewer: " + sampler.getMediaLocation());
		}
	}
	
    /**
     * Creates a new SpectrogramViewer based on a samples provider and a media descriptor.
     * The {@code WAVSamplesProvider} interface currently does not support
     * {@code MediaDescriptor}s (in order to limit the dependencies).
     * 
     * @param sampler the provider of audio samples to visualize as spectrogram
     * @param mediaDescriptor the media descriptor, can be {@code null}
     */
    public SpectrogramViewer(WAVSamplesProvider sampler, MediaDescriptor mediaDescriptor) {
    	this(sampler);
    	this.mediaDescriptor = mediaDescriptor;
    	if (mediaDescriptor != null && mediaDescriptor.timeOrigin != 0) {
    		setMediaTimeOffset(mediaDescriptor.timeOrigin);
    	}
    	
    	if (LOG.isLoggable(Level.INFO)) {
			LOG.log(Level.INFO, "MediaSampler URL for SpectrogramViewer: " + sampler.getMediaLocation());
		}
    }
    
    
	/**
	 * Check the offset of the media file of this viewer once the viewer 
	 * manager is being set. This viewer may have been created without a
	 * media descriptor and therefore without access to a possible offset.
	 */
    @Override
	public void setViewerManager(ViewerManager2 viewerManager) {
		super.setViewerManager(viewerManager);
		if (viewerManager != null) {
			mediaOffsetChanged();
		}
	}

	/**
     * Sets the source for this viewer. This will always create a 
     * {@link WAVSampler} instance.
     * <p>
     * It is synchronized to prevent interference with ControllerUpdates,
     * which run on separate threads and may call various methods which
     * end up looking at the WAVSampler.
     * <p>
     * Note: replacing the source media of this viewer isn't properly supported
     * yet.
     * 
     * @param mediaPath the URL of the source as a String
     */
    public void setMedia(String mediaPath) {

		if (mediaPath.startsWith("file:")) {
			mediaPath = mediaPath.substring(5);
		} else {
			// check protocol??
		}
		mediaFilePath = mediaPath;
		initSampler(mediaPath);
    }
    
    
    /**
     * Sets the media by means of a new samples provider.
     * The source can be a video or audio file, it depends on the provider which
     * formats are supported.
     * <p>
     * Note: replacing an existing sampler of this viewer isn't supported 
     * properly yet.
     * 
     * @param sampler the provider of audio samples from an audio or video file, not {@code null}
     * @param mediaDescriptor the media descriptor, can be {@code null}
     */
    public void setMediaSampler(WAVSamplesProvider sampler, MediaDescriptor mediaDescriptor) {
    	if (sampler != null) {
    		mediaSampler = sampler;
    		mediaFilePath = sampler.getMediaLocation();
    		// treat the path the same as in setMedia(String)?
    		if (mediaFilePath.startsWith("file:")) {
    			mediaFilePath = mediaFilePath.substring(5);
    		}
    		
    		specSettings.setSampleFrequency(mediaSampler.getSampleFrequency());
    		specSettings.setPossibleMaxFrequency(mediaSampler.getSampleFrequency() / 2d);
    		specSettings.setBitsPerSample(mediaSampler.getBitsPerSample());

    		specSettings.setNormalizedInputData(true);
    		if (specSettings.isNormalizedInputData()) {
    			specSettings.setLowerValueLimit(normalizedRange[0]);
    			specSettings.setUpperValueLimit(normalizedRange[1]);
    		} else {
    			specSettings.setLowerValueLimit(amplitudeRanges[(mediaSampler.getBitsPerSample() / 8) - 1][0]);
    			specSettings.setUpperValueLimit(amplitudeRanges[(mediaSampler.getBitsPerSample() / 8) - 1][1]);
    		}
    	}
    	this.mediaDescriptor = mediaDescriptor;
    	if (mediaDescriptor != null && mediaDescriptor.timeOrigin != mediaTimeOffset) {
    		setMediaTimeOffset(mediaDescriptor.timeOrigin);
    	}
    }
    
    /**
     * Initializes a <code>WaveSampler</code> for the given URL.
     * The URL should point to a {@code .wav} file, either uncompressed/PCM, or
     * ALAW compressed.
     *
     * @param sourcePath the URL of the source file as a String
     */
    private void initSampler(String sourcePath) {
        mediaSampler = null;
        errorKey = null;

        try {
        	mediaSampler = new WAVSampler(sourcePath);
            
            short compr = mediaSampler.getCompressionCode();
			if (compr != WAVHeader.WAVE_FORMAT_UNCOMPRESSED && 
            		compr != WAVHeader.WAVE_FORMAT_PCM && compr != WAVHeader.WAVE_FORMAT_ALAW
            		&& compr != WAVHeader.WAVE_FORMAT_EXTENSIBLE) {
            	errorKey = ElanLocale.getString("SignalViewer.Message.Compression") + ": " + 
            			mediaSampler.getCompressionString(compr);

    			if (LOG.isLoggable(Level.INFO)) {
                	StringBuilder sb = new StringBuilder("Unsupported WAVE file, information from the Header:\n");
                	sb.append("\tWAVE Format:\t" + mediaSampler.getCompressionString(compr) + "\n");
                	sb.append("\tNo. Channels:\t" + mediaSampler.getNumberOfChannels() + "\n");
                	sb.append("\tSample Rate:\t" + mediaSampler.getSampleFrequency());
    				LOG.info(sb.toString());
    			}
            }// else log some header info
			specSettings.setSampleFrequency(mediaSampler.getSampleFrequency());
			specSettings.setPossibleMaxFrequency(mediaSampler.getSampleFrequency() / 2d);
			specSettings.setBitsPerSample(mediaSampler.getBitsPerSample());
			//System.out.println("Sampler max amplitude: " + mediaSampler.getPossibleMaxSample());
        } catch (IOException ioe) {
        	if (LOG.isLoggable(Level.INFO)) {
    			LOG.log(Level.INFO, "Failed to create a WAVSampler " + ioe.getMessage());
    		}
            errorKey = ElanLocale.getString("SignalViewer.Message.NoReader") + ": " + ioe.getMessage();
        }
    }

    /**
     * Initializes some fields and utilities.
     */
	@Override
	protected void initViewer() {
		super.initViewer();
        rulerHeight = ruler.getHeight();
        timeRulerVisible = false;
        timeScaleConnected = true;
        specSettings = new SpectrogramSettings();// load from preferences
        calcWindowAndStride();
        fft = new FFT();
        imgCreator = new ImageCreator(specSettings);
       	specSettings.setChannelMode(SpectrogramSettings.FREQ_CHANNEL.CHANNEL_ALL);       
        amplitudeRanges = new double[4][2];
        amplitudeRanges[0]  = new double[]{-100, 40};
        amplitudeRanges[1]  = new double[]{-60, 90};
        amplitudeRanges[2]  = new double[]{-20, 140};
        amplitudeRanges[3]  = new double[]{20, 190};
	}	
	
	@Override
	protected void createPopupMenu() {
		super.createPopupMenu();
		timeRulerVisMI.setVisible(false);
		channelMenu = new JMenu(ElanLocale.getString("SpectrogramViewer.AudioChannel"));
		ButtonGroup channelGroup = new ButtonGroup();
		channel1MI = new JRadioButtonMenuItem(ElanLocale.getString("SpectrogramViewer.Channel1"), 
				specSettings.getChannelMode() == SpectrogramSettings.FREQ_CHANNEL.CHANNEL_1);
		channel1MI.addItemListener(this);
		channelGroup.add(channel1MI);
		channelMenu.add(channel1MI);
		channel2MI = new JRadioButtonMenuItem(ElanLocale.getString("SpectrogramViewer.Channel2"), 
				specSettings.getChannelMode() == SpectrogramSettings.FREQ_CHANNEL.CHANNEL_2);
		channel2MI.addItemListener(this);
		channelGroup.add(channel2MI);
		channelMenu.add(channel2MI);
		channel12MI = new JRadioButtonMenuItem(ElanLocale.getString("SpectrogramViewer.Channel12"),
				specSettings.getChannelMode() == SpectrogramSettings.FREQ_CHANNEL.CHANNEL_ALL);
		channel12MI.addItemListener(this);
		channelGroup.add(channel12MI);
		channelMenu.add(channel12MI);
		channel12SeparateMI = new JRadioButtonMenuItem(ElanLocale.getString("SpectrogramViewer.Channel12Separate"),
				specSettings.getChannelMode() == SpectrogramSettings.FREQ_CHANNEL.CHANNEL_SEPARATE);
		channel12SeparateMI.addItemListener(this);
		channelGroup.add(channel12SeparateMI);
		channelMenu.add(channel12SeparateMI);
		popup.add(channelMenu);
		
		//detailMI = new JMenuItem(ElanLocale.getString("SpectrogramViewer.DetailWindow"));
		//detailMI.setEnabled(false);
		//popup.add(detailMI);
		
		settingsMI = new JMenuItem(ElanLocale.getString("SpectrogramViewer.Settings"));
		settingsMI.addActionListener(this);
		popup.addSeparator();
		popup.add(settingsMI);
	}

	@Override
	protected void updateZoomPopup(float zoom) {
		super.updateZoomPopup(zoom);
	}

	@Override
	protected void zoomToSelection() {
    	long selInterval = getSelectionEndTime() - getSelectionBeginTime();
    	if (selInterval < 150) {
    		selInterval = 150;
    	}
    	int sw = imageWidth != 0 ? imageWidth - (2 * SCROLL_OFFSET) : getWidth() - (2 * SCROLL_OFFSET);
    	float nextMsPP = selInterval / (float) sw;
    	setMsPerPixel(nextMsPP);
    	
    	if (!playerIsPlaying()) {
    		long ibt = getSelectionBeginTime() - (long)(SCROLL_OFFSET * msPerPixel);
    		if (ibt < 0) {
    			ibt = 0;
    		}
    		setIntervalBeginTime(ibt);
    	}
	}

	/**
	 * Passes the media sampler's sample frequency to the settings object where
	 * the maximum frequency is (re)calculated as well as (actual) the window 
	 * and stride sizes. 
	 */
	private void calcWindowAndStride() {
		if (mediaSampler == null) {
			return;
		}
		if (specSettings.getSampleFrequency() != mediaSampler.getSampleFrequency()) {
			specSettings.setSampleFrequency(mediaSampler.getSampleFrequency());
		}

	}
	
	/**
	 * Performs the steps that are required to create a new spectrogram image.
	 * It conditionally (re-)loads audio data, (re-)applies the FFT transform
	 * and (re-)creates the image based on the frequency data.
	 */
	private void recreateSpectrogramImage() {
		if (mediaSampler == null) {
			//log
			return;
		}

		int[][] samples = null;
		int numToRead = 0;
		// start with possible changes in settings
		boolean needNewSamples = specSettings.isNewDataRequired();
		boolean needNewTransform = specSettings.isNewTransformRequired();
		boolean needNewImage = specSettings.isNewImageRequired();
		
		// check if reloading is required
		if (curCache == null) {
			needNewSamples = true;
			needNewTransform = true;
			needNewImage = true;
			curCache = new IntervalCache();
		} else {
			// check begin and end time
			if (curCache.beginTime != intervalBeginTime) {
				needNewSamples = true;
			} else if (curCache.endTime < intervalEndTime) {
				needNewSamples = true;
			}
		}		

		//==== sample loading ====
		if (needNewSamples) {
			// read some extra samples
			double samplesPerPixel = (msPerPixel * 0.001) * mediaSampler.getSampleFrequency();
			double extraSec = 0.0d;
			if (samplesPerPixel / specSettings.getNumSamplesPerWindow() < 1) {
				extraSec = (2 * specSettings.getActualWindowDurationSec()); 
			} else {
				extraSec = (2 * specSettings.getPixelDurationSec());
			}
			double toTimeSec = (intervalEndTime + mediaTimeOffset) * 0.001d;
			toTimeSec += extraSec;
			if (toTimeSec > mediaSampler.getDurationSeconds()) {
				toTimeSec = mediaSampler.getDurationSeconds();
			}
			double fromTimeSec = (intervalBeginTime + mediaTimeOffset) * 0.001d;
			// push the image a bit to the right? For better alignment with the waveform? 
//			if (fromTimeSec > specSettings.getActualWindowDurationSec()) {
//				fromTimeSec -= specSettings.getPixelDurationSec(); 
//			}
			double durSec = toTimeSec - fromTimeSec;
			if (durSec <= 0) {
				return;
			}
			numToRead = (int) Math.ceil(durSec * mediaSampler.getSampleFrequency()); 
			samples = loadSamples(fromTimeSec, toTimeSec, numToRead);
			
	    	if (LOG.isLoggable(Level.FINE)) {
				LOG.log(Level.FINE, String.format("New samples: from: %d, to %.3f, sample count: %.3f", 
						(samples == null ? 0 : numToRead), fromTimeSec, toTimeSec));
			}
		}	
		//==== end sample loading ====
		// check whether new transform is required
		if (needNewSamples) {
			needNewTransform = true;
		}
		//==== frequency transform ====
		double[][][] freqWindows = null;
		
		if (needNewTransform && (samples != null || curCache.loadedSamples != null)) {
			if (samples == null) {
				samples = curCache.loadedSamples;
				numToRead = curCache.samplesUsed;
			}
			
			freqWindows = new double[2][][];
			freqWindows[0] = getFrequencies(samples[0], numToRead);
			if (samples[1] != null) {
				freqWindows[1] = getFrequencies(samples[1], numToRead);
			}
			
	    	if (LOG.isLoggable(Level.FINE)) {
				LOG.log(Level.FINE, String.format("New frequencies: number of windows: %d, number of bins per window: %d", 
						(freqWindows == null ? 0 : freqWindows[0].length), (freqWindows == null ? 0 : freqWindows[0][0].length)));
			}
		}
		//==== end frequency transform ====
		// check if new image is required
		if (needNewTransform) {
			needNewImage = true;
		} else {
			if (curCache.imgHeight != getHeight() || curCache.imgWidth < getWidth()) {
				needNewImage = true;
			}
		}
		//==== image creation ====
		if (needNewImage && getWidth() > 0 && getHeight() > 0) {
			imageWidth = getWidth();
			if (mediaSampler.getDuration() < intervalEndTime) {
				// calculate image width based on actual duration
				double actDuration = mediaSampler.getDuration() - intervalBeginTime;
				imageWidth = (int) (actDuration / msPerPixel);
			}
			imageHeight = getHeight();
			if (freqWindows == null && curCache.freqWindows != null) {
				freqWindows = curCache.freqWindows;
			}
			
			if (freqWindows[1] == null) {
				specImage = toImage(freqWindows[0], imageWidth, imageHeight);
			} else {
				specImage = toStereoImage(freqWindows, imageWidth, imageHeight);
			}
			repaint();
			
			// set or update cached data
			curCache.beginTime = intervalBeginTime;
			curCache.endTime = intervalEndTime;
			if (freqWindows != null)
				curCache.freqWindows = freqWindows;
			if (samples != null)
				curCache.loadedSamples = samples;
			if (numToRead != 0)
				curCache.samplesUsed = numToRead;
			curCache.imgWidth = imageWidth;
			curCache.imgHeight = imageHeight;

			//System.out.println("Created image: W: " + specImage.getWidth(null) + " H: " + specImage.getHeight(null));
	    	if (LOG.isLoggable(Level.FINE)) {
				LOG.log(Level.FINE, String.format("New image: width: %d, height: %d", 
						imageWidth, imageHeight));
			}
		}
		specSettings.resetFlags();
		refreshPending = false;
	}
	
	// The method below attempts to load additional data if most of the 
	// data for the current/new interval are already there and only 10%
	// or 20% of the data is new. There are some glitches, especially
	// in case of rapid succession of updates: either the alignment with the
	// timeline gets a bit "off" or combination of the parts isn't seamless.
	/*
	@SuppressWarnings("unused")
	private boolean shiftAndLoad(float overlap) {
		if (curCache == null) return false;
		if (overlap >= 0.8 && overlap < 1.0f){
			// same size partial reload, new fragment left or right
			boolean extendLeft = intervalBeginTime < curCache.beginTime;
			boolean extendRight = intervalEndTime > curCache.endTime;
			
			if (!extendLeft && !extendRight) return false;
			
			System.out.println("Shifting left/right, Thread: " + Thread.currentThread().getName());
			System.out.println(String.format("Percentage to shift: %f, #samples: %d, #windows: %d", (1 - overlap), 
					(int)((1 - overlap) * curCache.samplesUsed), (int)((1 - overlap) * curCache.freqWindows.length)));
			double shiftPerc = 1 - overlap;
			double shiftSamples = shiftPerc * curCache.samplesUsed;
			double shiftWindows = shiftPerc * curCache.freqWindows.length;
						
			// attempt based on shifting of equivalents of whole pixels, and 1 pixel overlap loaded for new segment
			double samplesPerPixel = (msPerPixel * 0.001) * mediaSampler.getSampleFrequency();
			double secPerPixel = specSettings.getPixelDurationSec();
			double pixWinRatio = samplesPerPixel / specSettings.getNumSamplesPerWindow();			
			int outsideSampleShift;
			int outsideWinShift;

			double fromTimeSec, toTimeSec;
			System.out.println("Samples per pixel: " + samplesPerPixel + " per window: " + specSettings.getNumSamplesPerWindow());
			// read extra
			double extraSec = 0.0d;
			if (pixWinRatio < 1) {
				extraSec = (2 * specSettings.getActualWindowDurationSec()); 
			} else {
				extraSec = (2 * secPerPixel);
			}
			
			if (extendRight) {
				toTimeSec = (intervalEndTime + mediaTimeOffset) * 0.001d;
				toTimeSec += extraSec;
				fromTimeSec = (curCache.endTime + mediaTimeOffset) * 0.001d;
				if (toTimeSec > mediaSampler.getDurationSeconds()) {
					toTimeSec = mediaSampler.getDurationSeconds();
				}
			} else {
				toTimeSec = (curCache.beginTime + mediaTimeOffset) * 0.001d;
				toTimeSec += extraSec;
				fromTimeSec = (intervalBeginTime + mediaTimeOffset) * 0.001d;
				if (fromTimeSec < 0) {
					fromTimeSec = 0d;
				}
			}

			double durSec = toTimeSec - fromTimeSec;
			System.out.println("Orig sample diff: " + (durSec * mediaSampler.getSampleFrequency()));
			int numToRead = (int) Math.ceil(durSec * mediaSampler.getSampleFrequency());

			// check if we need to make a copy of the samples array
			int[] orgSamples = null;
			if (curCache.loadedSamples == mediaSampler.getChannelArray(0) || curCache.loadedSamples == mediaSampler.getChannelArray(1)) {
				orgSamples = Arrays.copyOf(curCache.loadedSamples, curCache.samplesUsed);
			} else {
				orgSamples = curCache.loadedSamples;
			}
			
			mediaSampler.seekTimeSeconds(fromTimeSec);
			int[][] samplesArray = loadSamples(fromTimeSec, toTimeSec, numToRead);
			int[] samples = samplesArray[0];
			if (samples == null) {
				return false;
			}

			// samples have been loaded
			double[][] freqWindows = getFrequencies(samples, numToRead);
			if (freqWindows == null) {
				return false;
			}
			System.out.println(String.format("Windows to drop %f, new windows %d", shiftWindows, freqWindows.length));
			outsideSampleShift = (int)Math.round(shiftSamples);			
			System.out.println(String.format("Num samples read: %d, shift samples: %d, discard samples: %d", 
					numToRead, outsideSampleShift, (numToRead - outsideSampleShift)));
			int[] mergedSamples = new int[curCache.samplesUsed];
			
			if (extendRight) {
				System.arraycopy(orgSamples, outsideSampleShift, mergedSamples, 0, 
						mergedSamples.length - outsideSampleShift);// includes the overlap
				int insert = curCache.samplesUsed - outsideSampleShift;
				System.arraycopy(samples, 0, mergedSamples, insert , mergedSamples.length - insert);
			} else {
				System.arraycopy(samples, 0, mergedSamples, 0, numToRead);// copies all
				System.arraycopy(orgSamples, 0, mergedSamples, outsideSampleShift, curCache.samplesUsed - outsideSampleShift);
			}
			
			curCache.loadedSamples = mergedSamples;
			curCache.samplesUsed = mergedSamples.length;
			curCache.beginTime = intervalBeginTime;
			curCache.endTime = intervalEndTime;
			
			outsideWinShift = (int)Math.round(shiftWindows);
			System.out.println(String.format("Shift windows: %d", outsideWinShift));
			System.out.println("Num windows to discard: " + (freqWindows.length - outsideWinShift));
			double[][] mergedFreqs = new double[curCache.freqWindows.length][];
			if (extendRight) {
				System.arraycopy(curCache.freqWindows, outsideWinShift, mergedFreqs, 0, curCache.freqWindows.length - outsideWinShift);		
				int winsert = mergedFreqs.length - outsideWinShift;
				System.arraycopy(freqWindows, 0, mergedFreqs, winsert, Math.min(mergedFreqs.length - winsert, freqWindows.length));
			} else {
				System.arraycopy(freqWindows, 0, mergedFreqs, 0, freqWindows.length);
				System.arraycopy(curCache.freqWindows, 0, mergedFreqs, outsideWinShift, curCache.freqWindows.length - outsideWinShift);
			}
			curCache.freqWindows = mergedFreqs;
			
			specImage = toImage(mergedFreqs, curCache.imgWidth, curCache.imgHeight);
			//curCache.image = specImage;
			repaint();
			return true;
			
		}
		return false;
	}
	*/
	// end test
	// load requested number of samples for the requested time interval
	//
	/**
	 * Loads audio samples for the specified time interval.
	 * Depending on settings the sampler combines (averages) the samples of two
	 * channels into a single array.
	 * 
	 * @param fromTimeSec the start time of the interval in seconds
	 * @param toTimeSec the end time of the interval in seconds
	 * @param numToRead the calculated number of bytes to read, corresponding
	 * to the interval. A WAVSampler might reuse arrays for multiple read 
	 * actions, therefore the length of the returned array can not be used
	 * as an indication of how many samples have been loaded.
	 * 
	 * @return a two-dimensional array of size 2 of loaded audio samples, in most
	 * cases the array at index 1 will be {@code null} or empty if the
	 * sampler is {@code null}
	 */
	private int[][] loadSamples(double fromTimeSec, double toTimeSec, int numToRead) {// double[] ?
		if (mediaSampler != null) {
			mediaSampler.seekTimeSeconds(fromTimeSec);			
			
			double duration = (Math.min(mediaSampler.getDurationSeconds(), toTimeSec)) - fromTimeSec;
			if (duration <= 0) {
				return null;
			}
			
			boolean padAtEnd = mediaSampler.getDurationSeconds() <= toTimeSec;
			int[][] loadedArray = new int[2][];
			@SuppressWarnings("unused")
			int numActRead = 0;
			if (mediaSampler.getNumberOfChannels() > 1) {
				if (specSettings.getChannelMode() == SpectrogramSettings.FREQ_CHANNEL.CHANNEL_2) {
					numActRead = mediaSampler.readInterval(numToRead, 2);
					if (padAtEnd) {
						int[] loaded = mediaSampler.getChannelArray(1);
						if (loaded.length < numToRead) {
							//int[] padded = Arrays.copyOf(loaded, numToRead);
							//System.out.println(String.format("Read and pad samples; req: %d, act: %d", padded.length, loaded.length));
							loaded = Arrays.copyOf(loaded, numToRead);
						}
						loadedArray[0] = loaded;
					} else {
						//System.out.println(String.format("Read samples; req: %d, act: %d", numToRead, numActRead));
						loadedArray[0] = mediaSampler.getChannelArray(1);
					}
				} else if (specSettings.getChannelMode() == SpectrogramSettings.FREQ_CHANNEL.CHANNEL_ALL) {
					numActRead = mediaSampler.readInterval(numToRead, 1);// merges the channels in the WAVSamplers
					if (padAtEnd) {
						int[] loaded = mediaSampler.getChannelArray(0);
						if (loaded.length < numToRead) {
							//int[] padded = Arrays.copyOf(loaded, numToRead);
							//System.out.println(String.format("Read and pad samples; req: %d, act: %d", padded.length, loaded.length));
							loaded = Arrays.copyOf(loaded, numToRead);
						}
						loadedArray[0] = loaded;
					} else {
						//System.out.println(String.format("Read samples; req: %d, act: %d", numToRead, numActRead));
						loadedArray[0] = mediaSampler.getChannelArray(0);
					}
				} else if (specSettings.getChannelMode() == SpectrogramSettings.FREQ_CHANNEL.CHANNEL_SEPARATE) {
					numActRead = mediaSampler.readInterval(numToRead, 2);
					if (padAtEnd) {
						int[] loaded = mediaSampler.getChannelArray(0);
						int[] loaded2 = mediaSampler.getChannelArray(1);
						if (loaded.length < numToRead) {
							//int[] padded = Arrays.copyOf(loaded, numToRead);
							//System.out.println(String.format("Read and pad samples; req: %d, act: %d", padded.length, loaded.length));
							loaded = Arrays.copyOf(loaded, numToRead);
							loaded2 = Arrays.copyOf(loaded2, numToRead);
						}
						loadedArray[0] = loaded;
						loadedArray[1] = loaded2;
					} else {
						//System.out.println(String.format("Read samples; req: %d, act: %d", numToRead, numActRead));					
						loadedArray[0] = mediaSampler.getChannelArray(0);
						loadedArray[1] = mediaSampler.getChannelArray(1);
					}
				} else {
					// first channel
					numActRead = mediaSampler.readInterval(numToRead, 2);
					if (padAtEnd) {
						int[] loaded = mediaSampler.getChannelArray(0);
						if (loaded.length < numToRead) {
							//int[] padded = Arrays.copyOf(loaded, numToRead);
							//System.out.println(String.format("Read and pad samples; req: %d, act: %d", padded.length, loaded.length));
							loaded = Arrays.copyOf(loaded, numToRead);
						}
						loadedArray[0] = loaded;
					} else {
						//System.out.println(String.format("Read samples; req: %d, act: %d", numToRead, numActRead));
						loadedArray[0] = mediaSampler.getChannelArray(0);
					}
				}
			} else {// one channel
				numActRead = mediaSampler.readInterval(numToRead, 1);
				if (padAtEnd) {
					int[] loaded = mediaSampler.getChannelArray(0);
					if (loaded.length < numToRead) {
						//int[] padded = Arrays.copyOf(loaded, numToRead);
						//System.out.println(String.format("Read and pad samples; req: %d, act: %d", padded.length, loaded.length));
						loaded = Arrays.copyOf(loaded, numToRead);
					}
					loadedArray[0] = loaded;
				} else {
					//System.out.println(String.format("Read samples; req: %d, act: %d", numToRead, numActRead));
					loadedArray[0] = mediaSampler.getChannelArray(0);
				}
			}
			return loadedArray;
		}

		return null;
	}
	
	/**
	 * Conditionally and loads and applies a weighting window function to the
	 * samples when passing sliding windows of the samples to the Fourier 
	 * transform. 
	 * 
	 * @param samples the loaded audio samples
	 * @param numSamplesToUse the number of samples to use for the transform
	 * @return a two-dimensional array of "frequency bins", each array of bins
	 * representing one column, one window of the samples, each bin 
	 * representing the power or intensity of a range of frequencies
	 * 
	 * @see {@link FFT}
	 */
	private double[][] getFrequencies(int[] samples, int numSamplesToUse) {
		if (specSettings.getNumSamplesPerWindow() == 0 || specSettings.getNumSamplesPerStride() == 0) {
			calcWindowAndStride();
		}
		
		return Frequency.getFrequencies(samples, numSamplesToUse, specSettings);
	}
	
	/**
	 * Creates an image based on the provided frequency data and image 
	 * dimensions. A speed/quality trade off might be made.
	 * 
	 * @param freqWindows the frequency data, the source for the image
	 * @param imWidth the width of the image to create
	 * @param imHeight the height of the image
	 * 
	 * @return a new image or {@code null}
	 * 
	 * @see {@link ImageCreator}
	 */
	private BufferedImage toImage(double[][] freqWindows, int imWidth, int imHeight) {
		if (freqWindows != null && imWidth > 0 && imHeight > 0) {
			if (playerIsPlaying()) {
				return imgCreator.createSpecImage(freqWindows, imWidth, imHeight, 
						SpectrogramSettings.PERFORMANCE.SPEED);
			} else {
				return imgCreator.createSpecImage(freqWindows, imWidth, imHeight, 
						SpectrogramSettings.PERFORMANCE.QUALITY);
			}
		}
		
		return null;
	}
	
	/**
	 * Creates an image with two channels based on the provided frequency 
	 * data and image dimensions. A speed/quality trade off might be made.
	 * 
	 * @param freqWindows the frequency data, the source for the image, the outer
	 *        array should have size two
	 * @param imWidth the width of the image to create
	 * @param imHeight the height of the image
	 * 
	 * @return a new image or {@code null}
	 * 
	 * @see {@link ImageCreator}
	 */
	private BufferedImage toStereoImage(double[][][] freqWindows, int imWidth, int imHeight) {
		if (freqWindows != null && imWidth > 0 && imHeight > 0) {
			if (playerIsPlaying()) {
				return imgCreator.createStereoSpecImage(freqWindows, imWidth, imHeight, 
						SpectrogramSettings.PERFORMANCE.SPEED);
			} else {
				return imgCreator.createStereoSpecImage(freqWindows, imWidth, imHeight, 
						SpectrogramSettings.PERFORMANCE.QUALITY);
			}
		}
		
		return null;
	}

	/**
	 * Applies the new media offset and recreates the spectrogram image
	 * 
	 * @param offset the new offset
	 */
	@Override
	public void setMediaTimeOffset(long offset) {
		if (offset != mediaTimeOffset) {
			long shift = offset - mediaTimeOffset;
			super.setMediaTimeOffset(offset);
			if (curCache != null) {
				curCache.beginTime += shift;
				curCache.endTime += shift;
			}
			// update the current image
	        crossHairPos = xAt(crossHairTime);
	        selectionBeginPos = xAt(getSelectionBeginTime());
	        selectionEndPos = xAt(getSelectionEndTime());
	        
	        recreateSpectrogramImage();
		}
	}

	/**
	 * Temporary implementation; it might be that other media files were changed
	 * and not this viewer's media. The check is performed here.
	 */
	@Override
	public void mediaOffsetChanged() {
		String url = null;
		if (this.mediaDescriptor != null) {
			url = mediaDescriptor.mediaURL;
		} else {
			url = FileUtility.pathToURLString(mediaFilePath);
		}
		List<MediaDescriptor> descs = getViewerManager().getTranscription().getMediaDescriptors();
		for (MediaDescriptor md : descs) {			
			if (url.equals(md.mediaURL)) {
				this.setMediaTimeOffset(md.timeOrigin);
				break;
			}			 
		}
	}

	@Override
	public void setMediaTime(long milliSeconds) {
		super.setMediaTime(milliSeconds);
	}
	
	/**
	 * A change of the interval almost always requires recreation of the spectrogram
	 * data and image. Partial updating of the image doesn't work reliably (yet).
	 * 
	 * @param begin the new interval begin time
	 */
	@Override
	protected void setLocalTimeScaleIntervalBeginTime(long begin) {
        if (begin == intervalBeginTime) {
            return;
        }
        
        if (playerIsPlaying()) {
	        // check the amount of time passed since the previous update and do nothing if 
	        // the time passed is < than a threshold value. Mainly an issue in ticker mode.
	        long curUpdate = System.currentTimeMillis();
	        long elapsed = curUpdate - lastUpdate;
	        
	        if (elapsed < 50) {
	        	refreshPending = true;
	        	return;
	        }
	        lastUpdate = curUpdate;
        }
        
        // this method will be only rarely be called simultaneously by separate threads
        // in most cases this will be one of AWT-EventQueue, EventPoster or a drag-scroll thread
    	try {
			if (shiftLoadLock.tryLock(40, TimeUnit.MILLISECONDS)) {
				try {
			        intervalBeginTime = begin;
			        intervalEndTime = intervalBeginTime +
			            (long) (intervalWidth * msPerPixel);
			        crossHairPos = xAt(crossHairTime);
			        selectionBeginPos = xAt(getSelectionBeginTime());
			        selectionEndPos = xAt(getSelectionEndTime());
			        
			        // compare amount of change between the old and new begin and end times  
			        // relative to the length of the interval, this doesn't work reliably (yet)
					/*
			        if (curCache != null) {
			            float overlap = amountOverlap(curCache.beginTime, curCache.endTime, 
			            		intervalBeginTime, intervalEndTime);
			            if (overlap > 0.8f && overlap < 1) {
			            	// shift and load
			            	shiftAndLoad(overlap);
			            } else {
			            	recreateSpectrogramImage();
			            }
			        } else {
			        */
			        recreateSpectrogramImage();
			        //}
				} finally {
					shiftLoadLock.unlock();
				}
		        //repaint();
			}
		} catch (InterruptedException ie) {
			// ignore interruption of the thread
		}
	}
	
	@SuppressWarnings("unused")
	private float amountOverlap(long ob, long oe, long nb, long ne) {
		if (nb >= oe) return 0f;
		if (ne <= ob) return 0f;
		if (nb == ob && ne > oe) return (ne - nb) / (float)(oe - ob); // > 1
		if (ne == oe && nb < ob) return (ne - nb) / (float)(oe - ob); // > 1
		
		if (nb > ob) return (oe - nb) / (float)(oe - ob);// scrolling left, new data on the right
		if (nb < ob) return (ne - ob) / (float)(oe - ob);// scrolling right
		
		return 0f;
	}
	
	@Override
	protected void recalculateInterval(long mediaTime) {
		super.recalculateInterval(mediaTime);
	}

	/**
	 * Calls the super implementation and then checks whether a new image needs
	 * to be created.
	 */
	@Override
	public void updateTimeScale() {
		super.updateTimeScale();
		recreateSpectrogramImage();
	}

	/**
	 * A change in the resolution of the viewer always requires recreation of 
	 * the spectrogram image.
	 */
	@Override
	protected void setLocalTimeScaleMsPerPixel(float step) {
		if (step == msPerPixel) {
			return;
		}

        if (step >= TimeScaleBasedViewer.MIN_MSPP) {
            msPerPixel = step;
        } else {
            msPerPixel = TimeScaleBasedViewer.MIN_MSPP;
        }
		
        long mediaTime = getMediaTime();
        int oldScreenPos = crossHairPos;
        long newMediaX = (long) (mediaTime / msPerPixel);
        int numScreens;

        if (intervalWidth > 0) {
            numScreens = (int) (mediaTime / (intervalWidth * msPerPixel));
        } else {
            numScreens = 0;
        }

        int newScreenPos = (int) newMediaX - (numScreens * intervalWidth) + vertRulerWidth;
        int diff = oldScreenPos - newScreenPos;

        //new values
        intervalBeginTime = (long) (((numScreens * intervalWidth) - diff) * msPerPixel);

        if (intervalBeginTime < 0) {
            intervalBeginTime = 0;
        }

        intervalEndTime = intervalBeginTime +
            (long) (intervalWidth * msPerPixel);

        crossHairPos = xAt(mediaTime);
        selectionBeginPos = xAt(getSelectionBeginTime());
        selectionEndPos = xAt(getSelectionEndTime());
        
		specSettings.setPixelDurationSec(msPerPixel * 0.001);

        recreateSpectrogramImage();
        repaint();
        //update popup menu
        int zoom = Math.round(100f * (10f / msPerPixel));
        if (zoom <= 0) {
            zoom = 100;
        }

        updateZoomPopup(zoom);
	}

	/**
	 * Calls the {@code super} implementation. In case of a stop event it 
	 * checks whether there is a pending update of the image.
	 */
	@Override
	public void controllerUpdate(ControllerEvent event) {	
		super.controllerUpdate(event);
		
		if (event instanceof StopEvent && refreshPending) {
	    	EventQueue.invokeLater(() -> {
	    		recreateSpectrogramImage();
			});
		}
		
	}

	/**
	 * Paints the spectrogram image, an indication of the selected interval 
	 * and a crosshair for the current media time.
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

    	synchronized(getTreeLock()) {	
			Graphics2D g2d = (Graphics2D) g;
			g2d.setColor(Constants.DEFAULTBACKGROUNDCOLOR);
			g2d.fillRect(0, 0, getWidth(), getHeight());
			/* currently the same behavior
			if (useBufferedImage) {
				if (specImage != null) {
					g2d.drawImage(specImage, 0, 0, this);
				}	
			} else {*/
				// use a spectrogram image directly
				if (specImage != null) {
					g2d.drawImage(specImage, 0, 0, this);
				}
			//}

			int h = getHeight();
			Stroke basicStr = g2d.getStroke();
	        //paint selection
	        if (selectionBeginPos != selectionEndPos) {
	        	/* maybe this obscures the image
	            g2d.setColor(Constants.SELECTIONCOLOR);
	            g2d.setComposite(alpha04);
	            g2d.fillRect(selectionBeginPos, 0,
	                (selectionEndPos - selectionBeginPos), h);
	            */
	            g2d.setComposite(AlphaComposite.Src);
	            g2d.setColor(Constants.ACTIVEANNOTATIONCOLOR);
	            g2d.setStroke(dashLine);
//	            g2d.drawRect(selectionBeginPos, 0,
//		                (selectionEndPos - selectionBeginPos), h - 1);
	            g2d.drawLine(selectionBeginPos, 0, selectionBeginPos, h - 1);
	            g2d.drawLine(selectionEndPos, 0, selectionEndPos, h - 1);
	        }
	        /* gridlines?
			Stroke basicStr = g2d.getStroke();
	        g2d.setColor(Color.WHITE);
	        g2d.setStroke(dashLine);
	        g2d.drawLine(0, h/2, getWidth(), h/2);
	        g2d.setStroke(basicStr);
	        */
	        //draw the cursor
	        g2d.setStroke(basicStr);
	        g2d.setColor(Constants.CROSSHAIRCOLOR);
	        g2d.drawLine(crossHairPos, 0, crossHairPos, h);
	        
	        if (errorKey != null) {
	        	g2d.setColor(Constants.DEFAULTFOREGROUNDCOLOR);
	        	g2d.drawString(errorKey, 10, getHeight() / 2);
	        }
    	} 

	}

	@Override
	public void setMsPerPixel(float mspp) {
		super.setMsPerPixel(mspp);
		// updateTimeScale might not be called?
		//recreateSpectrogramImage();
	}

	@Override
	public void updateSelection() {
		selectionBeginTime = getSelectionBeginTime();
		selectionEndTime = getSelectionEndTime();
		selectionBeginPos = xAt(selectionBeginTime);
		selectionEndPos = xAt(selectionEndTime);
		
		if (selectionEndPos < selectionBeginPos) {
			selectionEndPos = selectionBeginPos;
		}
		
		repaint();
	}

	/**
	 * A change in width often requires loading of new data and creation of a
	 * new image, a change in height requires a new image. 
	 */
	@Override
	public void componentResized(ComponentEvent e) {
		intervalWidth = getWidth();
		
    	long curEndTime = intervalEndTime;
        intervalEndTime = intervalBeginTime + (int) (intervalWidth * msPerPixel);
        
        // horizontal resizing
        if (curEndTime != intervalEndTime) {
        	if (timeScaleConnected) {
        		// this does not always trigger a setLocal... or updateTimeScale method
        		setGlobalTimeScaleIntervalEndTime(intervalEndTime);
        	}
    		// check if a new spectrogram image has to be created for the 
        	// current interval
    		// if the new interval end is smaller than the old, it might be
    		// worth clipping the image instead of recreating?
    		recreateSpectrogramImage();    	
        } else {
        	//possible vertical resizing; check if height changed, recreate image if so
        	if (specImage == null || specImage.getHeight() != getHeight() || 
        			specImage.getWidth() != getWidth()) {
        		recreateSpectrogramImage();
        	}
        }
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == settingsMI) {
			SpectrogramSettingsDialog settingsDiag = new SpectrogramSettingsDialog(null, specSettings);
			//settingsDiag.setLocationRelativeTo(this);
			settingsDiag.setVisible(true);
			recreateSpectrogramImage();
			storePreferences();
			return;
		}
		super.actionPerformed(e);
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			if (e.getSource() == channel1MI) {
				specSettings.setChannelMode(FREQ_CHANNEL.CHANNEL_1);			
			} else if (e.getSource() == channel2MI) {
				specSettings.setChannelMode(FREQ_CHANNEL.CHANNEL_2);	
			} else if (e.getSource() == channel12MI) {
				specSettings.setChannelMode(FREQ_CHANNEL.CHANNEL_ALL);
			} else if (e.getSource() == channel12SeparateMI) {
				specSettings.setChannelMode(FREQ_CHANNEL.CHANNEL_SEPARATE);
			}
		}

		recreateSpectrogramImage();
		setPreference("SpectrogramViewer.ChannelMode", specSettings.getChannelMode().toString(), 
				getViewerManager().getTranscription());
	}

	/**
	 * Sets and shows a tooltip text depending on the position of the mouse 
	 * pointer relative to the spectrogram image..
	 */
	@Override
	public void mouseMoved(MouseEvent e) {
		// ideally ticker mode should be detectable and checked for here
		if (playerIsPlaying()) {
			this.setToolTipText(null);
			return;
		}
		double t = (intervalBeginTime + (e.getX() * msPerPixel)) * 0.001d;
		long tt = timeAt(e.getX());
		// try to map the y-coordinate to a frequency range (not necessarily corresponding to frequency bins)
		double maxFreq = Math.min(specSettings.getMaxDisplayFrequency(), specSettings.getPossibleMaxFrequency());
		double freqRange = maxFreq - specSettings.getMinDisplayFrequency();
		double vertPos = (getHeight() - e.getY()) / (double) getHeight();
		double freqPerPix = freqRange / getHeight();
		// adjust values for separate channel display
		if (specSettings.getChannelMode() == SpectrogramSettings.FREQ_CHANNEL.CHANNEL_SEPARATE
				&& mediaSampler.getNumberOfChannels() > 1) {
			double h = getHeight() / 2.0d;
			freqPerPix = freqRange / h;
			double y = e.getY() > h ? e.getY() - h : e.getY();
			vertPos = (h - y) / h;
		}
		double freq = (vertPos * freqRange) + specSettings.getMinDisplayFrequency();
		this.setToolTipText(String.format(tooltipFormat, t, TimeFormatter.toString(tt), freq - freqPerPix, freq));
	}

	@Override
	public void preferencesChanged() {
		super.preferencesChanged();
		// check viewer specific preferences, only at load time
		if (!prefsInited) {
			initPreferences();
		}
		// check global spectrogram preferences, also only once?
		if (!globalPrefsInited) {
			loadPreferences();
		}
		useBufferedImage = false;
	}
    
	/**
	 * Restore some settings from previous sessions. 
	 */
	private void initPreferences() {
        String chMode = Preferences.getString("SpectrogramViewer.ChannelMode", getViewerManager().getTranscription());
        if (chMode != null) {
        	if (chMode.equals(SpectrogramSettings.FREQ_CHANNEL.CHANNEL_1.toString())) {
        		specSettings.setChannelMode(SpectrogramSettings.FREQ_CHANNEL.CHANNEL_1);
        	} else if (chMode.equals(SpectrogramSettings.FREQ_CHANNEL.CHANNEL_2.toString())) {
        		specSettings.setChannelMode(SpectrogramSettings.FREQ_CHANNEL.CHANNEL_2);
        	} else if (chMode.equals(SpectrogramSettings.FREQ_CHANNEL.CHANNEL_SEPARATE.toString())) {
        		specSettings.setChannelMode(SpectrogramSettings.FREQ_CHANNEL.CHANNEL_SEPARATE);
        	} else {
        		specSettings.setChannelMode(SpectrogramSettings.FREQ_CHANNEL.CHANNEL_ALL);
        	}
        }
        prefsInited = true;
	}
	
	private void storePreferences() {
		SpectrogramSettingsIO.storeSpectrogramSettings(specSettings);
	}
	
	private void loadPreferences() {
		SpectrogramSettingsIO.loadSpectrogramSettings(specSettings);
		
		globalPrefsInited = true;
		recreateSpectrogramImage();
	}
	 
	/** 
	 * A class to collect and store data concerning the current or latest 
	 * produced image. The cached values can help to determine whether new
	 * samples need to be loaded, and/or new transform is required and/or 
	 * a new image needs to be created.
	 */
    private class IntervalCache {
    	// two dimensional array of size 2, usually the array at index 1 is null
    	// the arrays might be reused by the sampler unless a copy is made
    	int[][] loadedSamples;
    	int   samplesUsed;// the array can be longer than the used samples
    	// three dimensional array of size 2, usually the two dimensional array at
    	// index 1 is null
    	double[][][] freqWindows;
    	int imgWidth;
    	int imgHeight;
    	long beginTime;
    	long endTime;
    }
	
}
