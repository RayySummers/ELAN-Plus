package mpi.eudico.server.corpora.clomimpl.flex;


import java.util.HashMap;
import java.util.List;

import mpi.eudico.server.corpora.clom.EncoderInfo;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;

/**
 * An encoder info object for FLEx files.
 * 
 * @author Aarthy Somasundaram
 */
public class FlexEncoderInfo implements EncoderInfo {

	/** a map containing element {@code name - List<TierImpl>} */
  	private HashMap<String, List<TierImpl>> elementMapping;
  	
	/** a map containing item {@code name - List<TierImpl>} */
  	private HashMap<String, List<TierImpl>> itemMapping;
  	
  	/** a map containing tier {@code name - List<String>} */
  	private HashMap<String, List<String>> typeLangMap;
  	
  	/** a list of {@code tiers - List<TierImpl>} */
  	private List<TierImpl> morphTypeList;
  	
  	private String filePath;
  	
  	private String mediaURL;
	 
    /**
     * Creates a encoder info instance with default values.
     */
    public FlexEncoderInfo() {
        super();
        elementMapping = new HashMap<String, List<TierImpl>>();
        itemMapping = new HashMap<String, List<TierImpl>>();
    }
    
    /**
     * Set the mapping for each element.
     * 
     * @param element the element
     * @param value  the value to be mapped for the element
     */
    public void setMappingForElement(String element, List<TierImpl> value){
    	if(element != null){
    		elementMapping.put(element, value);
    	}
    }  
    
    /**
     * Set the mapping for each element-item.
     * 
     * @param item the item
     * @param value the value to be mapped for the element
     */
    public void setMappingForItem(String item, List<TierImpl> value){
    	if(item != null){
    		itemMapping.put(item, value);
    	}
    }  
    
    /**
     * Set the type-lang value for tiers.
     * 
     * @param map mapping tier names to type and language combinations
     */
    public void setTypeLangMap(HashMap<String, List<String>> map){
    	typeLangMap = map;
    }  
    
    /**
     * Set the morphType tiers.
     *      
     * @param list the list containing the morph-type tiers
     */
    public void setMorphTypeTiers(List<TierImpl> list){
    	morphTypeList = list;
    }  
    
    /**
     * Path of the export file.
     * 
     * @param path the export file path
     */
    public void setFile(String path){
    	filePath = path;
    }
    
    /**
     * Sets the media file path.
     * 
     * @param path the media file path
     */
    public void setMediaFile(String path){
    	mediaURL= path;
    }
    
    /**
     * Returns the media file path.
     * 
     * @return the media file path 
     */
    public String getMediaFile(){
    	return  mediaURL;
    }
    
    /**
     * Returns the path of the file to export to.
     * 
     * @return the export file path
     */
    public String getPath(){
    	return  filePath;
    }
    
    /**
     * Get the type-lang value for a tier.
     * 
     * @param tierName the name of the tier
     * @return a list of size 2, {@code List<String> [0] = type; [1] = lang}
     */
    public List<String> getTypeLangValues(String tierName){
    	return typeLangMap.get(tierName);
    }  
    
    /**
     * Returns the linguistic type mapped for the given element.
     * 
     * @param element name of the element
     * @return {@code List<TierImpl>}, a list of tier objects
     */
    public List<TierImpl> getMappingForElement(String element){
    	return elementMapping.get(element);
    }
    
    /**
     * Returns the list of morph-type tiers.
     * @return {@code List<TierImpl>}, a list of tier objects
     */
    public List<TierImpl> getMorphTypeTiers(){
    	return morphTypeList;
    }
    
    /**
     * Returns the List of {@code TierImpl} objects mapped for 
     * the given element-item.
     * 
     * @param item the name of the item
     * @return {@code List<TierImpl>}, list of tier objects
     */
    public List<TierImpl> getMappingForItem(String item){
    	return itemMapping.get(item);
    }
}
    
   
