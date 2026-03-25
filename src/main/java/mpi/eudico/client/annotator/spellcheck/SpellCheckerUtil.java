package mpi.eudico.client.annotator.spellcheck;

import java.util.List;

import mpi.eudico.util.Pair;

/**
 * Utility class for spell checkers.
 * 
 */
public class SpellCheckerUtil {
	
	/**
	 * Private constructor.
	 */
	private SpellCheckerUtil() {
		super();
	}

	/**
	 * Serializes spelling suggestions to a string.
	 * 
	 * @param allSuggestions the suggestion to serialize
	 * @return the result string
	 */
	public static final String serializeSuggestions(List<Pair<String, List<String>>> allSuggestions) {
		StringBuilder stringBldr = new StringBuilder();
		for(Pair<String, List<String>> suggestions : allSuggestions) {
			stringBldr.append(suggestions.getFirst() + "\n");
			for(String suggestion : suggestions.getSecond()) {
				stringBldr.append("  " + suggestion + "\n");
			}
		}
		return stringBldr.toString();
	}
	
	/**
	 * Determines whether a suggestion's data structure actually contains 
	 * suggestions for one or more words. 
	 * 
	 * @param allSuggestions the suggestions to check
	 * @return {@code true} if at least one suggestion has been found in the 
	 * list, {@code false} if none were found
	 */
	public static final Boolean hasSuggestions(List<Pair<String, List<String>>> allSuggestions) {
		for(Pair<String, List<String>> suggestions : allSuggestions) {
			if(!suggestions.getSecond().isEmpty()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks whether the value part of the {@code Pair} is not empty.
	 *   
	 * @param suggestions the suggestions or a single input word
	 * @return {@code true} if there is at least one suggestion in the list 
	 */
	public static final Boolean isSuggestion(Pair<String, List<String>> suggestions) {
		if(!suggestions.getSecond().isEmpty()) {
			return true;
		}
		return false;
	}
} 
