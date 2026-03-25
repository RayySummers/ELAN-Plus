package mpi.eudico.client.annotator.gui;

import mpi.eudico.client.annotator.ElanLocale;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A dialog to import a collection from the annotation server. Input the collection Id ( collection url ) to be imported
 * along with authentication key if authentication is enabled.
 */
public class ImportCollectionFromServerDialog extends ClosableDialog implements ActionListener {

    private static final long serialVersionUID = 1L;

    private JTextField collectionIDTextField;

    private JTextField authenticationKeyTextField;

    private JLabel authenticationLabel;

    private JLabel collectionIDLabel;

    private JButton okButton;

    private JButton cancelButton;

    private String authenticationKey = "";

    private String collectionID;

    private Boolean isAuthenticationEnabled = false;

    private Boolean actionApplied = false;

    /**
     * Constructor
     *
     * @param owner the containing frame
     * @param modal the modal true or false identifier
     * @param isAuthenticationEnabled the authentication enabled or disabled identifier
     */
    public ImportCollectionFromServerDialog(Frame owner, boolean modal, boolean isAuthenticationEnabled) {
        super(owner, modal);
        this.isAuthenticationEnabled = isAuthenticationEnabled;
        initComponents();
    }

    /**
     * Initializes the components
     */
    private void initComponents() {
        setTitle(ElanLocale.getString("Frame.ElanFrame.AnnotationServer.Import"));

        getContentPane().setLayout(new GridBagLayout());
        getContentPane().setPreferredSize(new Dimension(800, 100));

        collectionIDLabel = new JLabel();
        collectionIDLabel.setText(ElanLocale.getString("Frame.ElanFrame.AnnotationServer.CollectionID"));
        authenticationLabel = new JLabel();
        authenticationLabel.setText(ElanLocale.getString("Frame.ElanFrame.AnnotationServer.AuthenticationKey"));

        collectionIDTextField = new JTextField();

        authenticationKeyTextField = new JTextField();

        okButton = new JButton(ElanLocale.getString("Button.OK"));
        cancelButton = new JButton(ElanLocale.getString("Button.Cancel"));

        Insets insets = new Insets(2, 6, 2, 6);

        Container contentPane = getContentPane();
        ((JComponent) contentPane).setBorder(new EmptyBorder(6, 8, 2, 8));

        GridBagConstraints lgbc = new GridBagConstraints();
        lgbc.anchor = GridBagConstraints.WEST;
        lgbc.insets = insets;
        lgbc.gridx = 0;
        contentPane.add(collectionIDLabel, lgbc);

        GridBagConstraints rgbc = new GridBagConstraints();
        rgbc.gridx = 1;
        rgbc.fill = GridBagConstraints.HORIZONTAL;
        rgbc.anchor = GridBagConstraints.WEST;
        rgbc.insets = insets;
        rgbc.weightx = 1.0;
        contentPane.add(collectionIDTextField, rgbc);

        if (isAuthenticationEnabled) {
            lgbc.gridy = 1;
            contentPane.add(authenticationLabel, lgbc);

            rgbc.gridy = 1;
            contentPane.add(authenticationKeyTextField, rgbc);
        }

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 6, 2));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 2.0;
        gbc.fill = GridBagConstraints.CENTER;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = insets;
        contentPane.add(buttonPanel, gbc);

        okButton.addActionListener(this);
        cancelButton.addActionListener(this);

    }

    /**
     * Returns the authentication key
     *
     * @return the authentication key
     */
    public String getAuthenticationKey() {
        return authenticationKey;
    }


    /**
     * Returns the collection id
     *
     * @return the collection id string
     */
    public String getCollectionID() {
        return collectionID;
    }


    /**
     * Returns whether the dialog is applied or cancelled.
     *
     * @return boolean value which indicates if OK button is pressed or not
     */
    public Boolean isActionApplied() {
        return actionApplied;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == cancelButton) {
            actionApplied = false;
            setVisible(false);
            dispose();
        } else if (e.getSource() == okButton) {
            collectionID = collectionIDTextField.getText().trim();
            if (collectionID.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                                              ElanLocale.getString("Frame.ElanFrame.AnnotationServer.EmptyCollectionID"),
                                              ElanLocale.getString("Message.Warning"),
                                              JOptionPane.WARNING_MESSAGE);

                actionApplied = false;
                return;
            }
            if (isAuthenticationEnabled) {
                authenticationKey = authenticationKeyTextField.getText().trim();
                if (authenticationKey.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                                                  ElanLocale.getString("Frame.ElanFrame.AnnotationServer.EmptyBearerKey"),
                                                  ElanLocale.getString("Message.Warning"),
                                                  JOptionPane.WARNING_MESSAGE);
                    actionApplied = false;
                    return;
                }
            }
            setVisible(false);
            dispose();
            actionApplied = true;
        }
    }

}
