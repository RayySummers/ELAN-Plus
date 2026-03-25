package mpi.eudico.client.annotator.commands.global;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.commands.ShortcutsUtil;
import mpi.eudico.client.annotator.export.ShortCutPrinter;
import mpi.eudico.client.annotator.gui.ClosableFrame;
import mpi.eudico.client.util.SubHeaderTableCellRenderer;
import mpi.eudico.client.util.TableSubHeaderObject;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A menu action that creates and fills a table showing the current keyboard shortcut bindings.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
@SuppressWarnings("serial")
public class ShortcutsMA extends FrameMenuAction {
    private JFrame shortcutsFrame;
    private JTabbedPane shortcutPane;
    private JButton printButton;
    private JButton printAllButton;
    private JButton editShortcutsButton;

    /**
     * Creates a new ShortcutsMA instance
     *
     * @param name the name of the action
     * @param frame the containing frame
     */
    public ShortcutsMA(String name, ElanFrame2 frame) {
        super(name, frame);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == printButton) {
            int index = shortcutPane.getSelectedIndex();
            JTable tableShortcuts = (JTable) ((JScrollPane) shortcutPane.getComponentAt(index)).getViewport().getView();
            new ShortCutPrinter(tableShortcuts).startPrint();
            return;
        }

        if (e.getSource() == printAllButton) {
            ArrayList<JTable> tableList = new ArrayList<JTable>();
            for (int i = 0; i < shortcutPane.getTabCount(); i++) {
                tableList.add((JTable) ((JScrollPane) shortcutPane.getComponentAt(i)).getViewport().getView());
            }

            new ShortCutPrinter(tableList).startPrint();
            return;
        }

        if (e.getSource() == editShortcutsButton) {
            if (shortcutsFrame != null) {
                shortcutsFrame.setVisible(false);
                shortcutsFrame.dispose();
            }
            new EditShortcutsMA(ELANCommandFactory.EDIT_SHORTCUTS, frame).actionPerformed(null);
            return;
        }

