package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

/**
 * A command action which produces a dialog that allows to choose the participants. Based on the participant selection the
 * tiers which have those participants as their attribute/property will be deleted.
 */
@SuppressWarnings("serial")
public class DeleteParticipantCA extends CommandAction {

    /**
     * Creates a new DeleteParticipantCA instance
     *
     * @param viewerManager the viewer manager
     */
    public DeleteParticipantCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.DELETE_PARTICIPANT);
    }


    /**
     * Creates a new delete participant dialog command.
     */
    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.DELETE_PARTICIPANT_DLG);
    }

    /**
     * @return the viewer manager
     */
    @Override
    protected Object getReceiver() {
        return vm;
    }

}
