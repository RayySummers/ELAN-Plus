package mpi.eudico.client.annotator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;

import mpi.eudico.client.annotator.prefs.PrefKeyMapper;
import mpi.eudico.client.annotator.prefs.PreferencesReader;
import mpi.eudico.client.annotator.prefs.PreferencesWriter;
import static mpi.eudico.client.annotator.util.ClientLogger.LOG;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import nl.mpi.util.FileUtility;

/**
 * Administers the global preferences for ELAN and the preferences for each
 * document. A document is in this implementation the same as a Transcription,
 * maybe this can be made more generic. The methods that return a document key
 * and a preference file path for a document must then be adapted.
 */
public class Preferences {
	
	private Preferences() {
	}
	
	/** The key which stores the Controlled Vocabulary related settings */
    public final static String CV_PREFS = "CV.ML.Prefs";
	/**
	 *  The key which stores the Controlled Vocabulary related settings 
	 *  in the format up to 2.7.
	 */
    public final static String CV_PREFS_OLD_2_7 = "CV.Prefs";
    
    /** key for the default Controlled Vocabulary language */
	public static final String PREF_ML_LANGUAGE = "DefaultControlledVocabularyLanguage";

    /** key for global preferences */
    private final static String GLOBAL_PREFS_KEY = "elan global prefs key";

    /** key for old global preferences */
    private final static String GLOBAL_PREFS_FILE_NAME = "elan.pfs";
    /** key for global XML preferences file */
    private final static String GLOBAL_PREFS_XML_FILE_NAME = "elan.pfsx";
    /** extension for preferences in XML */ 
    private final static String XML_EXT= "pfsx";
    /** key for remote preferences cache */
    public final static String REMOTE_PREF_CACHE = "RemotePrefCache";

    // map of maps, each document has its own hashmap with key value pairs for preferences
    private static Map<Object, Map<String, Object>> preferences;
    
    /** A map of preferences listeners, grouped per document */
    private static Map<Transcription, List<PreferencesListener>> listenerGroups = new HashMap<Transcription, List<PreferencesListener>>();
    
    private static PreferencesReader xmlPrefsReader = new PreferencesReader();
    private static PreferencesWriter xmlPrefsWriter = new PreferencesWriter();
    private static Map<Transcription, String> prefLocations = new HashMap<Transcription, String>();

    /**
     * Get the preference value for a certain preference key.
     * If the document is not {@code null} a document specific value is returned,
     * if it exists, otherwise the global preference value is returned.
     *
     * @param key the preference key value
     * @param document the document for which preferences are asked
     *
     * @return the preference value
     */
    public static Object get(String key, Transcription document) {
        // prevent null pointer exception in HashTables
        if (key == null) {
            return null;
        }

        // make sure the preferences data structure is initialized
        initPreferencesFor(document);

        // first look for a document specific preference setting
        Object result = preferences.get(documentKeyFor(document)).get(key);

        // ready if the preference exists 
        if (result != null) {
            return result;
        }

        // no document specific preference value found, look for a global value
        return preferences.get(GLOBAL_PREFS_KEY).get(key);
    }
    
    /**	
     * Wrapper function that gets a preference setting and
     * checks it is the correct type, in this case {@code Boolean}.
     * 
     * @param key the preference key
     * @param document the document or {@code null} for global preferences
     * 
     * @return a {@code Boolean} or {@code null}
     */
	public static Boolean getBool(String key, Transcription document) {
    	Object o = get(key, document);
    	if (!(o instanceof Boolean)) {
    		return null;
    	}
    	return (Boolean)o;
    }
    
    /**	
     * Wrapper function that gets a preference setting and checks it is the 
     * correct type, in this case {@code Integer}.
     * 
     * @param key the preference key
     * @param document the document or {@code null} for global preferences
     * 
     * @return an {@code Integer} or {@code null}
     */
	public static Integer getInt(String key, Transcription document) {
    	Object o = get(key, document);
    	if (!(o instanceof Integer)) {
    		return null;
    	}
    	return (Integer)o;
    }
    
    /**	
     * Wrapper function that gets a preference setting and checks it is the 
     * correct type, in this case {@code Long}.
     * 
     * @param key the preference key
     * @param document the document or {@code null} for global preferences
     * 
     * @return a {@code Long} or {@code null}
     */
	public static Long getLong(String key, Transcription document) {
    	Object o = get(key, document);
    	if (o == null || !(o instanceof Long)) {
    		return null;
    	}
    	return (Long)o;
    }
    
    /**	
     * Wrapper function that gets a preference setting and checks it is the 
     * correct type, in this case {@code Float}.
     * 
     * @param key the preference key
     * @param document the document or {@code null} for global preferences
     * 
     * @return a {@code Float} or {@code null}
     */
	public static Float getFloat(String key, Transcription document) {
    	Object o = get(key, document);
    	if (!(o instanceof Float)) {
    		return null;
    	}
    	return (Float)o;
    }
    
