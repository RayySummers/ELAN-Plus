package mpi.search.content.result.viewer;
/*
 * TODO this is a less desirable dependency on the client package
 */
import mpi.eudico.util.TimeFormatter;
import mpi.search.SearchLocale;

import mpi.search.content.result.model.ContentMatch;
import mpi.search.content.result.model.ContentResult;


/**
 * Transforms content search results to HTML.
 * 
 * @author klasal
 */
public class ContentResult2HTML {
	/**
	 * Private constructor.
	 */
    private ContentResult2HTML() {
		super();
	}

	/**
     * Appends match to StringBuilder with included highlight tags.
     * 
     * @param sb the builder to append to
     * @param match the match to add
     */
    public static void appendMatchValue(StringBuilder sb, ContentMatch match) {
        String s = match.getValue();
        int[][] highlights = match.getMatchedSubstringIndices();

        if ((highlights != null) &&
                ContentResult2HTML.arrayIsConsistent(s, highlights)) {
            String substring;

            if (highlights.length > 0) {
                for (int j = 0; j < highlights.length; j++) {
                    substring = s.substring((j == 0) ? 0 : highlights[j - 1][1],
                            highlights[j][0]);

                    sb.append(substring);

                    substring = s.substring(highlights[j][0], highlights[j][1]);
                    sb.append("<b>" + substring + "</b>");
                }

                sb.append(s.substring(highlights[highlights.length - 1][1]));
            } else {
                sb.append(s);
            }
        } else {
            sb.append(s);
        }
    }

    /**
     * Appends all matches of a result as HTML to a StringBuilder.
     * 
     * @param sb the builder to append to
     * @param result the result to add
     */
    public static void appendResultAsTable(StringBuilder sb, ContentResult result) {
        sb.append("<table border=\"1\" rules=\"all\" cellpadding=\"3\">\n");
        sb.append("<thead><tr><th>" + SearchLocale.getString("Search.Table.Count") + "</th><th>" +
            SearchLocale.getString("Search.Annotation_SG") +
            "</th><th>" + SearchLocale.getString("Search.Table.BeginTime") + "</th><th>" +
            SearchLocale.getString("Search.Table.EndTime") + "</th><th>" +
            SearchLocale.getString("Search.Table.Duration") + "</th></tr></thead>\n<tbody>");

        for (int i = 0; i < result.getRealSize(); i++) {
            ContentMatch match = (ContentMatch) result.getMatch(i + 1);
            sb.append("<tr>");
            sb.append("<td align=\"right\">" + (i + 1) + "</td><td>");
            appendMatchValue(sb, match);
            sb.append("</td><td align=\"right\">" +
                TimeFormatter.toSSMSString(match.getBeginTimeBoundary()) +
                "</td><td align=\"right\">" +
                TimeFormatter.toSSMSString(match.getEndTimeBoundary()) +
                "</td><td align=\"right\">" +
                TimeFormatter.toSSMSString(match.getEndTimeBoundary() -
                    match.getBeginTimeBoundary()) + "</td>");
            sb.append("</tr>\n");
        }

        sb.append("</tbody>\n</table>\n");
    }

    /**
     * Checks if highlights are in ascending order and within string size
     * 
     * @param s the string to check against
     * @param highlights the highlights array
     */
    private static boolean arrayIsConsistent(String s, int[][] highlights) {
        for (int j = 0; j < highlights.length; j++) {
            if ((highlights[j][0] < 0) ||
                    (highlights[j][0] > highlights[j][1]) ||
                    (highlights[j][1] > s.length())) {
                return false;
            }
        }

        return true;
    }
}
