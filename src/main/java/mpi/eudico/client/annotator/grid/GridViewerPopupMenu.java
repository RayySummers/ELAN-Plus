package mpi.eudico.client.annotator.grid;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.export.ExportGridTable;
import mpi.eudico.client.annotator.util.ClientLogger;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;

/**
 * Popupmenu for the AnnotationTable, for changing font size and layout of the table. Extracted from GridViewer on Jun 30,
 * 2004
 *
 * @author Alexander Klassmann
 * @version Jun 30, 2004
 */
@SuppressWarnings("serial")
public class GridViewerPopupMenu extends JPopupMenu implements ActionListener,
                                                               ClientLogger {
    /**
     * table identifier
     */
    final protected AnnotationTable table;
    final private JMenu fontMenu;
    final private JMenuItem toggleMenuItem;
    final private JMenuItem exportMenuItem;
    final private ButtonGroup fontSizeBG;
    private final ButtonGroup formatBG;
    private final JMenu formatMenu;
    private final JRadioButtonMenuItem hhmmssmsMI;
    private final JRadioButtonMenuItem timecodePalMI;
    private final JRadioButtonMenuItem timecodePal50MI;
    private final JRadioButtonMenuItem timecodeNtscMI;
    private final JRadioButtonMenuItem msMI;
    private final JRadioButtonMenuItem secMI;

    private int fontSize;
    /**
     * listeners that want to be notified of menu events
     */
    private List<ActionListener> actionListeners;

    /**
     * Constructor.
     *
     * @param table the table this popup menu applies to
     */
    public GridViewerPopupMenu(AnnotationTable table) {
        this.table = table;

        fontSizeBG = new ButtonGroup();
        fontMenu = new JMenu(ElanLocale.getString("Menu.View.FontSize"));

        JRadioButtonMenuItem fontRB;

        for (int element : Constants.FONT_SIZES) {
            fontRB = new JRadioButtonMenuItem(String.valueOf(element));
            fontRB.setActionCommand("font" + element);
            if (table.getFont().getSize() == element) {
                fontRB.setSelected(true);
            }
            fontRB.addActionListener(this);
            fontSizeBG.add(fontRB);
            fontMenu.add(fontRB);
        }

        //add timeformat toggle
        toggleMenuItem = new JMenuItem(ElanLocale.getString("Menu.Options.TimeFormat"));
        toggleMenuItem.setActionCommand("TOGGLETIMEFORMAT");
        toggleMenuItem.addActionListener(this);

        formatBG = new ButtonGroup();
        formatMenu = new JMenu();
        hhmmssmsMI = new JRadioButtonMenuItem();
        hhmmssmsMI.setSelected(true);
        hhmmssmsMI.setActionCommand(Constants.HHMMSSMS_STRING);
        timecodePalMI = new JRadioButtonMenuItem();
        timecodePalMI.setActionCommand(Constants.PAL_STRING);
        timecodePal50MI = new JRadioButtonMenuItem();
        timecodePal50MI.setActionCommand(Constants.PAL_50_STRING);
        timecodeNtscMI = new JRadioButtonMenuItem();
        timecodeNtscMI.setActionCommand(Constants.NTSC_STRING);
        msMI = new JRadioButtonMenuItem();
        msMI.setActionCommand(Constants.MS_STRING);
        secMI = new JRadioButtonMenuItem();
        secMI.setActionCommand(Constants.SSMS_STRING);
        formatBG.add(hhmmssmsMI);
        formatBG.add(timecodePalMI);
        formatBG.add(timecodePal50MI);
        formatBG.add(timecodeNtscMI);
        formatBG.add(msMI);
        formatBG.add(secMI);
        formatMenu.add(hhmmssmsMI);
        formatMenu.add(timecodePalMI);
        formatMenu.add(timecodePal50MI);
        formatMenu.add(timecodeNtscMI);
        formatMenu.add(msMI);
        formatMenu.add(secMI);
        hhmmssmsMI.addActionListener(this);
        timecodePalMI.addActionListener(this);
        timecodePal50MI.addActionListener(this);
        timecodeNtscMI.addActionListener(this);
        msMI.addActionListener(this);
        secMI.addActionListener(this);

        exportMenuItem = new JMenuItem(ElanLocale.getString("Frame.GridFrame.ExportTableAsTab"));
        exportMenuItem.setActionCommand("EXPORT");
        exportMenuItem.addActionListener(this);
    }

    /**
     * The menu action event handling.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        String actionCommand = e.getActionCommand();

        if (actionCommand.equals("TOGGLETIMEFORMAT")) {
            table.toggleTimeFormat();
        } else if (actionCommand.equals(Constants.HHMMSSMS_STRING)) {
            table.setTimeFormat(actionCommand);
        } else if (actionCommand.equals(Constants.PAL_STRING)) {
            table.setTimeFormat(actionCommand);
        } else if (actionCommand.equals(Constants.PAL_50_STRING)) {
            table.setTimeFormat(actionCommand);
        } else if (actionCommand.equals(Constants.NTSC_STRING)) {
            table.setTimeFormat(actionCommand);
        } else if (actionCommand.equals(Constants.MS_STRING)) {
            table.setTimeFormat(actionCommand);
        } else if (actionCommand.equals(Constants.SSMS_STRING)) {
            table.setTimeFormat(actionCommand);
        } else if (actionCommand.equals("EXPORT")) {
            ExportGridTable exporter = new ExportGridTable();
            exporter.exportTableAsTabDelimitedText(table);
        } else if (actionCommand.indexOf("font") != -1) {
            int index = actionCommand.indexOf("font") + 4;

            try {
                table.setFontSize(Integer.parseInt(actionCommand.substring(index)));
                table.repaint();
            } catch (Exception ex) {
                LOG.log(Level.INFO, "Could not set font size", ex);
            }
        } else {
            table.setColumnVisible(actionCommand, ((JCheckBoxMenuItem) e.getSource()).isSelected());
            table.adjustAnnotationColumns();
        }
        table.doLayout();

        if (actionListeners != null) {
            for (int i = 0; i < actionListeners.size(); i++) {
                actionListeners.get(i).actionPerformed(e);
            }
        }
    }

    /**
     * Prepares the layout
     */
    protected void makeLayout() {
        removeAll();
        for (int i = 0; i < table.getColumnCount(); i++) {
            TableColumn column = table.getColumnModel().getColumn(i);
            String columnName = (String) column.getIdentifier();
            //Column ANNOTATION should not be removable and thus not in selection menu
            if (!columnName.equals(GridViewerTableModel.TIMEPOINT)
                && !columnName.equals(GridViewerTableModel.COUNT)
                && !columnName.equals(GridViewerTableModel.ANNOTATION)) {
                JMenuItem menuItem = new JCheckBoxMenuItem(column.getHeaderValue().toString());
                menuItem.setActionCommand(columnName);
                menuItem.setSelected(table.isColumnVisible(columnName));
                menuItem.addActionListener(this);
                add(menuItem);
            }
        }
        addSeparator();
        add(fontMenu);
        addSeparator();
        //add(toggleMenuItem);
        add(formatMenu);
        add(exportMenuItem);
        updateLocale();
    }

    /**
     * Updates the menu when it pops up.
     *
     * @see java.awt.Component#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean b) {
        if (b) {
            makeLayout();
        }
        super.setVisible(b);
    }

    /**
     * Returns the current font size.
     *
     * @return the current font size
     */
    public int getFontSize() {
        return fontSize;
    }

    /**
     * Sets the font size.
     *
     * @param size the new font size
     */
    public void setFontSize(int size) {
        fontSize = size;
        if (fontSizeBG != null) {
            Enumeration<AbstractButton> en = fontSizeBG.getElements();
            JMenuItem item;
            String value;
            while (en.hasMoreElements()) {
                item = (JMenuItem) en.nextElement();
                value = item.getText();
                try {
                    int v = Integer.parseInt(value);
                    if (v == fontSize) {
                        item.setSelected(true);
                        if (table != null) {
                            table.setFontSize(size);
                            table.repaint();
                        }
                        break;
                    }
                } catch (NumberFormatException nfe) {
                    //// do nothing
                }
            }
        }
    }

    /**
     * Sets the time format, updates the popup menu items. Assumes that the table is notified otherwise.
     *
     * @param format time format, hh:mm:ss:ms, pal, ntsc, ms
     */
    public void setTimeFormat(String format) {
        if (format.equals(Constants.HHMMSSMS_STRING) || format.equals(GridViewerTableModel.HHMMSSsss)) {
            hhmmssmsMI.setSelected(true);
        } else if (format.equals(Constants.PAL_STRING)) {
            timecodePalMI.setSelected(true);
        } else if (format.equals(Constants.PAL_50_STRING)) {
            timecodePal50MI.setSelected(true);
        } else if (format.equals(Constants.NTSC_STRING)) {
            timecodeNtscMI.setSelected(true);
        } else if (format.equals(Constants.SSMS_STRING)) {
            secMI.setSelected(true);
        } else if (format.equals(Constants.MS_STRING) || format.equals(GridViewerTableModel.MILLISECONDS)) {
            msMI.setSelected(true);
        }
    }

    /**
     * Apply localized strings to the menu items.
     */
    public void updateLocale() {
        fontMenu.setText(ElanLocale.getString("Menu.View.FontSize"));
        toggleMenuItem.setText(ElanLocale.getString("Menu.Options.TimeFormat"));
        exportMenuItem.setText(ElanLocale.getString("Frame.GridFrame.ExportTableAsTab"));

        formatMenu.setText(ElanLocale.getString("TimeCodeFormat.Label.TimeFormat"));
        hhmmssmsMI.setText(ElanLocale.getString("TimeCodeFormat.TimeCode"));
        timecodePalMI.setText(ElanLocale.getString("TimeCodeFormat.TimeCode.SMPTE.PAL"));
        timecodePal50MI.setText(ElanLocale.getString("TimeCodeFormat.TimeCode.SMPTE.PAL50"));
        timecodeNtscMI.setText(ElanLocale.getString("TimeCodeFormat.TimeCode.SMPTE.NTSC"));
        msMI.setText(ElanLocale.getString("TimeCodeFormat.MilliSec"));
        secMI.setText(ElanLocale.getString("TimeCodeFormat.Seconds"));
    }

    /**
     * Add action listener that will be notified of menu item actions <b>after</b> this class has dealt with the event.
     *
     * @param listener the listener
     */
    public void addActionListener(ActionListener listener) {
        if (actionListeners == null) {
            actionListeners = new ArrayList<ActionListener>(2);
        }
        actionListeners.add(listener);
    }

}