    /**	
     * Wrapper function that gets a preference setting and checks it is the 
     * correct type, in this case {@code Double}.
     * 
     * @param key the preference key
     * @param document the document or {@code null} for global preferences
     * 
     * @return a {@code Double} or {@code null}
     */
	public static Double getDouble(String key, Transcription document) {
    	Object o = get(key, document);
    	if (!(o instanceof Double)) {
    		return null;
    	}
    	return (Double)o;
    }
    
    /**	
     * Wrapper function that gets a preference setting and checks it is the 
     * correct type, in this case {@code String}.
     * 
     * @param key the preference key
     * @param document the document or {@code null} for global preferences
     * 
     * @return a {@code String} or {@code null}
     */
	public static String getString(String key, Transcription document) {
    	Object o = get(key, document);
    	if (!(o instanceof String)) {
    		return null;
    	}
    	return (String)o;
    }
    
    /**	
     * Wrapper function that gets a preference setting and checks it is the 
     * correct type, in this case {@code Dimension}.
     * 
     * @param key the preference key
     * @param document the document or {@code null} for global preferences
     * 
     * @return a {@code Dimension} or {@code null}
     */
	public static Dimension getDimension(String key, Transcription document) {
    	Object o = get(key, document);
    	if (!(o instanceof Dimension)) {
    		return null;
    	}
    	return (Dimension)o;
    }
    
    /**	
     * Wrapper function that gets a preference setting and checks it is the 
     * correct type, in this case {@code Point}.
     * 
     * @param key the preference key
     * @param document the document or {@code null} for global preferences
     * 
     * @return a {@code Point} or {@code null}
     */
	public static Point getPoint(String key, Transcription document) {
    	Object o = get(key, document);
    	if (!(o instanceof Point)) {
    		return null;
    	}
    	return (Point)o;
    }
    
    /**	
     * Wrapper function that gets a preference setting and checks it is the 
     * correct type, in this case {@code Rectangle}.
     * 
     * @param key the preference key
     * @param document the document or {@code null} for global preferences
     * 
     * @return a {@code Rectangle} or {@code null}
     */
	public static Rectangle getRect(String key, Transcription document) {
    	Object o = get(key, document);
    	if (!(o instanceof Rectangle)) {
    		return null;
    	}
    	return (Rectangle)o;
    }
    
    /**	
     * Wrapper function that gets a preference setting and checks it is the 
     * correct type, in this case {@code Color}.
     * 
     * @param key the preference key
     * @param document the document or {@code null} for global preferences
     * 
     * @return a {@code Color} or {@code null}
     */
	public static Color getColor(String key, Transcription document) {
    	Object o = get(key, document);
    	if (!(o instanceof Color)) {
    		return null;
    	}
    	return (Color)o;
    }
    
    /**
     * Wrapper function that gets a preference setting and checks it is the 
     * correct type, in this case {@code List<?>}.
     * 
     * @param key the preference key
     * @param document the document or {@code null} for global preferences
     * 
     * @return a {@code List<?>} or {@code null}
     */
    public static List<?> getList(String key, Transcription document) {
    	Object o = get(key, document);
    	if (!(o instanceof List)) {
    		return null;
    	}
    	return (List<?>)o;
    }

    /**	
     * Wrapper function that gets a preference setting and checks it is the 
     * correct type, in this case {@code List<String>}.
     * 
     * @param key the preference key
     * @param document the document or {@code null} for global preferences
     * 
     * @return a {@code List<String>} or {@code null}
     */
    @SuppressWarnings("unchecked")
	public static List<String> getListOfString(String key, Transcription document) {
    	List<?> o = getList(key, document);
    	if (o == null) {
    		return null;
    	}
    	if (!o.isEmpty()) {
    		for (Object lo : o) {
        		if (!(lo instanceof String)) {
        			// don't try to remove non-Strings, something is wrong
        			return null;
        		}
    		}
    	}
    	List<String> l = (List<String>) o;
    	return l;
    }

    /**	
     * Wrapper function that gets a preference setting and checks it is the 
     * correct type, in this case {@code Map<String, ?>}.
     * <p>
     * Unfortunately, for {@code Map<K,V>} this isn't cheap.
     * 
     * @param key the preference key
     * @param document the document or {@code null} for global preferences
     * 
     * @return a {@code Map<String, ?>} or {@code null}
     */
    @SuppressWarnings("unchecked")
	public static Map<String, ?> getMap(String key, Transcription document) {
    	Object o = get(key, document);
    	if (!(o instanceof Map)) {
    		return null;
    	}
    	Map<String, String> m = (Map<String, String>) o;
    	return m;
    }

