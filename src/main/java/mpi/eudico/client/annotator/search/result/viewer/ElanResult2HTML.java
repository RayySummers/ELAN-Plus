package mpi.eudico.client.annotator.search.result.viewer;

import mpi.eudico.client.annotator.search.result.model.ElanMatch;

import mpi.search.SearchLocale;

import mpi.search.content.result.model.ContentMatch;
import mpi.search.content.result.model.ContentResult;
import mpi.search.content.result.viewer.ContentResult2HTML;


/**
 * There is no class ElanResult; it is merely assumed that a ContentResult
 * contains ElanMatches.
 *
 * @author klasal
 */
public class ElanResult2HTML {
    private static final int maxVisibleChildren = 5;

    /**
     * list style fragment
     */
    public static final String matchListStyle = "ul { list-style-type:none;}\n";
    private static final String css = "<style type=\"text/css\">" +
        matchListStyle + "<style>";

    /**
     * Private constructor.
     */
    private ElanResult2HTML() {
		super();
	}

	/**
     * Appends a match converted to {@code HTML} to a {@code StringBuilder}.
     *
     * @param sb the builder to append to
     * @param rootMatch the root match
     * @param withChildren if {@code true} append children as well
     * @param withCSS if {@code true} add and apply {@code CSS} information
     */
    public static void appendMatch(StringBuilder sb, ContentMatch rootMatch,
        boolean withChildren, boolean withCSS) {
        ContentResult2HTML.appendMatchValue(sb, rootMatch);

        if (rootMatch instanceof ElanMatch && withChildren) {
            addChildren(sb, (ElanMatch) rootMatch, withCSS);
        }
    }

    /**
     * Appends content results as a tree.
     *
     * @param sb the builder to append to
     * @param result the result to append
     */
    public static void appendResultAsTree(StringBuilder sb, ContentResult result) {
        for (int i = 0; i < result.getRealSize(); i++) {
            ElanResult2HTML.appendMatch(sb, (ElanMatch) result.getMatch(i + 1),
                true, true);
            sb.append("<br>\n");
        }
    }

    /**
     * Translates a match with or without children to a string.
     * 
     * @param rootMatch root respectively anchor match
     * @param withChildren {@code true} if children matches should be included
     *
     * @return a {@code HTML} representation of match
     */
    public static String translate(ContentMatch rootMatch, boolean withChildren) {
        return translate(rootMatch, withChildren, false);
    }

    /**
     * Translates a match with or without children to a string.
     *
     * @param rootMatch root respectively anchor match
     * @param withChildren {@code true} if children matches should be included
     * @param withCSS {@code true} if resulting {@code HTML} is to be interpreted
     *  by a browser (as opposed to visualization in Java components)
     *
     * @return a {@code HTML} representation of a match
     */
    public static String translate(ContentMatch rootMatch,
        boolean withChildren, boolean withCSS) {
        StringBuilder sb = new StringBuilder("<HTML>\n");

        if (withCSS) {
            sb.append("<HEAD>" + css + "</HEAD>\n");
        }

        sb.append("<BODY>\n");

        appendMatch(sb, rootMatch, withChildren, withCSS);

        sb.append("\n</BODY>\n</HTML>");

        return sb.toString();
    }

    /**
     * within java components, it isn't possible to turn off the marker of a
     * list item with css; as a workaround, this method applies tag BR instead
     * of LI; For an export to an html file (to be read by a browser), the use
     * of css is recommended.
     *
     * @param sb string builder
     * @param parentMatch the parent match
     * @param withCSS whether to use {@code CSS} in the output
     */
    private static void addChildren(StringBuilder sb, ElanMatch parentMatch,
        boolean withCSS) {
        if (parentMatch.getChildCount() > 0) {
            sb.append("<ul>");

            String lastConstraintId = null;

            for (int i = 0; i < parentMatch.getChildCount(); i++) {
                if (i >= maxVisibleChildren) {
                    sb.append("... (" +
                        (parentMatch.getChildCount() - maxVisibleChildren) +
                        " " + SearchLocale.getString("Search.More") + ")");

                    break;
                }

                ElanMatch childMatch = (ElanMatch) parentMatch.getChildAt(i);

                if ((lastConstraintId != null) &&
                        !(childMatch.getConstraintId().equals(lastConstraintId))) {
                    sb.append("</ul><ul>");

                    lastConstraintId = childMatch.getConstraintId();
                }

                //if (withCSS) {
                    sb.append("<li>");
                //}

                ContentResult2HTML.appendMatchValue(sb, childMatch);
                // append tier name of child??
                sb.append("  (" + childMatch.getTierName() + ")");

                addChildren(sb, childMatch, withCSS);

                //if (withCSS) {
                    sb.append("</li>");
                //} else {
                    //if (!withCSS){
                    //	sb.append("<br>\n");
                   // }                   
                //}
            }

            sb.append("</ul>");
        }
    }
}
