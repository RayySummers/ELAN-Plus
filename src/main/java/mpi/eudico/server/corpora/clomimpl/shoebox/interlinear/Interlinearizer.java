/*
 * Created on Sep 24, 2004
 */
package mpi.eudico.server.corpora.clomimpl.shoebox.interlinear;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;


/**
 * Interlinearizer renders a range of different interlinear views. These views
 * differ with respect to their 'unit of alignment' and can be controlled by a
 * number of configuration settings. Possible 'units of alignment' are pixels
 * (drawing is on a BufferedImage that can be used for ELAN's interlinear
 * viewer, for printout or for a  web application) and bytes (can be used for
 * Shoebox/Toolbox file output).  The role of Interlinearizer is to store and
 * manage configuration parameters, and to control the rendering process by
 * delegating subtasks to the appropriate helper classes. Interlinearizer uses
 * a Metrics object to store and pass around size  and position information.
 *
 * @author hennie
 */
public class Interlinearizer {
    // constants
    // units for page width and height

    /** constant for centimeter units */
    public static final int CM = 0;

    /** constant for inch units */
    public static final int INCH = 1;

    /** constant for pixel units */
    public static final int PIXEL = 2;

    // wrap styles for blocks and lines

    /** constant indicating that each block starts rendering at a new line */
    public static final int EACH_BLOCK = 0;

    /** constant indicating that wrapping should occur at block boundaries 
     * (not within a block) */
    public static final int BLOCK_BOUNDARY = 1;

    /** constant indicating that wrapping can occur within a block */
    public static final int WITHIN_BLOCKS = 2;

    /** constant indicating that wrapping should not occur */
    public static final int NO_WRAP = 3;

    // time code types

    /** time format hour:min:sec.milliseconds */
    public static final int HHMMSSMS = 0;

    /** time format seconds.milliseconds */
    public static final int SSMS = 1;

    // strings for unaligned time codes

    /** constant for unaligned or unknown time values in hh:mm:ss.ms format */
    public static final String UNALIGNED_HHMMSSMS = "??:??:??:???";

    /** constant for unaligned or unknown time values in ss.ms format */
    public static final String UNALIGNED_SSMS = "?.???";

    // unit for text alignment

    /** constant for pixel units for text positioning and alignment */
    public static final int PIXELS = 0;

    /** constant for byte units for text positioning and alignment */
    public static final int BYTES = 1;

    // font
    //	public static Font DEFAULTFONT = new Font("SansSerif", Font.PLAIN, 12);

    /** constant for default font for rendering */
    public static final Font DEFAULTFONT = new Font("MS Arial Unicode", Font.PLAIN, 12);

    /** constant for the default font size */
    public static final int DEFAULT_FONT_SIZE = 12;

    /** constant for the default font size for time codes */
    public static final int TIMECODE_FONT_SIZE = 10;

    // empty line style

    /** constant for rendering all selected tiers even if a tier is empty in 
     *  a specific block */
    public static final int TEMPLATE = 0;

    /** constant for rendering selected tiers only when they are not empty in
     *  a specific block */
    public static final int HIDE_EMPTY_LINES = 1;

    // sorting style

    /** constant to indicate that the tier order is specified in a user 
     * interface or otherwise */
    public static final int EXTERNALLY_SPECIFIED = 0;

    /** constant indicating the tiers should be shown according to their hierarchy */
    public static final int TIER_HIERARCHY = 1;

    /** constant indicating tiers should be sorted and grouped based on their type */
    public static final int BY_LINGUISTIC_TYPE = 2;

    /** constant indicating tiers should be sorted and grouped based on participant */
    public static final int BY_PARTICIPANT = 3;

    // character encoding

    /** constant for UTF-8 encoding */
    public static final int UTF8 = 0;

    /** constant for ISO-Latin encoding */
    public static final int ISOLATIN = 1;

    /** constant for SIL/Shoebox character encoding */
    public static final int SIL = 2;

    // other

    /** a scale value for dots per pixel conversion (from 72 dpi to 300 dpi) */
    public static double SCALE = 300.0 / 72.0; // from 72 dpi to 300 dpi

