package mpi.eudico.client.annotator.gui;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.server.corpora.clomimpl.shoebox.MarkerRecord;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import nl.mpi.util.FileExtension;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;


/**
 * The Shoebox Marker dialog is a custom dialog for defining markers used in Shoebox files that are imported. Alternative for
 * using a Shoebox .typ file.
 *
 * @author Hennie Brugman
 * @version July 2004
 */
@SuppressWarnings("serial")
public class ShoeboxMarkerDialog extends ClosableDialog implements ActionListener,
                                                                   ItemListener {

    /**
     * No stereotype, for top level tiers
     */
    public final String none = "None";

    private final Frame frame;

    //    private String oldType=new String();
    private final JLabel titleLabel = new JLabel();
    private final JLabel currentMarkerLabel = new JLabel();
    private final JLabel markerLabel = new JLabel();
    private final JLabel parentLabel = new JLabel();
    private final JLabel constraintsLabel = new JLabel();
    private final JLabel charsetLabel = new JLabel();
    private final JLabel participantLabel = new JLabel();
    private final JLabel excludeLabel = new JLabel();
    private final JComboBox<String> currentMarkers = new JComboBox<String>();
    private final JTextField markerTextField = new JTextField(30);
    private final JComboBox<String> parents = new JComboBox<String>();
    private final JComboBox<String> constraints = new JComboBox<String>();
    private final JComboBox<String> charsets = new JComboBox<String>();
    private final JCheckBox participantMarker = new JCheckBox();
    private final JCheckBox excludeCheckBox = new JCheckBox();

    private final JButton changeButton = new JButton();
    private final JButton cancelButton = new JButton();
    private final JButton addButton = new JButton();
    private final JButton deleteButton = new JButton();
    private final JButton loadButton = new JButton();
    private final JButton storeButton = new JButton();

    private final JPanel titlePanel = new JPanel();
    private final JPanel markerPanel = new JPanel();
    private final JPanel buttonPanel1 = new JPanel(new GridLayout(3, 1, 2, 6));
    private final JPanel buttonPanel2 = new JPanel(new GridLayout(1, 1, 0, 2));
    private final JPanel buttonPanel3 = new JPanel(new GridLayout(1, 3, 6, 6));

    private final ArrayList<MarkerRecord> markers = new ArrayList<MarkerRecord>();
    private boolean toolboxMode = false;

    /**
     * Constructor.
     *
     * @param theFrame the parent frame
     * @param modal whether the dialog should be modal or not
     */
    public ShoeboxMarkerDialog(Frame theFrame, boolean modal) {
        this(theFrame, modal, false);

    }

    /**
     * Constructor. In Toolbox mode the marker encoding combo box is disabled (Unicode is assumed).
     *
     * @param theFrame the parent frame
     * @param modal whether the dialog should be modal or not
     * @param toolboxMode a flag for Toolbox mode instead of Shoebox mode
     */
    public ShoeboxMarkerDialog(Frame theFrame, boolean modal, boolean toolboxMode) {
        super(theFrame, modal);
        frame = theFrame;
        this.toolboxMode = toolboxMode;

        loadMarkers(); // is this convenient or annoying?
        createDialog();
        updateForLocale();
        pack();
        setResizable(false);
        setLocationRelativeTo(frame);
    }

    /**
     * Shows shoebox marker dialog
     *
     * @param parent the containing frame
     *
     * @return the dialog value
     */
    public static Object showDialog(Frame parent) {
        ShoeboxMarkerDialog dlg = new ShoeboxMarkerDialog(parent, true);
        Object o = dlg.getValue();

        return o;
    }

    /**
     * In Toolbox mode the encoding per marker option is disabled.
     *
     * @param parent the parent frame
     * @param toolboxMode a flag for Toolbox mode instead of Shoebox mode
     *
     * @return a list of marker records
     */
    public static Object showDialog(Frame parent, boolean toolboxMode) {
        ShoeboxMarkerDialog dlg = new ShoeboxMarkerDialog(parent, true, toolboxMode);
        Object o = dlg.getValue();

        return o;
    }

    /**
     * The return value of the dialog.
     *
     * @return list of marker records
     */
    public List<MarkerRecord> getValue() {
        // return null;
        return markers;
    }

    /**
     * The return value of the dialog.
     *
     * @return list of marker records
     */
    public List<MarkerRecord> getMarkers() {
        // return ShoeboxTypFile.getMarkers();
        return markers;
    }

    /**
     * Load the markers from a preferences file.
     */
    private void loadMarkers() {
        //Object luMarkers = Preferences.get("LastUsedShoeboxMarkers", null);
        String markerFile = Preferences.getString("LastUsedShoeboxMarkerFile", null);
        if (markerFile != null) {
            File f = new File(markerFile);
            loadMarkersFromFile(f);
        }
    }

    /**
     * Initialize UI elements with the attributes from the argument marker
     *
     * @param markerName the name of the Shoebox marker
     */
    private void updateUIForMarker(String markerName) {
        if (markerName != null) {
            markerTextField.setText(markerName);
            Iterator<MarkerRecord> markerIt = getMarkers().iterator();
            MarkerRecord mr = null;

            while (markerIt.hasNext()) {
                mr = markerIt.next();
                if (mr.getMarker().equals(markerName)) {
                    currentMarkers.setSelectedItem(mr.getMarker());
                    fillParentMenu();
                    parents.setSelectedItem(mr.getParentMarker());

                    String stereoType = mr.getStereoType();
                    if (stereoType != null) {
                        constraints.setSelectedItem(mr.getStereoType());
                    } else {
                        constraints.setSelectedItem(none);
                    }
                    if (!toolboxMode) {
                        charsets.setSelectedItem(mr.getCharsetString());
                    }

                    participantMarker.setSelected(mr.getParticipantMarker());

                    excludeCheckBox.setSelected(mr.isExcluded());

                    break;
                }
            }
        }
    }

    private void fillCurrentMarkersMenu() {
        // empty parents menu
        currentMarkers.removeAllItems();

        // add all markers that are not the specified marker or any of it's descendants
        Iterator<MarkerRecord> markerIter = getMarkers().iterator();
        while (markerIter.hasNext()) {
            MarkerRecord mr = markerIter.next();
            currentMarkers.addItem(mr.getMarker());
        }
    }


    private void fillParentMenu() {
        // empty parents menu
        parents.removeAllItems();

        // add all markers that are not the specified marker or any of it's descendants
        Iterator<MarkerRecord> markerIter = getMarkers().iterator();
        while (markerIter.hasNext()) {
            MarkerRecord mr = markerIter.next();
            parents.addItem(mr.getMarker());
        }
        // select the first one (temporarily)
        if (parents.getItemCount() > 0) {
            parents.setSelectedIndex(0);
        }
    }

    @SuppressWarnings("unused")
    private boolean isDescendentOf(MarkerRecord markerRecord, String ofMarkerName) {
        // if markerRecord equals markerName, or
        // if markerRecord has markerName as parent, or
        // if markerRecord is descendent of 1 of the direct children of markerName
        if (markerRecord.getMarker().equals(ofMarkerName)) {
            return true;
        }

        if (markerRecord.getParentMarker() != null && markerRecord.getParentMarker().equals(ofMarkerName)) {
            return true;
        }

        // find children of markerName
        boolean isDescendent = false;
        Iterator<MarkerRecord> markerIter = getMarkers().iterator();
        while (markerIter.hasNext()) {
            MarkerRecord mr = markerIter.next();
            if (mr.getParentMarker() != null && mr.getParentMarker().equals(ofMarkerName)) {
                if (isDescendentOf(markerRecord, mr.getMarker())) {
                    isDescendent = true;
                    break;
                }
            }
        }

        return isDescendent;
    }

    private void createDialog() {

        // HB, 9-7-02, add 'None' to stereoTypes menu
        constraints.addItem(none);

        //get all stereotypes and add them to the choice menu
        String[] publicStereoTypes = Constraint.publicStereoTypes;

        for (int i = 0; i < publicStereoTypes.length; i++) {
            //    if (!publicStereoTypes[i].equals("Time Subdivision")) {
            constraints.addItem(publicStereoTypes[i]);
            //    }
        }

        charsets.addItem(MarkerRecord.ISOLATINSTRING);
        charsets.addItem(MarkerRecord.UNICODESTRING);
        charsets.addItem(MarkerRecord.SILIPASTRING);
        if (toolboxMode) {
            charsets.setSelectedItem(MarkerRecord.UNICODESTRING);
            charsets.setEnabled(false);
        }

        currentMarkers.addItemListener(this);
        currentMarkers.setMaximumRowCount(Constants.COMBOBOX_VISIBLE_ROWS);

        titleLabel.setFont(titleLabel.getFont().deriveFont((float) 16));
        titlePanel.add(titleLabel);

        constraints.addItemListener(this);
        changeButton.addActionListener(this);
        cancelButton.addActionListener(this);
        addButton.addActionListener(this);
        deleteButton.addActionListener(this);
        loadButton.addActionListener(this);
        storeButton.addActionListener(this);

        buttonPanel1.add(addButton);
        buttonPanel1.add(deleteButton);
        buttonPanel1.add(changeButton);

        buttonPanel2.add(loadButton);
        buttonPanel2.add(storeButton);

        buttonPanel3.add(cancelButton);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                close();
            }
        });

        //add Components
        getContentPane().setLayout(new GridBagLayout());
        markerPanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        Insets insets = new Insets(2, 6, 2, 6);

        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTH;
        c.insets = insets;
        getContentPane().add(titlePanel, c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        c.insets = insets;
        markerPanel.add(currentMarkerLabel, c);

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = insets;
        markerPanel.add(currentMarkers, c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.anchor = GridBagConstraints.WEST;
        c.insets = insets;
        markerPanel.add(markerLabel, c);

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = insets;
        c.weightx = 1.0;
        markerPanel.add(markerTextField, c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2;
        c.anchor = GridBagConstraints.WEST;
        c.insets = insets;
        markerPanel.add(parentLabel, c);

        parents.setMaximumRowCount(Constants.COMBOBOX_VISIBLE_ROWS);
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 2;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = insets;
        markerPanel.add(parents, c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 3;
        c.anchor = GridBagConstraints.WEST;
        c.insets = insets;
        markerPanel.add(constraintsLabel, c);

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 3;
        c.anchor = GridBagConstraints.WEST;
        c.insets = insets;
        markerPanel.add(constraints, c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 4;
        c.anchor = GridBagConstraints.WEST;
        c.insets = insets;
        markerPanel.add(charsetLabel, c);

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 4;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = insets;
        markerPanel.add(charsets, c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 5;
        c.anchor = GridBagConstraints.WEST;
        c.insets = insets;
        markerPanel.add(participantLabel, c);

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 5;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = insets;
        markerPanel.add(participantMarker, c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 6;
        c.anchor = GridBagConstraints.WEST;
        c.insets = insets;
        markerPanel.add(excludeLabel, c);

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 6;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = insets;
        markerPanel.add(excludeCheckBox, c);

        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 1;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.gridheight = 3;
        c.insets = insets;
        markerPanel.add(buttonPanel1, c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = insets;
        c.weightx = 1.0;
        getContentPane().add(markerPanel, c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2;
        c.insets = insets;
        getContentPane().add(buttonPanel2, c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 3;
        c.insets = insets;
        getContentPane().add(buttonPanel3, c);

        markerPanel.setBorder(new TitledBorder("Markers"));
        //pack();
        //setResizable(false);
        //setLocationRelativeTo(frame);

        if (getMarkers().size() > 0) {
            fillCurrentMarkersMenu();
            updateUIForMarker(getMarkers().get(0).getMarker());
        }
    }

    /**
     * Update the UI elements according to the current Locale and the current edit mode.
     */
    private void updateForLocale() {
        setTitle(ElanLocale.getString("ShoeboxMarkerDialog.Title"));
        currentMarkerLabel.setText(ElanLocale.getString("ShoeboxMarkerDialog.CurrentMarkers"));
        markerLabel.setText(ElanLocale.getString("ShoeboxMarkerDialog.Label.Type"));
        parentLabel.setText(ElanLocale.getString("ShoeboxMarkerDialog.Label.Parent"));
        constraintsLabel.setText(ElanLocale.getString("ShoeboxMarkerDialog.Label.Stereotype"));
        charsetLabel.setText(ElanLocale.getString("ShoeboxMarkerDialog.Label.Charset"));
        participantLabel.setText(ElanLocale.getString("ShoeboxMarkerDialog.Label.Participant"));
        excludeLabel.setText(ElanLocale.getString("ShoeboxMarkerDialog.Label.Exclude"));
        cancelButton.setText(ElanLocale.getString("Button.Close"));
        addButton.setText(ElanLocale.getString("Button.Add"));
        deleteButton.setText(ElanLocale.getString("Button.Delete"));
        changeButton.setText(ElanLocale.getString("Button.Change"));
        loadButton.setText(ElanLocale.getString("ShoeboxMarkerDialog.Button.Load"));
        storeButton.setText(ElanLocale.getString("ShoeboxMarkerDialog.Button.Store"));
        setTitle(ElanLocale.getString("ShoeboxMarkerDialog.Title"));

        if (currentMarkers.getModel().getSize() > 0) {
            updateUIForMarker(currentMarkers.getItemAt(0));
            currentMarkers.addItemListener(this);
        }

        titleLabel.setText(getTitle());
    }

    private void doAdd(String name) {
        // check existence
        MarkerRecord mr = null;
        Iterator<MarkerRecord> mIter = getMarkers().iterator();

        while (mIter.hasNext()) {
            mr = mIter.next();

            if (mr.getMarker().equals(name)) {
                String errorMessage = ElanLocale.getString("ShoeboxMarkerDialog.Message.Exists");
                markerTextField.requestFocus();
                JOptionPane.showMessageDialog(this,
                                              errorMessage,
                                              ElanLocale.getString("Message.Error"),
                                              JOptionPane.ERROR_MESSAGE);

                return;
            }
        }

        //create new MarkerRecord
        String stereoTypeString = (String) constraints.getSelectedItem();
        String parentMkrString = (String) parents.getSelectedItem();
        String charsetString = (String) charsets.getSelectedItem();
        if (stereoTypeString.equals(none)) {
            stereoTypeString = null;
        }
        // HS July 2006: check consistency: there should only be one marker without a parent
        // and if parent = null, stereotype should also be null
        if (stereoTypeString != null && parentMkrString == null) {
            // warning message
            JOptionPane.showMessageDialog(this,
                                          ElanLocale.getString("ShoeboxMarkerDialog.Message.Inconsistent"),
                                          ElanLocale.getString("Message.Error"),
                                          JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (stereoTypeString == null && parentMkrString != null) {
            // warning message
            JOptionPane.showMessageDialog(this,
                                          ElanLocale.getString("ShoeboxMarkerDialog.Message.Inconsistent2"),
                                          ElanLocale.getString("Message.Error"),
                                          JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean partMarker = participantMarker.isSelected();
        boolean exclude = excludeCheckBox.isSelected();

        // create and add MarkerRecord
        MarkerRecord newMR = new MarkerRecord();
        newMR.setMarker(name);
        newMR.setParentMarker(parentMkrString);
        newMR.setStereoType(stereoTypeString);
        newMR.setCharset(charsetString);
        newMR.setParticipantMarker(partMarker);
        newMR.setExcluded(exclude);

        // switch off other markers, if needed
        if (partMarker) {
            mIter = getMarkers().iterator();
            while (mIter.hasNext()) {
                MarkerRecord rec = mIter.next();
                if (!rec.getMarker().equals(name)) {
                    rec.setParticipantMarker(false);
                }
            }
        }

        getMarkers().add(newMR);
        currentMarkers.addItem(name);
        updateUIForMarker(name);
    }

    private void doChange() {
        String markerToChange = (String) (currentMarkers.getSelectedItem());

        MarkerRecord mr = null;

        Iterator<MarkerRecord> mIter = getMarkers().iterator();
        while (mIter.hasNext()) {
            mr = mIter.next();
            if (mr.getMarker().equals(markerToChange)) {
                break;
            }
        }

        if (mr != null) {
            String newMarker = markerTextField.getText();
            newMarker = newMarker.replace('\n', ' ');
            newMarker = newMarker.trim();

            if (newMarker.startsWith("\\")) {
                newMarker = newMarker.substring(1);
            }

            if (newMarker.isEmpty()) {
                String errorMessage = ElanLocale.getString("ShoeboxMarkerDialog.Message.MarkerName");
                markerTextField.requestFocus();
                JOptionPane.showMessageDialog(this,
                                              errorMessage,
                                              ElanLocale.getString("Message.Error"),
                                              JOptionPane.ERROR_MESSAGE);

                return;
            }

            String newParent = (String) parents.getSelectedItem();
            String newStereoType = (String) constraints.getSelectedItem();
            String newCharset = (String) charsets.getSelectedItem();
            boolean newPartMarker = participantMarker.isSelected();
            boolean newExclude = excludeCheckBox.isSelected();

            // HS July 2006: check consistency: there should only be one marker without a parent
            // and if parent = null, stereotype should also be null
            if (!newStereoType.equals(none) && newParent == null) {
                // warning message
                JOptionPane.showMessageDialog(this,
                                              ElanLocale.getString("ShoeboxMarkerDialog.Message.Inconsistent"),
                                              ElanLocale.getString("Message.Error"),
                                              JOptionPane.ERROR_MESSAGE);
                updateUIForMarker(markerToChange);
                return;
            }
            if (newStereoType.equals(none) && newParent != null) {
                // warning message
                JOptionPane.showMessageDialog(this,
                                              ElanLocale.getString("ShoeboxMarkerDialog.Message.Inconsistent2"),
                                              ElanLocale.getString("Message.Error"),
                                              JOptionPane.ERROR_MESSAGE);
                updateUIForMarker(markerToChange);
                return;
            }

            mr.setMarker(newMarker);
            currentMarkers.removeItem(markerToChange);
            currentMarkers.addItem(newMarker);

            // reset parent markers that refer to mr
            mIter = getMarkers().iterator();
            while (mIter.hasNext()) {
                MarkerRecord rec = mIter.next();
                if (rec.getParentMarker() != null && rec.getParentMarker().equals(markerToChange)) {
                    rec.setParentMarker(newMarker);
                }
            }

            mr.setParentMarker(newParent);
            mr.setStereoType(newStereoType);
            mr.setCharset(newCharset);
            mr.setParticipantMarker(newPartMarker);
            mr.setExcluded(newExclude);

            // switch off other markers, if needed
            if (newPartMarker) {
                mIter = getMarkers().iterator();
                while (mIter.hasNext()) {
                    MarkerRecord rec = mIter.next();
                    if (!rec.getMarker().equals(newMarker)) {
                        rec.setParticipantMarker(false);
                    }
                }
            }

            updateUIForMarker(newMarker);
        }
    }

    private void doDelete() {
        String markerToDelete = (String) (currentMarkers.getSelectedItem());

        MarkerRecord mr = null;

        Iterator<MarkerRecord> mIter = getMarkers().iterator();
        while (mIter.hasNext()) {
            mr = mIter.next();
            if (mr.getMarker().equals(markerToDelete)) {
                break;
            }
        }

        if (mr != null) {
            getMarkers().remove(mr);
            currentMarkers.removeItem(mr.getMarker());

            // reset parent markers that refer to mr
            mIter = getMarkers().iterator();
            while (mIter.hasNext()) {
                MarkerRecord rec = mIter.next();
                if (rec.getParentMarker() != null && rec.getParentMarker().equals(mr.getMarker())) {
                    rec.setParentMarker(null);
                }
            }
        }

        if (getMarkers().size() > 0) {
            updateUIForMarker(getMarkers().get(0).getMarker());
        }
    }

    /**
     * Check for the condition of zero or more than one root markers; this can indicate an inconsistent set of markers. In
     * special cases, such as original eaf files exported from ELAN, such situation might not be problematic, but most of the
     * time it will be. Just warn.
     */
    private void close() {
        if (getMarkers().size() > 0) {
            int numRoots = 0;
            MarkerRecord mr = null;

            Iterator<MarkerRecord> mIter = getMarkers().iterator();
            while (mIter.hasNext()) {
                mr = mIter.next();
                if (mr.getParentMarker() == null) {
                    numRoots++;
                }
            }
            if (numRoots != 1) {
                JOptionPane.showMessageDialog(this,
                                              ElanLocale.getString("ShoeboxMarkerDialog.Message.RootMarkers"),
                                              ElanLocale.getString("Message.Warning"),
                                              JOptionPane.WARNING_MESSAGE);
            }
        }

        dispose();
    }

    /**
     * Loads the markers from file
     */
    public void doLoad() {
        FileChooser chooser = new FileChooser(this);
        chooser.createAndShowFileDialog(ElanLocale.getString("ShoeboxMarkerDialog.Title.Select"),
                                        FileChooser.OPEN_DIALOG,
                                        ElanLocale.getString("ImportDialog.Approve"),
                                        null,
                                        FileExtension.SHOEBOX_MKR_EXT,
                                        false,
                                        "LastUsedShoeboxMarkerDir",
                                        FileChooser.FILES_ONLY,
                                        null);
        File f = chooser.getSelectedFile();
        if (f != null) {
            loadMarkersFromFile(f);
            Preferences.set("LastUsedShoeboxMarkerFile", f.getAbsolutePath(), null);
        }
    }

    private void loadMarkersFromFile(File f) {
        if (f != null) {
            String line = null;

            getMarkers().clear();
            currentMarkers.removeAllItems();

            FileReader filereader = null;
            BufferedReader br = null;

            try {
                filereader = new FileReader(f, UTF_8);
                br = new BufferedReader(filereader);

                MarkerRecord newRecord = null;

                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    String label = getLabelPart(line);
                    if (label == null) {
                        continue;
                    }
                    String value = getValuePart(line);

                    if (label.equals("marker")) {
                        newRecord = new MarkerRecord();
                        if (!value.equals("null")) {
                            newRecord.setMarker(value);
                        }
                    } else if (label.equals("parent")) {
                        if (!value.equals("null")) {
                            newRecord.setParentMarker(value);
                        }
                    } else if (label.equals("stereotype")) {
                        if (!value.equals("null")) {
                            newRecord.setStereoType(value);
                        }
                    } else if (label.equals("charset")) {
                        if (!value.equals("null")) {
                            newRecord.setCharset(value);
                        }
                    } else if (label.equals("exclude")) {
                        if (!value.equals("null")) {
                            newRecord.setExcluded(value.equals("true"));
                        }
                    } else if (label.equals("participant")) {
                        if (!value.equals("null")) {
                            newRecord.setParticipantMarker(value.equals("true"));
                        }
                        getMarkers().add(newRecord);
                        currentMarkers.addItem(newRecord.getMarker());
                    }
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                try {
                    if (br != null) {
                        br.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    if (filereader != null) {
                        filereader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (getMarkers().size() > 0) {
                updateUIForMarker(getMarkers().get(0).getMarker());
            }
        }
    }

    private String getLabelPart(String theLine) {
        String label = null;

        int index = theLine.indexOf(':');

        if (index > 0) {
            label = theLine.substring(0, index);
        }

        return label;
    }

    private String getValuePart(String theLine) {
        String value = null;

        int index = theLine.indexOf(':');

        if (index < (theLine.length() - 2)) {
            value = theLine.substring(index + 1).trim();
        }

        return value;
    }

    /**
     * Store the marker item in the file
     */
    public void doStore() {
        FileChooser chooser = new FileChooser(this);
        chooser.createAndShowFileDialog(ElanLocale.getString("ShoeboxMarkerDialog.Title.Select"),
                                        FileChooser.SAVE_DIALOG,
                                        ElanLocale.getString("ImportDialog.Approve"),
                                        null,
                                        FileExtension.SHOEBOX_MKR_EXT,
                                        true,
                                        "LastUsedShoeboxMarkerDir",
                                        FileChooser.FILES_ONLY,
                                        null);
        File newSaveFile = chooser.getSelectedFile();

        if (newSaveFile != null) {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newSaveFile),
                                                                                   UTF_8))) {
                for (MarkerRecord markerRecord : getMarkers()) {
                    writer.write(markerRecord.toString());
                }
                Preferences.set("LastUsedShoeboxMarkerFile", newSaveFile.getAbsolutePath(), null);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    //listeners
    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == addButton) {
            String markerName = markerTextField.getText();
            markerName = markerName.replace('\n', ' ');
            markerName = markerName.trim();

            if (markerName.startsWith("\\")) {
                markerName = markerName.substring(1);
            }

            if (markerName.isEmpty()) {
                String errorMessage = ElanLocale.getString("ShoeboxMarkerDialog.Message.MarkerName");
                markerTextField.requestFocus();
                JOptionPane.showMessageDialog(this,
                                              errorMessage,
                                              ElanLocale.getString("Message.Error"),
                                              JOptionPane.ERROR_MESSAGE);

                return;
            }
            doAdd(markerName);
        } else if (event.getSource() == deleteButton) {
            doDelete();
        } else if (event.getSource() == changeButton) {
            doChange();
        } else if (event.getSource() == cancelButton) {
            close();
        } else if (event.getSource() == loadButton) {
            doLoad();
        } else if (event.getSource() == storeButton) {
            doStore();
        }
    }

    /**
     * Item event handling.
     *
     * @param e the item event
     */
    @Override
    public void itemStateChanged(ItemEvent e) {
        if ((e.getSource() == currentMarkers) && (e.getStateChange() == ItemEvent.SELECTED)) {
            String name = (String) currentMarkers.getSelectedItem();

            if (name != null) {
                updateUIForMarker(name);
            }
        }
    }
}
