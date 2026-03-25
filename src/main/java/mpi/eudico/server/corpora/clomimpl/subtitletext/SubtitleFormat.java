package mpi.eudico.server.corpora.clomimpl.subtitletext;

/**
 * An enumeration of subtitle formats.
 */
public enum SubtitleFormat {
	/** the SubRip format ({@code .srt}) */
	SUBRIP,
	/** the WebVTT format ({@code .vtt}), very similar to SubRip  */
	WEBVTT,
	/** the Audacity label format */
	AUDACITY_lABELS // a special case of tab delimited text
	// add other formats when needed
}
