package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

/**
 * Action to start the import of an annotation collection from an annotation server.
 */
@SuppressWarnings("serial")
public class ImportCollectionFromServerCA extends CommandAction {

    /**
     * Constructor.
     *
     * @param theVM the viewer manager
     */
    public ImportCollectionFromServerCA(ViewerManager2 theVM) {
        super(theVM, ELANCommandFactory.IMPORT_COLLECTION_FROM_SERVER);

    }

    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.IMPORT_COLLECTION_FROM_SERVER);

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
