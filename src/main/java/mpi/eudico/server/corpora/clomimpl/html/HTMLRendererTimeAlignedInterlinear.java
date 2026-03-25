package mpi.eudico.server.corpora.clomimpl.html;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.AnnotationDocEncoder;
import mpi.eudico.server.corpora.clom.EncoderInfo;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.util.TimeFormatter;

import java.io.*;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.*;
import javax.swing.SwingConstants;

/**
 * A class that renders time-aligned interlinear gloss content in a html file.
 *
 * @author Steffen Zimmermann
 * @version 1.0
 */
public class HTMLRendererTimeAlignedInterlinear implements AnnotationDocEncoder {

    /** new line string in text document*/
    private static final String NEW_LINE = "\n";
    /** tolerance for an annotation to count as inside a block (large) */
    private int LARGE_BLOCK_TOLERANCE;
    /** tolerance for an annotation to count as inside a block (small) */
    private int SMALL_BLOCK_TOLERANCE;
    /** maximal space for one block, here in milliseconds */
    private int blockSpace;
    /** print only the selected section */
    private boolean selectionOnly;

    private TranscriptionImpl transcription;
    private TAIEncoderInfo encoderInfo;

    /** new line string in HTML */
    private static final String BREAK = "<br>";
    /** underlined start string in HTML */
    private static final String UND_START = "<u>";
    /** underlined end string in HTML */
    private static final String UND_END = "</u>";
    /** bold start string in HTML */
    private static final String BOLD_START = "<b>";
    /** bold end string in HTML */
    private static final String BOLD_END = "</b>";
    /** italic start string in HTML */
    private static final String ITALIC_START = "<i>";
    /** italic end string in HTML */
    private static final String ITALIC_END = "</i>";
    /** white space string in HTML */
    private static final String NBSP = " ";
    /** right arrow symbol string in HTML */
    private static final String RIGHT_ARROW = "&rarr;";
    private static final String BB_MARK = "[";
    private static final String EB_MARK = "]";

    // maintain a map of annotation to the last written char index of that annotation
    private Map<Annotation, Integer> wrapMap = new HashMap<Annotation, Integer>();
    
    /**
     * Constructor.
     */
    public HTMLRendererTimeAlignedInterlinear() {
		super();
		LARGE_BLOCK_TOLERANCE = 60;
        SMALL_BLOCK_TOLERANCE = 30;
	}

	/**
     * Constructor. 
     * 
     * @param transcription the transcription to convert
     * @param encoderInfo the settings for the encoder
     */
    public HTMLRendererTimeAlignedInterlinear(TranscriptionImpl transcription, 
    		TAIEncoderInfo encoderInfo) {
    	this.transcription = transcription;
    	this.encoderInfo = encoderInfo;
    	if (encoderInfo.getTierSettings() == null) {
    		// could / should throw null pointer or illegal argument exception
    		encoderInfo.setTierSettings(new ArrayList<TAITierSetting>(0));
    	}
    	this.blockSpace = encoderInfo.getBlockSpace() * encoderInfo.getTimeUnit();
    	this.selectionOnly = encoderInfo.getBeginTime() > 0 || encoderInfo.getEndTime() < Long.MAX_VALUE;    	
    	
        LARGE_BLOCK_TOLERANCE = 2 * encoderInfo.getTimeUnit();
        SMALL_BLOCK_TOLERANCE = 1 * encoderInfo.getTimeUnit();
    }
    
