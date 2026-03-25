package mpi.eudico.client.annotator;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import static mpi.eudico.client.annotator.util.ClientLogger.LOG;

/**
 * The class that handles most {@code Locale} related actions in an
 * ELAN instance.
 */
public class ElanLocale {
	//private static Vector listeners = new Vector();
	private static Locale locale;
	private static ResourceBundle resourcebundle;

	private static Map<Object, List<ElanLocaleListener>> listenerGroups = new HashMap<Object, List<ElanLocaleListener>>();

	/** constant for a custom language */
	public static final Locale CUSTOM = Locale.of("cu", "", "");

	/** constant for Dutch */
	public static final Locale DUTCH = Locale.of("nl", "NL");

	/** constant for English, the default language */
	public static final Locale ENGLISH = Locale.of("", "");

	/** constant for Catalan */
	public static final Locale CATALAN = Locale.of("ca");

	/** constant for Spanish */
	public static final Locale SPANISH = Locale.of("es", "ES");

	/** constant for Swedish */
	public static final Locale SWEDISH = Locale.of("sv", "SE");

	/** constant for German */
	public static final Locale GERMAN = Locale.of("de", "DE");

	/** constant for Portuguese */
	public static final Locale PORTUGUESE = Locale.of("pt");

	/** constant for Brazilian Portuguese */
	public static final Locale BRAZILIAN_PORTUGUESE = Locale.of("pt", "BR");

	/** constant for French */
	public static final Locale FRENCH = Locale.of("fr");

	/** constant for Japanese */
	public static final Locale JAPANESE = Locale.of("ja", "JP");

	/** constant for Chinese simplified */
	public static final Locale CHINESE_SIMP = Locale.of("zh", "CN");

	/** constant for Russian */
	public static final Locale RUSSIAN = Locale.of("ru", "RU");

	/** constant for Korean */
	public static final Locale KOREAN = Locale.of("ko");
	
	/** constant for Indonesian */
	public static final Locale INDONESIAN = Locale.of("id");

	/**
	 * Constructor
	 */
	ElanLocale() {
		locale = Locale.getDefault();
		resourcebundle =
			ResourceBundle.getBundle(
				"mpi.eudico.client.annotator.resources.ElanLanguage",
				locale);
	}

	/**
	 * Gets the current locale
	 *
	 * @return The current locale
	 */
	public static Locale getLocale() {
		return locale;
	}

	/**
	 * Sets the current locale
	 *
	 * @param locale_in The new locale
	 */
	public static void setLocale(Locale locale_in) {
		if (locale != null && locale.equals(locale_in)) {
			return;
		}
		//if (locale.getCountry().equals(locale_in.getCountry())) {
		//	return;
		//}

		locale = locale_in;
		if (locale.equals(CUSTOM)) {
			// try to read from a properties file from the user's ELAN directory
			try {
				File custFile = new File(Constants.ELAN_DATA_DIR + Constants.FILESEPARATOR + "ElanLanguage.properties");
				if (custFile.exists()) {
					FileInputStream stream = new FileInputStream(custFile);
					resourcebundle = new PropertyResourceBundle(stream);
					stream.close();
				} else {
					// log error
					LOG.warning("No custom localisation file found.");
					resourcebundle =
						ResourceBundle.getBundle(
							"mpi.eudico.client.annotator.resources.ElanLanguage");
				}
			} catch (Exception ex) {
				// log error
				LOG.warning("Could not load custom localisation file: " + ex.getMessage());
				resourcebundle =
					ResourceBundle.getBundle(
						"mpi.eudico.client.annotator.resources.ElanLanguage");
			}
		} else {
		    resourcebundle =
			    ResourceBundle.getBundle(
				    "mpi.eudico.client.annotator.resources.ElanLanguage",
				    locale);
		}
		notifyListeners();

		try {
			if (!locale.equals(CUSTOM)) {
				mpi.search.SearchLocale.setLocale(locale);
			} else {
				File custFile = new File(Constants.ELAN_DATA_DIR + Constants.FILESEPARATOR + "SearchLanguage.properties");
				if (custFile.exists()) {
					FileInputStream stream = new FileInputStream(custFile);
					ResourceBundle resBundle = new PropertyResourceBundle(stream);
					stream.close();
					mpi.search.SearchLocale.setResourceBundle(resBundle);
				} else {
					// log error
					LOG.warning("No custom search localisation file found.");
					mpi.search.SearchLocale.setLocale(locale);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the string in the right language from the right resource file
	 *
	 * @param str The string which has to be mapped to the right language
	 *
	 * @return The string in the right language
	 */
	public static String  getString(String str) {
		if (locale == null) {
			locale = Locale.getDefault();
			resourcebundle =
				ResourceBundle.getBundle(
					"mpi.eudico.client.annotator.resources.ElanLanguage",
					locale);
		}

		try {
			return resourcebundle.getString(str);
		}
		catch (Exception ex) {
			return "";
		}
	}

	/**
	 * Adds an ELAN Locale listener.
	 *
	 * @param key determines to which listener group the listener should be
	 * added, usually the key will be the transcription/document
	 * @param listener A new ELAN Locale listener
	 */
	public static void addElanLocaleListener(Object key,
			ElanLocaleListener listener) {
		if (listenerGroups.containsKey(key)) {
			listenerGroups.get(key).add(listener);

			listener.updateLocale();
		} else {
			List<ElanLocaleListener> list = new ArrayList<ElanLocaleListener>();
			list.add(listener);

			listenerGroups.put(key, list);
			listener.updateLocale();
		}
		/*
		if (!listeners.contains(listener)) {
			listeners.add(listener);

			// make sure the listener is up to date
			listener.updateLocale();
		}
		*/
	}

	/**
	 * Removes an ELAN Locale listener.
	 *
	 * @param listener The listener which has to be removed
	 */
	public static void removeElanLocaleListener(ElanLocaleListener listener) {
		//listeners.remove(listener);
		Iterator<Object> groupIt = listenerGroups.keySet().iterator();
		while (groupIt.hasNext()) {
			List<ElanLocaleListener> listeners = listenerGroups.get(groupIt.next());
			if (listeners.remove(listener)) {
				break;
			}
		}
	}

	/**
	 * Removes an ELAN Locale listener group.
	 *
	 * @param key The key of the group which has to be removed
	 */
	public static void removeElanLocaleListener(Object key) {
		//listeners.remove(listener);
		listenerGroups.remove(key);
	}

	/**
	 * Notifies all listeners if the locale has been changed.
	 */
	private static void notifyListeners() {
		//for (int i = 0; i < listeners.size(); i++) {
		//	((ElanLocaleListener) listeners.elementAt(i)).updateLocale();
		//}
		Iterator<Object> groupIt = listenerGroups.keySet().iterator();
		while (groupIt.hasNext()) {
			List<ElanLocaleListener> listeners = listenerGroups.get(groupIt.next());
			for (int i = 0; i < listeners.size(); i++) {
				listeners.get(i).updateLocale();
			}
		}
	}

	/**
	 * Returns the resource bundle for use in (generic) classes that do not
	 * directly access ElanLocale.
	 *
	 * @return the resource bundle
	 */
	public static ResourceBundle getResourceBundle() {
		return resourcebundle;
	}

}
