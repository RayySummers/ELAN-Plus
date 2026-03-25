package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

/**
 * A CommandAction for creation of new annotations based on the user input begin and end time. It creates a new
 * {@link NewAnnotationFromBeginEndTimeDlgCommand} which creates a dialog for creation of new annotations from user input
 * begin time, end time and user selected tier/tiers.
 */
@SuppressWarnings("serial")
public class NewAnnotationFromBeginEndTimeDlgCA extends CommandAction {


    /**
     * Creates a NewAnnotationFromBeginEndTimeDlgCA
     *
     * @param viewerManager the viewer manager
     */
    public NewAnnotationFromBeginEndTimeDlgCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.NEW_ANNOTATION_FROM_BIGIN_END_TIME_DLG);
    }

    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                                                   ELANCommandFactory.NEW_ANNOTATION_FROM_BIGIN_END_TIME_DLG);

    }

    /**
     * @return the viewer manager
     */
    @Override
    protected Object getReceiver() {
        return vm;
    }


}
