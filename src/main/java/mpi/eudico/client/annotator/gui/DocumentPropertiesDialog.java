package mpi.eudico.client.annotator.gui;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.LicenseRecord;
import nl.mpi.util.FileExtension;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * A dialog to change document properties: author and license information
 *
 * @author Allan van Hulst
 */
@SuppressWarnings("serial")
public class DocumentPropertiesDialog extends ClosableDialog implements ActionListener,
                                                                        ItemListener {
    private final String[] defLicense = {"Attribution (CC BY)",
                                         "Attribution ShareAlike (CC BY-SA)",
                                         "Attribution-NoDerivs (CC BY-ND)",
                                         "Attribution-NonCommercial (CC BY-NC)",
                                         "Attribution-NonCommercial-ShareAlike (CC BY-NC-SA)",
                                         "Attribution-NonCommercial-NoDerivs (CC BY-NC-ND)",
                                         "GNU General Public License",
                                         "GNU Lesser General Public License"};

    private final String[] urlLicense = {"https://creativecommons.org/licenses/by-sa/4.0/legalcode",
                                         "https://creativecommons.org/licenses/by-nd/4.0/legalcode",
                                         "https://creativecommons.org/licenses/by-nc/4.0/legalcode",
                                         "https://creativecommons.org/licenses/by-nc-sa/4.0/legalcode",
                                         "https://creativecommons.org/licenses/by-nc-nd/4.0/legalcode",
                                         "https://creativecommons.org/licenses/by/4.0/legalcode",
                                         "https://www.gnu.org/licenses/gpl-3.0.en.html",
                                         "https://www.gnu.org/licenses/lgpl-3.0.html"};

    private final Transcription transcription;

    /*
     * Normally, the license/URL is automatically adapted when the user selects a different
     * item in the JComboBox for licenses. However, this interferes with/does not work well
     * with other JComboBox therefore a boolean variable is set to true if changes should be
     * displayed.
     */
    private boolean handleChanges;

    private JTextField textAuthor;
    private JTextField textURL;
    private JTextArea textLicense;
    private JComboBox<String> comboLicense;

    private JButton buttonOK;
    private JButton buttonCancel;
    private JButton buttonImport;
    private JButton buttonNew;
    private JButton buttonRemove;
    private JButton buttonDefault;

    private String tfAuthor;
    private final List<LicenseRecord> copiedRecords;

    /**
     * Constructor.
     *
     * @param owner the owner frame
     * @param transcription the transcription
     *
     * @throws HeadlessException if created in a headless environment
     */
    public DocumentPropertiesDialog(Frame owner, Transcription transcription) throws
                                                                              HeadlessException {
        super(owner, true);

        this.transcription = transcription;
        this.handleChanges = false;

        tfAuthor = transcription.getAuthor();
        copiedRecords = new ArrayList<LicenseRecord>(transcription.getLicenses().size());
        for (LicenseRecord lr : transcription.getLicenses()) {
            LicenseRecord cr = new LicenseRecord();
            cr.setUrl(lr.getUrl());
            cr.setText(lr.getText());
            copiedRecords.add(cr);
        }

        Container pane = getContentPane();
        pane.setLayout(new BorderLayout());

        pane.add(createAuthorPanel(), BorderLayout.NORTH);
        pane.add(createLicensePanel(), BorderLayout.CENTER);
        pane.add(createButtonsPanel(), BorderLayout.SOUTH);

        initElements();

        pack();
        setLocationRelativeTo(getParent());
        setTitle(ElanLocale.getString("DocumentPropertiesDialog.Document.Properties"));
        setVisible(true);

        handleChanges = true;
    }

    /**
     * createAuthorPanel - label and textfield to modify author information
     *
     * @return A JPanel
     */
    private JPanel createAuthorPanel() {
        JPanel panelAuthor = new JPanel(new GridBagLayout());

        panelAuthor.setBorder(new TitledBorder(ElanLocale.getString("DocumentPropertiesDialog.Created.By")));
        textAuthor = new JTextField("", 20);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 4, 2, 4);
        panelAuthor.add(new JLabel(ElanLocale.getString("DocumentPropertiesDialog.Author")), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelAuthor.add(textAuthor, gbc);

        return panelAuthor;
    }

    /**
     * createLicensePanel - at dialog center: URL and license content
     *
     * @return A JPanel
     */
    private JPanel createLicensePanel() {
        JPanel panelLicense = new JPanel(new BorderLayout());
        JPanel panelNorth = new JPanel(new GridBagLayout());
        JPanel panelSouth = new JPanel();

        panelLicense.setBorder(new TitledBorder(ElanLocale.getString("DocumentPropertiesDialog.License.Information")));

        comboLicense = new JComboBox<String>();
        comboLicense.addItemListener(this);
        textURL = new JTextField("", 30);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 4, 2, 4);
        panelNorth.add(comboLicense, gbc);
        gbc.gridx = 1;
        panelNorth.add(new JLabel("URL:"), gbc);
        gbc.gridx = 2;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelNorth.add(textURL, gbc);

        buttonNew = new JButton(ElanLocale.getString("Button.New"));
        buttonNew.addActionListener(this);

        buttonRemove = new JButton(ElanLocale.getString("Button.Remove"));
        buttonRemove.addActionListener(this);

        buttonImport = new JButton(ElanLocale.getString("Button.Import"));
        buttonImport.addActionListener(this);

        buttonDefault = new JButton(ElanLocale.getString("Button.Default"));
        buttonDefault.addActionListener(this);

        panelSouth.add(buttonNew);
        panelSouth.add(buttonRemove);
        panelSouth.add(buttonImport);
        panelSouth.add(buttonDefault);

        textLicense = new JTextArea(15, 35);

        panelLicense.add(panelNorth, BorderLayout.NORTH);
        panelLicense.add(new JScrollPane(textLicense), BorderLayout.CENTER);
        panelLicense.add(panelSouth, BorderLayout.SOUTH);

        return panelLicense;
    }

    /**
     * createButtonsPanel - simple container for OK and Cancel buttons at the bottom of the dialog window
     *
     * @return A JPanel
     */
    private JPanel createButtonsPanel() {
        JPanel panelButtons = new JPanel();

        buttonOK = new JButton(ElanLocale.getString("Button.OK"));
        buttonOK.addActionListener(this);

        buttonCancel = new JButton(ElanLocale.getString("Button.Cancel"));
        buttonCancel.addActionListener(this);

        panelButtons.add(buttonOK);
        panelButtons.add(buttonCancel);

        return panelButtons;
    }

    /**
     * Handle button clicks
     *
     * @param evt the event
     */
    @Override
    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == buttonCancel) {
            setVisible(false);
        } else if (evt.getSource() == buttonOK) {
            saveAndExit();
        } else if (evt.getSource() == buttonImport) {
            importLicense();
        } else if (evt.getSource() == buttonDefault) {
            defaultLicense();
        } else if (evt.getSource() == buttonNew) {
            newLicense();
        } else if (evt.getSource() == buttonRemove) {
            removeLicense();
        }
    }

    /**
     * initElements - Set UI elements to initial values
     */
    private void initElements() {
        handleChanges = false;
        textAuthor.setText(tfAuthor);
        comboLicense.removeAllItems();

        if (copiedRecords.size() == 0) {
            comboLicense.insertItemAt(ElanLocale.getString("DocumentPropertiesDialog.No.License"), 0);
            comboLicense.setSelectedIndex(0);
            textLicense.setText("");
        } else {
            int size = copiedRecords.size();

            for (int i = 0; i < size; i = i + 1) {
                comboLicense.insertItemAt(Integer.toString(i + 1), i);
            }
            comboLicense.setSelectedIndex(size - 1);

            LicenseRecord lr = copiedRecords.get(size - 1);
            textURL.setText(lr.getUrl());
            textLicense.setText(lr.getText());
        }
        handleChanges = true;
    }

    /**
     * saveCurrentLicense - Stores the license selected at present in the combobox
     */
    private void saveCurrentLicense() {
        if (copiedRecords.size() > 0) {
            int index = comboLicense.getSelectedIndex();

            copiedRecords.get(index).setUrl(textURL.getText());
            copiedRecords.get(index).setText(textLicense.getText());
        }
    }

    /**
     * saveAndExit - Save data to the current transcription, make dialog invisible
     */
    private void saveAndExit() {
        tfAuthor = textAuthor.getText();
        saveCurrentLicense();

        // check if there are differences and create a command if so
        if (isChanged()) {
            Command com = ELANCommandFactory.createCommand(transcription, ELANCommandFactory.SET_DOCUMENT_PROPERTIES);
            com.execute(transcription, new Object[] {tfAuthor, copiedRecords});
        }

        setVisible(false);
    }

    /**
     * importLicense - Imports a license from a text file
     */
    private void importLicense() {
        FileChooser fc = new FileChooser(this);
        fc.createAndShowFileDialog("", FileChooser.OPEN_DIALOG, FileExtension.TEXT_EXT, "LastUsedImportDir");
        File file = fc.getSelectedFile();
        StringBuffer license = new StringBuffer();

        if (file != null) {

            if (file.canRead()) {
                BufferedReader reader = null;
                try {
                    reader = new BufferedReader(new FileReader(file, UTF_8));

                    String line = null;

                    while ((line = reader.readLine()) != null) {
                        license.append(line).append("\r\n");
                    }

                    textLicense.setText(license.toString());
                } catch (IOException exception) {
                    JOptionPane.showMessageDialog(ELANCommandFactory.getRootFrame(transcription),
                                                  ElanLocale.getString("DocumentPropertiesDialog.Read.Error"),
                                                  ElanLocale.getString("DocumentPropertiesDialog.Error"),
                                                  JOptionPane.ERROR_MESSAGE);
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (Throwable t) {
                        }
                    }
                }
            }
        }
    }

    /**
     * defaultLicense - Choose one of the available default licensing options.
     */
    private void defaultLicense() {
        String select = (String) JOptionPane.showInputDialog(ELANCommandFactory.getRootFrame(transcription),
                                                             ElanLocale.getString("DocumentPropertiesDialog.Select.Default"),
                                                             ElanLocale.getString("DocumentPropertiesDialog.Default"
                                                                                  + ".Licenses"),
                                                             JOptionPane.DEFAULT_OPTION,
                                                             null,
                                                             defLicense,
                                                             defLicense[0]);

        for (int i = 0; i < urlLicense.length; i = i + 1) {
            if (Objects.equals(defLicense[i], select)) {
                textURL.setText(urlLicense[i]);
                textLicense.setText("License: " + select + " (text available at URL).");
                break;
            }
        }
    }

    /**
     * newLicense - Inserts a new license
     */
    private void newLicense() {
        handleChanges = false;
        saveCurrentLicense();
        int size = copiedRecords.size();

        if (size == 0) {
            comboLicense.removeAllItems();
        }

        LicenseRecord lr = new LicenseRecord();
        lr.setText("");
        lr.setUrl("");
        copiedRecords.add(lr);

        comboLicense.insertItemAt(Integer.toString(size + 1), size);
        comboLicense.setSelectedIndex(size);

        textURL.setText("");
        textLicense.setText("");
        handleChanges = true;
    }

    /**
     * removeCurrentLicense - Removes the presently selected license from the combobox
     */
    private void removeLicense() {
        if (copiedRecords.size() > 0) {
            handleChanges = false;
            int index = comboLicense.getSelectedIndex();
            copiedRecords.remove(index);

            initElements();
            handleChanges = true;
        }
    }

    /**
     * @return true if author or licenses changed, including the order of the licenses, false otherwise
     */
    private boolean isChanged() {
        if (tfAuthor != null && !tfAuthor.equals(transcription.getAuthor())) {
            return true;
        }

        if (copiedRecords.size() != transcription.getLicenses().size()) {
            return true;
        }

        for (int i = 0; i < copiedRecords.size(); i++) {
            LicenseRecord lr1 = copiedRecords.get(i);
            LicenseRecord lr2 = transcription.getLicenses().get(i);

            if ((lr1.getUrl() == null && lr2.getUrl() != null) || (lr1.getUrl() != null && !lr1.getUrl()
                                                                                               .equals(lr2.getUrl()))) {
                return true;
            }

            if (!lr1.getText().equals(lr2.getText())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Called when an item in the combobox is being selected/deselected
     */
    @Override
    public void itemStateChanged(ItemEvent event) {
        if (handleChanges && event.getStateChange() == ItemEvent.DESELECTED && copiedRecords.size() > 0) {
            int index = Integer.parseInt(event.getItem().toString()) - 1;
            LicenseRecord lr = copiedRecords.get(index);
            lr.setUrl(textURL.getText());
            lr.setText(textLicense.getText());
        }

        if (handleChanges && event.getStateChange() == ItemEvent.SELECTED && copiedRecords.size() > 0) {
            int index = Integer.parseInt(event.getItem().toString()) - 1;
            LicenseRecord lr = copiedRecords.get(index);
            textURL.setText(lr.getUrl());
            textLicense.setText(lr.getText());
        }

    }
}
