package mpi.eudico.server.corpora.clomimpl.abstr;

/**
 * A class to store the information from a single &lt;LICENSE&gt;
 * element in an EAF file.
 *  
 * @author olasei
 */

public class LicenseRecord {
	private String url;
	private String text;
	
	/**
	 * Creates a new license record.
	 */
	public LicenseRecord() {
		super();
	}

	/**
	 * Returns the URL.
	 * 
	 * @return the URL or {@code null}
	 */
	public String getUrl() {
		return url;
	}
	
	/**
	 * Sets the URL.
	 * 
	 * @param url the URL of the license
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	
	/**
	 * Returns the text of the license.
	 * 
	 * @return the text, not {@code null}
	 */
	public String getText() {
		if (text == null) {
			text = "";
		}
		return text;
	}
	
	/**
	 * Sets the text of the license.
	 * 
	 * @param text the text of the license, not {@code null}
	 */
	public void setText(String text) {
		this.text = text;
	}
}
