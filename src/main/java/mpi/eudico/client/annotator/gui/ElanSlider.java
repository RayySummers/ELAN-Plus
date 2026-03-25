package mpi.eudico.client.annotator.gui;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.ElanLocaleListener;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.ViewerManager2;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Component which contains a slider with a name label and a value label.
 */

@SuppressWarnings("serial")
public class ElanSlider extends JComponent implements ElanLocaleListener {
    //private Dimension ELANSLIDERDIMENSION = new Dimension(200, 80);
    private final JSlider slider;
    private JTextField tfValue;
    private JLabel namelabel;
    private int min;
    private int max;
    private int initvalue;
    private String name;
    private ViewerManager2 vm;
    private List<Character> allowedList;

    /**
     * Creates a new ElanSlider instance with default slider values.
     */
    public ElanSlider() {
        super();
        slider = new JSlider();
        init();
    }

    /**
     * Creates a new ElanSlider instance
     *
     * @param name a label to display
     * @param min the minimum value of the slider
     * @param max the maximum value of the slider
     * @param initvalue the initial value of the slider
     * @param theVM the viewer manager
     */
    public ElanSlider(String name, int min, int max, int initvalue, ViewerManager2 theVM) {
        super();
        this.min = min;
        this.max = max;
        this.initvalue = initvalue;
        this.name = name;
        this.vm = theVM;
        slider = new JSlider(min, max, initvalue);
        //addComponentListener(new ElanSliderComponentListener(this));
        init();
    }

