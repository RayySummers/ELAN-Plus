package mpi.eudico.client.annotator.tier;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DecimalFormat;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;

/**
 * 
 * Panel for step 2: allows to enter begin and end time shift of annotations
 * also to allows to choose override options
 */
@SuppressWarnings("serial")
public class ModifyBoundariesOfAllAnnotationsStep2 extends StepPane {

	private Frame parentFrame;

	private JLabel milliSecLabel;

	private JLabel emptyBeginOrEndTimeText;

	private JFormattedTextField btTextField;
	private JFormattedTextField etTextField;

	private DecimalFormat threeDigits;

	private JCheckBox overwriteLeftCheckbox, overwriteRightCheckbox;

	/** list of files */
	protected List<String> files;
	/** list of selected tiers */
	protected List<String> selectedTiers;

	/** the empty string */
	protected final String EMPTY = "";

	/**
	 * Constructor
	 * 
	 * @param multiPane the container multiStepPane
	 * @param owner     the owner frame
	 */
	public ModifyBoundariesOfAllAnnotationsStep2(MultiStepPane multiPane, Frame owner) {
		super(multiPane);
		this.parentFrame = owner;
		initComponents();

	}

	/**
	 * Initialize the UI components.
	 */
	@Override
	protected void initComponents() {

		milliSecLabel = new JLabel(ElanLocale.getString("ModifyBoundariesOfAllAnnotations.Title.Millisecond"),
				SwingConstants.CENTER);
		JLabel btLabel = new JLabel(ElanLocale.getString("Frame.GridFrame.ColumnBeginTime"), SwingConstants.TRAILING);
		JLabel etLabel = new JLabel(ElanLocale.getString("Frame.GridFrame.ColumnEndTime"), SwingConstants.TRAILING);

		overwriteLeftCheckbox = new JCheckBox(ElanLocale.getString("ModifyBoundariesOfAllAnnotations.OverwriteLeft"),
				false);
		overwriteRightCheckbox = new JCheckBox(ElanLocale.getString("ModifyBoundariesOfAllAnnotations.OverwriteRight"),
				false);

		emptyBeginOrEndTimeText = new JLabel();
		emptyBeginOrEndTimeText.setForeground(Color.RED);
		emptyBeginOrEndTimeText.setText(EMPTY);

		JPanel checkboxPanel = new JPanel(new GridBagLayout());
		checkboxPanel
				.setBorder(new TitledBorder(ElanLocale.getString("ModifyBoundariesOfAllAnnotations.Title.Overwrite")));

		Insets insets = new Insets(2, 6, 2, 6);

		JPanel timeFramePanel = new JPanel(new GridBagLayout());
		timeFramePanel
				.setBorder(new TitledBorder(ElanLocale.getString("ModifyBoundariesOfAllAnnotations.Title.Scale")));
		threeDigits = new DecimalFormat("000");
		threeDigits.setMaximumIntegerDigits(3);
		threeDigits.setMinimumIntegerDigits(3);

		btTextField = new JFormattedTextField(threeDigits);
		etTextField = new JFormattedTextField(threeDigits);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = insets;

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.NONE;
		timeFramePanel.add(milliSecLabel, gbc);

		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		timeFramePanel.add(emptyBeginOrEndTimeText, gbc);

		gbc.gridy = 1;
		gbc.gridx = 0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		timeFramePanel.add(btLabel, gbc);

		gbc.gridy = 1;
		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 0.1;
		timeFramePanel.add(btTextField, gbc);

		JPanel filler = new JPanel();
		gbc.gridx = 2;
		gbc.weightx = 2.0;
		timeFramePanel.add(filler, gbc);

		gbc.gridy = 2;
		gbc.gridx = 0;
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		timeFramePanel.add(etLabel, gbc);

		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridy = 2;
		gbc.gridx = 1;
		gbc.weightx = 0.1;
		timeFramePanel.add(etTextField, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = insets;
		checkboxPanel.add(overwriteLeftCheckbox, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		checkboxPanel.add(overwriteRightCheckbox, gbc);

		setLayout(new GridBagLayout());
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = insets;
		gbc.weightx = 1.0;
		add(timeFramePanel, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = insets;
		add(checkboxPanel, gbc);

		JPanel filler1 = new JPanel();
		filler1.setPreferredSize(new Dimension(300, 100));
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 1.0;
		add(filler1, gbc);

	}

	@Override
	public String getStepTitle() {
		return ElanLocale.getString("ModifyBoundariesOfAllAnnotations.Title.Step2Title");
	}

	@Override
	public void enterStepForward() {
		emptyBeginOrEndTimeText.setText(EMPTY);
		updateButtonStates();
	}

	/**
	 * Check and store properties, if all conditions are met.
	 */
	@Override
	public boolean leaveStepForward() {

		Long beginTime = (Long) btTextField.getValue();
		Long endTime = (Long) etTextField.getValue();

		if ((endTime == null) && (beginTime == null)) {
			emptyBeginOrEndTimeText
					.setText("-" + ElanLocale.getString("ModifyBoundariesOfAllAnnotations.EmptyBeginOrEndTime"));
			return false;
		} else if (beginTime == null) {
			btTextField.setValue(Long.valueOf(0));
		} else if (endTime == null) {
			etTextField.setValue(Long.valueOf(0));
		}
		multiPane.putStepProperty("beginTime", btTextField.getValue());
		multiPane.putStepProperty("endTime", etTextField.getValue());
		multiPane.putStepProperty("overrideLeftAnnotation", overwriteLeftCheckbox.isSelected());
		multiPane.putStepProperty("overrideRightAnnotation", overwriteRightCheckbox.isSelected());
		return true;

	}

	/**
	 * Set the button states appropriately
	 */
	public void updateButtonStates() {
		multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);

		multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, true);

		multiPane.setButtonEnabled(MultiStepPane.PREVIOUS_BUTTON, true);
	}

	@Override
	public boolean doFinish() {
		multiPane.nextStep();
		return false;
	}

}
