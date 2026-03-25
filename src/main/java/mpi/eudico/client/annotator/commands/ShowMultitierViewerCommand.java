package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanLayoutManager;


/**
 * Tells the ViewerManager which Multitier Viewer should be visible.
 *
 * @author Han Sloetjes
 */
public class ShowMultitierViewerCommand implements Command {
    private final String commandName;

    /**
     * Creates a new ShowMultitierViewerCommand instance
     *
     * @param name the name of the command
     */
    public ShowMultitierViewerCommand(String name) {
        commandName = name;
    }

    /**
     * @param receiver the ViewerManager
     * @param arguments the arguments:  <ul><li>arg[0] = the multitier viewer to set visible (String)</li> </ul>
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {
        String com = (String) arguments[0];

        if (ELANCommandFactory.SHOW_TIMELINE.equals(com)) {
            ((ElanLayoutManager) receiver).showTimeLineViewer();
        } else if (ELANCommandFactory.SHOW_INTERLINEAR.equals(com)) {
            ((ElanLayoutManager) receiver).showInterlinearViewer();
        }
    }

    @Override
    public String getName() {
        return commandName;
    }
}
