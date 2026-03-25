package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.tier.AnnotationsToTiersDlg;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * Command that creates the dialog for configuring the annotation to tier conversion.
 *
 * @author Han Sloetjes
 */
public class AnnotationValuesToTiersDlgCommand implements Command {
    private final String name;

    /**
     * Constructor
     *
     * @param name the command name
     */
    public AnnotationValuesToTiersDlgCommand(String name) {
        this.name = name;
    }

    /**
     * Creates the dialog.
     *
     * @param receiver the transcription
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {
        TranscriptionImpl trans = (TranscriptionImpl) receiver;
        AnnotationsToTiersDlg attDialog = new AnnotationsToTiersDlg(ELANCommandFactory.getRootFrame(trans), trans);
        attDialog.setVisible(true);
    }

    @Override
    public String getName() {
        return name;
    }

}
