package mpi.eudico.server.corpora.clomimpl.subtitletext.dto;

public record SubtitleTransformationOptions(
    double frameRate,
    boolean reCalculateTime,
    boolean exportTiersSeparately) {
}
