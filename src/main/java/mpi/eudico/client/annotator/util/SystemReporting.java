package mpi.eudico.client.annotator.util;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Utility class that provides easy access to some common system properties.
 * Examples are the operating system and its version, location of 
 * {@code lib/ext}, information on the physical screen etc. Some of the
 * methods obsolete.  
 */
public class SystemReporting {
	/** name of the operating system */
	public static final String OS_NAME;
	/** user name */
	public static final String USER_HOME;
	/** anti aliased text property */
	public static final boolean antiAliasedText;
	/** buffered painting property */
	public static final boolean useBufferedPainting;
	/** whether the buffered painting property has been set */
	public static final boolean isBufferedPaintingPropertySet;
	/** whether the system is {@code macOS} */
	private static boolean isMacOS;
	/** whether the system is {@code macOS Sierra} or higher */
	private static boolean isMacSierraOrHigher = false;
	/** whether the system is {@code Windows} */
	private static boolean isWindows;
	/** whether the system is {@code Windows Vista} */
	private static boolean isVista = false;
	/** whether the system is {@code WIndows 7} */
	private static boolean isWin7 = false;
	/** whether the system is {@code Linux} */
	private static boolean isLinux;
	
	static {
		OS_NAME = System.getProperty("os.name");
		USER_HOME = System.getProperty("user.home");
		
		String lowerOS = OS_NAME.toLowerCase();
		
		if (lowerOS.indexOf("win") > -1) {
			isWindows = true;
		} else if (lowerOS.indexOf("mac") > -1) {
			isMacOS = true;
		} else if (lowerOS.indexOf("lin") > -1) {
			isLinux = true;
		}
		
		// check Windows versions and macOS
		String version = System.getProperty("os.version");// 6.0 = Vista, 6.1 = Win 7

		try {
			if (isWindows) {
				if (version.indexOf('.') > -1) {
					String[] verTokens = version.split("\\.");
					int major = Integer.parseInt(verTokens[0]);
					if (verTokens.length > 1) {
						int minor = Integer.parseInt(verTokens[1]);
						if (major > 6) {
							// treat as win 7 for now
							isWin7 = true;
						} else if (major == 6) {
							if (minor > 0) {
								isWin7 = true;
							} else {
								isVista = true;
							}
						}
					}
				} else {
					int major = Integer.parseInt(version);
					if (major > 6) {
						isWin7 = true;
					} else if (major == 6){
						isVista = true;// arbitrary assumption
					}
				}
			} else if (isMacOS) {
				String[] verTokens = version.split("\\.");
				if (verTokens.length >= 2) {
					int major = Integer.parseInt(verTokens[0]);
					if (major == 10) {
						int minor = Integer.parseInt(verTokens[1]);
						if (minor >= 11) {
							isMacSierraOrHigher = true;
						}
					} else if (major > 10) {
						isMacSierraOrHigher = true;
					}
				}
			}
		} catch (NumberFormatException nfe) {
			ClientLogger.LOG.warning("Unable to parse the OS version.");
		}
		
		boolean antiAliasTemp = false;
		String atp = System.getProperty("swing.aatext");
		if ("true".equals(atp)) {
			antiAliasTemp = true;
		}
		// for now under J 1.6 only apply the text anti aliasing property
		@SuppressWarnings("rawtypes")
		Map map = (Map)(Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints"));

		if (map != null) {
			Object aaHint = map.get(RenderingHints.KEY_TEXT_ANTIALIASING);

			if (RenderingHints.VALUE_TEXT_ANTIALIAS_OFF != aaHint /*|| 
					RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT.equals(aaHint)*/) {
				// treat default as anti-aliasing on??
				antiAliasTemp = true;
			}
			//Iterator mapIt = map.keySet().iterator();
		}
		
		String awtRH = System.getProperty("awt.useSystemAAFontSettings");
		if ("on".equals(awtRH)) {
			antiAliasTemp = true;
		} else if (map != null) {
			// a desktop setting is overridden by a -D argument
			// should do more specialized testing on the value of awtRH
			if ("off".equals(awtRH) || "false".equals(awtRH) || "default".equals(awtRH)) {
				antiAliasTemp = false;
			}
		}
		antiAliasedText = antiAliasTemp;
		
        String bufImg = System.getProperty("useBufferedImage");
        if (bufImg != null) {
        	isBufferedPaintingPropertySet = true;
        	useBufferedPainting = bufImg.toLowerCase().equals("true");
        } else {
        	isBufferedPaintingPropertySet = false;
        	useBufferedPainting = false;
        }
	}

	/**
	 * Private constructor.
	 */
	private SystemReporting() {
		super();
	}

	/**
	 * Returns whether this is a {@code macOS} system.
	 * 
	 * @return {@code true} if this is {@code macOS}, {@code false} otherwise
	 */
	public static boolean isMacOS() {
		return isMacOS;
	}

	/**
	 * Returns whether this is a {@code macOS Sierra} or higher system.
	 * 
	 * @return {@code true} if this is {@code macOS Sierra} or higher, 
	 * {@code false} otherwise
	 */
	public static boolean isMacOSSierraOrHigher() {
		return isMacSierraOrHigher;
	}
	
	/**
	 * Returns whether this is a {@code WIndows} system.
	 * 
	 * @return {@code true} if this is {@code Windows}, {@code false otherwise}
	 */
	public static boolean isWindows() {
		return isWindows;
	}
	
	/**
	 * Returns whether this is a {@code Windows Vista} system.
	 * 
	 * @return {@code true} if this is {@code Windows Vista}, {@code false}
	 * otherwise
	 */
	public static boolean isWindowsVista() {
		return isVista;
	}

	/**
	 * Returns whether this is a {@code Windows 7} (or higher) system.
	 * 
	 * @return {@code true} if this is {@code Windows 7} or higher,
	 * {@code false} otherwise
	 */
	public static boolean isWindows7OrHigher() {
		return isWin7;
	}
	
	/**
	 * Returns whether this is a {@code Linux} system (any flavor).
	 * 
	 * @return {@code true} if this is any distribution of {@code Linux},
	 * {@code false} otherwise
	 */
	public static boolean isLinux() {
		return isLinux;
	}
	
	/**
	 * Prints a system property to standard out, format 
	 * {@code property_name=value}.
	 * 
	 * @param prop the name of the property
	 */
	public static void printProperty(String prop) {
		System.out.println(prop + " = " + System.getProperty(prop));
	}

	/**
	 * Returns the JRE's {@code lib/ext} directory, if it exists.
	 * No longer relevant, newer versions of the Java runtime ({@code 10>})
	 * don't have this special folder.  
	 * 
	 * @return the {@code lib/ext} directory
	 */
	public static File getLibExtDir() {
		if (OS_NAME.startsWith("Mac OS X")) {
			return verifyMacUserLibExt();
		} else {
			return new File (System.getProperty("java.home")
								 + File.separator
								 + "lib"
								 + File.separator
								 + "ext");
		}
	}

	/**
	 * Returns the files that are in the {@code lib/ext} directory.
	 * No longer relevant.
	 * 
	 * @return the files from {@code lib/ext}, may be {@code null}
	 */
	public static File[] getLibExt() {
		File ext = SystemReporting.getLibExtDir();
		if (ext != null && ext.exists()) {
			return SystemReporting.getLibExtDir().listFiles();
		} else {
			return null;
		}
	}
	
    private static File verifyMacUserLibExt() {
		// im jars will be stored in the user home library ext dir
		String userLibJavaExt = USER_HOME + "/Library/Java/Extensions";

		File userLibExt = new File(userLibJavaExt);
		//System.out.println("Home lib ext: " + userLibJavaExt);
		if (!userLibExt.exists()) {
			try {
				boolean success = userLibExt.mkdirs();
				if (!success) {
					ClientLogger.LOG.warning("Unable to create folder: " + userLibExt);
					return null;
				}
			} catch (SecurityException se) {
				ClientLogger.LOG.warning("Unable to create folder: " + userLibExt);
				ClientLogger.LOG.warning("Cause: " + se.getMessage());
				return null;
			}
		}
		return userLibExt;
	}

	/**
	 * Report files from {@code lib/ext}.
	 * No longer relevant.
	 */
	public static void printLibExt() {
		File potext[] = getLibExt();
		int NOFfiles = potext==null?0:potext.length;
		System.out.println("Found " + NOFfiles+ " potential extension(s)");
		for (int i=0; i<NOFfiles; i++) {
			System.out.println("\t" + potext[i]);
		}
	}

	/**
	 * Returns a number of lines with information about the physical screens.
	 * Note: the reported resolution is often not correct.
	 * 
	 * @return a list containing one line per active, physical screen and
	 * one line with the main screen resolution
	 */
	public static List<String> getScreenInfo() {
		List<String> infoList = new ArrayList<String>(4);
		try {
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsDevice[] screens = ge.getScreenDevices();
			int count = 0;
			for (GraphicsDevice gd : screens) {
				count++;
				DisplayMode dMode = gd.getDisplayMode();
				infoList.add(String.format("Screen %d - isDefault:%b, %s", 
						count, (gd == ge.getDefaultScreenDevice()), 
						String.format("w:%d, h:%d, bitDepth:%d", dMode.getWidth(), dMode.getHeight(), 
								dMode.getBitDepth())));
			}

			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
			infoList.add(String.format("Main screen resolution:%d (w:%d, h:%d)", 
					Toolkit.getDefaultToolkit().getScreenResolution(), dim.width, dim.height));
		} catch (Throwable t) {
		}
		return infoList;
	}
	
	/**
	 * Returns the dots-per-inch resolution, is often not correct.
	 * 
	 * @return the resolution in dots-per-inch of the primary screen (which may not be actually 
	 * used for display)
	 */
	public static int getScreenResolution() {
		try {
			return Toolkit.getDefaultToolkit().getScreenResolution();
		} catch (Throwable t) {
			return 0;
		}
	}
	
	/**
	 * Returns the macro or main version of the Java runtime.
	 * 
	 * @return the macro version of the current Java Runtime Environment. 
	 * In case of a 1.x version, x will be returned (so 6 for 1.6, 7 for 1.7 etc.).
	 */
	public static int getJavaMacroVersion() {
		String versionStr = System.getProperty("java.version");
		if (versionStr != null) {
			String[] verSplit = versionStr.split("\\.");
			if (verSplit.length >= 2) {
				if (verSplit[0].equals("1")) {
					try {
						return Integer.parseInt(verSplit[1]);
					} catch (NumberFormatException nfe) {
						if (ClientLogger.LOG.isLoggable(Level.INFO)) {
							ClientLogger.LOG.info("Unable to parse the main Java version from: " + verSplit[1]);
						}
					}
				} else if (verSplit[0].length() > 1){
					try {
						return Integer.parseInt(verSplit[0]);
					} catch (NumberFormatException nfe) {
						if (ClientLogger.LOG.isLoggable(Level.INFO)) {
							ClientLogger.LOG.info("Unable to parse the main Java version from: " + verSplit[0]);
						}
					}
				}
			} else {
				try {
					return Integer.parseInt(versionStr);
				} catch (NumberFormatException nfe) {
					if (ClientLogger.LOG.isLoggable(Level.INFO)) {
						ClientLogger.LOG.info("Unable to parse the main Java version from: " + versionStr);
					}
				}
			}
		}
		// error condition
		return -1;
	}
	
	/**
	 * For testing.
	 * 
	 * @param args ignored
	 * @throws Exception any exception
	 */
    public static void main(String args[]) throws Exception {
		printProperty("java.home");
		printLibExt();
    }
}
