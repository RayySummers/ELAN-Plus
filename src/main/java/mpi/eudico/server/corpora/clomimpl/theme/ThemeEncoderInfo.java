package mpi.eudico.server.corpora.clomimpl.theme;

import mpi.eudico.server.corpora.clom.EncoderInfo;

/**
 * Stores properties for customization of export to Theme format.
 *  
 * @author Han Sloetjes
 */
public class ThemeEncoderInfo implements EncoderInfo {

	private boolean tierNameAsActor = false;
	private boolean useCVforVVT = true;
	private String encoding;
	
	/**
	 * Creates a new encoder info instance.
	 */
	public ThemeEncoderInfo() {
		super();
	}

	/**
	 * If true each tier name will be exported as a Theme Actor, otherwise 
	 * the participant attribute will be used as Actor and the tier name part of the event value. 
	 * 
	 * @return the flag that determines whether the tier name is Actor or the participant attribute
	 */
	public boolean isTierNameAsActor() {
		return tierNameAsActor;
	}

	/**
	 * Sets whether the tier name or participant attribute is Actor.
	 *  
	 * @param tierNameAsActor if {@code true} the tier name is the Theme Actor,
	 * otherwise the participant attribute is the Actor
	 */
	public void setTierNameAsActor(boolean tierNameAsActor) {
		this.tierNameAsActor = tierNameAsActor;
	}

	/**
	 * Returns whether the controlled vocabulary is used to export to the
	 * {@code .vvt} file.
	 * 
	 * @return {@code true} if the controlled vocabulary is used for export to
	 * the {@code vvt.vvt} file, {@code false} if all values of a tier are
	 * added to the {@code vvt.vvt}
	 */
	public boolean isUseCVforVVT() {
		return useCVforVVT;
	}

	/**
	 * Sets whether the CV should be used for the export to the {@code .vvt}
	 * file.
	 * 
	 * @param useCVforVVT if {@code true} the CV will be used for the export
	 * to the {@code vvt.vvt} file
	 */
	public void setUseCVforVVT(boolean useCVforVVT) {
		this.useCVforVVT = useCVforVVT;
	}

	/**
	 * Returns the file encoding use for the export.
	 * 
	 * @return the file encoding
	 */
	public String getEncoding() {
		return encoding;
	}

	/**
	 * Sets the file encoding for the export.
	 * 
	 * @param encoding the encoding to use
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
}
