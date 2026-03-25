package mpi.eudico.client.annotator.commands;

import static mpi.eudico.client.annotator.util.ClientLogger.LOG;

import java.util.logging.Level;

import mpi.eudico.client.annotator.Selection;
import mpi.eudico.client.annotator.gui.AudioSpectrogramPanel;
import mpi.eudico.server.corpora.clom.Transcription;
import nl.mpi.media.spectrogram.SpectrogramSettings;

/**
 * Displays a dialog window containing an audio spectrogram rendering of an 
 * interval of an audio source.
 *
 * @author Allan van Hulst
 */
public class AudioSpectrogramCommand implements Command {
    private final String name;
    private Transcription transcription;

    /**
     * Constructor.
     *
     * @param name the name
     */
    public AudioSpectrogramCommand(String name) {
        this.name = name;
    }

    /**
     * Executes the command.
     * 
     * @param receiver the transcription
     * @param arguments the arguments: <ul>
     * <li>arg[0] = a two dimensional array of audio samples (int[][])</li>
     * <li>arg[1] = settings for the spectrogram visualization (SpectrogramSettings)</li>
     * <li>arg[2] = the selection instance (Selection)</li>
     * <li>arg[3] = the interval start time (a bit before the selection start time) (Long)</li>
     * <li>arg[4] = the interval end time (a bit after the selection end time) (Long)</li>
     * </ul>
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {
        if (receiver instanceof Transcription) {
            transcription = (Transcription) receiver;
        }
        if (arguments.length < 5) {
    		if (LOG.isLoggable(Level.INFO)) {
    			LOG.log(Level.INFO, String.format(
    					"Too few arguments: %d instead of %d ", arguments.length, 5));
    		}
        	return;
        }
        AudioSpectrogramPanel panel = new AudioSpectrogramPanel(
        		(int[][]) arguments[0], 
        		(SpectrogramSettings)arguments[1],
        		(Selection) arguments[2], 
        		(Long)arguments[3],
        		(Long)arguments[4]);
        
        panel.show(ELANCommandFactory.getRootFrame(transcription));
    }

    @Override
    public String getName() {
        return name;
    }

}
