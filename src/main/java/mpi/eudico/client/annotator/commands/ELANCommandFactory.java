package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanLayoutManager;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.timeseries.TSTrackManager;
import mpi.eudico.server.corpora.clom.Transcription;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * The Command Factory is the central point for command constants, for creating and caching commands and command actions and
 * for the administration of the command history for undo and redo.
 */
public class ELANCommandFactory {

    private ELANCommandFactory() {
        // Hiding Constructor
    }

    // all HashMaps have Transcription as their key
    private static final Map<Transcription, Map<String, CommandAction>> commandActionHash = new HashMap<>();
    private static final Map<Transcription, UndoCA> undoCAHash = new HashMap<>();
    private static final Map<Transcription, RedoCA> redoCAHash = new HashMap<>();
    private static final Map<Transcription, CommandHistory> commandHistoryHash = new HashMap<>();
    private static final Map<Transcription, ViewerManager2> viewerManagerHash = new HashMap<>();
    private static final Map<Transcription, ElanLayoutManager> layoutManagerHash = new HashMap<>();
    private static final Map<Transcription, TSTrackManager> trackManagerHash = new HashMap<>();

    // root frame for dialogs
    private static final Map<Transcription, JFrame> rootFrameHash = new HashMap<>();

    // a table for the available languages
    private static final Map<String, Locale> languages = new HashMap<>();

    // list of commands/command actions
    /**
     * constant for set tier name command action
     */
    public static final String SET_TIER_NAME = "CommandActions.SetTierName";
    /**
     * constant for change tier menu
     */
    public static final String CHANGE_TIER = "Menu.Tier.ChangeTier";
    /**
     * constant for add new tier menu
     */
    public static final String ADD_TIER = "Menu.Tier.AddNewTier";
    /**
     * constant for delete tier menu
     */
    public static final String DELETE_TIER = "Menu.Tier.DeleteTier";
    /**
     * constant for delete tiers menu
     */
    public static final String DELETE_TIERS = "Menu.Tier.DeleteTiers";
    /**
     * constant for edit tier command action
     */
    public static final String EDIT_TIER = "CommandActions.EditTier";
    /**
     * constant for import tiers menu
     */
    public static final String IMPORT_TIERS = "Menu.Tier.ImportTiers";

    /**
     * constant for add participant to tier menu
     */
    public static final String ADD_PARTICIPANT = "Menu.Tier.AddParticipant";
    /**
     * constant for add participant dialog
     */
    public static final String ADD_PARTICIPANT_DLG = "AddParticipantDlg";

    /**
     * constant for add dependent tiers to existing tier structure
     */
    public static final String ADD_DEPENDENT_TIERS_TO_TIER_STRUCTURE = "Menu.Tier.AddDependentTiersToExistingStructure";
    /**
     * constant for add dependent tiers to existing tier structure command
     */
    public static final String ADD_DEPENDENT_TIERS_TO_TIER_STRUCTURE_CMD = "AddDependentTiersToExistingStructure.Command";
    /**
     * constant for delete participant menu
     */
    public static final String DELETE_PARTICIPANT = "Menu.Tier.DeleteParticipant";
    /**
     * constant for delete participant dialog
     */
    public static final String DELETE_PARTICIPANT_DLG = "DeleteParticipantDialog";

    /**
     * constant for edit type command action
     */
    public static final String EDIT_TYPE = "CommandActions.EditType";
    /**
     * constant for import types menu
     */
    public static final String IMPORT_TYPES = "Menu.Type.ImportTypes";
    /**
     * constant for add type menu
     */
    public static final String ADD_TYPE = "Menu.Type.AddNewType";
    /**
     * constant for change type menu
     */
    public static final String CHANGE_TYPE = "Menu.Type.ChangeType";
    /**
     * constant for delete type menu
     */
    public static final String DELETE_TYPE = "Menu.Type.DeleteType";

    /**
     * constant for add controlled vocabulary command action
     */
    public static final String ADD_CV = "CommandActions.AddCV";
    /**
     * constant for change controlled vocabulary command action
     */
    public static final String CHANGE_CV = "CommandActions.ChangeCV";
    /**
     * constant for delete controlled vocabulary command action
     */
    public static final String DELETE_CV = "CommandActions.DeleteCV";
    /**
     * constant for replace controlled vocabulary command action
     */
    public static final String REPLACE_CV = "CommandActions.ReplaceCV";
    /**
     * constant for merge controlled vocabulary command action
     */
    public static final String MERGE_CVS = "CommandActions.MergeCV";
    /**
     * constant for add controlled vocabulary entry command action
     */
    public static final String ADD_CV_ENTRY = "CommandActions.AddCVEntry";
    /**
     * constant for change controlled vocabulary entry command action
     */
    public static final String CHANGE_CV_ENTRY = "CommandActions.ChangeCVEntry";
    /**
     * constant for delete controlled vocabulary entry command action
     */
    public static final String DELETE_CV_ENTRY = "CommandActions.DeleteCVEntry";
    /**
     * constant for move controlled vocabulary entries
     */
    public static final String MOVE_CV_ENTRIES = "MoveEntries";
    /**
     * constant for replace controlled vocabulary entries
     */
    public static final String REPLACE_CV_ENTRIES = "ReplaceEntries";
    /**
     * constant for edit controlled vocabulary dialog
     */
    public static final String EDIT_CV_DLG = "Menu.Edit.EditCV";

    /**
     * constant for new annotation menu
     */
    public static final String NEW_ANNOTATION = "Menu.Annotation.NewAnnotation";
    /**
     * constant for new annotation recursive menu
     */
    public static final String NEW_ANNOTATION_REC = "Menu.Annotation.NewAnnotationRecursive";
    /**
     * constant for create depending annotations menu
     */
    public static final String CREATE_DEPEND_ANN = "Menu.Annotation.CreateDependingAnnotations";
    /**
     * constant for new annotation alt
     */
    public static final String NEW_ANNOTATION_ALT = "NA_Alt";
    /**
     * constant for new annotation before menu
     */
    public static final String NEW_ANNOTATION_BEFORE = "Menu.Annotation.NewAnnotationBefore";
    /**
     * constant for new annotation after menu
     */
    public static final String NEW_ANNOTATION_AFTER = "Menu.Annotation.NewAnnotationAfter";
    /**
     * constant for new annotation from begin and end time menu
     */
    public static final String NEW_ANNOTATION_FROM_BIGIN_END_TIME_DLG =
        "Menu.Annotation.NewAnnotationFromBeginEndTimeDialog";
    /* for a command that creates one or two annotations in an empty space, a gap, of a tier */
    /**
     * constant for new annotation in a gap menu
     */
    public static final String NEW_ANNOTATIONS_IN_GAP = "Menu.Annotation.NewAnnotationsInGap";
    /**
     * constant for modify annotation menu
     */
    public static final String MODIFY_ANNOTATION = "Menu.Annotation.ModifyAnnotation";
    /**
     * constant for modify annotation alt
     */
    public static final String MODIFY_ANNOTATION_ALT = "MA_Alt";
    /**
     * constant for modify annotations from data category menu
     */
    public static final String MODIFY_ANNOTATION_DC = "Menu.Annotation.ModifyAnnotationDatCat";
    /**
     * constant for modify annotations from data category dialog
     */
    public static final String MODIFY_ANNOTATION_DC_DLG = "ModifyAnnotationDCDlg";
    /**
     * constant for split annotation menu
     */
    public static final String SPLIT_ANNOTATION = "Menu.Annotation.SplitAnnotation";
    /**
     * constant for modify annotation dialog
     */
    public static final String MODIFY_ANNOTATION_DLG = "ModifyAnnotationDialog";
    /**
     * constant for remove annotation value menu
     */
    public static final String REMOVE_ANNOTATION_VALUE = "Menu.Annotation.RemoveAnnotationValue";
    /**
     * constant for modify annotation time dialog menu
     */
    public static final String MODIFY_ANNOTATION_TIME_DLG = "Menu.Annotation.ModifyAnnotationTimeDialog";
    /**
     * constant for modify annotation resource url menu
     */
    public static final String MODIFY_ANNOTATION_RESOURCE_URL = "Menu.Annotation.ModifyResourceURL";
    /**
     * constant for modify annotation resource url dialog
     */
    public static final String MODIFY_ANNOTATION_RESOURCE_URL_DLG = "ModifyResourceURLDlg";

    /**
     * constant for delete annotation menu
     */
    public static final String DELETE_ANNOTATION = "Menu.Annotation.DeleteAnnotation";
    /**
     * constant for delete annotation alt menu
     */
    public static final String DELETE_ANNOTATION_ALT = "DA_Alt";
    /**
     * constant for menu delete annotation in selection
     */
    public static final String DELETE_ANNOS_IN_SELECTION = "Menu.Annotation.DeleteAnnotationsInSelection";
    /**
     * constant for menu delete annotation in left
     */
    public static final String DELETE_ANNOS_LEFT_OF = "Menu.Annotation.DeleteAnnotationsLeftOf";
    /**
     * constant for menu delete annotation in right
     */
    public static final String DELETE_ANNOS_RIGHT_OF = "Menu.Annotation.DeleteAnnotationsRightOf";
    /**
     * constant for menu delete all annotations in the left
     */
    public static final String DELETE_ALL_ANNOS_LEFT_OF = "Menu.Annotation.DeleteAllLeftOf";
    /**
     * constant for menu delete all annotations in the right
     */
    public static final String DELETE_ALL_ANNOS_RIGHT_OF = "Menu.Annotation.DeleteAllRightOf";
    /**
     * constant for menu delete multiple annotations in selection
     */
    public static final String DELETE_MULTIPLE_ANNOS = "Menu.Annotation.DeleteSelectedAnnotations";

    /**
     * constant for copy annotation menu
     */
    public static final String COPY_ANNOTATION = "Menu.Annotation.CopyAnnotation";
    /**
     * constant for copy annotation tree menu
     */
    public static final String COPY_ANNOTATION_TREE = "Menu.Annotation.CopyAnnotationTree";
    /**
     * constant for paste annotation menu
     */
    public static final String PASTE_ANNOTATION = "Menu.Annotation.PasteAnnotation";
    /**
     * constant for paste annotation here menu
     */
    public static final String PASTE_ANNOTATION_HERE = "Menu.Annotation.PasteAnnotationHere";
    /**
     * constant for paste annotation tree menu
     */
    public static final String PASTE_ANNOTATION_TREE = "Menu.Annotation.PasteAnnotationTree";
    /**
     * constant for paste annotation tree here menu
     */
    public static final String PASTE_ANNOTATION_TREE_HERE = "Menu.Annotation.PasteAnnotationTreeHere";
    /**
     * constant for duplicate annotation menu
     */
    public static final String DUPLICATE_ANNOTATION = "Menu.Annotation.DuplicateAnnotation";
    /**
     * constant for duplicate remove annotation menu
     */
    public static final String DUPLICATE_REMOVE_ANNOTATION = "Menu.Annotation.DuplicateRemoveAnnotation";
    /**
     * constant for merge annotation with next menu
     */
    public static final String MERGE_ANNOTATION_WN = "Menu.Annotation.MergeWithNext";
    /**
     * constant for merge annotation with before menu
     */
    public static final String MERGE_ANNOTATION_WB = "Menu.Annotation.MergeWithBefore";
    /**
     * constant for move annotation to tier menu
     */
    public static final String MOVE_ANNOTATION_TO_TIER = "Menu.Annotation.MoveAnnotationToTier"; // not visible in ui
    /**
     * constant for add comment to annotation menu
     */
    public static final String ADD_COMMENT = "Menu.Annotation.AddComment";
    /**
     * constant for delete comment of annotation menu
     */
    public static final String DELETE_COMMENT = "Menu.Annotation.DeleteComment";
    /**
     * constant for change comment of annotation menu
     */
    public static final String CHANGE_COMMENT = "Menu.Annotation.ChangeComment";
    /**
     * constant for analyze annotation menu
     */
    public static final String ANALYZE_ANNOTATION = "Menu.Annotation.Analyze";
    /**
     * constant for add annotation to lexicon menu
     */
    public static final String ADD_TO_LEXICON = "Menu.Annotation.AddToLexicon";
    /**
     * constant for show annotation in the browser menu
     */
    public static final String SHOW_IN_BROWSER = "Menu.Annotation.ShowInBrowser";

    /**
     * constant for lexicon change annotations viewer
     */
    public static final String MODIFY_OR_ADD_DEPENDENT_ANNOTATIONS = "LexiconEntryViewer.ChangeAnnotations";

    /**
     * constant for copy to next annotation command action
     */
    public static final String COPY_TO_NEXT_ANNOTATION = "CommandActions.CopyToNextAnnotation";
    /**
     * constant for modify annotation time command action
     */
    public static final String MODIFY_ANNOTATION_TIME = "CommandActions.ModifyAnnotationTime";
    /**
     * constant for shift all annotations
     */
    public static final String SHIFT_ALL_DLG = "ShiftAllAnn";
    /**
     * constant for shift annotation dialog
     */
    public static final String SHIFT_ANN_DLG = "ShiftAnn";
    /**
     * constant for shift annotations of all tier dialog
     */
    public static final String SHIFT_ANN_ALLTIER_DLG = "ShiftAnnAllTier";
    /**
     * constant for shift annotations command action
     */
    public static final String SHIFT_ANNOTATIONS = "CommandActions.ShiftAnnotations";
    /**
     * constant for shift all annotations menu
     */
    public static final String SHIFT_ALL_ANNOTATIONS = "Menu.Annotation.ShiftAll";
    /**
     * constant for shift annotations from left to right command action
     */
    public static final String SHIFT_ALL_ANNOTATIONS_LROf = "CommandActions.ShiftAnnotationsLROf";
    /**
     * constant for shift active annotation menu
     */
    public static final String SHIFT_ACTIVE_ANNOTATION = "Menu.Annotation.ShiftActiveAnnotation";
    /**
     * constant for shift annotations in selection menu
     */
    public static final String SHIFT_ANNOS_IN_SELECTION = "Menu.Annotation.ShiftAnnotationsInSelection";
    /**
     * constant for shift annotations left menu
     */
    public static final String SHIFT_ANNOS_LEFT_OF = "Menu.Annotation.ShiftAnnotationsLeftOf";
    /**
     * constant for shift annotations right menu
     */
    public static final String SHIFT_ANNOS_RIGHT_OF = "Menu.Annotation.ShiftAnnotationsRightOf";
    /**
     * constant for shift all annotations left menu
     */
    public static final String SHIFT_ALL_ANNOS_LEFT_OF = "Menu.Annotation.ShiftAllLeftOf";
    /**
     * constant for shift all annotations right menu
     */
    public static final String SHIFT_ALL_ANNOS_RIGHT_OF = "Menu.Annotation.ShiftAllRightOf";

    /**
     * constant for transcription table column number
     */
    public static final String TRANS_TABLE_CLM_NO = "TranscriptionTable.Column.No";

    /* constants for viewers that can be shown or hidden in Annotation Mode */
    /**
     * constant for grid viewer menu
     */
    public static final String GRID_VIEWER = "Menu.View.Viewers.Grid";
    /**
     * constant for text viewer menu
     */
    public static final String TEXT_VIEWER = "Menu.View.Viewers.Text";
    /**
     * constant for subtitles viewer menu
     */
    public static final String SUBTITLE_VIEWER = "Menu.View.Viewers.Subtitles";
    /**
     * constant for lexicon viewer
     */
    public static final String LEXICON_VIEWER = "LexiconEntryViewer.Lexicon";
    /**
     * constant for comment viewer
     */
    public static final String COMMENT_VIEWER = "CommentViewer.Comment";
    /**
     * constant for recognizer menu
     */
    public static final String RECOGNIZER = "Menu.View.Viewers.Recognizer";
    /**
     * constant for meta data viewer menu
     */
    public static final String METADATA_VIEWER = "Menu.View.Viewers.MetaData";
    /**
     * constant for signal viewer menu
     */
    public static final String SIGNAL_VIEWER = "Menu.View.Viewers.Signal";
    /**
     * constant for spectrogram viewer menu
     */
    public static final String SPECTROGRAM_VIEWER = "Menu.View.Viewers.Spectrogram";
    /**
     * constant for interlinear viewer menu
     */
    public static final String INTERLINEAR_VIEWER = "Menu.View.Viewers.InterLinear";
    /**
     * constant for interlinearize lexicon viewer menu
     */
    public static final String INTERLINEAR_LEXICON_VIEWER = "Menu.View.Viewers.InterLinearize";
    /**
     * constant for time series viewer menu
     */
    public static final String TIMESERIES_VIEWER = "Menu.View.Viewers.TimeSeries";
    /**
     * constant for syntax viewer command action
     */
    public static final String SYNTAX_VIEWER = "CommandActions.SyntaxViewer";
    /**
     * constant for media player menu
     */
    public static final String MEDIA_PLAYERS = "Menu.View.MediaPlayer";
    /**
     * constant for waveforms menu
     */
    public static final String WAVEFORMS = "Menu.View.Waveform";
    /**
     * constant for viewers menu
     */
    public static final String VIEWERS = "Menu.View.Viewers";
    /* viewers that cannot be shown / hidden */
    /**
     * constant for time line viewer menu
     */
    public static final String TIMELINE_VIEWER = "Menu.View.Viewers.TimeLine";
    /**
     * constant for transcription viewer menu
     */
    public static final String TRANSCRIPTION_VIEWER = "Menu.View.Viewers.Transcription";
    /**
     * constant for annotation density viewer menu
     */
    public static final String ANNOTATION_DENSITY_VIEWER = "Menu.View.Viewers.AnnotationDensity";

