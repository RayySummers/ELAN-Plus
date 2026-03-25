package mpi.eudico.client.annotator.interannotator;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.client.annotator.tier.SelectableContentTableModel;
import mpi.eudico.client.annotator.tier.TierExportTable;
import mpi.eudico.client.util.UneditableTableModel;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.util.EmptyStringComparator;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import java.util.*;

import static mpi.eudico.client.annotator.util.ClientLogger.LOG;

/**
 * A AnnotatorCompare object creates both a wizard object and the objects that are the steps the wizard presents. This class,
 * the second or the third step in the wizard dialog, helps in making a choice between the classical way of comparing and
 * comparing by calculating the value of kappa for the matrices generated from two annotated tiers. For additional comments,
 * refer to the first step.
 *
 * @author keeloo
 * @author Han Sloetjes
 */
public class TiersSelectionStep extends StepPane implements ListSelectionListener,
                                                            TableModelListener {

    /*
     * The current document, can be null.
     */
    private final TranscriptionImpl transcription;

    /**
     * ide generated
     */
    private static final long serialVersionUID = 1L;

    // two tables
    JTable table1;
    JTable table2;

    // two labels
    private JLabel hint1;
    private JLabel hint2;
    
    // two buttons
    private JButton allButton;
    private JButton noneButton;
    private JPanel buttonPanel;

    // retrieve settings from previous steps globally
    private CompareConstants.METHOD method;
    private CompareConstants.FILE_MATCHING tierSource;
    private CompareConstants.MATCHING tierMatching;
    private List<File> selFiles;
    // have a list available for caching the files when going to the previous step
    private List<File> oldSelFiles;
    private final List<String> allTierNames;
    private String tierCustomSeparator;

    private JPanel tierPanel;

    private JScrollPane table1ScrollPane;
    private JScrollPane table2ScrollPane;
    // in case of affix based matching, highlight matching tiers in second, non-editable table?
    private final TierAndFileMatcher tierMatcher;
    // for debugging purposes introduce a synchronous mode
    private final boolean synchronousMode = false;
    private boolean groupCompareMode = false;

    /**
     * Creates a new selection step instance.
     *
     * @param wizard organizing the steps
     * @param transcription to select the tiers from, can be null
     */
    public TiersSelectionStep(MultiStepPane wizard, TranscriptionImpl transcription) {

        super(wizard);

        this.transcription = transcription;

        // enable the wizard to jump to a specific step by looking at step names
        this.setName("CompareAnnotatorsDialog.TierSelectionStep");

        allTierNames = new ArrayList<String>();
        tierMatcher = new TierAndFileMatcher();
        // create the dialog panel
        createPanel();
    }

    /**
     * Adjust the pane's component properties when in the preceding pane 'forward' was pushed
     *
     * @param enable the enabled flag for the finish button
     */
    private void setStateFinish(boolean enable) {
        multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, enable);
        //multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, enable);
    }

    @SuppressWarnings("unchecked")
    private void checkConditions() {
        // store "current" (or previous) values in order to determine whether tiers have to be (re)loaded
        // how best to check the list of files?
        CompareConstants.FILE_MATCHING oldTierSource = tierSource;
        CompareConstants.MATCHING oldTierMatching = tierMatching;
        boolean oldGroupCompare = groupCompareMode;

        // this is done in leave step backward
        /*
        if (selFiles != null) {
            oldSelFiles = new ArrayList<File>(selFiles);
        }
        */
        // load the new values
        method = (CompareConstants.METHOD) multiPane.getStepProperty(CompareConstants.METHOD_KEY); // "compareMethod"
        tierSource =
            (CompareConstants.FILE_MATCHING) multiPane.getStepProperty(CompareConstants.TIER_SOURCE_KEY); // "tierSource",
        // current, single file, across files
        tierMatching =
            (CompareConstants.MATCHING) multiPane.getStepProperty(CompareConstants.TIER_MATCH_KEY); // "tierMatching",
        // manual, prefix, suffix or sameName
        selFiles = (List<File>) multiPane.getStepProperty(CompareConstants.SEL_FILES_KEY); // "selectedFiles"
        tierCustomSeparator = (String) multiPane.getStepProperty(CompareConstants.TIER_SEPARATOR_KEY);
        groupCompareMode = (Boolean.TRUE.equals(multiPane.getStepProperty(CompareConstants.GROUP_COMPARE_KEY)));

        if (transcription == null && (selFiles == null || selFiles.isEmpty())) {
            setStateFinish(false);
            // return; //return here?
        }
        // if we have to switch from one table to two or vice versa, clear the layout and rebuild it
        boolean rebuildLayout = false;
        boolean reloadTiers = false;
        // rebuild layout if it is not the same as the previous
        if (oldTierMatching != tierMatching) {
            rebuildLayout = true;
        }
        if (oldTierSource != tierSource) {
            rebuildLayout = true;
            if ((oldTierSource == CompareConstants.FILE_MATCHING.CURRENT_DOC
                 && tierSource != CompareConstants.FILE_MATCHING.CURRENT_DOC) || (oldTierSource
                                                                                  != CompareConstants.FILE_MATCHING.CURRENT_DOC
                                                                                  && tierSource
                                                                                     == CompareConstants.FILE_MATCHING.CURRENT_DOC)) {
                reloadTiers = true;
            }
        }
        // if only the set of files changed the ui doesn't need to be updated (?), only
        // the tiers need to be reloaded
        if (oldSelFiles == null && selFiles != null) {
            reloadTiers = true;
        } else if (oldSelFiles != null && selFiles == null) {
            reloadTiers = true;
        } else if (oldSelFiles != null && selFiles != null) {
            if (oldSelFiles.size() != selFiles.size()) {
                reloadTiers = true;
            } else {
                // the equals method returns true of all elements are in the same order which is not necessary for
                // this check
                for (File f : oldSelFiles) {
                    if (!selFiles.contains(f)) {
                        reloadTiers = true;
                        break;
                    }
                }
            }
        }// end of testing if tiers have to be reloaded

        if (oldTierMatching != tierMatching) {
            rebuildLayout = true;
        }
        if (oldGroupCompare != groupCompareMode) { // extend this check
            rebuildLayout = true;
        }

        if (rebuildLayout) {
            if (tierPanel != null) {
                tierPanel.removeAll();
            }

            updatePanel(tierMatching);
        }

        if (reloadTiers) {
            reloadTiers();
            fillTables();
        } else {
            if (rebuildLayout) {
                // if the layout of tables has changed but the tiers haven't,
                // the tables still need to be updated
                fillTables();
            }
        }
        updateButtonState();
    }

    /**
     * Change component associated text on a change of language preference
     */
    public void updateLocale() {
        // update label texts
        hint1.setText(ElanLocale.getString("CompareAnnotatorsDialog.TierSelectionStep.Suggestion.Hint1"));
        hint2.setText(ElanLocale.getString("CompareAnnotatorsDialog.TierSelectionStep.Suggestion.Hint2"));
        allButton.setText(ElanLocale.getString("Button.SelectAll"));
        noneButton.setText(ElanLocale.getString("Button.SelectNone"));
    }

    /*
     * Creates all ui elements. The layout is made when entering this step.
     */
    private void createPanel() {

        // create labels showing a hint
        hint1 = new JLabel();
        hint2 = new JLabel();
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        allButton = new JButton();
        noneButton = new JButton();
        buttonPanel.add(allButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(6, 0)));
        buttonPanel.add(noneButton);
        SelectionAction buttonAction = new SelectionAction();
        allButton.addActionListener(buttonAction);
        noneButton.addActionListener(buttonAction);

        /*
         * With the labels, all language sensitive components have been created,
         * so a text can be added to them now.
         */
        updateLocale();

        UneditableTableModel model1 = new UneditableTableModel(0, 0);
        UneditableTableModel model2 = new UneditableTableModel(0, 0);

        // create two tables, and associate these with the models and a listener
        table1 = new JTable(model1);
        table1.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table2 = new JTable(model2);
        table2.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table1.getSelectionModel().addListSelectionListener(this);
        table2.getSelectionModel().addListSelectionListener(this);

        // add scrolling
        Dimension prdim = new Dimension(400, 80);
        table1ScrollPane = new JScrollPane(table1);
        table1ScrollPane.setPreferredSize(prdim);
        table2ScrollPane = new JScrollPane(table2);
        table2ScrollPane.setPreferredSize(prdim);

        // create the panel
        tierPanel = new JPanel();
        tierPanel.setLayout(new GridBagLayout());

        // prepare to add components to the table
        Insets insets = new Insets(6, 6, 6, 6);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = insets;
        gbc.weightx = 1.0;

        gbc.gridx = 0;
        gbc.gridy = 0;

        // add the hints and tables to the panel
        tierPanel.add(hint1, gbc);
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        tierPanel.add(table1ScrollPane, gbc);
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        gbc.insets = new Insets(3, 6, 6, 6);
        gbc.weighty = 0.0;
        tierPanel.add(buttonPanel, gbc);
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0.0;
        gbc.insets = insets;
        tierPanel.add(hint2, gbc);
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        tierPanel.add(table2ScrollPane, gbc);

        // add the panel to the wizard pane
        setLayout(new GridBagLayout());
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        add(tierPanel, gbc);
    }

    /**
     * Update the user interface based on the way tiers are coupled for comparison
     *
     * @param mode the matching mode
     */
    private void updatePanel(CompareConstants.MATCHING mode) {
        table2.getSelectionModel().removeListSelectionListener(this);
        if (mode == CompareConstants.MATCHING.MANUAL && !groupCompareMode) {
            // pair one by one, two tables, valid for current document and in same file
            // prepare to add components to the table
            Insets insets = new Insets(6, 6, 6, 6);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = insets;
            gbc.weightx = 1.0;

            gbc.gridx = 0;
            gbc.gridy = 0;

            // add the hints and tables to the panel
            tierPanel.add(hint1, gbc);
            gbc.gridy = 1;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weighty = 1.0;
            tierPanel.add(table1ScrollPane, gbc);
            gbc.gridy = 2;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weighty = 0.0;
            tierPanel.add(hint2, gbc);
            gbc.gridy = 3;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weighty = 1.0;
            tierPanel.add(table2ScrollPane, gbc);
            //
            table2.setEnabled(true);
            table2.getSelectionModel().addListSelectionListener(this);
            hint1.setText(ElanLocale.getString("CompareAnnotatorsDialog.TierSelectionStep.Suggestion.Hint1"));
            hint2.setText(ElanLocale.getString("CompareAnnotatorsDialog.TierSelectionStep.Suggestion.Hint2"));
        } else if (mode == CompareConstants.MATCHING.AFFIX
                   || mode == CompareConstants.MATCHING.SUFFIX
                   || mode == CompareConstants.MATCHING.PREFIX) {
            // or have disabled second table, highlighting the automatically detected counterpart
            // prepare to add components to the table

            Insets insets = new Insets(6, 6, 6, 6);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = insets;
            gbc.weightx = 1.0;

            gbc.gridx = 0;
            gbc.gridy = 0;

            // add the hints and tables to the panel
            tierPanel.add(hint1, gbc);
            gbc.gridy = 1;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weighty = 1.0;
            tierPanel.add(table1ScrollPane, gbc);
            gbc.gridy = 2;
            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.NORTHEAST;
            gbc.weighty = 0.0;
            gbc.insets = new Insets(3, 6, 6, 6);
            tierPanel.add(buttonPanel, gbc);
            gbc.gridy = 3;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weighty = 0.0;
            gbc.insets = insets;
            tierPanel.add(hint2, gbc);
            gbc.gridy = 4;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weighty = 1.0;
            tierPanel.add(table2ScrollPane, gbc);
            //
            table2.setEnabled(false);
            hint1.setText(ElanLocale.getString("CompareAnnotatorsDialog.TierSelectionStep.Suggestion.Hint3"));
            hint2.setText(ElanLocale.getString("CompareAnnotatorsDialog.TierSelectionStep.Suggestion.Hint4"));
        } else if (mode == CompareConstants.MATCHING.SAME_NAME || groupCompareMode) {
            // sameName or manual selection in group compare, only one table, multiple selection
            Insets insets = new Insets(6, 6, 6, 6);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = insets;
            gbc.weightx = 1.0;

            gbc.gridx = 0;
            gbc.gridy = 0;

            // add the hint label and table to the panel
            tierPanel.add(hint1, gbc);
            gbc.gridy = 1;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weighty = 1.0;
            tierPanel.add(table1ScrollPane, gbc);
            gbc.gridy = 2;
            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.NORTHEAST;
            gbc.weighty = 0.0;
            gbc.insets = new Insets(3, 6, 6, 6);
            tierPanel.add(buttonPanel, gbc);
            //
            hint1.setText(ElanLocale.getString("CompareAnnotatorsDialog.TierSelectionStep.Suggestion.Hint5"));
        }
    }

    /**
     * (Re)loads the tiers from a set of files or from a transcription and fills one or two table models, depending on the
     * method of matching of tiers to compare.
     */
    private void reloadTiers() {
        setStateFinish(false);
        List<String> tierNames = new ArrayList<>();

        if (transcription != null && tierSource == CompareConstants.FILE_MATCHING.CURRENT_DOC) {
            for (int i = 0; i < transcription.getTiers().size(); i++) {
                tierNames.add(transcription.getTiers().get(i).getName());
            }
        } else if (selFiles != null) {
            TierLoader tierLoader = new TierLoader(selFiles);
            if (!synchronousMode) {
                tierLoader.start();
                //long startTime = System.currentTimeMillis();
                // && System.currentTimeMillis() - startTime < 10000
                while (tierLoader.isAlive()) {
                    //System.out.println("Number of files  processed: " + tierLoader.getNumProccessed());
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ie) {

                    }
                }
            } else {
                tierLoader.run(); // call the run method on the current thread
            }
            //System.out.println("Final number of files  processed: " + tierLoader.getNumProccessed());

            // check the type of table (model) needed
            tierNames = tierLoader.getTierNames();
        }
        allTierNames.clear();
        allTierNames.addAll(tierNames);
        multiPane.putStepProperty(CompareConstants.ALL_TIER_NAMES_KEY, tierNames);
    }

    /**
     * Creates and populates one or two table models and adds them to the tables. Assumes tiers have been loaded and tables
     * have been created and are already in the gui layout.
     */
    private void fillTables() {

        if (tierMatching == CompareConstants.MATCHING.MANUAL && !groupCompareMode) {
            // two tables, both single selection
            UneditableTableModel tierModel1 = new UneditableTableModel(0, 1);
            UneditableTableModel tierModel2 = new UneditableTableModel(0, 1);
            for (String s : allTierNames) {
                String[] rowData = new String[] {s};
                tierModel1.addRow(rowData);
                tierModel2.addRow(rowData);
            }
            if (table1 instanceof TierExportTable) {
            	table1 = new JTable();
            	table1ScrollPane.setViewportView(table1);
            }
            table1.setModel(tierModel1);
            table2.setModel(tierModel2);
            table1.getTableHeader().setReorderingAllowed(false);
            table2.getTableHeader().setReorderingAllowed(false);
            tierModel1.setColumnIdentifiers(new Object[] {ElanLocale.getString("EditTierDialog.Label.TierName")});
            tierModel2.setColumnIdentifiers(new Object[] {ElanLocale.getString("EditTierDialog.Label.TierName")});

            makeRowsSortable(table1, tierModel1, new int[] {0});
            makeRowsSortable(table2, tierModel2, new int[] {0});

            table1.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table2.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table1.getSelectionModel().addListSelectionListener(this);
            table2.getSelectionModel().addListSelectionListener(this);
        } else if (tierMatching == CompareConstants.MATCHING.AFFIX
                   || tierMatching == CompareConstants.MATCHING.PREFIX
                   || tierMatching == CompareConstants.MATCHING.SUFFIX) {
            // two tables, the second one disabled, or one table, with a third column
            Set<String> tierColumnData = new TreeSet<String>();
            for (String s : allTierNames) {
                tierColumnData.add(s);
            }
            SelectableContentTableModel tierModel1 = new SelectableContentTableModel(tierColumnData);
            //SelectableContentTableModel tierModel2 = new SelectableContentTableModel(tierColumnData);
            UneditableTableModel tierModel2 = new UneditableTableModel(0, 1);
            tierModel2.setRowCount(tierColumnData.size());
            Iterator<String> tierIter = tierColumnData.iterator();
            int row = 0;
            while (tierIter.hasNext()) {
                tierModel2.setValueAt(tierIter.next(), row, 0);
                row++;
            }

            if (!(table1 instanceof TierExportTable)) {
            	table1 = new TierExportTable(new DefaultTableModel(0, 1), true);
            	table1ScrollPane.setViewportView(table1);
            }
            
            table1.setModel(tierModel1);
            table2.setModel(tierModel2);
            table1.getTableHeader().setReorderingAllowed(false);
            table2.getTableHeader().setReorderingAllowed(false);

            makeRowsSortable(table1, tierModel1, new int[] {1});
            makeRowsSortable(table2, tierModel2, new int[] {0});

            table1.getColumnModel().getColumn(0).setHeaderValue(null);
            table1.getColumnModel()
                  .getColumn(1)
                  .setHeaderValue(ElanLocale.getString("FileAndTierSelectionStepPane.Column.TierName"));
            table1.getColumnModel().getColumn(0).setMaxWidth(30);

            table2.getColumnModel()
                  .getColumn(0)
                  .setHeaderValue(ElanLocale.getString("FileAndTierSelectionStepPane.Column.TierName"));
            table1.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            table2.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            //table1.getSelectionModel().addListSelectionListener(this);
            table2.setEnabled(false);
            tierModel1.addTableModelListener(this);
        } else if (tierMatching == CompareConstants.MATCHING.SAME_NAME || groupCompareMode) {
            // one table, multi selection
            Set<String> tierColumnData = new TreeSet<String>();
            for (String s : allTierNames) {
                tierColumnData.add(s);
            }
            SelectableContentTableModel tierModel1 = new SelectableContentTableModel(tierColumnData);
            if (!(table1 instanceof TierExportTable)) {
            	table1 = new TierExportTable(new DefaultTableModel(0, 1), true);
            	table1ScrollPane.setViewportView(table1);
            }
            table1.setModel(tierModel1);
            table1.getTableHeader().setReorderingAllowed(false);

            makeRowsSortable(table1, tierModel1, new int[] {1});

            table1.getColumnModel().getColumn(0).setHeaderValue(null);
            table1.getColumnModel()
                  .getColumn(1)
                  .setHeaderValue(ElanLocale.getString("FileAndTierSelectionStepPane.Column.TierName"));
            table1.getColumnModel().getColumn(0).setMaxWidth(30);
            table1.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            //table1.getSelectionModel().addListSelectionListener(this);
            tierModel1.addTableModelListener(this);
        }
    }

    /**
     * Performs checks on table(s) to see if the next/finish button should be enabled or not.
     */
    private void updateButtonState() {
        if (tierMatching == null) {
            setStateFinish(false);
            return;
        }

        if (tierMatching == CompareConstants.MATCHING.MANUAL && !groupCompareMode) {
            // two tables visible, both should have one selected
            if (table1.getSelectedRow() != -1 && table2.getSelectedRow() != -1) {
                // in case of same document check if it is not the same
                if (tierSource == CompareConstants.FILE_MATCHING.ACROSS_FILES) {
                    setStateFinish(true);
                } else { // current document or in single file
                    setStateFinish(table1.getSelectedRow() != table2.getSelectedRow());
                }
            } else {
                setStateFinish(false);
            }
        } else if (tierMatching == CompareConstants.MATCHING.AFFIX
                   || tierMatching == CompareConstants.MATCHING.PREFIX
                   || tierMatching == CompareConstants.MATCHING.SUFFIX) {
            // tierMatching is based on suffix/prefix, at least one row in both tables
            boolean table1Sel = false;
            TableModel tierModel = table1.getModel();
            if (tierModel instanceof SelectableContentTableModel tm) {
                table1Sel = !tm.nothingSelected();
                //setStateFinish( !tm.nothingSelected() );
            } else {
                table1Sel = table1.getSelectedRow() != -1;
                //setStateFinish(table1.getSelectedRow() != -1);
            }
            boolean table2Sel = table2.getSelectedRowCount() > 0;
            setStateFinish(table1Sel && table2Sel);
        } else if (tierMatching == CompareConstants.MATCHING.SAME_NAME) {
            //at least one row for tierMatching is same name (implies different files)
            TableModel tierModel = table1.getModel();
            if (tierModel instanceof SelectableContentTableModel tm) {
                setStateFinish(!tm.nothingSelected());
            } else {
                setStateFinish(table1.getSelectedRow() != -1);
            }

        } else if (groupCompareMode) { // implies manual matching
            // if manual group compare mode
            TableModel tierModel = table1.getModel();
            if (tierModel instanceof SelectableContentTableModel tm) {
                setStateFinish(tm.getSelectedValues().size() > 1);
            } else {
                setStateFinish(table1.getSelectedRowCount() > 1);
            }
        }
    }

    /**
     * Updates the second table to highlight tiers that match (affix based) the tiers selected in the first table.
     */
    private void updateMatchingTiersTable() {
        TableModel tierModel = table1.getModel();
        if (tierModel instanceof SelectableContentTableModel tm) {
            List<Object> selectedTiers1 = tm.getSelectedValues();
            List<String> selTierNames = new ArrayList<String>(selectedTiers1.size());
            for (Object oo : selectedTiers1) {
                selTierNames.add((String) oo);
            }
            List<List<String>> matchedTiers =
                tierMatcher.getMatchingTiers(allTierNames, selTierNames, tierMatching, tierCustomSeparator);
            List<String> allMatches = new ArrayList<String>();
            for (List<String> mt : matchedTiers) {
                for (String name : mt) {
                    if (!selTierNames.contains(name)) {
                        allMatches.add(name);
                    }
                }
            }

            table2.getSelectionModel().clearSelection();
            int numRows = table2.getRowCount();
            String rowValue = null;
            for (int i = 0; i < numRows; i++) {
                rowValue = (String) table2.getValueAt(i, 0);
                if (allMatches.contains(rowValue)) {
                    table2.addRowSelectionInterval(i, i);
                }
            }
        }
    }

    /**
     * Listen to both the first and second table in the pane and update both the selected state of
     * tiers in the tables and the finish button.
     */
    @Override
    public void valueChanged(ListSelectionEvent e) {
        boolean isAdjusting = e.getValueIsAdjusting();
        if (!isAdjusting) {
            updateButtonState();
        }
    }


    /**
     * Table model events are only received from {@link SelectableContentTableModel} models, there (table row) selection
     * events are not always equivalent to content selection events.
     */
    @Override
    public void tableChanged(TableModelEvent e) {
        TableModel tierModel = table1.getModel();
        if (tierModel instanceof SelectableContentTableModel) {
            if (tierMatching == CompareConstants.MATCHING.AFFIX
                || tierMatching == CompareConstants.MATCHING.PREFIX
                || tierMatching == CompareConstants.MATCHING.SUFFIX) {
                updateMatchingTiersTable();
            }
            updateButtonState();
        }
    }

    /**
     * Reply to the wizard's question for the title of this step.
     *
     * @see mpi.eudico.client.annotator.gui.multistep.Step#getStepTitle()
     */
    @Override
    public String getStepTitle() {
        return (ElanLocale.getString("CompareAnnotatorsDialog.TierSelectionStep.Title"));
    }

    /**
     * Act on the message send when entering this step after choosing 'next' in the preceding step.
     *
     * @see mpi.eudico.client.annotator.gui.multistep.Step#enterStepBackward()
     */
    @Override
    public void enterStepForward() {
        setStateFinish(false);
        checkConditions();
    }

    /**
     * Have come back after canceling an ongoing calculation. Enable buttons.
     */
    @Override
    public void enterStepBackward() {
        setStateFinish(true);
    }

    /**
     * Cache the current list of selected files in order to be able to check whether tiers have to be reloaded.
     */
    @Override
    public boolean leaveStepBackward() {
        if (selFiles != null) {
            if (oldSelFiles == null) {
                oldSelFiles = new ArrayList<File>();
            }
            oldSelFiles.clear();
            oldSelFiles.addAll(selFiles);
        }

        return true;
    }

    @Override
    public boolean leaveStepForward() {
        Object matchingProp = multiPane.getStepProperty(CompareConstants.TIER_MATCH_KEY);
        if (matchingProp == CompareConstants.MATCHING.MANUAL && !groupCompareMode) {
            // two tables, one tier per table selected
            if (table1.getSelectedRow() < 0) {
                // warning message? These error conditions should never occur
                LOG.warning("For manual matching a tier should be selected in the first table.");
                return false;
            }
            if (table2.getSelectedRow() < 0) {
                // warning
                LOG.warning("For manual matching a tier should be selected in the second table.");
                return false;
            }
            multiPane.putStepProperty(CompareConstants.TIER_NAME1_KEY, table1.getValueAt(table1.getSelectedRow(), 0));
            multiPane.putStepProperty(CompareConstants.TIER_NAME2_KEY, table2.getValueAt(table2.getSelectedRow(), 0));

            return true;
        } else if (tierMatching == CompareConstants.MATCHING.AFFIX
                   || tierMatching == CompareConstants.MATCHING.PREFIX
                   || tierMatching == CompareConstants.MATCHING.SUFFIX) { // matching is SAME_NAME or AFFIX based
            // two tables, at least one tier selected. Check here if there is at least one matching tier.
            TableModel tierModel = table1.getModel();
            if (tierModel instanceof SelectableContentTableModel tm) {
                List<Object> selectedValues = tm.getSelectedValues();

                if (selectedValues.size() == 0) {
                    LOG.warning("For affix based matching at least one tier should be selected in the (first) table.");
                    return false;
                }

                if (table2.getSelectedRowCount() == 0) {
                    LOG.warning("For affix based matching at least one tier should be detected in the second table.");
                    return false;
                }

                List<String> selectedTierNames = new ArrayList<String>(selectedValues.size());
                for (Object selected : selectedValues) {
                    selectedTierNames.add((String) selected);
                }

                multiPane.putStepProperty(CompareConstants.TIER_NAMES_KEY, selectedTierNames);
            } else {
                // something is wrong?
                LOG.warning("The type of tier selection and tier matching is unclear.");
                return false;
            }

        } else if (tierMatching == CompareConstants.MATCHING.SAME_NAME) {
            // one table, at least one row selected
            TableModel tierModel = table1.getModel();
            if (tierModel instanceof SelectableContentTableModel tm) {
                List<Object> selectedValues = tm.getSelectedValues();

                if (selectedValues.size() == 0) {
                    LOG.warning("For same name based matching at least one tier should be selected in the (first) table.");
                    return false;
                }

                List<String> selectedTierNames = new ArrayList<String>(selectedValues.size());
                for (Object selected : selectedValues) {
                    selectedTierNames.add((String) selected);
                }

                multiPane.putStepProperty(CompareConstants.TIER_NAMES_KEY, selectedTierNames);
            } else {
                // something is wrong?
                LOG.warning("The type of tier selection and tier matching is inconsistent.");
                return false;
            }
        } else if (groupCompareMode) { // implies manual selection here
            TableModel tierModel = table1.getModel();
            if (tierModel instanceof SelectableContentTableModel tm) {
                List<Object> selectedValues = tm.getSelectedValues();

                if (selectedValues.size() <= 1) {
                    LOG.warning("For manual tier selection at least two tiers should be selected in the table.");
                    return false;
                }

                List<String> selectedTierNames = new ArrayList<String>(selectedValues.size());
                for (Object selected : selectedValues) {
                    selectedTierNames.add((String) selected);
                }

                multiPane.putStepProperty(CompareConstants.TIER_NAMES_KEY, selectedTierNames);
            } else {
                // something is wrong?
                LOG.warning("The combination of tier selection and tier matching is inconsistent.");
                return false;
            }
        }

        return true;
    }

    /**
     * Answer the wizard when it asks for the preferred previous step.
     *
     * @return the identifier of the preferred previous step; when null the wizard will follow the steps in the order of
     *     declaration.
     */
    @Override
    public String getPreferredPreviousStep() {
        return super.getPreferredPreviousStep();
        /*
        if (multiPane.getStepProperty(CompareConstants.METHOD_KEY) == CompareConstants.METHOD.CLASSIC) {
             // In case of the classic method, the document selection step will
             // be skipped when moving to the first step.
            return "CompareAnnotatorsDialog.MethodSelectionStep";
        } else {
            // otherwise, stick to the predefined step order
            return null;
        }
        */
    }

    /**
     * Delegates to {@link #leaveStepForward()}; Next and Finish both move to the Progress monitoring step in which the real
     * work is done and monitored.
     *
     * @return {@link #leaveStepForward()}
     */
    @Override
    public boolean doFinish() {

        if (leaveStepForward()) {
            multiPane.nextStep();
            return false;
        }
        return false;
    }

    /**
     * Sets the row sorter to the table
     *
     * @param table the table
     * @param model the table model
     * @param sortableColumnIndices array of indices
     */
    public static void makeRowsSortable(JTable table, TableModel model, int[] sortableColumnIndices) {
        if (sortableColumnIndices.length == 0) {
            return;
        }
        TableRowSorter<TableModel> rowSorter = new TableRowSorter<TableModel>(model);
        EmptyStringComparator emptyComp = new EmptyStringComparator();
        for (int i = 0; i < sortableColumnIndices.length; i++) {
            rowSorter.setComparator(sortableColumnIndices[i], emptyComp);
        }
        table.setRowSorter(rowSorter);
    }
    
    /**
     * Action listener for 'select' all and 'select' none buttons.
     */
    private class SelectionAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			boolean selectAll = (e.getSource() == allButton);
	        TableModel tierModel = table1.getModel();
	        if (tierModel instanceof SelectableContentTableModel tm) {
	        	if (selectAll) {
	        		tm.selectAll();
	        	} else {
	        		tm.selectNone();
	        	}
	        }
		}  	
    }
}
