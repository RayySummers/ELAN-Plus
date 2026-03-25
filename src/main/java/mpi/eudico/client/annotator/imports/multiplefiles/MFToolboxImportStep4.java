package mpi.eudico.client.annotator.imports.multiplefiles;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.shoebox.ToolboxDecoderInfo2;
import nl.mpi.util.FileUtility;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static mpi.eudico.client.annotator.util.ClientLogger.LOG;

/**
 * Step 4: Final step for multiple file import options
 */
public class MFToolboxImportStep4 extends AbstractMFImportStep4 {
    private ToolboxDecoderInfo2 decoderInfo;
    private String typFileLinked;

    /**
     * Constructor
     *
     * @param multiPane the multiple step pane
     */
    public MFToolboxImportStep4(MultiStepPane multiPane) {
        super(multiPane);
    }

    @Override
    public void enterStepForward() {
        decoderInfo = (ToolboxDecoderInfo2) multiPane.getStepProperty("ToolboxDecoderInfo");
        super.enterStepForward();
    }

    @Override
    protected boolean doImport(File sourceFile) {
        final File impFile = sourceFile;

        if ((impFile == null) || !impFile.exists()) {
            //progressInterrupted(null, ElanLocale.getString("MultiFileImport.Report.NoFile"));
            LOG.severe("Flex file not found : " + impFile != null ? impFile.getAbsolutePath() : "path is not available.");
            report(ElanLocale.getString("MultiFileImport.Report.NoFile"));
            return false;
        }

        if (decoderInfo == null) {
            return false;
        }

        decoderInfo.setSourceFilePath(impFile.getAbsolutePath());
        parseFile(impFile);

        if (typFileLinked != null) {
            String typeFileName = FileUtility.fileNameFromPath(decoderInfo.getTypeFile());
            if (typeFileName != null && typeFileName.length() > 0) { // could be a marker file has been specified
                int li = typeFileName.lastIndexOf(".");
                typeFileName = typeFileName.substring(0, li);
                if (!typeFileName.trim().equals(typFileLinked.trim())) {
                    //                LOG.warning("Type file mismatch");
                    report("Type file mismatch");
                    report("Type file used for import : " + typeFileName.trim());
                    report("Required type file : " + typFileLinked.trim());
                    report("The type file mentioned in the toolbox file is different from the type file selected for "
                           + "this import.");
                    report("If an eaf is imported, that might miss some details like tiers,dependency etc..");
                }
            } else if (decoderInfo.getShoeboxMarkers() == null || decoderInfo.getShoeboxMarkers().isEmpty()) {
                report("Neither a .typ file has been specified nor custom markers have been created. The import will "
                       + "probably fail.");
            }
        }

        try {
            transImpl = new TranscriptionImpl(impFile.getAbsolutePath(), decoderInfo);
        } catch (Exception e) {
            LOG.warning(ElanLocale.getString("MultiFileImport.Report.ExceptionOccured : ") + e.getMessage());
            report(ElanLocale.getString("MultiFileImport.Report.ExceptionOccured : ") + e.getMessage());
            return false;
        }

        return true;
    }

    private void parseFile(File toolboxFile) {
        Reader reader;
        BufferedReader bufRead = null;
        typFileLinked = null;

        try {
            if (decoderInfo.isAllUnicode()) {
                reader = new InputStreamReader(new FileInputStream(toolboxFile), StandardCharsets.UTF_8);
                bufRead = new BufferedReader(reader);
            } else {
                reader = new InputStreamReader(new FileInputStream(toolboxFile), StandardCharsets.ISO_8859_1);
                bufRead = new BufferedReader(reader);
            }
        } catch (FileNotFoundException fne) {
            LOG.severe("Toolbox file not found :" + toolboxFile.getAbsolutePath());
            report(ElanLocale.getString("MultiFileImport.Report.NoFile"));
            return;
        }

        String line = null;
        int lineCount = 0;

        try {
            while ((line = bufRead.readLine()) != null) {
                line = line.trim(); // trim the line immediately after reading
                lineCount++;

                if ((lineCount <= 3) && ((line.indexOf("\\_sh v4.0") > -1) || (line.indexOf("\\_sh v3.0") > -1))) {

                    int lastSpaceIndex = line.trim().lastIndexOf(' ');

                    if (lastSpaceIndex > -1) {
                        typFileLinked = line.substring(lastSpaceIndex).trim();
                        LOG.info("Database type in header: " + typFileLinked);
                        break;
                    }
                }

                if ((lineCount > 3)) {
                    LOG.severe("No Toolbox header found, no Toolbox file? :" + toolboxFile.getAbsolutePath());
                    report("No Toolbox header found, no Toolbox file?");
                    break;
                }
            }
        } catch (IOException ioe) {
            LOG.severe("Error reading file: " + ioe.getMessage());
            report("Error reading file: " + ioe.getMessage());
        } finally {
            try {
                if (bufRead != null) {
                    bufRead.close();
                }
            } catch (IOException e) {
            }
        }
    }
}
