package mpi.eudico.client.annotator.commands;

/**
 * A command to duplicate an annotation to the active tier and to then delete the original annotation.
 *
 * @author Han Sloetjes
 */
public class DuplicateRemoveAnnotationCommand extends DuplicateAnnotationCommand {

    /**
     * Creates a new command instance.
     *
     * @param name command name
     */
    public DuplicateRemoveAnnotationCommand(String name) {
        super(name);
    }

    /**
     *
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {
        super.execute(receiver, arguments);
    }

    /**
     *
     */
    @Override
    public void redo() {
        super.redo();
    }

    /**
     *
     */
    @Override
    public void undo() {
        super.undo();
    }


}