    /**	
     * Wrapper function that gets a preference setting and checks it is the 
     * correct type, in this case {@code Map<String, String>}.
     * <p>
     * Unfortunately, for M{@code Map<K,V>} this isn't cheap.
     * 
     * @param key the preference key
     * @param document the document or {@code null} for global preferences
     * 
     * @return a {@code Map<String, String>} or {@code null}
     */
    @SuppressWarnings("unchecked")
	public static Map<String, String> getMapOfString(String key, Transcription document) {
    	Map<String, ?> m = getMap(key, document);
    	if (m == null) {
    		return null;
    	}
    	if (!m.isEmpty()) {
    		Iterator<?> it = m.values().iterator();
    		Object mo = it.next();
    		if (!(mo instanceof String)) {
    			return null;
    		}
    	}
    	return (Map<String, String>)m;
    }
    
    /**	
     * Wrapper function that gets a preference setting and checks it is the 
     * correct type, in this case {@code Map<String, Color>}.
     * <p>
     * Unfortunately, for {@code Map<K,V>} this isn't cheap.
     * 
     * @param key the preference key
     * @param document the document or {@code null} for global preferences
     * 
     * @return a {@code Map<String, Color>} or {@code null}
     */
    @SuppressWarnings("unchecked")
	public static Map<String, Color> getMapOfColor(String key, Transcription document) {
    	Map<String, ?> m = getMap(key, document);
    	if (m == null) {
    		return null;
    	}
    	if (!m.isEmpty()) {
    		Iterator<?> it = m.values().iterator();
    		Object mo = it.next();
    		if (!(mo instanceof Color)) {
    			return null;
    		}
    	}
    	return (Map<String, Color>)m;
    }

    /**	
     * Wrapper function that gets a preference setting and checks it is the 
     * correct type, in this case {@code Map<String, Integer>}.
     * <p>
     * Unfortunately, for {@code Map<K,V>} this isn't cheap.
     * 
     * @param key the preference key
     * @param document the document or {@code null} for global preferences
     * 
     * @return a {@code Map<String, Integer>} or {@code null}
     */
    @SuppressWarnings("unchecked")
	public static Map<String, Integer> getMapOfInt(String key, Transcription document) {
    	Map<String, ?> m = getMap(key, document);
    	if (m == null) {
    		return null;
    	}
    	if (!m.isEmpty()) {
    		Iterator<?> it = m.values().iterator();
    		Object mo = it.next();
    		if (!(mo instanceof Integer)) {
    			return null;
    		}
    	}
    	return (Map<String, Integer>)m;
    }
    
    /**	
     * Wrapper function that gets a preference setting and checks it is the 
     * correct type, in this case {@code Map<String, Font>}.
     * <p>
     * Unfortunately, for {@code Map<K,V>} this isn't cheap.
     * 
     * @param key the preference key
     * @param document the document or {@code null} for global preferences
     * 
     * @return a {@code Map<String, Font>} or {@code null}
     */
    @SuppressWarnings("unchecked")
	public static Map<String, Font> getMapOfFont(String key, Transcription document) {
    	Map<String, ?> m = getMap(key, document);
    	if (m == null) {
    		return null;
    	}
    	if (!m.isEmpty()) {
    		Iterator<?> it = m.values().iterator();
    		Object mo = it.next();
    		if (!(mo instanceof Font)) {
    			return null;
    		}
    	}
    	return (Map<String, Font>)m;
    }
    
    /**
     * Access function for the whole of the preferences tree.
     * May load the preferences if the Transcription is unknown in the
     * global preferences collection, but if so, doesn't add to it.
     * 
     * @param document the Transcription
     * 
     * @return a map with all stored preferences for the document
     */
    public static Map<String, Object> getPreferencesFor(Transcription document) {
    	Map<String, Object> result = preferences.get(documentKeyFor(document));
    	if (result == null && document != null) {
    		result = loadPreferencesForFile(document.getFullPath());
    	}
    	return result;
    }
    
    /**
     * Set the preference value for a certain document.  If the document is
     * {@code null} a global preference is set.
     *
     * @param key preference key
     * @param value preference value
     * @param document identifier for document specific preferences
     * @param notify if {@code true}, listeners are notified 
     * @param savePrefs if {@code true}, the preferences are saved to file immediately
     */
    public static void set(String key, Object value, Transcription document, 
    		boolean notify, boolean savePrefs) {
        // prevent null pointer exception in HashTables
        if (key == null) {
            return;
        }

        // make sure the preference data structure is initialized
        initPreferencesFor(document);

        // if two ElanFrames are opened for the same .eaf file a cvs like
        // update must be done here. 
        // Disabled because it is not obvious better than doing nothing special
        //preferences.put(documentKeyFor(document), readPreferencesFor(document));
        // put the preference value in the hash table for the document
        preferences.get(documentKeyFor(document)).put(key, value);

        // make the current preferences for this document persistent
        if (savePrefs) {
        	writePreferencesFor(document);
        }
        
        // notify listeners
        if (notify) {
        	    if (document != null) {
        	        notifyListeners(document);
        	    } else {
        	    	    notifyAllListeners();// application wide setting
        	    }
        }
    }

