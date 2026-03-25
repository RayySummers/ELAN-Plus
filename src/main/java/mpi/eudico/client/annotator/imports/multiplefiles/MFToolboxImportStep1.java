package mpi.eudico.client.annotator.imports.multiplefiles;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import nl.mpi.util.FileExtension;

/**
 * Step 1: Step pane for selecting multiple files
 */
@SuppressWarnings("serial")
public class MFToolboxImportStep1 extends AbstractMFImportStep1 {

    /**
     * Constructor
     *
     * @param mp the multiple step pane
     */
    public MFToolboxImportStep1(MultiStepPane mp) {
        super(mp);
    }

    @Override
    protected Object[] getMultipleFiles() {
        Object[] files = getMultipleFiles(ElanLocale.getString("MultiFileImport.Toolbox.Select"),
                                          FileExtension.TOOLBOX_TEXT_EXT,
                                          "LastUsedShoeboxTypDir",
                                          FileChooser.FILES_AND_DIRECTORIES);

        if ((files == null) || (files.length == 0)) {
            return null;
        }

        return getFilesFromFilesAndFolders(files, FileExtension.TOOLBOX_TEXT_EXT);
    }

}
