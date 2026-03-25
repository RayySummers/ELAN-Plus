package mpi.eudico.client.annotator.lexicon.api;

import mpi.eudico.client.annotator.lexicon.api.Param;

/**
 * Holds a parameter for the Lexicon Service CMDI parser.
 * 
 * @author Micha Hulsbosch
 *
 */
public class Param implements Cloneable {
	private String type;
	private String content;
	
	/**
	 * Creates a new parameter instance.
	 */
	public Param() {
		super();
	}

	/**
	 * Returns the type of the parameter.
	 * 
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * Sets the type of the parameter.
	 * 
	 * @param type the type
	 */
	public void setType(String type) {
		this.type = type;
	}
	
	/**
	 * Returns the content of the parameter.
	 * 
	 * @return the content 
	 */
	public String getContent() {
		return content;
	}
	/**
	 * Sets the content o the parameter.
	 * 
	 * @param content the content
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * Creates a deep clone of this object.
	 */
	@Override
	public Param clone() throws CloneNotSupportedException {
		return (Param) super.clone();
	}
}
