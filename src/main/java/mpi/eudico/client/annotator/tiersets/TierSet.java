package mpi.eudico.client.annotator.tiersets;

import java.util.ArrayList;
import java.util.List;

/**
 * A TierSet is a named collection of tiers that are grouped by the user
 * and that don't necessarily have to be part of the same tier hierarchy.
 * This grouping mechanism can be used when showing and hiding of groups
 * of tiers in the user interface or when exporting tiers etc.  
 * 
 * @author aarsom
 *
 */
public class TierSet {
	private String name;
	private String desc;
	private List<String> tierList;
	private List<String> visibleTierList;
	private boolean visible = true;
	
	/**
	 * Creates a new TierSet instance.
	 * 
	 * @param name the name of the tier set
	 * @param tierList the list of tier names, members of the set
	 */
	protected TierSet(String name, List<String> tierList){
		this.name = name;
		this.tierList = tierList;
		visibleTierList = new ArrayList<String>();
	}
	
	/**
	 * Sets the name of the set.
	 * 
	 * @param name the name for the set
	 */
	protected void setName(String name){
		this.name = name;
	}
	
	/**
	 * Sets the description of the tier set.
	 * 
	 * @param description the description of the set
	 */
	protected void setDescription(String description){
		this.desc = description;
	}
	
	/**
	 * Returns the name.
	 * 
	 * @return the name
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * Returns the description.
	 * 
	 * @return the description
	 */
	public String getDescription(){
		return desc;
	}
	
	/**
	 * Sets the list of tiers for this tier set.
	 * 
	 * @param tierList the tier list for this set
	 */
	protected void setTierList(List<String> tierList){
		if(tierList != null){
			this.tierList = tierList;
			
			List<String> visibleTiersList = new ArrayList<String>();
			visibleTiersList.addAll(visibleTierList);
			visibleTierList.clear();
			
			for(String tier : visibleTiersList){
				if(tierList.contains(tier)){
					this.visibleTierList.add(tier);
				}
			}
		}
	}
	
	/**
	 * Returns the list of tiers in this set.
	 * 
	 * @return the tier list
	 */
	public List<String> getTierList(){
		return tierList;
	}
	
	/**
	 * Sets the visibility of this set.
	 * 
	 * @param visible if {@code true} (the tiers of) this set is visible
	 */
	public void setVisible(boolean visible){
		this.visible = visible;
	}
	
	/**
	 * Returns whether this set is visible.
	 * 
	 * @return the current visibility flag of the set
	 */
	public boolean isVisible(){
		return visible;
	}
	
	/**
	 * Checks if the tier is in this tier set.
	 * 
	 * @param tierName name of the tier to be checked
	 * 
	 * @return boolean true if yes, else false
	 */
	public boolean containsTier(String tierName){
		return tierList.contains(tierName);
	}
	
	/**
	 * Sets a list of tiers that are visible in this TierSet.
	 *
	 * @param visibleTiersList a list of tiers that should be set visible
	 */
	public void setVisibleTiers(List<String> visibleTiersList){
		if(visibleTiersList != null){
			this.visibleTierList.clear();
			// extra check if the tier is in this tierSet
			for(String tier : visibleTiersList){
				if(tierList.contains(tier)){
					this.visibleTierList.add(tier);
				}
			}
		}
	}
	
	/**
	 * Returns a list of tiers that are currently visible.
	 * 
	 * @return a list of visible tiers 
	 */
	public List<String> getVisibleTierList(){
		return visibleTierList;
	}
	
	/**
	 * Sets the visibility of a single tier.
	 * 
	 * @param tierName the tier
	 * @param visible if {@code true} the tier will be visible, if {@code false}
	 * it will be set invisible
	 */
	public void setTierVisiblity(String tierName, boolean visible){
		if(tierName != null && tierList.contains(tierName)){
			if(visible){
				if(!visibleTierList.contains(tierName)){
					visibleTierList.add(tierName);
				}
			} else {
				if(visibleTierList.contains(tierName)){
					visibleTierList.remove(tierName);
				}
			}
		}
	}
}
