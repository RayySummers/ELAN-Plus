package mpi.eudico.server.corpora.clomimpl.subtitletext.dto;

import mpi.eudico.server.corpora.clom.Transcription;

public record SubtitleEncoderInfo(
    SubtitleFile subtitleFile,
    Transcription transcription,
    InputFilter inputFilter,
    SubtitleTransformationOptions subtitleTransformationOptions
) {

    public static SubtitleEncoderInfo withTargetFileAndExportTier(
        SubtitleEncoderInfo subtitleEncoderInfo,
        String tierName
    ) {
        return new SubtitleEncoderInfo(
            SubtitleFile.withTierName(subtitleEncoderInfo.subtitleFile, tierName),
            subtitleEncoderInfo.transcription,
            InputFilter.withTier(subtitleEncoderInfo.inputFilter, tierName),
            subtitleEncoderInfo.subtitleTransformationOptions
        );
    }

}
