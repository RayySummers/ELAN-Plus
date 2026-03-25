package mpi.eudico.server.corpora.clomimpl.transcriber;

import mpi.eudico.server.corpora.clom.DecoderInfo;

/**
 * A decoder info object for Transcriber files.
 */
public class TranscriberDecoderInfo implements DecoderInfo {
    private boolean singleTierForSpeakers = false;
    private String sourceFilePath;
    
    /**
     * Creates a new TranscriberDecoderInfo object
     * @param sourceFilePath the Transcriber file
     */
    public TranscriberDecoderInfo(String sourceFilePath) {
        this.sourceFilePath = sourceFilePath;
    }
       
    /**
     * @return the path to the Transcriber file
     * @see mpi.eudico.server.corpora.clom.DecoderInfo#getSourceFilePath()
     */
    @Override
	public String getSourceFilePath() {
        return sourceFilePath;
    }
    
    /**
     * Returns whether each speaker should have its own tier.
     * 
     * @return true if all utterances should be stored on one tier
     */
    public boolean isSingleSpeakerTier() {
        return singleTierForSpeakers;
    }
    
    /**
     * Sets whether each speaker should have its own tier.
     * 
     * @param singleTierForSpeakers if true all utterances will be created on one tier 
     */
    public void setSingleSpeakerTier(boolean singleTierForSpeakers) {
        this.singleTierForSpeakers = singleTierForSpeakers;
    }
}
