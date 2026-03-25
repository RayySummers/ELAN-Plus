package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.Selection;
import mpi.eudico.client.annotator.player.ElanMediaPlayer;

/**
 * A command action for moving the crosshair to the center of the selection.
 *
 * @author Aarthy Somsundaram
 * @version Dec 2010
 * @see ActiveSelectionCenterCA
 */

public class ActiveSelectionCenterCommand implements Command {
    private final String commandName;

    /**
     * Creates a new ActiveSelectionCenterCommand instance
     *
     * @param theName the name of the command
     */
    public ActiveSelectionCenterCommand(String theName) {
        commandName = theName;
    }

    /**
     * @param receiver the ElanMediaPlayerController
     * @param arguments <ul><li>[0] = ELanMediaPlayer</li>
     *     <li>[1] = the Selection</li></ul>
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {

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

        player.setMediaTime((beginTime + endTime) / 2);

    }

    @Override
    public String getName() {
        return commandName;
    }
}
