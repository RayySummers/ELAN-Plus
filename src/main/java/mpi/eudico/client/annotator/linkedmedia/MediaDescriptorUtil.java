package mpi.eudico.client.annotator.linkedmedia;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLayoutManager;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.ElanMediaPlayerController;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.player.ElanMediaPlayer;
import mpi.eudico.client.annotator.player.EmptyMediaPlayer;
import mpi.eudico.client.annotator.player.NoPlayerException;
import mpi.eudico.client.annotator.recognizer.gui.AbstractRecognizerPanel;
import mpi.eudico.client.annotator.viewer.AbstractViewer;
import mpi.eudico.client.annotator.viewer.SignalViewer;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.util.MediaDescriptorUtility;
import nl.mpi.util.FileUtility;


/**
 * A utility class for creating, checking and updating media descriptors.
 *
 * @version 2020 part of this existing MediaDescriptorUtil has been split off 
 * into MediaDescriptorUtility in the "core" part of the code tree
 * 
 * @author Han Sloetjes
 */
public class MediaDescriptorUtil {
	/**
	 * Private constructor.
	 */
    private MediaDescriptorUtil() {
		super();
	}

	/**
     * Tries to update the mediaplayers in the viewermanager as well as the
     * layoutmanager and finally sets the mediadescriptors in the
     * transcription.
     *
     * @param transcription the Transcription with the old descriptors
     * @param descriptors the new media descriptors
     */
    public static void updateMediaPlayers(TranscriptionImpl transcription,
        List<MediaDescriptor> descriptors) {
        if ((transcription == null) || (descriptors == null)) {
            return;
        }

        long mediaTime = 0L;        

        ViewerManager2 viewerManager = ELANCommandFactory.getViewerManager(transcription);
        ElanLayoutManager layoutManager = ELANCommandFactory.getLayoutManager(transcription);
        SignalViewer signalViewer = layoutManager.getSignalViewer();
        
        // stop the player before destroying
        if(viewerManager.getMasterMediaPlayer() != null && 
        		viewerManager.getMasterMediaPlayer().isPlaying()){
        	viewerManager.getMasterMediaPlayer().stop();
	    }
        
        mediaTime = viewerManager.getMasterMediaPlayer().getMediaTime();
        
        ElanMediaPlayerController empc = viewerManager.getMediaPlayerController();

       	empc.deferUpdatePlayersVolumePanel(true);
       	
        // make sure all players are connected
        if (layoutManager.getMode() == ElanLayoutManager.SYNC_MODE) {
            layoutManager.connectAllPlayers();
        }

        // the master media player cannot be removed directly
        // replace the master; the master is added to the connected players
        viewerManager.setMasterMediaPlayer(new EmptyMediaPlayer(
                Integer.MAX_VALUE));

        // remove the connected
        List<ElanMediaPlayer> connectPlayers = viewerManager.getConnectedMediaPlayers();
        List<ElanMediaPlayer> remPlayers = new ArrayList<ElanMediaPlayer>(connectPlayers.size());

        remPlayers.addAll(connectPlayers);

        for (ElanMediaPlayer conPlay : remPlayers) {
            viewerManager.destroyMediaPlayer(conPlay);
        }
        for (ElanMediaPlayer conPlay : remPlayers) {
            layoutManager.remove(conPlay);
        }
        // The players are not actually finalized yet:
        // at least the remPlayers vector still refers to them.

        if (signalViewer != null) {
            viewerManager.destroyViewer(signalViewer);
            layoutManager.remove(signalViewer);
        }
        
        // create new players from the descriptors
        MediaDescriptorUtil.createMediaPlayers(transcription, descriptors);

        // After all these changes, we can now do all the updates to the connected volume sliders.
       	empc.deferUpdatePlayersVolumePanel(false);
        
        // check recognizer panel
        ArrayList<String> newAudioPaths = new ArrayList<String>(6);
        
        if (layoutManager.getSignalViewer() != null) {
        	newAudioPaths.add(layoutManager.getSignalViewer().getMediaPath());
        }
        
    	// there may be other audio files associated with the transcription
    	MediaDescriptor md;
    	for (int i = 0; i < descriptors.size(); i++) {  		
    		md = descriptors.get(i);
    		if (md.mimeType.equals(MediaDescriptor.GENERIC_AUDIO_TYPE) || md.mimeType.equals(MediaDescriptor.WAV_MIME_TYPE) ) {    			
    			if (!newAudioPaths.contains(md.mediaURL)) {
    			    newAudioPaths.add(md.mediaURL);
    			}
    		}
    	}
    	
    	
    	final AbstractRecognizerPanel recognizerPanel = viewerManager.getRecognizerPanel();
		if (recognizerPanel != null) {
    		// could check here for changes compared to the old setup
    		recognizerPanel.setAudioFilePaths(newAudioPaths);     			
    	}
    	
    	viewerManager.setAudioPaths(newAudioPaths);    	
    		
    	
    	 //check video recognizer panel
        ArrayList<String> newVideoPaths = new ArrayList<String>(6);
                 	
    	for (int i = 0; i < descriptors.size(); i++) {  		
    		md = descriptors.get(i);
    		if (!md.mimeType.equals(MediaDescriptor.GENERIC_AUDIO_TYPE) && !md.mimeType.equals(MediaDescriptor.WAV_MIME_TYPE) ) {    			
    			if (!newVideoPaths.contains(md.mediaURL)) {
    				newVideoPaths.add(md.mediaURL);
    			}
    		}
    	}
    	
    	if (recognizerPanel != null) {
    			recognizerPanel.setVideoFilePaths(newVideoPaths);     			
    	} 
    	viewerManager.setVideoPaths(newVideoPaths);
    	
        viewerManager.getMasterMediaPlayer().setMediaTime(mediaTime); 
        transcription.setMediaDescriptors(descriptors);
        transcription.setChanged();
        
        layoutManager.doLayout();
    }
    
