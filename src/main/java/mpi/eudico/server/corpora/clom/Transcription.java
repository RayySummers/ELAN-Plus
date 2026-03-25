package mpi.eudico.server.corpora.clom;

import java.net.URI;
import java.util.List;
import java.util.Map;

import mpi.eudico.server.corpora.clomimpl.abstr.LicenseRecord;
import mpi.eudico.server.corpora.clomimpl.abstr.LinkedFileDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.reflink.RefLinkSet;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.server.corpora.lexicon.LexiconLink;
import mpi.eudico.server.corpora.lexicon.LexiconQueryBundle2;
import mpi.eudico.server.corpora.util.ACMEditableDocument;
import mpi.eudico.server.corpora.util.ACMEditableObject;
import mpi.eudico.util.ControlledVocabulary;

/**
 * Transcription encapsulates the notion of an annotation document. 
 *
 * @author Hennie Brugman
 * @author Albert Russel
 * @version 5-Nov-1998
 * @version Aug 2005 Identity removed
 * @version Dec 2012 TreeViewable, UnsharedInfoObject, SharedDataObject removed
 */
public interface Transcription extends ACMEditableObject, ACMEditableDocument {
	/** Constant for default time change processing mode. */
	public static int NORMAL = 0;
	/** Constant for the time change processing mode in which adjacent 
	 * annotations are pushed forward instead of being overwritten in case an
	 * annotation's alignment is changed. Empty spaces are filled before 
	 * adjacent annotations are moved. */
	public static int BULLDOZER = 1;
	/** Constant for the time change processing mode in which adjacent 
	 * annotations are shifted with the number of milliseconds corresponding to
	 * the change of the source annotation. */
	public static int SHIFT = 2;

	/**
	 * Gives the Transcription name.
	 *
	 * @return	the name
	 */
	public String getName();

	/**
	 * Sets the name of the transcription.
	 * 
	 * @param theName the name of the transcription
	 */
	public void setName(String theName);
	
	/**
	 * Returns the full path as a url.
	 * <br>
	 * Version Dec 2012: this method used to be part of the LanguageResource interface
	 * @return the full path (url)
	 */
	public String getFullPath();
	

	/**
	 * Returns the list of Tiers that are accessible.
	 *
	 * @return	the list of Tiers
	 */
	public List<? extends Tier> getTiers();


	/**
	 * Adds a Tier to the Transcription.
	 *
	 * @param theTier the Tier to be added
	 */
	public void addTier(Tier theTier);


	/**
	 * Removes a Tier from the Transcription.
	 *
	 * @param theTier the Tier to be removed
	 */
	public void removeTier(Tier theTier);


	/**
	 * Returns all TimeSlots, ordered in a TimeOrder object.
	 *
	 *@return the {@code TimeOrder} instance managing the time slots
	 */
	public TimeOrder getTimeOrder();
	
	/**
	 * Returns a list of annotations with a begin or end time reference 
	 * to the specified slot.
	 * 
	 * @param theSlot the {@code TimeSlot} to find annotations for
	 * @return a list of annotations with a begin or end time reference 
	 * to the slot 
	 */
	public List<Annotation> getAnnotationsUsingTimeSlot(TimeSlot theSlot);
	
	/**
	 * Sets the author property.
	 * 
	 * @param theAuthor the author or owner of the document
	 */
	public void setAuthor(String theAuthor);

	/**
	 * Returns the author property.
	 * 
	 * @return the author or null
	 */
	public String getAuthor() ;

	// Linguistic Types
	/**
	 * Sets the list of tier types available in this transcription, mainly used
	 * at load time of the transcription.
	 * 
	 * @param theTypes the tier types to use
	 */
	public void setLinguisticTypes(List<LinguisticType> theTypes);
	
	/**
	 * Returns the list of registered tier types.
	 * 
	 * @return the list of tier types available in this transcription
	 */
	public List<LinguisticType> getLinguisticTypes();
	
	/**
	 * Adds a single new type to the transcription, mainly used after the initial
	 * load phase, e.g. after a user action.
	 *   
	 * @param theType the new type to add
	 */
	public void addLinguisticType(LinguisticType theType);
	
	/**
	 * When a tier type has to be removed first all tiers referencing it need
	 * to be removed or changed to another type.

	 * @param theType the type to remove
	 */
	public void removeLinguisticType(LinguisticType theType);
	