    // members	
    private TimeCodedTranscription transcription = null;
    private int width;
    private int height;
    private String[] visibleTiers;
    private boolean tierLabelsShown;
    private long[] visibleTimeInterval;
    private int blockWrapStyle;
    private int lineWrapStyle;
    private boolean timeCodeShown;
    private int timeCodeType;
    private Map<String, Font> fonts;
    private Map<String, Integer> fontSizes;
    private boolean emptySlotsShown;
    private int lineSpacing;
    private int blockSpacing = -1;
    private Annotation activeAnnotation;
    private long[] selection;
    private long mediaTime;
    private int alignmentUnit;
    private int emptyLineStyle; // show full 'template' for block, or hide empty lines
    private int sortingStyle;
    private Map<String, Integer> charEncodings;
    private Metrics metrics;

    //	private BufferedImage bi;
    private boolean forPrinting = false;
    private boolean renderingFirstPage = true;
    private int pageHeight = 0;
    private boolean sorted = false;
    private boolean correctAnnotationTimes = false;

    /**
     * Creates a new Interlinearizer instance
     *
     * @param tr the transcription
     */
    public Interlinearizer(TimeCodedTranscription tr) {
        transcription = tr;

        metrics = new Metrics(tr, this);
        setDefaultValues();
    }

    private void resetMetrics() {
        metrics.reset();
    }

    private void setDefaultValues() {
        tierLabelsShown = true;
        blockWrapStyle = NO_WRAP;
        lineWrapStyle = NO_WRAP;
        timeCodeShown = false;
        timeCodeType = HHMMSSMS;
        emptySlotsShown = false;
        alignmentUnit = PIXELS;
        emptyLineStyle = HIDE_EMPTY_LINES;
        sortingStyle = EXTERNALLY_SPECIFIED;

        // set default visible tiers to all tier names
        if (transcription != null) {
            List<Tier> tiers = transcription.getTiers();
            visibleTiers = new String[tiers.size()];

            for (int i = 0; i < tiers.size(); i++) {
                String tName = tiers.get(i).getName();
                visibleTiers[i] = tName;
            }
        }

        // defaults for font and fontsizes
        fonts = new HashMap<String, Font>();
        fontSizes = new HashMap<String, Integer>();
        charEncodings = new HashMap<String, Integer>();
    }

    /**
     * Renders the interlinear view as an image.
     *
     * @param bi the {@code BufferedImage} to render to
     */
    public void renderView(BufferedImage bi) {
        if (this.isTimeCodeShown()) {
            transcription.prepareTimeCodeRendering(getTimeCodeType(), correctAnnotationTimes);
            addTimeCodeTiers(false);
        }

        calculateMetrics(bi.getGraphics());
    }

    /**
     * Renders as interlinear text.
     *
     * @return an array of string, one line per item
     */
    public String[] renderAsText() {
        if (this.isTimeCodeShown()) {
            transcription.prepareTimeCodeRendering(getTimeCodeType(), correctAnnotationTimes);
            addTimeCodeTiers(true);
        }

        calculateMetrics();

        return ByteRenderer.render(metrics);
    }

    /**
     * Renders to a number of pages, e.g. for printing.
     *
     * @param g the graphics context
     * @param pageWidth the width of the page, in pixels
     * @param pageHeight the height of the page, in pixels
     * @param pageIndex the page index
     *
     * @return true if rendering was successful, false otherwise
     */
    public boolean renderPage(Graphics g, int pageWidth, int pageHeight,
        int pageIndex) {
        boolean pageExists = false;

        this.setWidth(pageWidth);
        this.setHeight(pageHeight);

        // first page
        if (renderingFirstPage) {
            if (this.isTimeCodeShown()) {
                transcription.prepareTimeCodeRendering(getTimeCodeType(), correctAnnotationTimes);
                addTimeCodeTiers(false);
            }

            calculateMetrics(g);
            renderingFirstPage = false;
        }

        // all pages
        pageExists = drawPage(g, pageIndex);

        // no more pages
        if (!pageExists) {
            if (isTimeCodeShown()) {
                transcription.cleanupTimeCodeTiers();
                removeTimeCodeTiers();
            }
        }

        return pageExists;
    }

