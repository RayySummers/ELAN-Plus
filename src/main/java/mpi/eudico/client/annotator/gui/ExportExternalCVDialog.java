package mpi.eudico.client.annotator.gui;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.tier.TierExportTable;
import mpi.eudico.client.annotator.tier.TierExportTableModel;
import nl.mpi.util.FileExtension;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A dialog to export as external CV ({@code .ecv}) file.
 *
 * @author Aarthy Somasundaram
 * @version June 2013
 */
@SuppressWarnings("serial")
public class ExportExternalCVDialog extends JDialog implements ActionListener,
                                                               TableModelListener {
    private String exportFilePath;
    private List<String> cvExportList;
    private JButton okButton;
    private JButton cancelButton;

    private JTable exportTable;
    private TierExportTableModel model;

    /**
     * column id for the include in export checkbox column, invisible
     */
    private final String EXPORT_COLUMN = "export";

    /**
     * column id for the cv name column, invisible
     */
    private final String CV_NAME_COLUMN = "cv";


    /**
     * Constructor.
     *
     * @param owner the parent dialog
     * @param cvList a list of CV names to choose from
     */
    public ExportExternalCVDialog(Dialog owner, List<String> cvList) {
        super(owner, ElanLocale.getString("ExportExternalCVDialog.Title"), true);
        initComponents(cvList);
        postInit();
    }

    /**
     * Initializes the GUI elements in the dialog and adds a window listener
     */
    private void initComponents(List<String> cvList) {

        cvExportList = new ArrayList<String>();

        model = new TierExportTableModel();
        model.setColumnIdentifiers(new String[] {EXPORT_COLUMN, CV_NAME_COLUMN});
        model.addTableModelListener(this);

        exportTable = new TierExportTable(model);

        for (String cv : cvList) {
            model.addRow(Boolean.FALSE, cv);
        }

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        getContentPane().setLayout(new GridBagLayout());

        Insets insets = new Insets(2, 6, 2, 6);

        JLabel titleLabel = new JLabel();
        titleLabel.setText(ElanLocale.getString("ExportExternalCVDialog.Title"));
        titleLabel.setFont(titleLabel.getFont().deriveFont((float) 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel linkPanel = new JPanel(new GridBagLayout());
        linkPanel.setBorder(new TitledBorder(ElanLocale.getString("ExportExternalCVDialog.Label.SelectCV")));
        JScrollPane scrollpane = new JScrollPane(exportTable);
        scrollpane.setPreferredSize(new Dimension(300, 100));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = insets;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        linkPanel.add(scrollpane, gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.weightx = 1.0;
        getContentPane().add(titleLabel, gbc);

        gbc = new GridBagConstraints();
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        getContentPane().add(linkPanel, gbc);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 6, 2));
        okButton = new JButton();
        okButton.setEnabled(false);
        okButton.setText(ElanLocale.getString("Button.OK"));
        okButton.addActionListener(this);
        cancelButton = new JButton();
        cancelButton.setText(ElanLocale.getString("Button.Cancel"));
        cancelButton.addActionListener(this);
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        gbc = new GridBagConstraints();
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.insets = insets;
        getContentPane().add(buttonPanel, gbc);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                doClose();
            }
        });
    }

    /**
     *
     */
    private void postInit() {
        pack();
        setResizable(true);
        setLocationRelativeTo(getParent());
    }

    /**
     *
     */
    private void doClose() {
        setVisible(false);
        dispose();
    }

    /**
     * Some value changed in the checkbox column
     */
    @Override // TableModelListener
    public void tableChanged(TableModelEvent e) {
        if (e.getType() == TableModelEvent.UPDATE) {
            int includeCol = model.findColumn(EXPORT_COLUMN);
            boolean enabled = false;
            final int rowCount = model.getRowCount();
            for (int index = 0; index < rowCount; index++) {
                if (((Boolean) model.getValueAt(index, includeCol)).booleanValue()) {
                    enabled = true;
                    break;
                }
            }
            okButton.setEnabled(enabled);
        }
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getSource() == okButton) {
            File file = getExportFile();
            if (file != null) {
                exportFilePath = file.getAbsolutePath();
                cvExportList = model.getSelectedTiers();
            } else {
                exportFilePath = null;
                cvExportList.clear();
            }
            doClose();
        } else if (actionEvent.getSource() == cancelButton) {
            exportFilePath = null;
            cvExportList.clear();
            doClose();
        }
    }

    /**
     * Returns the export file path.
     *
     * @return the export path of the external CV
     */
    public String getExportFilePath() {
        return exportFilePath;
    }

    /**
     * Returns a list of controlled  vocabularies to export.
     *
     * @return a list of CV's to be exported
     */
    public List<String> getCVList() {
        return cvExportList;
    }

    /**
     * Prompts the user to select an External CV file ({@code *.ecv}).
     *
     * @return the CV file, or null when no valid file was selected
     */
    private File getExportFile() {
        // setup a file chooser
        FileChooser chooser = new FileChooser(this);
        chooser.createAndShowFileDialog(ElanLocale.getString("ExportExternalCVDialog.Title"),
                                        FileChooser.SAVE_DIALOG,
                                        FileExtension.ECV_EXT,
                                        "ExternalCVDir");
        return chooser.getSelectedFile();
    }
}
