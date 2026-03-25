package mpi.eudico.webserviceclient.weblicht;

import static mpi.eudico.webserviceclient.weblicht.TCFConstants.ID;
import static mpi.eudico.webserviceclient.weblicht.TCFConstants.LEMMA;
import static mpi.eudico.webserviceclient.weblicht.TCFConstants.POSTAGS;
import static mpi.eudico.webserviceclient.weblicht.TCFConstants.SENT;
import static mpi.eudico.webserviceclient.weblicht.TCFConstants.TAG;
import static mpi.eudico.webserviceclient.weblicht.TCFConstants.TEXT;
import static mpi.eudico.webserviceclient.weblicht.TCFConstants.TOKEN;
import static mpi.eudico.webserviceclient.weblicht.TCFConstants.TOKEN_IDS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A content handler for TCF XML files or strings.
 */
public class TCFHandler extends DefaultHandler {
	// content handler
	private StringBuilder content = new StringBuilder();
	
	private TCFElement curElement;
	//private TCFType curType;
	private String text;
	private Map<TCFType, List<TCFElement>> baseElements;
	
	/**
	 * Constructor.
	 */
	public TCFHandler() {
		baseElements = new HashMap<TCFType, List<TCFElement>>();
		baseElements.put(TCFType.SENTENCE, new ArrayList<TCFElement>());
		baseElements.put(TCFType.TOKEN, new ArrayList<TCFElement>());
		baseElements.put(TCFType.TAG, new ArrayList<TCFElement>());
		baseElements.put(TCFType.LEMMA, new ArrayList<TCFElement>());
	}
	
	/**
	 * Returns the parsed text.
	 * 
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * Returns the list of elements of the specific type.
	 * 
	 * @param type the type to get the elements for
	 * @return the list of tcb elements or null
	 */
	public List<TCFElement> getElementsByType(TCFType type) {
		return baseElements.get(type);
	}
	
	/**
	 * Returns (part of) the parsed contents as a map with element types as
	 * keys.
	 * 
	 * @return (relevant part of) the parsed contents as a map with element 
	 * types as keys
	 */
	public Map<TCFType, List<TCFElement>> getAllElements() {
		return baseElements;
	}
	
	@Override
	public void startElement(String uri, String localName, String name,
			Attributes atts) throws SAXException {
		//System.out.println("Parse: " + uri + " - " + localName + " - " + name);
		// method stub
		if (SENT.equals(localName)) {
			baseElements.get(TCFType.SENTENCE).add(new TCFElement(atts.getValue(ID), atts.getValue(TOKEN_IDS), null));
			//curType = TCFType.SENTENCE;
		} else if (TOKEN.equals(localName)) {
			curElement = new TCFElement(atts.getValue(ID), null, null);
			baseElements.get(TCFType.TOKEN).add(curElement);
			//curType = TCFType.TOKEN;
		} else if (TAG.equals(localName)) {
			curElement = new TCFElement(atts.getValue(ID), atts.getValue(TOKEN_IDS), null);
			baseElements.get(TCFType.TAG).add(curElement);
		} else if (POSTAGS.equals(localName)) {
			//curType = TCFType.POS_TAG;
		} else if (LEMMA.equals(localName)) {
			curElement = new TCFElement(null, atts.getValue(TOKEN_IDS), null);
			baseElements.get(TCFType.LEMMA).add(curElement);
			//curType = TCFType.LEMMA;
		}
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		content.append(ch, start, length);
	}
	
	@Override
	public void endElement(String uri, String localName, String name)
			throws SAXException {
		if (TOKEN.equals(localName) || TAG.equals(localName) || LEMMA.equals(localName)) {
			curElement.setText(content.toString().trim());
		} else if (TEXT.equals(localName)) {
			text = content.toString().trim();
		}
		// reset the content
		content.delete(0, content.length());
	}
}