    /**
     * Implementation of the AnnotationDocEncoder interface method.
     * This modifies several fields that might have been set by the constructor with arguments.
     * 
     * @see AnnotationDocEncoder#encodeAndSave(Transcription, EncoderInfo, List, String)
     * @throws IOException any IO related exception
     */
	@Override
	public void encodeAndSave(Transcription transcription, EncoderInfo encInfo, List<TierImpl> tierOrder,
			String path) throws IOException {
    	this.transcription = (TranscriptionImpl) transcription;
    	this.encoderInfo = (TAIEncoderInfo) encInfo;
    	if (encoderInfo.getTierSettings() == null) {
    		if (tierOrder == null) {
	    		// could / should throw null pointer or illegal argument exception
	    		encoderInfo.setTierSettings(new ArrayList<TAITierSetting>(0));
    		} else {
    			List<TAITierSetting> tierSettings = new ArrayList<TAITierSetting>(tierOrder.size());
    			for (Tier t : tierOrder) {
    				tierSettings.add(new TAITierSetting(t.getName(), true, false, false));
    			}
    			encoderInfo.setTierSettings(tierSettings);
    		}
    	}
		
    	this.blockSpace = encoderInfo.getBlockSpace() * encoderInfo.getTimeUnit();
    	this.selectionOnly = encoderInfo.getBeginTime() > 0 || encoderInfo.getEndTime() < Long.MAX_VALUE;    	
    	
        LARGE_BLOCK_TOLERANCE = 2 * encoderInfo.getTimeUnit();
        SMALL_BLOCK_TOLERANCE = 1 * encoderInfo.getTimeUnit();
        
        renderToFile(new File(path));
	}

    /**
     * Renders (writes) the content to a html File, converting annotation blocks to html tables,
     * using the default utf-8 character encoding
     *
     * @param outFile the File to write the html to
     * @throws IOException any IOException that can occur while writing to a file
     * @throws FileNotFoundException thrown when the export file could not be found
     */
    public void renderToFile(File outFile) throws IOException, FileNotFoundException {
        renderToFile(outFile, "UTF-8");
    }

    /**
     * Renders (writes) the content to a html File, converting annotation blocks to html tables.
     *
     * @param outFile the File to write the html to
     * @param charEncoding the character encoding for output
     * @throws IOException any IOException that can occur while writing to a
     *         file
     * @throws FileNotFoundException thrown when the export file could not be
     *         found
     * @throws NullPointerException when the Interlinear object or the export
     *         file  is <code>null</code>
     */
    public void renderToFile(File outFile, String charEncoding) throws IOException, FileNotFoundException {
        if (transcription == null) {
            throw new NullPointerException("TranscriptionImpl object is null");
        }

        if (outFile == null) {
            throw new NullPointerException("Export file is null");
        }

        //create file writer
        FileWriter writer = null;
        Charset cs = null;
        try {
        	cs = Charset.forName(charEncoding);
        } catch (Throwable thr) {
        	cs = Charset.forName("UTF-8");
        }
        try { 
        	writer = new FileWriter(outFile, cs);
//            FileOutputStream out = new FileOutputStream(outFile);
//            OutputStreamWriter osw;
//            try {
//                osw = new OutputStreamWriter(out, charEncoding);
//            } catch (UnsupportedCharsetException uce) {
//                osw = new OutputStreamWriter(out, "UTF-8");
//            }
//            writer = new BufferedWriter(osw);

            writeHTMLHeader(writer);
            writeTiers(writer);
            writeFooter(writer);
        } finally {
            try {
                writer.close();
            } catch (Exception ee) {

            }
        }
    }

    /**
     * Writes html code to a StringWriter, the content of which is returned.
     * Special html generation is applied in order to be able to use the Java JEditorPane
     * to render the preview. This pane with the HTMLEditorkit does not seem to support all
     * css style attributes.
     * @return the html code as a single string
     */
    public String renderToText() {
        if (transcription == null) {
            throw new NullPointerException("TranscriptionImpl object is null");
        }

        StringWriter writer = new StringWriter(10000);

        try {
            writeHTMLHeader(writer);
            writeTiers(writer);
            writeFooter(writer);
        } catch (IOException ioe) {
            return writer.toString();
        }

        return writer.toString();
    }

