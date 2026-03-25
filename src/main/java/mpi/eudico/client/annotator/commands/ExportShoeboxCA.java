package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * A CommandAction that creates a Command for Shoebox style export.
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class ExportShoeboxCA extends CommandAction {
    /**
     * Creates a new ExportShoeboxCA instance
     *
     * @param viewerManager the viewer manager
     */
    public ExportShoeboxCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.EXPORT_SHOEBOX);
    }

    /**
     * Creates a new {@code ExportShoeboxCommand}.
     */
    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.EXPORT_SHOEBOX);
    }

    /**
     * There's no natural receiver for this CommandAction.
     *
     * @return {@code null}
     */
    @Override
    protected Object getReceiver() {
        return null;
    }

    /**
     * @return an array of size 1, containing the transcription
     */
    @Override
    protected Object[] getArguments() {
        return new Object[] {vm.getTranscription()};
    }
}
