package mpi.eudico.client.annotator.interlinear;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Renders a print preview of HTML content for the TimeAlignedInterlinear export.
 *
 * @author Steffen Zimmermann
 * @version 1.0
 */
@SuppressWarnings("serial")
public class PreviewPanelTimeAlignedInterlinear extends JPanel implements ComponentListener,
        AdjustmentListener {
    private BufferedImage bi;
    private JScrollPane scrollPane;
    private int[] offset = { 0, 0 };
    private Dimension visibleDimensions = new Dimension(550, 600);
    // preview of html export
    private JEditorPane htmlPanel;

    /** rendered HTML text */
    private String htmlText;

    /**
     * Constructor.
     */
    public PreviewPanelTimeAlignedInterlinear() {
        initComponents();
    }

    /**
     * Initialises the user interface components.
     */
    private void initComponents() {

        htmlPanel = new JEditorPane();
        htmlPanel.setContentType("text/html");
        htmlPanel.setEditable(false);
        scrollPane = new JScrollPane(htmlPanel);

        scrollPane.setPreferredSize(visibleDimensions);

        scrollPane.getHorizontalScrollBar().addAdjustmentListener(this);
        scrollPane.getVerticalScrollBar().addAdjustmentListener(this);

        scrollPane.getVerticalScrollBar().setUnitIncrement(Interlinear.DEFAULT_FONT_SIZE);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(Interlinear.DEFAULT_FONT_SIZE);

        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        add(scrollPane, gbc);

        createBufferedImage(10, 10);


        addComponentListener(this);
    }

    /**
     * Creates a BufferedImage of the given width and height.
     *
     * @param width the width of the buffer
     * @param height the height of the buffer
     */
    private void createBufferedImage(int width, int height) {
        if ((bi == null) || (bi.getWidth() < width) ||
                (bi.getHeight() < height)) {
            bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            repaint();
        }
    }

    /**
     * Returns the BufferedImage used for the buffered rendering.
     *
     * @return the BufferedImage
     */
    public BufferedImage getBufferedImage() {
        return bi;
    }

    /**
     * Returns the current horizontal and vertical (scroll) offset.
     *
     * @return an array of size 2, the current horizontal and vertical (scroll)
     *         offset
     */
    public int[] getOffset() {
        return offset;
    }

    /**
     * Causes the panel to adjust its size if necessary and the
     * interlinearization  to be repainted.
     */
    public void updateView() {
        if (htmlText != null) {
        	//Rectangle r = htmlPanel.getVisibleRect();
        	int vscroll = scrollPane.getVerticalScrollBar().getValue();
            htmlPanel.setText(htmlText);

            EventQueue.invokeLater(new Runnable() {

				@Override
				public void run() {
					//htmlPanel.scrollRectToVisible(r);
					scrollPane.getVerticalScrollBar().setValue(vscroll);
				}
			});

        } else {
            htmlPanel.setText("htmlText equals null.");
        }
        repaint();
    }

    /**
     * Implements the ComponentListener interface.  Invokes a repaint of the
     * linearization.
     *
     * @param e the component event
     */
    @Override
    public void componentResized(ComponentEvent e) {
        createBufferedImage(this.getWidth(), this.getHeight());
        repaint();
    }

    /**
     * Implements the ComponentListener interface.
     *
     * @param e the component event
     */
    @Override
    public void componentMoved(ComponentEvent e) {
    }

    /**
     * Implements the ComponentListener interface.
     *
     * @param e the component event
     */
    @Override
    public void componentShown(ComponentEvent e) {
    }

    /**
     * Implements the ComponentListener interface.
     *
     * @param e the component event
     */
    @Override
    public void componentHidden(ComponentEvent e) {
    }

    /**
     * Implements the AdjustmentListener interface.
     *
     * @param e the adjustment event
     */
    @Override
    public void adjustmentValueChanged(AdjustmentEvent e) {
        offset[0] = scrollPane.getHorizontalScrollBar().getValue();
        offset[1] = scrollPane.getVerticalScrollBar().getValue();

        repaint();
    }


    // getter and setter
    /**
     * Returns the {@code HTML} text.
     *
     * @return the {@code HTML} text
     */
    public String getHtmlText() {
        return htmlText;
    }

    /**
     * Sets the {@code HTML} text.
     *
     * @param htmlText the {@code HTML} text
     */
    public void setHtmlText(String htmlText) {
        this.htmlText = htmlText;
    }


    /**
     * A class for painting a buffered image to its graphics environment
     *
     * @author HS
     * @version 1.0
     */
    private class ImagePanel extends JPanel {
        /**
         * Overrides the JComponent's paintComponent method.
         *
         * @param g the graphics context
         */
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (bi != null) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.drawImage(bi, offset[0], offset[1], null);

                //g2d.drawImage(bi, 0, 0, null);
            }
        }
    }
}
