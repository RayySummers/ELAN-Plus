package mpi.eudico.client.annotator.interannotator;

/**
 * An interface defining constants for usage as step and preferences keys for inter-annotator agreement calculation wizard.
 *
 * @author Han Sloetjes
 */
public interface CompareConstants {

    /**
     * An enumeration of available algorithms.
     *
     * @author Han Sloetjes
     */
    enum METHOD {
        /**
         * classic
         */
        CLASSIC("Classic"),
        /**
         * modified kappa
         */
        MOD_KAPPA("Modified Kappa"),
        /**
         * staccato
         */
        STACCATO("Staccato"),
        /**
         * modified fleiss
         */
        MOD_FLEISS("Modified Fleiss");

        private final String value;

        METHOD(String val) {
            value = val;
        }

        @Override
        public String toString() {
            return getValue();
        }

        /**
         * value
         */
        public String getValue() {
            return value;
        }
    }

    /**
     * An enumeration of File and Tier matching constants
     *
     * @author Han Sloetjes
     */
    enum MATCHING {
        /**
         * manual
         */
        MANUAL("Manual"),
        /**
         * affix
         */
        AFFIX("Affix based"),
        /**
         * suffix
         */
        SUFFIX("Suffix"),
        /**
         * prefix
         */
        PREFIX("Prefix"),
        /**
         * same name
         */
        SAME_NAME("Same name");

        private final String value;

        MATCHING(String val) {
            this.value = val;
        }

        @Override
        public String toString() {
            return value;
        }

        /**
         * value
         */
        public String getValue() {
            return value;
        }
    }

    /**
     * An enumeration of File matching constants
     *
     * @author Han Sloetjes
     */
    enum FILE_MATCHING {
        /**
         * Current document
         */
        CURRENT_DOC("In current document"),
        /**
         * in same file
         */
        IN_SAME_FILE("In same file"),
        /**
         * across files
         */
        ACROSS_FILES("Across files");

        private final String value;

        FILE_MATCHING(String val) {
            this.value = val;
        }

        @Override
        public String toString() {
            return value;
        }

        /**
         * value identifier
         */
        public String getValue() {
            return value;
        }
    }

    // preferences keys
    /**
     * method key constant
     */
    String METHOD_KEY = "Compare.CompareMethod";
    /**
     * file match key constant
     */
    String FILE_MATCH_KEY = "Compare.FileMatching";
    /**
     * tier match key constant
     */
    String TIER_MATCH_KEY = "Compare.TierMatching";
    /**
     * file separator key constant
     */
    String FILE_SEPARATOR_KEY = "Compare.FileSeparator";
    /**
     * tier separator key constant
     */
    String TIER_SEPARATOR_KEY = "Compare.TierSeparator";
    /**
     * tier source key constant
     */
    String TIER_SOURCE_KEY = "Compare.TierSource";
    /**
     * selected files key constant
     */
    String SEL_FILES_KEY = "Compare.SelectedFiles";
    /**
     * tier name1 key constant
     */
    String TIER_NAME1_KEY = "Compare.TierName1";
    /**
     * tier name2 key constant
     */
    String TIER_NAME2_KEY = "Compare.TierName2";
    /**
     * selected tier names constant
     */
    String TIER_NAMES_KEY = "Compare.SelectedTierNames";
    /**
     * all tier names constant
     */
    String ALL_TIER_NAMES_KEY = "Compare.AllTierNames";
    /**
     * overlap percentage constant
     */
    String OVERLAP_PERCENTAGE = "Compare.OverlapPercentage";
    /**
     * overlap average key constant
     */
    String OVERLAP_AVERAGE = "Compare.OverlapAveragePercentage";
    /**
     * output per file constant
     */
    String OUTPUT_PER_FILE = "Compare.Output.PerFile";
    /**
     * output per tier pair
     */
    String OUTPUT_PER_TIER_PAIR = "Compare.Output.PerTierPair";
    /**
     * output table values constant
     */
    String OUTPUT_TABLES_VALUES = "Compare.Output.TablesOfValues";
    /**
     * monte carlo simulation constant
     */
    String MONTE_CARLO_SIM = "Compare.MonteCarloSimulations";
    /**
     * number of nominations constant
     */
    String NUM_NOMINATIONS = "Compare.NumberOfNominations";
    /**
     * null hypothesis value constant
     */
    String NULL_HYPOTHESIS = "Compare.NullHypothesis.Value";
    /**
     * group compare key constant
     */
    String GROUP_COMPARE_KEY = "Compare.GroupWise"; // compare >=2 tiers
    /**
     * export match tiers constant
     */
    String EXPORT_MATCHING_TIERS_KEY = "Compare.ExportMatchingTiers";
    /**
     * export tiers method key constant
     */
    String EXPORT_TIERS_METHOD_KEY = "Compare.ExportTiersMethod";
    /**
     * export tiers per tier set constant
     */
    String EXPORT_TIERS_PERTIERSET = "Compare.ExportPerTierSet";
    /**
     * export tiers per file constant
     */
    String EXPORT_TIERS_PERFILE = "Compare.ExportPerFile";
    /**
     * export folder key constant
     */
    String EXPORT_FOLDER_KEY = "Compare.ExportFolder";

    // constant for unmatched or unlinked annotations
    /**
     * unmatched constant
     */
    String UNMATCHED = "Unmatched";
}
