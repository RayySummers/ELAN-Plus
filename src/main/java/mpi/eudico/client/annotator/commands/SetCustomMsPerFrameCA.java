package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

/**
 * A command action to set the duration in milliseconds per frame/sample of a media player to a custom value specified by the
 * user.
 */
@SuppressWarnings("serial")
public class SetCustomMsPerFrameCA extends CommandAction {

    /**
     * Constructor.
     *
     * @param viewerManager the viewer manager
     */
    public SetCustomMsPerFrameCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.SET_CUSTOM_MS_PER_FRAME);

    }

    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.SET_CUSTOM_MS_PER_FRAME);
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
     * @return an array of size 1 containing the transcription, so that the command can show an option pane relative to its
     *     window
     */
    @Override
    protected Object[] getArguments() {
        return new Object[] {vm.getTranscription()};
    }
}
