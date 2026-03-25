package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.interannotator.*;
import mpi.eudico.client.annotator.interannotator.multi.CompareCombiMulti;
import mpi.eudico.server.corpora.clom.AnnotationCore;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.AnnotationCoreImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.util.ControlledVocabulary;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static mpi.eudico.client.annotator.interannotator.CompareConstants.MATCHING.PREFIX;
import static mpi.eudico.client.annotator.interannotator.CompareConstants.MATCHING.SUFFIX;
import static mpi.eudico.client.annotator.util.ClientLogger.LOG;

/**
 * Abstract class for inter-rater agreement calculation. In this abstract class the preparatory work is implemented which is
 * necessary for most(?) actual implementations.
 */
public abstract class AbstractCompareCommand extends AbstractProgressCommand {
    /**
     * The map to hold the user selection.
     */
    protected Map<Object, Object> compareProperties;
    /**
     * A list to hold the compare combinations.
     */
    protected List<CompareCombi> compareSegments;
    /**
     * The transcription object.
     */
    protected TranscriptionImpl transcription;
    /**
     * The class for matching tiers or files.
     */
    protected TierAndFileMatcher tfMatcher;
    /**
     * The eaf extension identifier.
     */
    protected final String eafExt = ".eaf";
    /**
     * The size of the number of files.
     */
    protected int numFiles;
    /**
     * Number of tiers selected.
     */
    protected int numSelTiers;
    /**
     * A boolean identifier.
     */
    protected boolean groupWiseCompare;

    /**
     * Constructor.
     *
     * @param theName name of the command.
     */
    public AbstractCompareCommand(String theName) {
        super(theName);
    }

    /**
     * The arguments parameter should contain the map with settings needed for the creation of combinations of segmentation
     * units.
     *
     * @param receiver the transcription in case of single ("current") document processing, null otherwise
     * @param arguments the array of arguments. arguments[0]: the map containing the selections made by the user
     */
    @SuppressWarnings("unchecked")
    @Override
    public void execute(Object receiver, Object[] arguments) {
        super.execute(receiver, arguments);
        transcription = (TranscriptionImpl) receiver;
        if (transcription != null) {
            numFiles = 1;
        }
        compareProperties = (Map<Object, Object>) arguments[0];

        if (compareProperties == null || compareProperties.size() == 0) {
            progressInterrupt("No input provided for calculations.");
            return;
        }

        compareSegments = new ArrayList<CompareCombi>();

        tfMatcher = new TierAndFileMatcher();

        CompareAnnotatorsThread cat = new CompareAnnotatorsThread();
        cat.start();
    }

    /**
     * Returns the results.
     *
     * @return the resulting comparison combinations, combi's that include the results of the agreement calculations.
     */
    public List<CompareCombi> getCompareSegments() {
        return compareSegments;
    }

    /**
     * Logs the error and interrupts the progress.
     *
     * @param message the message to be logged
     */
    protected void logErrorAndInterrupt(String message) {
        LOG.warning(message);
        progressInterrupt(message);
    }

