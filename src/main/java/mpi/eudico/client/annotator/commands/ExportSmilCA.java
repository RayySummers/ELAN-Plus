package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

/**
 * A command action to show a dialog for the export as SMIL.
 *
 * @author Alexander Klassmann
 * @version Apr 15, 2004
 */
@SuppressWarnings("serial")
public class ExportSmilCA extends CommandAction {
    /**
     * Creates a new ExportSmilCA instance
     *
     * @param viewerManager the viewer manager
     */
    public ExportSmilCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.EXPORT_SMIL_RT);
    }

    /**
     * Creates a new {@code ExportSmilCommand}.
     */
    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.EXPORT_SMIL_RT);
    }

    /**
     * There's no logical receiver for this CommandAction.
     *
     * @return {@code null}
     */
    @Override
    protected Object getReceiver() {
        return null;
    }

    /**
     * @return an array of size 2, the transcription and the selection
     */
    @Override
    protected Object[] getArguments() {
        return new Object[] {vm.getTranscription(), vm.getSelection()};
    }

}
