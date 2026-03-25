package mpi.eudico.client.annotator.export;

import mpi.eudico.client.annotator.ElanLocale;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;

/**
 * Pane to show the exported tiers and thier annotations to the server
 */
@SuppressWarnings("serial")
public class ExportedResultPane extends JPanel {

    private JLabel messageLabel;
    private JTable resultTable;
    private JScrollPane scrollPane;
    private DefaultTableModel model;
    private final int tableW = 360;

    /**
     * Constructor
     *
     * @param exportedData the map containing exported data
     */
    public ExportedResultPane(Map<String, Integer> exportedData) {
        initComponents();
        fillTable(exportedData);
    }

    /**
     * initialize the UI components
     */
    private void initComponents() {
        messageLabel = new JLabel("<html>"
                                  + ElanLocale.getString("ExportJSONToServerResult.Success.Info")
                                  + "<br>"
                                  + ElanLocale.getString("ExportJSONToServerResult.Columns")
                                  + "</html>");
        model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column <= 0;
            }
        };

        model.setColumnCount(2);

        resultTable = new JTable(model);
        resultTable.setTableHeader(null);

        scrollPane = new JScrollPane(resultTable);
        scrollPane.setPreferredSize(new Dimension(tableW, 100));

        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 6, 10, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        add(messageLabel, gbc);

        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        add(scrollPane, gbc);

    }

    /**
     * fill the exported result in a table with two colums column 1: tier names , column 2: number of annotations
     *
     * @param exportedData
     */
    private void fillTable(Map<String, Integer> exportedData) {
        if (!exportedData.isEmpty()) {
            for (Map.Entry<String, Integer> entry : exportedData.entrySet()) {
                //listOfExport = listOfExport + entry.getKey() + " : " + entry.getValue() + '\n';

                model.addRow(new Object[] {entry.getKey(), entry.getValue()});
            }
        }
    }
}