    /**
     * Creates combination objects containing minimal information necessary for the calculation.
     */
    @SuppressWarnings("unchecked")
    protected void createSegments() {
        // retrieve files or transcription, prepare tier-combinations for the actual compare routine
        // maybe prepare a list of pairs of objects, each object containing file name, tier name, and a list of
        // segments?
        CompareConstants.FILE_MATCHING sourceMatch =
            (CompareConstants.FILE_MATCHING) compareProperties.get(CompareConstants.TIER_SOURCE_KEY);
        CompareConstants.MATCHING tierMatching =
            (CompareConstants.MATCHING) compareProperties.get(CompareConstants.TIER_MATCH_KEY);
        CompareConstants.MATCHING fileMatching =
            (CompareConstants.MATCHING) compareProperties.get(CompareConstants.FILE_MATCH_KEY);
        String tierNameSeparators = (String) compareProperties.get(CompareConstants.TIER_SEPARATOR_KEY);
        String fileNameSeparators = (String) compareProperties.get(CompareConstants.FILE_SEPARATOR_KEY);
        // either tiername 1 and 2 or selTierNames
        String tierName1 = (String) compareProperties.get(CompareConstants.TIER_NAME1_KEY);
        String tierName2 = (String) compareProperties.get(CompareConstants.TIER_NAME2_KEY);
        List<String> selTierNames = (List<String>) compareProperties.get(CompareConstants.TIER_NAMES_KEY);
        List<String> allTierNames = (List<String>) compareProperties.get(CompareConstants.ALL_TIER_NAMES_KEY);
        List<File> selFiles = (List<File>) compareProperties.get(CompareConstants.SEL_FILES_KEY);
        if (selFiles != null) {
            numFiles = selFiles.size();
        }

        if (selTierNames != null) {
            numSelTiers = selTierNames.size(); // x 2?
        } else if (tierName1 != null && tierName2 != null) {
            numSelTiers = 2;
        }
        Boolean groupCompare = (Boolean) compareProperties.get(CompareConstants.GROUP_COMPARE_KEY);
        groupWiseCompare = groupCompare != null && groupCompare;

        curProgress = 1f;
        progressUpdate((int) curProgress, "Starting to extract segments from the selected tiers.");

        if (sourceMatch == CompareConstants.FILE_MATCHING.CURRENT_DOC) {
            if (transcription == null) {
                // report error, this shouldn't happen here
                progressInterrupt("The transcription is null");

                return;
            }

            if (tierMatching == CompareConstants.MATCHING.MANUAL) {
                if (groupWiseCompare) { // group comparison
                    if (selTierNames == null || selTierNames.isEmpty()) {
                        progressInterrupt("There are no manually selected tiers");

                        return;
                    }
                    extractSegments(transcription, selTierNames, tierMatching, null);
                } else {
                    if (tierName1 == null) {
                        progressInterrupt("The first manually selected tier is null");

                        return;
                    }
                    if (tierName2 == null) {
                        progressInterrupt("The second manually selected tier is null");

                        return;
                    }
                    if (tierName1.equals(tierName2)) {
                        progressInterrupt("The first and second selected tier have the same name (not allowed)");

                        return;
                    }

                    extractSegments(transcription, tierName1, tierName2);
                }
            } else if (tierMatching == PREFIX || tierMatching == SUFFIX) {
                if (selTierNames == null || selTierNames.isEmpty()) {
                    progressInterrupt("There are no tiers selected for comparing based on affix");

                    return;
                }
                extractSegments(transcription, selTierNames, tierMatching, tierNameSeparators);
            } else if (tierMatching == CompareConstants.MATCHING.SAME_NAME) {
                progressInterrupt("Cannot compare tiers with the same name in the same document");

                return;
            }
        } else if (sourceMatch == CompareConstants.FILE_MATCHING.IN_SAME_FILE) {
            if (selFiles == null || selFiles.isEmpty()) {
                progressInterrupt("There are no files selected, cannot retrieve the tiers to compare.");

                return;
            }
            if (tierMatching == CompareConstants.MATCHING.MANUAL) {
                if (groupWiseCompare) { // group comparison
                    if (selTierNames == null || selTierNames.isEmpty()) {
                        progressInterrupt("There are no manually selected tiers");

                        return;
                    }
                    extractSegments(selFiles, null, null, selTierNames);
                } else { // pair comparison
                    if (tierName1 == null) {
                        progressInterrupt("The first manually selected tier is null");

                        return;
                    }
                    if (tierName2 == null) {
                        progressInterrupt("The second manually selected tier is null");

                        return;
                    }
                    if (tierName1.equals(tierName2)) {
                        progressInterrupt("The first and second selected tier have the same name (not allowed)");

                        return;
                    }

                    extractSegments(selFiles, null, null, tierName1, tierName2);
                }
            } else if (tierMatching == PREFIX || tierMatching == SUFFIX) {
                if (selTierNames == null || selTierNames.isEmpty()) {
                    progressInterrupt("There are no tiers selected for comparing based on affix");

                    return;
                }

                extractSegments(selFiles, null, null, selTierNames, allTierNames, tierMatching, tierNameSeparators);
            } else if (tierMatching == CompareConstants.MATCHING.SAME_NAME) {
                // error condition: tiers of the same name should not be in the same file
                progressInterrupt("Tiers with the same name cannot be in the same file");

                return;

            }
        } else if (sourceMatch == CompareConstants.FILE_MATCHING.ACROSS_FILES) {
            if (selFiles == null || selFiles.size() < 2) {
                progressInterrupt("There are no files or too few files selected, cannot retrieve the tiers to compare.");

                return;
            }
            if (fileMatching != PREFIX && fileMatching != SUFFIX) {
                progressInterrupt("Cannot determine how to match files, e.g. based on prefix or suffix");

                return;
            }
            if (tierMatching == CompareConstants.MATCHING.MANUAL) {
                // this only makes sense if there are two tier names and exactly two files?? or how to know
                // which tier should be retrieved from which file? or try all possible combinations?
                if (tierName1 == null) {
                    progressInterrupt("The first manually selected tier is null");

                    return;
                }
                if (tierName2 == null) {
                    progressInterrupt("The second manually selected tier is null");

                    return;
                }
                if (tierName1.equals(tierName2)) {
                    progressInterrupt("The first and second selected tier have the same name (not allowed)");

                    return;
                }

                extractSegments(selFiles, fileMatching, fileNameSeparators, tierName1, tierName2);
            } else if (tierMatching == PREFIX || tierMatching == SUFFIX) {
                if (selTierNames == null || selTierNames.isEmpty()) {
                    progressInterrupt("There are no tiers selected for comparing based on affix");

                    return;
                }

                extractSegments(selFiles,
                                fileMatching,
                                fileNameSeparators,
                                selTierNames,
                                allTierNames,
                                tierMatching,
                                tierNameSeparators);
            } else if (tierMatching == CompareConstants.MATCHING.SAME_NAME) {
                if (selTierNames == null || selTierNames.isEmpty()) {
                    progressInterrupt("There are no tiers selected for comparing based on same name");

                    return;
                }

                extractSegments(selFiles, fileMatching, fileNameSeparators, selTierNames, null, null, null);
            }
        }
        if (errorOccurred) {
            return;
        }
        if (cancelled) {
            progressInterrupt("The process was cancelled while extracting segments from the tiers.");
        }
        curProgress = 30f;
        if (!compareSegments.isEmpty()) {
            progressUpdate((int) curProgress,
                           String.format("Extracted the annotations of %d pairs of tiers...", compareSegments.size()));
        } else {
            progressInterrupt("There are no segments for the agreement calculation, process stopped.");
        }
    }

    /**
     * Extracts the segments from the two tiers in the specified transcription. Assumes necessary checks have been performed.
     * result should be a list of size 1 with the segments of the two tiers, or null if something is wrong.
     *
     * @param transcription the transcription, not null
     * @param tierName1 first tier
     * @param tierName2 second tier
     */
    private void extractSegments(TranscriptionImpl transcription, String tierName1, String tierName2) {
        TierImpl t1 = transcription.getTierWithId(tierName1);
        TierImpl t2 = transcription.getTierWithId(tierName2);
        if (t1 == null) {
            // log or report?
            String message = String.format("The tier \"%s\" is not found in the transcription.", tierName1);
            logErrorAndInterrupt(message);
            return;
        }
        if (t2 == null) {
            // log or report?
            String message = String.format("The tier \"%s\" is not found in the transcription.", tierName2);
            logErrorAndInterrupt(message);
            return;
        }
        //
        List<AnnotationCore> segments1 = getAnnotationCores(t1);

        curProgress = 15f;
        progressUpdate((int) curProgress, "Extracted segments from tier " + tierName1);

        List<AnnotationCore> segments2 = getAnnotationCores(t2);

        curProgress = 25f;
        progressUpdate((int) curProgress, "Extracted segments from tier " + tierName2);

        if (segments1.isEmpty() && segments2.isEmpty()) {
            // log or report
            LOG.warning(String.format("Both tier \"%s\" and tier \"%s\" are empty (no segments).", tierName1, tierName2));
            return;
        }


        CompareUnit cu1 = new CompareUnit(transcription.getFullPath(), t1.getName(), t1.getAnnotator());
        cu1.annotations = segments1;
        CompareUnit cu2 = new CompareUnit(transcription.getFullPath(), t2.getName(), t2.getAnnotator());
        cu2.annotations = segments2;
        if (!groupWiseCompare) {
            CompareCombi cc = new CompareCombi(cu1, cu2);
            compareSegments.add(cc);
        } else {
            CompareCombiMulti ccm = new CompareCombiMulti(cu1, cu2);
            compareSegments.add(ccm);
            String cvName = t1.getLinguisticType().getControlledVocabularyName();
            if (cvName != null) {
                ccm.setCVName(cvName);
            }
            ccm.addValues(getValues(t1));

            cvName = t2.getLinguisticType().getControlledVocabularyName();
            if (cvName != null) {
                ccm.setCVName(cvName);
            }
            ccm.addValues(getValues(t2));
        }
    }

