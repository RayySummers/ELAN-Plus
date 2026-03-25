package mpi.eudico.client.annotator.spellcheck;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.apache.http.NameValuePair;
import org.apache.http.client.protocol.HttpClientContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import mpi.eudico.client.annotator.spellcheck.SpellCheckerFactory.SpellCheckerType;
import static mpi.eudico.client.annotator.util.ClientLogger.LOG;
import mpi.eudico.util.Pair;

/**
 * Uses a Gecco Spellchecker web service (https://github.com/proycon/gecco)
 * through CLAM (http://proycon.github.io/clam/).
 * <p>
 * HS 03-2022: The Gecco based service for English, {@code Fowlt}, doesn't seem
 * to exist anymore. The service for Dutch, {@code Valkuil}, doesn't require
 * (anymore?) a user login. The url changed in the meantime. There's no need
 * for configuration options in a properties file anymore, so it seems. 
 * 
 * @author michahulsbosch
 * @author Han Sloetjes
 */
public class GeccoClient extends ClamClient implements SpellChecker {
	/** the base URL of the web service */
	public static final String baseURL = "https://webservices.cls.ru.nl";
	/** the path of the Valkuil service */
	public static final String valkPath = "/valkuil";
	/** the full path of the Valkuil service */
	public static final String fullURL = baseURL + valkPath; 
	SpellCheckerType type = SpellCheckerType.GECCO;
	
	private String description = "A spellchecker that uses a Gecco Spellchecker webservice through CLAM. See also https://github.com/proycon/gecco";
	
	/** Contains cached suggestions that were analyzed before */
	HashMap<String, List<Pair<String, List<String>>>> cachedSuggestions = new HashMap<String, List<Pair<String,List<String>>>>();
	
	/**
	 * Creates a new GeccoClient instance.
	 * 
	 * @param host the service host
	 * @param protocol the service protocol
	 * @param path the path component
	 * @param username the user name
	 * @param password the password of the user
	 */
	public GeccoClient(String host, String protocol, String path, String username, String password) {
		super(host, protocol, path, username, password);
	}
	
	/**
	 * Creates a new GeccoClient instance without user name and password.
	 * 
	 * @param host the service host
	 * @param protocol the service protocol
	 * @param path the path component
	 */
	public GeccoClient(String host, String protocol, String path) {
		super(host, protocol, path, null, null);
	}
	
	/**
	 * Creates a GeccoClient with the arguments, if they are correct.
	 * 
	 * @param args the arguments
	 * @return a new client
	 */
	public static GeccoClient create(HashMap<String, String> args) {
		if(args.containsKey("url")
				/*&& args.containsKey("username")
				&& args.containsKey("password")*/) {
			try {
				URL url = new URL(args.get("url"));
				return new GeccoClient(url.getHost(), url.getProtocol(), url.getPath(), args.get("username"), args.get("password"));
			} catch (MalformedURLException e) {
				if(LOG.isLoggable(Level.WARNING)) {
	            	LOG.warning("The url " + args.get("url") + " is malformed (" + e.getMessage() + ")");
	            }
			}
		}
		return null;
	}
	
	@Override
	public void initializeSpellChecker() throws SpellCheckerInitializationException {
		super.initialize();
	}
	
	
	@Override
	protected void creatOrUpdateContext() {
		if (context == null) {
			context = HttpClientContext.create();
		}
	}

	/**
	 * Gives the necessary data fields for creating an instance, and their locale reference.
	 * 
	 * @return a list of key-value pairs
	 */
	public static ArrayList<Pair<String, String>> getDataFields() {
		ArrayList<Pair<String, String>> fields = new ArrayList<Pair<String, String>>();
		fields.add(new Pair<String, String>("url", "GeccoClient.DataField.Url"));
		//fields.add(new Pair<String, String>("username", "GeccoClient.DataField.Username"));
		//fields.add(new Pair<String, String>("password", "GeccoClient.DataField.Password"));
		return fields;
	}
		
	/**
	 * Processes a single sentence without creating a CLAM project first.
	 * Note that only tokens that get suggestions end up in the returned 
	 * data structure. This is due to the workings of this part of Gecco.
	 * <p> 
	 * In the current implementation the input is actually a word, not a 
	 * sentence. Based on some shallow testing it seems the sentence context
	 * doesn't matter too much for the produced suggestions.
	 * 
	 * @param sentence the sentence to check
	 * @return a list of pairs, with the tokens as keys and lists of suggestions
	 * as values
	 */
	public List<Pair<String, List<String>>> processSentence(String sentence) {
		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine(sentence);	
		}
		sentence = sentence.replaceAll("^\\s+", ""); // strip spaces from the front
		sentence = sentence.replaceAll("\\s+$", ""); // strip spaces from the ends
		
		// If the sentence is empty, don't bother calling Gecco
		if(sentence.equals("")) {
			return new ArrayList<Pair<String, List<String>>>();
		}

		String[] words = sentence.split("\\s+");
		
