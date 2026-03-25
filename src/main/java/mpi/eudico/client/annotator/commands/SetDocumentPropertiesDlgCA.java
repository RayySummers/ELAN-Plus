package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

/**
 * An action to create a dialog to change the document properties
 *
 * @author Allan van Hulst
 */
@SuppressWarnings("serial")
public class SetDocumentPropertiesDlgCA extends CommandAction {

    /**
     * Constructor.
     *
     * @param theVM the viewer manager
     */
    public SetDocumentPropertiesDlgCA(ViewerManager2 theVM) {
        super(theVM, ELANCommandFactory.SET_DOCUMENT_PROPERTIES);
    }

    /**
     * Creates a new command.
     */
    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.SET_DOCUMENT_PROPERTIES_DLG);
    }

    /**
     * Returns the transcription.
     *
     * @see mpi.eudico.client.annotator.commands.CommandAction#getReceiver()
     */
    @Override
    protected Object getReceiver() {
        return vm.getTranscription();
    }


}