    /**
     * Sets a preference after which the preferences are stored immediately.
     * 
     * @param key preference key
     * @param value preference value
     * @param document identifier for document specific preferences
     * @param notify it true preferences listeners for this document will be notified of 
     * the change
     */
    public static void set(String key, Object value, Transcription document, boolean notify) {
    	set(key, value, document, notify, true);
    }
    
    /**
     * Sets a preference without notification of listeners.
     * 
     * @param key preference key
     * @param value preference value
     * @param document identifier for document specific preferences
     */
    public static void set(String key, Object value, Transcription document) {
    	set(key, value, document, false);
    }
    
    /**
     * Specialized version for the Object value version of setPreference
     *
     * @param key preference key
     * @param value preference value as an int
     * @param document identifier for document specific preferences
     */
    public static void set(String key, int value, Transcription document) {
        set(key, Integer.valueOf(value), document);
    }

    /**
     * Specialized version for the Object value version of setPreference
     *
     * @param key preference key
     * @param value preference value as a long
     * @param document identifier for document specific preferences
     */
    public static void set(String key, long value, Transcription document) {
        set(key, Long.valueOf(value), document);
    }

    /**
     * Specialized version for the Object value version of setPreference
     *
     * @param key preference key
     * @param value preference value as a float
     * @param document identifier for document specific preferences
     */
    public static void set(String key, float value, Transcription document) {
        set(key, Float.valueOf(value), document);
    }

    /**
     * Specialized version for the Object value version of setPreference
     *
     * @param key preference key
     * @param value preference value as a double
     * @param document identifier for document specific preferences
     */
    public static void set(String key, double value, Transcription document) {
        set(key, Double.valueOf(value), document);
    }

    /**
     * Removes the stored preferences Map for the specified document
     * from the global Map. And removes the listeners to changes in preferences 
     * for this document.
     *  
     * @param document the transcription, used as a key in the map
     */
    public static void removeDocument(Transcription document) {
    	if (preferences != null) {
    		preferences.remove(document);
    	}
    	prefLocations.remove(document);
    	
    	listenerGroups.remove(document);
    }
    
    /**
     * Adds a PreferencesListener to the listener list of the specified document.
     *  
     * @param document the document in which changes the listener is interested, 
     *       the key to the group of listeners per document
     * @param listener the listener to changes in the preferences for the specified document
     */
    public static void addPreferencesListener(Transcription document, PreferencesListener listener) {
		if (listenerGroups.containsKey(document)) {
			// check whether it is already in the list
			List<PreferencesListener> listeners = listenerGroups.get(document);
			if (!listeners.contains(listener)) {
				listeners.add(listener);
			}
			
			//listener.preferencesChanged();//??
		} else {
			List<PreferencesListener> list = new ArrayList<>();
			list.add(listener);
			
			listenerGroups.put(document, list);
			//listener.preferencesChanged();//??
		}
    }
    
    /**
     * Removes a PreferencesListener from the listener list of the specified document.
     * 
     * @param document the document in which changes the listener is interested
     * @param listener the listener to changes in the preferences for the specified document
     */
    public static void removePreferencesListener(Transcription document, PreferencesListener listener) {
    	if (listenerGroups.containsKey(document)) {
    		listenerGroups.get(document).remove(listener);
    	}
    }
    
    /**
     * Exports the preferences for the specified document to a new Preferences file.
     * If the default save format is for an older EAF version, convert them.
     * 
     * @param document the document to export the preferences of
     * @param filePath the path to the new preferences file
     */
    public static void exportPreferences(Transcription document, String filePath) {
    	if (document == null || filePath == null) {
    		return;
    	}
        xmlPrefsWriter.encodeAndSave(preferences.get(documentKeyFor(document)),
        		filePath);
    }
    
