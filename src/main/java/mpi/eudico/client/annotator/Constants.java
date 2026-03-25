package mpi.eudico.client.annotator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.plaf.FontUIResource;


/**
 * Some constants for use in ELAN. Some members are no longer actual constants
 * but are set or changed when the application is launched.
 */
public class Constants {
	
	private Constants() {
	}

	/** ELAN main page URL */
	public static final String ELAN_INFO_URL =      "https://archive.mpi.nl/tla/elan";
	/** ELAN release notes URL */
	public static final String ELAN_REL_NOTES_URL = "https://archive.mpi.nl/tla/elan/release-notes";
	/** ELAN download URL */
	public static final String ELAN_DOWNLOAD_URL =  "https://archive.mpi.nl/tla/elan/download";
	/** ELAN forum URL */
	public static final String ELAN_FORUM_URL =     "https://archive.mpi.nl/forums/";
	/** ELAN privacy policy URL */
	public static final String ELAN_PRIVACY_POLICY_URL = "https://www.mpi.nl/tools/elan/docs/ELAN_Privacy_Policy_20240508.pdf";
	/** ELAN mailing list subscribe URL */
	public static final String ELAN_SUBSCRIBE_URL = "mailto:elanlist-request@mpi.nl?subject=subscribe";
	
    /** The home directory of the user */
    public static final String USERHOME = System.getProperty("user.home");

    /** The platform dependent file separator as a string */
    public static final String FILESEPARATOR = System.getProperty("file.separator");

    /** An old configuration file, will be deleted by current versions of ELAN */
    public static final String STRPROPERTIESFILE = USERHOME + FILESEPARATOR +
        ".elan.config";

    /** The (platform dependent) path of the folder where ELAN stores settings etc.*/
    public static final String ELAN_DATA_DIR;
    
	/**
	 * The directory (inside ELAN_DATA_DIR) where we search for
	 * lexicon-component (lexan) lexicons.
	 */
    public static final String LEXAN_LEXICON_DIR = "LexanLexicons";
	
    /** The name of a cache folder reserved for (text-based) analyzer extensions */
    public static final String ANALYZER_CACHE_FOLDER_NAME = "Analyzers";
    /** The name of a database directory */
    public static final String DB_DIR = "DB";

    /** The default background color for viewers, panels etc. */
    public static Color DEFAULTBACKGROUNDCOLOR = new Color(230, 230, 230);

    /** The default foreground color for viewers, panels etc. */
    public static Color DEFAULTFOREGROUNDCOLOR = Color.BLACK;

    /** The default color to mark out the selected time interval */
    public static Color SELECTIONCOLOR = new Color(204, 204, 255);

    /** The default color for the marker of the current media time, 
     * the crosshair or playhead */
    public static final Color CROSSHAIRCOLOR = Color.RED;
    
    /** The default color for marking audio segments boundaries */
    public static final Color SEGMENTATIONCOLOR = Color.BLUE;

    /** The default color for the active annotation (the selected annotation) */
    public static final Color ACTIVEANNOTATIONCOLOR = Color.BLUE;

    /** The default color to mark the advance in time in a media player slider */
    public static final Color MEDIAPLAYERCONTROLSLIDERSELECTIONCOLOR = Color.GRAY;

    /** The default color for the crosshair in a media player control slider */
    public static final Color MEDIAPLAYERCONTROLSLIDERCROSSHAIRCOLOR = Color.RED.darker();

    /** One of the colors used in blending of (the visualization of) two audio channels */
    public static final Color SIGNALSTEREOBLENDEDCOLOR1 = Color.GREEN;

    /** One of the colors used in blending of (the visualization of) two audio channels */
    public static final Color SIGNALSTEREOBLENDEDCOLOR2 = Color.BLUE;

    /** The default color for the waveform channel background */
    public static final Color SIGNALCHANNELCOLOR = new Color(224, 224, 224);

    /** A shared color (orange) */
    public static final Color SHAREDCOLOR1 = Color.ORANGE;

    /** A shared color (yellow) */
    public static final Color SHAREDCOLOR2 = Color.YELLOW;

    /** A shared color (gray) */
    public static final Color SHAREDCOLOR3 = Color.GRAY;

    /** A shared color (white) */
    public static final Color SHAREDCOLOR4 = Color.WHITE;
    
