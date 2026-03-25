package mpi.eudico.client.annotator.export;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Selection;
import mpi.eudico.client.annotator.tier.TierExportTable;
import mpi.eudico.client.annotator.tier.TierExportTableModel;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import static mpi.eudico.client.annotator.util.ResourceUtil.getImageIconFrom;


/**
 * Abstract class for a dialog for tier export.
 */
@SuppressWarnings("serial")
public abstract class AbstractTierExportDialog extends AbstractBasicExportDialog implements ActionListener {
    /**
     * table for tiers
     */
    protected final TierExportTableModel model;

    /**
     * restrict export to selection ?
     */
    protected final JCheckBox restrictCheckBox = new JCheckBox();

    /**
     * panel for a tier table
     */
    protected final JPanel tierSelectionPanel = new JPanel();

    /**
     * table ui
     */
    protected final JTable tierTable;

    /**
     * selection
     */
    protected final Selection selection;

    /**
     * column id for the include in export checkbox column, invisible
     */
    protected static final String EXPORT_COLUMN = "export";

    /**
     * column id for the tier name column, invisible
     */
    protected static final String TIER_NAME_COLUMN = "tier";

    /**
     * tier button panel initialization
     */
    protected final JPanel tierButtonPanel = new JPanel();
    /**
     * down button initialization
     */
    protected final JButton downButton = new JButton();
    /**
     * up button initialization
     */
    protected final JButton upButton = new JButton();
    /**
     * all button  initialization
     */
    protected final JButton allButton = new JButton();
    /**
     * none button initialization
     */
    protected final JButton noneButton = new JButton();
    //protected final JButton advancedSelectionButton = new JButton();

    /**
     * Creates a new AbstractTierExportDialog instance.
     *
     * @param parent the parent frame
     * @param modal whether this dialog should be modal
     * @param transcription the transcription to export
     * @param selection the selected time interval
     */
    public AbstractTierExportDialog(Frame parent, boolean modal, TranscriptionImpl transcription, Selection selection) {
        super(parent, modal, transcription);
        model = new TierExportTableModel();
        model.setColumnIdentifiers(new String[] {EXPORT_COLUMN, TIER_NAME_COLUMN});
        tierTable = new TierExportTable(model);

        this.selection = selection;
    }

    /**
     * Returns the list of selected tiers
     *
     * @return list of selected tiers
     */
    protected List<String> getSelectedTiers() {
        return model.getSelectedTiers();
    }

