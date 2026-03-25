package mpi.eudico.util;

import mpi.eudico.server.corpora.clom.ExternalReference;

/**
 * A simple, one-language version of a CVEntry.
 * <p>
 * This is useful to put into the InlineEditBox or other GUI elements.
 * At creation time you can choose which language should be used.
 * 
 * @author olasei
 */
public class SimpleCVEntry {
	private CVEntry ref;
	private String value;
	private String description;
	
	/**
	 * Creates a new simple entry instance.
	 * 
	 * @param e the (compound) reference entry
	 * @param langIndex the index of the language to use 
	 */
	public SimpleCVEntry(CVEntry e, int langIndex) {
		ref = e;
		value = e.getValue(langIndex);
		description = e.getDescription(langIndex);
	}

	/**
	 * Returns the entry value.
	 * 
	 * @return the value
	 */
	@Override
	public String toString() {
		return value;
	}

	/**
	 * Returns the entry value.
	 * 
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Returns the description.
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns the {@code id}.
	 *  
	 * @return the id
	 */
	public String getId() {
		return ref.getId();
	}

	/**
	 * Returns the external reference of the entry.
	 * 
	 * @return the external reference
	 */
	public ExternalReference getExternalRef() {
		return ref.getExternalRef();
	}

	/**
	 * Returns the shortcut key code of the entry.
	 * 
	 * @return the shortcut key code
	 */
	public int getShortcutKeyCode() {
		return ref.getShortcutKeyCode();
	}
}
