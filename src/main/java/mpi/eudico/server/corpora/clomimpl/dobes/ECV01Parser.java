package mpi.eudico.server.corpora.clomimpl.dobes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import mpi.eudico.server.corpora.clom.ExternalReference;
import mpi.eudico.server.corpora.clomimpl.abstr.ExternalReferenceImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.ParseException;
import mpi.eudico.util.CVEntry;
import mpi.eudico.util.ControlledVocabulary;
import mpi.eudico.util.ExternalCV;
import mpi.eudico.util.ExternalCVEntry;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A (SAX2) Parser for External CV (ECV) compliant XML files
 * 
 * @author Micha Hulsbosch
 * @version jul 2010
 */
public class ECV01Parser {
	/** the sax parser */
    private SAXParser parser;
    private boolean strict;

    /** the url of the external CV */
    private String url;

    /** stores the Locales */
//    private final ArrayList locales = new ArrayList();
    
    /** stores the ControlledVocabulary objects */
    private final ArrayList<ControlledVocabulary> cvList = new ArrayList<ControlledVocabulary>();
    /** stores external references */
	private final HashMap<String, ExternalReference> extReferences = new HashMap<String, ExternalReference>();
	private final HashMap<CVEntry, String> cvEntryExtRef = new HashMap<CVEntry, String>();
    private String currentCVId;
    private ControlledVocabulary currentCV;
    private String currentEntryDesc;
    private String currentEntryExtRef;
    private String content = "";
	//private boolean parseError;
	//private String author;
    /** field for the current entry ID */
	public String currentEntryId;

    /**
     * Creates a ECV01Parser instance
     * 
     * @param url (the url of the external CV)
     * @throws ParseException any SAX parse exception
     */
    public ECV01Parser(String url) throws ParseException {
    	this(url, false);
    }

	/**
	 * Creates a ECV01Parser instance
	 * 
	 * @param url (the url of the external CV)
	 * @param strict (whether the error handler should be used)
	 * @throws ParseException any SAX parse exception
	 */
	public ECV01Parser(String url, boolean strict) throws ParseException {
		if (url == null) {
	        throw new NullPointerException();
	    }
	
	    this.url = url;
	    this.strict = strict;
    	try {
    		boolean validate = Boolean.parseBoolean(System.getProperty("ELAN.EAF.Validate", "true"));
        	
			// first create a parser factory
    		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
			parserFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
			parserFactory.setNamespaceAware(true);
			
			if (validate) {
				// to get a validating parser, set the schema to the proper xsd schema
				SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
				Schema eafSchema = schemaFactory.newSchema(
						this.getClass().getResource(ACMTranscriptionStore.getCurrentEAFSchemaLocal()));// or EAFv2.7.xsd?
				
				parserFactory.setSchema(eafSchema);// the validating flag should be false (the default)
			} else {
				parserFactory.setValidating(false);// superfluous, false is already the default
			}
			
			parser = parserFactory.newSAXParser();
    	} catch (SAXException se) {
    		se.printStackTrace();
    	} catch (Throwable thr) {
    		thr.printStackTrace();
    		throw new ParseException(thr.getMessage());
    	}
	}

	/**
	 * Tries to parse the ECV file using the url
	 * 
	 * @throws ParseException any SAX parse exception
	 */
	public void parse() throws ParseException {
		//
        // get an input source for the URL, create a content handler
        // and call parse.
        try {
        	InputSource is = ACMTranscriptionStore.toInputSource(url);
        	ECV01Handler ecvHandler = new ECV01Handler();
        	
        	parser.parse(is, ecvHandler);
        	createObjects();
        } catch (IOException ioe) {
        	System.out.println("IO error: " + ioe.getMessage());
        	throw new ParseException("IO error: " + ioe.getMessage(), ioe.getCause());
        } catch (SAXException saex) {
        	System.out.println("Parsing error: " + saex.getMessage());
        	throw new ParseException("Parsing error: " + saex.getMessage(), saex.getCause());
        }
    }

	private void createObjects() {
		// post-processing of ext_ref's of CV entries
        if (cvEntryExtRef.size() > 0) {
        	for (Map.Entry<CVEntry, String> cvEntryEntry : cvEntryExtRef.entrySet()) {
        		CVEntry entry = cvEntryEntry.getKey();
        		String erId = cvEntryEntry.getValue();
        		
        		ExternalReferenceImpl eri = (ExternalReferenceImpl) extReferences.get(erId);
        		if (eri != null) {
        			try {
        				entry.setExternalRef(eri.clone());
        			} catch (CloneNotSupportedException cnse) {
        				System.out.println("Could not set the external reference: " + cnse.getMessage());
        			}
        		}
        	}        	
        }
	}
	