    /**
     * Combines tiers within a transcription based on the tier matching (prefix or suffix). Assumes necessary checks have
     * been performed. result should be a list of compare combinations, one combination (or more?) per selected tier name.
     *
     * @param transcription the transcription, not null
     * @param selTierNames the selected tiers, not null
     * @param tierMatching prefix or suffix matching
     * @param tierNameSeparators custom separators can be null
     */
    private void extractSegments(TranscriptionImpl transcription, List<String> selTierNames,
                                 CompareConstants.MATCHING tierMatching, String tierNameSeparators) {
        // get a map of tier combinations
        List<String> allTierNames = new ArrayList<String>();
        List<TierImpl> tiers = transcription.getTiers();
        List<List<String>> tierMatches = null;
        // default situation
        if (tierMatching != CompareConstants.MATCHING.MANUAL) {
            for (Tier t : tiers) {
                allTierNames.add(t.getName());
            }
            tierMatches = tfMatcher.getMatchingTiers(allTierNames, selTierNames, tierMatching, tierNameSeparators);
        } else {
            tierMatches = new ArrayList<>();
            List<String> curMatches = new ArrayList<String>();
            for (Tier t : tiers) {
                if (selTierNames.contains(t.getName())) {
                    curMatches.add(t.getName());
                }
            }
            if (!curMatches.isEmpty()) {
                tierMatches.add(curMatches);
            }
        }

        if (tierMatches.isEmpty()) {
            logErrorAndInterrupt("No matching tiers (same name, different affix) have been found.");
            return;
        }
        curProgress = 3;
        progressUpdate((int) curProgress, String.format("Found %d pairs of tiers.", tierMatches.size()));
        // create all possible tier combinations based on the tier matches in the list
        float perMatch = 25f / tierMatches.size();

        for (List<String> curMatches : tierMatches) {
            if (!groupWiseCompare) {
                for (int i = 0; i < curMatches.size(); i++) { // first loop
                    TierImpl t1 = transcription.getTierWithId(curMatches.get(i));
                    List<AnnotationCore> segments1 = null;
                    if (t1 != null) {
                        segments1 = getAnnotationCores(t1);
                        for (int j = i + 1; j < curMatches.size(); j++) { //second loop
                            TierImpl t2 = transcription.getTierWithId(curMatches.get(j));
                            if (t2 != null) {
                                List<AnnotationCore> segments2 = getAnnotationCores(t2);
                                if (!(segments1.isEmpty() && segments2.isEmpty())) {
                                    // add a combination
                                    CompareUnit cu1 =
                                        new CompareUnit(transcription.getFullPath(), t1.getName(), t1.getAnnotator());
                                    cu1.annotations = segments1;
                                    CompareUnit cu2 =
                                        new CompareUnit(transcription.getFullPath(), t2.getName(), t2.getAnnotator());
                                    cu2.annotations = segments2;
                                    CompareCombi cc = new CompareCombi(cu1, cu2);
                                    compareSegments.add(cc);
                                } else {
                                    LOG.warning(String.format(
                                        "Matching tiers \"%s\" and \"%s\" are both empty (no segments).",
                                        t1.getName(),
                                        t2.getName()));
                                }
                            } else {
                                LOG.warning(String.format("Matching tier \"%s\" and \"%s\": the second tier does not exist.",
                                                          t1.getName(),
                                                          curMatches.get(j)));
                            }
                        }
                    } else {
                        LOG.warning(String.format("Matching tiers: the first tier \"%s\" does not exist.",
                                                  curMatches.get(i)));
                    }
                }
            } else {
                CompareCombiMulti ccm = getCompareCombinationMulti(transcription, curMatches);
                if (ccm != null) {
                    compareSegments.add(ccm);
                }
                /*
                // potentially more than 2 tiers to compare
                List<CompareUnit> compUnits = new ArrayList<CompareUnit>(curMatches.size());

                for (int i = 0; i < curMatches.size(); i++) {
                    TierImpl t1 = transcription.getTierWithId(curMatches.get(i));
                    if (t1 != null) {
                        List<AnnotationCore> segments1 = getAnnotationCores(t1);
                        if (!segments1.isEmpty()) {
                            CompareUnit cu1 = new CompareUnit(transcription.getFullPath(), t1.getName(), t1
                            .getAnnotator());
                            cu1.annotations = segments1;
                            compUnits.add(cu1);
                        } else {
                            LOG.warning(String.format(
                                    "Matching tiers \"%s\" is empty (no segments).",
                                    t1.getName()));
                        }
                    } else {
                        LOG.warning(String.format(
                                "Matching tiers: the tier \"%s\" does not exist in this transcription.",
                                curMatches.get(i)));
                    }
                }

                if (compUnits.size() > 1) {
                    compareSegments.add(new CompareCombiMulti(compUnits));
                } else {
                    LOG.warning(
                            "Matching tiers: less than 2 non-empty matching tiers in this transcription.");
                }
                */
            }

            curProgress += perMatch;
            progressUpdate((int) curProgress, null);
            if (cancelled) {
                return;
            }
        }

    }

