package mpi.eudico.client.annotator.export;

import mpi.eudico.client.annotator.Selection;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.util.TimeFormatter;

import javax.xml.XMLConstants;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Map;

/**
 * This class extracts annotations out of an eaf-file puts them into the SMIL-format. The output consists of at least two
 * files for Quick Time One with ending .sml that contains layout information for the quick time player and a reference to
 * the media file and subtitle file. One or more ending with .txt which contain(s) the annotations of the corresponding
 * tier(s) as subtitles.
 *
 * @author Aarthy Somasundaram
 * @version Oct 08 2010
 */

@SuppressWarnings("serial")
public class ExportQtSmilDialog extends ExportQtSubtitleDialog {

    private static volatile Transformer transformer2smilQt;

    /**
     * Constructor.
     *
     * @param parent the parent frame
     * @param modal the modal flag
     * @param transcription the transcription
     * @param selection the time selection
     */
    public ExportQtSmilDialog(Frame parent, boolean modal, TranscriptionImpl transcription, Selection selection) {
        super(parent, modal, transcription, selection, true);
    }


    /**
     * Export with reference to a media file
     *
     * @param eafURL the URL of an EAF file
     * @param smilFile the output file
     * @param tierNames an array of tiers to export
     * @param mediaFileName reference to media file
     * @param fontSettingMap a {@code Map<String, Object>}, Objects are Color, or Boolean for the key "transparent"
     *
     * @throws IOException any IO related exception
     * @throws TransformerException any transformer exception
     */
    public static void export2SMILQt(URL eafURL, File smilFile, String[] tierNames, String mediaFileName,
                                     Map<String, Object> fontSettingMap) throws
                                                                         IOException,
                                                                         TransformerException {
        createTransformer();

        //Parameter setting
        String fileName = new File(eafURL.getFile()).getName();

        int index = fileName.lastIndexOf('.');
        String title = (index > 0) ? (fileName = fileName.substring(0, index)) : fileName;

        String comment = "Generated from " + fileName + " on " + new Date(System.currentTimeMillis());

        String txtFileName = smilFile.getName();
        index = txtFileName.lastIndexOf('.');

        if (index > 0) {
            txtFileName = txtFileName.substring(0, index);
        }


        transformer2smilQt.setParameter("comment", comment);
        transformer2smilQt.setParameter("title", title);
        if (fontSettingMap != null) {

            if (fontSettingMap.get("backColor") != null) {
                String rgb = Integer.toHexString(((Color) fontSettingMap.get("backColor")).getRGB());
                rgb = rgb.substring(2);

                transformer2smilQt.setParameter("background_color", "#" + rgb);
            }

            if (fontSettingMap.get("size") != null) {
                transformer2smilQt.setParameter("font_size", fontSettingMap.get("size"));
            }

            if (fontSettingMap.get("transparent") != null) {
                if ((Boolean) fontSettingMap.get("transparent")) {
                    transformer2smilQt.setParameter("transparent_background", "true");
                } else {
                    transformer2smilQt.setParameter("transparent_background", "false");
                }
            }

        }


        if (mediaFileName != null) {
            transformer2smilQt.setParameter("media_url", mediaFileName);
        }

        if (tierNames != null) {

            String tierString = tierNames[0];


            for (int i = 1; i < tierNames.length; i++) {
                tierString += ("," + tierNames[i]);
            }

            transformer2smilQt.setParameter("tier", tierString);
        }

        transformer2smilQt.setParameter("txtFileName", txtFileName + ".txt");

        FileOutputStream stream;
        //transformation
        // HS May 2008: use a FileOutputStream instead of just the file to
        // prevent a FileNotFoundException
        transformer2smilQt.transform(new StreamSource(eafURL.openStream()),
                                     new StreamResult(stream = new FileOutputStream(smilFile)));
        stream.close();

        //clear
        transformer2smilQt.clearParameters();
    }

