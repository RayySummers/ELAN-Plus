package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanMediaPlayerController;
import mpi.eudico.client.annotator.ModePanel;
//import mpi.eudico.client.annotator.player.ElanMediaPlayer;


/**
 * A command to start or stop play back of an interval in a loop.
 */
public class LoopModeCommand implements Command {
    private final String commandName;
    private ElanMediaPlayerController mediaPlayerController;
    //private ElanMediaPlayer masterMediaPlayer;

    /**
     * Creates a new LoopModeCommand instance
     *
     * @param name the name of the command
     */
    public LoopModeCommand(String name) {
        commandName = name;
    }

    /**
     * @param receiver {@code null}
     * @param arguments only arg[0], the media player controller, is used
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {
        mediaPlayerController = (ElanMediaPlayerController) arguments[0];
        //masterMediaPlayer = (ElanMediaPlayer) arguments[1];

        // HS dec 2006: perform the same action, whether the player is playing or not
        updateLoopMode(!mediaPlayerController.getLoopMode());
        mediaPlayerController.doToggleLoopMode();
        /*
        if (!masterMediaPlayer.isPlaying()) {
            updateLoopMode(!mediaPlayerController.getLoopMode());
            mediaPlayerController.doToggleLoopMode();
        } else {
            //updateLoopMode(mediaPlayerController.getLoopMode()); // reset checkbox
            // HS dec 20006: do the same, it shouldn't matter whether the player is playing
            updateLoopMode(!mediaPlayerController.getLoopMode());
            mediaPlayerController.doToggleLoopMode();
        }
        */
    }

    private void updateLoopMode(boolean onOff) {
        ((ModePanel) mediaPlayerController.getModePanel()).updateLoopMode(onOff);
    }

    @Override
    public String getName() {
        return commandName;
    }
}
