package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.TimeScale;
import mpi.eudico.client.annotator.player.ElanMediaPlayer;


/**
 * a command to move the media playhead forward with the amount of the main scroll view.
 */
public class NextScrollViewCommand implements Command {
    private final String commandName;

    /**
     * Creates a new NextScrollViewCommand instance
     *
     * @param theName the name of the command
     */
    public NextScrollViewCommand(String theName) {
        commandName = theName;
    }

    /**
     * @param receiver the ElanMediaPlayer
     * @param arguments args[0] = the TimeScale instance
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {
        // receiver is master ElanMediaPlayer
        // arguments[0] is TimeScale
        TimeScale ts = (TimeScale) arguments[0];

        if (receiver != null) {
            long duration = ts.getIntervalDuration();
            ts.setBeginTime(ts.getBeginTime() + duration);
            ts.setEndTime(ts.getBeginTime() + duration);

            if ((((ElanMediaPlayer) receiver).getMediaTime() + duration) > ((ElanMediaPlayer) receiver).getMediaDuration()) {
                ((ElanMediaPlayer) receiver).setMediaTime(((ElanMediaPlayer) receiver).getMediaDuration());
            } else {
                ((ElanMediaPlayer) receiver).setMediaTime(((ElanMediaPlayer) receiver).getMediaTime()
                                                          + ts.getIntervalDuration());
            }
        }
    }

    @Override
    public String getName() {
        return commandName;
    }
}
