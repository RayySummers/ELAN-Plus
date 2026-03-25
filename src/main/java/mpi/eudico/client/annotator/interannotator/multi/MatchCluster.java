package mpi.eudico.client.annotator.interannotator.multi;

import mpi.eudico.server.corpora.clom.AnnotationCore;

/**
 * A data structure for a cluster of matching annotations created by two or more raters or annotators. A cluster may contain
 * less annotations than the number of raters (e.g. if one or more of the raters did not observe an event where others did).
 * The matching algorithm is not part of this class.
 *
 * @author Han Sloetjes
 */
public class MatchCluster {
    // package private fields
    long overlapDuration;  // overlap duration
    long[] overlapInterval;  // begin and end time of the overlap
    int numberOfAnnos;    // number of involved annotations
    AnnotationCore[] matchingAnnos;    // matching annotations
    double sumOverlapRatios; // the sum of the ratio (0-1) of the overlap and each annotation's duration

    /**
     * Constructor, initializes some arrays.
     *
     * @param numRaters the number of raters, corresponding to the size of the array of annotations
     */
    public MatchCluster(int numRaters) {
        super();
        matchingAnnos = new AnnotationCore[numRaters];
        overlapInterval = new long[2];
    }

    /**
     * Returns an array of size 2 containing begin and end time of the overlap of all involved annotations.
     *
     * @return the overlap interval
     */
    public long[] getOverlapInterval() {
        return overlapInterval;
    }

    /**
     * Returns the array of the matching annotations.
     *
     * @return the array of annotations
     */
    public AnnotationCore[] getAnnotationArray() {
        return matchingAnnos;
    }

    /**
     * Returns the sum of the ratio or proportion of the overlap and the extent of each (non-null) annotation. The value will
     * be between 0 and number of annotations.
     *
     * @return the sum of the overlap ratios
     */
    public double getSumOfOverlapRatios() {
        return sumOverlapRatios;
    }

    /**
     * Returns the smallest begin time of all annotations in the cluster.
     *
     * @return the minimal begin time of the annotations in the cluster or {@code Long#MAX_VALUE} if there are no annotations
     */
    public long getOverallBeginTime() {
        long bt = Long.MAX_VALUE;
        for (AnnotationCore ac : matchingAnnos) {
            bt = ac.getBeginTimeBoundary() < bt ? ac.getBeginTimeBoundary() : bt;
        }
        return bt;
    }

    /**
     * Returns the greatest end time value of the annotations in the cluster.
     *
     * @return the maximum end time value of the annotations in the cluster or 0 if there are no annotations
     */
    public long getOverallEndTime() {
        long et = 0L;
        for (AnnotationCore ac : matchingAnnos) {
            et = ac.getEndTimeBoundary() > et ? ac.getEndTimeBoundary() : et;
        }
        return et;
    }

    /**
     * @return a parameterized representation of this cluster
     */
    @Override
    public String toString() {
        return String.format(
            "Overlap duration %d, overlap interval [%d-%d], number of annotations %d, sum of overlap ratios %f",
            overlapDuration,
            overlapInterval[0],
            overlapInterval[1],
            numberOfAnnos,
            sumOverlapRatios);
    }


}
