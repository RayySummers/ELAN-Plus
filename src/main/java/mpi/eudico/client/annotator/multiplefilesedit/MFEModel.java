package mpi.eudico.client.annotator.multiplefilesedit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import javax.swing.JOptionPane;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.server.corpora.lexicon.LexiconQueryBundle2;
import mpi.eudico.util.multilangcv.LangInfo;

/**
 * Multiple File Edit model.
 * <p>
 * Public methods must be synchronized since it is accessed both from the 
 * Event Dispatch Thread and the EAFLoadThread.
 */
public class MFEModel {
	/**
	 * An enumeration of change states. 
	 */
	public enum Changes {
		/** no changes */
		NONE,
		/** data modified */
		MODIFIED,
		/** data removed */
		REMOVED,
		/** new data added */
		NEW,
		/** new data added and modified */
		NEW_MODIFIED
	}
	/** id for tier name column */
	public static final int TIER_NAMECOLUMN = 0;
	/** id for tier type column */
	public static final int TIER_TYPECOLUMN = 1;
	/** id for annotator column */
	public static final int TIER_ANNOTATORCOLUMN = 2;
	/** id for participant column */
	public static final int TIER_PARTICIPANTCOLUMN = 3;
	/** id for tier language column */
	public static final int TIER_LANGUAGECOLUMN = 4;
	/** id for dependent tiers column */
	public static final int TIER_CHILDRENCOLUMN = 5;
	/** id for parent tier column */
	public static final int TIER_PARENTCOLUMN = 6;
	/** id for type name column */
	public static final int TYPE_NAMECOLUMN = 0;
	/** id for a type's data category column */
	public static final int TYPE_DATACATEGORYCOLUMN = 1;
	/** id for a type's alignable property column */
	public static final int TYPE_TIMEALIGNABLECOLUMN = 2;
	/** id for stereotype column */
	public static final int TYPE_STEREO = 3;
	/** id for indicating multiple languages */
	public static final String MULTIPLE_LANGUAGES_ID = "mul";
	
	/* Table headers */
	private List<String> type_headers;
	private List<String> tier_headers;
	
	/* Original data loaded from eaf files */
	private List<TierImpl> original_tier_data;
	private List<LinguisticType> original_type_data;
	private Map<String, List<String>> tier_children;
	/* HS 07-2010 use maps to connect original tier and changed tier and changes */
	private Map<TierImpl, TierImpl> orig_to_changed_tierMap;
	private Map<TierImpl, Changes> tier_to_changesMap;
	
	/* Modified data */
	private List<TierImpl> tier_data;
	private List<LinguisticType> type_data;
	
	/* Changes */ /* HS tier_changes and flagged_tiers are kept in sync */
	private List<Changes> tier_changes;
	private List<Changes> type_changes; /* HS linguistic types cannot be removed, lists are in sync */
	private List<Boolean> flagged_tiers;
	
	/* Inconsistent Types */
	private List<Boolean> type_consistency;
	private List<Boolean> tier_type_consistency;
	
	private boolean removable_tiers = true;
	
	/**
	 * Constructor that initializes the multiple file editing model.
	 * The most important thing to note is that there are two administrations of
	 * tiers and types. First the originals are stored in lists, but also mirrored
	 * (copied) to other lists that will administer all changes. Later these
	 * administrations can be used to find out what has changed and save these changes
	 * to files.
	 */
	public MFEModel() {
		type_headers = new ArrayList<String>();
		tier_headers = new ArrayList<String>();
		
		original_tier_data = new ArrayList<TierImpl>();
		original_type_data = new ArrayList<LinguisticType>();
		tier_children = new HashMap<String, List<String>>();
		
		orig_to_changed_tierMap = new HashMap<TierImpl, TierImpl>();
		tier_to_changesMap = new HashMap<TierImpl, Changes>();
		
		tier_data = new ArrayList<TierImpl>();
		type_data = new ArrayList<LinguisticType>();
		
		tier_changes = new ArrayList<Changes>();
		type_changes = new ArrayList<Changes>();
		flagged_tiers = new ArrayList<Boolean>();
		
		tier_type_consistency = new ArrayList<Boolean>();
		type_consistency = new ArrayList<Boolean>();
		
		setLocale();
	}
	/*
	 * TIER METHODS
	 */
	/**
	 * Returns the number of columns in the tier table.
	 * 
	 * @return the number of columns in the tier table
	 */
	public synchronized int getTierColumnCount() {
		return tier_headers.size();
	}
	/**
	 * Returns the number of rows in the tier table.
	 * 
	 * @return the number of rows in the tier table
	 */
	public synchronized int getTierRowCount() {
		return tier_data.size();
	}
	/**
	 * Returns the name of a column in the tier table.
	 * 
	 * @param col the index of the column
	 * @return the name of the column
	 */
	public synchronized String getTierColumnName(int col) {
		return tier_headers.get(col);
	}
	/**
	 * Returns the value of a cell in the tier table.
	 * 
	 * @param row the row index of the cell
	 * @param col the column index of the cell
	 * 
	 * @return the value of the specified cell or the empty string if an index is
	 * out of bounds
	 */
	public synchronized Object getTierValueAt(int row, int col) {
		Object val = "";
		if(withinTierBounds(row, col)) {
			TierImpl tier = tier_data.get(row);
			switch(col) {
			case TIER_NAMECOLUMN:
				val = tier.getName();
				break;
			case TIER_TYPECOLUMN:
				LinguisticType type = tier.getLinguisticType();
				if(type!=null)
					val = type.getLinguisticTypeName();
				else
					val = "";
				break;
			case TIER_ANNOTATORCOLUMN:
				val = tier.getAnnotator();
				break;
			case TIER_PARTICIPANTCOLUMN:
				val = tier.getParticipant();
				break;
			case TIER_LANGUAGECOLUMN:
				String sval = tier.getLangRef();
				if (sval == null) {
					sval = "";					
				} else if (sval.contains(",")) {
					// The MFEFrame made sure it is in the recent list.
					sval = MULTIPLE_LANGUAGES_ID;
				}
				val = sval;
				break;
			case TIER_CHILDRENCOLUMN:
				String tier_name = getTierName(row);
				String children = "";
				List<String> _children = tier_children.get(tier_name);
				if(_children!=null) {
					StringBuffer sb = new StringBuffer(children);
					for(String child:_children) {
						sb.append(child).append(" ");
					}
					children = sb.toString();	
				}
				val = (Object)children;
				break;

			case TIER_PARENTCOLUMN:
				StringBuffer parentName = new StringBuffer();
				//find the parent of a child tier (tier.getParentTier does not work, no clue why)
				for(Map.Entry<String, List<String>> entry : tier_children.entrySet()){
					List<String> childrenList = entry.getValue();
					if( childrenList.contains(tier.getName()) ){
						parentName.append(entry.getKey()).append(" ");
						continue;
					}
				}
				val = parentName.toString();
				break;
			}
		}
		return val;
	}
	
