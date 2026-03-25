package mpi.eudico.client.annotator.commands;

import mpi.eudico.server.corpora.clom.Transcription;

/**
 * A command that changes the time change propagation mode to Bulldozer Mode.
 */
public class BulldozerModeCommand implements Command {
    private final String commandName;

    /**
     * Creates a new BulldozerModeCommand instance
     *
     * @param name the name of the command
     */
    public BulldozerModeCommand(String name) {
        commandName = name;
    }

    /**
     * @param receiver the transcription
     * @param arguments {@code null}
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {
        ((Transcription) receiver).setTimeChangePropagationMode(Transcription.BULLDOZER);
    }

    @Override
    public String getName() {
        return commandName;
    }
}
