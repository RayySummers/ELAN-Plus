package mpi.eudico.server.corpora.clomimpl.flex;

import mpi.eudico.server.corpora.clom.DecoderInfo;
import mpi.eudico.server.corpora.clom.ExternalReference;
import mpi.eudico.server.corpora.clom.Property;
import mpi.eudico.server.corpora.clomimpl.abstr.ExternalReferenceImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.Parser;
import mpi.eudico.server.corpora.clomimpl.abstr.PropertyImpl;
import mpi.eudico.server.corpora.clomimpl.dobes.AnnotationRecord;
import mpi.eudico.server.corpora.clomimpl.dobes.LanguageRecord;
import mpi.eudico.server.corpora.clomimpl.dobes.LingTypeRecord;
import mpi.eudico.server.corpora.clomimpl.dobes.TierRecord;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.util.MediaDescriptorUtility;
import nl.mpi.util.FileUtility;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * A parser for FLEx files.
 *
 * @author Han Sloetjes
 * @author Aarthy Somasundarm, Feb 2013
 */
public class FlexParser extends Parser {	
    private SAXParser parser;

    private ContainerElem topElement;    
    
    private FlexDecoderInfo decoder;   
    
    /** stores tier name - tier record pairs */
    private final HashMap<String, TierRecord> tierMap = new HashMap<String, TierRecord>();
    
 	/** stores LingTypeName - LingTypeRecord pairs */
    private HashMap<String, LingTypeRecord> lingTypeRecords = new HashMap<String, LingTypeRecord>();
    
    /** stores language - font-vernacular pairs */
    private HashMap<String, String> langMap = new HashMap<String, String>();
    /** stores Language Records if it was possible to retrieve/construct those */
    private final List<LanguageRecord> languages = new ArrayList<LanguageRecord>();
    
    private ArrayList<Property> properties;
    
    /** stores ParticipantName - PrefixUsed  */
    private HashMap<String, String> participantMap = new HashMap<String, String>();   
    
    private TreeSet<String> tierNameSet = new TreeSet<String>();
    
    private ArrayList<long[]> timeOrder = new ArrayList<long[]>(); // of long[2], {id,time}
    private ArrayList<long[]> timeSlots = new ArrayList<long[]>(); // of long[2], {id,time}
    
    private ArrayList<AnnotationRecord> annotationRecords = new ArrayList<AnnotationRecord>();
    private HashMap<String, ArrayList<AnnotationRecord>> tierNameToAnnRecordMap = new HashMap<String, ArrayList<AnnotationRecord>>();
    private HashMap<AnnotationRecord, String> annotRecordToTierMap = new HashMap<AnnotationRecord, String>();
    
    // add a way to ensure that there is always a parent-tier-per-level so that an empty
    // annotation can be created if a potential child annotation is there but no parent
    private HashMap<String, String> parentPerLevel = new HashMap<String, String>(8);
    
    // while parsing, store all tiers created for each element
    private HashMap<String, LinkedHashSet<String>> tiersPerLevel = new HashMap<String, LinkedHashSet<String>>(8);
    
    private HashMap<String, ExternalReferenceImpl> externalRefMap;
    
    private ArrayList<MediaDescriptor> mediaDescriptors;

    private List<String> unitLevels;
   
    // maintain a mapping of guid-id, in order to be able to reconstruct the annotation order
    private HashMap<String, String> guidIdMap = new HashMap<String, String>();
    
    private final static String EXT_REF_ID = "er1";   
    private int annotId = 1;
    private int tsId = 1;
    private final static String ANN_ID_PREFIX = "ann";
    private final static String TS_ID_PREFIX = "ts";
    private final String DEL = "-"; 
    private final String PARTICIPANT_DEL ="_";
    
    private final String UNKNOWN = "Not Specified";
    
    private boolean parsed = false;   
    
    private String txtLanguage;
    
    /**
     * Creates a new FlexParser instance
     */
    public FlexParser() {
    	unitLevels = new ArrayList<String> (5);
    	unitLevels.add(FlexConstants.MORPH);
    	unitLevels.add(FlexConstants.WORD);
    	unitLevels.add(FlexConstants.PHRASE);
    	unitLevels.add(FlexConstants.PARAGR);
    	unitLevels.add(FlexConstants.IT);
    	
    	mediaDescriptors = new ArrayList<MediaDescriptor>();

        try {
	   		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
	   		parserFactory.setNamespaceAware(true);
	   		parserFactory.setValidating(false);
	   		parser = parserFactory.newSAXParser();
        } catch (SAXException se) {
            //se.printStackTrace();
        	System.out.println("Error creating a SAX Parser: " + se.getMessage());
            //throw new ParseException(se);
        } catch (ParserConfigurationException e) {
			//e.printStackTrace();
        	System.out.println("Error configuring a SAX Parser: " + e.getMessage());
			//throw new ParseException(e);
		} 
    }
    
    /**
     * Sets the decoder info object, containing user provided information
     * for the parser.
     */    
	@Override
	public void setDecoderInfo(DecoderInfo decoderInfo) {
		if (decoderInfo instanceof FlexDecoderInfo) {
			decoder = (FlexDecoderInfo) decoderInfo;
		}			
	}
	
	/**
	 * Returns a map of id - ext ref objects pairs. 
	 * 
	 * @param fileName the transcription file
	 * @return a map with id - object mappings, null by default
	 */
	@Override
	public Map<String, ExternalReferenceImpl> getExternalReferences (String fileName) {
		return externalRefMap;
	}
	
	/**
     * July 2018: now returns null, the item's language is set as the tier's 
     * content language from now on.
     * 
     * @param tierName the name of the tier
     * @param fileName the file name
     * @return the Locale, now null 
     */    
	@Override
	public Locale getDefaultLanguageOf(String tierName, String fileName) {
		return null;
		/*
    	parse(fileName);
		
    	TierRecord tr = tierMap.get(tierName);
    	if (tr != null && tr.getDefaultLocale() != null) {
    		// could/should check the length of the language string?
    		return new Locale(tr.getDefaultLocale());
    	}
    	// use "en" if no other specified. otherwise possible ID conflict with the "en-US" default of a tier.
        return new Locale("en");
        */
	}

    /**
     * Returns a list of media descriptors, if available
     *
     * @param fileName the file name
     *
     * @return a list of media descriptors
     */
    @Override
    public ArrayList<MediaDescriptor> getMediaDescriptors(String fileName) {
    	parse(fileName);
    	
    	List<MediaDescriptor> list = decoder.getMediaDescriptors();
    	
    	if(list != null){
    		for(int i = 0; i< list.size(); i++){
    			if(!mediaDescriptors.contains(list.get(i))){
    				mediaDescriptors.add(list.get(i));
    			}
    		}
    	}
    	
    	return mediaDescriptors;
    }

	/**
     * Returns a list of linguistic type records.
     *
     * @param fileName the file name
     *
     * @return a list of linguistic type records
     */
    @Override
    public ArrayList<LingTypeRecord> getLinguisticTypes(String fileName) {
    	parse(fileName);
    	
    	return new ArrayList<LingTypeRecord>(lingTypeRecords.values());
    }

