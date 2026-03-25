package mpi.eudico.webserviceclient.weblicht;
/**
 * Some constants concerning the TCF format, contains only a subset of the format.
 * 
 * @author Han Sloetjes
 */
public class TCFConstants {
	/** Private constructor */
	private TCFConstants() {
		super();
	}
	/** text element */
	public static final String TEXT = "text";
	/** sentence element */
	public static final String SENT = "sentence";
	/** token element */
	public static final String TOKEN = "token";
	/** POStags element */
	public static final String POSTAGS = "POStags";
	/** lemma element */
	public static final String LEMMA = "lemma";
	/** tag element */
	public static final String TAG = "tag";
	/** id attribute */
	public static final String ID = "ID";
	/** tokenIDs attribute*/
	public static final String TOKEN_IDS = "tokenIDs";
}
