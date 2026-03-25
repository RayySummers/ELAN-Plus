package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * A command action that brings up a dialog for tab delimited export.
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class ExportTeXDlgCA extends CommandAction {
    /**
     * Creates a new ExportTabDelDlgCA instance
     *
     * @param viewerManager the viewer manager
     */
    public ExportTeXDlgCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.EXPORT_TEX);
    }

    /**
     * Creates a new {@code ExportTeXDlgCommand}.
     */
    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.EXPORT_TEX);
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
     * @return an array of size 2, containing the transcription and the selection
     */
    @Override
    protected Object[] getArguments() {
        return new Object[] {vm.getTranscription(), vm.getSelection()};
    }
}
