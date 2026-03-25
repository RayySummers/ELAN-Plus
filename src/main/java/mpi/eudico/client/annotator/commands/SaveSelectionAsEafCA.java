package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.SelectionListener;
import mpi.eudico.client.annotator.ViewerManager2;

/**
 * A command action that creates a command that saves annotations within the selected time interval as eaf..
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class SaveSelectionAsEafCA extends CommandAction implements SelectionListener {

    /**
     * Creates a new command action instance.
     *
     * @param viewerManager the viewer manager
     */
    public SaveSelectionAsEafCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.SAVE_SELECTION_AS_EAF);
        viewerManager.connectListener(this);
    }

    /**
     * Creates a new <code>SaveSelectionAsEafCommand</code>.
     */
    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.SAVE_SELECTION_AS_EAF);
    }

    /**
     * The receiver of this CommandAction is the Transcription object
     *
     * @return the receiver
     */
    @Override
    protected Object getReceiver() {
        return vm.getTranscription();
    }

    /**
     * Returns the arguments for the related Command.
     *
     * @return the arguments for the related Command
     */
    @Override
    protected Object[] getArguments() {
        Object[] args = new Object[2];
        args[0] = Long.valueOf(vm.getSelection().getBeginTime());
        args[1] = Long.valueOf(vm.getSelection().getEndTime());

        return args;
    }

    /**
     * Disable the action if there is no selection.
     *
     * @see mpi.eudico.client.annotator.SelectionListener#updateSelection()
     */
    @Override
    public void updateSelection() {
        setEnabled(vm.getSelection().getBeginTime() != vm.getSelection().getEndTime());
    }

}
