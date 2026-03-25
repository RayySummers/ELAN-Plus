package mpi.eudico.server.corpora.clomimpl.dobes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
 * A (SAX2) Parser for External CV (ECV) compliant XML files.
 * This is version 0.2. It will accept files of version 0.1 too.
 * Old files will typically refer to the EAFv2.7.xsd schema; if they mention the new EAFv2.8.xsd
 * schema, it may not allow the old form and diagnostics will be printed. This is harmless.
 * 
 * @author Micha Hulsbosch
 * @author Olaf Seibert
 * @version jan 2014
 */
public class ECV02Parser {
	/** the sax parser */
    private SAXParser parser;
    private boolean strict;

    /** the url of the external CV */
    private String url;

    /** the expected External ControlledVocabulary objects */
    private List<ExternalCV> cvList;
    /** the External ControlledVocabulary objects that were found but not expected */
    private List<ExternalCV> unexpectedCvList;
    /** stores external references: maps id -> external reference */
	private final Map<String, ExternalReference> extReferences = new HashMap<String, ExternalReference>();
	/** maps CVEntry to the id of the external reference it references */
	private final Map<CVEntry, String> cvEntryExtRef = new HashMap<CVEntry, String>();
    private String currentCVId;
    private ControlledVocabulary currentCV;
    private String currentEntryDesc;
    private String currentEntryExtRef;
    private String content = "";
	//private String author;
    /** the current entry ID */
	public String currentEntryId;

    /**
     * Creates a ECV02Parser instance
     * 
     * @param url (the url of the external CV)
     * @throws ParseException any SAX parse exception
     */
    public ECV02Parser(String url) throws ParseException {
    	this(url, false);
    }

	/**
	 * Creates a ECV02Parser instance
	 * 
	 * @param url (the url of the external CV)
	 * @param strict (whether the error handler should be used)
	 */
	public ECV02Parser(String url, boolean strict) throws ParseException {
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
						this.getClass().getResource(ACMTranscriptionStore.getCurrentEAFSchemaLocal()));
				
