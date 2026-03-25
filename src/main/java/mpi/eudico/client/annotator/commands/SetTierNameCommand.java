package mpi.eudico.client.annotator.commands;

import mpi.eudico.server.corpora.clom.Tier;


/**
 * A command to change the name of a tier. Note implemented.
 */
public class SetTierNameCommand implements UndoableCommand {
    private final String commandName;

    // store state for undo and redo
    private Tier t;
    private String newTierName;
    private String oldTierName;

    /**
     * Creates a new SetTierNameCommand instance
     *
     * @param theName the name of the command
     */
    public SetTierNameCommand(String theName) {
        commandName = theName;
    }

    /**
     * DOCUMENT ME!
     *
     * @param receiver DOCUMENT ME!
     * @param arguments DOCUMENT ME!
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {
    }

    /**
     * DOCUMENT ME!
     */
    @Override
    public void undo() {
    }

    /**
     * DOCUMENT ME!
     */
    @Override
    public void redo() {
    }

    @Override
    public String getName() {
        return commandName;
    }
}
