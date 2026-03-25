package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

/**
 * A command action which produces a dialog that allows to add dependent tiers to the tier structure based on the participant
 * and prefix/suffix value. The new dependent tiers will be added to the matched tiers (names)based on the formation of
 * participant and prefix/suffix value.
 */
@SuppressWarnings("serial")
public class AddDependentTiersToTierStructureCA extends CommandAction {

    /**
     * Creates a new AddDependentTiersToTierStructureCA instance
     *
     * @param viewerManager the viewerManager
     */
    public AddDependentTiersToTierStructureCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.ADD_DEPENDENT_TIERS_TO_TIER_STRUCTURE);
    }


    /**
     * Creates a new add dependent tiers to tier structure dialog command.
     */
    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                                                   ELANCommandFactory.ADD_DEPENDENT_TIERS_TO_TIER_STRUCTURE);
    }


    /**
     * @return the viewer manager
     */
    @Override
    protected Object getReceiver() {
        return vm;
    }

}