    /**
     * Combines tiers in a set of files. Depending on the file matching style the tiers are in the same file or in different
     * files that are matched based on their names with different affix. result should be a list of compare combinations.
     *
     * @param selFiles the selected files containing the tier to compare
     * @param fileMatching null in case the tiers are in the same file, CompareConstants.MATCHING.PREFIX or
     *     CompareConstants.MATCHING.SUFFIX in case tiers are in different files
     * @param fileNameSeparators custom separator(s) for filename-affix separation
     * @param tierName1 the first selected tier
     * @param tierName2 the second selected tier
     */
    private void extractSegments(List<File> selFiles, CompareConstants.MATCHING fileMatching, String fileNameSeparators,
                                 String tierName1, String tierName2) {

        if (fileMatching == null) {
            // each file in the list should contain both tier1 and tier2
            progressUpdate((int) curProgress, "Extracting segments from each file...");
            float perFile = 28f / selFiles.size();
            for (File f : selFiles) {
                if (f.isDirectory()) {
                    curProgress += perFile;
                    continue; // log
                }
                // create a Transcription of the file and check tiers
                TranscriptionImpl t1 = createTranscription(f);
                if (t1 != null) {
                    TierImpl tier1 = t1.getTierWithId(tierName1);
                    TierImpl tier2 = t1.getTierWithId(tierName2);

                    CompareCombi cc = createCompareCombi(tier1, tier2);
                    if (cc != null) {
                        compareSegments.add(cc);
                    }
                } else {
                    LOG.warning(String.format("A transcription could not be loaded from file \"%s\"", f.getAbsolutePath()));
                }
                curProgress += perFile;
                progressUpdate((int) curProgress, null);
            }
        } else {
            progressUpdate((int) curProgress, "Extracting segments from file pairs...");
            List<List<File>> matchingFiles = tfMatcher.getMatchingFiles(selFiles, fileMatching, fileNameSeparators, eafExt);
            if (matchingFiles.isEmpty()) {
                logErrorAndInterrupt("No matching files found in the list of selected files");
                return;
            }
            curProgress = 4;
            progressUpdate((int) curProgress, String.format("Found %d pairs of matching files...", matchingFiles.size()));
            // loop over matches, find right tiers in all combinations of files
            TranscriptionImpl t1 = null;
            TranscriptionImpl t2 = null;
            float perMatch = 25f / matchingFiles.size();
            for (List<File> matchList : matchingFiles) {
                // convert to list of transcriptions first (to avoid loading the same file more than once)?
                // In most cases there will only be two files
                for (int i = 0; i < matchList.size(); i++) {
                    t1 = createTranscription(matchList.get(i));
                    if (t1 == null) {
                        LOG.info(String.format("A transcription could not be loaded from file (t1) \"%s\"",
                                               matchList.get(i).getAbsolutePath()));
                        continue;
                    }
                    for (int j = i + 1; j < matchList.size(); j++) {
                        t2 = createTranscription(matchList.get(j));
                        if (t2 == null) {
                            LOG.info(String.format("A transcription could not be loaded from file (t2) \"%s\"",
                                                   matchList.get(j).getAbsolutePath()));
                            continue;
                        }

                        compareSegments.addAll(getCompareCombinations(t1, t2, tierName1, tierName2));
                    }
                }
                curProgress += perMatch;
                progressUpdate((int) curProgress, null);
            }
        }
        progressUpdate((int) curProgress,
                       String.format("Extracted the annotations of %1$d pairs of tiers from %2$d files...",
                                     compareSegments.size(),
                                     selFiles.size()));
    }

    //

    /**
     * Combines tiers in a set of files. Depending on the file matching style the tiers are in the same file or in different
     * files that are matched based on their names with different affix. result should be a list of compare combinations.
     *
     * @param selFiles the selected files containing the tiers to compare
     * @param fileMatching null in case the tiers are in the same file, CompareConstants.MATCHING.PREFIX or
     *     CompareConstants.MATCHING.SUFFIX in case tiers are in different files
     * @param fileNameSeparators custom separator(s) for filename-affix separation
     * @param tierNames the list of selected tiers
     */
    private void extractSegments(List<File> selFiles, CompareConstants.MATCHING fileMatching, String fileNameSeparators,
                                 List<String> tierNames) {

        if (fileMatching == null) {
            // each file in the list should contain all tier names
            progressUpdate((int) curProgress, "Extracting segments from each file...");
            float perFile = 28f / selFiles.size();
            for (File f : selFiles) {
                if (f.isDirectory()) {
                    curProgress += perFile;
                    continue; // log
                }
                // create a Transcription of the file and check tiers
                TranscriptionImpl t1 = createTranscription(f);
                if (t1 != null) {
                    List<TierImpl> tiers = new ArrayList<TierImpl>(tierNames.size());
                    for (String name : tierNames) {
                        TierImpl tier = t1.getTierWithId(name);
                        // remove test for the number of annotations?
                        if (tier != null && tier.getNumberOfAnnotations() > 0) {
                            tiers.add(tier);
                        } else {
                            LOG.info("Tier or annotations are missing");
                        }
                    }
                    if (tiers.size() > 1) {
                        CompareCombiMulti ccm = createCompareCombi(tiers);
                        if (ccm != null) {
                            compareSegments.add(ccm);
                        }
                    } else {
                        LOG.info("There is only 1 tier");
                    }

                } else {
                    LOG.warning(String.format("A transcription could not be loaded from file \"%s\"", f.getAbsolutePath()));
                }
                curProgress += perFile;
                progressUpdate((int) curProgress, null);
            }
        } else {
            progressUpdate((int) curProgress, "Extracting segments from file pairs...");
            List<List<File>> matchingFiles = tfMatcher.getMatchingFiles(selFiles, fileMatching, fileNameSeparators, eafExt);
            if (matchingFiles.isEmpty()) {
                logErrorAndInterrupt("No matching files found in the list of selected files");
                return;
            }
            curProgress = 4;
            progressUpdate((int) curProgress, String.format("Found %d pairs of matching files...", matchingFiles.size()));
            // loop over matches, find right tiers in all combinations of files
            // hier... adapt
            List<TranscriptionImpl> transList = new ArrayList<TranscriptionImpl>();
            //TranscriptionImpl t1 = null;
            //TranscriptionImpl t2 = null;
            float perMatch = 25f / matchingFiles.size();
            for (List<File> matchList : matchingFiles) {
                transList.clear();

                for (File file : matchList) {
                    TranscriptionImpl t1 = createTranscription(file);
                    if (t1 == null) {
                        LOG.info(String.format("A transcription could not be loaded from file (t1) \"%s\"",
                                               file.getAbsolutePath()));
                        continue;
                    }
                    transList.add(t1);
                    /*
                    for (int j = i + 1; j < matchList.size(); j++) {
                        t2 = createTranscription(matchList.get(j));
                        if (t2 == null) {
                            LOG.info(String.format(
                                    "A transcription could not be loaded from file (t2) \"%s\"",
                                    matchList.get(j).getAbsolutePath()));
                            continue;
                        }

                        compareSegments.addAll(getCompareCombinations(t1, t2, tierName1, tierName2));
                    }*/
                }
                List<? extends CompareCombi> ccmList = getCompareCombinationsMulti(transList, tierNames);
                if (ccmList != null && !ccmList.isEmpty()) {
                    compareSegments.addAll(ccmList);
                } else {
                    // log
                }

                curProgress += perMatch;
                progressUpdate((int) curProgress, null);
            }
        }
        progressUpdate((int) curProgress,
                       String.format("Extracted the annotations of %1$d groups of tiers from %2$d files...",
                                     compareSegments.size(),
                                     selFiles.size()));

    }
    //

