package nl.mpi.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A utility class that performs some ELAN specific File operations.
 *
 * @author Han Sloetjes
 */
public class FileUtility {
	private static final Logger LOG = Logger.getLogger(FileUtility.class.getPackageName());
	
	/**
	 * Private constructor.
	 */
    private FileUtility() {
		super();
	}

	/**
     * Converts a path to a file URL string unless it concerns a remote path. 
     * Takes care of Samba related problems file:///path works for all files
     * except for samba file systems, there we need file://machine/path, 
     * i.e. 2 slashes instead of 3. Does not support relative paths.
     *
     * @param path the path to convert
     *
     * @return a file or remote URL string
     */
    public static String pathToURLString(String path) {
        if (path == null) {
            return null;
        }

        // replace all back slashes by forward slashes
        path = path.replace('\\', '/');
        
        if (FileUtility.isRemoteFile(path)) {
            return path;
        }

		if (path.startsWith("file:")) {
			path = path.substring(5);
		}
		
        // remove leading slashes and count them
        int n = 0;

        while (!path.isEmpty() && path.charAt(0) == '/') {
            path = path.substring(1);
            n++;
        }

        // add the file:// or file:/// prefix
        if (n == 2) {
            return "file://" + path;
        } else {
            return "file:///" + path;
        }
    }
    
    /**
     * Extracts the path part of a URL string, which will in most cases be a
     * URL with a file scheme as created by {@link #pathToURLString(String)}. 
     * Fragments etc. are not removed.
     *  
     * @param urlString the URL string to process
     * @return the path part or the input string if an error occurs
     * @see #pathToURLString(String)
     */
    public static String urlStringToPath(final String urlString) {
    	if (urlString == null) {
    		return urlString;
    	}
    	// using URI with file:/// as input fails on some platforms
    	/* try {
    		URI fileURI = new URI(urlString);
    		return fileURI.getPath();
    	} catch (URISyntaxException use){} */
    	String path = urlString;
    	// remove scheme part
    	if (path.startsWith("file:")) {
    		if (path.length() > 5) {
    			path = path.substring(5);
    		}
    		if (path.startsWith("///")) {
    			// remove two slashes, /C:/etc/etc seems to work on Windows 
    			path = path.substring(2);
    		} // in case of two slashes, assume a samba path
    	} 
    	// could use URI or URL for the other cases to remove scheme, authority, fragments?
    	/*
    	else {
    		try {
        		URI fileURI = new URI(path);
        		path = fileURI.getPath();
        	} catch (URISyntaxException use){} 
    	}*/
    	else if (path.startsWith("rtsp://")) {
    		if (path.length() > 7) {
    			path = path.substring(7);
    		}
    	} else if (path.startsWith("http://")) {
    		if (path.length() > 7) {
    			path = path.substring(7);
    		}
    	} else if (path.startsWith("https://")) {
    		if (path.length() > 8) {
    			path = path.substring(8);
    		}
    	} else { // the following test could replace the previous 3?
    		int cssIndex = path.indexOf("://");
    		if (cssIndex > -1 && path.length() > 3) {
    			path = path.substring(cssIndex + 3);
    		}
    	}
    	
    	return path;
    }

    /**
     * Method to compare the file names in two file/url paths without their
     * path and extensions.
     *
     * @param path1 first file path
     * @param path2 seconds file path
     *
     * @return boolean true if the file names without path and extension are
     *         the same
     */
    public static boolean sameNameIgnoreExtension(String path1, String path2) {
        // no name gives false, two nulls are equal but have no name
        if ((path1 == null) || (path2 == null)) {
            return false;
        }

        String name1 = fileNameFromPath(path1);
        int extensionPos = name1.lastIndexOf('.');

        if (extensionPos >= 0) {
            name1 = name1.substring(0, extensionPos);
        }

        String name2 = fileNameFromPath(path2);
        extensionPos = name2.lastIndexOf('.');

        if (extensionPos >= 0) {
            name2 = name2.substring(0, extensionPos);
        }

        return name1.equals(name2);
    }

    /**
     * Returns the file name from a file path.
     *
     * @param path the path
     *
     * @return the filename part of the path
     */
    public static String fileNameFromPath(final String path) {
        if (path == null) {
            return null;
        }

        String name = path.replace('\\', '/');
        int delimiterPos = name.lastIndexOf('/');

        if (delimiterPos >= 0) {
            name = name.substring(delimiterPos + 1);
        }

        return name;
    }

