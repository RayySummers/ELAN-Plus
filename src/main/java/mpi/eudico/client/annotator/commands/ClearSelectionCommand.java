package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanMediaPlayerController;
import mpi.eudico.client.annotator.Selection;


/**
 * A command that clears the selection. One of the few commands that implement {@link UndoableCommand} but don't change the
 * document, the transcription.
 */
public class ClearSelectionCommand implements UndoableCommand {
    private final String commandName;

    // store state
    private long oldBegin;
    private long oldEnd;
    private Selection selection;
    private ElanMediaPlayerController mediaPlayerController;

    /**
     * Creates a new ClearSelectionCommand instance
     *
     * @param theName the name of the command
     */
    public ClearSelectionCommand(String theName) {
        commandName = theName;
    }

    /**
     * @param receiver the {@code Selection}
     * @param arguments args[0]= media player controller
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {
        // receiver is Selection
        // arguments[0] is mediaPlayerController
        selection = (Selection) receiver;
        mediaPlayerController = (ElanMediaPlayerController) (arguments[0]);

        oldBegin = selection.getBeginTime();
        oldEnd = selection.getEndTime();

        if (mediaPlayerController.getSelectionMode()) {
            selection.setSelection(mediaPlayerController.getMediaTime(), mediaPlayerController.getMediaTime());
        } else {
            selection.setSelection(0, 0);
        }
    }

    @Override
    public String getName() {
        return commandName;
    }

    /**
     * Restores the selection as it was
     */
    @Override
    public void undo() {
        if (selection != null) {
            selection.setSelection(oldBegin, oldEnd);
        }
    }

    /**
     * Clears the selection again
     */
    @Override
    public void redo() {
        if (selection != null) {
            selection.setSelection(0, 0);
        }
    }
}
