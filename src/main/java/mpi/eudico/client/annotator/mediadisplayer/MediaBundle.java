package mpi.eudico.client.annotator.mediadisplayer;

/**
 * A class to transfer media data of any kind (so far only a URL).
 * @author michahulsbosch
 *
 */
public class MediaBundle {
	private String mediaUrl;

	/**
	 * Creates a new bundle instance.
	 */
	public MediaBundle() {
		super();
	}
	
	/**
	 * Returns the media URL.
	 * @return the media URL
	 */
	public String getMediaUrl() {
		return mediaUrl;
	}

	/**
	 * Sets the media URL.
	 * @param mediaUrl the new media URL
	 */
	public void setMediaUrl(String mediaUrl) {
		this.mediaUrl = mediaUrl;
	}
	
}
