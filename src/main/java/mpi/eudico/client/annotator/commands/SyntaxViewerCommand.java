package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.util.ClientLogger;

import java.util.logging.Level;


/**
 * A Command that creates a Syntax viewer.
 *
 * @author klasal
 */
public class SyntaxViewerCommand implements Command,
                                            ClientLogger {
    private static final String className = "mpi.syntax.elan.ElanSyntaxViewer";

    private final String commandName;

    /**
     * Checks if viewer is enabled or not
     *
     * @return true or false based on viewer
     */
    public static boolean isEnabled() {
        Object syntaxViewerClass = null;
        try {
            syntaxViewerClass = Class.forName(className);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Viewer does not seem to be enabled", e);
        }
        return (syntaxViewerClass != null);
    }

    /**
     * Creates a new SyntaxViewerCommand instance
     *
     * @param theName name of the command
     */
    public SyntaxViewerCommand(String theName) {
        commandName = theName;
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver null
     * @param arguments the arguments:  <ul><li>arg[0] = the Transcription object (Transcription)</li> <li>arg[1] = the
     *     ViewerManager (ViewerManager)</li> </ul>
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {
        ((ViewerManager2) arguments[1]).createViewer(className, 100);
    }

    @Override
    public String getName() {
        return commandName;
    }
}
