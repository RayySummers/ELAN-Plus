package mpi.eudico.client.annotator.commands;

import mpi.eudico.server.corpora.clom.Transcription;

/**
 * A command that sets the time change propagation mode to NORMAL.
 */
public class NormalTimePropCommand implements Command {
    private final String commandName;

    /**
     * Creates a new NormalTimePropCommand instance
     *
     * @param name the name of the command
     */
    public NormalTimePropCommand(String name) {
        commandName = name;
    }

    /**
     * @param receiver the transcription
     * @param arguments null
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {
        ((Transcription) receiver).setTimeChangePropagationMode(Transcription.NORMAL);
    }

    @Override
    public String getName() {
        return commandName;
    }
}
