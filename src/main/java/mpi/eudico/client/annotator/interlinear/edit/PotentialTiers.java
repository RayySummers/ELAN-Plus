package mpi.eudico.client.annotator.interlinear.edit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import static mpi.eudico.client.annotator.util.ClientLogger.LOG;
import nl.mpi.lexan.analyzers.helpers.TierNodeType;
import nl.mpi.lexan.analyzers.helpers.parameters.Parameter;
import nl.mpi.lexan.analyzers.helpers.parameters.TierTypeParameter;

/**
 * Interprets TierTypeParameters, in particular the constraints it prefers
 * on its source and target tiers.
 * 
 * @author olasei
 */
public class PotentialTiers {
	private List<Parameter> parameters;
	/** Usually assumed to be 1 */
	private int nrSourceTiers;
	/** Should be 1 or more */
	private int nrTargetTiers;
	
	private ArrayList<String> fixedTierNames;
	private Map<String, List<String>> typesChildMap;
	/** SourceName => targetList: map tiers to their direct child tiers */
	private Map<String, List<String>> tiersChildMap;
	/** Maps type names to the tiers of that type */
	private Map<String, List<String>> typesToTiersMap;
	/** Type mode is now assumed, is now the default mode */
	private boolean isTypeMode;
	
	/**
	 * Creates a new potential tiers instance.
	 * 
	 * @param parameters the parameters provided by an analyzer
	 */
	public PotentialTiers(List<Parameter> parameters) {
		/*
		 * Several analyzers depend on defaulted parameters:
		 * a single source and a single target.
		 */
		if (parameters == null || parameters.isEmpty()) {
			final TierTypeParameter source = new TierTypeParameter("Default source", TierNodeType.SOURCE,
					TierTypeParameter.NO_CONSTRAINT, -1);
			final TierTypeParameter target = new TierTypeParameter("Default target", TierNodeType.TARGET,
					TierTypeParameter.DIRECT_CHILD_OF_PARAMETER, 0);
			
			parameters = new ArrayList<Parameter>(2);
			parameters.add(source);
			parameters.add(target);
		}
		
		this.parameters = parameters;
		
		int size = parameters.size();
		//@SuppressWarnings("unused")
		//int nrOtherParameters = 0;
		
		/*
		 * Validate the Parameters.
		 */
		for (int n = 0; n < size; n++) {
			final Object type = parameters.get(n).getValue();
			if (type == TierNodeType.TARGET) {
				nrTargetTiers++;
			} else if (type == TierNodeType.SOURCE) {
				nrSourceTiers++;
			} else {
				//nrOtherParameters++; // current analyzers won't make this happen.
			}
		}
		
		/*
		 * This case is not actually allowed.
		 * Generate a default target tier parameter.
		 */
		if (nrTargetTiers == 0) {
			nrTargetTiers = 1;
			size++;
			final TierTypeParameter target = new TierTypeParameter("Default target", TierNodeType.TARGET,
					TierTypeParameter.DIRECT_CHILD_OF_PARAMETER, 0);
			parameters.add(target);
		}
		
		fixedTierNames = new ArrayList<String>(size);
		for (int i = 0; i < size; i++) {
			fixedTierNames.add("");
		}
	}

	/**
	 * Returns the number of  source tiers.
	 * 
	 * @return the number of source tiers the analyzer expects 
	 * (usually one)
	 */
	public int getNumberOfSourceTiers() {
		return nrSourceTiers;
	}
	
	/**
	 * Returns the number of target tiers.
	 * 
	 * @return the number of target tiers the analyzer supports, usually one or two
	 */
	public int getNumberOfTargetTiers() {
		return nrTargetTiers;
	}
	
	/**
	 * The user has chosen this tier in this parameter position.
	 * From now on, this information is available to determine the potential
	 * tiers for the next position.
	 * <p>
	 * If a parameter's possible values depend on the value of some other
	 * parameter, that one should be chosen/fixed first.
	 * The GUI enforces this by making the user choose strictly from left to right
	 * (which is slightly too strict, but that doesn't matter).
	 * 
	 * @param index the of the parameter
	 * @param tier the name of the type or tier
	 */
	public void setTierName(int index, String tier) {
		fixedTierNames.set(index, tier);
		
		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine(String.format("setTierName: %d := '%s': %s",
					index, tier, fixedTierNames.toString()));
		}
		
