package mpi.eudico.server.corpora.clomimpl.util;

import java.util.ArrayList;
import java.util.List;

import mpi.eudico.server.corpora.clom.ExternalReference;
import mpi.eudico.server.corpora.clomimpl.abstr.LicenseRecord;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.server.corpora.lexicon.LexiconLink;
import mpi.eudico.util.BasicControlledVocabulary;
import mpi.eudico.util.ControlledVocabulary;
import mpi.eudico.util.ExternalCV;
import mpi.eudico.util.multilangcv.LangInfo;

/**
 * A utility class to compare the structure of two transcriptions,
 * to detect and report differences and to extract items that are
 * there in one of the transcriptions but not in the other.
 * The intended use is in combination with the process of 
 * updating files based on new or updated information in a template file.
 * This class does not deal with annotations but only with the kind of
 * elements that can be found in a template. 
 * 
 * [This class does not store state; could remove the constructor and 
 * make all methods public static.]
 * 
 * @author Han Sloetjes
 *
 * July, 2018
 */
public class TranscriptionCompare {

	/**
	 * Constructor.
	 */
	public TranscriptionCompare() {
	}
	
	/**
	 * Returns a list of tier names only present in the first transcription.
	 * 
	 * @param tr1 first transcription
	 * @param tr2 second transcription
	 * 
	 * @return a non-null, but possibly empty, list with names of tiers that 
	 * are present in the first transcription but not in the second
	 */
	public List<String> getTierNamesOnlyInFirst(TranscriptionImpl tr1, TranscriptionImpl tr2) {
		List<String> uniqueNames = new ArrayList<String>();
		
		for (TierImpl t : tr1.getTiers()) {
			if (tr2.getTierWithId(t.getName()) == null) {
				uniqueNames.add(t.getName());
			}
		}
		
		return uniqueNames;
	}
	
	/**
	 * Returns a list of tier names present in both transcriptions.
	 * 
	 * @param tr1 first transcription
	 * @param tr2 second transcription
	 * 
	 * @return a non-null, but possibly empty, list with names of tiers that 
	 * are present in both transcriptions
	 */
	public List<String> getTierNamesInBoth(TranscriptionImpl tr1, TranscriptionImpl tr2) {
		List<String> sharedNames = new ArrayList<String>();
		
		for (TierImpl t : tr1.getTiers()) {
			if (tr2.getTierWithId(t.getName()) != null) {
				sharedNames.add(t.getName());
			}
		}
		
		return sharedNames;
	}
	
	/**
	 * Returns a list of tier names present in both transcriptions but with
	 * different tier type names.
	 * 
	 * @param tr1 first transcription
	 * @param tr2 second transcription
	 * 
	 * @return a non-null, but possibly empty, list with names of tiers that 
	 * are present in both transcriptions but with different tier type name
	 */
	public List<String> getTierNamesWithDifferentTypeNames(TranscriptionImpl tr1, TranscriptionImpl tr2) {
		List<String> diffTypeNames = new ArrayList<String>();
		
		for (TierImpl t : tr1.getTiers()) {
			TierImpl t2 = tr2.getTierWithId(t.getName());
			if (t2 != null) {
				LinguisticType lt1 = t.getLinguisticType();
				LinguisticType lt2 = t2.getLinguisticType();
				
				if (!lt1.getLinguisticTypeName().equals(lt2.getLinguisticTypeName())) {
					diffTypeNames.add(t.getName());
				}
			}
		}
		
		return diffTypeNames;
	}

	/**
	 * Returns a list of tier type names only present in the first transcription.
	 * 
	 * @param tr1 first transcription
	 * @param tr2 second transcription
	 * 
	 * @return a non-null, but possibly empty, list with names of tier types 
	 * that are present in the first transcription but not in the second
	 */
	public List<String> getTypeNamesOnlyInFirst(TranscriptionImpl tr1, TranscriptionImpl tr2) {
		List<String> uniqueNames = new ArrayList<String>();
		
		for (LinguisticType lt : tr1.getLinguisticTypes()) {
			if (tr2.getLinguisticTypeByName(lt.getLinguisticTypeName()) == null) {
				uniqueNames.add(lt.getLinguisticTypeName());
			}
		}
		
		return uniqueNames;
	}
	
