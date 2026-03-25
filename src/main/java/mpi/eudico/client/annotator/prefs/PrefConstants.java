/**
 * 
 */
package mpi.eudico.client.annotator.prefs;

/**
 * Interface defining some constants for reading and writing of preferences 
 * XML files.
 * 
 * @author Han SLoetjes
 */
public interface PrefConstants {
	// elements
	/** the pref element */
	public static final String PREF = "pref";
	/** the prefGroup element */
	public static final String PREF_GROUP = "prefGroup";
	/** the prefList element */
	public static final String PREF_LIST = "prefList";
	/** the Boolean element */
	public static final String BOOLEAN = "Boolean";
	/** the Int element */
	public static final String INT = "Int";
	/** the Long element */
	public static final String LONG = "Long";
	/** the Float element */
	public static final String FLOAT = "Float";
	/** the Double element */
	public static final String DOUBLE = "Double";
	/** the String element */
	public static final String STRING = "String";
	/** the Object element */
	public static final String OBJECT = "Object";
	// attributes
	/** the key attribute */
	public static final String KEY_ATTR = "key";
	/** the class attribute */
	public static final String CLASS_ATTR = "class";
	/** the version attribute */
	public static final String VERS_ATTR = "version";
}
