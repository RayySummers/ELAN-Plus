package mpi.eudico.client.annotator;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;

/**
 * Utility to create a dark theme variant of the cross platform Metal look and
 * feel.
 *
 */
public class LaFUtil {
	private Map<Color, Color> lightToDarkMap;
	private List<Color> darkThemeColors;
	private Color[] metalDarkGradient;
	private Color[] metalDarkMenuGradient;

	/**
	 * Constructor.
	 */
	public LaFUtil() {
		super();
		initMaps();
	}

	private void initMaps() {
		darkThemeColors = new ArrayList<Color>(12);
		darkThemeColors.add(DarkOceanTheme.DARK_FOREGROUND);
		darkThemeColors.add(DarkOceanTheme.DARK_BACKGROUND);
		darkThemeColors.add(DarkOceanTheme.DARK_PRIMARY1);
		darkThemeColors.add(DarkOceanTheme.DARK_PRIMARY2);
		darkThemeColors.add(DarkOceanTheme.DARK_PRIMARY3);
		darkThemeColors.add(DarkOceanTheme.DARK_SECONDARY1);
		darkThemeColors.add(DarkOceanTheme.DARK_SECONDARY2);
		darkThemeColors.add(DarkOceanTheme.DARK_SECONDARY3);
		darkThemeColors.add(DarkOceanTheme.DARK_CONTROL_TEXT_COLOR);
		darkThemeColors.add(DarkOceanTheme.DARK_INACTIVE_CONTROL_TEXT_COLOR);
		darkThemeColors.add(DarkOceanTheme.DARK_MENU_DISABLED_FOREGROUND);
		darkThemeColors.add(DarkOceanTheme.DARK_OCEAN_DROP);
		darkThemeColors.add(DarkOceanTheme.DARK_INACTIVE_TEXT_BACKGROUND);
		darkThemeColors.add(DarkOceanTheme.DARK_SELECTION_BACKGROUND);
		darkThemeColors.add(DarkOceanTheme.DARK_ACCELERATOR_COLOR);
		// the main colors of the Metal/Ocean theme, roughly as produced by 
		// toDarkColor with brightness minimum 0.16 and maximum 0.85, 
		// resulting in colors in a range (roughly) of 41,41,41 to 216,216,216
		lightToDarkMap = new HashMap<Color, Color>();
		lightToDarkMap.put(new Color(0, 0, 0), DarkOceanTheme.DARK_FOREGROUND);
		lightToDarkMap.put(new Color(51, 51, 51), new Color(182, 182, 182));
		lightToDarkMap.put(new Color(99, 130, 191), DarkOceanTheme.DARK_PRIMARY1);
		lightToDarkMap.put(new Color(122, 138, 153), DarkOceanTheme.DARK_SECONDARY1);
		lightToDarkMap.put(new Color(153, 153, 153), DarkOceanTheme.DARK_MENU_DISABLED_FOREGROUND);
		lightToDarkMap.put(new Color(163, 184, 204), DarkOceanTheme.DARK_PRIMARY2);
		lightToDarkMap.put(new Color(184, 207, 229), DarkOceanTheme.DARK_PRIMARY3);
		lightToDarkMap.put(new Color(200, 221, 242), new Color(41, 45, 50));
		lightToDarkMap.put(new Color(204, 204, 204), new Color(76, 76, 76));
		lightToDarkMap.put(new Color(218, 218, 218), DarkOceanTheme.DARK_INACTIVE_TEXT_BACKGROUND);
		lightToDarkMap.put(new Color(238, 238, 238), DarkOceanTheme.DARK_SECONDARY3);
		lightToDarkMap.put(new Color(255, 255, 255), DarkOceanTheme.DARK_BACKGROUND);
		
		// input [r=221,g=232,b=243], [r=255,g=255,b=255], [r=184,g=207,b=229] 
		// or    [r=200,g=221,b=242], [r=255,g=255,b=255], [r=184,g=207,b=229]
		metalDarkGradient = new Color[] {DarkOceanTheme.DARK_PRIMARY2, 
				DarkOceanTheme.DARK_INACTIVE_CONTROL_TEXT_COLOR, DarkOceanTheme.DARK_PRIMARY2};
		// menubar input [r=255,g=255,b=255], [r=218,g=218,b=218], [r=218,g=218,b=218]]
		metalDarkMenuGradient = new Color[] {DarkOceanTheme.DARK_BACKGROUND, 
				DarkOceanTheme.DARK_PRIMARY2, DarkOceanTheme.DARK_PRIMARY2};
	}

