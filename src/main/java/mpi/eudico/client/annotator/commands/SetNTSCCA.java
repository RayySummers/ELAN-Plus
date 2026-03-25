package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * CommandAction to manually set the video standard (of the media file) to NTSC. This only influences the number of
 * milliseconds per frame for Elan.<br> NTSC is interlaced and has 59.94 / 2 frames per second, resulting in 1000 / 29.97 =
 * 33.3667 milliseconds, rounded to 33 ms per frame. See <a
 * href="http://archive.ncsa.uiuc.edu/SCMS/training/general/details/ntsc.html">NCSA web site</a>.
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class SetNTSCCA extends CommandAction {
    // the number of ms per frame
    private final Object[] args = new Object[] {Double.valueOf((1.001d / 30) * 1000)};

    /**
     * Creates a new SetNTSCCA instance
     *
     * @param viewerManager the viewer manager
     */
    public SetNTSCCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.SET_NTSC);
    }

    /**
     * Creates a new {@code SetMsPerFrameCommand}
     */
    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.SET_NTSC);
    }

    /**
     * The receiver of this CommandAction is an ElanMediaPlayer.
     *
     * @return the media player
     */
    @Override
    protected Object getReceiver() {
        return vm.getMasterMediaPlayer();
    }

    /**
     * @return an array of size 1, containing the rounded value 33
     */
    @Override
    protected Object[] getArguments() {
        return args;
    }
}