    /**
     * constant for tokenize menu
     */
    public static final String TOKENIZE_DLG = "Menu.Tier.Tokenize";
    /**
     * constant for annotations on dependent tiers menu
     */
    public static final String ANN_ON_DEPENDENT_TIER = "Menu.Tier.AnnotationsOnDependentTiers";
    /**
     * constant for annotation from overlap menu
     */
    public static final String ANN_FROM_OVERLAP = "Menu.Tier.AnnotationsFromOverlaps";
    /**
     * constant for annotations from overlaps clas menu
     */
    public static final String ANN_FROM_OVERLAP_CLAS = "Menu.Tier.AnnotationsFromOverlapsClas";
    /**
     * constant for annotations from subtraction menu
     */
    public static final String ANN_FROM_SUBTRACTION = "Menu.Tier.AnnotationsFromSubtraction";
    /**
     * constant for annotations from gaps menu
     */
    public static final String ANN_FROM_GAPS = "Menu.Tier.AnnotationsFromGaps";
    //public static final String COMPARE_ANNOTATORS_DLG = "Menu.Tier.CompareAnnotators";
    /**
     * constant for change case menu
     */
    public static final String CHANGE_CASE = "Menu.Tier.ChangeCase";

    /* few constants only used as internal identifier for a command! */
    /**
     * constant for annotation on dependent tier
     */
    public static final String ANN_ON_DEPENDENT_TIER_COM = "AnnsOnDependentTier";
    /**
     * constant for annotation from overlap
     */
    public static final String ANN_FROM_OVERLAP_COM = "AnnsFromOverlaps";
    /**
     * constant for annotation from subtraction
     */
    public static final String ANN_FROM_SUBTRACTION_COM = "AnnsFromSubtraction";
    /**
     * constant for annotation from overlap
     */
    public static final String ANN_FROM_OVERLAP_COM_CLAS = "AnnsFromOverlapsClas";
    /**
     * constant for annotation from gaps
     */
    public static final String ANN_FROM_GAPS_COM = "AnnsFromGaps";
    /**
     * constant for change case
     */
    public static final String CHANGE_CASE_COM = "ChangeCase";
    /**
     * constant for modify all annotations boundaries
     */
    public static final String MODIFY_ALL_ANNOTATION_BOUNDARIES = "Menu.Tier.ModifyAllAnnotationBoundaries";
    /**
     * constant for modify all annotations boundaries command
     */
    public static final String MODIFY_ALL_ANNOTATION_BOUNDARIES_CMD = "CommandActions.ModifyAllAnnotationsBoundaries";

    /**
     * constant for merge tiers menu
     */
    public static final String MERGE_TIERS = "Menu.Tier.MergeTiers";
    /**
     * constant for merge tiers
     */
    public static final String MERGE_TIERS_COM = "MergeTiers";
    /**
     * constant for merge tiers class menu
     */
    public static final String MERGE_TIERS_CLAS = "Menu.Tier.MergeTiersClassic";
    /**
     * constant for merge tier dialog
     */
    public static final String MERGE_TIERS_DLG_CLAS = "MergeTiersDlgClas";
    /**
     * constant for merge tier menu
     */
    public static final String MERGE_TIER_GROUP = "Menu.Tier.MergeTierGroup";
    /**
     * constant for merge tier group dialog
     */
    public static final String MERGE_TIER_GROUP_DLG = "MergeTierGroupDlg";

    /**
     * constant for copy annotations of tier
     */
    public static final String COPY_ANN_OF_TIER = "Menu.Tier.CopyAnnotationsOfTier";
    /**
     * constant for copy annotations of tier dialog
     */
    public static final String COPY_ANN_OF_TIER_DLG = "Menu.Tier.CopyAnnotationsOfTierDialog";
    /**
     * constant for copy annotations of tier command
     */
    public static final String COPY_ANN_OF_TIER_COM = "Menu.Tier.CopyAnnotationsOfTierCommand";

    /**
     * constant for tokenize tier command
     */
    public static final String TOKENIZE_TIER = "CommandActions.Tokenize";
    /**
     * constant for regular annotation menu
     */
    public static final String REGULAR_ANNOTATION_DLG = "Menu.Tier.RegularAnnotation";
    /**
     * constant for regular annotation command action
     */
    public static final String REGULAR_ANNOTATION = "CommandActions.RegularAnnotation";

    /**
     * constant for show timeline menu
     */
    public static final String SHOW_TIMELINE = "Menu.View.ShowTimeline";
    /**
     * constant for show interlinear menu
     */
    public static final String SHOW_INTERLINEAR = "Menu.View.ShowInterlinear";
    /**
     * constant for show multiple tier viewer menu
     */
    public static final String SHOW_MULTITIER_VIEWER = "Commands.ShowMultitierViewer";

    /**
     * constant for search go to dialog menu
     */
    public static final String GOTO_DLG = "Menu.Search.GoTo";
    /**
     * constant for search find menu
     */
    public static final String SEARCH_DLG = "Menu.Search.Find";
    /**
     * Holds string for search in multiple files
     */
    public static final String SEARCH_MULTIPLE_DLG = "Menu.Search.Multiple";
    /**
     * Holds string for FAST search in multiple files
     */
    public static final String FASTSEARCH_DLG = "Menu.Search.FASTSearch";
    /**
     * Holds string for structured search in multiple files
     */
    public static final String STRUCTURED_SEARCH_MULTIPLE_DLG = "Menu.Search.StructuredMultiple";
    /**
     * Command action name of replacing matches with string
     */
    public static final String REPLACE = "CommandActions.Replace";
    /**
     * constant for tier dependencies menu
     */
    public static final String TIER_DEPENDENCIES = "Menu.View.Dependencies";
    /**
     * constant for shortcuts menu
     */
    public static final String SHORTCUTS = "Menu.View.Shortcuts";
    /**
     * constant for spreadsheet menu
     */
    public static final String SPREADSHEET = "Menu.View.SpreadSheet";
    /**
     * constant for statistics menu
     */
    public static final String STATISTICS = "Menu.View.Statistics";
    /* constants for the different perspectives or working modes */
    /**
     * constant for sync mode
     */
    public static final String SYNC_MODE = "Menu.Options.SyncMode";
    /**
     * constant for annotation mode
     */
    public static final String ANNOTATION_MODE = "Menu.Options.AnnotationMode";
    /**
     * constant for transcription mode
     */
    public static final String TRANSCRIPTION_MODE = "Menu.Options.TranscriptionMode";
    /**
     * constant for segmentation mode
     */
    public static final String SEGMENTATION_MODE = "Menu.Options.SegmentationMode";
    /**
     * constant for interlinearization mode
     */
    public static final String INTERLINEARIZATION_MODE = "Menu.Options.InterlinearizationMode";
    /**
     * though initially not in the Options menu but as a separate application, follow naming convention
     */
    public static final String TURNS_SCENE_MODE = "Menu.Options.TurnsAndSceneMode";

    /* three different ways of updating surrounding annotations when creating a new or modifying an existing
    annotation */
    /**
     * constant for bulldozer mode
     */
    public static final String BULLDOZER_MODE = "Menu.Options.BulldozerMode";
    /**
     * constant for normal propogation mode
     */
    public static final String TIMEPROP_NORMAL = "Menu.Options.NormalPropagationMode";
    /**
     * constant for shift mode
     */
    public static final String SHIFT_MODE = "Menu.Options.ShiftMode";

    /**
     * constant for selection mode
     */
    public static final String SELECTION_MODE = "CommandActions.SelectionMode";
    /**
     * constant for loop mode
     */
    public static final String LOOP_MODE = "CommandActions.LoopMode";
    /**
     * constant for playback mode
     */
    public static final String CONTINUOUS_PLAYBACK_MODE = "CommandActions.ContinuousPlaybackMode";
    /**
     * constant for clear selection
     */
    public static final String CLEAR_SELECTION = "Menu.Play.ClearSelection";
    /**
     * constant for clear selection Alt
     */
    public static final String CLEAR_SELECTION_ALT = "CS_Alt";
    /**
     * constant for clear selection more
     */
    public static final String CLEAR_SELECTION_AND_MODE = "Menu.Play.ClearSelectionAndMode";
    /**
     * constant for play selection
     */
    public static final String PLAY_SELECTION = "Menu.Play.PlaySelection";
    /**
     * constant for play selection slow
     */
    public static final String PLAY_SELECTION_SLOW = "CommandActions.PlaySelectionSlow";
    /**
     * constant for play selection normal speed
     */
    public static final String PLAY_SELECTION_NORMAL_SPEED = "CommandActions.PlaySelectionNormalSpeed";
    /**
     * constant for play around selection
     */
    public static final String PLAY_AROUND_SELECTION = "CommandActions.PlayAroundSelection";
    /**
     * constant for play around selection dialog
     */
    public static final String PLAY_AROUND_SELECTION_DLG = "Menu.Options.PlayAroundSelectionDialog";
    /**
     * constant for playback toggle menu
     */
    public static final String PLAYBACK_TOGGLE_DLG = "Menu.Options.PlaybackToggleDialog";
    /**
     * constant for playback toggle
     */
    public static final String PLAYBACK_TOGGLE = "PLAYBACK_TOGGLE";
    /**
     * constant for playback rate toggle
     */
    public static final String PLAYBACK_RATE_TOGGLE = "CommandActions.PlaybackRateToggle";
    /**
     * constant for playback volume toggle
     */
    public static final String PLAYBACK_VOLUME_TOGGLE = "CommandActions.PlaybackVolumeToggle";
    /**
     * frame length PAL menu option
     */
    public static final String SET_PAL = "Menu.Options.FrameLength.PAL";
    /**
     * constant frame length PAL50 menu option
     */
    public static final String SET_PAL_50 = "Menu.Options.FrameLength.PAL50";
    /**
     * constant frame length NTSC menu option
     */
    public static final String SET_NTSC = "Menu.Options.FrameLength.NTSC";
    /**
     * constant frame length custom menu option
     */
    public static final String SET_CUSTOM_MS_PER_FRAME = "Menu.Options.FrameLength.Custom";
    /**
     * constant for stepping through the media with next step
     */
    public static final String NEXT_FRAME = "Menu.Play.Next";
    /**
     * constant for stepping through the media with previous menu
     */
    public static final String PREVIOUS_FRAME = "Menu.Play.Previous";
    /**
     * constant for pause
     */
    public static final String PLAY_PAUSE = "Menu.Play.PlayPause";
    /**
     * constant for go to begin
     */
    public static final String GO_TO_BEGIN = "Menu.Play.GoToBegin";
    /**
     * constant for go to end
     */
    public static final String GO_TO_END = "Menu.Play.GoToEnd";
    /**
     * constant for go to previous scroll view
     */
    public static final String PREVIOUS_SCROLLVIEW = "Menu.Play.GoToPreviousScrollview";
    /**
     * constant for go to next view
     */
    public static final String NEXT_SCROLLVIEW = "Menu.Play.GoToNextScrollview";
    /**
     * constant for 1 pixel left
     */
    public static final String PIXEL_LEFT = "Menu.Play.1PixelLeft";
    /**
     * constant for 1 pixel right
     */
    public static final String PIXEL_RIGHT = "Menu.Play.1PixelRight";
    /**
     * constant for 1 second left
     */
    public static final String SECOND_LEFT = "Menu.Play.1SecLeft";
    /**
     * constant for 1 second right
     */
    public static final String SECOND_RIGHT = "Menu.Play.1SecRight";

    /**
     * constant for selection boundary
     */
    public static final String SELECTION_BOUNDARY = "Menu.Play.ToggleCrosshairInSelection";
    /**
     * alternative due to keyboard problems
     */
    public static final String SELECTION_BOUNDARY_ALT = "SB_Alt";
    /**
     * constant move crosshair to center of selection menu
     */
    public static final String SELECTION_CENTER = "Menu.Play.MoveCrosshairToCenterOfSelection";
    /**
     * constant for move crosshair to begin of selection
     */
    public static final String SELECTION_BEGIN = "Menu.Play.MoveCrosshairToBeginOfSelection";
    /**
     * constant for move crosshair to end of selection
     */
    public static final String SELECTION_END = "Menu.Play.MoveCrosshairToEndOfSelection";

    /**
     * constant for active annotation command
     */
    public static final String ACTIVE_ANNOTATION = "Commands.ActiveAnnotation"; // not in language file
    /**
     * constant for command action open in-line edit box
     */
    public static final String ACTIVE_ANNOTATION_EDIT = "CommandActions.OpenInlineEditBox";

    /**
     * constant for command action previous annotation
     */
    public static final String PREVIOUS_ANNOTATION = "CommandActions.PreviousAnnotation";
    /**
     * constant for command action previous annotation edit
     */
    public static final String PREVIOUS_ANNOTATION_EDIT = "CommandActions.PreviousAnnotationEdit";
    /**
     * constant for command action next annotation
     */
    public static final String NEXT_ANNOTATION = "CommandActions.NextAnnotation";
    /**
     * constant for command action next annotation edit
     */
    public static final String NEXT_ANNOTATION_EDIT = "CommandActions.NextAnnotationEdit";

    /**
     * constant for command action copy current time
     */
    public static final String COPY_CURRENT_TIME = "CommandActions.CopyCurrentTime";
    /**
     * constant for command action annotation up
     */
    public static final String ANNOTATION_UP = "CommandActions.AnnotationUp";
    /**
     * constant for command action annotation down
     */
    public static final String ANNOTATION_DOWN = "CommandActions.AnnotationDown";
    /**
     * constant for command action active annotation current time
     */
    public static final String ACTIVE_ANNOTATION_CURRENT_TIME = "CommandActions.AnnotationAtCurrentTime";
    /**
     * constant for command action cancel annotation edit
     */
    public static final String CANCEL_ANNOTATION_EDIT = "CommandActions.CancelAnnotationEdit";

    /**
     * constant for menu option Locale set
     */
    public static final String SET_LOCALE = "Menu.Options.Language";
    /* some constants for available languages for the UI, need not be hard-coded here probably */
    /**
     * constant for language catal
     */
    public static final String CATALAN = "Catal\u00E0";
    /**
     * constant for language nederlands
     */
    public static final String DUTCH = "Nederlands";
    /**
     * constant for language english
     */
    public static final String ENGLISH = "English";
    /**
     * constant for language spanish
     */
    public static final String SPANISH = "Espa\u00F1ol";
    /**
     * constant for language swedish
     */
    public static final String SWEDISH = "Svenska";
    /**
     * constant for language german
     */
    public static final String GERMAN = "Deutsch";
    /**
     * constant for language portugu
     */
    public static final String PORTUGUESE = "Portugu\u00EAs";
    /**
     * constant for language portugu brasileiro
     */
    public static final String BRAZ_PORTUGUESE = "Portugu\u00EAs Brasileiro";
    /**
     * constant for language france
     */
    public static final String FRENCH = "Fran\u00E7ais";
    /**
     * constant for language japanese
     */
    public static final String JAPANESE = "\u65e5\u672c\u8a9e";
    /**
     * constant for language chinese
     */
    public static final String CHINESE_SIMPL = "\uFEFF\u7B80\u4F53\u4E2D\u6587";
    /**
     * constant for language russian
     */
    public static final String RUSSIAN = "\u0420\u0443\u0441\u0441\u043a\u0438\u0439";
    /**
     * constant for language korean
     */
    public static final String KOREAN = "\ud55c\uad6d\uc5b4";
    /**
     * constant for language indonesian
     */
    public static final String INDONESIAN = "Bahasa Indonesia";
    /**
     * constant for custom language
     */
    public static final String CUSTOM_LANG = "Menu.Options.Language.Custom";