    /**
     * Writes the header part of the html file
     * @param writer the writer
     * @throws IOException io exception
     */
    private void writeHTMLHeader(Writer writer) throws IOException {
        // doctype
        writer.write("<!DOCTYPE html>");

        writer.write(NEW_LINE);
        writer.write("<html>" + NEW_LINE + "<head>" + NEW_LINE);

        writer.write("<meta charset=\"UTF-8\"/>" + NEW_LINE);

        writer.write("<title>" + transcription.getName() + "</title>" + NEW_LINE);
        writeStyles(writer);
        writer.write("</head>" + NEW_LINE);
        writer.write("<body>" + NEW_LINE);


        writer.write("<h3>");
        writer.write(transcription.getFullPath());
        writer.write("</h3>");
        writer.write(NEW_LINE);
        writer.write("<p>");
        writer.write(DateFormat.getDateTimeInstance(DateFormat.FULL,
                DateFormat.SHORT, Locale.getDefault()).format(new Date(
                System.currentTimeMillis())));
        writer.write("</p>");
        writer.write(BREAK + NEW_LINE);
    }

    /**
     * Inserts css style sheets into the header of the html document (rather than to a separate css file).
     * Creates a separate style for each tier in the output.
     * @param writer the writer
     * @throws IOException any ioexception
     */
    private void writeStyles(Writer writer) throws IOException {
        writer.write("<style>" + NEW_LINE);
        // body defaults
        writer.write("body {" + NEW_LINE);
        writer.write("    background-color: #FFFFFF;" + NEW_LINE);
        writer.write("    font-family: \"Courier New\";" + NEW_LINE);
        writer.write("    font-weight: normal;" + NEW_LINE);
        writer.write("    font-size: " + encoderInfo.getFontSize() + "px;" + NEW_LINE);
        writer.write("    color: #000000;" + NEW_LINE);
        writer.write("    line-height: 12px;" + NEW_LINE);
        writer.write("    font-style: normal;" + NEW_LINE);
        writer.write("}" + NEW_LINE);

        writer.write("pre {" + NEW_LINE);
        writer.write("    font-family: \"Courier New\";" + NEW_LINE);
        writer.write("    padding: 10px;" + NEW_LINE);
        writer.write("    line-height: 12px;" + NEW_LINE);
        writer.write("}" + NEW_LINE);

        writer.write("p {" + NEW_LINE);
        writer.write("    font-size: " + (encoderInfo.getFontSize() + 2) + "px;" + NEW_LINE);
        writer.write("}" + NEW_LINE);

        writer.write("</style>");
        writer.write(NEW_LINE);
    }

