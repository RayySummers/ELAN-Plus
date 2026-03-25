package mpi.eudico.client.annotator.spellcheck;

import java.util.List;
import java.util.Set;

import mpi.eudico.client.annotator.spellcheck.SpellCheckerFactory.SpellCheckerType;
import mpi.eudico.util.Pair;

/** 
 * Specifies a (wrapper for a) spell checker implementation. 
 * 
 * @author michahulsbosch
 *
 */
public interface SpellChecker {
	/**
	 * Attempts to initialize the spell checker based on the information provided.
	 * 
	 * @throws SpellCheckerInitializationException if initialization fails for
	 * whatever reason
	 */
	public void initializeSpellChecker() throws SpellCheckerInitializationException;
	
	/**
	 * Sets the type of the spell checker, one of the {@code SpellCheckerType}
	 * constants.
	 * 
	 * @param type the type of the checker
	 */
	public void setType(SpellCheckerType type);
	
	/**
	 * Returns the type of this checker.
	 * 
	 * @return the type of checker
	 */
	public SpellCheckerType getType();
	
	/**
	 * Sets the description of the checker.
	 * 
	 * @param description the description
	 */
	public void setDescription(String description);
	
	/**
	 * Returns the description of this spell checker.
	 * 
	 * @return the description
	 */
	public String getDescription();
	
	/**
	 * Returns the checker's information string.
	 * 
	 * @return an information string
	 */
	public String getInfo();
	
	/**
	 * Returns a preferences string.
	 * 
	 * @return a preferences string
	 */
	public String getPreferencesString();
	
	/**
	 * Returns a list of suggestions produced by the spell checker for the
	 * input text.
	 * 
	 * @param text the input text, a word or sentence
	 * 
	 * @return a list of pair objects, each pair holds an input token and a
	 * list of suggestions
	 */
	public List<Pair<String, List<String>>> getSuggestions(String text);
	
	/**
	 * Returns whether the input text is correctly spelled.
	 * 
	 * @param text the input text
	 * @return {@code true} of the text is spelled correctly, {@code false} otherwise
	 */
	public Boolean isCorrect(String text);
	
	/**
	 * Adds a word provided by the user which is not (yet) in the dictionary to
	 * the dictionary or to a local cache.
	 * 
	 * @param word the word to add
	 */
	public void addUserDefinedWord(String word);
	
	/**
	 * Returns all words provided by the user so far.
	 *  
	 * @return a set of new, user defined words
	 */
	public Set<String> getUserDefinedWords();
}
