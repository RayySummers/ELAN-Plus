package mpi.eudico.client.annotator.interannotator.multi;

import mpi.eudico.server.corpora.clom.AnnotationCore;

import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * A matrix with the categories (annotation values) in the columns and the subjects (the segments, the annotations) in the
 * rows. With marginal columns and rows for column and row totals and proportions. In fact maintains two matrices, one
 * without {@code Unmatched} or {@code Void} values, one with these values (incomplete subjects). Modeled after the example
 * at
 * <a href="https://en.wikipedia.org/wiki/Fleiss'_kappa">https://en.wikipedia.org/wiki/Fleiss'_kappa</a>.
 * Calculates Fleiss' kappa.
 *
 * @author Han Sloetjes
 */
public class MultiMatrix {
    /* matrix[annotations/units/clusters][codes/cv_entries/categories],
     * including "unmatched" or "void" */
    private int[][] matrixIU;
    /* matrix[annotations/units/clusters][codes/cv_entries/categories],
     * excluding "unmatched" or "void" */
    private int[][] matrixEU;
    /* marginal totals for rows and columns */
    private int[] rowIUMarginalTotals;
    private int[] rowEUMarginalTotals;
    private int[] colIUMarginalTotals;
    private int[] colEUMarginalTotals;

    private double[] rowIUMarginalProp;
    private double[] rowEUMarginalProp;
    private double[] colIUMarginalProp;
    private double[] colEUMarginalProp;
    /* the number of annotators or raters or tiers */
    private int numRaters;
    /* the number of observations or units => number of rows */
    private int numIUObservations;
    /* the number of observations or units => number of rows */
    private int numEUObservations;
    /* the number of categories or codes or values => number of columns */
    private int numIUCategories;
    /* the column headers, the categories */
    private List<String> valueList;
    private final String UNMATCHED = "Unmatched"; // or "Void"

    private double peIU;
    private double peEU;
    private double avgPIU;
    private double avgPEU;

    /**
     * Constructor, creates and fills the matrices, calculates marginal values.
     *
     * @param ccm the object that holds matching tiers and their annotations and the controlled vocabulary values
     * @param clusters the list of clusters of matching annotations
     */
    public MultiMatrix(CompareCombiMulti ccm, List<MatchCluster> clusters) {
        if (ccm == null || clusters == null) {
            //ccm..
            return;
        }
        numRaters = ccm.getCompareUnits().size();
        if (ccm.getValues() != null) {
            numIUCategories = ccm.getValues().size() + 1;
            valueList = new ArrayList<String>(ccm.getValues());
            // maybe sort the list?
            // maybe check if UNMATCHED happens to be a value?
            valueList.add(UNMATCHED);
        } else {
            // the following is meaningless
            numIUCategories = 1;
            valueList = new ArrayList<String>(1);
            valueList.add(UNMATCHED);
        }
        numIUObservations = clusters.size();
        createInclMatrix(clusters);
        createExclMatrix();
        //testFAMatrix();
        calculateMarginals();
    }

    /**
     * Creates the matrix including the unmatched or void category for incomplete matches.
     *
     * @param clusters the list of clusters
     */
    private void createInclMatrix(List<MatchCluster> clusters) {
        matrixIU = new int[numIUObservations][numIUCategories];

        for (int i = 0; i < clusters.size(); i++) {
            MatchCluster mc = clusters.get(i);
            for (AnnotationCore ac : mc.matchingAnnos) {
                if (ac != null) {
                    int col = valueList.indexOf(ac.getValue().strip());
                    col = col < 0 ? numIUCategories - 1 : col;
                    matrixIU[i][col]++;
                } else {
                    matrixIU[i][numIUCategories - 1]++;
                }
            }
        }
    }

    /**
     * Creates the matrix without the unmatched or void value, ignoring all incomplete subjects (clusters with less than
     * the-number-of-raters annotations).
     */
    private void createExclMatrix() {
        if (matrixIU == null) {
            return;
        }
        // calculate the number of items that have been rated by all raters
        int col = numIUCategories - 1;
        for (int i = 0; i < numIUObservations; i++) {
            if (matrixIU[i][col] == 0) {
                numEUObservations++;
            }
        }

        matrixEU = new int[numEUObservations][numIUCategories - 1];

        for (int i = 0, j = 0; i < numIUObservations && j < numEUObservations; i++) {
            if (matrixIU[i][col] == 0) {
                if (numIUCategories - 1 >= 0) {
                    System.arraycopy(matrixIU[i], 0, matrixEU[j], 0, numIUCategories - 1);
                }
                j++;
            }
        }
    }

