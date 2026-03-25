package mpi.eudico.client.annotator.search.model;

import java.util.List;
import java.util.Locale;

import mpi.eudico.client.annotator.search.query.viewer.EAFPopupMenu;
import mpi.eudico.util.CVEntry;
import mpi.search.content.model.CorpusType;


/**
 * Generic EAF-Implementation of the SearchableType-Interface. Aimed at
 * describing a minimal set of tiers present in every eaf-file; relevant when
 * searching through different eaf-files.
 *
 * @author Alexander Klassmann
 * @version april 2004
 */
public class EAFType extends CorpusType {
    /**
     * Creates a new EAFType object.
     */
    public EAFType() {
        frameTitle = "Elan Search";
    }

    /**
     * So far eaf tiers don't contain attributes (relevant for search).
     *
     * @param tierName name of tier with attributes
     *
     * @return dummy, empty String array
     */
    @Override
	public String[] getAttributeNames(String tierName) {
        return new String[0];
    }

    /**
     * Returns the closed vocabulary for a tier.
     *
     * @param tierName tier name
     *
     * @return list of ControlledVocabulary entries or {@code null} (the default)
     */
    @Override
	public List<CVEntry> getClosedVoc(String tierName) {
        return null;
    }

    /**
     * Returns whether a tier is connected to a Controlled Vocabulary.
     *
     * @param tierName tier name
     *
     * @return {@code true} if only a closed vocabulary is allowed for annotations values of the tier
     */
    @Override
	public boolean isClosedVoc(String tierName) {
        return false;
    }

    /**
     * Don't allow to change closed vocabularies within a query.
     *
     * @param closedVoc closed vocabulary
     *
     * @return always {@code false}
     */
    @Override
    public boolean isClosedVocEditable(List<CVEntry> closedVoc) {
//        return true;
        return false;
    }

    /**
     * Returns the default locale for a tier (used for input methods).
     *
     * @param tierName tier name
     *
     * @return Locale for the tier (e.g. Chinese, IPA, etc), {@code null} by default
     */
    @Override
	public Locale getDefaultLocale(String tierName) {
        return null;
    }

    /**
     * Determines the unit which should be preselected in GUI.
     *
     * @return the standard unit
     */
    @Override
	public String getDefaultUnit() {
        return standardUnit;
    }

    /**
     * In COREX some tiers contain an index, e.g. an id for a lexical entry.
     *
     * @return names of tiers that contain an index; always empty for eaf
     */
    @Override
	public String[] getIndexTierNames() {
        return new String[0];
    }

    /**
     * Returns a popup to specify input methods;
     * if case of eaf, it concerns the locale of the input GUI.
     *
     * @return the Class of the Popup menu
     */
    @Override
	public Class getInputMethodClass() {
        return EAFPopupMenu.class;
    }

    /**
     * In COREX, some tiers have attributes with a closed set of values.
     *
     * @param tierName name of tier
     * @param attributeName name of attribute
     *
     * @return always {@code null} in eaf
     */
    @Override
	public Object getPossibleAttributeValues(String tierName,
        String attributeName) {
        return null;
    }

    /**
     * Returns an array of related tiers.
     *
     * @param tierName the name of the tier
     *
     * @return an array of tier names
     */
    @Override
	public String[] getRelatedTiers(String tierName) {
        return tierNames;
    }

    /**
     * Returns a tooltip text.
     *
     * @param attributeName the name of the attribute
     *
     * @return always the empty String
     */
    @Override
	public String getToolTipTextForAttribute(String attributeName) {
        return "";
    }

    /**
     * Returns whether the search supports the 'NO' quantifier.
     *
     * @return always {@code true} for eaf
     */
    @Override
	public boolean allowsQuantifierNO() {
        return true;
    }

    /**
     * Returns whether search over multiple tiers is supported.
     *
     * @return {@code true} for eaf
     */
    @Override
	public boolean allowsSearchOverMultipleTiers() {
        return true;
    }

    /**
     * Returns whether temporal constraints are supported in the query.
     *
     * @return {@code true} for eaf
     */
    @Override
	public boolean allowsTemporalConstraints() {
        return true;
    }

    /**
     * Returns whether there are specific types of attributes.
     *
     * @return {@code false} by default
     */
    @Override
	public boolean hasAttributes() {
        return false;
    }

    /**
     * Returns whether search on a tier is strictly case sensitive.
     *
     * @param tierName name of the tier
     *
     * @return {@code false} by default
     */
    @Override
	public boolean strictCaseSensitive(String tierName) {
        return false;
    }
}
