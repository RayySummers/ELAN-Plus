package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanLayoutManager;
import mpi.eudico.server.corpora.clom.Transcription;


/**
 * Switches between different modes.
 *
 * @author Aarthy Somasundaram
 */
public class ChangeModeCommand implements Command {
    private final String commandName;

    /**
     * Creates a new AnnotationModeCommand instance
     *
     * @param name the name of the command
     */
    public ChangeModeCommand(String name) {
        commandName = name;
    }

    /**
     * @param receiver the ElanLayoutManager
     * @param arguments arguments{0] = the mode as Integer, one of {@link Transcription#NORMAL},
     *     {@link Transcription#BULLDOZER} or {@link Transcription#SHIFT}
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {

        // receiver is master ElanMediaPlayerController
        // arguments[0] is current mode - integer

        ((ElanLayoutManager) receiver).changeMode((Integer) arguments[0]);
    }

    @Override
    public String getName() {
        return commandName;
    }
}
