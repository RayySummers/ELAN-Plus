package mpi.eudico.client.annotator.tiersets;

/**
 * Defines a tier set change listener.
 *
 * @author Aarthy Somasundaram
 */
public interface TierSetListener {    
    /**
     * Notifies the listener of a change in multiple tier sets.
     */
    public void tierSetChanged();
    
    /**
     * Notifies the listener of a change in visibility of a single tier set.
     * 
     * @param set the changed tier set
     */
    public void tierSetVisibilityChanged(TierSet set);
    
    /**
     * Notifies the listener of a change in visibility of a single tier.
     * 
     * @param tierName the name of the tier
     * @param isVisible if {@code true} the tier has been made visible, 
     * otherwise invisible/hidden
     */
	public void tierVisibilityChanged(String tierName, boolean isVisible);
}