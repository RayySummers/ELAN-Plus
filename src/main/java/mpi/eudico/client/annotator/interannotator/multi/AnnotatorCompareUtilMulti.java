package mpi.eudico.client.annotator.interannotator.multi;

import mpi.eudico.client.annotator.interannotator.CompareUnit;
import mpi.eudico.server.corpora.clom.AnnotationCore;

import java.util.*;

/**
 * A utility class which detects and creates clusters of matching annotations on multiple tiers, produced by multiple
 * raters.
 *
 * @author Han Sloetjes
 */
public class AnnotatorCompareUtilMulti {
    private double avgRatioThreshold = 0.7d;

    /**
     * Constructor.
     */
    public AnnotatorCompareUtilMulti() {
        super();
    }

    /**
     * Sets the threshold, i.e. the minimum requirement, for the average ratio of overlap duration and annotation duration.
     *
     * @param avgRatioThreshold the new threshold
     */
    public void setAvgRatioThreshold(double avgRatioThreshold) {
        if (avgRatioThreshold >= 0 && avgRatioThreshold <= 1) {
            this.avgRatioThreshold = avgRatioThreshold;
        }
    }

    // return arrays of matching annotations  with some additional information
    // duration of overlap, sum of ratio of overlap / duration of involved annotations

    /**
     * Returns a list of {@code MatchCluster} objects compiled from the compare units in the compare combination. The current
     * approach starts with 'slicing' all annotations; the begin and end times are added to one, sorted, list. For each
     * interval (t, t + 1) a cluster is created if at least one annotation of one of the involved tiers overlaps that
     * interval. In the end those clusters remain with the largest number of overlapping annotations and a large enough total
     * amount of overlap percentages. In the end each annotation is part of only one cluster.
     *
     * @param compareCombi the compare combination containing the compare units
     *
     * @return a list of {@code MatchCluster} objects
     */
    public List<MatchCluster> matchAnnotationsMulti(CompareCombiMulti compareCombi) {
        if (compareCombi == null) {
            return null;
        }
        int numRaters = compareCombi.getCompareUnits().size();
        if (numRaters < 2) {
            return null;
        }

        // create a set of all present begin and end times
        SortedSet<Long> timePoints = new TreeSet<Long>();
        for (CompareUnit cmUnit : compareCombi.getCompareUnits()) {
            for (AnnotationCore ac : cmUnit.annotations) {
                timePoints.add(ac.getBeginTimeBoundary());
                timePoints.add(ac.getEndTimeBoundary());
            }
        }

        if (timePoints.size() == 0) {
            return null; // return null
        }

        // create an array of durations between each t and t + 1
        Long[] tpArray = timePoints.toArray(new Long[0]);
        int numSegments = tpArray.length - 1;
        MatchCluster[] clusters = new MatchCluster[numSegments];


        for (int i = 0; i < numSegments; i++) {
            clusters[i] = new MatchCluster(numRaters);
            clusters[i].overlapInterval[0] = tpArray[i];
            clusters[i].overlapInterval[1] = tpArray[i + 1];
            clusters[i].overlapDuration = tpArray[i + 1] - tpArray[i];
        }

        // create a list of number of overlapping annotations for each segment,
        // a value between 0 and numRaters, inclusive

        for (int cu = 0; cu < compareCombi.getCompareUnits().size(); cu++) {
            CompareUnit cmUnit = compareCombi.getCompareUnits().get(cu);
            int index = 0;
            for (AnnotationCore ac : cmUnit.annotations) {
                for (; index < numSegments; index++) {
                    if (ac.getBeginTimeBoundary() >= clusters[index].overlapInterval[1]) { // segments[index][1]
                        continue;
                    }
                    if (clusters[index].overlapInterval[0] >= ac.getEndTimeBoundary()) { // segments[index][0]
                        break;
                    }

                    clusters[index].numberOfAnnos++;
                    clusters[index].sumOverlapRatios +=
                        (clusters[index].overlapDuration / (double) (ac.getEndTimeBoundary() - ac.getBeginTimeBoundary()));
                    clusters[index].matchingAnnos[cu] = ac;

                }
            }
        }
        // collect overlapping, matching annotations based on number of overlapping
        // annotations and duration of the overlap
        /*
        System.out.println("Number of raters: " + numRaters);
        for (int i = 0; i < numSegments; i++) {
            System.out.println(String.format("Cluster %d, duration %d, interval[%d-%d], number of annotations %d, sum
            of overlap ratios %f",
                    i, clusters[i].overlapDuration, clusters[i].overlapInterval[0], clusters[i].overlapInterval[1],
                    clusters[i].numberOfAnnos, clusters[i].sumOverlapRatios));
        }
        */
        ClusterComparator clComp = new ClusterComparator();

        List<MatchCluster> clusterList = new ArrayList<MatchCluster>();
        for (MatchCluster mc : clusters) {
            if (mc.numberOfAnnos > 0) {
                clusterList.add(mc);
            }
        }
        clusterList.sort(clComp);
        //
        //        System.out.println("Number of (non empty) clusters: " + clusterSet.size());
        //        testCompare(clusterSet, clComp);
        //        for (int i = 0; i < clusterList.size(); i++) {
        //            System.out.println("i: " + i + " " + clusterList.get(i));
        //        }

        List<AnnotationCore> matchedAnnos = new ArrayList<AnnotationCore>();
        List<MatchCluster> resultList = new ArrayList<MatchCluster>();

        while (!clusterList.isEmpty()) {
            MatchCluster mc = clusterList.get(0);
            int oldNumAnnos = mc.numberOfAnnos;

            for (int i = 0; i < mc.matchingAnnos.length; i++) {
                AnnotationCore ac = mc.matchingAnnos[i];
                if (ac != null) {
                    if (matchedAnnos.contains(ac)) {
                        // already used in a better match, remove here
                        mc.matchingAnnos[i] = null;
                        mc.numberOfAnnos--;
                    }
                }
            }

            if (mc.numberOfAnnos == 0) {
                clusterList.remove(mc);
                continue;
            }
            // if an annotation had to be removed from the cluster, modify the cluster and re-insert
            if (oldNumAnnos != mc.numberOfAnnos) {
                // recalculate sumOverlaps
                double nextSOR = 0.0d;
                for (AnnotationCore ac : mc.matchingAnnos) {
                    if (ac != null) {
                        nextSOR += (mc.overlapDuration / (double) (ac.getEndTimeBoundary() - ac.getBeginTimeBoundary()));
                    }
                }
                mc.sumOverlapRatios = nextSOR;
                //System.out.println("Old and new num annotations: " + oldNumAnnos + " - " + mc.numberOfAnnos);
                clusterList.remove(mc);
                // re-insert
                int insPoint = Collections.binarySearch(clusterList, mc, clComp);
                if (insPoint < 0) { // should be
                    //System.out.println("Insert: " + (-insPoint - 1));
                    clusterList.add(-insPoint - 1, mc);
                }
            } else {
                // nothing changed, add to result list
                clusterList.remove(mc);
                resultList.add(mc);
                Collections.addAll(matchedAnnos, mc.matchingAnnos);
            }
            //System.out.println("Cluster list size: " + clusterList.size());
        }

        //System.out.println("Result clusters: " + resultList.size());
        //        for (MatchCluster mc : resultList) {
        //            System.out.println(mc);
        //        }
        //        validate(compareCombi, resultList);
        return resultList;
    }

