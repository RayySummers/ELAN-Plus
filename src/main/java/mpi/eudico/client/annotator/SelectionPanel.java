package mpi.eudico.client.annotator;

import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import mpi.eudico.util.TimeFormatter;


/**
 * A panel containing a few labels to present information about a selection.
 * This concerns begin time, end time and duration.
 * 
 * @version May 2017 changed the font setting for the labels, these now use 
 * a smaller version of the system/jre's default font
 */
@SuppressWarnings("serial")
public class SelectionPanel extends JPanel implements ElanLocaleListener,
    SelectionListener {
    private JLabel selectionLabel;
    private JLabel beginLabel;
    private JLabel endLabel;
    private JLabel lengthLabel;
    private long begin;
    private long end;
    private ViewerManager2 vm;

    /**
     * Creates a new SelectionPanel instance.
     *
     * @param theVM viewer manager
     */
    public SelectionPanel(ViewerManager2 theVM) {
        vm = theVM;
        init();
    }

    private void init() {
        FlowLayout flowLayout = new FlowLayout(FlowLayout.LEFT, 0, 0);
        setLayout(flowLayout);

        // declare first to enable set length
        lengthLabel = new JLabel();
        lengthLabel.setFont(Constants.deriveSmallFont(lengthLabel.getFont()));

        selectionLabel = new JLabel();
        selectionLabel.setFont(lengthLabel.getFont());
        add(selectionLabel);

        beginLabel = new JLabel();
        beginLabel.setFont(lengthLabel.getFont());
        setBegin(0);
        add(beginLabel);

        JLabel separator = new JLabel(" - ");
        separator.setFont(lengthLabel.getFont());
        add(separator);

        endLabel = new JLabel();
        endLabel.setFont(lengthLabel.getFont());
        setEnd(0);
        add(endLabel);

        JLabel spaces = new JLabel("  ");
        spaces.setFont(lengthLabel.getFont());
        add(spaces);

        add(lengthLabel);

        ElanLocale.addElanLocaleListener(vm.getTranscription(), this);
        updateLocale();

        vm.getSelection().addSelectionListener(this);
    }

    /**
     * Sets the language dependent text for the main label.
     *
     * @param str the text (name) for the label
     */
    public void setNameLabel(String str) {
        selectionLabel.setText(str + ": ");
    }

    /**
     * Sets the begin time, updates label and duration.
     *
     * @param begin the begin time in milliseconds
     */
    public void setBegin(long begin) {
        this.begin = begin;
        beginLabel.setText(TimeFormatter.toString(begin));
        setLength();
    }

    /**
     * Sets the end time, updates label and duration.
     *
     * @param end the end time in milliseconds
     */
    public void setEnd(long end) {
        this.end = end;
        endLabel.setText(TimeFormatter.toString(end));
        setLength();
    }

    //      private void setLength(long length)
    private void setLength() {
        lengthLabel.setText("" + (end - begin));
    }

    /**
     * Updates the name label.
     */
    @Override
	public void updateLocale() {
        setNameLabel(ElanLocale.getString(
                "MediaPlayerControlPanel.Selectionpanel.Name"));
    }

    /**
     * Notification of a change in selection. Retrieves the new begin and end
     * time via the viewer manager and updates labels.
     */
    @Override
	public void updateSelection() {
        long begin = vm.getSelection().getBeginTime();
        long end = vm.getSelection().getEndTime();

        // make sure it does not look ugly in the panel if selectionBegin == selectionEnd
        if (begin == end) {
            begin = 0;
            end = 0;
        }

        setBegin(begin);
        setEnd(end);
    }
}
 //end of SelectionPanel
