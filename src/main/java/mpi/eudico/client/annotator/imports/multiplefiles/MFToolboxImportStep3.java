package mpi.eudico.client.annotator.imports.multiplefiles;

import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;

/**
 * Step 3: Abstract step pane to set the save as settings
 */
public class MFToolboxImportStep3 extends AbstractMFImportStep3 {

    /**
     * Constructor
     *
     * @param multiStepPane the multiple step pane
     */
    public MFToolboxImportStep3(MultiStepPane multiStepPane) {
        super(multiStepPane);
    }

    /**
     * Set the Toolbox preference strings
     */
    @Override
    protected void setPreferenceStrings() {
        saveWithOriginalNames = "MFToolBoxImport.saveWithOriginalNames";
        saveInOriginalFolder = "MFToolBoxImport.saveInOriginalFolder";
        saveInRelativeFolder = "MFToolBoxImport.saveInRelativeFolder";
        saveInRelativeFolderName = "MFToolBoxImport.saveInRelativeFolderName";
        saveInSameFolderName = "MFToolBoxImport.saveInSameFolderName";
    }
}
