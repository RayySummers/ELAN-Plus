package mpi.eudico.client.annotator.commands;

import mpi.eudico.server.corpora.clom.Transcription;

/**
 * A command to change to {@code Shift} mode.
 */
public class ShiftModeCommand implements Command {
    private final String commandName;

    /**
     * Creates a new ShiftModeCommand instance
     *
     * @param name the name of the command
     */
    public ShiftModeCommand(String name) {
        commandName = name;
    }

    /**
     * @param receiver the transcription
     * @param arguments {@code null}
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {
        ((Transcription) receiver).setTimeChangePropagationMode(Transcription.SHIFT);
    }

    @Override
    public String getName() {
        return commandName;
    }
}
