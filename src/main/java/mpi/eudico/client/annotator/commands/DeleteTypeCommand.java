package mpi.eudico.client.annotator.commands;

import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;


/**
 * Deletes a Linguistic Type from a Transcription.
 *
 * @author Han Sloetjes
 */
public class DeleteTypeCommand implements UndoableCommand {
    private final String commandName;

    // receiver
    private Transcription transcription;

    // the LinguisticType to remove
    private LinguisticType linType;

    /**
     * Creates a new DeleteTypeCommand instance
     *
     * @param name the name of the command
     */
    public DeleteTypeCommand(String name) {
        commandName = name;
    }

    /**
     * Adds the type again
     */
    @Override
    public void undo() {
        if (linType != null) {
            transcription.addLinguisticType(linType);
        }
    }

    /**
     * Adds the type again
     */
    @Override
    public void redo() {
        if (linType != null) {
            transcription.removeLinguisticType(linType);
        }
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the Transcription
     * @param arguments the arguments: <ul><li>arg[0] = the Linguistic Type (LinguisticType)</li> </ul>
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {
        transcription = (Transcription) receiver;
        linType = (LinguisticType) arguments[0];

        transcription.removeLinguisticType(linType);
    }

    @Override
    public String getName() {
        return commandName;
    }
}