    /**
     * constant for menu save
     */
    public static final String SAVE = "Menu.File.Save";
    /**
     * constant for menu save as
     */
    public static final String SAVE_AS = "Menu.File.SaveAs";
    /**
     * constant for menu save as template
     */
    public static final String SAVE_AS_TEMPLATE = "Menu.File.SaveAsTemplate";
    /**
     * constant for menu save selection as eaf
     */
    public static final String SAVE_SELECTION_AS_EAF = "Menu.File.SaveSelectionAsEAF";
    /**
     * constant for menu store
     */
    public static final String STORE = "Commands.Store";

    /**
     * constant for menu export tab
     */
    public static final String EXPORT_TAB = "Menu.File.Export.Tab";
    /**
     * constant for menu export tex
     */
    public static final String EXPORT_TEX = "Menu.File.Export.TeX";
    /**
     * constant for menu export tiger
     */
    public static final String EXPORT_TIGER = "Menu.File.Export.Tiger";
    /**
     * constant for menu export eaf 2.7
     */
    public static final String EXPORT_EAF_2_7 = "Menu.File.Export.EAF2.7";
    /**
     * constant for menu export qt sub
     */
    public static final String EXPORT_QT_SUB = "Menu.File.Export.QtSub";
    /**
     * constant for menu export subtitles
     */
    public static final String EXPORT_SUBTITLES = "Menu.File.Export.Subtitles";
    /**
     * constant for menu export smil real player
     */
    public static final String EXPORT_SMIL_RT = "Menu.File.Export.Smil.RealPlayer";
    /**
     * constant for menu export smil quick time
     */
    public static final String EXPORT_SMIL_QT = "Menu.File.Export.Smil.QuickTime";
    /**
     * constant for menu export shoe box
     */
    public static final String EXPORT_SHOEBOX = "Menu.File.Export.Shoebox";
    /**
     * constant for menu export chat
     */
    public static final String EXPORT_CHAT = "Menu.File.Export.CHAT";
    /**
     * constant for menu export image from window
     */
    public static final String EXPORT_IMAGE_FROM_WINDOW = "Menu.File.Export.ImageFromWindow";
    /**
     * constant for menu export tool box
     */
    public static final String EXPORT_TOOLBOX = "Menu.File.Export.Toolbox";
    /**
     * constant for menu export flex
     */
    public static final String EXPORT_FLEX = "Menu.File.Export.Flex";
    /**
     * constant for menu export film strip
     */
    public static final String EXPORT_FILMSTRIP = "Menu.File.Export.FilmStrip";
    /**
     * constant for menu export recognizer tiers
     */
    public static final String EXPORT_RECOG_TIER = "Menu.File.Export.RecognizerTiers";
    /**
     * constant for menu export traditional transcription
     */
    public static final String EXPORT_TRAD_TRANSCRIPT = "Menu.File.Export.TraditionalTranscript";
    /**
     * constant for menu export interlinear gloss
     */
    public static final String EXPORT_TA_INTERLINEAR_GLOSS = "Menu.File.Export.TimeAlignedInterlinear";
    /**
     * constant for menu export interlinear
     */
    public static final String EXPORT_INTERLINEAR = "Menu.File.Export.Interlinear";
    /**
     * constant for menu export html
     */
    public static final String EXPORT_HTML = "Menu.File.Export.HTML";
    /**
     * constant for menu export regular multiple-tier eaf
     */
    public static final String EXPORT_REGULAR_MULTITIER_EAF = "Menu.File.Export.RegularMultitierEAF";
    /**
     * constant for menu export to json
     */
    public static final String EXPORT_JSON = "Menu.File.Export.JSON";
    /**
     * constant for menu export to server as json
     */
    public static final String EXPORT_JSON_TO_SERVER = "Menu.File.Export.ToServer.JSON";

    /**
     * constant for menu listing collection ids from server
     */
    public static final String LIST_IDS_FROM_SERVER = "Menu.File.ListIDsFrom.Server";
	
    /** 
     * constant for menu backup
     */
	public static final String BACKUP = "CommandActions.Backup";
	/** 
	 * constant for automatic backup settings menu 
	 */
	public static final String AUTOMATIC_BACKUP_SETTINGS_MA = "Menu.File.Backup.Settings";
	/**
	 * constant for automatic backup menu on 
	 */
	public static final String AUTOMATIC_BACKUP_TOGGLE_ON = "Menu.File.Backup.On";
	/**
	 * constant for automatic backup menu off 
	 */
	public static final String AUTOMATIC_BACKUP_TOGGLE_OFF = "Menu.File.Backup.Off";

    /**
     * constant for backup never menu
     */
    public static final String BACKUP_NEVER = "Menu.File.Backup.Never";
    /**
     * constant for backup 1  menu
     */
    public static final String BACKUP_1 = "Menu.File.Backup.1";
    /**
     * constant for backup every 5 min menu
     */
    public static final String BACKUP_5 = "Menu.File.Backup.5";
    /**
     * constant for backup every 10 min menu
     */
    public static final String BACKUP_10 = "Menu.File.Backup.10";
    /**
     * constant for backup every 20  min menu
     */
    public static final String BACKUP_20 = "Menu.File.Backup.20";
    /**
     * constant for backup every 30 min menu
     */
    public static final String BACKUP_30 = "Menu.File.Backup.30";

    /**
     * constant for print menu
     */
    public static final String PRINT = "Menu.File.Print";
    /**
     * constant for print preview menu
     */
    public static final String PREVIEW = "Menu.File.PrintPreview";
    /**
     * constant for page setup menu
     */
    public static final String PAGESETUP = "Menu.File.PageSetup";

    /**
     * constant for redo menu
     */
    public static final String REDO = "Menu.Edit.Redo";
    /**
     * constant for undo menu
     */
    public static final String UNDO = "Menu.Edit.Undo";

    /**
     * constant for linked files menu
     */
    public static final String LINKED_FILES_DLG = "Menu.Edit.LinkedFiles";
    /**
     * constant for languages list
     */
    public static final String EDIT_LANGUAGES_LIST = "Menu.Edit.LanguagesList";
    /**
     * constant for edit tier set menu
     */
    public static final String EDIT_TIER_SET = "Menu.Edit.TierSet";
    /**
     * constant for change linked files command action
     */
    public static final String CHANGE_LINKED_FILES = "CommandActions.ChangeLinkedFiles";
    /**
     * constant for add segmentation command action
     */
    public static final String ADD_SEGMENTATION = "CommandActions.AddSegmentation";
    /**
     * constant for filter tier menu
     */
    public static final String FILTER_TIER = "Menu.Tier.FilterTier";
    /**
     * constant for filter tier dialog menu
     */
    public static final String FILTER_TIER_DLG = "Menu.Tier.FilterTierDlg";
    /**
     * constant for re-parent tier dialog menu
     */
    public static final String REPARENT_TIER_DLG = "Menu.Tier.ReparentTierDialog";
    /**
     * constant for re-parent tier menu
     */
    public static final String REPARENT_TIER = "Menu.Tier.ReparentTier";
    /**
     * constant for copy tier menu
     */
    public static final String COPY_TIER = "Menu.Tier.CopyTier";
    /**
     * constant for copy tier dialog menu
     */
    public static final String COPY_TIER_DLG = "Menu.Tier.CopyTierDialog";
    /**
     * constant for merge transcriptions menu
     */
    public static final String MERGE_TRANSCRIPTIONS = "Menu.File.MergeTranscriptions";
    /**
     * constant for merge transcriptions undoable menu
     */
    public static final String MERGE_TRANSCRIPTIONS_UNDOABLE = "Menu.File.MergeTranscriptionsUndoable";
    /**
     * constant for weblicht merge menu
     */
    public static final String WEBLICHT_MERGE_TRANSCRIPTIONS = "Menu.Options.WebServices.WebLicht";

    /**
     * constant for next active tier command action
     */
    public static final String NEXT_ACTIVE_TIER = "CommandActions.NextActiveTier";
    /**
     * constant for previous active tier command action
     */
    public static final String PREVIOUS_ACTIVE_TIER = "CommandActions.PreviousActiveTier";
    /**
     * constant for active tier
     */
    public static final String ACTIVE_TIER = "ActiveTier";
    /**
     * constant for file close menu
     */
    public static final String CLOSE = "Menu.File.Close";
	/** 
	 * constant for restore backup file menu 
	 */
	public static final String RESTORE_BACKUP_DOC = "Menu.File.RestoreBackup";
    /**
     * constant for extract track data command action
     */
    public static final String EXT_TRACK_DATA = "CommandActions.ExtractTrackData";
    /**
     * constant for kiosk mode
     */
    public static final String KIOSK_MODE = "Menu.Options.KioskMode";
    /**
     * constant for praat tiers
     */
    public static final String IMPORT_PRAAT_GRID = "Menu.File.Import.PraatTiers";
    /**
     * constant for praat grid dialog
     */
    public static final String IMPORT_PRAAT_GRID_DLG = "Praat_Grid_Dlg";
    /**
     * constant for export praat menu
     */
    public static final String EXPORT_PRAAT_GRID = "Menu.File.Export.Praat";
    /**
     * constant for recognizer tiers
     */
    public static final String IMPORT_RECOG_TIERS = "Menu.File.Import.RecognizerTiers";
    /**
     * constant for remove annotations or values menu
     */
    public static final String REMOVE_ANNOTATIONS_OR_VALUES = "Menu.Tier.RemoveAnnotationsOrValues";
    /**
     * constant for remove annotations or values dialog
     */
    public static final String REMOVE_ANNOTATIONS_OR_VALUES_DLG = "RemoveAnnotationsOrValuesDlg";
    /**
     * constant for annotations to tiers
     */
    public static final String ANNOTATIONS_TO_TIERS = "Menu.Tier.AnnotationValuesToTiers";
    /**
     * constant for annotations to tiers dialog
     */
    public static final String ANNOTATIONS_TO_TIERS_DLG = "AnnotationValuesToTiersDlg";
    /**
     * constant for label and number
     */
    public static final String LABEL_AND_NUMBER = "Menu.Tier.LabelAndNumber";
    /**
     * constant for label num dialog
     */
    public static final String LABEL_N_NUM_DLG = "LabelNumDlg";
    /**
     * constant for segment to tiers dialog
     */
    public static final String SEGMENTS_2_TIER_DLG = "Seg2TierDlg";
    /**
     * constant for segments to tier command action
     */
    public static final String SEGMENTS_2_TIER = "CommandActions.SegmentsToTiers";
    /**
     * constant for key create annotation command action
     */
    public static final String KEY_CREATE_ANNOTATION = "CommandActions.KeyCreateAnnotation";
    /**
     * constant for export words menu
     */
    public static final String EXPORT_WORDS = "Menu.File.Export.WordList";
    /**
     * constant for export preferences
     */
    public static final String EXPORT_PREFS = "Menu.Edit.Preferences.Export";
    /**
     * constant for importing preferences
     */
    public static final String IMPORT_PREFS = "Menu.Edit.Preferences.Import";
    /**
     * constant for font browser
     */
    public static final String FONT_BROWSER = "Menu.View.FontBrowser";
    /**
     * constant for time viewer center selection
     */
    public static final String CENTER_SELECTION = "TimeLineViewer.CenterSelection";
    /**
     * constant for set author menu
     */
    public static final String SET_AUTHOR = "Menu.Edit.Author";
    /**
     * constant for set document properties
     */
    public static final String SET_DOCUMENT_PROPERTIES = "Menu.Edit.DocumentProperties";
    /**
     * constant for set document properties dialog
     */
    public static final String SET_DOCUMENT_PROPERTIES_DLG = "DocumentPropertiesDlg";
    /**
     * constant for document info
     */
    public static final String DOCUMENT_INFO = "Menu.View.DocumentInfo";
    /**
     * constant for annotation density plot
     */
    public static final String ANNOTATION_DENSITY_PLOT = "Menu.View.AnnotationDensityPlot";
    /**
     * constant for audio spectrogram
     */
    public static final String AUDIO_SPECTROGRAM = "Menu.View.AudioSpectrogram";
    /**
     * constant for moving annotation left bound to left command action
     */
    public static final String MOVE_ANNOTATION_LBOUNDARY_LEFT = "CommandActions.Annotation_LBound_Left";
    /**
     * constant for moving annotation left bound to right command action
     */
    public static final String MOVE_ANNOTATION_LBOUNDARY_RIGHT = "CommandActions.Annotation_LBound_Right";
    /**
     * constant for moving annotation right bound to left command action
     */
    public static final String MOVE_ANNOTATION_RBOUNDARY_LEFT = "CommandActions.Annotation_RBound_Left";
    /**
     * constant for moving annotation right bound to right command action
     */
    public static final String MOVE_ANNOTATION_RBOUNDARY_RIGHT = "CommandActions.Annotation_RBound_Right";
    /**
     * constant for modify boundaries of all annotations of a tier menu action
     */
    public static final String MODIFY_BOUNDARIES_ALL_ANNOTATIONS_MULTI = "Menu.File.MultiFileModifyBoundariesAnnotations";
    /**
     * constant for modify all annotations boundaries
     */
    public static final String MODIFY_BOUNDARIES_ALL_ANNOS_CMD = "Menu.Tier.ModifyAllAnnotationBoundaries";
    // action keys for global, document independent actions
    /**
     * constant for next window menu
     */
    public static final String NEXT_WINDOW = "Menu.Window.Next";
    /**
     * constant for previous window menu
     */
    public static final String PREV_WINDOW = "Menu.Window.Previous";
    /**
     * constant for edit preferences menu
     */
    public static final String EDIT_PREFS = "Menu.Edit.Preferences.Edit";
    /**
     * constant for shortcut menu
     */
    public static final String EDIT_SHORTCUTS = "Menu.Edit.Preferences.Shortcut";
    /**
     * constant for find and replace in multiple files menu
     */
    public static final String REPLACE_MULTIPLE = "Menu.Search.FindReplaceMulti";
    /**
     * constant for new file menu
     */
    public static final String NEW_DOC = "Menu.File.New";
    /**
     * constant for open file menu
     */
    public static final String OPEN_DOC = "Menu.File.Open";
    /**
     * constant for open remote file menu
     */
    public static final String OPEN_REMOTE_DOC = "Menu.File.OpenRemote";
    /**
     * constant for file validate menu
     */
    public static final String VALIDATE_DOC = "Menu.File.Validate";

    /**
     * constant for multiple export toolbox menu
     */
    public static final String EXPORT_TOOLBOX_MULTI = "Menu.File.MultipleExport.Toolbox";
    /**
     * constant for multiple export praat menu
     */
    public static final String EXPORT_PRAAT_MULTI = "Menu.File.MultipleExport.Praat";
    /**
     * constant for multiple export tab menu
     */
    public static final String EXPORT_TAB_MULTI = "Menu.File.MultipleExport.Tab";
    /**
     * constant for multiple export annotation list menu
     */
    public static final String EXPORT_ANNLIST_MULTI = "Menu.File.Export.AnnotationListMulti";
    /**
     * constant for multiple export word list menu
     */
    public static final String EXPORT_WORDLIST_MULTI = "Menu.File.MultipleExport.WordList";
    /**
     * constant for export tiers menu
     */
    public static final String EXPORT_TIERS_MULTI = "Menu.File.Export.Tiers";
    /**
     * constant for export overlaps menu
     */
    public static final String EXPORT_OVERLAPS_MULTI = "Menu.File.Export.OverlapsMulti";
    /**
     * constant for multiple export flex menu
     */
    public static final String EXPORT_FLEX_MULTI = "Menu.File.MultipleExport.Flex";
    /**
     * constant for multiple export theme menu
     */
    public static final String EXPORT_THEME_MULTI = "Menu.File.MultipleExport.Theme";
    /**
     * constant for multiple export annotated part menu
     */
    public static final String EXPORT_ANN_PART_MULTI = "Menu.File.MultipleExport.AnnotatedPart";

