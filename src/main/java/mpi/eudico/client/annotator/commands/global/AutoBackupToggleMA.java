package mpi.eudico.client.annotator.commands.global;

import java.awt.event.ActionEvent;

import javax.swing.JCheckBoxMenuItem;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.PreferencesListener;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.commands.BackupCA;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.server.corpora.clom.Transcription;

/**
 * A menu action that manages the Automatic backup ON/OFF settings
 *
 */
public class AutoBackupToggleMA extends FrameMenuAction implements PreferencesListener {

	/** serial version uid */
	private static final long serialVersionUID = -4475539797909113072L;

	/**
	 * Creates a new AutoBackupToggleMA instance
	 * 
	 * @param name  the name of the action
	 * @param frame the parent frame
	 */
	public AutoBackupToggleMA(String name, ElanFrame2 frame) {
		super(name, frame);
		Preferences.addPreferencesListener(null, this);
	}

	/**
	 * Sets the preference setting when changed
	 *
	 * @param e the action event
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JCheckBoxMenuItem) {

			ViewerManager2 vm = frame.getViewerManager();
			Transcription transcription = null;
			BackupCA ca = null;

			if (vm != null && vm.getTranscription() != null) {
				transcription = vm.getTranscription();
				ca = ((BackupCA) ELANCommandFactory.getCommandAction(transcription, ELANCommandFactory.BACKUP));
			}

			if (commandId.equals(ELANCommandFactory.AUTOMATIC_BACKUP_TOGGLE_ON)) {
				Preferences.set("AutomaticBackupOn", true, null, true);
				Integer backupDelay = Preferences.getInt("BackUpDelay", null);
				if (backupDelay == null || (backupDelay.intValue() == 0)) {
					// set default value
					Preferences.set("BackUpDelay", Constants.BACKUP_1, null);
					backupDelay = Preferences.getInt("BackUpDelay", null);
					if (ca != null) {
						ca.setDelay(backupDelay);
					}
				} else if ((backupDelay != null) && (backupDelay.intValue() > 0)) {
					if (ca != null) {
						ca.setDelay(backupDelay);
					}
				}
			} else if (commandId.equals(ELANCommandFactory.AUTOMATIC_BACKUP_TOGGLE_OFF)) {
				Preferences.set("AutomaticBackupOn", false, null, true);
				// stop the backup task
				if (ca != null) {
					ca.stopBackUp();
				}
			}
		}
	}

	@Override
	public void preferencesChanged() {
		Boolean boolPref = Preferences.getBool("AutomaticBackupOn", null);

		if (boolPref != null) {
			if (boolPref.booleanValue()) {
				if (commandId.equals(ELANCommandFactory.AUTOMATIC_BACKUP_TOGGLE_ON)) {
					this.putValue(SELECTED_KEY, boolPref);
				}
			} else if (commandId.equals(ELANCommandFactory.AUTOMATIC_BACKUP_TOGGLE_OFF)) {
				this.putValue(SELECTED_KEY, !boolPref);
			}
		}

	}

}
