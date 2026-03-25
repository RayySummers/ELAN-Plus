package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * A CommandAction to generate printout.
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class PageSetupCA extends CommandAction {

    /**
     * Creates a new PrintCA instance
     *
     * @param viewerManager the viewer manager
     */
    public PageSetupCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.PAGESETUP);
    }

    /**
     * Creates a new {@code PageSetupCommand}.
     */
    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.PAGESETUP);
    }

    /**
     * The receiver is the transcription.
     *
     * @return the transcription
     */
    @Override
    protected Object getReceiver() {
        return vm.getTranscription();
    }

    /**
     * @return {@code null}
     */
    @Override
    protected Object[] getArguments() {
        return null;
    }
}