    /**
     * After creating and populating the matrices the marginals can be calculated.
     */
    private void calculateMarginals() {
        if (matrixIU == null) {
            return;
        }
        rowIUMarginalTotals = new int[numIUObservations]; // the value of the sum should always be == number of raters
        rowIUMarginalProp = new double[numIUObservations];
        colIUMarginalTotals = new int[numIUCategories];
        colIUMarginalProp = new double[numIUCategories];

        for (int i = 0; i < numIUObservations; i++) {
            for (int j = 0; j < numIUCategories; j++) {
                colIUMarginalTotals[j] += matrixIU[i][j];
                rowIUMarginalTotals[i] += matrixIU[i][j];
            }
        }
        // validate total observations = numRaters * numIUObservations
        int totalObs = 0;
        for (int i = 0; i < colIUMarginalTotals.length; i++) {
            totalObs += colIUMarginalTotals[i];
        }
        // column marginal proportions, column total / matrix total
        for (int i = 0; i < colIUMarginalTotals.length; i++) {
            colIUMarginalProp[i] = colIUMarginalTotals[i] / (double) totalObs;
        }
        // row marginal proportions, Fleiss specific?
        double factor = 1.0d / (numRaters * (numRaters - 1));
        // calc per row
        for (int i = 0; i < numIUObservations; i++) {
            int powerTot = 0;
            for (int j = 0; j < numIUCategories; j++) {
                powerTot += Math.pow(matrixIU[i][j], 2.0);
            }
            rowIUMarginalProp[i] = factor * (powerTot - numRaters);
        }
        // the sum of all row marginal proportions, sum Pi
        double sumRowProp = 0.0d;
        for (int i = 0; i < rowIUMarginalProp.length; i++) {
            sumRowProp += rowIUMarginalProp[i];
        }
        avgPIU = sumRowProp / numIUObservations; // P

        peIU = 0.0d; // Pe
        for (int i = 0; i < colIUMarginalProp.length; i++) {
            peIU += Math.pow(colIUMarginalProp[i], 2.0);
        }
        // calc kappa
        double k = (avgPIU - peIU) / (1.0d - peIU);
        //System.out.println("Fleiss' kappa including Unmatched: " + k);

        //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        if (matrixEU == null) {
            return;
        }
        rowEUMarginalTotals = new int[numEUObservations]; // matrixEU.length
        rowEUMarginalProp = new double[numEUObservations];
        colEUMarginalTotals = new int[numIUCategories - 1];
        colEUMarginalProp = new double[numIUCategories - 1];

        for (int i = 0; i < numEUObservations; i++) {
            for (int j = 0; j < numIUCategories - 1; j++) {
                colEUMarginalTotals[j] += matrixEU[i][j];
                rowEUMarginalTotals[i] += matrixEU[i][j];
            }
        }
        // validate total observations = numRaters * numEUObservations
        int totalObsEx = 0;
        for (int i = 0; i < colEUMarginalTotals.length; i++) {
            totalObsEx += colEUMarginalTotals[i];
        }
        // column marginal proportions, column total / matrix total
        for (int i = 0; i < colEUMarginalTotals.length; i++) {
            colEUMarginalProp[i] = colEUMarginalTotals[i] / (double) totalObsEx;
        }
        // row marginal proportions, Fleiss specific?
        //double factor = 1.0d / (numRaters * (numRaters - 1));
        // calculate per row
        for (int i = 0; i < numEUObservations; i++) {
            int powerTot = 0;
            for (int j = 0; j < numIUCategories - 1; j++) {
                powerTot += Math.pow(matrixEU[i][j], 2.0);
            }
            rowEUMarginalProp[i] = factor * (powerTot - numRaters);
        }
        // the sum of all row marginal proportions, sum Pi
        double sumRowPropEx = 0.0d;
        for (int i = 0; i < rowEUMarginalProp.length; i++) {
            sumRowPropEx += rowEUMarginalProp[i];
        }
        avgPEU = sumRowPropEx / numEUObservations; // P

