package mpi.dcr.isocat;

import mpi.dcr.DCSmall;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;


/**
 * Minimal parser handler for ISOCat's single Data Category DCIF stream.
 *
 * @author Han Sloetjes
 * @version 1.0
 * @author aarsom
 * @version 2.0
 */
public class DCIF_DC_Handler extends DefaultHandler {
    private DCSmall curDC;
    private String curLang;
    private boolean inLanguage = false;
    private boolean recordContent = false;
    private String curContent;
    private List<Profile> curProfs = new ArrayList<Profile>();

    /**
     * Creates a new handler instance.
     */
    public DCIF_DC_Handler() {
		super();
	}

	@Override
	public void characters(char[] ch, int start, int length)
        throws SAXException {
        if (recordContent) {
        	if(curContent == null){
        		curContent = new String(ch, start, length);
        	} else {
        		curContent = curContent.concat(new String(ch, start, length));
        	}
        }
    }

    /**
     * Returns the resulting (small) Data Category object. Note: might be
     * extended in the future if more information is needed
     *
     * @return the DCSmall object
     */
    public DCSmall getDC() {
        return curDC;
    }

    /**
     * End of document, finishes current DC object
     *
     * @throws SAXException any SAX exception
     */
    @Override
	public void endDocument() throws SAXException {
    	if (curDC != null) {
    		curDC.setLoaded(true);
    	}
        curProfs.clear();
    }

    @Override
	public void endElement(String uri, String localName, String qName)
        throws SAXException {
        if (qName.equals(ISOCatConstants.DC)) {
            if (curProfs.size() > 0) {
                curDC.setProfiles(curProfs.toArray(new Profile[] {  }));
            }
        } else if (qName.equals(ISOCatConstants.ID)) {
            if (curDC != null) {
                curDC.setIdentifier(curContent);
            }
        } else if (qName.equals(ISOCatConstants.LANG)) {
            if (inLanguage && (curContent != null)) {
                curLang = curContent;
            }
        } else if (qName.equals(ISOCatConstants.LANG_SEC)) {
            inLanguage = false;
        } else if (qName.equals(ISOCatConstants.DEF) && curLang != null) {
        	if(curDC != null){
        		curDC.setDesc(curLang, curContent);
        	}
        } else if (qName.equals(ISOCatConstants.NAME) && curLang != null) {
            if (curDC != null) {
            	curDC.setName(curLang, curContent);
            }
        } else if (qName.equals(ISOCatConstants.PROF)) {
        	Profile pf = new Profile(null, curContent);
            if (!curProfs.contains(pf)) {
                curProfs.add(pf);
            }
        }

        recordContent = false;
        curContent = null;
    }

    /**
     * Resets objects.
     *
     * @throws SAXException any parse exception
     */
    @Override
	public void startDocument() throws SAXException {
        curDC = null;
    }

    /**
     * Extracts selected attributes and sets some flags.
     *
     * @param uri the namespace uri
     * @param localName the local namespace
     * @param qName qualified name
     * @param atts the attributes
     *
     * @throws SAXException any parse exception
     */
    @Override
	public void startElement(String uri, String localName, String qName,
        Attributes atts) throws SAXException {
        if (qName.equals(ISOCatConstants.DC)) {
            curDC = new DCSmall(atts.getValue(ISOCatConstants.PID), null);
        } else if (qName.equals(ISOCatConstants.PROF)) {
            recordContent = true;
        } else if (qName.equals(ISOCatConstants.LANG_SEC)) {
            inLanguage = true;
        } else if (qName.equals(ISOCatConstants.LANG)) {
            if (inLanguage) {
                recordContent = true;
            }
        } else if (qName.equals(ISOCatConstants.DEF)) {
            recordContent = true;
        } else if (qName.equals(ISOCatConstants.NAME)) {
            recordContent = true;
        } else if (qName.equals(ISOCatConstants.ID)) {
            recordContent = true;
        }

        // do something more with conceptual domain?
    }

}