    /**
     * Returns list of Strings in format "ts" + nnn.  Assumes that this method
     * is only called once.
     *
     * @param fileName the file name
     *
     * @return a list of time slot id's
     */
    @Override
    public ArrayList<String> getTimeOrder(String fileName) {
    	parse(fileName);
    	
        ArrayList<String> resultTimeOrder = new ArrayList<String>();

        for (int i = 0; i < timeOrder.size(); i++) {
            resultTimeOrder.add(TS_ID_PREFIX +
                (timeOrder.get(i))[0]);
        }

        return resultTimeOrder;
    }


    /**
     * Returns a map of time slot id's to time values, all as strings. This is
     * not the most effective solution, but adheres to the Parser calls.
     * Assumes that this method is only called once.
     *
     * @param fileName the parsed file
     *
     * @return mappings of time slot id's to time values
     */
    @Override
    public HashMap<String, String> getTimeSlots(String fileName) {
    	parse(fileName);

        HashMap<String, String> resultSlots = new HashMap<String, String>();

        Iterator<long[]> timeSlotIter = timeSlots.iterator();

        while (timeSlotIter.hasNext()) {
            long[] timeSlot = timeSlotIter.next();
            String tsId = TS_ID_PREFIX + ((long) timeSlot[0]);
            String timeValue = Long.toString(((long) timeSlot[1]));

            resultSlots.put(tsId, timeValue);
        }

        return resultSlots;       
    }

	/**
     * Creates a list of tier names.  Assumes
     * that this method is only called once.
     *
     * @param fileName the file name
     *
     * @return a list of tier names
     */
    @Override
    public ArrayList<String> getTierNames(String fileName) {
    	parse(fileName);
        return new ArrayList<String>(tierNameSet);
    }


	/**
     * Returns the participant part of the tier name, if any
     *
     * @param tierName the tier name
     * @param fileName the file name
     *
     * @return the participant
     */
    @Override
    public String getParticipantOf(String tierName, String fileName) {
    	parse(fileName);
    	
    	TierRecord tr = tierMap.get(tierName);
    	if (tr != null) {
    		return tr.getParticipant();
    	}	

        return null;
    }

	/**
     * Returns the name of the linguistic type for the specified tier.
     *
     * @param tierName name of the tier
     * @param fileName the file name
     *
     * @return the name of the linguistic type
     */
    @Override
    public String getLinguisticTypeIDOf(String tierName, String fileName) {
    	parse(fileName);
    	
    	TierRecord tr = tierMap.get(tierName);
    	if (tr != null) {
    		return tr.getLinguisticType();
    	}	

        return null;
    }

    /**
     * Returns the parent tier name for the specified tier
     *
     * @param tierName the tier name
     * @param fileName the file name
     *
     * @return the parent tier name
     */
    @Override
    public String getParentNameOf(String tierName, String fileName) {
    	parse(fileName);
    	
    	TierRecord tr = tierMap.get(tierName);
    	if (tr != null) {
    		return tr.getParentTier();
    	}
    	
        return null;
    }

	
	/**
     * Returns a list of AnnotationRecords for the given tier.
     *
     * @param tierName the name of the tier
     * @param fileName the file name (for historic reasons)
     *
     * @return a list of AnnotationRecords
     */
    @Override
    public ArrayList<AnnotationRecord> getAnnotationsOf(String tierName, String fileName) {
    	parse(fileName);
    	
        ArrayList<AnnotationRecord> records = tierNameToAnnRecordMap.get(tierName);

        if (records == null) {
            records = new ArrayList<AnnotationRecord>(0);
        }

        return records;
    }
    
    /**
	 * Returns a list of the document (header) properties.
	 * 
	 * @param fileName the file to be parsed
	 * 
	 * @return a list of the document (header) properties
	 */
	@Override
	public ArrayList<Property> getTranscriptionProperties(String fileName) {
		parse(fileName);
    	
    	if(properties == null){
    		properties = new ArrayList<Property>();
    		String langPropValue = null;
    		
    		Iterator<Entry<String, String>> it = langMap.entrySet().iterator();
    		while (it.hasNext()){
    			Entry<String, String> entry = it.next();
    			Property prop = new PropertyImpl(entry.getKey(), entry.getValue());
    			
    			properties.add(prop);
    			
    			if(langPropValue == null){
    				langPropValue = entry.getKey();
    			} else {
    				langPropValue = langPropValue + " " + entry.getKey();
    			}
    		}
    		
    		
    		if(langPropValue != null){
    			Property prop = new PropertyImpl(FlexConstants.LANGUAGES, langPropValue);
    			
    			properties.add(prop);
    		}
    	}     	
    	return properties;
	}
	
	/**
	 * @param fileName the file to be parsed
	 * @return a list of Language Records retrieved from the flextext file.
	 */
    @Override
	public List<LanguageRecord> getLanguages(String fileName) {
    	parse(fileName); // for historic reasons
		
		return languages;
	}

    /**
     * @param tierName the name of the tier to retrieve the language for
     * @param fileName the file to be parsed
     * @return the language reference as a string or null
     */
	@Override
	public String getLangRefOf(String tierName, String fileName) {
		parse(fileName); // for historic reasons
		
    	TierRecord tr = tierMap.get(tierName);
    	if (tr != null) {
    		// could/should check the length of the language string?
    		return tr.getLangRef();// could be null
    	}
    	
		return null;
	}

	private void parse(String fileName) {
        if (parsed) {
            return;
        }
       
        if (decoder == null) {
        	// create one with default values
        	setDecoderInfo(new FlexDecoderInfo());
        }
        
        topElement = null;
        
        try {
        	URL originalFileURL = new URL(FileUtility.pathToURLString(fileName));
        	//extract participant info from note field 
            if(decoder.importParticipantInfo){
            	try {            		
                  	URL flextTextURL = FlexParser.class.getResource( "/mpi/eudico/resources/flexTransformation.xsl"); 
                  	if(flextTextURL != null ){                  		
                  		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                       	
                      	TransformerFactory.newInstance().newTransformer(new StreamSource(
              				 flextTextURL.openStream())).transform(new StreamSource(originalFileURL.openStream()),
              				            new StreamResult(outputStream));
                        parser.parse(new InputSource(new ByteArrayInputStream(outputStream.toByteArray())), 
                        		new FlexContentHandler());
                        parsed = true;
                  	} else {
                  		 System.out.println("Unknown Error in the transformation file: /mpi/eudico/resources/flexTransformation.xsl");
                      	 parsed = false;
                  	}
            	} catch (MalformedURLException mue) { 
            		System.out.println("URL error" + mue.getMessage());
            		parsed = false;
            	} catch (TransformerException te) {
            		System.out.println("Transformation error" + te.getMessage());
            		parsed = false;
       			} catch (TransformerFactoryConfigurationError e) {
       				System.out.println("Transformer Factory Configuration error" + e.getMessage());
       				parsed = false;
       			}  
            } 
            
            if(!parsed){
            	parser.parse(new InputSource(originalFileURL.openStream()), new FlexContentHandler());
            }
          
            parsed = true;
            
            checkParentPerLevel();
            preprocessRecords(topElement);			
			calculateDurations();
            createRecords();
            
        } catch (SAXException e) {
            System.out.println("Parsing error: " + e.getMessage());
            // set parsed flag to true to prevent multiple failing attempts
            parsed = true;
        } catch (IOException ioe) {
            System.out.println("IO error: " + ioe.getMessage());
            // set parsed flag to true to prevent multiple attempts
            parsed = true;
        }
    }
    
