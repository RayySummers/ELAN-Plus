package mpi.eudico.client.annotator.commands;

import java.util.Arrays;
import java.util.logging.Level;

import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.spectrogram.SpectrogramSettingsIO;
import static mpi.eudico.client.annotator.util.ClientLogger.LOG;
import mpi.eudico.client.util.WAVSamplesProvider;
import nl.mpi.media.spectrogram.SpectrogramSettings;

/**
 * Opens a dialog window to display an audio spectrogram of an interval of an
 * audio stream.
 *
 * @author Allan van Hulst
 */
@SuppressWarnings("serial")
public class AudioSpectrogramCA extends CommandAction {
	private WAVSamplesProvider samplesProvider;
	private long offset;
	private SpectrogramSettings settings;

	/**
     * Constructor.
     *
     * @param theVM the viewer manager
     */
    public AudioSpectrogramCA(ViewerManager2 theVM) {
        super(theVM, ELANCommandFactory.AUDIO_SPECTROGRAM);
    }

    /**
     * Creates a new command.
     */
    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), 
        		ELANCommandFactory.AUDIO_SPECTROGRAM);
    }

    @Override
    protected Object getReceiver() {
        return vm.getTranscription();
    }

    /**
     * Creates and returns an array of arguments for the command's 
     * {@code execute} method.
     * 
     * @return an array of objects for the connected command
     */
    @Override
	protected Object[] getArguments() {
    	long selBegin = vm.getSelection().getBeginTime();
    	long selEnd = vm.getSelection().getEndTime();
    	long duration = selEnd - selBegin;
    	// 10% margin
    	long margin = duration / 10;
    	long intervalStart = Math.max(0, selBegin - margin);
    	long intervalEnd = Math.min((long)samplesProvider.getDuration(), selEnd + margin);
    	
    	if (settings == null) {
    		settings = SpectrogramSettingsIO.loadSpectrogramSettings();
    		settings.setSampleFrequency(samplesProvider.getSampleFrequency());
    		settings.setPossibleMaxFrequency(samplesProvider.getSampleFrequency() / 2d);
        	settings.setBitsPerSample(samplesProvider.getBitsPerSample());
        	settings.setNormalizedInputData(true);
    	}
    	int numChannels = Math.min(2, samplesProvider.getNumberOfChannels());
    	samplesProvider.seekTime(intervalStart + offset);
    	int numToRead = (int) Math.ceil((intervalEnd - intervalStart) *  0.001d * samplesProvider.getSampleFrequency());
    	int numRead = samplesProvider.readInterval(numToRead, numChannels);
    	if (numToRead != numRead) {
    		if (LOG.isLoggable(Level.INFO)) {
    			LOG.log(Level.INFO, String.format(
    					"Number of samples requested: %d, number actually read: %d", numToRead, numRead));
    		}
    	}
    	
    	int[] channel0 = samplesProvider.getChannelArray(0);
    	int[] channel1 = samplesProvider.getChannelArray(1); // can be null
    	int[][] channels = new int[2][];
    	channels[0] = Arrays.copyOf(channel0, numRead);
    	if (channel1 != null) {
    		channels[1] = Arrays.copyOf(channel1, numRead);
    	}
    	
    	return new Object[] {channels, settings, vm.getSelection(), 
    			Long.valueOf(intervalStart), Long.valueOf(intervalEnd)};
	}

    /**
     * The audio samples provider for loading the interval samples. The context
     * where this action is used should set (and update) the samples provider
     * field of this action. 
     *  
     * @param samplesProvider the provider of the audio samples
     */
	public void setSamplesProvider(WAVSamplesProvider samplesProvider) {
		this.samplesProvider = samplesProvider;
    	if (settings != null) {
    		settings.setSampleFrequency(samplesProvider.getSampleFrequency());
    		settings.setPossibleMaxFrequency(samplesProvider.getSampleFrequency() / 2d);
        	settings.setBitsPerSample(samplesProvider.getBitsPerSample());
    	}
	}

	/**
	 * Sets the offset of the media file, {@code 0} by default.
	 *  
	 * @param offset the offset that has been set for the media file
	 */
	public void setOffset(long offset) {
		this.offset = offset;
	}
}