	/**
	 * Changes an existing tier type (linguistic type). The type needs to be
	 * already in the list of the transcription. The transcription can perform
	 * checks, especially when the "stereotype" of the constraint is different
	 * from the current constraint. 
	 * Note: this and other methods could throw an exception if requirements are
	 * not met 
	 *   
	 * @param linType the type to change
 	 * @param newTypeName the new name of the type
	 * @param constraints the new constraint for the type
	 * @param newControlledVocabularyName the new controlled vocabulary name
	 * @param newTimeAlignable the new time alignable flag, should be consistent
	 * with the constraint
	 * @param dataCategoryId the data category the type is linked to
	 * @param queryBundle a lexicon bundle with information about a link to a
	 * lexicon and lexicon field
	 */
	public void changeLinguisticType(LinguisticType linType,
			String newTypeName,
			List<Constraint> constraints,
			String newControlledVocabularyName,
			boolean newTimeAlignable,
			String dataCategoryId,
			LexiconQueryBundle2 queryBundle);

	/**
	 * Returns the tier type with the specified name.
	 * 
	 * @param name the identifier of the type to get
	 * @return the linguistic (tier) type or {@code null} 
	 */
	public LinguisticType getLinguisticTypeByName(String name);
	
	/**
	 * Returns the tier types that are linked to the controlled vocabulary
	 * with the specified name.
	 * 
	 * @param name the identifier of the controlled vocabulary 
	 * @return a list of types that are linked to the specified controlled
	 * vocabulary
	 */
	public List<LinguisticType> getLinguisticTypesWithCV(String name);
	
	/**
	 * Returns the tiers based on the type with the specified name.
	 * 
	 * @param typeID the identifier of the linguistic (tier) type
	 * @return a list of tiers based on the specified type
	 */
	public List<? extends Tier> getTiersWithLinguisticType(String typeID);
	
	
	/**
	 * Returns the tiers based on the specified participant name.
	 * @param name the participant name
	 * @return a list of tiers based on the specified participant name
	 */
	public List<? extends Tier> getTiersWithParticipant(String name);

	// lexicon-link related methods
	/** 
	 * Adds a lexicon link to the list of lexicon links.
	 * 
	 * @param link the {@code LexiconLink} instance to add
	 */
	public void addLexiconLink(LexiconLink link);
	
	/**
	 * Returns a map of link id's to lexicon links.
	 * 
	 * @return a map of all connected lexicon links, with the name/identifier
	 * as key and the lexicon link object as value
	 */
	public Map<String, LexiconLink> getLexiconLinks();
	
	/**
	 * Returns the lexicon link with specified name.
	 * 
	 * @param linkName the identifier of the link to get
	 * @return the lexicon link or {@code null}
	 */
	public LexiconLink getLexiconLink(String linkName);
	
	/**
	 * Removes the specified lexicon link.
	 * 
	 * @param link the identifier of the link to remove
	 */
	public void removeLexiconLink(LexiconLink link);

	/**
	 * Returns a list of descriptors of the media files linked to this
	 * transcription.
	 * 
	 * @return the list of media descriptors linked to this transcription
	 */
	public List<MediaDescriptor> getMediaDescriptors();
	
	/**
	 * Sets the list of media descriptors, usually called at load time
	 * 
	 * @param theMediaDescriptors the list of media descriptors
	 */
	public void setMediaDescriptors(List<MediaDescriptor> theMediaDescriptors);
	
	/**
	 * Returns the collection of linked file descriptors.
	 * 
	 * @return the linked file descriptors
	 */
	public List<LinkedFileDescriptor> getLinkedFileDescriptors();
	
	/**
	 * Sets the collection of linked files descriptors.
	 * 
	 * @param descriptors the new descriptors
	 */
	public void setLinkedFileDescriptors(List<LinkedFileDescriptor> descriptors);

	/**
	 * <p>MK:02/06/12<br>
	 * The ID of a tier has yet to be defined.
	 * Tiers so far have only names given by the user,
	 * which have been used in the EAF XML file format as XML IDs.
	 * Because tier name are used as IDs, no two tiers can have the same name.
	 * Tiers should/could get a proper ID.
	 * 
	 * @param theTierId currently the name of the tier. Has to be changed!
	 * @return Tier with given name, or null
	 * */
	public Tier getTierWithId(String theTierId);

	/**
	 * Returns a List containing all annotations (as ids!) covering the 
	 * specified time.
	 * 
	 * @param time the time to get annotations for
	 * @return a list of annotation id's
	 */
	public List<String> getAnnotationIdsAtTime(long time);
	
	/**
	 * Returns the annotation with the corresponding id or null.
	 * 
	 * @param id the id to look for
	 * @return the {@code Annotation} or {@code null}
	 */
	public Annotation getAnnotationById(String id);

	/**
	 * Returns the highest end time of all annotations in the transcription.
	 * 
	 * @return the highest end time value of all annotations in the 
	 * transcription 
	 */
	public long getLatestTime();
	
