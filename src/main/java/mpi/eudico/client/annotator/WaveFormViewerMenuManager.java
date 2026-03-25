package mpi.eudico.client.annotator;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import mpi.eudico.client.annotator.gui.TextExtraOptionPane;
import mpi.eudico.client.annotator.player.WAVSamplerFactory;
import mpi.eudico.client.annotator.util.FrameConstants;
import mpi.eudico.client.annotator.viewer.SignalViewer;
import mpi.eudico.client.annotator.viewer.SignalViewerControlPanel;
import mpi.eudico.client.util.SelectableObject;
import mpi.eudico.client.util.WAVSamplesProvider;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.util.MediaDescriptorUtility;
import nl.mpi.util.FileUtility;

/**
 * A class that manages a list of WAVE files in a menu and communicates changes
 * to the {@code SignalViewerControlPanel}.
 *
 * @author aarsom
 * @version 1.0
 * @version 2.0, April 2014 : Communicates with the SignalViewerControlPanel
 */
public class WaveFormViewerMenuManager {
    private ElanFrame2 frame;
    private Transcription transcription;

    // all wave forms that are present in the menu, stored as Selectable objects,
    // holding a MediaDescriptor and a selection flag
    private List<SelectableObject<MediaDescriptor>> menuWaveForm;
    
    //<mediarul, action>
    private HashMap<String, AbstractAction> actionMap;
    
    private SignalViewerControlPanel signalViewerControlPanel;
    
    private TextExtraOptionPane masterMediaWarningPanel;
    /**
     * Creates a new WaveFormViewerMenuManager instance.
     *
     * @param frame the frame containing the menu's, players and viewers
     * @param transcription the transcription loaded in the frame, containing
     *        the media descriptors
     */
    public WaveFormViewerMenuManager(ElanFrame2 frame, Transcription transcription) {
        super();
        this.frame = frame;
        this.transcription = transcription;
        menuWaveForm = new ArrayList<SelectableObject<MediaDescriptor>>();
        actionMap = new HashMap<String, AbstractAction>();
        
        masterMediaWarningPanel = new TextExtraOptionPane();        
    }
    
    /**
     * Returns a list of WAV media descriptors.
     * 
     * @return a list of media descriptors wrapped in {@code SelectableObject}s
     */
    public List<SelectableObject<MediaDescriptor>> getWaveFormList(){
    	return menuWaveForm;
    }
    
    /**
     * Sets the  {@code SignalViewerControlPanel} to communicate with.
     * 
     * @param panel the control panel
     */
    public void setSignalViewerControlPanel(SignalViewerControlPanel panel){
    	signalViewerControlPanel = panel;
    }
    
    /**
     * Loads and applies stored preferences.
     */
    public void loadPreferences(){
    	String mediaURL = Preferences.getString("WaveFormViewer.ActiveURL", transcription);
    	AbstractAction action = actionMap.get(mediaURL);
    	if(mediaURL != null && action != null){
    		frame.setMenuSelected(mediaURL, FrameConstants.WAVE_FORM_VIEWER);
    		playerActionPerformed(action, null, true);
    	}
    }

