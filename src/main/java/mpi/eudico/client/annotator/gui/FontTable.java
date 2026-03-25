package mpi.eudico.client.annotator.gui;

import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.ResourceUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;

import static java.nio.charset.StandardCharsets.UTF_8;


/**
 * A table showing all characters of a certain unicode block in a certain font.
 */
@SuppressWarnings("serial")
class FontTable extends JFrame implements ClientLogger  {
    public static final String RESOURCE_UNICODE_DATA_TXT = "/mpi/eudico/client/annotator/resources/UnicodeData.txt";
    /**
     * The start or lowest code point
     */
    int _start;

    /**
     * The end or highest code points
     */
    int _end;

    /**
     * The name of the code page
     */
    String _codepgname;

    /**
     * The font
     */
    Font _font;

    DefaultTableModel _dataModel = null;
    JTable table = null;

    /**
     * Creates a new FontTable instance
     *
     * @param start the start code point
     * @param end the end code point
     * @param name the name of the page
     * @param font the fontto be used for display
     */
    public FontTable(int start, int end, String name, Font font) {
        super("Font Browser for Codepage:" + name);
        reload(start, end, name, font);
        setSize(500, 700);
    }

    /**
     * Reloads data for the table.
     *
     * @param start the start code point
     * @param end the end code point
     * @param name the name of the code page
     * @param font the font to use for display
     */
    public void reload(int start, int end, String name, Font font) {
        setTitle("Font Browser for Codepage:" + name);
        _start = start;
        _end = end;
        _codepgname = name;
        _font = font;

        getContentPane().removeAll();

        Object[][] data = {{" ", " ", " "}};
        String[] columnNames = {"Font", "Unicode Hex", "Display Name"};
        _dataModel = null;
        _dataModel = new DefaultTableModel(data, columnNames);
        table = new JTable(_dataModel);

        JScrollPane scrollpane = new JScrollPane(table);
        TableColumn column = null;
        table.setRowHeight(20);
        loadTable();
        column = table.getColumnModel().getColumn(0);
        column.setMaxWidth(100);
        column.sizeWidthToFit();
        column = table.getColumnModel().getColumn(1);
        column.setMaxWidth(130);
        column.sizeWidthToFit();
        column = table.getColumnModel().getColumn(2);
        //column.setMaxWidth(420);
        column.sizeWidthToFit();

        table.setFont(_font);
        getContentPane().add(scrollpane);
        getContentPane().validate();
        //setSize(500, 700);
    }

    private void loadTable() {

        try (BufferedReader cdTable = new BufferedReader(new InputStreamReader(ResourceUtil.getResourceAsStream(
            RESOURCE_UNICODE_DATA_TXT), UTF_8))) {
            String s;

            while ((s = cdTable.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(s, ";");
                String tmpe = st.nextToken();
                int uni = Integer.parseInt(tmpe, 16);
                String desc = st.nextToken();

                if ((uni >= _start) && (uni <= _end)) {
                    Vector<String> v = new Vector<String>();

                    char[] chars = Character.toChars(uni);
                    v.add(new String(chars));
                    v.add(Integer.toHexString(uni));
                    v.add(desc);
                    _dataModel.addRow(v);
                }
            }
        } catch (Exception ee) {
            LOG.log(Level.WARNING, "Some issue occurred while processing the file.", ee);
        }
    }

    /**
     * Sets the font to use for display.
     *
     * @param f the font to use
     */
    @Override
    public void setFont(Font f) {
        table.setFont(f);
    }

}
