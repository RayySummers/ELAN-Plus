package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Selection;
import mpi.eudico.client.annotator.export.ExportSmilDialog;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clom.TranscriptionStore;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.dobes.ACMTranscriptionStore;

import javax.swing.*;

/**
 * A command to show a dialog for the export as SMIL.
 *
 * @author Alexander Klassmann
 * @version Apr 15, 2004
 */
public class ExportSmilCommand implements Command {
    private final String commandName;

    /**
     * Creates a new ExportSmilCommand instance
     *
     * @param theName the name of the command
     */
    public ExportSmilCommand(String theName) {
        commandName = theName;
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver null
     * @param arguments the arguments:
     *     <ul>
     *     <li>arg[0] = the Transcription object (TranscriptionImpl)</li>
     *     <li>arg[1] = the Selection object (Selection)</li>
     *     </ul>
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {
        Transcription transcription = (Transcription) arguments[0];
        try {
            if ((transcription != null) && transcription.isChanged()) {
                boolean saveNewCopy = false;

                int response = JOptionPane.showConfirmDialog(null,
                                                             ElanLocale.getString("ExportSmil.Info")
                                                             + "\n"
                                                             + ElanLocale.getString("Frame.ElanFrame.UnsavedData"),
                                                             ElanLocale.getString("Message.Warning"),
                                                             JOptionPane.YES_NO_CANCEL_OPTION);

                if (response == JOptionPane.YES_OPTION) {
                    // save as dialog
                    // do a normal save
                    saveNewCopy = transcription.getName().equals(TranscriptionImpl.UNDEFINED_FILE_NAME);

                    TranscriptionStore ets = ACMTranscriptionStore.getCurrentTranscriptionStore();
                    StoreCommand storeComm = new StoreCommand(ELANCommandFactory.STORE);
                    storeComm.execute(transcription,
                                      new Object[] {ets,
                                                    Boolean.FALSE,
                                                    Boolean.valueOf(saveNewCopy),
                                                    null,
                                                    Integer.valueOf(TranscriptionStore.EAF)});
                } else if ((response == JOptionPane.CANCEL_OPTION) || (response == JOptionPane.CLOSED_OPTION)) {
                    return;
                }
            }

            new ExportSmilDialog(ELANCommandFactory.getRootFrame((Transcription) arguments[0]),
                                 true,
                                 (TranscriptionImpl) arguments[0],
                                 (Selection) arguments[1]).setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return commandName;
    }
}
