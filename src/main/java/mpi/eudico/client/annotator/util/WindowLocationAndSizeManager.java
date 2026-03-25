package mpi.eudico.client.annotator.util;

import mpi.eudico.client.annotator.Preferences;

import java.awt.*;

/**
 * Class to store and restore the location and size of a window.
 * 
 * @author aarsom
 *
 */
public class WindowLocationAndSizeManager {
	
	/**
	 * Private constructor.
	 */
	private WindowLocationAndSizeManager() {
		super();
	}

	/**
     * Pack, size and set location.
     * 
     * @param window the window for which the location and size has to be set
     * @param prefPrefix the preference prefix for reading the stored 
     * preferences for the window
     */
	public static void postInit(Window window, String prefPrefix){
		postInit(window, prefPrefix, 0, 0);
	}
	
	/**
     * Pack, size and set location.
     * 
     * @param window the window for which the location and size has to be set
     * @param prefPrefix the preference prefix for reading the stored
     * preferences for the window
     * @param minimalWidth a minimal width to maintain for the window
     * @param minimalHeight a minimal height to maintain for the window
     */
	public static void postInit(Window window, String prefPrefix, int minimalWidth, int minimalHeight ){
		window.pack();
        
        // set initial location and size, take insets into account
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        Insets ins = Toolkit.getDefaultToolkit().getScreenInsets(
      		ge.getDefaultScreenDevice().getDefaultConfiguration());
        
        int maxW = dim.width - ins.left - ins.right;
        int maxH = dim.height - ins.top - ins.bottom;
        
        Point location = null;
        Dimension size = null;
        
        if(prefPrefix != null){
        	location = Preferences.getPoint(prefPrefix+".Location", null);
        	size = Preferences.getDimension(prefPrefix+".Size", null);
        }
        
		if (location != null) {
			Point p = location;
			int x = p.x <= maxW - 50 ? p.x : maxW - 50;
			int y = p.y <= maxH - 50 ? p.y : maxH - 50;
			window.setLocation(x, y);
		} else {
			window.setLocationRelativeTo(window.getParent());
		}
		
		int targetW;
		int targetH;
		if (size != null) {
			Dimension d = size;
			targetW = d.width < maxW ? d.width : maxW;
	        targetH = d.height < maxH ? d.height : maxH;
		} else {
			targetW = window.getSize().width < minimalWidth ? minimalWidth : window.getSize().width;
	        targetW = targetW > maxW ? maxW : targetW;
	        
	        targetH = window.getSize().height < minimalHeight ? minimalHeight : window.getSize().height;
	        targetH = targetH > maxH ? maxH : targetH;
		}
		window.setSize(targetW, targetH);        
	}
	
	/**
	 * Stores the location and size preferences of the window. 
	 * 
	 * @param window the window for which the preferences have to be stored
	 * @param prefPrefix prefix for storing the window preferences
	 */
	public static void storeLocationAndSizePreferences(Window window, String prefPrefix){
		if(window != null && prefPrefix != null){
			Point p = window.getLocation();
	        Dimension d = window.getSize();
	        Preferences.set(prefPrefix+".Location", p, null, false, false);
	        Preferences.set(prefPrefix+".Size", d, null, false, false);
		}
	}
}