		List<NameValuePair> data = new ArrayList<NameValuePair>();
		data.add(makeNameValuePair("sentence", sentence));
		String body = request(Method.GET, basePath + "/actions/process_sentence", data);
		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine(body);	
		}
		
		// Initialize the complete suggestions list with empty suggestions
		List<Pair<String, List<String>>> allSuggestionsList = new ArrayList<Pair<String, List<String>>>();
			
		try {
			JSONArray json = new JSONArray(body);
			for(int w = 0; w < words.length; w++) {
				allSuggestionsList.add(new Pair<String, List<String>>(words[w], new ArrayList<String>()));
			}
			for (int i = 0; i < json.length(); i++) {
				JSONObject annotation = (JSONObject) json.get(i);
				String token = (String) annotation.get("text");
				Integer index = (Integer) annotation.get("index");
				JSONArray suggestions = (JSONArray) annotation.get("suggestions");
				
				ArrayList<String> suggestionsList = new ArrayList<String>();
				for (int j = 0; j < suggestions.length(); j++) {
					JSONObject suggestion = (JSONObject) suggestions.get(j);
					suggestionsList.add((String) suggestion.get("suggestion"));
				} 
				//allSuggestionsList.add(new Pair<String, List<String>>(token,suggestionsList));
				allSuggestionsList.set(index, new Pair<String, List<String>>(token,suggestionsList));
			}
		} catch(JSONException je) {
			if (LOG.isLoggable(Level.SEVERE)) {
				LOG.severe("An error occurred while processing '" + sentence 
						+ "' by " + basePath + ". " + je.getMessage());
			}
		}
		
		return allSuggestionsList;
	}
	
	/**
	 * The type of this checker can not be set.
	 */
	@Override
	public void setType(SpellCheckerType type) {
		// the type can not be set	
	}

	@Override
	public SpellCheckerType getType() {
		return type;
	}

	@Override
	public String getInfo() {
		return getHost() + getPath();
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String getDescription() {
		return description;
	}
	
	@Override
	public String getPreferencesString() {
		//return type + "," + getAuthenticationProtocol() + "," + getHost() + "," + getPath() + "," + getUsername() + "," + getPassword();
		return type + "," + getHost() + "," + getPath();
	}
	
	@Override
	public String toString() {
		return "Gecco webservice: " + getInfo();
	}
	
	/** 
	 * Gets the suggestions for a text from either the cached suggestions
	 * or the Gecco webservice. In latter case the suggestions are put
	 * in the cached suggestions.
	 */
	@Override
	public List<Pair<String, List<String>>> getSuggestions(String text) {
		// Check to see if the text has already been analyzed.
		if(cachedSuggestions.containsKey(text)) {
			return cachedSuggestions.get(text);
		}
		
		// If the text has not been analyzed, do it now and 
		// put it in the cached suggestions.
		List<Pair<String, List<String>>> suggestions = processSentence(text);
		cachedSuggestions.put(text, suggestions);
		return suggestions;
	}

	@Override
	public Boolean isCorrect(String text) {
		if(cachedSuggestions.containsKey(text)) {
			return !SpellCheckerUtil.hasSuggestions(cachedSuggestions.get(text));
		} else {
			return !SpellCheckerUtil.hasSuggestions(getSuggestions(text));
		}
	}

	/**
	 * Used for testing from the command line
	 * @param args the application arguments
	 */
	public static void main(String[] args) {
		if(args.length < 7) {
			System.err.println("Specify the following:");
			System.err.println("  protocol (http or https)");
			System.err.println("  hostname"); // e.g. webservices-lst.science.ru.nl
			System.err.println("  path"); // e.g. /valkuil
			System.err.println("  username");
			System.err.println("  password");
			System.err.println("  command (createProject, deleteProject, ...)");
			System.err.println("  data (projectName ...)");
			System.exit(0);
		}
		String protocol = args[0];
		String host = args[1];
		String path = args[2];
		String username = args[3];
		String password = args[4];
		String command = args[5];
		String[] data = Arrays.copyOfRange(args, 6, args.length);
		
		GeccoClient client = new GeccoClient(host, protocol, path, username, password);
		
		if(command.equals("createProject")) {
			client.createProject(data[0]);
		} else if(command.equals("deleteProject")) {
			client.deleteProject(data[0]);
		} else if(command.equals("uploadText")) {
			client.uploadText(data[0], data[1], data[2]); 
		} else if(command.equals("startProject")) {
			client.startProject(data[0], data[1]);
		} else if(command.equals("pollProject")) {
			client.pollProject(data[0]);
		} else if(command.equals("retrieveOutput")) {
			client.retrieveOutput(data[0], data[1]);
		} else if(command.equals("processSentence")) {
			for(int i = 0; i < data.length; i++) {
				List<Pair<String, List<String>>> suggestions = client.getSuggestions(data[i]);
			}
		}
	}

	/**
	 * This is not supported. It could be considered to add words to a local
	 * cache or storage. 
	 */
	@Override
	public void addUserDefinedWord(String word) {
		// not supported
		
	}

	/**
	 * This is not supported at the moment. 
	 * 
	 * @return a new, empty {@code HashSet}
	 */
	@Override
	public Set<String> getUserDefinedWords() {
		return new HashSet<String>(); // stub
	}
}