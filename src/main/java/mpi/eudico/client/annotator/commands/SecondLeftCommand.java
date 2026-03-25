package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.player.ElanMediaPlayer;


/**
 * A command to move the media playhead one second to the left or backward.
 */
public class SecondLeftCommand implements Command {
    private final String commandName;

    /**
     * Creates a new SecondLeftCommand instance
     *
     * @param theName the name of the command
     */
    public SecondLeftCommand(String theName) {
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
            ((ElanMediaPlayer) receiver).setMediaTime(((ElanMediaPlayer) receiver).getMediaTime() - 1000);
        }
    }

    @Override
    public String getName() {
        return commandName;
    }
}