    /**
     * creates a external reference for punct type annotations
     */
    private void createExternalRefForPunct(){
    	if(externalRefMap == null){
    		externalRefMap = new HashMap<String, ExternalReferenceImpl>();
    	
    		externalRefMap.put(EXT_REF_ID, new ExternalReferenceImpl(FlexConstants.PUNCT_ISOCAT_URL, ExternalReference.ISO12620_DC_ID));
    		
    	}
    } 
    
    /**
	 * Use the first txt per FLEx level or element as the parent for that level,
	 * if not there, just use the first value. For the phrase level it is now
	 * possible for the user to specify an alternative item type as the parent.
	 * Could be extended by using a user-preferred language.
	 */
	private void checkParentPerLevel() {
		for (Map.Entry<String, LinkedHashSet<String>> tierLevelEntry : tiersPerLevel.entrySet()) {
			String key = tierLevelEntry.getKey();
			LinkedHashSet<String> values = tierLevelEntry.getValue();
			String first = null;
			String txtstart = null;
			// on the phrase level, allow a custom item type to translate to  
			// become the parent tier for sibling types
			if (key.equals(FlexConstants.PHRASE)) {
				txtstart = key + DEL + decoder.topLevelItemType;
			} else {
				txtstart = key + DEL + FlexConstants.TXT;
			}			
			// first try to find a txt type
			boolean found = false;
			int count = 0;
			Iterator<String> valIt = values.iterator();
			while (valIt.hasNext()) {
				String nextVal = valIt.next();
				int subIndex = 2;
				if (decoder.useSpeakerAsTierPrefix) {
					subIndex = nextVal.indexOf(PARTICIPANT_DEL) + 1;
					subIndex = subIndex < 2 ? 2 : subIndex;
				}
				String combi = nextVal.substring(subIndex);
				if (count == 0) {
					first = combi;
					count++;
				}
				if (combi.startsWith(txtstart)) {
					txtLanguage = (combi.split("-"))[2];
					parentPerLevel.put(key, combi);
					found = true;
					break;
				}
			}
			if (!found) {
				parentPerLevel.put(key, first);
			}
		}
		
		if(txtLanguage == null){
			Iterator<String> it = langMap.keySet().iterator();
			if(it.hasNext()){
				txtLanguage = it.next();
			}
		}
	}
	
	/**
	 * Ensures that in every item list the per-level-parent is the first in the list.
	 * Add an empty item in case the per-level-parent is absent. 
	 */
	private void preprocessRecords(ContainerElem elem) {
    	if (elem == null) {
    		return;
    	}
		boolean parentFound = false;
				
    	if (elem.getItems() != null && elem.getItems().size() != 0) {
			for (int i = 0; i < elem.getItems().size(); i++) {
				Item item = elem.getItems().get(i);
				if (item.tierName != null) {
					if (item.tierName.contains(parentPerLevel.get(elem.flexType))) {
						parentFound = true;
						if (i != 0) {
							elem.getItems().add(0, elem.getItems().remove(i));
						}
						break;
					}
				}				
			}
			
			if (!parentFound) {				
				elem.getItems().add(0, getAnItemFor(elem));
			}
    	} else {
			elem.addItem(getAnItemFor(elem));
    	}
    	if (elem.getChildElems() != null && elem.getChildElems().size() > 0) {
    		for (ContainerElem celem : elem.getChildElems()) {
    			preprocessRecords(celem);
    		}
    	}
	}    
	
	/**
	 * Creates a new Item for the given ContainerElem
	 * 
	 * @param elem, ContainerElem for which a item has to be created
	 * @return Item
	 */
	private Item getAnItemFor(ContainerElem elem){	
		
		String ppl = parentPerLevel.get(elem.flexType);
		if (ppl == null) {			
			ppl = elem.flexType + DEL + FlexConstants.TXT + DEL + txtLanguage;
			parentPerLevel.put(elem.flexType, ppl);
		}
		
		Item empty = new Item();
		String typeLang = ppl.substring(ppl.indexOf(elem.flexType)+1);
		empty.type = typeLang.substring(0, typeLang.indexOf(DEL));
		empty.lang = typeLang.substring(typeLang.lastIndexOf(DEL)+1);
		
		if(!FlexConstants.IT.equals(elem.flexType) && !FlexConstants.PARAGR.equals(elem.flexType)){
			String speaker = getParticipantForSpeaker(elem.speaker);
			//if(speaker >= 65 ){
    		if(speaker != null){
    			empty.tierName =  speaker + PARTICIPANT_DEL + ppl;
    		}else {
    			empty.tierName = ppl;
    		}
		} else {
			empty.tierName = ppl;
		}
		
		return empty;
	}
    /**
     * Converts the stored container elements and items to tier records 
     * and annotation records.
     *
     */
    private void createRecords() {
    	if (topElement == null) {
    		return;
    	}
    	
    	// the top element is always "interlinear-text"
    	AnnotationRecord par = null;
    	String topLevelTierName = null;
    	if (decoder.inclITElement) {
    		par = null;    		
    		
    		if (topElement.getItems() != null && topElement.getItems().size() > 0) {
    			
    			for (int i = 0; i < topElement.getItems().size(); i++) {
    				Item item = topElement.getItems().get(i);
    				String tName = item.tierName;
    				tierNameSet.add(tName);
    				
    				TierRecord tr = new TierRecord();
    				tr.setName(tName);
    				tr.setDefaultLocale(item.lang);
    				tr.setLangRef(item.lang);
    				tierMap.put(tName, tr);
    				
    				if (i == 0) {
        				tr.setLinguisticType(getLingType(FlexConstants.IT, item));
        				topLevelTierName = tName;
    					par = createAnnotationRecord(tName, null, null, topElement.bt, topElement.et);
    					par.setValue(item.value);
    					
    					if (topElement.id != null) {
    						// store guid-id mapping
    						guidIdMap.put(topElement.id, par.getAnnotationId());
    						par.setAnnotationId(par.getAnnotationId() + FlexConstants.FLEX_GUID_ANN_PREFIX + topElement.id);
    					}    					
    				} else {
        				tr.setLinguisticType(getLingItemType(FlexConstants.IT, item));
        				tr.setParentTier(topLevelTierName);
	    				AnnotationRecord child = createRefAnnotationRecord(tName, par, null);
	    				child.setValue(item.value);
    				}
    			}
    		} else {    			
        		tierNameSet.add(FlexConstants.IT);
        		TierRecord tr = new TierRecord();
        		tr.setLinguisticType(getLingType(FlexConstants.IT, null));
        		tr.setName(FlexConstants.IT);
        		tierMap.put(FlexConstants.IT, tr);        			
    			par = createAnnotationRecord(FlexConstants.IT, null, null, topElement.bt, topElement.et);
    			if (topElement.id != null) {
					guidIdMap.put(topElement.id, par.getAnnotationId());
					par.setAnnotationId(par.getAnnotationId() +FlexConstants.FLEX_GUID_ANN_PREFIX + topElement.id);
				}
    		}
    	} 
   	
    	
    	List<ContainerElem> childElems = topElement.getChildElems();
    		
    	if (childElems != null && childElems.size() > 0) {
    		par = null;
    		
    		if(decoder.inclParagraphElement){
    			//HashMap<Character, AnnotationRecord> recordMap = new HashMap<Character, AnnotationRecord>();
    			List<String> participants = new ArrayList<String>();
    			participants.addAll(participantMap.values());
    			
    			for (int i = 0; i < childElems.size(); i++) {
    				ContainerElem elem = childElems.get(i);
    				
    				createPhraseChildRecords(elem);
    			}
			} else{
				//phrase element
				for (int i = 0; i < childElems.size(); i++) {
					ContainerElem elem = childElems.get(i);	
	    			
	    			if (elem.getItems() != null && elem.getItems().size() > 0) {
	    				for (int j = 0; j < elem.getItems().size(); j++) {            			
	    					Item item = elem.getItems().get(j);
	    					String tName = item.tierName;
	    					if(!tierNameSet.contains(tName)){
	    						tierNameSet.add(tName);
		    					TierRecord tr = new TierRecord();   			    		
			    			    tr.setName(tName);			    			   
			    			    tr.setDefaultLocale(item.lang);	
			    			    tr.setLangRef(item.lang);
			    			    if(elem.speaker != null && !elem.speaker.equals(UNKNOWN)){				    		
						    		tr.setParticipant(elem.speaker);
						    	}
			    			    
			    			    if (j == 0) {
	    			    			tr.setLinguisticType(getLingType(elem.flexType, item));
	    			    		} else {
	    			    			 tr.setLinguisticType(getLingItemType(elem.flexType, item));
	    			    			 tr.setParentTier(topLevelTierName);	
	    			    		}
			    			    
			    			    tierMap.put(tName, tr);
	    					}
	    					
	    					if (j == 0) {
    	    					topLevelTierName = tName;
    	    					par = createAnnotationRecord(tName, null, null, elem.bt, elem.et);
    	    					par.setValue(item.value);
    	    					if (elem.id != null) {
    	    						guidIdMap.put(elem.id, par.getAnnotationId());
    	    						par.setAnnotationId(par.getAnnotationId() + FlexConstants.FLEX_GUID_ANN_PREFIX + elem.id);
    	    					}
    	    				} else {
    	    					AnnotationRecord child = createRefAnnotationRecord(tName, par, null);
    	    					child.setValue(item.value);
    	    				}
	    				}
	    			} else {// should not happen	    				
	    				String tName = participantMap.get(elem.speaker).toString() + PARTICIPANT_DEL + elem.flexType;
						 
						if(!tierNameSet.contains(tName)){
							tierNameSet.add(tName);
							
							TierRecord tr = new TierRecord();
					    	tr.setLinguisticType(getLingType(elem.flexType, null));
					    	tr.setName(tName);
					    	if(elem.speaker != null && !elem.speaker.equals(UNKNOWN)){				    		
					    		tr.setParticipant(elem.speaker);
					    	}
					    	tierMap.put(tName, tr);
						}
						
						// create empty annotation on phrase tier
						par = createAnnotationRecord(tName, par, null, elem.bt, elem.et);					
						if (elem.id != null) {
							guidIdMap.put(elem.id, par.getAnnotationId());
							par.setAnnotationId(par.getAnnotationId() + FlexConstants.FLEX_GUID_ANN_PREFIX + elem.id);
						}
    				}
	    			createChildRecords(elem, par);
	    		}
			}
    	}
    }
    
