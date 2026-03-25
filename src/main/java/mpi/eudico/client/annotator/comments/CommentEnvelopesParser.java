package mpi.eudico.client.annotator.comments;

import mpi.eudico.client.annotator.util.ClientLogger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;

import static mpi.eudico.client.annotator.util.ResourceUtil.getResource;

/**
 * A parser for CommentEnvelope XML.
 */
public class CommentEnvelopesParser implements ClientLogger  {
    private SAXParser parser;
    private CommentEnvelopesHandler ceHandler;

    private List<CommentEnvelope> messages;
    private Predicate<CommentEnvelope> filter;

    /**
     * Constructor, creates a new SAXParser
     */
    public CommentEnvelopesParser() {
        try {
            // first get general XML parsing warnings and errors
            SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            parserFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
            parserFactory.setNamespaceAware(true);

            // to get a validating parser, set the schema to the proper xsd schema
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema eafSchema = schemaFactory.newSchema(getResource("/mpi/eudico/resources/ColTime.xsd"));
            parserFactory.setSchema(eafSchema);
            parser = parserFactory.newSAXParser();
            ceHandler = new CommentEnvelopesHandler();
        } catch (SAXException se) {
            LOG.log(Level.WARNING, "Exception occurred processing the schema", se);
        } catch (ParserConfigurationException pce) {
            LOG.log(Level.WARNING, "Exception occurred parsing the schema", pce);
        }
    }

    /**
     * parses the filename to a list of envelopes
     *
     * @param filename the filename
     *
     * @return the list of comment envelopes
     */
    public List<CommentEnvelope> parse(String filename) {
        return parse(filename, null);
    }

    /**
     * Parse a file and apply the filter to check if each encountered CommentEnvelope is wanted. This class isn't thread
     * safe: concurrent calls to parse() will for instance get confused about the filter (but sequential parses can of course
     * use different filters).
     *
     * @param filename the file to read
     * @param filter a Predicate to check which CommentEnvelopes are wanted
     *
     * @return the {@code List<CommentEnvelope>} which were selected by the filter
     */
    public List<CommentEnvelope> parse(String filename, Predicate<CommentEnvelope> filter) {

        File pf = new File(filename);

        if (pf.exists()) {
            try (FileInputStream fis = new FileInputStream(pf)) {
                // Call reader.parse(InputSource) rather than the easier
                // reader.parse(fileName) because the latter really takes a SystemId
                // and may try (and fail with error) to interpret it as a URL or similar.
                InputSource source = new InputSource(fis);
                source.setSystemId(filename);
                return parse(source, filter);
            } catch (IOException e) {
                LOG.log(Level.WARNING, "File not found. Should not happen, because pf.exists() checked it.", e);
            }
        }

        this.messages = new ArrayList<>();
        return this.messages;
    }

    /**
     * Parses the input source
     *
     * @param input the input source
     * @param filter the predicate
     *
     * @return list of comment envelope
     */
    public List<CommentEnvelope> parse(InputSource input, Predicate<CommentEnvelope> filter) {
        this.messages = new ArrayList<>();
        this.filter = filter;

        try {
            parser.parse(input, ceHandler);
        } catch (IOException | SAXException e) {
            LOG.log(Level.WARNING, "parsing error.", e);
        }

        return this.messages;
    }

    /**
     * The content handler, error handler, and entity resolver.
     */
    class CommentEnvelopesHandler extends DefaultHandler {

        private CommentEnvelope ctm;
        private String content;

        @Override
        public void characters(char[] ch, int start, int end) throws
                                                              SAXException {
            content += new String(ch, start, end);
        }

        @Override
        public void startElement(String nameSpaceURI, String name, String rawName, Attributes attrs) throws
                                                                                                     SAXException {

            content = "";

            if (name.equals("ColTime")) {
                ctm = new CommentEnvelope();

                String id = attrs.getValue("ColTimeMessageID");
                if (id != null) {
                    ctm.setMessageID(id);
                }
                String url = attrs.getValue("URL");
                if (url != null) {
                    ctm.setMessageURL(url);
                }
                ctm.setRecipient("");
            } else if (name.equals("AnnotationFile")) {
                String id = attrs.getValue("URL");
                //                if (id == null) { // accept old name of this attribute
                //                    id = attrs.getValue("ColTimeID");
                //                }
                if (id != null) {
                    ctm.setAnnotationFileURL(id);
                }
                String type = attrs.getValue("type");
                if (type != null) {
                    ctm.setAnnotationFileType(type);
                }
            }
        }

        @Override
        public void endElement(String nameSpaceURI, String name, String rawName) throws
                                                                                 SAXException {
            if (name.equals("ColTime")) {
                if (filter == null || filter.test(ctm)) {
                    messages.add(ctm);
                }
                ctm = null;
            } else if (name.equals("Initials")) {
                ctm.setInitials(content);
            } else if (name.equals("ThreadID")) {
                ctm.setThreadID(content);
            } else if (name.equals("Sender")) {
                ctm.setSender(content);
            } else if (name.equals("Recipient")) {
                // Can occur 0 or more times. Comma-separate the values.
                ctm.addRecipient(content);
            } else if (name.equals("CreationDate")) {
                ctm.setCreationDate(content);
            } else if (name.equals("ModificationDate")) {
                ctm.setModificationDate(content);
            } else if (name.equals("Category")) {
                ctm.setCategory(content);
            } else if (name.equals("Status")) {
                ctm.setStatus(content);
            } else if (name.equals("AnnotationFile")) {
                ctm.setAnnotationFile(content);
            } else if (name.equals("Message")) {
                ctm.setMessage(content);
            }
        }

        //        @Override
        //        public void startDocument() throws SAXException {
        //        }

        /*
         * ErrorHandler
         * @see org.xml.sax.helpers.DefaultHandler#error(org.xml.sax.SAXParseException)
         */
        @Override
        public void error(SAXParseException exception) throws
                                                       SAXException {
            System.out.println("Error:     " + exception.getMessage());
            // system id is the file path
            System.out.println("System id: " + exception.getSystemId());
            System.out.println("Public id: " + exception.getPublicId());
            System.out.println("Line:      " + exception.getLineNumber());
            System.out.println("Column:    " + exception.getColumnNumber());
        }

        @Override
        public void fatalError(SAXParseException exception) throws
                                                            SAXException {
            System.out.println("FatalError: " + exception.getMessage());

        }

        @Override
        public void warning(SAXParseException exception) throws
                                                         SAXException {
            System.out.println("Warning: " + exception.getMessage());

        }

    } // class CommentEnvelopesContentHandler
}
