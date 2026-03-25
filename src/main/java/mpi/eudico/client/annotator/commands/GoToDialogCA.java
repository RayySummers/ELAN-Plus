package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

/**
 * An action to produce an input dialog where a time position to jump to can be entered.
 */
@SuppressWarnings("serial")
public class GoToDialogCA extends CommandAction {
    /**
     * Creates a new GoToDialogCA instance
     *
     * @param viewerManager the viewer manager
     */
    public GoToDialogCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.GOTO_DLG);
    }

    /**
     * Creates a new {@code GoToDialogCommand}.
     */
    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.GOTO_DLG);
    }

    /**
     * @return {@code null}
     */
    @Override
    protected Object getReceiver() {
        return null;
    }

    /**
     * @return an array with one object, the time panel.
     */
    @Override
    protected Object[] getArguments() {
        Object[] args = new Object[1];
        args[0] = vm.getTimePanel();

        return args;
    }
}
