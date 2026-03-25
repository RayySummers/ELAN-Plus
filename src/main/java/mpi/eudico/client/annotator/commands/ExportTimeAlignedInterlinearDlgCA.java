package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * Show a dialog for export in a time-aligned interlinear gloss style.
 *
 * @author Steffen Zimmermann
 * @version 1.0
 */
@SuppressWarnings("serial")
public class ExportTimeAlignedInterlinearDlgCA extends CommandAction {
    /**
     * Creates a Command Action for the export of text in a time-aligned interlinear gloss style.
     *
     * @param theVM the ViewerManager
     */
    public ExportTimeAlignedInterlinearDlgCA(ViewerManager2 theVM) {
        super(theVM, ELANCommandFactory.EXPORT_TA_INTERLINEAR_GLOSS);
    }

    /**
     * Creates a new command.
     */
    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.EXPORT_TA_INTERLINEAR_GLOSS);
    }

    /**
     * There's no natural receiver for this CommandAction.
     *
     * @return null
     */
    @Override
    protected Object getReceiver() {
        return null;
    }

    /**
     * Returns the Transcription and the Selection object.
     *
     * @return an array containing the Transcription and the Selection
     */
    @Override
    protected Object[] getArguments() {
        return new Object[] {vm.getTranscription(), vm.getSelection()};
    }
}
