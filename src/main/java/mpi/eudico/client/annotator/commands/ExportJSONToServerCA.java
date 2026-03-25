package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

/**
 * Opens a dialog window to load the tiers/annotations to annotation server
 */
@SuppressWarnings("serial")
public class ExportJSONToServerCA extends CommandAction {

    /**
     * Constructor.
     *
     * @param theVM the viewer manager
     */
    public ExportJSONToServerCA(ViewerManager2 theVM) {
        super(theVM, ELANCommandFactory.EXPORT_JSON_TO_SERVER);
    }

    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.EXPORT_JSON_TO_SERVER);

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
