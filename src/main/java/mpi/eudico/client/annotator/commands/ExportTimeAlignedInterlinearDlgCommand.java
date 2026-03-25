package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.Selection;
import mpi.eudico.client.annotator.interlinear.ExportTimeAlignedInterlinear;
import mpi.eudico.client.annotator.util.WindowLocationAndSizeManager;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;


/**
 * Show a dialog for export in a time-aligned interlinear gloss style.
 *
 * @author Steffen Zimmermann
 * @version 1.0
 */
public class ExportTimeAlignedInterlinearDlgCommand implements Command {
    private final String commandName;

    /**
     * Creates a new ExportTabDelDlgCommand instance
     *
     * @param theName the name of the command
     */
    public ExportTimeAlignedInterlinearDlgCommand(String theName) {
        commandName = theName;
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver null
     * @param arguments the arguments:
     *     <ul>
     *     <li>arg[0] = the Transcription object(Transcription)</li>
     *     <li>arg[1] = the Selection object (Selection)</li>
     *     </ul>
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {

        ExportTimeAlignedInterlinear window =
            new ExportTimeAlignedInterlinear(ELANCommandFactory.getRootFrame((Transcription) arguments[0]),
                                             true,
                                             (TranscriptionImpl) arguments[0],
                                             (Selection) arguments[1]);
        WindowLocationAndSizeManager.postInit(window, "ExportTimeAlignedInterlinearDialog");
        window.setVisible(true);
        WindowLocationAndSizeManager.storeLocationAndSizePreferences(window, "ExportTimeAlignedInterlinearDialog");
    }

    @Override
    public String getName() {
        return commandName;
    }
}