	/**
	 * Returns a list of tier type names present in both transcriptions.
	 *  
	 * @param tr1 first transcription
	 * @param tr2 second transcription
	 * 
	 * @return a non-null, but possibly empty, list with names of tier types 
	 * that are present in both transcriptions
	 */
	public List<String> getTypeNamesInBoth(TranscriptionImpl tr1, TranscriptionImpl tr2) {
		List<String> sharedNames = new ArrayList<String>();
		
		for (LinguisticType lt : tr1.getLinguisticTypes()) {
			if (tr2.getLinguisticTypeByName(lt.getLinguisticTypeName()) != null) {
				sharedNames.add(lt.getLinguisticTypeName());
			}
		}
		
		return sharedNames;
	}
	
	/**
	 * Returns a list of tier type names present in both transcriptions but 
	 * with different constraints (stereotypes).
	 * 
	 * @param tr1 first transcription
	 * @param tr2 second transcription
	 * 
	 * @return a non-null, but possibly empty, list with names of tier types 
	 * that are present in both transcriptions but with different constraints
	 */
	public List<String> getTypeNamesWithDifferentConstraints(TranscriptionImpl tr1, TranscriptionImpl tr2) {
		List<String> diffConstraints = new ArrayList<String>();
		
		for (LinguisticType lt : tr1.getLinguisticTypes()) {
			LinguisticType lt2 = tr2.getLinguisticTypeByName(lt.getLinguisticTypeName());
			if (lt2 != null) {
				if (lt.hasConstraints() && lt2.hasConstraints()) {
					if (lt.getConstraints().getStereoType() != lt2.getConstraints().getStereoType()) {// is this a valid check?
						diffConstraints.add(lt.getLinguisticTypeName());
					}
				} else if (lt.hasConstraints() != lt2.hasConstraints()) {
					diffConstraints.add(lt.getLinguisticTypeName());
				}// else both types have no constraints 
			}
		}
		
		return diffConstraints;
	}
	
	/**
	 * Returns a list of CV names only present in the first transcription.
	 * 
	 * @param tr1 first transcription
	 * @param tr2 second transcription
	 * 
	 * @return a non-null, but possibly empty, list with names of controlled
	 * vocabularies that are present in the first transcription but not 
	 * in the second
	 */
	public List<String> getCVNamesOnlyInFirst(TranscriptionImpl tr1, TranscriptionImpl tr2) {
		List<String> uniqueNames = new ArrayList<String>();
		
		for (ControlledVocabulary cv : tr1.getControlledVocabularies()) {
			if (tr2.getControlledVocabulary(cv.getName()) == null) {
				uniqueNames.add(cv.getName());
			}
		}
		return uniqueNames;
	}
	
	/**
	 * Returns a list of CV names present in both transcriptions.
	 * 
	 * @param tr1 first transcription
	 * @param tr2 second transcription
	 * 
	 * @return a non-null, but possibly empty, list with names of controlled
	 * vocabularies that are present in both transcriptions
	 */
	public List<String> getCVNamesInBoth(TranscriptionImpl tr1, TranscriptionImpl tr2) {
		List<String> sharedNames = new ArrayList<String>();
		
		for (ControlledVocabulary cv : tr1.getControlledVocabularies()) {
			if (tr2.getControlledVocabulary(cv.getName()) != null) {
				sharedNames.add(cv.getName());
			}
		}
		
		return sharedNames;
	}
	
