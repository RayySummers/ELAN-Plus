package mpi.eudico.server.corpora.clomimpl.dobes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import mpi.eudico.server.corpora.clom.Property;
import mpi.eudico.server.corpora.clom.TimeSlot;
import mpi.eudico.server.corpora.clomimpl.abstr.ExternalReferenceImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.LinkedFileDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.Parser;
import mpi.eudico.server.corpora.clomimpl.abstr.PropertyImpl;

import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A (SAX2) Parser for Elan Annotation Format (EAF) compliant XML files.
 *
 * @author Hennie Brugman
 * @author Han Sloetjes
 * @version 1-Dec-2003
 * @version jun 2004 addition of ControlledVocabularies
 * @version sep 2005 the constructor is now public giving up the singleton pattern
 * the path parameter of all getter methods can be removed in the next parser version
 * (replace by a public parse(String path) method)
 * @version Feb 2006 support for LinkedFleDescrptors and for stereotype
 * Included In is added. For compatibility reasons the filename parameter to the getters is maintained.
 * @version Dec 2006 element PROPERTY has been added to the HEADER element, attribute
 * ANNOTATOR has been added to element TIER
 * @version Nov 2007 EAF v2.5, added attribute RELATIVE_MEDIA_URL to MEDIA_DESCRIPTOR and
 * RELATIVE_LINK_URL to LINKED_FILE_DESCRIPTOR
 * @version May 2008 added attributes and elements concerning DCR references
 */
public class EAF27Parser extends Parser {
	private SAXParser parser;

	/** stores tier name - tier record pairs */
    private final HashMap<String, TierRecord> tierMap = new HashMap<String, TierRecord>();

    /** a map with tier name - ArrayList with Annotation Records pairs */
    private final Map<String, ArrayList<AnnotationRecord>> tiers = new HashMap<String, ArrayList<AnnotationRecord>>();

    /** a list of tier names */
    private final ArrayList<String> tierNames = new ArrayList<String>();

    /** a list of type records */
    private final ArrayList<LingTypeRecord> linguisticTypes = new ArrayList<LingTypeRecord>();

    /** a list of Locale objects */
    private final ArrayList<Locale> locales = new ArrayList<Locale>();

    /** a map of time slot id to value, as strings */
    private final HashMap<String, String> timeSlots = new HashMap<String, String>();

    /** stores the ControlledVocabulary objects by their ID */
    private final HashMap<String, CVRecord> controlledVocabularies = new HashMap<String, CVRecord>();

    /** stores the Lexicon Service objects */
    private final HashMap<String, LexiconServiceRecord> lexiconServices = new HashMap<String, LexiconServiceRecord>();
    
    private final ArrayList<Property> docProperties = new ArrayList<Property>();
    
    private final Map<String, ExternalReferenceImpl> extReferences = new HashMap<String, ExternalReferenceImpl>();

    /** stores the time slots ordered by id */
    private final ArrayList<String> timeOrder = new ArrayList<String>(); // since a HashMap is not ordered, all time_slot_ids have to be stored in order separately.
    private String mediaFile;
    private ArrayList<MediaDescriptor> mediaDescriptors = new ArrayList<MediaDescriptor>();
    private ArrayList<LinkedFileDescriptor> linkedFileDescriptors = new ArrayList<LinkedFileDescriptor>();
    private String author;
    private String currentTierId;
    private String currentAnnotationId;
    private AnnotationRecord currentAnnRecord;
    private String currentCVId;
    private CVEntryRecord currentEntryRecord;
    private String content = "";
    private String lastParsed = "";
    private String currentFileName;
    private String currentPropertyName;
    private String fileFormat;