    /**
     * Adds the contents of the annotation blocks to the body of the html file.
     * @param writer the writer
     * @throws IOException any io exception
     */
    private void writeTiers(Writer writer) throws IOException {

        // time counter
        long counter;
        // begin time of each block
        long blockBT;
        // end time of each block
        long blockET;
        // begin of selection / timeline (depending on selection mode)
        long minTime;
        // end of selection / timeline (depending on selection mode)
        long maxTime;

        if (selectionOnly) {
            blockET = encoderInfo.getBeginTime();
            minTime = encoderInfo.getBeginTime();
            maxTime = encoderInfo.getEndTime();

        } else {
            blockET = 0;
            minTime = 0;
            maxTime = 0;

            for (TAITierSetting ts : encoderInfo.getTierSettings()) {
                Tier t = transcription.getTierWithId(ts.getTierName());
                // in principle the annotations should be in time order, 
                // so checking the last annotation should suffice
                
                for (Annotation ann : t.getAnnotations()) {
                    if (maxTime < ann.getEndTimeBoundary()) {
                        maxTime = ann.getEndTimeBoundary();
                    }
                }
            }
        }
        writer.write("<pre>");

        // search refTier:
        TAITierSetting referenceTierSetting = getRefTier();

        // there is no reference tier
        if (referenceTierSetting == null) {
            while (blockET < maxTime) {
                blockBT = blockET;
                blockET = blockBT + blockSpace;
                if (blockET > maxTime) {
                    blockET = maxTime;
                }

                // write annotations (of tiers) inside the current block
                for (int j = 0; j < encoderInfo.getTierSettings().size(); j++) {
                    String tierName = encoderInfo.getTierSettings().get(j).getTierName();
                    Tier t = transcription.getTierWithId(tierName);

                    writeTierLabel(t, writer);
                    writeTierBlock(t, writer, blockBT, blockET);                   
                    // end with a new line
                    writer.write(NEW_LINE);
                }
                if (encoderInfo.isShowTimeLine()) {
                	writeTimeLine(writer, blockBT, blockET);
                }
                // draw line to indicate end of block
                writer.write(UND_START);
                for (int l = 0; l <= (encoderInfo.getLeftMargin() + encoderInfo.getBlockSpace()); l++) {
                    writer.write(NBSP);
                }
                writer.write(UND_END);
                // new line in between blocks
                writer.write(NEW_LINE);
                writer.write(NEW_LINE);
            }
        // there is a reference tier
        } else {
            Tier referenceTier = transcription.getTierWithId(referenceTierSetting.getTierName());
            Annotation refAnn;
            for (int i = 0; i < referenceTier.getAnnotations().size(); i++) {

                refAnn = referenceTier.getAnnotations().get(i);

                if (refAnn.getEndTimeBoundary() > minTime) {
                    // boolean for subtracting i=i-1, if block is split up,
                    // so that refAnn is looked at again
                    boolean iMinusOne = false;

                    // first block: begin block with BT = 0
                    // other blocks: begin block with BT = last ET
                    blockBT = blockET;

                    // break if selection end time is reached
                    if (blockBT >= maxTime  || 
                    		refAnn.getBeginTimeBoundary() > maxTime) {
                        break;
                    }

                    // if space between two reference annotations is big (> 1/2*blockSpace),
                    // then one whole block is dedicated to this space
                    if (refAnn.getBeginTimeBoundary() > (blockBT + blockSpace / 2)) {
                        if (blockBT >= minTime && (blockBT + blockSpace / 2) <= maxTime) {
                            blockET = refAnn.getBeginTimeBoundary();
                            iMinusOne = true;
                            if (blockET > (blockBT + blockSpace)) {
                                // additional wrapping of the size of blockSpace
                                blockET = blockBT + blockSpace;
                            }
                        }
                    }
                    // wrapping within one block, if 'Wrap Within One Block' is selected in the menu
                    else if (refAnn.getEndTimeBoundary() > (blockBT + blockSpace) && encoderInfo.isWrapWithinBlock()) {
                        // additional wrapping of the size of blockSpace
                        blockET = blockBT + blockSpace;
                        iMinusOne = true;
                    }
                    // if (refAnn.getEndTimeBoundary() <= (blockBT + blockSpace) || !wrapWithinBlock)
                    else {
                        // no additional wrapping
                        blockET = refAnn.getEndTimeBoundary();
                    }

                    counter = blockBT;


                    // if next reference annotation fits into the same block, update blockET
                    if (i < (referenceTier.getAnnotations().size() - 1)) {
                        for (int j = i; j < referenceTier.getAnnotations().size(); j++) {
                            Annotation nextRefAnn = referenceTier.getAnnotations().get(i + 1);

                            // break if selection end time is reached
                            if (nextRefAnn.getBeginTimeBoundary() >= maxTime) {
                                break;
                            }

                            if ((nextRefAnn.getEndTimeBoundary() - blockBT) <= blockSpace) {
                                // pad spaces while updating counter
                                while (counter <= blockET) {
                                    counter = counter + encoderInfo.getTimeUnit();
                                }
                                // add one space between two annotations (as in the other tiers as well)
                                counter = counter + encoderInfo.getTimeUnit();

                                counter = counter + nextRefAnn.getValue().length() * encoderInfo.getTimeUnit();
                                blockET = nextRefAnn.getEndTimeBoundary();
                                // now, blockBT is the one of the first annotation in this block
                                // now, blockET is the one of the last annotation in this block

                                i++;
                                if (i < (referenceTier.getAnnotations().size() - 1)) {
                                    refAnn = referenceTier.getAnnotations().get(i + 1);
                                } else {
                                    break;
                                }
                            } else {
                                break;
                            }
                        }
                    }

                    if (blockET > maxTime) {
                        blockET = maxTime;
                    }

                    // write annotations (of tiers) inside the current block
                    for (int j = 0; j < encoderInfo.getTierSettings().size(); j++) {
                        String tierName = encoderInfo.getTierSettings().get(j).getTierName();
                        Tier t = transcription.getTierWithId(tierName);

                        if (! (blockBT >= blockET)) {
                            writeTierLabel(t, writer);
                            writeTierBlock(t, writer, blockBT, blockET);
                            // end with a new line
                            writer.write(NEW_LINE);
                        }
                    }
                    
                    if (encoderInfo.isShowTimeLine()) {
                    	writeTimeLine(writer, blockBT, blockET);
                    }
                    // draw line to indicate end of block
                    writer.write(UND_START);
                    for (int l = 0; l <= (encoderInfo.getLeftMargin() + encoderInfo.getBlockSpace()); l++) {
                        writer.write(NBSP);
                    }
                    writer.write(UND_END);
                    // new line in between blocks
                    writer.write(NEW_LINE);
                    writer.write(NEW_LINE);

                    // when the last refAnn is reached, keep writing the annotations with larger begin times,
                    // so that all of the annotations will be exported. iMinusOne has to be false, so that
                    // you can be sure that the last refAnn is really finished.
                    if (i == referenceTier.getAnnotations().size() - 1 && ! iMinusOne && ! selectionOnly) {
                        while (blockET < maxTime) {
                            blockBT = blockET;
                            blockET = blockBT + blockSpace;
                            if (blockET > maxTime) {
                                blockET = maxTime;
                            }

                            // write annotations (of tiers) inside the current block
                            for (int j = 0; j < encoderInfo.getTierSettings().size(); j++) {
                                String tierName = encoderInfo.getTierSettings().get(j).getTierName();
                                Tier t = transcription.getTierWithId(tierName);

                                writeTierLabel(t, writer);
                                writeTierBlock(t, writer, blockBT, blockET);

                                // end with a new line
                                writer.write(NEW_LINE);
                            }
                            // draw line to indicate end of block
                            writer.write(UND_START);
                            for (int l = 0; l <= (encoderInfo.getLeftMargin() + encoderInfo.getBlockSpace()); l++) {
                                writer.write(NBSP);
                            }
                            writer.write(UND_END);
                            // new line in between blocks
                            writer.write(NEW_LINE);
                            writer.write(NEW_LINE);
                        }
                    }
                    if (iMinusOne) {
                        i--;
                    }
                }
            }
        }
        writer.write("</pre>");
    }