    /**
     * constant for import shoebox menu
     */
    public static final String IMPORT_SHOEBOX = "Menu.File.Import.Shoebox";
    /**
     * constant for import toolbox menu
     */
    public static final String IMPORT_TOOLBOX = "Menu.File.Import.Toolbox";
    /**
     * constant for import chat menu
     */
    public static final String IMPORT_CHAT = "Menu.File.Import.CHAT";
    /**
     * constant for import transcriber menu
     */
    public static final String IMPORT_TRANS = "Menu.File.Import.Transcriber";
    /**
     * constant for import tab delimited menu
     */
    public static final String IMPORT_TAB = "Menu.File.Import.Delimited";
    /**
     * constant for import delimited dialog
     */
    public static final String IMPORT_TAB_DLG = "Delimited_Text_Dlg";
    /**
     * constant for import subtitle menu
     */
    public static final String IMPORT_SUBTITLE = "Menu.File.Import.Subtitle";
    /**
     * constant for import subtitle dialog
     */
    public static final String IMPORT_SUBTITLE_DLG = "Subtitle_Text_Dlg";
    /**
     * constant for import flex menu
     */
    public static final String IMPORT_FLEX = "Menu.File.Import.FLEx";
    /**
     * constant for import json web annotation menu
     */
    public static final String IMPORT_JSON_WA = "Menu.File.Import.JSON.WA";
    /**
     * constant for import collection json from annotation server
     */
    public static final String IMPORT_COLLECTION_FROM_SERVER = "Menu.File.Import.Collection.Server";

    /**
     * constant for import toolbox menu
     */
    public static final String IMPORT_TOOLBOX_MULTI = "Menu.File.MultipleImport.Toolbox";
    /**
     * constant for import praat tiers menu
     */
    public static final String IMPORT_PRAAT_GRID_MULTI = "Menu.File.MultipleImport.PraatTiers";
    /**
     * constant for import flex menu
     */
    public static final String IMPORT_FLEX_MULTI = "Menu.File.MultipleImport.FLEx";

    /**
     * constant for file exit menu
     */
    public static final String EXIT = "Menu.File.Exit";
    /**
     * constant for help contents menu
     */
    public static final String HELP = "Menu.Help.Contents";
    /**
     * constant for about menu
     */
    public static final String ABOUT = "Menu.Help.About";
    /**
     * constant for clip media menu
     */
    public static final String CLIP_MEDIA = "Menu.File.Export.MediaWithScript";
    /**
     * constant for add TS track panel menu
     */
    public static final String ADD_TRACK_AND_PANEL = "AddTSTrackAndPanel";
    // TODO add to shortcuts / actions that can have a shortcut assigned
    /**
     * constant for multiple eaf creation menu
     */
    public static final String CREATE_NEW_MULTI = "Menu.File.MultiEAFCreation";
    /**
     * constant for edit multiple file menu
     */
    public static final String EDIT_MULTIPLE_FILES = "Menu.File.Process.EditMF";
    /**
     * constant for scrub transcriptions menu
     */
    public static final String SCRUB_MULTIPLE_FILES = "Menu.File.ScrubTranscriptions";
    /**
     * constant for annotation from overlap menu
     */
    public static final String ANNOTATION_OVERLAP_MULTI = "Menu.File.MultipleFileAnnotationFromOverlaps";
    /**
     * constant for multiple file compare menu
     */
    public static final String ANNOTATOR_COMPARE_MULTI = "Menu.File.MultipleFileCompareAnnotators";
    /**
     * constant for annotation from substraction menu
     */
    public static final String ANNOTATION_SUBTRACTION_MULTI = "Menu.File.MultipleFileAnnotationFromSubtraction";
    /**
     * constant for multiple file statistics menu
     */
    public static final String STATISTICS_MULTI = "Menu.File.MultiFileStatistics";
    /**
     * constant for multiple statistics file menu
     */
    public static final String NGRAMSTATS_MULTI = "Menu.File.MultiFileNgramStats";
    /**
     * constant for multiple media clips menu
     */
    public static final String CLIP_MEDIA_MULTI = "Menu.File.MultipleMediaClips";
    /**
     * constant for multiple file merge clips menu
     */
    public static final String MERGE_TIERS_MULTI = "Menu.File.MultipleFileMergeTiers";
    /**
     * constant for update multiple transcriptions menu
     */
    public static final String UPDATE_TRANSCRIPTIONS_FOR_ECV = "Menu.File.MultiEAFECVUpdater";
    /**
     * constant for update multiple file with template menu
     */
    public static final String UPDATE_TRANSCRIPTIONS_WITH_TEMPLATE = "Menu.File.MultipleFileUpdateWithTemplate";

    /**
     * constant for edit lexicon service menu
     */
    public static final String EDIT_LEX_SRVC_DLG = "Menu.Edit.EditLexSrvc";
    /**
     * constant for add lexicon link menu
     */
    public static final String ADD_LEX_LINK = "CommandActions.AddLexLink";
    /**
     * constant for change lexicon link command action
     */
    public static final String CHANGE_LEX_LINK = "CommandActions.ChangeLexLink";
    /**
     * constant for delete lexicon link command action
     */
    public static final String DELETE_LEX_LINK = "CommandActions.DeleteLexLink";
    /**
     * constant for play step and repeat menu
     */
    public static final String PLAY_STEP_AND_REPEAT = "Menu.Play.PlayStepAndRepeat";
    /**
     * constant for edit spell checker menu
     */
    public static final String EDIT_SPELL_CHECKER_DLG = "Menu.Edit.EditSpellChecker";

    // Transcription mode actions
    /**
     * constant for transcription mode commit changes action
     */
    public static final String COMMIT_CHANGES = "TranscriptionMode.Actions.CommitChanges";
    /**
     * constant for transcription mode cancel changes action
     */
    public static final String CANCEL_CHANGES = "TranscriptionMode.Actions.CancelChanges";
    /**
     * constant for transcription mode move up action
     */
    public static final String MOVE_UP = "TranscriptionMode.Actions.MoveUp";
    /**
     * constant for transcription mode move down action
     */
    public static final String MOVE_DOWN = "TranscriptionMode.Actions.MoveDown";
    /**
     * constant for transcription mode move left action
     */
    public static final String MOVE_LEFT = "TranscriptionMode.Actions.MoveLeft";
    /**
     * constant for transcription mode move right action
     */
    public static final String MOVE_RIGHT = "TranscriptionMode.Actions.MoveRight";
    /**
     * constant for transcription play from start action
     */
    public static final String PLAY_FROM_START = "TranscriptionMode.Actions.PlayFromStart";
    /**
     * constant for transcription mode hide linked tiers action
     */
    public static final String HIDE_TIER = "TranscriptionTable.Label.HideLinkedTiers";
    /**
     * constant for transcription mode freeze tier action
     */
    public static final String FREEZE_TIER = "TranscriptionMode.Actions.FreezeTier";
    /**
     * constant for transcription mode edit in annotation mode
     */
    public static final String EDIT_IN_ANN_MODE = "TranscriptionTableEditBox.EditInAnnotationMode";

    // Segmentation mode actions
    /**
     * constant for segmentation mode
     */
    public static final String SEGMENT = "SegmentationMode.Actions.Segment";

    /**
     * constant for shortcuts common
     */
    public static final String COMMON_SHORTCUTS = "Shortcuts.Common";

    /**
     * constant for update elan menu
     */
    public static final String UPDATE_ELAN = "Menu.Options.CheckForUpdate";
    /**
     * constant for web services menu
     */
    public static final String WEBSERVICES_DLG = "Menu.Options.WebServices";
    /**
     * constant for web licht dialog
     */
    public static final String WEBLICHT_DLG = "WebServicesDialog.WebService.WebLicht";
    /**
     * constant for zoom in menu
     */
    public static final String ZOOM_IN = "Menu.View.ZoomIn"; // not really a menu item. TimescaleViewer.ZoomIn?
    /**
     * constant for zoom out menu
     */
    public static final String ZOOM_OUT = "Menu.View.ZoomOut";
    /**
     * constant for zoom default menu
     */
    public static final String ZOOM_DEFAULT = "Menu.View.ZoomDefault";
    /**
     * constant for cycle tier sets
     */
    public static final String CYCLE_TIER_SETS = "CommandActions.CycleTierSets";
    /**
     * constant for annotations from suggestion set
     */
    public static final String ANNS_FROM_SUGGESTION_SET = "CommandActions.AnnotationsFromSuggestionSet";


    static {
        languages.put(CATALAN, ElanLocale.CATALAN);
        languages.put(CHINESE_SIMPL, ElanLocale.CHINESE_SIMP);
        languages.put(DUTCH, ElanLocale.DUTCH);
        languages.put(ENGLISH, ElanLocale.ENGLISH);
        languages.put(RUSSIAN, ElanLocale.RUSSIAN);
        languages.put(SPANISH, ElanLocale.SPANISH);
        languages.put(SWEDISH, ElanLocale.SWEDISH);
        languages.put(GERMAN, ElanLocale.GERMAN);
        languages.put(PORTUGUESE, ElanLocale.PORTUGUESE);
        languages.put(BRAZ_PORTUGUESE, ElanLocale.BRAZILIAN_PORTUGUESE);
        languages.put(FRENCH, ElanLocale.FRENCH);
        languages.put(JAPANESE, ElanLocale.JAPANESE);
        languages.put(KOREAN, ElanLocale.KOREAN);
        languages.put(INDONESIAN, ElanLocale.INDONESIAN);
        languages.put(CUSTOM_LANG, ElanLocale.CUSTOM);
    }

    /**
     * Adds the document(transcription) to the root map, viewer manager map and layout manager map
     *
     * @param fr the Jframe
     * @param vm the viewer manager
     * @param lm the layout manager
     */
    public static synchronized void addDocument(JFrame fr, ViewerManager2 vm, ElanLayoutManager lm) {
        Transcription t = vm.getTranscription();

        rootFrameHash.putIfAbsent(t, fr);
        viewerManagerHash.putIfAbsent(t, vm);
        layoutManagerHash.putIfAbsent(t, lm);
    }

    /**
     * Removes the transcription associated with the specified viewer manager
     *
     * @param vm the viewer manager
     */
    public static synchronized void removeDocument(ViewerManager2 vm) {
        if (vm != null) {
            Transcription t = vm.getTranscription();

            commandActionHash.remove(t);
            undoCAHash.remove(t);
            redoCAHash.remove(t);
            commandHistoryHash.remove(t);
            viewerManagerHash.remove(t);
            layoutManagerHash.remove(t);
            rootFrameHash.remove(t);
            trackManagerHash.remove(t);
        }
    }

    /**
     * Returns the main root frame for the transcription.
     *
     * @param forTranscription the transcription to get the frame for
     *
     * @return the root frame
     */
    public static synchronized JFrame getRootFrame(Transcription forTranscription) {
        if (forTranscription == null) {
            return null;
        }
        return rootFrameHash.get(forTranscription);
    }

    /**
     * Returns the viewer manager
     *
     * @param forTranscription the transcription
     *
     * @return the viewer manager
     */
    public static synchronized ViewerManager2 getViewerManager(Transcription forTranscription) {
        return viewerManagerHash.get(forTranscription);
    }

    /**
     * Gets the layout manager associated with the transcription.
     *
     * @param forTranscription the transcription to get the layout manager for
     *
     * @return the layout manager
     */
    public static synchronized ElanLayoutManager getLayoutManager(Transcription forTranscription) {
        return layoutManagerHash.get(forTranscription);
    }

    /**
     * Creation of the a track manager is postponed until it is necessary: when at least one time series source has been
     * added.
     *
     * @param forTranscription the document / transcription
     * @param trackManager the manager for tracks and track sources
     */
    public static synchronized void addTrackManager(Transcription forTranscription, TSTrackManager trackManager) {
        if (forTranscription != null && trackManager != null) {
            trackManagerHash.put(forTranscription, trackManager);
        }
    }

    /**
     * Returns the time series track manager for the transcription.
     *
     * @param forTranscription the transcription
     *
     * @return the track manager or null
     */
    public static synchronized TSTrackManager getTrackManager(Transcription forTranscription) {
        return trackManagerHash.get(forTranscription);
    }

