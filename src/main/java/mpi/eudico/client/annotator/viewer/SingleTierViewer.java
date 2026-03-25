package mpi.eudico.client.annotator.viewer;

import mpi.eudico.server.corpora.clom.Tier;


/**
 * Defines methods for viewers that show only (the contents of) a single Tier.
 */
public interface SingleTierViewer {
	
    /**
     * Sets the tier to be shown by this viewer.
     *
     * @param tier the tier to show
     */
    public void setTier(Tier tier);

    /**
     * Returns the current tier shown by this viewer.
     *
     * @return the current tier or {@code null}
     */
    public Tier getTier();
}
