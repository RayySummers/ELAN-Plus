package mpi.eudico.webserviceclient.weblicht;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.SAXException;

import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.server.corpora.clomimpl.type.SymbolicAssociation;
import mpi.eudico.server.corpora.clomimpl.type.SymbolicSubdivision;
import static mpi.eudico.webserviceclient.weblicht.TCFConstants.*;

/**
 * A class that creates a transcription from the contents of a TCF string.
 * 
 * @version Apr 2022: Added methods to add tiers and annotations to an existing
 * transcription. This supports iterative processing of input annotations and
 * conversion of the returned TCF to new annotations.
 * 
 * @author Han Sloetjes
 */
public class TCFtoTranscription {
	private TranscriptionImpl transcription;
	private TCFParser parser;
	
	private long defDuration = 5000;
	// by default everything is converted to tiers and annotations
	private boolean includeTokens = true;
	private boolean includePOSTags = true;
	private boolean includeLemmas = true;

	/**
	 * Constructor
	 */
	public TCFtoTranscription() {
		super();
	}
	
	/**
	 * Sets the default duration per annotation.
	 * 
	 * @param duration the default duration;
	 */
	public void setDefaultDuration(long duration) {
		defDuration = duration;
	}
	
	/**
	 * Configures which tiers to include.
	 * 
	 * @param includeTokens the tokens tier
	 * @param includePOSTags the POS tag tier
	 * @param includeLemmas the lemmas tier
	 */
	public void setTiersToInclude(boolean includeTokens, boolean includePOSTags, boolean includeLemmas) {
		this.includeTokens = includeTokens;
		this. includePOSTags = includePOSTags;
		this.includeLemmas = includeLemmas;
	}
	
	/**
	 * Creates a transcription based on the TCF input text.
	 * 
	 * @param tcbString the TCF XML returned by a WebLicht service
	 * @return the created transcription
	 * 
	 * @throws SAXException any SAX parser exception
	 * @throws IOException any IO exception
	 */
	public TranscriptionImpl createTranscription(String tcbString) throws SAXException, IOException {
		if (tcbString != null) {
			//System.out.println(tcbString);
			parser = new TCFParser(tcbString);
			parser.parse();
			
			transcription = new TranscriptionImpl();
			transcription.setNotifying(false);
			// create default tiers
			createTiers();
			// check sentences, tokens, tags
			createAnnotations();
			
			return transcription;
		} else {
			throw new IOException("The TCF input is null.");
		}
	}
	
	/**
	 * Create a default set of tiers.
	 */
	private void createTiers() {
		// sentence
		LinguisticType senType = new LinguisticType(SENT);
		senType.setTimeAlignable(true);// should not be settable
		transcription.addLinguisticType(senType);
		TierImpl senTier = new TierImpl(SENT, null, transcription, senType);
		transcription.addTier(senTier);
		
		if (includeTokens) {
			LinguisticType tokType = new LinguisticType(TOKEN);
			tokType.addConstraint(new SymbolicSubdivision());
			tokType.setTimeAlignable(false);// 
			transcription.addLinguisticType(tokType);
			TierImpl tokTier = new TierImpl(senTier, TOKEN, null, transcription, tokType);
			transcription.addTier(tokTier);
			
			if (includePOSTags) {
				LinguisticType posType = new LinguisticType(POSTAGS);
				posType.addConstraint(new SymbolicAssociation());
				posType.setTimeAlignable(false);
				transcription.addLinguisticType(posType);
				TierImpl posTagTier = new TierImpl(tokTier, POSTAGS, null, transcription, posType);
				transcription.addTier(posTagTier);
			}
			
			if (includeLemmas) {
				LinguisticType lemType = new LinguisticType(LEMMA);
				lemType.addConstraint(new SymbolicAssociation());
				lemType.setTimeAlignable(false);
				transcription.addLinguisticType(lemType);
				TierImpl lemTagTier = new TierImpl(tokTier, LEMMA, null, transcription, lemType);
				transcription.addTier(lemTagTier);
			}
		}
		
	}
	
