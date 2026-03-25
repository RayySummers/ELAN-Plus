package mpi.eudico.client.annotator.export.multiplefiles;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.ProgressStepPane;
import mpi.eudico.client.annotator.imports.multiplefiles.ApprovalEnum;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

import javax.swing.*;
import java.io.File;
import java.util.List;

import static mpi.eudico.client.annotator.util.ClientLogger.LOG;

/**
 * Abstract Step Pane. This is the final step and actual export is done here. The ui is a progress monitor.
 *
 * @author aarsom
 */
@SuppressWarnings("serial")
public abstract class AbstractMultiFileExportProgessStepPane extends ProgressStepPane {

    private List<String> files;
    /**
     * list of selected tiers
     */
    protected List<String> selectedTiers;
    private boolean useOriginalDir;
    private boolean newDir;
    private String newDirName;
    private boolean sameDir;
    private String sameDirName;
    private boolean dontExportFilesWithoutTiers;
    private boolean useOriginalFileName;
    private boolean useOriginalFileNameWithSuffix;
    private String extension;

    /**
     * Constructor
     *
     * @param multiPane the container pane
     */
    public AbstractMultiFileExportProgessStepPane(MultiStepPane multiPane) {
        super(multiPane);

        initComponents();
    }

    @Override
    public String getStepTitle() {
        return ElanLocale.getString("MultiFileExport.ProgessPane.Title");
    }

    /**
     * Calls doFinish.
     */
    @Override
    public void enterStepForward() {
        files = (List<String>) multiPane.getStepProperty("OpenedFiles");
        selectedTiers = (List<String>) multiPane.getStepProperty("SelectedTiers");
        useOriginalDir = (Boolean) multiPane.getStepProperty("UseOriginalDir");
        newDir = (Boolean) multiPane.getStepProperty("NewDirectory");
        newDirName = (String) multiPane.getStepProperty("NewDirName");
        sameDir = (Boolean) multiPane.getStepProperty("TogetherInSameDir");
        sameDirName = (String) multiPane.getStepProperty("SameDirectoryName");

        Object doNotExportFiles = multiPane.getStepProperty("DontExportFilesWithoutTiers");
        if (doNotExportFiles instanceof Boolean) {
            dontExportFilesWithoutTiers = (Boolean) doNotExportFiles;
        }

        useOriginalFileName = (Boolean) multiPane.getStepProperty("UseOriginalFileName");
        useOriginalFileNameWithSuffix = (Boolean) multiPane.getStepProperty("UseOriginalFileNameWithSuffix");

        extension = (String) multiPane.getStepProperty("ExportExtension");

        doFinish();
    }

    /**
     * Disables all buttons, starts the export in a separate thread and returns {@code false}
     *
     * @return {@code false}
     */
    @Override
    public boolean doFinish() {
        completed = false;

        // disable buttons
        multiPane.setButtonEnabled(MultiStepPane.ALL_BUTTONS, false);

        Thread t = new ExportThread();
        t.start();

        // the action is performed on a separate thread, don't close
        return false;
    }

    /**
     * The actual writing.
     *
     * @param transImpl the transcription
     * @param fileName path to the file, not null
     *
     * @return true if all went well, false otherwise
     */
    protected abstract boolean doExport(TranscriptionImpl transImpl, String fileName);

    private class ExportThread extends Thread {

        /**
         * Returns the directory where the corresponding file needs to be exported in. This depends on the selected options
         * for the export
         *
         * @param fileName particular file where the directory is computed for
         *
         * @return the directory where the file needs to be saved
         */
        private String getDirectoryToSave(String fileName) {
            int index = fileName.lastIndexOf(File.separatorChar);
            String originalDirectory = fileName.substring(0, index + 1);

            //if original directory is selected
            if (useOriginalDir) {
                return originalDirectory;
            }

            //if new directory has been selected
            if (newDir) {
                return originalDirectory + newDirName;
            }

            //if same directory has been chosen
            if (sameDir) {
                return sameDirName;
            }

            return null;
        }

        /**
         * Gets the file name of the specified transcription and edits it to the format specified for the export
         *
         * @param path the path of the new file
         * @param transImpl the transcription implementation of the file
         *
         * @return the name of the file to be saved or null if no name could be found
         */
        private String getFileName(String path, TranscriptionImpl transImpl) {
            if (extension != null) {
                if (!extension.startsWith(".")) {
                    extension = "." + extension;
                }

                String orifileName = transImpl.getName();
                int index = orifileName.lastIndexOf('.');
                orifileName = orifileName.substring(0, index);

                if (useOriginalFileName) {
                    return orifileName + extension;
                }

                if (useOriginalFileNameWithSuffix) {
                    int i = 1;
                    String fileName = orifileName;
                    File f = new File(path + fileName + extension);
                    while (f.exists()) {
                        fileName = orifileName + i;
                        f = new File(path + fileName + extension);
                        i = i + 1;
                    }
                    return fileName + extension;
                }
            }
            return null;
        }

        /**
         * Checks if the given directory exists. If it does not exist, it will try to create one.
         *
         * @param directoryPath the path to an existing (or non-existing) directory
         *
         * @return boolean value indicating if directory exists (and is created successfully if needed)
         */
        private boolean createDirectory(String directoryPath) {
            boolean directoryExists = true;
            File directory = new File(directoryPath);

            //if directory does not exist, try to make it
            if (!directory.exists()) {
                directoryExists = (new File(directoryPath)).mkdir();
            }

            return directoryExists;
        }

