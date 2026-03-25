package mpi.eudico.client.annotator.lexicon;

import java.util.List;
import java.util.Map;

import mpi.eudico.client.annotator.lexicon.api.LexSrvcAvailabilityDetector;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.server.corpora.lexicon.LexiconLink;
import mpi.eudico.server.corpora.lexicon.LexiconServiceClient;
import mpi.eudico.server.corpora.lexicon.LexiconServiceClientFactory;

/**
 * Utility class to load and add lexicon service client factories to a transcription
 * and to create service clients to lexicon link objects.
 *  
 * @author Han Sloetjes
 *
 */
public class LexiconClientFactoryLoader {
	/**
	 * Constructor, does nothing
	 */
	public LexiconClientFactoryLoader() {
		super();
	}

	/**
	 * Loads factories, if any installed, and creates client objects.
	 * 
	 * @param transcription the transcription to update
	 */
	public void loadLexiconClientFactories(TranscriptionImpl transcription) {
		if (transcription == null) {
			return;
		}
		if (transcription.isLexiconServicesLoaded()) {
			return;
		}
		Map<String, LexiconServiceClientFactory> factories = LexSrvcAvailabilityDetector.getLexiconServiceClientFactories();
		
		if (factories.size() == 0) {
			transcription.setLexiconServicesLoaded(true);
			return;
		}
		
		Map<String, LexiconLink> lexLinks = transcription.getLexiconLinks();

		for (Map.Entry<String, LexiconServiceClientFactory> entry : factories.entrySet()) {
			String name = entry.getKey();
			LexiconServiceClientFactory factory = entry.getValue();
			transcription.addLexiconServiceClientFactory(name, factory);
		
			if (lexLinks.size() > 0) {				
				for (Map.Entry<String, LexiconLink> linkEntry : lexLinks.entrySet()) {
					LexiconLink link = linkEntry.getValue();
					if (name.equals(link.getLexSrvcClntType())) {
						LexiconServiceClient client = factory.createClient(link.getUrl());
						link.setSrvcClient(client);
					}
				}
			}
		}
		
		List<LinguisticType> linTypes= transcription.getLinguisticTypes();
		
		for (int i = 0; i < linTypes.size(); i++) {
			LinguisticType lt = linTypes.get(i);
			
			if (lt.isUsingLexiconQueryBundle() && lt.getLexiconQueryBundle().getLink().getSrvcClient() == null) {
				LexiconServiceClientFactory factory = factories.get(lt.getLexiconQueryBundle().getLink().getLexSrvcClntType());
				if (factory != null) {
					LexiconServiceClient client = factory.createClient(lt.getLexiconQueryBundle().getLink().getUrl());
					lt.getLexiconQueryBundle().getLink().setSrvcClient(client);
				}
			}
		}
		
		transcription.setLexiconServicesLoaded(true);
	}
	
	/**
	 * Loads a single factory for a lexicon link that is to be added to a transcription.
	 * 
	 * @param transcription the transcription
	 * @param link the lexicon link
	 */
	public void loadLexiconClientFactory(TranscriptionImpl transcription, LexiconLink link) {
		if (transcription == null) {
			return;
		}
		if (link == null) {
			return;
		}
		
		Map<String, LexiconServiceClientFactory> factories = LexSrvcAvailabilityDetector.getLexiconServiceClientFactories();
		
		if (factories.size() == 0) {
			return;
		}

		for (Map.Entry<String, LexiconServiceClientFactory> entry : factories.entrySet()) {
			String name = entry.getKey();
			
			if (name.equals(link.getLexSrvcClntType())) {
				LexiconServiceClientFactory factory = entry.getValue();
				LexiconServiceClient client = factory.createClient(link.getUrl());
				link.setSrvcClient(client);
				
				if (!transcription.getLexiconServiceClientFactories().containsKey(name)) {
					transcription.addLexiconServiceClientFactory(name, factory);
				}
				break;
			}
		}
	}
}