    /**
     * Combines tiers in a set of files. Depending on the file matching style the tiers are in the same file or in different
     * files that are matched based on their names with different affix. Based on the tier matching style the tiers either
     * the same name but in different files or they are matched based on tier name and affix. It is an error if both file
     * matching and tier matching parameters are null.
     *
     * @param selFiles the selected files
     * @param fileMatching null in case the tiers are in the same file, CompareConstants.MATCHING.PREFIX or
     *     CompareConstants.MATCHING.SUFFIX in case tiers are in different files
     * @param fileNameSeparators custom separator(s) for filename-affix separation
     * @param selTierNames the selected tier names
     * @param allTierNames all the tiers in the selected files
     * @param tierMatching null in case the tiers are matched based on same-name-different-file,
     *     CompareConstants.MATCHING.PREFIX or CompareConstants.MATCHING.SUFFIX in case tiers are matched based on
     *     tiername-affix separation. In the latter case matching can be different-tiername-same file of
     *     different-tiername-different-file
     * @param tierNameSeparators custom separator(s) for tiername-affix separation
     *
     * @return a list of compare combinations
     */
    private void extractSegments(List<File> selFiles, CompareConstants.MATCHING fileMatching, String fileNameSeparators,
                                 List<String> selTierNames, List<String> allTierNames,
                                 CompareConstants.MATCHING tierMatching, String tierNameSeparators) {
        if (fileMatching == null && tierMatching == null) {
            // this would mean comparing tiers with the same name in the same file
            logErrorAndInterrupt("Cannot compare tiers with the same name in the same file.");
            return;
        }
        List<List<File>> matchingFiles = null;
        List<List<String>> matchingTiers = null;

        if (fileMatching == null) {
            // each file in the list should contain the combination of tiers

        } else { // file matching based on prefix or suffix
            matchingFiles = tfMatcher.getMatchingFiles(selFiles, fileMatching, fileNameSeparators, eafExt);
            if (matchingFiles.isEmpty()) { // can't compare
                // log or report...
                logErrorAndInterrupt("No matching files found in the list of selected files.");
                return;
            }
        }

        if (tierMatching == PREFIX || tierMatching == SUFFIX) {
            matchingTiers = tfMatcher.getMatchingTiers(allTierNames, selTierNames, tierMatching, tierNameSeparators);
        }
        curProgress = 3;
        progressUpdate((int) curProgress, null);

        if (matchingFiles == null) { // tiers in the same file, matchingTiers cannot be null
            progressUpdate((int) curProgress, "Extracting segments from each file...");
            float perFile = 26f / selFiles.size();

            for (File f : selFiles) {
                if (f.isDirectory()) {
                    curProgress += perFile;
                    continue; // log
                }
                // create a Transcription of the file and check tiers
                TranscriptionImpl t1 = createTranscription(f);
                if (t1 != null) {
                    // loop over all matched tiers (in most cases probably two tiers)
                    for (List<String> tierMatch : matchingTiers) {
                        if (!groupWiseCompare) {
                            for (int i = 0; i < tierMatch.size(); i++) {
                                String tierName1 = tierMatch.get(i);
                                for (int j = i + 1; j < tierMatch.size(); j++) {
                                    String tierName2 = tierMatch.get(j);

                                    TierImpl tier1 = t1.getTierWithId(tierName1);
                                    TierImpl tier2 = t1.getTierWithId(tierName2);

                                    CompareCombi cc = createCompareCombi(tier1, tier2);
                                    if (cc != null) {
                                        compareSegments.add(cc);
                                    }
                                }
                            }
                        } else { // group wise compare
                            CompareCombiMulti ccm = getCompareCombinationMulti(t1, tierMatch);
                            if (ccm != null) {
                                compareSegments.add(ccm);
                            }
                        }
                    }
                } //else log?
                curProgress += perFile;
                progressUpdate((int) curProgress, null);
            }

        } else { // tiers in different files
            progressUpdate((int) curProgress, "Extracting segments from file pairs...");
            float perMatch = 28f / matchingFiles.size();

            for (List<File> fileMatch : matchingFiles) {
                List<TranscriptionImpl> transMatch = createTranscriptions(fileMatch);
                if (transMatch.size() <= 1) {
                    // log the files that cannot be processed
                    curProgress += perMatch;
                    continue;
                }
                // group comparison
                if (groupWiseCompare) {
                    if (matchingTiers == null) { // tier of the same name in different files
                        List<? extends CompareCombi> ccmList = getCompareCombinationsMulti(transMatch, selTierNames);
                        if (ccmList != null && !ccmList.isEmpty()) {
                            compareSegments.addAll(ccmList);
                        } else {
                            LOG.warning(String.format(
                                "Cannot compare any of the selected tiers (%s) from matching files (%s)",
                                selTierNames,
                                getNames(transMatch)));
                        }
                    } else { // tiers matching based on affix, in different files based on affix
                        for (List<String> tierMatch : matchingTiers) {
                            CompareCombiMulti ccm = getCompareCombinationMulti(transMatch, tierMatch);
                            if (ccm != null) {
                                compareSegments.add(ccm);
                            } else {
                                LOG.warning(String.format("Cannot compare matching tiers (%s) from matching files (%s)",
                                                          tierMatch,
                                                          getNames(transMatch)));
                            }
                        }
                    }
                    curProgress += perMatch;
                    progressUpdate((int) curProgress, null);
                    continue;
                }
                // what follows is pair wise comparison
                TranscriptionImpl ti1 = null;
                TranscriptionImpl ti2 = null;

                for (int i = 0; i < transMatch.size() - 1; i++) {
                    ti1 = transMatch.get(i);
                    for (int j = i + 1; j < transMatch.size(); j++) {
                        ti2 = transMatch.get(j);
                        // we have two transcriptions now, loop over selected tiers or tier combinations

                        if (matchingTiers == null) { // tiers of the same name in different files
                            TierImpl tier1 = null;
                            TierImpl tier2 = null;

                            for (String tierName : selTierNames) {
                                tier1 = ti1.getTierWithId(tierName);
                                tier2 = ti2.getTierWithId(tierName);

                                if (tier1 != null && tier2 != null) {
                                    CompareCombi cc = createCompareCombi(tier1, tier2);
                                    if (cc != null) {
                                        compareSegments.add(cc);
                                    }
                                } else {
                                    if (tier1 == null) {
                                        LOG.warning(String.format(
                                            "Tiers with same name in different files: tier \"%s\" not found in "
                                            + "transcription \"%s\"",
                                            tierName,
                                            ti1.getName()));
                                    }
                                    if (tier2 == null) {
                                        LOG.warning(String.format(
                                            "Tiers with same name in different files: tier \"%s\" not found in "
                                            + "transcription \"%s\"",
                                            tierName,
                                            ti2.getName()));
                                    }
                                }
                            }
                        } else { // tiers matching based on affix, in different files based on affix
                            for (List<String> tierMatch : matchingTiers) {
                                String name1 = null;
                                String name2 = null;
                                for (int m = 0; m < tierMatch.size() - 1; m++) {
                                    for (int n = m + 1; n < tierMatch.size(); n++) {
                                        name1 = tierMatch.get(m);
                                        name2 = tierMatch.get(n);

                                        compareSegments.addAll(getCompareCombinations(ti1, ti2, name1, name2));
                                    }
                                }
                            }
                        }
                    }
                }
                curProgress += perMatch;
                progressUpdate((int) curProgress, null);
            }
        }
        progressUpdate((int) curProgress,
                       String.format("Extracted the annotations of %1$d pairs of tiers from %2$d files...",
                                     compareSegments.size(),
                                     selFiles.size()));
    }

