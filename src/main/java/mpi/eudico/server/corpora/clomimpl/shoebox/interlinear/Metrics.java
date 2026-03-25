/*
 * Created on Sep 24, 2004
 *
 */
package mpi.eudico.server.corpora.clomimpl.shoebox.interlinear;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mpi.eudico.server.corpora.clom.Annotation;

/**
 * Metrics is a data container for storage and transfer of size and position
 * information. It is used by Interlinearizer during the generation of
 * interlinear views of Transcription objects.
 *
 * @author Hennie Brugman
 */
public class Metrics {
    private TimeCodedTranscription transcription;
    private Interlinearizer interlinearizer;
    private Map<Annotation, Integer> sizeTable;
    private Map<Annotation, Integer> usedWidthTable;
    private Map<Annotation, Integer> horizontalPositions;
    private Map<Annotation, Integer> verticalPositions;
    private Map<String, Integer> tierHeights;
    private List<Annotation> blockWiseOrdered; // annotations sorted on hor. pos and tier hierarchy
    private List<Annotation> verticallyOrdered; // annotations sorted on vertical position
    private int leftMargin;
    private boolean leftMarginOn = false;

    /**
     * Creates a new Metrics instance
     *
     * @param tr the transcription containing the annotations
     * @param interlinearizer the main organizing object holding settings for
     * the interlinearization  
     */
    public Metrics(TimeCodedTranscription tr, Interlinearizer interlinearizer) {
        transcription = tr;
        this.interlinearizer = interlinearizer;

        sizeTable = new HashMap<Annotation, Integer>();
        usedWidthTable = new HashMap<Annotation, Integer>();
        horizontalPositions = new HashMap<Annotation, Integer>();
        verticalPositions = new HashMap<Annotation, Integer>();
        tierHeights = new HashMap<String, Integer>();
    }

    /**
     * Clears all cached information from lists and maps.
     */
    public void reset() {
        sizeTable.clear();
        usedWidthTable.clear();
        horizontalPositions.clear();
        verticalPositions.clear();
        tierHeights.clear();

        blockWiseOrdered = null;
        verticallyOrdered = null;
    }

    /**
     * Adds the size of an annotation to a table.
     * 
     * @param annot the annotation
     * @param size the size for the annotation
     */
    public void setSize(Annotation annot, int size) {
        sizeTable.put(annot, Integer.valueOf(size));
    }

    /**
     * Returns the size of the specified annotation.
     * 
     * @param annot the annotation
     *
     * @return the size of the annotation
     */
    public int getSize(Annotation annot) {
        int size = 0;

        Integer intSize = sizeTable.get(annot);

        if (intSize != null) {
            size = intSize.intValue();
        }

        return size;
    }

    /**
     * The used width of an annotation depends on other, depending annotations
     * too, not only its own size.
     * 
     * @param annot the annotation
     * @param width the width of the annotation
     */
    public void setUsedWidth(Annotation annot, int width) {
        usedWidthTable.put(annot, Integer.valueOf(width));
    }

    /**
     * Returns the width of an annotation.
     * 
     * @param annot the annotation
     *
     * @return the used width
     * @see #setUsedWidth(Annotation, int)
     */
    public int getUsedWidth(Annotation annot) {
        int usedWidth = 0;

        Integer intWidth = usedWidthTable.get(annot);

        if (intWidth != null) {
            usedWidth = intWidth.intValue();
        }

        return usedWidth;
    }

    /**
     * Sets the horizontal position of an annotation.
     * 
     * @param annot the annotation
     * @param hPos the horizontal position, the x coordinate
     */
    public void setHorizontalPosition(Annotation annot, int hPos) {
        horizontalPositions.put(annot, Integer.valueOf(hPos));
    }

    /**
     * Returns the horizontal position of an annotation.
     * 
     * @param annot the annotation
     *
     * @return the horizontal position, the x coordinate
     */
    public int getHorizontalPosition(Annotation annot) {
        int hPos = 0;

        Integer intHPos = horizontalPositions.get(annot);

        if (intHPos != null) {
            hPos = intHPos.intValue();
        }

        return hPos;
    }

    /**
     * Sets the vertical position of an annotation.
     * 
     * @param annot the annotation
     * @param vPos the vertical position, the y coordinate
     * @see #setVerticalPosition(Annotation)
     */
    public void setVerticalPosition(Annotation annot, int vPos) {
        verticalPositions.put(annot, Integer.valueOf(vPos));
    }