    /**
     * Returns a list of {@code ControlledVocabulary} objects.
     * 
     * @return cvList (ArrayList containing CVs)
     */
    public ArrayList<ControlledVocabulary> getControlledVocabularies() {
    	return cvList;
    }

    /**
     * Returns a with map {@code ExternalReference} values.
     * 
     * @return extReferences (the external reference)
     */
    public Map<String, ExternalReference> getExternalReferences() {
		return extReferences;
	}
    
    /**
     * ECV01Handler.
     * 
     * The content handler for the SAX parser.
     * 
     * @author Micha Hulsbosch
     *
     */
	public class ECV01Handler extends DefaultHandler {
		
        /**
         * Creates a new handler.
         */
		public ECV01Handler() {
			super();
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.ContentHandler#characters(char[], int, int)
		 */
		@Override
		public void characters(char[] ch, int start, int end)
				throws SAXException {
			content += new String(ch, start, end);
		}
	
		/* (non-Javadoc)
		 * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
		 */
		@Override
		public void endElement(String nameSpaceURI, String name, String rawName)
        throws SAXException {
	        if (name.equals("CV_ENTRY")) {
	        	ExternalCVEntry entry = new ExternalCVEntry(currentCV, content, currentEntryDesc, currentEntryId);
	            currentCV.addEntry(entry);
	            if (currentEntryExtRef != null) {
	            	cvEntryExtRef.put(entry, currentEntryExtRef);
	            }
	        }
	    }
	
		/* (non-Javadoc)
		 * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
		 */
		@Override
		public void startElement(String nameSpaceURI, String name,
	            String rawName, Attributes attributes) throws SAXException {
			content = "";
			
			if (name.equals("CV_RESOURCE")) {
				//author = attributes.getValue("AUTHOR");
            } 
//			else if (name.equals("LOCALE")) {
//                String langCode = attributes.getValue("LANGUAGE_CODE");
//                String countryCode = attributes.getValue("COUNTRY_CODE");
//
//                if (countryCode == null) {
//                    countryCode = "";
//                }
//
//                String variant = attributes.getValue("VARIANT");
//
//                if (variant == null) {
//                    variant = "";
//                }
//
//                Locale l = new Locale(langCode, countryCode, variant);
//                locales.add(l);
//            } 
			else if (name.equals("CONTROLLED_VOCABULARY")) {
            	currentCVId = attributes.getValue("CV_ID");
                currentCV = new ExternalCV(currentCVId);

                String desc = attributes.getValue("DESCRIPTION");
                if (desc != null) {
                    currentCV.setDescription(desc);
                }
                
                // To be discussed whether there is both
                // a CV_ID and a NAME
//                String cvName = attributes.getValue("NAME");
//                if (cvName != null) {
//                    currentCV.setName(cvName);
//                }
                
                // To be discussed whether a CV in an ECV can have an EXT_REF
            	String extRefId = attributes.getValue("EXT_REF");
            	if (extRefId != null) {
            		ExternalReferenceImpl eri = (ExternalReferenceImpl) extReferences.get(
        					(extRefId));
        			if (eri != null) {
        				try {
        					((ExternalCV) currentCV).setExternalRef(eri.clone());
        				} catch (CloneNotSupportedException cnse) {
        					//LOG.severe("Could not set the external reference: " + cnse.getMessage());
        				}
        			}
            	}
                
                cvList.add(currentCV);
            } else if (name.equals("CV_ENTRY")) {
                currentEntryDesc = attributes.getValue("DESCRIPTION");
                currentEntryExtRef = attributes.getValue("EXT_REF");
                currentEntryId = attributes.getValue("CVE_ID");
            } else if (name.equals("EXTERNAL_REF")) {
    			String value = attributes.getValue("VALUE");
    			String type = attributes.getValue("TYPE");
    			String refId = attributes.getValue("EXT_REF_ID");
    			if (value != null) {
    				ExternalReferenceImpl eri = new ExternalReferenceImpl(value, type);
    				extReferences.put(refId, eri);
    			}
    			
    		}
		}
	
		@Override
		public void error(SAXParseException exception) throws SAXException {
			if (strict) {	
				System.out.println("Error: " + exception.getMessage());
				// system id is the file path
				System.out.println("System id: " + exception.getSystemId());
				System.out.println("Public id: " + exception.getPublicId());
				System.out.println("Line: " + exception.getLineNumber());
				System.out.println("Column: " + exception.getColumnNumber());
				throw exception;
			}
		}

		@Override
		public void fatalError(SAXParseException exception) throws SAXException {
			if (strict) {
				System.out.println("FatalError: " + exception.getMessage());
				throw exception;
			}
		}

		@Override
		public void warning(SAXParseException exception) throws SAXException {
			if (strict) {
				System.out.println("Warning: " + exception.getMessage());
			}
		}
	
	}
}
