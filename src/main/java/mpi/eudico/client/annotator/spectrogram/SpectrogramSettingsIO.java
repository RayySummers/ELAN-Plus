package mpi.eudico.client.annotator.spectrogram;

import java.awt.Color;

import mpi.eudico.client.annotator.Preferences;
import nl.mpi.media.spectrogram.SpectrogramSettings;

/**
 * A class for storing and loading of settings and user preferences concerning
 * spectrogram visualization.
 */
public class SpectrogramSettingsIO {
	
	/**
	 * Stores spectrogram settings as global preferences.
	 * 
	 * @param specSettings current settings, not {@code null}
	 */
	public static void storeSpectrogramSettings(SpectrogramSettings specSettings) {
		// store settings that deviate from the default setting, 
		// first retrieve already stored settings, remove settings that have been reset
		SpectrogramSettings defSettings = new SpectrogramSettings();
		
		if (specSettings.getMinDisplayFrequency() != defSettings.getMinDisplayFrequency()) {
			Preferences.set("SpectrogramViewer.MinDisplayedFrequency", specSettings.getMinDisplayFrequency(), 
					null, false, false);
		} else {
			Preferences.set("SpectrogramViewer.MinDisplayedFrequency", null, null, false, false);
		}
		
		if (specSettings.getMaxDisplayFrequency() != defSettings.getMaxDisplayFrequency()) {
			Preferences.set("SpectrogramViewer.MaxDisplayedFrequency", specSettings.getMaxDisplayFrequency(),
					null, false, false);
		} else {
			Preferences.set("SpectrogramViewer.MaxDisplayedFrequency", null, null, false ,false);
		}
		
		if (specSettings.getColorScheme() != defSettings.getColorScheme()) {
			Preferences.set("SpectrogramViewer.ColorScheme", specSettings.getColorScheme().toString(), null);
			if (specSettings.getColorScheme() == SpectrogramSettings.COLOR_SCHEME.BI_COLOR) {
				Preferences.set("SpectrogramViewer.ColorScheme.FG", specSettings.getColor1(),
						null, false, false);
				Preferences.set("SpectrogramViewer.ColorScheme.BG", specSettings.getColor2(), 
						null, false, false);
			}
		} else {
			Preferences.set("SpectrogramViewer.ColorScheme", null, null, false, false);
			// remove bi-color colors?
		}
		
		if (specSettings.isAdaptiveContrast() != defSettings.isAdaptiveContrast()) {
			Preferences.set("SpectrogramViewer.AdaptiveContrast", specSettings.isAdaptiveContrast(), 
					null, false, false);
		} else {
			Preferences.set("SpectrogramViewer.AdaptiveContrast", null, null, false ,false);
		}
		
		if (specSettings.getLowerValueAdjustment() != defSettings.getLowerValueAdjustment()) {
			Preferences.set("SpectrogramViewer.Brightness.Lower", specSettings.getLowerValueAdjustment(), 
					null, false, false);
		} else {
			Preferences.set("SpectrogramViewer.Brightness.Lower", null, null, false, false);
		}
		
		if (specSettings.getUpperValueAdjustment() != defSettings.getUpperValueAdjustment()) {
			Preferences.set("SpectrogramViewer.Brightness.Upper", specSettings.getUpperValueAdjustment(), 
					null, false, false);
		} else {
			Preferences.set("SpectrogramViewer.Brightness.Upper", null, null, false, false);
		}
		
		if (!specSettings.getWindowFunction().equals(defSettings.getWindowFunction())) {
			Preferences.set("SpectrogramViewer.WindowFunction", specSettings.getWindowFunction(), 
					null, false, false);
		} else {
			Preferences.set("SpectrogramViewer.WindowFunction", null, null, false, false);
		}
		
		if (specSettings.getWindowDurationSec() != defSettings.getWindowDurationSec()) {
			Preferences.set("SpectrogramViewer.WindowDuration", specSettings.getWindowDurationSec(),
					null, false, false);
		} else {
			Preferences.set("SpectrogramViewer.WindowDuration", null, null, false, false);
		}
		
		if (specSettings.getStrideDurationSec() != defSettings.getStrideDurationSec()) {
			Preferences.set("SpectrogramViewer.StrideDuration", specSettings.getStrideDurationSec(), 
					null, false, false);
		} else {
			Preferences.set("SpectrogramViewer.StrideDuration", null, null, false, false);
		}
	}

