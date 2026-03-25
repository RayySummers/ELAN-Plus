package mpi.eudico.client.annotator.gui;

import javax.swing.*;
import java.awt.*;

/**
 * A panel to render a density plot for annotation information.
 *
 * @author Allan van Hulst
 */
@SuppressWarnings("serial")
public class AnnotationDensityPanel extends JPanel {
    private AnnotationDensityPlotDialog parent = null;

    /**
     * Constructor
     *
     * @param parent the parent dialog
     */
    public AnnotationDensityPanel(AnnotationDensityPlotDialog parent) {
        this.parent = parent;

        setBorder(BorderFactory.createLineBorder(Color.black));
        setBackground(Color.GRAY);
    }

    /**
     * Display a (relatively simple) spread of the annotation distribution
     */
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        parent.drawPlot(g, false);
    }
}
