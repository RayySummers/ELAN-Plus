package mpi.eudico.util.multilangcv;

/**
 * Interface for classes which wish to be kept informed if a new language
 * appears in the Recent Languages list.
 * 
 * @author olasei
 */
public interface RecentLanguageListener {
	/**
	 * Notification of addition of a new recent language.
	 * @param index the index in the list
	 * @param langInfo the language information object
	 */
	public void recentLanguageAdded(int index, LangInfo langInfo);
	/**
	 * Notification of change of a recent language entry.
	 * If langInfo is null, the entry at the given position has been removed.
	 * 
	 * @param index the index in the list
	 * @param langInfo the language information object or {@code null}
	 */
	public void recentLanguageChanged(int index, LangInfo langInfo);
}