    /**
     * Loads the preferences stored in the specified file and applies them to the 
     * (listeners of) the specified document.
     * 
     * @param document the document to apply the loaded preferences to
     * @param filePath the path to the preferences file to load
     */
    public static void importPreferences(Transcription document, String filePath) {
    	if (document == null) {
    		return;
    	}
    	// parse the file, read the prefs
    	
    	Map<String, Object> loadedPrefs = xmlPrefsReader.parse(filePath);
    	if (loadedPrefs.size() == 0) {
    		return;
    	}
    	// apply the prefs to the document by using set(key, object, document) for all elements
    	if (preferences == null) {
    		preferences = new HashMap<Object, Map<String, Object>>();
    	}
    	// replace current settings
    	if(preferences.get(documentKeyFor(document)) == null){
    		preferences.put(documentKeyFor(document), loadedPrefs);
    	} else { 
    		preferences.get(documentKeyFor(document)).putAll(loadedPrefs);
    	}
    	// write preferences
    	writePreferencesFor(document);
    	
    	// notify all listeners of the document
    	notifyListeners(document);
    }
    
    /**
     * Applies the preferences loaded in the specified map to the 
     * (listeners of) the specified document.
     * 
     * @param document the document to apply the loaded preferences to
     * @param loadedPrefs the map with preferences from another file, already loaded
     */
    public static void importPreferences(Transcription document, Map<String, Object> loadedPrefs) {
    	if (document == null) {
    		return;
    	}

    	if (loadedPrefs == null || loadedPrefs.size() == 0) {
    		return;
    	}
    	// apply the prefs to the document by using set(key, object, document) for all elements
    	if (preferences == null) {
    		preferences = new HashMap<Object, Map<String, Object>>();
    	}
    	// replace current settings
    	if(preferences.get(documentKeyFor(document)) == null){
    		preferences.put(documentKeyFor(document), loadedPrefs);
    	} else { 
    		preferences.get(documentKeyFor(document)).putAll(loadedPrefs);
    	}
    	// write preferences
    	writePreferencesFor(document);
    	
    	// notify all listeners of the document
    	notifyListeners(document);
    }
    
    /**
     * This method can be used to load preferences for a file without having to completely 
     * load that transcription file. The preferences are not cached.
     * 
     * @param filePath the path to the eaf file
     * @return a {@code Map} containing the preferences, or null
     */
    public static Map<String, Object> loadPreferencesForFile(String filePath) {
    	if (filePath == null) {
    		return null;
    	}
    	String prefFileName = filePath.replace('\\', '/');
        
    	if (prefFileName.length() > 3) {
            prefFileName = prefFileName.substring(0,
                    prefFileName.length() - 3) + XML_EXT;
        }
        // check if there is a "default" directory for preferences files
        Object val = Preferences.get("DefaultPreferencesLocation", null);
        
        if (val instanceof String) {
        	String genPrefsLocation = ((String) val).replace('\\', '/');
        	String fileName = prefFileName;
        	int lastSep = fileName.lastIndexOf('/');
        	if (lastSep > -1) {
        		fileName = fileName.substring(lastSep);// includes the separator
        	}

        	prefFileName = genPrefsLocation + fileName;
        }
        
    	if (prefFileName.startsWith("file:")) {
    		prefFileName = prefFileName.substring(5);
    	}
    	
    	try {
    		if (new File(prefFileName).exists()) {
    			return xmlPrefsReader.parse(prefFileName);
    		}
    	} catch (Exception ex) { //any
    		if (LOG.isLoggable(Level.WARNING)) {
    			LOG.log(Level.WARNING, "Could not load preferences file: " + ex.getMessage());
    		}
    	}
    	return null;
    }
    
    /**
     * Notifies the preferences listeners of the specified document that the 
     * preferences have changed.
     * 
     * @param document the document of which the listeners have to be notified
     */
    public static void notifyListeners(Transcription document) {
    	List<PreferencesListener> listeners = listenerGroups.get(document);
    	if (listeners != null) {
			for (int i = 0; i < listeners.size(); i++) {
				listeners.get(i).preferencesChanged();
			}
    	}
    }
    
    /**
     * Method for notifying all listeners of document independent, application wide
     * preference changes.
     */
    private static void notifyAllListeners() {
    	    List<PreferencesListener> listeners = null;
    	    Iterator<List<PreferencesListener>> listIt = listenerGroups.values().iterator();
    	    
    	    while (listIt.hasNext()) {
    	    	   listeners = listIt.next();
    	       	if (listeners != null) {
    				for (int i = 0; i < listeners.size(); i++) {
    					listeners.get(i).preferencesChanged();
    				}
    	    	    }
    	    }
    }
    
    /**
     * Takes care of initializing the data structures for the preferences of a
     * certain document.
     *
     * @param document the document for which preferences are to be
     *        initialized, {@code null} means global preferences
     */
    private static void initPreferencesFor(Transcription document) {
        // make sure the master hash table exists 
        if (preferences == null) {
            preferences = new HashMap<Object, Map<String, Object>>();
        }

        // make sure the preferences for the document are initialized
        if (!preferences.containsKey(documentKeyFor(document))) {
            // read the Hashtable from the preference file if it exists, otherwise create a new hash table
            Map<String, Object> documentPreferences = readPreferencesFor(document);

            // place the document specific preferences table in the master table
            preferences.put(documentKeyFor(document), documentPreferences);
            
        }
    }
    