    /**
     * Adjusts visibleTiers to show time code tiers for 'tier bundles' with one
     * or more visible tiers. The tc tiers are positioned right after the last
     * visible tier of their bundle.
     *
     * @param atTopOfBlock if true, time codes are rendered before the tiers of
     * a block, otherwise the time codes follow the other tiers
     */
    private void addTimeCodeTiers(boolean atTopOfBlock) {
        // clean up first
        removeTimeCodeTiers();

        List<Tier> tcTiers = transcription.getTimeCodeTiers();
        List<String> vTierVector = new ArrayList<String>(Arrays.asList(visibleTiers));

        // if tcTier has a root tier in common with a visible tier, then
        // it is visible. Add at proper position to vTierList.
        for (int j = 0; j < tcTiers.size(); j++) {
            Tier tcTier = tcTiers.get(j);

            // set font size for time code tiers
            // find minimum font size for visible tiers
            int minSize = TIMECODE_FONT_SIZE;

            for (int i = 0; i < vTierVector.size(); i++) {
                int sz = this.getFontSize(vTierVector.get(i));

                if (sz < minSize) {
                    minSize = sz;
                }
            }

            setFontSize(tcTier.getName(), minSize);

            Tier rootTier = transcription.getRootTier(tcTier);

            // iterate over visibleTiers, remember position of last tier
            // that has same root tier as tcTier
            int lastIndex = -1;
            int firstIndex = -1;
            int index = 0;

            for (String visTierName : vTierVector) {
                Tier visTier = transcription.getTranscription().getTierWithId(visTierName);
                Tier rootOfVisTier = transcription.getRootTier(visTier);

                if ((rootOfVisTier != null) && (rootTier == rootOfVisTier)) {
                    lastIndex = index;

                    if (firstIndex == -1) { // not yet set
                        firstIndex = index;
                    }
                }

                index++;
            }

            if (atTopOfBlock && (firstIndex >= 0)) {
                vTierVector.add(firstIndex + 1, tcTier.getName());

                continue;
            }

            if (lastIndex >= 0) {
                vTierVector.add(lastIndex + 1, tcTier.getName());
            }
        }

        int counter = 0;
        String[] newVisTiers = new String[vTierVector.size()];
        Iterator<String> vIter = vTierVector.iterator();

        while (vIter.hasNext()) {
            String newTierName = vIter.next();

            newVisTiers[counter] = newTierName;
            counter++;
        }

        setVisibleTiers(newVisTiers);
    }

    private void removeTimeCodeTiers() {
    	List<String> newVTierVector = new ArrayList<String>();

        for (String visibleTier : visibleTiers) {
            String vTierName = visibleTier;

            if (!vTierName.startsWith(TimeCodedTranscription.TC_TIER_PREFIX)) {
                newVTierVector.add(vTierName);
            } else if (fontSizes.containsKey(vTierName)) {
                fontSizes.remove(vTierName);
            }
        }

        String[] newVisTiers = new String[newVTierVector.size()];

        for (int i = 0; i < newVTierVector.size(); i++) {
            newVisTiers[i] = newVTierVector.get(i);
        }

        setVisibleTiers(newVisTiers);
    }

    private void calculateMetrics(Graphics graphics) {
        resetMetrics();

        SizeCalculator.calculateSizes(metrics, graphics);
        SizeCalculator.calculateUsedWidths(metrics);

        Positioner.calcHorizontalPositions(metrics);

        if ((lineWrapStyle != NO_WRAP) || (blockWrapStyle != NO_WRAP)) {
            Positioner.wrap(metrics);
        }

        if (emptyLineStyle == HIDE_EMPTY_LINES) {
            Positioner.hideEmptyLines(metrics);
        }
    }

    /**
     * Calculates metrics based on byte-wise alignment.
     */
    private void calculateMetrics() {
        resetMetrics();

        SizeCalculator.calculateSizes(metrics);
        SizeCalculator.calculateUsedWidths(metrics);

        Positioner.calcHorizontalPositions(metrics);

        if ((lineWrapStyle != NO_WRAP) || (blockWrapStyle != NO_WRAP)) {
            Positioner.wrap(metrics);
        }

        if (emptyLineStyle == HIDE_EMPTY_LINES) {
            Positioner.hideEmptyLines(metrics);
        }
    }

    /**
     * Renders the output to an image.
     *
     * @param bi the image to render to
     * @param offset horizontal and vertical offset
     */
    public void drawViewOnImage(BufferedImage bi, int[] offset) {
        if (alignmentUnit == PIXELS) { // call to renderView should be consistent with params
            ImageRenderer.render(metrics, bi, offset);
        }
    }