    /**
     * Creates tier and annotation records of the specified paragraph
     * element and for the children phrase elements.
     * 
     * The passed element has not been processed.
     * 
     * @param parElem the parent paragraph element 
     */
    private void createPhraseChildRecords(ContainerElem parElem) {
    	if (parElem.getChildElems() == null || parElem.getChildElems().size() == 0) {
    		return;
    	}
		AnnotationRecord nextPar = null;
		AnnotationRecord prevAnn = null;
		String firstItemTierName = null;	
		
		HashMap<String, AnnotationRecord> parentAnnMap = new HashMap<String, AnnotationRecord>();
    	
		for (int i = 0; i < parElem.getChildElems().size(); i++) {
			ContainerElem elem = parElem.getChildElems().get(i);		
			
			AnnotationRecord parentAnn = parentAnnMap.get(getParticipantForSpeaker(elem.speaker));
			if(parentAnn == null){
				// create paragraph annotation for this participant
				String tierName = getParticipantForSpeaker(elem.speaker) + PARTICIPANT_DEL + parElem.flexType;   
				
				if(!tierNameSet.contains(tierName)){
					tierNameSet.add(tierName);
					
					TierRecord tr = new TierRecord();
					tr.setLinguisticType(getLingType(parElem.flexType, null));
					tr.setName(tierName);
					if(elem.speaker != null && !elem.speaker.equals(UNKNOWN)){				    		
			    		tr.setParticipant(elem.speaker);
			    	}
					tierMap.put(tierName, tr);
				}
				
				//create empty
				parentAnn = createAnnotationRecord(tierName, null, null, parElem.bt, parElem.et);        				
				if (elem.id != null) {
					guidIdMap.put(elem.id, parentAnn.getAnnotationId());
					parentAnn.setAnnotationId(parentAnn.getAnnotationId() + FlexConstants.FLEX_GUID_ANN_PREFIX + parElem.id);
				}
				// save the parent annotation in the map
				parentAnnMap.put(getParticipantForSpeaker(elem.speaker), parentAnn);
			}	
			
			if (elem.getItems() != null && elem.getItems().size() > 0) {
				for (int j = 0; j < elem.getItems().size(); j++) {
					Item item = elem.getItems().get(j);
					String tName = item.tierName;
					
					if (!tierNameSet.contains(tName)) {
						tierNameSet.add(tName);
			    		TierRecord tr = new TierRecord();
			    		tr.setName(tName);
			    		tr.setDefaultLocale(item.lang);
			    		tr.setLangRef(item.lang);
			    		if(elem.speaker != null && !elem.speaker.equals(UNKNOWN)){				    		
				    		tr.setParticipant(elem.speaker);
				    	}
			    		if(j==0){
			    			tr.setLinguisticType(getLingType(elem.flexType, item));
			    			tr.setParentTier(annotRecordToTierMap.get(parentAnn));
			    		}else{
			    			tr.setLinguisticType(getLingItemType(elem.flexType,item));
				    		tr.setParentTier(firstItemTierName);
			    		}
			    		
			    		tierMap.put(tName, tr);
					}
					
					if (j == 0) {
						firstItemTierName = tName;
			    		if (isAlignable(elem.flexType)) {
			    			nextPar = createAnnotationRecord(tName, parentAnn, prevAnn, elem.bt, elem.et);
			    			
			    		} else {
			    			nextPar = createRefAnnotationRecord(tName, parentAnn, prevAnn);
			    		}
			    		if (elem.id != null) {
		    				guidIdMap.put(elem.id, nextPar.getAnnotationId());
		    				nextPar.setAnnotationId(nextPar.getAnnotationId() + FlexConstants.FLEX_GUID_ANN_PREFIX + elem.id);
		    			}
			    		
			    		if(item.type.equals(FlexConstants.PUNCT)){
			    			nextPar.setExtRefId(EXT_REF_ID);
			    			createExternalRefForPunct();
    					}
			    		
			    		nextPar.setValue(item.value);
			    		prevAnn = nextPar;
					} else {
    					AnnotationRecord child = createRefAnnotationRecord(tName, nextPar, null);
	    				child.setValue(item.value);
	    				if(item.type.equals(FlexConstants.PUNCT)){
	    					child.setExtRefId(EXT_REF_ID);
	    					createExternalRefForPunct();
    					}
					}
				}
				
				if (nextPar != null) {
					createChildRecords(elem, nextPar);
				}
			} else {
				if(firstItemTierName != null){
					String tName = participantMap.get(elem.speaker).toString() + PARTICIPANT_DEL + elem.flexType;		
				
					if(!tierNameSet.contains(firstItemTierName)){
						tierNameSet.add(firstItemTierName);
					
						TierRecord tr = new TierRecord();
						tr.setLinguisticType(getLingType(elem.flexType, null));
						tr.setName(tName);
						tr.setParentTier(annotRecordToTierMap.get(parentAnn));
						if(elem.speaker != null && !elem.speaker.equals(UNKNOWN)){				    		
							tr.setParticipant(elem.speaker);
						}
						tierMap.put(firstItemTierName, tr);
					}
				
				// create empty annotation 
				if (isAlignable(elem.flexType)) {
					nextPar = createAnnotationRecord(firstItemTierName, parentAnn, prevAnn, elem.bt, elem.et);
					if (elem.id != null) {
						guidIdMap.put(elem.id, nextPar.getAnnotationId());
	    				nextPar.setAnnotationId(nextPar.getAnnotationId() + FlexConstants.FLEX_GUID_ANN_PREFIX + elem.id);
	    			}
				} else {
					nextPar = createRefAnnotationRecord(firstItemTierName, parentAnn, prevAnn);
				}	
				
				prevAnn = nextPar;
				}
				
				if (nextPar != null) {
					createChildRecords(elem, nextPar);
				}
			}
		}
    }
    