    /**
     * Creates a list of AnnotationCore objects from the annotations in the tier.
     *
     * @param t the tier
     *
     * @return a list of annotation core objects
     */
    private List<AnnotationCore> getAnnotationCores(TierImpl t) {
        List<AnnotationCore> acs = new ArrayList<AnnotationCore>();

        if (t != null) {
            List<AbstractAnnotation> anns = t.getAnnotations();

            if (anns != null) {
                for (int i = 0; i < anns.size(); i++) {
                    AnnotationCore ac = anns.get(i);
                    acs.add(new AnnotationCoreImpl(ac.getValue(), ac.getBeginTimeBoundary(), ac.getEndTimeBoundary()));
                }
                if (anns.isEmpty()) {
                    LOG.warning(String.format("There are no annotations on tier \"%s\", cannot retrieve segments.",
                                              t.getName()));
                }
            } else {
                LOG.warning(String.format("There are no annotations on tier \"%s\", cannot retrieve segments.",
                                          t.getName()));
            }
        } else {
            LOG.warning("The tier is null, cannot retrieve segments.");
        }

        return acs;
    }

    private CompareCombi createCompareCombi(TierImpl t1, TierImpl t2) {
        if (t1 == null || t2 == null) {
            LOG.warning(String.format("Cannot compare tiers: t1 is \"%s\", t2 is \"%s\".",
                                      (t1 == null ? "null" : t1.getName()),
                                      (t2 == null ? "null" : t2.getName())));
            return null;
        }

        List<AnnotationCore> segments1 = getAnnotationCores(t1);
        List<AnnotationCore> segments2 = getAnnotationCores(t2);
        if (segments1.isEmpty() && segments2.isEmpty()) {
            LOG.warning(String.format("Cannot compare tiers \"%s\" and \"%s\", both are empty (no annotations at all).",
                                      t1.getName(),
                                      t2.getName()));
            return null;
        }

        CompareUnit cu1 = new CompareUnit(t1.getTranscription().getFullPath(), t1.getName(), t1.getAnnotator());
        cu1.annotations = segments1;
        CompareUnit cu2 = new CompareUnit(t2.getTranscription().getFullPath(), t2.getName(), t2.getAnnotator());
        cu2.annotations = segments2;

        return new CompareCombi(cu1, cu2);
    }

