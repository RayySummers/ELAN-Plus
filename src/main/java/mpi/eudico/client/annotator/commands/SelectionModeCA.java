package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

/**
 * A command action to switch selection mode on or off. In selection mode changes in the position of the media player
 * playhead, change a boundary of the selection (and therefore its extent).
 */
@SuppressWarnings("serial")
public class SelectionModeCA extends CommandAction {
    /**
     * Creates a new SelectionModeCA instance
     *
     * @param theVM the viewer manager
     */
    public SelectionModeCA(ViewerManager2 theVM) {
        super(theVM, ELANCommandFactory.SELECTION_MODE);
    }

    /**
     * Creates a new {@code SelectionModeCommand}.
     */
    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.SELECTION_MODE);
    }

    /**
     * @return {@code null}
     */
    @Override
    protected Object getReceiver() {
        return null;
    }

    /**
     * @return an array of size 2, containing the media player controller and the media player
     */
    @Override
    protected Object[] getArguments() {
        Object[] args = new Object[2];
        args[0] = vm.getMediaPlayerController();
        args[1] = vm.getMasterMediaPlayer();

        return args;
    }
}
