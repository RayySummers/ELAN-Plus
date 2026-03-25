package nl.mpi.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * A class to apply modifications to localization properties files. 
 * Implemented sorting, grouping, formatting.
 */
public class LocaleUtil {
	
	/**
	 * Private constructor.
	 */
	private LocaleUtil() {
		super();
	}

	/**
	 * Run as application it is possible to specify a base or source directory,
	 * the qualified name of the main properties file (without the extension),
	 * the output directory and a number to indicate how the keys should 
	 * be grouped and formatted.  
	 * @param args program arguments
	 */
	public static void main(String[] args) {
		try {
			if (args.length == 2) {
				LocaleUtil.sortPropertiesFiles(Path.of(args[0]), args[1], null, 2);
			} else if (args.length == 3) {
				LocaleUtil.sortPropertiesFiles(Path.of(args[0]), args[1], Path.of(args[2]), 2);
			} else if (args.length >= 4) {
				int numDots = 2;
				try {
					numDots = Integer.parseInt(args[3]);
				} catch (NumberFormatException nfe) {}
				LocaleUtil.sortPropertiesFiles(Path.of(args[0]), args[1], Path.of(args[2]), numDots);
			} else {
				System.out.println("At least specify a source/base directory and a qualified name of a properties file");
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	/**
	 * Sorts the keys in the bundle, groups them based on {@code <n>} equal dot-separated
	 * parts of the keys, aligns the "=" of all key-value pairs in a group and 
	 * inserts a white line between groups. Everything is returned in a StringBuffer.
	 * Could consider a variant that returns a LinkedHashMap, or one that accepts an
	 * output stream as parameter.
	 * 
	 * @param bundle the source resource bundle
	 * @param groupPerNumDots the number of dot-separated parts that must be equal to 
	 * make the keys belong to the same group.  E.g. aa.bb.cc.dd and aa.bb.yy.zz
	 * belong to the same group if {@code groupPerNumDots} is 2. If a key has 
	 * groupPerNumDots parts or less the parts count - 1 will be used.
	 * 
	 * @return a StringBuilder containing all formatted key-value lines
	 */
	public static StringBuilder sort(ResourceBundle bundle, int groupPerNumDots) {
    	// get a sorted list of the keys
		List<String> keyList = new ArrayList<String>(bundle.keySet());
    	Collections.sort(keyList);
    	
    	Map<String, List<String>> linkedKeyMap = new LinkedHashMap<String, List<String>>();
		
    	for (String key : keyList) {
    		String[] parts = key.split("\\.", groupPerNumDots + 1);
    		// count number of parts, construct keyPart
    		int actualNumDots = Math.min(groupPerNumDots, parts.length - 1);
    		
    		String prefix = parts[0];
    		for (int i = 1; i < actualNumDots; i++) {
    			prefix = String.join(".", prefix, parts[i]);
    		}
    		
    		if (linkedKeyMap.containsKey(prefix)) {
    			linkedKeyMap.get(prefix).add(key);
    		} else {
    			List<String> curGroup = new ArrayList<String>();
    			curGroup.add(key);
    			linkedKeyMap.put(prefix, curGroup);
    		}
    	}
    	
    	final StringBuilder sb = new StringBuilder();
    	final String space = " ";
    	final String lineSep = "\r\n";
    	
    	// append to buffer
    	for (Map.Entry<String, List<String>> linkedEntry : linkedKeyMap.entrySet()) {
//    		String key = linkedEntry.getKey();
    		List<String> group = linkedEntry.getValue();
    		int keyLength = 0;
    		
    		for (String k : group) {
    			keyLength = Math.max(keyLength, k.length());
    		}
    		keyLength++;
    		
    		for (String k : group) {
    			sb.append(k);
    			sb.append(space.repeat(keyLength - k.length()));
    			sb.append("= ");
    			sb.append(bundle.getString(k).replaceAll("\n", "\\\\n"));// retain new line characters
    			sb.append(lineSep);
    		}
    		sb.append(lineSep);
    	}
    	
		return sb;
	}
	
	/**
	 * Sorts a language properties file and all localized versions of those properties
	 * in the same directory.
	 * 
	 * @param basePath the directory to start the search
	 * @param resBaseName the qualified name of the language file, e.g. org.organ.register
	 * @param baseOutPath the base directory where the output will go. If {@code null} the
	 * output will go to standard out, if baseOutPath is equal to the basePath, the file
	 * will be overwritten, otherwise the output will be e.g. baseOutPath/register.properties
	 * (without the package folder structure).
	 * @param groupPerNumDots a number for grouping keys, see {@link #sort(ResourceBundle, int)}
	 * @throws IOException any file IO related error
	 */
	public static void sortPropertiesFiles(Path basePath, String resBaseName, Path baseOutPath, int groupPerNumDots) throws IOException {
		// create an input path based on base path and qualified base name
		String resourceName = ResourceBundle.Control.getControl(
				ResourceBundle.Control.FORMAT_PROPERTIES).toResourceName(resBaseName, "properties");
		Path inputPath = Path.of(basePath.toString(), resourceName);
		// construct an absolute output path
		Path absOutputPath = null;
		boolean overwrite = basePath.equals(baseOutPath);
		if (baseOutPath != null) {
			if (!overwrite) {
				absOutputPath = Path.of(baseOutPath.toString(), inputPath.getFileName().toString());
			} else {
				absOutputPath = inputPath;
			}
		}
		// load a resource bundle, sort and save
		sortPropertiesFile(inputPath, absOutputPath, groupPerNumDots);
		
		//if success, scan folder for other properties files with the same base name
		Path parentPath = inputPath.getParent();
		String inFileName = inputPath.getFileName().toString();
		int dotIndex = inFileName.lastIndexOf('.');
		String filePat = inFileName.substring(0, dotIndex) + "_[a-zA-Z_]*" + inFileName.substring(dotIndex);
		
		if (parentPath != null) {
			String[] fileNames = parentPath.toFile().list();
			for (String fileName : fileNames) {
				if (fileName.matches(filePat)) {
					// sort and save
					Path nextInPath = Path.of(parentPath.toString(), fileName);
					Path nextOutPath = null;
					if (baseOutPath != null) {
						if (!overwrite) {
							nextOutPath = Path.of(baseOutPath.toString(), fileName);
						} else {
							nextOutPath = nextInPath;
						}
					}
					sortPropertiesFile(nextInPath, nextOutPath, groupPerNumDots);
				}
			}
		}
	}
	
	/**
	 * Sorts, groups and saves a single properties file.
	 * 
	 * @param absInPath the input file path
	 * @param absOutPath the output file path
	 * @param groupPerNumDots a number for grouping keys, see {@link #sort(ResourceBundle, int)}
	 * @throws IOException any file IO related error
	 */
	public static void sortPropertiesFile(Path absInPath, Path absOutPath, int groupPerNumDots) throws IOException {
		PropertyResourceBundle bundle = new PropertyResourceBundle(
				Files.newInputStream(absInPath, StandardOpenOption.READ));
		
		StringBuilder sb = LocaleUtil.sort(bundle, groupPerNumDots);
		
		if (sb == null) {
			System.out.println("No sorted strings returned (null)");
			return;
		}
		
		if (absOutPath != null) {
			try (BufferedWriter bw = Files.newBufferedWriter(absOutPath, Charset.forName("UTF-8"), 
						StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
				bw.write(sb.toString());
			} catch (IOException ioe) {
				throw ioe;
			} catch (Throwable thr) {
				throw new IOException(thr);
			}
		} else {
			System.out.println(sb.toString());
		}
	}
}
