package mpi.eudico.client.annotator.integration;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.util.HashMap;


/**
 * Parses an IMDI session file, only to extract (the first) .eaf file,  (the first) video file and (the first) audio file
 * (url's).  Preliminary implementation...
 *
 * @author Han Sloetjes
 */
public class IMDISessionParser {
    /**
     * Holds value of property eaf Dec 2005: the format string for eaf files in imdi files has been changed to
     * "text/x-eaf+xml".
     */
    public static final String EAF = "text/x-eaf+xml";
    //private final String EAF = "eaf";
    private final SAXParser parser;
    private final HashMap<String, String> filesMap;

    /**
     * Creates a new IMDISessionParser instance
     *
     * @throws SAXException any SAX parsing exception
     */
    public IMDISessionParser() throws
                               SAXException {
        filesMap = new HashMap<>(3);

        try {
            SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            parserFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
            parserFactory.setNamespaceAware(true);
            parser = parserFactory.newSAXParser();
        } catch (ParserConfigurationException pce) {
            throw new SAXException(pce);
        }
    }

    /**
     * Starts the actual parsing of the {@code imdi} file.
     *
     * @param pathToImdiSession the path to the {@code imdi} file
     *
     * @throws IOException io exception
     * @throws SAXException parse exception
     */
    public void parse(String pathToImdiSession) throws
                                                IOException,
                                                SAXException {
        if (pathToImdiSession == null) {
            return;
        }

        parser.parse(pathToImdiSession, new SessionHandler());
    }

    /**
     * Returns the HashMap containing the extracted eaf ,video and or audio file mappings.
     *
     * @return a map containing eaf and media files mappings
     */
    public HashMap<String, String> getFilesMap() {
        /*
        Iterator it = filesMap.keySet().iterator();

        while (it.hasNext()) {
            String key = (String) it.next();
            String value = (String) filesMap.get(key);
            System.out.println("K: " + key + " V: " + value);
        }
        */
        return filesMap;
    }

    /**
     * Test main method.
     *
     * @param args the args
     */
    public static void main(String[] args) {
        if ((args != null) && (args.length > 0)) {
            try {
                new IMDISessionParser().parse(args[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /*******************************************************************/

    /**
     * the content handler
     */
    class SessionHandler extends DefaultHandler {
        /**
         * Holds value of property MediaFile
         */
        private final String MF = "MediaFile";

        /** Holds value of property AnnotationUnit */
        //private final String AU = "AnnotationUnit";

        /**
         * Holds value of property WrittenResource
         */
        private final String WRITTEN_RES = "WrittenResource";

        /**
         * Holds value of property ResourceLink
         */
        private final String LINK = "ResourceLink";

        /**
         * Holds value of property Type
         */
        private final String TYPE = "Type";

        /**
         * Holds value of property Format
         */
        private final String FORMAT = "Format";

        /**
         * Holds value of property video
         */
        private final String VIDEO = "video";

        /**
         * Holds value of property audio
         */
        private final String AUDIO = "audio";
        private String curResLink;
        private String curContent;
        private String curFormat;
        private String curType;

        /**
         * The contents of an element
         *
         * @param ch the characters
         * @param start start index
         * @param length number of characters
         *
         * @throws SAXException parse ex
         */
        @Override
        public void characters(char[] ch, int start, int length) throws
                                                                 SAXException {
            curContent = new String(ch, start, length);
        }

        /**
         * End of element. Handles the elements we are interested in.
         *
         * @param namespaceURI namespace
         * @param localName name of the element
         * @param qName raw name?
         *
         * @throws SAXException parse ex
         */
        @Override
        public void endElement(String namespaceURI, String localName, String qName) throws
                                                                                    SAXException {
            if (localName.equals(TYPE)) {
                curType = curContent;
            } else if (localName.equals(FORMAT)) {
                curFormat = curContent;
            } else if (localName.equals(LINK)) {
                curResLink = curContent;
            } else if (localName.equals(WRITTEN_RES)) {
                if (curFormat.equalsIgnoreCase(EAF)) {
                    if (!filesMap.containsKey(EAF) && (curResLink != null) && (curResLink.length() > 0)) {
                        filesMap.put(EAF, curResLink);
                    }
                }

                resetFields();
            } else if (localName.equals(MF)) {
                if (curType.equalsIgnoreCase(VIDEO)) {
                    if (!filesMap.containsKey(VIDEO) && (curResLink != null) && (curResLink.length() > 0)) {
                        filesMap.put(VIDEO, curResLink);
                    }
                } else if (curType.equalsIgnoreCase(AUDIO)) {
                    if (!filesMap.containsKey(AUDIO) && (curResLink != null) && (curResLink.length() > 0)) {
                        filesMap.put(AUDIO, curResLink);
                    }
                }

                resetFields();
            }
        }

        /**
         * Resets the fields at the end of a relevant element.
         */
        void resetFields() {
            curResLink = null;
            curContent = null;
            curFormat = null;
            curType = null;
        }
    }
}
