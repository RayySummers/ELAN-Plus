package mpi.search.content.query.model;

import mpi.search.content.model.CorpusType;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


/**
 * A search utilities class.
 * 
 * @author klasal
 */
public class Utilities {
	/**
	 * Private constructor.
	 */
    private Utilities() {
		super();
	}

	/**
     * Adapts regular expression corresponding to {@code constraint.isRegex} 
     * and {@code constraint.isCaseSensitive}.
     *
     * @param constraint the constraint 
     * @param type the corpus type
     * @return Pattern the configured pattern
     */
    public static final Pattern getPattern(Constraint constraint,
        CorpusType type) throws PatternSyntaxException {
        Pattern pattern = null;
        String regex = constraint.getPattern();
        boolean emptyString = regex.isEmpty();

        if (!constraint.isRegEx() && // ||
                type.isClosedVoc(constraint.getTierName())) {
        	// HS 03-2012 added additional check so that it is possible to search tiers with a CV and 
        	// use regular expressions without escaping
            regex = Pattern.quote(regex);
        }

        if (!constraint.isRegEx()) {
            if (emptyString) {
                // match every non-empty 'word' (substring without spaces)
                regex = "\\b\\S+?\\b";
            } else {
                regex = "\\b" + regex + "\\b";
            }
        }

        int flag = constraint.isCaseSensitive() ? Pattern.UNICODE_CHARACTER_CLASS : 
        	Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS;
        pattern = Pattern.compile(regex, flag);

        return pattern;
    }
}
