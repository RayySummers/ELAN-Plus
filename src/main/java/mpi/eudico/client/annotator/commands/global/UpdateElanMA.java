package mpi.eudico.client.annotator.commands.global;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.update.ElanUpdateDialog;

import static mpi.eudico.client.annotator.util.ClientLogger.LOG;

import java.awt.event.ActionEvent;
import java.util.logging.Level;

import javax.swing.ImageIcon;

/**
 * Menu action that checks for new updates of ELAN
 *
 * @author aarsom
 */
@SuppressWarnings("serial")
public class UpdateElanMA extends FrameMenuAction {

    /**
     * Creates a new UpdateElanMA instance
     *
     * @param name the name of the action
     * @param frame the containing frame
     */
    public UpdateElanMA(String name, ElanFrame2 frame) {
        super(name, frame);
        try {
        	ImageIcon icon = new ImageIcon(FrameMenuAction.class.getResource(Constants.ICON_LOCATION + "ExternalLink.gif"));
        	putValue(SMALL_ICON, icon);
        } catch (Exception e) {
        	if (LOG.isLoggable(Level.INFO)) {
        		LOG.log(Level.INFO, "Error loading image: " + e.getMessage());
        	}
        }
    }

    /**
     * Creates a updater and checks for update.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        ElanUpdateDialog updater = new ElanUpdateDialog(frame);
        updater.checkForUpdates();
    }

}
