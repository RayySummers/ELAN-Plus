package mpi.eudico.client.annotator.spellcheck;

import java.util.ArrayList;
import java.util.HashMap;

import mpi.eudico.util.Pair;

/**
 * Creates spell checkers from a list of data.
 *  
 * @author micha
 */
public class SpellCheckerFactory {
	/**
	 * List of all currently known spell checker types.
	 * Each entry should correspond with a class implementing the SpellChecker interface. 
	 * @author micha
	 *
	 */
	enum SpellCheckerType {
		/** the Hunspell checker type */
		HUNSPELL,
		/** the Gecco checker type */
		GECCO;
		
		@Override
		public String toString() {
			return this.name().substring(0, 1) + this.name().substring(1).toLowerCase();
		}
	}
	
	/**
	 * Private constructor.	
	 */
	private SpellCheckerFactory() {
		super();
	}

	/**
	 * Asks the corresponding class for the appropriate data fields and returns them.
	 * 
	 * @param type the spell checker type
	 * @return a list of key-value pairs
	 */
	public static ArrayList<Pair<String, String>> getDataFields(SpellCheckerType type) {
		switch(type) {
		case HUNSPELL: return HunspellChecker.getDataFields();
		case GECCO: return GeccoClient.getDataFields();
		}
		return null;
	}
	
	/**
	 * Creates a spell checker for the type, using the data in {@code args}.
	 * 
	 * @param type the spell checker type to create
	 * @param args the parameters required by that spell checker
	 * 
	 * @return a new spell checker instance or {@code null}
	 */
	public static SpellChecker create(SpellCheckerType type, HashMap<String, String> args) {
		switch(type) {
		case HUNSPELL: return HunspellChecker.create(args);
		case GECCO: return GeccoClient.create(args);
		}
		return null;
	}
	
	/**
	 * Returns the names of known spell checkers.
	 * 
	 * @return an array of names of known spell checkers
	 */
	public static String[] getTypes() {
		String[] types = new String[SpellCheckerType.values().length];
		int i = 0;
		for(SpellCheckerType type : SpellCheckerType.values()) {
			types[i] = type.name();
			i++;
		}
		return types;
	}
}
