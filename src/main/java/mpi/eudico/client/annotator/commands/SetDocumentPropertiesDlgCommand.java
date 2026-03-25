package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.gui.DocumentPropertiesDialog;
import mpi.eudico.server.corpora.clom.Transcription;

/**
 * A command to display a dialog to change some document properties
 *
 * @author Allan van Hulst
 */
public class SetDocumentPropertiesDlgCommand implements Command {
    private final String name;

    /**
     * Constructor.
     *
     * @param name the name
     */
    public SetDocumentPropertiesDlgCommand(String name) {
        super();
        this.name = name;
    }

    /**
     * @param receiver the transcription
     * @param arguments ignored
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {
        Transcription transcription = null;

        if (receiver instanceof Transcription) {
            transcription = (Transcription) receiver;
        }

        if (transcription == null) {
            return;
        }

        new DocumentPropertiesDialog(ELANCommandFactory.getRootFrame(transcription), transcription);
    }

    @Override
    public String getName() {
        return name;
    }

}
