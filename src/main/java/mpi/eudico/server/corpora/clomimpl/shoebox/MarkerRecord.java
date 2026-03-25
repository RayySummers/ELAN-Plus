/*
 * Created on Jul 1, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package mpi.eudico.server.corpora.clomimpl.shoebox;

import java.io.Serializable;

/**
 * A record to store properties of a Toolbox marker (which is more or less 
 * equivalent to a tier in EAF).
 * 
 * @author hennie
 */
@SuppressWarnings("serial")
public class MarkerRecord implements Serializable {
	// character encoding
	/** constant for UTF-8 encoding */
	public static final int UTF8 = 0;
	/** constant for Latin-1 encoding */
	public static final int ISOLATIN = 1;
	/** constant for SIL encoding of IPA */
	public static final int SILIPA = 2;

	/** a string for Latin 1 encoding */
	public static final String ISOLATINSTRING = "ISO-Latin-1";
	/** a string for UTF-8 encoding */
	public static final String UNICODESTRING = "Unicode (UTF-8)";
	/** a string for SIL IPA encoding */
	public static final String SILIPASTRING = "SIL IPA";
	/** an array of encoding strings */
	public static final String[] charsetStrings = {ISOLATINSTRING, UNICODESTRING, SILIPASTRING};
	/** the marker */
	private String marker;
	/** the parent marker */
	private String parentMarker;
	/** the stereotype string */
	private String stereoType;
	/** the character set */
	private String charsetString;
	/** the participant marker */
	private boolean participantMarker = false;
	/** the exclude flag */
	private boolean exclude = false;
	
	/**
	 * Creates a new record.
	 */
	public MarkerRecord() {
		super();
	}

	/**
	 * Returns the name of the character set.
	 * 
	 * @return the name of the character set
	 */
	public String getCharsetString() {
		return charsetString;
	}
	
	/**
	 * Returns a constant for the character set.
	 * 
	 * @return the constant for the character set, one of {@code #ISOLATIN},
	 * {@code #UTF8} or {@code #SILIPA}
	 */
	public int getCharset() {
		int charset = -1;
		if (charsetString.equals(ISOLATINSTRING)) {
			charset = ISOLATIN;
		}
		else if (charsetString.equals(UNICODESTRING)) {
			charset = UTF8;
		}
		else if (charsetString.equals(SILIPASTRING)) {
			charset = SILIPA;
		}
		
		return charset;
	}

	/**
	 * Returns  the record marker, the marker that indicates the start of a
	 * new record.
	 * 
	 * @return the record marker
	 */
	public String getMarker() {
		return marker;
	}

	/**
	 * Returns the parent marker.
	 * 
	 * @return the parent marker
	 */
	public String getParentMarker() {
		return parentMarker;
	}

	/**
	 * Return the stereotype as a string.
	 * 
	 * @return the stereotype
	 */
	public String getStereoType() {
		return stereoType;
	}

	/**
	 * Returns the marker that holds the participant's name or id.
	 * 
	 * @return the participant marker
	 */
	public boolean getParticipantMarker() {
		return participantMarker;
	}
	
	/**
	 * Returns whether this marker will be excluded from the import.
	 * 
	 * @return the excluded flag
	 */
	public boolean isExcluded() {
		return exclude;
	}
	
	/**
	 * Sets the character encoding of the file.
	 * 
	 * @param charset the character set name
	 */
	public void setCharset(String charset) {
		this.charsetString = charset;
	}
	
	/**
	 * Sets the record marker.
	 * 
	 * @param string the record marker
	 */
	public void setMarker(String string) {
		marker = string;
	}

	/**
	 * Sets the parent marker of this marker.
	 * 
	 * @param string the parent marker
	 */
	public void setParentMarker(String string) {
		parentMarker = string;
	}

	/**
	 * Sets the stereotype.
	 * 
	 * @param string the stereotype
	 */
	public void setStereoType(String string) {
		stereoType = string;
	}
	
	/**
	 * Sets whether this is a participant marker.
	 * 
	 * @param bool if true this is the participant marker
	 */
	public void setParticipantMarker(boolean bool) {
		participantMarker = bool;
	}
	
	/**
	 * Sets whether this marker has to be excluded from import.
	 * 
	 * @param bool if true this marker is excluded
	 */
	public void setExcluded(boolean bool) {
		exclude = bool;
	}
	
	@Override
	public String toString() {
		return  "marker:      " + marker + "\n" +
				"parent:      " + parentMarker + "\n" +
				"stereotype:  " + stereoType + "\n" +
				"charset:     " + charsetString + "\n" +
				"exclude:     " + exclude + "\n" +
				"participant: " + participantMarker + "\n";
	}
}
