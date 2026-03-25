/*
 * Created on Jun 15, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package mpi.eudico.server.corpora.clomimpl.dobes;

import mpi.eudico.server.corpora.clom.Annotation;

/**
 * A record to store most of the properties of an {@link Annotation}
 * object, most of them as a {@code String}.
 * 
 * @author hennie
 */
public class AnnotationRecord {
	/** constant for the alignable annotation type */
	public static final String ALIGNABLE = "alignable";
	/** constant for the reference annotation type */
	public static final String REFERENCE = "reference";
	
	private String annotId;
	private String annotType;
	private String beginTimeSlotId;
	private String endTimeSlotId;
	private String referredAnnotId;
	private String previousAnnotId;
	private String annotValue;
	private String extRefId;
	private String cvEntryId;
	
	private TimeSlotRecord beginTimeSlotRecord;
	private TimeSlotRecord endTimeSlotRecord;
	
	
	/**
	 * Creates a new annotation record with all fields set to {@code null}.
	 */
	public AnnotationRecord() {
		super();
	}

	/**
	 * Returns the Id of the annotation.
	 * 
	 * @return the annotation ID
	 */
	public String getAnnotationId() {
		return annotId;
	}
	
	/**
	 * Sets the ID of the annotation.
	 * 
	 * @param annotId the annotation ID
	 */
	public void setAnnotationId(String annotId) {
		this.annotId = annotId;
	}
	
	/**
	 * Returns the type of the annotation.
	 * 
	 * @return the annotation type
	 */
	public String getAnnotationType() {
		return annotType;
	}
	
	/**
	 * Sets the type of the annotation.
	 * 
	 * @param annotType the annotation type
	 */
	public void setAnnotationType(String annotType) {
		this.annotType = annotType;
	}
	
	/**
	 * Returns the ID of the begin time slot.
	 * 
	 * @return the ID of the begin time slot record prefixed with "ts" if
	 * the record is not {@code null}, otherwise this record's begin slot id
	 */
	public String getBeginTimeSlotId() {
		if (beginTimeSlotRecord != null) {
			return "ts" + beginTimeSlotRecord.getId();
		}
		return beginTimeSlotId;
	}
	
	/**
	 * Sets the begin time slot ID.
	 * 
	 * @param beginTSId the ID of the begin time slot
	 */
	public void setBeginTimeSlotId(String beginTSId) {
		beginTimeSlotId = beginTSId;
	}
	
	/**
	 * Returns the ID of the end time slot.
	 * 
	 * @return the ID of the end time slot record prefixed with "ts" if
	 * the record is not {@code null}, otherwise this record's end slot id
	 */
	public String getEndTimeSlotId() {
		if (endTimeSlotRecord != null) {
			return "ts" + endTimeSlotRecord.getId();
		}
		return endTimeSlotId;
	}
	
	/**
	 * Sets the end time slot ID.
	 * 
	 * @param endTSId the ID of the end time slot
	 */
	public void setEndTimeSlotId(String endTSId) {
		endTimeSlotId = endTSId;
	}

	/**
	 * Returns the ID of a referred annotation.
	 * 
	 * @return the referred annotation id
	 */
	public String getReferredAnnotId() {
		return referredAnnotId;
	}
	
	/**
	 * Sets the referred annotation ID.
	 * 
	 * @param refAnnotId the referred annotation id
	 */
	public void setReferredAnnotId(String refAnnotId) {
		referredAnnotId = refAnnotId;
	}
	
	/**
	 * Returns the ID of the previous annotation (on a subdivision tier).
	 * 
	 * @return the id of the previous annotation
	 */
	public String getPreviousAnnotId() {
		return previousAnnotId;
	}
	
	/**
	 * Sets the ID of the previous annotation (on a subdivision tier).
	 * 
	 * @param previousAnnotId the id of the previous annotation
	 */
	public void setPreviousAnnotId(String previousAnnotId) {
		this.previousAnnotId = previousAnnotId;
	}
	
	/**
	 * Returns the id of an external reference object
	 * 
	 * @return the extRefId the id of an external reference, e.g. a concept defined in ISO DCR
	 */
	public String getExtRefId() {
		return extRefId;
	}

	/**
	 * Sets the external reference id.
	 * 
	 * @param extRefId the extRefId to set
	 */
	public void setExtRefId(String extRefId) {
		this.extRefId = extRefId;
	}
	
	/**
	 * Returns the ID reference to a controlled vocabulary entry.
	 * 
	 * @return the CV entry id
	 */
	public String getCvEntryId() {
		return cvEntryId;
	}

	/**
	 * Sets the ID reference to an entry in a controlled vocabulaty.
	 * 
	 * @param cvEntryId the CV entry id to set
	 */
	public void setCvEntryId(String cvEntryId) {
		this.cvEntryId = cvEntryId;
	}
	
	/**
	 * Returns the textual contents or value of the annotation.
	 * 
	 * @return the annotation value
	 */
	public String getValue() {
		return annotValue;
	}
	
	/**
	 * Sets the textual contents or value of the annotation.
	 * 
	 * @param annotValue the annotation value
	 */
	public void setValue(String annotValue) {
		this.annotValue = annotValue;
	}
	
	@Override
	public String toString() {
		String result = "";
		
		result += "id:        " + annotId + "\n";
		result += "type:      " + annotType + "\n";
		result += "begin id:  " + beginTimeSlotId + "\n";
		result += "end id:    " + endTimeSlotId + "\n";
		result += "ref'ed id: " + referredAnnotId + "\n";
		result += "prev id:   " + previousAnnotId + "\n";
		result += "extref id: " + extRefId + "\n";
		result += "cventry id: " + cvEntryId + "\n";
		result += "value:     " + annotValue + "\n";
		
		return result;
	}

	/**
	 * Returns the record of the begin time slot.
	 * 
	 * @return the begin time slot record
	 */
	public TimeSlotRecord getBeginTimeSlotRecord() {
		return beginTimeSlotRecord;
	}

	/**
	 * Sets the begin time slot record.
	 * 
	 * @param beginTimeSlotRecord the begin time slot record to set
	 */
	public void setBeginTimeSlotRecord(TimeSlotRecord beginTimeSlotRecord) {
		this.beginTimeSlotRecord = beginTimeSlotRecord;
	}

	/**
	 * Returns the record of the end time slot.
	 * 
	 * @return the end time slot record
	 */
	public TimeSlotRecord getEndTimeSlotRecord() {
		return endTimeSlotRecord;
	}

	/**
	 * Sets the end time slot record.
	 * 
	 * @param endTimeSlotRecord the end time slot record to set
	 */
	public void setEndTimeSlotRecord(TimeSlotRecord endTimeSlotRecord) {
		this.endTimeSlotRecord = endTimeSlotRecord;
	}

}
