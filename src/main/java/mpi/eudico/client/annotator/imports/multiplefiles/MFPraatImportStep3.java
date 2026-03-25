package mpi.eudico.client.annotator.imports.multiplefiles;

import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;

/**
 * Step 3: Abstract pane to set the save as settings
 */
public class MFPraatImportStep3 extends AbstractMFImportStep3 {

    /**
     * Constructor
     *
     * @param multiStepPane the multi step pane
     */
    public MFPraatImportStep3(MultiStepPane multiStepPane) {
        super(multiStepPane);
    }

    /**
     * Set the Praat preference strings.
     */
    @Override
    protected void setPreferenceStrings() {
        saveWithOriginalNames = "MFPraatImport.saveWithOriginalNames";
        saveInOriginalFolder = "MFPraatImport.saveInOriginalFolder";
        saveInRelativeFolder = "MFPraatImport.saveInRelativeFolder";
        saveInRelativeFolderName = "MFPraatImport.saveInRelativeFolderName";
        saveInSameFolderName = "MFPraatImport.saveInSameFolderName";
    }
}
