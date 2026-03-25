/* This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package mpi.search.content.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import mpi.eudico.util.CVEntry;


/**
 * This interface describes Corpus-specific Types and Relations of tiers and possible
 * units of distance between them. Both Tiers and Units are stored in Arrays of
 * FieldTypes, witch contain a full name and a mnemonic, resp.
 *
 * @author klasal
 */
public abstract class CorpusType {
    /** Holds value of short names of units */
    protected final Map<String, String> unitMnemonics = new HashMap<String, String>();
    /** the frame title */
    protected String frameTitle;
    /** coarser unit 1 */
    protected String coarserUnit1 = null;
    /** coarser unit 2 */
    protected String coarserUnit2 = null;
    /** coarser unit 3 */
    protected String coarserUnit3 = null;
    /** standard unit */
    protected String standardUnit = null;
    /** array of tier names */
    protected String[] tierNames;

    /**
     * Creates a new corpus type instance.
     */
    public CorpusType() {
		super();
	}

	/**
     * Returns a fixed, hard-wired list of Tier-Types.
     *
     * @return the tier names
     */
    public String[] getTierNames() {
        return tierNames;
    }

    /**
     * Returns index tier names.
     * 
     * @return an array of index tier names
     */
    public abstract String[] getIndexTierNames();

    /**
     * Returns a mnemonic (used by the {@code perl/python} script) for a unit.
     *
     * @param unit the unit
     *
     * @return the mnemonic
     */
    public String getUnitMnemonic(String unit) {
        return unitMnemonics.containsKey(unit) ? (String) unitMnemonics.get(unit) : null;
    }

    /**
     * Returns whether the search can be executed over multiple tiers.
     *  
     * @return true if search over multiple tiers is supported
     */
    public abstract boolean allowsSearchOverMultipleTiers();

    /**
     * Returns the unit string for a specified mnemonic.
     *
     * @param mnemonic the mnemonic to get the unit for
     *
     * @return the unit
     */
    public String getUnitFromMnemonic(String mnemonic) {
//        for (String key : unitMnemonics.keySet()) {
//
//            if (unitMnemonics.get(key).equals(mnemonic)) {
//                return key;
//            }
//        }
      for (Map.Entry<String, String> e : unitMnemonics.entrySet()) {

          if (e.getValue().equals(mnemonic)) {
              return e.getKey();
          }
      }

        return null;
    }

    /**
     * Returns the full tier name for the specified (short) tier name.
     * 
     * @param tierName the tier name
     *
     * @return unabbreviatedTierName or {@code null}
     */
    public String getUnabbreviatedTierName(String tierName) {
        return Arrays.asList(tierNames).contains(tierName) ? tierName : null;
    }

    /**
     * Returns a title for the Search-Frame
     *
     * @return the title for the search frame
     */
    public String getFrameTitle() {
        return frameTitle;
    }

    /**
     * Returns, if exists, a list of closed vocabulary corresponding to a tier; Otherwise
     * null.
     *
     * @param tierName the tier to find the CV for
     *
     * @return a list of CV entries
     */
    public abstract List<CVEntry> getClosedVoc(String tierName);

    /**
     * Returns whether the specified tier is linked to a closed or controlled
     * vocabulary.
     *  
     * @param tierName the tier
     *
     * @return {@code true} if there is a "closed vocabulary" for this fieldType,
     * {@code false} otherwise
     */
    public abstract boolean isClosedVoc(String tierName);

    /**
     * Returns true if a "Closed Vocabulary" should be editable (thus not really
     * closed).
     *
     * @param closedVoc the list of entries
     *
     * @return {@code true} if the list is editable
     */
    public abstract boolean isClosedVocEditable(List<CVEntry> closedVoc);

    /**
     * Returns the default Locale of a Field Type
     *
     * @param tierName the tier
     *
     * @return the {@code Locale} or {@code null}
     */
    public abstract Locale getDefaultLocale(String tierName);

    /**
     * Returns all tiers with the same root tier as the specified tier.
     *
     * @param tierName the tier
     *
     * @return array of tier Names
     */
    public abstract String[] getRelatedTiers(String tierName);

    /**
     * Returns the possible units, which fieldType1 and fieldType2 both can be measured
     * with
     *
     * @param tierName1 first tier
     * @param tierName2 second tier
     *
     * @return an array of possible units
     */
    public String[] getPossibleUnitsFor(String tierName1, String tierName2) {
        List<String> list = new ArrayList<String>();

        if (standardUnit != null) {
            list.add(standardUnit);
        }

        if (coarserUnit1 != null) {
            list.add(coarserUnit1);
        }

        if (coarserUnit2 != null) {
            list.add(coarserUnit2);
        }

        if (coarserUnit3 != null) {
            list.add(coarserUnit3);
        }

        return (String[]) list.toArray(new String[0]);
    }

    /**
     * Some tiers might be obligatory case sensitive (pho)
     *
     * @param tierName the tier
     *
     * @return the case sensitive flag
     */
    public abstract boolean strictCaseSensitive(String tierName);

    /**
     * Returns the default unit
     *
     * @return default unit
     */
    public String getDefaultUnit() {
        return standardUnit;
    }

    /**
     * Returns whether this type has attributes.
     * 
     * @return {@code true} if this type has attributes
     */
    public abstract boolean hasAttributes();

    /**
     * Returns attribute names for the specified tier.
     * 
     * @param tierName the tier
     *
     * @return an array of attribute names
     */
    public abstract String[] getAttributeNames(String tierName);

    /**
     * Returns a short description of an attribute.
     * 
     * @param attributeName the attribute
     *
     * @return the tooltip text
     */
    public abstract String getToolTipTextForAttribute(String attributeName);

    /**
     * Returns whether quantifier {@code NO} is supported.
     * 
     * @return {@code true} if {@code NO} quantifier is allowed
     */
    public abstract boolean allowsQuantifierNO();
    
    /**
     * Returns whether temporal constraints are supported.
     * 
     * @return {@code true} if temporal constraints are allowed
     */
    public abstract boolean allowsTemporalConstraints();
    
    /**
     * Returns possible attribute values for a tier and an attribute name.
     * 
     * @param tierName the tier name
     * @param attributeName the attribute name
     *
     * @return the attribute values
     */
    public abstract Object getPossibleAttributeValues(
        String tierName, String attributeName);

    /**
     * Returns the class of an input method for this corpus type.
     * 
     * @return the input method class or {@code null}
     */
    public abstract Class getInputMethodClass();
}