    /**
     * Gets the command action identified by the specified name.
     *
     * @param tr the transcription the action pertains to
     * @param caName the name of the command
     *
     * @return the action, retrieved from cache or created on demand
     */
    public static CommandAction getCommandAction(Transcription tr, String caName) {
        CommandAction ca = null;

        synchronized (ELANCommandFactory.class) {
            if (commandActionHash.get(tr) == null) {
                commandActionHash.put(tr, new HashMap<String, CommandAction>());
            }

            if (commandHistoryHash.get(tr) == null) {
                commandHistoryHash.put(tr, new CommandHistory(CommandHistory.historySize, tr));
            }
        }

        ViewerManager2 viewerManager = viewerManagerHash.get(tr);
        ElanLayoutManager layoutManager = layoutManagerHash.get(tr);

        ca = commandActionHash.get(tr).get(caName);
        if (ca == null) {
            if (SET_TIER_NAME.equals(caName)) {
                ca = new SetTierNameCA(viewerManager);
            } else if (ADD_TIER.equals(caName)) {
                ca = new AddTierDlgCA(viewerManager);
            } else if (CHANGE_TIER.equals(caName)) {
                ca = new ChangeTierDlgCA(viewerManager);
            } else if (DELETE_TIER.equals(caName) || DELETE_TIERS.equals(caName)) {
                ca = new DeleteTierDlgCA(viewerManager);
            } else if (ADD_PARTICIPANT.equals(caName)) {
                ca = new AddParticipantCA(viewerManager);
            } else if (ADD_DEPENDENT_TIERS_TO_TIER_STRUCTURE.equals(caName)) {
                ca = new AddDependentTiersToTierStructureCA(viewerManager);
            } else if (DELETE_PARTICIPANT.equals(caName)) {
                ca = new DeleteParticipantCA(viewerManager);
            } else if (IMPORT_TIERS.equals(caName)) {
                ca = new ImportTiersDlgCA(viewerManager);
            } else if (ADD_TYPE.equals(caName)) {
                ca = new AddLingTypeDlgCA(viewerManager);
            } else if (CHANGE_TYPE.equals(caName)) {
                ca = new ChangeLingTypeDlgCA(viewerManager);
            } else if (DELETE_TYPE.equals(caName)) {
                ca = new DeleteLingTypeDlgCA(viewerManager);
            } else if (IMPORT_TYPES.equals(caName)) {
                ca = new ImportTypesDlgCA(viewerManager);
            } else if (EDIT_CV_DLG.equals(caName)) {
                ca = new EditCVDlgCA(viewerManager);
            } else if (NEW_ANNOTATION.equals(caName)) {
                ca = new NewAnnotationCA(viewerManager);
            } else if (CREATE_DEPEND_ANN.equals(caName)) {
                ca = new CreateDependentAnnotationsCA(viewerManager);
            } else if (NEW_ANNOTATION_REC.equals(caName)) {
                ca = new NewAnnotationRecursiveCA(viewerManager);
            } else if (NEW_ANNOTATION_ALT.equals(caName)) {
                ca = new NewAnnotationAltCA(viewerManager);
            } else if (NEW_ANNOTATION_BEFORE.equals(caName)) {
                ca = new AnnotationBeforeCA(viewerManager);
            } else if (NEW_ANNOTATION_AFTER.equals(caName)) {
                ca = new AnnotationAfterCA(viewerManager);
            } else if (NEW_ANNOTATION_FROM_BIGIN_END_TIME_DLG.equals(caName)) {
                ca = new NewAnnotationFromBeginEndTimeDlgCA(viewerManager);
            } else if (MODIFY_ANNOTATION.equals(caName)) {
                ca = new ModifyAnnotationCA(viewerManager);
            } else if (MODIFY_ANNOTATION_ALT.equals(caName)) {
                ca = new ModifyAnnotationAltCA(viewerManager);
            } else if (SPLIT_ANNOTATION.equals(caName)) {
                ca = new SplitAnnotationCA(viewerManager);
            } else if (REMOVE_ANNOTATION_VALUE.equals(caName)) {
                ca = new RemoveAnnotationValueCA(viewerManager);
            } else if (DELETE_ANNOTATION.equals(caName)) {
                ca = new DeleteAnnotationCA(viewerManager);
            } else if (DELETE_ANNOTATION_ALT.equals(caName)) {
                ca = new DeleteAnnotationAltCA(viewerManager);
            } else if (DELETE_ANNOS_IN_SELECTION.equals(caName)) {
                ca = new DeleteAnnotationsInSelectionCA(viewerManager);
            } else if (DELETE_ANNOS_LEFT_OF.equals(caName)) {
                ca = new DeleteAnnotationsLeftOfCA(viewerManager);
            } else if (DELETE_ANNOS_RIGHT_OF.equals(caName)) {
                ca = new DeleteAnnotationsRightOfCA(viewerManager);
            } else if (DELETE_ALL_ANNOS_LEFT_OF.equals(caName)) {
                ca = new DeleteAllAnnotationsLeftOfCA(viewerManager);
            } else if (DELETE_ALL_ANNOS_RIGHT_OF.equals(caName)) {
                ca = new DeleteAllAnnotationsRightOfCA(viewerManager);
            } else if (DUPLICATE_ANNOTATION.equals(caName)) {
                ca = new DuplicateAnnotationCA(viewerManager);
            } else if (MERGE_ANNOTATION_WN.equals(caName)) {
                ca = new MergeAnnotationWithNextCA(viewerManager);
            } else if (MERGE_ANNOTATION_WB.equals(caName)) {
                ca = new MergeAnnotationWithBeforeCA(viewerManager);
            } else if (COPY_TO_NEXT_ANNOTATION.equals(caName)) {
                ca = new CopyToNextAnnotationCA(viewerManager);
            } else if (COPY_ANNOTATION.equals(caName)) {
                ca = new CopyAnnotationCA(viewerManager);
            } else if (COPY_ANNOTATION_TREE.equals(caName)) {
                ca = new CopyAnnotationTreeCA(viewerManager);
            } else if (PASTE_ANNOTATION.equals(caName)) {
                ca = new PasteAnnotationCA(viewerManager);
            } else if (PASTE_ANNOTATION_HERE.equals(caName)) {
                ca = new PasteAnnotationHereCA(viewerManager);
            } else if (PASTE_ANNOTATION_TREE.equals(caName)) {
                ca = new PasteAnnotationTreeCA(viewerManager);
            } else if (PASTE_ANNOTATION_TREE_HERE.equals(caName)) {
                ca = new PasteAnnotationTreeHereCA(viewerManager);
            } else if (MODIFY_ANNOTATION_TIME.equals(caName)) {
                ca = new ModifyAnnotationTimeCA(viewerManager);
            } else if (MODIFY_ANNOTATION_DC.equals(caName)) {
                ca = new ModifyAnnotationDatCatCA(viewerManager);
            } else if (MODIFY_ALL_ANNOTATION_BOUNDARIES.equals(caName)) {
                ca = new ModifyAllAnnotationsDlgCA(viewerManager);
            } else if (SHOW_IN_BROWSER.equals(caName)) {
                ca = new ShowInBrowserCA(viewerManager);
            } else if (SHIFT_ALL_ANNOTATIONS.equals(caName)) {
                ca = new ShiftAllAnnotationsDlgCA(viewerManager);
            } else if (SHIFT_ACTIVE_ANNOTATION.equals(caName)) {
                ca = new ShiftActiveAnnotationCA(viewerManager);
            } else if (SHIFT_ANNOS_IN_SELECTION.equals(caName)) {
                ca = new ShiftAnnotationsInSelectionCA(viewerManager);
            } else if (SHIFT_ANNOS_LEFT_OF.equals(caName)) {
                ca = new ShiftAnnotationsLeftOfCA(viewerManager);
            } else if (SHIFT_ANNOS_RIGHT_OF.equals(caName)) {
                ca = new ShiftAnnotationsRightOfCA(viewerManager);
            } else if (SHIFT_ALL_ANNOS_LEFT_OF.equals(caName)) {
                ca = new ShiftAllAnnotationsLeftOfCA(viewerManager);
            } else if (SHIFT_ALL_ANNOS_RIGHT_OF.equals(caName)) {
                ca = new ShiftAllAnnotationsRightOfCA(viewerManager);
            } else if (TOKENIZE_DLG.equals(caName)) {
                ca = new TokenizeDlgCA(viewerManager);
            } else if (REGULAR_ANNOTATION_DLG.equals(caName)) {
                ca = new RegularAnnotationDlgCA(viewerManager);
            } else if (REMOVE_ANNOTATIONS_OR_VALUES.equals(caName)) {
                ca = new RemoveAnnotationsOrValuesCA(viewerManager);
            } else if (ANN_FROM_OVERLAP.equals(caName)) {
                ca = new AnnotationsFromOverlapsDlgCA(viewerManager);
            } else if (ANN_FROM_SUBTRACTION.equals(caName)) {
                ca = new AnnotationsFromSubtractionDlgCA(viewerManager);
            }
            //temp
            else if (ANN_FROM_OVERLAP_CLAS.equals(caName)) {
                ca = new AnnotationsFromOverlapsClasDlgCA(viewerManager);
            } else if (MERGE_TIERS.equals(caName)) {
                ca = new MergeTiersDlgCA(viewerManager);
            } else if (MERGE_TIERS_CLAS.equals(caName)) {
                ca = new MergeTiersClasDlgCA(viewerManager);
            } else if (MERGE_TIER_GROUP.equals(caName)) {
                ca = new MergeTierGroupDlgCA(viewerManager);
            } else if (ANN_ON_DEPENDENT_TIER.equals(caName)) {
                ca = new CreateAnnsOnDependentTiersDlgCA(viewerManager);
            } else if (ANN_FROM_GAPS.equals(caName)) {
                ca = new AnnotationsFromGapsDlgCA(viewerManager);
            } else if (ANNOTATOR_COMPARE_MULTI.equals(caName)) {
                ca = new CompareAnnotatorsDlgCA(viewerManager);
            } else if (SHOW_TIMELINE.equals(caName)) {
                ca = new ShowTimelineCA(viewerManager, layoutManager);
            } else if (SHOW_INTERLINEAR.equals(caName)) {
                ca = new ShowInterlinearCA(viewerManager, layoutManager);
            } else if (SEARCH_DLG.equals(caName)) {
                ca = new SearchDialogCA(viewerManager);
            } else if (GOTO_DLG.equals(caName)) {
                ca = new GoToDialogCA(viewerManager);
            } else if (TIER_DEPENDENCIES.equals(caName)) {
                ca = new TierDependenciesCA(viewerManager);
            } else if (SPREADSHEET.equals(caName)) {
                ca = new SpreadSheetCA(viewerManager);
            } else if (STATISTICS.equals(caName)) {
                ca = new StatisticsCA(viewerManager);
            } else if (SYNC_MODE.equals(caName)) {
                //ca = new SyncModeCA(viewerManager, layoutManager);
                ca = new ChangeModeCA(viewerManager, layoutManager, SYNC_MODE);
            } else if (ANNOTATION_MODE.equals(caName)) {
                //ca = new AnnotationModeCA(viewerManager, layoutManager);
                ca = new ChangeModeCA(viewerManager, layoutManager, ANNOTATION_MODE);
            } else if (TRANSCRIPTION_MODE.equals(caName)) {
                //ca = new TranscriptionModeCA(viewerManager, layoutManager);
                ca = new ChangeModeCA(viewerManager, layoutManager, TRANSCRIPTION_MODE);
            } else if (SEGMENTATION_MODE.equals(caName)) {
                //ca = new SegmentationModeCA(viewerManager, layoutManager);
                ca = new ChangeModeCA(viewerManager, layoutManager, SEGMENTATION_MODE);
            } else if (INTERLINEARIZATION_MODE.equals(caName)) {
                ca = new ChangeModeCA(viewerManager, layoutManager, INTERLINEARIZATION_MODE);
            } else if (SELECTION_MODE.equals(caName)) {
                ca = new SelectionModeCA(viewerManager);
            } else if (LOOP_MODE.equals(caName)) {
                ca = new LoopModeCA(viewerManager);
            } else if (BULLDOZER_MODE.equals(caName)) {
                ca = new BulldozerModeCA(viewerManager);
            } else if (TIMEPROP_NORMAL.equals(caName)) {
                ca = new NormalTimePropCA(viewerManager);
            } else if (SHIFT_MODE.equals(caName)) {
                ca = new ShiftModeCA(viewerManager);
            } else if (SET_PAL.equals(caName)) {
                ca = new SetPALCA(viewerManager);
            } else if (SET_PAL_50.equals(caName)) {
                ca = new SetPAL50CA(viewerManager);
            } else if (SET_NTSC.equals(caName)) {
                ca = new SetNTSCCA(viewerManager);
            } else if (CLEAR_SELECTION.equals(caName)) {
                ca = new ClearSelectionCA(viewerManager);
            } else if (CLEAR_SELECTION_ALT.equals(caName)) {
                ca = new ClearSelectionAltCA(viewerManager);
            } else if (CLEAR_SELECTION_AND_MODE.equals(caName)) {
                ca = new ClearSelectionAndModeCA(viewerManager);
            } else if (PLAY_SELECTION.equals(caName)) {
                ca = new PlaySelectionCA(viewerManager);
            } else if (PLAY_AROUND_SELECTION.equals(caName)) {
                ca = new PlayAroundSelectionCA(viewerManager);
            } else if (PLAY_SELECTION_SLOW.equals(caName)) {
                ca = new PlaySelectionSlowCA(viewerManager, (float) 0.5);
            } else if (PLAY_SELECTION_NORMAL_SPEED.equals(caName)) {
                ca = new PlaySelectionSlowCA(viewerManager, (float) 1.0);
            } else if (NEXT_FRAME.equals(caName)) {
                ca = new NextFrameCA(viewerManager);
            } else if (PREVIOUS_FRAME.equals(caName)) {
                ca = new PreviousFrameCA(viewerManager);
            } else if (PLAY_PAUSE.equals(caName)) {
                ca = new PlayPauseCA(viewerManager);
            } else if (GO_TO_BEGIN.equals(caName)) {
                ca = new GoToBeginCA(viewerManager);
            } else if (GO_TO_END.equals(caName)) {
                ca = new GoToEndCA(viewerManager);
            } else if (PREVIOUS_SCROLLVIEW.equals(caName)) {
                ca = new PreviousScrollViewCA(viewerManager);
            } else if (NEXT_SCROLLVIEW.equals(caName)) {
                ca = new NextScrollViewCA(viewerManager);
            } else if (PIXEL_LEFT.equals(caName)) {
                ca = new PixelLeftCA(viewerManager);
            } else if (PIXEL_RIGHT.equals(caName)) {
                ca = new PixelRightCA(viewerManager);
            } else if (SECOND_LEFT.equals(caName)) {
                ca = new SecondLeftCA(viewerManager);
            } else if (SECOND_RIGHT.equals(caName)) {
                ca = new SecondRightCA(viewerManager);
            } else if (SELECTION_BOUNDARY.equals(caName)) {
                ca = new ActiveSelectionBoundaryCA(viewerManager);
            } else if (SELECTION_CENTER.equals(caName)) {
                ca = new ActiveSelectionCenterCA(viewerManager);
            } else if (SELECTION_BEGIN.equals(caName)) {
                ca = new ActiveSelectionBeginCA(viewerManager);
            } else if (SELECTION_END.equals(caName)) {
                ca = new ActiveSelectionEndCA(viewerManager);
            } else if (SELECTION_BOUNDARY_ALT.equals(caName)) {
                ca = new ActiveSelectionBoundaryAltCA(viewerManager);
            } else if (PREVIOUS_ANNOTATION.equals(caName)) {
                ca = new PreviousAnnotationCA(viewerManager);
            } else if (PREVIOUS_ANNOTATION_EDIT.equals(caName)) {
                ca = new PreviousAnnotationEditCA(viewerManager);
            } else if (NEXT_ANNOTATION.equals(caName)) {
                ca = new NextAnnotationCA(viewerManager);
            } else if (NEXT_ANNOTATION_EDIT.equals(caName)) {
                ca = new NextAnnotationEditCA(viewerManager);
            } else if (ACTIVE_ANNOTATION_EDIT.equals(caName)) {
                ca = new ActiveAnnotationEditCA(viewerManager);
            } else if (ANNOTATION_UP.equals(caName)) {
                ca = new AnnotationUpCA(viewerManager);
            } else if (ANNOTATION_DOWN.equals(caName)) {
                ca = new AnnotationDownCA(viewerManager);
            } else if (SAVE.equals(caName)) {
                ca = new SaveCA(viewerManager);
            } else if (SAVE_AS.equals(caName)) {
                ca = new SaveAsCA(viewerManager);
            } else if (EXPORT_EAF_2_7.equals(caName)) {
                ca = new SaveAs2_7CA(viewerManager);
            } else if (SAVE_AS_TEMPLATE.equals(caName)) {
                ca = new SaveAsTemplateCA(viewerManager);
            } else if (SAVE_SELECTION_AS_EAF.equals(caName)) {
                ca = new SaveSelectionAsEafCA(viewerManager);
            } else if (BACKUP.equals(caName)) {
                ca = new BackupCA(viewerManager);
            } else if (PRINT.equals(caName)) {
                ca = new PrintCA(viewerManager);
            } else if (PREVIEW.equals(caName)) {
                ca = new PrintPreviewCA(viewerManager);
            } else if (PAGESETUP.equals(caName)) {
                ca = new PageSetupCA(viewerManager);
            } else if (EXPORT_TAB.equals(caName)) {
                ca = new ExportTabDelDlgCA(viewerManager);
            } else if (EXPORT_TEX.equals(caName)) {
                ca = new ExportTeXDlgCA(viewerManager);
            } else if (EXPORT_TIGER.equals(caName)) {
                ca = new ExportTigerDlgCA(viewerManager);
            } else if (EXPORT_QT_SUB.equals(caName)) {
                ca = new ExportQtSubCA(viewerManager);
            } else if (EXPORT_SMIL_RT.equals(caName)) {
                ca = new ExportSmilCA(viewerManager);
            } else if (EXPORT_SMIL_QT.equals(caName)) {
                ca = new ExportSmilQTCA(viewerManager);
            } else if (EXPORT_SHOEBOX.equals(caName)) {
                ca = new ExportShoeboxCA(viewerManager);
            } else if (EXPORT_CHAT.equals(caName)) {
                ca = new ExportCHATCA(viewerManager);
            } else if (EXPORT_IMAGE_FROM_WINDOW.equals(caName)) {
                ca = new ExportImageFromWindowCA(viewerManager);
            } else if (LINKED_FILES_DLG.equals(caName)) {
                ca = new LinkedFilesDlgCA(viewerManager);
            } else if (PLAYBACK_RATE_TOGGLE.equals(caName)) {
                ca = new PlaybackRateToggleCA(viewerManager);
            } else if (PLAYBACK_VOLUME_TOGGLE.equals(caName)) {
                ca = new PlaybackVolumeToggleCA(viewerManager);
            } else if (FILTER_TIER_DLG.equals(caName)) {
                ca = new FilterTierDlgCA(viewerManager);
            } else if (EXPORT_TRAD_TRANSCRIPT.equals(caName)) {
                ca = new ExportTradTranscriptDlgCA(viewerManager);
            } else if (EXPORT_TA_INTERLINEAR_GLOSS.equals(caName)) {
                ca = new ExportTimeAlignedInterlinearDlgCA(viewerManager);
            } else if (EXPORT_INTERLINEAR.equals(caName)) {
                ca = new ExportInterlinearDlgCA(viewerManager);
            } else if (EXPORT_HTML.equals(caName)) {
                ca = new ExportHTMLDlgCA(viewerManager);
            } else if (EXPORT_JSON.equals(caName)) {
                ca = new ExportJSONCA(viewerManager);
            } else if (REPARENT_TIER.equals(caName)) {
                ca = new ReparentTierDlgCA(viewerManager);
            } else if (COPY_CURRENT_TIME.equals(caName)) {
                ca = new CopyCurrentTimeToPasteBoardCA(viewerManager);
            } else if (COPY_TIER_DLG.equals(caName)) {
                ca = new CopyTierDlgCA(viewerManager);
            } else if (NEXT_ACTIVE_TIER.equals(caName)) {
                ca = new NextActiveTierCA(viewerManager);
            } else if (PREVIOUS_ACTIVE_TIER.equals(caName)) {
                ca = new PreviousActiveTierCA(viewerManager);
            } else if (MERGE_TRANSCRIPTIONS.equals(caName)) {
                ca = new MergeTranscriptionDlgCA(viewerManager);
            } else if (SYNTAX_VIEWER.equals(caName)) {
                if (SyntaxViewerCommand.isEnabled()) {
                    ca = new SyntaxViewerCA(viewerManager);
                }
            } else if (CLOSE.equals(caName)) {
                ca = new CloseCA(viewerManager);
            } else if (KIOSK_MODE.equals(caName)) {
                ca = new KioskModeCA(viewerManager);
            } else if (IMPORT_PRAAT_GRID.equals(caName)) {
                ca = new ImportPraatGridCA(viewerManager);
            } else if (EXPORT_PRAAT_GRID.equals(caName)) {
                ca = new ExportPraatGridCA(viewerManager);
            } else if (LABEL_AND_NUMBER.equals(caName)) {
                ca = new LabelAndNumberCA(viewerManager);
            } else if (KEY_CREATE_ANNOTATION.equals(caName)) {
                ca = new KeyCreateAnnotationCA(viewerManager);
            } else if (EXPORT_WORDS.equals(caName)) {
                ca = new ExportWordsDialogCA(viewerManager);
            } else if (IMPORT_PREFS.equals(caName)) {
                ca = new ImportPrefsCA(viewerManager);
            } else if (EXPORT_PREFS.equals(caName)) {
                ca = new ExportPrefsCA(viewerManager);
            } else if (EXPORT_TOOLBOX.equals(caName)) {
                ca = new ExportToolboxDlgCA(viewerManager);
            } else if (EXPORT_FLEX.equals(caName)) {
                ca = new ExportFlexDlgCA(viewerManager);
            } else if (EXPORT_SUBTITLES.equals(caName)) {
                ca = new ExportSubtitlesCA(viewerManager);
            } else if (EXPORT_FILMSTRIP.equals(caName)) {
                ca = new ExportFilmStripCA(viewerManager);
            } else if (CENTER_SELECTION.equals(caName)) {
                ca = new CenterSelectionCA(viewerManager);
            } else if (EXPORT_JSON_TO_SERVER.equals(caName)) {
                ca = new ExportJSONToServerCA(viewerManager);
            } else if (LIST_IDS_FROM_SERVER.equals(caName)) {
                ca = new ListCollectionIDSCA(viewerManager);
            } else if (IMPORT_COLLECTION_FROM_SERVER.equals(caName)) {
                ca = new ImportCollectionFromServerCA(viewerManager);
            } else if (SET_AUTHOR.equals(caName)) {
                ca = new SetAuthorCA(viewerManager);
            } else if (SET_DOCUMENT_PROPERTIES.equals(caName)) {
                ca = new SetDocumentPropertiesDlgCA(viewerManager);
            } else if (DOCUMENT_INFO.equals(caName)) {
                ca = new DocumentInfoCA(viewerManager);
            } else if (ANNOTATION_DENSITY_PLOT.equals(caName)) {
                ca = new AnnotationDensityPlotCA(viewerManager);
            } else if (AUDIO_SPECTROGRAM.equals(caName)) {
                ca = new AudioSpectrogramCA(viewerManager);
            } else if (CHANGE_CASE.equals(caName)) {
                ca = new ChangeCaseDlgCA(viewerManager);
            } else if (CLIP_MEDIA.equals(caName)) {
                ca = new ClipMediaCA(viewerManager);
            } else if (EXPORT_RECOG_TIER.equals(caName)) {
                ca = new ExportTiersForRecognizerCA(viewerManager);
            } else if (IMPORT_RECOG_TIERS.equals(caName)) {
                ca = new ImportRecogTiersCA(viewerManager);
            }
            // For opening a Edit Lexicon Service Dialog:
            else if (EDIT_LEX_SRVC_DLG.equals(caName)) {
                ca = new EditLexSrvcDlgCA(viewerManager);
            } else if (MOVE_ANNOTATION_LBOUNDARY_LEFT.equals(caName)) {
                ca = new MoveActiveAnnLBoundarytoLeftCA(viewerManager);
            } else if (MOVE_ANNOTATION_LBOUNDARY_RIGHT.equals(caName)) {
                ca = new MoveActiveAnnLBoundarytoRightCA(viewerManager);
            } else if (MOVE_ANNOTATION_RBOUNDARY_LEFT.equals(caName)) {
                ca = new MoveActiveAnnRBoundarytoLeftCA(viewerManager);
            } else if (MOVE_ANNOTATION_RBOUNDARY_RIGHT.equals(caName)) {
                ca = new MoveActiveAnnRBoundarytoRightCA(viewerManager);
            } else if (PLAY_STEP_AND_REPEAT.equals(caName)) {
                ca = new PlayStepAndRepeatCA(viewerManager);
            } else if (WEBLICHT_DLG.equals(caName)) {
                ca = new WebLichtDlgCA(viewerManager);
            } else if (ANNOTATIONS_TO_TIERS.equals(caName)) {
                ca = new AnnotationValuesToTiersDlgCA(viewerManager);
            } else if (ADD_COMMENT.equals(caName)) {
                ca = new AddCommentCA(viewerManager);
            } else if (ZOOM_IN.equals(caName)) {
                ca = new ZoomInCA(viewerManager);
            } else if (ZOOM_OUT.equals(caName)) {
                ca = new ZoomOutCA(viewerManager);
            } else if (ZOOM_DEFAULT.equals(caName)) {
                ca = new ZoomToDefaultCA(viewerManager);
            } else if (CYCLE_TIER_SETS.equals(caName)) {
                ca = new CycleTierSetsCA(viewerManager);
            } else if (EXPORT_REGULAR_MULTITIER_EAF.equals(caName)) {
                ca = new ExportRegularMultitierEafCA(viewerManager);
            } else if (COPY_ANN_OF_TIER.equals(caName)) {
                ca = new CopyAnnotationsOfTierDlgCA(viewerManager);
            } else if (MODIFY_ANNOTATION_TIME_DLG.equals(caName)) {
                ca = new ModifyAnnotationTimeDlgCA(viewerManager);
            } else if (IMPORT_TAB.equals(caName)) {
                ca = new ImportDelimitedTextCA(viewerManager);
            } else if (IMPORT_SUBTITLE.equals(caName)) {
                ca = new ImportSubtitleTextCA(viewerManager);
            } else if (SET_CUSTOM_MS_PER_FRAME.equals(caName)) {
                ca = new SetCustomMsPerFrameCA(viewerManager);
            } else if (MODIFY_ANNOTATION_RESOURCE_URL.equals(caName)) {
                ca = new ModifyAnnotationResourceURLCA(viewerManager);
            }

            if (ca != null) {
                synchronized (ELANCommandFactory.class) {
                    commandActionHash.get(tr).put(caName, ca);
                }
            }
        }
        return ca;
    }