	/**
	 * Inverts the brightness of a color and forces it within certain extremes
	 * in order to prevent a very high contrast.
	 * 
	 * @param c the input color
	 * @return the converted color
	 */
    private Color toDarkColor(Color c) {
    	float nmin = 0.16f, nmax = 0.85f;
    	float nrange = nmax - nmin;
    	
    	float[] hsbVals = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
    	float newb = 1.0f - hsbVals[2];
    	newb = newb * nrange;
		newb = Math.min(nmax, nmin + newb);

    	return new Color(Color.HSBtoRGB(hsbVals[0], hsbVals[1], newb)); 	
    }
    
    private Color getDarkColor(String propertyKey, Color inputColor) {
    	Color outColor = lightToDarkMap.get(inputColor);
    	if (outColor == null) {
    		outColor = toDarkColor(inputColor);
    	}
    	
    	return outColor;
    }
    
    private Color[] getDarkColors(String propertyKey, List<Color> inputColors) {
    	if (propertyKey.equals("MenuBar.gradient")) {
    		return metalDarkMenuGradient;
    	} else if (propertyKey.toLowerCase().indexOf("gradient") > -1 && 
    			inputColors.size() == 3) {
    		return metalDarkGradient;
    	} else {
    		List<Color> convColors = new ArrayList<Color>(inputColors.size());
    		for (Color c : inputColors) {
    			convColors.add(toDarkColor(c));
    		}
    		return (Color[])convColors.toArray(new Color[0]);
    	}  	
    }