    /**
     * Create a Map with preference key/value pairs from the persistent
     * format. If there are no persistent preferences for the document an
     * empty Map is returned.
     * Feb 2009: if a default preferences directory has been defined, read preferences from
     * that directory. Some preferences are taken from the "normal" preferences file.
     * Oct 2013 no preference locations are created anymore for non-eaf files
     *
     * @param document the document for which the preferences are asked
     *
     * @return a {@code Map} with the persistent preferences for the document
     */
    @SuppressWarnings("unchecked")
	private static Map<String, Object> readPreferencesFor(Transcription document) {
        Map<String, Object> preferencesHashtable = null;
        String xmlPath = "";

        try {    
        	xmlPath = preferenceXmlFilePathFor(document);
        	if (xmlPath == null) {
        		return new HashMap<String, Object>();
        	}

        	if (new File(xmlPath).exists()) {
        		preferencesHashtable = xmlPrefsReader.parse(xmlPath);
        	} else if (new File(preferenceFilePathFor(document)).exists()) {
        		// try the old format
                try(FileInputStream fileIn = new FileInputStream(preferenceFilePathFor(document));
                    ObjectInputStream objectIn = new ObjectInputStream(fileIn)) {               
	                preferencesHashtable = (Hashtable<String, Object>) objectIn.readObject();
	                // convert old Hashtable to new HashMap etc
	                preferencesHashtable = convertPreferencesTable(preferencesHashtable);
	                // printPrefs(preferencesHashtable);
                }

            } else if (document != null && FileUtility.isRemoteFile(document.getFullPath())) {
            	// remote preferences for remote file,not yet in the cache?
            	xmlPath = document.getFullPath().replaceFirst("eaf$", XML_EXT);
        		preferencesHashtable = xmlPrefsReader.parse(xmlPath);
            }
        } catch (Exception e) {
            //e.printStackTrace();
            if (LOG.isLoggable(Level.WARNING)) {
            	LOG.log(Level.WARNING, "Could not read a document's preferences file: " + e.getMessage());
            }
        }
        
        if (preferencesHashtable != null) {
        	return preferencesHashtable;
        } else {
        	return new HashMap<String, Object>();
        }       
    }

    /**
     * Make the preferences for a certain document persistent.
     *
     * @param document the document for which the preferences are to be saved
     */
    private static void writePreferencesFor(Transcription document) {
		//ObjectOutputStream objectOut = null;
		//FileOutputStream fileOut = null;
        try {
            // do not save prefs for a new file that has no new name yet
            if ((document != null) &&
                    document.getName().equals(TranscriptionImpl.UNDEFINED_FILE_NAME)) {
                return;
            }
            /*
            fileOut = new FileOutputStream(preferenceFilePathFor(document));

            objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(preferences.get(documentKeyFor(document)));
            objectOut.close();
            fileOut.close();
            */
            // test this should become the default
            String prefPath = preferenceXmlFilePathFor(document);
            if (prefPath != null) {
            	               
            	// convert the preferences to the other format if needed...
            	int format = SaveAs27Preferences.saveAsType(document);
            	Object orig = SaveAs27Preferences.adjustPreferencesForSavingFormat(document, format);
            	
            	xmlPrefsWriter.encodeAndSave(preferences.get(documentKeyFor(document)),
            			prefPath);

            	// Restore our original preferences
                SaveAs27Preferences.restoreAdjustedPreferences(document, orig);
            }
        } catch (Exception e) {
            e.printStackTrace();
            /*
            try {
            	if (objectOut != null) {
					objectOut.close();
            	}
				if (fileOut != null) {
					fileOut.close();
				}				
            } catch (Exception e2){}
            */
        }

    }

    /**
     * A valid key is generated for a Transcription document.
     * If the transcription is {@code null} the global preferences key is
     * returned, otherwise the transcription is returned.
     *
     * @param document a Transcription or {@code null} for the global preferences
     *
     * @return a unique key for the map that holds the preferences for
     *         this document
     */
    private static Object documentKeyFor(Transcription document) {
        if (document == null) {
            return GLOBAL_PREFS_KEY;
        } else {
            return document;
        }
    }

