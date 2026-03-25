package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.TimeScale;
import mpi.eudico.client.annotator.player.ElanMediaPlayer;


/**
 * A command to move the media playhead one pixel to the right.
 */
public class PixelRightCommand implements Command {
    private final String commandName;

    /**
     * Creates a new PixelRightCommand instance
     *
     * @param theName the name of the command
     */
    public PixelRightCommand(String theName) {
        commandName = theName;
    }

    /**
     * @param receiver the media player
     * @param arguments args[0] = the {@code TimeScale}
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {
        // receiver is master ElanMediaPlayer
        // arguments[0] is TimeScale
        TimeScale ts = (TimeScale) arguments[0];

        if (receiver != null) {
            ((ElanMediaPlayer) receiver).setMediaTime(((ElanMediaPlayer) receiver).getMediaTime()
                                                      + (int) ts.getMsPerPixel());
        }
    }

    @Override
    public String getName() {
        return commandName;
    }
}
