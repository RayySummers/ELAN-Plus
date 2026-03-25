package mpi.eudico.server.corpora.clomimpl.dobes;

/**
 * Stores information needed to construct a Language Info object
 * 
 * @see mpi.eudico.util.multilangcv.LangInfo
 * 
 * @author Olaf Seibert
 * @version march 2014
 */
public class LanguageRecord {
	private String id;
	private String def;
	private String label;                  
	
	/**
	 * Creates a new record with the specified properties.
	 * 
	 * @param id the language id
	 * @param def the language definition or URL
	 * @param label a label to use for the language
	 */
	public LanguageRecord(String id, String def, String label) {
		this.id = id;
		this.def = def;
		this.label = label != null ? label : "";
	}

	/**
	 * Returns the id.
	 * 
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the definition.
	 * 
	 * @return the definition
	 */
	public String getDef() {
		return def;
	}

	/**
	 * Returns the language label.
	 * 
	 * @return the language label
	 */
	public String getLabel() {
		return label;
	}	
}
