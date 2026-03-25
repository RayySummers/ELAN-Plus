package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanMediaPlayerController;
import mpi.eudico.client.annotator.player.ElanMediaPlayer;


/**
 * A command to start or pause the media player.
 */
public class PlayPauseCommand implements Command {
    private final String commandName;
    private ElanMediaPlayer player;
    private ElanMediaPlayerController mediaPlayerController;

    /**
     * Creates a new PlayPauseCommand instance
     *
     * @param theName the name of the command
     */
    public PlayPauseCommand(String theName) {
        commandName = theName;
    }

    /**
     * @param receiver the media player
     * @param arguments args[0] = the media player controller
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {
        // receiver is master ElanMediaPlayer
        // arguments[0] is ElanMediaPlayerController
        player = (ElanMediaPlayer) receiver;
        mediaPlayerController = (ElanMediaPlayerController) arguments[0];

        if (player == null) {
            return;
        }

        boolean playSel = mediaPlayerController.isPlaySelectionMode();
        mediaPlayerController.setPlaySelectionMode(false);
        mediaPlayerController.stopLoop();

        if (player.isPlaying()) {
            player.stop();

            if (playSel) {
                player.setStopTime(player.getMediaDuration());
            }
        } else {
            player.start();
        }
    }

    @Override
    public String getName() {
        return commandName;
    }
}