    private boolean drawPage(Graphics g, int pageIndex) {
        boolean pageExists = true;

        if (alignmentUnit == PIXELS) { // call to renderView should be consistent with params
            pageExists = ImageRenderer.render(metrics, g, pageIndex);
        }

        return pageExists;
    }

    // getters and setters

    /**
     * Returns the active annotation.
     * 
     * @return the active annotation
     */
    public Annotation getActiveAnnotation() {
        return activeAnnotation;
    }

    /**
     * Returns the current alignment unit.
     * 
     * @return the current alignment unit, {@link #PIXELS} or {@link #BYTES}
     */
    public int getAlignmentUnit() {
        return alignmentUnit;
    }

    /**
     * Returns the block wrapping style.
     * 
     * @return the current setting for block wrapping, {@link #EACH_BLOCK}, {@link #BLOCK_BOUNDARY}
     */
    public int getBlockWrapStyle() {
        return blockWrapStyle;
    }

    /**
     * Empty slots are positions on a tier where there could be a dependent 
     * annotation, because there is an annotation on the parent tier, but
     * there isn't an annotation yet.
     * 
     * @return if true empty slots are shown (painted), false otherwise 
     */
    public boolean isEmptySlotsShown() {
        return emptySlotsShown;
    }

    /**
     * Returns the font for the specified tier.
     * 
     * @param tierName the tier to get the font for
     *
     * @return a {@code Font} object, the default font if no font has been
     * set for that tier
     */
    public Font getFont(String tierName) {
        Font f = fonts.get(tierName);

        if (f == null) { // use default font
            f = DEFAULTFONT;
        }

        return f;
    }

    /**
     * Returns the font size for the specified tier.
     * 
     * @param tierName the tier to get the font size for
     *
     * @return the font size, the default size if no size has been set for the tier
     */
    public int getFontSize(String tierName) {
        int size = 0;
        Integer sizeInt = fontSizes.get(tierName);

        if (sizeInt != null) {
            size = sizeInt.intValue();
        } else {
            size = DEFAULT_FONT_SIZE;
        }

        return size;
    }

    /**
     * Returns the height of the output.
     * @return the height of the output
     */
    public int getHeight() {
        if (height > 0) {
            return height;
        } else {
            // find width from max horizontally used space
            return metrics.getMaxVerticalPosition();
        }
    }

    /**
     * Returns the line spacing.
     * 
     * @return the current setting for the amount of space between lines,
     * depends on output method, pixels or text 
     */
    public int getLineSpacing() {
        return lineSpacing;
    }

    /**
     * Returns the block spacing.
     * 
     * @return the amount of space between interlinear blocks 
     */
    public int getBlockSpacing() {
        if (blockSpacing < 0) { // default: derived from line spacing

            return 20 + (3 * getLineSpacing());
        } else {
            return blockSpacing;
        }
    }

    /**
     * Sets the amount of space between interlinear blocks.
     * 
     * @param blockSpacing the new block spacing
     */
    public void setBlockSpacing(int blockSpacing) {
        this.blockSpacing = blockSpacing;
    }

    /**
     * Returns the current line wrap style.
     * 
     * @return the setting for line wrapping, one of {@code #WITHIN_BLOCKS}
     *  and {@code #NO_WRAP}
     */
    public int getLineWrapStyle() {
        return lineWrapStyle;
    }

    /**
     * Returns the media time.
     * 
     * @return the current media time
     */
    public long getMediaTime() {
        return mediaTime;
    }

    /**
     * Returns the time selection.
     * 
     * @return the current selected time interval [begin, end] or {@code null}
     */
    public long[] getSelection() {
        return selection;
    }

    /**
     * Returns whether tier labels are shown or hidden.
     * 
     * @return true if tier labels should be shown on the lines, false otherwise
     */
    public boolean isTierLabelsShown() {
        return tierLabelsShown;
    }

    /**
     * Returns whether the time code is shown.
     * 
     * @return true if each block should contain a line with the begin and 
     * end time of the entire block
     */
    public boolean isTimeCodeShown() {
        return timeCodeShown;
    }