        peEU = 0.0d; // Pe
        for (int i = 0; i < colEUMarginalProp.length; i++) {
            peEU += Math.pow(colEUMarginalProp[i], 2.0);
        }
        // calculate Fleiss' kappa excluding unmatched
        double ke = (avgPEU - peEU) / (1.0d - peEU);
        //System.out.println("Fleiss' kappa excluding Unmatched: " + ke);
    }

    // add getters for P and Pe for both matrices
    // maybe a getString for k=(P - Pe)/(1 - Pe)

    /**
     * Writes the contents of one of the matrices to the specified writer.
     *
     * @param writer the writer to write to
     * @param decFormat the formatter for decimal values
     * @param includingUnmatched if {@code true} the matrix which includes unmatched values is printed, otherwise the
     *     matrix excluding unmatched values is printed
     *
     * @throws IOException any IO exception during the writing
     */
    public void printMatrix(Writer writer, DecimalFormat decFormat, boolean includingUnmatched) throws
                                                                                                IOException {
        writer.write("\t");
        if (includingUnmatched) {
            for (String s : valueList) {
                writer.write(s);
                writer.write("\t");
            }
            writer.write("Pi");
            writer.write("\n");
            for (int i = 0; i < numIUObservations; i++) {
                writer.write(String.valueOf(i + 1));
                writer.write("\t");
                for (int j = 0; j < numIUCategories; j++) {
                    writer.write(String.valueOf(matrixIU[i][j]));
                    writer.write("\t");
                }
                writer.write(decFormat.format(rowIUMarginalProp[i]));
                writer.write("\n");
            }
            writer.write("Total\t");
            for (int j = 0; j < numIUCategories; j++) {
                writer.write(String.valueOf(colIUMarginalTotals[j]));
                writer.write("\t");
            }
            writer.write("\t\n");
            writer.write("pj\t");
            for (int j = 0; j < numIUCategories; j++) {
                writer.write(decFormat.format(colIUMarginalProp[j]));
                writer.write("\t");
            }
            writer.write("\t\n");
        } else {
            for (int i = 0; i < valueList.size() - 1; i++) {
                writer.write(valueList.get(i));
                writer.write("\t");
            }
            writer.write("Pi");
            writer.write("\n");
            for (int i = 0; i < numEUObservations; i++) {
                writer.write(String.valueOf(i + 1));
                writer.write("\t");
                for (int j = 0; j < numIUCategories - 1; j++) {
                    writer.write(String.valueOf(matrixEU[i][j]));
                    writer.write("\t");
                }
                writer.write(decFormat.format(rowEUMarginalProp[i]));
                writer.write("\n");
            }
            writer.write("Total\t");
            for (int j = 0; j < numIUCategories - 1; j++) {
                writer.write(String.valueOf(colEUMarginalTotals[j]));
                writer.write("\t");
            }
            writer.write("\t\n");
            writer.write("pj\t");
            for (int j = 0; j < numIUCategories - 1; j++) {
                writer.write(decFormat.format(colEUMarginalProp[j]));
                writer.write("\t");
            }
            writer.write("\t\n");
        }
    }

    /**
     * Returns the sum of the power of 2 of each column marginal proportion, the {@code Pe} value. 1 - {@code Pe} gives the
     * degree of agreement that is attainable above chance.
     *
     * @return the {@code Pe} value of the matrix including unmatched values
     */
    public double getPeIU() {
        return peIU;
    }

    /**
     * Returns the sum of the power of 2 of each column marginal proportion, the {@code Pe} value. 1 - {@code Pe} gives the
     * degree of agreement that is attainable above chance.
     *
     * @return the {@code Pe} value of the matrix excluding unmatched values
     */
    public double getPeEU() {
        return peEU;
    }

    /**
     * Returns the {@code P} value, the average of the row marginal proportions.
     *
     * @return the {@code P} value of the matrix including unmatched values
     */
    public double getAvgPIU() {
        return avgPIU;
    }

    /**
     * Returns the {@code P} value, the average of the row marginal proportions.
     *
     * @return the {@code P} value of the matrix excluding unmatched values
     */
    public double getAvgPEU() {
        return avgPEU;
    }

    /**
     * Returns the Fleiss' kappa value of the matrix including unmatched values.
     *
     * @return the Fleiss' kappa value of the matrix including unmatched values
     */
    public double getKappaIU() {
        //        if (peIU == 1) {
        //            return Double.NaN;
        //        }

        return (avgPIU - peIU) / (1.0d - peIU);
    }

    /**
     * Returns the Fleiss' kappa value of the matrix excluding unmatched values.
     *
     * @return the Fleiss' kappa value of the matrix excluding unmatched values
     */
    public double getKappaEU() {
        //        if (peEU == 1) {
        //            return Double.NaN;
        //        }

        return (avgPEU - peEU) / (1.0d - peEU);
    }

    // test matrix 1
    /*
     * Tests the example worked out at:
     * https://en.wikipedia.org/wiki/Fleiss%27_kappa
     */
    private void testMatrix() {
        matrixEU = new int[][] {{0, 0, 0, 0, 14},
                                {0, 2, 6, 4, 2},
                                {0, 0, 3, 5, 6},
                                {0, 3, 9, 2, 0},
                                {2, 2, 8, 1, 1},
                                {7, 7, 0, 0, 0},
                                {3, 2, 6, 3, 0},
                                {2, 5, 3, 2, 2},
                                {6, 5, 2, 1, 0},
                                {0, 2, 2, 3, 7}};
        numEUObservations = matrixEU.length;
        numIUCategories = matrixEU[0].length + 1;
        numRaters = 14;

        matrixIU = new int[][] {{0, 0, 0, 0, 14, 0},
                                {0, 2, 6, 4, 2, 0},
                                {0, 0, 3, 5, 6, 0},
                                {0, 3, 9, 2, 0, 0},
                                {2, 2, 8, 1, 1, 0},
                                {7, 7, 0, 0, 0, 0},
                                {3, 2, 6, 3, 0, 0},
                                {2, 5, 3, 2, 2, 0},
                                {6, 5, 2, 1, 0, 0},
                                {0, 2, 2, 3, 7, 0}};
        numIUObservations = matrixIU.length;

        valueList = new ArrayList<String>(numIUCategories);
        valueList.add("1");
        valueList.add("2");
        valueList.add("3");
        valueList.add("4");
        valueList.add("5");
        valueList.add(UNMATCHED);
    }

    // test matrix 2
    /*
     * Tests full agreement (kappa should be 1).
     * https://en.wikipedia.org/wiki/Fleiss%27_kappa
     */
    private void testFAMatrix() {
        matrixEU = new int[][] {{0, 0, 0, 14, 0},
                                {0, 0, 0, 14, 0},
                                {0, 0, 14, 0, 0},
                                {0, 14, 0, 0, 0},
                                {14, 0, 0, 0, 0},
                                {0, 14, 0, 0, 0},
                                {0, 0, 14, 0, 0},
                                {0, 0, 0, 14, 0},
                                {0, 0, 0, 14, 0},
                                {0, 0, 0, 14, 0}};
        numEUObservations = matrixEU.length;
        numIUCategories = matrixEU[0].length + 1;
        numRaters = 14;

        matrixIU = new int[][] {{14, 0, 0, 0, 0, 0},
                                {14, 0, 0, 0, 0, 0},
                                {14, 0, 0, 0, 0, 0},
                                {14, 0, 0, 0, 0, 0},
                                {14, 0, 0, 0, 0, 0},
                                {14, 0, 0, 0, 0, 0},
                                {14, 0, 0, 0, 0, 0},
                                {14, 0, 0, 0, 0, 0},
                                {14, 0, 0, 0, 0, 0},
                                {0, 0, 14, 0, 0, 0}};
        numIUObservations = matrixIU.length;

        valueList = new ArrayList<String>(numIUCategories);
        valueList.add("1");
        valueList.add("2");
        valueList.add("3");
        valueList.add("4");
        valueList.add("5");
        valueList.add(UNMATCHED);
    }
}
