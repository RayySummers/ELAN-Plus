package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.ReportDialog;
import mpi.eudico.client.annotator.interannotator.CompareConstants;
import mpi.eudico.client.annotator.interannotator.TierAndFileMatcher;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.server.corpora.util.SimpleReport;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A command to add the new dependent tiers to the transcription file The command first matches the input data ( participant
 * + prefux/suffix) combination to the existing tiers and the new dependent tiers are added to the matched tiers in the
 * transcription
 */
public class AddDependentTiersToTierStructureCommand implements UndoableCommand {

    private final String commandName;
    private TranscriptionImpl transcription;
    private List<String> selectedParticipants;
    private Boolean changePrefix;
    private String newSuffixOrPrefixValue;
    private Tier parentTier;
    private String annotator;
    private String lingType;
    private Locale locale;
    private String langRef;
    private String separator;
    private CompareConstants.MATCHING tierMatching;
    private TierAndFileMatcher tierMatcher;
    private TierImpl newTier = null;
    private SimpleReport report;

    private ArrayList<TierImpl> newTiers;

    /**
     * Constructor.
     *
     * @param name the name of the command
     */
    public AddDependentTiersToTierStructureCommand(String name) {
        this.commandName = name;
    }


    /**
     * @param receiver the TranscriptionImpl
     * @param arguments the arguments: <ul>
     *     <li>arg[0] = the selected participants (ArrayList)</li>
     *     <li>arg[1] = prefix/suffix indicator(Boolean )</li>
     *     <li>arg[2] = new prefix/suffix value (String)</li>
     *     <li>arg[3] = parent tier (TierImpl)</li>
     *     <li>arg[4] = annotator (String)</li>
     *     <li>arg[5] = linguistic type (String)</li>
     *     <li>arg[6] = locale (Locale)</li>
     *     <li>arg[7] = language reference (String)</li>
     *     <li>arg[8] = separator character(String)</li>
     *     </ul>
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {

        this.transcription = (TranscriptionImpl) receiver;
        this.selectedParticipants = (List<String>) arguments[0];
        this.changePrefix = (Boolean) arguments[1];
        this.newSuffixOrPrefixValue = (String) arguments[2];
        this.parentTier = (TierImpl) arguments[3];
        this.annotator = (String) arguments[4];
        this.lingType = (String) arguments[5];
        this.locale = (Locale) arguments[6];
        this.langRef = (String) arguments[7];
        this.separator = (String) arguments[8];

        tierMatcher = new TierAndFileMatcher();

        newTiers = new ArrayList<TierImpl>();
        report = new SimpleReport(ElanLocale.getString("AddDependentTierToTierStructureDlg.Title"));

        List<String> allTierNames = new ArrayList<>();
        transcription.getTiers().forEach(tier -> allTierNames.add(tier.getName()));

        List<String> exampleParentTierName = new ArrayList<>();
        exampleParentTierName.add(parentTier.getName());

        if (changePrefix) {
            tierMatching = CompareConstants.MATCHING.SUFFIX;
        } else {
            tierMatching = CompareConstants.MATCHING.PREFIX;
        }

        List<List<String>> matchedTiers =
            tierMatcher.getMatchingTiers(allTierNames, exampleParentTierName, tierMatching, separator);

        if (matchedTiers.isEmpty() && !exampleParentTierName.isEmpty()) {
            matchedTiers.add(exampleParentTierName);
        }

        String newTierName;

        if (!matchedTiers.isEmpty() && !matchedTiers.get(0).isEmpty()) {

            for (String parentTierName : matchedTiers.get(0)) {
                for (String participant : selectedParticipants) {

                    if (parentTierName.contains(participant)) {
                        if (changePrefix) {
                            newTierName = newSuffixOrPrefixValue + separator + participant;
                        } else {
                            newTierName = participant + separator + newSuffixOrPrefixValue;
                        }
                        newTier = new TierImpl(transcription.getTierWithId(parentTierName),
                                               newTierName,
                                               null,
                                               transcription,
                                               null);

                        List<LinguisticType> types = transcription.getLinguisticTypes();

                        for (LinguisticType type : types) {
                            if (type.getLinguisticTypeName().equals(lingType)) {
                                newTier.setLinguisticType(type);
                                break;
                            }
                        }

                        newTier.setParticipant(participant);
                        newTier.setAnnotator(annotator);
                        newTier.setDefaultLocale(locale);
                        newTier.setLangRef(langRef);

                        if (transcription.getTierWithId(newTier.getName()) == null) {
                            newTiers.add(newTier);
                            transcription.addTier(newTier);

                            report("Created the dependent tier for : " + parentTierName);
                            report("New dependent tier : " + newTier.getName());
                            report("\n");
                        }


                    }
                }
            }
        }

        report("Total Number of tiers created : " + newTiers.size());
        ReportDialog dialog = new ReportDialog(report);
        dialog.setModal(true);
        dialog.setAlwaysOnTop(true);
        dialog.setVisible(true);
        setWaitCursor(false);

    }

    @Override
    public String getName() {
        return commandName;
    }


    /**
     * The undo action. removes the new tiers from the transcription
     */
    @Override
    public void undo() {
        if ((transcription != null) && (newTiers != null)) {
            for (int i = 0; i < newTiers.size(); i++) {
                TierImpl t = newTiers.get(i);
                transcription.removeTier(t);
            }
        }
    }

    /**
     * The redo action. Adds the created tiers to the transcription
     */
    @Override
    public void redo() {
        if ((transcription != null) && (newTiers != null)) {
            int curPropMode = transcription.getTimeChangePropagationMode();

            if (curPropMode != Transcription.NORMAL) {
                transcription.setTimeChangePropagationMode(Transcription.NORMAL);
            }
            setWaitCursor(true);

            TierImpl tier = null;

            for (int i = 0; i < newTiers.size(); i++) {
                tier = newTiers.get(i);

                if (transcription.getTierWithId(tier.getName()) == null) {
                    transcription.addTier(tier);
                }
            }

            setWaitCursor(false);
            // restore the time propagation mode
            transcription.setTimeChangePropagationMode(curPropMode);
        }
    }

    /**
     * Changes the cursor to either a 'busy' cursor or the default cursor.
     *
     * @param showWaitCursor when <code>true</code> show the 'busy' cursor
     */
    private void setWaitCursor(boolean showWaitCursor) {
        if (showWaitCursor) {
            ELANCommandFactory.getRootFrame(transcription)
                              .getRootPane()
                              .setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        } else {
            ELANCommandFactory.getRootFrame(transcription).getRootPane().setCursor(Cursor.getDefaultCursor());
        }
    }

    /**
     * Adds a message to the report.
     *
     * @param message the message
     */
    public void report(String message) {
        if (report != null) {
            report.append(message);
        }
    }


    /**
     * Returns the parent tier
     *
     * @return the parent tier
     */
    public Tier getParentTier() {
        return parentTier;
    }

    /**
     * Sets the parent tier
     *
     * @param parentTier the parent tier
     */
    public void setParentTier(Tier parentTier) {
        this.parentTier = parentTier;
    }


}