	/**
	 * Returns a list of content languages only present in the first
	 * transcription.
	 * 
	 * @param tr1 first transcription
	 * @param tr2 second transcription
	 * 
	 * @return a non-null, but possibly empty, list with names (short id's) of
	 * languages that are present in the first transcription but not 
	 * in the second
	 */
	public List<String> getLanguagesOnlyInFirst(TranscriptionImpl tr1, TranscriptionImpl tr2) {
		List<String> uniqueNames = new ArrayList<String>();
		
		List<String> firstList = new ArrayList<String>();
		for (TierImpl t1 : tr1.getTiers()) {
			String lref = t1.getLangRef();
			
			if (lref != null) {
				firstList.add(lref);
			}	
		}
		for (ControlledVocabulary cv1 : tr1.getControlledVocabularies()) {
			for (int i = 0; i < cv1.getNumberOfLanguages(); i++) {
				firstList.add(cv1.getLanguageId(i));
			}
		}
		
		if (firstList != null) {
			List<String> secList = new ArrayList<String>();
			for (TierImpl t : tr2.getTiers()) {
				String lref = t.getLangRef();
				if (lref != null) {
					secList.add(lref);
				}
			}
			for (ControlledVocabulary cv2 : tr2.getControlledVocabularies()) {
				for (int i = 0; i < cv2.getNumberOfLanguages(); i++) {
					secList.add(cv2.getLanguageId(i));
				}
			}
			for (String s : firstList) {
				if (!secList.contains(s) && !uniqueNames.contains(s)) {
					uniqueNames.add(s);
				}
			}
		}	
		
		return uniqueNames;
	}
	
	/**
	 * Returns a list of content languages present in both transcriptions.
	 * 
	 * @param tr1 first transcription
	 * @param tr2 second transcription
	 * 
	 * @return a non-null, but possibly empty, list with names (short id's) of
	 * languages that are present in both transcriptions
	 */
	public List<String> getLanguagesInBoth(TranscriptionImpl tr1, TranscriptionImpl tr2) {
		List<String> sharedNames = new ArrayList<String>();
		
		List<String> firstList = new ArrayList<String>();
		for (TierImpl t1 : tr1.getTiers()) {
			String lref = t1.getLangRef();
			
			if (lref != null) {
				firstList.add(lref);
			}	
		}
		for (ControlledVocabulary cv1 : tr1.getControlledVocabularies()) {
			for (int i = 0; i < cv1.getNumberOfLanguages(); i++) {
				firstList.add(cv1.getLanguageId(i));
			}
		}
		
		if (firstList != null) {
			List<String> secList = new ArrayList<String>();
			for (TierImpl t : tr2.getTiers()) {
				String lref = t.getLangRef();
				if (lref != null) {
					secList.add(lref);
				}
			}
			for (ControlledVocabulary cv2 : tr2.getControlledVocabularies()) {
				for (int i = 0; i < cv2.getNumberOfLanguages(); i++) {
					secList.add(cv2.getLanguageId(i));
				}
			}
			for (String s : firstList) {
				if (secList.contains(s) && !sharedNames.contains(s)) {
					sharedNames.add(s);
				}
			}
		}
		
		return sharedNames;
	}
	
	
// ###  ### 
	/*
	private List<String> addTo(List<String> inList, String s) {
		if (inList == null) {
			inList = new ArrayList<String>();
		}
		if (!inList.contains(s)) {
			inList.add(s);
		}
		
		return inList;
	}
	*/
// ### Tier level comparisons ###
	
	/**
	 * Returns whether two tiers have the same value for a specific property.
	 * <p>
	 * Note: this method does not perform <code>null</code> checks!
	 * 
	 * @param t1 the first tier, not {@code null}
	 * @param t2 the second tier, not {@code null}
	 * @param propGetter a ValueGetter for one of the tier properties, not
	 * {@code null}
	 * 
	 * @return {@code true} if the property handled by the ValueGetter is equal
	 * in both tiers, {@code false} otherwise
	 */
	public boolean sameTierProperty(TierImpl t1, TierImpl t2, TierImpl.ValueGetter propGetter) {
		return propGetter.getSortValue(t1).equals(propGetter.getSortValue(t2));
	}
	
