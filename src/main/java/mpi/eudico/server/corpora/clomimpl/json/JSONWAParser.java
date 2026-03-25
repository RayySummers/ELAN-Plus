package mpi.eudico.server.corpora.clomimpl.json;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import mpi.eudico.server.corpora.clom.DecoderInfo;
import mpi.eudico.server.corpora.clom.Property;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.Parser;
import mpi.eudico.server.corpora.clomimpl.abstr.PropertyImpl;
import mpi.eudico.server.corpora.clomimpl.dobes.AnnotationRecord;
import mpi.eudico.server.corpora.clomimpl.dobes.LanguageRecord;
import mpi.eudico.server.corpora.clomimpl.dobes.LingTypeRecord;
import mpi.eudico.server.corpora.clomimpl.dobes.TierRecord;
import mpi.eudico.server.corpora.clomimpl.dobes.TimeSlotRecord;
import static mpi.eudico.server.corpora.clomimpl.json.WAConstants.*;
import static mpi.eudico.server.corpora.util.ServerLogger.LOG;
import mpi.eudico.util.TimeFormatter;
import mpi.eudico.util.TimeFormatter.TIME_FORMAT;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * A parser for <a href="https://www.w3.org/TR/2017/REC-annotation-model-20170223/">WebAnnotation</a>
 * {@code .json} or {@code .jsonld} files.
 * The format allows some variation in how the elements are structured in the
 * file. 
 * The current implementation<ul>
 * <li>is based on unfinished work on a WebAnnotation application profile for
 * video annotation 
 * <li>assumes that an AnnotationCollection corresponds to the concept of a tier
 * <li>assumes that all information is embedded in the document; elements are 
 * identified by an IRI but no attempts are made to dereference annotations 
 * (or other elements) if they are not embedded
 * </ul>
 * 
 * This implementation is based on the {@code org.json} package.
 * 
 * @version April 2021
 * @author Han Sloetjes
 */
public class JSONWAParser extends Parser {
	// Parser type of intermediate lists and maps
	private JSONWADecoderInfo waDecoderInfo;
	private String lastParsedFile = "";
	private Map<TierRecord, List<String>> tierAnnotationsMap = new HashMap<TierRecord, List<String>>();
	private List<TierRecord> tierRecordList = new ArrayList<TierRecord>();
	private List<String> mediaUrls = new ArrayList<String>();
	private List<String> creators = new ArrayList<String>();
	
	private int tsId = 0;
	// copied from EAFParser
	/** stores tier name - tier record pairs */
    private final Map<String, TierRecord> tierMap = new HashMap<String, TierRecord>();

    /** a map with tier name - List with Annotation Records pairs */
    private final Map<String, List<AnnotationRecord>> tiers = new HashMap<String, List<AnnotationRecord>>();

    /** a list of tier names */
    private final List<String> tierNames = new ArrayList<String>();

    /** a list of tier type records */
    private final List<LingTypeRecord> linguisticTypes = new ArrayList<LingTypeRecord>();

    /** a map of time slot id to time value as a string */
    private ArrayList<TimeSlotRecord> timeSlots = new ArrayList<TimeSlotRecord>(); // of TimeSlotRecords
    private Map<String, String> timeSlotsMap = new HashMap<String, String>();
    
    private final List<Property> docProperties = new ArrayList<Property>();
    
    //private final Map<String, ExternalReferenceImpl> extReferences = new HashMap<String, ExternalReferenceImpl>();
    
    private final List<LanguageRecord> languages = new ArrayList<LanguageRecord>();
    private final List<String> langList = new ArrayList<String>();
    
    //private final List<LicenseRecord> licenses = new ArrayList<LicenseRecord>();

    /** stores the time slots ordered by id */
    private final List<String> timeOrder = new ArrayList<String>(); // since a HashMap is not ordered, all time_slot_ids have to be stored in order separately.
    private List<MediaDescriptor> mediaDescriptors = new ArrayList<MediaDescriptor>();
    //private List<LinkedFileDescriptor> linkedFileDescriptors = new ArrayList<LinkedFileDescriptor>();
    private Map<String, List<WAAnnotationRecord>> annIdToRecordsMap = new HashMap<String, List<WAAnnotationRecord>>();
    private Map<String, List<String>> colIdToPageIdMap              = new HashMap<String, List<String>>();
    private Map<String, List<String>> pageIdToAnnoIdMap             = new HashMap<String, List<String>>();
    private static final String TYPE_NAME = "wa-type";
    private static final String TS_ID_PREFIX = "ts";

    
    /**
     * Constructor.
     */
	public JSONWAParser() {
		
	}
	
	/**
	 * Sets a decoder information object for the parser.
	 * 
	 * @param decoderInfo the object with relevant information for the parsing
	 * process
	 */
	@Override
	public void setDecoderInfo(DecoderInfo decoderInfo) {
		if (decoderInfo instanceof JSONWADecoderInfo) {
			waDecoderInfo = (JSONWADecoderInfo) decoderInfo;
		}
	}

