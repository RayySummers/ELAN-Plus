package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.*;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.timeseries.io.TSConfigurationEncoder;
import mpi.eudico.client.annotator.util.MonitoringLogger;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clom.TranscriptionStore;
import mpi.eudico.server.corpora.clomimpl.abstr.LinkedFileDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import nl.mpi.util.FileExtension;
import nl.mpi.util.FileUtility;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


/**
 * Saves a transcription as an {@code .eaf} or {@code .etf} (template) file, either creating a new file ({@code Save As}) or
 * overwriting an existing file ({@code Save}).<p> The command will save as 2.7 format, if the user preference indicates it.
 * There is no need to pass TranscriptionStore.EAF_2_7 for that purpose.<br> In fact, it must not be done: the logic to
 * determine if the transcription's name must be updated does so if the format is EAF, and not if it is EAF_2_7.
 *
 * @author Hennie Brugman
 * @version Nov 2007 added support for relative media paths
 */
public class StoreCommand implements Command {
    private final String commandName;

    /**
     * Creates a new StoreCommand instance
     *
     * @param name the name of the command
     */
    public StoreCommand(String name) {
        commandName = name;
    }

    /**
     * the underscore constant
     */
    public static final String UNDERSCORE = "_";

    /**
     * Stores a file.
     *
     * @param receiver the receiver of the command, the  transcription
     * @param arguments the parameters for the command
     *     <ul>
     *     <li>EAF TranscriptionStore (TranscriptionStore)
     *     <li>saveAsTemplate (Boolean)
     *     <li>saveNewCopy (Boolean)
     *     <li>visibleTiers ({@code List<TierImpl>})
     *     <li>format, optional (TranscriptionStore.EAF or TranscriptionStore.EAF_2_7)
     *     </ul>
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {
        TranscriptionImpl tr = (TranscriptionImpl) receiver;
        TranscriptionStore eafTranscriptionStore = (TranscriptionStore) arguments[0];
        boolean saveAsTemplate = (Boolean) arguments[1];
        boolean saveNewCopy = (Boolean) arguments[2];
        List<TierImpl> visibleTiers;
        List<TierImpl> tiers;

        if (arguments[3] != null) {
            if (arguments[3] instanceof List<?>) {
                visibleTiers = (List<TierImpl>) arguments[3];
            } else {
                visibleTiers = new ArrayList<>(0); // just to be on the safe side, should be ignored in most cases
            }
        } else {
            if (ELANCommandFactory.getViewerManager(tr).getMultiTierControlPanel() != null) {
                visibleTiers = ELANCommandFactory.getViewerManager(tr).getMultiTierControlPanel().getVisibleTiers();
            } else {
                visibleTiers = new ArrayList<>(0); // just to be on the safe side, should be ignored in most cases
            }
        }
        int format = TranscriptionStore.EAF;
        if (arguments.length > 4) {
            int f = (Integer) arguments[4];
            if (f == TranscriptionStore.EAF || f == TranscriptionStore.EAF_2_7) {
                format = f;
            }
        }
        // Only if we save in the default format, we change the file name of the current
        // transcription. For an Save A Copy As EAF 2.7... we keep such things unchanged.
        boolean updateFileName = (format == TranscriptionStore.EAF);

        // If the format is default, it can still be overridden by the preference.
        if (format == TranscriptionStore.EAF) {
            format = SaveAs27Preferences.saveAsType(tr);
        }

        // If either implicitly (based on preference) or explicitly the format is EAF_2_7
        // check if anything will be lost and ask for confirmation (unless suppressed by preference)
        if (format == TranscriptionStore.EAF_2_7) {
            boolean saveWillLose = SaveAs27Preferences.savingWillLoseInformation(tr);
            if (saveWillLose && !SaveAs27Preferences.askIfLosingInformationIsOk()) {
                return;

            }
        }

        if (saveNewCopy) {
            // prompt for new file name
            // open dialog at directory of original eaf file
            FileChooser chooser = new FileChooser(ELANCommandFactory.getRootFrame(tr));

            if (saveAsTemplate) {
                chooser.createAndShowFileDialog(ElanLocale.getString("SaveDialog.Template.Title"),
                                                FileChooser.SAVE_DIALOG,
                                                FileExtension.TEMPLATE_EXT,
                                                "LastUsedEAFDir");
            } else {
                String fileName = "";
                String appender = "";

                if (tr.getName().equals(TranscriptionImpl.UNDEFINED_FILE_NAME)) {

                    if (tr.getMediaDescriptors() != null && !tr.getMediaDescriptors().isEmpty()) {

                        MediaDescriptor md = tr.getMediaDescriptors().get(0);
                        fileName = FileUtility.fileNameFromPath(md.mediaURL);
                        String filePath = FileUtility.urlToAbsPath(md.mediaURL);
                        chooser.setCurrentDirectory(filePath.substring(0, filePath.indexOf(fileName)));

                        tiers = tr.getTiers();
                        List<String> annotators =
                            tiers.stream().map(TierImpl::getAnnotator).filter(annotator -> !annotator.isEmpty()).toList();

                        long distinctAnnotators = annotators.stream().distinct().count();

                        if (distinctAnnotators == 1) {
                            appender = UNDERSCORE + annotators.stream().findFirst().orElse("");
                        } else {
                            if (tr.getAuthor() != null && !tr.getAuthor().isEmpty()) {
                                appender = getShortAuthorName(tr.getAuthor());
                            }
                        }
                    }

                } else {
                    fileName = tr.getName();
                    String filePath = FileUtility.urlToAbsPath(tr.getFullPath());
                    chooser.setCurrentDirectory(filePath.substring(0, filePath.indexOf(fileName)));
                }
            	
				if (!fileName.isEmpty()) {
					if (FileUtility.isBackupFile(fileName)) {
						fileName = fileName.substring(0, fileName.lastIndexOf("."));
						if (fileName.endsWith(".eaf")) {
							fileName = fileName.substring(0, fileName.lastIndexOf(".")) + "_Restored" + ".eaf";
						}
					} else {
						int dotIndex = fileName.lastIndexOf('.');
						if (dotIndex > 1) {
							fileName = fileName.substring(0, dotIndex) + appender + ".eaf";
						} else {
							fileName = fileName + appender + ".eaf";
						}
					}
                    chooser.createAndShowFileDialog(ElanLocale.getString("SaveDialog.Title"),
                                                    FileChooser.SAVE_DIALOG,
                                                    null,
                                                    FileExtension.EAF_EXT,
                                                    null,
                                                    fileName);
                } else {
                    chooser.createAndShowFileDialog(ElanLocale.getString("SaveDialog.Title"),
                                                    FileChooser.SAVE_DIALOG,
                                                    FileExtension.EAF_EXT,
                                                    "LastUsedEAFDir");
                }
            }

            File f = chooser.getSelectedFile();
            if (f != null) {
                // make sure pathname finishes with .eaf or .etf extension
                String pathName = f.getAbsolutePath();
                if (saveAsTemplate) {
                    try {
                        eafTranscriptionStore.storeTranscriptionAsTemplateIn(tr, visibleTiers, pathName);

                        //overwrite the currentmode preferences such that, a new file created from a template
                        //should always open in annotation mode
                        Integer currentMode = Preferences.getInt("LayoutManager.CurrentMode", tr);
                        Preferences.set("LayoutManager.CurrentMode", ElanLayoutManager.NORMAL_MODE, tr);

                        // HS Nov 2009: save a preferences file alongside the template
                        storePreferences(tr, format, pathName);

                        // restore the currentmode for the current transcription preferences
                        if (currentMode != null) {
                            Preferences.set("LayoutManager.CurrentMode", currentMode, tr);
                        }
                    } catch (IOException ioe) {
                        JOptionPane.showMessageDialog(ELANCommandFactory.getRootFrame(tr),
                                                      //ElanLocale.getString("ExportDialog.Message.Error") + "\n" +
                                                      "Unable to save the template file: " + "(" + ioe.getMessage() + ")",
                                                      ElanLocale.getString("Message.Error"),
                                                      JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    storePreferences(tr, format, pathName);

                    String oldPathName = tr.getPathName();
                    String name = pathName;
                    int lastSlashPos = name.lastIndexOf(System.getProperty("file.separator"));

                    if (lastSlashPos >= 0) {
                        name = name.substring(lastSlashPos + 1);
                    }

                    if (updateFileName) {
                        //System.out.println("nm " + name);
                        tr.setName(name);

                        //tr.setName(pathName);
                        tr.setPathName(pathName);
                        ((ElanFrame2) ELANCommandFactory.getRootFrame(tr)).setFrameTitle();
                        FrameManager.getInstance().updateFrameTitle(ELANCommandFactory.getRootFrame(tr), pathName);
                    }

                    // check, copy and update linked files, configuration and svg files
                    List<LinkedFileDescriptor> linkedFiles = tr.getLinkedFileDescriptors();
                    String svgExt = ".svg";
                    String confExt = "_tsconf.xml";
                    String curExt;
                    if (!linkedFiles.isEmpty()) {
                        LinkedFileDescriptor lfd;
                        for (LinkedFileDescriptor linkedFile : linkedFiles) {
                            curExt = null;
                            lfd = linkedFile;
                            if (lfd.linkURL.toLowerCase(Locale.getDefault()).endsWith(confExt)) {
                                curExt = confExt;
                            } else if (lfd.linkURL.toLowerCase(Locale.getDefault()).endsWith(svgExt)) {
                                curExt = svgExt;
                            }
                            if (curExt != null) {
                                // ELAN generated configuration file, copy
                                String url = pathName.substring(0, pathName.length() - 4) + curExt;
                                System.out.println("New conf: " + url);
                                // copy conf or svg
                                try {
                                    File source = null;
                                    File dest = null;
                                    if (lfd.linkURL.startsWith("file:")) {
                                        source = new File(lfd.linkURL.substring(5));
                                    } else {
                                        source = new File(lfd.linkURL);
                                    }
                                    if (url.startsWith("file:")) {
                                        dest = new File(url.substring(5));
                                    } else {
                                        dest = new File(url);
                                    }
                                    if (source.exists() && source.compareTo(dest) != 0) {
                                        FileUtility.copyToFile(source, dest);
                                    } else {
                                        TSConfigurationEncoder enc = new TSConfigurationEncoder();
                                        enc.encodeAndSave(tr, ELANCommandFactory.getTrackManager(tr).getConfigs());
                                    }
                                } catch (Exception ex) {
                                    System.out.println("Could not copy the configuration file.");
                                }
                                lfd.linkURL = FileUtility.pathToURLString(url);
                                tr.setChanged();
                            }
                        }
                    }

                    // update relative media paths
                    // make sure the eaf path is treated the same way as media files,
                    // i.e. it starts with file:/// or file://
                    String fullEAFURL = FileUtility.pathToURLString(pathName);
                    fixRelativePathsOfLinkedFiles(tr, fullEAFURL);

                    // save
                    try {
                        eafTranscriptionStore.storeTranscriptionIn(tr, null, visibleTiers, pathName, format);
                        if (MonitoringLogger.isInitiated()) {
                            Objects.requireNonNull(MonitoringLogger.getLogger(tr)).log(MonitoringLogger.SAVE_FILE);
                        }
                    } catch (IOException ioe) {
                        JOptionPane.showMessageDialog(ELANCommandFactory.getRootFrame(tr),
                                                      //ElanLocale.getString("ExportDialog.Message.Error") + "\n" +
                                                      "Unable to save the transcription file: "
                                                      + "("
                                                      + ioe.getMessage()
                                                      + ")",
                                                      ElanLocale.getString("Message.Error"),
                                                      JOptionPane.ERROR_MESSAGE);
                    }
                    // HS Sept 2019 after a save as check file locks
                    FrameManager.getInstance().updateFileLock(ELANCommandFactory.getRootFrame(tr), oldPathName, pathName);
                    //                    String name = pathName;
                    //                    int lastSlashPos = name.lastIndexOf(System.getProperty(
                    //                                "file.separator"));
                    //
                    //                    if (lastSlashPos >= 0) {
                    //                        name = name.substring(lastSlashPos + 1);
                    //                    }
                    //
                    //                    //System.out.println("nm " + name);
                    //                    tr.setName(name);
                    //
                    //                    //tr.setName(pathName);
                    //                    if (tr instanceof TranscriptionImpl) {
                    //                        ((TranscriptionImpl) tr).setPathName(pathName);
                    //                        ELANCommandFactory.getRootFrame(tr).setTitle("ELAN - " +
                    //                            tr.getName());
                    //                        FrameManager.getInstance().updateFrameTitle(ELANCommandFactory.getRootFrame
                    //                        (tr),
                    //                                pathName);
                    //                    } else {
                    //                        ELANCommandFactory.getRootFrame(tr).setTitle("ELAN - " +
                    //                            name);
                    //                    }

                    if (updateFileName) {
                        tr.setUnchanged();
                        // create a new backup timer
                        ((BackupCA) ELANCommandFactory.getCommandAction(tr,
                                                                        ELANCommandFactory.BACKUP)).setFilePath(pathName);

                    }
                }
            }
        } else if (tr.isChanged()) {
            // check if relative media paths have to be generated or updated
            // make sure the eaf path is treated the same way as media files,
            // i.e. it starts with file:/// or file://
            String fullEAFURL = FileUtility.pathToURLString(tr.getFullPath());
            fixRelativePathsOfLinkedFiles(tr, fullEAFURL);

            try {
                eafTranscriptionStore.storeTranscription(tr, null, visibleTiers, format);
                if (MonitoringLogger.isInitiated()) {
                    MonitoringLogger.getLogger(tr).log(MonitoringLogger.SAVE_FILE);
                }
                if (ELANCommandFactory.getTrackManager(tr) != null) {
                    ELANCommandFactory.getTrackManager(tr).saveIfChanged();
                }
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(ELANCommandFactory.getRootFrame(tr),
                                              //ElanLocale.getString("ExportDialog.Message.Error") + "\n" +
                                              "Unable to save the transcription file: " + "(" + ioe.getMessage() + ")",
                                              ElanLocale.getString("Message.Error"),
                                              JOptionPane.ERROR_MESSAGE);
            }

            tr.setUnchanged();
        } else {

        }
    }

    private void fixRelativePathsOfLinkedFiles(Transcription tr, String fullEAFURL) {
        List<MediaDescriptor> mediaDescriptors = tr.getMediaDescriptors();
        String relUrl;

        for (MediaDescriptor md : mediaDescriptors) {
            relUrl = FileUtility.getRelativePath(fullEAFURL, md.mediaURL);
            md.relativeMediaURL = relUrl;
        }

        // linked other files
        List<LinkedFileDescriptor> linkedFiles = tr.getLinkedFileDescriptors();

        for (LinkedFileDescriptor lfd : linkedFiles) {
            relUrl = FileUtility.getRelativePath(fullEAFURL, lfd.linkURL);
            lfd.relativeLinkURL = relUrl;
        }
    }

    /**
     * Save the preferences, if need be converted for the format we're exporting as. It can be in the old format because of
     * the preference, but also because of explicit command.
     *
     * @param t the preferences belong to this
     * @param format this format might or might not need conversion
     * @param pathName the name of the eaf, to find the name of the preferences.
     */
    private void storePreferences(Transcription t, int format, String pathName) {
        String templatePrefPath = pathName.substring(0, pathName.length() - 3) + "pfsx";
        // convert the preferences to the other format if needed...
        Object orig = SaveAs27Preferences.adjustPreferencesForSavingFormat(t, format);
        Preferences.exportPreferences(t, templatePrefPath);
        // Restore our original preferences
        SaveAs27Preferences.restoreAdjustedPreferences(t, orig);

    }

    @Override
    public String getName() {
        return commandName;
    }


    /**
     * returns the shortened author name to append to file name
     *
     * @param fullAuthorName
     *
     * @return shortened authorname to append to file name
     */
    private String getShortAuthorName(String fullAuthorName) {

        try {

            String[] parts = fullAuthorName.split(" ");
            if (parts.length == 3) {
                return UNDERSCORE + parts[0].charAt(0) + parts[1].charAt(0) + parts[2].charAt(0);
            } else if (parts.length == 2) {
                return UNDERSCORE + parts[0].charAt(0) + parts[1].charAt(0);
            } else if (parts.length == 1) {
                return UNDERSCORE + parts[0].charAt(0);
            } else {
                return UNDERSCORE + fullAuthorName;
            }

        } catch (Exception e) {
            System.out.println("Error while fetching the short name of Author");
        }
        return "";
    }

}
