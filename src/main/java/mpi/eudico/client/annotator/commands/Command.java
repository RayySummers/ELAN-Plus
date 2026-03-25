package mpi.eudico.client.annotator.commands;

/**
 * Defines a command which is used in combination with a {@link CommandAction} and which has as main method the
 * {@code execute} method.
 */
public interface Command {
    /**
     * The actual work of the command.
     *
     * @param receiver the receiver of the command, the object that is going to be changed or is the main executor of the
     *     changes
     * @param arguments an array of unknown size containing arguments or parameters that determine the actual effect of
     *     the command
     */
    void execute(Object receiver, Object[] arguments);

    /**
     * Returns the name of the command.
     *
     * @return the name of the command
     */
    String getName();
}
