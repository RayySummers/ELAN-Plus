package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.TimePanel;

/**
 * Creates a dialog for entering the media time to jump to.
 */
public class GoToDialogCommand implements Command {
    private final String commandName;
    private TimePanel timepanel;

    /**
     * Creates a new GoToDialogCommand instance
     *
     * @param name the name of the command
     */
    public GoToDialogCommand(String name) {
        commandName = name;
    }

    /**
     * @param receiver {@code null}
     * @param arguments args[0] = the {@code TimePanel} which will show the input dialog and will handle the entered
     *     media time
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {
        timepanel = (TimePanel) (arguments[0]);
        timepanel.showCrosshairTimeInputBox();
    }

    @Override
    public String getName() {
        return commandName;
    }
}