    /**
     * Gets a preference file name for a document. If the key is {@code null}
     * the global preference file is used. Otherwise the .eaf file name is 
     * used with extension .pfsx instead of .eaf.
     * 
     * @see #preferenceXmlFilePathFor(Transcription) this method is still here 
     * for backward compatibility
     * 
     * @param document a Transcription or {@code null} for the global preferences
     *
     * @return a full path to the preferences file for the document
     *
     * @throws Exception any IO exception
     */
    private static String preferenceFilePathFor(Transcription document)
        throws Exception {
        if (document == null) {
            return Constants.ELAN_DATA_DIR +
            System.getProperty("file.separator") + GLOBAL_PREFS_FILE_NAME;
        } else {
            String prefFileName = "";

            if (document instanceof TranscriptionImpl) {
                prefFileName = ((TranscriptionImpl) document).getPathName(); // do not use getFullPath Name from Transcription
            }

            if (prefFileName.length() > 3) {
                prefFileName = prefFileName.substring(0,
                        prefFileName.length() - 3) + "pfs";
            }

            return prefFileName;
        }
    }

    /**
     * Gets a new XML preference file name for a document. If the key is 
     * {@code null} the global preference file is used. Otherwise the .eaf file
     * name is used with extension .pfsx instead of .eaf.
     * Oct 2013 no preference path is created for non-eaf files.
     *
     * @param document a Transcription or {@code null} for the global preferences
     *
     * @return a full path to the preferences file for the document
     *
     * @throws Exception any exception
     */
    private static String preferenceXmlFilePathFor(Transcription document)
        throws Exception {
        if (document == null) {
            return Constants.ELAN_DATA_DIR +
            System.getProperty("file.separator") + GLOBAL_PREFS_XML_FILE_NAME;
        } else {
        	if (prefLocations.containsKey(document)) {
        		// return the stored path
        		return prefLocations.get(document);
        	}
            String prefFileName = "";

            if (document instanceof TranscriptionImpl) {
                prefFileName = ((TranscriptionImpl) document).getPathName(); // do not use getFullPath Name from Transcription
            }

            if (!prefFileName.toLowerCase().endsWith("eaf")) {
            	return null;
            }
            
            if (prefFileName.length() > 3) {
                prefFileName = prefFileName.substring(0,
                        prefFileName.length() - 3) + XML_EXT;
            }

	        // check if there is a "default" directory for preferences files
	        Object val = Preferences.get("DefaultPreferencesLocation", null);
	        
	        if (val instanceof String) {
	        	String genPrefsLocation = ((String) val).replace('\\', '/');
	        	String fileName = prefFileName.replace('\\', '/');
	        	int lastSep = fileName.lastIndexOf('/');
	        	if (lastSep > -1) {
	        		fileName = fileName.substring(lastSep);// includes the separator
	        	}
	        	genPrefsLocation = genPrefsLocation + fileName;
	        	if (genPrefsLocation.startsWith("file:")) {
	        		genPrefsLocation = genPrefsLocation.substring(5);
	        	}
	        	try {
	        		if (new File(genPrefsLocation).exists()) {
	        			prefFileName = genPrefsLocation;
	        			// redirected path
	        			prefLocations.put(document, prefFileName);
	        		}
	        	} catch (Exception ex) { //any
	        		
	        	}
	        } else if (FileUtility.isRemoteFile(prefFileName)) {
	        	File cacheDir = new File(Constants.ELAN_DATA_DIR + Constants.FILESEPARATOR + 
	        			REMOTE_PREF_CACHE);
	        	if (!cacheDir.exists()) {
	        		if (!cacheDir.mkdir()) {
	        			if (LOG.isLoggable(Level.WARNING)) {
	        				LOG.log(Level.WARNING, 
	        						"Could not create a folder for preferences for remote files");
	        			}
	        		}
	        		
	        	}
	        	prefFileName = Constants.ELAN_DATA_DIR + Constants.FILESEPARATOR + 
	        			REMOTE_PREF_CACHE + Constants.FILESEPARATOR +
	        			FileUtility.fileNameFromPath(prefFileName);
	        	// redirected path
    			prefLocations.put(document, prefFileName);
	        }
	        
            return prefFileName;
        }
    }
 