    /**
     * Writes tier labels of a single tier of one block to the file.
     *
     * @param t the tier to write
     * @param writer the buffered writer
     *
     * @throws IOException any ioexception
     */
    private void writeTierLabel(Tier t, Writer writer) throws IOException{
        // tier label
        String label = t.getName();

        if (label.length() > encoderInfo.getLeftMargin()) {
            for (int i = 0; i < label.length(); i++) {
                if ((i + 2) < encoderInfo.getLeftMargin()) {
                    writer.write(label.charAt(i));
                } else {
                    writer.write("..");
                    break;
                }
            }
        } else {
            writer.write(label);
            padSpaces(writer, encoderInfo.getLeftMargin() - label.length());
        }
        writer.write("|");
    }

    /**
     * Writes the contents of the annotations of a single tier of one block to the file.
     *
     * @param t the tier to write
     * @param writer the buffered writer
     *
     * @throws IOException any ioexception
     */
    private void writeTierBlock(Tier t, Writer writer, long blockBT, long blockET) throws IOException {

        TAITierSetting tierSetting = getTierSettingById(t.getName());

        long counter = blockBT;
        for (Annotation ann : t.getAnnotations()) { 
            long bt = ann.getBeginTimeBoundary();
            long et = ann.getEndTimeBoundary();
            if (bt > blockET) {
            	break;
            }
            if (et < blockBT) {
            	continue;
            }
            long localET = et;
            String val = ann.getValue();
            // check if this annotation value is line wrapped
            Integer wrapInd = wrapMap.get(ann);
            if (wrapInd != null && wrapInd<ann.getValue().length() - 2) {
            	int splitInd = wrapInd.intValue();
            	// this jump to the previous word boundary (white space) could be optional
            	int prevSpace = val.lastIndexOf(' ', splitInd);
            	if (prevSpace > 0) {
            		splitInd = prevSpace + 1;
            		wrapMap.put(ann, splitInd);
            	}
            	val = val.substring(splitInd);           	
            }
            //
            
            // annotation ann has to be inside of the block
            if (bt >= blockBT && bt < (blockET - LARGE_BLOCK_TOLERANCE)
                    || et > (blockBT + SMALL_BLOCK_TOLERANCE) && et <= blockET
                    || bt < blockBT && et > blockET) {
            	// test whether begin and/or end boundary marker need to be included
                int bbInt = 0;
                int ebInt = 0;
                if (encoderInfo.isShowAnnotationBoundaries()) {
                	if (bt >= blockBT && bt < (blockET - LARGE_BLOCK_TOLERANCE)) {
                		bbInt = 1;
                	}
                	if (et > (blockBT + SMALL_BLOCK_TOLERANCE) && et <= blockET) {
                		ebInt = 1;
                	}
                }

                // if bt is larger than blockBT, pad spaces
                if (counter < (bt - encoderInfo.getTimeUnit() / 2)) {
                    // pad spaces while updating counter
                    while (counter < (bt - encoderInfo.getTimeUnit() / 2)) {
                        writer.write(NBSP);
                        counter = counter + encoderInfo.getTimeUnit();
                    }
                    // pad one space, to synchronize with other annotations, if no begin boundary marker
                    if (bbInt == 0) {
                    	writer.write(NBSP);
                    	counter = counter + encoderInfo.getTimeUnit();
                    }
                }

                // if et of ann is later than blockET, set localET to ET of that block
                if (et > (blockET + encoderInfo.getTimeUnit() / 2)) {
                    localET = blockET;
                }

                // if et of ann is larger than blockET
                // count counter for character '->'
                int arrowInt;
                if (et > blockET + SMALL_BLOCK_TOLERANCE) {
                    arrowInt = 1;
                } else {
                    arrowInt = 0;
                }

                // if value (name) wouldn't fit for a correct export
                if ((counter + encoderInfo.getTimeUnit() * (val.length() + arrowInt + bbInt + ebInt)) > localET) {
                    if (bbInt == 1) {
                    	writer.write(BB_MARK);
                    	counter = counter + encoderInfo.getTimeUnit();
                    }
                    // according to tierSetting attributes, start underlined, bold, italic
                    if (tierSetting.isUnderlined()) {
                        writer.write(UND_START);
                    }
                    if (tierSetting.isBold()) {
                        writer.write(BOLD_START);
                    }
                    if (tierSetting.isItalic()) {
                        writer.write(ITALIC_START);
                    }

                    // if et of ann is later than blockET
                    // count counter for character '->'
                    if (et > blockET + SMALL_BLOCK_TOLERANCE) {
                        counter = counter + encoderInfo.getTimeUnit();
                    }
                    counter = counter + ebInt * encoderInfo.getTimeUnit();
                    
                    if (counter + 2 * encoderInfo.getTimeUnit() <= localET) {
                        // count counter for characters '..'
                        counter = counter + 2 * encoderInfo.getTimeUnit();
                        
                        int i = 0;                        
                        // write characters of val one by one, keep updating counter                  
                        /*
                        for (/*int i = 0/; i < val.length(); i++) {
                            if (counter < localET) {
                                writer.write(val.charAt(i));
                                counter = counter + encoderInfo.getTimeUnit();
                            } else {
                                // when end of time duration is reached, write '..'
                                writer.write("..");
                                break;
                            }
                        }
                        */
                        // or calculate the number of characters
                        int numChars = (int)Math.ceil(((localET - counter) / (double)encoderInfo.getTimeUnit()));
                        counter = counter + numChars * encoderInfo.getTimeUnit();
                        // special case, because of rounding effects?, numChars == val.length - 2
                        if (numChars == val.length() - 2) {
                        	numChars += 2;
                        	writer.write(val, 0, numChars);
                        } else {
	                        writer.write(val, 0, numChars);
	                        writer.write("..");
                        }
                        i = numChars;
                        // store the new index
                        if (i < val.length() - 1) {
                        	Integer old = wrapMap.get(ann);
                        	if (old != null) {
                        		wrapMap.put(ann, i + old.intValue());
                        	} else {
                        		wrapMap.put(ann, i);
                        	}
                        } else {
                        	// remove or set index to or beyond last character
                        	wrapMap.remove(ann);
                        }
                    } else if (counter + encoderInfo.getTimeUnit() <= localET) {
                        counter = counter + encoderInfo.getTimeUnit();
                        writer.write(".");
                    }
                    
                    // if et of ann is later than blockET
                    // write character '->'
                    if (et > blockET + SMALL_BLOCK_TOLERANCE) {
                        writer.write(RIGHT_ARROW);
                    }

                    // according to tierSetting attributes, end underlined, bold, italic
                    if (tierSetting.isItalic()) {
                        writer.write(ITALIC_END);
                    }
                    if (tierSetting.isBold()) {
                        writer.write(BOLD_END);
                    }
                    if (tierSetting.isUnderlined()) {
                        writer.write(UND_END);
                    }
                    
                    if (ebInt == 1) {
                    	writer.write(EB_MARK);
                    	//counter = counter + encoderInfo.getTimeUnit();//already incremented above
                    }
                    // space between annotations (takes up one space)
                    if (!encoderInfo.isShowAnnotationBoundaries()) {
                    	writer.write(NBSP);
                    	counter = counter + encoderInfo.getTimeUnit();
                    }

                } // value (name) fits in the designated space
                else {
                    counter = counter + encoderInfo.getTimeUnit() * (val.length() + ebInt);
                    if (bbInt == 1) {
                    	writer.write(BB_MARK);
                    	counter = counter + encoderInfo.getTimeUnit();
                    }
                    
                    // according to tierSetting attributes, start underlined, bold, italic
                    if (tierSetting.isUnderlined()) {
                        writer.write(UND_START);
                    }
                    if (tierSetting.isBold()) {
                        writer.write(BOLD_START);
                    }
                    if (tierSetting.isItalic()) {
                        writer.write(ITALIC_START);
                    }

                    // if et of ann is later than blockET
                    // count counter for character '->'
                    if (et > blockET + SMALL_BLOCK_TOLERANCE) {
                        counter = counter + encoderInfo.getTimeUnit();
                    }

                    //pad (underlined) spaces while updating counter, if it is not the reference tier
                    // or if the text should be exported left aligned
                    if (! tierSetting.isReference() && encoderInfo.getTextAlignment() != SwingConstants.LEFT) {
                        while (counter < localET) {
                            writer.write(NBSP);
                            counter = counter + encoderInfo.getTimeUnit();
                        }
                    }

                    writer.write(val);

                    //pad (underlined) spaces while updating counter, if it is the reference tier
                    if (tierSetting.isReference() || encoderInfo.getTextAlignment() == SwingConstants.LEFT) {
                        while (counter < localET) {
                            writer.write(NBSP);
                            counter = counter + encoderInfo.getTimeUnit();
                        }
                    }
                    // if et of ann is later than blockET
                    // write character '->'
                    if (et > blockET + SMALL_BLOCK_TOLERANCE) {
                        writer.write(RIGHT_ARROW);
                    }

                    // according to tierSetting attributes, end underlined, bold, italic
                    if (tierSetting.isItalic()) {
                        writer.write(ITALIC_END);
                    }
                    if (tierSetting.isBold()) {
                        writer.write(BOLD_END);
                    }
                    if (tierSetting.isUnderlined()) {
                        writer.write(UND_END);
                    }
                    
                    if (ebInt == 1) {
                    	writer.write(EB_MARK);
                    }
                    // space between annotations (takes up one space)
                    if (!encoderInfo.isShowAnnotationBoundaries()) {
                    	writer.write(NBSP);                   
                    	counter = counter + encoderInfo.getTimeUnit();
                    }
                    // in case this was the remainder of a wrapped annotation, remove it (so that it
                    // might be printed again in the next block
                    wrapMap.remove(ann);
                }
            }
        }
    }
    
