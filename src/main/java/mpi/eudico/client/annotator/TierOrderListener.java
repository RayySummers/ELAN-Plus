package mpi.eudico.client.annotator;

import java.util.List;

/**
 * Defines methods for a listener of changes in the order of tiers.
 */
public interface TierOrderListener {
	/**
     * Notification that the tier order changed.
     * 
     * @param tierOrder a list of tier names in a set order, whatever the ordering
     * principle may have been
     */  
	public void updateTierOrder(List<String> tierOrder);

}




