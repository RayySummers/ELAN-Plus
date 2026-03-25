package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

/**
 * A command action for moving the crosshair to the center of the selection.
 *
 * @author Aarthy Somasundaram
 * @version Dec 2010
 * @see ActiveSelectionCenterCommand
 */

@SuppressWarnings("serial")
public class ActiveSelectionCenterCA extends CommandAction {

    /**
     * Creates a new ActiveSelectionCenterCA instance
     *
     * @param theVM the viewer manager
     */
    public ActiveSelectionCenterCA(ViewerManager2 theVM) {
        super(theVM, ELANCommandFactory.SELECTION_CENTER);
    }

    /**
     * Creates a new command.
     */
    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.SELECTION_CENTER);
    }

    /**
     * @return the media player controller
     */
    @Override
    protected Object getReceiver() {
        return vm.getMediaPlayerController();
    }

    /**
     * @return an array containing the master media player and the selection
     */
    @Override
    protected Object[] getArguments() {
        Object[] args = new Object[2];
        args[0] = vm.getMasterMediaPlayer();
        args[1] = vm.getSelection();

        return args;
    }
}