    /**
     * Restricts export to annotations within a time interval; the media file isn't changed, however a player should play
     * only the indicated interval
     *
     * @param eafFile the EAF file to export
     * @param smilFile the output file for the SMIL
     * @param tierNames the tiers to export
     * @param mediaFileName the media file name or location
     * @param beginTime the interval begin time
     * @param endTime the end time
     * @param recalculateTimeInterval modify the begin and end time based on the media offset
     * @param merged the merge parameter for the output
     * @param fontSettingHashMap a {@code Map<String, Object>}, Objects are Color, or Boolean for the key "transparent"
     *
     * @throws IOException any IO exception
     * @throws TransformerException any transformer exception
     */
    public static void export2SMILQt(File eafFile, File smilFile, String[] tierNames, String mediaFileName, long beginTime,
                                     long endTime, boolean recalculateTimeInterval, boolean merged,
                                     Map<String, Object> fontSettingHashMap) throws
                                                                             IOException,
                                                                             TransformerException {
        try {

            URL eafURL = new URL("file:///" + eafFile.getAbsolutePath());
            export2SMILQt(eafURL,
                          smilFile,
                          tierNames,
                          mediaFileName,
                          beginTime,
                          endTime,
                          recalculateTimeInterval,
                          merged,
                          fontSettingHashMap);
        } catch (MalformedURLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Same as above, only URL instead of File
     *
     * @param eafURL the URL of an EAF file
     * @param smilFile the output file
     * @param tierNames the tiers to export
     * @param mediaFileName the media file
     * @param beginTime the begin time
     * @param endTime the end time
     * @param recalculateTimeInterval whether the media offset should be used to recalculate time value
     * @param merged the merge property
     * @param fontSettingHashMap a {@code Map<String, Object>}, Objects are Color, or Boolean for the key "transparent"
     *
     * @throws IOException any IO exception
     * @throws TransformerException any transformer exception
     */
    public static void export2SMILQt(URL eafURL, File smilFile, String[] tierNames, String mediaFileName, long beginTime,
                                     long endTime, boolean recalculateTimeInterval, boolean merged,
                                     Map<String, Object> fontSettingHashMap) throws
                                                                             IOException,
                                                                             TransformerException {
        createTransformer();

        String begin_time = TimeFormatter.toString(beginTime);
        int index = begin_time.indexOf(':');
        begin_time = begin_time.substring(index + 1, begin_time.indexOf('.'));

        String end_time = TimeFormatter.toString(endTime + 1000L);
        index = end_time.indexOf(':');
        end_time = end_time.substring(index + 1, end_time.indexOf('.'));

        // set parameter merged to tru and add chaanges acc in the xsl
        transformer2smilQt.setParameter("selected_time_interval", "true");
        if (recalculateTimeInterval) {
            transformer2smilQt.setParameter("recalculate_time_interval", "true");
        }
        if (merged) {
            transformer2smilQt.setParameter("merge", "true");
        }
        transformer2smilQt.setParameter("media_start_time", begin_time);
        transformer2smilQt.setParameter("media_stop_time", end_time);
        transformer2smilQt.setParameter("media_dur", TimeFormatter.toString(endTime - beginTime));
        export2SMILQt(eafURL, smilFile, tierNames, mediaFileName, fontSettingHashMap);
    }


    /**
     * Export all the annotations
     *
     * @param eafURL the EAF URL
     * @param smilFile the output file
     * @param tierNames the tiers to export
     * @param mediaURL the media URL to include
     * @param mediaDur the duration of the media
     * @param merged the merged property for the output
     * @param fontSettingHashMap a {@code Map<String, Object>}, Objects are Color, or Boolean for the key "transparent"
     *
     * @throws IOException any IO exception
     * @throws TransformerException any transformer exception
     */
    public static void export2SMILQt(URL eafURL, File smilFile, String[] tierNames, String mediaURL, long mediaDur,
                                     boolean merged, Map<String, Object> fontSettingHashMap) throws
                                                                                             IOException,
                                                                                             TransformerException {
        createTransformer();

        if (merged) {
            transformer2smilQt.setParameter("merge", "true");
        }
        transformer2smilQt.setParameter("media_dur", TimeFormatter.toString(mediaDur));
        export2SMILQt(eafURL, smilFile, tierNames, mediaURL, fontSettingHashMap);
    }

    /**
     * Same as above with File instead of URL
     *
     * @param eafFile the EAF file
     * @param smilFile the output file
     * @param tierNames the tiers to export
     * @param mediaURL the media URL to include
     * @param mediaDur the media duration
     * @param merged the merged property
     * @param fontSettingHashMap a {@code Map<String, Object>}, Objects are Color, or Boolean for the key "transparent"
     *
     * @throws IOException any IO exception
     * @throws TransformerException any transformer exception
     */
    public static void export2SMILQt(File eafFile, File smilFile, String[] tierNames, String mediaURL, long mediaDur,
                                     boolean merged, Map<String, Object> fontSettingHashMap) throws
                                                                                             IOException,
                                                                                             TransformerException {
        try {
            URL eafURL = new URL("file:///" + eafFile.getAbsolutePath());
            export2SMILQt(eafURL, smilFile, tierNames, mediaURL, mediaDur, merged, fontSettingHashMap);
        } catch (MalformedURLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }


    /**
     * Sets the xsl-scripts for the transformer
     *
     * @throws TransformerException any transformer exception
     * @throws IOException any IO exception
     */
    private static void createTransformer() throws
                                            TransformerException,
                                            IOException {
        if (transformer2smilQt == null) {
            synchronized (ExportQtSmilDialog.class) {
                if (transformer2smilQt == null) {
                    String file = "/mpi/eudico/resources/eaf2smilQt.xsl";
                    URL eaf2smilQt = ExportQtSmilDialog.class.getResource(file);

                    TransformerFactory tFactory = TransformerFactory.newInstance();
                    tFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                    tFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
                    tFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

                    transformer2smilQt = tFactory.newTransformer(new StreamSource(eaf2smilQt.openStream()));
                }
            }
        }
    }
}