    /** A shared color (purple) */
    public static final Color SHAREDCOLOR5 = new Color(128, 0, 128);
    
    /** A shared color (dark gray) */
    public static final Color SHAREDCOLOR6 = Color.DARK_GRAY;

    /** The default color for the background of the active tier */
    public static Color ACTIVETIERCOLOR = new Color(230, 210, 210);
    
    /** The default color for a lighter background */
    public static Color LIGHTBACKGROUNDCOLOR = new Color(240, 240, 240);
    
    /** the background color for the even rows in a table or list */
    public static Color EVEN_ROW_BG = new Color(234, 245, 245);
    
    /** the background color for the selected row in a table or list */
    public static Color SELECTED_ROW_BG = new Color(200, 215, 215);
    /** a light yellow background color */
    public static final Color LIGHT_YELLOW = new Color(255, 255, 192);

    /** the {@code Look & Feel} default font for labels, not a constant, can be null */
    public static Font DEFAULT_LF_LABEL_FONT = null;
    
    /** the default font for tiers and annotations (if installed), no longer a constant */
    public static Font DEFAULTFONT = new Font("Arial Unicode MS", Font.PLAIN, 12);

    /** a slightly smaller version of the default font, no longer a constant */
    public static Font SMALLFONT = new Font("Arial Unicode MS", Font.PLAIN, 10);
    
    /** the size for a slightly smaller version of the default font, not a constant */
    public static float SMALLFONT_SIZE = 10f;
    
    /** the scale factor to use for a slightly smaller version of a font */
    public static final float SMALLFONT_SCALE_FACTOR = 0.84f;
    
    /** the lower boundary for automatic scaling of the font for high resolution
     * displays. The default UI font seems readable with DPI is 144 or lower */
    public static final int LOW_RES_SCREEN_DPI = 144;

    /**  */
    //public static final int SCROLLMIN = 0;

    
    //public static final int SCROLLINCREMENT = 1;
    
    /** A constant for a custom number of visible rows in a combobox */
    public static final int COMBOBOX_VISIBLE_ROWS = 20;
    
    /** A constant for the number of visible menu items in a scrollable menu */
    public static final int VISIBLE_MENUITEMS = 20;
    
    /** The default maximum of visible media (video) players */
    public static final int MAX_VISIBLE_PLAYERS = 4;

    // backup interval constants
    
    /** 1 minute interval backup */
    public static final Integer BACKUP_1 = Integer.valueOf(60000);
    
	/** 5 minute interval backup */
	public static final Integer BACKUP_5 = Integer.valueOf(300000);

    /** 10 minute interval backup */
    public static final Integer BACKUP_10 = Integer.valueOf(600000);

    /** 20 minute interval backup */
    public static final Integer BACKUP_20 = Integer.valueOf(1200000);

    /** 30 minute interval backup */
    public static final Integer BACKUP_30 = Integer.valueOf(1800000);
    
    /** font sizes available in several viewers */
    public static final int[] FONT_SIZES = new int[]{8, 9, 10, 12, 14, 16, 18, 24, 36, 42, 48, 60, 72};
    
    /** one decimal digit */
    public static final int ONE_DIGIT = 1;
    /** two decimal digits */
    public static final int TWO_DIGIT = 2;
    /** three decimal digits */
    public static final int THREE_DIGIT = 3;
    
    // time format constants
	/** the hour/minutes/seconds/milliseconds format */
	public static final int HHMMSSMS = 100;

	/** the seconds/milliseconds format */
	public static final int SSMS = 101;

	/** the pure milliseconds format */
	public static final int MS = 102;
	
	/** the frame number format */
	public static final int HHMMSSFF = 103;

