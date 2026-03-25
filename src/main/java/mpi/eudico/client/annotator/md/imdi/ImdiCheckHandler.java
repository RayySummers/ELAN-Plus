package mpi.eudico.client.annotator.md.imdi;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * Performs a quick, trivial test on a file. It checks the "type" attribute of
 * the "METATRANSCRIPT" element to be "SESSION" and checks that the next
 * element is a "Session" element. Then returns.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class ImdiCheckHandler extends DefaultHandler {
    private int numElements = 0;
    private boolean isSessionType = false;
    private boolean isSessionElem = false;
    private boolean isCMDI = false;

    /**
     * Creates a new ImdiCheckHandler instance
     */
    public ImdiCheckHandler() {
        super();
    }

    /**
     * Returns whether the file is an IMDI session file.
     *
     * @return true if it is an IMDI Session file
     */
    public boolean isSessionFile() {
        return (isSessionType && isSessionElem) || (isCMDI && isSessionElem);
    }

    /**
     * Checks 2 elements, then throws an exception.
     *
     *
     * @throws SAXException thrown to stop parsing beyond the first 2 elements
     */
    @Override
	public void startElement(String uri, String localName, String qName,
        Attributes atts) throws SAXException {
        numElements++;

        if (localName.equals("METATRANSCRIPT")) {
            String type = atts.getValue("Type");

            if ((type != null) && type.equals("SESSION")) {
                isSessionType = true;
            }
        } else if (localName.equals("CMD")) {
        	isCMDI = true;
        } else if (localName.equals("Session")) {
            //if (numElements < 3) {
                isSessionElem = true;
            //}
            // got to a Session element, throw an exception to stop parsing
            throw new SAXException("Parsed " + numElements + " elements...");
        }
    }

}