    /**
     * Sets initial (before any wrapping takes place) position of annotation
     * on basis of tier heights.
     *
     * @param annot the annotation
     */
    public void setVerticalPosition(Annotation annot) {
        int vPos = 0;

        String tierName = annot.getTier().getName();
        String[] visibleTiers = getInterlinearizer().getVisibleTiers();

        for (int i = 0; i < visibleTiers.length; i++) {
            if (tierName.equals(visibleTiers[i])) {
                vPos += getTierHeight(visibleTiers[i]);

                break;
            } else {
                vPos += (getTierHeight(visibleTiers[i]) +
                getInterlinearizer().getLineSpacing());
            }
        }

        verticalPositions.put(annot, Integer.valueOf(vPos));
    }

    /**
     * Returns the vertical position of an annotation. 
     * 
     * @param annot the annotation
     *
     * @return the vertical position, the y coordinate
     */
    public int getVerticalPosition(Annotation annot) {
        int vPos = 0;

        Integer intVPos = verticalPositions.get(annot);

        if (intVPos != null) {
            vPos = intVPos.intValue();
        }

        return vPos;
    }

    /**
     * Returns the current maximum vertical position.
     * 
     * @return the highest value of the stored vertical positions
     */
    public int getMaxVerticalPosition() {
        int maxPosition = 0;

        Collection<Integer> c = verticalPositions.values();
        Iterator<Integer> cIter = c.iterator();

        while (cIter.hasNext()) {
            int vPos = cIter.next().intValue();

            if (vPos > maxPosition) {
                maxPosition = vPos;
            }
        }

        return maxPosition;
    }

    /**
     * Returns the current maximum of horizontal positions.
     * 
     * @return the highest value of the stored horizontal positions
     */
    public int getMaxHorizontallyUsedWidth() {
        int maxHUsed = 0;
        
        for (Map.Entry<Annotation, Integer> horPosEntry : horizontalPositions.entrySet()) {
        	Annotation a = horPosEntry.getKey();
            int hpos = horPosEntry.getValue().intValue();
            int usedWidth = usedWidthTable.get(a).intValue();

            if ((hpos + usedWidth) > maxHUsed) {
                maxHUsed = hpos + usedWidth;
            }
        }

        return maxHUsed + getLeftMargin();
    }

    /**
     * Stores the height of a tier.
     * 
     * @param tierName the tier name
     * @param tierHeight the tier height
     */
    public void setTierHeight(String tierName, int tierHeight) {
        tierHeights.put(tierName, Integer.valueOf(tierHeight));
    }

    /**
     * Returns the height of a tier.
     * 
     * @param forTier the tier
     *
     * @return the stored height
     * @see #setTierHeight(String, int)
     */
    public int getTierHeight(String forTier) {
        Integer i = tierHeights.get(forTier);

        if (i != null) {
            return i.intValue();
        } else {
            return 0;
        }
    }

    /**
     * Calculates total height of all visible tiers, plus potential additional
     * line spacing (total height of 'tier bundle').
     *
     * @return total height
     */
    public int getCumulativeTierHeights() {
        int totalHeight = 0;

        Collection<Integer> heights = tierHeights.values();
        Iterator<Integer> hIter = heights.iterator();

        while (hIter.hasNext()) {
            totalHeight += hIter.next().intValue();
        }

        int numOfVisibleTiers = getInterlinearizer().getVisibleTiers().length;
        totalHeight += (numOfVisibleTiers * getInterlinearizer().getLineSpacing());

        return totalHeight;
    }

    /**
     * Derives list of vertical (Integer) positions on basis of the vertical
     * positions of all annotations (after position and wrapping).
     *
     * @return List with Integers for unique vertical positions of tiers
     */
    public List<Integer> getPositionsOfNonEmptyTiers() {
        Collection<Integer> c = verticalPositions.values();

        return new ArrayList<Integer>(new HashSet<Integer>(c));
    }

    /**
     * Returns the tier label at the specified vertical position.
     * 
     * @param position an y coordinate
     *
     * @return the tier label exactly at that position, or null
     */
    public String getTierLabelAt(int position) {
        String label = null;
        
        for (Map.Entry<Annotation, Integer> vertPosEntry : verticalPositions.entrySet()) {
            if (vertPosEntry.getValue().intValue() == position) {
                label = vertPosEntry.getKey().getTier().getName();

                break;
            }
        }

        return label;
    }

