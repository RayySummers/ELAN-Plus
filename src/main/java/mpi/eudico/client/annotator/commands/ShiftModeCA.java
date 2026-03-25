package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * A command action to switch to time change propagation mode {@code Shift}.
 */
@SuppressWarnings("serial")
public class ShiftModeCA extends CommandAction {
    /**
     * Creates a new ShiftModeCA instance
     *
     * @param theVM the viewer manager
     */
    public ShiftModeCA(ViewerManager2 theVM) {
        super(theVM, ELANCommandFactory.SHIFT_MODE);
    }

    /**
     * Creates a new {@code ShiftModeCommand}.
     */
    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.SHIFT_MODE);
    }

    /**
     * @return the transcription
     */
    @Override
    protected Object getReceiver() {
        return vm.getTranscription();
    }

    /**
     * Returns null, no arguments need to be passed.
     *
     * @return {@code null}
     */
    @Override
    protected Object[] getArguments() {
        return null;
    }
}
