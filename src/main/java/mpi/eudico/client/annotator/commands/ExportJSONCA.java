package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

/**
 * Opens a dialog window to export annotations to JSON
 *
 * @author Allan van Hulst
 */
public class ExportJSONCA extends CommandAction {

    /**
     * Constructor.
     *
     * @param theVM the viewer manager
     */
    public ExportJSONCA(ViewerManager2 theVM) {
        super(theVM, ELANCommandFactory.EXPORT_JSON);
    }

    /**
     * Creates a new command.
     */
    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.EXPORT_JSON);
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