	/**
	 * Creates annotations based on the elements extracted by the parser.
	 * Called after the creation of a transcription. The parser should not be null either.
	 */
	private void createAnnotations() {
		if (parser == null || transcription == null) {
			return;
		}
		
		final String SPACE = " ";
		List<TCFElement> sentences = parser.getElementsByType(TCFType.SENTENCE);
		long t = 0;
		long dur = defDuration;
		int numProcessedTokens = 0;
		if (sentences != null) {
			for (TCFElement senEl : sentences) {
				if (senEl.getIdRefs() != null) {
					//System.out.println(senEl.getIdRefs());
					String[] toks = senEl.getIdRefs().split(SPACE);
					List<TCFElement> tokens = getTokensWithIDs(toks);
					
					if (tokens != null && tokens.size() > 0) {
						// make a sentence annotation and dependent token/word annotations
						TierImpl sentTier = (TierImpl) transcription.getTierWithId(SENT);
						if (sentTier == null) {
							return;// error message
						}
						AbstractAnnotation sentAnn = (AbstractAnnotation) sentTier.createAnnotation(t, t + dur);
						if (sentAnn == null) {
							return;// message
						}
						StringBuilder sentBuilder = new StringBuilder();
						TCFElement tokEl;
						AbstractAnnotation curTokAnn = null;
						for (int i = 0; i < tokens.size(); i++) {
							tokEl = tokens.get(i);
							if (i != 0 && tokEl.getText().length() > 0 && 
									Character.getType(tokEl.getText().charAt(0)) != Character.END_PUNCTUATION) {
								sentBuilder.append(SPACE);
							}
							sentBuilder.append(tokEl.getText());
							AbstractAnnotation nextAnnotation = null;
							
							if (includeTokens && transcription.getTierWithId(TOKEN) != null) {
								if (curTokAnn == null) {
									nextAnnotation = (AbstractAnnotation) ((TierImpl) transcription.getTierWithId(TOKEN)).createAnnotation(
										t + dur / 2, t + dur / 2);
								} else {
									nextAnnotation = (AbstractAnnotation) ((TierImpl) transcription.getTierWithId(TOKEN)).createAnnotationAfter(curTokAnn);
								}
							}
							
							if (nextAnnotation != null) {
								if (tokEl.getText() != null) {
									nextAnnotation.setValue(tokEl.getText());
								}
								curTokAnn = nextAnnotation;
								long mid = (nextAnnotation.getBeginTimeBoundary() + nextAnnotation.getEndTimeBoundary()) / 2;
								// immediately create depending lemma / pos annotations?
								if (includePOSTags && transcription.getTierWithId(POSTAGS) != null) {
									TCFElement posEl = getDependentsForToken(tokEl.getId(), TCFType.TAG);
									if (posEl != null) {
										// Note the created tier is POSTags not Tags
										AbstractAnnotation posAnn = (AbstractAnnotation) ((TierImpl) transcription.getTierWithId(POSTAGS)).createAnnotation(
												 mid, mid);
										if (posAnn != null && posEl.getText() != null) {
											posAnn.setValue(posEl.getText());
										} // else {} log
									}
								}
								
								if (includeLemmas && transcription.getTierWithId(LEMMA) != null) {
									TCFElement lemEl = getDependentsForToken(tokEl.getId(), TCFType.LEMMA);
									if (lemEl != null) {
										AbstractAnnotation lemAnn = (AbstractAnnotation) ((TierImpl) transcription.getTierWithId(LEMMA)).createAnnotation(
												 mid, mid);
										
										if (lemAnn != null && lemEl.getText() != null) {
											lemAnn.setValue(lemEl.getText());
										}
									}
								}
							}						
						}
						sentAnn.setValue(sentBuilder.toString());
						numProcessedTokens += tokens.size();
					}
				} //else no tokens? no sentence
				t += dur;
			}
		} // else no sentences? check tokens?
	}
	
	/**
	 * Collect the tokens with the given id's.
	 * 
	 * @param ids the token id's
	 * @return a list of token elements or null
	 */
	private List<TCFElement> getTokensWithIDs(String[] ids) {
		if (ids == null || ids.length == 0) {
			return null;
		}
		List<TCFElement> tokEls = new ArrayList<TCFElement>(ids.length);
		
		for (String id : ids) {
			// if we can rely on the order of the tokens this could be optimized considerably.
			for (TCFElement tokEl : parser.getElementsByType(TCFType.TOKEN)) {
				if (id.equals(tokEl.getId())) {
					tokEls.add(tokEl);
					break;
				}
			}
		}
		
		return tokEls;
	}
	
