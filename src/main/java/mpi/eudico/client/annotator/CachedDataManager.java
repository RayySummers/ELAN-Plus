package mpi.eudico.client.annotator;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import nl.mpi.util.FileUtility;

import static mpi.eudico.client.annotator.util.ClientLogger.LOG;

/**
 * Class to manage cached data.
 * 
 * @author michahulsbosch
 */
public class CachedDataManager implements PreferencesListener {

	private static final CachedDataManager cachedDataManager = new CachedDataManager();
	private String cacheLocation;
	
	@SuppressWarnings("serial")
	private static final Set<String> subdirectories = new HashSet<String>() {{
		add("lexica");
		//add("CVCACHE");
	}};
	
	private ArrayList<CacheSettingsChangeListener> cacheSettingsChangeListeners = new ArrayList<CacheSettingsChangeListener>(); 
	
	/**
	 * Private constructor.
	 */
	private CachedDataManager() {
		Preferences.addPreferencesListener(null, this);
		cacheLocation = getCacheLocationFromPreferences();
	}
	
	private String getCacheLocationFromPreferences() {
		String cacheLocationFromPreferences = Preferences.getString("CacheLocation", null);
		if(cacheLocationFromPreferences == null || cacheLocationFromPreferences.equals("") || cacheLocationFromPreferences.equals("-")) {
			cacheLocationFromPreferences = Constants.ELAN_DATA_DIR; 
		} else if(cacheLocationFromPreferences.startsWith("file:")) {
			cacheLocationFromPreferences = cacheLocationFromPreferences.substring(5);
		}
		return cacheLocationFromPreferences;
	}
	
	/**
	 * Returns the single instance.
	 * 
	 * @return the single instance of this manager
	 */
	public static CachedDataManager getInstance() {
		return cachedDataManager;
	}
	
	/**
	 * Returns the location of the cache.
	 * 
	 * @return the cache location
	 */
	public String getCacheLocation() {
		return cacheLocation;
	}

	@Override
	public void preferencesChanged() {
		String newCacheLocation = getCacheLocationFromPreferences();
		if (!cacheLocation.equals(newCacheLocation)) {
			moveCache(cacheLocation, newCacheLocation);
			cacheLocation = newCacheLocation;
			for(CacheSettingsChangeListener listener : cacheSettingsChangeListeners) {
				listener.cacheSettingsChanged();
			}
		}
	}
	
	private static void moveCache(String oldDirectory, String newDirectory) {
		LOG.info("Dirs: " + oldDirectory + " " + newDirectory);
		try {
			for(String subdirectory : subdirectories) {
				File sourcePath = new File(oldDirectory + File.separator + subdirectory);
				File destinationPath = new File(newDirectory + File.separator + subdirectory);
				FileUtility.moveDirectory(sourcePath.toPath(), destinationPath.toPath(), 
						(CopyOption[])null);
			}
		} catch (IOException e) {
			if(LOG.isLoggable(Level.WARNING)) {
            	LOG.warning("The cache directory could not be moved (" + e.getMessage() + ")");
            }
		}
	}
	
	/**
	 * Adds a listener for changes in cache settings.
	 * 
	 * @param listener the listener for changes in cache settings
	 */
	public void addCacheSettingsListener(CacheSettingsChangeListener listener) {
		cacheSettingsChangeListeners.add(listener);
	}
	
	/**
	 * Removes a listener for cache change events.
	 * 
	 * @param listener the listener to remove
	 */
	public void removeCacheSettingsListener(CacheSettingsChangeListener listener) {
		cacheSettingsChangeListeners.remove(listener);
	}
	
	/**
	 * Checks whether a folder contains the the cache folder.
	 * 
	 * @param directory the folder to check
	 * @return {@code true} if the specified folder is the parent folder of the
	 * cache folder
	 */	
	public static Boolean containsCacheSubdirs(File directory) {
		if(directory.isDirectory()) {
			String[] files = directory.list();
			Set<String> dirListing = new HashSet<String>(Arrays.asList(files));
			dirListing.retainAll(subdirectories);
			if(dirListing.size() > 0) {
				return true;
			}
		}
		return false;
	}
}