	/**
	 * Returns a tier given its current name.
	 * 
	 * @param name The current name of a tier
	 * @return TierImpl if a tier with name exists or null otherwise.
	 */
	public synchronized TierImpl getTierByName(String name) {
		for(TierImpl tier:tier_data) {
			if(tier.getName().equals(name))
				return tier;
		}
		return null;
	}
	
	/**
	 * Returns the name of the tier at the specified row.
	 *  
	 * @param row the row index
	 * @return the name of the tier at that row
	 */
	public synchronized String getTierName(int row) {
		return tier_data.get(row).getName();
	}
	
	/**
	 * Returns an array of indexes of every tier with any of the specified types.
	 * 
	 * @param types A list of LinguisticTypes
	 * @return an array of indexes or an empty array if there are no tiers with any of
	 * the specified types.
	 */
	public synchronized int[] getTiersWithTypesByIndex(List<LinguisticType> types) {
		int[] tiers_with_types_indeces;
		ArrayList<Integer> tiers_with_types = new ArrayList<Integer>();
		
		for(TierImpl tier:tier_data) {
			if(types.contains(tier.getLinguisticType())) {
				tiers_with_types.add(tier_data.indexOf(tier));
			}
		}
		tiers_with_types_indeces=new int[tiers_with_types.size()];
		int i=0;
		for(Integer index:tiers_with_types)
			tiers_with_types_indeces[i++] = index;
		
		return tiers_with_types_indeces;
	}
	