	/**
	 * Returns an element of a specific type with an id-reference to the
	 * specified id.
	 * 
	 * @param id the id of the token
	 * @param type the type of dependent annotation
	 * 
	 * @return an element of the specified type or null
	 */
	private TCFElement getDependentsForToken(String id, TCFType type) {
		if (id == null) {
			return null;
		}
		
		List<TCFElement> elemList = parser.getElementsByType(type);
		
		if (elemList != null) {
			for (TCFElement te : elemList) {
				String idRefString = te.getIdRefs();
				if (idRefString != null) {
					String[] idRefs = idRefString.split(" ");
					for (int i = 0; i < idRefs.length; i++) {
						if (id.equals(idRefs[i])) {
							return te;
						}
					}
				}
			}
		}
		
		return null;
	}
	
//##############################################################################
// New methods to support calls to WebLicht As A Service using a tool chain file
//#############################

	/**
	 * Add types and tiers to the specified transcription.
	 * 
	 * @param trans the transcription to add types and tiers to
	 * @param sourceTier the existing tier which was the source of the WebLicht
	 * processing
	 * @param sourceType indicates the type of the source tier, sentence or 
	 * token
	 * @param nextTypes the types that need to be created, if {@code null} all 
	 * currently known and supported types or levels are created 
	 * ({@code sentence}, {@code token}, {@code pos-tags}, {@code lemma}
	 * 
	 * @return a map containing TCF type to tier mappings
	 */
	public Map<TCFType, TierImpl> createTiers(TranscriptionImpl trans, TierImpl sourceTier, 
			TCFType sourceType, List<TCFType> nextTypes) {
		Map<TCFType, TierImpl> tierMap = new HashMap<TCFType, TierImpl>();
		TierImpl senTier = null;
		TierImpl tokenTier = null;
		String suffix = "";
		if (sourceTier != null) {
			int index = sourceTier.getName().indexOf('@');
			if (index > 0) {
				suffix = sourceTier.getName().substring(index);
			}
		}
		
		if (sourceTier == null) {
			// ignore sourceType parameter, start with sentence level
			if (trans.getLinguisticTypeByName(SENT) == null) {
				LinguisticType senType = new LinguisticType(SENT);
				senType.setTimeAlignable(true);
				trans.addLinguisticType(senType);
			}
			senTier = trans.getTierWithId(SENT);
			if (senTier == null) {
				senTier = new TierImpl(SENT, null, trans, trans.getLinguisticTypeByName(SENT));
				trans.addTier(senTier);
			}
			tierMap.put(TCFType.SENTENCE, senTier);
		} else {
			if (sourceType == TCFType.SENTENCE) {
				senTier = trans.getTierWithId(sourceTier.getName());
				tierMap.put(TCFType.SENTENCE, senTier);
			} else if (sourceType == TCFType.TOKEN) {
				tokenTier = trans.getTierWithId(sourceTier.getName());
				tierMap.put(TCFType.TOKEN, tokenTier);
				senTier = tokenTier.getParentTier();// can still be null
				if (senTier != null) {
					tierMap.put(TCFType.SENTENCE, senTier);
				}
			}
		}
		
		if (nextTypes != null && nextTypes.contains(TCFType.SENTENCE)) {
			if (senTier == null) {
				// this is an error condition, the sentence tier should exist now
			}
		}
		
		if (nextTypes == null || nextTypes.contains(TCFType.TOKEN)) {
			if (tokenTier == null) {
				// assume sentence tier != null
				if (senTier != null) {
					LinguisticType tokType = trans.getLinguisticTypeByName(TOKEN);
					if (tokType == null) {
						tokType = new LinguisticType(TOKEN);
						tokType.addConstraint(new SymbolicSubdivision());
						tokType.setTimeAlignable(false);
						trans.addLinguisticType(tokType);
					}
					tokenTier = new TierImpl(senTier, TOKEN + suffix, null, trans, tokType);
					tokenTier.setLangRef(sourceTier.getLangRef());
					trans.addTier(tokenTier);
					tierMap.put(TCFType.TOKEN, tokenTier);
				} else {
					// report an error or unknown state
				}
			}// else tokenTier has already been added
		}
		
		if (nextTypes == null || nextTypes.contains(TCFType.POS_TAG)) {
			LinguisticType posType = trans.getLinguisticTypeByName(POSTAGS);
			if (posType == null) {
				posType = new LinguisticType(POSTAGS);
				posType.addConstraint(new SymbolicAssociation());
				posType.setTimeAlignable(false);
				trans.addLinguisticType(posType);
			}
			TierImpl posTagTier = new TierImpl(tokenTier, POSTAGS + suffix, null, trans, posType);
			posTagTier.setLangRef(sourceTier.getLangRef());
			trans.addTier(posTagTier);
			tierMap.put(TCFType.POS_TAG, posTagTier);
		}
		
		if (nextTypes == null || nextTypes.contains(TCFType.LEMMA)) {
			LinguisticType lemType = trans.getLinguisticTypeByName(LEMMA);
			if (lemType == null) {
				lemType = new LinguisticType(LEMMA);
				lemType.addConstraint(new SymbolicAssociation());
				lemType.setTimeAlignable(false);
				trans.addLinguisticType(lemType);
			}
			TierImpl lemTagTier = new TierImpl(tokenTier, LEMMA, null, trans, lemType);
			lemTagTier.setLangRef(sourceTier.getLangRef());
			trans.addTier(lemTagTier);
			tierMap.put(TCFType.LEMMA, lemTagTier);
		}
		// could add support for morphology tags
		
		return tierMap;
	}
	
	
	/**
	 * Creates new annotations based on the parsed contents returned by the
	 * service. 
	 *  
	 * @param sourceAnn the single annotation that provided the input for
	 * the call to the service
	 * @param inputType the type or level of the input, either 
	 * {@link TCFType#SENTENCE} or {@link TCFType#TOKEN}
	 * @param parsedContent the TCF elements retrieved from the response of
	 * the service
	 * @param tierMapping a map determining which destination tier (in the 
	 * destination transcription) should receive which type of contents 
	 */
	public void createAnnotations(AbstractAnnotation sourceAnn, 
			TCFType inputType, Map<TCFType, List<TCFElement>> parsedContent, 
			Map<TCFType, TierImpl> tierMapping) {
		
		if (inputType == TCFType.TOKEN) {
			// the input annotation represents a single token, create POS and lemma child annotations
			List<TCFElement> posList = parsedContent.get(TCFType.POS_TAG);
			TierImpl pt = tierMapping.get(TCFType.POS_TAG);
			long time = (sourceAnn.getBeginTimeBoundary() + sourceAnn.getEndTimeBoundary()) / 2;
			
			if (pt != null && posList != null && !posList.isEmpty()) {// size == 1 ?
				TCFElement e = posList.get(0); 			
				AbstractAnnotation posAnn = (AbstractAnnotation) pt.createAnnotation(time, time);
				if (posAnn != null && e.getText() != null) {
					posAnn.setValue(e.getText());
				}
			}
			// lemma 
			List<TCFElement> lemList = parsedContent.get(TCFType.LEMMA);
			TierImpl lemt = tierMapping.get(TCFType.LEMMA);
			if (lemt != null && lemList != null && !lemList.isEmpty()) {// size == 1 ?
				TCFElement e = lemList.get(0); 
				AbstractAnnotation lemAnn = (AbstractAnnotation) lemt.createAnnotation(time, time);
				if (lemAnn != null && e.getText() != null) {
					lemAnn.setValue(e.getText());
				}
			}
		} else { // input type is sentence, the source annotation potentially contains multiple words
			List<TCFElement> tokList = parsedContent.get(TCFType.TOKEN);
			TierImpl tokTier = tierMapping.get(TCFType.TOKEN);
			TierImpl posTier = tierMapping.get(TCFType.POS_TAG);
			TierImpl lemTier = tierMapping.get(TCFType.LEMMA);
			long time = (sourceAnn.getBeginTimeBoundary() + sourceAnn.getEndTimeBoundary()) / 2;
			AbstractAnnotation nextTokAnn = null;
			
			if (tokTier != null && tokList != null && !tokList.isEmpty()) {
				for (TCFElement tokEl : tokList) {
					AbstractAnnotation nextAnn = null;
					if (nextTokAnn == null) {
						nextAnn = (AbstractAnnotation) tokTier.createAnnotation(time, time);
					} else {
						nextAnn = (AbstractAnnotation) tokTier.createAnnotationAfter(nextTokAnn);
					}
					
					if (nextAnn != null) {
						if (tokEl.getText() != null) {
							nextAnn.setValue(tokEl.getText());
						}
						long nextMid = (nextAnn.getBeginTimeBoundary() + nextAnn.getEndTimeBoundary()) / 2;
						// part-of-speech tag
						TCFElement posElem = getFirstElementWithIdRef(parsedContent.get(TCFType.TAG), tokEl.getId());
						if (posElem != null && posTier != null) {
							AbstractAnnotation posAnn = (AbstractAnnotation) posTier.createAnnotation(nextMid, nextMid);
							if (posAnn != null && posElem.getText() != null) {
								posAnn.setValue(posElem.getText());
							}
						}
						// lemma
						TCFElement lemElem = getFirstElementWithIdRef(parsedContent.get(TCFType.LEMMA), tokEl.getId());
						if (lemElem != null && lemTier != null) {
							AbstractAnnotation lemAnn = (AbstractAnnotation) lemTier.createAnnotation(nextMid, nextMid);
							if (lemAnn != null && lemElem.getText() != null) {
								lemAnn.setValue(lemElem.getText());
							}
						}
						// update the nextTokAnn reference
						nextTokAnn = nextAnn;
					}
				}
			}
		}
	}
	
