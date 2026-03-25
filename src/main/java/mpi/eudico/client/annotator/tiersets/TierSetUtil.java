package mpi.eudico.client.annotator.tiersets;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.PreferencesListener;
import static mpi.eudico.client.annotator.util.ClientLogger.LOG;
import mpi.eudico.server.corpora.clom.Transcription;
import nl.mpi.util.FileUtility;

/**
 * A singleton utility class for handling tier sets.
 *
 */
public class TierSetUtil implements PreferencesListener{

	private List<String> tierSetSortOrder;
	
	private List<String> tierSortOrder;
	
	private List<String> visibleTierSetList;
	
	private HashMap<String, TierSet> tierSetMap;
	
	private static TierSetUtil tierSetUtil;
	
	private static String fileURL;
	
	private TierSetIO tierSetIO;
	
	/** A map of tierset listeners, grouped per document */
    private static Map<Transcription, ArrayList<TierSetListener>> listenerGroups = new HashMap<
    		Transcription, ArrayList<TierSetListener>>();
	
    /**
     * Private constructor.
     */
	private TierSetUtil(){
		// read file and load tier set
		tierSetMap = new HashMap<String, TierSet>();
		tierSetSortOrder = new ArrayList<String>();
		tierSortOrder = new ArrayList<String>();
		
		visibleTierSetList = new ArrayList<String>();
		
		tierSetIO = new TierSetIO();
		
		Object val = Preferences.get("DefaultTierSetFilePath", null) ;
		if(val != null && val instanceof String){
			fileURL = val.toString();
		} else {
			fileURL = Constants.ELAN_DATA_DIR + File.separator + "TierSet.xml";;
		}
		
		readTierSetsFromFile();
	}
	
	private void readTierSetsFromFile(){
		List<TierSet> tierSetList = null;
		try {
			tierSetList = tierSetIO.read(new File(FileUtility.urlToAbsPath(fileURL)));
		} catch (IOException e) {
			LOG.info(ElanLocale.getString("TierSet.Error.FileNotFound"));
		}
		
		if(tierSetList != null){
			for(TierSet tierSet: tierSetList){
				tierSetMap.put(tierSet.getName(), tierSet);
				tierSetSortOrder.add(tierSet.getName());				
			}
			
			updateVisibleTierSetList();
		}
	}
	
	/**
	 * Saves the current tier sets to a file.
	 * 
	 * @since November 2018 replaced the call to {@link TierSetIO#write(File, List)} by
	 *  a call to the newly introduced {@link TierSetIO#writeLS(File, List)}
	 */
	public void writeTierSetsToFile(){
		List<TierSet> tierSetList = new ArrayList<TierSet>();
		for(String name : tierSetSortOrder){
			tierSetList.add(tierSetMap.get(name));
		}
		
		try {
			tierSetIO.writeLS(new File(FileUtility.urlToAbsPath(fileURL)), tierSetList);
			updateVisibleTierSetList();
		} catch (IOException e) {
			LOG.warning("Error while writing the tier set file: " + e.getMessage());
			//e.printStackTrace();
		}
	}
	
	/**
	 * Returns the single instance of this class.
	 * Creates it if it hasn't been created yet.
	 * 
	 * @return the single instance
	 */
	public static TierSetUtil getTierSetUtilInstance(){
		if(tierSetUtil == null){
			tierSetUtil = new TierSetUtil();
		} else {
			checkTierSetFile();
		}
		return tierSetUtil;
	}
	
	private static void checkTierSetFile(){
		String f = null;
		Object val = Preferences.get("DefaultTierSetFilePath", null) ;
		if(val != null && val instanceof String){
			f = val.toString();
		}
		
		if(f != null && !f.equals(fileURL)){
			fileURL = f;
			tierSetUtil = new TierSetUtil();
		}
	}
	
	/**
	 * Returns (a selection of) the tiers of the transcription in the order
	 * of the tier sort order of this utility.
	 *  
	 * @param transcription a transcription containing the tiers
	 * @return a sorted list of tier names
	 */
	public List<String> getTierOrder(Transcription transcription){
		List<String>  tierOrder = new ArrayList<String>();
		for(String tier : tierSortOrder){
			if(transcription.getTierWithId(tier) != null){
				tierOrder.add(tier);
			}
		}
		return tierOrder;
	}
	
	/**
	 * Creates a tier sort order based on the order of the available tier sets
	 * and the order of tiers in each set. Each tier is added to the sorted list
	 * only once.
	 */
	private void updateTierOrder(){
		tierSortOrder.clear();
		for (int i = 0; i < tierSetSortOrder.size(); i++) {
            TierSet tierSet = tierSetMap.get(tierSetSortOrder.get(i));
            if(tierSet.isVisible()){
            	for(String tierName: tierSet.getTierList()){
            		if(!tierSortOrder.contains(tierName)){
            			tierSortOrder.add(tierName);
            		}
            	}
            }
        }    
	}
	
