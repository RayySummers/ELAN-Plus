package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.player.ElanMediaPlayer;

/**
 * A command for a frame forward step of the media player.
 */
public class NextFrameCommand implements Command {
    private final String commandName;

    /**
     * Creates a new NextFrameCommand instance
     *
     * @param theName the name of the command
     */
    public NextFrameCommand(String theName) {
        commandName = theName;
    }

    /**
     * @param receiver the media player
     * @param arguments ignored
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {
        // receiver is master ElanMediaPlayer
        if (receiver != null) {
            // replaced by nextFrame.
            // getMillisecondsPerSample returns a long so there are rounding errors for ntsc
            // further some media platforms support direct frame stepping that can handle dropped frames
           /* long msPerFrame = ((ElanMediaPlayer) receiver).getMilliSecondsPerSample();
            long frame = ((ElanMediaPlayer) receiver).getMediaTime() / msPerFrame;
            ((ElanMediaPlayer) receiver).setMediaTime((frame + 1) * msPerFrame);*/

            ((ElanMediaPlayer) receiver).nextFrame();
        }
    }

    @Override
    public String getName() {
        return commandName;
    }
}