    /**
     * Changes the cross-platform "Metal" look and feel to a dark theme.
     * Partially (or initially) by converting each color resource in 
     * {@code UIDefaults} to a color with 'inverted' brightness. This is not
     * sufficient to change all elements, therefore a {@code DarkOceanTheme}
     * class has been created as well, using the same converted colors.
     */
	public void setDarkThemeMetal() {
		Map<Object, Object> metalDef = new HashMap<Object, Object>(UIManager.getDefaults());
		MetalLookAndFeel.setCurrentTheme(new DarkOceanTheme());
		// re-install the Metal Look and Feel after changing the theme
		try {
			UIManager.setLookAndFeel(new MetalLookAndFeel());
		} catch(UnsupportedLookAndFeelException ulf) {
		}

		// store the modified key-value pairs to add them at the end of the loops
        Map<String, Object> modifiedResourcesMap = new HashMap<String, Object>();
        
        // process all used Colors and ColorUIResources 
        UIDefaults curLFDefaults = UIManager.getDefaults();
        Map<Object, Object> uid = new HashMap<Object, Object>(curLFDefaults);
        Iterator<Object> keyIt = uid.keySet().iterator();
        while (keyIt.hasNext()) {
        	Object key = keyIt.next();
        	String keyString = key.toString();
        	Object value = curLFDefaults.get(keyString);

        	if (value instanceof ColorUIResource || value instanceof Color) {
        		if (value.equals(metalDef.get(keyString))) {// not changed, converted yet
        			if (lightToDarkMap.containsKey(value)) {
        				modifiedResourcesMap.put(keyString, lightToDarkMap.get(value));
        			} else {
        				Color cur = getDarkColor(keyString, (Color) value);
        				if (value instanceof ColorUIResource) {
        					cur = new ColorUIResource(cur);
        				}
        				lightToDarkMap.put((Color)value, cur);
        				modifiedResourcesMap.put(keyString, cur);
        			}
        		}
        	} else if (value instanceof List<?>) {
        		// gradients
        		List<?> valList = (List<?>) value;
        		List<Color> colEntries = new ArrayList<Color>(valList.size());
        		for (Object listEntry : valList) {
        			if (listEntry instanceof ColorUIResource || listEntry instanceof Color) {
        				colEntries.add((Color)listEntry);
        			}
        		}
        		if (colEntries.size() > 0) {
        			Color[] convColors = getDarkColors(keyString, colEntries);
        			if (convColors != null && convColors.length == colEntries.size()) {
        				List<Object> replList = new ArrayList<Object>();
        				int count = 0;
                		for (Object listEntry : valList) {
                			if (listEntry instanceof ColorUIResource || listEntry instanceof Color) {
                				if (count < convColors.length) {
                					replList.add(convColors[count]);
                					count++;
                				}
                			} else {
                				replList.add(listEntry);
                			}
                		}
                		modifiedResourcesMap.put(keyString, replList);
        			}
        		}
        	}
        }
        
        // manual adjustment of some colors for better results
        // the default, converted selected tab color is very dark and difficult to discern
        modifiedResourcesMap.put("TabbedPane.selected", DarkOceanTheme.DARK_SECONDARY1);
        // more accent for the menu bar border
        modifiedResourcesMap.put("MenuBar.borderColor", DarkOceanTheme.DARK_SECONDARY1);
        // default converted color for acceleratorForeground hardly visible, replaced by a lighter color
        modifiedResourcesMap.put("Menu.acceleratorForeground", DarkOceanTheme.DARK_ACCELERATOR_COLOR);
        modifiedResourcesMap.put("MenuItem.acceleratorForeground", DarkOceanTheme.DARK_ACCELERATOR_COLOR);
		modifiedResourcesMap.put("CheckBoxMenuItem.acceleratorForeground", DarkOceanTheme.DARK_ACCELERATOR_COLOR);
		modifiedResourcesMap.put("RadioButtonMenuItem.acceleratorForeground", DarkOceanTheme.DARK_ACCELERATOR_COLOR);
		// the table header background a bit lighter than the panel back ground
		modifiedResourcesMap.put("TableHeader.background", DarkOceanTheme.DARK_PRIMARY2);
		// slightly lighter background color for selected table rows
		modifiedResourcesMap.put("Table.selectionBackground",DarkOceanTheme.DARK_SELECTION_BACKGROUND);
		// lighter and more readable
		modifiedResourcesMap.put("ComboBox.disabledForeground", DarkOceanTheme.DARK_MENU_DISABLED_FOREGROUND);
		// makes the separator (e.g. in menus) more visible
		modifiedResourcesMap.put("Separator.foreground", DarkOceanTheme.DARK_INACTIVE_CONTROL_TEXT_COLOR);
		// slightly lighter background color for selected list rows
		modifiedResourcesMap.put("List.selectionBackground",DarkOceanTheme.DARK_PRIMARY2);
		modifiedResourcesMap.put("ProgressBar.selectionForeground", DarkOceanTheme.DARK_FOREGROUND);
		// colors for inactive text field and area
		modifiedResourcesMap.put("TextField.inactiveForeground", DarkOceanTheme.DARK_INACTIVE_CONTROL_TEXT_COLOR);
		modifiedResourcesMap.put("TextField.inactiveBackground", DarkOceanTheme.DARK_INACTIVE_TEXT_BACKGROUND);
		modifiedResourcesMap.put("TextArea.inactiveForeground", DarkOceanTheme.DARK_INACTIVE_CONTROL_TEXT_COLOR);
        
		modifiedResourcesMap.put("Tree.line", DarkOceanTheme.DARK_FOREGROUND);//?? doesn't seem to work
		modifiedResourcesMap.put("Tree.dropLineColor", DarkOceanTheme.DARK_FOREGROUND);//?? doesn't seem to work
		// this actually changes the tree line color, to make it lighter
		modifiedResourcesMap.put("Tree.hash", DarkOceanTheme.DARK_FOREGROUND);
		
		// make the border more visible
		modifiedResourcesMap.put("TitledBorder.border", new BorderUIResource.LineBorderUIResource(
				DarkOceanTheme.DARK_INACTIVE_CONTROL_TEXT_COLOR, 1));
		modifiedResourcesMap.put("PopupMenu.border", new BorderUIResource.LineBorderUIResource(
				DarkOceanTheme.DARK_INACTIVE_CONTROL_TEXT_COLOR, 1));
		
		modifiedResourcesMap.put("Slider.altTrackColor", DarkOceanTheme.DARK_INACTIVE_CONTROL_TEXT_COLOR);
		// slightly lighter selection backgrounds for some elements, maybe not really necessary
		modifiedResourcesMap.put("Tree.selectionBackground", DarkOceanTheme.DARK_SELECTION_BACKGROUND);
		modifiedResourcesMap.put("MenuItem.selectionBackground", DarkOceanTheme.DARK_SELECTION_BACKGROUND);
		modifiedResourcesMap.put("CheckBoxMenuItem.selectionBackground", DarkOceanTheme.DARK_SELECTION_BACKGROUND);
		modifiedResourcesMap.put("ComboBox.selectionBackground", DarkOceanTheme.DARK_SELECTION_BACKGROUND);
		modifiedResourcesMap.put("ProgressBar.selectionBackground", DarkOceanTheme.DARK_SELECTION_BACKGROUND);
		modifiedResourcesMap.put("RadioButtonMenuItem.selectionBackground", DarkOceanTheme.DARK_SELECTION_BACKGROUND);
		
		// selected text colors, foreground and background
		modifiedResourcesMap.put("TextField.selectionBackground", DarkOceanTheme.DARK_TEXT_SELECTION_BACKGROUND);
		modifiedResourcesMap.put("TextField.selectionForeground", DarkOceanTheme.DARK_BACKGROUND);
		modifiedResourcesMap.put("EditorPane.selectionBackground", DarkOceanTheme.DARK_TEXT_SELECTION_BACKGROUND);
		modifiedResourcesMap.put("EditorPane.selectionForeground", DarkOceanTheme.DARK_BACKGROUND);
		modifiedResourcesMap.put("FormattedTextField.selectionBackground", DarkOceanTheme.DARK_TEXT_SELECTION_BACKGROUND);
		modifiedResourcesMap.put("FormattedTextField.selectionForeground", DarkOceanTheme.DARK_BACKGROUND);
		modifiedResourcesMap.put("PasswordField.selectionBackground", DarkOceanTheme.DARK_TEXT_SELECTION_BACKGROUND);
		modifiedResourcesMap.put("PasswordField.selectionForeground", DarkOceanTheme.DARK_BACKGROUND);
		modifiedResourcesMap.put("TextPane.selectionBackground", DarkOceanTheme.DARK_TEXT_SELECTION_BACKGROUND);
		modifiedResourcesMap.put("TextPane.selectionForeground", DarkOceanTheme.DARK_BACKGROUND);
		modifiedResourcesMap.put("TextArea.selectionBackground", DarkOceanTheme.DARK_TEXT_SELECTION_BACKGROUND);
		modifiedResourcesMap.put("TextArea.selectionForeground", DarkOceanTheme.DARK_BACKGROUND);
		
        UIManager.getLookAndFeelDefaults().putAll(modifiedResourcesMap);

	}
	