        /**
         * The start of the save thread. All files will be saved here.
         */
        @Override
        public void run() {
            int failedExports = 0; //counter to count number of failed exports
            int refusedExports =
                0; //nr exports refused because user indicated that files should not overwrite existing files
            int emptyFiles = 0; //nr of files not exported because they were empty
            boolean saveForever = false;
            boolean skipForever = false;

            String fileName;
            TranscriptionImpl transImpl;
            //walk through all transcriptions
            for (int f = 0; f < files.size(); f++) {
                fileName = files.get(f);

                if (fileName == null) {
                    continue;
                }

                try {
                    transImpl = new TranscriptionImpl(fileName);
                    //Try to save the transcription
                    String path = getDirectoryToSave(fileName);
                    if (path.charAt(path.length() - 1) != File.separatorChar) {
                        path += File.separatorChar;
                    }
                    String newfileName = path + getFileName(path, transImpl);

                    //if directory does not exist, then create it
                    boolean directoryExists = createDirectory(path);
                    boolean saveThisFile = true;
                    boolean fileExists = new File(newfileName).exists();

                    if (fileExists && skipForever) {
                        saveThisFile = false;
                    }

                    //overwrite files is not selected, then check if file exists and ask for overwriting
                    if (!skipForever && !saveForever && fileExists) {
                        //show dialog to ask if existing file should be overwritten
                        Object[] possibleValues = {"Yes To All", "Yes", "No", "No To All"};

                        String message = ElanLocale.getString("ExportTiersDialog.Message.OverwriteMessage.Description1")
                                         + " "
                                         + newfileName
                                         + " "
                                         + ElanLocale.getString("ExportTiersDialog.Message.OverwriteMessage.Description2")
                                         + "\n\n"
                                         + ElanLocale.getString("ExportTiersDialog.Message.OverwriteMessage.Description3")
                                         + "\t "
                                         + newfileName
                                         + "\n\n"
                                         + ElanLocale.getString("ExportTiersDialog.Message.OverwriteMessage.Description4")
                                         + "\n";

                        String title = ElanLocale.getString("ExportTiersDialog.Message.OverwriteMessage.Title");

                        int choice = JOptionPane.showOptionDialog(null,
                                                                  message,
                                                                  title,
                                                                  JOptionPane.DEFAULT_OPTION,
                                                                  JOptionPane.WARNING_MESSAGE,
                                                                  null,
                                                                  possibleValues,
                                                                  possibleValues[2]);

                        switch (ApprovalEnum.fromInt(choice)) {
                            case YES:
                                saveThisFile = true;
                                break;

                            case YES_TO_ALL:
                                saveForever = true;
                                break;

                            case NO_TO_ALL:
                                skipForever = true;
                                saveThisFile = false;
                                break;

                            default: //NO and other
                                saveThisFile = false;
                        }

                    }

                    //save files
                    if (directoryExists) {
                        //if all files need to be saved OR this file need to be saved
                        boolean exported = false;
                        if (saveForever || saveThisFile) {
                            if (!dontExportFilesWithoutTiers) {
                                exported = doExport(transImpl, newfileName);
                            } else {
                                if (!transImpl.getTiers().isEmpty()) {
                                    exported = doExport(transImpl, newfileName);
                                } else {
                                    emptyFiles++;
                                }
                            }
                        } else {
                            refusedExports++;
                        }
                        if (!exported) {
                            failedExports++;
                        }
                    } else {
                        failedExports++;
                    }
                } catch (Exception ex) {
                    // catch any exception that could occur and continue
                    failedExports++;
                    LOG.warning("Could not handle file: " + fileName);
                }


                //update progress bar
                if ((f + 1) < files.size()) {
                    progressUpdated(AbstractMultiFileExportProgessStepPane.this,
                                    Math.round(100 * (f + 1) / (float) files.size()),
                                    null);
                }
            }

            //show information on the export process
            String outOfTextMessage = ElanLocale.getString("ExportTiersDialog.Message.OutOf");
            String infoTextMessage = ElanLocale.getString("ExportTiersDialog.Message.InfoMsg1");
            int numOfExportedFiles = files.size() - failedExports - refusedExports - emptyFiles;
            String msg = numOfExportedFiles + " " + outOfTextMessage + " " + files.size() + " " + infoTextMessage;

            //construct additional details if not all files are exported successfully
            if (emptyFiles + refusedExports + failedExports > 0) {
                if (emptyFiles == 1) {
                    msg += "\n\n" + emptyFiles + " " + ElanLocale.getString("ExportTiersDialog.Message.InfoMsg2single");
                } else {
                    msg += "\n\n" + emptyFiles + " " + ElanLocale.getString("ExportTiersDialog.Message.InfoMsg2");
                }

                if (refusedExports == 1) {
                    msg += "\n" + refusedExports + " " + ElanLocale.getString("ExportTiersDialog.Message.InfoMsg3single");
                } else {
                    msg += "\n" + refusedExports + " " + ElanLocale.getString("ExportTiersDialog.Message.InfoMsg3");
                }

                if (failedExports == 1) {
                    msg += "\n" + failedExports + " " + ElanLocale.getString("ExportTiersDialog.Message.InfoMsg4single");
                } else {
                    msg += "\n" + failedExports + " " + ElanLocale.getString("ExportTiersDialog.Message.InfoMsg4");
                }
            }

            //exporting finished, so update progress bar
            progressCompleted(AbstractMultiFileExportProgessStepPane.this, msg);
        }
    }
}
