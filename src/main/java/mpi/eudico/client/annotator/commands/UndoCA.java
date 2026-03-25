package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

import java.awt.event.ActionEvent;

/**
 * The {@code undo} action can be added to a menu, it interacts with the command history by activating the previous action
 * (step backward).
 */
@SuppressWarnings("serial")
public class UndoCA extends CommandAction {
    private final CommandHistory commandHistory;

    /**
     * Creates a new UndoCA instance
     *
     * @param theVM the viewer manager
     * @param theHistory the {@code CommandHistory} instance
     */
    public UndoCA(ViewerManager2 theVM, CommandHistory theHistory) {
        super(theVM, ELANCommandFactory.UNDO);
        commandHistory = theHistory;

        setEnabled(false); // initially disable
    }

    /**
     * Stub, does nothing.
     */
    @Override
    protected void newCommand() {
    }

    /**
     * Calls {@link CommandHistory#undo()}, this moves the index pointer backward in the history queue.
     *
     * @param event the action event
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        commandHistory.undo();
    }
}
