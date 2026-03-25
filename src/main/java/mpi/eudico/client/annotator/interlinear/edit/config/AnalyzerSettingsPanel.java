package mpi.eudico.client.annotator.interlinear.edit.config;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.interlinear.edit.TextAnalyzerHostContext;
import nl.mpi.lexan.analyzers.helpers.ConfigurationChangeListener;
import nl.mpi.lexan.analyzers.helpers.ConfigurationChanger;
import nl.mpi.lexan.analyzers.helpers.Information;

/**
 * A tab-pane to host the configuration panels provided by
 * the analyzers. Analyzers that allow to change settings and
 * to configure behavior, need to have their own, custom
 * settings panel.
 *
 * @author aarsom, olasei
 */
@SuppressWarnings("serial")
public class AnalyzerSettingsPanel extends JPanel implements ConfigurationChangeListener {
	private TextAnalyzerHostContext hostContext;
	//private List<Information> analyzers = null;

	private JTabbedPane tabPane;
	private TitledBorder titledBorder;

	/**
	 * Constructor for a panel with a tab pane for all available analyzers.
	 * Modifications to the settings will be applied to the general, global
	 * settings of those analyzers.
	 *
	 * @param hostContext the context from which to get the analyzers
	 */
	public AnalyzerSettingsPanel(TextAnalyzerHostContext hostContext) {
		super();
		this.hostContext = hostContext;
		initForList(hostContext.listTextAnalyzersInfo());
	}

	/**
	 * Constructor for a panel for a single analyzer and source-target combination.
	 *
	 * @param hostContext the context from which to get the analyzer
	 * @param forConfig the analyzer and source-target configuration object
	 */
	public AnalyzerSettingsPanel(TextAnalyzerHostContext hostContext, AnalyzerConfig forConfig) {
		super();
		this.hostContext = hostContext;

		initForConfig(forConfig);
	}

	/**
	 * Constructor for a panel for a single analyzer, to modify global settings
	 * of the analyzer.
	 *
	 * @param hostContext the context from which to get the analyzer
	 * @param analyzerInfo the Information object identifying the analyzer
	 */
	public AnalyzerSettingsPanel(TextAnalyzerHostContext hostContext, Information analyzerInfo) {
		super();
		this.hostContext = hostContext;

		initForAnalyzer(analyzerInfo);
	}

	private void initBorder() {
		EmptyBorder marginBorder = new EmptyBorder(4, 6, 4, 6);
		titledBorder = new TitledBorder(ElanLocale.getString("InterlinearAnalyzerConfigPanel.ConfigureSettings"));
		setBorder(new CompoundBorder(marginBorder, titledBorder));
	}

	/**
	 * Creates a tab pane, adds the configuration panels of the analyzers
	 * to the tab pane and adds the tab pane to the main panel.
	 * @param analyzers the list of analyzers
	 */
	private void initForList(List<Information> analyzers) {
		if (analyzers == null || analyzers.isEmpty()) {
			return;
		}
		initBorder();

		tabPane = new JTabbedPane();
		int tabNr = 0;

		for (int i = 0; i < analyzers.size(); i++) {
			Information analyzer = analyzers.get(i);
			Component comp = hostContext.getConfigurationComponent(analyzer, null, true);
			if (comp != null) {
				tabPane.addTab(analyzer.getName(), new JScrollPane(comp));
				if (tabNr < 10) {
					tabPane.setMnemonicAt(tabNr, KeyEvent.VK_0 + (tabNr + 1) % 10);
				}
				tabNr++;
			}
		}

		if (tabNr > 0) {
			setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.anchor = GridBagConstraints.NORTHWEST;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.gridwidth = 1;
			gbc.weightx = 1.0;
			gbc.weighty = 1.0;
			add(tabPane, gbc);

			readPreferences();
		}
	}

	/**
	 * Adds the configuration component of a single analyzer to the main
	 * panel.
	 * @param forConfig the configuration object containing the analyzer
	 * information and the source-target configuration
	 */
	private void initForConfig(AnalyzerConfig forConfig) {
		if (forConfig == null) {
			return;
		}

		Component comp = hostContext.getConfigurationComponent(
				forConfig.getAnnotId(), forConfig.getConfigKey(), false);// assume the analyzer has been loaded already
		if (comp == null) {
			return;
		}

		initBorder();
		// add a label to indicate that this particular source target
		// combination is edited?
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridwidth = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;

		add(new JScrollPane(comp), gbc);
		// register as listener to configuration change events? to close the
		// dialog (if the parent is a dialog)
		if (comp instanceof ConfigurationChanger) {
			((ConfigurationChanger) comp).addConfigurationChangeListener(this);
		}
	}

	/**
	 * Adds the configuration component of a single analyzer to the main
	 * panel for modifying global settings.
	 * @param analyzerInfo the analyzer information object
	 */
	private void initForAnalyzer(Information analyzerInfo) {
		if (analyzerInfo == null) {
			return;
		}

		Component comp = hostContext.getConfigurationComponent(
				analyzerInfo, null, false);//assume the analyzer has been loaded already
		if (comp == null) {
			return;
		}

		initBorder();

		// add a label to indicate that general, global settings
		// are edited?
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridwidth = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;

		add(new JScrollPane(comp), gbc);

		// register as listener to configuration change events? to close the
		// dialog (if the parent is a dialog)
		if (comp instanceof ConfigurationChanger) {
			((ConfigurationChanger) comp).addConfigurationChangeListener(this);
		}
	}

	/**
	 * Can be called by the mode/layout manager if the UI language changed.
	 * NB. no longer the case if this panel is presented in a modal dialog
	 */
	public void updateLocale() {
		titledBorder.setTitle(ElanLocale.getString("InterlinearAnalyzerConfigPanel.ConfigureSettings"));
	}
	/**
	 * Selects, in case of a tab pane with multiple configuration panels, the
	 * tab of the last used analyzer.
	 */
	private void readPreferences() {
		if (tabPane != null) {
			String analyzer = Preferences.getString("AnalyzerConfigurationPanel.CurrentAnalyzer",
					 hostContext.getTranscription());

			if (analyzer != null) {
				int tabNr = tabPane.indexOfTab(analyzer);
				if (tabNr >= 0) {
					tabPane.setSelectedIndex(tabNr);
				}
			}
		}
	}

	/**
	 * Stores the analyzer which was configured last
	 */
	public void storePreferences() {
		if (tabPane != null && tabPane.getSelectedIndex() >= 0) {
			Preferences.set("AnalyzerConfigurationPanel.CurrentAnalyzer",
					tabPane.getTitleAt(tabPane.getSelectedIndex()),
					hostContext.getTranscription());
		}
	}

	/**
	 * In case of a single panel for a single analyzer, this class registers
	 * as listener and closes the dialog once changes to the configuration
	 * have been applied.In the end this class unregisters as listener.
	 *
	 * @param cc the source of the event
	 */
	@Override
	public void configurationChanged(ConfigurationChanger cc) {
		if (this.getTopLevelAncestor() instanceof Dialog) {
			((Dialog) this.getTopLevelAncestor()).setVisible(false);
			((Dialog) this.getTopLevelAncestor()).dispose();
			EventQueue.invokeLater(new Runnable() {

				@Override
				public void run() {
					cc.removeConfigurationChangeListener(AnalyzerSettingsPanel.this);
				}
			});

		}
	}


}
