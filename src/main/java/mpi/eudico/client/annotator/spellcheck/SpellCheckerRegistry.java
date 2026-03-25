package mpi.eudico.client.annotator.spellcheck;

import java.awt.EventQueue;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.FrameManager;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.ShutdownListener;
import mpi.eudico.client.annotator.spellcheck.SpellCheckerFactory.SpellCheckerType;
import static mpi.eudico.client.annotator.util.ClientLogger.LOG;
import mpi.eudico.util.IoUtil;

/**
 * Singleton class that keeps all existing spell checkers in one place.
 * <p>
 * March 2022: removed the support for the settings in a properties file 
 * for the Gecco spell checkers. Only {@code Valkuil} still seems to be online
 * ({@code Fowlt} seems to be removed) and the service call that is made does
 * not require a login. Currently the service is called with single words 
 * (not sentences) and the suggestions are produced by Hunspell, so using
 * this service doesn't make too much sense.
 * 
 * @author michahulsbosch
 */
public class SpellCheckerRegistry implements ShutdownListener {
	/** The map of all existing spell checkers, identified by language reference (ISO 639-2) */
	private HashMap<String, SpellChecker> spellCheckers;// = new HashMap<String, SpellChecker>();
	
	/** The set of default spell checkers as found in ElanDefault.properties. */
	Set<String> defaultCheckers = new HashSet<String>();
	
	/** The singleton instance */
	private static SpellCheckerRegistry instance = null;
	
	/** File name of the spell check preferences */
	public static final String privatePreferencesFile = "SpellCheckers.xml";
	
	/** Sources of spell checker settings. */
	public enum SettingSource {
		/**
		 * Settings read from a properties file.
		 */
		PROPERTIES,
		/**
		 * Settings from stored preferences.
		 */
		PREFERENCES
	}
	
	/**
	 * Private constructor.
	 */
	private SpellCheckerRegistry() {
		FrameManager.getInstance().addWindowCloseListener(this);
		spellCheckers = new HashMap<String, SpellChecker>();
	}
	
	/**
	 * Returns the single instance of this registry.
	 * 
	 * @return the single registry instance
	 */
	public static synchronized SpellCheckerRegistry getInstance() {
		if(instance == null) {
			instance = new SpellCheckerRegistry();
			// Create and register spell checkers from preferences
			instance.loadFrom(SettingSource.PREFERENCES);
			//instance.loadFrom(SettingSource.PROPERTIES);
		}
		return instance;
	}
	
	/**
	 * Returns a spell checker for a specific language, if one has been 
	 * registered.
	 * 
	 * @param languageRef the language reference
	 * @return the spell checker or {@code null}
	 */
	public SpellChecker getSpellChecker(String languageRef) {
		return spellCheckers.get(languageRef);
	}
	
	/**
	 * Returns whether or not a spell checker has been loaded for a language.
	 * 
	 * @param languageRef the language reference
	 * @return {@code true} if a spell checker has been loaded for the 
	 * language, {@code false} otherwise 
	 */
	public Boolean hasSpellCheckerLoaded(String languageRef) {
		return spellCheckers.containsKey(languageRef);
	}
	
	/**
	 * Returns the number of currently loaded spell checkers.
	 * 
	 * @return the number of loaded spell checkers
	 */
	public int getSpellCheckerCount() {
		return spellCheckers.size();
	}
	
	/**
	 * Registers a loaded spell checker for a specific language.
	 * 
	 * @param languageRef the language reference
	 * @param checker the spell checker instance
	 */
	public void putSpellChecker(String languageRef, SpellChecker checker) {
		putSpellChecker(languageRef, checker, false);
	}
	
	/**
	 * Registers a loaded spell checker for a specific language.
	 * 
	 * @param languageRef the language reference
	 * @param checker the spell checker instance
	 * @param isDefault if {@code true} this spell checker is the default for the language
	 */	
	public void putSpellChecker(String languageRef, SpellChecker checker, Boolean isDefault) {
		spellCheckers.put(languageRef, checker);
		if(isDefault) {
			defaultCheckers.add(languageRef);
		} else if(defaultCheckers.contains(languageRef)) {
			defaultCheckers.remove(languageRef);
		}
		updatePreferences();	
	}
	
	/**
	 * Removes the spell checker for a language.
	 * 
	 * @param languageRef the language to remove
	 */
	public void delete(String languageRef) {
		spellCheckers.remove(languageRef);
		if(defaultCheckers.contains(languageRef)) {
			defaultCheckers.remove(languageRef);
		}
		updatePreferences();
	}
	
