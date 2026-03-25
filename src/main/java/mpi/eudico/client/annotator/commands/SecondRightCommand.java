package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.player.ElanMediaPlayer;

/**
 * A command to move the media playhead forward with one second.
 */
public class SecondRightCommand implements Command {
    private final String commandName;

    /**
     * Creates a new SecondRightCommand instance
     *
     * @param theName the name of the command
     */
    public SecondRightCommand(String theName) {
        commandName = theName;
    }

    /**
     * @param receiver the media player
     * @param arguments {@code null}
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {
        // receiver is master ElanMediaPlayer
        if (receiver != null) {
            ((ElanMediaPlayer) receiver).setMediaTime(((ElanMediaPlayer) receiver).getMediaTime() + 1000);
        }
    }

    @Override
    public String getName() {
        return commandName;
    }
}
