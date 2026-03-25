package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanMediaPlayerController;
import mpi.eudico.client.annotator.ModePanel;
import mpi.eudico.client.annotator.player.ElanMediaPlayer;


/**
 * A command to switch selection mode on or off.
 */
public class SelectionModeCommand implements Command {
    private final String commandName;
    private ElanMediaPlayerController mediaPlayerController;
    private ElanMediaPlayer masterMediaPlayer;

    /**
     * Creates a new SelectionModeCommand instance
     *
     * @param name the name of the command
     */
    public SelectionModeCommand(String name) {
        commandName = name;
    }

    /**
     * @param receiver {@code null}
     * @param arguments <ul><li>arg[0] = ElanMEdiaPlayerController</li>
     *     <li>arg[1] = ElanMediaPlayer</li></ul>
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {
        mediaPlayerController = (ElanMediaPlayerController) arguments[0];
        masterMediaPlayer = (ElanMediaPlayer) arguments[1];

        if (!masterMediaPlayer.isPlaying()) {
            updateSelectionMode(!mediaPlayerController.getSelectionMode());
            mediaPlayerController.doToggleSelectionMode();

            if ((mediaPlayerController.getSelectionMode()) && (mediaPlayerController.getSelectionBeginTime()
                                                               == mediaPlayerController.getSelectionEndTime())) {
                mediaPlayerController.setSelection(mediaPlayerController.getMediaTime(),
                                                   mediaPlayerController.getMediaTime());
            }
        } else {
            updateSelectionMode(mediaPlayerController.getSelectionMode()); // reset checkbox
        }
    }

    private void updateSelectionMode(boolean onOff) {
        ((ModePanel) mediaPlayerController.getModePanel()).updateSelectionMode(onOff);
    }

    @Override
    public String getName() {
        return commandName;
    }
}
