package mpi.eudico.client.annotator.interlinear.edit.config;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.layout.InterlinearizationManager;
import mpi.eudico.client.annotator.util.WindowLocationAndSizeManager;
import nl.mpi.lexan.analyzers.TextAnalyzer;
import nl.mpi.lexan.analyzers.helpers.ConfigurationChangeListener;
import nl.mpi.lexan.analyzers.helpers.Information;

/**
 * A class for selecting and configuring available "analyzers".
 *
 * <pre>
 * +------------------------------------------------------------------+
 * |                                                                  |
 * |  +--Analyzer configuration------------------------------------+  |
 * |  |                                                            |  |
 * |  |  | Analyzer           || Source         ||  Target      |  |  |
 * |  |  ---------------------++----------------++---------------  |  |
 * |  |  | Parse and Gloss ...|| testtier       ||  sub1,sub2   |  |  |
 * |  |  | Random Analyzer    || default-lt     ||  sub-lt      |  |  | &lt;= type config
 * |  |  |                    || /test/         ||  /testsub/   |  |  | &lt;= 1 or more tiers that fit the types
 * |  |  |                    ||                ||              |  |  |
 * |  |                                                            |  |
 * |  +------------------------------------------------------------+  |
 * |                                                                  |
 * |  [v] Show tier mapping for all type based configurations         |
 * |                                                                  |
 * |  [Configure Analyzer]     [ Remove ] [ Edit configurations... ]  |
 * |                                                                  |
 * +------------------------------------------------------------------+
 * </pre>
 *
 * @version Nov 2019 the panel is now shown in a (modal) dialog, no longer
 * permanently in the main window.
 *
 * @author Han Sloetjes
 * @author Aarthy Somasundaram
 *
 */
