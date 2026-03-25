package mpi.eudico.client.annotator.search.viewer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;

import mpi.eudico.client.annotator.search.result.viewer.ElanResult2HTML;
import mpi.search.SearchLocale;
import mpi.search.content.query.model.ContentQuery;
import mpi.search.content.query.viewer.Query2HTML;
import mpi.search.content.result.model.ContentResult;
import mpi.search.content.result.viewer.ContentResult2HTML;


/**
 * Converts an ELAN search query to {@code HTML}.
 */
public class ElanQuery2HTML {
    /* background colour same as default in elan constants */
    private static final String bodyStyle = "body { background-color: #E6E6E6; }\n";

    static final String css = "<style type=\"text/css\">\n" + bodyStyle +
        Query2HTML.bodyStyle + Query2HTML.constraintStyle +
        Query2HTML.patternStyle + ElanResult2HTML.matchListStyle +
        "</style>\n";

    /**
     * Private constructor.
     */
    private ElanQuery2HTML() {
		super();
	}

	/**
     * Exports a Query with its Result to an {@code HTML} file.
     *
     * @param query the query to export
     * @param exportFile the file to write to
     * @param asTable if {@code true}, export as table analogous to the table in the
     *        application;  if {@code false} export of matches in tree structure
     *        analogous to the tooltips of the annotation column
     * @param transcriptionFilePath the path to the transcription
     * @param encoding the encoding to use for the export
     *
     * @throws IOException any IO exception that could occur
     */
    public static void exportQuery(ContentQuery query, File exportFile,
        boolean asTable, String transcriptionFilePath, String encoding)
        throws IOException {
        if (exportFile == null) {
            return;
        }
        /* date format conform to ISO 8601 */
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd'T'hh:mmz");
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
        		new FileOutputStream(exportFile), encoding))) {
	        StringBuilder sb = new StringBuilder("<!doctype html><html>\n");
	        sb.append(
	            "<head profile=\"http://dublincore.org/documents/dcq-html/\">\n");
	        sb.append(
	            "<link rel=\"schema.DC\" href=\"http://purl.org/dc/elements/1.1/\" />\n");
	        sb.append(
	            "<link rel=\"schema.DCTERMS\" href=\"http://purl.org/dc/terms/\" />\n");
	        sb.append(
	            "<meta http-equiv=\"content-type\" content=\"text/html; charset=" +
	            encoding + "\" />\n");
	        sb.append("<meta name=\"DC.date\" content=\"" +
	            dateFormat.format(query.getCreationDate()) + "\" scheme=\"DCTERMS.W3CDTF\" />\n");
	        sb.append(
	            "<meta name=\"DC.description\" content=\"Query performed by ELAN on file " +
	            new File(transcriptionFilePath).getName() + "\" />\n");
	        sb.append(css);
	        sb.append("</head>\n");
	        sb.append("<body>\n");
	        sb.append("<h2>" + SearchLocale.getString("SearchDialog.Query") +
	            ":</h2>\n");
	        Query2HTML.appendQuery(sb, query);
	        sb.append("<br>\n");
	
	        String resultString = SearchLocale.getString("Search.Result");
	
	        //capitalize first letter
	        resultString = resultString.substring(0, 1).toUpperCase() +
	            resultString.substring(1);
	        sb.append("<h2>" + resultString + ":</h2>\n");
	
	        if (asTable) {
	            ContentResult2HTML.appendResultAsTable(sb,
	                (ContentResult) query.getResult());
	        } else {
	            ElanResult2HTML.appendResultAsTree(sb,
	                (ContentResult) query.getResult());
	        }
	
	        sb.append("</body>\n</html>");
	
	        writer.write(sb.toString());
	
	        writer.close();
        }
    }
}
