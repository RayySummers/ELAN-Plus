package mpi.eudico.client.annotator.viewer;

import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;

import java.util.List;

/**
 * Defines methods for viewers that can show more than one Tier.
 */
public interface MultiTierViewer {
    /**
     * Sets the tiers that should be visible (not hidden) in the viewer.
     *
     * @param tiers the list of tiers that should be visible 
     */
    public void setVisibleTiers(List<TierImpl> tiers);

    /**
     * Sets the active tier. The viewer can mark the tier in some way, paint it
     * differently from other tiers.
     *
     * @param tier the active tier
     */
    public void setActiveTier(Tier tier);

    /**
     * Sets the control panel for the viewer. The control panel manages tier 
     * order, tier visibility etc. Not all viewers are required to support
     * and use the control panel.
     *
     * @param controller the control panel
     */
    public void setMultiTierControlPanel(MultiTierControlPanel controller);
}
