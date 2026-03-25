package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * A command action that brings up a dialog for TIGER export.
 *
 * @author Han Sloetjes
 */
public class ExportTigerDlgCA extends CommandAction {
    /**
     * Creates a new ExportTigerDlgCA instance
     *
     * @param viewerManager the viewer manager
     */
    public ExportTigerDlgCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.EXPORT_TIGER);
    }

    /**
     * Creates a new {@code ExportTigerDlgCommand}.
     */
    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.EXPORT_TIGER);
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
