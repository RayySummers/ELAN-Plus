package mpi.eudico.client.annotator.multiplefilesedit;

/**
 * An exception thrown if an inconsistency in tier parent - child relations
 * in multiple files is detected. 
 */
public class InconsistentChildrenException extends Exception {
	private static final long serialVersionUID = -2374884117873928484L;
	private String parent;
	private String child;
	private String loadedParents;
	
	/**
	 * Constructor.
	 * 
	 * @param parent the name of the parent tier
	 * @param child the name of the child tier
	 */
	public InconsistentChildrenException(String parent, String child) {
		this.parent = parent;
		this.child = child;
	}
	
	/**
	 * Constructor.
	 * 
	 * @param parent the name of the parent tier
	 * @param child the name of the child tier
	 * @param loadedParents the names of already loaded parents
	 */
	public InconsistentChildrenException(String parent, String child, String loadedParents) {
		this(parent, child);
		this.loadedParents = loadedParents;
	}
	
	/**
	 * Returns the name of the parent tier.
	 *  
	 * @return the name of the parent tier
	 */
	public String getParent() {
		return parent;
	}
	
	/**
	 * Returns the name of a child tier.
	 * 
	 * @return the name of the child tier
	 */
	public String getChild() {
		return child;
	}

	/**
	 * Returns the loaded parents.
	 * 
	 * @return the loaded parents
	 */
	public String getLoadedParents() {
		return loadedParents;
	}
}
