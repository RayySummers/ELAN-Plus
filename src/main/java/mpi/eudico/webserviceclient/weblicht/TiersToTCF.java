package mpi.eudico.webserviceclient.weblicht;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;

/**
 * A class that converts annotations of one or more tiers to tcf.
 * It stores a mapping of annotations to token and or sentence id's.
 * Currently it constructs a string without creating a DOM document first. (might change)
 * 
 * @author Han Sloetjes
 */
public class TiersToTCF {
	private int tokenId = 1;
	private int sentenceId = 1;
	private Map<AbstractAnnotation, String> annTokenMap;
	private Map<AbstractAnnotation, String> annSentenceMap;
	private StringBuilder textBuilder;
	private StringBuilder tokenBuilder;
	private StringBuilder sentenceBuilder;
	private String langAttr = "en";
	
	private static final String SPACE = " ";
	private static final String NL = "\n";
	private static final String LT = "<";
	private static final String LT_S = "</";
	private static final String GT = ">";
	private static final String S_GT = "/>";
	private static final String EQ = "=";
	private static final String QUOTE = "\"";
	private static final String FS = ".";
	// some optimizations
	private static final String TOKEN_B = LT + TCFConstants.TOKEN + SPACE + TCFConstants.ID + EQ + QUOTE;
	private static final String TOKEN_E = LT_S + TCFConstants.TOKEN + GT + NL;
	private static final String SENT_B = LT + TCFConstants.SENT + SPACE + TCFConstants.ID + EQ + QUOTE;
	private static final String SENT_M = QUOTE + SPACE + TCFConstants.TOKEN_IDS + EQ + QUOTE;
	
	/**
	 * No arg constructor.
	 */
	public TiersToTCF() {
		super();
	}
	
	/**
	 * Converts annotations to tcf elements.
	 * Is it necessary to fill the "text" element in case tokens and sentences are created?
	 * 
	 * @param sourceTier the tier containing the annotations to convert
	 * @param type the sort of annotation the tier contains, sentence or token level (or other) 
	 * 
	 * @return tcf as a String
	 */
	public String toTCFString(TierImpl sourceTier, String type) {
		if (sourceTier == null) {
			return null;
		}
		boolean sourceTierIsToken = true;
		if (type != null) {
			if (type.equals("Sentence")) {
				sourceTierIsToken = false;
			} else {// are there other options than sentence and token?...
				// sourceTierIsToken = true;
			}
		}
		
		if (sourceTier.getLangRef() != null) {
			langAttr = sourceTier.getLangRef();
			// the WebLicht service seems to support only 2 letter language codes
			// should actually do proper conversion from 3 to 2 letter code
			// a 500 error is returned if the code is longer than 2 chars or if the
			// service does not support the input language
			if (langAttr.length() > 2) {
				langAttr = langAttr.substring(0, 2);
			}
		}
		
		annTokenMap = new HashMap<AbstractAnnotation, String>();
		annSentenceMap = new LinkedHashMap<AbstractAnnotation, String>();
		String curSentId;
		
		textBuilder = new StringBuilder();
		tokenBuilder = new StringBuilder();
		sentenceBuilder = new StringBuilder();
		
		if (sourceTierIsToken) {
			// get parent tier for grouping into sentences //or root level? .getRootTier();
			Tier parTier = (Tier) sourceTier.getParentTier();
			if (parTier == null) {
				// treat all tokens as one sentence?
				createSentence(sourceTier.getAnnotations());
			} else {
				List<AbstractAnnotation> group = new ArrayList<AbstractAnnotation>();
				AbstractAnnotation curRootAnn = null;
				AbstractAnnotation aa;
				// loop over children, find all in same group, make sentence
				List<AbstractAnnotation> annotations = sourceTier.getAnnotations();
				for (int i = 0; i < annotations.size(); i++) {
					aa = (AbstractAnnotation) annotations.get(i);
					if (curRootAnn == null) {
						curRootAnn = (AbstractAnnotation) aa.getParentAnnotation();
						group.add(aa);
					} else if (curRootAnn == aa.getParentAnnotation()) {
						group.add(aa);
					} else {
						// new group starting
						curSentId = createSentence(group);
						if (curSentId != null) {
							annSentenceMap.put(curRootAnn, curSentId);
						}
						group.clear();
						group.add(aa);
						curRootAnn = (AbstractAnnotation) aa.getParentAnnotation();
					}
					
					if (i == annotations.size() - 1) {// last annotation
						curSentId = createSentence(group);
						if (curSentId != null) {
							annSentenceMap.put(curRootAnn, curSentId);
						}
					}
				}
				
			}
		} else {// sentences
			// treat every annotation as a sentence, tokenize or send as plain text?
			// give this a try

			AbstractAnnotation aa;
			// loop over children, find all in same group, make sentence
			List<AbstractAnnotation> annotations = sourceTier.getAnnotations();
			String value;
			String[] splitValue;
			Pattern pattern = Pattern.compile(SPACE);
			
			for (int i = 0; i < annotations.size(); i++) {
				aa = (AbstractAnnotation) annotations.get(i);
				value = aa.getValue();
				if (value == null || value.length() == 0) {
					continue;// no use to send an empty sentence/token to a service
				}
				splitValue = pattern.split(value);
				if (splitValue.length > 0) {
					
					curSentId = createSentence(splitValue);
					
					if (curSentId != null) {
						annSentenceMap.put(aa, curSentId);
					}
				}
			}
			
		}
		
		return currentContentToTCF();
	}
	
