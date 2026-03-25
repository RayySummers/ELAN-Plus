package mpi.eudico.client.annotator.interlinear.edit;

import java.awt.Color;
import mpi.eudico.client.annotator.Constants;

/**
 * Some constants for the IGTViewer and related classes.
 * The pixel values for sizes etc. are default values. When applying these
 * might need to scale along with other components.
 *
 * @author Han Sloetjes
 */
public interface IGTConstants {
	/*
	 * General text-to-boundary margins. The boundary can be a side of a
	 * bounding box or the boundary of (a part of) the rendering area.
	 */
	/** left margin for text */
	public static final int TEXT_MARGIN_LEFT = 4;
	/** right margin for text */
	public static final int TEXT_MARGIN_RIGHT = 4;
	/** top margin for text */
	public static final int TEXT_MARGIN_TOP = 2;
	/** bottom margin of text */
	public static final int TEXT_MARGIN_BOTTOM = 2;
	/**
	 * Width of an indentation of a level in a tree like representation.
	 */
	public static final int INDENTATION_SIZE = 10;

	/* Margin spaces between tier lines and rows within one block and between
	 * annotations on the same row. */
	/** vertical margin between rows within a block */
	public static final int VERTICAL_ROW_MARGIN = 4;
	/** vertical margin between lines within a row (line spacing) */
	public static final int VERTICAL_LINE_MARGIN = 2;
	/** the width in pixels of a white space */
	public static final int WHITESPACE_PIXEL_WIDTH = 8;
	/** space between suggestions, horizontal and vertical */
	public static final int SUGGESTION_MARGIN = 12;
	/** a factor for a calculating a color for the selected and editing row */
	public static final float SELECTED_ROW_FACTOR = Constants.DARK_MODE ? 0.3f : 0.7f;
	/** background color for the selected row and/or the editing row */
	public final static Color EDIT_BG_COLOR = Constants.DARK_MODE ?
			new Color(Math.max(0, Constants.DEFAULTBACKGROUNDCOLOR.getRed() - 5),
					Math.max(0, Constants.DEFAULTBACKGROUNDCOLOR.getGreen() - 5),
					Math.min(255, Constants.DEFAULTBACKGROUNDCOLOR.getBlue() + 45)) :
						new Color(200, 200, 255);

	/* Some default colors for the IGT viewer */
	/** color for the border of an annotation's bounding box */
	public static final Color ANNO_BORDER_COLOR = Constants.DARK_MODE ? 
			new Color(Math.min(255, Constants.DEFAULTBACKGROUNDCOLOR.getRed() + 60),
					Math.min(255, Constants.DEFAULTBACKGROUNDCOLOR.getGreen() + 60),
					Math.min(255, Constants.DEFAULTBACKGROUNDCOLOR.getBlue() + 60)) : 
			Color.LIGHT_GRAY;
	/** color for the border of the active annotation's bounding box */
	public static final Color ACTIVE_ANNO_BORDER_COLOR = Constants.DARK_MODE ? 
			Color.CYAN : Color.BLUE;
	/** color for the background of an annotation's bounding box */
	public static final Color ANNO_BACKGROUND_COLOR = Constants.DARK_MODE ? 
			Constants.LIGHTBACKGROUNDCOLOR : new Color(255, 255, 245);
	/** color for the foreground of an annotation, used for the text */
	public static final Color ANNO_FOREROUND_COLOR = Constants.DEFAULTFOREGROUNDCOLOR;
	/** first background color of the table the IGT blocks are in (even rows) */
	public static final Color TABLE_BACKGROUND_COLOR1 = Constants.DARK_MODE ? 
			Constants.DEFAULTBACKGROUNDCOLOR : Color.WHITE;
	/** second background color of the table the IGT blocks are in (odd rows) */
	public static final Color TABLE_BACKGROUND_COLOR2 = Constants.DARK_MODE ?
			new Color(Math.min(255, Constants.LIGHTBACKGROUNDCOLOR.getRed() + 20),
					Math.min(255, Constants.LIGHTBACKGROUNDCOLOR.getGreen() + 20),
					Math.min(255, Constants.LIGHTBACKGROUNDCOLOR.getBlue() + 20)) :
			new Color(230, 230, 230);
	/** background color for the header of suggestions in the suggestion window */
	public static final Color SUGGESTION_HEADER_BACKGROUND = Constants.DARK_MODE ? 
			new Color(Math.min(255, Constants.LIGHTBACKGROUNDCOLOR.getRed()),
					Math.min(255, Constants.LIGHTBACKGROUNDCOLOR.getGreen()),
					Math.min(255, Constants.LIGHTBACKGROUNDCOLOR.getBlue() + 40)) : 
			new Color(200, 200, 240);
	/** background color for the block part of suggestions, the part containing the suggestion values */
	public static final Color SUGGESTION_BLOCK_BACKGROUND = Constants.DARK_MODE ? 
			new Color(Math.min(255, Constants.DEFAULTBACKGROUNDCOLOR.getRed()),
					Math.min(255, Constants.DEFAULTBACKGROUNDCOLOR.getGreen()),
					Math.min(255, Constants.DEFAULTBACKGROUNDCOLOR.getBlue() + 30)) :
			new Color(225, 225, 255);

	/** flag for the visibility of the border of an annotation's bounding box */
	public static final boolean SHOW_ANNOTATION_BORDER = true;
	/** flag for the visibility of the background of an annotation's bounding box */
	public static final boolean SHOW_ANNOTATION_BACKGROUND = false;

	/* a number of keys for identifying properties and storing Preferences */
	// corresponds to TABLE_BACKGROUND_COLOR1
	/** property key for the first background color (even) */
	public static final String KEY_BACKGROUND_COLOR_EVEN   = "InterlinearEditor.BackgroundColor.Even";
	// corresponds to TABLE_BACKGROUND_COLOR2
	/** property key for the second background color (odd) */
	public static final String KEY_BACKGROUND_COLOR_ODD    = "InterlinearEditor.BackgroundColor.Odd";
	/** property key for the annotation border color */
	public static final String KEY_ANN_BORDER_COLOR        = "InterlinearEditor.Annotation.BorderColor";
	/** property key for the annotation background color */
	public static final String KEY_ANN_BACKGROUND_COLOR    = "InterlinearEditor.Annotation.BackgroundColor";
	/** property key for the annotation border visibility */
	public static final String KEY_ANN_BORDER_VIS_FLAG     = "InterlinearEditor.PaintAnnotationBorders";
	/** property key for the annotation background visibility */
	public static final String KEY_ANN_BACKGROUND_VIS_FLAG = "InterlinearEditor.PaintAnnotationBackground";
	/** property key for the left margin of text in its bounding box */
	public static final String KEY_BBOX_LEFT_MARGIN        = "InterlinearEditor.Annotation.BB.LeftMargin";
	/** property key for the top margin of text in its bounding box */
	public static final String KEY_BBOX_TOP_MARGIN         = "InterlinearEditor.Annotation.BB.TopMargin";
	/** property key for the width of a white space */
	public static final String KEY_WHITESPACE_WIDTH        = "InterlinearEditor.WhitespaceWidth";

}