    /**
     * Creates tier and annotation records for the children of the specified element.
     * The passed element has already been processed.
     * 
     * @param parElem the parent Element
     * @param parentAnn the parent annotation to add direct children to
     */
    private void createChildRecords(ContainerElem parElem, AnnotationRecord parentAnn) {
    	if (parElem.getChildElems() == null || parElem.getChildElems().size() == 0) {
    		return;
    	}
		AnnotationRecord nextPar = null;
		AnnotationRecord prevAnn = null;
		String firstItemTierName = null;		
    	
		for (int i = 0; i < parElem.getChildElems().size(); i++) {
			ContainerElem elem = parElem.getChildElems().get(i);		
			
			if (elem.getItems() != null && elem.getItems().size() > 0) {
				for (int j = 0; j < elem.getItems().size(); j++) {
					Item item = elem.getItems().get(j);
					String tName = item.tierName;
					
					if (!tierNameSet.contains(tName)) {
						tierNameSet.add(tName);
			    		TierRecord tr = new TierRecord();
			    		tr.setName(tName);
			    		tr.setDefaultLocale(item.lang);
			    		tr.setLangRef(item.lang);
			    		if(elem.speaker != null && !elem.speaker.equals(UNKNOWN)){				    		
				    		tr.setParticipant(elem.speaker);
				    	}
			    		if(j==0){
			    			tr.setLinguisticType(getLingType(elem.flexType, item));
			    			tr.setParentTier(annotRecordToTierMap.get(parentAnn));
			    		}else{
			    			tr.setLinguisticType(getLingItemType(elem.flexType,item));
				    		tr.setParentTier(firstItemTierName);
			    		}
			    		
			    		tierMap.put(tName, tr);
					}
					
					if (j == 0) {
						firstItemTierName = tName;
			    		if (isAlignable(elem.flexType)) {
			    			nextPar = createAnnotationRecord(tName, parentAnn, prevAnn, elem.bt, elem.et);
			    			
			    		} else {
			    			nextPar = createRefAnnotationRecord(tName, parentAnn, prevAnn);
			    		}
			    		if (elem.id != null) {
		    				guidIdMap.put(elem.id, nextPar.getAnnotationId());
		    				nextPar.setAnnotationId(nextPar.getAnnotationId() + FlexConstants.FLEX_GUID_ANN_PREFIX + elem.id);
		    			}
			    		
			    		if(item.type.equals(FlexConstants.PUNCT)){
			    			nextPar.setExtRefId(EXT_REF_ID);
			    			createExternalRefForPunct();
    					}
			    		
			    		nextPar.setValue(item.value);
			    		prevAnn = nextPar;
					} else {
    					AnnotationRecord child = createRefAnnotationRecord(tName, nextPar, null);
	    				child.setValue(item.value);
	    				if(item.type.equals(FlexConstants.PUNCT)){
	    					child.setExtRefId(EXT_REF_ID);
	    					createExternalRefForPunct();
    					}
					}
				}
				
				if (nextPar != null) {
					createChildRecords(elem, nextPar);
				}
			} else {
				if(firstItemTierName != null){
					String tName = participantMap.get(elem.speaker).toString() + PARTICIPANT_DEL + elem.flexType;		
				
					if(!tierNameSet.contains(firstItemTierName)){
						tierNameSet.add(firstItemTierName);
					
						TierRecord tr = new TierRecord();
						tr.setLinguisticType(getLingType(elem.flexType, null));
						tr.setName(tName);
						tr.setParentTier(annotRecordToTierMap.get(parentAnn));
						if(elem.speaker != null && !elem.speaker.equals(UNKNOWN)){				    		
							tr.setParticipant(elem.speaker);
						}
						tierMap.put(firstItemTierName, tr);
					}
				
				// create empty annotation 
				if (isAlignable(elem.flexType)) {
					nextPar = createAnnotationRecord(firstItemTierName, parentAnn, prevAnn, elem.bt, elem.et);
					if (elem.id != null) {
						guidIdMap.put(elem.id, nextPar.getAnnotationId());
	    				nextPar.setAnnotationId(nextPar.getAnnotationId() + FlexConstants.FLEX_GUID_ANN_PREFIX + elem.id);
	    			}
				} else {
					nextPar = createRefAnnotationRecord(firstItemTierName, parentAnn, prevAnn);
				}	
				
				prevAnn = nextPar;
				}
				
				if (nextPar != null) {
					createChildRecords(elem, nextPar);
				}
			}
		}
    }
    
    /** 
     * Returns whether a tier of a certain level (phrase, word, morph etc) is 
     * alignable, according to the settings.
     * 
     * @param tierLevel the level identifier
     * @return true if it is alignable, false otherwise
     */
    private boolean isAlignable(String tierLevel) {
 	   return unitLevels.indexOf(tierLevel) >= 
 		   unitLevels.indexOf(decoder.smallestWithTimeAlignment);
    }
    
    /**
     * Returns the prefix for the given speaker
     * 
     * @param speaker the speaker attribute
     * @return the participant prefix, can be the same as the speaker attribute
     */
    private String getParticipantForSpeaker(String speaker){
    	if(participantMap.containsKey(speaker)){
        	return  participantMap.get(speaker);
        }
    	
    	if (decoder.useSpeakerAsTierPrefix) {
    		if (!speaker.equals(UNKNOWN)) {
    			participantMap.put(speaker, speaker);
    			return speaker;
    		}
    	}
    	// create a prefix string for speaker unknown, might not start with 'A'
    	String participant  = String.valueOf((char) (participantMap.size()+'A'));
        participantMap.put(speaker, participant);
        return participant;  
    }
    