    /**
     * Creates and returns the command identified by the name.
     *
     * @param tr the transcription the command pertains to
     * @param cName the name of the command
     *
     * @return the command
     */
    public static Command createCommand(Transcription tr, String cName) {
        Command c = null;

        if (SET_TIER_NAME.equals(cName)) {
            c = new SetTierNameCommand(cName);
        }

        if (EDIT_TIER.equals(cName)) {
            c = new EditTierDlgCommand(cName);
        } else if (CHANGE_TIER.equals(cName)) {
            c = new ChangeTierAttributesCommand(cName);
        } else if (ADD_TIER.equals(cName)) {
            c = new AddTierCommand(cName);
        } else if (DELETE_TIER.equals(cName)) {
            c = new DeleteTierCommand(cName);
        } else if (DELETE_TIERS.equals(cName)) {
            c = new DeleteTiersCommand(cName);
        } else if (ADD_PARTICIPANT.equals(cName)) {
            c = new AddParticipantCommand(cName);
        } else if (ADD_PARTICIPANT_DLG.equals(cName)) {
            c = new AddParticipantDlgCommand(cName);
        } else if (ADD_DEPENDENT_TIERS_TO_TIER_STRUCTURE_CMD.equals(cName)) {
            c = new AddDependentTiersToTierStructureCommand(cName);
        } else if (DELETE_PARTICIPANT_DLG.equals(cName)) {
            c = new DeleteParticipantDlgCommand(cName);
        } else if (DELETE_PARTICIPANT.equals(cName)) {
            c = new DeleteParticipantCommand(cName);
        } else if (IMPORT_TIERS.equals(cName)) {
            c = new ImportTiersCommand(cName);
        } else if (EDIT_TYPE.equals(cName)) {
            c = new EditLingTypeDlgCommand(cName);
        } else if (ADD_TYPE.equals(cName)) {
            c = new AddTypeCommand(cName);
        } else if (CHANGE_TYPE.equals(cName)) {
            c = new ChangeTypeCommand(cName);
        } else if (DELETE_TYPE.equals(cName)) {
            c = new DeleteTypeCommand(cName);
        } else if (IMPORT_TYPES.equals(cName)) {
            c = new ImportLinguisticTypesCommand(cName);
        } else if (EDIT_CV_DLG.equals(cName)) {
            c = new EditCVDlgCommand(cName);
        } else if (ADD_CV.equals(cName)) {
            c = new AddCVCommand(cName);
        } else if (CHANGE_CV.equals(cName)) {
            c = new ChangeCVCommand(cName);
        } else if (DELETE_CV.equals(cName)) {
            c = new DeleteCVCommand(cName);
        } else if (REPLACE_CV.equals(cName)) {
            c = new ReplaceCVCommand(cName);
        } else if (ADD_CV_ENTRY.equals(cName)) {
            c = new AddCVEntryCommand(cName);
        } else if (CHANGE_CV_ENTRY.equals(cName)) {
            c = new ChangeCVEntryCommand(cName);
        } else if (DELETE_CV_ENTRY.equals(cName)) {
            c = new DeleteCVEntryCommand(cName);
        } else if (MOVE_CV_ENTRIES.equals(cName)) {
            c = new MoveCVEntriesCommand(cName);
        } else if (REPLACE_CV_ENTRIES.equals(cName)) {
            c = new ReplaceCVEntriesCommand(cName);
        } else if (MERGE_CVS.equals(cName)) {
            c = new MergeCVSCommand(cName);
        } else if (NEW_ANNOTATION.equals(cName)) {
            c = new NewAnnotationCommand(cName);
        } else if (NEW_ANNOTATION_REC.equals(cName)) {
            c = new NewAnnotationRecursiveCommand(cName);
        } else if (CREATE_DEPEND_ANN.equals(cName)) {
            c = new CreateDependentAnnotationsCommand(cName);
        } else if (NEW_ANNOTATION_BEFORE.equals(cName)) {
            c = new AnnotationBeforeCommand(cName);
        } else if (NEW_ANNOTATION_AFTER.equals(cName)) {
            c = new AnnotationAfterCommand(cName);
        } else if (NEW_ANNOTATIONS_IN_GAP.equals(cName)) {
            c = new NewAnnotationsInGap(cName);
        } else if (DUPLICATE_ANNOTATION.equals(cName)) {
            c = new DuplicateAnnotationCommand(cName);
        } else if (MERGE_ANNOTATION_WN.equals(cName)) {
            c = new MergeAnnotationsCommand(cName);
        } else if (MERGE_ANNOTATION_WB.equals(cName)) {
            c = new MergeAnnotationsCommand(cName);
        } else if (COPY_TO_NEXT_ANNOTATION.equals(cName)) {
            c = new CopyPreviousAnnotationCommand(cName);
        } else if (COPY_CURRENT_TIME.equals(cName)) {
            c = new CopyCurrentTimeToPasteBoardCommand(cName);
        } else if (COPY_ANNOTATION.equals(cName)) {
            c = new CopyAnnotationCommand(cName);
        } else if (COPY_ANNOTATION_TREE.equals(cName)) {
            c = new CopyAnnotationTreeCommand(cName);
        } else if (PASTE_ANNOTATION.equals(cName)) {
            c = new PasteAnnotationCommand(cName);
        } else if (PASTE_ANNOTATION_HERE.equals(cName)) {
            c = new PasteAnnotationCommand(cName);
        } else if (PASTE_ANNOTATION_TREE.equals(cName)) {
            c = new PasteAnnotationTreeCommand(cName);
        } else if (PASTE_ANNOTATION_TREE_HERE.equals(cName)) {
            c = new PasteAnnotationTreeCommand(cName);
        } else if (MODIFY_ANNOTATION_DLG.equals(cName)) {
            c = new ModifyAnnotationDlgCommand(cName);
        } else if (MODIFY_ANNOTATION.equals(cName)) {
            c = new ModifyAnnotationCommand(cName);
        } else if (MODIFY_ANNOTATION_DC_DLG.equals(cName)) {
            c = new ModifyAnnotationDatCatDlgCommand(cName);
        } else if (MODIFY_ANNOTATION_DC.equals(cName)) {
            c = new ModifyAnnotationDatCatCommand(cName);
        } else if (MODIFY_ALL_ANNOTATION_BOUNDARIES.equals(cName)) {
            c = new ModifyAllAnnotationsDlgCommand(cName);
        } else if (MODIFY_ALL_ANNOTATION_BOUNDARIES_CMD.equals(cName)) {
            c = new ModifyAllAnnotationsBoundariesCommand(cName);
        } else if (SHOW_IN_BROWSER.equals(cName)) {
            c = new ShowInBrowserCommand(cName);
        } else if (SPLIT_ANNOTATION.equals(cName)) {
            c = new SplitAnnotationCommand(cName);
        } else if (REMOVE_ANNOTATION_VALUE.equals(cName)) {
            c = new RemoveAnnotationValueCommand(cName);
        } else if (DELETE_ANNOTATION.equals(cName)) {
            c = new DeleteAnnotationCommand(cName);
        } else if (DELETE_ANNOS_IN_SELECTION.equals(cName)) {
            c = new DeleteAnnotationsCommand(cName);
        } else if (DELETE_MULTIPLE_ANNOS.equals(cName)) {
            c = new DeleteSelectedAnnotationsCommand(cName);
        } else if (MODIFY_ANNOTATION_TIME.equals(cName)) {
            c = new ModifyAnnotationTimeCommand(cName);
        } else if (MOVE_ANNOTATION_TO_TIER.equals(cName)) {
            c = new MoveAnnotationToTierCommand(cName);
        } else if (SHIFT_ALL_ANNOTATIONS.equals(cName)) {
            c = new ShiftAllAnnotationsCommand(cName);
        } else if (SHIFT_ANNOTATIONS.equals(cName)) {
            c = new ShiftAnnotationsCommand(cName);
        } else if (SHIFT_ALL_ANNOTATIONS_LROf.equals(cName)) {
            c = new ShiftAnnotationsLROfCommand(cName);
        } else if (SHIFT_ALL_DLG.equals(cName)) {
            c = new ShiftAllAnnotationsDlgCommand(cName);
        } else if (SHIFT_ANN_DLG.equals(cName)) {
            c = new ShiftAnnotationsDlgCommand(cName);
        } else if (SHIFT_ANN_ALLTIER_DLG.equals(cName)) {
            c = new ShiftAnnotationsLROfDlgCommand(cName);
        } else if (TOKENIZE_DLG.equals(cName)) {
            c = new TokenizeDlgCommand(cName);
        } else if (REGULAR_ANNOTATION_DLG.equals(cName)) {
            c = new RegularAnnotationDlgCommand(cName);
        } else if (REMOVE_ANNOTATIONS_OR_VALUES.equals(cName)) {
            c = new RemoveAnnotationsOrValuesCommand(cName);
        } else if (REGULAR_ANNOTATION.equals(cName)) {
            c = new RegularAnnotationCommand(cName);
        } else if (TOKENIZE_TIER.equals(cName)) {
            c = new TokenizeCommand(cName);
        } else if (ANN_FROM_OVERLAP.equals(cName)) {
            c = new AnnotationsFromOverlapsUndoableCommand(cName);
        } else if (ANN_FROM_SUBTRACTION.equals(cName)) {
            c = new AnnotationsFromSubtractionUndoableCommand(cName);
        } else if (ANN_FROM_OVERLAP_COM.equals(cName)) {
            c = new AnnotationsFromOverlapsDlgCommand(cName);
        } else if (ANN_FROM_SUBTRACTION_COM.equals(cName)) {
            c = new AnnotationsFromOverlapsDlgCommand(cName, true);
        }
        // temp
        else if (ANN_FROM_OVERLAP_CLAS.equals(cName)) {
            c = new AnnotationsFromOverlapsClasCommand(cName);
        } else if (ANN_FROM_OVERLAP_COM_CLAS.equals(cName)) {
            c = new AnnotationsFromOverlapsClasDlgCommand(cName);
        } else if (MERGE_TIERS_COM.equals(cName)) {
            c = new MergeTiersDlgCommand(cName);
        } else if (MERGE_TIERS.equals(cName)) {
            c = new MergeTiersUndoableCommand(cName);
        }
        // temp
        else if (MERGE_TIERS_DLG_CLAS.equals(cName)) {
            c = new MergeTiersClasDlgCommand(cName);
        } else if (MERGE_TIERS_CLAS.equals(cName)) {
            c = new MergeTiersClasCommand(cName);
        } else if (MERGE_TIER_GROUP_DLG.equals(cName)) {
            c = new MergeTierGroupDlgCommand(cName);
        } else if (MERGE_TIER_GROUP.equals(cName)) {
            c = new MergeTierGroupCommand(cName);
        } else if (ANN_ON_DEPENDENT_TIER.equals(cName)) {
            c = new CreateAnnsOnDependentTiersCommand(cName);
        } else if (ANN_ON_DEPENDENT_TIER_COM.equals(cName)) {
            c = new CreateAnnsOnDependentTiersDlgCommand(cName);
        } else if (ANN_FROM_GAPS.equals(cName)) {
            c = new AnnotationsFromGapsCommand(cName);
        } else if (ANN_FROM_GAPS_COM.equals(cName)) {
            c = new AnnotationsFromGapsDlgCommand(cName);
        } else if (ANNOTATOR_COMPARE_MULTI.equals(cName)) {
            c = new CompareAnnotatorsDlgCommand(cName);
        } else if (SHOW_MULTITIER_VIEWER.equals(cName)) {
            c = new ShowMultitierViewerCommand(cName);
        } else if (SEARCH_DLG.equals(cName)) {
            c = new SearchDialogCommand(cName);
        } else if (REPLACE.equals(cName)) {
            c = new ReplaceCommand(cName);
        } else if (GOTO_DLG.equals(cName)) {
            c = new GoToDialogCommand(cName);
        } else if (TIER_DEPENDENCIES.equals(cName)) {
            c = new TierDependenciesCommand(cName);
        } else if (SPREADSHEET.equals(cName)) {
            c = new SpreadSheetCommand(cName);
        } else if (STATISTICS.equals(cName)) {
            c = new StatisticsCommand(cName);
        } else if (SYNC_MODE.equals(cName)
                   || ANNOTATION_MODE.equals(cName)
                   || TRANSCRIPTION_MODE.equals(cName)
                   || SEGMENTATION_MODE.equals(cName)
                   || INTERLINEARIZATION_MODE.equals(cName)) {
            c = new ChangeModeCommand(cName);
        } else if (SELECTION_MODE.equals(cName)) {
            c = new SelectionModeCommand(cName);
        } else if (LOOP_MODE.equals(cName)) {
            c = new LoopModeCommand(cName);
        } else if (BULLDOZER_MODE.equals(cName)) {
            c = new BulldozerModeCommand(cName);
        } else if (TIMEPROP_NORMAL.equals(cName)) {
            c = new NormalTimePropCommand(cName);
        } else if (SHIFT_MODE.equals(cName)) {
            c = new ShiftModeCommand(cName);
        } else if (SET_PAL.equals(cName)) {
            c = new SetMsPerFrameCommand(cName);
        } else if (SET_PAL_50.equals(cName)) {
            c = new SetMsPerFrameCommand(cName);
        } else if (SET_NTSC.equals(cName)) {
            c = new SetMsPerFrameCommand(cName);
        } else if (CLEAR_SELECTION.equals(cName)) {
            c = new ClearSelectionCommand(cName);
        } else if (CLEAR_SELECTION_AND_MODE.equals(cName)) {
            c = new ClearSelectionAndModeCommand(cName);
        } else if (PLAY_SELECTION.equals(cName)) {
            c = new PlaySelectionCommand(cName);
        } else if (NEXT_FRAME.equals(cName)) {
            c = new NextFrameCommand(cName);
        } else if (PREVIOUS_FRAME.equals(cName)) {
            c = new PreviousFrameCommand(cName);
        } else if (PLAY_PAUSE.equals(cName)) {
            c = new PlayPauseCommand(cName);
        } else if (GO_TO_BEGIN.equals(cName)) {
            c = new GoToBeginCommand(cName);
        } else if (GO_TO_END.equals(cName)) {
            c = new GoToEndCommand(cName);
        } else if (PREVIOUS_SCROLLVIEW.equals(cName)) {
            c = new PreviousScrollViewCommand(cName);
        } else if (NEXT_SCROLLVIEW.equals(cName)) {
            c = new NextScrollViewCommand(cName);
        } else if (PIXEL_LEFT.equals(cName)) {
            c = new PixelLeftCommand(cName);
        } else if (PIXEL_RIGHT.equals(cName)) {
            c = new PixelRightCommand(cName);
        } else if (SECOND_LEFT.equals(cName)) {
            c = new SecondLeftCommand(cName);
        } else if (SECOND_RIGHT.equals(cName)) {
            c = new SecondRightCommand(cName);
        } else if (SELECTION_BOUNDARY.equals(cName)) {
            c = new ActiveSelectionBoundaryCommand(cName);
        } else if (SELECTION_CENTER.equals(cName)) {
            c = new ActiveSelectionCenterCommand(cName);
        } else if (SELECTION_BEGIN.equals(cName) || SELECTION_END.equals(cName)) {
            c = new ActiveSelectionBeginOrEndCommand(cName);
        } else if (ACTIVE_ANNOTATION.equals(cName)) {
            c = new ActiveAnnotationCommand(cName);
        } else if (ACTIVE_ANNOTATION_EDIT.equals(cName)) {
            c = new ActiveAnnotationEditCommand(cName);
        } else if (STORE.equals(cName)) {
            c = new StoreCommand(cName);
        } else if (BACKUP.equals(cName)) {
            c = new SetBackupDelayCommand(cName);
        } else if (PRINT.equals(cName)) {
            c = PrintCommand.getInstance().withCommandName(cName);
        } else if (PREVIEW.equals(cName)) {
            c = new PrintPreviewCommand(cName);
        } else if (PAGESETUP.equals(cName)) {
            c = new PageSetupCommand(cName);
        } else if (EXPORT_TAB.equals(cName)) {
            c = new ExportTabDelDlgCommand(cName);
        } else if (EXPORT_TEX.equals(cName)) {
            c = new ExportTeXDlgCommand(cName);
        } else if (EXPORT_TIGER.equals(cName)) {
            c = new ExportTigerDlgCommand(cName);
        } else if (EXPORT_SMIL_RT.equals(cName)) {
            c = new ExportSmilCommand(cName);
        } else if (EXPORT_SMIL_QT.equals(cName)) {
            c = new ExportSmilQTCommand(cName);
        } else if (EXPORT_QT_SUB.equals(cName)) {
            c = new ExportQtSubCommand(cName);
        } else if (EXPORT_IMAGE_FROM_WINDOW.equals(cName)) {
            c = new ExportImageFromWindowCommand(cName);
        } else if (EXPORT_SHOEBOX.equals(cName)) {
            c = new ExportShoeboxCommand(cName);
        } else if (EXPORT_CHAT.equals(cName)) {
            c = new ExportCHATCommand(cName);
        } else if (LINKED_FILES_DLG.equals(cName)) {
            c = new LinkedFilesDlgCommand(cName);
        } else if (CHANGE_LINKED_FILES.equals(cName)) {
            c = new ChangeLinkedFilesCommand(cName);
        } else if (PLAYBACK_RATE_TOGGLE.equals(cName)) {
            c = new PlaybackRateToggleCommand(cName);
        } else if (PLAYBACK_VOLUME_TOGGLE.equals(cName)) {
            c = new PlaybackVolumeToggleCommand(cName);
        } else if (ADD_SEGMENTATION.equals(cName)) {
            c = new AddSegmentationCommand(cName);
        } else if (FILTER_TIER_DLG.equals(cName)) {
            c = new FilterTierDlgCommand(cName);
        } else if (FILTER_TIER.equals(cName)) {
            c = new FilterTierCommand(cName);
        } else if (EXPORT_TRAD_TRANSCRIPT.equals(cName)) {
            c = new ExportTradTranscriptDlgCommand(cName);
        } else if (EXPORT_TA_INTERLINEAR_GLOSS.equals(cName)) {
            c = new ExportTimeAlignedInterlinearDlgCommand(cName);
        } else if (EXPORT_INTERLINEAR.equals(cName)) {
            c = new ExportInterlinearDlgCommand(cName);
        } else if (EXPORT_HTML.equals(cName)) {
            c = new ExportHTMLDlgCommand(cName);
        } else if (EXPORT_JSON.equals(cName)) {
            c = new ExportJSONCommand(cName);
        } else if (EXPORT_JSON_TO_SERVER.equals(cName)) {
            c = new ExportJSONToServerCommand(cName);
        } else if (LIST_IDS_FROM_SERVER.equals(cName)) {
            c = new ListCollectionIDSCommand(cName);
        } else if (IMPORT_COLLECTION_FROM_SERVER.equals(cName)) {
            c = new ImportCollectionFromServerCommand(cName);
        } else if (REPARENT_TIER_DLG.equals(cName)) {
            c = new ReparentTierDlgCommand(cName);
        } else if (COPY_TIER_DLG.equals(cName)) {
            c = new CopyTierDlgCommand(cName);
        } else if (COPY_TIER.equals(cName) || REPARENT_TIER.equals(cName)) {
            c = new CopyTierCommand(cName);
        } else if (SAVE_SELECTION_AS_EAF.equals(cName)) {
            c = new SaveSelectionAsEafCommand(cName);
        } else if (ACTIVE_TIER.equals(cName)) {
            c = new ActiveTierCommand(cName);
        } else if (MERGE_TRANSCRIPTIONS.equals(cName)) {
            c = new MergeTranscriptionsDlgCommand(cName);
        } else if (SYNTAX_VIEWER.equals(cName)) {
            c = new SyntaxViewerCommand(cName);
        } else if (EXT_TRACK_DATA.equals(cName)) {
            c = new ExtractTrackDataCommand(cName);
        } else if (CLOSE.equals(cName)) {
            c = new CloseCommand(cName);
        } else if (KIOSK_MODE.equals(cName)) {
            c = new KioskModeCommand(cName);
        } else if (IMPORT_PRAAT_GRID.equals(cName)) {
            c = new ImportPraatGridCommand(cName);
        } else if (IMPORT_PRAAT_GRID_DLG.equals(cName)) {
            c = new ImportPraatGridDlgCommand(cName);
        } else if (EXPORT_PRAAT_GRID.equals(cName)) {
            c = new ExportPraatGridCommand(cName);
        } else if (LABEL_N_NUM_DLG.equals(cName)) {
            c = new LabelAndNumberDlgCommand(cName);
        } else if (REMOVE_ANNOTATIONS_OR_VALUES_DLG.equals(cName)) {
            c = new RemoveAnnotationsOrValuesDlgCommand(cName);
        } else if (LABEL_AND_NUMBER.equals(cName)) {
            c = new LabelAndNumberCommand(cName);
        } else if (EXPORT_WORDS.equals(cName)) {
            c = new ExportWordsDialogCommand(cName);
        } else if (IMPORT_PREFS.equals(cName)) {
            c = new ImportPrefsCommand(cName);
        } else if (EXPORT_PREFS.equals(cName)) {
            c = new ExportPrefsCommand(cName);
        } else if (EXPORT_TOOLBOX.equals(cName)) {
            c = new ExportToolboxDlgCommand(cName);
        } else if (EXPORT_FLEX.equals(cName)) {
            c = new ExportFlexDlgCommand(cName);
        } else if (EXPORT_SUBTITLES.equals(cName)) {
            c = new ExportSubtitlesCommand(cName);
        } else if (EXPORT_FILMSTRIP.equals(cName)) {
            c = new ExportFilmStripDlgCommand(cName);
        } else if (SEGMENTS_2_TIER_DLG.equals(cName)) {
            c = new SegmentsToTiersDlgCommand(cName);
        } else if (SEGMENTS_2_TIER.equals(cName)) {
            c = new SegmentsToTiersCommand(cName);
        } else if (CENTER_SELECTION.equals(cName)) {
            c = new CenterSelectionCommand(cName);
        } else if (SET_AUTHOR.equals(cName)) {
            c = new SetAuthorCommand(cName);
        } else if (SET_DOCUMENT_PROPERTIES.equals(cName)) {
            c = new SetDocumentPropertiesCommand(cName);
        } else if (SET_DOCUMENT_PROPERTIES_DLG.equals(cName)) {
            c = new SetDocumentPropertiesDlgCommand(cName);
        } else if (DOCUMENT_INFO.equals(cName)) {
            c = new DocumentInfoCommand(cName);
        } else if (ANNOTATION_DENSITY_PLOT.equals(cName)) {
            c = new AnnotationDensityPlotCommand(cName);
        } else if (AUDIO_SPECTROGRAM.equals(cName)) {
            c = new AudioSpectrogramCommand(cName);
        } else if (CHANGE_CASE.equals(cName)) {
            c = new ChangeCaseCommand(cName);
        } else if (CHANGE_CASE_COM.equals(cName)) {
            c = new ChangeCaseDlgCommand(cName);
        } else if (CLIP_MEDIA.equals(cName)) {
            c = new ClipMediaCommand(cName);
        } else if (EXPORT_RECOG_TIER.equals(cName)) {
            c = new ExportTiersForRecognizerCommand(cName);
        } else if (ADD_TRACK_AND_PANEL.equals(cName)) {
            c = new AddTSTrackAndPanelCommand(cName);
        } else if (IMPORT_RECOG_TIERS.equals(cName)) {
            c = new ImportRecogTiersCommand(cName);
        }
        // For Lexicon Service editing:
        else if (EDIT_LEX_SRVC_DLG.equals(cName)) {
            c = new EditLexSrvcDlgCommand(cName);
        } else if (ADD_LEX_LINK.equals(cName)) {
            c = new AddLexLinkCommand(cName);
        } else if (CHANGE_LEX_LINK.equals(cName)) {
            c = new ChangeLexLinkCommand(cName);
        } else if (DELETE_LEX_LINK.equals(cName)) {
            c = new DeleteLexLinkCommand(cName);
        } else if (PLAY_STEP_AND_REPEAT.equals(cName)) {
            c = new PlayStepAndRepeatCommand(cName);
        } else if (WEBSERVICES_DLG.equals(cName)) {
            c = new WebServicesDlgCommand(cName);
        } else if (MERGE_TRANSCRIPTIONS_UNDOABLE.equals(cName)) {
            c = new MergeTranscriptionsByAddingCommand(cName);
        } else if (WEBLICHT_MERGE_TRANSCRIPTIONS.equals(cName)) {
            c = new MergeTranscriptionsByAddingCommand(cName);
        } else if (ANNOTATIONS_TO_TIERS_DLG.equals(cName)) {
            c = new AnnotationValuesToTiersDlgCommand(cName);
        } else if (ANNOTATIONS_TO_TIERS.equals(cName)) {
            c = new AnnotationValuesToTiersCommand(cName);
        } else if (ADD_COMMENT.equals(cName)) {
            c = new AddCommentCommand(cName);
        } else if (DELETE_COMMENT.equals(cName)) {
            c = new DeleteCommentCommand(cName);
        } else if (CHANGE_COMMENT.equals(cName)) {
            c = new ChangeCommentCommand(cName);
        } else if (MODIFY_OR_ADD_DEPENDENT_ANNOTATIONS.equals(cName)) {
            c = new ModifyOrAddDependentAnnotationsCommand(cName);
        } else if (ZOOM_IN.equals(cName) || ZOOM_OUT.equals(cName) || ZOOM_DEFAULT.equals(cName)) {
            c = new ZoomCommand(cName);
        } else if (CYCLE_TIER_SETS.equals(cName)) {
            c = new CycleTierSetsCommand(cName);
        } else if (ANNS_FROM_SUGGESTION_SET.equals(cName)) {
            c = new AnnotationsFromSuggestionSetCommand(cName);
        } else if (EXPORT_REGULAR_MULTITIER_EAF.equals(cName)) {
            c = new ExportRegularMultitierEafCommand(cName);
        } else if (COPY_ANN_OF_TIER_DLG.equals(cName)) {
            c = new CopyAnnotationsOfTierDlgCommand(cName);
        } else if (COPY_ANN_OF_TIER.equals(cName)) {
            c = new CopyAnnotationsOfTierCommand(cName);
        } else if (MODIFY_ANNOTATION_TIME_DLG.equals(cName)) {
            c = new ModifyAnnotationTimeDlgCommand(cName);
        } else if (NEW_ANNOTATION_FROM_BIGIN_END_TIME_DLG.equals(cName)) {
            c = new NewAnnotationFromBeginEndTimeDlgCommand(cName);
        } else if (IMPORT_TAB_DLG.equals(cName)) {
            c = new ImportDelimitedTextDlgCommand(cName);
        } else if (IMPORT_SUBTITLE_DLG.equals(cName)) {
            c = new ImportSubtitleTextDlgCommand(cName);
        } else if (IMPORT_TAB.equals(cName) || IMPORT_SUBTITLE.equals(cName)) {
            c = new ImportDelimitedTextCommand(cName);
        } else if (SET_CUSTOM_MS_PER_FRAME.equals(cName)) {
            c = new SetCustomMsPerFrameCommand(cName);
        } else if (MODIFY_ANNOTATION_RESOURCE_URL_DLG.equals(cName)) {
            c = new ModifyAnnotationResourceURLDlgCommand(cName);
        } else if (MODIFY_ANNOTATION_RESOURCE_URL.equals(cName)) {
            c = new ModifyAnnotationResourceURLCommand(cName);
        } else if (ADD_DEPENDENT_TIERS_TO_TIER_STRUCTURE.equals(cName)) {
            c = new AddDependentTiersToTierStructureDlgCommand(cName);
        }

        if (c instanceof UndoableCommand) {
            synchronized (commandHistoryHash) {
                commandHistoryHash.get(tr).addCommand((UndoableCommand) c);
            }
        }

        return c;
    }

