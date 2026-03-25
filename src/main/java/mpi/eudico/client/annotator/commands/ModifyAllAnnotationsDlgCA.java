package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

/**
 * A command action for modifying all annotation's boundaries of selected tiers
 */
@SuppressWarnings("serial")
public class ModifyAllAnnotationsDlgCA extends CommandAction {

    /**
     * Constructor.
     *
     * @param viewerManager the ViewerManager
     */
    public ModifyAllAnnotationsDlgCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.MODIFY_ALL_ANNOTATION_BOUNDARIES);
    }

    /**
     * Creates a new {@code ModifyAllAnnotationsDlgCommand}
     */
    @Override
    protected void newCommand() {
        command =
            ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.MODIFY_ALL_ANNOTATION_BOUNDARIES);
    }

    /**
     * Returns the transcription
     *
     * @return the transcription
     */
    @Override
    protected Object getReceiver() {
        return vm.getTranscription();
    }

}