	/**
	 * Returns the {@code changed state} of the transcription.
	 * 
	 * @return whether any modifications are made since the last reset (when saving)
	 */
	public boolean isChanged();

	/**
	 * Resets the {@code changed state} to unchanged.
	 */
	public void setUnchanged();

	/**
	 * Sets the {@code changed state} to changed.
	 */
	public void setChanged();
	
	/**
	 * Returns the current {@code Time Change Propagation Mode} (normal, 
	 * bulldozer or shift).
	 * <br>
	 * Added by:  hennie
	 * @return one of the constants {@link #NORMAL}, {@link Transcription#BULLDOZER}
	 * or {@link #SHIFT}
	 */
	public int getTimeChangePropagationMode();
	
	/**
	 * Sets the {@code Time Change Propagation Mode} (normal, bulldozer or 
	 * shift).
	 * <br>
	 * Added by:  hennie
	 * @param theMode the new time change propagation mode, one of the constants
	 * @see #getTimeChangePropagationMode()
	 */
	public void setTimeChangePropagationMode(int theMode);
	
	/**
	 * Each Transcription has a unique/universal resource name which is used to
	 * uniquely identify this transcription and its previous and future versions of it.
	 * 
	 * @return The Universal Resource Name.
	 */
	public URI getURN();
	
	/**
	 * The license(s) that apply to the Transcription.
	 * Must not return {@code null} but the List may be empty.
	 * 
	 * @return a list with license records
	 */
	public List<LicenseRecord> getLicenses();
	 
	/** 
	 * Sets the list of licenses, mostly used at load time.
	 * 
	 * @param licenses the new list of licenses
	 */
	public void setLicenses(List<LicenseRecord> licenses);

	/** 
	 * Returns a list of dependent (or child) annotations of the specified
	 * annotation. These annotations are {@code ParentAnnotationListener}s and
	 * can be part of different tiers. 
	 *  
	 * @param theAnnot the annotation to get the dependent annotations of
	 * @return the list of child annotations (parent listeners)
	 */
	public List<? extends Annotation> getChildAnnotationsOf(Annotation theAnnot);

	// Controlled Vocabularies
	/**
	 * Sets the list of controlled vocabularies, usually called at load time.
	 * 
	 * @param controlledVocabs the new controlled vocabularies
	 */
	public void setControlledVocabularies(List<ControlledVocabulary> controlledVocabs);
	
	/**
	 * Returns a list of registered controlled vocabularies.
	 * 
	 * @return the list of controlled vocabularies
	 */
	public List<ControlledVocabulary> getControlledVocabularies();
	
	/**
	 * Returns the {@code ControlledVocabulary} with the specified name.
	 * 
	 * @param name the name or identifier of the controlled vocabulary
	 * @return the {@code ControlledVocabulary} or {@code null}
	 */
	public ControlledVocabulary getControlledVocabulary(String name);

	// Properties
	/**
	 * Adds the {@code Property}'s of the list to the document properties.
	 *  
	 * @param props the collection of document properties to add
	 */
	public void addDocProperties(List<Property> props);
	
	/**
	 * Adds the {@code Property} to the document properties.
	 * 
	 * @param prop a single document property to add
	 */
	public void addDocProperty(Property prop);
	
	/**
	 * Removes the specified {@code Property} from the document properties.
	 * 
	 * @param prop the document property to remove
	 * @return true if the property was there and was removed
	 */
	public boolean removeDocProperty(Property prop);
	
	/**
	 * Returns the list of document properties.
	 * 
	 * @return the list of current properties
	 */
	public List<? extends Property> getDocProperties();

	// reference links sets
	/**
	 * Adds a set of reference links.
	 * 
	 * @param refLinkSet the reference link set to add
	 */
	public void addRefLinkSet(RefLinkSet refLinkSet);
	
	/**
	 * Removes a set of reference links.
	 * 
	 * @param refLinkSet the set to remove
	 */
	public void removeRefLinkSet(RefLinkSet refLinkSet);
	
	/**
	 * Returns the set of reference links with the specified name.
	 * 
	 * @param name the (friendly) name of the set
	 * @return the set of that name or null
	 */
	public RefLinkSet getRefLinkSetByName(String name);
	
	/**
	 * Returns the set of reference links with the specified id.
	 *  
	 * @param id the (generated) id of the set
	 * @return the set with that id or null
	 */
	public RefLinkSet getRefLinkSetById(String id);
	
	/**
	 * Returns a list of all sets of reference links.
	 * 
	 * @return all sets of reference links
	 */
	public List<RefLinkSet> getRefLinkSets();
}