    /** 
     * Convert a pre-XML preferences Map to a new HashMap.
     * 
     * @param oldPrefs the old Hashtable
     * @return a HashMap with converted preference objects
     */
    @SuppressWarnings("unchecked")
	private static Map<String, Object> convertPreferencesTable(Map<String, Object> oldPrefs) {
    	if (oldPrefs == null) {
    		return null;
    	}
    	// convert...
    	Map<String, Object> nextMap = new HashMap<String, Object>();
    	oldPrefs.remove("LastUsedShoeboxMarkers");
    	// Vector to List
    	Object tierOrder = oldPrefs.remove("TierOrder");
    	if (tierOrder instanceof Vector) {
    		ArrayList<String> al = new ArrayList<String>((Vector<String>)tierOrder);
    		nextMap.put("MultiTierViewer.TierOrder", al);
    	}
    	// array to List
    	Object to = oldPrefs.remove("Interlinear.VisibleTiers");   	
    	if (to instanceof String[]) {
    		String[] visb = (String[]) to;
    		ArrayList<String> vtList = new ArrayList<String>(visb.length);
    		for (String element : visb) {
    			vtList.add(element);
    		}
    		nextMap.put("Interlinear.VisibleTiers", vtList);
    	}
    	// flatten the viewer state preferences
    	Map<String, Object> viewersState = (Map<String, Object>) oldPrefs.remove("LayoutManagerState");
    	if (viewersState != null) {
    		for (Map.Entry<String, Object> entry : viewersState.entrySet()) {
    			String key = entry.getKey();
    			if (key.equals("TimeSeriesPanelMap")) {
    				// special treatment
    				Map<?, ?> tsMap = null;
    				if (entry.getValue() instanceof Map<?, ?>) {
    					tsMap = (Map<?, ?>) entry.getValue();
    				}
    				
    				if (tsMap != null) {
    					for (Map.Entry<?, ?> tsEntry : tsMap.entrySet()) {
    						Object tsKey = tsEntry.getKey();
    						if (tsKey instanceof Integer) {
    							Object tsVal = tsEntry.getValue();
    							String newKey = "TimeSeriesViewer.Panel-" + ((Integer) tsKey).intValue();
    							if (tsVal instanceof String[]) {
    								String[] names = (String[]) tsVal;
    								ArrayList<String> namesList = new ArrayList<String>(names.length);
    								for (String name : names) {
    									namesList.add(name);
    								}
    								nextMap.put(newKey, namesList);
    							}
    						}
    					}
    				}
    			} else {
    				String newKey = (String) PrefKeyMapper.keyMapper.get(key);
    				if (newKey != null) {
    					if (key.startsWith("SubTitleTierName") && key.length() > 16) {
    						newKey += key.substring(16);
    					} else if (key.startsWith("SubTitleFontSize") 
    							&& key.length() > 16) {
    						newKey += key.substring(16);
    					} 
    					nextMap.put(newKey, entry.getValue());					
    				} else {
    					nextMap.put(key, entry.getValue());
    				}
    			}
    		}
    	}
    	// finally add the rest
    	nextMap.putAll(oldPrefs);
    	return nextMap;
    }
    
    /**
     * Print the preferences that are currently stored.
     * 
     * @param prefs a collection of preferences
     */
    @SuppressWarnings({ "unused", "unchecked" })
    private static void printPrefs(Object prefs) {
		if (prefs == null || !(prefs instanceof Map)) {
			return;
		}
		Map<?, ?> prf = (Map<?, ?>) prefs;
		for (Map.Entry<?, ?> entry : prf.entrySet()) {
			Object ke = entry.getKey();
			Object val = entry.getValue();
			if (ke == null) {
				System.out.println("Entry: key is null...");
				continue;
			}
			System.out.println("K: " + ke  + " (" + ke.getClass() + ")");
			if (val == null) {
				System.out.println("Entry: value is null...");
				continue;
			}
			System.out.println("V: " + val + " (" + val.getClass() + ")");
			if (val instanceof Object[]) {
				val = Arrays.asList((Object[])val);
			}
			if (val instanceof List) {
				List<Object> li = (List<Object>) val;
				for (int i = 0; i < li.size(); i++) {
					Object vv = li.get(i);
					if (vv != null) {
						System.out.println("\tentry: " + vv.toString());
					}					
				}
			} else if (val instanceof Map) {
				Map<?, ?> mm = (Map<?, ?>) val;

				for (Map.Entry<?, ?> mapEntry : mm.entrySet()) {
					Object kk = mapEntry.getKey();
					Object vv = mapEntry.getValue();
					if (kk != null) {
						System.out.println("\tK: " + kk + " (" + kk.getClass() + ")");
						if (vv != null) {
							System.out.println("\tV: " + vv + " (" + vv.getClass() + ")");
						} else {
							System.out.println("\tvalue is null");
						}
					} else {
						System.out.println("\tkey is null");
					}				
				}
			}
		}
    }

	/**
	 * Notify all open Transcriptions that the preferred CV language has changed,
	 * so they may need to re-fetch values from CVs that are associated with Annotations.
	 * This isn't a normal Preference, because that would force Transcriptions (a server
	 * side concept) to become a PreferencesListener (a client side concept).
	 * 
	 * @param newLanguageValue the new preferred CV language
	 * @param force if {@code true} annotations connected to a CV entry are
	 * updated, if necessary 
	 */
	static public void updateAllCVLanguages(String newLanguageValue, boolean force) {
		for (Transcription t : FrameManager.getInstance().getOpenTranscriptions()) {
			((TranscriptionImpl) t).updateCVLanguage(newLanguageValue, force);
		}
	}
}