	/**
	 * Converts the annotations of the given tier into plain text (a single String object).
	 * 
	 * @param sourceTier the tier to convert
	 * @return a single string containing the text of all annotations
	 */
	public String toPlainText(TierImpl sourceTier) {
		if (sourceTier == null) {
			return null;
		}
		
		textBuilder = new StringBuilder();
		
		List<AbstractAnnotation> annotations = sourceTier.getAnnotations();
		AbstractAnnotation aa;
		String value;
		
		for (int i = 0; i < annotations.size(); i++) {
			aa = annotations.get(i);
			value = aa.getValue();
			if (value != null && aa.getValue().length() > 0) {
				textBuilder.append(value);
				if (Character.getType(value.charAt(value.length() - 1)) != Character.END_PUNCTUATION) {
					textBuilder.append(FS);
				}
				textBuilder.append(SPACE);
			}
		}
		
		return textBuilder.toString();
	}
	
	private String currentContentToTCF() {
		StringBuilder tcfBuilder = new StringBuilder();
		// outer element
		tcfBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		tcfBuilder.append(NL);
		tcfBuilder.append("<D-Spin xmlns=\"http://www.dspin.de/data\" version=\"0.4\">");
		tcfBuilder.append(NL);
		// is metadata mandatory?
		tcfBuilder.append("<MetaData xmlns=\"http://www.dspin.de/data/metadata\"><source/></MetaData>");
		tcfBuilder.append(NL);
		// add text element, add a language attribute
		tcfBuilder.append("<TextCorpus xmlns=\"http://www.dspin.de/data/textcorpus\" lang=\"");
		tcfBuilder.append(langAttr);
		tcfBuilder.append("\">");
		tcfBuilder.append(NL);
		tcfBuilder.append("<text>");
		tcfBuilder.append(NL);
		if (textBuilder != null) {
			tcfBuilder.append(textBuilder.toString());
			tcfBuilder.append(NL);
		}
		tcfBuilder.append("</text>");
		tcfBuilder.append(NL);
		// add tokens / words
		tcfBuilder.append("<tokens>");
		tcfBuilder.append(NL);
		if (tokenBuilder != null) {
			tcfBuilder.append(tokenBuilder.toString());
		}
		tcfBuilder.append("</tokens>");
		tcfBuilder.append(NL);
		// add sentences
		tcfBuilder.append("<sentences>");
		tcfBuilder.append(NL);
		if (sentenceBuilder != null) {
			tcfBuilder.append(sentenceBuilder.toString());
		}
		tcfBuilder.append("</sentences>");
		tcfBuilder.append(NL);
		tcfBuilder.append("</TextCorpus>");
		tcfBuilder.append(NL);
		tcfBuilder.append("</D-Spin>");
		
		return tcfBuilder.toString();
	}