	/**
	 * A dark theme extension of the {@code OceanTheme}.
	 */
	static class DarkOceanTheme extends OceanTheme {
		// in the Dark theme "BLACK" is actually white/light gray
	    private static final ColorUIResource DARK_FOREGROUND =
                new ColorUIResource(0xd8d8d8);//216, 216, 216
	    private static final ColorUIResource DARK_BACKGROUND =
                new ColorUIResource(0x292929);//41, 41, 41    
	    private static final ColorUIResource DARK_PRIMARY1 =
                new ColorUIResource(0x2c3a55);//44, 58, 85
		private static final ColorUIResource DARK_PRIMARY2 =
		        new ColorUIResource(0x3d454c);//61, 69, 76
		private static final ColorUIResource DARK_PRIMARY3 =
		        new ColorUIResource(0x2f353b);//47, 53, 59  
		private static final ColorUIResource DARK_SECONDARY1 =
		        new ColorUIResource(0x59646f);//89, 100, 111
		private static final ColorUIResource DARK_SECONDARY2 =
		        new ColorUIResource(0x2f353b);//47, 53, 59
		private static final ColorUIResource DARK_SECONDARY3 =
		        new ColorUIResource(0x353535);//53, 53, 53
	    private static final ColorUIResource DARK_CONTROL_TEXT_COLOR =
	    		new ColorUIResource(0xd8d8d8);//216, 216, 216
	    		//new ColorUIResource(0xb6b6b6);//182, 182, 182
	    		//new ColorUIResource(0x353535);//53, 53, 53
	    private static final ColorUIResource DARK_INACTIVE_CONTROL_TEXT_COLOR =
                new ColorUIResource(0x828282);//130,130,130
        private static final ColorUIResource DARK_MENU_DISABLED_FOREGROUND =
                new ColorUIResource(0x6f6f6f);//111, 111, 111
        private static final ColorUIResource DARK_OCEAN_DROP =
                new ColorUIResource(0x464e55);//[210, 233, 255]->70,78,85 
        // "new" colors
	    private static final ColorUIResource DARK_INACTIVE_TEXT_BACKGROUND =
                new ColorUIResource(0x424242);//66, 66, 66
	    private static final ColorUIResource DARK_SELECTION_BACKGROUND =
                new ColorUIResource(0x464f58);//70, 79, 88
	    private static final ColorUIResource DARK_ACCELERATOR_COLOR =
                new ColorUIResource(0xaaafb6);//170, 175, 182
	    private static final ColorUIResource DARK_TEXT_SELECTION_BACKGROUND =
                new ColorUIResource(0xd2e9ff);//210, 233, 255
	    