	/**
	 * Returns whether two tiers have parent tiers with the same name.
	 * <p>
	 * Note: this method does not perform <code>null</code> checks!
	 * 
	 * @param t1 the first tier, not {@code null}
	 * @param t2 the second tier, not {@code null}
	 * 
	 * @return {@code true} if both tiers either don't have a parent tier or
	 * if the names of their parent tiers are equal 
	 */
	public boolean sameParentTierName(TierImpl t1, TierImpl t2) {
		if (!t1.hasParentTier() && !t2.hasParentTier()) {
			return true; // treat as same, "no parent" 
		}
		if (t1.hasParentTier() ^ t2.hasParentTier()) {
			// xor, only one has a parent
			return false;
		}
		// both have a parent
		return t1.getParentTier().getName().equals(t2.getParentTier().getName());
	}
	
// ### tier type comparisons ###
	/**
	 * Returns whether two tier types have the same value for a specific
	 * property.
	 * <p>
	 * Note: this method does not perform <code>null</code> checks!
	 * 
	 * @param lt1 the first type, not {@code null}
	 * @param lt2 the second type, not {@code null}
	 * @param propKey the property key specifying which property to compare, not
	 * {@code null}
	 * 
	 * @return {@code true} if the specified property is equal in both types, or is 
	 * absent in both types, {@code false} otherwise
	 */
	public boolean sameTypeProperty(LinguisticType lt1, LinguisticType lt2, LinguisticType.PropKey propKey) {
		switch (propKey) {
		case ID:
			// fall through, name is used as ID until introduction of a separate ID
		case NAME:
			return lt1.getLinguisticTypeName().equals(lt2.getLinguisticTypeName());
			
		case CONSTRAINT:
			return sameStereoType(lt1, lt2);
			
		case CV_NAME:
			if (lt1.isUsingControlledVocabulary() && lt2.isUsingControlledVocabulary()) {
				return lt1.getControlledVocabularyName().equals(lt2.getControlledVocabularyName());
			} 
			if (!lt1.isUsingControlledVocabulary() && !lt2.isUsingControlledVocabulary()) {
				return true;// "none"
			}
			return false;
			
		case DC:
			if (lt1.getDataCategory() == null && lt2.getDataCategory() == null) {
				return true;// "none"
			}
			if (lt1.getDataCategory() == null ^ lt2.getDataCategory() == null) {
				return false;
			}
			return lt1.getDataCategory().equals(lt2.getDataCategory());

		case LEX_BUNDLE:
			if (lt1.getLexiconQueryBundle() == null && lt2.getLexiconQueryBundle() == null) {
				return true;
			}
			if (lt1.getLexiconQueryBundle() == null ^ lt2.getLexiconQueryBundle() == null) {
				return false;
			}
			return lt1.getLexiconQueryBundle().equals(lt2.getLexiconQueryBundle());
			
		case LEX_FIELD:
			if (lt1.getLexiconQueryBundle() != null && lt2.getLexiconQueryBundle() != null) {
				if (lt1.getLexiconQueryBundle().getFldId() != null) {
					return lt1.getLexiconQueryBundle().getFldId().compareTo(lt2.getLexiconQueryBundle().getFldId()) == 0;
				} else if (lt2.getLexiconQueryBundle().getFldId() == null) {
					return true;// both no lexicon field
				}
			} else if (lt1.getLexiconQueryBundle() == null && lt2.getLexiconQueryBundle() == null) {
				return true;// both no lexicon field
			}
			return false;
		case LEX_LINK:
			if (lt1.getLexiconQueryBundle() != null && lt2.getLexiconQueryBundle() != null) {
				if (lt1.getLexiconQueryBundle().getLink() != null) {
					return lt1.getLexiconQueryBundle().getLink().compareNameAndLexId(
							lt2.getLexiconQueryBundle().getLink()) == 0;
				} else if (lt2.getLexiconQueryBundle().getLink() == null) {
					return true;// both no lexicon link
				}
			} else if (lt1.getLexiconQueryBundle() == null && lt2.getLexiconQueryBundle() == null) {
				return true;// both no lexicon link
			}
			return false;
		default:
			break;
		}
		
		return false;
	}
	
