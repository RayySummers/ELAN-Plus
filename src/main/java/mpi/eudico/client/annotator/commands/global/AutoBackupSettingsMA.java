package mpi.eudico.client.annotator.commands.global;

import java.awt.event.ActionEvent;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.prefs.gui.EditPrefsDialog;

/**
 *  A menu action that pre-loads the Automatic backup settings panel in the 
 *  Edit Preferences dialog 
 *
 */
public class AutoBackupSettingsMA extends FrameMenuAction {
	
	/** serial version uid */
	private static final long serialVersionUID = -5527295095638129114L;

	
	/**
	 * Creates a new AutoBackupSettingsMA instance
	 * 
	 * @param name  the name of the action
	 * @param frame the parent frame
	 */
	public AutoBackupSettingsMA(String name, ElanFrame2 frame) {
		super(name, frame);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		EditPrefsDialog dialog = new EditPrefsDialog(frame, true);
		dialog.preLoadPanel("PreferencesDialog.Category.AutomaticBackup");
		dialog.setVisible(true);
	}
	

}
