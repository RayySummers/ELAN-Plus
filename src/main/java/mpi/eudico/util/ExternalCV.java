package mpi.eudico.util;

import mpi.eudico.server.corpora.clom.ExternalReference;

/**
 * A class for an externally defined and stored controlled vocabulary, 
 * i.e. not part of the annotation document.
 *
 */
public class ExternalCV extends ControlledVocabulary {

    private ExternalReference externalRef;
    private boolean isLoadedFromURL;
    private boolean isLoadedFromCache;

	/**
	 * Constructor.
	 * 
	 * @param name the name of the CV
	 */
	public ExternalCV(String name) {
		super(name);
	}

	/**
	 * Create an External CV from a plain CV.
	 * 
	 * @param cv the plain CV
	 */
	public ExternalCV(BasicControlledVocabulary cv) {
		super(cv.getName());
		cloneStructure(cv);
		cloneEntries(cv);
	}
	
	/**
	 * Copy constructor.
	 * 
	 * @param ecv the {@code ExternalCV} to copy
	 */
	public ExternalCV(ExternalCV ecv) {
		super(ecv.getName());
		cloneStructure(ecv);
		externalRef = ecv.getExternalRef();
		if (externalRef != null) {
			try {
				externalRef = externalRef.clone();
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
		}
		isLoadedFromURL = ecv.isLoadedFromURL;
		isLoadedFromCache = ecv.isLoadedFromCache;
		cloneEntries(ecv);
	}
	
//	public ExternalCV(String cvName, String extRefId) {
//		this(cvName);
//		setExternalRef(extRefId);
//	}

	/**
	 * Returns the external reference of this CV.
	 * 
	 * @return the external reference of this CV or {@code null}
	 */
	public ExternalReference getExternalRef() {
		return externalRef;
	}

	/**
	 * Sets the external reference.
	 * @param externalRef the external reference
	 */
	public void setExternalRef(ExternalReference externalRef) {
		if (this.externalRef == null) {
			this.externalRef = externalRef;
		} else if (!this.externalRef.equals(externalRef)){
			this.externalRef = externalRef;
			// in principle this ECV should be reloaded from the new url?
			super.handleModified();
		}
	}
	
	
	@Override
	protected void handleModified() {
		// do nothing, external CV's are not editable
	}

	/**
	 * Returns whether this ECV has been loaded from the actual URL.
	 * 
	 * @return {@code true} if the ECV was loaded from the actual URL, 
	 * {@code false} if it was loaded from cache or not loaded at all
	 */
	public boolean isLoadedFromURL() {
		return isLoadedFromURL;
	}

	/**
	 * Sets whether this ECV has been loaded from the actual URL.
	 * 
	 * @param isLoadedFromURL if {@code true} the ECV has been loaded from the
	 * actual location
	 */
	public void setLoadedFromURL(boolean isLoadedFromURL) {
		this.isLoadedFromURL = isLoadedFromURL;
	}

	/**
	 * Returns whether this ECV has been loaded from cache instead of the
	 * actual URL.
	 * 
	 * @return {@code true} if it has been loaded from cache, {@code false}
	 * otherwise
	 */
	public boolean isLoadedFromCache() {
		return isLoadedFromCache;
	}

	/**
	 * Sets whether this ECV has been loaded from cache.
	 * 
	 * @param isLoadedFromCache if {@code true} the ECV has been loaded from
	 * cache instead of the actual address
	 */
	public void setLoadedFromCache(boolean isLoadedFromCache) {
		this.isLoadedFromCache = isLoadedFromCache;
	}
	
    /**
     * An implementation of clone() that uses the copy constructor.
     * (This implies that subclasses must override)
     * @return a clone of this CV
     */
	@Override // Cloneable
	public ExternalCV clone() {
		return new ExternalCV(this);
	}
	
    /**
     * This function clones all CVEntries from an original vocabulary.
     * This CV will contain copies of the entries, converted to ExternalCVEntries.
     * <p>
     * This assumes that the languages match.
     * 
     * @param orig CV to clone the entries from
     */
    
    @Override
	public void cloneEntries(BasicControlledVocabulary orig) {
    	initMode = true;
    	entries.clear();
    	for (CVEntry e : orig) {
    		addEntry(new ExternalCVEntry(this, e));
    	}
    	initMode = false;
    }
    
    /**
     * This function clones all ExternalCVEntries from an original external vocabulary.
     * This CV will contain copies of the entries.
     * <p>
     * This assumes that the languages match.
     * 
     * @param orig ExternalCV to clone the entries from
     */
    
    public void cloneEntries(ExternalCV orig) {
    	initMode = true;
    	entries.clear();
    	for (CVEntry e : orig) {
    		addEntry(new ExternalCVEntry(this, (ExternalCVEntry)e));
    	}
    	initMode = false;
    }
	
//	public ArrayList getSuggestions(String input) {
//		ArrayList suggestions = new ArrayList();
//		for(String entry: getEntryValues()) {
//			if(entry.startsWith(input)) {
//				suggestions.add(entry);
//			}
//		}
//		return suggestions;
//	}
}