    private void init() {

        namelabel = new JLabel(name + ": ");

        tfValue = new JTextField("" + initvalue);

        //React when the user presses Enter.
        tfValue.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "check");
        tfValue.getActionMap().put("check", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean bGoodValue = true;
                String str = tfValue.getText();

                List<Character> vecAllowed = getAllowed();

                if (!str.equals("")) {
                    int str_length = str.length();

                    for (int i = 0; i < str_length; i++) {
                        Character objChar = Character.valueOf(str.charAt(i));

                        if (!vecAllowed.contains(objChar)) {
                            bGoodValue = false;

                            break;
                        }
                    }

                    if (bGoodValue) {
                        int intValue = Integer.parseInt(str);

                        //no need for check < 0 because '-' is not in getAllowed vector
                        if (intValue > max) {
                            //intValue = max;
                            //tfValue.setText(""+intValue);
                            handleInputError();
                        } else {
                            slider.setSnapToTicks(false);
                            slider.setValue(intValue);
                            slider.setSnapToTicks(true);
                        }
                    } else {
                        handleInputError();
                    }
                }
            }
        });

        tfValue.setText("" + initvalue);

        String maxString = String.valueOf(max);
        Dimension si = new Dimension(10 * (maxString.length() + 1), 20);
        tfValue.setMinimumSize(si);
        tfValue.setPreferredSize(si);

        slider.setMajorTickSpacing(max / 2);
        slider.setMinorTickSpacing((max - min) / 20);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setSnapToTicks(true);

        if (vm != null) {
            ElanLocale.addElanLocaleListener(vm.getTranscription(), this);
        }
        updateLocale();

        slider.addChangeListener(new ElanSliderChangeListener(this));

        initComponents();
    }

    private void handleInputError() {
        Toolkit.getDefaultToolkit().beep();
        tfValue.selectAll();
    }

    private List<Character> getAllowed() {
        if (allowedList == null) {
            allowedList = new ArrayList<Character>(10);

            allowedList.add(Character.valueOf('1'));
            allowedList.add(Character.valueOf('2'));
            allowedList.add(Character.valueOf('3'));
            allowedList.add(Character.valueOf('4'));
            allowedList.add(Character.valueOf('5'));
            allowedList.add(Character.valueOf('6'));
            allowedList.add(Character.valueOf('7'));
            allowedList.add(Character.valueOf('8'));
            allowedList.add(Character.valueOf('9'));
            allowedList.add(Character.valueOf('0'));
        }

        return allowedList;
    }

    /**
     * Returns the name of the slider.
     *
     * @return the name
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Sets the name label.
     *
     * @param strName the new label
     */
    public void setNameLabel(String strName) {
        namelabel.setText(strName + ": ");
    }

    /**
     * Returns the current slider value.
     *
     * @return the slider value
     */
    public int getValue() {
        return slider.getValue();
    }

    /**
     * Sets the slider value.
     *
     * @param value the new value for the slider.
     */
    public void setValue(int value) {
        slider.setValue(value);
    }

    /**
     * Returns the value of the textfield. This can be different from the value of the slider in the case the user enters a
     * value in the textfield without confirming with the enter key.
     *
     * @return the value of the textfield
     */
    public int getTextFieldValue() {
        int v = slider.getValue();

        try {
            v = Integer.parseInt(tfValue.getText());
        } catch (NumberFormatException nfe) {
            System.out.println("Play around selection: invalid input");
        }

        return v;
    }

    /**
     * Calls {@link #validate()}. Version jan 2005: obsolete; the layout manager calculates the position of the components
     */
    public void placeComponents() {
        /*
        int width = getWidth();

        namelabel.setBounds(0, 0, width, 10);
        tfValue.setBounds(0, 25, 40, 20);

        //if (width - 40 <= 0) {
        //    width = 200;
        //}
        slider.setBounds(40, 15, width - 40, 50);
        */
        validate();

    }

    /**
     * 01-05: This component now uses a GridBagLayout to layout it's components.
     *
     * @see #placeComponents()
     */
    private void initComponents() {
        /*
        setLayout(null);

        add(namelabel);
        add(tfValue);
        add(slider);
        */
        setLayout(new GridBagLayout());

        Insets insets = new Insets(2, 2, 2, 2);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.ipady = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = insets;

        add(namelabel, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = insets;

        add(tfValue, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = insets;

        add(slider, gbc);
    }


    /**
     * When a slider is being dragged the contemplating actions are taken
     *
     * @param elanslider The slider on which the operation took place
     * @param value The value of the slider after user interaction
     */
    private void updateSlider(ElanSlider elanslider, int value) {
        String name = elanslider.getName();

        if (name != null) {
            if (name.equals("ELANSLIDERRATE")) {
                vm.getMasterMediaPlayer().setRate((float) value / 100);
                Preferences.set("MediaControlRate", Float.valueOf((float) value / 100), vm.getTranscription(), false, false);
            } else if (name.equals("ELANSLIDERVOLUME")) {
                vm.getVolumeManager().setMasterVolume((float) value / 100);
                Preferences.set("MediaControlVolume",
                                Float.valueOf((float) value / 100),
                                vm.getTranscription(),
                                false,
                                false);
            }
        }
    }

    /**
     * method from ElanLocaleListener
     */
    @Override
    public void updateLocale() {
        if (name.equals("ELANSLIDERRATE")) {
            setNameLabel(ElanLocale.getString("MediaPlayerControlPanel.ElanSlider.Rate"));
        } else if (name.equals("ELANSLIDERVOLUME")) {
            setNameLabel(ElanLocale.getString("MediaPlayerControlPanel.ElanSlider.Volume"));
        }
    }


    //////////////////////////////////////////////////////////////
    //
    // Listener for ElanSlider
    //
    //////////////////////////////////////////////////////////////
    private class ElanSliderChangeListener implements ChangeListener {
        ElanSlider elanslider;

        /**
         * Creates a new ElanSliderChangeListener instance
         *
         * @param elanslider the slider to listen to
         */
        ElanSliderChangeListener(ElanSlider elanslider) {
            this.elanslider = elanslider;
        }

        /**
         * Updates the UI after a change in slider value.
         *
         * @param e the change event
         */
        @Override
        public void stateChanged(ChangeEvent e) {
            int value = ((JSlider) e.getSource()).getValue();
            if (!"ELANSLIDERRATE".equals(elanslider.getName())) {
                updateSlider(elanslider, value);
            } else if (!slider.getValueIsAdjusting()) {
                updateSlider(elanslider, value);
            }

            tfValue.setText("" + value);
        }
    }
    //end of ElanSliderChangeListener

    /**
     * Component listener for the {@code ElanSlider}.
     */
    @SuppressWarnings("unused")
    private class ElanSliderComponentListener implements ComponentListener {
        ElanSlider slider;

        //in real usage probably no parameters, but class variables
        ElanSliderComponentListener(ElanSlider slider) {
            this.slider = slider;
        }

        @Override
        public void componentResized(ComponentEvent e) {
            slider.placeComponents();
        }

        @Override
        public void componentHidden(ComponentEvent e) {
        }

        @Override
        public void componentMoved(ComponentEvent e) {
        }

        @Override
        public void componentShown(ComponentEvent e) {
        }
    }
    //end of ElanSliderComponentListener
}
//end of ElanSlider
