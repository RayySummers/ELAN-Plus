package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.export.CHATExportDlg;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clom.TranscriptionStore;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A command tor the export to CHAT format
 *
 * @author Hennie Brugman
 */
public class ExportCHATCommand implements Command {
    private final String commandName;

    /**
     * Creates a new StoreCommand instance
     *
     * @param name the name of the command
     */
    public ExportCHATCommand(String name) {
        commandName = name;
    }

    /**
     * @param receiver the transcription
     * @param arguments <ul><li>arg[0] = TranscriptionStore</li>
     *     <li>arg[1] = list of visible tiers</li></ul>
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {
        //arguments:
        //[0]: TranscriptionStore eafTranscriptionStore
        //[1]: List<TierImpl> visibleTiers
        Transcription tr = (Transcription) receiver;
        TranscriptionStore eafTranscriptionStore = (TranscriptionStore) arguments[0];

        List<TierImpl> visibleTiers;
        if (arguments[1] != null) {
            visibleTiers = (List<TierImpl>) arguments[1];
        } else {
            visibleTiers = new ArrayList<TierImpl>(0);
        }

        JFrame fr = ELANCommandFactory.getRootFrame(tr);

        JOptionPane.showMessageDialog(fr,
                                      ElanLocale.getString("ExportCHATDialog.Message.CLANutility"),
                                      "ELAN",
                                      JOptionPane.INFORMATION_MESSAGE);

        JDialog dlg = new CHATExportDlg(fr, true, tr, eafTranscriptionStore, visibleTiers);
        dlg.setVisible(true);

    }

    @Override
    public String getName() {
        return commandName;
    }
}