	/**
	 * Adds token elements and finally a sentence element.
	 * 
	 * @param annotations the tokens of one sentence 
	 */
	private String createSentence(List<AbstractAnnotation> annotations) {
		AbstractAnnotation aa;
		String value;
		String tokenID;
		
		StringBuilder tokenIDs = new StringBuilder();
		
		for (int i = 0; i < annotations.size(); i++) {
			aa = annotations.get(i);
			value = aa.getValue();
			
			if (value != null && value.length() > 0) {
				if (i != 0) {
					textBuilder.append(SPACE);
				}
				textBuilder.append(value);
				if (i == annotations.size() - 1) {
					textBuilder.append(SPACE);
				}
				// check for punctuations? start and end, or more?
				if (Character.getType(value.charAt(0)) == Character.START_PUNCTUATION) {
					tokenID = addToken(value.substring(0, 1));
					tokenIDs.append(tokenID);					
					if (value.length() > 1) {
						tokenIDs.append(SPACE);
						value = value.substring(1);
					} else {
						annTokenMap.put(aa,  tokenID);
						if (i != annotations.size() - 1) {
							tokenIDs.append(SPACE);
						}
						continue;
					}
					
				}
				if (Character.getType(value.charAt(value.length() - 1)) == Character.END_PUNCTUATION) {
					if (value.length() > 1) {
						String punct = value.substring(value.length() - 1);
						value = value.substring(0,  value.length() - 1);
						tokenID = addToken(value);
						tokenIDs.append(tokenID);
						tokenIDs.append(SPACE);
						annTokenMap.put(aa,  tokenID);
						
						tokenID = addToken(punct);						
						tokenIDs.append(tokenID);
						if (i != annotations.size() - 1) {
							tokenIDs.append(SPACE);
						}
					} else {
						tokenID = addToken(value);
						tokenIDs.append(tokenID);
						if (i != annotations.size() - 1) {
							tokenIDs.append(SPACE);
						}
						annTokenMap.put(aa,  tokenID);
					}
				} else {
					tokenID = addToken(value);
					tokenIDs.append(tokenID);
					if (i != annotations.size() - 1) {
						tokenIDs.append(SPACE);
					}
					annTokenMap.put(aa,  tokenID);
				}
			}
		}
		if (tokenIDs.length() > 0) {
			String sentenceID = "s" + sentenceId++;
			
			sentenceBuilder.append(SENT_B);
			sentenceBuilder.append(sentenceID);
			sentenceBuilder.append(SENT_M);
			sentenceBuilder.append(tokenIDs.toString());
			sentenceBuilder.append(QUOTE);
			sentenceBuilder.append(S_GT);
			sentenceBuilder.append(NL);
			
			return sentenceID;
		}
		
		return null;
	}
	
