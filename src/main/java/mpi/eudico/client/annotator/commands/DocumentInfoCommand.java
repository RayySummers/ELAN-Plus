package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.gui.DocumentInfoDialog;
import mpi.eudico.server.corpora.clom.Transcription;

/**
 * A command to display a dialog to show document properties
 *
 * @author Allan van Hulst
 */
public class DocumentInfoCommand implements Command {
    private final String name;

    /**
     * Constructor.
     *
     * @param name the name
     */
    public DocumentInfoCommand(String name) {
        this.name = name;
    }

    /**
     * @param receiver the transcription
     * @param arguments null
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {
        if (receiver instanceof Transcription transcription) {
            new DocumentInfoDialog(ELANCommandFactory.getRootFrame(transcription), transcription);
        }
    }

    @Override
    public String getName() {
        return name;
    }

}
