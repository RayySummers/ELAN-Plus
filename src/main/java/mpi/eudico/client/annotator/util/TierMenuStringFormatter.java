package mpi.eudico.client.annotator.util;

import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;

import java.util.List;

/**
 * This class is used to return a string value of a given tier name together with the annotation count.
 * The visibility of the annotation count can be enabled/disabled via the preference settings (Show Annotation Count).
 * By calling initialize with a list of tiers, the class computes the ideal width such that subsequent
 * calls to getFormattedString return a string value that is nicely aligned with the string values of the
 * other tiers. 
 * 
 * @author Jeffrey Lemein
 * @version June 2011
 */
public class TierMenuStringFormatter {
	private static int cellWidth = 0;
	private static int maxCharacterLength = 0;
	
	/**
	 * Private constructor.
	 */
	private TierMenuStringFormatter() {
		super();
	}

	/**
	 * Initializes the cell width and maximum character length.
	 * 
	 * @param tierList the list of tiers
	 */
	public static void InitializeWithTierList(List<TierImpl> tierList){
		maxCharacterLength = 0;
        for( TierImpl t : tierList ){
        	int currLength = t.getName().length();
        	if( currLength > maxCharacterLength ) {
				maxCharacterLength = currLength;
			}
        }
        
        cellWidth = maxCharacterLength*8;
	}
	
	/**
	 * Updates the string formatter to take into account another tier implementation.
	 * This is much more efficient than calling initialize again for all tiers.
	 * 
	 * @param tier the tier to add
	 */
	public static void UpdateWithTier(TierImpl tier){
		int length = tier.getName().length();
		if( length > maxCharacterLength ) {
			maxCharacterLength = length;
		}
	}
	
	/**
	 * Updates the string formatter to take into account a string value.
	 * 
	 * @param value the string (a tier name) to add 
	 */
	public static void UpdateWithString(String value){
		int length = value.length();
		if( length > maxCharacterLength ) {
			maxCharacterLength = length;
		}
	}
	
	/**
	 * Returns a formatted string for the specified tier implementation. The result is based on the
	 * preference settings.
	 * 
	 * @param tier the tier to get the formatted string for
	 * @return the HTML formatted string or just the tier name, depending on preference settings
	 */
	public static String GetFormattedString(TierImpl tier){
		Boolean boolPref = Preferences.getBool("UI.MenuItems.ShowAnnotationCount", null);
		if( boolPref == null || boolPref == false ) {
			return tier.getName();
		} else {
			return "<html><table cellpadding='0' cellspacing='0'><tr><td width ='" + cellWidth + "'>" + tier.getName() + "</td><td>[" + tier.getNumberOfAnnotations()+ "]</td></tr></table></html>";
		}
	}
	
	/**
	 * Returns a formatted string for the specified tier name and annotation count. 
	 * The result is based on the preference settings.
	 * 
	 * @param tierName the name of the tier
	 * @param suffix the number of annotations as a string
	 * 
	 * @return a HTML formatted string or the tier name
	 */
	public static String GetFormattedString(String tierName, String suffix){
		Boolean boolPref = Preferences.getBool("UI.MenuItems.ShowAnnotationCount", null);
		if( boolPref == null || boolPref == false ) {
			return tierName;
		} else {
			return "<html><table cellpadding='0' cellspacing='0'><tr><td width ='" + cellWidth + "'>" + tierName + "</td><td>" + suffix + "</td></tr></table></html>";
		}
	}
}

