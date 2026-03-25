package mpi.eudico.server.corpora.clomimpl.json;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.json.JSONArray;
import org.json.JSONObject;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.AnnotationDocEncoder;
import mpi.eudico.server.corpora.clom.EncoderInfo;
import mpi.eudico.server.corpora.clom.Property;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import static mpi.eudico.server.corpora.clomimpl.json.WAConstants.*;
import static mpi.eudico.server.corpora.util.ServerLogger.LOG;
import mpi.eudico.util.TimeFormatter;

/**
 * An encoder that converts a transcription to JSON text. It can return the text
 * as a String or write it to File. 
 * <p>
 * This implementation is based on the {@code org.json} package.
 * 
 * @author Allan van Hulst
 */
public class JSONWAEncoder implements AnnotationDocEncoder {

	/**
	 * Creates a new encoder.
	 */
	public JSONWAEncoder() {
		super();
	}

	/**
	 * Creates a JSON String based on the encoder settings and writes it to 
	 * the specified path
	 * @param transcription the transcription to export
	 * @param encoderInfo the settings for the encoder
	 * @param tierOrder an ordered list of selected tiers, if null the EncoderInfo's list
	 *   of selected tiers is used
	 * @param path the file path to export to 
	 */
	@Override
	public void encodeAndSave(Transcription transcription, EncoderInfo encoderInfo, List<TierImpl> tierOrder,
			String path) throws IOException {
		if (transcription == null) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "Error while exporting to JSON: transcription is null");
			}
			throw new IllegalArgumentException("The transcription is null");
		}
		if (path == null) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "Error while exporting to JSON: output path is null");
			}
			throw new IllegalArgumentException("No export path for the JSON file specified");
		}
		/* ensure non-null encoder of the correct type */
		JSONWAEncoderInfo waEncoderInfo;
		if (encoderInfo == null)
			waEncoderInfo = new JSONWAEncoderInfo();
		else 
			waEncoderInfo = (JSONWAEncoderInfo) encoderInfo;
		/* update the encoder info's selected tier list */
		List<String> selTierNames = waEncoderInfo.getSelectedTiers();
		if (tierOrder != null) 
			waEncoderInfo.setSelectedTiers(toTierNames(tierOrder));
		
		/* create string and export */
		String content = createJSONText(transcription, waEncoderInfo);
		
		FileWriter writer = null;
		try {
		    writer = new FileWriter(path, Charset.forName("UTF-8"));
		    writer.write(content);
		} catch (IOException ioe) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "Error while exporting to JSON: " + ioe.getMessage());
			}
			throw ioe;
		} finally {
			try {
				writer.close();
			} catch (Throwable t) {}		
		}
		
		/* revert changes to the encoder info */
		waEncoderInfo.setSelectedTiers(selTierNames);
	}
	
    /**
     * Construct the JSON-representation of the transcript.
     *
     * @param transcription the transcription to convert
     * @param encoderInfo object containing settings for the conversion
     * 
     * @return The JSON data as a String.
     */
	public String createJSONText(Transcription transcription, JSONWAEncoderInfo encoderInfo) {
		if (transcription == null) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "Error while creating JSON text: no transcription");
			}
			return "";
		}
		if (encoderInfo == null) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "Error while creating JSON text: no encoder settings");
			}
			return "";
		}
		
		final MediaDescriptor missingDesc = new MediaDescriptor("./nomedia", MediaDescriptor.GENERIC_VIDEO_TYPE);
		List<MediaDescriptor> noFileList = new ArrayList<MediaDescriptor>(1);
		noFileList.add(missingDesc);
		
		JSONObject contJSON = new JSONObject();
		JSONArray contextArray = new JSONArray();
		contextArray.put(WA_CONTEXT);
		contextArray.put(LDP_CONTEXT);
		
		contJSON.put(CONTEXT, contextArray);
		for (Property p : transcription.getDocProperties()) {
			if (p.getName().equals(ID)) {
				contJSON.put(ID, p.getValue());
			} else if (p.getName().equals(LABEL)) {
				contJSON.put(LABEL, p.getValue());
			}
		}
		contJSON.put(TYPE, CONTAINER);
		
		JSONArray json = new JSONArray();
		contJSON.put(CONTAINS, json);
		
		int id = 0;
		
		List <? extends Tier> tiers = transcription.getTiers();
		List <String> selected = encoderInfo.getSelectedTiers();
		/* if selected is null, export all tiers */
		if (selected == null)
			selected = toTierNames(tiers);
		
		/* Iterate through tiers */
		for (int i = 0 ; i < selected.size() ; i = i + 1) {
			//Tier tier = retrieveTier (selected.get(i), tiers);
			Tier tier = transcription.getTierWithId(selected.get(i));
			if (tier == null) {
				continue;
			}
			if (tier.getNumberOfAnnotations() == 0) {
				continue;
			}
			String language = null;
			if (tier.getLangRef() != null) {
				language = tier.getLangRef();
			}
			JSONObject tierJSON = new JSONObject();
			//LinkedHashMap<String, Object> tierProp = new LinkedHashMap<String, Object>();
			
			/* Set @context to the value defined in the WA-specification */
			tierJSON.put(CONTEXT, LDP_CONTEXT);
			
			/* Set the id of this tier */
			tierJSON.put(ID, transcription.getURN() + "#collection" + Integer.toString(i + 1));
			
			/* Set type to the value defined in the WA-specification */
			tierJSON.put(TYPE, ANN_COLLECTION);
			
			/* Set the creator element to the author of the transcription or the annotator of the tier */
			if (transcription.getAuthor() != null && !transcription.getAuthor().isEmpty())
				tierJSON.put(CREATOR, transcription.getAuthor());
			else if (tier.getAnnotator() != null && !tier.getAnnotator().isEmpty())
				tierJSON.put(CREATOR, tier.getAnnotator());
			
			/* Set the generator element*/
			if (encoderInfo.getGenerator() != null)
				tierJSON.put(GENERATOR, encoderInfo.getGenerator());
			
			/* Set the label to the name of this tier */
			tierJSON.put(LABEL, tier.getName());
			
			List <? extends Annotation> annotations = tier.getAnnotations();
			
			int total = 0;
			
			JSONObject firstJSON = new JSONObject ();
			
			/* Set type to AnnotationPage */
			firstJSON.put(TYPE, ANN_PAGE);
			
			/* Add the ID for this first object (note: there is only 1 annotation page per iteration) 
			 * Make collection-page separator dependent on URL/IRI format */
			firstJSON.put(ID, transcription.getURN() + "#collection" + Integer.toString(i + 1) + "-page1");
			
			/* Set startindex to zero (this refers to the "items" array) */
			firstJSON.put(START_INDEX, "0");
			
			/* All individual annotations are collected in this array */
			JSONArray items = new JSONArray();
			
			/* Iterate through the annotations in this tier */
			for (int j = 0 ; j < annotations.size() ; j = j + 1) {
			
				Annotation annot = annotations.get(j);
			
				/* Handle the limitation of the export to the current selection */
				if (!withinSelection(annot, encoderInfo.getBeginTime(), encoderInfo.getEndTime()))
					continue;
			
				JSONObject annotJSON = new JSONObject();
				
				/* Annotations must have the @context property */
				annotJSON.put(CONTEXT, WA_CONTEXT);
				
				/* Only count number of annotations that fall within selection */
				total = total + 1;
			
				/* Add type Annotation to the JSON object for this annotation */
				annotJSON.put(TYPE, ANNOTATION);
			
				/* Set annotation.id value to the incremental id */
				if (encoderInfo.isIncrementalIDs()) {
					id = id + 1;
					annotJSON.put(ID, Integer.toString(id));
				} else
					annotJSON.put(ID, transcription.getURN () + "#" + annot.getId());
			
				JSONObject bodyJSON = new JSONObject ();
				
				/* Define this type to be a TextualBody */
				bodyJSON.put(TYPE, TEXTUAL_BODY);
			
				/* Set body.value to the contents of the annotation */
				bodyJSON.put(VALUE, annot.getValue());
			
				/* Set body.purpose if provided e.g. "transcribing" */
				if (encoderInfo.getPurpose() != null)
					bodyJSON.put(PURPOSE, encoderInfo.getPurpose());
			
				/* Set body.format to "text/plain" unless specified differently */
				if (encoderInfo.getBodyFormat() != null)
					bodyJSON.put(FORMAT, encoderInfo.getBodyFormat());					
				else
					bodyJSON.put(FORMAT, TEXT_FORMAT);
				
				if (language != null) 
					bodyJSON.put(LANGUAGE, language);
			
				/* Add the JSON body object to the JSON annotation object */
				annotJSON.put(BODY, bodyJSON);
			
				/* We will create a target the first or for each of the media files associated with this transcription */
				List <MediaDescriptor> files = transcription.getMediaDescriptors();
				// if there are no media descriptors, use a dummy descriptor
				if (files == null || files.isEmpty()) {
					files = noFileList;
				}
				
				if (encoderInfo.isSingleTargetExport()) {
					if (files.size() > 0) {
						annotJSON.put(TARGET, createTargetObject(files.get(0), 
								encoderInfo, annot.getBeginTimeBoundary(), annot.getEndTimeBoundary()));
					}
				} else {
					/* start with an array of targets */
					JSONArray targets = new JSONArray();
					/* Iterate through media descriptors */
					for (int k = 0 ; k < files.size() ; k = k + 1) {
				
						/* Add target JSON element to the array of targets */
						targets.put(createTargetObject(files.get(k), encoderInfo, 
								annot.getBeginTimeBoundary(), annot.getEndTimeBoundary()));
					}
				
					/* add one target object if there is only one media file, add the array if there are more */
					if (targets.length() == 1) {
						annotJSON.put(TARGET, targets.get(0));
					} else if (targets.length() > 1) {
						annotJSON.put(TARGET, targets);
					}
				}
			
				/* Add the current annotation object to the items array */
				items.put(annotJSON);
				
				if (encoderInfo.isEncodePreview() && total > 4) {
					JSONObject remark = new JSONObject();
					remark.put("Note: ", String.format("Limiting the number of annotations for the preview to %d",
							total));
					items.put(remark);
					break;
				}
			}
			
			/* Add the array of items to the "first" object */
			firstJSON.put(ITEMS, items);
			
			/* Add the "first" object to the tier object */
			tierJSON.put(FIRST, firstJSON);
			
			/* Set the total to the number of annotations in this tier */
			tierJSON.put(TOTAL, total);
			
			/* Add the JSON object for this tier to the JSON array */
			/* Providing a LinkedHashMap to the constructor of JSONObject does not
			 * result in a predictable output order in toString; the map is copied */
			//json.put(new JSONObject(tierProp));
			json.put(tierJSON);
		}
		
		/* Return container JSON object formatted with indentation-level 2 */
		return contJSON.toString(encoderInfo.getIndentationLevel());
	}
	

    /**
     * Helper function to lookup a selected tier in the list
     *
     * @param name The name of the tier for lookup
     * @param tiers The list of tiers to search
     * @return The Tier object with the specified name
     */
    private Tier retrieveTier (String name, List <? extends Tier> tiers) {
        for (int i = 0 ; i < tiers.size(); i = i + 1)
                if (tiers.get(i).getName().equals(name))
                        return tiers.get(i);

        return null;
    }
    
    /**
     * Check whether an annotation lies within the current selection.
     * 
     * @param ann The annotation to check.
     * @param selectionBegin the begin time of the selection
     * @param selectionEnd the end time of the selection
     * 
     * @return {@code true} if the annotation is within the selection boundaries
     */
    private boolean withinSelection (Annotation ann, long selectionBegin, long selectionEnd) {

        return (selectionBegin <= ann.getBeginTimeBoundary() &&
                        ann.getEndTimeBoundary() <= selectionEnd);
    }
    
    /**
     * 
     * @param tierList a list of tiers
     * @return a list of tier names
     */
    private List<String> toTierNames(List<? extends Tier> tierList) {
    	List<String> nameList = new ArrayList<String>(tierList.size());
    	for (Tier t : tierList)
    		nameList.add(t.getName());
    		
    	return nameList;
    }
    
    /**
     * Creates a JSONObject for a {@code target} element.
     * 
     * @param md the source of the target, a media file, not {@code null}
     * @param encoderInfo the encoder settings, not {@code null}
     * @param fragBT the begin time of the fragment
     * @param fragET the end time of the fragment
     * 
     * @return the configured {@code target} object 
     */
    private JSONObject createTargetObject(MediaDescriptor md, 
    		JSONWAEncoderInfo encoderInfo, long fragBT, long fragET) {
    	/* Start of target element */
		JSONObject targetJSON = new JSONObject();

		/* Add annotation timespan using the selector construct, if desired */
		if (encoderInfo.isFragmentSelector()) {
			/* Add target.source element in this case */
			targetJSON.put(SOURCE, md.mediaURL);
			
			JSONObject selectorJSON = new JSONObject ();

			/* Add the conformsTo element to the selector */
			selectorJSON.put(CONFORMS_TO, MEDIA_SELECTOR);

			/* Add the type for this target selector */
			selectorJSON.put(TYPE, FRAG_SELECTOR);

			/* Specify begin and end times for this selector */
			selectorJSON.put(VALUE, "t=" + TimeFormatter.toSSMSString(fragBT + md.timeOrigin) + "," +
					TimeFormatter.toSSMSString(fragET + md.timeOrigin));

			/* Add timespan-representation to target JSON object */
			targetJSON.put(SELECTOR, selectorJSON);
		} else {
			/* Add target.id element */
			String timeSpan = 
				"#t=" + TimeFormatter.toSSMSString(fragBT + md.timeOrigin) + "," +
				TimeFormatter.toSSMSString(fragET + md.timeOrigin);
			targetJSON.put(ID, md.mediaURL + timeSpan);
		}

		/* Add target.format element (i.e. the MIME type) */
		targetJSON.put(FORMAT, md.mimeType);
		
		/* Add target.type element (i.e. VIDEO or SOUND type) */
		if (md.mimeType.startsWith("video")) {
			targetJSON.put(TYPE, VIDEO);
		} else if (md.mimeType.startsWith("audio")) {
			targetJSON.put(TYPE, AUDIO);
		}
		
    	return targetJSON;
    }
}
