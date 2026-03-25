package mpi.eudico.server.corpora.clomimpl.dobes;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Creates an ECV document.
 * 
 * @author Micha Hulsbosch, Olaf Seibert
 * @version january 2015
 */
public class ECV02 {
	/** the schema location for EAF 2.8 */
	public static final String ECV_SCHEMA_LOCATION = "http://www.mpi.nl/tools/elan/EAFv2.8.xsd";
	/** the local path to the 2.8 schema */
	public static final String ECV_SCHEMA_RESOURCE = "/mpi/eudico/resources/EAFv2.8.xsd";
	/** the document object */
	protected Document doc;
	
    /**
     * Creates an empty document
     * 
     * @throws Exception any exception that can occur when building the DOM
     */
    public ECV02() throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder        db  = dbf.newDocumentBuilder();
		this.doc = db.newDocument();
    }

	/** 
	 * Returns the document element.
	 * 
	 * @return the document element
	 * @see Document#getDocumentElement() 
	 */
	public Element getDocumentElement() { return this.doc.getDocumentElement(); }

	/** 
	 * Adds and returns a node.
	 * 
	 * @param e the node to append
	 * @return the child node
	 * @see Element#appendChild(Node) 
	 */
	public Node appendChild(Node e) { return this.doc.appendChild(e); }

	/**
	 * Creates a new external CV document element
	 * 
	 * @param creationDate the date string
	 * @param author the author, owner
	 * @param version the version of the ECV format
	 * @return result (Element)
	 */
	public Element newExternalCVDocument
	(String creationDate, String author, String version) {
		// these attributes are all optional
		//if (creationDate == null) throw new RuntimeException("ECV");
		//if (author == null) throw new RuntimeException("ECV");
		//if (version == null) throw new RuntimeException("ECV");
		Element result = this.doc.createElement("CV_RESOURCE");
		result.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		result.setAttribute("xsi:noNamespaceSchemaLocation", ECV_SCHEMA_LOCATION);
		if (creationDate != null) {
			result.setAttribute("DATE",   creationDate);
		}
		if (author != null) {
			result.setAttribute("AUTHOR", author);
		}
		if (version != null) {
			result.setAttribute("VERSION", version);
		} else {
			result.setAttribute("VERSION", "0.2");// default
		}
		//result.setAttribute("FORMAT", "0.2");
		return result;
	}

	/**
	 * Creates a new CV element
	 * 
	 * @param conVocId the id attribute
	 * @return result (Element)
	 */
	public Element newControlledVocabulary (String conVocId) {
		if (conVocId == null) throw new RuntimeException("ECV");
		Element result = this.doc.createElement("CONTROLLED_VOCABULARY");
		result.setAttribute("CV_ID", conVocId);
		return result;
	}
	
	/**
	 * Creates a {@code LANGUAGE} element
	 * @param id the id attribute
	 * @param def the language definition
	 * @param label the label
	 * @return the {@code LANGUAGE} element
	 */
	public Element newLanguage(String id, String def, String label) {
		Element result = this.doc.createElement("LANGUAGE");
		result.setAttribute("LANG_ID", id);
		result.setAttribute("LANG_DEF", def);
		result.setAttribute("LANG_LABEL", label);
		return result;
	}

	/**
	 * Creates a {@code DESCRIPTION} element
	 * @param languageId the language id
	 * @param description the description in that language
	 * @return the {@code DESCRIPTION} element
	 */
	public Element newDescription(String languageId, String description) {
		Element result = this.doc.createElement("DESCRIPTION");
		result.setAttribute("LANG_REF", languageId);
		result.appendChild(doc.createTextNode(description));
		return result;
	}

	/**
	 * Creates a {@code CVE_VALUE} element
	 * @param langRef the language reference
	 * @param value the value in that language
	 * @param description the description in that language
	 * @return the {@code CVE_VALUE} element
	 */
	public Element newCVEntryValue(String langRef, String value, String description) {
		Element result = this.doc.createElement("CVE_VALUE");
		if (description == null) {
			description = "";
		}
		result.setAttribute("DESCRIPTION", description);
		result.setAttribute("LANG_REF", langRef);
		result.appendChild(doc.createTextNode(value));
		return result;
	}
	
	/**
	 * Creates a {@code CV_ENTRY_ML} element.
	 * 
	 * @param id the ID of the entry
	 * @param extRefId the external reference ID
	 * @return the new {@code CV_ENTRY_ML} element
	 */
	public Element newCVEntryML(String id, String extRefId) {
		Element result = this.doc.createElement("CV_ENTRY_ML");
		result.setAttribute("CVE_ID", id);
		if (extRefId != null && !extRefId.isEmpty()) {
			result.setAttribute("EXT_REF", extRefId);
		}
		return result;
	}
	
	/**
	 * Creates a new External Reference element
	 * 
	 * @param id the id attribute
	 * @param type the type of external reference
	 * @param value the value
	 * @return result (Element)
	 */
	public Element newExternalReference(String id, String type, String value) {
    	if (id == null || type == null || value == null) throw new RuntimeException("ECV");
    	
    	Element result = this.doc.createElement("EXTERNAL_REF");
    	result.setAttribute("EXT_REF_ID", id);
    	result.setAttribute("TYPE", type);
    	result.setAttribute("VALUE", value);
    	
    	return result;
    }
}
