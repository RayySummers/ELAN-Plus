package mpi.eudico.webserviceclient.weblicht;

/**
 * A class representing a TCF element.
 */
public class TCFElement {
	private String id;
	private String idRefs;
	private String text;
	//private Map<String, String> attributes;
	
	/**
	 * Constructor.
	 * 
	 * @param id the id, can be {@code null}
	 * @param idRefs one or more id refs, can be {@code null}
	 * @param text the content of the element
	 */
	public TCFElement(String id, String idRefs, String text) {
		super();
		this.id = id;
		this.idRefs = idRefs;
		this.text = text;
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
	 * Sets the id.
	 * @param id the new id
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * Returns the {@code idrefs} string.
	 * 
	 * @return the {@code idrefs} string or {@code null}
	 */
	public String getIdRefs() {
		return idRefs;
	}
	
	/**
	 * Sets the {@code idrefs} string.
	 * 
	 * @param idRefs the {@code idrefs} string
	 */
	public void setIdRefs(String idRefs) {
		this.idRefs = idRefs;
	}
	
	/**
	 * Returns the text of the element.
	 * 
	 * @return the text
	 */
	public String getText() {
		return text;
	}
	
	/**
	 * Sets the text of the element.
	 * 
	 * @param text the text
	 */
	public void setText(String text) {
		this.text = text;
	}
	
}