				parserFactory.setSchema(eafSchema);// the validating flag should be false (the default)
			} else {
				parserFactory.setValidating(false);// superfluous, false is already the default
			}
			
			parser = parserFactory.newSAXParser();
    	} catch (SAXException se) {
    		se.printStackTrace();
    	} catch (Throwable thr) {
    		thr.printStackTrace();
    	}
	}

	/**
	 * Tries to parse the ECV file using the url.
	 * <p>
	 * You can pass a list of ECVs that you expect to find, and when found, they are modified
	 * and filled with the entries that are found.
	 * Any extra ECVs can be retrieved via {@link #getExtraControlledVocabularies()}.
	 * {@link #getControlledVocabularies()} delivers the list of expected ECVs.
	 * <p>
	 * If you pass null, you simply get all ECVs that are found.
	 * In that case, they are all returned via {@link #getControlledVocabularies()}.
	 * {@link #getExtraControlledVocabularies()} delivers the same list.
	 * @param ecvList the list of ECV's
	 * @throws ParseException any SAX parse exception
	 */
	public void parse(List<ExternalCV> ecvList) throws ParseException {
		if (ecvList != null) {
			cvList = ecvList;
			unexpectedCvList = null;
		} else {
			cvList = new ArrayList<ExternalCV>();
			unexpectedCvList = cvList;
		}
		
		//
        // get an input source for the URL, create a content handler
        // and call parse.
        try {
        	InputSource is = ACMTranscriptionStore.toInputSource(url);
        	ECV02Handler ecvHandler = new ECV02Handler();
        	
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

	/**
	 * Search if the given ECV name occurs on the list of expected ECVs.
	 * If so, return that ECV.
	 * <br>
	 * Otherwise, create a new ECV and put it on the 'unexpected' list. 

	 * @param name the name of the ExternalCV to find or create
	 * @return the ECV
	 */
	private ExternalCV findOrCreate(String name) {
        for (int j = 0; j < cvList.size(); j++) {
        	ExternalCV cvFromList = cvList.get(j);
     
        	if (name.equals(cvFromList.getName())) {
        		return cvFromList;
        	}
        }
        if (unexpectedCvList == null) {
        	unexpectedCvList = new ArrayList<ExternalCV>();
        }
        ExternalCV ecv = new ExternalCV(name);
        unexpectedCvList.add(ecv);
        return ecv;
	}

	private void createObjects() {
		// post-processing of ext_ref's of CV entries
        if (cvEntryExtRef.size() > 0) {
        	for (Entry<CVEntry, String> mapentry : cvEntryExtRef.entrySet()) {
        		CVEntry entry = mapentry.getKey();
        		String erId = mapentry.getValue();
        		
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
     * @return cvList (List containing CVs)
     */
    public List<ControlledVocabulary> getControlledVocabularies() {
    	return Collections.<ControlledVocabulary>unmodifiableList(cvList);
    }

    /**
     * Returns a list of additional {@code ControlledVocabulary} objects.
     * 
     * @return cvList (List containing unexpected CVs)
     */
    public List<ControlledVocabulary> getExtraControlledVocabularies() {
    	return Collections.<ControlledVocabulary>unmodifiableList(unexpectedCvList);
    }

    /**
     * Returns a map with {@code ExternalReference} values.
     * 
     * @return extReferences (the external reference)
     */
    public Map<String, ExternalReference> getExternalReferences() {
		return extReferences;
	}
    
    /**
     * ECV02Handler
     * 
     * The content handler for the SAX parser
     * 
     * @author Micha Hulsbosch
     *
     */
	public class ECV02Handler extends DefaultHandler {
		//private Locator locator;
		private ExternalCVEntry currentEntry;
		private boolean controlledVocabularyIsMultiLanguage;
		private String currentEntryLangRef;
//	    private final Map<String, String> languages = new HashMap<String, String>();
        private List<LanguageRecord> languages = new ArrayList<LanguageRecord>();

        /**
         * Creates a new handler.
         */
		public ECV02Handler() {
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
            if (name.equals("CV_ENTRY")) {				// Deprecated in EAF 2.8 / ECV 0.2
            	if (!controlledVocabularyIsMultiLanguage) {
	            	CVEntry entry = new ExternalCVEntry(currentCV, content, currentEntryDesc, currentEntryId);
	                currentCV.addEntry(entry);
	                if (currentEntryExtRef != null) {
	                	cvEntryExtRef.put(entry, currentEntryExtRef);
	                }
            	}
            } else if (name.equals("DESCRIPTION")) {	// New for 2.8 / 0.2
            	if (content.length() > 0) {
            		int index = currentCV.getNumberOfLanguages() - 1;
            		currentCV.setDescription(index, content);
            	}
            } else if (name.equals("CVE_VALUE")) {		// New for 2.8 / 0.2
        		int index = currentCV.getIndexOfLanguage(currentEntryLangRef);
        		if (index >= 0) {
	            	currentEntry.setDescription(index, currentEntryDesc);
	                currentEntry.setValue(index, content);
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
            } else if (name.equals("LANGUAGE")) {					// New in 2.8
    			String id = attributes.getValue("LANG_ID");
    			String def = attributes.getValue("LANG_DEF");
    			String label = attributes.getValue("LANG_LABEL");
    			if (id != null && def != null) {
    				languages.add(new LanguageRecord(id, def, label));
    			}
            } else if (name.equals("CONTROLLED_VOCABULARY")) {
            	currentCVId = attributes.getValue("CV_ID");
                currentCV = findOrCreate(currentCVId);
    			controlledVocabularyIsMultiLanguage = false;		// initial value

                String desc = attributes.getValue("DESCRIPTION");	// Deprecated in 2.8
                if (desc != null) {
                    currentCV.setDescription(desc);
                }
    			controlledVocabularyIsMultiLanguage = false;		// initial value
  
                // To be discussed whether a CV in an ECV can have an EXT_REF
    			// However, at this point we haven't seen the <EXTERNAL_REF> elements yet
    			// so we can't look them up either.
            	String extRefId = attributes.getValue("EXT_REF");
            	if (extRefId != null) {
            		ExternalReference eri = extReferences.get(extRefId);
        			if (eri != null) {
        				try {
        					((ExternalCV) currentCV).setExternalRef(eri.clone());
        				} catch (CloneNotSupportedException cnse) {
        					//LOG.severe("Could not set the external reference: " + cnse.getMessage());
        				}
        			}
            	}
                
            } else if (name.equals("DESCRIPTION")) {				// New in 2.8
    			controlledVocabularyIsMultiLanguage = true;

            	String shortId = attributes.getValue("LANG_REF");
    			if (shortId != null) {
    				for (LanguageRecord lr : languages) {
    					if (shortId.equals(lr.getId())) {
    						String longId = lr.getDef();
    						String label = lr.getLabel();
    	    				int index =	currentCV.addLanguage(shortId, longId, label);
    	    				break;
    					}
    				}
    			}

            } else if (name.equals("CV_ENTRY")) {			// Deprecated in 2.8
                currentEntryDesc = attributes.getValue("DESCRIPTION");
                currentEntryExtRef = attributes.getValue("EXT_REF");
                currentEntryId = attributes.getValue("CVE_ID");
            } else if (name.equals("CV_ENTRY_ML")) {	// New for 2.8
    			assert(controlledVocabularyIsMultiLanguage);
    			currentEntry = new ExternalCVEntry(currentCV);
    			
    			String cveID = attributes.getValue("CVE_ID");
    			String extRefID = attributes.getValue("EXT_REF");
    			
    			currentEntry.setId(cveID);
    			
    			currentCV.addEntry(currentEntry);
                if (extRefID != null && !extRefID.isEmpty()) {
                	cvEntryExtRef.put(currentEntry, extRefID);
                }
            } else if (name.equals("CVE_VALUE")) {		// New for 2.8
    			assert(controlledVocabularyIsMultiLanguage);
    			currentEntryDesc = attributes.getValue("DESCRIPTION");
    			currentEntryLangRef = attributes.getValue("LANG_REF");          	
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
				System.out.println("Error:     " + exception.getMessage());
				// system id is the file path
				System.out.println("System id: " + exception.getSystemId());
				System.out.println("Public id: " + exception.getPublicId());
				System.out.println("Line:      " + exception.getLineNumber());
				System.out.println("Column:    " + exception.getColumnNumber());
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
