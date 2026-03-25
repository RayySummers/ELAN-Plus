package mpi.eudico.client.annotator.gui;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.Selection;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.SystemReporting;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.util.CVEntry;
import mpi.eudico.util.ControlledVocabulary;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Generate and display plots for the density of annotations for a set of tiers.
 *
 * @author Allan van Hulst
 */
@SuppressWarnings("serial")
public class AnnotationDensityPlotDialog extends ClosableDialog implements ActionListener,
                                                                           ItemListener,
                                                                           ClientLogger {
    private final Transcription transcription;
    private final Selection selection;
    private AnnotationDensityPanel densityPanel;

    private JButton buttonClose;
    private JButton buttonExport;
    private JButton buttonUpdate;

    private JTextField textWidth;
    private JTextField textHeight;
    private JTextField textColumn;
    private JTextField textTier;
    private JTextField textMargin;

    private JCheckBox checkSelection;
    private JCheckBox checkFill;
    private JCheckBox checkOutlines;

    private TranscriptionTierSortAndSelectPanel tiersPanel = null;
    private JScrollPane scrollPane = null;

    private boolean limitSelection = false;
    private int imageWidth = 600;
    private int imageHeight = 400;
    private int columnWidth = 120;
    private int tierHeight = 20;
    private int tierMargin = 2;
    private boolean includeOutlines = true;
    private boolean fillOut = true;

    private int first = 0;
    private int last = 0;
    private Map<String, Color> colorMap = null;

    /**
     * Constructor.
     *
     * @param owner the owner frame
     * @param transcription the transcription containing the tiers
     * @param selection the selected time interval
     *
     * @throws HeadlessException if run in a headless environment
     */
    public AnnotationDensityPlotDialog(Frame owner, Transcription transcription, Selection selection) throws
                                                                                                      HeadlessException {
        super(owner, true);

        this.transcription = transcription;
        this.selection = selection;

        /* Set up gridbag layout and add title panel */
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        add(createTitlePanel());

        /* Create a separate panel for both tier-list and options */
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(createTiersPanel());
        panel.add(createOptionsPanel());

        /* Add the panel we just created */
        add(panel);

        /* Create and add panel containing the actual annotation density plot */
        add(createPlotPanel());

        /* Create and add panel for update, export, close buttons */
        add(createButtonPanel());

        /* Initialize data required for plot drawing */
        computeFirstTimePoint();
        computeLastTimePoint();
        colorMap = Preferences.getMapOfColor("TierColors", transcription);

        setTitle(ElanLocale.getString("AnnotationDensityPlotDialog.Annotation.Density.Plot"));
        pack();
        setLocationRelativeTo(getParent());

        /*
         * Set up dialog elements:
         *
         * (1) Load preferences from settings file
         * (2) Read preferences into variables
         */
        loadPreferences();
        readAndUpdate();

        /*
         * Disable the "Limit to current selection" checkbox if either
         * selection is null or the selection window is less than 10 ms.
         *
         */
        if (selection == null || Math.abs(selection.getEndTime() - selection.getBeginTime()) <= 10) {
            checkSelection.setEnabled(false);
        }

        setVisible(true);
    }

    /**
     * Create a simple JPanel to display the ELAN-standard title for this dialog
     *
     * @return the title panel
     */
    private JPanel createTitlePanel() {
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel(ElanLocale.getString("AnnotationDensityPlotDialog.Annotation.Density.Plot"));

        titleLabel.setFont(titleLabel.getFont().deriveFont((float) 16));
        titlePanel.add(titleLabel);

        return titlePanel;
    }

    /**
     * Create a JPanel containing the tier selection JTable. This has been updated to use the
     * TranscriptionTierSortAndSelectPanel which is derived from JPanel.
     *
     * @return the tiers panel
     */
    private JPanel createTiersPanel() {
        tiersPanel = new TranscriptionTierSortAndSelectPanel((TranscriptionImpl) transcription);

        return tiersPanel;
    }

    /**
     * Create a JPanel containing the actual panel that displays the density plot
     *
     * @return the plot panel
     */
    private JPanel createPlotPanel() {
        this.densityPanel = new AnnotationDensityPanel(this);

        JPanel panelPlot = new JPanel();
        panelPlot.setBorder(new TitledBorder(ElanLocale.getString("AnnotationDensityPlotDialog.Annotation.Density.Plot")));

        /* Get rid of the default BorderLayout for this JPanel because it does not work well with scaling */
        panelPlot.setLayout(new BoxLayout(panelPlot, BoxLayout.X_AXIS));

        scrollPane = new JScrollPane(densityPanel);
        scrollPane.setPreferredSize(new Dimension(600, 400));
        panelPlot.add(scrollPane);

        return panelPlot;
    }

    /**
     * Create a JPanel containing the width and height specification textfields.
     *
     * @return the configuration panel
     */
    private JPanel createOptionsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();

        /* Initialize grid bag constraints */
        gc.weightx = 1.0;
        gc.gridx = 0;
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.WEST;

        /* Add checkbox to limit plot to current selection */
        checkSelection = new JCheckBox(ElanLocale.getString("AnnotationDensityPlotDialog.Limit.Selection"));
        panel.add(checkSelection, gc);

        /* Add textfield for specification of the width of the tier names column */
        gc.gridx = 0;
        gc.gridy = gc.gridy + 1;
        panel.add(new JLabel(ElanLocale.getString("AnnotationDensityPlotDialog.Tier.Col.Width")), gc);

        gc.gridy = gc.gridy + 1;
        gc.gridx = 0;
        JPanel panelColumn = new JPanel();
        JPanel fillColumn = new JPanel();
        fillColumn.setPreferredSize(new Dimension(10, 10));
        panelColumn.add(fillColumn);
        textColumn = new JTextField(Integer.toString(columnWidth));
        textColumn.setPreferredSize(new Dimension(50, 20));
        textColumn.setHorizontalAlignment(SwingConstants.RIGHT);
        panelColumn.add(textColumn);
        panel.add(panelColumn, gc);

        /* Add textfield for specification of the total width of the image */
        gc.gridx = 0;
        gc.gridy = gc.gridy + 1;
        panel.add(new JLabel(ElanLocale.getString("AnnotationDensityPlotDialog.Image.Width")), gc);

        gc.gridy = gc.gridy + 1;
        gc.gridx = 0;
        JPanel panelWidth = new JPanel();
        JPanel fillWidth = new JPanel();
        fillWidth.setPreferredSize(new Dimension(10, 10));
        panelWidth.add(fillWidth);
        textWidth = new JTextField(Integer.toString(columnWidth + imageWidth));
        textWidth.setPreferredSize(new Dimension(50, 20));
        textWidth.setHorizontalAlignment(SwingConstants.RIGHT);
        panelWidth.add(textWidth);
        panel.add(panelWidth, gc);

        /* Add textfield for specification of the total height of the image */
        gc.gridx = 0;
        gc.gridy = gc.gridy + 1;
        panel.add(new JLabel(ElanLocale.getString("AnnotationDensityPlotDialog.Image.Height")), gc);

        gc.gridy = gc.gridy + 1;
        gc.gridx = 0;
        JPanel panelHeight = new JPanel();
        JPanel fillHeight = new JPanel();
        fillHeight.setPreferredSize(new Dimension(10, 10));
        panelHeight.add(fillHeight);
        textHeight = new JTextField(Integer.toString(imageHeight));
        textHeight.setPreferredSize(new Dimension(50, 20));
        textHeight.setHorizontalAlignment(SwingConstants.RIGHT);
        panelHeight.add(textHeight);
        panel.add(panelHeight, gc);

        /* Add textfield for specification of the height of individual tiers */
        gc.gridx = 0;
        gc.gridy = gc.gridy + 1;
        panel.add(new JLabel(ElanLocale.getString("AnnotationDensityPlotDialog.Tier.Height")), gc);

        gc.gridy = gc.gridy + 1;
        gc.gridx = 0;
        JPanel panelTier = new JPanel();
        JPanel fillTier = new JPanel();
        fillTier.setPreferredSize(new Dimension(10, 10));
        panelTier.add(fillTier);
        textTier = new JTextField(Integer.toString(tierHeight));
        textTier.setPreferredSize(new Dimension(50, 20));
        textTier.setHorizontalAlignment(SwingConstants.RIGHT);
        panelTier.add(textTier);
        checkFill = new JCheckBox(ElanLocale.getString("AnnotationDensityPlotDialog.Fill.Out"));
        checkFill.addItemListener(this);
        panelTier.add(checkFill);
        panel.add(panelTier, gc);

        /* Add textfield for the specification of the margins */
        gc.gridx = 0;
        gc.gridy = gc.gridy + 1;
        panel.add(new JLabel(ElanLocale.getString("AnnotationDensityPlotDialog.Margin.Height")), gc);

        gc.gridy = gc.gridy + 1;
        gc.gridx = 0;
        JPanel panelMargin = new JPanel();
        JPanel fillMargin = new JPanel();
        fillMargin.setPreferredSize(new Dimension(10, 10));
        panelMargin.add(fillMargin);
        textMargin = new JTextField(Integer.toString(tierMargin));
        textMargin.setPreferredSize(new Dimension(50, 20));
        textMargin.setHorizontalAlignment(SwingConstants.RIGHT);
        panelMargin.add(textMargin);
        panel.add(panelMargin, gc);

        /* Add checkbox to select whether to include outlines */
        gc.gridx = 0;
        gc.gridy = gc.gridy + 1;

        checkOutlines = new JCheckBox(ElanLocale.getString("AnnotationDensityPlotDialog.Include.Outlines"));
        panel.add(checkOutlines, gc);

        /* Add a filler-JPanel for vertical scaling */
        gc.gridx = 0;
        gc.gridy = gc.gridy + 1;
        gc.fill = GridBagConstraints.VERTICAL;
        gc.weighty = 1.0;
        panel.add(new JPanel(), gc);

        panel.setBorder(new TitledBorder(ElanLocale.getString("AnnotationDensityPlotDialog.Options")));
        return panel;
    }

    /**
     * Create panel for Update, Export, and Close buttons
     *
     * @return a buttons panel
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel();

        buttonUpdate = new JButton(ElanLocale.getString("AnnotationDensityPlotDialog.Update"));
        buttonUpdate.addActionListener(this);

        buttonExport = new JButton(ElanLocale.getString("AnnotationDensityPlotDialog.Export"));
        buttonExport.addActionListener(this);

        buttonClose = new JButton(ElanLocale.getString("AnnotationDensityPlotDialog.Close"));
        buttonClose.addActionListener(this);

        panel.add(buttonUpdate);
        panel.add(buttonExport);
        panel.add(buttonClose);

        return panel;
    }

    /**
     * Handle button clicks.
     *
     * @param evt The action event that was just performed.
     */
    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == buttonClose) {
            savePreferences();

            setVisible(false);
        } else if (evt.getSource() == buttonUpdate) {
            readAndUpdate();
        } else if (evt.getSource() == buttonExport) {
            exportImage();
        }
    }

    /**
     * Handle checkbox changes.
     *
     * @param evt The change event that was just performed.
     */
    public void itemStateChanged(ItemEvent evt) {
        if (evt.getSource() == checkFill) {
            textTier.setEnabled(evt.getStateChange() != ItemEvent.SELECTED);
        }
    }

    /**
     * Handle the clicks for the update button
     */
    private void readAndUpdate() {
        int prevColumnWidth = columnWidth;
        int prevWidth = imageWidth;
        int prevHeight = imageHeight;
        int prevTierHeight = tierHeight;
        int prevTierMargin = tierMargin;

        /* Set new tier column width */
        try {
            columnWidth = Integer.parseInt(textColumn.getText());
        } catch (NumberFormatException e) {
            columnWidth = prevColumnWidth;
        }

        /* Set new image width */
        try {
            imageWidth = Integer.parseInt(textWidth.getText());

            imageWidth = imageWidth - columnWidth;
        } catch (NumberFormatException e) {
            imageWidth = prevWidth;
        }

        /* Set new image height */
        try {
            imageHeight = Integer.parseInt(textHeight.getText());
        } catch (NumberFormatException e) {
            imageHeight = prevHeight;
        }

        /* Set new tier height */
        try {
            tierHeight = Integer.parseInt(textTier.getText());
        } catch (NumberFormatException e) {
            tierHeight = prevTierHeight;
        }

        /* Set new margin height */
        try {
            tierMargin = Integer.parseInt(textMargin.getText());
        } catch (NumberFormatException e) {
            tierMargin = prevTierMargin;
        }

        limitSelection = checkSelection.isSelected();
        includeOutlines = checkOutlines.isSelected();
        fillOut = checkFill.isSelected();

        /*
         * Compute first and last time point again
         *
         * Note: we may have to re-compute here since the "limit to selection"
         * checkbox influences the values of first and last.
         */
        computeFirstTimePoint();
        computeLastTimePoint();

        /*
         * Adjust size of density panel based on the image width and
         * height specified by the user. Set to dimensions of scrollpane
         * if not sufficiently wide or high.
         */
        int spWidth = scrollPane.getWidth();
        int spHeight = scrollPane.getHeight();
        int dpWidth = columnWidth + imageWidth > spWidth ? columnWidth + imageWidth : spWidth;
        int dpHeight = imageHeight > spHeight ? imageHeight : spHeight;

        densityPanel.setPreferredSize(new Dimension(dpWidth - 2, dpHeight - 2));
        densityPanel.revalidate();
        densityPanel.repaint();
    }

    /**
     * Export the annotation density plot to a PNG image.
     */
    private void exportImage() {
        readAndUpdate();

        FileChooser chooser = new FileChooser(this);

        String[] extensions = {"png"};

        chooser.createAndShowFileDialog(ElanLocale.getString("AnnotationDensityPlotDialog.Export.Location"),
                                        FileChooser.SAVE_DIALOG,
                                        extensions,
                                        "AnnotationDensityPlotDialog.Current.Directory");

        File file = chooser.getSelectedFile();

        if (file != null) {
            BufferedImage buffimg = new BufferedImage(columnWidth + imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);

            drawPlot(buffimg.getGraphics(), true);

            try {
                ImageIO.write(buffimg, "png", file);
            } catch (IOException e) {
                LOG.log(Level.WARNING, "writing to image file encountered a problem.", e);
            }
        }
    }

    /**
     * Check whether an annotation lies within the current selection.
     *
     * @param ann The annotation to check.
     */
    private boolean withinSelection(Annotation ann) {
        if (selection == null) {
            return false;
        }

        return (selection.getBeginTime() <= ann.getBeginTimeBoundary()
                && ann.getEndTimeBoundary() <= selection.getEndTime());
    }

    /**
     * Compute a possible substring of a tier name, based upon the font metrics derived from the graphics context.
     *
     * @param g The graphics context.
     * @param s The string of the tier name.
     */
    private String cutoffTier(Graphics g, String s) {
        FontMetrics fm = g.getFontMetrics();

        if (fm.stringWidth(s) + 20 <= columnWidth) {
            return s;
        }

        String r = "";
        String d = "...";

        int dw = fm.stringWidth(d);

        for (int i = 0; i < s.length(); i = i + 1) {
            int w = fm.stringWidth(s.substring(0, i + 1));
            if (w < columnWidth - 20 - dw) {
                r = r + s.charAt(i);
            }
        }

        return (r + d);
    }

    /**
     * Draw the plot.
     *
     * @param g The Graphics context, either for a JPanel or a PNG-image.
     * @param image if {@code true} the drawing is for an image
     */
    public void drawPlot(Graphics g, boolean image) {

        if (transcription == null) {
            return;
        }

        if (g instanceof Graphics2D && SystemReporting.antiAliasedText) {
            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }

        /*
         * Compute offset based on whether we are creating an image or
         * drawing in the JPanel.
         */
        int spWidth = scrollPane.getWidth();
        int spHeight = scrollPane.getHeight();
        int offsetX = 0;
        int offsetY = 0;

        if (!image) {
            offsetX = (spWidth <= columnWidth + imageWidth ? 0 : (spWidth - (columnWidth + imageWidth)) / 2);
            offsetY = (spHeight <= imageHeight ? 0 : (spHeight - imageHeight) / 2);
        }

        /* Image-filling white rectangle (simplest fix if we also want to write to PNG file) */
        g.setColor(Color.white);
        g.fillRect(offsetX, offsetY, columnWidth + imageWidth, imageHeight);
        g.setColor(Color.black);

        List<? extends Tier> tiers = transcription.getTiers();
        List<String> selected = tiersPanel.getSelectedTiers();

        /* Display a warning message if the user did not select any tier */
        if (tiers == null || tiers.size() == 0 || selected == null || selected.size() == 0) {
            g.drawString(ElanLocale.getString("AnnotationDensityPlotDialog.No.Tiers"), offsetX + 10, offsetY + 20);
            return;
        }

        int count = selected.size();
        int delta = fillOut ? (imageHeight / count) : tierHeight;

        /* Display a warning message if the tier height cannot fit into the image height */
        if (!fillOut && count * delta > imageHeight) {
            g.drawString(ElanLocale.getString("AnnotationDensityPlotDialog.No.Fit"), offsetX + 10, offsetY + 20);
            return;
        }

        /* Display a warning message if there is not at least 5 px per tier available */
        if (delta < 5) {
            g.drawString(ElanLocale.getString("AnnotationDensityPlotDialog.5px.Tier"), offsetX + 10, offsetY + 20);
            return;
        }

        /*
         * Use font size 12 <= tier height / 4 <= 16 in order to make the font size
         * scale with the tier height and to avoid unreasonably small or large font size.
         */
        int fontSize = Math.min(Math.max(12, delta / 4), 16);

        g.setFont(g.getFont().deriveFont((float) fontSize));

        /*
         * Display the tier names on the left-hand side of the image
         * but only do this under the following conditions:
         *
         * (1) No tier names are written if columnWidth = 0.
         * (2) Tier names are abbreviated if they are too wide.
         */
        if (columnWidth > 0) {
            for (int i = 0; i < count; i = i + 1) {
                g.drawString(cutoffTier(g, selected.get(i)),
                             offsetX + 10,
                             offsetY + (i + 1) * delta - delta / 2 + fontSize / 2);
            }
        }

        /*
         * Iterate through selected tiers and draw annotation-rectangles
         * using the colors retrieved through the color map.
         */
        for (int i = 0; i < selected.size(); i = i + 1) {
            Tier tier = retrieveTier(selected.get(i), tiers);

            if (tier != null) {
                List<? extends Annotation> annotations = tier.getAnnotations();
                ControlledVocabulary cv = null;
                String cvName = tier.getLinguisticType().getControlledVocabularyName();
                if (cvName != null) {
                    cv = transcription.getControlledVocabulary(cvName);
                }

                /*
                 * Check whether a color has been assigned to this tier. If not,
                 * check whether the parent has been assigned a color. If that is
                 * also not the case, default to the color gray.
                 */
                if (colorMap != null && colorMap.containsKey(tier.getName())) {
                    g.setColor(colorMap.get(tier.getName()));
                } else if (colorMap != null && tier.hasParentTier() && colorMap.containsKey(tier.getParentTier()
                                                                                                .getName())) {
                    g.setColor(colorMap.get(tier.getParentTier().getName()));
                } else if (colorMap != null && colorMap.containsKey(tier.getRootTier().getName())) {
                    g.setColor(colorMap.get(tier.getRootTier().getName()));
                } else {
                    g.setColor(Color.GRAY);
                }

                /* cache the tier color */
                Color ct = g.getColor();
                /* Iterate through annotations, draw rectangles */
                for (int j = 0; j < annotations.size(); j = j + 1) {
                    Annotation annot = annotations.get(j);

                    if (!limitSelection || withinSelection(annot)) {
                        int begin = (int) annot.getBeginTimeBoundary();
                        begin = columnWidth + (int) (((double) imageWidth) * ((double) (begin - first)) / ((double) (last - first)));

                        int end = (int) annot.getEndTimeBoundary();
                        end = columnWidth + (int) (((double) imageWidth) * ((double) (end - first)) / ((double) (last - first)));
                        Color ca = getAnnColor(cv, annot);
                        if (ca != null) {
                            g.setColor(ca);
                        }
                        g.fillRect(offsetX + begin, offsetY + i * delta + tierMargin, end - begin, delta - 2 * tierMargin);

                        if (includeOutlines) {
                            if (end - begin >= 3) {
                                g.setColor(Color.DARK_GRAY);
                                g.drawLine(offsetX + begin,
                                           offsetY + i * delta + tierMargin,
                                           offsetX + begin,
                                           offsetY + i * delta + delta - tierMargin - 1);
                                g.drawLine(offsetX + end,
                                           offsetY + i * delta + tierMargin,
                                           offsetX + end,
                                           offsetY + i * delta + delta - tierMargin - 1);
                                g.setColor(ct);
                            }
                        }
                        // reset color
                        g.setColor(ct);
                    }
                }
            }
        }

        /*
         * Draw outlines after density plot rectangles to force overlapping.
         *
         * (1) Draw a line to separate tier-column from annotation density plot
         * (2) Draw lines between tiers
         * (3) Write tier names in rows
         *
         * Only draw these outlines if desired by the user.
         */
        if (includeOutlines) {
            g.setColor(Color.BLACK);

            if (columnWidth > 0) {
                g.drawLine(offsetX + columnWidth,
                           offsetY,
                           offsetX + columnWidth,
                           offsetY + Math.min(imageHeight, delta * count));
            }

            for (int i = 0; i <= count; i = i + 1) {
                g.drawLine(offsetX, offsetY + i * delta, offsetX + columnWidth + imageWidth, offsetY + i * delta);
            }
        }
    }

    /**
     * Compute the begin value for the left-most annotation
     *
     * <p>(i.e. just compute the minimum begin-point of all annotations)
     */
    private void computeFirstTimePoint() {
        List<? extends Tier> tiers = transcription.getTiers();

        first = 1000000000;

        for (int i = 0; i < tiers.size(); i = i + 1) {
            List<? extends Annotation> annotations = tiers.get(i).getAnnotations();

            for (int j = 0; j < annotations.size(); j = j + 1) {
                Annotation annot = annotations.get(j);

                if (!limitSelection || withinSelection(annot)) {
                    first = Math.min(first, (int) annot.getBeginTimeBoundary());
                }
            }
        }
    }

    /**
     * Compute the end value for the right-most annotation
     *
     * <p>(i.e. just compute the maximum end-point of all annotations)
     */
    private void computeLastTimePoint() {
        List<? extends Tier> tiers = transcription.getTiers();

        last = 0;

        for (int i = 0; i < tiers.size(); i = i + 1) {
            List<? extends Annotation> annotations = tiers.get(i).getAnnotations();

            for (int j = 0; j < annotations.size(); j = j + 1) {
                Annotation annot = annotations.get(j);

                if (!limitSelection || withinSelection(annot)) {
                    last = Math.max(last, (int) annot.getEndTimeBoundary());
                }
            }
        }
    }

    /**
     * Helper function to lookup a selected tier in the list
     */
    private Tier retrieveTier(String name, List<? extends Tier> tiers) {
        for (int i = 0; i < tiers.size(); i = i + 1) {
            if (tiers.get(i).getName().equals(name)) {
                return tiers.get(i);
            }
        }

        return null;
    }

    /**
     * Helper function to retrieve the custom color for an annotation if it is linked to a CV entry and if a color has been
     * set for that entry.
     *
     * @param cv the Controlled Vocabulary or {@code null}
     * @param annot the annotation
     *
     * @return the color of the CV entry or {@code null}
     */
    private Color getAnnColor(ControlledVocabulary cv, Annotation annot) {
        if (cv != null) {
            String id = annot.getCVEntryId();
            if (id != null) {
                CVEntry cve = cv.getEntrybyId(id);
                if (cve != null) {
                    return cve.getPrefColor();
                }
            }
        }
        return null;
    }

    /**
     * Load the preferences from the settings file and initialize dialog elements.
     */
    private void loadPreferences() {

        Boolean boolPref = null;
        String strPref = null;

        boolPref = Preferences.getBool("AnnotationDensityPlotDialog.checkSelection", null);
        if (boolPref != null) {
            checkSelection.setSelected(boolPref);
        }

        strPref = Preferences.getString("AnnotationDensityPlotDialog.textWidth", null);
        if (strPref != null) {
            textWidth.setText(strPref);
        }

        strPref = Preferences.getString("AnnotationDensityPlotDialog.textHeight", null);
        if (strPref != null) {
            textHeight.setText(strPref);
        }

        strPref = Preferences.getString("AnnotationDensityPlotDialog.textColumn", null);
        if (strPref != null) {
            textColumn.setText(strPref);
        }

        strPref = Preferences.getString("AnnotationDensityPlotDialog.textTier", null);
        if (strPref != null) {
            textTier.setText(strPref);
        }

        strPref = Preferences.getString("AnnotationDensityPlotDialog.textMargin", null);
        if (strPref != null) {
            textMargin.setText(strPref);
        }

        boolPref = Preferences.getBool("AnnotationDensityPlotDialog.checkOutlines", null);
        if (boolPref != null) {
            checkOutlines.setSelected(boolPref);
        }

        boolPref = Preferences.getBool("AnnotationDensityPlotDialog.checkFill", null);
        if (boolPref != null) {
            checkFill.setSelected(boolPref);
        }
    }

    /**
     * Save the preferences to the settings file.
     */
    private void savePreferences() {

        Preferences.set("AnnotationDensityPlotDialog.checkSelection", checkSelection.isSelected(), null, false, false);

        Preferences.set("AnnotationDensityPlotDialog.textWidth", textWidth.getText(), null, false, false);

        Preferences.set("AnnotationDensityPlotDialog.textHeight", textHeight.getText(), null, false, false);

        Preferences.set("AnnotationDensityPlotDialog.textColumn", textColumn.getText(), null, false, false);

        Preferences.set("AnnotationDensityPlotDialog.textTier", textTier.getText(), null, false, false);

        Preferences.set("AnnotationdensityPlotDialog.textMargin", textMargin.getText(), null, false, false);

        Preferences.set("AnnotationDensityPlotDialog.checkOutlines", checkOutlines.isSelected(), null, false, false);

        Preferences.set("AnnotationDensityPlotDialog.checkFill", checkFill.isSelected(), null, false, false);
    }
}