	/** string version of format, "hh:mm:ss.ms" */
	public static final String HHMMSSMS_STRING = "hh:mm:ss.ms";
	/** string version of format, "ss.ms" */
	public static final String SSMS_STRING = "ss.ms";
	/** string version of format, "ms" */
	public static final String MS_STRING = "ms";
	/** string version of format, "hh:mm:ss:ff" */
	public static final String HHMMSSFF_STRING = "hh:mm:ss:ff";
	/** string version of format, "PAL" */
	public static final String PAL_STRING = "PAL";
	/** string version of format, "PAL-50fps" */
	public static final String PAL_50_STRING = "PAL-50fps";
	/** string version of format, "NTSC" */
	public static final String NTSC_STRING = "NTSC";
	/** A label for the begin time marker in Toolbox file conversions */
	public static final String ELAN_BEGIN_LABEL = "ELANBegin";
	/** A label for the end time marker in Toolbox file conversions */
	public static final String ELAN_END_LABEL = "ELANEnd";
	/** A label for the participant marker in Toolbox file conversions */
	public static final String ELAN_PARTICIPANT_LABEL = "ELANParticipant";
	
	/** HS May 2008: new location of ELAN data folder on the Mac. */
	static {
		if (System.getProperty("os.name").indexOf("Mac OS") > -1) {
			ELAN_DATA_DIR = USERHOME + FILESEPARATOR + "Library" + FILESEPARATOR + 
					"Preferences" + FILESEPARATOR + "ELAN";
			maxButtonHeight = 22; // to prevent rounded corners in macOS L&F
		} else {
			ELAN_DATA_DIR = USERHOME + FILESEPARATOR + ".elan_data";
			maxButtonHeight = -1;
		}
	}
	/** constants for copying the annotation value plus time information */
	public static final String TEXTANDTIME_STRING = "annotation + begintime + endtime";
	/** constants for copying the annotation value  */
	public static final String TEXT_STRING = "annotation only";
	/** constants for copying the file path, tier name, begin and end time */
	public static final String URL_STRING = "filepath + tier name + begintime + endtime";
	/** constants for copying the file name, tier name, begin and end time */
	public static final String CITE_STRING = "filename + tiername + begintime + endtime";
	
	/** a flag to indicate whether a dark theme has been set for the Look and Feel */
	public static boolean DARK_MODE = false;
	/** the folder or package where most of the (custom) icon images are 
	 * located, can be changed for e.g. dark theme icons. */
	public static String ICON_LOCATION = "/mpi/eudico/client/annotator/resources/";
	/** the height of buttons with an icon image relative to the font size */
	public static final float FONT_TO_BUTTON_SIZE = 1.8f;
	/** the default size of buttons with an icon image */
	public static final Dimension ICON_BUTTON_SIZE = new Dimension(30, 20);
	private static int maxButtonHeight;
	
	/**
	 * Sets a scale factor for the default font for tiers and annotations. 
	 * Best be called after setting a preferred default font and/or size.
	 * 
	 * @param scaleFactor the scale factor
	 */
	public static void setFontScaling(float scaleFactor) {
		DEFAULTFONT = new Font(DEFAULTFONT.getFontName(), DEFAULTFONT.getStyle(), 
				(int) Math.ceil((DEFAULTFONT.getSize() * scaleFactor)));
		SMALLFONT_SIZE = scaleFactor * SMALLFONT_SIZE;
		SMALLFONT = new Font(SMALLFONT.getFontName(), SMALLFONT.getStyle(),
				(int) Math.ceil(SMALLFONT_SIZE));
		
		int h = (int) (FONT_TO_BUTTON_SIZE * DEFAULTFONT.getSize());
		if (maxButtonHeight > 0 && h > maxButtonHeight) {
			h = maxButtonHeight;
		}
		ICON_BUTTON_SIZE.setSize((int) (1.5 * h), h);
	}
	
	/**
	 * Sets the default font to use for tiers and annotations. The application's default is "Arial Unicode MS",
	 * if that font is not installed it is up to the JRE which font is used.
	 * 
	 * If this method is called before setFontScaling the actual size will be different depending on the scale factor.
	 * 
	 * @param fontName the name of the font
	 * @param fontSize the size of the font
	 */
	public static void setDefaultFont(String fontName, int fontSize) {
		DEFAULTFONT = new Font(fontName, Font.PLAIN, fontSize);
		SMALLFONT_SIZE = fontSize * SMALLFONT_SCALE_FACTOR;
		SMALLFONT = DEFAULTFONT.deriveFont(Font.PLAIN, SMALLFONT_SIZE);
		
		int h = (int) (FONT_TO_BUTTON_SIZE * DEFAULTFONT.getSize());
		if (maxButtonHeight > 0 && h > maxButtonHeight) {
			h = maxButtonHeight;
		}
		ICON_BUTTON_SIZE.setSize((int) (1.5 * h), h);
	}
	