    /**
     * Adds an action for each video media descriptor to the {@code View -> Media
     * Player} menu and sets the selected and enabled state if possible.
     */
    public void initWaveFormViewerMenu() {       
        List<MediaDescriptor> descriptors = transcription.getMediaDescriptors();
        MediaDescriptor md;
        String fileName;
        int visibles = 0;
        
        actionMap.clear();

        for (int i = 0; i < descriptors.size(); i++) {
            md = descriptors.get(i);
            
            if( md.mimeType != null && (md.mimeType.equals(MediaDescriptor.WAV_MIME_TYPE) ||
            		md.mimeType.equals(MediaDescriptor.GENERIC_AUDIO_TYPE))){  
            	 boolean curValid = MediaDescriptorUtility.checkLinkStatus(md);

                 // if we get here create an action and menuitem
                 fileName = FileUtility.fileNameFromPath(md.mediaURL);

                 PlayerAction action = new PlayerAction(md.mediaURL, fileName);

                 if (!curValid) {
                     action.setEnabled(false);
                 }

                 frame.addActionToMenu(action, FrameConstants.WAVE_FORM_VIEWER, -1);
                 actionMap.put(md.mediaURL, action);

                 if (visibles == 0 ) {
                     frame.setMenuSelected(md.mediaURL, FrameConstants.WAVE_FORM_VIEWER);
                     menuWaveForm.add(new SelectableObject<MediaDescriptor>(md, true));
                     visibles++;
                 } else {
                 	menuWaveForm.add(new SelectableObject<MediaDescriptor>(md, false));
                 }
            }
        }
        // Jan 2021 add non-wave files to the list after the wave files
        for (int i = 0; i < descriptors.size(); i++) {
            md = descriptors.get(i);
            
            if( md.mimeType != null && !md.mimeType.equals(MediaDescriptor.WAV_MIME_TYPE) &&
            		!md.mimeType.equals(MediaDescriptor.GENERIC_AUDIO_TYPE)){  
            	 boolean curValid = MediaDescriptorUtility.checkLinkStatus(md);

                 // if we get here create an action and menuitem
                 fileName = FileUtility.fileNameFromPath(md.mediaURL);

                 PlayerAction action = new PlayerAction(md.mediaURL, fileName);

                 if (!curValid) {
                     action.setEnabled(false);
                 }

                 frame.addActionToMenu(action, FrameConstants.WAVE_FORM_VIEWER, -1);
                 actionMap.put(md.mediaURL, action);

                 if (visibles == 0 ) {
                     frame.setMenuSelected(md.mediaURL, FrameConstants.WAVE_FORM_VIEWER);
                     menuWaveForm.add(new SelectableObject<MediaDescriptor>(md, true));
                     visibles++;
                 } else {
                 	menuWaveForm.add(new SelectableObject<MediaDescriptor>(md, false));
                 }
            }
        }
        
        loadPreferences();
        
        if(signalViewerControlPanel != null){
        	signalViewerControlPanel.initViewerPopUPMenu();
        }
    }

    /**
     * Called after a change in the linked media files.
     * It <ul>
     * <li>removes all player menu items 
     * <li>adds new menu items for new players
     * </ul> 
     */
    public void reinitializeWaveFormMenu() {
        //remove current menu items

        for (int i = 0; i < menuWaveForm.size(); i++) {
            SelectableObject<MediaDescriptor> sob = menuWaveForm.get(i);
            MediaDescriptor md = sob.getValue();            

            frame.removeActionFromMenu(md.mediaURL, FrameConstants.WAVE_FORM_VIEWER);
        }

        // new players have been created, populate the players menu again 
        menuWaveForm.clear();
        initWaveFormViewerMenu();
    }
    
    /**
     * Performs the player action for the given mediaURL and
     * updates the waveform viewer menu of the frame.
     *
     * @param mediaURL the URL for which the action should be performed
     * @param e the event
     */
    public void performActionFor(String mediaURL, ActionEvent e){
    	frame.setMenuSelected(mediaURL, FrameConstants.WAVE_FORM_VIEWER);
    	playerActionPerformed(actionMap.get(mediaURL), e);
    }

    /**
     * Updates the WAV file in the the signal viewer. Also
     * checks and updates the master media player if necessary.
     *
     * @param action the action that received the event
     * @param e the event
     */
    void playerActionPerformed(AbstractAction action, ActionEvent e) {
    	Boolean boolPref = Preferences.getBool("WaveFormViewer.SupressMasterMediaWarning", null);
    	boolean supressWarning = false;
    	if (boolPref != null) {
    		supressWarning = boolPref.booleanValue();
    	}
    	
    	playerActionPerformed(action, e, supressWarning);
    	
    }
    
