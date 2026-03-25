package mpi.eudico.client.annotator.gui;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.AbstractTierSortAndSelectPanel.Modes;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * A dialog as a host for a tier-sort-and-select panel.
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class TierSortAndSelectDialog2 extends JDialog implements ActionListener {
    private final TranscriptionTierSortAndSelectPanel selectPanel;

    /**
     * panel for start and close buttons (bottom component)
     */
    protected JPanel buttonPanel;
    /**
     * close button
     */
    private JButton cancelButton;

    /**
     * ok button
     */
    private JButton okButton;

    private List<String> returnedTiers = null;

    /**
     * Constructor.
     *
     * @param owner the parent window
     * @param transcription the transcription containing the tiers
     * @param tierOrder the initial order for the tiers
     * @param selectedTiers the tiers to set selected
     */
    public TierSortAndSelectDialog2(Dialog owner, TranscriptionImpl transcription, List<String> tierOrder,
                                    List<String> selectedTiers) {
        super(owner, true);
        selectPanel = new TranscriptionTierSortAndSelectPanel(transcription, tierOrder, selectedTiers);
        initComponents();
    }

    /**
     * Constructor.
     *
     * @param owner the parent window
     * @param transcription the transcription containing the tiers
     * @param tierOrder the initial order for the tiers
     * @param selectedTiers the tiers to set selected
     * @param allowReordering whether or not the UI should support reordering of the tiers
     * @param allowSorting whether or not the UI should support sorting
     */
    public TierSortAndSelectDialog2(Dialog owner, TranscriptionImpl transcription, List<String> tierOrder,
                                    List<String> selectedTiers, boolean allowReordering, boolean allowSorting) {
        super(owner, true);
        selectPanel =
            new TranscriptionTierSortAndSelectPanel(transcription, tierOrder, selectedTiers, allowReordering, allowSorting);
        initComponents();
    }

    /**
     * Constructor.
     *
     * @param owner the parent window
     * @param transcription the transcription containing the tiers
     * @param tierOrder the initial order for the tiers
     * @param selectedTiers the tiers to set selected
     * @param allowReordering whether or not the UI should support reordering of the tiers
     * @param allowSorting whether or not the UI should support sorting
     * @param tierMode one of the {@link AbstractTierSortAndSelectPanel.Modes}
     */
    public TierSortAndSelectDialog2(Dialog owner, TranscriptionImpl transcription, List<String> tierOrder,
                                    List<String> selectedTiers, boolean allowReordering, boolean allowSorting,
                                    Modes tierMode) {
        super(owner, true);
        selectPanel = new TranscriptionTierSortAndSelectPanel(transcription,
                                                              tierOrder,
                                                              selectedTiers,
                                                              allowReordering,
                                                              allowSorting,
                                                              tierMode);
        initComponents();
    }

    /**
     * Constructor.
     *
     * @param owner the parent window
     * @param transcription the transcription containing the tiers
     * @param tierOrder the initial order for the tiers
     * @param selectedTiers the tiers to set selected
     */
    public TierSortAndSelectDialog2(Frame owner, TranscriptionImpl transcription, List<String> tierOrder,
                                    List<String> selectedTiers) {
        super(owner, true);
        selectPanel = new TranscriptionTierSortAndSelectPanel(transcription, tierOrder, selectedTiers);
        initComponents();
    }

    /**
     * Constructor.
     *
     * @param owner the parent window
     * @param transcription the transcription containing the tiers
     * @param tierOrder the initial order for the tiers
     * @param selectedTiers the tiers to set selected
     * @param allowReordering whether or not the UI should support reordering of the tiers
     * @param allowSorting whether or not the UI should support sorting
     */
    public TierSortAndSelectDialog2(Frame owner, TranscriptionImpl transcription, List<String> tierOrder,
                                    List<String> selectedTiers, boolean allowReordering, boolean allowSorting) {
        super(owner, true);
        selectPanel =
            new TranscriptionTierSortAndSelectPanel(transcription, tierOrder, selectedTiers, allowReordering, allowSorting);
        initComponents();
    }

    /**
     * Constructor.
     *
     * @param owner the parent window
     * @param transcription the transcription containing the tiers
     * @param tierOrder the initial order for the tiers
     * @param selectedTiers the tiers to set selected
     * @param allowReordering whether or not the UI should support reordering of the tiers
     * @param allowSorting whether or not the UI should support sorting
     * @param tierMode one of the {@link AbstractTierSortAndSelectPanel.Modes}
     */
    public TierSortAndSelectDialog2(Frame owner, TranscriptionImpl transcription, List<String> tierOrder,
                                    List<String> selectedTiers, boolean allowReordering, boolean allowSorting,
                                    Modes tierMode) {
        super(owner, true);
        selectPanel = new TranscriptionTierSortAndSelectPanel(transcription,
                                                              tierOrder,
                                                              selectedTiers,
                                                              allowReordering,
                                                              allowSorting,
                                                              tierMode);
        initComponents();
    }

    private void initComponents() {
        getContentPane().setLayout(new GridBagLayout());
        Insets insets = new Insets(4, 6, 4, 6);
        // add tab pane / panel
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = insets;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(selectPanel, gridBagConstraints);

        // add ok/cancel panel
        cancelButton = new JButton(ElanLocale.getString("Button.Cancel"));
        cancelButton.addActionListener(this);
        okButton = new JButton(ElanLocale.getString("Button.OK"));
        okButton.addActionListener(this);
        buttonPanel = new JPanel(new GridLayout(1, 2, 6, 0));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = insets;
        getContentPane().add(buttonPanel, gridBagConstraints);

        pack();
        int minW = 400;
        int minH = 320;
        setSize(Math.max(minW, getWidth()), Math.max(minH, getHeight()));
    }

    /**
     * Returns the selected tiers.
     *
     * @return the selected tiers or null in case of canceling the dialog
     */
    public List<String> getSelectedTiers() {
        return returnedTiers;
    }

    /**
     * Sets the selected tiers. Tiers not in the list will be unselected.
     *
     * @param selectedTiers the list of selected tiers
     *
     * @see #setHiddenTiers(List)
     */
    public void setSelectedTiers(List<String> selectedTiers) {
        selectPanel.setSelectedTiers(selectedTiers);
    }

    /**
     * Returns the hidden, the unselected tiers.
     *
     * @return the hidden tiers
     *
     * @see #getSelectedTiers()
     */
    public List<String> getHiddenTiers() {
        return selectPanel.getHiddenTiers();
    }

    /**
     * Sets the unselected, hidden tiers. Tiers not in this list will be selected.
     *
     * @param hiddenTiers the list of hidden tiers
     *
     * @see #setSelectedTiers(List)
     */
    public void setHiddenTiers(List<String> hiddenTiers) {
        selectPanel.setHiddenTiers(hiddenTiers);
    }

    /**
     * Returns all tiers in the current order.
     *
     * @return the list of all tiers in the current order
     */
    public List<String> getTierOrder() {
        selectPanel.applyChanges();

        return selectPanel.getTierOrder();
    }

    /**
     * Returns the currently used selection mode.
     *
     * <p>(i.e whether the selection of tiers is based on
     * types/ participant/ tier names/ annotators)
     *
     * @return the selection mode
     */
    public String getSelectionMode() {
        return selectPanel.getSelectionMode();
    }

    /**
     * Sets the last used selection mode.
     *
     * @param mode the name of the active mode or tab
     * @param hiddenItems the tiers that have been unselected before
     */
    public void setSelectionMode(String mode, List<String> hiddenItems) {
        selectPanel.setSelectionMode(mode, hiddenItems);
    }

    /**
     * Returns the current selected items.
     *
     * @return the list of selected items
     *
     * @see #getUnselectedItems()
     */
    public List<String> getSelectedItems() {
        return selectPanel.getSelectedItems();
    }

    /**
     * Returns the items that are unselected in the current tab.
     *
     * @return the list of unselected items
     *
     * @see #getSelectedItems()
     */
    public List<String> getUnselectedItems() {
        return selectPanel.getUnselectedItems();
    }

    /**
     * Sets items selected in the current table/tab.
     *
     * @param items the selected items
     *
     * @see #setUnselectedItems(List)
     */
    public void setSelectedItems(List<String> items) {
        selectPanel.setSelectedItems(items);
    }

    /**
     * Sets items unselected in the current table/tab.
     *
     * @param items the unselected items
     *
     * @see #setSelectedItems(List)
     */
    public void setUnselectedItems(List<String> items) {
        selectPanel.setUnselectedItems(items);
    }

    /**
     * Ok and cancel button actions.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == cancelButton) {
            setVisible(false);
            dispose();
        } else if (e.getSource() == okButton) {
            selectPanel.applyChanges();
            returnedTiers = selectPanel.getSelectedTiers();
            setVisible(false);
            dispose();
        }

    }
}
