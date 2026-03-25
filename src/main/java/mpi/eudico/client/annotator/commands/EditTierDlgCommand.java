package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.EditTierDialog2;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;

import javax.swing.*;


/**
 * Creates a JDialog for defining, changing or deleting a Tier.
 *
 * @author Han Sloetjes
 */
public class EditTierDlgCommand implements Command {
    private final String commandName;

    /**
     * Creates a new EditTierDlgCommand instance
     *
     * @param name the name of the command
     */
    public EditTierDlgCommand(String name) {
        commandName = name;
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the Transcription
     * @param arguments the arguments:  <ul><li>arg[0] = the edit mode, ADD, CHANGE or DELETE (Integer)</li> <li>arg[1] =
     *     the tier to initialise the dialog with (TierImpl)</li> </ul>
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {
        Transcription transcription = (Transcription) receiver;
        JFrame frame = ELANCommandFactory.getRootFrame(transcription);
        Integer mode = (Integer) arguments[0];
        TierImpl tier = null;

        if (arguments[1] instanceof TierImpl) {
            tier = (TierImpl) arguments[1];
        }

        if (mode == EditTierDialog2.ADD) {
            // don't show the add tier dialog when no linguistic types have been defined
            if (transcription.getLinguisticTypes().isEmpty()) {
                String buf = ElanLocale.getString("EditTierDialog.Message.NoTypes") + "\n" + ElanLocale.getString(
                    "EditTierDialog.Message.CreateType");
                JOptionPane.showMessageDialog(frame, buf, ElanLocale.getString("Message.Error"), JOptionPane.ERROR_MESSAGE);

                return;
            }
        }

        new EditTierDialog2(frame, true, transcription, mode, tier).setVisible(true);
    }

    @Override
    public String getName() {
        return commandName;
    }
}
