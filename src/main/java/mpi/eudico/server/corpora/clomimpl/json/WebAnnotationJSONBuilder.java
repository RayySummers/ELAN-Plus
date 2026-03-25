package mpi.eudico.server.corpora.clomimpl.json;

import static mpi.eudico.server.corpora.clomimpl.json.WAConstants.*;

import static mpi.eudico.server.corpora.util.ServerLogger.LOG;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.json.JSONArray;
import org.json.JSONObject;

import mpi.eudico.client.annotator.util.ProgressListener;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.util.TimeFormatter;
import nl.mpi.util.FileUtility;


/**
 * Web annotation JSON Builder to 1. build the w3c web annotation requests for tier and their annotations
 * 2. forwards the Json to client
 * 3. forwards the returned ID's to be saved in database.
 */
public class WebAnnotationJSONBuilder {
	
	private Transcription transcription;
    private JSONWAEncoderInfo encoderInfo;
    private HSQLAnnotationMapperDB annotationMapperDB;
    
    private ArrayList<ProgressListener> listeners;
    
    private Map<String, Integer> exportedData = new HashMap<>();
    int exportedAnnotationsCount = 0;
	
    
    /**
     * Constructor which initializes the transcription, 
     * encoder info and HSQL database
     * @param transcription the transcription file object
     * @param encoderInfo encoder information object
     */
	public WebAnnotationJSONBuilder(Transcription transcription, JSONWAEncoderInfo encoderInfo) {
		this.transcription = transcription;
		this.encoderInfo = encoderInfo;
		annotationMapperDB = new HSQLAnnotationMapperDB();
		annotationMapperDB.createDatabaseTables();
	}
	