    /**
     * Returns the linguistic type for FLEx elements.
     * 
     * @param flexType type of FLEx element
     * @param item the item type
     * @return the name of a tier type
     */
    private String getLingType(String flexType, Item item) {    	
    	String lingName = flexType;
    	
    	if(flexType.equals(FlexConstants.IT)){     			
			lingName = FlexConstants.TXT;   
    	}
    	
    	if(item != null){
			if(decoder.createLingForNewType){    				
				if(item.type.equals(FlexConstants.PUNCT)){
					lingName = lingName + DEL + FlexConstants.TXT;
				} else {
					lingName = lingName + DEL + item.type;
				}
				
				if(decoder.createLingForNewLang && item.lang != null){
					lingName = lingName + DEL + item.lang;
				}
			}
		} 
    	
    	LingTypeRecord lt = lingTypeRecords.get(lingName);
    	
    	if(lt == null){
    		boolean alignable = false;
    		String stereoType = null;  		
    		
    		if(flexType.equals(FlexConstants.IT)){     			
    			alignable = true;
            	stereoType = null;            	
        	}  
    		else if(flexType.equals(FlexConstants.PARAGR)){
        		alignable = true;
            	stereoType = null;            	
        	} 
    		else if(flexType.equals(FlexConstants.PHRASE)){
        		alignable = true;
        		
            	//if paragraph tier is included, then phrase should be its child tier
            	if(decoder.inclParagraphElement){
            		stereoType = Constraint.stereoTypes[Constraint.INCLUDED_IN];
            	} else {
            	   	//if paragraph tier is not included, then phrase tier is the toplevel tier
            		stereoType = null;
            	}
        	}     	
        	else if(flexType.equals(FlexConstants.WORD)){           		        		
        		if(decoder.smallestWithTimeAlignment.equals(FlexConstants.WORD)){
        			alignable = true;
        			stereoType = Constraint.stereoTypes[Constraint.TIME_SUBDIVISION];
        	    } else {
        	    	alignable = false;
        	    	stereoType = Constraint.stereoTypes[Constraint.SYMBOLIC_SUBDIVISION];
        	    }        	    
        	}        	
        	else if(flexType.equals(FlexConstants.MORPH)){
        		alignable = false;        	
        		stereoType = Constraint.stereoTypes[Constraint.SYMBOLIC_SUBDIVISION];
        	}    	
    		
    		lt  = new LingTypeRecord();
    		lt.setLingTypeId(lingName);
    	    lt.setTimeAlignable(String.valueOf(alignable));
    	    lt.setStereoType(stereoType);
    	    lingTypeRecords.put(lingName, lt);
		}
    	
    	return lt.getLingTypeId(); 
    }
        
    /**
     * Returns the linguistic type for FLEx item elements.
     * 
     * @param flexType type of FLEx element item
     * @param item the item type
     * 
     * @return the name of the tier type
     */
    private String getLingItemType(String flexType, Item item){  
    	String lingName;
    	
    	if(decoder.createLingForNewType){
    		if(item.type.equals(FlexConstants.PUNCT)){
    			lingName = flexType + DEL + FlexConstants.TXT;
        	} else {
        		lingName = flexType + DEL + item.type;
        	}
    		if(decoder.createLingForNewLang && !item.type.equals(FlexConstants.TYPE)){
    			lingName = lingName + DEL + item.lang;
    		}
    	}else{
    		lingName = flexType + DEL + FlexConstants.ITEM;
    	}
    		
    	LingTypeRecord lt = lingTypeRecords.get(lingName);
    	if(lt == null){
    		lt  = new LingTypeRecord();
        	lt.setLingTypeId(lingName);
            lt.setTimeAlignable("false");
            lt.setStereoType(Constraint.stereoTypes[Constraint.SYMBOLIC_ASSOCIATION]);
            lingTypeRecords.put(lingName, lt);
    	}
    	
    	return lt.getLingTypeId();  
    }
    
    /**
     * Creates an annotation record.
     * 
     * @param tierName the name of the tier
     * @param par the parent annotation
     * @param prev the previous annotation
     * @param bt the begin time
     * @param et the end time
     * @return a record for an annotation
     */
    private AnnotationRecord createAnnotationRecord(String tierName, 
    		AnnotationRecord par, AnnotationRecord prev, long bt, long et) {
    	AnnotationRecord ar = new AnnotationRecord();    	
    	ar.setAnnotationId(ANN_ID_PREFIX + annotId++);
    	ar.setAnnotationType(AnnotationRecord.ALIGNABLE);
    	    	  	
    	String stereotype = lingTypeRecords.get(tierMap.get(tierName).getLinguisticType()).getStereoType();

    	if (par != null && stereotype != null && !stereotype.equals(Constraint.publicStereoTypes[Constraint.INCLUDED_IN])) {
    		if (prev != null) {
    			String oldEndTSId = prev.getEndTimeSlotId(); 
    			ar.setEndTimeSlotId(prev.getEndTimeSlotId());
    			int beginTSId = tsId++;
    			String nextId = TS_ID_PREFIX + beginTSId;
    			ar.setBeginTimeSlotId(nextId);
    			prev.setEndTimeSlotId(nextId);
    			// update all existing depending annotation records with the same end slot
    			updateAnnRecordEndTS(oldEndTSId, nextId, prev.getAnnotationId());
    			long[] ts = { beginTSId, bt};
    		       timeSlots.add(ts);
    		       timeOrder.add(ts);
    		} else {
    			ar.setBeginTimeSlotId(par.getBeginTimeSlotId());
    			ar.setEndTimeSlotId(par.getEndTimeSlotId());
    		}    		
    	} else {
    		int beginTSId = tsId++;
            int endTSId = tsId++;
            ar.setBeginTimeSlotId(TS_ID_PREFIX + beginTSId);
            ar.setEndTimeSlotId(TS_ID_PREFIX + endTSId);

            // time info
            long[] begin = { beginTSId, bt };
            long[] end = { endTSId, et };

            timeSlots.add(begin);
            timeSlots.add(end);

            timeOrder.add(begin);
            timeOrder.add(end);
    	}
    	
        annotationRecords.add(ar);

        addRecordToTierMap(ar, tierName);
        
    	return ar;
    }
    
    /**
     * Creates a reference annotation record.
     *
     * @param tierName the name of the tier
     * @param par the parent annotation
     * @param prev the previous annotation
     * @return an annotation record
     */
    private AnnotationRecord createRefAnnotationRecord(String tierName, 
    		AnnotationRecord par, AnnotationRecord prev) {
    	AnnotationRecord ar = new AnnotationRecord();
    	ar.setAnnotationId(ANN_ID_PREFIX + annotId++);
    	ar.setAnnotationType(AnnotationRecord.REFERENCE);
    	
    	if (par != null) {
    		ar.setReferredAnnotId(par.getAnnotationId());
    	} else {
    		// LOG error
    		System.out.println("Error: null as parent! " + tierName);
    	}
    	if (prev != null && prev.getAnnotationType() == AnnotationRecord.REFERENCE) {
			ar.setPreviousAnnotId(prev.getAnnotationId());
		}
    	
        annotationRecords.add(ar);
        addRecordToTierMap(ar, tierName);
        
    	return ar;
    }
    