    private CompareCombiMulti createCompareCombi(List<TierImpl> tierList) {
        if (tierList == null) {
            LOG.warning("Cannot compare tiers, the list is null or empty");
            return null;
        }
        List<CompareUnit> cuList = new ArrayList<CompareUnit>(tierList.size());
        CompareCombiMulti ccm = new CompareCombiMulti(cuList);

        for (TierImpl t : tierList) {
            List<AnnotationCore> segments = getAnnotationCores(t);

            //if (!segments.isEmpty()) { // include or exclude empty tiers?
            CompareUnit cu = new CompareUnit(t.getTranscription().getFullPath(), t.getName(), t.getAnnotator());
            cu.annotations = segments;
            cuList.add(cu);
            String cvName = t.getLinguisticType().getControlledVocabularyName();
            if (cvName != null) {
                ccm.setCVName(cvName);
            }
            ccm.addValues(getValues(t));
            // } else {
            //  LOG.info(String.format(
            //    "Cannot include tier \"%s\", it is empty (no annotations at all).", t.getName()));
            //  }
        }

        if (cuList.size() > 1) {
            return ccm;
        } else {
            LOG.warning("The number of tiers with annotations to compare is < 2, cannot compare.");
            return null;
        }
    }

    /**
     * Returns a transcription object for a  file or null.
     *
     * @param f the file
     *
     * @return a transcription object or null
     */
    private TranscriptionImpl createTranscription(File f) {
        if (f == null || f.isDirectory()) {
            return null;
        }
        try {
            return new TranscriptionImpl(f.getAbsolutePath());
        } catch (Throwable t) { // catch any
            // log
            LOG.warning("Could not load a transcription from file: " + f.getName());
        }
        return null;
    }

    /**
     * Creates a list of transcriptions based on the list of files.
     *
     * @param files the list of files to load, not null
     *
     * @return a list of transcriptions
     */
    private List<TranscriptionImpl> createTranscriptions(List<File> files) {
        List<TranscriptionImpl> transList = new ArrayList<TranscriptionImpl>(files.size());

        for (File f : files) {
            //long st = System.currentTimeMillis();
            TranscriptionImpl ti = createTranscription(f);
            //System.out.println("Complete: " + f.getName() + ": " + (System.currentTimeMillis() - st));
            if (ti != null) {
                transList.add(ti);
            }
        }
        return transList;
    }

    /**
     * Returns a list of tier combinations for comparing, the list contains 0, 1 or 2 elements.
     *
     * @param t1 first transcription
     * @param t2 second transcription
     * @param tierName1 first tier name
     * @param tierName2 second tier name
     *
     * @return a list of CompareCombi objects
     */
    private List<CompareCombi> getCompareCombinations(TranscriptionImpl t1, TranscriptionImpl t2, String tierName1,
                                                      String tierName2) {
        List<CompareCombi> combinations = new ArrayList<CompareCombi>();
        if (t1 == null || t2 == null) {
            LOG.warning(String.format(
                "Cannot compare tiers (\"%s\", \"%s\") from transcriptions: transcription 1 is \"%s\", transcription "
                + "2 is \"%s\".",
                tierName1,
                tierName2,
                (t1 == null ? "null" : t1.getName()),
                (t2 == null ? "null" : t2.getName())));
            return combinations;
        }
        TierImpl tier1 = null;
        TierImpl tier2 = null;

        tier1 = t1.getTierWithId(tierName1);
        tier2 = t2.getTierWithId(tierName2);

        if (tier1 != null && tier2 != null) {
            CompareCombi cc = createCompareCombi(tier1, tier2);
            if (cc != null) {
                combinations.add(cc);
            }
        } else {
            LOG.info(String.format("Tier \"%s\" not in transcription \"%s\" and/or tier \"%s\" not in transcription \"%s\".",
                                   tierName1,
                                   t1.getName(),
                                   tierName2,
                                   t2.getName()));
        }
        tier1 = t1.getTierWithId(tierName2);
        tier2 = t2.getTierWithId(tierName1);

        if (tier1 != null && tier2 != null) {
            CompareCombi cc = createCompareCombi(tier1, tier2);
            if (cc != null) {
                combinations.add(cc);
            }
        } else {
            LOG.info(String.format("Tier \"%s\" not in transcription \"%s\" and/or tier \"%s\" not in transcription \"%s\".",
                                   tierName1,
                                   t2.getName(),
                                   tierName2,
                                   t1.getName()));
        }

        return combinations;
    }

    /**
     * Creates and returns a list of comparison combinations extracted from different files. This variant assumes the "same
     * tier name" matching mode; for each of the selected names it tries to find the tier in each transcription.
     *
     * @param transList the list of transcriptions
     * @param tierNames the list of tiers to include
     *
     * @return a list of {@code CompareCombiMulti} instances or {@code null}
     */
    private List<? extends CompareCombi> getCompareCombinationsMulti(List<TranscriptionImpl> transList,
                                                                     List<String> tierNames) {
        List<CompareCombiMulti> combiList = new ArrayList<>();

        for (String name : tierNames) {
            List<CompareUnit> cuList = new ArrayList<>();
            CompareCombiMulti ccm = new CompareCombiMulti(cuList);

            for (TranscriptionImpl tr : transList) {
                TierImpl t = tr.getTierWithId(name);
                if (t != null) {
                    CompareUnit cu1 = new CompareUnit(tr.getFullPath(), t.getName(), t.getAnnotator());
                    cu1.annotations = getAnnotationCores(t);
                    //if (!cu1.annotations.isEmpty()) { //skip tiers with 0 annotations?
                    cuList.add(cu1);
                    // the tiers are or should be linked to the same CV, if any,
                    // the following probably only needs to be done once
                    String cvName = t.getLinguisticType().getControlledVocabularyName();
                    if (cvName != null) {
                        ccm.setCVName(cvName);
                    }
                    ccm.addValues(getValues(t));
                    //}
                }
            }
            if (cuList.size() > 1) {
                combiList.add(ccm);
            } else {
                LOG.info("with less than 2 raters nothing to compare");
            }
        }

        return combiList;
    }

