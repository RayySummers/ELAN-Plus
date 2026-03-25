package mpi.eudico.server.corpora.clomimpl.json;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.json.JSONObject;
import static mpi.eudico.server.corpora.util.ServerLogger.LOG;


/**
 * Client class to carry out the http requests to the annotation server. 
 * The {@link HttpClient} is used to send requests and retrieve their responses.
 */
public class WebAnnotationClient {
	/** accept header */
	public static final String ACCEPT = "application/ld+json; profile=\"http://www.w3.org/ns/anno.jsonld\"";
	/** content type header */
	public static final String CONTENT_TYPE = "application/ld+json; profile=\"http://www.w3.org/ns/anno.jsonld\"";
	/** server URL property */
	public static final String URI ;
	/** HTTP created status code */
	public static final int HTTP_CREATED = 201;
	
	/** HTTP OK status code */
	public static final int OK = 200;
	
	/** HTTP UNAUTHORIZED status code */
	public static final int UNAUTHORIZED = 401;
	
    /** Boolean to indicate authentication enabled */
	static Boolean isAuthenticationEnabled = false;
			
	HttpClient httpClient = HttpClient.newBuilder()
	        .version(HttpClient.Version.HTTP_1_1)
	        .connectTimeout(Duration.ofSeconds(20))
	        .build();
	
	static {
		String uriProperty = System.getProperty("AnnotationServer.URL");
		if (uriProperty == null || uriProperty.isEmpty()) {
			//URI = "http://localhost:8080";
			URI = "https://annorepo.dev.clariah.nl";
		} else {
			URI = uriProperty;
		}
	}
	
	/**
	 * Creates a new client instance.
	 */
	public WebAnnotationClient() {
		super();
	}

