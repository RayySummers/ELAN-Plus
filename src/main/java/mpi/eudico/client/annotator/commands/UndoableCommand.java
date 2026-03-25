package mpi.eudico.client.annotator.commands;

/**
 * Defines commands that support {@code undo} and {@code redo}
 */
public interface UndoableCommand extends Command {
    /**
     * This method should undo any changes made by a call to the command's {@link #execute(Object, Object[])} method.
     */
    void undo();

    /**
     * This method should make the changes again which were made earlier by the {@link #execute(Object, Object[])} method. In
     * some cases this method can simply call {@code execute()} again.
     */
    void redo();
}
