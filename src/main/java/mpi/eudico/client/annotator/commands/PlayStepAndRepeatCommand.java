package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanMediaPlayerController;
import mpi.eudico.client.annotator.player.ElanMediaPlayer;

/**
 * A command to start or stop the step-and-repeat play back mode
 *
 * @author Han Sloetjes
 */
public class PlayStepAndRepeatCommand implements Command {
    private final String commandName;

    /**
     * Creates a new command instance.
     *
     * @param commandName the name of the command
     */
    public PlayStepAndRepeatCommand(String commandName) {
        super();
        this.commandName = commandName;
    }

    /**
     * @param receiver the master media player
     * @param arguments the media player controller object
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {
        ElanMediaPlayer player = (ElanMediaPlayer) receiver;
        ElanMediaPlayerController mediaPlayerController = (ElanMediaPlayerController) arguments[0];

        if (player == null) {
            return;
        }

        if (mediaPlayerController.isStepAndRepeatMode()) {
            mediaPlayerController.setStepAndRepeatMode(false);
        } else {
            if (player.isPlaying()) {
                player.stop();
            }
            if (mediaPlayerController.isPlaySelectionMode()) {
                mediaPlayerController.stopLoop();
                mediaPlayerController.setPlaySelectionMode(false);
                player.setStopTime(player.getMediaDuration());
            }
            // this starts the player in step and repeat mode
            mediaPlayerController.setStepAndRepeatMode(true);
        }
    }

    @Override
    public String getName() {
        return commandName;
    }

}