	/**
	 * Creates a sentence element and token elements based on the specified array of strings.
	 * 
	 * @param tokens the array of tokens
	 * @return the id of the sentence
	 */
	private String createSentence(String[] tokens) {
		if (tokens != null && tokens.length > 0) {
			String value;
			String tokenID;
			
			StringBuilder tokenIDs = new StringBuilder();
			
			for (int i = 0; i < tokens.length; i++) {
				value = tokens[i];
				
				if (value == null || value.length() == 0) {
					continue;
				}
				// add to text element
				if (i != 0) {
					textBuilder.append(SPACE);
				}
				textBuilder.append(value);
				if (i == tokens.length -1 && 
						Character.getType(textBuilder.charAt(textBuilder.length() - 1)) != Character.START_PUNCTUATION) {
					textBuilder.append(FS);
				}
				
				if (i == tokens.length - 1) {
					textBuilder.append(SPACE);
				}
				
				// check for punctuations? start and end, or more?
				if (Character.getType(value.charAt(0)) == Character.START_PUNCTUATION) {
					tokenID = addToken(value.substring(0, 1));
					tokenIDs.append(tokenID);					
					if (value.length() > 1) {
						tokenIDs.append(SPACE);
						value = value.substring(1);
					} else {
						if (i != tokens.length - 1) {
							tokenIDs.append(SPACE);
						}
						continue;
					}
					
				}
				if (Character.getType(value.charAt(value.length() - 1)) == Character.END_PUNCTUATION) {
					if (value.length() > 1) {
						String punct = value.substring(value.length() - 1);
						value = value.substring(0,  value.length() - 1);
						tokenID = addToken(value);
						tokenIDs.append(tokenID);
						tokenIDs.append(SPACE);
						
						tokenID = addToken(punct);						
						tokenIDs.append(tokenID);
						if (i != tokens.length - 1) {
							tokenIDs.append(SPACE);
						}
					} else {
						tokenID = addToken(value);
						tokenIDs.append(tokenID);
						if (i != tokens.length - 1) {
							tokenIDs.append(SPACE);
						}
					}
				} else {
					tokenID = addToken(value);
					tokenIDs.append(tokenID);
					if (i != tokens.length - 1) {
						tokenIDs.append(SPACE);
					}
				}
				/* rather don't add end punctuation if it is not in the source
				if (i == tokens.length - 1) {
					// check punctuation, add a full stop if not there
					if (Character.getType(value.charAt(value.length() - 1)) != Character.END_PUNCTUATION) {
						tokenIDs.append(SPACE);
						tokenID = addToken(FS);
						tokenIDs.append(tokenID);
					}
				}
				*/
			}
			// sentence
			if (tokenIDs.length() > 0) {
				String sentenceID = "s" + sentenceId++;
				
				sentenceBuilder.append(SENT_B);
				sentenceBuilder.append(sentenceID);
				sentenceBuilder.append(SENT_M);
				sentenceBuilder.append(tokenIDs.toString());
				sentenceBuilder.append(QUOTE);
				sentenceBuilder.append(S_GT);
				sentenceBuilder.append(NL);
				
				return sentenceID;
			}
		}
		
		return null;
	}
	
	/**
	 * Adds a token element.
	 * 
	 * @param value the text value
	 * @return the token id for this next token
	 */
	private String addToken(String value) {
		String id = "t" + tokenId++;
		
		tokenBuilder.append(TOKEN_B);
//		tokenBuilder.append(LT);
//		tokenBuilder.append(TCFConstants.TOKEN);
//		tokenBuilder.append(SPACE);
//		tokenBuilder.append(TCFConstants.ID);
//		tokenBuilder.append(EQ);
//		tokenBuilder.append(QUOTE);
		tokenBuilder.append(id);
		tokenBuilder.append(QUOTE);
		tokenBuilder.append(GT);
		tokenBuilder.append(value);
//		tokenBuilder.append(LT_S);
//		tokenBuilder.append(TCFConstants.TOKEN);
//		tokenBuilder.append(GT);
//		tokenBuilder.append(NL);
		tokenBuilder.append(TOKEN_E);
		
		return id;
	}
	