    /**
     * Returns the directory name from a file path.
     *
     * @param path the file path
     *
     * @return the directory
     */
    public static String directoryFromPath(final String path) {
        if (path == null) {
            return null;
        }

        String name = path.replace('\\', '/');
        int delimiterPos = name.lastIndexOf('/');

        if (delimiterPos >= 0) {
            name = name.substring(0, delimiterPos);
        }

        return name;
    }

    /**
     * Tests whether a file exists.
     *
     * @param path the file path to test
     *
     * @return true if the file exists, false otherwise
     */
    public static boolean fileExists(final String path) {
        if (path == null) {
            return false;
        }

        // remove the file: part of the URL, leading slashes are no problem
        int colonPos = path.indexOf(':');
        String fileName = path;

        if (colonPos > -1) {
            fileName = path.substring(colonPos + 1);
        }

        // replace all back slashes by forward slashes
        fileName = fileName.replace('\\', '/');

        File file = new File(fileName);

        if (!file.exists()) {
            return false;
        } else {
            return true;
        }
    }
    
    /**
     * Performs a naive test whether a path represents a remote file, resource. 
     * If the path does not start with "file" but does start with scheme:// it
     * is assumed it represents a remote resource.
     * 
     * @param path the path as a string
     * @return true for any path starting with scheme:// provided the scheme is 
     * not equal to "file" 
     */
    public static boolean isRemoteFile(final String path) {
        if (path == null) {
            return false;
        }
        
        int cssIndex = path.indexOf("://");
        if (cssIndex > 0 && !path.startsWith("file")) {
        	return true;
        } 
        
        return false;
    }    
    
    /**
     * Fail safe, very basic test if the resource identified by the string 
     * parameter exists. This method does not verify if the string denotes a
     * remote file, it tries to create a {@link URL}, to open an {@link HttpURLConnection}
     * and get a response code. This method does not throw any exceptions, all possible  
     * exceptions (e.g. {@link MalformedURLException}, {@link IOException})
     * are caught and will result in return value {@code false}.
     * Therefore the return value {@code false} does not necessarily mean that
     * the file does not exist (it may be inaccessible somehow, a time out may have
     * occurred etc.).
     *  
     * @param urlString the remote file to test, http/https is assumed
     * @return {@code true} if opening a connection succeeded and a HTTP status 
     * code 200 (OK) was received, {@code false} otherwise
     */
    public static boolean remoteFileExists(String urlString) {
    	try {
    		URL url = new URI(urlString).toURL();
    		HttpURLConnection urlConnect = (HttpURLConnection) url.openConnection();
			urlConnect.setConnectTimeout(4000);
			urlConnect.setReadTimeout(1000);
			urlConnect.connect();
			if (urlConnect.getResponseCode() == 200) {
				return true;
			}

    		return false;
    	} catch (Throwable t) {
    		// URISyntaxException, MalformedURLException, IOException, etc.
        	if (LOG.isLoggable(Level.INFO)) {
        		LOG.log(Level.INFO, "Could not check remote file: " + t.getMessage());
        	}
    	}
    	
    	return false;
    }

    //////////////////////////////////////
    // Copied from 'old' FileUtil  class
    //////////////////////////////////////

    /**
     * If file f is a file and has an extension, it is returned. Otherwise,
     * null is returned.
     *
     * @param f a File
     * @return the file extension or null
     * @see {@link #getFileExtension}
     */
    @Deprecated
    public static final String getExtension(final File f) {
        String name = f.getName();

        return getExtension(name);
    }

    public static Optional<String> getFileExtension(File exportFile) {
        String fileName = exportFile.getName();
        int index = fileName.lastIndexOf('.');
        Optional<String> mayBeExtension = Optional.empty();
        if (0 <= index && index < fileName.length() - 1) {
            String extension = fileName.substring(index + 1);
            mayBeExtension = Optional.of(extension.toLowerCase());
        }

        return mayBeExtension;
    }

    /**
     * If file has a name with an extension, it is returned. Otherwise,
     * null is returned.
     *
     * @param name a filename
     *
     * @return the file extension or null
     */
    public static final String getExtension(final String name) {
        int li = name.lastIndexOf(".");

        return (li == -1) ? null : name.substring(li + 1);
    }

