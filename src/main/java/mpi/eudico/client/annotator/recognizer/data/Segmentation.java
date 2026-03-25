package mpi.eudico.client.annotator.recognizer.data;

import java.util.ArrayList;
import java.util.List;

/**
 * A segmentation consists of:
 * <ul>
 *  <li>a name
 * 	<li>an ArrayList of Segments
 *  <li>an ArrayList of MediaDescriptors
 *  </ul>
 * The Name should be chosen unique if a recognizer produces more than one Segmentation
 * 
 * The Segments should contain Segment objects that have no time overlap
 * 
 * Some recognizers work with more than one media file (combined video and audio)
 * or they use more than one channel (channel1 and channel2 in a stereo audio file)
 * These recognizer should add more than one MediaDescriptor to the Segmentation
 * 
 *  
 * @author albertr
 *
 */
public class Segmentation {
	private String name;
	private List<RSelection> segments;
	private ArrayList<MediaDescriptor> mediaDescriptors;

	/**
	 * Construct a Segmentation
	 * 
	 * @param name the Segmentations name
	 * @param segments the list of Segment objects
	 * @param mediaFilePath the file path of the media file the recognizer used
	 */
	public Segmentation(String name, List<RSelection> segments, String mediaFilePath) {
		this.name = name;
		this.segments = segments;
		MediaDescriptor descriptor = new MediaDescriptor();
		descriptor.mediaFilePath = mediaFilePath;
		descriptor.channel = 1;
		mediaDescriptors = new ArrayList<MediaDescriptor>();
		mediaDescriptors.add(descriptor);
	}

	/**
	 * 
	 * Construct a Segmentation
	 * 
	 * @param name the Segmentations name
	 * @param segments the list of Segment objects
	 * @param mediaFilePath the file path of the media file the recognizer used
	 * @param channel the channel of the media file the recognizer used 
	 */
	public Segmentation(String name, List<RSelection> segments, String mediaFilePath, int channel) {
		this.name = name;
		this.segments = segments;
		MediaDescriptor descriptor = new MediaDescriptor();
		descriptor.mediaFilePath = mediaFilePath;
		descriptor.channel = channel;
		mediaDescriptors = new ArrayList<MediaDescriptor>();
		mediaDescriptors.add(descriptor);
	}
	
	/**
	 * Construct a Segmentation
	 * 
	 * @param name the Segmentations name
	 * @param segments the list of Segment objects
	 * @param descriptor the MediaDescriptor that describes the media file used by the recognizer
	 */
	public Segmentation(String name, List<RSelection> segments, MediaDescriptor descriptor) {
		this.name = name;
		this.segments = segments;
		mediaDescriptors = new ArrayList<MediaDescriptor>();
		mediaDescriptors.add(descriptor);
	}

	/**
	 * Add another MediaDescriptor to the Segmentation. 
	 * Should be used for recognizers that use more than one media file or more than one channel
	 * 
	 * @param descriptor the MediaDescriptor that describes the media file used by the recognizer
	 */
	public void addMediaDescriptor(MediaDescriptor descriptor) {
		mediaDescriptors.add(descriptor);
	}
	
	/**
	 * Returns the name of this collection of segments.
	 * 
	 * @return the Segmentations name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns a list of segments.
	 * 
	 * @return the ArrayList with Segment objects
	 */
	public List<RSelection> getSegments() {
		return segments;
	}

	/**
	 * Returns a list of media descriptors.
	 * 
	 * @return the ArrayList with MediaDescriptor objects
	 */
	public ArrayList<MediaDescriptor> getMediaDescriptors() {
		return mediaDescriptors;
	}
}