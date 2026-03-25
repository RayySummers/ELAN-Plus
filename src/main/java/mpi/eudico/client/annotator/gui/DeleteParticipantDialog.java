package mpi.eudico.client.annotator.gui;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.tier.TierExportTable;
import mpi.eudico.client.annotator.tier.TierExportTableModel;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A dialog to delete the tiers based on the participant attribute. The dialog displays the participants. Tiers which have
 * the selected participant attribute will be deleted.
 */
@SuppressWarnings("serial")
public class DeleteParticipantDialog extends ClosableDialog implements ActionListener {

    private final TranscriptionImpl transcription;
    private final ViewerManager2 vm;
    private JLabel titleLabel;
    private TierExportTableModel model;
    private JTable participantTable;
    private JPanel buttonPanel;

    private JButton closeButton;
    private JButton deleteButton;


    /**
     * Creates a new DeleteParticipantDialog instance
     *
     * @param viewerManager the viewer manahger
     * @param owner the parent frame
     * @param modal whether the dialog is modal or not
     */
    public DeleteParticipantDialog(ViewerManager2 viewerManager, Frame owner, boolean modal) {
        super(owner, modal);
        this.vm = viewerManager;
        this.transcription = (TranscriptionImpl) viewerManager.getTranscription();

        if (isParticipantsExists()) {
            JOptionPane.showMessageDialog(owner,
                                          ElanLocale.getString("DeleteParticipantDialog.Message.EmptyParticipant"),
                                          ElanLocale.getString("Message.Info"),
                                          JOptionPane.INFORMATION_MESSAGE);
        } else {
            initComponents();
            extractParticipants();
            postInit();
            setVisible(true);
        }
    }


    /**
     * Checks if there are any participants defined for tiers
     *
     * @return the boolean value based on the presence of the participant
     */
    private Boolean isParticipantsExists() {

        List<String> participants = new ArrayList<String>();

        if (transcription != null) {
            List<TierImpl> tiers = transcription.getTiers();

            TierImpl tier;

            for (int i = 0; i < tiers.size(); i++) {
                tier = tiers.get(i);
                String participant = tier.getParticipant();
                if (participant != null && participant.trim().length() != 0 && !participants.contains(participant)) {
                    participants.add(participant);
                }
            }
        }

        return participants.isEmpty();

    }


