package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

/**
 * Command action class to list the collection id's from the server
 */
@SuppressWarnings("serial")
public class ListCollectionIDSCA extends CommandAction {


    /**
     * Constructor.
     *
     * @param theVM the viewer manager
     */
    public ListCollectionIDSCA(ViewerManager2 theVM) {
        super(theVM, ELANCommandFactory.LIST_IDS_FROM_SERVER);

    }


    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.LIST_IDS_FROM_SERVER);
    }


    /**
     * Returns the Transcription and the Selection object.
     *
     * @return an array containing the Transcription and the Selection
     */
    @Override
    protected Object[] getArguments() {
        return new Object[] {vm.getTranscription()};
    }

}
