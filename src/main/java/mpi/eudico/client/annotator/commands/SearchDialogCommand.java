package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.search.viewer.ElanSearchFrame;


/**
 * A command for creating a search dialog.
 *
 * @author Han Sloetjes
 */
public class SearchDialogCommand implements Command {
    private final String commandName;

    /**
     * Creates a new SearchDialogCommand instance
     *
     * @param name command name
     */
    public SearchDialogCommand(String name) {
        commandName = name;
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver {@code null}, there is no clear receiver object for this command
     * @param arguments the arguments:  <ul><li>arg[0] = the ViewerManager for this document/frame (ViewerManager)</li>
     *     </ul>
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {
        ViewerManager2 vm = (ViewerManager2) arguments[0];
        new ElanSearchFrame(vm).setVisible(true);
    }

    @Override
    public String getName() {
        return commandName;
    }
}
