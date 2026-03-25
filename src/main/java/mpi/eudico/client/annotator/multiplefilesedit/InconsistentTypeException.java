package mpi.eudico.client.annotator.multiplefilesedit;

import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;

/**
 * An exception thrown in case of an inconsistency in tier types in multiple
 * files.
 */
public class InconsistentTypeException extends Exception {
	private static final long serialVersionUID = -8494027511309895429L;
	private LinguisticType inconsistent_type;
	
	/**
	 * Constructor.
	 * 
	 * @param inconsistent_type the type that contains or constitutes an 
	 * inconsistency in a set of multiple files 
	 */
	public InconsistentTypeException(LinguisticType inconsistent_type) {
		this.inconsistent_type = inconsistent_type;
	}
	
	/**
	 * Returns the tier type which causes the inconsistency.
	 * 
	 * @return the tier type introducing an inconsistency
	 */
	public LinguisticType getInconsistentType() {
		return inconsistent_type;
	}

}