    /**
     * Returns the undo command action. This is the action which is added to the Edit menu and which calls the command
     * history's {@code undo}.
     *
     * @param tr the transcription to get the undo action for
     *
     * @return the undo action
     */
    public static synchronized UndoCA getUndoCA(Transcription tr) {
        UndoCA undoCA = undoCAHash.get(tr);

        if (undoCAHash.get(tr) == null) {
            undoCA = new UndoCA(viewerManagerHash.get(tr), commandHistoryHash.get(tr));
            commandHistoryHash.get(tr).setUndoCA(undoCA);

            undoCAHash.put(tr, undoCA);
        }

        return undoCA;
    }

    /**
     * Returns the redo command action. This is the action which is added to the Edit menu and which calls the command
     * history's {@code redo}.
     *
     * @param tr the transcription to get the redo action for
     *
     * @return the redo action
     */
    public static synchronized RedoCA getRedoCA(Transcription tr) {
        RedoCA redoCA = redoCAHash.get(tr);

        if (redoCA == null) {
            redoCA = new RedoCA(viewerManagerHash.get(tr), commandHistoryHash.get(tr));
            commandHistoryHash.get(tr).setRedoCA(redoCA);

            redoCAHash.put(tr, redoCA);
        }

        return redoCA;
    }

    /**
     * Returns the Locale for the specified key.
     *
     * @param key a CommandAction language key
     *
     * @return the associated Locale, defaults to English
     */
    public static Locale getLocaleForKey(Object key) {
        if (key != null) {
            Locale l = languages.get(key);
            if (l != null) {
                return l;
            }
        }
        // default english
        return ElanLocale.ENGLISH;
    }