	    /**
	     * Black in a dark theme is actually white/light gray, renamed here
	     * to foreground.
	     */
		@Override
		protected ColorUIResource getBlack() {
			return DARK_FOREGROUND;
		}
		@Override
		public String getName() {
			return "Dark Ocean Theme";
		}
		@Override
		protected ColorUIResource getPrimary1() {
			return DARK_PRIMARY1;
		}
		@Override
		protected ColorUIResource getPrimary2() {
			return DARK_PRIMARY2;
		}
		@Override
		protected ColorUIResource getPrimary3() {
			return DARK_PRIMARY3;
		}
		@Override
		protected ColorUIResource getSecondary1() {
			return DARK_SECONDARY1;
		}
		@Override
		protected ColorUIResource getSecondary2() {
			return DARK_SECONDARY2;
		}
		@Override
		protected ColorUIResource getSecondary3() {
			return DARK_SECONDARY3;
		}
		@Override
		public ColorUIResource getDesktopColor() {
			return DARK_BACKGROUND;
		}
		@Override
		public ColorUIResource getInactiveControlTextColor() {
			return DARK_INACTIVE_CONTROL_TEXT_COLOR;
		}
		@Override
		public ColorUIResource getControlTextColor() {
			return DARK_CONTROL_TEXT_COLOR;
		}
		@Override
		public ColorUIResource getMenuDisabledForeground() {
			return DARK_MENU_DISABLED_FOREGROUND;
		}
		@Override
		protected ColorUIResource getWhite() {
			return DARK_BACKGROUND;
		}
		
	}
	
