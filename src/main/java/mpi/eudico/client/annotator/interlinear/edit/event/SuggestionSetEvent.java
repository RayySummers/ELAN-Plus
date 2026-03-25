package mpi.eudico.client.annotator.interlinear.edit.event;

import java.util.EventObject;
import java.util.List;
import nl.mpi.lexan.analyzers.helpers.SuggestionSet;

/**
 * An event object to transfer incoming suggestion sets to the user interface.
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class SuggestionSetEvent extends EventObject {
	private List<SuggestionSet> suggestions;
	private int recursionLevel;

	/**
	 * Constructor.
	 *
	 * @param source the event source
	 * @param suggestions a list of suggestion sets
	 * @param recursionLevel the recursion level
	 */
	public SuggestionSetEvent(Object source, List<SuggestionSet> suggestions, int recursionLevel) {
		super(source);

		this.suggestions = suggestions;
		this.recursionLevel = recursionLevel;
	}

	/**
	 * Returns the list containing the suggestion sets.
	 * Each SuggestionSet represents an alternative, for the user to choose from.
	 *
	 * @return the list of suggestion sets
	 */
	public List<SuggestionSet> getSuggestionSets() {
		return suggestions;
	}

	/**
	 * Returns the recursion level.
	 *
	 * @return the recursion level
	 */
	public int getRecursionLevel() {
		return recursionLevel;
	}
}