	private static final String docStart = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><D-Spin xmlns=\"http://www.dspin.de/data\" version=\"5\">";
	private static final String docEnd = "</D-Spin>";
	private static final String meta = "<MetaData xmlns=\"http://www.dspin.de/data/metadata\"></MetaData>";
	private static final String corpusStart = "<TextCorpus xmlns=\"http://www.dspin.de/data/textcorpus\" lang=\"%s\">";// 1 parameter
	private static final String corpusEnd = "</TextCorpus>";
	private static final String textNode = "<text>%s</text>";// 1 parameter, element maybe never needs to have actual content
	private static final String tokensStart = "<tc:tokens xmlns:tc=\"http://www.dspin.de/data/textcorpus\">";
	private static final String tokensEnd = "</tc:tokens>";
	private static final String tokenNode = "<tc:token ID=\"w%d\">%s</tc:token>";// 2 parameters
	private static final String sentencesStart = "<tc:sentences xmlns:tc=\"http://www.dspin.de/data/textcorpus\">";
	private static final String sentencesEnd = "</tc:sentences>";
	private static final String sentenceNode = "<tc:sentence tokenIDs=\"%s\" ID=\"s%d\"/>";// parameters, first one are space separated id refs
	
	/**
	 * Converts a token annotation to a TCF 5 string.
	 * 
	 * @param ann the annotation
	 * @param lang the language property
	 * 
	 * @return a TCF 5 string
	 */
	public String tokenToTCF5String(AbstractAnnotation ann, String lang) {
		if (ann.getValue().isBlank()) {
			return null;
		}
		StringBuilder sb = new StringBuilder(docStart);
		sb.append(meta);
		sb.append(String.format(corpusStart, lang));
		sb.append(String.format(textNode, ""));
		sb.append(tokensStart);
		sb.append(String.format(tokenNode, 1, ann.getValue()));
		sb.append(tokensEnd);
		sb.append(sentencesStart);
		sb.append(String.format(sentenceNode, "w1", 1));
		sb.append(sentencesEnd).append(corpusEnd).append(docEnd);
		
		return sb.toString();
	}
	
	/**
	 * Converts a sentence annotation to a TCF 5 string.
	 * 
	 * @param ann the annotation
	 * @param lang the language property
	 * @return a TCF 5 string
	 */
	public String sentenceToTCF5String(AbstractAnnotation ann, String lang) {
		if (ann.getValue().isBlank()) {
			return null;
		}
		String[] tokens = ann.getValue().split("\\s");
		StringBuilder tokenIds = new StringBuilder();
		int t = 0;
		StringBuilder sb = new StringBuilder(docStart);
		sb.append(meta);
		sb.append(String.format(corpusStart, lang));
		sb.append(String.format(textNode, ""));
		sb.append(tokensStart);
		for (int i = 0; i < tokens.length; i++) {
			String id = "w" + ++t;
			sb.append(String.format(tokenNode, t, tokens[i]));
			tokenIds.append(id);
			if (i < tokens.length - 1) {
				tokenIds.append(SPACE);
			}
		}
		sb.append(tokensEnd);
		sb.append(sentencesStart);
		sb.append(String.format(sentenceNode, tokenIds.toString(), 1));
		sb.append(sentencesEnd).append(corpusEnd).append(docEnd);
		
		return sb.toString();
	}
	
	/**
	 * Converts multiple token annotations into a TCF 5 string.
	 * 
	 * @param tokList a list of token annotations
	 * @param lang the language property
	 * 
	 * @return a TCF 5 string
	 */
	public String tokensToTCF5String(List<AbstractAnnotation> tokList, String lang) {
		if (tokList.isEmpty()) {
			return null;
		}
		
		StringBuilder tokenIds = new StringBuilder();
		int t = 0;
		
		StringBuilder sb = new StringBuilder(docStart);
		sb.append(meta);
		sb.append(String.format(corpusStart, lang));
		sb.append(String.format(textNode, ""));
		sb.append(tokensStart);
		
		for (int i = 0; i < tokList.size(); i++) {
			String id = "w" + ++t;
			sb.append(String.format(tokenNode, t, tokList.get(i).getValue()));
			tokenIds.append(id);
			if (i < tokList.size() - 1) {
				tokenIds.append(SPACE);
			}
		}

		sb.append(tokensEnd);
		sb.append(sentencesStart);
		sb.append(String.format(sentenceNode, tokenIds.toString(), 1));
		sb.append(sentencesEnd).append(corpusEnd).append(docEnd);
		
		return sb.toString();
	}
}