	/**
	 * Changes a specific value of a specific tier.
	 * 
	 * @param value the new value
	 * @param row the index of the tier
	 * @param col the column index of the property to change, one of TIER_NAMECOLUMN,
	 * TIER_TYPECOLUMN, TIER_ANNOTATORCOLUMN, TIER_PARTICIPANTCOLUMN
	 */
	public synchronized void changeTierValueAt(Object value, int row, int col) {
		TierImpl tier = tier_data.get(row);
		switch(col) {
		case TIER_NAMECOLUMN:
			
			///** do nothing if the tier name hasn't changed*/
			if(tier.getName().equals(value)){
				break;
			}
			
			/** checks whether there is another tier with that name */
			if(getTierByName((String)value) != null){
				//error message
				JOptionPane.showMessageDialog(null,
                        ElanLocale.getString(
                            "EditTierDialog.Message.Exists"),
                        ElanLocale.getString("Message.Error"),
                        JOptionPane.ERROR_MESSAGE);
				break;
			}
			
			/** updates the tier_children list */
			List<String> removeList = new ArrayList<String>();
			Iterator<Entry<String, List<String>>> it = tier_children.entrySet().iterator();
			while( it.hasNext() ){
				Entry<String, List<String>> entry = it.next();
				String parent = entry.getKey();
				List<String> childrenList = entry.getValue();
				if(parent.equals(tier.getName())){
					removeList.add(parent);
				} 
				
				for(int i=0; i < childrenList.size(); i++){
					if( childrenList.get(i).equals(tier.getName()) ){
						childrenList.remove(i);
						childrenList.add(i, (String)value);
					}
				}
			}
			
			for(String s : removeList){
				tier_children.put((String)value, tier_children.get(s));
				tier_children.remove(s);
			}
			
			tier.setName((String)value); /** HS should check whether there is another tier with that name */
			break;
		case TIER_TYPECOLUMN:
			String name = (String)value;
			if(!name.equals(ElanLocale.getString("MFE.Multiple"))) {
				LinguisticType type = getLinguisticType(name);
				tier.setLinguisticType(type);
			}
			break;
		case TIER_ANNOTATORCOLUMN:
			String annotator1 = tier.getAnnotator();
			String annotator2 = (String)value;
			if(!annotator2.contains(",")) {
				//New annotator string does not contain illegal characters
				if(annotator1.contains(",")) {
					//Display warning, overriding multiple annotators to single
					int n = JOptionPane.showConfirmDialog(null,
							ElanLocale.getString("MFE.OverrideAnnotatorDiag"),
							ElanLocale.getString("MFE.OverrideAnnotatorDiagTitle"),
							JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
					if(n==JOptionPane.YES_OPTION)
						tier.setAnnotator(annotator2);
				} else {
					tier.setAnnotator(annotator2);
				}
			}
			break;
		case TIER_PARTICIPANTCOLUMN:
			String participant1 = tier.getParticipant();
			String participant2 = (String)value;
			if(!participant2.contains(",")) {
				//New participant string does not contain illegal characters
				if(participant1.contains(",")) {
					//Display warning, overriding multiple participant to single
					int n = JOptionPane.showConfirmDialog(null,
							ElanLocale.getString("MFE.OverrideParticipantDiag"),
							ElanLocale.getString("MFE.OverrideParticipantDiagTitle"),
							JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
					if(n==JOptionPane.YES_OPTION)
						tier.setParticipant(participant2);
				} else {
					tier.setParticipant(participant2);
				}
			}
			break;
		case TIER_LANGUAGECOLUMN:
			String id = null;
			if (value instanceof String) {
				id = (String)value;
			} else if (value instanceof LangInfo) {
				id = ((LangInfo)value).getId();
			}
			if ("".equals(id)) {
				id = null;
			}
			String origLangRef = tier.getLangRef();
			if (origLangRef != null && origLangRef.contains(",")) {
				//Display warning, overriding multiple languages to single
				int n = JOptionPane.showConfirmDialog(null,
						ElanLocale.getString("MFE.OverrideLanguageDiag"),
						ElanLocale.getString("MFE.OverrideLanguageDiagTitle"),
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if(n==JOptionPane.YES_OPTION)
					tier.setLangRef(id);
			} else {
				tier.setLangRef(id);
			}
			break;
		}
		Changes curChange = tier_to_changesMap.get(tier);
		if (curChange == Changes.NEW || curChange == Changes.NEW_MODIFIED) {
			tier_to_changesMap.put(tier, Changes.NEW_MODIFIED);
		} else {
			tier_to_changesMap.put(tier, Changes.MODIFIED);
		}
		
		/*
		if(tier_changes.get(row)==Changes.NEW || tier_changes.get(row)==Changes.NEW_MODIFIED)
			tier_changes.set(row, Changes.NEW_MODIFIED);
		else
			tier_changes.set(row, Changes.MODIFIED);
		*/
	}
	
	private int getTierIndex(String name) {
		for(int i=0; i<tier_data.size(); i++) {
			if(tier_data.get(i).getName().equals(name))
				return i;
		}
		return -1;
	}
	
	/**
	 * Add a tier and its dependent tiers to the flagged tiers list.
	 * 
	 * @param row the row in the model to flag
	 */
	public synchronized void flagTier(int row) {
		String name = getTierName(row);
		// HS 07-2010 this should be done recursively
		// Also flag child tiers
		flagDependingTiers(name);
		/*
		List<String> children = tier_children.get(name);
		if(children!=null) {		
			for(String child:children) {
				int i = getTierIndex(child);
				if (i == -1) {
					System.out.println("Child not found in tier data...." + child);
				}
				flagged_tiers.set(i, true);
				
			}
		}
		*/
		// Check if there is a parent tier and update it
		/* HS 07-2010 the parent is never set in this model so don't remove
		Tier parent = tier_data.get(row).getParentTier();
		if(parent!=null) {
			//parent.removeChild(tier_data.get(row));
			tier_data.get(row).unreferenced();
		}
		*/
		flagged_tiers.set(row, true);
	}
	
	/**
	 * Flags a tier's direct children as deleted, recursive.
	 * 
	 * @since 07-2010
	 * 
	 * @param tierName the name of the parent tier
	 */
	private void flagDependingTiers(String tierName) {
		List<String> children = tier_children.get(tierName);
		
		if(children != null) {			
			for(String child: children) {
				int i = getTierIndex(child);
				if (i == -1) {
					System.out.println("Child not found in tier data...." + child);
				} else {
					flagged_tiers.set(i, true);
				}
				flagDependingTiers(child);
			}
		}
	}
	
	/**
	 * Removes flagged tiers from the table.
	 */
	public synchronized void removeFlaggedTiers() {
		TierImpl tier;
		
		for(int i=flagged_tiers.size()-1;i>=0;i--) {
			if(flagged_tiers.get(i)) {
				tier = tier_data.remove(i);
				flagged_tiers.remove(i);
				tier_changes.set(i, Changes.REMOVED);
				if (tier != null) {
					tier_to_changesMap.put(tier, Changes.REMOVED);
				}
			}
		}
	}
	
//	public void removeTierRow(int row) {
//		String name = getTierName(row);
//		List<String> children = tier_children.get(name);
//		if(children!=null) {
//			for(String child:children) {
//				int i = getTierIndex(child);
//				tier_data.remove(i);
//			}
//		}
//		tier_data.remove(row);
//	}
	private boolean withinTierBounds(int row, int col) {
		return (row>=0 && row<tier_data.size() && tier_data.size()>0) &&
				(col>=0 && col<tier_headers.size() && tier_headers.size()>0);
	}
	/**
	 * Returns the status in changes of a (pre-existing) tier.
	 * 
	 * @param name the original tier name
	 * @return changes in status or null if the tier doesn't exist
	 */
	public synchronized Changes getTierChangeByOriginalName(String name) {
		TierImpl o_tier = null;
		for(TierImpl _o_tier:original_tier_data) {
			if(_o_tier.getName().equals(name)) {
				o_tier = _o_tier;
				break;
			}
		}
		
		if(o_tier != null) {
			/* name is a tier that was in the original data */
			//int index = original_tier_data.indexOf(o_tier);
			//return tier_changes.get(index);
			return tier_to_changesMap.get(orig_to_changed_tierMap.get(o_tier));
		} else {
			/* name is either a new tier, or is non-existent
			 * either way it's an error. */
			return null;
		}
		
	}
	/**
	 * Returns the names of the child tiers.
	 *  
 	 * @param name name of the tier
	 * @return a list of names of child tier
	 */
	public synchronized List<String> getChildrenOfTier(String name) {
		/* with this way of getting the child tiers, should it be prevented 
		 * that a tier with the same name is added later?*/
		return tier_children.get(name);
	}
	
	/**
	 * Returns a tier given its original name.
	 * This only applies to pre-existing tiers.
	 * 
	 * @param o_name original tier name
	 * @return the tier or null if original name doesn't exist
	 */
	public synchronized TierImpl getTierByOriginalName(String o_name) {
		int o_index = -1;
		TierImpl tier = null;
		
		for (int i = 0; i < original_tier_data.size(); i++) {
			TierImpl o_tier = original_tier_data.get(i);
			if (o_tier.getName().equals(o_name)) {
				o_index = i;
				tier = o_tier;
				break;
			}
		}
		// HS 07-2010 
		if (tier != null) {
			return orig_to_changed_tierMap.get(tier);
		}
		
		return null;
		/* disabled HS 07-2010
		if (o_index >= 0) {
			int nr_removed_tiers = 0;
			for (int i = 0; i < tier_changes.size() || i <= o_index; i++) {
				nr_removed_tiers += (tier_changes.get(i) == Changes.REMOVED) ? 1
						: 0;
			}
			o_index -= nr_removed_tiers;
			return tier_data.get(o_index);
		} else {
			return null;
		}
		*/
	}
	/**
	 * This method adds a tier to the model. Make sure the type is already added.
	 * And only add originals before editing.
	 * <p>
	 * Is called from the EAFLoadThread.
	 * @param name the tier name
	 * @param type the tier type name
	 * @param annotator the annotator property
	 * @param participant the participant property
	 * @param parent the name of the parent tier
	 * @param langRef the content language property
	 * 
	 * @return the number, index, of the row
	 * 
	 * @throws InconsistentChildrenException if a parent-child tier inconsistency
	 * is encountered
	 */
	public synchronized int addOriginalTier(String name, String type, String annotator,
			String participant, String parent, String langRef) throws InconsistentChildrenException {
		TierImpl o_tier = tierAllreadyAdded(name);
		int o_index = original_tier_data.indexOf(o_tier);
		if(o_tier != null) {
			/* If the tier is already added, check it's properties */
			TierImpl tier = getTierByName(name);
			
			String _type = o_tier.getLinguisticType().getLinguisticTypeName();
			if(!_type.equals(type)) {
				LinguisticType multi = new LinguisticType(ElanLocale.getString("MFE.Multiple"));
				o_tier.setLinguisticType(multi);
				tier.setLinguisticType(multi);
			}
			if(tier_type_consistency.get(o_index)) {
				tier_type_consistency.set(o_index,
						isConsistentType(type_data.indexOf(getLinguisticType(type))));
			}
			
			String _annotator = o_tier.getAnnotator();
			if (annotator.isEmpty()) {
				if(!_annotator.isEmpty() && !_annotator.startsWith(", ")) {
					o_tier.setAnnotator(", "+_annotator);
					tier.setAnnotator(", "+_annotator);
				}
			} else if (!_annotator.contains(annotator)) {
				o_tier.setAnnotator(_annotator+", "+annotator);
				tier.setAnnotator(_annotator+", "+annotator);
			}
			
			String _participant = o_tier.getParticipant();
			if (participant.isEmpty()) {
				if(!_participant.isEmpty() && !_participant.startsWith(", ")) {
					o_tier.setParticipant(", "+_participant);
					tier.setParticipant(", "+_participant);
				}
			} else if (!_participant.contains(participant)) {
				o_tier.setParticipant(_participant+", "+participant);
				tier.setParticipant(_participant+", "+participant);
			}	
			
			String _langref = o_tier.getLangRef();
			if (_langref == null) {
				_langref = "";
			}
			// This seems a better strategy to merge names, but I didn't want to
			// casually change behaviour people may be used to (the code above).
			if (langRef.isEmpty()) {
				if (!_langref.isEmpty() && !_langref.startsWith(", ")) {
					o_tier.setLangRef(", "+_langref);
					tier.setLangRef(", "+_langref);
				}
			} else if (!_langref.contains(langRef)) { // a substring could wrongly match here!
				o_tier.setLangRef(_langref+", "+langRef);
				tier.setLangRef(_langref+", "+langRef);
			}
			
			if(parent != null && !parent.equals("")) {
				// current tier has a parent;
				// check whether this parent is correct
				// according to the previously loaded tier hierarchy
				String parentOfTierInModel = ""; 
				for (Map.Entry<String, List<String>> tcEntry : tier_children.entrySet()) {
					String loadedParent = tcEntry.getKey();
					if(tcEntry.getValue().contains(name)) {
						parentOfTierInModel = loadedParent;
					}
				}
				if(parentOfTierInModel.isEmpty()) {
					throw new InconsistentChildrenException(parent, name, null);
				} else if(!parentOfTierInModel.equals(parent)) {
					throw new InconsistentChildrenException(parent, name, parentOfTierInModel);
				}
			} else {
				// current tier has no parent
				// check whether this is correct
				boolean consistent = true;
				StringBuffer loadedParents = new StringBuffer(); 
				for (Map.Entry<String, List<String>> tcEntry : tier_children.entrySet()) {
					String loadedParent = tcEntry.getKey();
					if(tcEntry.getValue().contains(name)) {
						loadedParents.append(loadedParent).append(" ");
						consistent = false;
					}
				}
				if(!consistent) {
					throw new InconsistentChildrenException(null, name, loadedParents.toString());
				}
			}
			
			return tier_data.indexOf(tier);
		} else {
			/* If the tier is not added, add it */
			LinguisticType _type=getLinguisticType(type);
			o_tier = new TierImpl(name, participant, null, _type);
			o_tier.setAnnotator(annotator);
			o_tier.setLangRef(langRef);
			TierImpl tier = new TierImpl(name, participant, null, _type);
			tier.setAnnotator(annotator);
			tier.setLangRef(langRef);
			
			if(parent!=null && !parent.equals("")) {
				List<String> children = tier_children.get(parent);
				if(children!=null)
					/* parent already has children */
					children.add(name);
				else {
					/* new parent-child */
					children = new ArrayList<String>();
					children.add(name);
					tier_children.put(parent, children);
				}
			}
			
			original_tier_data.add(o_tier);
			tier_data.add(tier);
			orig_to_changed_tierMap.put(o_tier, tier); // HS
			tier_to_changesMap.put(tier, Changes.NONE); // HS
			tier_changes.add(Changes.NONE);
			flagged_tiers.add(false);
			tier_type_consistency.add(isConsistentType(type_data.indexOf(_type)));
			return tier_data.indexOf(tier);
		}
	}
	
	private TierImpl tierAllreadyAdded(String name) {
		for(TierImpl tier:original_tier_data) {
			if(tier.getName().equals(name))
				return tier;
		}
		return null;
	}
	
	/**
	 * Adds a new tier to the model.
	 * 
	 * @param name the name of the tier
	 * @param type the tier type
	 * @param annotator the annotator property
	 * @param participant the participant property
	 * @param langRef a content language reference
	 * 
	 * @return the index of the row where the tier has been added
	 */
	public synchronized int addTier(String name, String type, String annotator, String participant, String langRef) {
		LinguisticType _type = getLinguisticType(type);
		TierImpl tier = new TierImpl(name, participant, null, _type);
		tier.setAnnotator(annotator);
		tier.setLangRef(langRef);
		tier_data.add(tier);
		tier_changes.add(Changes.NEW);
		tier_to_changesMap.put(tier, Changes.NEW);// HS
		flagged_tiers.add(false);
		tier_type_consistency.add(true);
		return tier_data.indexOf(tier);
	}
	
	/**
	 * Adds a new dependent tier.
	 * 
	 * @param name the name of the tier
	 * @param type the type of the tier
	 * @param annotator the annotator property
	 * @param participant the participant property
	 * @param parent the name of the parent tier
	 * @param langRef a content language reference
	 * 
	 * @return the index of the row where the tier has been added
	 */
	public synchronized int addChildTier(String name, String type, String annotator, String participant, String parent, String langRef) {
		LinguisticType _type = getLinguisticType(type);
		TierImpl tier = new TierImpl(name, participant, null, _type);
		tier.setAnnotator(annotator);
		tier.setLangRef(langRef);
		tier.setParentTier(getTierByName(parent));
		tier_data.add(tier);
		
		List<String> children = tier_children.get(parent);
		if(children!=null)
			/* parent already has children */
			children.add(name);
		else {
			/* new parent-child */
			children = new ArrayList<String>();
			children.add(name);
			tier_children.put(parent, children);
		}
		
		tier_changes.add(Changes.NEW);
		tier_to_changesMap.put(tier, Changes.NEW);// HS
		flagged_tiers.add(false);
		tier_type_consistency.add(true);
		return tier_data.indexOf(tier);
	}
	
	/**
	 * Returns the tier-type consistency of a tier.
	 * 
	 * @param tier_row the row index of the tier
	 * @return {@code true} if the type is consistent, {@code false} otherwise
	 */
	public synchronized boolean isTypeConsistentTier(int tier_row) {
		return tier_type_consistency.get(tier_row);
	}
	
	/**
	 * Returns a list of tier instances.
	 * 
	 * @return a list of tier objects
	 */
	public synchronized List<TierImpl> getTiers() {
		return tier_data;
	}
	
	/**
	 * Returns the modification status of a tier.
	 * 
	 * @param i the row index of the tier
	 * @return a {@code Changes} item indicating the status
	 */
	public synchronized Changes getTierChange(int i) {
		return tier_changes.get(i);
	}
	
	/**
	 * Returns an array of names of the tiers that are consistent with respect
	 * to its type.
	 * 
	 * @return An array of strings with all different consistent tier names.
	 */
	public synchronized String[] getConsistentTierNames() {
		List<String> consistent_tiers = new ArrayList<String>();
		for(int i=0;i<tier_data.size();i++) {
			if(tier_type_consistency.get(i))
				consistent_tiers.add(tier_data.get(i).getName());
		}
		
		String[] tiers = new String[consistent_tiers.size()];
		for(int i=0;i<consistent_tiers.size();i++) {
			tiers[i] = consistent_tiers.get(i);
		}
		return tiers;
	}
	
	/*
	 * TYPE METHODS 
	 */
	/**
	 * Returns an array of tier type names.
	 * 
	 * @return An array of strings with all different LinguisticType names.
	 */
	public synchronized String[] getLinguisticTypeNames() {
		String[] types = new String[type_data.size()];
		for(int i=0;i<type_data.size();i++) {
			types[i] = type_data.get(i).getLinguisticTypeName();
		}
		return types;
	}
	
	/**
	 * Returns an array of tier type names for the tier at the specified row.
	 * If all is well and consistent the array has only one element.
	 * 
	 * @param row the row index of the tier
	 * @return an array of type names
	 */
	public synchronized String[] getLinguisticTypeNamesByTier(int row) {
		TierImpl tier = tier_data.get(row);
		List<String> type_names = new ArrayList<String>();
		type_names.add(ElanLocale.getString("MFE.Multiple"));
		boolean onlyTimeAlignable = false;// means only root types
		int stereotype = -1;
		
		if(tier.getLinguisticType().getConstraints()==null) {
			onlyTimeAlignable = true;
		} else {
			stereotype = tier.getLinguisticType().getConstraints().getStereoType();
		}
		if(onlyTimeAlignable) {
			for(int i=0;i<type_data.size();i++) {
				if(type_data.get(i).getConstraints() == null)
					type_names.add(type_data.get(i).getLinguisticTypeName());
			}
		} else if(stereotype >= 0) {
			for(int i=0;i<type_data.size();i++) {
				Constraint cs = type_data.get(i).getConstraints();
				if(cs != null && cs.getStereoType() == stereotype)
					type_names.add(type_data.get(i).getLinguisticTypeName());
			}
		}
		
		String[] filtered_type_names = new String[type_names.size()];
		for(int i=0;i<type_names.size();i++) {
			filtered_type_names[i] = type_names.get(i);
		}
		return filtered_type_names;
	}
	
	/**
	 * Returns an array of strings with all different consistent StereoType names
	 * that are actually used by the LinguisticTypes that are available.
	 * 
	 * The array is a subset of Constraint.publicStereoTypes.
	 * There is some extra complexity because the (public) stereotype numbers are not nicely 0...3.
	 * 
	 * @return An array of strings with all different consistent StereoType names
	 * that are actually used by the available LinguisticTypes
	 */
	public synchronized String[] getConsistentStereoTypeNames() {
		int numPublicStereotypes = Constraint.publicStereoTypes.length;
		int numStereotypes = Constraint.stereoTypes.length;
		int count = 0;
		int[] consistent_types = new int[numStereotypes];

		for (int i = 0; i < numStereotypes; i++) {
			consistent_types[i] = -1;
		}

		// Loop over all types. If we have seen all possible stereotypes,
		// we can quit early.
		for (int i = 0; i < type_data.size() && count < numPublicStereotypes; i++) {
			if (type_consistency.get(i)) {
				LinguisticType type = type_data.get(i);
				if (type.getConstraints() != null) {
					int stereotype = type.getConstraints().getStereoType();
					if (stereotype == Constraint.TIME_SUBDIVISION ||
							stereotype == Constraint.INCLUDED_IN ||
							stereotype == Constraint.SYMBOLIC_SUBDIVISION ||
							stereotype == Constraint.SYMBOLIC_ASSOCIATION) {
						if (consistent_types[stereotype] == -1) {
							consistent_types[stereotype] = stereotype;
							count++;
						}
					}
				}
			}
		}
		
		// Often, we will have all stereotypes; in that case just return the full array.
		if (count == numPublicStereotypes) {
			return Constraint.publicStereoTypes;
		}
		
		String[] types = new String[count];
		int j = 0;
		
		for (int i = 0; i < numStereotypes; i++) {
			String s; 
			switch (consistent_types[i]) {
			case Constraint.TIME_SUBDIVISION:
				s = Constraint.publicStereoTypes[0];
				break;
			case Constraint.INCLUDED_IN:
				s = Constraint.publicStereoTypes[1];
				break;
			case Constraint.SYMBOLIC_SUBDIVISION:
				s = Constraint.publicStereoTypes[2];
				break;
			case Constraint.SYMBOLIC_ASSOCIATION:
				s = Constraint.publicStereoTypes[3];
				break;
			default:
				continue;	// don't add to types[].
			}
			types[j++] = s;
		}
		return types;
	}
	
	/**
	 * Returns an array of consistent tier type names.
	 * 
	 * @return An array of strings with all different consistent LinguisticType names.
	 */
	public synchronized String[] getConsistentLinguisticTypeNames() {
		List<String> consistent_types = new ArrayList<String>();
		for(int i=0;i<type_data.size();i++) {
			if(type_consistency.get(i))
				consistent_types.add(type_data.get(i).getLinguisticTypeName());
		}
		
		String[] types = new String[consistent_types.size()];
		for(int i=0;i<consistent_types.size();i++) {
			types[i] = consistent_types.get(i);
		}
		return types;
	}
	
	/**
	 * Returns an array of names of tier types with a specific stereotype.
	 * 
	 * @param stereotype stereotype of linguistic type names to be returned
	 * 
	 * @return An array of strings with all different consistent LinguisticType
	 * 			names with the given stereotype.
	 */
	public synchronized String[] getConsistentLinguisticTypeNames(int stereotype) {
		List<String> consistent_types = new ArrayList<String>();
		for(int i=0;i<type_data.size();i++) {
			if(type_consistency.get(i))
				if(type_data.get(i).getConstraints() != null &&
						type_data.get(i).getConstraints().getStereoType() == stereotype)
				consistent_types.add(type_data.get(i).getLinguisticTypeName());
		}
		
		String[] types = new String[consistent_types.size()];
		for(int i=0;i<consistent_types.size();i++) {
			types[i] = consistent_types.get(i);
		}
		return types;
	}
	
	/**
	 * Returns the number of columns.
	 * 
	 * @return the number of columns in the type table
	 */
	public synchronized int getTypeColumnCount() {
		return type_headers.size();
	}
	
	/**
	 * Returns the number of rows.
	 * 
	 * @return the number of rows in the type table
	 */
	public synchronized int getTypeRowCount() {
		return type_data.size();
	}
	/**
	 * Returns the name of a column in the type table.
	 * 
	 * @param col the column index
	 * @return the column name
	 */
	public synchronized String getTypeColumnName(int col) {
		return type_headers.get(col);
	}
	/**
	 * Returns a cell value from the type table.
	 * 
	 * @param row the row index
	 * @param col the column index
	 * @return the value
	 */
	public synchronized Object getTypeValueAt(int row, int col) {
		Object val = "";
		if(withinTypeBounds(row, col)) {
			LinguisticType type = type_data.get(row);
			switch(col) {
			case TYPE_NAMECOLUMN:
				val = type.getLinguisticTypeName();
				break;
			case TYPE_STEREO:
				if(type.getConstraints()!=null) {
					//String[] publicStereoTypes = Constraint.publicStereoTypes;
					val = type.getConstraints();
				}
				else
					val = "";
				break;
			case TYPE_DATACATEGORYCOLUMN:
				val = type.getDataCategory();
				val = (val==null)? "":val;
				break;
			case TYPE_TIMEALIGNABLECOLUMN:
				val = type.isTimeAlignable();
				break;
			}
		}
		return val;
	}
	
	/**
	 * Modifies a value in the type table.
	 * 
	 * @param value the new value
	 * @param row the row index
	 * @param col the column index
	 */
	public synchronized void changeTypeValueAt(Object value, int row, int col) {
		LinguisticType type = type_data.get(row);
		switch(col) {
		case TYPE_NAMECOLUMN:
			String name = (String)value;
			if(getLinguisticType(name)==null && name!="") {
				String old_type_name = type_data.get(row).getLinguisticTypeName();
				for(TierImpl tier:tier_data) {
					LinguisticType old_type = tier.getLinguisticType();
					if(old_type!=null) {
						String type_name = old_type.getLinguisticTypeName();
						if(type_name!=null && type_name.equals(old_type_name)) {
							old_type.setLinguisticTypeName((String)value);
						}
					}
				}
				type.setLinguisticTypeName((String)value);
				if(type_changes.get(row)==Changes.NEW || type_changes.get(row)==Changes.NEW_MODIFIED)
					type_changes.set(row, Changes.NEW_MODIFIED);
				else
					type_changes.set(row, Changes.MODIFIED);
			}
			break;
		case TYPE_TIMEALIGNABLECOLUMN:
			// do nothing, not editable
			break;
		}
	}
	
	/**
	 * Removes a row from the type table.
	 * 
	 * @param row the row index
	 */
	public synchronized void removeTypeRow(int row) {
		type_data.remove(row);
	}
	
	private boolean withinTypeBounds(int row, int col) {
		return (row>=0 && row<type_data.size() && type_data.size()>0) &&
				(col>=0 && col<type_headers.size() && type_headers.size()>0);
	}
	
	/**
	 * Returns the modification status of a tier type.
	 * 
	 * @param o_name the original name of a type
	 * @return a {@code Changes} item indicating the change status
	 */
	public synchronized Changes getTypeChangeByOriginalName(String o_name) {
		LinguisticType o_type = null;
		for(LinguisticType _o_type:original_type_data) {
			if(_o_type.getLinguisticTypeName().equals(o_name)) {
				o_type = _o_type;
				break;
			}
		}
		if(o_type!=null) {
			/* name is a type that was in the original data */
			int index = original_type_data.indexOf(o_type);
			return type_changes.get(index);
		} else {
			/* name is either a new type, or is non-existend
			 * either way it's an error. */
			return null;
		}
	}
	
	/**
	 * Returns a type given its original name
	 * @param o_name original name
	 * @return type or null if original name doesn't exist
	 */
	public synchronized LinguisticType getTypeByOriginalName(String o_name) {
		int o_index=-1;
		for(int i=0;i<original_type_data.size();i++) {
			LinguisticType o_type = original_type_data.get(i);
			if(o_type.getLinguisticTypeName().equals(o_name)) {
				o_index=i;
				break;
			}
		}
		if(o_index>=0) {
			int nr_removed_types=0;
			for(int i=0;i<type_changes.size() || i<=o_index;i++)
				nr_removed_types += (type_changes.get(i)==Changes.REMOVED)? 1 : 0;
			o_index -= nr_removed_types;
			return type_data.get(o_index);
		} else {
			return null;
		}
	}
	/**
	 * Returns a tier type, a {@code LinguisticType} instance.
	 * 
	 * @param name LinguisticType name
	 * @return The corresponding LinguisticType or null if it does not exist.
	 */
	public synchronized LinguisticType getLinguisticType(String name) {
		LinguisticType type=null;
		for(LinguisticType t:type_data) {
			if(t.getLinguisticTypeName().equals(name))
				type=t;
		}
		return type;
	}
	/**
	 * This method adds a type to the model. Add types before adding tiers.
	 * <p>
	 * Is called from the EAFLoadThread.
	 * @param o_type the tier type to add
	 * @return number of the row
	 * @throws InconsistentTypeException thrown when a type is added which 
	 * already exists (based on its name) but with different properties 
	 */
	public synchronized int addOriginalType(LinguisticType o_type) throws InconsistentTypeException {
		boolean contains = false;
		LinguisticType existing_type = null;
		for(LinguisticType type:original_type_data) {
			contains |= o_type.getLinguisticTypeName().equals(type.getLinguisticTypeName());
			if(contains) {
				existing_type = type;
				break;
			}
		}
		if(contains) {
			// check type consistency
			int existing_index = original_type_data.indexOf(existing_type);
			String existing_datac = existing_type.getDataCategory();
			String type_datac = o_type.getDataCategory();
			if(!(Objects.equals(existing_datac, type_datac) &&
			   existing_type.isTimeAlignable() == o_type.isTimeAlignable())) {
				type_consistency.set(existing_index, false);
				throw new InconsistentTypeException(o_type);
			} else {
				//type_consistency.set(existing_index, true);
			}
		} else {
			type_consistency.add(true);
			original_type_data.add(o_type);
			LinguisticType type = new LinguisticType(o_type.getLinguisticTypeName());
			type.addConstraint(o_type.getConstraints());
			type.setDataCategory(o_type.getDataCategory());
			type.setTimeAlignable(o_type.isTimeAlignable());
			// HS Aug 2018 must set controlled vocabulary and lexicon bundle because these 
			// fields are part of the equals() implementation
			type.setControlledVocabularyName(o_type.getControlledVocabularyName());
			if (o_type.getLexiconQueryBundle() != null) {
				type.setLexiconQueryBundle(new LexiconQueryBundle2(o_type.getLexiconQueryBundle()));
			}
			type_data.add(type);
			type_changes.add(Changes.NONE);
		}
		// HS Aug 2018 indexOf depends on equals() of LinguisticType
		return type_data.indexOf(o_type); 
	}
	
	/** 
	 * Returns whether a type is consistent.
	 * Consistency depends on types with the same type name having the same 
	 * or different time-alignable flag and/or data category.
	 * 
	 * @param type_row the row index of the type
	 * @return {@code true} if the type is consistent, {@code false} otherwise
	 */
	public synchronized boolean isConsistentType(int type_row) {
		return type_consistency.get(type_row);
	}

	/**
	 * Adds a tier type based on the specified properties.
	 * 
	 * @param name the type name
	 * @param dataCategory a data category
	 * @param timeAlignable the time alignable property, should be consistent
	 * with the {@code constraint} parameter
	 * @param constraint the constraint object for the type
	 * 
	 * @return the index of the row of the new type 
	 */
	public synchronized int addType(String name, String dataCategory, 
			boolean timeAlignable, Constraint constraint) {
		LinguisticType type = new LinguisticType(name);
		type.setDataCategory(dataCategory);
		type.addConstraint(constraint);
		type.setTimeAlignable(timeAlignable);
		type_data.add(type);
		type_changes.add(Changes.NEW);
		type_consistency.add(true);
		return type_data.indexOf(type);
	}

	/**
	 * Cleanup and initialization method, clears lists and maps.
	 */
	public synchronized void clear() {
		type_headers.clear();
		tier_headers.clear();
		
		original_tier_data.clear();
		original_type_data.clear();
		tier_children.clear();
		
		tier_data.clear();
		type_data.clear();
		
		tier_changes.clear();
		type_changes.clear();
		flagged_tiers.clear();
		
		orig_to_changed_tierMap.clear();
		tier_to_changesMap.clear();
		
		tier_type_consistency.clear();
		type_consistency.clear();

		setRemovableTiers(true);
		setLocale();
	}
	
	/**
	 * Sets whether or not tiers can be removed.
	 * 
	 * @param b if {@code true} tiers may be removed
	 */
	public synchronized void setRemovableTiers(boolean b) {
		removable_tiers = b;
	}
	
	/**
	 * Returns whether or not tiers can be removed.
	 * 
	 * @return whether tiers can be removed
	 */
	public synchronized boolean areTiersRemovable() {
		return removable_tiers;
	}
	
	/**
	 * Returns a list of tier type objects.
	 * 
	 * @return the tier types
	 */
	public synchronized List<LinguisticType> getTypes() {
		return type_data;
	}
	
	/**
	 * Returns the modification status of a tier type.
	 * 
	 * @param i the row index of the type 
	 * @return a {@code Changes} item
	 */
	public synchronized Changes getTypeChange(int i) {
		return type_changes.get(i);
	}
	     
	private void setLocale() {
		type_headers.add(ElanLocale.getString("EditTypeDialog.Label.Type"));
		type_headers.add(ElanLocale.getString("MFE.TypeHeader.DataCategory"));
		type_headers.add(ElanLocale.getString("EditTypeDialog.Label.TimeAlignable"));
//		type_headers.add(ElanLocale.getString("EditTypeDialog.Label.Stereotype"));
		
		tier_headers.add(ElanLocale.getString("MFE.TierHeader.Name"));
		tier_headers.add(ElanLocale.getString("MFE.TierHeader.LinguisticType"));
		tier_headers.add(ElanLocale.getString("MFE.TierHeader.Annotator"));
		tier_headers.add(ElanLocale.getString("MFE.TierHeader.Participant"));
		tier_headers.add(ElanLocale.getString("MFE.TierHeader.Language"));
		tier_headers.add(ElanLocale.getString("MFE.TierHeader.Children"));
		tier_headers.add(ElanLocale.getString("MFE.TierHeader.Parents"));
	}
}