    /**
     * Returns the time format to use.
     * 
     * @return the time format to be used when time codes are shown, one of
     * {@link #HHMMSSMS} and {@link #SSMS}
     */
    public int getTimeCodeType() {
        return timeCodeType;
    }

    /**
     * Returns the names of visible tiers.
     * 
     * @return the names of the visible tiers
     */
    public String[] getVisibleTiers() {
        if (sorted) {
            return visibleTiers;
        }

        if (sortingStyle == TIER_HIERARCHY) {
            visibleTiers = sortByHierarchy(visibleTiers);
        } else if (sortingStyle == BY_LINGUISTIC_TYPE) {
            visibleTiers = sortByLinguisticType(visibleTiers);
        } else if (sortingStyle == BY_PARTICIPANT) {
            visibleTiers = sortByParticipant(visibleTiers);
        }

        sorted = true;

        return visibleTiers;
    }

    /**
     * Sorts according to tier hierarchy.
     *
     * @param visibleTiers the tiers to include
     *
     * @return the tier names sorted according to the hierarchy
     */
    private String[] sortByHierarchy(String[] visibleTiers) {
        List<Tier> sortedTiers = new ArrayList<Tier>();
        String[] sortedTierNames = new String[visibleTiers.length];

        List<String> vTierList = Arrays.asList(visibleTiers);

        List<TierImpl> topTiers = ((TranscriptionImpl)transcription.getTranscription()).getTopTiers();

        for (int i = 0; i < topTiers.size(); i++) {
            TierImpl topTier = topTiers.get(i);
            sortedTiers.addAll(transcription.getTierTree(topTier));
        }

        int arrayIndex = 0;

        for (int j = 0; j < sortedTiers.size(); j++) {
            Tier t = sortedTiers.get(j);

            if (vTierList.contains(t.getName())) {
                sortedTierNames[arrayIndex++] = t.getName();
            }
        }

        return sortedTierNames;
    }

    private String[] sortByLinguisticType(String[] visibleTiers) {
        Map<LinguisticType, List<String>> typesHash = new HashMap<LinguisticType, List<String>>();

        LinguisticType notSpecifiedLT = new LinguisticType("NOT_SPECIFIED");
        LinguisticType tcLT = new LinguisticType(TimeCodedTranscription.TC_LING_TYPE);

        for (String tierName : visibleTiers) {
            LinguisticType lt = notSpecifiedLT;
            TierImpl tier = ((TierImpl) transcription.getTranscription()
                                                     .getTierWithId(tierName));

            if (tier != null) {
                lt = tier.getLinguisticType();

                if (lt == null) {
                    lt = notSpecifiedLT;
                }
            } else if (tierName.startsWith(
                        TimeCodedTranscription.TC_TIER_PREFIX)) {
                lt = tcLT;
            }

            List<String> tiersOfType = typesHash.get(lt);

            if (tiersOfType == null) {
                tiersOfType = new ArrayList<String>();
                typesHash.put(lt, tiersOfType);
            }

            tiersOfType.add(tierName);
        }

        List<String> sortedTierNames = new ArrayList<String>();

        for (List<String> value : typesHash.values()) {
            sortedTierNames.addAll(value);
        }

        String[] sortedNameStrings = new String[visibleTiers.length];

        for (int j = 0; j < sortedTierNames.size(); j++) {
            sortedNameStrings[j] = sortedTierNames.get(j);
        }

        return sortedNameStrings;
    }

    private String[] sortByParticipant(String[] visibleTiers) {
        Map<String, List<String>> participantHash = new HashMap<String, List<String>>();

        String notSpecifiedParticipant = "NOT_SPECIFIED";

        for (String tierName : visibleTiers) {
            String participant = notSpecifiedParticipant;
            TierImpl tier = ((TierImpl) transcription.getTranscription()
                                                     .getTierWithId(tierName));

            if (tier != null) {
                participant = tier.getParticipant();

                if (participant == null) {
                    participant = notSpecifiedParticipant;
                }
            }

            List<String> tiersOfParticipant = participantHash.get(participant);

            if (tiersOfParticipant == null) {
                tiersOfParticipant = new ArrayList<String>();
                participantHash.put(participant, tiersOfParticipant);
            }

            tiersOfParticipant.add(tierName);
        }

        List<String> sortedTierNames = new ArrayList<String>();

        for (List<String> value : participantHash.values()) {
            sortedTierNames.addAll(value);
        }

        String[] sortedNameStrings = new String[visibleTiers.length];

        for (int j = 0; j < sortedTierNames.size(); j++) {
            sortedNameStrings[j] = sortedTierNames.get(j);
        }

        return sortedNameStrings;
    }

