package mpi.eudico.server.corpora.lexicon;

import java.util.ArrayList;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;


/**
 * A Lexicon structure holding entries and their elements.
 * 
 * @author Micha Hulsbosch
 */
public class Lexicon implements TreeModel {

	ArrayList<LexiconEntry> entries; // Not List: uses get(n)
	private String name;
	
	/**
	 * Creates a new Lexicon instance with an empty entries list.
	 */
	public Lexicon() {
		entries = new ArrayList<LexiconEntry>();
	}
	
	@Override
	public String toString() {
		return name;
	}

	/**
	 * Adds an entry to the lexicon.
	 * 
	 * @param entry the entry to add
	 */
	public void addEntry(LexiconEntry entry) {
		entries.add(entry);
	}
	
	/**
	 * Returns the entry at index {@code index}.
	 * 
	 * @param index the index of the entry to retrieve
	 * @return the entry at that index or {@code null}
	 */
	public LexiconEntry getEntry(int index) {
		return entries.get(index);
	}

	@Override
	public void addTreeModelListener(TreeModelListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object getChild(Object parent, int index) {
		if(parent instanceof Lexicon) {
			// This doesn't use 'parent', because the only Lexicon object returned
			// from this model is from getRoot(), and that is 'this'.
			// assert(parent == this);
			return entries.get(index); // returns LexiconEntry is-a EntryElement
		} else {
			return ((EntryElement) parent).getElements().get(index); // returns EntryElement
		}
	}

	@Override
	public int getChildCount(Object parent) {
		if(parent instanceof Lexicon) {
			return entries.size();
		} else {
			return ((EntryElement) parent).getElements().size();
		}
	}

	@Override
	public int getIndexOfChild(Object parent, Object child) {
		if(parent instanceof Lexicon) {
			return entries.indexOf(child);
		} else {
			return ((EntryElement) parent).getElements().indexOf(child);
		}
	}

	@Override
	public Object getRoot() {
		return this;
	}

	@Override
	public boolean isLeaf(Object node) {
		if(node instanceof Lexicon) {
			return false;
		} else {
			return ((EntryElement) node).isField();
		}
	}

	@Override
	public void removeTreeModelListener(TreeModelListener listener) {
		// stub
	}

	@Override
	public void valueForPathChanged(TreePath path, Object newvalue) {
		// stub
	}

	/**
	 * Sets the name of the lexicon.
	 * 
	 * @param name the new name of the lexicon
	 */
	public void setName(String name) {
		this.name = name;
	}
}
