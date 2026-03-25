package mpi.eudico.client.annotator.gui;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.util.ControlledVocabulary;
import mpi.eudico.util.ExternalCV;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class for dialogs for editing Controlled Vocabularies.
 */
@SuppressWarnings("serial")
public abstract class AbstractEditControlledVocabulariesDialog extends JDialog implements ActionListener,
                                                                                          ItemListener {
    private static final int DEFAULT_MINIMUM_HEIGHT = 500;
    private static final int DEFAULT_MINIMUM_WIDTH = 550;
    protected EditCVPanel cvEditorPanel;
    protected JButton addCVButton;
    protected JButton changeCVButton;
    protected JButton closeDialogButton;
    protected JButton deleteCVButton;
    protected JComboBox cvComboBox;
    protected JLabel currentCVLabel;
    protected JComboBox cvLanguageComboBox;
    protected JLabel currentCVLanguageLabel;
    protected JLabel cvDescLabel;
    protected JLabel cvNameLabel;
    protected JLabel titleLabel;
    protected JPanel cvButtonPanel;
    protected JPanel cvPanel;
    protected JTextArea cvDescArea;
    protected JTextField cvNameTextField;
    protected String cvContainsEntriesMessage = "contains entries.";
    protected String cvInvalidNameMessage = "Invalid name.";
    protected String cvNameExistsMessage = "Name exists already.";
    protected String deleteQuestionMessage = "delete anyway?";
    protected String oldCVDesc;

    // internal caching fields
    protected String oldCVName;
    protected int minimumHeight;
    protected int minimumWidth;

    private final boolean multipleCVs;
    private int editLanguagesNumber;
    private Color defTextFieldBgColor;

    /**
     * Constructor.
     *
     * @param parent the parent window
     * @param modal the modality of the dialog
     * @param multipleCVs if true, the user can edit more than one CV
     */
    public AbstractEditControlledVocabulariesDialog(Frame parent, boolean modal, boolean multipleCVs) {
        this(parent, modal, multipleCVs, new EditCVPanel());
    }

    /**
     * Constructor with a standard EditCVPanel.
     *
     * @param parent the parent window
     * @param modal the modality of the dialog
     * @param multipleCVs if true, the user can edit more than one CV
     * @param cvEditorPanel panel which might already have a controlled vocabulary
     */
    public AbstractEditControlledVocabulariesDialog(Frame parent, boolean modal, boolean multipleCVs,
                                                    EditCVPanel cvEditorPanel) {
        super(parent, modal);
        this.cvEditorPanel = cvEditorPanel;
        this.multipleCVs = multipleCVs;
        minimumHeight = DEFAULT_MINIMUM_HEIGHT;
        minimumWidth = DEFAULT_MINIMUM_WIDTH;
        makeLayout();
    }

    /**
     * The button actions.
     *
     * @param actionEvent the actionEvent
     */
    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        Object source = actionEvent.getSource();

        // check source equality
        if (source == closeDialogButton) {
            closeDialog();
        } else if (source == addCVButton) {
            addCV();
        } else if (source == changeCVButton) {
            changeCV();
        } else if (source == deleteCVButton) {
            deleteCV();
        }
    }

    /**
     * Handles a change in the cv selection. Implements ItemListener.
     *
     * @param ie the item event
     */
    @Override
    public void itemStateChanged(ItemEvent ie) {
        ControlledVocabulary cv = (ControlledVocabulary) cvComboBox.getSelectedItem();

        if (ie.getSource() == cvComboBox) {
            if (ie.getStateChange() == ItemEvent.SELECTED) {
                cvEditorPanel.setControlledVocabulary(cv);
            }

            updateLanguageComboBox();
            updateCVButtons();
        } else if (ie.getSource() == cvLanguageComboBox && (ie.getStateChange() == ItemEvent.SELECTED)) {
            int languageIndex = cvLanguageComboBox.getSelectedIndex();
            if (cv != null) {
                // Add a language?
                if (languageIndex == editLanguagesNumber) {
                    new EditCVLanguagesDialog(this, cv).setVisible(true);
                    updateLanguageComboBox();
                    cvEditorPanel.setControlledVocabulary(cv);
                } else {
                    // Select a language
                    updateCVButtons();
                }
            }

        }
    }

    /**
     * Test main method with a cv list of size 1.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {
        javax.swing.JFrame frame = new javax.swing.JFrame();
        AbstractEditControlledVocabulariesDialog dialog = new AbstractEditControlledVocabulariesDialog(frame, false, true) {
            private List<ControlledVocabulary> cvList;

            @Override
            protected List<ControlledVocabulary> getCVList() {
                if (cvList == null) {
                    cvList = new ArrayList<>();
                    cvList.add(new ControlledVocabulary("name"));
                }
                return cvList;
            }
        };

        dialog.updateComboBox();
        dialog.pack();
        dialog.setVisible(true);
    }

    /**
     * Get Controlled vocabulary list.
     *
     * @return returns the list of controlled vocabularies
     */
    protected abstract List<ControlledVocabulary> getCVList();

    /**
     * Pack, size and set location.
     */
    protected void setPosition() {
        pack();
        setSize(Math.max(getSize().width, DEFAULT_MINIMUM_WIDTH), Math.max(getSize().height, DEFAULT_MINIMUM_HEIGHT));
        setLocationRelativeTo(getParent());
    }

    /**
     * Adds a CV if the name is valid and unique.
     */
    protected void addCV() {
        String name = cvNameTextField.getText();

        name = name.trim();

        if (name.isEmpty()) {
            showWarningDialog(cvInvalidNameMessage);

            return;
        }

        if (cvExists(name)) {
            // cv with that name already exists, warn
            showWarningDialog(cvNameExistsMessage);

            return;
        }

        addCV(name);
    }

    /**
     * Creates a new ControlledVocabulary when there isn't already one with the same name and adds it to the List.
     *
     * @param name name of new CV
     */
    protected void addCV(String name) {
        ControlledVocabulary cv = new ControlledVocabulary(name, "");
        cvComboBox.addItem(cv);
        cvEditorPanel.setControlledVocabulary(cv);
    }

    /**
     * Changes the properties of a CV. Checks whether the name is valid and unique.
     */
    protected void changeCV() {
        ControlledVocabulary cv = (ControlledVocabulary) cvComboBox.getSelectedItem();

        if (cv == null) {
            return;
        }

        String name = cvNameTextField.getText();
        String desc = cvDescArea.getText();

        if (name != null) {
            name = name.trim();

            if (name.isEmpty()) {
                showWarningDialog(cvInvalidNameMessage);
                cvNameTextField.setText(oldCVName);

                return;
            }
        }

        if ((oldCVName != null) && !oldCVName.equals(name)) {
            // check if there is already a cv with the new name
            if (cvExists(name)) {
                // cv with that name already exists, warn
                showWarningDialog(cvNameExistsMessage);

                return;
            }

            changeCV(cv, name, desc);
        } else if ((oldCVDesc == null && desc != null && !desc.isEmpty()) || (oldCVDesc != null && (desc == null
                                                                                                    || desc.isEmpty())) || (
                       oldCVDesc != null
                       && !oldCVDesc.equals(desc))) {
            changeCV(cv, null, desc);
        }
    }

    /**
     * Changes name and description of the specified ControlledVocabulary.
     *
     * @param cv ControlledVocabulary to be changed
     * @param name new name (may be {@code null -> no change of name}!)
     * @param description new description
     */
    protected void changeCV(ControlledVocabulary cv, String name, String description) {
        int languageIndex = cvLanguageComboBox.getSelectedIndex();
        cv.setDescription(languageIndex, description);

        if (name != null) {
            cv.setName(name);
            cvEditorPanel.setControlledVocabulary(cv);
            cvLanguageComboBox.setSelectedIndex(languageIndex);
        }
    }

    /**
     * Closes the dialog.
     */
    protected void closeDialog() {
        for (ControlledVocabulary cv : getCVList()) {
            if (cv.isChanged()) {
                // get all Transcriptions to update their CVE-linked Annotations.
                String lang = Preferences.getString(Preferences.PREF_ML_LANGUAGE, null);
                if (lang != null) {
                    Preferences.updateAllCVLanguages(lang, true);
                }
                break;
            }
        }
        setVisible(false);
        dispose();
    }

    /**
     * Returns whether a controlled vocabulary with a specific name exists.
     *
     * @param name the name to check
     *
     * @return true if ControlledVocabulary with specified name is in the list
     */
    protected boolean cvExists(String name) {
        boolean nameExists = false;

        for (int i = 0; i < cvComboBox.getItemCount(); i++) {
            if (((ControlledVocabulary) cvComboBox.getItemAt(i)).getName().equals(name)) {
                nameExists = true;

                break;
            }
        }

        return nameExists;
    }

    /**
     * Deletes the selected CV. If the CV is not empty, ask the user for confirmation.
     */
    protected void deleteCV() {
        ControlledVocabulary conVoc = (ControlledVocabulary) cvComboBox.getSelectedItem();

        if (!conVoc.isEmpty()) {
            String mes = cvContainsEntriesMessage + " " + deleteQuestionMessage;

            if (!showConfirmDialog(mes)) {
                return;
            }
        }

        deleteCV(conVoc);
    }

    /**
     * Deletes the controlled vocabulary from the list.
     *
     * @param cv the ControlledVocabulary to delete
     */
    protected void deleteCV(ControlledVocabulary cv) {
        cvComboBox.removeItem(cv);

        if (cvComboBox.getItemCount() > 0) {
            cvComboBox.setSelectedIndex(0);
        } else {
            cvEditorPanel.setControlledVocabulary(null);
        }
    }

    /**
     * Makes the layout.
     */
    protected void makeLayout() {
        JPanel closeButtonPanel;
        JPanel titlePanel;

        GridBagConstraints gridBagConstraints;

        cvPanel = new JPanel();
        currentCVLabel = new JLabel();
        cvComboBox = new JComboBox();
        currentCVLanguageLabel = new JLabel();
        cvLanguageComboBox = new JComboBox();
        cvNameLabel = new JLabel();
        cvNameTextField = new JTextField();
        cvDescLabel = new JLabel();
        cvDescArea = new JTextArea();
        cvButtonPanel = new JPanel();
        addCVButton = new JButton();
        changeCVButton = new JButton();
        changeCVButton.setEnabled(false);
        deleteCVButton = new JButton();
        deleteCVButton.setEnabled(false);

        closeButtonPanel = new JPanel();
        closeDialogButton = new JButton();
        titlePanel = new JPanel();
        titleLabel = new JLabel();

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evt) {
                closeDialog();
            }
        });

        getContentPane().setLayout(new GridBagLayout());

        Insets insets = new Insets(2, 6, 2, 6);

        titleLabel.setFont(titleLabel.getFont().deriveFont((float) 16));
        titlePanel.add(titleLabel);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.insets = insets;
        getContentPane().add(titlePanel, gridBagConstraints);

        cvPanel.setLayout(new GridBagLayout());

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        cvPanel.add(currentCVLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = insets;
        cvPanel.add(cvComboBox, gridBagConstraints);


        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        cvPanel.add(currentCVLanguageLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = insets;
        cvPanel.add(cvLanguageComboBox, gridBagConstraints);


        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        cvPanel.add(cvNameLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        gridBagConstraints.weightx = 1.0;
        cvPanel.add(cvNameTextField, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = insets;

        cvPanel.add(cvDescLabel, gridBagConstraints);
        cvDescArea.setLineWrap(true);
        cvDescArea.setWrapStyleWord(true);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = insets;

        cvPanel.add(new JScrollPane(cvDescArea), gridBagConstraints);

        cvButtonPanel.setLayout(new GridLayout(0, 1, 6, 6));

        addCVButton.addActionListener(this);
        cvButtonPanel.add(addCVButton);

        changeCVButton.addActionListener(this);
        cvButtonPanel.add(changeCVButton);

        deleteCVButton.addActionListener(this);
        cvButtonPanel.add(deleteCVButton);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.insets = insets;
        cvPanel.add(cvButtonPanel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = insets;
        gridBagConstraints.weightx = 1.0;

        if (multipleCVs) {
            getContentPane().add(cvPanel, gridBagConstraints);
        }

        //
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = insets;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(cvEditorPanel, gridBagConstraints);

        closeButtonPanel.setLayout(new GridLayout(1, 1, 0, 2));

        closeDialogButton.addActionListener(this);
        closeButtonPanel.add(closeDialogButton);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = insets;
        getContentPane().add(closeButtonPanel, gridBagConstraints);

        InputMap inputMap = ((JComponent) getContentPane()).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = ((JComponent) getContentPane()).getActionMap();

        if ((inputMap != null) && (actionMap != null)) {
            final String esc = "Esc";
            final String enter = "Enter";
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), esc);
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), enter);
            actionMap.put(esc, new EscapeAction());
            actionMap.put(enter, new EnterAction());
        }
        cvComboBox.setRenderer(new CVListRenderer());
        defTextFieldBgColor = cvDescArea.getBackground();
        //defLabelFgColor = cvComboBox.getForeground();
        // set font to default tier font
        cvNameTextField.setFont(Constants.DEFAULTFONT);
        cvDescArea.setFont(Constants.DEFAULTFONT);
    }

    /**
     * Shows a confirm (yes/no) dialog with the specified message string.
     *
     * @param message the message to display
     *
     * @return true if the user clicked OK, false otherwise
     */
    protected boolean showConfirmDialog(String message) {
        int confirm = JOptionPane.showConfirmDialog(this, message, "Warning", JOptionPane.YES_NO_OPTION);

        return confirm == JOptionPane.YES_OPTION;
    }

    /**
     * Shows a warning/error dialog with the specified message string.
     *
     * @param message the message to display
     */
    protected void showWarningDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Updates the controlled vocabulary buttons.
     */
    protected void updateCVButtons() {
        ControlledVocabulary cv = (ControlledVocabulary) cvComboBox.getSelectedItem();
        boolean isExternal = cv instanceof ExternalCV;
        int lang = Math.max(0, cvLanguageComboBox.getSelectedIndex());
        changeCVButton.setEnabled(cv != null);
        deleteCVButton.setEnabled(cv != null);
        cvNameTextField.setText((cv != null) ? cv.getName() : "");
        cvDescArea.setText((cv != null) ? cv.getDescription(lang) : "");
        oldCVName = (cv != null) ? cv.getName() : null;
        oldCVDesc = (cv != null) ? cv.getDescription(lang) : null;
        if (isExternal) {
            //cvComboBox.setForeground(Constants.ACTIVEANNOTATIONCOLOR);
            if (Constants.DARK_MODE) {
                cvNameTextField.setBackground(Constants.EVEN_ROW_BG);
                cvDescArea.setBackground(Constants.EVEN_ROW_BG);
            } else {
                cvNameTextField.setBackground(Constants.LIGHT_YELLOW);
                cvDescArea.setBackground(Constants.LIGHT_YELLOW);
            }
        } else {
            //cvComboBox.setForeground(defLabelFgColor);
            cvNameTextField.setBackground(defTextFieldBgColor);
            cvDescArea.setBackground(defTextFieldBgColor);
        }
    }

    /**
     * Extracts the CVs from the transcription and fills the CV combobox.
     */
    protected void updateComboBox() {
        cvComboBox.removeItemListener(this);

        // extract
        List<ControlledVocabulary> v = getCVList();
        cvComboBox.removeAllItems();

        for (ControlledVocabulary cvEntries : v) {
            cvComboBox.addItem(cvEntries);
        }

        if (!v.isEmpty()) {
            cvComboBox.setSelectedIndex(0);
            cvEditorPanel.setControlledVocabulary((ControlledVocabulary) cvComboBox.getItemAt(0));
        }

        updateCVButtons();

        cvComboBox.addItemListener(this);

        updateLanguageComboBox();
    }

    /**
     * Extracts the languages from the CV and fills the cv language combobox.
     */
    protected void updateLanguageComboBox() {
        cvLanguageComboBox.removeItemListener(this);

        ControlledVocabulary cv = (ControlledVocabulary) cvComboBox.getSelectedItem();
        if (cv == null) {
            return;
        }
        int numberOfLanguages = cv.getNumberOfLanguages();

        cvLanguageComboBox.removeAllItems();

        for (int i = 0; i < numberOfLanguages; i++) {
            String id = cv.getLanguageId(i);
            String label = cv.getLanguageLabel(i);
            String item = id + " - " + label;
            cvLanguageComboBox.addItem(item);
        }
        // The BasicControlledVocabulary has some magic where it starts constructed with
        // one default language, but calling addLanguage() for the first time modifies
        // that language instead of really adding a new one.
        cvLanguageComboBox.addItem(ElanLocale.getString("EditCVDialog.Label.EditLanguages"));
        editLanguagesNumber = numberOfLanguages;

        cvLanguageComboBox.setSelectedIndex(0);

        cvLanguageComboBox.addItemListener(this);
    }

    /**
     * Since this dialog is meant to be modal a Locale change while this dialog is open is not supposed to happen. This will
     * set the labels etc. using the current locale  strings.
     */
    protected void updateLabels() {
        closeDialogButton.setText("Close");
        deleteCVButton.setText("Delete");
        changeCVButton.setText("Change");
        addCVButton.setText("Add");
        cvNameLabel.setText("Name");
        cvDescLabel.setText("Description");
        currentCVLabel.setText("Current");
    }

    /**
     * An action to put in the dialog's action map and that is being performed when the enter key has been hit.
     *
     * @author Han Sloetjes
     */
    protected class EnterAction extends AbstractAction {
        /**
         * Creates a new action instance.
         */
        public EnterAction() {
            super();
        }

        /**
         * The action that is performed when the enter key has been hit.
         *
         * @param ae the action event
         */
        @Override
        public void actionPerformed(ActionEvent ae) {
            Component com = AbstractEditControlledVocabulariesDialog.this.getFocusOwner();

            if (com instanceof JButton comp) {
                comp.doClick();
            }
        }
    }

    ////////////
    // action classes for handling escape and enter key.
    ////////////

    /**
     * An action to put in the dialog's action map and that is being performed when the escape key has been hit.
     *
     * @author Han Sloetjes
     */
    protected class EscapeAction extends AbstractAction {
        /**
         * Creates a new action instance.
         */
        public EscapeAction() {
            super();
        }

        /**
         * The action that is performed when the escape key has been hit.
         *
         * @param ae the action event
         */
        @Override
        public void actionPerformed(ActionEvent ae) {
            AbstractEditControlledVocabulariesDialog.this.closeDialog();
        }
    }

    /**
     * A renderer that marks External CV's in the combo box with a different background color.
     */
    protected static class CVListRenderer extends DefaultListCellRenderer {
        /**
         * Creates a new renderer instance.
         */
        public CVListRenderer() {
            super();
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof ExternalCV) {
                if (!isSelected) {
                    if (Constants.DARK_MODE) {
                        c.setBackground(Constants.EVEN_ROW_BG);
                    } else {
                        c.setBackground(Constants.LIGHT_YELLOW);
                    }
                } else {
                    if (Constants.DARK_MODE) {
                        c.setBackground(Constants.SELECTED_ROW_BG);
                    }
                }
            }
            return c;
        }
    }
}
