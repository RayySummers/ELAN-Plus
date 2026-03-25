package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clom.Transcription;
import nl.mpi.util.FileExtension;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.logging.Level;


/**
 * A Command for the export of an image of the Elan window.
 *
 * @author Han Sloetjes
 */
public class ExportImageFromWindowCommand implements Command,
                                                     ClientLogger {
    /**
     * the Logger for this class
     */
    private final String commandName;

    /**
     * Creates a new Command for the export of an image of the Elan window.
     *
     * @param theName the name of the command
     */
    public ExportImageFromWindowCommand(String theName) {
        commandName = theName;
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver null
     * @param arguments the arguments:  <ul><li>arg[0] = the Transcription object (TranscriptionImpl)</li> </ul>
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {
        Transcription transcription = (Transcription) arguments[0];
        final JFrame frame = ELANCommandFactory.getRootFrame(transcription);

        FileChooser chooser = new FileChooser(frame);
        chooser.createAndShowFileDialog(null, FileChooser.SAVE_DIALOG, FileExtension.IMAGE_EXT, "MediaDir");

        File saveFile = chooser.getSelectedFile();
        String imageIOType = "jpg";
        if (saveFile != null) {
            String fileName = saveFile.getAbsolutePath();
            String lowerFileName = fileName.toLowerCase(Locale.getDefault());

            if (lowerFileName.endsWith("png")) {
                imageIOType = "png";
            } else if (lowerFileName.endsWith("bmp")) {
                imageIOType = "bmp";
            } else if (!lowerFileName.endsWith("jpg") && !lowerFileName.endsWith("jpeg")) {
                fileName += ".jpg";
            }

            final File newSaveFile = new File(fileName);

            // the actual saving is done on a seperate thread
            // this allows the UI to repaint after the disposal of the dialog
            new CaptureThread(frame, imageIOType, newSaveFile).start();
        }
    }

    @Override
    public String getName() {
        return commandName;
    }

    /**
     * A Thread that uses the java.awt.Robot class to capture a part from the screen and save it as a .jpg or .png file.
     *
     * @author Han Sloetjes
     */
    class CaptureThread extends Thread {
        private final JFrame frame;
        private final String imageType;
        private final File saveFile;

        /**
         * Creates a new CaptureThread instance
         *
         * @param frame the Elan Frame to capture
         * @param imageType the image type as a String, either "jpg" or "png"
         * @param saveFile the File to save the image to
         */
        CaptureThread(JFrame frame, String imageType, File saveFile) {
            this.frame = frame;
            this.imageType = imageType;
            this.saveFile = saveFile;
        }

        /**
         * Creates a Robot, captures the part of the screen occupied by the frame and writes the resulting image to file.
         */
        @Override
        public void run() {
            try {
                // wait a little
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ie) {
                    LOG.log(Level.WARNING, "Thread seems to be interrupted.", ie);
                }

                Robot robot = new Robot();
                BufferedImage image = robot.createScreenCapture(frame.getBounds());

                //BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                //Graphics2D g2d = image.createGraphics();
                //frame.paintAll(g2d); // does not receive the video image
                ImageIO.write(image, imageType, saveFile);
            } catch (AWTException ae) {
                LOG.warning("Could not capture an image of the screen: " + ae.getMessage());
            } catch (IOException ioe) {
                LOG.warning("Could not save the screen capture image: " + ioe.getMessage());
            }
        }
    }
}
