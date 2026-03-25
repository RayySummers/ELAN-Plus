package mpi.eudico.client.annotator.recognizer.data;

/**
 * A class for numerical parameters. 
 * 
 * @author Han Sloetjes
 */
public class NumParam extends Param implements Cloneable {
	/** the minimum value */
	public float min;
	/** the maximum value */
	public float max;
	/** the default value */
	public float def;//default
	/** the current, user provided or stored value */
	public float current = Float.MIN_VALUE;
	/** the number of decimal positions */
	public int precision = 1;
	/** the type used to represent the values in the UI */
	public String type = FLOAT;
	
	/** constant used for {@code integer} numbers */
	public static final String INT= "int";
	/** constant used for {@code floating point} numbers */
	public static final String FLOAT= "float";
	
	/**
	 * Constructor.
	 */
	public NumParam() {
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param id the id of the parameter
	 * @param info the description
	 */
	public NumParam(String id, String info) {
		super(id, info);
	}


	/**
	 * Creates a clone of this object.
	 */
	@Override
	public NumParam clone() throws CloneNotSupportedException {
		return (NumParam) super.clone();
	}
}