   /**
    * Adds a record to the list of records of a specified tier. If the list
    * does not exist yet, it is created.
    *
    * @param annRec the record
    * @param tierName the tier name
    */
   private void addRecordToTierMap(AnnotationRecord annRec, String tierName) {
	   annotRecordToTierMap.put(annRec, tierName);

       if (tierNameToAnnRecordMap.containsKey(tierName)) {
           tierNameToAnnRecordMap.get(tierName).add(annRec);
       } else {
           ArrayList<AnnotationRecord> ar = new ArrayList<AnnotationRecord>();
           ar.add(annRec);
           tierNameToAnnRecordMap.put(tierName, ar);
       }
   }
   
   /**
    * Adjust records with annotation id > than the specified id.
    * 
    * @param oldEndTSId the end time slot id to find
    * @param nextId the new end time slot id
    * @param annotId only update annotations with an id higher than this id
    */
   private void updateAnnRecordEndTS(String oldEndTSId, String nextId, String annotId) {
	   try {
		   int refId = 0;
		   int index = annotId.indexOf(FlexConstants.FLEX_GUID_ANN_PREFIX);
		   if (index > 0) {
			   refId = Integer.parseInt(guidIdMap.get(annotId.substring(index + FlexConstants.FLEX_GUID_ANN_PREFIX.length())).substring(ANN_ID_PREFIX.length()));
		   } else {
			   refId = Integer.parseInt(annotId.substring(ANN_ID_PREFIX.length()));
		   }
		   int depId = 0;
		   
		   for (AnnotationRecord record : annotationRecords) {
			   if (record.getAnnotationType() == AnnotationRecord.ALIGNABLE) {
				  
				   if (record.getEndTimeSlotId() != null && record.getEndTimeSlotId().equals(oldEndTSId)) {
					   try {
						   index = record.getAnnotationId().indexOf(FlexConstants.FLEX_GUID_ANN_PREFIX);;
						   if (index > 0) {
							   depId = Integer.parseInt(guidIdMap.get(record.getAnnotationId().substring(index + FlexConstants.FLEX_GUID_ANN_PREFIX.length())).substring(
											   ANN_ID_PREFIX.length()));
						   } else {
							   depId = Integer.parseInt(
								   record.getAnnotationId().substring(ANN_ID_PREFIX.length()));
						   }
						   if (depId > refId) {
							   record.setEndTimeSlotId(nextId);
						   }
					   } catch (Exception ex) {
						   System.out.println("Cannot update depending annotation record: " 
								   + record.getAnnotationId()); 
					   }				   
				   }
			   }
		   }
	   } catch (NumberFormatException nfe) {
		   System.out.println("Cannot update depending annotation records of: " + annotId);
	   }
   }
   
   /**
    * Calculate the duration per unit, based on the information in the 
    * decoder object.
    */
   private void calculateDurations() {
	   List<ContainerElem> elements = topElement.getChildElems();
   		if (elements != null && elements.size() > 0) {
   			
   			if(decoder.inclParagraphElement){
   	   		    long paragraphBt= 0L;
				long curTime = 0L;	
				String prevSpeaker = null;
				
				for (int i = 0; i < elements.size(); i++) {
					ContainerElem elem = elements.get(i);
   	   				
					List<ContainerElem> phraseElems= elem.getChildElems();   
   	   				
   	   				if(phraseElems != null){
   	   					for(int j=0; j < phraseElems.size(); j++){
   	   						ContainerElem phraseElem = phraseElems.get(j);
   	   						String curSpeaker = phraseElem.speaker;
   	   						
	   	   					if(prevSpeaker == null || prevSpeaker.equals(curSpeaker)){
	   	   	   					// true for first element and element on same tier as previous element
	   	   						if(phraseElem.bt == -1 || phraseElem.bt < curTime ){
		   							phraseElem.bt = curTime;
		   						}   	   						
	   	   					} else {
	  							// find the previous element of the same speaker. This is maybe not very efficient?
	  							for (int k = j - 1; k >= 0; k--) {
	  								ContainerElem prevElem = elements.get(k);
	  								if (prevElem.et <= phraseElem.bt) {// no overlap, no correction
	  									break;
	  								}
	  								if (curSpeaker.equals(prevElem.speaker)) {
	  									//if (phraseElem.bt < prevElem.et) {// double check?
	  										phraseElem.bt = prevElem.et;
	  									//}
	  								}
	  							}
	   	   					}	   	   					
	   	  					// this check should be more sophisticated and take into account a possible 
	   	   	   				// next element's bt (of same speaker)
   	   						if( phraseElem.et < phraseElem.bt){
   	   							phraseElem.et = phraseElem.bt + decoder.perPhraseDuration;
   	   						}
   	   					
   	   						if(curTime < phraseElem.et){
   	   							curTime = phraseElem.et;
   	   						}
   	   				
   	   						if(j == 0){   	   						
   	   							paragraphBt = phraseElem.bt;
   	   						} 
   	   						
   	   						prevSpeaker = curSpeaker;
   	   					}
   	   					
   	   					elem.bt = paragraphBt;
   	   					elem.et = curTime;
   	   					paragraphBt = curTime;
   	   				}
   	   			}
				
				topElement.bt = 0L;
				topElement.et = curTime;
   			} else {
   				//ContainerElem phraseElem;
				//ContainerElem prevElem;
				long curTime = 0L;	
				//String curSpeaker;
				String prevSpeaker = null;
				
				for(int j=0; j < elements.size(); j++){
					ContainerElem phraseElem = elements.get(j);
					String curSpeaker = phraseElem.speaker;
   	   					
   	   				if(prevSpeaker == null || prevSpeaker.equals(curSpeaker)){
   	   					// true for first element and element on same tier as previous element
  						if(phraseElem.bt == -1 || phraseElem.bt < curTime ){
  							phraseElem.bt = curTime;
  						}    	   							
  					} else {// element on different tier, different speaker
  						if(phraseElem.bt == -1 ){
  							phraseElem.bt = curTime;
  						} else {
  							// find the previous element of the same speaker. This is maybe not very efficient?
  							for (int k = j - 1; k >= 0; k--) {
  								ContainerElem prevElem = elements.get(k);
  								if (prevElem.et <= phraseElem.bt) {// no overlap, no correction
  									break;
  								}
  								if (curSpeaker.equals(prevElem.speaker)) {
  									//if (phraseElem.bt < prevElem.et) {// double check?
  										phraseElem.bt = prevElem.et;
  									//}
  								}
  							} 							
  						}
  					}
  					// this check should be more sophisticated and take into account a possible 
   	   				// next element's bt (of same speaker)
  					if( phraseElem.et < phraseElem.bt){
  						phraseElem.et = phraseElem.bt + decoder.perPhraseDuration;
  					}
  					
  					if(curTime < phraseElem.et){
  						curTime = phraseElem.et;
  					}
  					
  					prevSpeaker = curSpeaker;
   	   			}
				
				topElement.bt = 0L;
				topElement.et = curTime;
   			}
   		}
   }  

    // ######################################################################
    /**
     * The SAX ContentHandler for FLEx files. 
     * The following elements can have "item" child elements:
     * "interlinear-text", "phrase", "word" and "morph" 
     * @author Han Sloetjes
     * @author Aarthy Somasundaram, Feb 2013
     */
    private class FlexContentHandler extends DefaultHandler {
        private ContainerElem curElem;
        private ContainerElem nextElem;
        private Item nextItem;
        private String type;
        private String participant;
        
