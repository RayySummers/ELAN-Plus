package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

/**
 * An action to switch on and off the loop mode, a mode in which the media player repeatedly plays a selected time interval.
 */
@SuppressWarnings("serial")
public class LoopModeCA extends CommandAction {
    /**
     * Creates a new LoopModeCA instance
     *
     * @param theVM the viewer manager
     */
    public LoopModeCA(ViewerManager2 theVM) {
        super(theVM, ELANCommandFactory.LOOP_MODE);
    }

    /**
     * Creates a new {@code LoopModeCommand}.
     */
    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.LOOP_MODE);
    }

    /**
     * @return {@code null}
     */
    @Override
    protected Object getReceiver() {
        return null;
    }

    /**
     * @return an array of size 2, containing the media player controller and the leading media player
     */
    @Override
    protected Object[] getArguments() {
        Object[] args = new Object[2];
        args[0] = vm.getMediaPlayerController();
        args[1] = vm.getMasterMediaPlayer();

        return args;
    }
}
