package mpi.eudico.client.annotator.export;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.ngramstats.NgramStatsResult;
import mpi.eudico.server.corpora.clom.Transcription;
import nl.mpi.util.FileExtension;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Exports the N-gram analysis result to a CSV file delimited by tabs (\t).
 *
 * @author Larwan Berke, DePaul University
 * @version 1.0
 * @since August 2013
 */
public class ExportNgramRawData extends AbstractBasicExportDialog {
    private static final long serialVersionUID = -8050510198504976188L;
    private final NgramStatsResult result;

    // constants for exporting
    /**
     * constant for delimiter
     */
    final static public String DELIM = "\t";
    /**
     * constant for newline
     */
    final static private String NEWLINE = "\n";

    /**
     * Constructor.
     *
     * @param parent the parent frame
     * @param modal the modal flag
     * @param transcription not used, can be null
     * @param res data structure containing the ngram statistics to export
     */
    public ExportNgramRawData(Frame parent, boolean modal, Transcription transcription, NgramStatsResult res) {
        super(parent, modal, null);

        // store the result
        result = res;

        try {
            startExport();
        } catch (Exception ee) {
            JOptionPane.showMessageDialog(this,
                                          ElanLocale.getString("ExportDialog.Message.Error")
                                          + "\n"
                                          + "("
                                          + ee.getMessage()
                                          + ")",
                                          ElanLocale.getString("Message.Error"),
                                          JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected boolean startExport() throws
                                    IOException {
        // prompt for file name and location
        File file = promptForFile(ElanLocale.getString("ExportTabDialog.Title"), null, FileExtension.TEXT_EXT, true);

        // did we get a file to export to?
        if (file == null) {
            return false;
        }

        // setup the writer
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), encoding))) {

            // write out the header
            writer.write("# RAW DATA Export of N-gram Analysis done on " + new Date() + NEWLINE);
            writer.write("# Selected Domain: " + result.getDomain() + NEWLINE);
            writer.write("# Selected Tier: " + result.getTier() + NEWLINE);
            writer.write("# N-gram Size: " + result.getNgramSize() + NEWLINE);
            writer.write("# Search Time: " + result.getSearchTime() + "s" + NEWLINE);
            writer.write("# Files Inspected: " + result.getNumFiles() + NEWLINE);
            writer.write("# Total Annotations: " + result.getNumAnnotations() + NEWLINE);
            writer.write("# Total N-grams: " + result.getNumNgrams() + NEWLINE);
            writer.write("#" + NEWLINE);

            // write out the columns
            List<String> columns = result.getNgramAt(0).toCSVColumns();
            Iterator<String> itr = columns.iterator();
            while (itr.hasNext()) {
                writer.write(itr.next());
                if (!itr.hasNext()) {
                    // last column :)
                    writer.write(NEWLINE);
                } else {
                    writer.write(DELIM);
                }
            }

            // write out the ngrams :)
            for (int i = 0; i < result.getNumNgrams(); i++) {
                writer.write(result.getNgramAt(i).toCSV(DELIM) + NEWLINE);
            }
        }

        return true;
    }
}
