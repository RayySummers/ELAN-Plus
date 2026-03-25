package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * Action to change the name of a tier.
 */
@SuppressWarnings("serial")
public class SetTierNameCA extends CommandAction {
    /**
     * Creates a new SetTierNameCA instance
     *
     * @param theVM the viewer manager
     */
    public SetTierNameCA(ViewerManager2 theVM) {
        super(theVM, ELANCommandFactory.SET_TIER_NAME);

        //putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F6,
        // Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    }

    /**
     * Creates a new {@code SetTierNameCommand}.
     */
    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(), ELANCommandFactory.SET_TIER_NAME);
    }
}