    /**
     * Returns vertical positions of every visible tier in the 'tier template':
     * all visible tier labels in the correct order, at the proper position.
     * This template can be repeated when blocks are wrapped. In that case
     * empty lines are also labeled.
     *
     * @return Array with vertical positions for every visible tier
     */
    public int[] getVPositionsInTemplate() {
        int[] vPositions = new int[getInterlinearizer().getVisibleTiers().length];
        int lineSpacing = getInterlinearizer().getLineSpacing();

        int positionInTemplate = 0;

        String[] vTierNames = getInterlinearizer().getVisibleTiers();

        for (int index = 0; index < vTierNames.length; index++) {
            int tierHeight = getTierHeight(vTierNames[index]);

            positionInTemplate += tierHeight;
            vPositions[index] = positionInTemplate;
            positionInTemplate += lineSpacing;
        }

        return vPositions;
    }

    /**
     * Returns a time coded transcription.
     * 
     * @return the  transcription with time code tier, the source for
     * interlinearization
     */
    public TimeCodedTranscription getTranscription() {
        return transcription;
    }

    /**
     * Returns the interlinearizer object.
     * 
     * @return the main object holding settings and defaults for interlinearization
     */
    public Interlinearizer getInterlinearizer() {
        return interlinearizer;
    }

    /**
     * Returns the left margin.
     * 
     * @return the left margin, 0 if there is no left margin
     */
    public int getLeftMargin() {
        if (leftMarginOn) {
            return leftMargin;
        } else {
            return 0;
        }
    }

    /**
     * Sets the left margin.
     * 
     * @param i the new value for the left margin
     */
    public void setLeftMargin(int i) {
        if (leftMarginOn) {
            leftMargin = i;
        }
    }

    /**
     * Sets whether there should be a left margin.
     * 
     * @param show the new flag for applying a left margin
     */
    public void showLeftMargin(boolean show) {
        leftMarginOn = show;
    }

    /**
     * Returns whether there should be a left margin shown.
     * 
     * @return whether or not a margin is shown on the left side
     */
    public boolean leftMarginShown() {
        return leftMarginOn;
    }

    /**
     * Generates and returns a sorted List of visible annotations. Sorting is
     * done on basis of left to right occurance in interlinear blocks:
     * horizontal position and position in tier hierarchy are used.
     *
     * @return sorted List with annotations
     */
    public List<Annotation> getBlockWiseOrdered() {
        if (blockWiseOrdered == null) { // calculate vector

            // assume that all annotations, visible and invisible, are sized
            blockWiseOrdered = new ArrayList<Annotation>();

            Set<Annotation> allAnnots = sizeTable.keySet();

            String[] visibleTiers = getInterlinearizer().getVisibleTiers();
            List<String> vTierList = Arrays.asList(visibleTiers);

            Iterator<Annotation> annIter = allAnnots.iterator();

            while (annIter.hasNext()) {
                Annotation a = annIter.next();

                if (vTierList.contains(a.getTier().getName())) {
                    blockWiseOrdered.add(a);
                }
            }

            Collections.sort(blockWiseOrdered, new AnnotationComparator());
        }

        return blockWiseOrdered;
    }

    /**
     * Generates and returns a sorted List of visible annotations. Sorting is
     * done on basis of vertical position.
     *
     * @return sorted List with annotations
     */
    public List<Annotation> getVerticallyOrdered() {
        if (verticallyOrdered == null) { // calculate vector

            // assume that all annotations, visible and invisible, are sized
            verticallyOrdered = new ArrayList<Annotation>();

            Set<Annotation> allAnnots = sizeTable.keySet();

            String[] visibleTiers = getInterlinearizer().getVisibleTiers();
            List<String> vTierList = Arrays.asList(visibleTiers);

            Iterator<Annotation> annIter = allAnnots.iterator();

            while (annIter.hasNext()) {
                Annotation a = annIter.next();

                if (vTierList.contains(a.getTier().getName())) {
                    verticallyOrdered.add(a);
                }
            }

            Collections.sort(verticallyOrdered, new AnnotComparatorOnVPos());
        }

        return verticallyOrdered;
    }