	/**
	 * Returns whether two tier types have the same {@code stereotype}.
	 * <p>
	 * Note: this method does not perform <code>null</code> checks!
	 * 
	 * @param lt1 the first tier type, not {@code null}
	 * @param lt2 the second tier type, not {@code null}
	 * 
	 * @return {@code true} if either both types have no constraints or if the 
	 * constraints have the same stereotype
	 */
	public boolean sameStereoType(LinguisticType lt1, LinguisticType lt2) {
		if (!lt1.hasConstraints() && !lt2.hasConstraints()) {
			return true;// stereotype "None"
		}
		if (lt1.hasConstraints() ^ lt2.hasConstraints()) {
			return false;
		}
		
		return lt1.getConstraints().getStereoType() == lt2.getConstraints().getStereoType();
	}
	
// ### controlled vocabulary comparison ###
	/**
	 * Returns whether two controlled vocabularies have the same value for a
	 * specific property.
	 * <p>
	 * Note: this method does not perform <code>null</code> checks!
	 * 
	 * @param cv1 the first controlled vocabulary, not {@code null}
	 * @param cv2 the second controlled vocabulary, not {@code null}
	 * @param propKey the property key specifying which property to compare,
	 * not {@code null} 
	 * 
	 * @return {@code true} if the property is equal in both CV's, 
	 * {@code false} otherwise  
	 */
	public boolean sameCVProperty(ControlledVocabulary cv1, ControlledVocabulary cv2, 
			BasicControlledVocabulary.PropKey propKey) {
		switch (propKey) {
		case NAME:
			return cv1.getName().equals(cv2.getName());
		case DESCRIPTION:
			for (int i = 0; i < cv1.getNumberOfLanguages(); i++) {
				String lid = cv1.getLanguageId(i);
				int index2 = cv2.getIndexOfLanguage(lid);
				if (cv1.getDescription(i) != null) {
					if (index2 < 0 || !cv1.getDescription(i).equals(cv2.getDescription(index2))) {
						return false;
					}
				} else {
					if (index2 > -1 && cv2.getDescription(index2) != null) {
						return false;
					}
				}
			}
			for (int i = 0; i < cv2.getNumberOfLanguages(); i++) {
				String lid = cv2.getLanguageId(i);
				int index1 = cv1.getIndexOfLanguage(lid);
				if (cv2.getDescription(i) != null) {
					if (index1 < 0 || !cv2.getDescription(i).equals(cv1.getDescription(index1))) {
						return false;
					}
				} else {
					if (index1 > -1 && cv1.getDescription(index1) != null) {
						return false;
					}
				}
			}
				
			return true;
		case NUM_ENTRIES:
			return cv1.size() == cv2.size();
		case NUM_LANGUAGES:
			return cv1.getNumberOfLanguages() == cv2.getNumberOfLanguages();
		case EXTERNAL_REF:
			if (cv1 instanceof ExternalCV) {
				if (cv2 instanceof ExternalCV) {
					ExternalReference er1 = ((ExternalCV) cv1).getExternalRef();
					ExternalReference er2 = ((ExternalCV) cv2).getExternalRef();
					
					return er1.getReferenceType() == er2.getReferenceType() && 
							er1.getValue().equals(er2.getValue());
				} else {
					return false;
				}
			} else {
				return !(cv2 instanceof ExternalCV);
			}
		}
		
		return false;
	}
	
// ### external references ###
	/**
	 * Returns whether two external references are of the same type and have
	 * the same value.
	 * <p>
	 * Note: this method does not perform <code>null</code> checks!
	 * 
	 * @param er1 the first external reference, not {@code null}
	 * @param er2 the second external reference, not {@code null}
	 * 
	 * @return {@code true} if both references are of the same type and their
	 * value is equal, {@code false} otherwise 
	 */
	public boolean sameExternalReference(ExternalReference er1, ExternalReference er2) {
		return er1.getReferenceType() == er2.getReferenceType() && 
				er1.getValue().equals(er2.getValue());
	}
	
// ### languages comparison ###
	