    /**
     * Updates the time offsets of players and connected viewers in case the time offset has changed.
     * 
     * @param transcription the transcription to be updated
     * @param newPlayerOffsets the new offsets, mappings of url to offset
     */
    public static void updateMediaPlayerOffsets(TranscriptionImpl transcription, Map<String, Long> newPlayerOffsets) {
    	if (transcription == null || newPlayerOffsets == null) {
    		return; // log
    	}
    	
    	List<MediaDescriptor> changedDescs = new ArrayList<MediaDescriptor>(10);
    	MediaDescriptor md;
    	
    	for (int i = 0; i < transcription.getMediaDescriptors().size(); i++) {
    		md = transcription.getMediaDescriptors().get(i);
    		Long newOffset = newPlayerOffsets.get(md.mediaURL);
    		if (newOffset != null && newOffset != md.timeOrigin) {
    			changedDescs.add(md);
    			md.timeOrigin = newOffset;
    		}
    	}
    	
    	if (changedDescs.size() == 0) {
    		return; // log...
    	}
    	transcription.setChanged();
    	
        ViewerManager2 viewerManager = ELANCommandFactory.getViewerManager(transcription);
        ElanLayoutManager layoutManager = ELANCommandFactory.getLayoutManager(transcription);
        
        if (viewerManager.getMasterMediaPlayer().isPlaying()) {
        	viewerManager.getMasterMediaPlayer().stop();
        }
        boolean masterOffsetChanged = changedDescs.contains(viewerManager.getMasterMediaPlayer().getMediaDescriptor());
    	long curMediaTime = viewerManager.getMasterMediaPlayer().getMediaTime();
    	
    	ElanMediaPlayer player;
    	for (int i = 0; i < viewerManager.getConnectedMediaPlayers().size(); i++) {
    		player = viewerManager.getConnectedMediaPlayers().get(i);
    		
    		if (changedDescs.contains(player.getMediaDescriptor())) {
    			player.setOffset(player.getMediaDescriptor().timeOrigin);// media descriptor has already been updated
    		}
    	}

    	if (layoutManager.getSignalViewer() != null) {
    		// the SignalViewer may or may not have a MediaDescriptor
    		if (changedDescs.contains(layoutManager.getSignalViewer().getMediaDescriptor())) {
    			layoutManager.getSignalViewer().setOffset(layoutManager.getSignalViewer().getMediaDescriptor().timeOrigin);
    		} else {
	    		String wavUrl = FileUtility.pathToURLString(layoutManager.getSignalViewer().getMediaPath());
	    		Long nextOffset = newPlayerOffsets.get(wavUrl);
	    		
	    		if (nextOffset != null) {
	    			layoutManager.getSignalViewer().setOffset(nextOffset); // could be unchanged
	    		}
    		}
    	}
    	
    	if (viewerManager.getSpectrogramViewer() != null) {
    		// the SpectrogramViewer may or may not have a MediaDescriptor, let the viewer check its offset
    		viewerManager.getSpectrogramViewer().mediaOffsetChanged();
    	}
    	
    	if (masterOffsetChanged) {
    		long masterOffset = viewerManager.getMasterMediaPlayer().getMediaDescriptor().timeOrigin;
    		viewerManager.getMasterMediaPlayer().setOffset(masterOffset);
    		// notify viewers? duration changed as a result of change of offset
    		AbstractViewer viewer;
    		viewer = viewerManager.getAnnotationDensityViewer();
    		if (viewer != null) {
    			viewer.mediaOffsetChanged();
    		}

    		viewerManager.getMasterMediaPlayer().setMediaTime(curMediaTime);
    	}
    }
    

