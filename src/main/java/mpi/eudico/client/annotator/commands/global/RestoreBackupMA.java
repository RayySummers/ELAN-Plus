package mpi.eudico.client.annotator.commands.global;

import java.awt.event.ActionEvent;
import java.io.File;
import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.FrameManager;
import mpi.eudico.client.annotator.gui.FileChooser;
import nl.mpi.util.FileExtension;

/**
 * Action that allows to choose and open the backup files in the elanframe. 
 *
 */
@SuppressWarnings("serial")
public class RestoreBackupMA extends FrameMenuAction {

	/**
     * Creates a new RestoreBackupMA instance.
     *
     * @param name the name of the action (command)
     * @param frame the associated frame
     */
	public RestoreBackupMA(String name, ElanFrame2 frame) {
		super(name, frame);
	}
	
	/**
	 * Shows an .001, .002 etc back up files in the chooser and opens them in the new elan frame
	 *
	 * @param e the action event
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		FileChooser chooser = new FileChooser(frame);
		File selectedBackupFile = null;
		chooser.createAndShowFileDialog(ElanLocale.getString("Frame.ElanFrame.RestoreDialog.Title"),
				FileChooser.OPEN_DIALOG, FileExtension.BACKUP_EXT, "LastUsedEAFDir");
		
		selectedBackupFile = chooser.getSelectedFile();
		if (selectedBackupFile == null) {
			return;
		}
		
        createFrameForPath(selectedBackupFile.getAbsolutePath());
	}
	
    
    /**
     * Creates a new frame for the specified file path.
     * 
     * @param filePath the full path to backup file
     */
    public void createFrameForPath(String filePath) {
    	FrameManager.getInstance().createFrame(filePath);
    }

}
