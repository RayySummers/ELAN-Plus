package mpi.eudico.client.annotator.commands;

import javax.swing.*;

/**
 * A command to show the tier dependency window.
 */
public class TierDependenciesCommand implements Command {
    private final String commandName;
    private JFrame dependencyFrame;

    /**
     * Creates a new TierDependenciesCommand instance
     *
     * @param name the name of the command
     */
    public TierDependenciesCommand(String name) {
        commandName = name;
    }

    /**
     * @param receiver {@code null}
     * @param arguments arg[0] = the frame to show
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {
        dependencyFrame = (JFrame) (arguments[0]);

        showTierDependencies();
    }

    @Override
    public String getName() {
        return commandName;
    }

    private void showTierDependencies() {
        if (dependencyFrame != null) {
            dependencyFrame.setVisible(true);
            dependencyFrame.toFront();
        }
    }
}