    /**
     * If file has a name with an extension, it is returned. Otherwise,
     * the default, as supplied, is returned.
     *
     * @param name a filename
     * @param defaultExtension what to return if there is no extension
     *
     * @return the file extension or null
     */
    public static final String getExtension(final String name, final String defaultExtension) {
        int li = name.lastIndexOf(".");

        return (li == -1) ? defaultExtension : name.substring(li + 1);
    }

    /**
     * If file f is a filename with an extension, the name is returned without the extension.
     * Otherwise, the name is returned unmodified.
     *
     * @param name the name
     *
     * @return the file name without extension
     */
    public static final String dropExtension(final String name) {
        int dot = name.lastIndexOf(".");
        
        if (dot == -1) {
        	return name;
        }
        
        String basename = name.substring(0, dot);
        
        // Check if there may be a / following
        
        int slash = name.lastIndexOf('/');
        
        return (slash < dot) ? basename : name;
    }

    /**
     * Copies a file to a destination directory. 
     * <p>This can better be replaced by using {@code Files} methods.
     *
     * @param sourceFile the source
     * @param destDir the destination directory
     *
     * @throws Exception any exception that can occur during the copy action
     */
    public static void copyToDir(final File sourceFile, final File destDir)
        throws Exception {
        // create the directory if it doesn't already exist.
        if (destDir.mkdirs()) {
        	if (LOG.isLoggable(Level.FINE)) {
        		LOG.log(Level.FINE, "The target directory was created: " + destDir.getAbsolutePath());
        	}
        }

        File destFile = new File(destDir, sourceFile.getName());
        
        try (FileInputStream in = new FileInputStream(sourceFile);
             FileOutputStream out = new FileOutputStream(destFile)) {
            byte[] buffer = new byte[4096];
	        int len;
	
	        while ((len = in.read(buffer)) != -1) {
	            out.write(buffer, 0, len);
	        }
	
	        out.flush();
        }
    }

    /**
     * Copies a source file to a destination file.
     * <p>This can better be replaced by using {@code Files} methods.
     *
     * @param sourceFile the source file
     * @param destFile the destination file
     *
     * @throws Exception any exception that can occur during the copy action
     */
    public static void copyToFile(final File sourceFile, final File destFile)
        throws Exception {
        try (FileInputStream in = new FileInputStream(sourceFile);
             FileOutputStream out = new FileOutputStream(destFile)) {
        	byte[] buffer = new byte[4096];
	        int len;
	
	        while ((len = in.read(buffer)) != -1) {
	            out.write(buffer, 0, len);
	        }
	        
	        out.flush();
        }
    }
 
    //########## methods copied from mpi.bcarchive.repaircheck.Util ###################
    //########## modifications HS 09-2011 ############
    
    /**
     * translates URL to absolute path (removes "file:" prefix from URL)
     * @param filename name of file
     * @return returns filename without url prefixes
     */
    public static String urlToAbsPath(String filename) {
    	if (filename == null) {
    		return filename;
    	}
        if (filename.startsWith("file:")) {
            filename = filename.substring("file:".length());
            // Jan 2020 harmonize with urlStringToPath
    		if (filename.startsWith("///")) {
    			// remove two slashes, /C:/etc/etc seems to work on Windows 
    			filename = filename.substring(2);
    		} // in case of two slashes, assume a samba path
    		
    		// paths like /C:/etc/etc seems to work on Windows most of the times
    		// but not always. After conversion to \C:\etc\etc on Windows this doesn't
    		// always work for other, native, Windows applications.
    		if (filename.matches("^/[a-zA-Z]:/.+")) {
    			filename = filename.substring(1);
    		}
        }
        return filename;
    }
    
