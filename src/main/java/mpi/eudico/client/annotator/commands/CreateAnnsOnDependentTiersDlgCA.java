package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.ViewerManager2;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Command action for creating the annotations on dependent tiers
 */
@SuppressWarnings("serial")
public class CreateAnnsOnDependentTiersDlgCA extends CommandAction {
    /**
     * Creates a new CreateEmptyAnnsOnDependentTiersDlgCA instance
     *
     * @param viewerManager the ViewerManager
     */
    public CreateAnnsOnDependentTiersDlgCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.ANN_ON_DEPENDENT_TIER);
    }

    /**
     * Creates a new <code>Command</code>.
     */
    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.ANN_ON_DEPENDENT_TIER_COM);
    }

    /**
     * Returns the viewerManager as the central object giving access to all necessary components.
     *
     * @return the ViewerManager
     */
    @Override
    protected Object getReceiver() {
        return vm.getTranscription();
    }

    /**
     * Check if there are tiers first.
     *
     * @param event the action event
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        if (vm.getTranscription().getTiers().size() < 1) {
            JOptionPane.showMessageDialog(ELANCommandFactory.getRootFrame(vm.getTranscription()),
                                          ElanLocale.getString("CreateAnnsOnDependentTiersDlg.Warning.NoTiers"),
                                          ElanLocale.getString("Message.Error"),
                                          JOptionPane.WARNING_MESSAGE);

            return;
        }

        super.actionPerformed(event);
    }
}