    /**
     * Initializes UI elements.
     */
    @Override
    protected void makeLayout() {
        super.makeLayout();
        getContentPane().setLayout(new GridBagLayout());
        tierButtonPanel.setLayout(new GridBagLayout());
        optionsPanel.setLayout(new GridBagLayout());

        getImageIconFrom("/toolbarButtonGraphics/navigation/Up16.gif").ifPresentOrElse(upButton::setIcon,
                                                                                       () -> upButton.setText("Up"));

        getImageIconFrom("/toolbarButtonGraphics/navigation/Down16.gif").ifPresentOrElse(downButton::setIcon,
                                                                                         () -> downButton.setText("Down"));

        GridBagConstraints gridBagConstraints;

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = insets;
        getContentPane().add(titleLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = insets;
        getContentPane().add(tierSelectionPanel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = insets;
        getContentPane().add(optionsPanel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = insets;
        getContentPane().add(buttonPanel, gridBagConstraints);

        // tierSelectionPanel
        tierSelectionPanel.setLayout(new GridBagLayout());

        Dimension tableDim = new Dimension(50, 100);
        JScrollPane tierScrollPane = new JScrollPane(tierTable);
        tierScrollPane.setPreferredSize(tableDim);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = insets;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        tierSelectionPanel.add(tierScrollPane, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = insets;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        //gridBagConstraints.weightx = 1.0;
        //gridBagConstraints.weighty = 1.0;
        tierSelectionPanel.add(tierButtonPanel, gridBagConstraints);

        // tierButtonPanel
        getImageIconFrom("/toolbarButtonGraphics/navigation/Up16.gif").ifPresentOrElse(upButton::setIcon,
                                                                                       () -> upButton.setText("Up"));
        getImageIconFrom("/toolbarButtonGraphics/navigation/Down16.gif").ifPresentOrElse(downButton::setIcon,
                                                                                         () -> downButton.setText("Down"));

        upButton.addActionListener(this);
        downButton.addActionListener(this);
        allButton.addActionListener(this);
        noneButton.addActionListener(this);
        //advancedSelectionButton.addActionListener(this);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        tierButtonPanel.add(upButton, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        tierButtonPanel.add(downButton, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = insets;
        tierButtonPanel.add(noneButton, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = insets;
        tierButtonPanel.add(allButton, gridBagConstraints);

        //      gridBagConstraints = new GridBagConstraints();
        //      gridBagConstraints.gridx = 4;
        //      gridBagConstraints.gridy = 0;
        //      gridBagConstraints.anchor = GridBagConstraints.EAST;
        //      tierButtonPanel.add(advancedSelectionButton, gridBagConstraints);

    }

    /**
     * Set the localized text on UI elements
     */
    @Override
    protected void updateLocale() {
        super.updateLocale();
        tierSelectionPanel.setBorder(new TitledBorder(ElanLocale.getString("ExportDialog.Label.SelectTiers")));
        optionsPanel.setBorder(new TitledBorder(ElanLocale.getString("ExportDialog.Label.Options")));
        restrictCheckBox.setText(ElanLocale.getString("ExportDialog.Restrict"));

        allButton.setText(ElanLocale.getString("Button.SelectAll"));
        noneButton.setText(ElanLocale.getString("Button.SelectNone"));
        //        advancedSelectionButton.setText(ElanLocale.getString("ExportDialog.AdvacedSelectionOptions"));
    }

    /**
     * The action performed event handling.
     *
     * @param ae the action event
     */
    @Override
    public void actionPerformed(ActionEvent ae) {
        Object source = ae.getSource();

        if (source == upButton) {
            moveUp();
        } else if (source == downButton) {
            moveDown();
        } else if (source == allButton) {
            selectAll();
        } else if (source == noneButton) {
            selectNone();
        }
        //        else if (source == advancedSelectionButton) {
        //            ShowHideMoreTiersDlg dialog = new ShowHideMoreTiersDlg(transcription, new Vector(getSelectedTiers()));
        //            selectTiers(dialog.getVisibleTierNames());
        //        }
        else {
            super.actionPerformed(ae);
        }
    }

    /**
     * Moves selected tiers down in the list of tiers.
     */
    protected void moveDown() {
        if (tierTable == null || model == null || model.getRowCount() < 2) {
            return;
        }

        int[] selected = tierTable.getSelectedRows();

        for (int i = selected.length - 1; i >= 0; i--) {
            int row = selected[i];

            if ((row < (model.getRowCount() - 1)) && !tierTable.isRowSelected(row + 1)) {
                model.moveRow(row, row, row + 1);
                tierTable.changeSelection(row, 0, true, false);
                tierTable.changeSelection(row + 1, 0, true, false);
            }
        }
    }

    /**
     * Moves selected tiers up in the list of tiers.
     */
    protected void moveUp() {
        if (tierTable == null || model == null || model.getRowCount() < 2) {
            return;
        }

        int[] selected = tierTable.getSelectedRows();

        for (int row : selected) {

            if ((row > 0) && !tierTable.isRowSelected(row - 1)) {
                model.moveRow(row, row, row - 1);
                tierTable.changeSelection(row, 0, true, false);
                tierTable.changeSelection(row - 1, 0, true, false);
            }
        }
    }

    /**
     * Set all tiers selected.
     */
    protected void selectAll() {
        if (tierTable == null || model == null || model.getRowCount() < 1) {
            return;
        }
        int includeCol = model.findColumn(EXPORT_COLUMN);

        for (int i = 0; i < model.getRowCount(); i++) {
            model.setValueAt(Boolean.TRUE, i, includeCol);
        }
    }

    /**
     * Set all tiers deselected.
     */
    protected void selectNone() {
        if (tierTable == null || model == null || model.getRowCount() < 1) {
            return;
        }
        int includeCol = model.findColumn(EXPORT_COLUMN);

        for (int i = 0; i < model.getRowCount(); i++) {
            model.setValueAt(Boolean.FALSE, i, includeCol);
        }
        tierTable.revalidate();
    }

    /**
     * Loads the selected tiers from the preferences.
     *
     * @param tierList a list of selected tiers read from the preferences
     */
    protected void loadTierPreferences(List<String> tierList) {
        if (!tierList.isEmpty()) {
            int includeCol = model.findColumn(EXPORT_COLUMN);
            int nameCol = model.findColumn(TIER_NAME_COLUMN);

            final int rowCount = model.getRowCount();
            for (int i = 0; i < rowCount; i++) {
                for (String tier : tierList) {
                    if (((String) model.getValueAt(i, nameCol)).compareTo(tier) == 0) {
                        model.setValueAt(Boolean.TRUE, i, includeCol);
                        break;
                    } else {
                        model.setValueAt(Boolean.FALSE, i, includeCol);
                    }
                }
            }
        }
    }
}
