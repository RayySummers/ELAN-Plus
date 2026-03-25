package mpi.eudico.client.annotator.interlinear.edit;

import static mpi.eudico.client.annotator.util.ClientLogger.LOG;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.server.corpora.lexicon.LexiconQueryBundle2;
import nl.mpi.lexan.analyzers.helpers.Position;
import nl.mpi.lexan.analyzers.helpers.PositionLexicon;
import nl.mpi.lexan.analyzers.helpers.Suggestion;
import nl.mpi.lexan.analyzers.helpers.SuggestionSet;
import nl.mpi.lexan.analyzers.lexicon.LexAtom;
import nl.mpi.lexan.analyzers.lexicon.LexCont;
import nl.mpi.lexan.analyzers.lexicon.LexEntry;
import nl.mpi.lexan.analyzers.lexicon.LexItem;

/**
 * A utility class to enrich a set of suggestions produced by an analyzer which
 * works with a lexicon. The suggestions are usually based on one or two fields
 * of the lexical entries, e.g. for parsing and or glossing, and are through 
 * the configuration of the analyzer targeting one or two tiers.
 * <p>
 * This class tries to infer additional target tiers in the following way:
 * <ol>
 * <li>for each suggestion in the suggestion set, check if it contains a
 * lexical entry and extract the target tier 
 * <li>for each target tier, check if it has sibling tiers (tiers with the same
 * parent as the target)
 * <li>for each sibling tier, check if its tier type is linked to a field in a 
 * lexical entry and check if the tier has a content language set
 * <li>if there is a lexicon link, try to find that entry field in the entry of
 * the suggestion. The entry field might have a language attribute; first try
 * to match it with the tier's content language (if any), if that fails try to
 * match the field ignoring the language
 * <li>if the suggestion has a {@code fieldId} attribute, check the entry for a 
 * container with that id (probably a {@code sense} of the entry) and start
 * matching in that container
 * <li>in case of a match, add a new suggestion to the set
 * <li>for each target tier, check its direct dependent tiers in the same way
 * and apply this routine recursive for both sibling and child tiers
 * </ol>
 * 
 * If a field is present multiple times in the lexical entry, either without a
 * language attribute or with the same attribute, the first one will simple be
 * selected (there is no way to further disambiguate). 
 * 
 * @implNote the {@code SuggestionSet} which is passed to this class is changed
 * by this process; new suggestions are added to the set.
 */
public class SuggestionInference {
	
	/**
	 * Infers additional suggestions based on the targets in the suggestion set,
	 * the configuration of related tiers and the contents of the lexical entry
	 * the suggestions are based on.
	 * <p>
	 * The provided set is changed by this method. If this is not acceptable, a
	 * copy should be used instead.
	 * 
	 * @param transcription the transcription containing the tiers
	 * @param suggestionSet the set of suggestions produced by an analyzer
	 */
	public static void inferAdditionalTargets(Transcription transcription, SuggestionSet suggestionSet) {
		if (transcription == null) {
			if (LOG.isLoggable(Level.INFO)) {
				LOG.log(Level.INFO, "Cannot infer targets, the transcription is null");
			}
			return;
		}
		if (suggestionSet == null) {
			if (LOG.isLoggable(Level.INFO)) {
				LOG.log(Level.INFO, "Cannot infer targets, the suggestion set is null");
			}
			return;
		}
		List<Suggestion> suggestions = suggestionSet.getSuggestions();
		// restrict the loop to the initial items in the set
		int size = suggestions.size();
		for (int i = 0; i < size; i++) {
			Suggestion suggestion = suggestions.get(i);
			List<Suggestion> nextSuggestionList = createInferredSiblingTargets(transcription, suggestion);
			if (nextSuggestionList != null) {
				for (Suggestion s : nextSuggestionList) {
					suggestionSet.add(s);
					inferDescendantTargets(transcription, s);
				}
			}
			
			inferDescendantTargets(transcription, suggestion);
		}
	}
	
	/**
	 * Infers additional sibling and descendant suggestions for the provided
	 * suggestion. This suggestion is already a newly created suggestion, there
	 * shouldn't be a clash with suggestions in the initial set.
	 * New suggestions are added to the input suggestion and are processed
	 * recursively.
	 * 
	 * @param transcription the transcription containing the tiers, not 
	 *        {@code null}
	 * @param parentSuggestion the suggestion to start from, not {@code null}
	 */
	private static void inferDescendantTargets(Transcription transcription, Suggestion parentSuggestion) {
		List<Suggestion> suggestions = parentSuggestion.getChildren();
		// restrict the loop to the initial items in the list
		int size = suggestions.size();
		for (int i = 0; i < size; i++) {
			Suggestion suggestion = suggestions.get(i);
			List<Suggestion> nextSuggestionList = createInferredSiblingTargets(transcription, suggestion);
			if (nextSuggestionList != null) {
				for (Suggestion s : nextSuggestionList) {
					parentSuggestion.addChild(s);
				}
			}
			
			inferDescendantTargets(transcription, suggestion);
			createInferredChildTargets(transcription, suggestion);
		}
	}

