package mpi.eudico.client.annotator.export.multiplefiles;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * First step in the export of the number of annotated seconds of the media, on selected tiers, as absolute value and
 * compared to the highest annotation end-time or, if possible, the total media duration. This can be interpreted as an
 * indication of the "level of completeness", it can be applied to multiple files or a single transcription.
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class MFExportAnnotatedPartStep1 extends AbstractFilesAndTierSelectionStepPane {

    /**
     * Creates the panel for the first step of the export.
     *
     * @param mp the parent pane
     * @param transcription the transcription or {@code null} for multiple files
     */
    public MFExportAnnotatedPartStep1(MultiStepPane mp, TranscriptionImpl transcription) {
        super(mp, transcription);
    }

    @Override
    protected void initComponents() {
        super.initComponents();
        String mode = Preferences.getString("ExportAnnotatedPartStep1.FileMode", transcription);
        if (mode != null) {
            if (mode.equals("domain")) {
                filesFromDomainRB.setSelected(true);
                selectFilesBtn.setEnabled(false);
                selectDomainBtn.setEnabled(true);
            } else if (mode.equals("files")) {
                selectedFilesFromDiskRB.setSelected(true);
                selectFilesBtn.setEnabled(true);
                selectDomainBtn.setEnabled(false);
            } else if (transcription != null && mode.equals("transcription")) {
                currentlyOpenedFileRB.setSelected(true);
                selectFilesBtn.setEnabled(false);
                selectDomainBtn.setEnabled(false);
            }
        }
    }

    @Override
    public String getStepTitle() {
        return ElanLocale.getString("MultiFileExportAnnPart.Step1.Title");
    }

    @Override
    public void updateButtonStates() {
        //multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);
        multiPane.setButtonEnabled(MultiStepPane.PREVIOUS_BUTTON, false);
        multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON,
                                   (tierSelectPanel != null && tierSelectPanel.getSelectedTiers().size() > 0));
        multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON,
                                   (tierSelectPanel != null && tierSelectPanel.getSelectedTiers().size() > 0));
    }

    @Override
    public boolean leaveStepForward() {
        String mode = filesFromDomainRB.isSelected() ? "domain" : "files";
        if (transcription != null && currentlyOpenedFileRB.isSelected()) {
            mode = "transcription";
        }
        Preferences.set("ExportAnnotatedPartStep1.FileMode", mode, transcription);
        return super.leaveStepForward();
    }

    /**
     * Calls {@link #leaveStepForward()}.
     */
    @Override
    public boolean doFinish() {
        leaveStepForward();
        multiPane.nextStep();
        return false;
    }

}