	/**
	 * Test dark theme for non-Metal Look and Feel implementations.
	 */
	public void setDarkThemeGeneric() {
		// all color resource related key - value pairs
        Map<String, Object> colorResourcesMap = new HashMap<String, Object>();
        // the same color resource is (or is possibly) used for multiple keys, 
        // replicate that approach
        //List<Color> colorResources = new ArrayList<Color>();
        // store the modified key-value pairs to add them at the end of the loops
        Map<String, Object> modifiedResourcesMap = new HashMap<String, Object>();
        // create a mapping from old colors to new colors
        Map<Color, Color> colorMap = new HashMap<Color, Color>();
        Map<Color, Color> gradientColorMap = new HashMap<Color, Color>();
        
        // collect all used Colors and ColorUIResources 
        UIDefaults curLFDefaults = UIManager.getLookAndFeelDefaults();
        Map<Object, Object> uid = new HashMap<Object, Object>(curLFDefaults);
        Iterator<Object> keyIt = uid.keySet().iterator();
        while (keyIt.hasNext()) {
        	Object key = keyIt.next();
        	String keyString = key.toString();
        	Object value = curLFDefaults.get(keyString);
        	if (value instanceof ColorUIResource || value instanceof Color) {
        		//System.out.println(keyString);
        		colorResourcesMap.put(keyString, value);
        		if (!colorMap.containsKey(value)) {
        			Color cur = (Color)value;
        			//colorResources.add(cur);
        			Color updateCUR = toDarkColor(cur);
                	if (cur instanceof ColorUIResource) {
                		updateCUR = new ColorUIResource(updateCUR);
                	}
                	colorMap.put(cur, updateCUR);
        		}
        	} else if (value instanceof List<?>) {
        		List<?> valList = (List<?>) value;
        		for (Object listEntry : valList) {
        			if (listEntry instanceof ColorUIResource || listEntry instanceof Color) {
        				if (!colorResourcesMap.containsKey(keyString)) {
        					colorResourcesMap.put(keyString, value);
        				}
                		if (!gradientColorMap.containsKey(listEntry)) {
                			Color cur = (Color)listEntry;
                			//colorResources.add(cur);
                			Color updateCUR = toDarkColor(cur);// could/should be a modified version
                        	if (cur instanceof ColorUIResource) {
                        		updateCUR = new ColorUIResource(updateCUR);
                        	}
                        	gradientColorMap.put(cur, updateCUR);
                		}
        			}
        		}
        	}
        }
        
        // create a mapping from old colors to new colors
       // Map<Color, Color> colorMap = new HashMap<Color, Color>();
        /*
        for (Color cur : colorResources) {
        	Color updateCUR = toDarkColor(cur);
        	if (cur instanceof ColorUIResource) {
        		updateCUR = new ColorUIResource(updateCUR);
        	}
        	colorMap.put(cur, updateCUR);
        }
        */
        for (Map.Entry<String, Object> entry : colorResourcesMap.entrySet()) {
        	String key = entry.getKey();
        	Object value = entry.getValue();
        	
        	if (value instanceof ColorUIResource || value instanceof Color) {
        		modifiedResourcesMap.put(key, colorMap.get(value));
        	} else if (value instanceof List<?>) {
        		List<?> valList = (List<?>) value;
        		List<Object> replList = new ArrayList<Object>();
        		for (Object listEntry : valList) {
        			if (listEntry instanceof ColorUIResource || listEntry instanceof Color) {
        				replList.add(gradientColorMap.get((Color)listEntry));
        			} else {
        				replList.add(listEntry);
        			}
        		}
        		modifiedResourcesMap.put(key, replList);
        	}
    	}
    	
        UIManager.getLookAndFeelDefaults().putAll(modifiedResourcesMap);
	}
}
