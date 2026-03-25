package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

/**
 * Sets the time change propagation mode to "normal" ({@code Transcription#NORMAL}
 */
@SuppressWarnings("serial")
public class NormalTimePropCA extends CommandAction {
    /**
     * Creates a new NormalTimePropCA instance
     *
     * @param theVM the viewer manager
     */
    public NormalTimePropCA(ViewerManager2 theVM) {
        super(theVM, ELANCommandFactory.TIMEPROP_NORMAL);
    }

    /**
     * Creates a new {@code NormalTimePropCommand}.
     */
    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.TIMEPROP_NORMAL);
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
