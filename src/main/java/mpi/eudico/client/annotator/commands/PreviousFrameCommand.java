package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.player.ElanMediaPlayer;


/**
 * A command to move the media position one frame backward.
 */
public class PreviousFrameCommand implements Command {
    private final String commandName;

    /**
     * Creates a new PreviousFrameCommand instance
     *
     * @param theName the viewer manager
     */
    public PreviousFrameCommand(String theName) {
        commandName = theName;
    }

    /**
     * @param receiver the media player
     * @param arguments {@code null}
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {
        // receiver is master ElanMediaPlayer
        if (receiver != null) {
            // replaced by nextFrame.
            // getMillisecondsPerSample returns a long so there are rounding errors for ntsc
            // further some media platforms support direct frame stepping that can handle dropped frames
            /*long msPerFrame = ((ElanMediaPlayer) receiver).getMilliSecondsPerSample();
            long frame = ((ElanMediaPlayer) receiver).getMediaTime() / msPerFrame;

            if (frame > 0) {
                ((ElanMediaPlayer) receiver).setMediaTime((frame - 1) * msPerFrame);
            }*/
            ((ElanMediaPlayer) receiver).previousFrame();
        }
    }

    @Override
    public String getName() {
        return commandName;
    }
}
