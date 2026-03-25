package mpi.dcr.isocat;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;

/**
 * The workspace parser handler, extracts profiles from the workspace
 * 
 * @author Han Sloetjes
 * @version 1.0
  */
public class WSHandler extends DefaultHandler {
    private ArrayList<Profile> profiles = new ArrayList<Profile>(12);
    private final static String PROF = "cat:profile";

    /**
     * Creates a new handler.
     */
    public WSHandler() {
		super();
	}

	/**
     * Returns a list with Profile objects, containing name and id
     *
     * @return a list with Profile objects
     */
    public ArrayList<Profile> getProfiles() {
        return new ArrayList<Profile>(profiles);
    }

    /**
     * Clear the current profiles map
     *
     * @throws SAXException any parse exception
     */
    @Override
	public void startDocument() throws SAXException {
        profiles.clear();
    }

    /**
     * Extracts id and name from a profile element
     *
     * @param uri the namespace uri
     * @param localName the local name
     * @param qName the qualified name
     * @param atts the attributes
     *
     * @throws SAXException any parse exception 
     */
    @Override
	public void startElement(String uri, String localName, String qName,
        Attributes atts) throws SAXException {
        if (qName.equals(PROF)) {
        	profiles.add(new Profile(atts.getValue("id"), atts.getValue("name")));
        }
    }

}
