package mpi.eudico.client.annotator.interlinear.edit.config;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.ClosableDialog;
import nl.mpi.lexan.analyzers.helpers.ConfigurationChangeListener;
import nl.mpi.lexan.analyzers.helpers.ConfigurationChanger;

/**
 * A dialog to host the configuration component of one analyzer.
 *
 * When provided, it adds and removes a list of listeners for
 * configuration changes. The dialog registers as listener itself in order
 * to close the window when changes have been applied.
 *
 */
@SuppressWarnings("serial")
public class AnalyzerSettingsDialog extends ClosableDialog implements ConfigurationChangeListener {
	private Component configPanel;
	private ConfigurationChanger confChanger;

	/**
	 * Constructor.
	 *
	 * @param owner the parent dialog
	 * @param configPanel the configurations panel of an analyzer
	 * @param ccList a list of change listeners, can be {@code null}
	 *
	 * @throws HeadlessException if run in a headless environment
	 */
	public AnalyzerSettingsDialog(Dialog owner, Component configPanel,
			List<ConfigurationChangeListener> ccList) throws HeadlessException {
		super(owner, true);
		this.configPanel = configPanel;
		initListeners(ccList);
		initComponents();
	}

	/**
	 * Constructor.
	 *
	 * @param owner the parent frame
	 * @param configPanel the configurations panel of an analyzer
	 * @param ccList a list of change listeners, can be {@code null}
	 *
	 * @throws HeadlessException if run in a headless environment
	 */
	public AnalyzerSettingsDialog(Frame owner, Component configPanel,
			List<ConfigurationChangeListener> ccList) throws HeadlessException {
		super(owner, true);
		this.configPanel = configPanel;
		initListeners(ccList);
		initComponents();
	}

	/**
	 * If the configuration component is an instance of {@link ConfigurationChanger}
	 * the listeners are added as listener.
	 *
	 * @param ccList list of configuration change listeners
	 */
	private void initListeners(List<ConfigurationChangeListener> ccList) {
		if (configPanel instanceof ConfigurationChanger) {
			confChanger = (ConfigurationChanger) configPanel;
			confChanger.addConfigurationChangeListener(this);

			if (ccList != null) {
				for (ConfigurationChangeListener ccl : ccList) {
					confChanger.addConfigurationChangeListener(ccl);
				}
			}
		}
	}

	/**
	 * Adds the configuration panel to a scroll pane to the content pane.
	 */
	private void initComponents() {
		// set border, title
		JPanel contentPane = new JPanel(new GridBagLayout());
		EmptyBorder marginBorder = new EmptyBorder(4, 6, 4, 6);
		TitledBorder titledBorder = new TitledBorder(ElanLocale.getString("InterlinearAnalyzerConfigPanel.ConfigureSettings"));
		contentPane.setBorder(new CompoundBorder(marginBorder, titledBorder));
		// add conf. panel
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridwidth = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;

		contentPane.add(new JScrollPane(configPanel), gbc);

		this.setContentPane(contentPane);
	}

	/**
	 * Closes the dialog and in the end removes all listeners from the panel.
	 *
	 * @param cc the configuration panel
	 */
	@Override
	public void configurationChanged(ConfigurationChanger cc) {
		setVisible(false);
		dispose();

		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				cc.removeAllListeners();
			}
		});
	}

}