    /**
     * getRelativePath gets path of resource relative to a reference file
     * Forward slashes are assumed, loose attempts are made to not calculate a relative path
     * for files that are on different drives (problems differ per platform). 
     * 
     * @param referenceFilename path+filename of the reference file
     * @param resourceFilename path+filename of resource
     * @return relative path or null
     */
    public static String getRelativePath(String referenceFilename, String resourceFilename) {
    	if (referenceFilename == null || resourceFilename == null) {
    		return resourceFilename;
    	}
        if (resourceFilename.startsWith("../") || resourceFilename.startsWith("./")) {
            return resourceFilename;
        }
        String refNoProt = FileUtility.urlToAbsPath(referenceFilename);
        String resourceNoProt = FileUtility.urlToAbsPath(resourceFilename);
        
        int numSlashesRef = 0;
        while (numSlashesRef < refNoProt.length() && refNoProt.charAt(numSlashesRef) == '/') {
        	numSlashesRef++;
        }
        
        int numSlashesRes = 0;
        while (numSlashesRes < resourceNoProt.length() && resourceNoProt.charAt(numSlashesRes) == '/') {
        	numSlashesRes++;
        }
        
        if (numSlashesRef != numSlashesRes) {// probably not on the same drive
        	return null;
        }
        
        String  refFields2[] = refNoProt.split("/");
        String resourceFields2[] = resourceNoProt.split("/");
        // remove the empty elements
        String  refFields[] = new String[refFields2.length - numSlashesRef];
        for (int i = 0; i < refFields.length; i++) {
        	refFields[i] = refFields2[i + numSlashesRef];
        }
        String resourceFields[] = new String[resourceFields2.length - numSlashesRes];
        for (int i = 0; i < resourceFields.length; i++) {
        	resourceFields[i] = resourceFields2[i + numSlashesRes];
        }
        // stop if the first element of both arrays is not the same, assuming they are not on the same drive
        // this is sloppy: if they are the same this does not guarantee that the file are on the same drive 
        if (refFields.length > 0 && resourceFields.length > 0 && !refFields[0].equals(resourceFields[0])) {
        	return null;
        }
        
        int refFieldsLen = refFields.length;
        int resourceFieldsLen = resourceFields.length;
        
        int minFieldsLen = Math.min(refFieldsLen, resourceFieldsLen);
        
        int i = 0;
        while (i < minFieldsLen && refFields[i].equals(resourceFields[i])) {
            i++;
        }
        
        int restIndex = i;
        StringBuilder relPathBuf = new StringBuilder();
        
        if (i == refFieldsLen - 1) {
            relPathBuf.append("./");
        } else if (i == refFieldsLen) {
        	// Exceptional case: both file names are the same! Create "./filename".
            relPathBuf.append("./");
            restIndex--;
        } else {
	        while (i < refFieldsLen - 1) {
	            relPathBuf.append("../");
	            i++;
	        }
        }
        
        i = restIndex;
        while (i < resourceFieldsLen - 1) {
            relPathBuf.append(resourceFields[i] + "/");
            i++;
        }
        
        relPathBuf.append(resourceFields[i]);
        
        return relPathBuf.toString();
    }
    
