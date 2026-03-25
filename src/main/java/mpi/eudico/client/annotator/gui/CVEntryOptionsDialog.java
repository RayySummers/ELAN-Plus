package mpi.eudico.client.annotator.gui;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.util.FavoriteColorPanel;
import mpi.eudico.util.CVEntry;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;


/**
 * A dialog to set or change some more attributes of an entry in a controlled vocabulary. At this moment these are just a few
 * user preferences.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
@SuppressWarnings("serial")
public class CVEntryOptionsDialog extends JDialog implements ActionListener,
                                                             KeyListener {
    private final CVEntry origEntry;
    private CVEntry copyEntry;
    private JPanel colorPreviewPanel;
    private JButton colorButton;
    private JButton resetColorButton;

    private JTextField keyTextField;
    private JButton revertKeyButton; // revert to previous?
    private JButton resetKeyButton;
    private JButton changeButton;
    private JButton cancelButton;
    private int newKeyCode = -1;
    private final int langIndex;

    /**
     * Creates a new Instance.
     *
     * @param owner the owner window
     * @param modal modal flag
     * @param origEntry the entry to change
     * @param langIndex the language index
     *
     * @throws HeadlessException if the dialog is created in a headless environment
     */
    public CVEntryOptionsDialog(Dialog owner, boolean modal, CVEntry origEntry, int langIndex) throws
                                                                                               HeadlessException {
        this(owner, "", modal, origEntry, langIndex);
    }

    /**
     * Creates a new Instance.
     *
     * @param owner the owner window
     * @param title the dialog title
     * @param modal modal flag
     * @param origEntry the entry to change
     * @param langIndex the language index
     *
     * @throws HeadlessException if the dialog is created in a headless environment
     * @throws IllegalArgumentException if the entry is null
     */
    public CVEntryOptionsDialog(Dialog owner, String title, boolean modal, CVEntry origEntry, int langIndex)
        throws HeadlessException {
        super(owner, title, modal);

        if (origEntry == null) {
            throw new IllegalArgumentException("The entry is null");
        }
        this.langIndex = langIndex;
        this.origEntry = origEntry;
        copyEntry = new CVEntry(origEntry);
        initComponents();
        postInit();
    }

    /**
     * Initializes the UI components.
     */
    private void initComponents() {
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        getContentPane().setLayout(new GridBagLayout());

        Insets insets = new Insets(2, 6, 2, 6);

        JPanel prefPanel = new JPanel();

        JLabel titleLabel = new JLabel();
        titleLabel.setText(ElanLocale.getString("EditCVDialog.Label.Value") + ": " + copyEntry.getValue(langIndex));
        // use index 0 because the preferences are attached to that value.
        titleLabel.setFont(titleLabel.getFont().deriveFont((float) 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.weightx = 1.0;
        getContentPane().add(titleLabel, gbc);

        prefPanel.setBorder(new TitledBorder(ElanLocale.getString("EditCVDialog.Label.EntryPreferences")));
        prefPanel.setLayout(new GridBagLayout());

        JLabel colorLabel = new JLabel(ElanLocale.getString("EditCVDialog.Label.EntryColor"));
        colorPreviewPanel = new JPanel();
        colorPreviewPanel.setBorder(new LineBorder(Color.GRAY, 1));
        colorPreviewPanel.setBackground(Color.WHITE);
        colorButton = new JButton(ElanLocale.getString("Button.Browse"));
        colorButton.addActionListener(this);
        resetColorButton = new JButton(ElanLocale.getString("Button.Default"));
        resetColorButton.addActionListener(this);
        colorPreviewPanel.setPreferredSize(new Dimension(colorButton.getPreferredSize().height,
                                                         colorButton.getPreferredSize().height));
        colorPreviewPanel.setMinimumSize(new Dimension(colorButton.getPreferredSize().height,
                                                       colorButton.getPreferredSize().height));

        if (copyEntry.getPrefColor() != null) {
            colorPreviewPanel.setBackground(copyEntry.getPrefColor());
        }

        JLabel keyLabel = new JLabel(ElanLocale.getString("EditCVDialog.Label.EntryKey"));
        keyTextField = new JTextField(10);
        keyTextField.setEditable(true);

        if (copyEntry.getShortcutKeyCode() >= 0) {
            keyTextField.setText(KeyEvent.getKeyText(copyEntry.getShortcutKeyCode())); // convert code to string
        }

        keyTextField.addKeyListener(this);

        revertKeyButton = new JButton(ElanLocale.getString("EditCVDialog.Label.EntryKeyRevert"));
        revertKeyButton.addActionListener(this);
        resetKeyButton = new JButton(ElanLocale.getString("Button.Default"));
        resetKeyButton.addActionListener(this);

        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = insets;
        prefPanel.add(colorLabel, gbc);
        gbc.gridx = 1;
        prefPanel.add(colorPreviewPanel, gbc);
        gbc.gridx = 3;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.weighty = 0;
        prefPanel.add(colorButton, gbc);
        gbc.gridx = 4;
        prefPanel.add(resetColorButton, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;

        prefPanel.add(keyLabel, gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        prefPanel.add(keyTextField, gbc);
        gbc.gridx = 3;
        gbc.gridwidth = 1;
        //gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        prefPanel.add(revertKeyButton, gbc);
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 4;
        prefPanel.add(resetKeyButton, gbc);

        gbc = new GridBagConstraints();
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        getContentPane().add(prefPanel, gbc);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 6, 2));
        changeButton = new JButton();
        changeButton.setText(ElanLocale.getString("Button.Apply"));
        changeButton.addActionListener(this);
        cancelButton = new JButton();
        cancelButton.setText(ElanLocale.getString("Button.Cancel"));
        cancelButton.addActionListener(this);
        buttonPanel.add(changeButton);
        buttonPanel.add(cancelButton);

        gbc = new GridBagConstraints();
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.insets = insets;
        getContentPane().add(buttonPanel, gbc);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                CVEntryOptionsDialog.this.copyEntry = null;
                doClose();
            }
        });
    }

    /**
     * Pack, size and set location.
     */
    private void postInit() {
        pack();
        /*
           int w = 300;
           int h = 200;
           setSize((getSize().width < w) ? w : getSize().width,
               (getSize().height < h) ? h : getSize().height);
         */
        setResizable(false);
        setLocationRelativeTo(getParent());
    }

    private void selectColor() {
        Color oldColor = copyEntry.getPrefColor();
        if (oldColor == null) {
            oldColor = Color.WHITE;
        }

        final JColorChooser chooser = new JColorChooser(oldColor);
        AbstractColorChooserPanel[] panels = chooser.getChooserPanels();
        AbstractColorChooserPanel[] panels2 = new AbstractColorChooserPanel[panels.length + 1];
        FavoriteColorPanel fcp = new FavoriteColorPanel();
        panels2[0] = fcp;

        System.arraycopy(panels, 0, panels2, 1, panels.length);

        chooser.setChooserPanels(panels2);
        // read stored favorite colors
        Map<String, Color> oldColors = null;
        oldColors = Preferences.getMapOfColor("FavoriteColors", null);
        if (oldColors != null) {
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
        AbstractAction aa = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                putValue(Action.DEFAULT, chooser.getColor());
            }
        };

        JDialog cd = JColorChooser.createDialog(this, ElanLocale.getString("ColorChooser.Title"), true, chooser, aa, null);
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

        Color newColor = (Color) aa.getValue(Action.DEFAULT);

        //Color newColor = JColorChooser.showDialog(this, "", oldColor);

        if (newColor != null) {
            colorPreviewPanel.setBackground(newColor);
            copyEntry.setPrefColor(newColor);
        }
    }

    /**
     * Reverts to the previous value of the shortcut key.
     */
    private void revertKey() {
        if (origEntry.getShortcutKeyCode() == -1) {
            copyEntry.setShortcutKeyCode(-1);
            keyTextField.setText("");
        } else {
            copyEntry.setShortcutKeyCode(origEntry.getShortcutKeyCode());
            keyTextField.setText(KeyEvent.getKeyText(origEntry.getShortcutKeyCode()));
        }
    }

    private void doClose() {
        setVisible(false);
        dispose();
    }

    private boolean isEntryChanged() {
        if (copyEntry.getPrefColor() != null && origEntry.getPrefColor() == null) {
            return true;
        }
        if (copyEntry.getPrefColor() == null && origEntry.getPrefColor() != null) {
            return true;
        }
        if (copyEntry.getPrefColor() != null && !copyEntry.getPrefColor().equals(origEntry.getPrefColor())) {
            return true;
        }
        return copyEntry.getShortcutKeyCode() != origEntry.getShortcutKeyCode();
    }

    /**
     * The action event handling
     *
     * @param e the event
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == changeButton) {
            if (!isEntryChanged()) {
                copyEntry = null;
            }
            doClose();
        } else if (e.getSource() == cancelButton) {
            copyEntry = null;
            doClose();
        } else if (e.getSource() == colorButton) {
            selectColor();
        } else if (e.getSource() == resetColorButton) {
            colorPreviewPanel.setBackground(Color.WHITE);
            copyEntry.setPrefColor(null);
        } else if (e.getSource() == revertKeyButton) {
            revertKey();
        } else if (e.getSource() == resetKeyButton) {
            newKeyCode = -1;
            copyEntry.setShortcutKeyCode(newKeyCode);
            keyTextField.setText("");
        }
    }

    /**
     * Returns the, possibly modified, entry copy.
     *
     * @return the entry or null (canceled or no change)
     */
    public CVEntry getCVEntry() {
        return copyEntry;
    }

    /**
     * The key pressed handling, store the key code, ignore modifiers
     *
     * @param ke the event
     */
    @Override
    public void keyPressed(KeyEvent ke) {
        keyTextField.setText("");
        newKeyCode = ke.getKeyCode();
    }

    /**
     * The key released handling, set the text in the field.
     *
     * @param ke the event
     */
    @Override
    public void keyReleased(KeyEvent ke) {
        if (newKeyCode > -1) {
            copyEntry.setShortcutKeyCode(newKeyCode);
            keyTextField.setText(KeyEvent.getKeyText(newKeyCode));
        }
    }

    /**
     * The key typed handling. Stub
     *
     * @param ke the event
     */
    @Override
    public void keyTyped(KeyEvent ke) {
        // stub
    }
}