    private void writeTimeLine(Writer writer, long blockBT, long blockET) throws IOException {
    	String tc = "Time";
        if (tc.length() >  encoderInfo.getLeftMargin()) {
            for (int i = 0; i < tc.length(); i++) {
                if ((i + 2) < encoderInfo.getLeftMargin()) {
                    writer.write(tc.charAt(i));
                } else {
                    writer.write("..");
                    break;
                }
            }
        } else {
            writer.write(tc);
            padSpaces(writer, encoderInfo.getLeftMargin() - tc.length());
        }
        writer.write("|");
        // write the time ruler
        long counter = blockBT;
        String blockBTString = null;
        switch (encoderInfo.getTimeFormat()) {
        case HHMMSSMS:
        	blockBTString = TimeFormatter.toString(blockBT);
        	break;
        case SSMS:
        	blockBTString = TimeFormatter.toSSMSString(blockBT);
        	break;
        	
        default:
        	blockBTString = String.valueOf(blockBT);
        }
        //blockBTString = timeFormat.format(blockBT / 1000d);
    	writer.write(blockBTString);
    	counter = counter + blockBTString.length() * encoderInfo.getTimeUnit();
    	while (counter < blockET) {
    		counter = counter + encoderInfo.getTimeUnit();
    		if (counter % 1000 < encoderInfo.getTimeUnit()) {// or < timeUnit
    			writer.write("\u00a6");// broken vertical bar, or 01c2, double barred pipe?
    		} else {
    			writer.write("\u00b7");// middle dot
    		}
    	}
    	
    	writer.write(NEW_LINE);
        
    }

