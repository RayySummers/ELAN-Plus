package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.viewer.MultiTierControlPanel;

/**
 * A command to activate the next tier set and to go to the first one if the current set is the last in the list.
 */
public class CycleTierSetsCommand implements Command {
    private final String commandName;

    /**
     * Creates a new CycleTierSetsCommand instance
     *
     * @param name name of the command
     */
    public CycleTierSetsCommand(String name) {
        commandName = name;
    }

    @Override
    public void execute(Object receiver, Object[] arguments) {
        MultiTierControlPanel multiTierControlPanel = (MultiTierControlPanel) receiver;

        multiTierControlPanel.cycleTierSets();

    }

    @Override
    public String getName() {
        return commandName;
    }

}
