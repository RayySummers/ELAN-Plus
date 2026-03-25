package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.TimeScale;
import mpi.eudico.client.annotator.player.ElanMediaPlayer;


/**
 * Command to move the media playhead backward with the number of seconds corresponding to the scroll view of the main
 * timeline view.
 */
public class PreviousScrollViewCommand implements Command {
    private final String commandName;

    /**
     * Creates a new PreviousScrollViewCommand instance
     *
     * @param theName the name of the command
     */
    public PreviousScrollViewCommand(String theName) {
        commandName = theName;
    }

    /**
     * @param receiver the media player
     * @param arguments args[0] = the {@link TimeScale}
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {
        // receiver is master ElanMediaPlayer
        // arguments[0] is TimeScale
        TimeScale ts = (TimeScale) arguments[0];

        if (receiver != null) {
            long duration = ts.getIntervalDuration();

            long newBegin = ts.getBeginTime() - duration;

            if (newBegin < 0) {
                newBegin = 0L;
            }

            ts.setBeginTime(newBegin);
            ts.setEndTime(ts.getBeginTime() + duration);

            if ((((ElanMediaPlayer) receiver).getMediaTime() - ts.getIntervalDuration()) < 0) {
                ((ElanMediaPlayer) receiver).setMediaTime(0L);
            } else {
                ((ElanMediaPlayer) receiver).setMediaTime(((ElanMediaPlayer) receiver).getMediaTime()
                                                          - ts.getIntervalDuration());
            }
        }
    }

    @Override
    public String getName() {
        return commandName;
    }
}