    /**
     * Tries to create the mediaplayers in the viewermanager as well as the
     * layoutmanager and finally sets the mediadescriptors in the
     * transcription.
     *
     * @param transcription the Transcription with the old descriptors
     * @param descriptors the new media descriptors
     */
    public static void createMediaPlayers(TranscriptionImpl transcription,
        List<MediaDescriptor> descriptors) {
        if ((transcription == null) || (descriptors == null)) {
            return;
        }

        int numDesc = descriptors.size();

        try {
            ViewerManager2 viewerManager = ELANCommandFactory.getViewerManager(transcription);
            ElanLayoutManager layoutManager = ELANCommandFactory.getLayoutManager(transcription);         
          
            int nrOfPlayers = 0;
            int nrVisualPlayers = 0;
            String signalSource = null;
            long signalOffset = 0;

            viewerManager.getMediaPlayerController().deferUpdatePlayersVolumePanel(true);

            MediaDescriptor curMD;
            ArrayList<MediaDescriptor> failedPlayers = null;
            StringBuilder errors = new StringBuilder();
            
            for (int i = 0; i < numDesc; i++) {
                curMD = descriptors.get(i);

                if (!curMD.isValid) {
                	continue;
                }
                
                if (!MediaDescriptorUtility.checkLinkStatus(curMD)) {
                    continue;
                } 
                                
                if(!curMD.mimeType.equals(MediaDescriptor.WAV_MIME_TYPE) &&
                		nrVisualPlayers == Constants.MAX_VISIBLE_PLAYERS){
                	continue;
                }
               

                try {
                    ElanMediaPlayer player = viewerManager.createMediaPlayer(curMD);                    			
                    
                    if(player == null){
                    	continue;
                    }
                    
                    nrOfPlayers++;

                    if (nrOfPlayers == 1) {
                        // here comes the mastermedia player
                        viewerManager.setMasterMediaPlayer(player);
                    }
                    if (signalSource == null && curMD.mimeType.equals(MediaDescriptor.WAV_MIME_TYPE)) {                    	
                            signalSource = curMD.mediaURL;
                            signalOffset = curMD.timeOrigin;
                            // HS Aug 2008: pass this player to the viewermanager; important for synchronisation mode
                            viewerManager.setSignalSourcePlayer(player);
                    }
                    
                    if (player.getVisualComponent() != null && !(curMD.mimeType.equals(MediaDescriptor.WAV_MIME_TYPE))) {
                    	nrVisualPlayers++;
                    }
                    // only add layoutable players to the layout
                    // add no more than 4 visual players
                    if (nrVisualPlayers <= Constants.MAX_VISIBLE_PLAYERS || player.getVisualComponent() == null) {
                    	layoutManager.add(player);
                    	//System.out.println("Player Added... " + System.currentTimeMillis());
                    }

                } catch (NoPlayerException npe) {
                    if (failedPlayers == null) {
                        failedPlayers = new ArrayList<MediaDescriptor>();
                    }
                    errors.append(npe.getMessage() + "\n");
                    failedPlayers.add(curMD);
                }
            }

            if (nrOfPlayers == 0) {
            	if (viewerManager.getMasterMediaPlayer() instanceof EmptyMediaPlayer) {
            		((EmptyMediaPlayer) viewerManager.getMasterMediaPlayer()).setMediaDuration(
            				transcription.getLatestTime());
            	} else {
	                viewerManager.setMasterMediaPlayer(new EmptyMediaPlayer(
	                        transcription.getLatestTime()));
                }
                layoutManager.add(viewerManager.getMasterMediaPlayer());
            }

            if (signalSource == null && numDesc > 0) {
            	MediaDescriptor md = descriptors.get(0);
            	if (md.isValid) {
            		signalSource = md.mediaURL;
            		signalOffset = md.timeOrigin;
            	}
            }
            
            // Create a signal viewer
            if (signalSource != null) {
                SignalViewer newSignalViewer = viewerManager.createSignalViewer(signalSource);
                if(newSignalViewer != null){
                	newSignalViewer.setOffset(signalOffset);
                	newSignalViewer.preferencesChanged();
                
                	layoutManager.add(newSignalViewer);
                }
            }

           	viewerManager.getMediaPlayerController().deferUpdatePlayersVolumePanel(false);

            layoutManager.doLayout();

            // inform the user of failures...
            if ((failedPlayers != null) && (failedPlayers.size() > 0)) {
                StringBuilder sb = new StringBuilder(
                        "No player could be created for:\n");

                for (int i = 0; i < failedPlayers.size(); i++) {
                    sb.append("- " +
                        failedPlayers.get(i).mediaURL +
                        "\n");
                }
                sb.append(errors.toString());

                JOptionPane.showMessageDialog(ELANCommandFactory.getRootFrame(transcription), sb.toString(),
                    ElanLocale.getString("Message.Warning"),
                    JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception rex) {
            rex.printStackTrace();
            JOptionPane.showMessageDialog(ELANCommandFactory.getRootFrame(transcription), 
            		"An error occurred while creating media players: " + rex.getMessage(),
                    ElanLocale.getString("Message.Warning"),
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Creates a single media player for a media descriptor. For customized
     * player  creates, e.g. when showing (recreating) a player that has
     * previously been destroyed.
     *
     * @param transcription the transcription
     * @param curMD the media descriptor
     *
     * @return a player or null
     */
    public static ElanMediaPlayer createMediaPlayer(
        TranscriptionImpl transcription, MediaDescriptor curMD) {
        if ((transcription == null) || (curMD == null)) {
            return null;
        }

        if (!MediaDescriptorUtility.checkLinkStatus(curMD)) {
            return null;
        }

        ElanMediaPlayer player = null;

        try {
            ViewerManager2 viewerManager = ELANCommandFactory.getViewerManager(transcription);

            player = viewerManager.createMediaPlayer(curMD);
            

        } catch (NoPlayerException npe) {
        }

        return player;
    }
}
