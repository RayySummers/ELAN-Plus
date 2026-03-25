package mpi.eudico.client.annotator.interlinear.edit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;

/**
 * Utility class which creates maps, to some extent quite specific, of 
 * tier and tier type relations.
 * The maps are not updated when the transcription changes, they provide
 * a one-time view on the structure of tiers and types in the transcription. 
 */
public class TierTypeMapCreator {
	/** a tier name to list of child tier names map */
	private Map<String, List<String>> tiersChildrenMap;
	/** a type name to list of "child" type names map; this map is based on
	 *  the hierarchical relations of tiers that are actually present  */
	private Map<String, List<String>> typesChildrenMap;
	/** a type name to list of tier  */
	private Map<String, List<String>> typesToTiersMap;
	
	/**
	 * Constructor initiates the creation of the maps
	 * 
	 * @param transcription the transcription to create the maps for
	 */
	public TierTypeMapCreator(Transcription transcription) {
		createMaps(transcription);
	}
	
	private void createMaps(Transcription transcription) {
		typesChildrenMap = new HashMap<String, List<String>>();
		tiersChildrenMap = new HashMap<String, List<String>>();
		typesToTiersMap  = new HashMap<String, List<String>>();
		
		List<? extends Tier> list = transcription.getTiers();
	  	List<String> allTierNames = new ArrayList<String>(list.size());	
	  	List<String> allTypeNames = new ArrayList<String>();
	  	
	    // start with tier based iteration
	  	for (Tier tier : list) {
	  		String typeName = tier.getLinguisticType().getLinguisticTypeName();
	  		allTierNames.add(tier.getName());
	  		if (!allTypeNames.contains(typeName)) {
	  			allTypeNames.add(typeName);
	  		}

	  		List<String> childTypeNamesList = null;
	  		
	  		if (typesChildrenMap.containsKey(typeName)) {
	  			childTypeNamesList = typesChildrenMap.get(typeName);
	  		}
	  		
	  		List<String> typeToTiersList = typesToTiersMap.get(typeName);
	  		if (typeToTiersList == null) {
	  			typeToTiersList = new ArrayList<String>();
	  			typesToTiersMap.put(typeName, typeToTiersList);	  			
	  		}
	  		typeToTiersList.add(tier.getName());
	  		
	  		List<? extends Tier> childList = tier.getChildTiers();
	  		
	  		if (childList != null && !childList.isEmpty()) {
	  			List<String>  childTierNamesList = new ArrayList<String>();
	  			
	  			if (childTypeNamesList == null) {
	  				childTypeNamesList = new ArrayList<String>();
	  			}
	  			
	  			for (Tier child : childList) { 
	  				childTierNamesList.add(child.getName());	
	  				final String linguisticTypeName = child.getLinguisticType().getLinguisticTypeName();
					
	  				if (!childTypeNamesList.contains(linguisticTypeName)) {
	  					childTypeNamesList.add(linguisticTypeName);
	  				}
	  			}        			
	  			
	  			tiersChildrenMap.put(tier.getName(), childTierNamesList);
	  			typesChildrenMap.put(typeName, childTypeNamesList);      			
	  		}
	  	}
	  	// add tier types not referenced by any tier
	  	for (LinguisticType lt : transcription.getLinguisticTypes()) {
	  		if (!typesChildrenMap.containsKey(lt.getLinguisticTypeName())) {
	  			typesChildrenMap.put("", new ArrayList<String>(0));
	  		}
	  	}
	  	
	  	// finally add the "all" list under the empty string key
	  	tiersChildrenMap.put("", allTierNames);
	  	typesChildrenMap.put("", allTypeNames);
	}
	
	// getters for the maps
	/**
	 * Returns a tier name to child tier names map.
	 * 
	 * @return the tier to (direct) tier children map, not null
	 */
	public Map<String, List<String>> getTiersChildrenMap() {
		return tiersChildrenMap;
	}

	/**
	 * Returns a map of the type of a parent tier to the types of its direct
	 * child tiers. 
	 * 
	 * @return a "parent" type to types referred to by tiers that are a child
	 * of a tier referencing the "parent" type map, not null
	 */
	public Map<String, List<String>> getTypesChildrenMap() {
		return typesChildrenMap;
	}

	/**
	 * Returns a map of type name to names of tiers referencing that type. 
	 * 
	 * @return a type name to list of tiers referencing that type map, not null
	 */
	public Map<String, List<String>> getTypesToTiersMap() {
		return typesToTiersMap;
	}

	/**
	 * Returns the names of all tiers excluding the ones referring a specific
	 * tier type.
	 * 
	 * @param typeName the name of a tier type
	 * @return a list of all tiers except for the tiers of type {@code typeName}
	 */
	public List<String> getTiersNotOfType(String typeName) {
		List<String> otherTypeTiers = new ArrayList<String>();
		
		for (Map.Entry<String, List<String>> entry : typesToTiersMap.entrySet()) {
			if (!entry.getKey().equals(typeName)) {
				otherTypeTiers.addAll(entry.getValue());
			}
		}
		
		return otherTypeTiers;
	}
}
