package mpi.eudico.client.annotator.commands;

import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

import java.util.List;

/**
 * Deletes the tiers based on the participant attribute.
 */
public class DeleteParticipantCommand implements UndoableCommand {

    private final String commandName;
    private TranscriptionImpl transcription;
    private List<TierImpl> allMatchedTiers;
    DeleteTiersCommand deleteTiersCommand;


    /**
     * Constructor to instantiate the DeleteParticipantCommand
     *
     * @param name the name of the command
     */
    public DeleteParticipantCommand(String name) {
        this.commandName = name;
    }


    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the transcription
     * @param arguments the arguments:  <ul><li>arg[0] = list of tiers to be deleted (TierImpl)</li> </ul>
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {

        this.transcription = (TranscriptionImpl) receiver;
        this.allMatchedTiers = (List<TierImpl>) arguments[0];


        Object[] args = allMatchedTiers.toArray();
        deleteTiersCommand = new DeleteTiersCommand(ELANCommandFactory.DELETE_TIERS);
        deleteTiersCommand.execute(transcription, args);

    }

    @Override
    public String getName() {
        return commandName;
    }

    /**
     * Adds the removed tiers to the transcription.
     */
    @Override
    public void undo() {
        deleteTiersCommand.undo();

    }


    /**
     * Again removes the tiers from the transcription.
     */
    @Override
    public void redo() {
        deleteTiersCommand.redo();

    }

}
