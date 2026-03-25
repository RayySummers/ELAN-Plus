package mpi.eudico.client.annotator.gui;

import mpi.eudico.client.annotator.util.WindowLocationAndSizeManager;

import java.awt.*;

/**
 * Display an audio spectrogram for a user-selected audio-interval.
 *
 * @author Allan van Hulst
 */
@SuppressWarnings("serial")
public class AudioSpectrogramDialog extends ClosableDialog {
    private AudioSpectrogramPanel spectrogramPanel;
    
    /**
     * Constructor.
     *
     * @param owner the owner frame
     * @param spectrogramPanel the panel to show
     */
    public AudioSpectrogramDialog(Frame owner, AudioSpectrogramPanel spectrogramPanel) {
        super(owner, false);

        this.spectrogramPanel = spectrogramPanel;

        initComponents();
        setTitle("Audio Spectrogram");
        WindowLocationAndSizeManager.postInit(this, "SpectrogramDetailWindow", 600, 400);
    }
    
    private void initComponents() {
    	//mainPanel = new JPanel(new GridBagLayout());
    	Container mainPanel = getContentPane();
    	mainPanel.setLayout(new BorderLayout());
    	mainPanel.add(spectrogramPanel, BorderLayout.CENTER);
    }

    /**
     * When the dialog is closed, size and location preferences are stored.
     * 
     * @param visible the flag to make the dialog visible or to hide it
     */
	@Override
	public void setVisible(boolean visible) {
		if (!visible) {
			WindowLocationAndSizeManager.storeLocationAndSizePreferences(this, "SpectrogramDetailWindow");
			spectrogramPanel.destroyPlayer();
		}
		super.setVisible(visible);
	}
    
    

}