    /**
     * Calculates the number of pages and the vertical positions of page
     * boundaries.
     *
     * @param pageHeight the available height for a page
     *
     * @return an array of vertical page break positions
     */
    public int[] getPageBoundaries(int pageHeight) {
        int[] pageBoundaries = null;
        int lastPageBreak = 0;

        List<Integer> boundaries = new ArrayList<Integer>();
        List<Integer> vPosIntegers = null;
        int[] tierLblVPositions = null;

        // find sorted int[] of vPositions of tier labels
        vPosIntegers = getPositionsOfNonEmptyTiers();
        Collections.sort(vPosIntegers);

        tierLblVPositions = new int[vPosIntegers.size()];

        for (int i = 0; i < vPosIntegers.size(); i++) {
            tierLblVPositions[i] = vPosIntegers.get(i).intValue();
        }

        // loop over tierLabel positions, find each next page break
        for (int k = 0; k < tierLblVPositions.length; k++) {
            if (tierLblVPositions[k] > (lastPageBreak + pageHeight)) { // next break passed

                if (k > 0) {
                    lastPageBreak = tierLblVPositions[k - 1];
                    boundaries.add(Integer.valueOf(lastPageBreak));
                }
            }
        }

        pageBoundaries = new int[boundaries.size()];

        for (int m = 0; m < boundaries.size(); m++) {
            pageBoundaries[m] = boundaries.get(m).intValue();
        }

        return pageBoundaries;
    }

    /**
     * Returns page boundaries.
     * 
     * @param pageIndex the page index 
     * @param pageHeight the height of the page
     *
     * @return an array size 2, specifying start and end vertical coordinates
     */
    public int[] getPageBoundaries(int pageIndex, int pageHeight) {
        int pageCounter = 0;
        int lastPageBreak = 0;

        int[] boundaries = { 0, 0 };

        List<Integer> vPosIntegers = null;
        int[] tierLblVPositions = null;

        // find sorted int[] of vPositions of tier labels
        vPosIntegers = getPositionsOfNonEmptyTiers();

        Collections.sort(vPosIntegers);

        tierLblVPositions = new int[vPosIntegers.size()];

        for (int i = 0; i < vPosIntegers.size(); i++) {
            tierLblVPositions[i] = vPosIntegers.get(i).intValue();
        }

        // loop over tierLabel positions, find each next page break, count pages
        for (int k = 0; k < tierLblVPositions.length; k++) {
            if (tierLblVPositions[k] > (lastPageBreak + pageHeight)) { // next break passed

                if (pageCounter == pageIndex) { // right page found

                    break;
                } else {
                    if (k > 0) {
                        lastPageBreak = tierLblVPositions[k - 1];
                    }

                    pageCounter++;
                }
            }

            boundaries[0] = lastPageBreak;
            boundaries[1] = tierLblVPositions[k];
        }

        if (pageIndex > pageCounter) { // nothing on page pageIndex, terminate
            boundaries[0] = 0;
            boundaries[1] = 0;
        }

        return boundaries;
    }

    /**
     * A specialized comparator for annotations, compares Annotations, first on
     * basis of their horizontal position as stored in horizontalPositions, 
     * then on basis of their position in the tier hierarchy.
     */
    class AnnotationComparator implements Comparator<Annotation> {
    	/**
    	 * Creates a comparator. 
    	 */
        public AnnotationComparator() {
			super();
		}

		/**
         * Compares Annotations, first on basis of their horizontal position as
         * stored in horizontalPositions, then on basis of their position in
         * the tier hierarchy.
         *
         * @see java.util.Comparator#compare(java.lang.Object,
         *      java.lang.Object)
         */
        @Override
		public int compare(Annotation a0, Annotation a1) {
            int hpos0 = horizontalPositions.get(a0).intValue();
            int hpos1 = horizontalPositions.get(a1).intValue();

            if (hpos0 < hpos1) {
                return -1;
            }

            if (hpos0 > hpos1) {
                return 1;
            }

            if (hpos0 == hpos1) {
                if (transcription.isAncestorOf(a1.getTier(), a0.getTier())) {
                    //	if (((TierImpl)a0.getTier()).hasAncestor((TierImpl)a1.getTier())) {
                    return 1;
                } else {
                    return -1;
                }
            }

            return 0;
        }
    }

    /**
     * A specialized comparator, compares Annotations on the basis of vertical 
     * position. 
     */
    class AnnotComparatorOnVPos implements Comparator<Annotation> {
    	/**
    	 * Creates a new comparator.
    	 */
        public AnnotComparatorOnVPos() {
			super();
		}

		/**
         * Compares Annotations, on basis of vertical position
         *
         * @see java.util.Comparator#compare(java.lang.Object,
         *      java.lang.Object)
         */
        @Override
		public int compare(Annotation a0, Annotation a1) {
            int vpos0 = verticalPositions.get(a0).intValue();
            int vpos1 = verticalPositions.get(a1).intValue();

            if (vpos0 < vpos1) {
                return -1;
            }

            if (vpos0 >= vpos1) {
                return 1;
            }

            return 0;
        }
    }
}