    /**
     * Constructor, creates a new SAXParser
     *
     */
    public EAF27Parser() {
    	try {
    		boolean validate = Boolean.parseBoolean(System.getProperty("ELAN.EAF.Validate", "true"));
    		boolean nsAware = Boolean.parseBoolean(System.getProperty("ELAN.EAF.NamespaceAware", "true"));
        	
			// first create a parser factory
    		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
			parserFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
			parserFactory.setNamespaceAware(nsAware);
			
			if (validate) {
				// to get a validating parser, set the schema to the proper xsd schema
				SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
				Schema eafSchema = schemaFactory.newSchema(
						this.getClass().getResource(EAF27.EAF27_SCHEMA_RESOURCE));
				
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
     * For backward compatibility; not used anymore
     *
     * @param fileName the eaf filename, parameter also for historic reasons
     *
     * @return media file name
     */
    @Override
	public String getMediaFile(String fileName) {
        parse(fileName);

        return mediaFile;
    }

    /**
     * Returns the media descriptors
     *
     * @param fileName the eaf filename, parameter also for historic reasons
     *
     * @return the media descriptors
     */
    @Override
	public ArrayList<MediaDescriptor> getMediaDescriptors(String fileName) {
        parse(fileName);

        return mediaDescriptors;
    }

    /**
     * Returns the linked file descriptors
     *
     * @param fileName the eaf file name, for historic reasons
     *
     * @return a list of linked file descriptors
     */
    @Override
	public ArrayList<LinkedFileDescriptor> getLinkedFileDescriptors(String fileName) {
        parse(fileName);

        return linkedFileDescriptors;
    }

    /**
     * @param fileName the source file
     *
     * @return the name of the author or owner
     */
    @Override
	public String getAuthor(String fileName) {
        parse(fileName);

        return author;
    }

    /**
     * Returns a list of PropertyImpl objects that have been retrieved from the eaf.
     *
	 * @see mpi.eudico.server.corpora.clomimpl.abstr.Parser#getTranscriptionProperties(java.lang.String)
	 */
	@Override
	public ArrayList<Property> getTranscriptionProperties(String fileName) {
		parse(fileName);

		return docProperties;
	}

	/**
     * @param fileName the source file
     *
     * @return a list of tier type records
     */
    @Override
	public ArrayList<LingTypeRecord> getLinguisticTypes(String fileName) {
        parse(fileName);

        return linguisticTypes;
    }

    /**
     * @param fileName the source file
     *
     * @return an ordered list of time slot id's
     */
    @Override
	public ArrayList<String> getTimeOrder(String fileName) {
        parse(fileName);

        return timeOrder;
    }

    /**
     * @param fileName the source file
     *
     * @return a map with slot id's as keys and slot time values as values
     */
    @Override
	public HashMap<String, String> getTimeSlots(String fileName) {
        parse(fileName);

        return timeSlots;
    }

	/**
	 * Returns a Hashtable of ArrayLists with the cv id's as keys.<br>
	 * Each ArrayList can contain one String, the description and an
	 * unknown number of CVEntryRecords.
	 *
	 * @param fileName the eaf filename
	 *
	 * @return a Hashtable of ArrayLists with the cv id's as keys
	 */
    @Override
	public HashMap<String, CVRecord> getControlledVocabularies(String fileName) {
    	parse(fileName);

    	return controlledVocabularies;
    }

    /**
     * Returns a HashMap of LexiconServiceRecords with the lexicon names as keys<br>
     * 
     * @param fileName the eaf filename
     * @return a String to LexiconServiceRecord map
     */
    @Override
	public HashMap<String, LexiconServiceRecord> getLexiconServices(String fileName) {
    	parse(fileName);
    	
    	return lexiconServices;
    }
    
    /**
     * Returns the names of the Tiers that are present in the Transcription
     * file
     *
     * @param fileName the source file
     *
     * @return a list of tier names
     */
    @Override
	public ArrayList<String> getTierNames(String fileName) {
        parse(fileName);

        return tierNames;
    }

    /**
     * Returns participant attribute of a tier.
     * The tier record is not used in TranscriptionStore yet.
     *
     * @param tierName name of tier
     * @param fileName the eaf
     *
     * @return the participant
     */
    @Override
	public String getParticipantOf(String tierName, String fileName) {
        parse(fileName);

        if (tierMap.get(tierName) != null) {
        	if (tierMap.get(tierName).getParticipant() != null) {
        		return tierMap.get(tierName).getParticipant();
        	}
        }

        return "";
    }

    /**
     * Returns the annotator attribute of a tier.
     * The tier record is not used in TranscriptionStore yet.
     *
     * @param tierName name of tier
     * @param fileName the eaf
     *
     * @return the annotator of the tier
     */
    @Override
	public String getAnnotatorOf(String tierName, String fileName) {
    	parse(fileName);

        if (tierMap.get(tierName) != null) {
        	if (tierMap.get(tierName).getAnnotator() != null) {
        		return tierMap.get(tierName).getAnnotator();
        	}
        }

        return "";
	}


	/**
     * Returns the name of the linguistic type of a tier.
     * The tier record is not used in TranscriptionStore yet.
     *
     * @param tierName the name of the tier
     * @param fileName the eaf
     *
     * @return name of the type
     */
    @Override
	public String getLinguisticTypeIDOf(String tierName, String fileName) {

        parse(fileName);

        if (tierMap.get(tierName) != null) {
        	if (tierMap.get(tierName).getLinguisticType() != null) {
        		return tierMap.get(tierName).getLinguisticType();
        	}
        }

        return "";
    }

    /**
     * Returns the Locale object for a tier.
     *
     * @param tierName the name of the tier
     * @param fileName the eaf
     *
     * @return the default Locale object
     */
    @Override
	public Locale getDefaultLanguageOf(String tierName, String fileName) {
        parse(fileName);

        Locale resultLoc = null;

        String localeId = null;
        if (tierMap.get(tierName) != null) {
            localeId = tierMap.get(tierName).getDefaultLocale();
        }

        Iterator<Locale> locIter = locales.iterator();

        while (locIter.hasNext()) {
            Locale l = locIter.next();

            if (l.getLanguage().equals(localeId)) {
                resultLoc = l;
            }
        }

        return resultLoc;
    }

    /**
     * Returns the name of the parent tier, if any.
     *
     * @param tierName the name of the tier
     * @param fileName the eaf
     *
     * @return the name of the parent tier, or null
     */
    @Override
	public String getParentNameOf(String tierName, String fileName) {
        parse(fileName);

        if (tierMap.get(tierName) != null) {
            return tierMap.get(tierName).getParentTier();
        }

        return null;
    }

    /**
     * Returns a ArrayList with the Annotations for this Tier. Each
     * AnnotationRecord contains begin time, end time and text values
     *
     * @param tierName the name of the tier
     * @param fileName the eaf
     *
     * @return ArrayList of AnnotationRecord objects for the tier
     */
    @Override
	public List<AnnotationRecord> getAnnotationsOf(String tierName, String fileName) {
        // make sure that the correct file has been parsed
        parse(fileName);

        return tiers.get(tierName);
    }

    @Override
	public Map<String, ExternalReferenceImpl> getExternalReferences (String fileName) {
    	parse(fileName); //historic reasons
    	
    	return extReferences;
    }
    
    /**
     * @see mpi.eudico.server.corpora.clomimpl.abstr.Parser#getFileFormat()
     */
    @Override
    public int getFileFormat() {
    	int result = 0;
    	if (fileFormat != null) {
	    	// split string in parts separated by dot.
	    	String parts[] = fileFormat.split("\\.");
	    	try {
		    	result = Integer.parseInt(parts[0]) * 1000 * 1000;
		    	
		    	if (parts.length >= 2) {
		    		result += Integer.parseInt(parts[1]) * 1000;
		        	if (parts.length >= 3) {
		        		result += Integer.parseInt(parts[2]);
		        	}
		    	}
	    	} catch (NumberFormatException nfe) {
	    		
	    	}
    	}
    	
    	return result;
    }

    /**
     * Reset data for a fresh parse.
     */
    private void clear() {
        // (re)set everything to null for each parse
        tiers.clear();
        tierNames.clear(); // HB, 2-1-02, to store name IN ORDER
        //tierAttributes.clear();
        mediaFile = "";
        linguisticTypes.clear();
        locales.clear();
        timeSlots.clear();
        timeOrder.clear();
        mediaDescriptors.clear();
        linkedFileDescriptors.clear();
        controlledVocabularies.clear();
    }
    
    /**
     * Parses a {@code EAF v2.7 (or <)} xml file.
     *
     * @param fileName the EAF v2.6 xml file that must be parsed.
     */
    private void parse(String fileName) {
        //long start = System.currentTimeMillis();

        //		System.out.println("Parse : " + fileName);
        //		System.out.println("Free memory : " + Runtime.getRuntime().freeMemory());
        // only parse the same file once
        if (lastParsed.equals(fileName)) {
            return;
        }

        // parse the file
        lastParsed = fileName;
        currentFileName = fileName;

        clear();
        
        // get an input source for the file name, create a content handler
        // and call parse.
        try {
        	InputSource is = ACMTranscriptionStore.toInputSource(fileName);
        	EAFContentHandler contentHandler = new EAFContentHandler(
        			parser.isNamespaceAware());
        	
        	parser.parse(is, contentHandler);
        } catch (IOException ioe) {
        	System.out.println("IO error: " + ioe.getMessage());
        } catch (SAXException saex) {
        	System.out.println("Parsing error: " + saex.getMessage());
        }
        //long duration = System.currentTimeMillis() - start;

        //	System.out.println("Parsing took " + duration + " milli seconds");
    }

    /**
     * An error handler for the eaf parser.<br>
     * The exception thrown (by Xerces 2.6.2) contains apart from file name,
     * line and column number, only a description of the problem in it's message.
     * To really deal with a problem a handler would need to parse the message
     * for certain strings (defined in a Xerces resource .properties file) and/or
     * read the file to the specified problem line.
     * Problematic...
     *
     * @author Han Sloetjes, MPI
     */
    class EAFErrorHandler implements ErrorHandler {

		@Override
		public void error(SAXParseException exception) throws SAXException {
			System.out.println("Error: " + exception.getMessage());
			// system id is the file path
			System.out.println("System id" + exception.getSystemId());
			System.out.println("Public id" + exception.getPublicId());
			System.out.println("Line: " + exception.getLineNumber());
			System.out.println("Column: " + exception.getColumnNumber());
		}

		@Override
		public void fatalError(SAXParseException exception) throws SAXException {
			System.out.println("FatalError: " + exception.getMessage());

		}

		@Override
		public void warning(SAXParseException exception) throws SAXException {
			System.out.println("Warning: " + exception.getMessage());

		}

    }

    /**
     * EAF 2.7 content handler.
     */
    class EAFContentHandler extends DefaultHandler {
    	private boolean namespaceAware = true;
    	
    	/**
    	 * Constructor for namespace aware parsing.
    	 */
        public EAFContentHandler() {
			this(true);
		}

        /**
         * Constructor with namespace parameter.
         * 
         * @param namespaceAware if {@code false} the parser and content
         * handler ignore namespaces
         */
        public EAFContentHandler(boolean namespaceAware) {
			super();
			this.namespaceAware = namespaceAware;
		}

        @Override
		public void startElement(String nameSpaceURI, String name,
            String rawName, Attributes attributes) throws SAXException {
            //	System.out.println("startElement called for name:" + name);
            content = "";
            // use the rawName instead of name parameter
            if (!namespaceAware) name = rawName;
            
            if (name.equals("ANNOTATION_DOCUMENT")) {
                author = attributes.getValue("AUTHOR");
                fileFormat = attributes.getValue("FORMAT");
            } else if (name.equals("HEADER")) {
                mediaFile = attributes.getValue("MEDIA_FILE");
            } else if (name.equals("MEDIA_DESCRIPTOR")) {
                String mediaURL = attributes.getValue("MEDIA_URL");
                String mimeType = attributes.getValue("MIME_TYPE");

                MediaDescriptor md = new MediaDescriptor(mediaURL, mimeType);

                long timeOrigin = 0;

                if (attributes.getValue("TIME_ORIGIN") != null) {
                    timeOrigin = Long.parseLong(attributes.getValue(
                                "TIME_ORIGIN"));
                    md.timeOrigin = timeOrigin;
                }

                String extractedFrom = "";

                if (attributes.getValue("EXTRACTED_FROM") != null) {
                    extractedFrom = attributes.getValue("EXTRACTED_FROM");
                    md.extractedFrom = extractedFrom;
                }
                // eaf 2.5 addition
                String relURL = attributes.getValue("RELATIVE_MEDIA_URL");
                if (relURL != null) {
                	md.relativeMediaURL = relURL;
                }

                mediaDescriptors.add(md);
            } else if (name.equals("LINKED_FILE_DESCRIPTOR")) {
                String linkURL = attributes.getValue("LINK_URL");
                String mime = attributes.getValue("MIME_TYPE");
                LinkedFileDescriptor lfd = new LinkedFileDescriptor(linkURL, mime);

                if (attributes.getValue("TIME_ORIGIN") != null) {
                    try {
                        long origin = Long.parseLong(attributes.getValue("TIME_ORIGIN"));
                        lfd.timeOrigin = origin;
                    } catch (NumberFormatException nfe) {
                        System.out.println("Could not parse the time origin: " + nfe.getMessage());
                    }
                }

                String assoc = attributes.getValue("ASSOCIATED_WITH");
                if (assoc != null) {
                    lfd.associatedWith = assoc;
                }

                // eaf 2.5 addition
                String relURL = attributes.getValue("RELATIVE_LINK_URL");
                if (relURL != null) {
                	lfd.relativeLinkURL = relURL;
                }

                linkedFileDescriptors.add(lfd);
            } else if (name.equals("PROPERTY")) {
                // transcription properties
            	currentPropertyName = attributes.getValue("NAME");
            } else if (name.equals("TIME_ORDER")) {
                // nothing to be done, tierOrder ArrayList already created
            } else if (name.equals("TIME_SLOT")) {
                String timeValue = String.valueOf(TimeSlot.TIME_UNALIGNED);

                if (attributes.getValue("TIME_VALUE") != null) {
                    timeValue = attributes.getValue("TIME_VALUE");
                }

                timeSlots.put(attributes.getValue("TIME_SLOT_ID"), timeValue);
                timeOrder.add(attributes.getValue("TIME_SLOT_ID"));
            } else if (name.equals("TIER")) {
                currentTierId = attributes.getValue("TIER_ID");

                // First check whether this tier already exists
                if (!tiers.containsKey(currentTierId)) {
                    // create a record
                    TierRecord tr = new TierRecord();
                    tr.setName(currentTierId);
                    tierMap.put(currentTierId, tr);

                    tr.setParticipant(attributes.getValue("PARTICIPANT"));
                    tr.setAnnotator(attributes.getValue("ANNOTATOR"));
                    tr.setLinguisticType(attributes.getValue(
                            "LINGUISTIC_TYPE_REF"));
                    tr.setDefaultLocale(attributes.getValue("DEFAULT_LOCALE"));
                    tr.setParentTier(attributes.getValue("PARENT_REF"));

                    // create entries in the tiers and tierAttributes HashMaps for annotations and attributes resp.
                    tiers.put(currentTierId, new ArrayList<AnnotationRecord>());

                    tierNames.add(currentTierId);
                }
            } else if (name.equals("ALIGNABLE_ANNOTATION")) {
                currentAnnotationId = attributes.getValue("ANNOTATION_ID");

                // create new "AnnotationRecord" and add to annotations HashMap for current tier
                ////
                currentAnnRecord = new AnnotationRecord();
                currentAnnRecord.setAnnotationId(currentAnnotationId);
                // ignore any attribute SVG_REF, it is not supported any more
                currentAnnRecord.setAnnotationType(AnnotationRecord.ALIGNABLE);
				currentAnnRecord.setBeginTimeSlotId(attributes.getValue("TIME_SLOT_REF1"));
				currentAnnRecord.setEndTimeSlotId(attributes.getValue("TIME_SLOT_REF2"));
				currentAnnRecord.setExtRefId(attributes.getValue("EXT_REF"));
				
				tiers.get(currentTierId).add(currentAnnRecord);

            } else if (name.equals("REF_ANNOTATION")) {
                currentAnnotationId = attributes.getValue("ANNOTATION_ID");

                // create new "AnnotationRecord" and add to annotations HashMap for current tier
                ////
                 currentAnnRecord = new AnnotationRecord();
                 currentAnnRecord.setAnnotationId(currentAnnotationId);
                 currentAnnRecord.setAnnotationType(AnnotationRecord.REFERENCE);
                 currentAnnRecord.setReferredAnnotId(attributes.getValue("ANNOTATION_REF"));
				if (attributes.getValue("PREVIOUS_ANNOTATION") != null) {
				    currentAnnRecord.setPreviousAnnotId(attributes.getValue("PREVIOUS_ANNOTATION"));
				} else {
				    currentAnnRecord.setPreviousAnnotId("");
				}
				currentAnnRecord.setExtRefId(attributes.getValue("EXT_REF"));
				
				tiers.get(currentTierId).add(currentAnnRecord);

            } else if (name.equals("ANNOTATION_VALUE")) { // For CVEntryID is case of an external CV
//    			currentAnnRecord.setCvEntryId(attributes.getValue("CVEntryID"));
    		} else if (name.equals("LINGUISTIC_TYPE")) {
            	LingTypeRecord ltr = new LingTypeRecord();

				ltr.setLingTypeId(attributes.getValue(
						"LINGUISTIC_TYPE_ID"));

                String timeAlignable = "true";

                if ((attributes.getValue("TIME_ALIGNABLE") != null) &&
                        (attributes.getValue("TIME_ALIGNABLE").equals("false"))) {
                    timeAlignable = "false";
                }

                ltr.setTimeAlignable(timeAlignable);

                // ignore any attribute GRAPHIC_REFERENCES, it is not supported any more

                String stereotype = attributes.getValue("CONSTRAINTS");
                ltr.setStereoType(stereotype);
                
                if(stereotype != null && stereotype.startsWith("Symbolic")) {
                    ltr.setTimeAlignable("false");
                }

				ltr.setControlledVocabulary(
					attributes.getValue("CONTROLLED_VOCABULARY_REF"));
				
				ltr.setLexiconReference(attributes.getValue("LEXICON_REF"));
				
				ltr.setExtRefId(attributes.getValue("EXT_REF"));

                linguisticTypes.add(ltr);
            } 
            
    		// Load the Lexicon Query Bundle or Lexicon Link
    		else if (name.equals("LEXICON_REF")) {
    			String lexiconSrvcRef = attributes.getValue("LEX_REF_ID");	// Ref of LexiconQueryBundle
    			String lexiconClientName = attributes.getValue("NAME");		// Name of LexiconClientService
    			String lexiconSrvcType = attributes.getValue("TYPE");		// Type of LexiconClientService
    			String lexiconSrvcUrl = attributes.getValue("URL");			// URL of LexiconClientService
    			String lexiconSrvcId = attributes.getValue("LEXICON_ID");	// ID of Lexicon
    			String lexiconSrvcName = attributes.getValue("LEXICON_NAME");	// Name of Lexicon
    			String dataCategory = attributes.getValue("DATCAT_NAME");	// Name of LexicalEntryField
    			String dataCategoryId = attributes.getValue("DATCAT_ID");	// ID of LexicalEntryField
    			
    			LexiconServiceRecord lsr = new LexiconServiceRecord();
    			lsr.setName(lexiconClientName);
    			lsr.setType(lexiconSrvcType);
    			lsr.setUrl(lexiconSrvcUrl);
    			lsr.setLexiconId(lexiconSrvcId);
    			lsr.setLexiconName(lexiconSrvcName);
    			lsr.setDatcatName(dataCategory);
    			lsr.setDatcatId(dataCategoryId);
    			
    			lexiconServices.put(lexiconSrvcRef, lsr);
    		}

    		else if (name.equals("LOCALE")) {
                String langCode = attributes.getValue("LANGUAGE_CODE");
                String countryCode = attributes.getValue("COUNTRY_CODE");

                if (countryCode == null) {
                    countryCode = "";
                }

                String variant = attributes.getValue("VARIANT");

                if (variant == null) {
                    variant = "";
                }

                Locale l = new Locale(langCode, countryCode, variant);
                locales.add(l);
            } else if (name.equals("CONTROLLED_VOCABULARY")) {
    			currentCVId = attributes.getValue("CV_ID");
    			CVRecord cv = new CVRecord(currentCVId);

    			String desc = attributes.getValue("DESCRIPTION");
    			if (desc != null) {
    				cv.setDescription(desc);
    			}
    			
    			// by Micha: if it is an external CV it has an external reference
    			String extRefId = attributes.getValue("EXT_REF");
    			if (extRefId != null) {
    				cv.setExtRefId(extRefId);
    			}
    			
    			controlledVocabularies.put(currentCVId, cv);
    		} else if (name.equals("CV_ENTRY")) {
    			currentEntryRecord = new CVEntryRecord();

    			currentEntryRecord.setDescription(
    				attributes.getValue("DESCRIPTION"));
    			currentEntryRecord.setExtRefId(attributes.getValue("EXT_REF"));
    			currentEntryRecord.setId(attributes.getValue("ID"));

    			controlledVocabularies.get(currentCVId).addEntry(currentEntryRecord);
    		} else if (name.equals("EXTERNAL_REF")) {
    			String value = attributes.getValue("VALUE");
    			String type = attributes.getValue("TYPE");
    			String dcId = attributes.getValue("EXT_REF_ID");
    			if (value != null && value.length() > 0) {
    				ExternalReferenceImpl eri = new ExternalReferenceImpl(value, type);
    				extReferences.put(dcId, eri);
    			}
    		}
        }
         //startElement

        @Override
		public void endElement(String nameSpaceURI, String name, String rawName)
            throws SAXException {
            // use the rawName instead of name parameter
            if (!namespaceAware) name = rawName;
            
            if (name.equals("ANNOTATION_VALUE")) {
                currentAnnRecord.setValue(content);
            } else if (name.equals("CV_ENTRY")) {
            	currentEntryRecord.setValue(content);
            } else if (name.equals("PROPERTY")) {
            	if (content.length() > 0 && currentPropertyName != null) {
                	PropertyImpl prop = new PropertyImpl(currentPropertyName, content);
                	docProperties.add(prop);
            	}
            }
        }

        @Override
		public void characters(char[] ch, int start, int end)
            throws SAXException {
            content += new String(ch, start, end);
        }
        
		@Override
		public void error(SAXParseException exception) throws SAXException {
			System.out.println("Error: " + exception.getMessage());
			// system id is the file path
			System.out.println("System id" + exception.getSystemId());
			System.out.println("Public id" + exception.getPublicId());
			System.out.println("Line: " + exception.getLineNumber());
			System.out.println("Column: " + exception.getColumnNumber());
		}

		@Override
		public void fatalError(SAXParseException exception) throws SAXException {
			System.out.println("FatalError: " + exception.getMessage());

		}

		@Override
		public void warning(SAXParseException exception) throws SAXException {
			System.out.println("Warning: " + exception.getMessage());

		}
    }

}
