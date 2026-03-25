package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.ListCollectionIDsDialog;
import mpi.eudico.server.corpora.clom.Transcription;

import javax.swing.*;

/**
 * A command that brings up the JDialog to list all the tiers and their collection IDs which were exported to the server from
 * the hsql db.
 */
public class ListCollectionIDSCommand implements Command {

    private final String name;
    private Transcription transcription;

    /**
     * Constructor.
     *
     * @param name the name
     */
    public ListCollectionIDSCommand(String name) {
        this.name = name;
    }

    /**
     * Creates the dialog
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {
        if (arguments[0] instanceof Transcription) {
            transcription = (Transcription) arguments[0];
        }

        if (transcription == null) {
            return;
        }

        ListCollectionIDsDialog dialog =
            new ListCollectionIDsDialog(ELANCommandFactory.getRootFrame(transcription), transcription);
        Boolean isListPresent = dialog.populateList();
        if (isListPresent) {
            dialog.setVisible(true);
        } else {
            dialog.setVisible(false);
            JOptionPane.showMessageDialog(dialog.getParent(),
                                          ElanLocale.getString("ListCollectionIDResult.Empty.Info"),
                                          ElanLocale.getString("ListCollectionIDResult.Info.Title"),
                                          JOptionPane.INFORMATION_MESSAGE);
        }

    }

    @Override
    public String getName() {
        return name;
    }

}
