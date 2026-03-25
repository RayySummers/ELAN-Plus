package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

/**
 * A command action to create and show a dialog to configure lexicon services.
 */
@SuppressWarnings("serial")
public class EditLexSrvcDlgCA extends CommandAction {

    /**
     * Constructor to call the super constructor to pass the viewer manager
     *
     * @param theVM the viewer manager
     */
    public EditLexSrvcDlgCA(ViewerManager2 theVM) {
        super(theVM, ELANCommandFactory.EDIT_LEX_SRVC_DLG);
    }

    /**
     * Creates a new Command.
     */
    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.EDIT_LEX_SRVC_DLG);
    }

    /**
     * Returns the receiver of the command, the transcription.
     *
     * @return the transcription
     */
    @Override
    protected Object getReceiver() {
        return vm.getTranscription();
    }

    /**
     * There are no arguments for the command that creates a dialog.
     *
     * @return {@code null}
     */
    @Override
    protected Object[] getArguments() {
        return null;
    }
}
