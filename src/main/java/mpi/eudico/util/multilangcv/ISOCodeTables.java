package mpi.eudico.util.multilangcv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A class that reads a {@code iso-639-3} tabular data file as distributed by
 * SIL (as the ISO authorized {@code ISO 639-3 Registration Authority} for this
 * standard).
 * Currently this is only used to get the corresponding {@code 639-1} 
 * 2-character code for a {@code 639-3} 3-character code (if it exists).
 * <p> 
 * This class could be extended to produce a list of {@link LangInfo} objects
 * as an alternative to the list produced in {@link LanguageCollection}, which
 * depends on a {@code CLARIN} registry service. The 3-character code could be
 * used as the {@code short id} while the {@code long id} could be constructed
 * with a URL prefix like this {@code https://iso639-3.sil.org/code/aaa}.
 * <p>
 * The main table is also available separately, but it is not clear if this
 * is a stable, permanent link: 
 * {@code https://iso639-3.sil.org/sites/iso639-3/files/downloads/iso-639-3.tab}
 * 
 * @author Han Sloetjes
 * @see <a href="https://iso639-3.sil.org/">iso639-3.sil.org</a>
 */
public class ISOCodeTables {
	// could use a SoftReference like in LanguageCollection
	private static Map<String, String> iso3To1Map;

	/**
	 * Private constructor.
	 */
	private ISOCodeTables() {
		super();
	}

	private static void readTables() {
		URL tableURL = ISOCodeTables.class.getResource("/iso-639-3.tab");
		if (tableURL != null) {
			BufferedReader reader = null;
			try {
				iso3To1Map = new LinkedHashMap<String, String>();
				reader = new BufferedReader(new InputStreamReader(
						tableURL.openStream(), StandardCharsets.UTF_8));
				String line = null;
				// could/should skip the first line, the header
				while ((line = reader.readLine()) != null) {
					String[] cols = line.split("\t");
					if (cols.length >= 4) {
						iso3To1Map.put(cols[0], cols[3]);
					}
				}

			} catch (IOException ex) {
				//
				ex.printStackTrace();
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (Throwable t) {}
				}
			}
		}
	}
	
	/**
	 * Returns the {@code iso-693-1} code for the specified {@code iso-693-3}
	 * code, or {@code null} if it does not exist.
	 *  
	 * @param part3Code a 3-character code
	 * @return a 2-character code or {@code null}
	 */
	public static String toPart1Code(String part3Code) {
		if (iso3To1Map == null) {
			readTables();
		}
		
		if (iso3To1Map != null) {
			return iso3To1Map.get(part3Code);
		}
		return null;
	}
	
	/**
	 * Returns the {@code iso-693-1} code for the specified {@code iso-693-3}
	 * code, or the {@code iso-693-3} input if there is no corresponding 
	 * 2-character code.
	 *  
	 * @param part3Code a 3-character code
	 * @return a 2-character code or the input 3-character code
	 */
	public static String checkPart1Code(String part3Code) {
		if (iso3To1Map == null) {
			readTables();
		}
		
		if (iso3To1Map != null) {
			String part1 = iso3To1Map.get(part3Code);
			if (part1 == null) {
				return part3Code;
			}
			return part1;
		}
		
		// the map could not be instantiated
		return part3Code;
	}
}