    /**
     * Creates and returns a compare combination containing two or more tiers from the same transcription.
     *
     * @param tr the transcription
     * @param tierNames the selected or matched tier names
     *
     * @return a compare combination or {@code null}
     */
    private CompareCombiMulti getCompareCombinationMulti(TranscriptionImpl tr, List<String> tierNames) {
        // checks
        List<CompareUnit> cuList = new ArrayList<CompareUnit>(tierNames.size());
        CompareCombiMulti ccm = new CompareCombiMulti(cuList);

        for (String name : tierNames) {
            TierImpl t = tr.getTierWithId(name);
            CompareUnit cu = new CompareUnit(tr.getFullPath(), name, t.getAnnotator());
            cu.annotations = getAnnotationCores(t);
            //if (cu.annotations.size() > 0) { // skip empty tiers, or include to maintain the correct number of raters
            cuList.add(cu);
            String cvName = t.getLinguisticType().getControlledVocabularyName();
            if (cvName != null) {
                ccm.setCVName(cvName);
            }
            ccm.addValues(getValues(t));
            //            } else {
            //                LOG.info(String.format(
            //                        "Excluding empty tier \"%s\" from file \"%s\".", t.getName(), tr.getFullPath()));
            //            }
        }

        if (cuList.size() > 1) {
            return ccm;
        } else {
            LOG.warning(String.format("Cannot compare selected tiers from file \"%s\": less than 2 tiers with annotations",
                                      tr.getFullPath()));
        }

        return null;
    }

    /**
     * Tries to create one compare combination with tiers from multiple files and with different names. This variant does not
     * assume that e.g. the same affix has been used for file and tier matching, instead it assumes each tier is only present
     * and non-empty in one transcription. The first valid combination found is returned.
     *
     * <p>E.g. if 3 files have been matched named {@code file_R1.eaf}, {@code file_R2.eaf}
     * and {@code file_R3.eaf}, tiers could have been selected and matched named {@code tier_A}, {@code tier_B} and
     * {@code tier_C} and no assumptions can be made as to which tier has to be found in which file.
     *
     * <p>It is expected that the number of transcriptions is equal to the number
     * of tiers, but if not a partial combination might be returned.
     *
     * @param transList the list of matched transcriptions
     * @param tierNames the list of matched tier names
     *
     * @return a single compare combination or {@code null}
     */
    private CompareCombiMulti getCompareCombinationMulti(List<TranscriptionImpl> transList, List<String> tierNames) {
        if (transList.size() != tierNames.size()) {
            LOG.warning(String.format("The list of transcriptions and the list of tiers are not of the same size (%d and "
                                      + "%d)", transList.size(), tierNames.size()));
        }
        List<CompareUnit> cuList = new ArrayList<>(transList.size());
        CompareCombiMulti ccm = new CompareCombiMulti(cuList);
        List<TranscriptionImpl> usedTrans = new ArrayList<>(transList.size());

        for (String name : tierNames) {
            for (TranscriptionImpl tr : transList) {
                if (usedTrans.contains(tr)) {
                    continue;
                }
                TierImpl tier = tr.getTierWithId(name);
                // remove the test for 0 annotations?
                if (tier != null /*&& tier.getNumberOfAnnotations() > 0*/) {
                    CompareUnit cu = new CompareUnit(tr.getFullPath(), tier.getName(), tier.getAnnotator());
                    cu.annotations = getAnnotationCores(tier);
                    cuList.add(cu);
                    String cvName = tier.getLinguisticType().getControlledVocabularyName();
                    if (cvName != null) {
                        ccm.setCVName(cvName);
                    }
                    ccm.addValues(getValues(tier));
                    usedTrans.add(tr);
                    break;
                } else {
                    LOG.info("Tier is null.");
                }
            }
        }

        if (cuList.size() > 1) {
            return ccm;
        }

        return null;
    }

    /**
     * Returns a list of values, codes or categories for the specified tier. If possible this is taken from an associated
     * controlled vocabulary, else the values of the annotations of the tier are collected.
     *
     * @param tier the tier to get the list of values for
     *
     * @return a list of available or used values or categories
     */
    private List<String> getValues(Tier tier) {
        List<String> values = new ArrayList<String>();
        if (tier.getLinguisticType().isUsingControlledVocabulary()) {
            ControlledVocabulary cv =
                tier.getTranscription().getControlledVocabulary(tier.getLinguisticType().getControlledVocabularyName());

            for (String s : cv.getValuesIterable(cv.getDefaultLanguageIndex())) {
                values.add(s);
            }

        } else {
            for (AnnotationCore ac : tier.getAnnotations()) {
                String v = ac.getValue().strip();
                if (!values.contains(v)) {
                    values.add(v);
                }
            }
        }

        return values;
    }

    /**
     * Converts the list to the comma separated transcription names as a one string.
     *
     * @param trList list of transcriptions
     *
     * @return comma separated string of transcription names
     */
    protected String getNames(List<TranscriptionImpl> trList) {
        StringBuffer sb = new StringBuffer();
        for (TranscriptionImpl tr : trList) {
            sb.append(tr.getName());
            sb.append(", ");
        }
        return sb.substring(0, sb.length() - 2);
    }

    /**
     * Applies the selected algorithm to the segments to calculate agreement values.
     */
    protected void calculateAgreement() {

    }

    /**
     * Saves the results to a file. The default is to write comparison combinations with an agreement value to a text file.
     * Note: maybe there is no need for a default implementation.
     *
     * @param toFile the file to write to
     * @param encoding the encoding to use when saving as text file
     *
     * @throws IOException any IO exception
     */
    public void writeResultsAsText(File toFile, String encoding) throws
                                                                 IOException {
        CompareResultWriter crWriter = new CompareResultWriter();
        crWriter.writeResults(compareSegments, toFile, encoding);
    }

    /**
     * Starts the calculations in a separate thread. From this thread some methods are called that can be or have to be
     * implemented by the actual class.
     */
    class CompareAnnotatorsThread extends Thread {

        @Override
        public void run() {
            // pre-processing
            createSegments();
            // actual calculation
            if (!errorOccurred && !cancelled) {
                calculateAgreement();
            }
        }

    }


}
