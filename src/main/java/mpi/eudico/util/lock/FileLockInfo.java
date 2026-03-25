package mpi.eudico.util.lock;

import java.io.File;
import java.nio.channels.FileLock;

/**
 * Utility class to store information on a lock file for a file
 * at a location specified as a string.
 * Supports both (global, system wide) Java FileLocks and application specific
 * lock files (only the latter is used at the moment).  
 */
public class FileLockInfo {
	private String   fileString;
	private FileLock globalLock;
	private File     appLock;
	private boolean globalLocked = false;
	private boolean appLocked = false;
	
	/**
	 * Constructor.
	 * Note: could throw an exception if the file is identified by e.g. the
	 * http or https protocol.  
	 * 
	 * @param fileString the location of the file as a string
	 */
	public FileLockInfo(String fileString) {
		super();
		this.fileString = fileString;
	}
	
	/**
	 * Returns the path to the file to lock.
	 * 
	 * @return the location of the file to lock
	 */
	public String getFileString() {
		return fileString;
	}

	/**
	 * Returns the application specific lock file.
	 * 
	 * @return the application specific lock file 
	 */
	public File getAppLock() {
		return appLock;
	}

	/**
	 * Sets the application specific lock file.
	 * 
	 * @param appLock the acquired lock file 
	 */
	public void setAppLock(File appLock) {
		this.appLock = appLock;
		appLocked = (appLock != null);
	}

	/**
	 * Returns the global file lock.
	 * 
	 * @return the native or system wide file lock
	 */
	public FileLock getGlobalLock() {
		return globalLock;
	}

	/**
	 * Sets the global file lock.
	 * 
	 * @param globalLock the acquired FileLock
	 */
	public void setGlobalLock(FileLock globalLock) {
		this.globalLock = globalLock;
		globalLocked = (globalLock != null);
	}

	/**
	 * Returns whether a global lock has been acquired.
	 *  
	 * @return whether the source file is globally locked (on behalf of this JVM)
	 */
	public boolean isGlobalLocked() {
		return globalLocked;
	}

	/**
	 * Sets whether a global lock has been acquired.
	 * 
	 * @param globalLocked the new value for the global lock flag
	 */
	public void setGlobalLocked(boolean globalLocked) {
		this.globalLocked = globalLocked;
	}

	/**
	 * Returns whether this application has the application specific lock.
	 * 
	 * @return whether this application holds the application specific lock
	 * file
	 */
	public boolean isAppLocked() {
		return appLocked;
	}

	/**
	 * Sets whether this application has the lock on this file.
	 * 
	 * @param appLocked the new value for the application lock flag
	 */
	public void setAppLocked(boolean appLocked) {
		this.appLocked = appLocked;
	}
	
	
}