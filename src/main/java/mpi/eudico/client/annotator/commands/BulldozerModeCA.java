package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * Sets the time change propagation mode to bulldozer mode.
 */
@SuppressWarnings("serial")
public class BulldozerModeCA extends CommandAction {
    /**
     * Creates a new BulldozerModeCA instance
     *
     * @param theVM the viewer manager
     */
    public BulldozerModeCA(ViewerManager2 theVM) {
        super(theVM, ELANCommandFactory.BULLDOZER_MODE);
    }

    /**
     * Creates a new {@link BulldozerModeCommand}.
     */
    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.BULLDOZER_MODE);
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