	/**
	 * Sets the default {@code Look&Feel} font used for JLabels.
	 * 
	 * @param labelFont the font used for labels by the current {@code L&F} 
	 */
	public static void setLookAndFeelLabelFont(Font labelFont) {
		if (labelFont instanceof FontUIResource) {
			// could maybe use the FontUIResource, but create a Font now
			DEFAULT_LF_LABEL_FONT = new Font(labelFont.getFontName(), 
					labelFont.getStyle(), labelFont.getSize());
		} else {
			DEFAULT_LF_LABEL_FONT = labelFont;
		}
		
		int h = (int) (FONT_TO_BUTTON_SIZE * DEFAULT_LF_LABEL_FONT.getSize());
		if (maxButtonHeight > 0 && h > maxButtonHeight) {
			h = maxButtonHeight;
		}
		ICON_BUTTON_SIZE.setSize((int) (1.5 * h), h);
	}
	
	/**
	 * Returns a slightly smaller version of the font based on the 
	 * scale factor constant. 
	 * 
	 * @param sourceFont the Font to create a smaller version of
	 * @return a scaled version of the font
	 */
	public static Font deriveSmallFont(Font sourceFont) {
		if (sourceFont == null) {
			return null;
		}
		return sourceFont.deriveFont(sourceFont.getSize2D() * Constants.SMALLFONT_SCALE_FACTOR);
	}
	
	/**
	 * Sets the default panel foreground and background colors. A few other 
	 * colors are derived as shades of these default colors.
	 * 
	 * @param panelBackground the {@code L&F} panel background color
	 * @param panelForeground the {@code L&F} panel foreground color
	 */
	public static void setLookAndFeelColors(Color panelBackground, Color panelForeground) {
		if (panelBackground != null) {
	    	DEFAULTBACKGROUNDCOLOR = panelBackground;
	    	LIGHTBACKGROUNDCOLOR = new Color(
	    			Math.min(255, DEFAULTBACKGROUNDCOLOR.getRed() + 10),
	    			Math.min(255, DEFAULTBACKGROUNDCOLOR.getGreen() + 10),
	    			Math.min(255, DEFAULTBACKGROUNDCOLOR.getBlue() + 10));
		}
		if (panelForeground != null) {
			DEFAULTFOREGROUNDCOLOR = panelForeground;
		}
    	if (DARK_MODE) {
    		SELECTIONCOLOR = new Color(
    				Math.min(255, DEFAULTBACKGROUNDCOLOR.getRed() + 15),
    				Math.min(255, DEFAULTBACKGROUNDCOLOR.getGreen() + 50),
        			Math.min(255, DEFAULTBACKGROUNDCOLOR.getBlue() + 110));
    		ACTIVETIERCOLOR = new Color(
    				Math.min(255, DEFAULTBACKGROUNDCOLOR.getRed() + 40),
    				DEFAULTBACKGROUNDCOLOR.getGreen(),
        			DEFAULTBACKGROUNDCOLOR.getBlue());
    		//CROSSHAIRCOLOR = new Color(235, 0, 0);
    		SELECTED_ROW_BG = new Color(    				
    				Math.max(0, DEFAULTBACKGROUNDCOLOR.getRed() - 10),
    				DEFAULTBACKGROUNDCOLOR.getGreen(),
        			DEFAULTBACKGROUNDCOLOR.getBlue());
    		EVEN_ROW_BG = new Color(    				
    				Math.min(255, LIGHTBACKGROUNDCOLOR.getRed()),
    				Math.min(255, LIGHTBACKGROUNDCOLOR.getGreen() + 15),
        			Math.min(255, LIGHTBACKGROUNDCOLOR.getBlue() + 15));
    	}
	}
	
	/**
	 * If a dark theme is selected some settings are updated.
	 * 
	 * @param isDarkTheme if {@code true} a dark {@code L&F} is in place, 
	 * {@code false} by default
	 */
	public static void setDarkTheme(boolean isDarkTheme) {
		DARK_MODE = isDarkTheme;
		if (isDarkTheme) {
			ICON_LOCATION = "/mpi/eudico/client/annotator/resources/dark/";
		}
	}
}
