package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.player.ElanMediaPlayer;


/**
 * A command to move the crosshair or media playhead to the beginning of the file.
 */
public class GoToBeginCommand implements Command {
    private final String commandName;
    private ElanMediaPlayer player;

    /**
     * Creates a new GoToBeginCommand instance
     *
     * @param theName the name of the command
     */
    public GoToBeginCommand(String theName) {
        commandName = theName;
    }

    /**
     * @param receiver the media player
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

        player.setMediaTime(0);
    }

    @Override
    public String getName() {
        return commandName;
    }
}