	private void updatePreferences() {
		// setting the preference triggers an update event which can interfere with an editor
		EventQueue.invokeLater(() -> {		
			ArrayList<String> list = new ArrayList<String>();
			for(Map.Entry<String, SpellChecker> entry : spellCheckers.entrySet()) {
				SpellChecker checker = entry.getValue();
				list.add(entry.getKey() + "," + checker.getPreferencesString());
			}
			Preferences.set("SpellCheckerRegistry", list, null, true, true);
		});
	}

	/**
	 * Returns all spell checkers.
	 * 
	 * @return a map of all registered spell checkers
	 */
	public HashMap<String, SpellChecker> getSpellCheckers() {
		return spellCheckers;
	}
	
	/**
	 * Loads a spell checker.
	 * 
	 * @param source the source for preferred settings
	 */
	public synchronized void loadFrom(SettingSource source) {
		/*if(source.equals(SettingSource.PROPERTIES)) {
			loadFromProperties();
		} else*/ 
		if(source.equals(SettingSource.PREFERENCES)) {
			loadFromPreferences();
		}
	}
	
	/**
	 * Loads spell checker settings from the ElanDefault.properties file 
	 * containing default settings. 
	 * No longer in use, only one Gecco service is still supported, with user
	 * "anonymous".
	 */
	/*
	public void loadFromProperties() {
		// Read the properties file
		ResourceBundle resourcebundle = ResourceBundle.getBundle( "mpi.eudico.client.annotator.resources.ElanDefault");
		
		// Add a spell checker for every language encountered.
		String geccoClientLanguages = resourcebundle.getString("GeccoClient.Languages");
		if (geccoClientLanguages != null) {
			String[] languages = geccoClientLanguages.split(",");
			for (String language : languages) {
				// Add the spell checker if there is none yet for this language.
				if (!SpellCheckerRegistry.getInstance().hasSpellCheckerLoaded(language)) {
					String url = resourcebundle.getString("GeccoClient.Url." + language);
					String username = resourcebundle.getString("GeccoClient.Username." + language);
					String passwordEncoded = resourcebundle.getString("GeccoClient.Password." + language);
					String password = new String(Base64.decode(passwordEncoded));

					HashMap<String, String> spellCheckerSettings = new HashMap<String, String>();
					spellCheckerSettings.put("url", url);
					spellCheckerSettings.put("username", username);
					spellCheckerSettings.put("password", password);
					SpellChecker spellChecker = SpellCheckerFactory.create(SpellCheckerType.GECCO,
							spellCheckerSettings);

					if (spellChecker != null) {
						try {
							spellChecker.initializeSpellChecker();
							putSpellChecker(language, spellChecker, true);
						} catch (SpellCheckerInitializationException e) {
							if(LOG.isLoggable(Level.WARNING)) {
								LOG.warning("The spell checker '" + spellChecker.getInfo() + "'could not be initialized (" + e.getMessage() + ")");
							}
						}
					}
				}
			}
		}
	}
	*/
	
	/**
	 * Loads spell checker settings from preferences, creates spell checkers from that settings
	 * and registers the spell checkers in the SpellCheckerRegistry.
	 * This method is usually called only once when ELAN is started.
	 */
	public void loadFromPreferences() {
		syncWithPrivatePreferences();		
	}

	@Override
	public void somethingIsClosing(ShutdownListener.Event e) {
		if (e.getType() == Event.ELAN_EXITS_EARLY) {
			savePrivatePreferences();
		}
	}

	private void savePrivatePreferences() {
		DocumentBuilderFactory dbf;
	    DocumentBuilder db;

        dbf = DocumentBuilderFactory.newInstance();
        try {
			db = dbf.newDocumentBuilder();
			
			Document doc = db.newDocument();
			Element root = doc.createElement("SPELL_CHECKERS");
			doc.appendChild(root);
			
			for (Map.Entry<String, SpellChecker> spellEntry : spellCheckers.entrySet()) {
				String languageRef = spellEntry.getKey();
				// Only save non default spell checkers
				if(!defaultCheckers.contains(languageRef)) {
					SpellChecker checker = spellEntry.getValue();
					Element l = doc.createElement("SPELL_CHECKER");
					l.setAttribute("LANG_ID", languageRef);
					l.setAttribute("PREFS", checker.getPreferencesString());
					
					for (String newWord : checker.getUserDefinedWords()) {
						Element w = doc.createElement("USER_DEFINED_WORD");
						w.setTextContent(newWord);
						l.appendChild(w);
					}
					
					root.appendChild(l);
				}
			}
			
			// DOM tree finished; now write a file.
			
            String fileName = Constants.ELAN_DATA_DIR + File.separator + privatePreferencesFile;
			IoUtil.writeEncodedFile("UTF-8", fileName, doc.getDocumentElement());
			
		} catch (ParserConfigurationException e) {
			if(LOG.isLoggable(Level.WARNING)) {
            	LOG.warning("Error in XML parser configuration (" + e.getMessage() + ")");
            }
		} catch (Exception e) {
			if(LOG.isLoggable(Level.WARNING)) {
            	LOG.warning("Error when writing private preferences for spell checkers (" + e.getMessage() + ")");
            }
		}
	}
	