@SuppressWarnings("serial")
public class AnalyzerConfigPanel extends JPanel implements ComponentListener,
	ActionListener, ListSelectionListener {
	private static final int ANALYZER_NAME_COL = 0;
	private static final int SOURCE_COL = 1;
	private static final int CONFIG_COL = 4;		             // Annot{Tier,Type}Config
	private static final int TIER_SUBCONFIG_COL = 5;             // only if the CONFIG_COL has a AnalyzerTypeConfig
	private static final String LIN_TYPE = "LinType";            // name for column TYPE_MODE_COL
	private static final String ANNOT_CONFIG = "AnalyzerConfig"; // name for column CONFIG_COL
	private static final String SUB_CONFIG = "SubConfig";        // name for column TIER_SUBCONFIG_COL
	private static final Color TYPE_BG = Color.WHITE;
	private static final Color TYPE_FG = Color.BLACK;
	private InterlinearizationManager manager;
	private JButton editConfigButton;
	private JButton closeButton;
	private JButton removeConfigButton;
	private JButton configAnalyzerButton;
	private JCheckBox showTiersCB;
	private JTable configsTable;
	private DefaultTableModel configsModel;
	private JPanel configPanel;
	private AnalyzerConfigRenderer anRenderer;

	/**
	 * Constructor
	 * @param manager the manager holding references to the list of available
	 * analyzers and the related analyzer context objects
	 */
	public AnalyzerConfigPanel(InterlinearizationManager manager) {
		super();
		this.manager = manager;
		initComponents();
	}

	private void initComponents() {
		setLayout(new GridBagLayout());
		Insets insets = new Insets (4, 6, 4, 6);
		
		// config Panel
		configPanel = new JPanel(new GridBagLayout());
		configPanel.setBorder(new TitledBorder(""));   
       
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = insets;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		//add(scrollPane, gbc);

		add(configPanel, gbc);
		// table
		configsModel = new DefaultTableModel(new String[]{
				ElanLocale.getString("InterlinearAnalyzerConfigPanel.Analyzer"),
				ElanLocale.getString("InterlinearAnalyzerConfigPanel.SourceTier"),
				ElanLocale.getString("InterlinearAnalyzerConfigPanel.TargetTier"),
				LIN_TYPE, ANNOT_CONFIG, SUB_CONFIG}, 0){
			@Override
			public boolean isCellEditable(int row, int column) {
				 return false;
			 }
		};

		configsTable = new JTable(configsModel);

		//TableCellRenderer
        anRenderer = new AnalyzerConfigRenderer();
        configsTable.setDefaultRenderer(Object.class, anRenderer);

		configsTable.getTableHeader().setReorderingAllowed(false);
		configsTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		configsTable.getSelectionModel().addListSelectionListener(this);

		// These columns are for our internal use; hide them from the user.
		configsTable.removeColumn(configsTable.getColumn(LIN_TYPE));
		configsTable.removeColumn(configsTable.getColumn(ANNOT_CONFIG));
		configsTable.removeColumn(configsTable.getColumn(SUB_CONFIG));

		removeConfigButton = new JButton();
		removeConfigButton.addActionListener(this);
		removeConfigButton.setEnabled(false);

		editConfigButton = new JButton();
		editConfigButton.addActionListener(this);

		configAnalyzerButton = new JButton();
		configAnalyzerButton.addActionListener(this);
		configAnalyzerButton.setEnabled(false);

		closeButton = new JButton();
		closeButton.addActionListener(this);

		showTiersCB = new JCheckBox();
		showTiersCB.setSelected(true);
		showTiersCB.addActionListener(this);

		gbc = new GridBagConstraints();
		gbc.insets = insets;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridwidth = 4;
		configPanel.add(new JScrollPane(configsTable), gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		configPanel.add(showTiersCB, gbc);

		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		configPanel.add(configAnalyzerButton, gbc);

		gbc.gridx = 1;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.NORTHEAST;
		configPanel.add(new JPanel(), gbc);

		gbc.gridx = 2;
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.NORTHEAST;
		configPanel.add(removeConfigButton, gbc);

		gbc.gridx = 3;
		gbc.anchor = GridBagConstraints.NORTHEAST;
		configPanel.add(editConfigButton, gbc);

		gbc.gridx = 3;
		gbc.gridy = 3;
		gbc.anchor = GridBagConstraints.EAST;
		configPanel.add(closeButton, gbc);

		addComponentListener(this);
		TableMouseListener mouseListener = new TableMouseListener();
		configsTable.addMouseListener(mouseListener);
		updateLocale();
	}

	/**
	 * A table cell renderer for analyzer and associated source-target
	 * configurations. The table is assumed to have 3 visible columns, one
	 * for the analyzer name (plus a button for configuring the analyzer),
	 * one for the source tier type (plus possibly the relevant tiers) and
	 * one for the target tier type(s) (plus possibly tiers).
	 */
	class AnalyzerConfigRenderer extends JPanel implements TableCellRenderer {
		private JLabel configIconLabel;
		private JLabel mainLabel;
		private ImageIcon confIcon;
		private JTextArea tierArea;
		private boolean showTiers = true;
		private String anTooltip = ElanLocale.getString(
				"InterlinearAnalyzerConfigPanel.ConfigureAnalyzer");
		private String appliesToTiers = ElanLocale.getString(
				"InterlinearAnalyzerConfigPanel.AppliesToTiers");

		/**
		 * Constructor, initializes the rendering components.
		 */
		public AnalyzerConfigRenderer() {
			super();
			initComponents();
		}

		/**
		 * @return true if the tiers are shown too
		 */
		public boolean isShowTiers() {
			return showTiers;
		}

		/**
		 * @param showTiers set if the tiers should be shown too
		 */
		public void setShowTiers(boolean showTiers) {
			this.showTiers = showTiers;
		}

		private void initComponents() {
			configIconLabel = new JLabel();
			try {
				confIcon = new ImageIcon(this.getClass().getResource(
						Constants.ICON_LOCATION + "Configure16.gif"));
				configIconLabel.setIcon(confIcon);
			} catch (Exception ex) {
				// catch any image loading exception
				configIconLabel.setText("Conf.");
			}

			mainLabel = new JLabel();
			tierArea = new JTextArea();
			tierArea.setLineWrap(true);
			tierArea.setWrapStyleWord(true);
			tierArea.setOpaque(true);
			tierArea.setBackground(new Color(200, 200, 200, 64));

			setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.insets = new Insets(1, 2, 1, 2);
			gbc.anchor = GridBagConstraints.WEST;
			add(configIconLabel, gbc);
			gbc.gridx = 1;
			gbc.weightx = 1.0;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			add(mainLabel, gbc);
			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.gridwidth = 2;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weighty = 1.0;
			add(tierArea, gbc);
		}

		/**
		 *
		 * @return a panel configured to render the contents of the specified cell
		 */
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column) {
			setBackground(TYPE_BG);
			setForeground(TYPE_FG);
			tierArea.setVisible(showTiers);

			if (column == ANALYZER_NAME_COL) {
				configIconLabel.setVisible(true);
				// tierArea pushes the button and label to the top if tiers are shown
				tierArea.setOpaque(false);
				tierArea.setText("");
				setToolTipText(anTooltip);
			} else {
				configIconLabel.setVisible(false);

				if (showTiers) {
					tierArea.setOpaque(true);
					tierArea.setText(appliesToTiers);
					// concatenate and add tier names
					Object subConfObj = configsModel.getValueAt(row, CONFIG_COL);
					if (subConfObj instanceof AnalyzerTypeConfig) {
						List<AnalyzerConfig> tierConfig = ((AnalyzerTypeConfig)subConfObj).getTierConfigurations();

						String target;
						for (AnalyzerConfig atc : tierConfig) {
							target = null;
							if (column == SOURCE_COL) {
								target = atc.getSource();
							} else { // column == TARGET_COL
								for (String s : atc.getDest()) {
									if(target == null){
										target = s;
									} else {
										target = target.concat(", ").concat(s);
									}
								}
							}
							if (target != null) {
								tierArea.append("\n" + target);
							}
						}
					}
				}
			}

			mainLabel.setText(String.valueOf(value));

			if (table.getSelectedRow() == row) {
				setBackground(table.getSelectionBackground());
   	 			//setForeground(table.getSelectionForeground());
				mainLabel.setForeground(table.getSelectionForeground());
   	 			tierArea.setForeground(table.getSelectionForeground());
			} else {
				setBackground(table.getBackground());
				mainLabel.setForeground(table.getForeground());
				tierArea.setForeground(table.getForeground());
			}

			return this;
		}

	}

	/**
	 * Updates the locale
	 */
	public void updateLocale(){
		((TitledBorder)configPanel.getBorder()).setTitle(ElanLocale.getString("InterlinearAnalyzerConfigDlg.Title"));
		removeConfigButton.setText(ElanLocale.getString("InterlinearAnalyzerConfigPanel.RemoveConfig"));
		editConfigButton.setText(ElanLocale.getString("InterlinearAnalyzerConfigPanel.EditConfig"));
		configAnalyzerButton.setText(ElanLocale.getString("InterlinearAnalyzerConfigPanel.ConfigureSettings"));
		closeButton.setText(ElanLocale.getString("Button.Close"));
		showTiersCB.setText(ElanLocale.getString("InterlinearAnalyzerConfigPanel.ShowTiers"));
		/* the panel is now shown in a modal dialog, so the labels don't change while already visible
		configsModel.setColumnIdentifiers(new String[]{
				ElanLocale.getString("InterlinearAnalyzerConfigPanel.Analyzer"),
				ElanLocale.getString("InterlinearAnalyzerConfigPanel.SourceTier"),
				ElanLocale.getString("InterlinearAnalyzerConfigPanel.TargetTier"),
				LIN_TYPE, ANNOT_CONFIG, SUB_CONFIG});
		// These columns are for our internal use; hide them from the user.
		configsTable.removeColumn(configsTable.getColumn(LIN_TYPE));
		configsTable.removeColumn(configsTable.getColumn(ANNOT_CONFIG));
		configsTable.removeColumn(configsTable.getColumn(SUB_CONFIG));

		repaint();// the titled border does not automatically update
		*/
	}

	/**
	 * Changes read from stored preferences or resulting from editing of
	 * configurations.
	 */
	public void configsChanged() {
		configsTable.getSelectionModel().removeListSelectionListener(this);

		if (configsModel.getRowCount() > 0) {
			for (int i = configsModel.getRowCount() - 1; i >= 0; i--) {
				configsModel.removeRow(i);
			}
		}

		List<AnalyzerConfig> conf = manager.getTextAnalyzerContext().getConfigurations();

		AnalyzerConfig activeConf = manager.getInterEditor().getActiveConfiguration();
		int activeConfRowIndex = -1;

		String target;
		for (AnalyzerConfig ac : conf) {
			target = null;
			for (String s : ac.getDest()) {
				if(target == null){
					target = s;
				} else {
					target = target.concat(", ").concat(s);
				}
			}
			configsModel.addRow(new Object[]{ac.getAnnotId().getName(), ac.getSource(), target, ac.isTypeMode(), ac, null});

			if (activeConf == null) {
				activeConf = ac;
				activeConfRowIndex = 0;
				manager.getInterEditor().setActiveConfiguration(ac);
			}
		}

		configsTable.getSelectionModel().addListSelectionListener(this);

		if (activeConfRowIndex < 0 && conf.size() > 0) {
			//configsTable.getSelectionModel().setSelectionInterval(configsTable.getRowCount()-1, configsTable.getRowCount()-1);
			activeConfRowIndex = 0;
			activeConf = conf.get(0);
			configsTable.getSelectionModel().setSelectionInterval(0, 0);
			manager.getInterEditor().setActiveConfiguration(activeConf);
		} else {
			configsTable.getSelectionModel().setSelectionInterval(activeConfRowIndex, activeConfRowIndex);
		}

		updateRowHeights();
	}

	/**
	 * Updates the configuration table with the configurations
	 * from the configuration dialog AnalyzerConfigDialog.
	 *
	 * @param configList  {@code List<AConfig>}
	 * @param typeMode  true if the mapping is based on linguistic types
	 */
	protected void updateConfigurations(List<AnalyzerConfig> configList,
			boolean typeMode) {

		if (configList == null) {
			return;
		}

		List<Information> analyzers = manager.getTextAnalyzerContext().listTextAnalyzersInfo();
		// Duplicate list of existing configs
		ArrayList<AnalyzerConfig> oldConfigs = new ArrayList<AnalyzerConfig>(manager.getTextAnalyzerContext().getConfigurations());
		boolean changed = false;
		// the provided list already contains AnalyzerConfig's with Information objects
		// double check with the current list of the context
		for (AnalyzerConfig  annotConfig : configList) {
			String annot = annotConfig.getAnnotId().getName();
			String source = annotConfig.getSource();
			List<String>  targetList = annotConfig.getDest();
			Information info = null;

			// find the analyzer info object again.
			if (analyzers != null) {
				for (Information li : analyzers) {
					if (annot.equals(li.getName())) {
						info = li;
						break;
					}
				}
			}

			// if info == null, popup warning
			if (info == null) {
				JOptionPane.showMessageDialog(this, annot + ": " +
			ElanLocale.getString("InterlinearAnalyzerConfigPanel.AnalyzerNotFound")
						+ " -" + source, ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE);
			} else {
				if (typeMode) {
					AnalyzerTypeConfig atc = new AnalyzerTypeConfig(info, source, targetList);
					manager.fillWithTierConfigs(info, atc, null);
					changed = updateConfig(oldConfigs, atc) | changed;
				} else {
					AnalyzerConfig ac = new AnalyzerConfig(info, source, targetList);
					changed = updateConfig(oldConfigs, ac) | changed;
				}
			}
		}

		// Remove the old configs which didn't get matched by equal new configs,
		// i.e. the ones that were changed or removed by the user.
		for (AnalyzerConfig toRemove : oldConfigs) {
			if (toRemove.isTypeMode() == typeMode) {
				manager.getTextAnalyzerContext().removeConfig(toRemove);
				changed = true;
			}
		}

		if (changed) {
			configsChanged();
		}
	}

	/**
	 * If the AnalyzerConfig was not there already, add it.
	 * Otherwise, remove it from the 'old' list and keep it.
	 * <p>
	 * We don't need to look in more detail at AnnotTypeConfigs, like
	 * configExists() does, because we are dealing here with the data
	 * as entered by the user. It would be unexpected to mix it with
	 * AnnotConfigs derived from AnnotTypeConfigs.
	 * <p>
	 * Note that AnalyzerConfig and AnalyzerTypeConfig's equals() methods
	 * require the argument's type mode to be the same, and disregard
	 * the derived data.
	 *
	 * @return whether the config was new.
	 */
	private boolean updateConfig(ArrayList<AnalyzerConfig> oldConfigs, AnalyzerConfig config) {
		int index = oldConfigs.indexOf(config);

		if (index < 0) {
			// The old configurations don't contain this new one.
			manager.getTextAnalyzerContext().addConfig(config);
			return true;
		} else {
			// Don't delete this config later on: it's still there.
			oldConfigs.remove(index);
			return false;
		}
	}

	/**
	 * Calculates and sets the height of each row in the configuration table.
	 */
	private void updateRowHeights() {
		for (int i = 0; i < configsTable.getRowCount(); i++) {
			int h = 0;
			for (int j = 0; j < configsTable.getColumnCount(); j++) {
				Component c = configsTable.getCellRenderer(i, j).getTableCellRendererComponent(
						configsTable, configsTable.getValueAt(i, j), false, false, i, j);
				int ch = c.getPreferredSize().height;
				if (ch > h) {
					h = ch;
				}
			}
			configsTable.setRowHeight(i, h);
		}
	}

	/**
	 * Creates a configuration dialog for the selected analyzer, if any, and if
	 * the analyzer provides a configuration panel.
	 *
	 * @param row the selected row in the table
	 */
	private void configureAnalyzerAtRow(int row) {
		if (row > -1) {
			AnalyzerTypeConfig atc = (AnalyzerTypeConfig)
					configsModel.getValueAt(row, CONFIG_COL);

			// create a dialog with the configuration panel of the analyzer, if any
			List<AnalyzerConfig> analyzerConfigs = manager.getTextAnalyzerContext().getConfigurations();
			if (analyzerConfigs.contains(atc)) {
				// prompt if global or configuration specific settings need to be edited
				StringBuilder questionBuilder = new StringBuilder("<html>");
				questionBuilder.append(ElanLocale.getString("InterlinearAnalyzerConfigPanel.ConfigurationWhat"));
				questionBuilder.append("<ul><li>");
				questionBuilder.append(ElanLocale.getString("InterlinearAnalyzerConfigPanel.GlobalSettingsInfo"));
				questionBuilder.append("</li><li>");
				questionBuilder.append(ElanLocale.getString("InterlinearAnalyzerConfigPanel.SpecificSettingsInfo"));
				questionBuilder.append(String.format(" (%s - %s)", atc.getSource(), String.valueOf(atc.getDest())));
				questionBuilder.append("</li></ul></html>");
				String[] configOptions = {
						ElanLocale.getString("InterlinearAnalyzerConfigPanel.GlobalSettingsButton"),
						String.format(ElanLocale.getString("InterlinearAnalyzerConfigPanel.SpecificSettingsButton"))};
				int selOption = JOptionPane.showOptionDialog(this,
						questionBuilder.toString(),
						ElanLocale.getString("InterlinearAnalyzerConfigPanel.ConfigureSettings"), JOptionPane.DEFAULT_OPTION,
						JOptionPane.QUESTION_MESSAGE, null, configOptions, configOptions[0]);

				if (selOption < 0) {
					// the dialog has been cancelled, no choice was made
					return;
				}
				// 0 = global, 1 = specific

				String configId = selOption == 1 ? atc.getConfigKey() : null;
				Component cc = manager.getTextAnalyzerContext().getConfigurationComponent(atc.getAnnotId(), configId, false);
				if (cc == null) {
					// no configuration component, show message
					JOptionPane.showMessageDialog(this, ElanLocale.getString("InterlinearAnalyzerConfigPanel.NoConfigPanel"),
							ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE);
					return;
				}

				List<ConfigurationChangeListener> listeners = toListenersList(
						manager.getTextAnalyzerContext().getAnalyzerInstances(
								atc.getAnnotId(), atc.getConfigKey()));
				// all listeners are passed etc. the dialog takes care of cleaning up
				AnalyzerSettingsDialog configureDialog = new AnalyzerSettingsDialog(
						((Dialog) getTopLevelAncestor()), cc, listeners);

				WindowLocationAndSizeManager.postInit(configureDialog, "AnalyzerSettingsDialog", 400, 300);
				configureDialog.setVisible(true);// blocks

				WindowLocationAndSizeManager.storeLocationAndSizePreferences(configureDialog, "AnalyzerSettingsDialog");

			}
		}
	}

	/**
	 * Checks if the analyzers in the list are actually instances of
	 * {@link ConfigurationChangeListener}.
	 *
	 * @param analyzers list of {@link TextAnalyzer} objects
	 * @return a list of {@link ConfigurationChangeListener} objects
	 */
	private List<ConfigurationChangeListener> toListenersList(List<TextAnalyzer> analyzers) {
		if (analyzers != null && !analyzers.isEmpty()) {
			List<ConfigurationChangeListener> changeListeners = new ArrayList<ConfigurationChangeListener>();
			for (TextAnalyzer ta : analyzers) {
				if (ta instanceof ConfigurationChangeListener) {
					changeListeners.add((ConfigurationChangeListener) ta);
				}
			}
			return changeListeners;
		}
		return null;
	}

	/**
	 * Get the targetList from the given string
	 *
	 * @param target, string in which the targets are comma separated
	 * @return targetList
	 */
	public List<String> getTargetList(String target){
		List<String> targetList = null;
		if(target != null){
			targetList = new ArrayList<String>();
			String[] targets = target.split(",");
			for(String s : targets){
				targetList.add(s);
			}
		}
		return targetList;

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == configAnalyzerButton) {
			int row = configsTable.getSelectedRow();
			if (row > -1) {
				configureAnalyzerAtRow(row);
			}
		}
		else if (e.getSource() == closeButton) {
			Window parWindow = SwingUtilities.getWindowAncestor(this);
			if (parWindow != null) {
				parWindow.setVisible(false);
				parWindow.dispose();
			}
		}
		else if (e.getSource() == removeConfigButton) {
			int row = configsTable.getSelectedRow();
			if (row > -1) {
				int selOption = JOptionPane.showConfirmDialog(this, ElanLocale.getString("InterlinearAnalyzerConfigPanel.RemoveWarning"),
						ElanLocale.getString("Message.Warning"), JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.WARNING_MESSAGE, null);
				if (selOption != JOptionPane.OK_OPTION) {
					return;
				}

				AnalyzerConfig ac = (AnalyzerConfig) configsModel.getValueAt(row, CONFIG_COL);
				// a type config which usually has 1 or more tier sub-configs to be removed as well
				if (ac instanceof AnalyzerTypeConfig) {
					configsModel.removeRow(row);
					manager.getTextAnalyzerContext().removeConfig(ac);
					repaint();
				}
			}
		}
		else if (e.getSource() == editConfigButton) {
			AnalyzerConfigDialog configDialog = null;
			Container w = this.getTopLevelAncestor();
			if (w instanceof Dialog) {
				configDialog = new AnalyzerConfigDialog((Dialog) w, manager);
			} else {
				configDialog = new AnalyzerConfigDialog((Frame) w, manager);
			}
			WindowLocationAndSizeManager.postInit(configDialog, "AnalyzerConfigurationsDlg", 400, 300);
			configDialog.setVisible(true);
			WindowLocationAndSizeManager.storeLocationAndSizePreferences(configDialog, "AnalyzerConfigurationsDlg");//ConfigureAnalyzersDlg

			if (configDialog.isApplied()) {
				// assumes the dialog can contain a mixture of tier based
				// and type based configurations
				updateConfigurations(configDialog.getConfigurationMap(false), false);
				updateConfigurations(configDialog.getConfigurationMap(true), true);
			}
		} else if (e.getSource() == showTiersCB) {
			anRenderer.setShowTiers(showTiersCB.isSelected());
			configsChanged();
		}
	}


	@Override
	public void componentHidden(ComponentEvent e) {
		// stub
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		// stub
	}

	@Override
	public void componentResized(ComponentEvent e) {
		revalidate();
		updateRowHeights();
	}

	@Override
	public void componentShown(ComponentEvent e) {
		// stub
	}

	/**
	 * Responds to changes in selected table row by setting
	 * the active configuration.
	 * Might need to be reconsidered (obsolete?).
	 */
	@Override
	public void valueChanged(ListSelectionEvent e) {
		int row = configsTable.getSelectedRow();
		if (row > -1 && !e.getValueIsAdjusting()) {

			AnalyzerConfig ac = (AnalyzerConfig) configsModel.getValueAt(row, TIER_SUBCONFIG_COL);
			if (ac == null) {
				ac = (AnalyzerConfig) configsModel.getValueAt(row, CONFIG_COL);
			}
			if (ac != null) {
				manager.getInterEditor().setActiveConfiguration(ac);
			}
		}
		configAnalyzerButton.setEnabled(configsTable.getRowCount() > 0 && row > -1);
		if (row > -1) {
			AnalyzerTypeConfig atc = (AnalyzerTypeConfig)
					configsModel.getValueAt(row, CONFIG_COL);
			if (manager.getTextAnalyzerContext().isConfigurable(atc.getAnnotId())) {
				configAnalyzerButton.setText(ElanLocale.getString("InterlinearAnalyzerConfigPanel.Configure")
					+ " " + atc.getAnnotId().getName());
				configAnalyzerButton.setEnabled(true);
			} else {
				configAnalyzerButton.setText(ElanLocale.getString("InterlinearAnalyzerConfigPanel.ConfigureSettings"));
				configAnalyzerButton.setEnabled(false);
			}
		} else {
			configAnalyzerButton.setText(ElanLocale.getString("InterlinearAnalyzerConfigPanel.ConfigureSettings"));
		}

		removeConfigButton.setEnabled(configsTable.getRowCount() > 0 && row > -1);
	}

	private class TableMouseListener extends MouseAdapter {

		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getSource() == configsTable) {
				if (e.getClickCount() > 1) {
					int column = configsTable.columnAtPoint(e.getPoint());
					int row = configsTable.rowAtPoint(e.getPoint());
					if (column == ANALYZER_NAME_COL) {
						if (row > -1) {
							configureAnalyzerAtRow(row);
						}
					} else {
						editConfigButton.doClick();
					}
				}
			}
		}
	}


}
