package mpi.eudico.client.annotator.commands;

import mpi.eudico.util.CVEntry;
import mpi.eudico.util.ControlledVocabulary;


/**
 * A Command to add an entry to a Controlled Vocabulary.
 *
 * @author Han Sloetjes
 */
public class AddCVEntryCommand implements Command {
    private final String commandName;

    /**
     * Creates a new AddCVEntryCommand instance
     *
     * @param name the name of the command
     */
    public AddCVEntryCommand(String name) {
        commandName = name;
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.<br> When the CV is connected to a Transcription it will handle the notification of the change.
     *
     * @param receiver the Controlled Vocabulary
     * @param arguments the arguments: <ul> <li>arg[0] = the value of the new entry (String)</li> <li>arg[1] = the
     *     description of the  new entry</li> </ul>
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {

        ControlledVocabulary conVoc = (ControlledVocabulary) receiver;
        String value = (String) arguments[0];
        String desc = (String) arguments[1];
        CVEntry entry = new CVEntry(conVoc, value, desc);

        conVoc.addEntry(entry);
    }

    @Override
    public String getName() {
        return commandName;
    }
}
