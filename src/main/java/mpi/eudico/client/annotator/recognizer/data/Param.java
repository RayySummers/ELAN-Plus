package mpi.eudico.client.annotator.recognizer.data;

/**
 * Lightweight base class for recognizer parameter. 
 * All members are public.
 * 
 * @author Han Sloetjes
 */
public abstract class Param implements Cloneable {
	/** the identifier of the parameter */
	public String id;
	/** Information string for ui etc. */
	public String info;
	/** the level of the parameter */
	public String level = Param.BASIC ;	
	
	/** the basic level constant */
	public static final String BASIC= "basic";
	/** the advanced level constant */
	public static final String ADVANCED= "advanced";
	
	/**
	 * Constructor
	 */
	public Param() {
		super();
	}
	
	/**
	 * Constructor.
	 * @param id the id
	 * @param info the information string
	 */
	public Param(String id, String info) {
		this(id, info, Param.BASIC);
	}
	
	/**
	 * Constructor.
	 * @param id the id
	 * @param info the information string
	 * @param level the level of the parameter
	 */
	public Param(String id, String info, String level) {
		super();
		this.id = id;
		this.info = info;
		this.level = level;
	}
	
	@Override
	public Param clone() throws CloneNotSupportedException {
		return (Param) super.clone();
	}
	
}