	/**
	 * Returns whether two content languages have the same short id.
	 * <p>
	 * Note: this method does not perform <code>null</code> checks!
	 * 
	 * @param li1 the first language info object, not {@code null}
	 * @param li2 the second language info object, not {@code null}
	 * 
	 * @return {@code true} if the (short) id's of both languages are equal
	 */
	public boolean sameLanguage(LangInfo li1, LangInfo li2) {
		return li1.getId().equals(li2.getId());
	}
	
// ### lexicon links comparison ###
	/**
	 * Returns whether two lexicon links have the same name and the same id.
	 * <p>
	 * Note: this method does not perform <code>null</code> checks!
	 * 
	 * @param ll1 the first lexicon link, not {@code null}
	 * @param ll2 the second lexicon link, not {@code null}
	 * 
	 * @return {@code true} if the name and lexicon id of both links are equal,
	 * {@code false} otherwise
	 */
	public boolean sameLexiconLink(LexiconLink ll1, LexiconLink ll2) {
		return ll1.compareNameAndLexId(ll2) == 0;
	}
	
	/**
	 * Returns whether two lexicon links have the same value for a specific
	 * property.
	 * <p>
	 * Note: this method does not perform <code>null</code> checks!
	 * 
	 * @param ll1 the first lexicon link, not {@code null}
	 * @param ll2 the second lexicon link, not {@code null}
	 * @param propKey the property key specifying which property to compare,
	 * not {@code null} 
	 * 
	 * @return {@code true} if the requested property is equal in both links or 
	 * absent/empty in both links, {@code false} otherwise 
	 */
	public boolean sameLexiconLinkProperty(LexiconLink ll1, LexiconLink ll2, LexiconLink.PropKey propKey) {
		switch (propKey) {
		case NAME:
			return ll1.getName().equals(ll2.getName());
		case LEXICON_CLIENT_TYPE:
			if (ll1.getLexSrvcClntType() != null) {
				return ll1.getLexSrvcClntType().equals(ll2.getLexSrvcClntType());
			} else {
				return ll2.getLexSrvcClntType() == null;
			}
		case LEXICON_IDENTIFICATION:
			if (ll1.getLexId() != null && ll2.getLexId() != null) {
				if (ll1.getName() != null && ll2.getName() != null) {
					return ll1.getLexId().getName().equals(ll2.getLexId().getName());
				} else if (ll1.getLexId().getId() != null) {
					return ll1.getLexId().getId().equals(ll2.getLexId().getId());
				}
			} else if (ll1.getLexId() == null && ll2.getLexId() == null) {
				return true;
			}
			break;
		case URL:
			if (ll1.getUrl() != null && ll1.getUrl().equals(ll2.getUrl())) {
				return true;
			}
			// the following should not happen
			if (ll1.getUrl() == null && ll2.getUrl() == null) {
				return true;
			}
		}
		
		return false;
	}
	
// ### licenses ###
	/**
	 * Returns whether two license records have the same URL and the same value
	 * (text).
	 * <p>
	 * Note: this method does not perform <code>null</code> checks!
	 * 
	 * @param lr1 the first license record, not {@code null}
	 * @param lr2 the second license record, not {@code null}
	 * 
	 * @return {@code true} if the URL's of the licenses are either both
	 * {@code null} or equal and the text of the licenses is either 
	 * {@code null} or equal, {@code false} otherwise 
	 */
	public boolean sameLicense(LicenseRecord lr1, LicenseRecord lr2) {
		// the text is usually not null but "", so the following should in principle be safe too
//		if (lr1.getUrl() != null && lr1.getUrl().equals(lr2.getUrl())) {
//			return lr1.getText().equals(lr2.getText());
//		} else if (lr1.getUrl() == null && lr2.getUrl() == null) {
//			return lr1.getText().equals(lr2.getText());
//		}
		
		boolean urlEq = (lr1.getUrl() == null && lr2.getUrl() == null) || 
				(lr1.getUrl() != null && lr1.getUrl().equals(lr2.getUrl()));
		boolean txtEq = (lr1.getText() == null && lr2.getText() == null) ||
				(lr1.getText() != null && lr1.getText().equals(lr2.getText()));
		
		return urlEq && txtEq;
	}
}
