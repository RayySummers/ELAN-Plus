package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

/**
 * A command action to show a dialog for the export as QuickTime Subtitles.
 *
 * @author Alexander Klassmann
 * @version Jul 2, 2004
 */
public class ExportQtSubCA extends CommandAction {
    /**
     * Creates a new ExportQtSubCA instance
     *
     * @param viewerManager the viewer manager
     */
    public ExportQtSubCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.EXPORT_QT_SUB);
    }

    /**
     * Creates a new {@code ExportQtSubCommand} command.
     */
    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.EXPORT_QT_SUB);
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