    /**
     * Returns the visible time interval.
     * 
     * @return an array containing the begin and end time of the interval to
     * be exported or displayed
     */
    public long[] getVisibleTimeInterval() {
        return visibleTimeInterval;
    }

    /**
     * Returns the width of the output.
     * 
     * @return the width of the output
     */
    public int getWidth() {
        if (width > 0) {
            return width;
        } else {
            // find width from max horizontally used space
            return metrics.getMaxHorizontallyUsedWidth();
        }
    }

    /**
     * Sets the active annotation
     *
     * @param annotation the new active annotation
     */
    public void setActiveAnnotation(Annotation annotation) {
        activeAnnotation = annotation;
    }

    /**
     * Sets the alignment unit.
     * 
     * @param i the alignment unit
     * @see #getAlignmentUnit()
     */
    public void setAlignmentUnit(int i) {
        alignmentUnit = i;
    }

    /**
     * Sets the block wrap style to use.
     * 
     * @param i the block wrap style
     * @see #getBlockWrapStyle()
     */
    public void setBlockWrapStyle(int i) {
        blockWrapStyle = i;
    }

    /**
     * Sets whether empty slot should be shown or marked.
     * 
     * @param b if true empty blocks are shown
     * @see #isEmptySlotsShown()
     */
    public void setEmptySlotsShown(boolean b) {
        emptySlotsShown = b;
    }

    /**
     * Sets the output height.
     * 
     * @param i the height for the output
     */
    public void setHeight(int i) {
        height = i;
    }

    /**
     * Sets the line spacing.
     * 
     * @param i the amount for line spacing
     * @see #getLineSpacing()
     */
    public void setLineSpacing(int i) {
        lineSpacing = i;
    }

    /**
     * Sets the line wrap style to use.
     * 
     * @param i the new line wrap style 
     * @see #getLineWrapStyle()
     */
    public void setLineWrapStyle(int i) {
        lineWrapStyle = i;
    }

    /**
     * Sets the current media time.
     * 
     * @param l the current media time
     */
    public void setMediaTime(long l) {
        mediaTime = l;
    }

    /**
     * Sets the selected time interval.
     * 
     * @param ls the selected time interval
     */
    public void setSelection(long[] ls) {
        selection = ls;
    }

    /**
     * Sets whether tier labels should be shown.
     * 
     * @param show if true tier labels are shown in the output
     */
    public void setTierLabelsShown(boolean show) {
        tierLabelsShown = show;
        metrics.showLeftMargin(show);
    }

    /**
     * Sets whether time codes should be shown.
     * 
     * @param b if true time codes are shown in the output
     */
    public void setTimeCodeShown(boolean b) {
        timeCodeShown = b;
    }

    /**
     * Sets the time code format.
     * 
     * @param i the format for the time codes
     * @see #getTimeCodeType()
     */
    public void setTimeCodeType(int i) {
        timeCodeType = i;
    }

    /**
     * Sets the array of visible tiers.
     * 
     * @param strings the visible tiers
     */
    public void setVisibleTiers(String[] strings) {
        visibleTiers = strings;
    }

    /**
     * Sets the visible time interval.
     * 
     * @param ls the time interval to include in the output
     */
    public void setVisibleTimeInterval(long[] ls) {
        visibleTimeInterval = ls;
    }

    /**
     * Sets the output width.
     * 
     * @param i the width for the output
     */
    public void setWidth(int i) {
        width = i;
    }

    /**
     * Sets the font for a specific tier.
     * 
     *@param tierName the tier to set the font for
     *@param f the font to use for the tier
     */
    public void setFont(String tierName, Font f) {
        int fontSize = getFontSize(tierName);
        f = f.deriveFont((float) fontSize);

        fonts.put(tierName, f);
    }

    /**
     * Sets the font size for a specific tier.
     * 
     * @param tierName the tier
     * @param size the size of the font to use for the tier
     */
    public void setFontSize(String tierName, int size) {
        fontSizes.put(tierName, Integer.valueOf(size));
        fonts.put(tierName, getFont(tierName).deriveFont((float) size));
    }