    /**
     * Updates the WAV file in the the signal viewer. Also
     * checks and updates the master media player if necessary.
     *
     * @param action the action that received the event
     * @param e the event
     * @param suppressWarning if {@code true} no warning message will be shown
     * if the WAV file is (associated with) the master media file
     */
    void playerActionPerformed(AbstractAction action, ActionEvent e, boolean suppressWarning) {
        if (action != null) {
        	
        	String url = (String) action.getValue(Action.LONG_DESCRIPTION);
        	
        	//check if master media is a wave file
        	if(!suppressWarning && isMasterMediaWavFile()){
        		// check if the current Url is the mastermedia
        		if(!isMasterMediaPlayer(url)){
        			masterMediaWarningPanel.setMessage(
        					"<html>" +
        					ElanLocale.getString("WaveFormViewer.MasterMedia.Warn1") + "<br>" +
        			        ElanLocale.getString("WaveFormViewer.MasterMedia.Warn2") + "<br>"+
        					"<br>"+ "</html>");
            		int s = JOptionPane.showConfirmDialog(frame, 
            				masterMediaWarningPanel, 
            				ElanLocale.getString("Message.Warning "),JOptionPane.YES_NO_OPTION,
            				JOptionPane.WARNING_MESSAGE);
            		if(s == JOptionPane.NO_OPTION){
            			return;
            		}
            		
            		Preferences.set("WaveFormViewer.SupressMasterMediaWarning", 
            				masterMediaWarningPanel.getDontShowAgain(), null);
        		} 
        	}
        	
        	// update the wave form viewer list
            MediaDescriptor newMD = null;
            MediaDescriptor md = null;
            SelectableObject<MediaDescriptor> sob = null;

            for (int i = 0; i < menuWaveForm.size(); i++) {
                 sob = menuWaveForm.get(i);
                 md = sob.getValue();
                   
                 if(sob.isSelected() && !md.mediaURL.equals(url)){
              	   sob.setSelected(false);
                 }

                 if (md.mediaURL.equals(url) && newMD == null ) {
                     sob.setSelected(true);
                     newMD = md;
                 }
            }

            // item is selected
            if (newMD != null) {
            	if(signalViewerControlPanel != null){
                	signalViewerControlPanel.updateWaveFormPanel(newMD.mediaURL);
                }
            	
            	SignalViewer viewer = frame.getViewerManager().getSignalViewer();
            	if(viewer != null){
            		WAVSamplesProvider sampler = WAVSamplerFactory.createWAVSamplesProvider(newMD);
            		if (sampler != null) {
            			viewer.setMediaSampler(sampler, newMD);
            			//viewer.setOffset(newMD.timeOrigin);
                    	frame.getViewerManager().updateSignalViewerMedia(newMD.mediaURL);     
                    	
                    	//store preference
                    	Preferences.set("WaveFormViewer.ActiveURL", newMD.mediaURL, transcription);
            		} else {
                		// previous implementation might work. Otherwise the menu selection,see above, should maybe be reversed.
            			viewer.setMediaDescriptor(newMD);
//                		viewer.setMedia(newMD.mediaURL);
//                		viewer.setOffset(newMD.timeOrigin);
                    	frame.getViewerManager().updateSignalViewerMedia(newMD.mediaURL);     
                    	
                    	//store preference
                    	Preferences.set("WaveFormViewer.ActiveURL", newMD.mediaURL, transcription);
            		}

            	}
            }
        }
    }

    /**
     * Checks whether the given {@code mediaUrl} is the master media.
     *
     * @param mediaURL the media descriptor
     */
    private boolean isMasterMediaPlayer(String mediaURL) {
        if (mediaURL != null) {
        	MediaDescriptor otherMd = frame.getViewerManager().getMasterMediaPlayer().getMediaDescriptor();

            if ((otherMd != null) && mediaURL.equals(otherMd.mediaURL)) {
            	return true;
            }
        }        
        return false;
    }
    
    /**
     * Checks if the master media is a WAV file.
     * 
     * @return {@code true} if the main media file is a WAV file
     *
     */
    private boolean isMasterMediaWavFile() {
        MediaDescriptor otherMd = frame.getViewerManager().getMasterMediaPlayer().getMediaDescriptor();

        if ((otherMd != null) && otherMd.mimeType.equals(MediaDescriptor.WAV_MIME_TYPE)) {
        	return true;
        }	
        return false;
    }

    /**
     * An action class for menu items in the media players menu.
     *
     * @author Han Sloetjes
     */
    @SuppressWarnings("serial")
	class PlayerAction extends AbstractAction {
        /**
         * Creates a new PlayerAction instance
         *
         * @param fileUrl the full URL of the media file
         * @param fileName the file name of the media file
         */
        PlayerAction(String fileUrl, String fileName) {
            putValue(Action.NAME, fileName);
            // use LONG_DESCRIPTION or DEFAULT ?
            putValue(Action.LONG_DESCRIPTION, fileUrl);
        }

        /**
         * Handles selection and deselection of a player. Delegates the action 
         * to this (enclosing) manager.
         *
         * @param e action event
         */
        @Override
		public void actionPerformed(ActionEvent e) {
            WaveFormViewerMenuManager.this.playerActionPerformed(this, e);
        }
    }
}
