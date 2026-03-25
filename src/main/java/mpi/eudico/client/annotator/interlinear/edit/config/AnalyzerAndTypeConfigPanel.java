package mpi.eudico.client.annotator.interlinear.edit.config;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.ClosableDialog;
import mpi.eudico.client.annotator.layout.InterlinearizationManager;
import mpi.eudico.client.annotator.util.WindowLocationAndSizeManager;

/**
 * A panel for configuration of analyzers and source and target tier types.
 */
@SuppressWarnings("serial")
public class AnalyzerAndTypeConfigPanel extends JPanel implements ActionListener {
	/** code analyzer string constant*/
	public static final String CONF_ANALYZER = "ConfigAnalyzer";
	/** code source target string constant*/
	public static final String CONF_SOURCE_TARGET = "ConfigSourceTarget";
	private InterlinearizationManager manager;
	private JButton configTypesButton;
	private JLabel numCombinationsLabel;
	private TitledBorder titledBorder;

	/**
	 * Constructor.
	 *
	 * @param manager the interlinearization manager
	 */
	public AnalyzerAndTypeConfigPanel(InterlinearizationManager manager) {
		super();
		this.manager = manager;

		initComponents();
	}

	private void initComponents() {
		// set a titled border
		EmptyBorder marginBorder = new EmptyBorder(4, 6, 4, 6);
		titledBorder = new TitledBorder("");
		setBorder(new CompoundBorder(marginBorder, titledBorder));

		// add one button
		configTypesButton = new JButton();
		configTypesButton.addActionListener(this);
		numCombinationsLabel = new JLabel("");

		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(4, 6, 4, 6);
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;

		gbc.gridy = 1;
		add(configTypesButton, gbc);

		gbc.gridy = 2;
		add(numCombinationsLabel, gbc);

		gbc.gridy = 3;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 1.0;
		gbc.insets = new Insets(0, 0, 0, 0);
		JPanel fillPanel = new JPanel();
		fillPanel.setPreferredSize(new Dimension(1, 0));
		add (fillPanel, gbc);
		updateLocale();
	}

	/**
	 * Can be called by the mode/layout manager if the UI language changed.
	 */
	public void updateLocale() {
		titledBorder.setTitle(ElanLocale.getString("InterlinearAnalyzerConfigPanel.ConfigureSettings"));
		configTypesButton.setText(ElanLocale.getString("InterlinearAnalyzerConfigDlg.ButtonText"));
	}

	/**
	 * Updates a label to show the number of configurations.
	 */
	public void configsChanged() {
		try {
			numCombinationsLabel.setText(String.format(
					ElanLocale.getString("InterlinearAnalyzerConfigPanel.NumberConfigs"),
					manager.getTextAnalyzerContext().getConfigurations().size()));
		} catch (Throwable t) {}
	}

	/**
	 *
	 *
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == configTypesButton || CONF_SOURCE_TARGET.equals(e.getActionCommand())) {
			AnalyzerConfigPanel configPanel = new AnalyzerConfigPanel(manager);
			configPanel.updateLocale();
			configPanel.configsChanged();// initialize the table
			JDialog configureDialog = new ClosableDialog();
			configureDialog.setTitle(ElanLocale.getString("InterlinearAnalyzerConfigDlg.ButtonTitle"));
			configureDialog.getContentPane().add(configPanel);
			configureDialog.setModal(true);

			WindowLocationAndSizeManager.postInit(configureDialog, "ConfigureAnalyzersAndTierTypesDlg", 400, 300);
			configureDialog.setVisible(true);// blocks
			configsChanged();
			WindowLocationAndSizeManager.storeLocationAndSizePreferences(configureDialog, "ConfigureAnalyzersAndTierTypesDlg");
		}

	}


}