	/**
	 * Checks the tiers with the same parent as the tier of the suggestion
	 * passed to this method. Processes suggestions recursively.
	 * 
	 * @param transcription the transcription containing the tiers, not 
	 *        {@code null}
	 * @param suggestion the suggestion to start from, not {@code null}
	 * 
	 * @return a list of new suggestions for sibling tiers, can be {@code null}
	 */
	private static List<Suggestion> createInferredSiblingTargets(Transcription transcription, Suggestion suggestion) {
		LexEntry entry = suggestion.getLexEntry();
		if (entry == null) {
			return null;
		}
		String tierId = suggestion.getPosition().getTierId();
		Tier tier = transcription.getTierWithId(tierId);
		List<Suggestion> inferList = null;
		// process siblings tiers, if there are any
		if (tier.getParentTier() != null) {
			List<? extends Tier> siblingTierList = tier.getParentTier().getChildTiers();
			for (Tier siblingTier : siblingTierList) {
				if (siblingTier == tier) {
					// skip the tier that was the starting point
					continue;
				}
				
				Suggestion nextSuggestion = createInferredForTier(suggestion, siblingTier);
				if (nextSuggestion != null) {
					if (inferList == null) {
						inferList = new ArrayList<Suggestion>(5);
					}
					inferList.add(nextSuggestion);
					createInferredChildTargets(transcription, nextSuggestion);
				}
			}
		}

		return inferList;
	}
	
	/**
	 * Method for recursive processing of newly created, inferred suggestions.
	 * Any new suggestions are added to the provided parent suggestion and
	 * processed recursively.
	 * 
	 * @param transcription the transcription containing the tiers, not 
	 *        {@code null}
	 * @param parentSuggestion a newly created suggestion, not {@code null}
	 */
	private static void createInferredChildTargets(Transcription transcription, Suggestion parentSuggestion) {
		Tier parentTier = transcription.getTierWithId(parentSuggestion.getPosition().getTierId());
		if (parentTier == null) {
			return;
		}
		List<? extends Tier> childTiers = parentTier.getChildTiers();
		for (Tier childTier : childTiers) {
			Suggestion nextSuggestion = createInferredForTier(parentSuggestion, childTier);
			if (nextSuggestion != null) {
				parentSuggestion.addChild(nextSuggestion);
				createInferredChildTargets(transcription, nextSuggestion);
			}
		}
	}
	
