package mpi.eudico.client.annotator.prefs;

/**
 * A simple class to hold a preferences object.
 * A preference can have a key and can have a value. 
 * Known value types are: Boolean, Integer, Long, Float, Double, String, Map,
 * List and the ObjectPref type.
 * 
 * @author Han Sloetjes, MPI
 */
public class Pref {
	private String key;
	private Object value;
	
	/**
	 * No-arg constructor.
	 */
	public Pref() {
	}

	/**
	 * Creates a Pref instance from the specified key and value objects.
	 * 
	 * @param key the key
	 * @param value the value
	 */
	public Pref(String key, Object value) {
		this.key = key;
		this.value = value;
	}

	/**
	 * Returns the preference key.
	 * 
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Sets the key for this preference.
	 * 
	 * @param key the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * Returns the value of this preference.
	 * 
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * Sets the value of this preference.
	 * 
	 * @param value the value to set
	 */
	public void setValue(Object value) {
		this.value = value;
	}
	
}
