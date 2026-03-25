package mpi.eudico.client.annotator.gui;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.util.FavoriteColorPanel;

import javax.swing.*;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * A dialog for setting color preferences. This class is based on code duplicated in AdvancedTierOptionsDialog and
 * DisplaySettingsPanel. The new class can be used in those two, and it is already used in the ViewerPanel class.
 *
 * @author kj
 * @version 1.0
 */
public class ColorDialog /* implements ActionListener */ {

    private final Component parent;
    private final Color oldColor;
    final JColorChooser chooser;
    private final AbstractAction aa;
    private Map<String, Color> oldColors = null;
    private final FavoriteColorPanel fcp;

    /**
     * Creates a customized color chooser, which includes a panel for (persistent) favorite colors.
     *
     * @param parent the parent of this dialog
     * @param oldColor the color to start with
     */
    @SuppressWarnings("serial")
    public ColorDialog(Component parent, final Color oldColor) {
        this.parent = parent;
        this.oldColor = oldColor;

        chooser = new JColorChooser(oldColor);
        AbstractColorChooserPanel[] panels = chooser.getChooserPanels();
        AbstractColorChooserPanel[] panels2 = new AbstractColorChooserPanel[panels.length + 1];
        fcp = new FavoriteColorPanel();
        panels2[0] = fcp;

        System.arraycopy(panels, 0, panels2, 1, panels.length);

        chooser.setChooserPanels(panels2);
        // read stored favorite colors
        oldColors = Preferences.getMapOfColor("FavoriteColors", null);

        if (oldColors != null) {
            //Color[] favColors = new Color[fcp.NUM_COLS * fcp.NUM_ROWS];
            Color[] favColors = fcp.getColors(); // use the array of the panel

            for (Map.Entry<String, Color> e : oldColors.entrySet()) {
                String key = e.getKey();
                Color val = e.getValue();

                try {
                    int index = Integer.parseInt(key);
                    if (index < favColors.length) {
                        favColors[index] = val;
                    }
                } catch (NumberFormatException nfe) {
                    // ignore
                }
            }
            //fcp.setColors(favColors);
        }
        // have to provide an "OK" action listener...
        aa = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                putValue(Action.DEFAULT, chooser.getColor());
            }
        };
    }

    /**
     * Lets the user choose colors.
     *
     * @return a new color or null
     */
    public Color chooseColor() {
        Color newColor;

        JDialog cd = JColorChooser.createDialog(parent, ElanLocale.getString("ColorChooser.Title"), true, chooser, aa, null);
        cd.setVisible(true);

        // if necessary store the current favorite colors
        HashMap<String, Color> colMap = new HashMap<String, Color>();
        Color[] colors = fcp.getColors();
        for (int i = 0; i < colors.length; i++) {
            if (colors[i] != null) {
                colMap.put(String.valueOf(i), colors[i]);
            }
        }

        if (colMap.size() > 0 || oldColors != null) {
            Preferences.set("FavoriteColors", colMap, null);
        }

        newColor = (Color) aa.getValue(Action.DEFAULT);
        return newColor; // substitute default here
    }

}