        if (shortcutsFrame == null) {
            shortcutsFrame = new ClosableFrame("Shortcuts");
            shortcutsFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            shortcutPane = new JTabbedPane();

            JTable table = getNewTable();
            table.setName(ElanLocale.getString(ELANCommandFactory.COMMON_SHORTCUTS));
            shortcutPane.add(table.getName(), new JScrollPane(table));

            table = getNewTable();
            table.setName(ElanLocale.getString(ELANCommandFactory.ANNOTATION_MODE));
            shortcutPane.add(table.getName(), new JScrollPane(table));

            table = getNewTable();
            table.setName(ElanLocale.getString(ELANCommandFactory.SYNC_MODE));
            shortcutPane.add(table.getName(), new JScrollPane(table));

            table = getNewTable();
            table.setName(ElanLocale.getString(ELANCommandFactory.TRANSCRIPTION_MODE));
            shortcutPane.add(table.getName(), new JScrollPane(table));

            table = getNewTable();
            table.setName(ElanLocale.getString(ELANCommandFactory.SEGMENTATION_MODE));
            shortcutPane.add(table.getName(), new JScrollPane(table));

            shortcutsFrame.getContentPane().setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(2, 2, 4, 2);
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            shortcutsFrame.getContentPane().add(shortcutPane, gbc);

            printButton = new JButton();
            printButton.addActionListener(this);
            printAllButton = new JButton();
            printAllButton.addActionListener(this);
            editShortcutsButton = new JButton();
            editShortcutsButton.addActionListener(this);

            JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 6, 0));
            buttonPanel.add(printButton);
            buttonPanel.add(printAllButton);
            buttonPanel.add(editShortcutsButton);

            gbc.gridy = 1;
            gbc.weightx = 0;
            gbc.weighty = 0;
            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.CENTER;
            shortcutsFrame.getContentPane().add(buttonPanel, gbc);

            updateLocale();

            shortcutsFrame.pack();
            shortcutsFrame.setLocationRelativeTo(frame);
            shortcutsFrame.setVisible(true);
            shortcutsFrame.addWindowListener(new CloseListener());
        } else {
            shortcutsFrame.setVisible(true);
            shortcutsFrame.setState(JFrame.NORMAL);
            shortcutsFrame.toFront();
        }
    }

    private JTable getNewTable() {

        // replaced by Shortcut utils, in updateLocale
        DefaultTableModel model = new DefaultTableModel(0, 2);
        JTable tableShortcuts = new JTable(model);
        tableShortcuts.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
        tableShortcuts.setPreferredScrollableViewportSize(new Dimension(500, 600));
        tableShortcuts.setShowVerticalLines(false);
        tableShortcuts.getTableHeader().setReorderingAllowed(false);
        tableShortcuts.setEnabled(false);

        tableShortcuts.getColumnModel().getColumn(0).setPreferredWidth(200);
        tableShortcuts.getColumnModel().getColumn(1).setPreferredWidth(300);

        tableShortcuts.setDefaultRenderer(Object.class, new SubHeaderTableCellRenderer());

        for (int i = 0; i < tableShortcuts.getRowCount(); i++) {
            if (tableShortcuts.getValueAt(i, 0) instanceof TableSubHeaderObject) {
                tableShortcuts.setRowHeight(i, tableShortcuts.getRowHeight() + 6);
            }
        }

        return tableShortcuts;
    }

    /**
     * Updates shortcut labels and column headers.
     */
    @Override
    public void updateLocale() {
        super.updateLocale();

        if (shortcutPane != null) {
            printButton.setText(ElanLocale.getString("Menu.File.Print"));
            printAllButton.setText(ElanLocale.getString("Button.PrintAll"));
            editShortcutsButton.setText(ElanLocale.getString(ELANCommandFactory.EDIT_SHORTCUTS));

            for (int i = 0; i < shortcutPane.getTabCount(); i++) {
                String modeConstant = getConstant(shortcutPane.getTitleAt(i));
                JTable tableShortcuts = (JTable) ((JScrollPane) shortcutPane.getComponentAt(i)).getViewport().getView();

                tableShortcuts.getColumnModel()
                              .getColumn(0)
                              .setHeaderValue(ElanLocale.getString("Frame.ShortcutFrame.ColumnShortcut"));
                tableShortcuts.getColumnModel()
                              .getColumn(1)
                              .setHeaderValue(ElanLocale.getString("Frame.ShortcutFrame.ColumnDescription"));

                DefaultTableModel model = (DefaultTableModel) tableShortcuts.getModel();
                model.setRowCount(0);
                loadModel(model,
                          ShortcutsUtil.getInstance().getShortcuttableActions(modeConstant),
                          ShortcutsUtil.getInstance().getShortcutKeysOnlyIn(modeConstant));

                // special additions for inline edit box in annotation mode
                if (modeConstant == ELANCommandFactory.ANNOTATION_MODE) {
                    model.addRow(new Object[] {new TableSubHeaderObject(ElanLocale.getString("InlineEditBox.Menu.Editor")),
                                               new TableSubHeaderObject(null)});
                    model.addRow(new Object[] {ShortcutsUtil.getInstance().getDescriptionForKeyStroke(KeyStroke.getKeyStroke(
                        KeyEvent.VK_ENTER,
                        InputEvent.SHIFT_DOWN_MASK)), ElanLocale.getString("InlineEditBox.Detach")});
                    model.addRow(new Object[] {ShortcutsUtil.getInstance().getDescriptionForKeyStroke(KeyStroke.getKeyStroke(
                        KeyEvent.VK_ENTER,
                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx())),
                                               ElanLocale.getString("InlineEditBox.Commit")});
                    model.addRow(new Object[] {ShortcutsUtil.getInstance().getDescriptionForKeyStroke(KeyStroke.getKeyStroke(
                        KeyEvent.VK_ESCAPE,
                        0)), ElanLocale.getString("InlineEditBox.Cancel")});
                    model.addRow(new Object[] {ShortcutsUtil.getInstance().getDescriptionForKeyStroke(KeyStroke.getKeyStroke(
                        KeyEvent.VK_U,
                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx())),
                                               ElanLocale.getString("InlineEditBox.ToggleSuggestPanel")});
                }
            }

            // replace by ShortCutUtils

            if (shortcutsFrame != null) {
                shortcutsFrame.setTitle(ElanLocale.getString("Menu.View.ShortcutsDialog"));
                shortcutsFrame.repaint(); //if tableShortcuts != null, then shortcutsFrame != null too
            }
        }
    }

    private String getConstant(String tabName) {
        String constant = null;

        if (tabName.equals(ElanLocale.getString(ELANCommandFactory.COMMON_SHORTCUTS))) {
            constant = ELANCommandFactory.COMMON_SHORTCUTS;
        } else if (tabName.equals(ElanLocale.getString(ELANCommandFactory.ANNOTATION_MODE))) {
            constant = ELANCommandFactory.ANNOTATION_MODE;
        } else if (tabName.equals(ElanLocale.getString(ELANCommandFactory.TRANSCRIPTION_MODE))) {
            constant = ELANCommandFactory.TRANSCRIPTION_MODE;
        } else if (tabName.equals(ElanLocale.getString(ELANCommandFactory.SYNC_MODE))) {
            constant = ELANCommandFactory.SYNC_MODE;
        } else if (tabName.equals(ElanLocale.getString(ELANCommandFactory.SEGMENTATION_MODE))) {
            constant = ELANCommandFactory.SEGMENTATION_MODE;
        }
        return constant;
    }

    private void loadModel(DefaultTableModel model, Map<String, List<String>> curShorts,
                           Map<String, KeyStroke> shortcutMap) {

        Iterator<Map.Entry<String, List<String>>> csIt = curShorts.entrySet().iterator();
        String key;
        String shortcutId;
        KeyStroke stroke;
        List<String> ids;

        while (csIt.hasNext()) {
            key = csIt.next().getKey();
            ids = curShorts.get(key);
            boolean categoryAdded = false;
            for (int i = 0; i < ids.size(); i++) {
                shortcutId = ids.get(i);
                stroke = shortcutMap.get(shortcutId);

                if (stroke != null) { // only show the actions with a shortcut in this table
                    if (!categoryAdded) {
                        model.addRow(new Object[] {new TableSubHeaderObject(ElanLocale.getString(key)),
                                                   new TableSubHeaderObject(null)});
                        categoryAdded = true;
                    }
                    model.addRow(new Object[] {ShortcutsUtil.getInstance().getDescriptionForKeyStroke(stroke),
                                               ShortcutsUtil.getInstance().getDescriptionForAction(shortcutId)});
                }
            }
        }
    }

    //#####################################################

    /**
     * Listener for closing events.
     *
     * @author Han Sloetjes
     * @version 1.0
     */
    private class CloseListener extends WindowAdapter {
        /**
         * If the shortcut frame is closed, set it to null.
         *
         * @see java.awt.event.WindowAdapter#windowClosed(java.awt.event.WindowEvent)
         */
        @Override
        public void windowClosed(WindowEvent e) {
            shortcutsFrame.removeWindowListener(this);
            shortcutsFrame = null;
        }
    }
}