	private void syncWithPrivatePreferences() {		
        try {
    		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
    		parserFactory.setNamespaceAware(false);
    		parserFactory.setValidating(false);
    		
    		SAXParser parser = parserFactory.newSAXParser();
            SpellCheckersHandler sch = new SpellCheckersHandler();
            String fileName = Constants.ELAN_DATA_DIR + File.separator + privatePreferencesFile;
            
            parser.parse(fileName, sch);
        } catch (SAXException e) {
        	if(LOG.isLoggable(Level.WARNING)) {
            	LOG.warning("The XML of the spell checking private preferences could not be read (" + e.getMessage() + ")");
            }
        } catch (FileNotFoundException e) {
        	// If the file is not there, that's not a problem.
        } catch (IOException e) {
        	if(LOG.isLoggable(Level.WARNING)) {
            	LOG.warning("The file of the spell checking private preferences could not be read (" + e.getMessage() + ")");
            }
		} catch (ParserConfigurationException e) {
			if(LOG.isLoggable(Level.WARNING)) {
            	LOG.warning("No parser for the XML of the spell checking private preferences (" + e.getMessage() + ")");
            }
		}
	}
	
	/**
	 * Handler for parsing the list of spell checkers.
	 * 
	 * @author michahulsbosch
	 */
	private class SpellCheckersHandler extends DefaultHandler {
		SpellChecker currentChecker;
		private StringBuffer curCharValue = new StringBuffer(1024);
		
		public SpellCheckersHandler() {
		}

        @Override
        public void startElement(String nameSpaceURI, String name, String rawName,
                Attributes attrs) throws SAXException {

        	if ("SPELL_CHECKER".equals(rawName)) {
            	String id = attrs.getValue("LANG_ID");
        		String checkerStr = attrs.getValue("PREFS");
        		
        		SpellChecker checker = null;
    			String[] checkerSettings = checkerStr.split(",");
    			if(checkerSettings[0].equals(SpellCheckerType.GECCO.toString())) {
    				HashMap<String, String> args = new HashMap<String,String>();
    				if (checkerSettings.length == 3) {// new preferences
    					args.put("url", checkerSettings[1] + checkerSettings[2]);
    				} else if (checkerSettings.length > 3) {
    					args.put("url", checkerSettings[2] + checkerSettings[3]);
    				}
    				//args.put("username", checkerSettings[4]);
    				//args.put("password", checkerSettings[5]);
    				checker = SpellCheckerFactory.create(SpellCheckerType.GECCO, args);
    			} else if(checkerSettings[0].equals(SpellCheckerType.HUNSPELL.toString())) {
    				HashMap<String, String> args = new HashMap<String,String>();
    				args.put("path", checkerSettings[1]);
    				checker = SpellCheckerFactory.create(SpellCheckerType.HUNSPELL, args);
    			}
    			
    			if(checker != null) {
    				try {
    					checker.initializeSpellChecker();
    					putSpellChecker(id, checker);
    					currentChecker = checker;
    				} catch (SpellCheckerInitializationException e) {
    					if(LOG.isLoggable(Level.WARNING)) {
    		            	LOG.warning("The spell checker '" + checker.getInfo() + " could not be initialized (" + e.getMessage() + ")");
    		            }
    				}
    			}
    			
    			
        	} else if ("USER_DEFINED_WORD".equals(rawName)) {
        		curCharValue.setLength(0);
        	}
        }
        
        @Override
        public void endElement(String nameSpaceURI, String name, String rawName) {
        	if ("USER_DEFINED_WORD".equals(rawName)) {
        		String word = curCharValue.toString();
        		if(currentChecker != null) {
        			currentChecker.addUserDefinedWord(word);
        		}
        		curCharValue.setLength(0);
        	} else if ("SPELL_CHECKER".equals(rawName)) {
        		currentChecker = null;
        	}
        }
        
        @Override
        public void characters(char ch[], int start, int length) throws SAXException {
        	curCharValue.append(ch, start, length);
        }
	}
}