	/**
	 * Annotation collection post request exporting the collection to an 
	 * annotation server.
	 * 
	 * @param annotatioCollectionJson the collection json as a string
	 * @param authenticationBearerKey the authentication bearer key if authentication is enabled 
	 * @return the collection id from the server or an empty string in case of
	 * an exception 
	 */
	public String exportAnnotationCollection(String annotatioCollectionJson, String authenticationBearerKey) {
		if(!authenticationBearerKey.isEmpty()) {
			authenticationBearerKey = "Bearer " + authenticationBearerKey ;
		}
		try {
			HttpRequest request = HttpRequest.newBuilder()
					  .uri(new URI(URI + "/w3c/"))
					  .headers("Accept", ACCEPT, "Content-Type", CONTENT_TYPE, "Authorization" , authenticationBearerKey)
					  .POST(HttpRequest.BodyPublishers.ofString(annotatioCollectionJson))
					  .build();
			HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
						
			if (response.statusCode() == HTTP_CREATED) {
				JSONObject annotationCollectionResponse = new JSONObject(response.body());
				String collectionId = (String) annotationCollectionResponse.get("id");
				return collectionId;
			} else {
				if (LOG.isLoggable(Level.WARNING)) {
					LOG.log(Level.WARNING, "Unable to export tier or collection to server. The server responded with status code: " + response.statusCode());
				}
				return "";
			}
			
		} catch (URISyntaxException e) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "URI Syntax Exception when exporting tier to annotation server ");
			}
			return "";
		} catch (IOException e) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "IO error while connecting to annotation server " + e);
			}
			return "";
		} catch (InterruptedException e) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "InterruptedException when exporting tier to annotation server");
			}
			return "";
		} catch (Exception e) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "Error when exporting tier to annotation server");
			}
			return "";
		}
		
	}
	
	/**
	 * Annotation post request exporting an annotation to an annotation server.
	 * 
	 * @param annotationJson the annotation json as a string
	 * @param collectionIDURI the collection id returned from from the server
	 * @param authenticationBearerKey the authentication bearer key if authentication is enabled 
	 * 
	 * @return the annotation id from the server or an empty string in case of
	 * an exception
	 */
	public String exportAnnotation(String annotationJson, String collectionIDURI, String authenticationBearerKey) {
		if(!authenticationBearerKey.isEmpty()) {
			authenticationBearerKey = "Bearer " + authenticationBearerKey ;
		}
		try {
			HttpRequest request = HttpRequest.newBuilder()
							.uri(new URI(collectionIDURI))
							 .headers("Accept", ACCEPT, "Content-Type", CONTENT_TYPE, "Authorization", authenticationBearerKey)
							 .POST(HttpRequest.BodyPublishers.ofString(annotationJson))
							 .build();
			
			HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
									
			if (response.statusCode() == HTTP_CREATED) {
				JSONObject annotationResponse = new JSONObject(response.body());
				String annotationID = (String) annotationResponse.get("id");
				return annotationID;
			} else {
				if (LOG.isLoggable(Level.WARNING)) {
					LOG.log(Level.WARNING, "Unable to export annotation to server. The server responded with status code: " + response.statusCode());
				}
				return "";
			}
		} catch (URISyntaxException e) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "URI Syntax Exception when exporting annotation to annotation server ");
			}
			return "";
		} catch (IOException e) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "IO exception when exporting annotation to annotation server");
			}
			return "";
		} catch (InterruptedException e) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "InterruptedException when exporting annotation to annotation server");
			}
			return "";
		} catch (Exception e) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "Error when exporting annotation to annotation server");
			}
			return "";
		}
	}
	
	/**
	 * Get collection request to fetch the collection response
	 * @param collectionIdURI the collection uri to be passed to the request
	 * @param authenticationBearerKey the authentication bearer key if authentication is enabled 
	 * @return the json collection response body
	 */
	public Map<Integer, String> importCollection(String collectionIdURI , String authenticationBearerKey) {
		Map<Integer,String> responseMap = new HashMap<Integer, String>();
		
		if(!authenticationBearerKey.isEmpty()) {
			authenticationBearerKey = "Bearer " + authenticationBearerKey ;
		}
		try {
			HttpRequest httpRequest = HttpRequest.newBuilder().uri(new URI(collectionIdURI)).headers("Accept", ACCEPT , "Authorization" , authenticationBearerKey).GET().build();
			
			HttpResponse<String> response = httpClient.send(httpRequest, BodyHandlers.ofString());
			
			if(response.statusCode() == OK) {
				JSONObject collectionJSON = new JSONObject(response.body());
				responseMap.put(response.statusCode(), collectionJSON.toString());
				return responseMap;
			}else if(response.statusCode() == UNAUTHORIZED) {
				String errorMessage = response.body();
				responseMap.put(response.statusCode(), errorMessage);
				return responseMap;
			} else {
				if (LOG.isLoggable(Level.WARNING)) {
					LOG.log(Level.WARNING, "Unable to import collection from server. The server responded with status code: " + response.statusCode());
				}
				return responseMap;
			}
		} catch (URISyntaxException e) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "URI Syntax Exception when importing collection from server ");
			}
			return responseMap;
		} catch (IOException e) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "IO exception when importing collection from server");
			}
			return responseMap;
		} catch (InterruptedException e) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "InterruptedException when importing collection from server");
			}
			return responseMap;
		} catch (Exception e) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "Error when importing collection from server");
			}
			return responseMap;
		}
		
	}
	
	/**
	 * Checks if the server is enabled withAuthentication: true 
	 * Makes a call to retrieve info about server  
	 * @return boolean value to indicate if authentication is enabled or not
	 */
	public Boolean isAuthenticationEnabled() {
		try {
			HttpRequest httpRequest = HttpRequest.newBuilder().uri(new URI(URI + "/about/")).GET().build();

			HttpResponse<String> response = httpClient.send(httpRequest, BodyHandlers.ofString());

			if (response.statusCode() == OK) {
				JSONObject serverInfoJSON = new JSONObject(response.body());
				isAuthenticationEnabled = (Boolean) serverInfoJSON.get("withAuthentication");
			}
		} catch (URISyntaxException e) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "URI Syntax Exception when getting the server info ");
			}
			return false;
		} catch (IOException e) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "IO exception when getting the server info");
			}
			return false;
		} catch (InterruptedException e) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "InterruptedException when getting the server info");
			}
			return false;
		} catch (Exception e) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "Error when getting the server info");
			}
			return false;
		}

		return isAuthenticationEnabled;

	}

}
