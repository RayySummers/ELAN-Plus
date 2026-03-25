package mpi.eudico.server.corpora.clomimpl.delimitedtext;

import mpi.eudico.server.corpora.clom.DecoderInfo;
import mpi.eudico.server.corpora.clom.Property;
import mpi.eudico.server.corpora.clomimpl.abstr.ExternalReferenceImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.LicenseRecord;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.Parser;
import mpi.eudico.server.corpora.clomimpl.abstr.ParserFactory;
import mpi.eudico.server.corpora.clomimpl.dobes.ACMTranscriptionStore;
import mpi.eudico.server.corpora.clomimpl.dobes.AnnotationRecord;
import mpi.eudico.server.corpora.clomimpl.dobes.AnnotationRecordComparator;
import mpi.eudico.server.corpora.clomimpl.dobes.CVRecord;
import mpi.eudico.server.corpora.clomimpl.dobes.LanguageRecord;
import mpi.eudico.server.corpora.clomimpl.dobes.LexiconServiceRecord;
import mpi.eudico.server.corpora.clomimpl.dobes.LingTypeRecord;
import mpi.eudico.server.corpora.clomimpl.dobes.TimeSlotRecord;
import mpi.eudico.server.corpora.clomimpl.dobes.TimeSlotRecordComparator;
import mpi.eudico.server.corpora.clomimpl.reflink.RefLinkSetRecord;