    /**
     * Initializes the components
     */
    private void initComponents() {
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evt) {
                closeDialog(evt);
            }
        });

        titleLabel = new JLabel();
        titleLabel.setFont(titleLabel.getFont().deriveFont((float) 12));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        getContentPane().setLayout(new GridBagLayout());
        getContentPane().setPreferredSize(new Dimension(400, 300));
        Insets insets = new Insets(2, 6, 2, 6);
        GridBagConstraints gridBagConstraints;

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = insets;
        getContentPane().add(titleLabel, gridBagConstraints);

        model = new TierExportTableModel();
        participantTable = new TierExportTable(model, ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        JScrollPane participantScroll = new JScrollPane(participantTable);
        Dimension size = new Dimension(350, 120);
        participantScroll.setPreferredSize(size);
        participantScroll.setMinimumSize(size);

        setModal(true);

        JPanel participantPanel = new JPanel();
        participantPanel.setLayout(new GridBagLayout());
        participantPanel.setBorder(new TitledBorder(ElanLocale.getString("DeleteParticipantDialog.SelectParticipants")));

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = insets;
        getContentPane().add(participantPanel, gridBagConstraints);

        buttonPanel = new JPanel();
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = insets;
        getContentPane().add(buttonPanel, gridBagConstraints);

        Dimension tableDim = new Dimension(100, 100);
        JScrollPane participantScrollPane = new JScrollPane(participantTable);
        participantScroll.setPreferredSize(tableDim);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = insets;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        participantPanel.add(participantScrollPane, gridBagConstraints);

        deleteButton = new JButton();
        closeButton = new JButton();

        buttonPanel.setLayout(new GridLayout(1, 2, 6, 0));

        buttonPanel.add(deleteButton);
        buttonPanel.add(closeButton);

        deleteButton.addActionListener(this);
        closeButton.addActionListener(this);
        updateLocale();
    }

    /**
     * Pack, size and set location.
     */
    private void postInit() {
        pack();
        setLocationRelativeTo(getParent());
    }


    private void updateLocale() {
        setTitle(ElanLocale.getString("DeleteParticipantDialog.Title"));
        //titleLabel.setText(ElanLocale.getString("AddDependentTierToTierStructureDlg.Title"));

        deleteButton.setText(ElanLocale.getString("Button.Delete"));
        closeButton.setText(ElanLocale.getString("Button.Close"));
    }


    /**
     * Closes the dialog
     *
     * @param evt the window closing event
     */
    private void closeDialog(WindowEvent evt) {
        setVisible(false);
        dispose();
    }


    /**
     * Extract all participants and fill the table.
     */
    private void extractParticipants() {
        if (transcription != null) {
            while (participantTable.getRowCount() > 0) {
                model.removeRow(0);
            }

            List<TierImpl> tiers = transcription.getTiers();
            List<String> participants = new ArrayList<String>();

            TierImpl tier;

            for (int i = 0; i < tiers.size(); i++) {
                tier = tiers.get(i);
                String participant = tier.getParticipant();
                if (participant != null && participant.trim().length() != 0 && !participants.contains(participant)) {
                    participants.add(participant);
                }
            }

            if (participants.size() > 0) {
                for (int i = 0; i < participants.size(); i++) {
                    model.addRow(Boolean.FALSE, participants.get(i));          
                }
            }

        }

    }

    /**
     * Returns the tiers that have been selected in the table.
     *
     * @return a list of the selected tiers
     */
    private List<String> getSelectedParticipants() {
        return model.getSelectedTiers();
    }


    /**
     * The action performed event handling
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == deleteButton) {
            startOperation();
        } else if (e.getSource() == closeButton) {
            setVisible(false);
            dispose();
        }
    }

    /**
     * Validates the input data and initiates the start process
     */
    private void startOperation() {
        List<String> selectedParticipants = getSelectedParticipants();

        if (selectedParticipants.size() == 0) {
            JOptionPane.showMessageDialog(this,
                                          ElanLocale.getString("DeleteParticipantDialog.Warning.NoParticipantSelected"),
                                          ElanLocale.getString("Message.Error"),
                                          JOptionPane.WARNING_MESSAGE);

            return;
        }

        List<TierImpl> allMatchedTiers = new ArrayList<TierImpl>();

        for (String participant : selectedParticipants) {
            List<TierImpl> matchedTiers = transcription.getTiersWithParticipant(participant);
            allMatchedTiers.addAll(matchedTiers);
        }

        Iterator<TierImpl> allMatchedIterator = allMatchedTiers.iterator();
        StringBuilder builder = new StringBuilder();

        while (allMatchedIterator.hasNext()) {
            TierImpl tier = allMatchedIterator.next();
            if (tier.hasParentTier()) {
                builder.append("-   ");
                builder.append(tier.getName() + "\n");
            } else {
                builder.append(tier.getName() + "\n");
            }
            List<TierImpl> depTiers = tier.getDependentTiers();
            if ((depTiers != null) && (depTiers.size() > 0)) {
                StringBuilder tmpBuf = new StringBuilder();

                Iterator<TierImpl> depIt = depTiers.iterator();
                while (depIt.hasNext()) {
                    Tier depTier = depIt.next();
                    if (!allMatchedTiers.contains(depTier)) {
                        tmpBuf.append("-   ");

                        tmpBuf.append(depTier.getName() + "\n");
                    }
                }

                if (tmpBuf.length() > 0) {
                    builder.append(ElanLocale.getString("EditTierDialog.Message.AlsoDeleted") + "\n");
                    builder.append(tmpBuf);
                }
            }

            builder.append("\n");
        }


        JPanel panel = new JPanel(new GridBagLayout());

        JTextArea tierList = new JTextArea(builder.toString());
        tierList.setEditable(false);
        tierList.setBackground(panel.getBackground());

        JScrollPane scrollPane = new JScrollPane(tierList);
        JLabel label = new JLabel(ElanLocale.getString("DeleteParticipantDialog.Message.ConfirmDelete"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(label, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(6, 10, 6, 6);
        panel.add(scrollPane, gbc);


        int prefheight = scrollPane.getViewport().getPreferredSize().height;
        int prefwidth = label.getPreferredSize().width;

        if (prefheight > this.getPreferredSize().height / 2) {
            prefheight = this.getPreferredSize().height / 2;
        }

        scrollPane.getViewport().setPreferredSize(new Dimension(prefwidth, prefheight));

        int option =
            JOptionPane.showConfirmDialog(this, panel, ElanLocale.getString("Message.Warning"), JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {

            Object[] args = new Object[] {allMatchedTiers};
            Command command = ELANCommandFactory.createCommand(transcription, ELANCommandFactory.DELETE_PARTICIPANT);
            command.execute(transcription, args);
            setVisible(false);
            dispose();

        }

    }

}
