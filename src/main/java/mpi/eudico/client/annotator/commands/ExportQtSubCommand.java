package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.Selection;
import mpi.eudico.client.annotator.export.ExportQtSubtitleDialog;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * A command to create and show a dialog for the export as QuickTime Subtitles.
 *
 * @author Alexander Klassmann
 * @version Jul 2, 2004
 */
public class ExportQtSubCommand implements Command {
    private final String commandName;

    /**
     * Creates a new ExportQtSubCommand instance
     *
     * @param theName the name of the command
     */
    public ExportQtSubCommand(String theName) {
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
        new ExportQtSubtitleDialog(ELANCommandFactory.getRootFrame((Transcription) arguments[0]),
                                   true,
                                   (TranscriptionImpl) arguments[0],
                                   (Selection) arguments[1]).setVisible(true);
    }

    @Override
    public String getName() {
        return commandName;
    }

}