	/**
	 * Creates annotations for a number of token-type source annotations that
	 * have the same parent annotation and have been processed together. 
	 * 
	 * @param sourceAnnList the annotations providing the contents for the 
	 * call to the web service
	 * @param parsedContent the TCF elements retrieved from the response of
	 * the service 
	 * @param tierMapping a map determining which destination tier (in the 
	 * destination transcription) should receive which type of contents
	 */
	public void createAnnotations(List<AbstractAnnotation> sourceAnnList, 
			Map<TCFType, List<TCFElement>> parsedContent, Map<TCFType, TierImpl> tierMapping) {
		List<TCFElement> tokList = parsedContent.get(TCFType.TOKEN);
		// the tier containing the source annotations, could check against the annotations in the list
		//TierImpl tokTier = tierMapping.get(TCFType.TOKEN);
		TierImpl posTier = tierMapping.get(TCFType.POS_TAG);
		TierImpl lemTier = tierMapping.get(TCFType.LEMMA);
		// if the tokList has a different size than sourceAnnList, this is likely due to punctuation
		int numTokens = tokList.size();
		int numAnnos = sourceAnnList.size();
		if (numTokens != numAnnos) {
			// log difference
		}
		int ti = 0, ai = 0;
		for( ; ti < numTokens && ai < numAnnos; ti++, ai++) {
			TCFElement tokElem = tokList.get(ti);
			AbstractAnnotation aa = sourceAnnList.get(ai);
			if (numTokens > numAnnos) {
				// if there are more token than annotations check if a token can be skipped
				if (tokElem.getText() != null && tokElem.getText().length() < aa.getValue().length()
						&& tokElem.getText().length() == 1) {
					ti++;
					tokElem = tokList.get(ti);// assume there are not two successive punctuation tokens
				}
			}
			// create
			long nextMid = (aa.getBeginTimeBoundary() + aa.getEndTimeBoundary()) / 2;
			// part-of-speech tag
			if (posTier != null) {
				TCFElement posElem = getFirstElementWithIdRef(parsedContent.get(TCFType.TAG), tokElem.getId());
				if (posElem != null) {
					AbstractAnnotation posAnn = (AbstractAnnotation) posTier.createAnnotation(nextMid, nextMid);
					if (posAnn != null && posElem.getText() != null) {
						posAnn.setValue(posElem.getText());
					}
				}
			}
			// lemma
			if (lemTier != null) {
				TCFElement lemElem = getFirstElementWithIdRef(parsedContent.get(TCFType.LEMMA), tokElem.getId());
				if (lemElem != null) {
					AbstractAnnotation lemAnn = (AbstractAnnotation) lemTier.createAnnotation(nextMid, nextMid);
					if (lemAnn != null && lemElem.getText() != null) {
						lemAnn.setValue(lemElem.getText());
					}
				}
			}
		}
		
	}
	
	/**
	 * Returns the first element from a list with an {@code idref} to the 
	 * specified {@code id}.
	 * 
	 * @param elemList the list of elements to inspect
	 * @param id the {@code id} to look for
	 * 
	 * @return the first {@code TCFElement} or {@code null}
	 */
	private TCFElement getFirstElementWithIdRef(List<TCFElement> elemList, String id) {
		if (elemList == null || elemList.isEmpty()) {
			return null;
		}
		
		for (TCFElement elem : elemList) {
			String[] ids = elem.getIdRefs().split(" ");
			for (String s : ids) {
				if (s.equals(id)) return elem;
			}
		}
		
		return null;
	}
	
}
