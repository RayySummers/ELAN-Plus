package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanMediaPlayerController;
import mpi.eudico.client.annotator.Selection;
import mpi.eudico.client.annotator.player.ElanMediaPlayer;


/**
 * A Command to set the active boundary of the selection.
 *
 * @see ActiveSelectionBoundaryCA
 */
public class ActiveSelectionBoundaryCommand implements Command {
    private final String commandName;

    /**
     * Creates a new ActiveSelectionBoundaryCommand instance
     *
     * @param theName the name of the command
     */
    public ActiveSelectionBoundaryCommand(String theName) {
        commandName = theName;
    }

    /**
     * @param receiver the ElanMediaPlayerController
     * @param arguments <ul><li>[0] = ElanMediaPlayer</li>
     *     <li>[1] = the Selection</li>
     *     <li>[2] = the ActiveSelectionBoundaryCA</li></ul>
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {
        ElanMediaPlayerController mediaPlayerController = (ElanMediaPlayerController) receiver;
        ElanMediaPlayer player = (ElanMediaPlayer) arguments[0];
        Selection selection = (Selection) arguments[1];

        if (player == null) {
            return;
        }

        if (player.isPlaying()) {
            return;
        }

        long beginTime = selection.getBeginTime();
        long endTime = selection.getEndTime();

        if (beginTime == endTime) {
            return;
        }

        mediaPlayerController.toggleActiveSelectionBoundary();

        if (mediaPlayerController.isBeginBoundaryActive()) {
            player.setMediaTime(beginTime);
        } else {
            player.setMediaTime(endTime);
        }
    }

    @Override
    public String getName() {
        return commandName;
    }
}
