/*
 * Created on Oct 12, 2004
 *
 */
package mpi.eudico.server.corpora.clom;

import java.io.IOException;
import java.util.List;

import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;

/**
 * To be implemented for every annotation document format that is to
 * be written. Used by TranscriptionStore to encode and write to the
 * desired output format.
 * 
 * @author hennie
 *
 */
public interface AnnotationDocEncoder {

	/**
	 * Saves the transcription in a specific format, e.g. by creating a DOM and
	 * serializing it to an XML file.
	 * @param theTranscription the transcription to be saved
	 * @param theEncoderInfo information for the encoder, depends on the output
	 * format
	 * @param tierOrder a preferred tier order, if {@code null} the order in 
	 * the transcription is used, maintained
	 * @param path the file path for the output 
	 * @throws IOException any IO related exception
	 */
	public void encodeAndSave(Transcription theTranscription, EncoderInfo theEncoderInfo, 
	        List<TierImpl> tierOrder, String path) throws IOException;
}