    /**
     * getAbsolutePath returns the absolute path of a resource of which a relative path is known
     * @param referenceFilename path+filename of reference file
     * @param resourceFilename relative path of resource to reference
     * @return absolute path
     */
    public static String getAbsolutePath(String referenceFilename, String resourceFilename) {
    	if (referenceFilename == null || resourceFilename == null) {
    		return resourceFilename;
    	}
        if (resourceFilename.startsWith("../") || resourceFilename.startsWith("./")) {
            
            String  refFields[] = FileUtility.urlToAbsPath(referenceFilename).split("/");
            String resourceFields[] = FileUtility.urlToAbsPath(resourceFilename).split("/");
            
            int refFieldsLen = refFields.length;
            int resourceFieldsLen = resourceFields.length;
            
            int minFieldsLen = refFieldsLen;
            if (resourceFieldsLen < refFieldsLen) {
                minFieldsLen =  resourceFieldsLen;
            }
            
            int i = 0;
            
            while (i < resourceFieldsLen && resourceFields[i].equalsIgnoreCase("..")) {
                i++;
            }
            
            int refBaseIndex = i;
            StringBuilder absPathBuf = new StringBuilder();
            
            int s = 0;
            while (s < refFieldsLen - refBaseIndex - 1 ) {
                absPathBuf.append(refFields[s] + "/");
                s++;
            }
            
            if (resourceFilename.startsWith("./")) {
                i = 1;
            }
            
            while (i < resourceFieldsLen - 1) {
                absPathBuf.append(resourceFields[i] + "/");
                i++;
            }
            
            absPathBuf.append(resourceFields[i]);
            
            
            return absPathBuf.toString();
            
        } else {
            return resourceFilename;
        }
    }
    
// ########### Methods below use (more of the) java.nio classes. 
// ########### Some methods above could/should be rewritten or replaced by java.nio facilities.
	/**
	 * Moves a directory (with its contents and sub-directories), possibly by
	 * copying and deleting.
	 *  
	 * @param source the directory to move
	 * @param destination the destination for the directory
	 * @param options copy options for the move action
	 * 
	 * @return {@code true} if moving succeeded completely, {@code false} in 
	 * case of (partial or complete) failure
	 * 
	 * @throws IOException wrapper for any exception that could occur during 
	 * the move
	 */
	public static boolean moveDirectory(Path source, Path destination, CopyOption... options) throws IOException {
		boolean success = true;
		Path destPath = null;
		/*
		try {
			// on Windows the FileAttribute... is not optional, contrary to what the documentation states (NullPointerException)
			destPath = Files.createDirectories(destination, (FileAttribute<?>[])null);
		} catch (UnsupportedOperationException oue) {
			// cannot happen when FileAttribute[] is null
		} catch (FileAlreadyExistsException faee) {
			// the directory may already exist, maybe acceptable
			success = false;
		} catch (SecurityException se) {
			throw new IOException(se);
		}
		*/
		// create directories the java.io way
		File destFile = destination.toFile();
		try {
			success = destFile.mkdirs();
			if (success) {
				destPath = destination; 
			}
		} catch (SecurityException se) {
			if (LOG.isLoggable(Level.INFO)) {
				LOG.log(Level.INFO, String.format("Failed to create directory '%s': %s", 
						destination, se.getMessage()));
			}
		}
		
		if (options == null) {
			options = new CopyOption[] {StandardCopyOption.REPLACE_EXISTING};
		}
		
		// either existing or newly created
		if (destPath != null) {
			File sourceDir = source.toFile();
			// move files and directories
			for (File f : sourceDir.listFiles()) {
				if (!f.isDirectory()) {
					// try moving
					Path df = null;
					try {
						df = Files.move(f.toPath(), destination.resolve(f.getName()), options);
					} catch (Throwable t) {
						if (LOG.isLoggable(Level.INFO)) {
							LOG.log(Level.INFO, String.format("Failed to move file '%s' to file '%s': %s", 
									f.toPath(), destination.resolve(f.getName()), t.getMessage()));
						}
						success = false;
					}
					// else copy and delete
					if (df == null) {
						try {
							Files.copy(f.toPath(), destination.resolve(f.getName()), options);
							Files.deleteIfExists(f.toPath());
						} catch (Throwable t) {
							if (LOG.isLoggable(Level.INFO)) {
								LOG.log(Level.INFO, String.format("Failed to copy and delete file '%s' to file '%s': %s", 
										f.toPath(), destination.resolve(f.getName()), t.getMessage()));
							}
							success = false;
						}
					}
				} else { // directory
					try {
						boolean dirSuc = moveDirectory(f.toPath(), destination.resolve(f.getName()), options);
						if (dirSuc) {
							// f is deleted in the call to moveDirectory
						} else {
							success = false;
						}
					} catch (Throwable t) {
						if (LOG.isLoggable(Level.INFO)) {
							LOG.log(Level.INFO, String.format("Failed to move directory '%s' to '%s': %s", 
									f.toPath(), destination.resolve(f.getName()), t.getMessage()));
						}
						success = false;
					}
				}
			}
			if (success) {
				success = sourceDir.delete();
				//success = Files.deleteIfExists(sourceDir.toPath());
			}
		} else {
			success = false;
		}
		
		return success;
	}
	
	/**
	 * Checks if the file is a back up file
	 * 
	 * @param name, filename to be checked
	 * @return true, if file is a media file else false
	 */
	public static boolean isBackupFile(String path) {
		if (path == null) {
			return false;
		}
		String lowerPath = path.toLowerCase();
		int index = lowerPath.lastIndexOf(".");
		if (index > -1) {
			String extension = lowerPath.substring(lowerPath.lastIndexOf("."));
			Pattern backupFilePattern = Pattern.compile(".00[12345]");
			Matcher matcher = backupFilePattern.matcher(extension);
			if (matcher.matches()) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}

	}
    
}
