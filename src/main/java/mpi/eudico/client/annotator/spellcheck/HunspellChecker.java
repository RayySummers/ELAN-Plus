package mpi.eudico.client.annotator.spellcheck;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mpi.eudico.client.annotator.spellcheck.SpellCheckerFactory.SpellCheckerType;
import mpi.eudico.util.Pair;

import dumonts.hunspell.Hunspell;

/**
 * A wrapper for the Hunspell BridJ implementation from https://github.com/thomas-joiner/HunspellBridJ.
 * It needs a locale and a local file path to the .dic and .aff files to create a dictionary.
 * 
 * @author michahulsbosch
 *
 */
public class HunspellChecker implements SpellChecker {
	private String description = "A spellchecker that uses localy installed Hunspell dictionaries. See also http://hunspell.github.io/";
	
	private SpellCheckerType type = SpellCheckerType.HUNSPELL;
	private String filePath;
	
	private Hunspell dict;
	
	private Set<String> newWords = new HashSet<String>();
	
	/**
	 * Creates a spell checker for a specific dictionary.
	 * 
	 * @param filePath path to a {@code .dic} file
	 */
	public HunspellChecker(String filePath) {
		if(filePath.endsWith(".dic")) {
			this.filePath = filePath.substring(0, filePath.length() - 4); 
		} else {
			this.filePath = filePath;
		}
		
	}
	
	/**
	 * Creates a HunspellChecker with the specified parameters, if they are correct.
	 * 
	 * @param args the argument key-value pairs
	 * @return a new {@code HunspellChecker} or {@code null} if the path (to the
	 * dictionary file) is not specified
	 */
	public static HunspellChecker create(HashMap<String, String> args) {
		// Assume args contains the following keys:
		// * path 
		if(args.containsKey("path")) {
			return new HunspellChecker(args.get("path"));
		}
		return null;
	}
	
	/**
	 * Gives the necessary data fields for creating an instance, and their locale reference.
	 * 
	 * @return a list of key-value pairs
	 */
	public static ArrayList<Pair<String, String>> getDataFields() {
		ArrayList<Pair<String, String>> fields = new ArrayList<Pair<String, String>>();
//		fields.add(new Pair<String, String>("language", ""Button.Language""));
//		fields.add(new Pair<String, String>("region", "HunspellChecker.DataField.Region"));
		fields.add(new Pair<String, String>("path", "HunspellChecker.DataField.Path"));
		return fields;
	}
	
	/**
	 * Initializes this spellchecker by creating a Hunspell dictionary.
	 * 
	 * @throws SpellCheckerInitializationException if the checker can not be 
	 * initialized
	 */
	@Override
	public void initializeSpellChecker() throws SpellCheckerInitializationException {
		//String path = filePath + locale.toString();

		try {
			boolean dicError = hasFormatError(filePath + ".dic");
			if (dicError) {
				throw new SpellCheckerInitializationException(String.format(
						"The Hunspell dictionary file %s seems to be in the wrong format. Maybe it is saved as HTML?", 
						filePath + ".dic"));
			}
			boolean affError = hasFormatError(filePath + ".aff");
			if (affError) {
				throw new SpellCheckerInitializationException(String.format(
						"The Hunspell affix file seems to be in the wrong format. Maybe it is saved as HTML?",
						filePath + ".aff"));
			}
			
			dict = new Hunspell(Paths.get(filePath + ".dic"), Paths.get(filePath + ".aff"));
		} catch (UnsatisfiedLinkError e) {
			throw new SpellCheckerInitializationException("No Hunspell dictionary could be opened from " + filePath + " - " + e.getMessage(), e);
		} catch (UnsupportedOperationException e) {
			throw new SpellCheckerInitializationException("No Hunspell dictionary could be opened from " + filePath + " - " + e.getMessage(), e);
		}
	}
	
	@Override
	public void setType(SpellCheckerType type) {
		this.type = type;
	}

	@Override
	public SpellCheckerType getType() {
		return type;
	}

	@Override
	public String getInfo() {
		return filePath;
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
		return type + "," + filePath;
	}
	
	@Override
	public String toString() {
		return "Hunspell: " + getInfo();
	}

	@Override
	public List<Pair<String, List<String>>> getSuggestions(String text) {
		List<Pair<String, List<String>>> suggestions = new ArrayList<Pair<String, List<String>>>();
		
		// Simple split on one or more spaces
		String[] words = text.split("\\s+");
		
		for(int i = 0; i < words.length; i++) {
			String word = words[i];
			if (word.matches(".*\\p{L}.*")) {
				String[] wordSuggestions = new String[] {};
				if (!isCorrect(word)) {
					wordSuggestions = dict.suggest(word);
				}
				suggestions.add(new Pair<String, List<String>>(word, Arrays.asList(wordSuggestions)));
			}
		}
		
		return suggestions;
	}

	@Override
	public Boolean isCorrect(String text) {
		// Simple split on one or more spaces
		String[] words = text.split("\\s+");
		
		for(int i = 0; i < words.length; i++) {
			String word = words[i];
			if(!dict.spell(word)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void addUserDefinedWord(String word) {
		newWords.add(word);
	}

	@Override
	public Set<String> getUserDefinedWords() {
		return newWords;
	}
	
	/**
	 * Reads one line from a dictionary or affix file and performs a basic check:
	 * if the line is empty or starts with {@code "<!" ("<!DOCTYPE html>")} it
	 * is assumed the file hasn't been saved correctly.
	 * 
	 * @param filePath the path to the .dic or .aff file
	 * @return true if the file can be read and an apparent error is detected, 
	 * false otherwise 
	 */
	private boolean hasFormatError(String filePath) {
		BufferedReader bufRead = null;
		try {
			FileReader fr = new FileReader(filePath);
			bufRead = new BufferedReader(fr);
			String fLine = bufRead.readLine();
//			if (fLine == null || fLine.isEmpty()) {
//				return true;
//			} else 
			if (fLine != null && fLine.startsWith("<!")) {
				// probably saved as html
				return true;
			}
		} catch (Throwable t) {
			// catch anything
		} finally {
			if (bufRead != null) {
				try {
					bufRead.close();
				} catch (Throwable tt) {}
			}
		}
		
		return false;
	}

}