	/**
	 * Follows the old pattern of checking the last parsed file.
	 * 
	 * The {@code JSON} file is parsed and the contents converted to the type 
	 * of intermediate objects expected by the transcription loader.
	 * 
	 * @param path the path to the file to parse
	 */
	private void parse(String path) {
		if (path == null) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "The path to the file to parse is null");
			}
			return;
		}
		if (path.equals(lastParsedFile)) {
			return;
		}
		
		reset();
		
		try {
			String charsetName = "UTF-8";
			if (waDecoderInfo != null && waDecoderInfo.getCharsetName() != null) {
				charsetName = waDecoderInfo.getCharsetName();
			}
			JSONTokener jt = new JSONTokener(new InputStreamReader(
					new FileInputStream(new File(path)), charsetName));
			// the root object can be a JSONArray or JSONObject
			JSONArray rootJA = null;
			JSONObject rootJO = null;
			try {
				rootJO = new JSONObject(jt);
			} catch (JSONException jse) {
				if (LOG.isLoggable(Level.WARNING)) {
					LOG.log(Level.WARNING, "JSON syntax error: the root is not a JSONObject:");
					LOG.log(Level.WARNING, jse.getMessage());
				}
				try {
					rootJA = new JSONArray(jt);
				} catch (JSONException jse2) {
					if (LOG.isLoggable(Level.WARNING)) {
						LOG.log(Level.WARNING, "JSON syntax error: the root is not a JSONArray:");
						LOG.log(Level.WARNING, jse2.getMessage());
					}
				}
			}
			if (rootJO != null) {
				handleRoot(rootJO);
			
				Iterator<String> keyIter = rootJO.keys();
				while (keyIter.hasNext()) {
					Object jobj = rootJO.get(keyIter.next());
					if (jobj instanceof JSONObject) {
						//System.out.println("JOBJ: " + jobj.toString());
						//printJSONObject((JSONObject) jobj);						
						handleJSONObject(null, (JSONObject) jobj);
					} else if (jobj instanceof JSONArray) {
						//printJSONArray((JSONArray) jobj);
						handleJSONArray(null, (JSONArray) jobj);
					}
				}
			} else if (rootJA != null) {

				Iterator<Object> jaIter = rootJA.iterator();
				while (jaIter.hasNext()) {
					Object jobj = jaIter.next();
					if (jobj instanceof JSONObject) {
						//System.out.println("JOBJ: " + jobj.toString());
						//printJSONObject((JSONObject) jobj);
						handleJSONObject(null, (JSONObject) jobj);
					} else if (jobj instanceof JSONArray) {
						//printJSONArray((JSONArray) jobj);
						handleJSONArray(null, (JSONArray) jobj);
					}
				}
			}

		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} catch (Throwable t) {
			t.printStackTrace();
		}

		// create objects, complete tier records, annotations records, time order and time slot records etc.
		createObjects();

		
		lastParsedFile = path;
	}
	
	/**
	 * After parsing of the file intermediate objects are created and returned
	 * by {@link Parser} implementations, as expected by TranscriptionStores
	 * at load time.
	 */
	private void createObjects() {
		// tier type objects
		LingTypeRecord ltr = new LingTypeRecord();
		ltr.setLingTypeId(TYPE_NAME);
		ltr.setStereoType(LingTypeRecord.ALIGNABLE);
		ltr.setTimeAlignable("true");
		linguisticTypes.add(ltr);
		
		// language objects
		if (!langList.isEmpty()) {
			for (String lng : langList) {
				languages.add(new LanguageRecord(lng, lng, lng));
			}
		}
		
		// tier objects
		if (!tierRecordList.isEmpty()) {
			for (TierRecord tr : tierRecordList) {
				if (!tierNames.contains(tr.getName())) {
					tierNames.add(tr.getName());
				}
				if (!tiers.containsKey(tr.getName())) {
					tiers.put(tr.getName(), new ArrayList<AnnotationRecord>());
				}
				
				// collect the annotations belonging to the tier
				// reverse lookup of the collection id
				String colId = null;
				for (Map.Entry<String, TierRecord> tierEntry : tierMap.entrySet()) {
					if (tr == tierEntry.getValue()) {
						colId = tierEntry.getKey();
						break;
					}
				}
				if (colId == null) {
					if (LOG.isLoggable(Level.WARNING)) {
						LOG.log(Level.WARNING, "The Collection ID of the tier record could not be found");
					}
				} else {
					List<String> curAnnIdList = new ArrayList<String>();
					List<String> curPageIdList = colIdToPageIdMap.get(colId);
					for (String pid : curPageIdList) {
						List<String> paAnn = pageIdToAnnoIdMap.get(pid);
						if (paAnn != null) {
							curAnnIdList.addAll(paAnn);
						}
					}
					
					// if curAnnIdlist is not empty
					if (!curAnnIdList.isEmpty()) {
						for (String annId : curAnnIdList) {
							List<WAAnnotationRecord> arList = annIdToRecordsMap.get(annId);
							if (arList != null && !arList.isEmpty()) {
								// to do? if more than one annotation record, check if a new tier is required, e.g. based on language?
								WAAnnotationRecord ar = arList.get(0);
								tiers.get(tr.getName()).add(ar);
								// check/update the ID of the annotation
								String shortId = extractLastFragment(ar.getAnnotationId());
								if (shortId != null) {
									ar.setAnnotationId(shortId);
								}
								// check XML validity?
								if (!validXMLID(ar.getAnnotationId())) {
									ar.setAnnotationId(null);
								}
								
								if (ar.getLanguage() != null && !ar.getLanguage().isEmpty()) {
									if (tr.getLangRef() == null || tr.getLangRef().isEmpty()) {
										tr.setLangRef(ar.getLanguage());
									}
									// add a sym.ass. depending annotation for language?
								}
								// add a depending annotation for "purpose"?
							} else {
								if (LOG.isLoggable(Level.INFO)) {
									LOG.log(Level.INFO, "The annotation record list for this annotation is empty: " + annId);
								}
							}
						}
					} else {
						if (LOG.isLoggable(Level.INFO)) {
							LOG.log(Level.INFO, "The annotation ID list of this tier is empty: " + tr.getName());
						}
					}
				}
				
			}
		}
		
		// time order and time slots
        for (int i = 0; i < timeSlots.size(); i++) {
        	TimeSlotRecord tsr = timeSlots.get(i);
            timeOrder.add(TS_ID_PREFIX + tsr.getId());
        }

        for (int i = 0; i < timeSlots.size(); i++) {
        	TimeSlotRecord tsr = timeSlots.get(i);
            // note: could use the timeOrder objects (if we are sure of the order in
            // which the methods are called)?
            timeSlotsMap.put(TS_ID_PREFIX + tsr.getId(), Long.toString(tsr.getValue()));
        }

	}
	
	/**
	 * The AnnotationCollections are possibly contained in a single "root" 
	 * {@code JSONObject}, some properties are retrieved and stored as
	 * document (transcription) properties.
	 * The root {@code JSONObject} might also be an AnnotationCollection or
	 * a single Annotation.
	 *    
	 * @param rootObj the root {@code JSONObject} 
	 */
	private void handleRoot(JSONObject rootObj) {
		// extract information, properties for the whole document
		// there might be a container element e.g. with key "ldp:contains"
		
		String type = getTypeOfObject(rootObj);
		if (type != null) {
			if (type.indexOf(CONTAINER) > -1) {
				String docId = getIdOfObject(rootObj);
				if (docId != null) {
					docProperties.add(new PropertyImpl(ID, docId));
					String label = rootObj.optString(LABEL);
					if (label != null && !label.isEmpty()) {
						docProperties.add(new PropertyImpl(LABEL, label));
					}
				}
			} else if (type.equals(ANN_COLLECTION)) {
				handleCollection(rootObj);
			} else if (type.equals(ANN_PAGE)) {
				handlePage(null, rootObj);
			} else if (type.equals(ANNOTATION)) {
				handleAnnotation(null, rootObj);
			}
		}
	}
	
	/**
	 * Checks the type of the {@code JSONObject} parameter and forwards some
	 * known types to dedicated methods for those types.
	 *  
	 * @param parentOrContextType the parent or context id or label or {@code null}
	 * @param jObj the {@code JSONObject} to convert
	 */
	private void handleJSONObject(String parentOrContextType, JSONObject jObj) {

		String objType = getTypeOfObject(jObj);
		if (ANN_COLLECTION.equals(objType)) {
			handleCollection(jObj);//parentOrContextType not relevant?
		} else if (ANN_PAGE.equals(objType)) {
			handlePage(parentOrContextType, jObj);
		} else if (ANNOTATION.equals(objType)) {
			handleAnnotation(parentOrContextType, jObj);
		} // other types of objects are handled in a subprocess of the above
 	}
	
	/**
	 * Iterates the provided array and forwards {@code JSONObject} and 
	 * {@code JSONArray} objects to respective methods. 
	 * 
	 * @param contextKey the context or property key
	 * @param jArray the array to handle
	 */
	private void handleJSONArray(String contextKey, JSONArray jArray) {
		for (Object obj : jArray) {
			if (obj instanceof JSONObject) {
				handleJSONObject(contextKey, (JSONObject) obj);
			} else if (obj instanceof JSONArray) {
				handleJSONArray(contextKey, (JSONArray) obj);
			}
		}
	}
	
	/**
	 * Utility method to print the contents of a {@code JSONObject}.
	 * 
	 * @param jObj the object to print to standard out
	 */
	private void printJSONObject(JSONObject jObj) {
		System.out.println("JObject Type: " + getTypeOfObject(jObj));
		System.out.println("JObject length: " + jObj.length());
		// names returned in random order, just like is the case with keys()
		System.out.println("Keys: " + jObj.names());
		Iterator<String> keyIter = jObj.keys();
		while (keyIter.hasNext()) {
			String key = keyIter.next();
			Object vObj = jObj.get(key);
			
			if (vObj instanceof JSONArray) {
				System.out.println("k: " + key + " - v: ");
				printJSONArray((JSONArray) vObj);
			} else {
				System.out.println("k: " + key + " - v: " + vObj);
			}
		}
		System.out.println();
	}
	
	/**
	 * Utility method to print the contents of a {@code JSONArray}.
	 * 
	 * @param jArr the array to print to standard out
	 */
	private void printJSONArray (JSONArray jArr) {
		System.out.println("JArray length: " + jArr.length());
		Iterator<Object> arrIter = jArr.iterator();
		while (arrIter.hasNext()) {
			Object arrObj = arrIter.next();
			if (arrObj instanceof JSONObject) {
				//System.out.println("JOBJ: " + jobj.toString());
				printJSONObject((JSONObject) arrObj);
			} else if (arrObj instanceof JSONArray) {
				printJSONArray((JSONArray) arrObj);
			} else if (arrObj instanceof String) {
				System.out.println("S: " + arrObj.toString());
			}
		}
	}
	
	/**
	 * Queries an object for the {@code type} property. If the property is 
	 * an array the first "known" type is returned or, if not found, the first
	 * string value is returned.
	 * <p>
	 * This method checks for both {@code "type"} and {@code "@type"}.
	 * 
	 * @param jObj the JSONObject to query
	 * @return the value of the {@code type} property or {@code null}
	 */
	private String getTypeOfObject(JSONObject jObj) {
		if (jObj != null) {
			Object typeObj = jObj.opt(TYPE);
			if (typeObj == null) {
				typeObj = jObj.opt(AT_TYPE);
			}
			
			if (typeObj instanceof String) {
				String typeStr = (String) typeObj;
				typeStr = noNameSpace(typeStr);
				if (typeStr.equals(TEXTUAL_BODY) || typeStr.equals(TEXT)) {
					return TEXTUAL_BODY;
				}
				return typeStr;
			} else if (typeObj instanceof JSONArray) {
				// @type can be an array, return the first encountered known
				// and relevant type or otherwise the first string value 
				JSONArray typeArr = (JSONArray) typeObj;
				String typeStr = null;
				
				for (Object nextObj : typeArr) {
					if (nextObj instanceof String) {
						String nextStr = noNameSpace((String) nextObj);
						
						if (nextStr.equals(TEXTUAL_BODY) || nextStr.equals(TEXT)) {
							return TEXTUAL_BODY;
						}
						if (ANNOTATION.equals(nextStr) || ANN_PAGE.equals(nextStr) || 
								ANN_COLLECTION.equals(nextStr)) {
							return nextStr;
						}
				
						if (typeStr == null) {
							typeStr = nextStr;
						}
					}
				}
				// if we get here the first string value is returned, can be null
				return typeStr;
			}
		}
		
		return null;
	}
	
	/**
	 * Queries an object for the {@code id} property.
	 * <p>
	 * This method checks for both {@code "id"} and {@code "@id"}.
	 * 
	 * @param jObj the JSONObject to query
	 * @return the value of the {@code id} property or {@code null}
	 */
	private String getIdOfObject(JSONObject jObj) {
		if (jObj != null) {
			Object idObj = jObj.opt(ID);
			if (idObj == null) {
				idObj = jObj.opt(AT_ID);
			}
			
			if (idObj instanceof String) {
				return (String) idObj;
			} else if (idObj instanceof Integer) {
				// this shouldn't be the case, but for annotations "a"+id could be returned?
				return ((Integer) idObj).toString();
			}
		}
		return null;
	}
	
	/**
	 * Checks if a property key or value seems to have a namespace prefix and,
	 * if so, returns the string without the prefix. 
	 * The implementation in fact finds the last index of {@code :} and returns
	 * the substring from that index + 1.<p>
	 * Example: the return value for the input string {@code "oa:TextualBody"}
	 * would be {@code "TextualBody"}.  
	 * 
	 * @param s the input string
	 * @return a string without namespace prefix
	 */
	private String noNameSpace(String s) {
		if (s == null) {
			return s;
		}
		int index = s.lastIndexOf(':');
		if (index > -1 && index < s.length() - 1) {
			return s.substring(index + 1);
		}
		
		return s;
	}
	
	/**
	 * Extracts the last fragment of an {@code IRI} (Internationalized Resource
	 * Identifier), the part after a {@code '#'} or the last {@code '/'} 
	 * character.
	 * 
	 * @param iri the full identifier
	 * @return the last fragment or the input string if fragment specifiers
	 * are not found
	 */
	private String extractLastFragment(String iri) {
		if (iri != null) {
			int index = iri.lastIndexOf('#');
			
			if (index < 0 || index == iri.length() - 1) {
				index = iri.lastIndexOf('/');
			}
			if (index > -1 && index < iri.length() - 1) {
				return iri.substring(index + 1);
			}		
		}
		
		return iri;
	}
	
	/**
	 * It would probably be best to convert {@code IRI}'s to valid XML ID's.
	 * <p>
	 * For now this is a quick, unsophisticated test whether the input string
	 * can be used as an XML ID. Rejects any kind of punctuation or other 
	 * 'special' characters except for '_' and '-' (even if they would be part
	 * of a valid XML escape sequence). This should be revised.
	 * 
	 * @param id the id string to test
	 * @return {@code false} if a coarse grained test detects invalid characters
	 */
	private boolean validXMLID(String id) {
		if (id == null || id.isEmpty()) {
			return false;
		}
		if (Character.isDigit(id.charAt(0))) {
			return false;
		}
		int[] codepoints = id.codePoints().toArray();
		String s = "_-";
		int underCP = Character.codePointAt(s, 0);
		int hyphCP = Character.codePointAt(s, 1);
		for (int cp : codepoints) {
			if (!Character.isLetterOrDigit(cp)) {
				if (cp != underCP && cp != hyphCP) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	/**
	 * Ensures that there are intermediate collection instances for annotations
	 * that are not part of a named {@code AnnotationCollection} or
	 * {@code AnnotationPage}. A tier record  for a default, unnamed tier is 
	 * created too.  
	 */
	private void createUnnamedCollection() {
		// check if an "Unnamed" collection has already been added and create a 
		// tier record and maps and lists for it
		if (!colIdToPageIdMap.containsKey(UNNAMED) && !tierMap.containsKey(UNNAMED)) {
			TierRecord tr = new TierRecord();
			tr.setName(UNNAMED);
			tierMap.put(UNNAMED, tr);
			tierRecordList.add(tr);
			tierAnnotationsMap.put(tr, new ArrayList<String>());
			colIdToPageIdMap.put(UNNAMED, new ArrayList<String>());
			pageIdToAnnoIdMap.put(UNNAMED, new ArrayList<String>());
			colIdToPageIdMap.get(UNNAMED).add(UNNAMED);
		}
	}
	
	// the type has been checked to be 
	/**
	 * Handles an object that has been checked to be a WebAnnotation 
	 * {@code AnnotationCollection}.<p>
	 * This corresponds to a tier in EAF. As name of the tier either the 
	 * {@code label} property or (part of) the {@code id} property is used.
	 * A {@code TierRecord} is created and added to relevant lists. 
	 * Actual {@code Annotation} objects are embedded in one or more 
	 * {@code AnnotationPage}s. The {@code AnnotationPage}s of an 
	 * {@code AnnotationCollection} can either be embedded or referenced, in
	 * the latter case the {@code first} property is a reference to a page
	 * defined elsewhere in the document and there might be a {@code last}
	 * property too.<p> 
	 * Example:
	 * <code>
	 * {
     * "@context": "http://www.w3.org/ns/anno.jsonld",
     * "id": "http://example.org/collection1",
     * "type": "AnnotationCollection",
     * "label": "Tier_1",
     * "total": 42023,
     * "first": "http://example.org/page1",
     * "last": "http://example.org/page42"
     * }
	 * </code>
	 * 
	 * @param jObj the {@code AnnotationCollection} {@code JSONObject}
	 */
	private void handleCollection(JSONObject jObj) {
		if (jObj == null) return;
		
		TierRecord tr = new TierRecord();
		// there can be 1 or more "label"s
		String label = getFirstStringValue(jObj, LABEL);

		String collId = getIdOfObject(jObj);
		if (collId == null) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "The AnnotationCollection does not have an \"id\" property: ignored");
			}
			return;
		}

		tierMap.put(collId, tr);
		tierRecordList.add(tr);
		
		if (label == null) {
			// try to extract from id
			label = extractLastFragment(collId);
			// still no label?
			if (label == null) {
				label = UNNAMED;
			}
		}
		tr.setName(label);
		
		// get annotator from creator object, if present
		// it could also be a key-value pair
		Object creatorObj = jObj.opt(CREATOR);
		if (creatorObj instanceof JSONObject) {
			Object partName = ((JSONObject) creatorObj).opt(NAME);
			if (partName == null) {
				partName = ((JSONObject) creatorObj).opt(NICKNAME);
			}
			if (partName instanceof String) {
				tr.setAnnotator(partName.toString());
			}
		} else if (creatorObj instanceof String) {
			tr.setAnnotator((String) creatorObj);
		}
		
		if (tr.getAnnotator() != null && !tr.getAnnotator().isEmpty() && !creators.contains(tr.getAnnotator())) {
			creators.add(tr.getAnnotator());
		}
		
		int totalNum = jObj.optInt(TOTAL, -1);
		if (totalNum == -1) {
			// not specified, continue with a default capacity
			totalNum = 10;
		}
		List<String> annIdList = new ArrayList<String>(totalNum); 
		tierAnnotationsMap.put(tr, annIdList);
		
		// if total number of annotations is 0 don't bother to parse annotation pages 
		//...
		
		// if the collection has a "first" key, which is an Annotation page,
		// assume all annotations are in its "items" list,
		// if it is a String, it is the id of an Annotation Page elsewhere in the stream
		Object firstObj = jObj.opt(FIRST);
		
		if (firstObj instanceof String) {
			List<String> pageList = new ArrayList<String>();
			pageList.add((String) firstObj);
			String lastPage = jObj.optString(LAST);
			if (lastPage != null && !lastPage.isEmpty()) {
				pageList.add(lastPage);
			}
			colIdToPageIdMap.put(collId, pageList);
		} else if (firstObj instanceof JSONObject) {
			// embedded page -> forward to specialized method with collId as parent context
			handlePage(collId, (JSONObject) firstObj);
		}
	}
	
	/**
	 * Handles an object that has been checked to be a WebAnnotation 
	 * {@code AnnotationPage}.<p>
	 * An {@code AnnotationPage} is part of an {@code AnnotationCollection},
	 * either embedded or by reference. In the latter case the 
	 * {@code AnnotationPage} should have a {@code partOf} property which 
	 * refers to the {@code AnnotationCollection}. {@code Annotation}s are
	 * listed in the {@code items} property.<p>
	 * Example:
	 * <code>
	 * {
     * "@context": "http://www.w3.org/ns/anno.jsonld",
     * "id": "http://example.org/page1",
     * "type": "AnnotationPage",
     * "partOf": "http://example.org/collection1",
     * "next": "http://example.org/page2",
     * "startIndex": 0,
     * "items": [
     *   {
     *     "id": "http://example.org/anno1",
     *     "type": "Annotation",
     *  ...
	 * </code>
	 * 
	 * @param collId the parent or containing collection
	 * @param jObj the {@code AnnotationPage} object
	 */
	private void handlePage(String collId, JSONObject jObj) {
		if (jObj == null) {
			return;
		}
		// double check if the object represents an AnnotationPage
		String type = getTypeOfObject(jObj);  
		
		if (type == null || !ANN_PAGE.equals(type)) {
			if (LOG.isLoggable(Level.INFO)) {
				LOG.log(Level.INFO, "The JSONObject does not represent an AnnotationPage: " + type);
			}
			return;
		}
		
		String pageId = getIdOfObject(jObj);

		if (pageId == null) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "The AnnotationPage does not have an \"id\" property, skipping this page");
			}
			return;
		}
		
		// if the collection this page is part of is not clear
		if (collId == null) {
			collId = getFirstStringValue(jObj, PART_OF);
		}	
		
		if (collId != null) {
			List<String> pList = colIdToPageIdMap.get(collId);
			if (pList == null) {
				pList = new ArrayList<String>();
				colIdToPageIdMap.put(collId, pList);
			}
			if (!pList.contains(pageId)) {
				pList.add(pageId);
			}
		} else {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "The AnnotationPage does not seem to belong to an AnnotationCollection: " + pageId);
			}
			return;
		}		
		
		String next = getFirstStringValue(jObj, NEXT);
		if (next != null) {
			List<String> pList = colIdToPageIdMap.get(collId);
			if (pList != null && !pList.contains(next)) {
				pList.add(next);
			}
		}
		// ignore 'prev' and 'startIndex' at the moment
		
		// a page must have an array of one or more annotations
		Object itemObj = jObj.opt(ITEMS);
		// not sure if a single annotation is allowed to be included as a JSONObject instead of part of a JSONArray
		if (itemObj instanceof JSONObject) {
			handleAnnotation(pageId, (JSONObject) itemObj);
		} else if (itemObj instanceof JSONArray) {
			JSONArray itemArray = (JSONArray) itemObj;
			for (Object annObj : itemArray) {
				// it is probably illegal to have only id's of annotations in the array?
				if (annObj instanceof JSONObject) {
					handleAnnotation(pageId, (JSONObject) annObj);
				} else if (annObj instanceof String) {
					String annId = (String) annObj;
					// store the annotation id reference
					List<String> annList = pageIdToAnnoIdMap.get(pageId);
					if (annList == null) {
						annList = new ArrayList<String>();
						pageIdToAnnoIdMap.put(pageId, annList);
					}
					if (annList.contains(annId)) {
						annList.add(annId);
					}
				}
			}
		}
	}

	/**
	 * Handles an object that has been checked to be a WebAnnotation 
	 * {@code Annotation}.<p>
	 * {@code Annotation}s may or may not be part of an {@code AnnotationCollection} 
	 * (via an {@code AnnotationPage}), the WebAnnotation model supports both.
	 * Annotations that are not part of a collection will be added to a tier
	 * labeled "Unnamed". The {@code body} and {@code target} properties 
	 * contain the most relevant information to be converted to ELAN 
	 * annotations.<p>
	 * Example:
	 * <code>
	 * {
     * "@context": "http://www.w3.org/ns/anno.jsonld",
     * "id": "http://example.org/anno25",
     * "type": "Annotation",
     * "body": {
     *   "type": "TextualBody",
     *   "format": "text/plain",
     *   "value": "example annotation"
     * },
     * "target": {   
     *   "type": "Video",
     *   "source": "http://example.org/video1.mp4",
     *   "selector": {
     *     "conformsTo": "http://www.w3.org/TR/media-frags/",
     *     "type": "FragmentSelector",
     *     "value": "t=18.39,22.55"
     *   }
     * }
     * }
	 * </code>
	 * 
	 * @param pageId the parent or containing annotation page
	 * @param jObj the {@code Annotation} object
	 */
	private void handleAnnotation(String pageId, JSONObject jObj) {
		if (jObj == null) {
			return;
		}
		String annType = getTypeOfObject(jObj);
		if (annType == null || !ANNOTATION.equals(annType)) {
			if (LOG.isLoggable(Level.INFO)) {
				LOG.log(Level.INFO, "The JSONObject does not represent an Annotation: " + annType);
			}
			return;
		}
		String annId = getIdOfObject(jObj);
		if (annId == null) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "The Annotation does not have an \"id\" property, skipping this annotation");
			}
			return;
		}
		// not sure if Annotations always have to be embedded in an annotation page 
		// or can be referenced, in the latter case 'id' and 'type' should be present
		Object target = jObj.opt(TARGET);
		if (target == null) {
			// the 'id' is a reference to an annotation defined elsewhere 
			if (pageId == null) {
				pageId = UNNAMED;
			}
			List<String> annList = pageIdToAnnoIdMap.get(pageId);
			if (annList == null) {
				annList = new ArrayList<String>();
				pageIdToAnnoIdMap.put(pageId, annList);
			}
			if (!annList.contains(annId)) {
				annList.add(annId);
			}		
		} else {
			// annotation defined here, might have been referenced elsewhere
			if (pageId == null) {
				for (Map.Entry<String, List<String>> pageIdEntry : pageIdToAnnoIdMap.entrySet()) {
					List<String> annIds = pageIdEntry.getValue();
					if (annIds != null && annIds.contains(annId)) {
						pageId = pageIdEntry.getKey();
						break;
					}
				}
			}
			// if pageId still is null
			if (pageId == null) {
				pageId = UNNAMED;
			}

			// get the collection id
			String collId = null;
			for (Map.Entry<String, List<String>> colIdEntry : colIdToPageIdMap.entrySet()) {
				List<String> pageIds = colIdEntry.getValue();
				if (pageIds != null && pageIds.contains(pageId)) {
					collId = colIdEntry.getKey();
					break;
				}
			}
			
			if (collId == null) {
				collId = UNNAMED;
				createUnnamedCollection();
			}
			// maybe adding the page to the tier's (collection's) page list is superfluous
			if (!colIdToPageIdMap.containsKey(collId)) {
				List<String> pidList = new ArrayList<String>();
				colIdToPageIdMap.put(collId, pidList);
				pidList.add(pageId);
			} else {
				List<String> pageList = colIdToPageIdMap.get(collId);
				if (!pageList.contains(pageId)) {
					pageList.add(pageId);
				}
			}
			// build annotation record(s) for this annotation
			// there can be 0 or more bodies and 1 or more targets
			// bodies might be e.g. the same value but in different languages
			// a single body value
			List<WAAnnotationRecord> curRecords = new ArrayList<WAAnnotationRecord>();
			String motivation = getFirstStringValue(jObj, MOTIVATION);
			String bodyValue = jObj.optString(BODY_VALUE);
			// a single body object or an array of bodies
			if (bodyValue != null && !bodyValue.isEmpty()) {
				WAAnnotationRecord ar = new WAAnnotationRecord();
				ar.setValue(bodyValue);
				curRecords.add(ar);
			} else {
				Object bodyObj = jObj.opt(BODY);
				// if "body" is a String, it is an IRI of a body defined elsewhere, not supported yet
				// if there is a single JSONObject body with type "TextualBody" we can use it
				// if there is an array of bodies, each element can either be an IRI a "TextualBody"
				// type of object or another type of body object
				if (bodyObj instanceof JSONObject) {
					handleBody((JSONObject) bodyObj, curRecords);
				} else if (bodyObj instanceof JSONArray) {
					JSONArray bodyArr = (JSONArray) bodyObj;
					for (Object obj : bodyArr) {
						if (obj instanceof JSONObject) {
							handleBody((JSONObject) obj, curRecords);
						}
					}
				} else if (bodyObj instanceof String) {
					// an IRI to an external web resource as body, not supported yet 
				}
			} 
			
			// extract target(s). There can be multiple targets, in theory with different 
			// fragment begin and end time. Here only one time fragment is extracted and
			// used for the annotation records
			// the target could also be a single String, an IRI with fragment identifier
			// we collect URL/IRI from "id" or "source" property, selector's begin and 
			// end time and possibly the mime-type from the "format" property 
			long[] times = handleTargets(target);
			
			if (times != null) {
				List<String> annList = pageIdToAnnoIdMap.get(pageId);
				if (annList == null) {
					annList = new ArrayList<String>();
					pageIdToAnnoIdMap.put(pageId, annList);
				}
				if (!annList.contains(annId)) {
					annList.add(annId);
				}
				for (WAAnnotationRecord ar : curRecords) {
					ar.setAnnotationType(AnnotationRecord.ALIGNABLE);
					ar.setAnnotationId(annId); // rework later
					if (motivation != null) {
						ar.setMotivation(motivation);
					}
	                TimeSlotRecord tsRec = new TimeSlotRecord(tsId++, times[0]);
	                ar.setBeginTimeSlotRecord(tsRec);
	                timeSlots.add(tsRec);
                    tsRec = new TimeSlotRecord(tsId++, times[1]);
                    ar.setEndTimeSlotRecord(tsRec);
                    timeSlots.add(tsRec);
				}
				annIdToRecordsMap.put(annId, curRecords);
			}
			
		}
	}
	
	/**
	 * Extracts one or more annotation values from an annotation body, although 
	 * currently only the first value will be used.<p>
	 * The {@code body} property of an {@code Annotation} can have an array of 
	 * objects or values (e.g. with different {@code language} properties), 
	 * possibly as part of a {@code Choice} element. The {@code body} property
	 * should have the type {@code TextualBody}, if any. The {@code bodyValue}
	 * property of {@code Annotation}, instead of the {@code body} property, is
	 * also supported.
	 * 
	 * @param bodyJSON the body {@code JSONObject} 
	 * @param annList the list to add annotation records to
	 * 
	 * @see #handleAnnotation(String, JSONObject)
	 */
	private void handleBody(JSONObject bodyJSON, List<WAAnnotationRecord> annList) {
		String type = getTypeOfObject(bodyJSON);
		if (TEXTUAL_BODY.equals(type)) {
			// check format (only accept text/plain?) and language?
			// String format = bodyJSON.optString(FORMAT);
			String lang = bodyJSON.optString(LANGUAGE);
			String annId = getIdOfObject(bodyJSON);
			Object valueObj = bodyJSON.opt(VALUE);
			// the "value" property should be a character sequence
			if (valueObj instanceof String) {
				String value = (String) valueObj;			
				WAAnnotationRecord ar = new WAAnnotationRecord();
				ar.setAnnotationId(annId);
				ar.setValue(value);
				if (lang != null && !lang.isEmpty()) {
					ar.setLanguage(lang);
					if (!langList.contains(lang)) {
						langList.add(lang);
					}
				}
				String purposeStr = getFirstStringValue(bodyJSON, PURPOSE);
				if (purposeStr != null && !purposeStr.isEmpty()) {
					ar.setPurpose(purposeStr);
				}
				annList.add(ar);
			} else if (valueObj instanceof JSONArray) {
				// shouldn't be an array
				if (LOG.isLoggable(Level.INFO)) {
					LOG.log(Level.INFO, "The 'value' property is an array instead of a character sequence");
				}
			} else if (valueObj instanceof JSONObject) {
				// shouldn't be an object
				if (LOG.isLoggable(Level.INFO)) {
					LOG.log(Level.INFO, "The 'value' property is an object instead of a character sequence");
				}
			}
			
		} else if (CHOICE.equals(type)) {
			// if type == Choice -> items property can contain multiple TextualBody's, the first being the default
			JSONArray items = bodyJSON.optJSONArray(ITEMS);
			if (items != null) {
				JSONObject firstItem = getFirstJSONObjectWithKey(items, TYPE, TEXTUAL_BODY);
				if (firstItem == null) {
					firstItem = getFirstJSONObjectWithKey(items, AT_TYPE, TEXTUAL_BODY);
				}
				if (firstItem != null) {
					handleBody(firstItem, annList);
				}
			}
		}
	}
	
	/**
	 * Extracts file name(s) and time fragment that act as the {@code target}
	 * of an annotation.<p>
	 * There can be multiple targets, all URL's or file names are added to the
	 * list of media files. All {@code body} values will be linked to this
	 * segment of the media file(s).
	 * The {@code target} can be a single string consisting of a file name or
	 * URL and a fragment identifier or an object with the same string as the 
	 * {@code id} property, or an object with a {@code source} property and a 
	 * {@code selector} property containing the time selection.<p>
	 * 
	 * @param obj the {@code target} string or object
	 * @return the (first) time segment ([begin, end]), converted to millisecond
	 * values
	 */
	private long[] handleTargets(Object obj) {
		if (obj instanceof String) {
			String urlWithFrag = (String) obj;
			int fragIndex = urlWithFrag.lastIndexOf('#');
			if (fragIndex > 0 && fragIndex < urlWithFrag.length() - 1) {// useful media fragments require more characters than 1
				String url = urlWithFrag.substring(0, fragIndex);
				addMediaDescriptor(url, null);
				
				String timeFrag = urlWithFrag.substring(fragIndex + 1);
				long[] times = extractTimeSelection(timeFrag);
				
				return times;
			} else {
				addMediaDescriptor(urlWithFrag, null);
			}
		} else if (obj instanceof JSONObject) {
			JSONObject jObj = (JSONObject) obj;
			String source = jObj.optString(SOURCE);
			if (source == null || source.isEmpty()) {
				source = getIdOfObject(jObj);
			}
			// a selector object must have a type:FragmentSelector and a value:"..."
			Object selectObj = jObj.opt(SELECTOR);
			
			if (selectObj instanceof JSONObject) {
				return handleSelector(source, (JSONObject) selectObj);
			} else if (selectObj instanceof JSONArray) {
				// an array of selector objects within a single target object
				long[] times = null;
				for (Object sobj : (JSONArray) selectObj) {
					if (sobj instanceof JSONObject) {
						long[] nt = handleSelector(source, (JSONObject) sobj);
						if (times == null) {
							times = nt;
						}
					}
				}
				return times;
			} else if (selectObj != null){
				// extract time from source string
				return handleTargets(source);
			}
		} else if (obj instanceof JSONArray) {
			// an array of target objects
			long[] times = null;
			
			for (Object iterObj : (JSONArray) obj) {
				long[] nt = handleTargets(iterObj);
				if (times == null) {
					times = nt;
				}
			}
			
			return times;
		}
		
		return null;
	}
	
	/**
	 * Handles a target's {@code selector} {@code JSONObject} by checking its 
	 * type, should be {@code FragmentSelector}, possibly checking its
	 * {@code conformsTo} property and by extracting time values from the 
	 * {@code value} property.
	 * 
	 * @param source the media source of the target
	 * @param selectObj the selector object
	 * @return an array containing begin and end time of the fragment or
	 * {@code null}
	 */
	private long[] handleSelector(String source, JSONObject selectObj) {
		
		if(selectObj.has(REFINED_BY)) {
			Object refinedBySelectorObj = selectObj.opt(REFINED_BY);
			if (refinedBySelectorObj instanceof JSONObject) {
				return handleSelector(source, (JSONObject) refinedBySelectorObj);
			}
		}
		
		String type = getTypeOfObject(selectObj);
		if (FRAG_SELECTOR.equals(type)) {
			// could/should test for the "conformsTo" property?
			String conforms = selectObj.optString(CONFORMS_TO);
			if (conforms != null && !conforms.isEmpty()) {
				if (!conforms.equals(MEDIA_SELECTOR)) {
					return null;
				}
			}
			String value = selectObj.optString(VALUE);
			if (value != null && !value.isEmpty()) {
				// if there is a fragment selector, add the media source to the descriptors
				addMediaDescriptor(source, null);
				long[] times = extractTimeSelection(value);
				return times;
			}
		}
		
		return null;
	}
	
	/**
	 * Adds a media URL to the list of media descriptors, if it hasn't been 
	 * added before.
	 * 
	 * @param url the file path or URL
	 * @param mime the mime-type or {@code null}
	 */
	private void addMediaDescriptor(String url, String mime) {
		if (url == null || url.isEmpty()) {
			return;
		}
		
		for (MediaDescriptor md : mediaDescriptors) {
			if (md.mediaURL.equals(url)) {
				// already added, check mime-type
				if (md.mimeType == null && mime != null) {
					md.mimeType = mime;
				}
				return;
			}
		}
		// if we get here, add the descriptor
		mediaDescriptors.add(new MediaDescriptor(url, mime));
	}
	
	/**
	 * Extracts the time selection from a fragment string. The string could be 
	 * something like {@code t=18.39,22.55} but a lot of variation is allowed
	 * (see the <a href="http://www.w3.org/TR/media-frags/">media-frags</a> 
	 * recommendation).
	 * 
	 * @param timeString the input string
	 * @return an array of length 2 ([begin time - end time]) or {@code null}
	 */
	private long[] extractTimeSelection(String timeString) {
		if (timeString != null)	{
			if (LOG.isLoggable(Level.FINE)) {
				if (!timeString.startsWith("t=")) {
					LOG.log(Level.FINE, String.format("Time string \"%s\" does not start with \"t=\"", timeString));
				}
			}
			
			// format "t=12.112,14.557" or "t=,20" etc.
			int eqInd = timeString.indexOf('=');
			int comInd = timeString.indexOf(',');
			
			// the following is too simple to capture allowed variations in time fragments
			if (eqInd >= 0 && comInd > eqInd) {
				long bt, et;
				try {
					if (comInd == eqInd + 1) {
						bt = 0;
						if (comInd == timeString.length() - 1) {
							et = Long.MAX_VALUE;
						} else {
							String etString = timeString.substring(comInd + 1);
							if (etString.indexOf('.') > -1 || etString.indexOf(':') > -1) {
								et = TimeFormatter.toMilliSeconds(etString);
							} else {
								et = TimeFormatter.toMilliSeconds(etString, TIME_FORMAT.SSMS);
							}
						}
					} else {
						String btString = timeString.substring(eqInd + 1, comInd);
						if (btString.indexOf('.') > -1 || btString.indexOf(':') > -1) {
							bt = TimeFormatter.toMilliSeconds(btString);
						} else {
							bt = TimeFormatter.toMilliSeconds(btString, TIME_FORMAT.SSMS);
						}
						if (comInd == timeString.length() - 1) {
							et = Long.MAX_VALUE;
						} else {
							String etString = timeString.substring(comInd + 1);
							if (etString.indexOf('.') > -1 || etString.indexOf(':') > -1) {
								et = TimeFormatter.toMilliSeconds(etString);
							} else {
								et = TimeFormatter.toMilliSeconds(etString, TIME_FORMAT.SSMS);
							}
						}	
					}
					
					return new long[] {bt, et};
				} catch (NumberFormatException nfe) {
					if (LOG.isLoggable(Level.INFO)) {
						LOG.log(Level.INFO, "Cannot parse time: " + nfe.getMessage());
					}
				}
			} else {
				// message, unparsable
				if (LOG.isLoggable(Level.INFO)) {
					LOG.log(Level.INFO, "Cannot extract time values from: " + timeString);
				}
			}
		}
		return null;
	}
	/*
	private TierRecord toTierRecord(JSONObject jObj) {
		if (jObj != null) {
			try {
				Object val = jObj.get(TYPE);
				if (ANN_COLLECTION.equals(val)) {
					TierRecord tr = new TierRecord();
					String label = jObj.optString(LABEL);
					if (label == null || label.isEmpty()) {
						label = "Unknown";
					}
					tr.setName(label);
					// get annotator from creator object, if there
					// it could also be a key-value pair
					Object creatorObj = jObj.opt(CREATOR);
					if (creatorObj instanceof JSONObject) {
						Object partName = ((JSONObject) creatorObj).opt(NAME);
						if (partName == null) {
							partName = ((JSONObject) creatorObj).opt(NICKNAME);
						}
						if (partName instanceof String) {
							tr.setAnnotator(partName.toString());
						}
					}
					tierRecordList.add(tr);
					List<String> annIdList = new ArrayList<String>(); 
					tierAnnotationsMap.put(tr, annIdList);
					int totalNum = jObj.optInt(TOTAL);
					// if the collection has a "first" key, which is an Annotation page,
					// assume all annotations are in its "items" list,
					// if it is a String, it is the id of an Annotation Page elsewhere in the stream
					Object firstObj = jObj.opt(FIRST);
					if (firstObj instanceof JSONObject) {
						JSONObject pageObject = (JSONObject) firstObj;
						// confirm it is an Annotation Page
						String objType = pageObject.optString(TYPE);
						if (!ANN_PAGE.equals(objType)) {
							// error? report
						}
						
						JSONArray itemsArray = pageObject.optJSONArray(ITEMS);
						if (itemsArray != null) {
							Iterator<Object> itemIter = itemsArray.iterator();
							while (itemIter.hasNext()) {
								Object annItem = itemIter.next();
								
								if (annItem instanceof JSONObject) {
									JSONObject annJI = (JSONObject) annItem;
									// could check whether type is Annotation
									String annId = annJI.optString(ID);
									if (annId != null) {
										annIdList.add(annId);
									}
									// if this is an annotation object with a body, create an annotation record too
									if (annJI.has(BODY)) {
										AnnotationRecord ar = toAnnotationRecord(annJI);
										if (ar != null) {
											annRecordList.add(ar);
										}
									}
								}
							}
						}
						
					} else if (firstObj instanceof String) {
						// a reference to the id of an annotation page, there should be a "last" too then
						// the AnnotationPage objects should be objects not contained in another object (top-level)
						// and the "next" reference can be followed to reconstruct the sequence
						//...
					}
					return tr;
				}
			} catch (JSONException je) {
				je.printStackTrace();
			}
		}
		return null;
	}
	*/
	/*
	private AnnotationRecord toAnnotationRecord(JSONObject jObj) {
		if (jObj != null) {
			try {
				Object val = jObj.opt(TYPE); 
				if (ANNOTATION.equals(val)) {
					AnnotationRecord ar = new AnnotationRecord();
					// extract text, begin time, end time, tier?
					if (jObj.has(ID)) {
						ar.setAnnotationId(jObj.getString(ID));
					}
					
					Object bodyObject = jObj.opt(BODY);
					if (bodyObject != null) {
						JSONObject textBody = getFirstJSONObjectWithKey(bodyObject, TYPE, TEXTUAL_BODY);
						if (textBody != null) {
							String textValue = getFirstStringValue(textBody, VALUE);
							ar.setValue(textValue);
						}
					} else {
						// no text value, is possible
					}
					
					JSONObject targetObject = getFirstJSONObjectWithKey(jObj, TARGET, null);
					if (targetObject != null) {
						// retrieve source and time
						String medUrl = getFirstStringValue(targetObject, ID);
						//String medType = getFirstStringValue(targetObject, TYPE);
						//add the media url to the list of media, if it has not been added before
						if (!mediaUrls.contains(medUrl)) {
							mediaUrls.add(medUrl);
						}
						String timeString = null;
						JSONObject selObject = getFirstJSONObjectWithKey(targetObject, SELECTOR, null);
						if (selObject != null) {
							if (FRAG_SELECTOR.equals(selObject.optString(TYPE))) {
								timeString = selObject.getString(VALUE);
							}
						}
						// hier... parse time string, if not null
						if (timeString != null)	{
							// format "t=12.112,14.557" or "t=,20" etc.
							int eqInd = timeString.indexOf('=');
							int comInd = timeString.indexOf(',');
							
							// the following is too simple to capture allowed variations in time fragments
							//
							if (eqInd >= 0 && comInd > eqInd) {
								long bt, et;
								try {
									if (comInd == eqInd + 1) {
										bt = 0;
										if (comInd == timeString.length() - 1) {
											et = Long.MAX_VALUE;
										} else {
											et = (long) (1000 * Float.parseFloat(timeString.substring(comInd + 1)));
										}
									} else {													
										bt = (long) (1000 * Float.parseFloat(timeString.substring(eqInd + 1, comInd)));
										if (comInd == timeString.length() - 1) {
											et = Long.MAX_VALUE;
										} else {
											et = (long) (1000 * Float.parseFloat(timeString.substring(comInd + 1)));
										}	
									}
									
									//String btId = "ts" + tsId++;
									//String etId = "ts" + tsId++;
									ar.setBeginTimeSlotRecord(new TimeSlotRecord(tsId++, bt));
									ar.setEndTimeSlotRecord(new TimeSlotRecord(tsId++, et));
//									ar.setBeginTimeSlotId(btId);
//									ar.setEndTimeSlotId(etId);
//									
//									timeSlotIdMap.put(btId, bt);
//									timeSlotIdMap.put(etId, et);
								} catch (NumberFormatException nfe) {
									
								}
							} else {
								// message, unparsable
							}
						}
					} else {
						// no target, no time fragment, return null
						return null;
					}
					
					return ar;
				}
			} catch (JSONException je) {
				// there is no "type" key, returns null
			}
			// the "type" is either null or a String
		}
		return null;
		
	}
	*/
	// set of methods to get the first string value for a specific key
	/**
	 * Extracts and returns the first occurring string value for a given
	 * property key. Checks the type of the parameter object and delegates 
	 * to specialized versions of this method.
	 *  
	 * @param inputObj the object to query
	 * @param key the property key
	 * @return the value string or {@code null}
	 */
	@SuppressWarnings("unused")
	private String getFirstStringValue(Object inputObj, String key) {
		if (inputObj instanceof String) {
			return (String) inputObj;
		} else if (inputObj instanceof JSONArray) {
			return getFirstStringValue((JSONArray)inputObj, key);
		} else if (inputObj instanceof JSONObject) {
			return getFirstStringValue((JSONObject)inputObj, key);
		}
		return null;
	}
	
	/**
	 * Extracts and returns the first occurring string value for a given
	 * property key from the given array. Possibly delegates 
	 * to other versions of this method.
	 * 
	 * @param inputArr the {@code JSONArray} to query
	 * @param key the property key
	 * @return the value string or {@code null}
	 */
	private String getFirstStringValue(JSONArray inputArr, String key) {
		for (Object itObj : inputArr) {
			if (itObj instanceof JSONArray) {
				String nextObj = getFirstStringValue((JSONArray)itObj, key);
				if (nextObj != null) {
					return nextObj;
				}
			} else if (itObj instanceof JSONObject) {
				String nextObj = getFirstStringValue((JSONObject)itObj, key);
				if (nextObj != null) {
					return nextObj;
				}	
			} 
			// does this make sense? simply assume the key was already matched?
			else if (itObj instanceof String) {
				return (String) itObj;
			}
		}
		
		return null;
	}
	
	/**
	 * Extracts and returns the first occurring string value for a given
	 * property key from the given {@code JSONObject}. Possibly delegates 
	 * to other versions of this method.
	 * 
	 * @param inputJSObj the {@code JSONObject} to query
	 * @param key the property key
	 * @return the value string or {@code null}
	 */
	private String getFirstStringValue(JSONObject inputJSObj, String key) {
		Object nextObj = inputJSObj.opt(key);
		if (nextObj instanceof String) {
			return (String) nextObj;
		}
		if (nextObj instanceof JSONObject) {
			return getFirstStringValue((JSONObject)nextObj, key);
		}
		if (nextObj instanceof JSONArray) {
			return getFirstStringValue((JSONArray)nextObj, key);
		}
		
		return null;
	}
	// end of set to get string for key
	
	// set of methods to get first JSONObject with specified key or key = value combination
	/**
	 * Returns the first {@code JSONObject} for the specified key or, optionally,
	 * for the specified key-value pair. Checks the type of the input object
	 * and forwards the call to a specialized version of this method.
	 * 
	 * @param inputObj the input object to query
	 * @param key the property key
	 * @param value the value of the property or {@code null}
	 * 
	 * @return the {@code JSONObject} or {@code null}
	 */
	@SuppressWarnings("unused")
	private JSONObject getFirstJSONObjectWithKey(Object inputObj, String key, String value) {
		if (inputObj instanceof JSONObject) {
			return getFirstJSONObjectWithKey((JSONObject)inputObj, key, value);
		} else if (inputObj instanceof JSONArray) {
			return getFirstJSONObjectWithKey((JSONArray)inputObj, key, value);
		}
		return null;
	}
	
	/**
	 * Returns the first {@code JSONObject} for the specified key or, optionally,
	 * for the specified key-value pair.
	 * 
	 * @param inputJSObj the input {@code JSONObject}
	 * @param key the property key
	 * @param value the property value
	 * @return the matching {@code JSONObject} or {@code null}
	 */
	private JSONObject getFirstJSONObjectWithKey(JSONObject inputJSObj, String key, String value) {
		Object nextObj = inputJSObj.opt(key);
		if (nextObj instanceof JSONObject) {
			if (value == null) {
				return (JSONObject) nextObj;// found
			} else if (value.equals(((JSONObject) nextObj).opt(key))) {
				return (JSONObject) nextObj;
			} 
		} else if (nextObj instanceof JSONArray) {
			return getFirstJSONObjectWithKey((JSONArray) nextObj, key, value);
		} else if (nextObj instanceof String) {
			if (value != null && value.equals(nextObj)) {
				return inputJSObj;
			}
		}
			
		return null;
	}
	
	/**
	 * Returns the first {@code JSONObject} for the specified key or, optionally,
	 * for the specified key-value pair.
	 * 
	 * @param inputJSArr the input {@code JSONArray} 
	 * @param key the property key
	 * @param value the property value
	 * @return the matching {@code JSONObject} or {@code null}
	 */
	private JSONObject getFirstJSONObjectWithKey(JSONArray inputJSArr, String key, String value) {
		for (Object itObj : inputJSArr) {
			if (itObj instanceof JSONArray) {
				// if item n returns null, continue with n + 1
				JSONObject fjo = getFirstJSONObjectWithKey((JSONArray) itObj, key, value);
				if (fjo == null) {
					continue;
				}
				return fjo;
			} else if (itObj instanceof JSONObject) {
				if (value != null && value.equals(((JSONObject) itObj).get(key))) {
					return (JSONObject) itObj;
				} else {
					continue;
				}
			}
		}
		return null;
	}
	
	/**
	 * Empties all lists and maps (in the case of repeated use of the same
	 * parser, which is currently not the case).
	 */
	private void reset() {
		mediaDescriptors.clear();
		langList.clear();
		languages.clear();
		linguisticTypes.clear();
		timeSlots.clear();
		timeSlotsMap.clear();
		timeOrder.clear();
		docProperties.clear();
		annIdToRecordsMap.clear();
		colIdToPageIdMap.clear();
		pageIdToAnnoIdMap.clear();
		tierMap.clear();
		tiers.clear();
		mediaUrls.clear();
		creators.clear();
		tierRecordList.clear();
		tierAnnotationsMap.clear();
		tierNames.clear();
		// add other maps and lists
	}
	
	/**
	 * Main method for testing.
	 * 
	 * @param args containing the file to parse
	 */
	public static void main(String[] args) {
		JSONWAParser waj = new JSONWAParser();
		waj.parse(args[0]);
	}

	private class WAAnnotationRecord extends AnnotationRecord {
		private String language;
		private String purpose;
		private String motivation;
		
		public String getLanguage() {
			return language;
		}
		public void setLanguage(String language) {
			this.language = language;
		}
		public String getPurpose() {
			return purpose;
		}
		public void setPurpose(String purpose) {
			this.purpose = purpose;
		}
		public String getMotivation() {
			return motivation;
		}
		public void setMotivation(String motivation) {
			this.motivation = motivation;
		}
		
	}
	
	// following implementations of the Parser interface
	@Override
	public String getMediaFile(String fileName) {
		parse(fileName);
		
		if (mediaUrls.size() > 0) {
			return mediaUrls.get(0);
		} else if (mediaDescriptors.size() > 0) {
			return mediaDescriptors.get(0).mediaURL;
		}
		
		return super.getMediaFile(fileName);
	}

	/**
	 * If only one {@code creator} was found, that property is used for the
	 * author property.
	 */
	@Override
	public String getAuthor(String fileName) {
		parse(fileName);
		// if only one creator was found in the file, use it as author?
		if (creators.size() == 1) {
			return creators.get(0);
		}
		return null;
	}

	@Override
	public List<Property> getTranscriptionProperties(String fileName) {
		parse(fileName);
		// store any top-level key - value pairs?
		return docProperties;
	}

	@Override
	public String getAnnotatorOf(String tierName, String fileName) {
		parse(fileName);
		// the creator of a collection
		TierRecord tr = tierMap.get(tierName);
		if (tr != null) {
			return tr.getAnnotator();
		}
		return super.getAnnotatorOf(tierName, fileName);
	}

	@Override
	public List<LanguageRecord> getLanguages(String fileName) {
		parse(fileName);
		
		return languages;
	}

	@Override
	public String getLangRefOf(String tierName, String fileName) {
		parse(fileName);
		// the language (short id) of the tier, if any 
		TierRecord tr = tierMap.get(tierName);
		if (tr != null) {
			return tr.getLangRef();
		}
		
		return super.getLangRefOf(tierName, fileName);
	}

	@Override
	public List<MediaDescriptor> getMediaDescriptors(String fileName) {
		parse(fileName);
		
		return mediaDescriptors;
	}

	@Override
	public List<LingTypeRecord> getLinguisticTypes(String fileName) {
		parse(fileName);

		return linguisticTypes;
	}

	@Override
	public List<String> getTimeOrder(String fileName) {
		parse(fileName);
		
		return timeOrder;
	}

	@Override
	public Map<String, String> getTimeSlots(String fileName) {
		parse(fileName);
		
		return timeSlotsMap;
	}

	@Override
	public List<String> getTierNames(String fileName) {
		parse(fileName);
		
		return tierNames;
	}

	@Override
	public String getParticipantOf(String tierName, String fileName) {
		parse(fileName);
		// no place for participant in Web Annotation
		TierRecord tr = tierMap.get(tierName);
		if (tr != null) {
			return tr.getParticipant();
		}
		return null;
	}

	@Override
	public String getLinguisticTypeIDOf(String tierName, String fileName) {
		parse(fileName);
		
		return TYPE_NAME;
	}

	/**
	 * Returns {@code null} for now, until tier hierarchies are supported in
	 * Web Annotation or a profile or namespace specific approach is accepted. 
	 */
	@Override
	public String getParentNameOf(String tierName, String fileName) {
		parse(fileName);
		// tier hierarchy still undefined in Web Annotation
		return null;
	}

	@Override
	public List<AnnotationRecord> getAnnotationsOf(String tierName, String fileName) {
		parse(fileName);
		
		return tiers.get(tierName);
	}

}
