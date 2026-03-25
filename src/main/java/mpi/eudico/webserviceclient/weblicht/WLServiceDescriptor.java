package mpi.eudico.webserviceclient.weblicht;
/**
 * A minimalistic class for storing some information about WebLicht
 * web services.
 * 
 * @author Han Sloetjes
 */
public class WLServiceDescriptor {
	/** name property */
	public String name;
	/** description property */
	public String description;
	/** creator property */
	public String creator;
	/** full URL property */
	public String fullURL;
	/** plain text input property */
	public boolean plainTextInput;
	/** TCF input property */
	public boolean tcfInput;
	/** TCF output property */
	public boolean tcfOutput;
	/** sentence input property */
	public boolean sentenceInput;
	/** tokens input property */
	public boolean tokensInput;
	/** sentence output property */
	public boolean sentenceOutput;
	/** tokens output property */
	public boolean tokensOutput;
	// output
	/** POS tags support property */
	public boolean posTagSupport;
	/** lemma support property */
	public boolean lemmaSupport;
	
	/**
	 * Creates a new descriptor instance.
	 * 
	 * @param name the name of the service
	 */
	public WLServiceDescriptor(String name) {
		super();
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
	
}
