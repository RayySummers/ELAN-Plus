/**
 *
 */
package mpi.eudico.client.annotator.interlinear.edit.event;

/**
 * An interface for listeners to SuggestionSet events.
 * A SuggestionSet typically has to be presented to the user as a list of choices.
 *
 * @author Han Sloetjes
 */
public interface SuggestionSetListener {
	/**
	 * Notification that a suggestion set has been delivered.
	 *
	 * @param event the suggestion set event
	 */
	public void suggestionSetDelivered(SuggestionSetEvent event);

	/**
	 * Cancels the suggestion set.
	 */
	public void cancelSuggestionSet();
}
