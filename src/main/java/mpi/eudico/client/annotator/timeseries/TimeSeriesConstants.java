package mpi.eudico.client.annotator.timeseries;

/**
 * Defines constants for time series tracks and viewer.
 * 
 * @author Han Sloetjes
 */
public interface TimeSeriesConstants {
	/** constant for fixed rate */
	public static final String FIXED_RATE = "Fixed rate";
	/** constant for variable rate */
	public static final String VARIABLE_RATE = "Variable rate";
	/** constant for unknown rate type */
	public static final String UNKNOWN_RATE_TYPE = "Unknown";
	/** constant for discontinuous rate */
	public static final String DISCONTINUOUS_RATE = "Discontinuous Rate";
	/** constant for continuous rate */
	public static final String CONTINUOUS_RATE = "Continuous Rate";
	
	// keys for storage of properties
	/** auto detect range property */
    public static final String AUTO_DETECT_RANGE = "detect-range";
    /** position element */
    public static final String SAMPLE_POS = "pos";
    
    // configuration xml strings
    /** file suffix */
    public static final String CONF_SUFFIX = "_tsconf.xml";
    /** timeseries element */
    public static final String TIMESERIES = "timeseries";
    /** date attribute */
    public static final String DATE = "date";
    /** version attribute */
    public static final String VERS = "version";
    /** tracksource element */
    public static final String SOURCE = "tracksource";
    /** source url attribute */
    public static final String URL = "source-url";
    /** time-origin / offset attribute */
    public static final String ORIGIN = "time-origin";
    /** (index of) time column attribute */
    public static final String TIME_COLUMN = "time-column";
    /** sample type attribute */
    public static final String SAMPLE_TYPE = "sample-type";
    /** provider attribute value */
    public static final String PROVIDER = "provider";
    /** property element */
    public static final String PROP = "property";
    /** key attribute */
    public static final String KEY = "key";
    /** value attribute */
    public static final String VALUE = "value";
    /** track element */
    public static final String TRACK = "track";
    /** name attribute */
    public static final String NAME = "name";
    /** derivative attribute */
    public static final String DERIVATION = "derivative";
    /** description element */
    public static final String DESC = "description";
    /** units element */
    public static final String UNITS = "units";
    /** sample-position element  */
    public static final String POSITION = "sample-position";
    /** row (index) attribute */
    public static final String ROW = "row";
    /** column (index) attribute */
    public static final String COL = "col";
    /** range element */
    public static final String RANGE = "range";
    /** minimum attribute */
    public static final String MIN = "min";
    /** maximum attribute */
    public static final String MAX = "max";
    /** data type attribute */
    public static final String DATA_TYPE = "data-type";
    /** color element */
    public static final String COLOR = "color";
}