    @SuppressWarnings("unused")
    private void testCompare(Set<MatchCluster> clusterSet, ClusterComparator comparator) {
        Iterator<MatchCluster> mcIter = clusterSet.iterator();
        MatchCluster prevMC = null;
        while (mcIter.hasNext()) {
            MatchCluster curMC = mcIter.next();
            if (prevMC != null) {
                System.out.println("Compare: 1 to 2: "
                                   + comparator.compare(prevMC, curMC)
                                   + " 2 to 1: "
                                   + comparator.compare(curMC, prevMC));
            }

            prevMC = curMC;
        }
    }

    /*
     * Checks if the number of input annotations equals the number of
     * annotations in the clusters.
     */
    @SuppressWarnings("unused")
    private void validate(CompareCombiMulti ccm, List<MatchCluster> clusters) {
        int annIn = 0;
        int annOut = 0;
        for (CompareUnit cu : ccm.getCompareUnits()) {
            annIn += cu.annotations.size();
        }
        for (MatchCluster mc : clusters) {
            annOut += mc.numberOfAnnos;
        }
        System.out.printf("Validation of clustering: annotations in: %d, annotations out: %d%n", annIn, annOut);
    }

    /**
     * A comparator for {@code MatchCluster}s which compares clusters based on the number of (non-null) annotations and on
     * the average overlap ratios. All involved annotations do overlap and the overlap ratio for each annotation is the
     * duration of the overlap divided by the duration of the annotation.
     */
    public class ClusterComparator implements Comparator<MatchCluster> {