	/**
	 * Logic to perform the formation of JSON, forwarding the formed JSON to client 
	 * and forwarding the returned response to the hsql database
	 * @param authenticationKey the authentication bearer key if authentication is enabled 
	 * @return returns the map containing the tiers exported as keys and their number of annotations exported as values
	 * 
	 */
	public Map<String, Integer> exportAnnotationsToServer(String authenticationKey) {
		if (this.transcription == null) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "Error while creating JSON text: no transcription");
			}
			progressInterrupt("");
			return new HashMap<>();
		}
		if (this.encoderInfo == null) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "Error while creating JSON text: no encoder settings");
			}
			progressInterrupt("");
			return new HashMap<>();
		}
		//List <? extends Tier> tiers = transcription.getTiers();
		List <String> selectedTiers = encoderInfo.getSelectedTiers();
		
		/* if selected is null, export all tiers */
		if (selectedTiers.isEmpty()) {
			return new HashMap<>();
		}
		int progressCount = 0;
		int size = selectedTiers.size();
		
		int progressPercent = (100/size);
		WebAnnotationClient annotationClient = new WebAnnotationClient();
		
		for (String tierId : selectedTiers) {
			String collectionID = "";
			
			Tier tier = transcription.getTierWithId(tierId);
			
			String collectionJSON = constructW3CAnnotationCollection(tier);
			
			if (collectionJSON != null) {
				collectionID = annotationClient.exportAnnotationCollection(collectionJSON, authenticationKey);
				if (collectionID != null && !collectionID.isEmpty()) {
					exportedData.put(tier.getName(), exportedAnnotationsCount);
					annotationMapperDB.saveCollectionIDinDB(transcription, tier, collectionID);
					progressCount++;
					progressUpdate(progressPercent, "");
				}
			}
			if (tier.getNumberOfAnnotations() != 0 && collectionID != null && !collectionID.isEmpty()) {
				
				for (Annotation annotation: tier.getAnnotations()) {
					
					String annotationJson = constructW3CAnnotation(annotation , tier);
					String annotationID = annotationClient.exportAnnotation(annotationJson, collectionID, authenticationKey);
					
					if (collectionID != null && !collectionID.isEmpty() && annotationID != null && !annotationID.isEmpty()) {
						exportedAnnotationsCount = exportedAnnotationsCount + 1;
						annotationMapperDB.saveAnnotationIDinDB(transcription, annotation, annotationID, collectionID);
					}
				}
				exportedData.put(tier.getName(), exportedAnnotationsCount);
			}
		
			progressPercent = progressPercent + (100/size);
			
			if (progressCount == size) {
				progressComplete("");
			}

			exportedAnnotationsCount = 0;
		}
		
		try {
			Connection connection = DBConnection.getDBConnection();
			try (PreparedStatement closeStatement = connection.prepareStatement("SHUTDOWN")) {
				closeStatement.execute();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		DBConnection.closeDBConnection();
		
		return exportedData;
		
	}
	
	


    /**
     * Constructs the web annotation collection JSON model from the tier object
     * @param tier the tier object to create the annotation collection json
     * @return returns the annotation collection json as string
     */
	public String constructW3CAnnotationCollection(Tier tier) {

		if (tier == null) {
			return null;
		}
		
		JSONObject annoCollJSON = new JSONObject();
		
		JSONArray contextArray = new JSONArray();
		contextArray.put(WA_CONTEXT);
		contextArray.put(LDP_CONTEXT);
		annoCollJSON.put(CONTEXT,contextArray);
		
		JSONArray typeArray = new JSONArray();
		typeArray.put(BASIC_CONTAINER);
		typeArray.put(ANN_COLLECTION);
		annoCollJSON.put(TYPE, typeArray);
		
		annoCollJSON.put(LABEL, tier.getName());
		
		if (tier.getAnnotator() != null && !tier.getAnnotator().isEmpty()) {
			annoCollJSON.put(CREATOR, tier.getAnnotator());
		}
		else if (transcription.getAuthor() != null && !transcription.getAuthor().isEmpty()) {
			annoCollJSON.put(CREATOR, transcription.getAuthor());
		}
		
		return annoCollJSON.toString();
	}

	
	
    /**
     * Constructs the web annotation JSON model from annotation object
     * @param annotation the annotation object to create the annotation json
     * @param tier the tier object of the annotation to fetch some values
     * @return returns the annotation json as a string
     */
	public String constructW3CAnnotation(Annotation annotation , Tier tier) {
		
		final MediaDescriptor missingDesc = new MediaDescriptor("./nomedia", MediaDescriptor.GENERIC_VIDEO_TYPE);
		List<MediaDescriptor> noFileList = new ArrayList<MediaDescriptor>(1);
		noFileList.add(missingDesc);
		
		JSONObject annotJSON = new JSONObject();
		
		/* Annotations must have the @context property */
		annotJSON.put(CONTEXT, WA_CONTEXT);
		
		/* Add type Annotation to the JSON object for this annotation */
		annotJSON.put(TYPE, ANNOTATION);
		
		//annotJSON.put(GENERATOR, encoderInfo.getGenerator());
		
		if (tier.getAnnotator() != null && !tier.getAnnotator().isEmpty()) {
			annotJSON.put(CREATOR, tier.getAnnotator());
		}
		else if (transcription.getAuthor() != null && !transcription.getAuthor().isEmpty()) {
			annotJSON.put(CREATOR, transcription.getAuthor());
		}

		
		JSONObject bodyJSON = new JSONObject ();
		
		/* Define this type to be a TextualBody */
		bodyJSON.put(TYPE, TEXTUAL_BODY);
		
		/* Set body.value to the contents of the annotation */
		bodyJSON.put(VALUE, annotation.getValue());
		
		bodyJSON.put(PURPOSE, TRANSCRIBING);
		
		String language = null;
		if (tier.getLangRef() != null) {
			language = tier.getLangRef();
		}
		
		if (language != null) {
			bodyJSON.put(LANGUAGE, language);
		}
		
		/* Add the JSON body object to the JSON annotation object */
		annotJSON.put(BODY, bodyJSON);
		
		
		JSONObject targetJSON = new JSONObject();
		
		/* We will create a target the first or for each of the media files associated with this transcription */
		List <MediaDescriptor> files = transcription.getMediaDescriptors();
		// if there are no media descriptors, use a dummy descriptor
		if (files == null || files.isEmpty()) {
			files = noFileList;
		}
		if (files.size() > 0) {
			MediaDescriptor md = files.get(0);
			
			String source;
			
			if(md.mediaURL.startsWith("file:") || md.mediaURL.startsWith("///")) {
				source = FileUtility.fileNameFromPath(md.mediaURL);
			}else {
				source = md.mediaURL;
			}
			
			targetJSON.put(SOURCE, source);
			
			/* Add target.format element (i.e. the MIME type) */
			targetJSON.put(FORMAT, md.mimeType);
			
			/* Add target.type element (i.e. VIDEO or SOUND type) */
			if (md.mimeType.startsWith("video")) {
				targetJSON.put(TYPE, VIDEO);
			} else if (md.mimeType.startsWith("audio")) {
				targetJSON.put(TYPE, AUDIO);
			}
			
			JSONObject selectorJSON = new JSONObject ();
			
			/* Add the conformsTo element to the selector */
			selectorJSON.put(CONFORMS_TO, MEDIA_SELECTOR);
			
			/* Add the type for this target selector */
			selectorJSON.put(TYPE, FRAG_SELECTOR);
			
			/* Specify begin and end times for this selector */
			selectorJSON.put(VALUE, "t=" + TimeFormatter.toSSMSString(annotation.getBeginTimeBoundary() + md.timeOrigin) + "," +
					TimeFormatter.toSSMSString(annotation.getEndTimeBoundary() + md.timeOrigin));

			/* Add timespan-representation to target JSON object */
			targetJSON.put(SELECTOR, selectorJSON);
		}
		
		annotJSON.put(TARGET, targetJSON);
		
		return annotJSON.toString();
	}
	
	 /**
     * Adds a ProgressListener to the list of ProgressListeners.
     *
     * @param pl the new ProgressListener
     */
    public synchronized void addProgressListener(ProgressListener pl) {
        if (listeners == null) {
            listeners = new ArrayList<ProgressListener>(2);
        }

        listeners.add(pl);
    }

    /**
     * Removes the specified ProgressListener from the list of listeners.
     *
     * @param pl the ProgressListener to remove
     */
    public synchronized void removeProgressListener(ProgressListener pl) {
        if ((pl != null) && (listeners != null)) {
            listeners.remove(pl);
        }
    }

    /**
     * Notifies any listeners of a progress update.
     *
     * @param percent the new progress percentage, [0 - 100]
     * @param message a descriptive message
     */
    private void progressUpdate(int percent, String message) {
        if (listeners != null) {
            for (int i = 0; i < listeners.size(); i++) {
                ((ProgressListener) listeners.get(i)).progressUpdated(this,
                    percent, message);
            }
        }
    }

    /**
     * Notifies any listeners that the process has completed.
     *
     * @param message a descriptive message
     */
    private void progressComplete(String message) {
        if (listeners != null) {
            for (int i = 0; i < listeners.size(); i++) {
                ((ProgressListener) listeners.get(i)).progressCompleted(this,
                    message);
            }
        }
    }

    /**
     * Notifies any listeners that the process has been interrupted.
     *
     * @param message a descriptive message
     */
    private void progressInterrupt(String message) {
        if (listeners != null) {
            for (int i = 0; i < listeners.size(); i++) {
                ((ProgressListener) listeners.get(i)).progressInterrupted(this,
                    message);
            }
        }
    }

	

}