		// Clear the parameters that depend on this.
		// The GUI should take advantage of this at some point.
		final int size = fixedTierNames.size();
		for (int i = index + 1; i < size; i++) {
			Parameter p = parameters.get(i);
			if (p instanceof TierTypeParameter) {
				TierTypeParameter ttp = (TierTypeParameter) p;
				
				if (ttp.getRelativeToParameter() == index) {
					setTierName(i, "");
				}
			}
		}
	}

	/**
	 * Tell us the children of all tiers (or types).
	 * The key "" maps to all top-level tiers/types.
	 * 
	 * @param typesChildMap the map of type names and types for which there 
	 *   are potential tier combinations
	 * @param tiersChildMap the map of tier names to names of direct child tiers
	 * @param typesToTiersMap the mapping from type names to the tiers using 
	 * that type 
	 * @param isTypeMode if true the primary selection is based on the 
	 * {@code #typeChildMap}, otherwise it is based on the tier map
	 */
	public void setMaps(Map<String, List<String>> typesChildMap, Map<String, List<String>> tiersChildMap,
			Map<String, List<String>> typesToTiersMap, boolean isTypeMode) {
		this.typesChildMap = typesChildMap;
		this.tiersChildMap = tiersChildMap;
		this.typesToTiersMap = typesToTiersMap;
		this.isTypeMode = isTypeMode;
	}

	/**
	 * Get a list of target tier names that the user may choose from.
	 * Impossible options are avoided, as are tier names that have been used in
	 * this configuration already.
	 * <p>
	 * Checks if the parameter is of type TierNodeType.SOURCE.
	 * 
	 * @param index The index of the Parameter (in the list of parameters)
	 * @param exclude A list of source tier names as used in other configurations.
	 *        These are excluded as well.
	 *      
	 * @return a list of candidate tiers
	 * @see #getPotentialSourceTierNames(int, List) 
	 */
	public List<String> getPotentialSourceNames(int index, List<String> exclude) {
		Parameter p = parameters.get(index);
		
		if (p.getValue() == TierNodeType.SOURCE) {
			return getPotentialNames(index, exclude);
		}
		
		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine(String.format("getPotentialSourceNames: parameter %d: EMPTY because not SOURCE",
					index));
		}
		
		return Collections.emptyList();
	}
	
	/**
	 * Variant of {@link #getPotentialSourceNames(int, List)} based on the assumption that
	 * we are in tier type mode but want a list of potential tiers (instead of tier types).
	 * 
	 * @param index The index of the Parameter (in the list of parameters)
	 * @param exclude A list of source tier names as used in other configurations.
	 *        These are excluded as well.
	 * @return a list of tier names, maybe empty if no potential tiers are found
	 * 
	 * @see PotentialTiers#getPotentialSourceNames(int, List)
	 */
	public List<String> getPotentialSourceTierNames(int index, List<String> exclude) {
		if (!isTypeMode) {
			return getPotentialSourceNames(index, exclude);
		}
		
		List<String> srcTypes = getPotentialSourceNames(index, null);
		
		if (!srcTypes.isEmpty()) {
			List<String> tierList = new ArrayList<String>();
			
			for (String tn : srcTypes) {
				List<String> tiersOfType = typesToTiersMap.get(tn);
				if (tiersOfType != null && !tiersOfType.isEmpty()) {
					tierList.addAll(tiersOfType);
				}
			}
			
			return tierList;
		}
		
		return Collections.emptyList();
	}
	
	/**
	 * Get a list of target type or tier names that the user may choose from.
	 * Impossible options are avoided, as are tier names that have been used in
	 * this configuration already.
	 * <p>
	 * Checks if the parameter is of type TierNodeType.TARGET.
	 * 
	 * @param index The index of the Parameter (in the list of parameters)
	 * @return a list of type or tier names
	 * 
	 * @see #getPotentialTargetTierNames(int)
	 */
	public List<String> getPotentialTargetNames(int index) {
		Parameter p = parameters.get(index);
		
		if (p.getValue() == TierNodeType.TARGET) {
			return getPotentialNames(index, null);
		}
		
		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine(String.format("getPotentialTargetNames: parameter %d: EMPTY because not TARGET",
					index));
		}
		
		return Collections.emptyList();
	}
	
	/**
	 * Get a list of target tier names that the user may choose from.
	 * Impossible options are avoided, as are tier names that have been used in
	 * this configuration already.
	 * <p>
	 * Checks if the parameter is of type TierNodeType.TARGET.
	 * <p>
	 * In tier mode this calls {@link #getPotentialTargetNames(int)}, in type mode
	 * this translates a list of type names into a list of tier names
	 * 
	 * @param index The index of the Parameter (in the list of parameters)
	 * 
	 * @return a list of tier names, can be empty
	 * 
	 * @see #getPotentialTargetNames(int)
	 */
	public List<String> getPotentialTargetTierNames(int index) {
		if (!isTypeMode) {
			return getPotentialTargetNames(index);
		}
		
		List<String> types = getPotentialTargetNames(index);
		if (!types.isEmpty()) {
			List<String> tierList = new ArrayList<String>();
			
			for (String tn : types) {
				List<String> tiersOfType = typesToTiersMap.get(tn);
				if (tiersOfType != null && !tiersOfType.isEmpty()) {
					tierList.addAll(tiersOfType);
				}
			}
			
			return tierList;
		}
		
		return Collections.emptyList();
	}
	
	/**
	 * Returns a list of names for the current mode, tier or type.
	 * 
	 * @return a list of all names for the current mode, or null
	 */
	public List<String> getAllNames() {
		if (isTypeMode) {
			if (typesChildMap != null) {
				return typesChildMap.get("");
			} 
		} else {
			if (tiersChildMap != null) {
				return tiersChildMap.get("");
			}
		}
		
		return Collections.emptyList();
	}
	
	private Map<String, List<String>> getCurrentChildMap() {
		if (!isTypeMode) {
			return tiersChildMap;
		}
		return typesChildMap;
	}
	
	/**
	 * Generalised version of getPotentialSourceNames() and getPotentialTargetNames().
	 * It doesn't check the Parameter type.
	 * 
	 * @param index the index of the parameter in the list of parameters
	 * @param exclude a list of names to exclude from the returned list
	 * 
	 * @return a list of candidates
	 */
	private List<String> getPotentialNames(int index, List<String> exclude) {
		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine(String.format("getPotentialNames: parameter %d exclude %s",
					index, String.valueOf(exclude)));
		}
		
		Set<String> set = getPotentialNames(index);

		if (!isTypeMode) {	
			// From the set of potential target tiers,
			// remove the ones that have been used already.
			// When we're in type mode, we can't be so strict:
			// there can be many tiers of the given type.
			set.removeAll(fixedTierNames);
		}
		
		if (!isTypeMode && exclude != null) {
			if (LOG.isLoggable(Level.FINE)) {
				LOG.fine(String.format("getPotentialNames: removeAll %s from %s",
					exclude.toString(), set.toString()));
			}
			set.removeAll(exclude);
		}
		
		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine(String.format("getPotentialNames: parameter %d: %s",
					index, set.toString()));
		}
		
		return new ArrayList<String>(set);
	}

	/**
	 * Implements part of getPotentialNames(int, List) without the removal of some names.
	 * Just generates candidate names that are possible (taking the next Parameters into account).
	 * 
	 * @param index the index of the parameter in the list of parameters
	 * @return a set of names of potential types or tiers
	 */
	private Set<String> getPotentialNames(int index) {
		Set<String> set = new HashSet<String>();

		Parameter p = parameters.get(index);
		
		if (p instanceof TierTypeParameter) {
			TierTypeParameter ttp = (TierTypeParameter)p;
			
			int constraint = ttp.getConstraint();
			int parm = ttp.getRelativeToParameter();
			
			// Parameters cannot depend on something to their right.
			if (parm >= index) {
				constraint = TierTypeParameter.NO_CONSTRAINT;
			}
			Map<String, List<String>> currentChildMap = getCurrentChildMap();
			
			switch (constraint) {
			case TierTypeParameter.NO_CONSTRAINT:
			default:
				addAllPossible(index, set, currentChildMap.get(""));
				break;
				
			case TierTypeParameter.DIRECT_CHILD_OF_PARAMETER:
				String tierName = fixedTierNames.get(parm);
				
				if (tierName != null) {
					addAllPossible(index, set, currentChildMap.get(tierName));
				}
				break;
				
			case TierTypeParameter.ANY_CHILD_OF_PARAMETER:
				tierName = fixedTierNames.get(parm);
				
				if (tierName != null) {
					addAllPossible(index, set, indirectChildren(currentChildMap, tierName));
				}
				break;
			case TierTypeParameter.SIBLING_OR_CHILD_OF_PARAMETER:
				tierName = fixedTierNames.get(parm);
				
				if (tierName != null) {
					addSiblings(index, set, currentChildMap, tierName);
					addAllPossible(index, set, indirectChildren(currentChildMap, tierName));
				}
				break;
			}
		}
	
		return set;
	}

	/**
	 * Create a list of all direct and indirect child tiers, given a map of direct children.
	 */
	private List<String> indirectChildren(Map<String, List<String>> typeChildMap, String tierName) {
		Set<String> accumulator = new HashSet<String>();
		
		accumulateChildren(accumulator, typeChildMap, tierName);
		
		return new ArrayList<String>(accumulator);
	}

	/**
	 * Recursively adds descendants to a set of names
	 * 
	 * @param accumulator the set to add to
	 * @param typeChildMap the map to use for the iterations (type or tiers)
	 * @param tierName the name of the type or tier to start with
	 */
	private void accumulateChildren(Set<String> accumulator,
			Map<String, List<String>> typeChildMap, String tierName) {
		
		List<String> directChildren = typeChildMap.get(tierName);
		
		if (directChildren != null) {
			for (String child : directChildren) {
				if (accumulator.add(child)) {
					accumulateChildren(accumulator, typeChildMap, child);
				}
			}
		}
	}

	/**
	 * Add all possible tier names to the potential source or target names.
	 * <p>
	 * Tier names are possible if choosing them doesn't end up with a dead end,
	 * i.e. no possible choices for any parameters that depend on this one.
	 * 
	 * @param index the index of the parameter
	 * @param set the set to (potentially) add to
	 * @param candidates the tier names to (potentially) add
	 */
	private void addAllPossible(int index, Set<String> set,
			final List<String> candidates) {
		if (candidates != null) {
			for (String c : candidates) {
				if (isPossible(index, c)) {
					set.add(c);
				}
			}
		}
	}
	
	/**
	 * Adds the sibling tiers or types of the tier(type) by moving up in the 
	 * hierarchy (if possible) and then perform the same action of
	 * {@link #addAllPossible(int, Set, List)} to the sibling items. 
	 * 
	 * @param index the index of the parameter
	 * @param set the set to add to
	 * @param typeChildMap the parent to child list mapping
	 * @param tierName the tier or type name which is the starting point
	 */
	private void addSiblings(int index, Set<String> set, Map<String, 
			List<String>> typeChildMap, String tierName) {
		// find the child list in the child map containing tier name
		for (Map.Entry<String, List<String>> entry : typeChildMap.entrySet()) {
			if (entry.getKey().equals("")) {
				// skip the "all" key
				continue;
			}
			List<String> chList = entry.getValue();
			if (chList.contains(tierName)) {
				List<String> copyList = new ArrayList<String>(chList);
				copyList.remove(tierName);
				addAllPossible(index, set, copyList);
				// found
				break;
			}
		}
	}
	
	/**
	 * Check if this candidate tier name is possible for this index'th Parameter:
	 * this means that if other parameter choices depend on this one,
	 * there should be at least one possibility for each of them.
	 * <p>
	 * Since parameters are only allowed to depend on lower-numbered ones,
	 * we only need to search to the right.
	 * <p>
	 * This function has two recursions:
	 * <ul>
	 * <li>it calls getPotentialNames() on Parameters that depend on this candidate value.
	 * <li>it also calls isPossible/3 to check if any more Parameters relate to this same
	 *     position and if the candidates from the previous step are possible.
	 * </ul>
	 * 
	 * @param position it's about the position'th Parameter
	 * @param candidate and the candidate value for it
	 */
	private boolean isPossible(int position, String candidate) {
		boolean possible = true;	// start optimistic; require ALL
		String backup = fixedTierNames.get(position);

		fixedTierNames.set(position, candidate);
		
		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine(String.format("isPossible: %d '%s'?",
					position, candidate));
		}
		
		final int size = fixedTierNames.size();
		for (int i = position + 1; possible && i < size; i++) {
			Parameter p = parameters.get(i);
			if (p instanceof TierTypeParameter) {
				TierTypeParameter ttp = (TierTypeParameter) p;
				
				if (ttp.getRelativeToParameter() == position) {
					if (LOG.isLoggable(Level.FINE)) {
						LOG.fine(String.format("isPossible: %d '%s': %d relates to it",
								position, candidate, i));
					}
					// Ok, found a parameter that relates to the one we're examining.
					// Check if it has possibilities. For each of those relating
					// parameters it must be possible.
					List<String> options = getPotentialNames(i, null); // recurses over related-to's
					if (options.isEmpty()) {
						possible = false;
					} else {
						// Here, we should try to try out each of them and
						// see if there are other possibilities for any
						// further parameters that depend on the same parameter
						// as this one.
						possible = isPossible(position, i, options);
						break;
					}
				}
			}
		}
		
		fixedTierNames.set(position, backup);
		
		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine(String.format("isPossible: %d '%s'? %s!",
					position, candidate, String.valueOf(possible)));
		}
		
		return possible;
	}
	
	/**
	 * This is (almost?) a more general version of isPossible(int position, String candidate)
	 * except the recursion is different, and the other is an 'ForAll' and this is 'Exists'.
	 * <p>
	 * It recurses on relating Parameters via getPotentialNames().
	 * 
	 * @param relates_to
	 * @param position
	 * @param candidates
	 * @return
	 */
	private boolean isPossible(int relates_to, int position, List<String> candidates) {
		final int size = fixedTierNames.size();

		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine(String.format("isPossible: relates_to=%d position=%d %s?",
					relates_to, position, candidates.toString()));
		}
		
		if (position >= size - 1) {
			if (LOG.isLoggable(Level.FINE)) {
				LOG.fine(String.format("isPossible: relates_to=%d position=%d %s: TRUE because there are no more parameters",
						relates_to, position, candidates.toString()));
			}
			
			return true;
		}
		
		boolean possible = true;	// start optimistic: maybe there are no relating Parameters
		String backup = fixedTierNames.get(position);

		/**
		 * Find another parameter that relates to the given one.
		 * Just one is enough. If there are none, that's no problem.
		 */
		for (int i = position + 1; i < size; i++) {
			Parameter p = parameters.get(i);
			if (p instanceof TierTypeParameter) {
				TierTypeParameter ttp = (TierTypeParameter) p;
				
				if (ttp.getRelativeToParameter() == relates_to) {
					possible = false;	// now get pessimistic: but only one candidate needs to be possible
					if (LOG.isLoggable(Level.FINE)) {
						LOG.fine(String.format("isPossible/3: %d '%s': %d relates to it",
								relates_to, String.valueOf(candidates), i));
					}
					// Ok, found a parameter that depends on the one we're examining.
					// Check if it has possibilities for just one of our candidates.
					
					for (String candidate : candidates) {
						fixedTierNames.set(position, candidate);
						
						if (LOG.isLoggable(Level.FINE)) {
							LOG.fine(String.format("isPossible/3: relates_to=%d position=%d %s",
									relates_to, position, candidate));
						}
						List<String> options = getPotentialNames(i, null); // recurses over related-to's

						if (!options.isEmpty()) {
							possible = true;
							break;
						}
					}
					break; // Just need to check one relating Parameter; the rest are reached via recursion.
				}
			}
		}
		
		fixedTierNames.set(position, backup);
		
		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine(String.format("isPossible/3: relates_to=%d position=%d %s? %s!",
					relates_to, position, candidates.toString(), String.valueOf(possible)));
		}
		
		return possible;
		
	}
}