    /**
     * Finishes the html document.
     * @param writer the writer
     * @throws IOException any io exception
     */
    private void writeFooter(Writer writer) throws IOException {
        writer.write("</body>" + NEW_LINE);
        writer.write("</html>");
    }

    /**
     * Do padding with spaces and/or a tab.
     * <p>
     * There are two checkboxes: isInsertTabs() which adds tabs to the normal spaces,
     * and isTabsReplaceSpaces() which may additionally be set to omit the spaces.
     * The second is greyed out if the first is not enabled.
     *
     * @param writer
     * @param pad
     * @throws IOException
     */
    private void padSpaces(Writer writer, int pad)
            throws IOException {
        for (int i = 0; i < pad; i++) {
            writer.write(NBSP);
        }
    }

    /**
     * Validates that tierSettings has exactly one reference tier.
     * If this is not the case, the first tier is used as default.
     */
    private TAITierSetting getRefTier() {
        for (TAITierSetting ts : encoderInfo.getTierSettings()) {
            if (ts.isReference()) {
                return ts;
            }
        }
        return null;
    }

    /** Gets a tierSetting by Id
     *
     * @return the wanted tierSetting, otherwise null
     * */
    private TAITierSetting getTierSettingById(String tierName) {
        for (TAITierSetting ts : encoderInfo.getTierSettings()) {
            if (ts.getTierName().equals(tierName)) {
                return ts;
            }
        }
        return null;
    }


}
