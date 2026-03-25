package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.player.ElanMediaPlayer;


/**
 * A command to move the media player's playhead to the end of the media.
 */
public class GoToEndCommand implements Command {
    private final String commandName;
    private ElanMediaPlayer player;

    /**
     * Creates a new GoToEndCommand instance
     *
     * @param theName the name of the command
     */
    public GoToEndCommand(String theName) {
        commandName = theName;
    }

    /**
     * @param receiver the receiving media player
     * @param arguments ignored
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {
        // receiver is master ElanMediaPlayer
        // arguments[0] is
        player = (ElanMediaPlayer) receiver;

        if (player == null) {
            return;
        }

        player.setMediaTime(player.getMediaDuration());
    }

    @Override
    public String getName() {
        return commandName;
    }
}
