package mpi.eudico.server.corpora.lexicon;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * Represents an element in a Lexicon structure
 * @author Micha Hulsbosch
 *
 */
public class EntryElement {
	/** flag to indicate if this entry element represents a field (leaf) element */
	protected boolean isField;
	/** the name of the entry */
	protected  String name;
	/** the textual value of the entry */
	protected  String value;
	/** a list of sub- or child-elements */
	protected  List<EntryElement> elements;
	/** the parent element */
	protected  EntryElement parent;
	
	/**
	 * Creates a new entry element.
	 * 
	 * @param name the name of the entry
	 * @param parent the parent of the entry
	 */
	public EntryElement(String name, EntryElement parent) {
		this.parent = parent;
		isField = true;
		this.name = name;
		this.value = "";
		elements = new ArrayList<EntryElement>();
	}
	
	/**
	 * Adds a child element to this entry element.
	 * 
	 * @param element the element to add
	 */
	public void addElement(EntryElement element) {
		elements.add(element);
		isField = false;
	}
	
	/**
	 * Removes a child element from this entry element
	 * @param element the element to delete
	 */
	public void removeElement(EntryElement element) {
		elements.remove(element);
		isField = false;
	}

	/**
	 * Returns whether this entry element is a leaf element.
	 * 
	 * @return {@code true} if this is a field element, an element without
	 * child elements, {@code false} otherwise
	 */
	public boolean isField() {
		return isField;
	}

	/**
	 * Sets whether this element is a field or leaf element.
	 * 
	 * @param isField if {@code true} this element is a field or leaf
	 */
	public void setField(boolean isField) {
		this.isField = isField;
	}

	/**
	 * Returns the name of this element.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of this element.
	 * 
	 * @param name the name of this element
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the value of this element.
	 * 
	 * @return the value of this element
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Sets the value of this element.
	 * 
	 * @param value the value of this element
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * Returns the list of child {@code EntryElement}s.
	 * 
	 * @return the child elements
	 */
	public List<EntryElement> getElements() {
		return elements;
	}

	/**
	 * Sets the list of child {@code EntryElement}s.
	 * 
	 * @param elements the new list of child elements
	 */
	public void setElements(List<EntryElement> elements) {
		this.elements = elements;
	}

	/**
	 * Returns the parent element.
	 * 
	 * @return the parent element
	 */
	public EntryElement getParent() {
		return parent;
	}

	/**
	 * Sets the parent element.
	 * 
	 * @param parent the new parent element
	 */
	public void setParent(EntryElement parent) {
		this.parent = parent;
	}
	
	@Override
	public String toString() {
		String str = "<html><b>" + name + "</b>";
		if(value != null && !value.equals("")) {
			try {
				URL url = new URL(value);
				str += ": <i><a href=\"" + value + "\">" + value + "</a></i>";
			} catch (MalformedURLException mue) {
				// Apparently the value was not a URL; Do nothing
				str += ": <i>" + value + "</i>";
			}
		}
		str += "</html>";
		return str;
	}
}