import static mpi.eudico.server.corpora.util.ServerLogger.LOG;
import mpi.eudico.util.TimeFormatter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * A Parser for comma separated values or tab-delimited text files. Annotations
 * are extracted based on information on the structure of the  file. This
 * structure is stored in a decoder info object.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class DelimitedTextParser extends Parser {
    private DelimitedTextDecoderInfo decoderInfo;
    private DelimitedTextReader reader;
    private String lastParsedFile;
    private static final String TS_ID_PREFIX = "ts";
    private static final String ANN_PREFIX = "a";
    private static final String TYPE_NAME = "imported-sep";
    private ArrayList<LingTypeRecord> lingTypeRecords = new ArrayList<LingTypeRecord>();
    private ArrayList<String> tierNames = new ArrayList<String>();
    private ArrayList<String> timeOrder = new ArrayList<String>(); // in the end a list of time slot id's
    private ArrayList<TimeSlotRecord> timeSlots = new ArrayList<TimeSlotRecord>(); // of TimeSlotRecords
    private HashMap<String, ArrayList<AnnotationRecord>> recordsPerTier = new HashMap<String, ArrayList<AnnotationRecord>>();
    
    // for import in combination with template
    private Parser templateParser;
    
    /** a map with tier name - List with Annotation Records pairs */
    private final Map<String, List<AnnotationRecord>> modRecordsPerTier = new HashMap<String, List<AnnotationRecord>>();


    /**
     * Creates a new DelimitedTextParser instance
     */
    public DelimitedTextParser() {
        super();
    }

    /**
     * Creates a new DelimitedTextParser instance
     *
     * @param decoderInfo the decoder info object containing information on how
     *        to interpret the rows
     */
    public DelimitedTextParser(DelimitedTextDecoderInfo decoderInfo) {
        super();
        this.decoderInfo = decoderInfo;
        parse(decoderInfo.getSourceFilePath());
    }

    /**
     * Reads the delimited text file (specified in the decoder info object) and
     * creates  annotation and tier objects etc. based on the information in
     * the decoder info object.
     *
     * @param fileName the file to parse
     */
    public void parse(String fileName) {
        if ((fileName == null) || fileName.equals(lastParsedFile)) {
            return;
        }

        // reset
        lingTypeRecords.clear();
        tierNames.clear();
        timeOrder.clear();
        timeSlots.clear();
        recordsPerTier.clear();

        try {
            reader = new DelimitedTextReader(fileName);

            if (decoderInfo == null) {
            } else {
                reader.setDelimiter(decoderInfo.getDelimiter()); // default tab

                if (decoderInfo.isSingleAnnotationPerRow()) {
                    parseSingleAnnColumn();
                    sortTimeSlots();
                } else {
                    parseMultiAnnColumns();
                    sortTimeSlots();
                }
            }
        } catch (FileNotFoundException fnfe) {
            LOG.warning("The file is not found: " + fileName);

            return;
        }
        
        if (decoderInfo.getTemplateFilePath() != null) {
        	int version = ACMTranscriptionStore.eafFileFormatTaster(decoderInfo.getTemplateFilePath());
        	templateParser = ParserFactory.getParser(version);
        	
        	updateAnnotationRecordsForTemplate();
        }
        
        lastParsedFile = fileName;
    }

    /**
     * Parse a table file with 1 annotation per row.
     */
    private void parseSingleAnnColumn() {
        int[] annColumn = decoderInfo.getAnnotationColumns(); // should be length 1 in this case
        int annIndex = -1;

        if ((annColumn != null) && (annColumn.length >= 1)) {
            annIndex = annColumn[0];
        }

        int tierIndex = decoderInfo.getTierColumnIndex();
        int btIndex = decoderInfo.getBeginTimeColumn();
        int etIndex = decoderInfo.getEndTimeColumn();
        int durIndex = decoderInfo.getDurationColumn();
        // possible detected time formats, null if not stored
        TimeFormatter.TIME_FORMAT btFormat = decoderInfo.getTimeFormat(btIndex);
        TimeFormatter.TIME_FORMAT etFormat = decoderInfo.getTimeFormat(etIndex);
        TimeFormatter.TIME_FORMAT durFormat = decoderInfo.getTimeFormat(durIndex);

        int[] inclColumns = decoderInfo.getIncludedColumns();
        Map<Integer, String> colToTierName = decoderInfo.getColumnsWithTierNames();
        String defTier = "Tier-1";

        // check if the tier name is in the header
        if ((annIndex > -1) && (colToTierName != null)) {
        	for (Map.Entry<Integer, String> columnEntry : colToTierName.entrySet()) {
        		Integer keyInt = columnEntry.getKey();

                if (keyInt.intValue() == annIndex) {
                    String tName = columnEntry.getValue();

                    if (tName != null) {// superfluous now?
                        defTier = tName;
                    }
                }
            }
        }

        // check the indices and calculate result indices on the basis of the included columns array
        for (int i = 0; i < inclColumns.length; i++) {
            if (inclColumns[i] == annIndex) {
                annIndex = i;
            } else if (inclColumns[i] == tierIndex) {
                tierIndex = i;
            } else if (inclColumns[i] == btIndex) {
                btIndex = i;
            } else if (inclColumns[i] == etIndex) {
                etIndex = i;
            } else if (inclColumns[i] == durIndex) {
                durIndex = i;
            }
        }

        try {
            List<String[]> rows = reader.getRowDataForColumns(decoderInfo.getFirstRowIndex(),
                    inclColumns);
            String curTier = defTier;
            int tsCount = 1;
            int annCount = 1;

            for (int i = 0; i < rows.size(); i++) {
            	String[]row = rows.get(i);
                long bt = -1;
                long et = -1;
                long dur = -1;
                
                if ((btIndex > -1) && (btIndex < row.length)) {
                    bt = TimeFormatter.toMilliSeconds(row[btIndex], btFormat);
                }

                if ((etIndex > -1) && (etIndex < row.length)) {
                    et = TimeFormatter.toMilliSeconds(row[etIndex], etFormat);
                } else if ((durIndex > -1) && (durIndex < row.length)) {
                    // use duration
                    dur = TimeFormatter.toMilliSeconds(row[durIndex], durFormat);

                    if ((dur == -1) || (bt == -1)) {
                        et = -1; // if there is no begintime but there is a duration, ignore it
                    } else {
                        et = bt + dur;
                    }
                }
                String ann = "";
                
                if ((annIndex > -1) && (annIndex < row.length)) {
                    ann = row[annIndex];
                }

                if ((tierIndex > -1) && (tierIndex < row.length)) {
                    curTier = row[tierIndex];

                    if (curTier.length() == 0) {
                        curTier = defTier;
                    }
                } else {
                    curTier = defTier;
                }

                if (!tierNames.contains(curTier)) {
                    tierNames.add(curTier);
                    recordsPerTier.put(curTier, new ArrayList<AnnotationRecord>());
                }
                // check the skip empty cells flag
                if (decoderInfo.isSkipEmptyCells() && ann.length() == 0) {
                	continue;
                }
                AnnotationRecord annRec = new AnnotationRecord();
                annRec.setAnnotationType(AnnotationRecord.ALIGNABLE);
                annRec.setValue(ann);
                annRec.setAnnotationId(ANN_PREFIX + annCount++);
                TimeSlotRecord tsRec = new TimeSlotRecord(tsCount++, bt);
                annRec.setBeginTimeSlotRecord(tsRec);
                timeSlots.add(tsRec);
                tsRec = new TimeSlotRecord(tsCount++, et);
                annRec.setEndTimeSlotRecord(tsRec);
                timeSlots.add(tsRec);

                //annotationRecords.add(annRec);
                recordsPerTier.get(curTier).add(annRec);
            }
        } catch (IOException ioe) {
            LOG.severe("Error retrieving rowdata: " + ioe.getMessage());
        }

        AnnotationRecordComparator comp = new AnnotationRecordComparator(true);
        
        for (List<AnnotationRecord> records : recordsPerTier.values()) {
        	Collections.sort(records, comp);
        }

        // calculate unaligned slots per tier, then reorder all slots and regenerate the id's.
        for (List<AnnotationRecord> records : recordsPerTier.values()) {
            calculateUnalignedSlots(records);
        }

        // correct overlaps??
        for (List<AnnotationRecord> records : recordsPerTier.values()) {
            correctOverlaps(records);
        }
    }

    /**
     * Parse a table file with more than 1 annotation per row, with, possibly,
     * the tier name  in the 'header' or first row.
     */
    private void parseMultiAnnColumns() {
        // check if all info that is expected is availble
        // int[] annColumns = decoderInfo.getAnnotationColumns();
        int[] inclColumns = decoderInfo.getIncludedColumns();
        Map<Integer, String> orgColsToTierName = decoderInfo.getColumnsWithTierNames();
        Map<Integer, String> colsToTierName = new HashMap<Integer, String>(orgColsToTierName.size());

        int btIndex = decoderInfo.getBeginTimeColumn();
        int etIndex = decoderInfo.getEndTimeColumn();
        int durIndex = decoderInfo.getDurationColumn();
        // possible detected time formats, null if not stored
        TimeFormatter.TIME_FORMAT btFormat = decoderInfo.getTimeFormat(btIndex);
        TimeFormatter.TIME_FORMAT etFormat = decoderInfo.getTimeFormat(etIndex);
        TimeFormatter.TIME_FORMAT durFormat = decoderInfo.getTimeFormat(durIndex);

        // check the indices and calculate result indices on the basis of the included columns array
        for (Map.Entry<Integer, String> columnEntry : orgColsToTierName.entrySet()) {
        	Integer colIndex = columnEntry.getKey();

            for (int i = 0; i < inclColumns.length; i++) {
                if (inclColumns[i] == colIndex.intValue()) {
                	String tierName = columnEntry.getValue();
                    colsToTierName.put(Integer.valueOf(i), tierName);

                    if (!tierNames.contains(tierName)) {
                        tierNames.add(tierName);
                        recordsPerTier.put(tierName, new ArrayList<AnnotationRecord>());
                    }
                }
            }
        }

        Integer[] tierCols = new Integer[colsToTierName.size()];
        Iterator<Integer> colIter = colsToTierName.keySet().iterator();

        int z = 0;

        while (colIter.hasNext()) {
            tierCols[z] = colIter.next();
            z++;
        }

        for (int i = 0; i < inclColumns.length; i++) {
            if (inclColumns[i] == btIndex) {
                btIndex = i;
            } else if (inclColumns[i] == etIndex) {
                etIndex = i;
            } else if (inclColumns[i] == durIndex) {
                durIndex = i;
            }
        }

        // start parsing rows
        Map<TimeSlotRecord, ArrayList<TimeSlotRecord>> slotToSlots = 
        		new HashMap<TimeSlotRecord, ArrayList<TimeSlotRecord>>();
        String refTier = null; // for single calculation of timeslots

        try {
            List<String[]> rows = reader.getRowDataForColumns(decoderInfo.getFirstRowIndex(),
                    inclColumns);
            int tsCount = 1;
            int annCount = 1;

            for (int i = 0; i < rows.size(); i++) {
                long bt = -1;
                long et = -1;
                long dur = -1;
                String[] row = rows.get(i);

                if ((btIndex > -1) && (btIndex < row.length)) {
                    bt = TimeFormatter.toMilliSeconds(row[btIndex], btFormat);
                }

                if ((etIndex > -1) && (etIndex < row.length)) {
                    et = TimeFormatter.toMilliSeconds(row[etIndex], etFormat);
                } else if ((durIndex > -1) && (durIndex < row.length)) {
                    // use duration
                    dur = TimeFormatter.toMilliSeconds(row[durIndex], durFormat);

                    if ((dur == -1) || (bt == -1)) {
                        et = -1; // if there is no begintime but there is a duration, ignore it
                    } else {
                        et = bt + dur;
                    }
                }
                
                TimeSlotRecord curRefBTTS = null;
                TimeSlotRecord curRefETTS = null;
                
                for (int j = 0; j < tierCols.length; j++) {
                	String ann = "";
                    if ((tierCols[j].intValue() > -1) &&
                            (tierCols[j].intValue() < row.length)) {
                        ann = row[tierCols[j].intValue()];
                    }

                    String curTier = colsToTierName.get(tierCols[j]);

                    if ((j == 0) && (refTier == null)) {
                        refTier = curTier;
                    }

                    /*
                       if (!tierNames.contains(curTier)) {
                           tierNames.add(curTier);
                           recordsPerTier.put(curTier, new ArrayList());
                       }
                     */
                    AnnotationRecord annRec = new AnnotationRecord();
                    annRec.setAnnotationType(AnnotationRecord.ALIGNABLE);
                    annRec.setValue(ann);
                    annRec.setAnnotationId(ANN_PREFIX + annCount++);
                    TimeSlotRecord tsRec = new TimeSlotRecord(tsCount++, bt);
                    annRec.setBeginTimeSlotRecord(tsRec);
                    timeSlots.add(tsRec);
                    tsRec = new TimeSlotRecord(tsCount++, et);
                    annRec.setEndTimeSlotRecord(tsRec);
                    timeSlots.add(tsRec);

                    //annotationRecords.add(annRec);
                    recordsPerTier.get(curTier).add(annRec);

                    if (j == 0) {
                        curRefBTTS = annRec.getBeginTimeSlotRecord();
                        curRefETTS = annRec.getEndTimeSlotRecord();
                        slotToSlots.put(curRefBTTS, new ArrayList<TimeSlotRecord>(5));
                        slotToSlots.put(curRefETTS, new ArrayList<TimeSlotRecord>(5));
                    } else {
                        slotToSlots.get(curRefBTTS).add(annRec.getBeginTimeSlotRecord());
                        slotToSlots.get(curRefETTS).add(annRec.getEndTimeSlotRecord());
                    }
                }
            }
        } catch (IOException ioe) {
            LOG.severe("Error retrieving rowdata: " + ioe.getMessage());
        }

        if (!decoderInfo.isSkipEmptyCells()){ // not skipping empty annotations, default behavior
	        // calculate unaligned slots for the ref tier, update 'depending' time slots
	        List<AnnotationRecord> records = recordsPerTier.get(refTier);
	        // sort records
	        Collections.sort(records, new AnnotationRecordComparator(true));
	
	        calculateUnalignedSlots(records);
	        // correct overlaps
	        correctOverlaps(records);
	
	        // now update the referring time slots
	        for (Map.Entry<TimeSlotRecord, ArrayList<TimeSlotRecord>> recordEntry : slotToSlots.entrySet()) {
	        	TimeSlotRecord keyRec = recordEntry.getKey();
	        	ArrayList<TimeSlotRecord> refSlots = recordEntry.getValue();
	
	            for (TimeSlotRecord valRec : refSlots) {
	                valRec.setValue(keyRec.getValue());
	            }
	        }
        } else {// delete empty annotations, calculate overlaps per tier
        	AnnotationRecordComparator annRecComp = new AnnotationRecordComparator(true);
        	for (int i = 0; i < tierNames.size(); i++) {
        		String tierNm = tierNames.get(i);
        		List<AnnotationRecord> records = recordsPerTier.get(tierNm);
                // remove empty annotations
                removeEmptyRecords(records);
                // sort records
                Collections.sort(records, annRecComp);

                calculateUnalignedSlots(records);
                // correct overlaps
                correctOverlaps(records);
        	}
        }
    }

    /**
     * Sorts the time slot records using a TimeSlotRecordComparator and then
     * regenerates the id's.
     */
    private void sortTimeSlots() {
        if ((timeSlots != null) && (timeSlots.size() > 1)) {
            Collections.sort(timeSlots, new TimeSlotRecordComparator());

            for (int i = 0; i < timeSlots.size(); i++) {
            	TimeSlotRecord tsr = timeSlots.get(i);
                tsr.setId(i + 1);
            }
        }
    }

    /**
     * Calculates time values for (series of) unaligned time slots for the
     * annotation records of one tier.
     *
     * @param records a list containing AnnotationRecord objects
     */
    private void calculateUnalignedSlots(List<AnnotationRecord> records) {
        if ((records == null) || (records.size() == 0)) {
            return;
        }

        long curAdv = 0L;

        for (int i = 0; i < records.size(); i++) {
        	AnnotationRecord annRec = records.get(i);
        	TimeSlotRecord tsBRec = annRec.getBeginTimeSlotRecord();
        	TimeSlotRecord tsERec = annRec.getEndTimeSlotRecord();

            if (tsBRec.getValue() < 0) {
                if (i > 0) {
                    if (tsERec.getValue() < 0) {
                        tsBRec.setValue(curAdv); // same as last end time
                    } else {
                        tsBRec.setValue(Math.max(curAdv,
                                tsERec.getValue() -
                                decoderInfo.getDefaultDuration()));
                    }
                } else {
                    tsBRec.setValue(0);
                }
            } else {
                curAdv = Math.max(curAdv, tsBRec.getValue());
            }

            if (tsERec.getValue() < 0) {
                // find the next aligned slot
                int numUnalignedSlots = 1;
                long nextTime = 0L;

                for (int j = i + 1; j < records.size(); j++) {
                	AnnotationRecord nextRec = records.get(j);

                    if (nextRec.getBeginTimeSlotRecord().getValue() >= 0) {
                        nextTime = nextRec.getBeginTimeSlotRecord().getValue();

                        break;
                    }

                    if (nextRec.getEndTimeSlotRecord().getValue() < 0) {
                        numUnalignedSlots++;
                    } else {
                        nextTime = nextRec.getEndTimeSlotRecord().getValue();

                        break;
                    }
                }

                if (numUnalignedSlots > 0) {
                    if (nextTime == 0) { // if there are no aligned slots at all
                        nextTime = curAdv +
                            (numUnalignedSlots * decoderInfo.getDefaultDuration());
                    }

                    long avgDur = (nextTime - curAdv) / numUnalignedSlots;

                    avgDur = Math.min(avgDur, decoderInfo.getDefaultDuration());

                    int j = i;

                    for (int k = 0;
                            ((j < records.size()) && (k < numUnalignedSlots));
                            j++, k++) {
                    	AnnotationRecord nextRec = records.get(j);

                        if (k == 0) {
                            nextRec.getEndTimeSlotRecord()
                                   .setValue(curAdv + avgDur);
                        } else {
                            if (nextRec.getBeginTimeSlotRecord().getValue() < 0) {
                                nextRec.getBeginTimeSlotRecord()
                                       .setValue(curAdv + (avgDur * k));
                            }

                            if (nextRec.getEndTimeSlotRecord().getValue() < 0) {
                                nextRec.getEndTimeSlotRecord()
                                       .setValue(curAdv + (avgDur * (k + 1)));
                            }
                        }
                    }

                    curAdv = nextTime;
                    i = j - 1;
                } else {
                    tsERec.setValue(Math.min(nextTime,
                            curAdv + decoderInfo.getDefaultDuration()));
                    curAdv = tsERec.getValue();
                }
            } else {
                // there is an endtime
                curAdv = tsERec.getValue();
            }
        }
    }

    /**
     * Tries to detect and correct possible overlaps within a single tier.
     *
     * @param annRecords the annotation records of one tier
     */
    private void correctOverlaps(List<AnnotationRecord> annRecords) {

        for (int i = 0; i < (annRecords.size() - 1); i++) {
        	AnnotationRecord annRec = annRecords.get(i);
        	AnnotationRecord nextRec = annRecords.get(i + 1);
        	TimeSlotRecord tsERec = annRec.getEndTimeSlotRecord();
        	TimeSlotRecord tsBRec = nextRec.getBeginTimeSlotRecord();

            if (tsERec.getValue() > tsBRec.getValue()) {
                tsERec.setValue(tsBRec.getValue());
            }
        }
    }
    
    private void removeEmptyRecords (List<AnnotationRecord> annRecords) {
    	for (int i = annRecords.size() - 1; i >= 0; i--) {
    		AnnotationRecord annRec = annRecords.get(i);
    		if (annRec.getValue() == null || annRec.getValue().length() == 0) {
    			annRecords.remove(i);
    			timeSlots.remove(annRec.getBeginTimeSlotRecord());
    			timeSlots.remove(annRec.getEndTimeSlotRecord());
    		}
    	}
    }

    //########### Interface getters, not all applicable ######################
    /**
     * Returns an empty List
     *
     * @param fileName the file to parse (for historic reasons)
     *
     * @return an empty list
     */
    @Override
	public ArrayList<MediaDescriptor> getMediaDescriptors(String fileName) {
        parse(fileName);

        return new ArrayList<MediaDescriptor>();
    }

    /**
     * Returns a list of Linguistic Type Records
     *
     * @param fileName the file to parse (for historic reasons)
     *
     * @return a list of ling. type records, by default only one lin. type
     */
    @Override
	public ArrayList<LingTypeRecord> getLinguisticTypes(String fileName) {
        parse(fileName);

        if (lingTypeRecords.size() == 0) {
            LingTypeRecord lt = new LingTypeRecord();
            lt.setLingTypeId(TYPE_NAME);
            lt.setTimeAlignable("true");

            lingTypeRecords.add(lt);
        }

        if (templateParser != null) {
        	return (ArrayList<LingTypeRecord>) mergedTypeList(templateParser.getLinguisticTypes(
        			decoderInfo.getTemplateFilePath()), lingTypeRecords);
        }
        
        return lingTypeRecords;
    }

    /**
     * Returns an ordered list of time slot id's (Strings)
     *
     * @param fileName the file to parse (for historic reasons)
     *
     * @return a list of time slot id's
     */
    @Override
	public ArrayList<String> getTimeOrder(String fileName) {
        parse(fileName);
        timeOrder.clear();

        for (int i = 0; i < timeSlots.size(); i++) {
        	TimeSlotRecord tsr = timeSlots.get(i);
            timeOrder.add(TS_ID_PREFIX + tsr.getId());
        }

        return timeOrder;
    }

    /**
     * Returns a map containing id - time key value pairs
     *
     * @param fileName the file to parse (for historic reasons)
     *
     * @return a map containing id - time key value pairs
     */
    @Override
	public HashMap<String, String> getTimeSlots(String fileName) {
        parse(fileName);

        HashMap<String, String> tsMap = new HashMap<String, String>(timeSlots.size());

        for (int i = 0; i < timeSlots.size(); i++) {
        	TimeSlotRecord tsr = timeSlots.get(i);
            // note: could use the timeOrder objects (if we are sure of the order in
            // which the methods are called)?
            tsMap.put(TS_ID_PREFIX + tsr.getId(), Long.toString(tsr.getValue()));
        }

        return tsMap;
    }

    /**
     * Returns a list of tier names.
     *
     * @param fileName the file to parse (for historic reasons)
     *
     * @return a list of tier names
     */
    @Override
	public ArrayList<String> getTierNames(String fileName) {
        parse(fileName);

        if (templateParser != null) {
        	return (ArrayList<String>) mergedList(templateParser.getTierNames(
        			decoderInfo.getTemplateFilePath()), tierNames);
        }
        
        return tierNames;
    }

    /**
     * Returns null
     *
     * @param tierName the tier
     * @param fileName the file to parse (for historic reasons)
     *
     * @return null
     */
    @Override
	public String getParticipantOf(String tierName, String fileName) {
        parse(fileName);
        
        if (templateParser != null) {
        	return templateParser.getParticipantOf(tierName, decoderInfo.getTemplateFilePath());
        }

        return null;
    }

    /**
     * Returns the linguistic type name for a tier.
     *
     * @param tierName the tier
     * @param fileName the file to parse (for historic reasons)
     *
     * @return the linguistic type name, defaults to "default"
     */
    @Override
	public String getLinguisticTypeIDOf(String tierName, String fileName) {
        parse(fileName);

        if (templateParser != null) {
        	String typeId = templateParser.getLinguisticTypeIDOf(tierName, decoderInfo.getTemplateFilePath());
        	if (typeId != null && !typeId.isEmpty()) {
        		return typeId;
        	}
        }
        
        return TYPE_NAME;
    }

    /**
     * Returns the parent tier name for a tier.
     *
     * @param tierName the tier
     * @param fileName the file to parse (for historic reasons)
     *
     * @return the parent tier name, null by default
     */
    @Override
	public String getParentNameOf(String tierName, String fileName) {
        parse(fileName);

        if (templateParser != null) {
        	return templateParser.getParentNameOf(tierName, decoderInfo.getTemplateFilePath());
        }
        // parent - child relations not in a delimited text file.
        return null;
    }

    /**
     * Returns a list of annotation records for the specified tier.
     *
     * @param tierName the tier
     * @param fileName the file to parse (for historic reasons)
     *
     * @return a list of annotation records
     */
    @Override
	public ArrayList<AnnotationRecord> getAnnotationsOf(String tierName, String fileName) {
        parse(fileName);
        
        if (modRecordsPerTier != null) {
        	List<AnnotationRecord> modRecords = modRecordsPerTier.get(tierName);
        	if (modRecords != null) {
        		return (ArrayList<AnnotationRecord>) modRecords;
        	}
        }
        
        ArrayList<AnnotationRecord> records = recordsPerTier.get(tierName);

        if (records != null) {
            return records;
        }

        return new ArrayList<AnnotationRecord>();
    }

    /**
     * Sets the decoder info object. Should be called before calling parse or
     * any of the getters.
     *
     * @param decoderInfo The decoderInfo to set.
     */
    @Override
	public void setDecoderInfo(DecoderInfo decoderInfo) {
        if (decoderInfo instanceof DelimitedTextDecoderInfo) {
            this.decoderInfo = (DelimitedTextDecoderInfo) decoderInfo;
        }
    }
    
/////
//  New overrides to support import into (i.e. in combination with) a template.
//  If this would be useful for other text import options as well, a more generalized
//  version of such a 'wrapper' parser could be considered.  
/////
    
    private List<String> mergedList(List<String> baseList, List<String> otherList) {
    	ArrayList<String> unionList = new ArrayList<>(baseList);
    	
    	for (String other : otherList) {
    		if (!unionList.contains(other)) {
    			unionList.add(other);
    		}
    	}
    	
    	return unionList;
    }

    @Override
	public String getAuthor(String fileName) {
		if (templateParser != null) {
			return templateParser.getAuthor(decoderInfo.getTemplateFilePath());
		}
		return super.getAuthor(fileName);
	}

	@Override
	public List<Property> getTranscriptionProperties(String fileName) {
		if (templateParser != null) {
			return templateParser.getTranscriptionProperties(decoderInfo.getTemplateFilePath());
		}
		return super.getTranscriptionProperties(fileName);
	}

	@Override
	public Map<String, CVRecord> getControlledVocabularies(String fileName) {
		if (templateParser != null) {
			return templateParser.getControlledVocabularies(decoderInfo.getTemplateFilePath());
		}
		return super.getControlledVocabularies(fileName);
	}

	@Override
	public Map<String, ExternalReferenceImpl> getExternalReferences(String fileName) {
		if (templateParser != null) {
			return templateParser.getExternalReferences(decoderInfo.getTemplateFilePath());
		}
		return super.getExternalReferences(fileName);
	}

	@Override
	public Map<String, LexiconServiceRecord> getLexiconServices(String fileName) {
		if (templateParser != null) {
			return templateParser.getLexiconServices(decoderInfo.getTemplateFilePath());
		}
		return super.getLexiconServices(fileName);
	}

	@Override
	public String getAnnotatorOf(String tierName, String fileName) {
		if (templateParser != null) {
			return templateParser.getAnnotatorOf(tierName, decoderInfo.getTemplateFilePath());
		}
		return super.getAnnotatorOf(tierName, fileName);
	}

	@Override
	public Locale getDefaultLanguageOf(String tierName, String fileName) {
		if (templateParser != null) {
			return templateParser.getDefaultLanguageOf(tierName, decoderInfo.getTemplateFilePath());
		}
		return super.getDefaultLanguageOf(tierName, fileName);
	}

	@Override
	public List<LanguageRecord> getLanguages(String fileName) {
		if (templateParser != null) {
			return templateParser.getLanguages(decoderInfo.getTemplateFilePath());
		}
		return super.getLanguages(fileName);
	}

	@Override
	public List<LicenseRecord> getLicenses(String fileName) {
		if (templateParser != null) {
			return templateParser.getLicenses(decoderInfo.getTemplateFilePath());
		}
		return super.getLicenses(fileName);
	}

	@Override
	public String getLangRefOf(String tierName, String fileName) {
		if (templateParser != null) {
			return templateParser.getLangRefOf(tierName, decoderInfo.getTemplateFilePath());
		}
		return super.getLangRefOf(tierName, fileName);
	}

	@Override
	public String getExtRefOf(String tierName, String fileName) {
		if (templateParser != null) {
			return templateParser.getExtRefOf(tierName, decoderInfo.getTemplateFilePath());
		}
		return super.getExtRefOf(tierName, fileName);
	}

	@Override
	public List<RefLinkSetRecord> getRefLinkSetList(String fileName) {
		if (templateParser != null) {
			return templateParser.getRefLinkSetList(decoderInfo.getTemplateFilePath());
		}
		return super.getRefLinkSetList(fileName);
	}

	private List<LingTypeRecord> mergedTypeList(List<LingTypeRecord> baseList, List<LingTypeRecord> otherList) {
    	ArrayList<LingTypeRecord> unionList = new ArrayList<>(baseList);
    	
    	for (LingTypeRecord other : otherList) {
    		boolean isDuplicate = false;
    		for (LingTypeRecord ltr : baseList) {
    			if (other.getLingTypeId().equals(ltr.getLingTypeId())) {
    				isDuplicate = true;
    				break;
    			}
    		}
    		if (!isDuplicate) {
    			unionList.add(other);
    		}
    	}
    	
    	return unionList;
    }
	
	private void updateAnnotationRecordsForTemplate() {
		// create a list of the tiers in the template where any parent tier comes 
		// before its dependent tiers  
		if (templateParser == null) {
			return;
		}
		final String source = decoderInfo.getTemplateFilePath(); 
		List<String> templateTiers = new ArrayList<>(templateParser.getTierNames(source));
		List<String> orderedTiers = new ArrayList<>();
		// creates a depth-first like list
		for (int i = 0; i < templateTiers.size(); i++) {
			String name = templateTiers.get(i);
			String parent = templateParser.getParentNameOf(name, source);
			if ((parent == null || parent.isEmpty()) && !orderedTiers.contains(name)) {
				orderedTiers.add(name);
				i = -1;
				continue;
			}
			if (orderedTiers.contains(parent) && !orderedTiers.contains(name)) {
				orderedTiers.add(name);
				i = -1;
				continue;
			}
			if (templateTiers.size() == orderedTiers.size()) {
				break;
			}
		}
				
		/* slightly faster method, creates an order neither breadth first nor depth first
		while (!templateTiers.isEmpty()) {
			for (int i = 0; i < templateTiers.size(); i++) {
				String name = templateTiers.get(i);
				String parent = templateParser.getParentNameOf(name, source);
				if (parent == null || parent.isEmpty()) {
					orderedTiers.add(templateTiers.remove(i));
					i--;
				} else if (orderedTiers.contains(parent)) {
					orderedTiers.add(templateTiers.remove(i));
					i--;
				}
			}
		}
		*/

		// sort the annotation record lists before processing
		AnnotationRecordComparator recordComparator = new AnnotationRecordComparator(false);
		for (List<AnnotationRecord> recordList : recordsPerTier.values()) {
			Collections.sort(recordList, recordComparator);
		}
		
		for (String tierName : orderedTiers) {
			// a tier is in the template but not in the imported text file
			if (!tierNames.contains(tierName)) {
				continue;
			}
			String parentTierName = templateParser.getParentNameOf(tierName, source);
			// a top-level tier in the template
			if (parentTierName == null) {
				if (recordsPerTier.containsKey(tierName)) {
					// the tier is in the template and the text file, 
					// but no need to modify the records, just copy them
					modRecordsPerTier.put(tierName, recordsPerTier.get(tierName));
				} // else the tier is not in the imported text file
				continue;
			}
			// a tier is in the template and in the text file and is a dependent tier
			// check and possibly modify the annotation records
			if (recordsPerTier.containsKey(tierName)) {
				modifyTierRecords(tierName, parentTierName);
			} // else the tier is not in the imported text file, ignore and continue
		}
	}
	
	private void modifyTierRecords(String dependentTierName, String parentTierName) {
		List<AnnotationRecord> depRecords = recordsPerTier.get(dependentTierName);
		// get the modified/filtered parent list first, if it exists
		List<AnnotationRecord> parentRecords = modRecordsPerTier.get(parentTierName);
		if (parentRecords == null) {
			// otherwise use the original list
			parentRecords = recordsPerTier.get(parentTierName);
		}
		// if there are no parent records, either the depending annotations are skipped
		// (there are no parent annotations) or empty annotations can be added to the parent
		if (parentRecords == null) {
			// for now ignore/remove the depending records if there are no parent annotations
			recordsPerTier.remove(dependentTierName);
			return;
		}
		
		String dependentTypeName = templateParser.getLinguisticTypeIDOf(dependentTierName, decoderInfo.getTemplateFilePath());
		LingTypeRecord depTypeRecord = null;
		for (LingTypeRecord ltRecord : templateParser.getLinguisticTypes(decoderInfo.getTemplateFilePath())) {
			if (ltRecord.getLingTypeId().equals(dependentTypeName)) {
				depTypeRecord = ltRecord;
				break;
			}
		}
		
		if (depTypeRecord == null) {
			return;
		}
			
		String stereoType = depTypeRecord.getStereoType();
		if (stereoType == null || stereoType.isEmpty()) {
			// error condition, apparently not a depending tier after all
			return;
		}
		// if the parent is not a top level tier, it has a constraint as well, this is ignored here
		
		if (stereoType.indexOf("Association") > -1) {
			List<AnnotationRecord> modRecords = new ArrayList<>();
			List<Integer> usedParentList = new ArrayList<Integer>();
			int parentIndex = 0;
			for (AnnotationRecord depRecord : depRecords) {
				long dbt = depRecord.getBeginTimeSlotRecord().getValue();
				long det = depRecord.getEndTimeSlotRecord().getValue();
				// assume correct order
				for (int i = parentIndex; i < parentRecords.size(); i++) {
					AnnotationRecord parRecord = parentRecords.get(i);
					if (dbt >= parRecord.getBeginTimeSlotRecord().getValue()
							&& det <= parRecord.getEndTimeSlotRecord().getValue()
							&& !usedParentList.contains(i)) {
						AnnotationRecord modRecord = new AnnotationRecord();
						modRecord.setAnnotationType(AnnotationRecord.REFERENCE);
						modRecord.setReferredAnnotId(parRecord.getAnnotationId());
						modRecord.setAnnotationId(depRecord.getAnnotationId());
						modRecord.setValue(depRecord.getValue());
						// copy time slots for possible deeper levels in the hierarchy?
						modRecord.setBeginTimeSlotRecord(depRecord.getBeginTimeSlotRecord());
						modRecord.setEndTimeSlotRecord(depRecord.getEndTimeSlotRecord());
						modRecords.add(modRecord);
						parentIndex = i; // start next iteration with the same parent
						break;
					}
				}
			}
			
			modRecordsPerTier.put(dependentTierName, modRecords);
		} else if (stereoType.indexOf("Included") > -1) {
			// only need to check if there is a parent annotation
			List<AnnotationRecord> modRecords = new ArrayList<>();
			int parentIndex = 0;
			
			for (AnnotationRecord depRecord : depRecords) {
				long dbt = depRecord.getBeginTimeSlotRecord().getValue();
				long det = depRecord.getEndTimeSlotRecord().getValue();

				for (int i = parentIndex; i < parentRecords.size(); i++) {
					AnnotationRecord parRecord = parentRecords.get(i);
					// only accept really "included" records, partially overlapping annotations are ignored
					if (dbt >= parRecord.getBeginTimeSlotRecord().getValue()
							&& det <= parRecord.getEndTimeSlotRecord().getValue()) {
						modRecords.add(depRecord);
						parentIndex = i;
						break;
					}
				}
			}
			
			modRecordsPerTier.put(dependentTierName, modRecords);
		} else if (stereoType.indexOf("Subdivision") > -1) {
			boolean isSymbolic = stereoType.indexOf("Symbolic") > -1;
			List<AnnotationRecord> modRecords = new ArrayList<>();
			int parentIndex = 0;
			String lastParentId = null;
			//String previousSiblingId = null;
			AnnotationRecord previousSiblingRecord = null;
			
			for (AnnotationRecord depRecord : depRecords) {
				long dbt = depRecord.getBeginTimeSlotRecord().getValue();
				long det = depRecord.getEndTimeSlotRecord().getValue();
				// assume correct order
				for (int i = parentIndex; i < parentRecords.size(); i++) {
					AnnotationRecord parRecord = parentRecords.get(i);
					
					if (dbt >= parRecord.getBeginTimeSlotRecord().getValue()
							&& det <= parRecord.getEndTimeSlotRecord().getValue()) {
						if (!parRecord.getAnnotationId().equals(lastParentId)) {
							previousSiblingRecord = null;
						}
						
						AnnotationRecord modRecord = new AnnotationRecord();
						modRecord.setReferredAnnotId(parRecord.getAnnotationId());
						modRecord.setAnnotationId(depRecord.getAnnotationId());
						modRecord.setValue(depRecord.getValue());
						// copy time slots for possible deeper levels in the hierarchy?
						modRecord.setBeginTimeSlotRecord(depRecord.getBeginTimeSlotRecord());
						modRecord.setEndTimeSlotRecord(depRecord.getEndTimeSlotRecord());
						if (isSymbolic) {
							modRecord.setAnnotationType(AnnotationRecord.REFERENCE);
							if (previousSiblingRecord != null) {
								modRecord.setPreviousAnnotId(previousSiblingRecord.getAnnotationId());
							}
						} else {
							// chaining based on shared time slot references
							modRecord.setAnnotationType(AnnotationRecord.ALIGNABLE);
							if (dbt == parRecord.getBeginTimeSlotRecord().getValue()) {
								modRecord.setBeginTimeSlotRecord(parRecord.getBeginTimeSlotRecord());
							}
							if (det == parRecord.getEndTimeSlotRecord().getValue()) {
								modRecord.setEndTimeSlotRecord(parRecord.getEndTimeSlotRecord());
							}
							if (previousSiblingRecord != null) {
								modRecord.setBeginTimeSlotRecord(previousSiblingRecord.getEndTimeSlotRecord());								
							}
						}
						
						modRecords.add(modRecord);
						parentIndex = i;
						lastParentId = parRecord.getAnnotationId();
						previousSiblingRecord = depRecord;
						break;
					}
				}
			}
			
			modRecordsPerTier.put(dependentTierName, modRecords);	
		}
	}
}
	
