package mpi.eudico.client.annotator.gui;

import mpi.eudico.client.annotator.ElanLocale;
import nl.mpi.util.FileExtension;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

/**
 * A dialog to enter a URL of a external CV or to browse to an external CV file.
 *
 * @author Micha Hulsbosch
 * @version jul 2010
 */
@SuppressWarnings("serial")
public class GetExternalCVURLDialog extends JDialog implements ActionListener {
    private String externalCVURLString;
    private JButton okButton;
    private JButton cancelButton;
    private JButton browseButton;
    private JTextField urlTextField;

    /**
     * Creates a new dialog instance.
     *
     * @param owner the parent dialog
     */
    public GetExternalCVURLDialog(Dialog owner) {
        super(owner, ElanLocale.getString("ConnectExternalCVDialog.Title.Value"), true);
        initComponents();
        postInit();
    }

    /**
     * Initializes the GUI element in the dialog and adds a window listener.
     */
    private void initComponents() {
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        getContentPane().setLayout(new GridBagLayout());

        Insets insets = new Insets(2, 6, 2, 6);

        JPanel linkPanel = new JPanel();

        JLabel titleLabel = new JLabel();
        titleLabel.setText(ElanLocale.getString("ConnectExternalCVDialog.Label.Value"));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.weightx = 1.0;
        getContentPane().add(titleLabel, gbc);

        linkPanel.setLayout(new GridBagLayout());

        JLabel urlLabel = new JLabel(ElanLocale.getString("ConnectExternalCVDialog.Entry.Label.Value"));

        browseButton = new JButton(ElanLocale.getString("Button.Browse"));
        browseButton.addActionListener(this);

        urlTextField = new JTextField(30);
        urlTextField.setEditable(true);

        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = insets;
        linkPanel.add(urlLabel, gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        linkPanel.add(urlTextField, gbc);
        gbc.gridx = 3;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.weighty = 0;
        linkPanel.add(browseButton, gbc);

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
        setResizable(false);
        setLocationRelativeTo(getParent());
    }

    /**
     *
     */
    private void doClose() {
        setVisible(false);
        dispose();
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        Object source = actionEvent.getSource();

        if (source == okButton) {
            externalCVURLString = urlTextField.getText();
            doClose();
        } else if (source == cancelButton) {
            externalCVURLString = null;
            doClose();
        } else if (source == browseButton) {
            File file = getExternalCVFile();
            if (file != null) {
                urlTextField.setText(file.toURI().toString());
            }
        }
    }

    /**
     * Returns the URL as a string.
     *
     * @return The URL of the external CV (String)
     */
    public String getExternalCVURLString() {
        return externalCVURLString;
    }

    /**
     * Prompts the user to select an External CV file (*.ecv).
     *
     * @return The CV file, or null when no valid file was selected
     */
    private File getExternalCVFile() {
        // setup a file chooser
        FileChooser chooser = new FileChooser(this);
        chooser.createAndShowFileDialog(ElanLocale.getString("Button.Import"),
                                        FileChooser.OPEN_DIALOG,
                                        FileExtension.ECV_EXT,
                                        "ExternalCVDir");
        /*
        JTextArea textAr = new JTextArea(ElanLocale.getString(
                    "EditCVDialog.Message.Browse"));
        textAr.setBackground(chooser.getBackground());
        textAr.setWrapStyleWord(true);
        textAr.setLineWrap(true);
        textAr.setEditable(false);
        textAr.setPreferredSize(new Dimension(160, 100));
        textAr.setMargin(new Insets(5, 5, 5, 5));

        chooser.setAccessory(textAr);
    */

        return chooser.getSelectedFile();
    }
}
