package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

/**
 * An action to view the document properties
 *
 * @author Allan van Hulst
 */
@SuppressWarnings("serial")
public class DocumentInfoCA extends CommandAction {

    /**
     * Constructor.
     *
     * @param theVM the viewer manager
     */
    public DocumentInfoCA(ViewerManager2 theVM) {
        super(theVM, ELANCommandFactory.DOCUMENT_INFO);
    }

    /**
     * Creates a new command.
     */
    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.DOCUMENT_INFO);
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