	/**
	 * Checks if the provided tier has a link to a lexical entry field and 
	 * tries to find the best match in the entry provided by the suggestion,
	 * taking into account the {@code fieldId} of the suggestion, if available,
	 * and the content language of the tiers, if set.
	 *    
	 * 
	 * @param fromSuggestion the suggestion containing the entry to query, not
	 *        {@code null}
	 * @param forTier the target tier for a new suggestion, not {@code null}
	 *  
	 * @return the newly created suggestion, or {@code null}
	 */
	private static Suggestion createInferredForTier(Suggestion fromSuggestion, Tier forTier) {
		LexEntry entry = fromSuggestion.getLexEntry();
		if (entry == null) {
			return null;
		}
		String fieldId = fromSuggestion.getFieldId();// either entry id, sense id or null
		LinguisticType tierType = forTier.getLinguisticType();
		String tierLanguage = forTier.getLangRef();
		// the query bundle, if not null, contains the name of the lexicon and of the entry field
		LexiconQueryBundle2 lexBundle = tierType.getLexiconQueryBundle();
		if (lexBundle != null) {
			// assume the same lexicon
			String field = lexBundle.getFldId().getName();

			if (field != null) {
				String value = null;
				// a field name with a language attribute, e.g. 'sense/definition/en'
				String fieldLang = field;
				if (tierLanguage != null && !tierLanguage.isBlank()) {
					// the format in place of a field with a language attribute
					fieldLang = field + "/" + tierLanguage;
				}
				// unlikely case
				if (fieldLang.equals(entry.getMainTypeName()) || field.equals(entry.getMainTypeName())) {
					value = fromSuggestion.getContent();
				}
				// if the field id is not null and equals the id of one of the lexical containers 
				// (e.g. a sense container) check that container first
				LexCont startContainer = null;
				if (fieldId != null) {
					containerloop:
					for (LexItem item : entry.getLexItems()) {
						if (item instanceof LexCont container) {
							for (LexItem item2 : container.getLexItems()) {
								if (item2 instanceof LexAtom atom) {
									if (fieldId.equals(atom.getLexValue())) {
										startContainer = container;
										break containerloop;
									}
								}
							}
						}
					}
				}
				
				// if found, start the search in this container (probably 'sense') 
				// before anything else 
				if (startContainer != null) {
					// test the more specific field first (with language)
					for (LexItem item2 : startContainer.getLexItems()) {
						if (item2 instanceof LexAtom atom2) {
							if (fieldLang.equals(item2.getType())) {
								value = atom2.getLexValue();
								break;
							}
						} // ignore deeper level containers
					}
					// test the generic field name next
					if (value == null && !fieldLang.equals(field)) {
						for (LexItem item2 : startContainer.getLexItems()) {
							if (item2 instanceof LexAtom atom2) {
								if (field.equals(item2.getType())) {
									value = atom2.getLexValue();
									break;
								}
							} // ignore deeper level containers
						}
					}
				}
				
				// if a value was not found, iterate over all items, matching
				// the more specific field name (i.e. including language) first
				if (value == null) {
					firstitemloop:
					for (LexItem item : entry.getLexItems()) {
						if (item instanceof LexAtom atom) {
							if (fieldLang.equals(item.getType())) {
								value = atom.getLexValue();
								break;
							}
						} else if (item instanceof LexCont container) {
							boolean correctSenseLevel = false;
							for(LexItem item2 : container.getLexItems()) {
								if (item2 instanceof LexAtom atom2) {
									if(atom2.getLexValue().equals(fromSuggestion.getContent())) {
										correctSenseLevel = true;
									}
								}
							}
							// check items in a 'sense' container
							if(correctSenseLevel) {
								for (LexItem item2 : container.getLexItems()) {
									if (item2 instanceof LexAtom atom2) {
											if (fieldLang.equals(item2.getType())) {
												value = atom2.getLexValue();
												break firstitemloop;
											}
									} // ignore deeper level containers
								}
							}
						}
					}
				}
				
				// if a value was not found, iterate again, matching the more
				// generic field name
				if (value == null && !fieldLang.equals(field)) {
					seconditemloop:
					for (LexItem item : entry.getLexItems()) {
						if (item instanceof LexAtom atom) {
							if (field.equals(item.getType())) {
								value = atom.getLexValue();
								break;
							}
						} else if (item instanceof LexCont container) {
							// check items in a 'sense' container
							for (LexItem item2 : container.getLexItems()) {
								if (item2 instanceof LexAtom atom2) {
									if (field.equals(item2.getType())) {
										value = atom2.getLexValue();
										break seconditemloop;
									}
								} // ignore deeper level containers
							}
						}
					}
				}

				// if a value was found create a Suggestion and return it
				if (value != null) {
					Suggestion nextSuggestion = new Suggestion(value, new Position(forTier.getName(), 
							fromSuggestion.getPosition()));
					nextSuggestion.setLexEntry(entry);
					nextSuggestion.setFieldId(fromSuggestion.getFieldId());
					return nextSuggestion;
				}
			}
		}
		
		return null;
	}

	/**
	 * Checks dependent tiers of a source tier, recursively, to see if additional
	 * target mappings can be determined automatically based on references of 
	 * tier types to lexical entry fields.
	 * <p>
	 * The provided list of target positions will be changed by this method, i.e.
	 * the detected targets are added to this list. 
	 *  
	 * @param parentTier starting point, not {@code null}, the child tiers will
	 *        be checked recursively
	 * @param targetPosList the list of already established mappings, 
	 *        not {@code null}
	 */
	public static void detectAdditionalTargets(Tier parentTier, List<Position> targetPosList) {
		if (parentTier == null) {
			if (LOG.isLoggable(Level.INFO)) {
				LOG.log(Level.INFO, "Cannot detect additional targets, the tier is null");
			}
			return;
		}
		if (targetPosList == null) {
			if (LOG.isLoggable(Level.INFO)) {
				LOG.log(Level.INFO, "Cannot detect additional targets, the targets list is null");
			}
			return;
		}
		for (Tier tier : parentTier.getChildTiers()) {
			boolean alreadyMapped = false;
			for (Position pos : targetPosList) {
				if (tier.getName().equals(pos.getTierId())) {
					// already mapped as a target
					alreadyMapped = true;
					break;
				}
			}
			
			if (!alreadyMapped) {
				LinguisticType tierType = tier.getLinguisticType();
				// the query bundle, if not null, contains the name of the lexicon and of the entry field
				LexiconQueryBundle2 lexBundle = tierType.getLexiconQueryBundle();
				if (lexBundle != null && lexBundle.getLink() != null && 
						lexBundle.getLink().getLexId() != null) {
					String lexiconName = lexBundle.getLink().getLexId().getName();
					String field = lexBundle.getFldId().getName();
	
					if (lexiconName != null && field != null) {
						targetPosList.add(new PositionLexicon(tier.getName(), lexiconName, 
								field, tier.getLangRef()));
					}
				}
				
			}
			// check child tiers
			detectAdditionalTargets(tier, targetPosList);
		}
	}
}
