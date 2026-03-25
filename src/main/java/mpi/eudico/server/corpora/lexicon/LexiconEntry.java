package mpi.eudico.server.corpora.lexicon;

import java.util.ArrayList;
import java.util.Iterator;


/**
 * Represents an entry in a Lexicon structure
 * @author Micha Hulsbosch
 *
 */
public class LexiconEntry  extends EntryElement {
	private String id;
	private String fieldOfFocus;
	private ArrayList<String> focusFieldValues;

	/**
	 * Creates an entry instance with the specified name.
	 * 
	 * @param name the name of the entry
	 */
	public LexiconEntry(String name) {
		super(name, null);
		id = null;
		fieldOfFocus = null;
		focusFieldValues = new ArrayList<String>();
	}
	
	/**
	 * Returns whether this entry is a field or leaf node.
	 * 
	 * @return {@code true} if this entry is a field or leaf, {@code false}
	 * otherwise
	 */
	@Override
	public boolean isField() {
		return false;
	}

	/**
	 * Sets whether this entry is a field or leaf node.
	 * 
	 * @param isField if {@code true} this entry is a field
	 */
	@Override
	public void setField(boolean isField) {
		this.isField = false;
	}
	
	@Override
	public String getName() {
		return "Entry";
	}

	/**
	 * Sets the id of the entry.
	 * 
	 * @param id the id of the entry
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Returns the id of the entry.
	 * 
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	
	@Override
	public String toString() {
		String str = "<html><b>" + getName() + "</b> (" + fieldOfFocus + ": ";
		Iterator iter = focusFieldValues.iterator();
		StringBuilder buffer = new StringBuilder();
		if (iter.hasNext()) {
		    buffer.append("<i>" + iter.next() + "</i>");
		    while (iter.hasNext()) {
			buffer.append(" or ");
			buffer.append("<i>" + iter.next() + "</i>");
		    }
		}
		str += buffer.toString();
		str += ")</html>";
		return str;
	}

	/**
	 * Marks a field as having the focus.
	 * 
	 * @param fieldOfFocus the field to mark as focused
	 */
	public void setFieldOfFocus(String fieldOfFocus) {
		this.fieldOfFocus = fieldOfFocus;
	}

	/**
	 * Returns the field having the focus.
	 * 
	 * @return the field having the focus 
	 */
	public String getFieldOfFocus() {
		return fieldOfFocus;
	}

	/**
	 * Adds a value to the list of focus field values.
	 * 
	 * @param value the value to add
	 */
	public void addFocusFieldValue(String value) {
		focusFieldValues.add(value);
	}

	/**
	 * Returns the list of focus field values.
	 * 
	 * @return the list of focus field values
	 */
	public ArrayList<String> getFocusFieldValues() {
		return focusFieldValues;
	}
}