    /**
     * Returns a Set view of the registered Locales.
     *
     * @return a Set view of the registered Locales
     */
    public static Collection<Locale> getLocales() {
        return languages.values();
    }

    /**
     * Refinement of the shortcuts table texts with grouping of related actions and with
     * a sub-header per group.
     * The method now returns a 2 dim. array of Objects instead of Strings
     *
     * <p>This function is unused, but if it were used, it could be simplified a lot
     * by using a few separate commandConstants arrays instead of one,
     * and abstracting the handling loop into a separate function instead
     * of it being duplicated.
     *
     * @param tr the transcription
     * @return a 2 dimensional array of Objects
     */
    //    public static Object[][] getShortCutText(Transcription tr) {
    //        ArrayList<Object> shortCuts = new ArrayList<Object>();
    //        ArrayList<Object> descriptions = new ArrayList<Object>();
    //        CommandAction ca;
    //        KeyStroke acc;
    //        String accString;
    //        String descString;
    //        int index = 0;
    //
    //        // start with subheader for the annotation editing group
    //        shortCuts.add(new TableSubHeaderObject(ElanLocale.getString("Frame.ShortcutFrame.Sub.AnnotationEdit")));
    //        descriptions.add(new TableSubHeaderObject(null));
    //        for (int i = 0; i < 20; i++) {
    //            ca = getCommandAction(tr, commandConstants[i]);
    //            acc = (KeyStroke) ca.getValue(Action.ACCELERATOR_KEY);
    //
    //            if (acc != null) {
    //                accString = convertAccKey(acc);
    //                descString = (String) ca.getValue(Action.SHORT_DESCRIPTION);
    //
    //                if (descString == null) {
    //                    descString = "";
    //                }
    //
    //                if (accString != null) {
    //                    shortCuts.add(accString);
    //                    descriptions.add(descString);
    //                }
    //            }
    //            index = i;
    //        }
    //        // annotation navigation group
    //        shortCuts.add(new TableSubHeaderObject(ElanLocale.getString("Frame.ShortcutFrame.Sub.AnnotationNavigation")));
    //        descriptions.add(new TableSubHeaderObject(null));
    //        for (int i = ++index, j = 0; j < 6; i++, j++) {
    //            ca = getCommandAction(tr, commandConstants[i]);
    //            acc = (KeyStroke) ca.getValue(Action.ACCELERATOR_KEY);
    //
    //            if (acc != null) {
    //                accString = convertAccKey(acc);
    //                descString = (String) ca.getValue(Action.SHORT_DESCRIPTION);
    //
    //                if (descString == null) {
    //                    descString = "";
    //                }
    //
    //                if (accString != null) {
    //                    shortCuts.add(accString);
    //                    descriptions.add(descString);
    //                }
    //            }
    //            index = i;
    //        }
    //        // tier and type
    //        shortCuts.add(new TableSubHeaderObject(ElanLocale.getString("Frame.ShortcutFrame.Sub.TierType")));
    //        descriptions.add(new TableSubHeaderObject(null));
    //        for (int i = ++index, j = 0; j < 5; i++, j++) {
    //            ca = getCommandAction(tr, commandConstants[i]);
    //            acc = (KeyStroke) ca.getValue(Action.ACCELERATOR_KEY);
    //
    //            if (acc != null) {
    //                accString = convertAccKey(acc);
    //                descString = (String) ca.getValue(Action.SHORT_DESCRIPTION);
    //
    //                if (descString == null) {
    //                    descString = "";
    //                }
    //
    //                if (accString != null) {
    //                    shortCuts.add(accString);
    //                    descriptions.add(descString);
    //                }
    //            }
    //            index = i;
    //        }
    //        // selection
    //        shortCuts.add(new TableSubHeaderObject(ElanLocale.getString("Frame.ShortcutFrame.Sub.Selection")));
    //        descriptions.add(new TableSubHeaderObject(null));
    //        for (int i = ++index, j = 0; j < 7; i++, j++) {
    //            ca = getCommandAction(tr, commandConstants[i]);
    //            acc = (KeyStroke) ca.getValue(Action.ACCELERATOR_KEY);
    //
    //            if (acc != null) {
    //                accString = convertAccKey(acc);
    //                descString = (String) ca.getValue(Action.SHORT_DESCRIPTION);
    //
    //                if (descString == null) {
    //                    descString = "";
    //                }
    //
    //                if (accString != null) {
    //                    shortCuts.add(accString);
    //                    descriptions.add(descString);
    //                }
    //            }
    //            index = i;
    //        }
    //        // media navigation group
    //        shortCuts.add(new TableSubHeaderObject(ElanLocale.getString("Frame.ShortcutFrame.Sub.MediaNavigation")));
    //        descriptions.add(new TableSubHeaderObject(null));
    //        for (int i = ++index, j = 0; j < 15; i++, j++) {
    //            ca = getCommandAction(tr, commandConstants[i]);
    //            acc = (KeyStroke) ca.getValue(Action.ACCELERATOR_KEY);
    //
    //            if (acc != null) {
    //                accString = convertAccKey(acc);
    //                descString = (String) ca.getValue(Action.SHORT_DESCRIPTION);
    //
    //                if (descString == null) {
    //                    descString = "";
    //                }
    //
    //                if (accString != null) {
    //                    shortCuts.add(accString);
    //                    descriptions.add(descString);
    //                }
    //            }
    //            index = i;
    //        }
    //        // document group
    //        shortCuts.add(new TableSubHeaderObject(ElanLocale.getString("Frame.ShortcutFrame.Sub.Document")));
    //        descriptions.add(new TableSubHeaderObject(null));
    //
    //        accString = convertAccKey(KeyStroke.getKeyStroke(KeyEvent.VK_N,
    //            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    //        descString = ElanLocale.getString("Menu.File.NewToolTip");
    //
    //        if (accString != null) {
    //            shortCuts.add(accString);
    //            descriptions.add(descString);
    //        }
    //
    //        accString = convertAccKey(KeyStroke.getKeyStroke(KeyEvent.VK_O,
    //            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    //        descString = ElanLocale.getString("Menu.File.OpenToolTip");
    //
    //        if (accString != null) {
    //            shortCuts.add(accString);
    //            descriptions.add(descString);
    //        }
    //
    //        for (int i = ++index, j = 0; j < 6; i++, j++) {
    //            ca = getCommandAction(tr, commandConstants[i]);
    //            acc = (KeyStroke) ca.getValue(Action.ACCELERATOR_KEY);
    //
    //            if (acc != null) {
    //                accString = convertAccKey(acc);
    //                descString = (String) ca.getValue(Action.SHORT_DESCRIPTION);
    //
    //                if (descString == null) {
    //                    descString = "";
    //                }
    //
    //                if (accString != null) {
    //                    shortCuts.add(accString);
    //                    descriptions.add(descString);
    //                }
    //            }
    //            index = i;
    //        }
    //
    //        accString = convertAccKey(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,
    //            ActionEvent.SHIFT_MASK));
    //        descString = ElanLocale.getString("Menu.Window.NextToolTip");
    //
    //        if (accString != null) {
    //            shortCuts.add(accString);
    //            descriptions.add(descString);
    //        }
    //
    //        accString = convertAccKey(KeyStroke.getKeyStroke(KeyEvent.VK_UP,
    //            ActionEvent.SHIFT_MASK));
    //        descString = ElanLocale.getString("Menu.Window.PreviousToolTip");
    //
    //        if (accString != null) {
    //            shortCuts.add(accString);
    //            descriptions.add(descString);
    //        }
    //
    //        accString = convertAccKey(KeyStroke.getKeyStroke(KeyEvent.VK_W,
    //            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    //        descString = ElanLocale.getString("Menu.File.CloseToolTip");
    //
    //        if (accString != null) {
    //            shortCuts.add(accString);
    //            descriptions.add(descString);
    //        }
    //
    //        accString = convertAccKey(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
    //            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    //        descString = ElanLocale.getString("Menu.File.ExitToolTip");
    //
    //        if (accString != null) {
    //            shortCuts.add(accString);
    //            descriptions.add(descString);
    //        }
    //        // miscellaneous
    //        shortCuts.add(new TableSubHeaderObject(ElanLocale.getString("Frame.ShortcutFrame.Sub.Misc")));
    //        descriptions.add(new TableSubHeaderObject(null));
    //
    //        ca = getUndoCA(tr);
    //        accString = convertAccKey((KeyStroke) ca.getValue(Action.ACCELERATOR_KEY));
    //        descString = ElanLocale.getString("Menu.Edit.Undo");
    //
    //        if (accString != null) {
    //            shortCuts.add(accString);
    //            descriptions.add(descString);
    //        }
    //
    //        ca = getRedoCA(tr);
    //        accString = convertAccKey((KeyStroke) ca.getValue(Action.ACCELERATOR_KEY));
    //        descString = ElanLocale.getString("Menu.Edit.Redo");
    //
    //        if (accString != null) {
    //            shortCuts.add(accString);
    //            descriptions.add(descString);
    //        }
    //
    //        for (int i = ++index, j = 0; j < 7; i++, j++) {
    //            ca = getCommandAction(tr, commandConstants[i]);
    //            acc = (KeyStroke) ca.getValue(Action.ACCELERATOR_KEY);
    //
    //            if (acc != null) {
    //                accString = convertAccKey(acc);
    //                descString = (String) ca.getValue(Action.SHORT_DESCRIPTION);
    //
    //                if (descString == null) {
    //                    descString = "";
    //                }
    //
    //                if (accString != null) {
    //                    shortCuts.add(accString);
    //                    descriptions.add(descString);
    //                }
    //            }
    //            index = i;
    //        }
    //        accString = convertAccKey(KeyStroke.getKeyStroke(KeyEvent.VK_H,
    //                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    //        descString = ElanLocale.getString("Menu.Help.Contents");
    //        if (accString != null) {
    //            shortCuts.add(accString);
    //            descriptions.add(descString);
    //        }
    //
    //        accString = convertAccKey(KeyStroke.getKeyStroke(KeyEvent.VK_1,
    //                ActionEvent.ALT_MASK + ActionEvent.SHIFT_MASK +
    //                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    //        accString = accString.substring(0, accString.length() - 2);
    //        shortCuts.add(accString);
    //        descriptions.add(ElanLocale.getString("MultiTierViewer.ShiftToolTip"));
    //
    //        // create array
    //        Object[][] resultTable = new Object[shortCuts.size()][2];
    //
    //        for (int j = 0; j < shortCuts.size(); j++) {
    //            resultTable[j][0] = shortCuts.get(j);
    //            resultTable[j][1] = descriptions.get(j);
    //        }
    //        return resultTable;
    //    }

    //Input is something like: 'Keycode Ctrl+Alt+ShiftB-P'
    //Matching output: 'Ctrl+Alt+Shift+B'
    //
    //The order of Ctrl, Alt and Shift is always like this, regardless of the order
    //when the accelerator was made.

    /**
     * The String representation has changed in J1.5. Therefore the construction of the shortcut (accelerator) text is now
     * based only only the modifiers and the KeyCode or KeyChar.
     *
     * @param acc the key stroke
     *
     * @return a platform dependent string representation
     */
    public static String convertAccKey(KeyStroke acc) {
        // special case for the Mac
        if (System.getProperty("os.name").startsWith("Mac")) {
            return convertMacAccKey(acc);
        }
        int modifier = acc.getModifiers();
        String nwAcc = "";
        if ((modifier & InputEvent.CTRL_MASK) != 0) {
            nwAcc += "Ctrl+";
        }
        if ((modifier & InputEvent.ALT_MASK) != 0) {
            nwAcc += "Alt+";
        }
        if ((modifier & InputEvent.SHIFT_MASK) != 0) {
            nwAcc += "Shift+";
        }
        if (acc.getKeyChar() == KeyEvent.CHAR_UNDEFINED) {
            nwAcc += KeyEvent.getKeyText(acc.getKeyCode());
        } else {
            nwAcc += String.valueOf(acc.getKeyChar());
        }
        return nwAcc;
    }

    /**
     * @param acc the KeyStroke
     *
     * @return a String representation
     *
     * @see #convertAccKey(KeyStroke)
     */
    private static String convertMacAccKey(KeyStroke acc) {
        int modifier = acc.getModifiers();
        String nwAcc = "";
        if ((modifier & InputEvent.META_MASK) != 0) {
            nwAcc += "Command+";
        }
        if ((modifier & InputEvent.CTRL_MASK) != 0) {
            nwAcc += "Ctrl+";
        }
        if ((modifier & InputEvent.ALT_MASK) != 0) {
            nwAcc += "Alt+";
        }
        if ((modifier & InputEvent.SHIFT_MASK) != 0) {
            nwAcc += "Shift+";
        }
        if (acc.getKeyChar() == KeyEvent.CHAR_UNDEFINED) {
            nwAcc += KeyEvent.getKeyText(acc.getKeyCode());
        } else {
            nwAcc += String.valueOf(acc.getKeyChar());
        }
        return nwAcc;
    }
}