        /**
         * Creates a new comparator instance.
         */
        public ClusterComparator() {
            super();
        }

        /**
         * The main criteria are the number of involved annotations and the average of the overlap ratios. Higher values come
         * first in the ordering.
         *
         * @param mc1 the first cluster
         * @param mc2 the second cluster
         *
         * @return -1 if this group is a better match than the other, based on higher number of involved annotations and a
         *     greater overlap ratio.
         */
        @Override
        public int compare(MatchCluster mc1, MatchCluster mc2) {
            if (mc1 == mc2) {
                return 0;
            }
            // the first cluster has more annotations
            if (mc1.numberOfAnnos > mc2.numberOfAnnos) {
                // first check the ratio threshold requirement
                if (mc1.sumOverlapRatios / mc1.numberOfAnnos >= avgRatioThreshold) {
                    return -1;
                } else if (mc1.numberOfAnnos - mc2.numberOfAnnos == 1
                           && mc2.sumOverlapRatios / mc2.numberOfAnnos >= avgRatioThreshold) {
                    // if the other has only one annotation less and meets the ratio requirement
                    return 1;
                } else {
                    // use the bare total overlap ratio
                    if (mc1.sumOverlapRatios > mc2.sumOverlapRatios) {
                        return -1;
                    } else if (mc2.sumOverlapRatios > mc1.sumOverlapRatios) {
                        return 1;
                    }
                }
            }

            // the second cluster contains more annotations, reverse comparison
            if (mc2.numberOfAnnos > mc1.numberOfAnnos) {
                if (mc2.sumOverlapRatios / mc2.numberOfAnnos >= avgRatioThreshold) {
                    return 1;
                } else if (mc2.numberOfAnnos - mc1.numberOfAnnos == 1
                           && mc1.sumOverlapRatios / mc1.numberOfAnnos >= avgRatioThreshold) {
                    return -1;
                } else {
                    if (mc2.sumOverlapRatios > mc1.sumOverlapRatios) {
                        return 1;
                    } else if (mc1.sumOverlapRatios > mc2.sumOverlapRatios) {
                        return -1;
                    }
                }
            }

            // equal number of annotations
            if (mc1.numberOfAnnos == mc2.numberOfAnnos) {
                if (mc1.sumOverlapRatios > mc2.sumOverlapRatios) {
                    return -1;
                } else if (mc2.sumOverlapRatios > mc1.sumOverlapRatios) {
                    return 1;
                }
            }
            // longer duration gives higher rank
            if (mc1.overlapDuration > mc2.overlapDuration) {
                return -1;
            } else if (mc2.overlapDuration > mc1.overlapDuration) {
                return 1;
            }

            // if everything else is undecided, the cluster with the earliest
            // start time comes first
            if (mc1.overlapInterval[0] < mc2.overlapInterval[0]) {
                return -1;
            } else if (mc2.overlapInterval[0] < mc1.overlapInterval[0]) {
                return 1;
            }

            // undecided, this can not happen
            return 0;
        }
    }
}