	/**
	 * Returns a list of tier set names, in the order as defined by the user.
	 * 
	 * @return a list of tier sets
	 */
	public List<String> getTierSetList(){
		return tierSetSortOrder;
	}
	
	/**
	 * Returns the tier set with the specified name.
	 *  
	 * @param tierSetName the name of the tier set
	 * @return the {@code TierSet} or {@code null} if there is no set with 
	 * that name
	 */
	public TierSet getTierSet(String tierSetName){
		return tierSetMap.get(tierSetName);
	}
	
	/**
	 * Updates the list of visible tier sets in the order of the tier set 
	 * sort order.
	 */
	private void updateVisibleTierSetList(){
		visibleTierSetList.clear();
        for (int i = 0; i < tierSetSortOrder.size(); i++) {
            TierSet tierSet = tierSetMap.get(tierSetSortOrder.get(i));
            
            if(tierSet.isVisible()){
            	visibleTierSetList.add(tierSet.getName());
            }
        } 
        
        updateTierOrder();
	}
	
	/**
	 * Returns a list of visible tier set names.
	 * 
	 * @return a list of visible tier set names
	 */
	public List<String> getVisibleTierSets(){
		return visibleTierSetList;
	}
	
	/**
	 * Checks if there is already a tier set with the specified name.
	 * 
	 * @param tierSetName the name of the tier set
	 * 
	 * @return {@code true} if there is a set with that name, {@code false} 
	 * otherwise
	 */
	public boolean checkIfTierSetExists(String tierSetName){
		// To Do : should the names be case sensitive????????
		// Should the tier set name be unique including the tier names
		
		if(tierSetSortOrder.contains(tierSetName)){
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Create a new {@code TierSet} with the specified name, containing the 
	 * specified tiers.
	 * 
	 * @param name the name for the tier set
	 * @param tierList the list of tiers in the set
	 * 
	 * @return the new {@code TierSet} or {@code null} if there is already a 
	 * set with that name
	 */
	public TierSet createTierSet(String name, List<String> tierList){
		if(name == null || tierList == null){
			// TO DO error Message;
			return null;
		}
		
		//Tier set name should be unique, check should be implemented
		
		if(!checkIfTierSetExists(name)){
			TierSet tierSet = new TierSet(name, tierList);
			tierSetSortOrder.add(name);
			tierSetMap.put(name, tierSet);
			
			updateVisibleTierSetList();
				
			return tierSet;
		} else {
			
			// TO DO : error message ????????
			return null;
		}
	}
	
	/**
	 * Updates lists and maps of tier sets after a change of a tier set's name.
	 * 
	 * @param oldName the previous name of the set
	 * @param tierSet the modified tier set
	 */
	public void updateTierSet(String oldName, TierSet tierSet){
		tierSetSortOrder.set(tierSetSortOrder.indexOf(oldName), tierSet.getName());
		
		if(visibleTierSetList.contains(oldName)){
			visibleTierSetList.remove(oldName);
			visibleTierSetList.add( tierSet.getName());
		}
		
		tierSetMap.remove(oldName);
		tierSetMap.put(tierSet.getName(), tierSet);
	}
	
	/**
	 * Removes the tier set with the specified name.
	 * 
	 * @param name the name of the set to remove
	 */
	public void deleteTierSet(String name){
		if(tierSetSortOrder.contains(name)){
			tierSetSortOrder.remove(name);
		}
		
		if(tierSetMap.containsKey(name)){
			tierSetMap.remove(name);
		}
		
		updateVisibleTierSetList();
	}
	
	/**
	 * Updates the tier set sort order.
	 *  
	 * @param tierSetOrder the new order
	 */
	public void updateTierSetSortOrder(List<String> tierSetOrder){
		if(tierSetOrder != null){
			tierSetSortOrder = tierSetOrder;
		}
	}
	
	/**
	 * Not implemented.
	 * 
	 * @param name tier set name
	 */
	public void updateTierSet(String name){
		// TO  DO : update tier name, tier list, visible tiers etc
	}
	
	 /**
     * Adds a TierSetListener to the listener list of the specified document.
     *  
     * @param document the document in which changes the listener is interested, 
     *       the key to the group of listeners per document
     * @param listener the listener to changes in the tier set for the specified document
     */
    public void addTierSetListener(Transcription document, TierSetListener listener) {
		if (listenerGroups.containsKey(document)) {
			// check whether it is already in the list
			ArrayList<TierSetListener> listeners = listenerGroups.get(document);
			if (!listeners.contains(listener)) {
				listeners.add(listener);
			}
		} else {
			ArrayList<TierSetListener> list = new ArrayList<TierSetListener>();
			list.add(listener);
			
			listenerGroups.put(document, list);
		}
    }
    
    /**
     * Removes a TierSetListener from the listener list of the specified document.
     *  
     * @param document the document in which changes the listener was interested, 
     *       the key to the group of listeners per document
     * @param listener the listener to changes in the tier set for the specified document
     */
    public void removeTierSetListener(Transcription document, TierSetListener listener) {
		if (listenerGroups.containsKey(document)) {
			// check whether it is already in the list
			ArrayList<TierSetListener> listeners = listenerGroups.get(document);
			if (listeners.contains(listener)) {
				listeners.remove(listener);
			}
		} 
    }
    
    /**
     * Method for notifying all listeners of document independent, application wide
     * tier set changes.
     * 
     * @param tierSet the changed tier set
     */
    public void notifyAllListeners(TierSet tierSet) {
    	updateVisibleTierSetList();
    	ArrayList<TierSetListener> listeners = null;
    	Iterator<ArrayList<TierSetListener>> listIt = listenerGroups.values().iterator();
    	    
    	while (listIt.hasNext()) {
    		listeners = listIt.next();
    		if (listeners != null) {
    			for (int i = 0; i < listeners.size(); i++) {
    				listeners.get(i).tierSetVisibilityChanged(tierSet);
    			}
    		}
    	}
    }
    
    /**
     * Method for notifying all listeners of document independent, application wide
     * tier set changes.
     * 
     * @param tierName the name of the tier
     * @param isVisible the new visibility of the tier
     */
    public void notifyAllListeners(String tierName, boolean isVisible) {
    	ArrayList<TierSetListener> listeners = null;
    	Iterator<ArrayList<TierSetListener>> listIt = listenerGroups.values().iterator();
    	    
    	while (listIt.hasNext()) {
    		listeners = listIt.next();
    		if (listeners != null) {
    			for (int i = 0; i < listeners.size(); i++) {
    				listeners.get(i).tierVisibilityChanged(tierName, isVisible);
    			}
    		}
    	}
    }

    /**
     * Method for notifying all listeners of document independent, application wide
     * tier set changes.
     */
    public void notifyAllListeners() {
    	updateVisibleTierSetList();
    	ArrayList<TierSetListener> listeners = null;
    	Iterator<ArrayList<TierSetListener>> listIt = listenerGroups.values().iterator();
    	    
    	while (listIt.hasNext()) {
    		listeners = listIt.next();
    		if (listeners != null) {
    			for (int i = 0; i < listeners.size(); i++) {
    				listeners.get(i).tierSetChanged();
    			}
    		}
    	}
    }

	@Override
	public void preferencesChanged() {
		// stub		
	}
	
	/**
	 * Returns whether a specific tier is visible.
	 * <br>
	 * Added by: Micha Hulsbosch
	 * @param name the name of a tier
	 * 
	 * @return {@code true} if a tier with that name is in the list of visible
	 * tiers, {@code false} if the tier is not there or is hidden
	 */
	public Boolean isVisibleTier(String name) {
		return getVisibleTiers().contains(name);
	}
	
	/**
	 * Returns (the names of) all visible tiers.
	 * <br>
	 * Added by: Micha Hulsbosch
	 * @return a list of tier names that are visible
	 *         in the visible tier sets
	 */
	public List<String> getVisibleTiers() {
		List<String> tierList = new ArrayList<String>();
		for(String tierSetName : getVisibleTierSets()){
			for(String tierName : getTierSet(tierSetName).getVisibleTierList()){
				if(!tierList.contains(tierName)){
					tierList.add(tierName);
				}
			}
		}
		return tierList;
	}
	
	/**
	 * Returns whether a tier is in a visible tier set.
	 * 
	 * @param name the name of the tier
	 * @return {@code true} if the tier is in a visible set, {@code false}
	 * otherwise
	 */
	public Boolean isTierInVisibleTierSets(String name) {
		return getTiersInVisibleTierSets().contains(name);
	}
	
	/**
	 * Returns a list of tier names in all visible sets.
	 * <br>
	 * Added by: Micha Hulsbosch
	 * @return a list of tier names that are in
	 *         the visible tier sets
	 */
	public List<String> getTiersInVisibleTierSets() {
		List<String> tierList = new ArrayList<String>();
		for(String tierSetName : getVisibleTierSets()){
			for(String tierName : getTierSet(tierSetName).getTierList()){
				if(!tierList.contains(tierName)){
					tierList.add(tierName);
				}
			}
		}
		return tierList;
	}
}
