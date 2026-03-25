package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.interlinear.Interlinear;
import mpi.eudico.client.annotator.interlinear.InterlinearPrintable;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterJob;


/**
 * Command for interlinear printing.
 *
 * @author Hennie Brugman
 * @author Han Sloetjes
 */
public class PrintCommand implements Command {
    /**
     * the PrinterJob object
     */
    private final PrinterJob printJob = PrinterJob.getPrinterJob();

    /**
     * the PageFormat object
     */
    private PageFormat pageFormat = printJob.defaultPage();

    private String commandName;

    private static final class PrintCommandHolder {
        private static final PrintCommand INSTANCE = new PrintCommand();
    }

    public static PrintCommand getInstance() {
        return PrintCommandHolder.INSTANCE;
    }

    // check for preferred paper format etc.
    private PrintCommand() {
        double w;
        double h;
        double imgX;
        double imgY;
        double imgH;
        double imgW;
        int orient = pageFormat.getOrientation();

        Double doublePref;
        doublePref = Preferences.getDouble("PageFormat.Height", null);

        if (doublePref != null) {
            h = doublePref.doubleValue();
        } else {
            h = pageFormat.getHeight();
        }

        doublePref = Preferences.getDouble("PageFormat.Width", null);

        if (doublePref != null) {
            w = doublePref.doubleValue();
        } else {
            w = pageFormat.getWidth();
        }

        doublePref = Preferences.getDouble("PageFormat.ImgX", null);

        if (doublePref != null) {
            imgX = doublePref.doubleValue();
        } else {
            imgX = pageFormat.getImageableX();
        }

        doublePref = Preferences.getDouble("PageFormat.ImgY", null);

        if (doublePref != null) {
            imgY = doublePref.doubleValue();
        } else {
            imgY = pageFormat.getImageableY();
        }

        doublePref = Preferences.getDouble("PageFormat.ImgHeight", null);

        if (doublePref != null) {
            imgH = doublePref.doubleValue();
        } else {
            imgH = pageFormat.getImageableHeight();
        }

        doublePref = Preferences.getDouble("PageFormat.ImgWidth", null);

        if (doublePref != null) {
            imgW = doublePref.doubleValue();
        } else {
            imgW = pageFormat.getImageableWidth();
        }

        Integer intPref = Preferences.getInt("PageFormat.Orientation", null);

        if (intPref != null) {
            orient = intPref.intValue();
        }

        Paper p = new Paper();
        p.setSize(w, h);
        p.setImageableArea(imgX, imgY, imgW, imgH);

        PageFormat pf = new PageFormat();
        pf.setOrientation(orient);
        pf.setPaper(p);

        pageFormat = pf;
    }

    /**
     * Shows a Print dialog and starts the printing process.
     *
     * @param receiver a Transcription object
     * @param arguments either null (printing withou preview, or argument[0] is an Interlinear object
     */
    @Override
    public void execute(Object receiver, Object[] arguments) {
        Transcription tr = (Transcription) receiver;
        Interlinear interlinear;

        if (arguments == null) {
            interlinear = new Interlinear((TranscriptionImpl) tr);

            // init the interlinearizer
            interlinear.renderView(new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB));
        } else {
            interlinear = (Interlinear) arguments[0];
        }

        InterlinearPrintable printable = new InterlinearPrintable(interlinear);
        printJob.setPrintable(printable, pageFormat);
        printJob.setPageable(printable);

        if (printJob.printDialog()) {
            try {
                printJob.print();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(ELANCommandFactory.getRootFrame(tr),
                                              ElanLocale.getString("InterlinearizerOptionsDlg.Error.Print")
                                              + " \n"
                                              + "("
                                              + ex.getMessage()
                                              + ")",
                                              ElanLocale.getString("Message.Error"),
                                              JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public PrinterJob getPrintJob() {
        return printJob;
    }

    public PageFormat getPageFormat() {
        return pageFormat;
    }

    public void setPageFormat(PageFormat pageFormat) {
        this.pageFormat = pageFormat;
    }

    public String getName() {
        return commandName;
    }

    public PrintCommand withCommandName(String commandName) {
        this.commandName = commandName;
        return getInstance();
    }
}
