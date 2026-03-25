package mpi.eudico.client.annotator.mediadisplayer;

import mpi.eudico.client.annotator.mediadisplayer.MediaDisplayerFactory.MEDIA_TYPE;

/**
 * An interface for media providers.
 * 
 * @author michahulsbosch
 */
public interface MediaProvider {
	/**
	 * Returns a media bundle based on the provided parameters.
	 * 
	 * @param type the media type
	 * @param args the parameters for the media bundle, containing e.g. the URL
	 * 
	 * @return a {@code MediaBundle} instance
	 */
	public MediaBundle getMedia(MediaDisplayerFactory.MEDIA_TYPE type, Object[] args);
	
	/**
	 * Returns whether the provider provides a given media type.
	 * 
	 * @param mediaType the media type to test
	 * @return {@code true} if the provider supports the specified media type,
	 * {@code false} otherwise
	 */
	public Boolean providesType(MEDIA_TYPE mediaType);
	
	/**
	 * Returns the preferred media type of this provider.
	 * 
	 * @return the preferred media type
	 */
	public MEDIA_TYPE getPreferredMediaType();
}