	/**
	 * Creates a new settings instance and loads stored preferences into it.
	 * 
	 * @return a settings object with stored preferred settings
	 */
	public static SpectrogramSettings loadSpectrogramSettings() {
		SpectrogramSettings specSettings = new SpectrogramSettings();
		
		SpectrogramSettingsIO.loadSpectrogramSettings(specSettings);
		
		return specSettings;
	}
	
	/**
	 * Loads stored preferences into an existing settings instance. 
	 * 
	 * @param specSettings an existing settings object, not {@code null}
	 */
	public static void loadSpectrogramSettings(SpectrogramSettings specSettings) {
		Double prefDouble = Preferences.getDouble("SpectrogramViewer.MinDisplayedFrequency", null);
		if (prefDouble != null) {
			specSettings.setMinDisplayFrequency(prefDouble.doubleValue());
		}
		
		prefDouble = Preferences.getDouble("SpectrogramViewer.MaxDisplayedFrequency", null);
		if (prefDouble != null) {
			specSettings.setMaxDisplayFrequency(prefDouble.doubleValue());
		}
		
		String prefString = Preferences.getString("SpectrogramViewer.ColorScheme", null);
		if (prefString != null) {
			if (prefString.equals(SpectrogramSettings.COLOR_SCHEME.REVERSED_GRAY.toString())) {
				specSettings.setColorScheme(SpectrogramSettings.COLOR_SCHEME.REVERSED_GRAY);
			} else if (prefString.equals(SpectrogramSettings.COLOR_SCHEME.BI_COLOR.toString())) {
				specSettings.setColorScheme(SpectrogramSettings.COLOR_SCHEME.BI_COLOR);
			} 
			// unlikely case as long as this is the default setting
			else if (prefString.equals(SpectrogramSettings.COLOR_SCHEME.GRAY.toString())) {
				specSettings.setColorScheme(SpectrogramSettings.COLOR_SCHEME.GRAY);
			}			
		}
		// load colors regardless of preferred color scheme
		Color prefColor = Preferences.getColor("SpectrogramViewer.ColorScheme.FG", null);
		if (prefColor != null) {
			specSettings.setColor1(prefColor);
		}
		prefColor = Preferences.getColor("SpectrogramViewer.ColorScheme.BG", null);
		if (prefColor != null) {
			specSettings.setColor2(prefColor);
		}
		
		Boolean prefBool = Preferences.getBool("SpectrogramViewer.AdaptiveContrast", null);
		if (prefBool != null) {
			specSettings.setAdaptiveContrast(prefBool.booleanValue());
		}
		
		prefDouble = Preferences.getDouble("SpectrogramViewer.Brightness.Lower", null);
		if (prefDouble != null) {
			specSettings.setLowerValueAdjustment(prefDouble.doubleValue());
		}
		
		prefDouble = Preferences.getDouble("SpectrogramViewer.Brightness.Upper", null);
		if (prefDouble != null) {
			specSettings.setUpperValueAdjustment(prefDouble.doubleValue());
		}
		
		prefString = Preferences.getString("SpectrogramViewer.WindowFunction", null);
		if (prefString != null) {
			specSettings.setWindowFunction(prefString);
		}
		
		prefDouble = Preferences.getDouble("SpectrogramViewer.WindowDuration", null);
		if (prefDouble != null) {
			specSettings.setWindowDurationSec(prefDouble.doubleValue());
		}
		
		prefDouble = Preferences.getDouble("SpectrogramViewer.StrideDuration", null);
		if (prefDouble != null) {
			specSettings.setStrideDurationSec(prefDouble.doubleValue());
		}
	}
}