    /**
     * Returns the policy for empty line handling.
     * 
     * @return the setting for empty lines, one of {@link #TEMPLATE} and 
     * {@link #HIDE_EMPTY_LINES}
     */
    public int getEmptyLineStyle() {
        return emptyLineStyle;
    }

    /**
     * Sets the policy for empty lines.
     * 
     * @param i the new setting for empty lines
     * @see #getEmptyLineStyle()
     */
    public void setEmptyLineStyle(int i) {
        emptyLineStyle = i;
    }

    /**
     * Returns the current tier sorting style.
     * @return the setting for tier sorting, one of {@link #EXTERNALLY_SPECIFIED}, 
     * {@link #TIER_HIERARCHY}, {@link #BY_LINGUISTIC_TYPE} and {@link #BY_PARTICIPANT} 
     */
    public int getSortingStyle() {
        return sortingStyle;
    }

    /**
     * Sets the tier sorting style to apply.
     * 
     * @param i the sorting style
     * @see #getSortingStyle()
     */
    public void setSortingStyle(int i) {
        sortingStyle = i;
        sorted = false;
    }

    /**
     * Returns a {@code TimeCodedTranscription}.
     * 
     * @return the transcription containing the data for output
     */
    public TimeCodedTranscription getTranscription() {
        return transcription;
    }

    /**
     * Returns whether the layout is for printing.
     * 
     * @return if true, the interlinear output is for printing, otherwise
     * it is for textual output
     */
    public boolean forPrinting() {
        return forPrinting;
    }

    /**
     * Sets whether the layout is for printing.
     * 
     * @param forPrinting sets the printing flag
     */
    public void setForPrinting(boolean forPrinting) {
        this.forPrinting = forPrinting;
    }

    /**
     * Returns the height of a page.
     * 
     * @return the height in pixels of a single page
     */
    public int getPageHeight() {
        return pageHeight;
    }

    /**
     * Sets the height of a page.
     * 
     * @param height the height for a single page
     */
    public void setPageHeight(int height) {
        pageHeight = height;
    }

    /**
     * Returns the character encoding for a specific tier.
     * 
     * @param tierName the tier to get the encoding for
     *
     * @return the encoding to use for text output of the tier, one of {@link #UTF8}, 
     * {@link #ISOLATIN} and {@link #SIL}
     */
    public int getCharEncoding(String tierName) {
        int encoding = UTF8;

        if (tierName == null) {
            return encoding; // UTF8 is always the default
        }

        if (!charEncodings.containsKey(tierName)) {
            return encoding;
        }

        Integer encodingInt = charEncodings.get(tierName);

        if (encodingInt != null) {
            encoding = encodingInt.intValue();
        }

        return encoding;
    }

    /**
	 * Sets the character encoding for a tier
     * @param tierName the tier
     * @param charEncoding the encoding to use
     * @see #getCharEncoding(String)
     */
    public void setCharEncoding(String tierName, int charEncoding) {
        charEncodings.put(tierName, Integer.valueOf(charEncoding));
    }

    /**
     * Empty horizontal space between neighboring annotations, the value depends
     * on the alignment unit in use
     *
     * @return the amount of empty space
     * @see #getAlignmentUnit()
     */
    public int getEmptySpace() {
        int emptySpace = 10; // 10 pixels in case of image

        if (getAlignmentUnit() == BYTES) {
            emptySpace = 1;
        }

        return emptySpace;
    }

    /**
     * Returns the metrics object.
     * 
     * @return the {@code Metrics} object holding information about positions
     * and dimension etc.
     */
    public Metrics getMetrics() {
        return metrics;
    }
    
    /**
     * Returns whether annotation times should be recalculated.
     * 
     * @return the flag whether annotation times need to be corrected based
     * on a possible offset of the media file
     */
    public boolean getCorrectAnnotationTimes() {
        return correctAnnotationTimes;
    }
    
    /**
     * Sets whether annotation times should be recalculated.
     * 
     * @param correctAnnotationTimes the new flag for correction of annotation
     * time based on media file offset
     */
    public void setCorrectAnnotationTimes(boolean correctAnnotationTimes) {
        this.correctAnnotationTimes = correctAnnotationTimes;
    }
}
