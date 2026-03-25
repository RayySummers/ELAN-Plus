package mpi.eudico.client.annotator.commands;

import static mpi.eudico.client.annotator.util.ClientLogger.LOG;

import java.util.logging.Level;

import javax.swing.ImageIcon;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ViewerManager2;

/**
 * A command action to open the Web Services dialog.
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class WebLichtDlgCA extends CommandAction {

    /**
     * Constructor
     *
     * @param theVM viewer manager
     */
    public WebLichtDlgCA(ViewerManager2 theVM) {
        super(theVM, ELANCommandFactory.WEBLICHT_DLG);
        try {
        	ImageIcon icon = new ImageIcon(CommandAction.class.getResource(Constants.ICON_LOCATION + "ExternalLink.gif"));
        	putValue(SMALL_ICON, icon);
        } catch (Exception e) {
        	if (LOG.isLoggable(Level.INFO)) {
        		LOG.log(Level.INFO, "Error loading image: " + e.getMessage());
        	}
        }
    }

    /**
     * Creates a WebServicesDlgCommand.
     */
    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.WEBSERVICES_DLG);
    }

    /**
     * @return the viewer manager
     */
    @Override
    protected Object getReceiver() {
        return vm;
    }

    /**
     * @return the constant for WebLicht to create a dialog for interacting with that service.
     */
    @Override
    protected Object[] getArguments() {
        return new String[] {ELANCommandFactory.WEBLICHT_DLG};
    }


}
