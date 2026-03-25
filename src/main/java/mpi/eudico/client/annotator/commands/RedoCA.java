package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

import java.awt.event.ActionEvent;

/**
 * The {@code redo} action can be added to a menu, it interacts with the command history by activating the next action (step
 * forward).
 */
@SuppressWarnings("serial")
public class RedoCA extends CommandAction {
    private final CommandHistory commandHistory;

    /**
     * Creates a new RedoCA instance
     *
     * @param theVM the viewer manager
     * @param theHistory the {@code CommandHistory} instance to interact with
     */
    public RedoCA(ViewerManager2 theVM, CommandHistory theHistory) {
        super(theVM, ELANCommandFactory.REDO);
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
     * Calls {@link CommandHistory#redo()}, this moves the index pointer forward in the history queue.
     *
     * @param event the action event
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        commandHistory.redo();
    }
}
