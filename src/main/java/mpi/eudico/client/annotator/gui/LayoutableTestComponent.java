package mpi.eudico.client.annotator.gui;

import javax.swing.*;
import java.awt.*;


/**
 * A test component for a {@link Layoutable} implementation. (Unused so far.)
 */
@SuppressWarnings("serial")
public class LayoutableTestComponent extends JComponent implements Layoutable {
    //    private boolean bWantsAllAvailableSpace = false;
    //    private boolean bIsOptional = false;
    //    private boolean bIsDetachable = false;
    //    private boolean bIsHorizontallyResizable = false;
    //    private boolean bIsVerticallyResizable = false;
    //    private int imageOffset = 0;
    //    private int minimalWidth = 0;
    //    private int minimalHeight = 0;
    private final int nr;
    private final Color color;

    /**
     * Creates a new LayoutableTestComponent instance
     *
     * @param nr index
     * @param color background color
     */
    LayoutableTestComponent(int nr, Color color) {
        this.nr = nr;
        this.color = color;

        setBackground(color);

        JButton but = new JButton("" + nr);
        but.setBackground(color);
        but.setSize(getMinimalWidth(), getMinimalHeight());

        setLayout(new BorderLayout());
        add(but, BorderLayout.CENTER);
    }

    // uses all free space in horizontal direction
    @Override
    public boolean wantsAllAvailableSpace() {
        return (nr == 7) || (nr == 8);
    }

    // can be shown/hidden. If hidden, dimensions are (0,0), position in layout is kept
    @Override
    public boolean isOptional() {
        return false;
    }

    // can be detached, re-attached from main document window
    @Override
    public boolean isDetachable() {
        return false;
    }

    @Override
    public boolean isWidthChangeable() {
        return false;
    }

    @Override
    public boolean isHeightChangeable() {
        return false;
    }

    @Override
    public int getMinimalWidth() {
        return 50;
    }

    @Override
    public int getMinimalHeight() {
        return 50;
    }

    // position of image wrt Layoutable's origin, to be used for spatial alignment
    @Override
    public int getImageOffset() {
        return 10;
    }
}
