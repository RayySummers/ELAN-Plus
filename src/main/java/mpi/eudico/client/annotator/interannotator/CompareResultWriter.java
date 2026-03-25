package mpi.eudico.client.annotator.interannotator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static mpi.eudico.client.annotator.util.IOUtil.getOutputStreamWriter;

/**
 * Writes the results of agreement calculations to a text file. Note: maybe this class can be deleted if there is too little
 * in common in the way different algorithms are applied and exported.
 */
public class CompareResultWriter {
    private static final String LINE_FEED = "\n";
    private static final String DOUBLE_LINE_FEED = LINE_FEED + LINE_FEED;

    /**
     * No argument constructor
     */
    public CompareResultWriter() {
        super();
    }



    /**
     * Writes the results of agreement calculations to file.
     *
     * @param resultList the list of tier pairs and the calculated agreement
     * @param outputFile the file to write to
     * @param encoding the preferred encoding, defaults to UTF-8
     *
     * @throws IOException any io exception that can occur
     */
    public void writeResults(List<CompareCombi> resultList, File outputFile, String encoding) throws
                                                                                              IOException {
        if (resultList == null) {
            throw new NullPointerException("There are no results to save.");
        }

        if (outputFile == null) {
            throw new IOException("There is no file location specified.");
        }

        if (encoding == null) {
            encoding = "UTF-8";
        }

        try (BufferedWriter writer = new BufferedWriter(getOutputStreamWriter(encoding, new FileOutputStream(outputFile)))) {
            // write BOM?

            DecimalFormat decFormat = new DecimalFormat("#0.0000", new DecimalFormatSymbols(Locale.US));

            // write "header" date and time
            writer.write(String.format("Output created: %tD %<tT", Calendar.getInstance()));
            writer.write(DOUBLE_LINE_FEED);
            writer.write("Number of pairs of tiers in the comparison: " + resultList.size());
            writer.write(DOUBLE_LINE_FEED);
            // write agreement output
            int totalCount = 0;
            double totalAgr = 0.0d;

            for (CompareCombi cc : resultList) {
                if (cc.getPerValueAgreement() == null || cc.getPerValueAgreement().isEmpty()) {
                    // output the overall agreement
                    writer.write("File 1: " + cc.getFirstUnit().fileName + " Tier 1: " + cc.getFirstUnit().tierName);
                    writer.write(LINE_FEED);
                    writer.write("Number of annotations 1: " + cc.getFirstUnit().annotations.size());
                    writer.write(LINE_FEED);
                    writer.write("File 2: " + cc.getSecondUnit().fileName + " Tier 2: " + cc.getSecondUnit().tierName);
                    writer.write(LINE_FEED);
                    writer.write("Number of annotations 2: " + cc.getSecondUnit().annotations.size());
                    writer.write(LINE_FEED);
                    writer.write("Agreement: " + decFormat.format(cc.getOverallAgreement()));
                    writer.write(DOUBLE_LINE_FEED);
                }

                totalCount++;
                totalAgr += cc.getOverallAgreement();
            }

            if (totalCount > 0) {
                writer.write("Average agreement: " + decFormat.format(totalAgr / totalCount));
            } else {
                writer.write("There is no overall average agreement avaialable: no tier combinations found.");
            }
        }

    }
}