        //Property prop;
        
        private StringBuilder content = new StringBuilder();
      
        /**
         * Creates container objects for selected elements and item objects
         * for any "item" element.
         * 
         * @param uri empty string
         * @param localName local element name
         * @param qName qualified element name
         * @param atts the attributes of the element
         *
         * @throws SAXException 
         */
        @Override
		public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
            //System.out.println("E: " + qName);

            if (FlexConstants.DOC.equals(qName)) {
            	return;
            }
            
            // always create a top element for the root regardless of setting for
            // include interlinear-text element
            if (FlexConstants.IT.equals(qName)) {
                topElement = new ContainerElem(FlexConstants.IT);
                curElem = topElement;            
                curElem.id = atts.getValue(FlexConstants.GUID);                
                return;
            }
         

            if (decoder.inclParagraphElement && FlexConstants.PARAGR.equals(qName)) {            
                nextElem = new ContainerElem(FlexConstants.PARAGR);
                nextElem.id = atts.getValue(FlexConstants.GUID);// can be null
                curElem.addElement(nextElem);
                curElem = nextElem;
            } 
            
            else if (FlexConstants.PHRASE.equals(qName)) {
                nextElem = new ContainerElem(FlexConstants.PHRASE);
                if(atts.getValue(FlexConstants.BEGIN_TIME) != null){
                	nextElem.bt = Long.parseLong(atts.getValue(FlexConstants.BEGIN_TIME));
                }
                
                if(atts.getValue(FlexConstants.END_TIME) != null){
                	 nextElem.et = Long.parseLong(atts.getValue(FlexConstants.END_TIME));
                }
                
                if(atts.getValue(FlexConstants.SPEAKER) != null){
                	nextElem.speaker = atts.getValue(FlexConstants.SPEAKER);
                }
                
                if(nextElem.speaker == null || nextElem.speaker.trim().length() == 0){
                	nextElem.speaker = UNKNOWN;
                }                
                participant = getParticipantForSpeaker(nextElem.speaker);                
                
                nextElem.id = atts.getValue(FlexConstants.GUID);
                curElem.addElement(nextElem);
                curElem = nextElem;
            }
            else if (FlexConstants.WORD.equals(qName)) {
                nextElem = new ContainerElem(FlexConstants.WORD);
                nextElem.id = atts.getValue(FlexConstants.GUID);// can be null
                nextElem.speaker = curElem.speaker;
                curElem.addElement(nextElem);
                curElem = nextElem;
            } else if (FlexConstants.MORPH.equals(qName)) {
                nextElem = new ContainerElem(FlexConstants.MORPH);
                nextElem.id = atts.getValue(FlexConstants.GUID);
                nextElem.speaker = curElem.speaker;
               
                String typeVal = atts.getValue(FlexConstants.TYPE);
                if ((typeVal != null) && (typeVal.length() > 0)) {
                    nextItem = new Item();
                    nextItem.type = FlexConstants.TYPE;
                    nextItem.value = typeVal;                    
                    nextElem.addItem(nextItem);
                    nextItem.tierName = participant + PARTICIPANT_DEL + nextElem.flexType + DEL + nextItem.type;
                    
                 // add to map
                    if (tiersPerLevel.get(nextElem.flexType) == null) {
                    	tiersPerLevel.put(nextElem.flexType, new LinkedHashSet<String>(10));
                    }
                    tiersPerLevel.get(nextElem.flexType).add(nextItem.tierName);
                }
                
                curElem.addElement(nextElem);
                curElem = nextElem;
            } else if (FlexConstants.ITEM.equals(qName)) {
                // check attributes
                type = atts.getValue(FlexConstants.TYPE);

                if ((type != null) && (type.length() > 0)) {
                    nextItem = new Item();
                    
                    nextItem.type = type;
                    
                    // add "punct" to the text, txt, layer
                    // use the TXT constant for the type in order to check with "=="
                    if (FlexConstants.TXT.equals(type) || FlexConstants.PUNCT.equals(type)) {
                    	type = FlexConstants.TXT; 
                    }

                    nextItem.lang = atts.getValue(FlexConstants.LANG);
                    String tierName = null;
                    
                    //if(participant >= 65){
                    if(participant != null){
                    	tierName = participant + PARTICIPANT_DEL + curElem.flexType + DEL + type;                    	
                    }else {
                    	tierName = curElem.flexType + DEL + type;
                    }
                    	
                    if (nextItem.lang != null) {                       	
                       tierName = tierName + DEL + nextItem.lang;
                    }
                    
                    nextItem.tierName = tierName;
                    
                    if (tiersPerLevel.get(curElem.flexType) == null) {
                    	tiersPerLevel.put(curElem.flexType, new LinkedHashSet<String>(10));
                    }
                    tiersPerLevel.get(curElem.flexType).add(tierName);
                }

                return;
            } else if (FlexConstants.LANGUAGE.equals(qName)) {
            	String lang = atts.getValue(FlexConstants.LANG);
            	
            	
            	if(lang  != null){
            		String font = atts.getValue(FlexConstants.FONT);
                	String vernacular = atts.getValue(FlexConstants.VERNACULAR);
                	
                	String value;
                	
            		if(font != null){
            			value = font;
            		} else {
            			value = "";
            		}
            		
            		if(vernacular != null){
            			value = value + DEL + vernacular;
            		}
            		langMap.put(lang, value);
            		languages.add(new LanguageRecord(lang, lang, lang));
            	}
            } else if(FlexConstants.MEDIA.equals(qName)){
            	String medPath = atts.getValue(FlexConstants.LOCATION);
            	if(medPath != null && medPath.trim().length() > 0){
            		MediaDescriptor descriptor = MediaDescriptorUtility.createMediaDescriptor(medPath);

                    if (descriptor != null && !mediaDescriptors.contains(descriptor)) {
                      mediaDescriptors.add(descriptor);
                    }
            	}
            }
        }

        /**
         * Only "item" elements can have content.
         *
         * @param ch the characters
         * @param start start position
         * @param length length of the contents
         *
         * @throws SAXException 
         */
        @Override
		public void characters(char[] ch, int start, int length)
            throws SAXException {
           
            content.append(ch, start, length);
        }

        /**
         * Traverse up the containment tree for most relevant elements,
         * set the value of an item in case of "item" element.
         *
         * @param uri empty string
         * @param localName local element name
         * @param qName qualified element name
         *
         * @throws SAXException 
         */
        @Override
		public void endElement(String uri, String localName, String qName)
            throws SAXException {
            // traverse up the container hierarchy
            if (FlexConstants.IT.equals(qName)) {            	
                return;
            }

            if ((decoder.inclParagraphElement && FlexConstants.PARAGR.equals(qName)) ||
                    FlexConstants.PHRASE.equals(qName) ||
                    FlexConstants.WORD.equals(qName) ||
                    FlexConstants.MORPH.equals(qName)) {            	
                curElem = curElem.parent;
            } else if (FlexConstants.ITEM.equals(qName)) {
                nextItem.value = content.toString().trim();
                content.delete(0, content.length());
                curElem.addItem(nextItem);
            }
        }

    }
}
