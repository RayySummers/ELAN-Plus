package mpi.eudico.client.annotator.util;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.ExternalReference;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.util.BasicControlledVocabulary;
import mpi.eudico.util.CVEntry;
import mpi.eudico.util.ControlledVocabulary;
import nl.mpi.util.FileExtension;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.logging.Level;

/**
 * Utility class to open a URL and log exceptions.
 * A {@code mailto:} URL is directed to the default mail application,
 * all other remote URL's are directed to the browser.<p>
 * Local files are opened in the system's default application, 
 * if this is supported by the platform.  
 */
public class UrlOpener {
	
	/**
	 * Private constructor.
	 */
	private UrlOpener() {
		super();
	}

	/**
	 * Opens the URI either via {@link Desktop#mail(URI)} or via 
	 * {@link Desktop#browse(URI)}.
	 * 
	 * @param url the address to open or to mail to
	 * @param inNewThread if {@code true} opening the browser or mail 
	 * application is performed in a new thread
	 * @throws Exception any exception that can occur
	 */
	public static void openUrl(String url, boolean inNewThread) throws Exception {
		//System.out.println("openUrl: " + url);
		if (url == null) {
            return;
        }
 		
        URI uri = null;;
		try {
			uri = new URI(url);
		} catch (URISyntaxException use) {
			ClientLogger.LOG.warning("Error in URI creation: " + use.getMessage());
			throw(use);
			//use.printStackTrace();
		} 
		
		if (inNewThread) {
			new Thread(new UrlOpenerRunnable(uri)).start();
		} else {
			try {
				if (url.startsWith("mailto:")) {
					Desktop.getDesktop().mail(uri);
				} else {
					Desktop.getDesktop().browse(uri);
				}
			} catch (IOException ioe) {
				ClientLogger.LOG.warning("Error opening webpage or mail: " + ioe.getMessage());
				throw(ioe);
				//ioe.printStackTrace();
			}
		}
	}
	
	/**
	 * Opens the URI either via {@link Desktop#mail(URI)} or via 
	 * {@link Desktop#browse(URI)}.
	 * 
	 * @param uri the URI of the address to open or to mail to
	 * @param inNewThread if {@code true} opening the browser or mail 
	 * application is performed in a new thread
	 * @throws Exception any exception that can occur
	 */
	public static void openUrl(URI uri, boolean inNewThread) throws Exception {
		//System.out.println("openUrl: " + uri);
		if (uri == null) {
            return;
        }

		if (inNewThread) {
			new Thread(new UrlOpenerRunnable(uri)).start();
		} else {
			try {
				if (uri.isOpaque() && uri.getScheme().equals("mailto:")) {
					Desktop.getDesktop().mail(uri);
				} else {
					Desktop.getDesktop().browse(uri);
				}
			} catch (IOException ioe) {
				ClientLogger.LOG.warning("Error opening webpage or mail: " + ioe.getMessage());
				throw(ioe);
				//ioe.printStackTrace();
			}
		}
	}
	
	/**
	 * Opens a file in the system's default application.
	 * 
	 * @param uri a file URI
	 * @param inNewThread if {@code true} opening the file in the  
	 * application is performed in a new thread
	 * @throws Exception any exception that can occur
	 */
	public static void openResourceInDefaultApplication(URI uri, boolean inNewThread) throws Exception {
		if (uri == null) {
            return;
        }
		File f = new File(uri);
		
		if (f.exists()) {
			if (inNewThread) {
				new Thread(new FileOpenerRunnable(f)).start();
			} else {
				try {
					Desktop.getDesktop().open(f);
				} catch (Exception ex) {
					//
					throw ex;
				} catch (Throwable t) {
					//
					throw new Exception(t);
				}
			}
		}
	}
	
	/**
	 * Checks whether the annotation links in any known way to an Internet
	 * resource.
	 * 
	 * @param annotation the annotation to check
	 * @return {@code true} if at least one such link is found, {@code false}
	 * otherwise
	 */
	public static boolean hasBrowserLink(AbstractAnnotation annotation) {
		return  UrlOpener.hasBrowserLinkInExtRef(annotation) ||
				UrlOpener.hasBrowserLinkInCV(annotation) ||
				UrlOpener.hasBrowserLinkInContent(annotation);
	}
	
	/**
	 * Checks whether the (E)CV for this annotation has an (E)CV entry with a
	 * URL.  
	 * 
	 * @param annotation the annotation to check
	 * @return {@code true} if there is such a URL
	 */
	public static boolean hasBrowserLinkInCV(Annotation annotation) {
		if (annotation == null) return false;
		
		String cvName = annotation.getTier().getLinguisticType().getControlledVocabularyName();
		if(cvName != null) {
			ControlledVocabulary cv = annotation.getTier().getTranscription().getControlledVocabulary(cvName);
			String entryId = annotation.getCVEntryId();
			if (entryId != null && !entryId.isBlank()) {
				CVEntry cvEntry = cv.getEntrybyId(entryId);
				if (cvEntry != null) {
					/*
					// 1 external resource property
					ExternalReference extRef = cvEntry.getExternalRef();
					if (extRef != null && extRef.getReferenceType() == ExternalReference.RESOURCE_URL) {
						String baseUrlString = extRef.getValue();
						if (baseUrlString != null && !baseUrlString.equals("")) {
							return true;
						} 
					}
					// 2 entry description
					String descString = cvEntry.getDescription(cv.getDefaultLanguageIndex());
					if (UrlOpener.maybeLink(descString, true)) {
						return true;
					}
					// 3 entry value (unlikely to occur)
					if (UrlOpener.maybeLink(cvEntry.getValue(cv.getDefaultLanguageIndex()), true)) {
						return true;
					}
					*/
					return UrlOpener.hasBrowserLink(cvEntry);
				}
			}
		}
		return false;
	}

	/**
	 * Checks whether the annotation (probably) has an Internet address as 
	 * its value.<p>
	 * The check currently only looks for a {@code http(s)} protocol at the 
	 * start of the annotation value. Could also check if creating a 
	 * {@code URL} succeeds without exception. 
	 * 
	 * @param annotation the annotation to check
	 * @return {@code true} if the contents appears to point to an Internet
	 * location, {@code false} otherwise
	 */
	public static boolean hasBrowserLinkInContent(Annotation annotation) {
		if (annotation == null) return false;
		
		//return annotation.getValue().matches("^\s?http[s]?://");
		return annotation.getValue().strip().startsWith("http");
	}
	
	/**
	 * Checks whether the annotation has an external reference of type
	 * {@link ExternalReference#RESOURCE_URL}, pointing to an Internet address.
	 * 
	 * @param annotation the annotation to check
	 * @return {@code true} if an external resource {@code URL} is found, 
	 * {@code false} otherwise
	 */
	public static boolean hasBrowserLinkInExtRef(AbstractAnnotation annotation) {
		if (annotation == null) return false;

		String refURL = annotation.getExtRefValue(ExternalReference.RESOURCE_URL);
		
		if (refURL != null && !refURL.isBlank()) {
			if (refURL.strip().startsWith("http")) {
				return true;
			}
			// check if the URL is relative to the location of the transcription
			if (refURL.startsWith("./") || refURL.startsWith("../")) {
				int s1 = refURL.indexOf('/');
				String trPath = annotation.getTier().getTranscription().getFullPath();
				if (trPath != null && trPath.startsWith("http")) {
					int s2 = trPath.lastIndexOf('/');
					if (s2 > 0) {
						if (refURL.startsWith("../")) {
							int s3 = refURL.substring(0, s2).lastIndexOf('/');
							if (s3 > 0) {
								s2 = s3;
							}
						}
						String fullPath = trPath.substring(0, s2) + refURL.substring(s1);
						try {
							new URI(fullPath);
							return true;
						} catch (Throwable t) {
							// return false
						}
					}
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Get a {@code http(s)} URI from the annotation's 
	 * external reference, controlled vocabulary entry or its contents.
	 *  
	 * @param annotation the annotation to check
	 * @return the first URI or {@code null}
	 */
	public static URI getBrowserURIFrom(AbstractAnnotation annotation) {
		if (annotation == null) return null;
		// 1 external resource URL
		String refURL = annotation.getExtRefValue(ExternalReference.RESOURCE_URL);
		
		if (refURL != null && !refURL.isBlank()) {
			if (refURL.strip().startsWith("http")) {
				try {
					return new URI(refURL);
				} catch (URISyntaxException use) {
					//
				}				
			} else {// relative to transcription URI
				try {
					URI docURI = new URI(annotation.getTier().getTranscription().getFullPath());
					if (docURI.getScheme() != null && docURI.getScheme().startsWith("http")) {
						URI refURI = URI.create(refURL);
						URI resolved = docURI.resolve(refURI);
						if (resolved != refURI && resolved != docURI) {
							return resolved;
						}
					}
				} catch (Exception ex) {
					//
				}
			}
		}
		// 2 link in controlled vocabulary entry
		String cvName = annotation.getTier().getLinguisticType().getControlledVocabularyName();
		if(cvName != null) {
			ControlledVocabulary cv = annotation.getTier().getTranscription().getControlledVocabulary(cvName);
			String entryId = annotation.getCVEntryId();
			
			boolean signGlossURL = false;
			// The following bit is for removing the 'gloss' prefix of an entry id
	    	if(entryId != null && entryId.startsWith("gloss")) {
	    		entryId = entryId.substring(5);
	    		signGlossURL = true;
	    	}
	    	
			if (entryId != null && !entryId.isBlank()) {
				CVEntry entry = cv.getEntrybyId(entryId);
				if (entry != null) {
					ExternalReference extRef1 = entry.getExternalRef();
					if (extRef1 != null && extRef1.getReferenceType() == ExternalReference.RESOURCE_URL) {
						String extRefString = extRef1.getValue();
						if (extRefString != null && extRefString.startsWith("http")) {
							if (signGlossURL) {
								// the following construction of a URL is project specific
								if(!extRefString.endsWith("/")) {
									extRefString = extRefString + "/";
								}
								extRefString = extRefString + entryId;
							}
							
							try {
								return new URI(extRefString);
							} catch (Throwable t) {
								//
							}
						}
					} 
					// get URI from value or description
					else if (UrlOpener.maybeLink(entry.getDescription(cv.getDefaultLanguageIndex()), true)) {
						try {
							return new URI(entry.getDescription(cv.getDefaultLanguageIndex()));
						} catch (Throwable t) {
							//
						}
					} else if (UrlOpener.maybeLink(entry.getValue(cv.getDefaultLanguageIndex()), true)) {
						try {
							return new URI(entry.getValue(cv.getDefaultLanguageIndex()));
						} catch (Throwable t) {
							//
						}
					}
					
				}
			}
		}
		// 3 URI from content
		if (annotation.getValue().strip().startsWith("http")) {
			try {
				return new URI(annotation.getValue().strip());
			} catch (Exception ex) {
				//
			}
		}
		return null;
	}
	
	/**
	 * Get a local file URI from the annotation's external reference, 
	 * controlled vocabulary entry or its contents.
	 *  
	 * @param annotation the annotation to check
	 * @return the first URI or {@code null}
	 */
	public static URI getLocalURIFrom(AbstractAnnotation annotation) {
		if (annotation == null) return null;
		// 1 external reference
		String extRefURL = annotation.getExtRefValue(ExternalReference.RESOURCE_URL);
			
		// 2 CV entry reference
		if (extRefURL == null) {
			String cvName = annotation.getTier().getLinguisticType().getControlledVocabularyName();
			if (cvName != null) {
				ControlledVocabulary cv = annotation.getTier().getTranscription().getControlledVocabulary(cvName);
				String entryId = annotation.getCVEntryId();
				
				boolean signGlossURL = false;
				// The following bit is for removing the 'gloss' prefix of an entry id
		    	if(entryId != null && entryId.startsWith("gloss")) {
		    		entryId = entryId.substring(5);
		    		signGlossURL = true;
		    	}
		    	
				if (entryId != null && !entryId.isBlank()) {
					CVEntry entry = cv.getEntrybyId(entryId);
					if (entry != null) {
						ExternalReference extRef1 = entry.getExternalRef();
						if (extRef1 != null && extRef1.getReferenceType() == ExternalReference.RESOURCE_URL) {
							String extRefString = extRef1.getValue();
							
							if (signGlossURL) {
								// the following construction of a URL is project specific
								if(!extRefString.endsWith("/")) {
									extRefString = extRefString + "/";
								}
								extRefString = extRefString + entryId;
							}
							
							extRefURL = extRefString;
						}
						// else try entry description or entry value
						else if (UrlOpener.maybeLink(entry.getDescription(cv.getDefaultLanguageIndex()), false)) {
							extRefURL = entry.getDescription(cv.getDefaultLanguageIndex());
						} else if (UrlOpener.maybeLink(entry.getValue(cv.getDefaultLanguageIndex()), false)) {
							extRefURL = entry.getValue(cv.getDefaultLanguageIndex());
						}
					}
				}
			}
		}
		// 3 from contents
		if (extRefURL == null) {
			String value = annotation.getValue();
			// minimal requirement for a local, possibly relative, file path is
			// that it contains a "." character, much more can not be expected
			if (value.indexOf('.') >= 0) {
				extRefURL = value;
			}
		}
		
		if (extRefURL != null) {
			Path path = null;
			try {
				// assume or try "file" protocol
				path = Path.of(extRefURL);
				
				if (!path.isAbsolute()) {
					String docString = annotation.getTier().getTranscription().getFullPath();
					if (docString.startsWith("file:")) {
						docString = docString.substring(5);
					}
					
					if (docString.startsWith("///") && File.separatorChar != '/') {
						docString = docString.substring(3);
					}
					
					Path docPath = Path.of(docString);
					if (docPath.getParent() != null) {
						path = docPath.getParent().resolve(path);
					}
					path = path.normalize();
				}

				return path.toUri();
			} catch (Throwable t) {}
			// exception occurred
		}
		
		return null;
	}
	
	/**
	 * Creates a {@code URI} from the contents of an annotation. If this fails,
	 * most likely because of a {@code URISyntaxException}, {@code null} is
	 * returned. Any exception is silently ignored.
	 * 
	 * @param annotation the annotation to check
	 * @return the {@link URI} of the annotation value or {@code null}
	 */
	public static URI contentToURI(Annotation annotation) {
		if (annotation == null) return null;

		try {
			return new URI(annotation.getValue());
		} catch (Throwable ex) {
		}
		return null;
	}
	
	/**
	 * Creates a {@code URL} from the contents of an annotation. If this fails,
	 * most likely because of a {@code MalformedURLException}, {@code null} is
	 * returned. Any exception is silently ignored.
	 * 
	 * @param annotation the annotation to check
	 * @return the {@link URL} of the annotation value or {@code null}
	 */
	public static URL contentToURL(Annotation annotation) {
		if (annotation == null) return null;

		try {
			return new URL(annotation.getValue());
		} catch (Throwable ex) {
		}
		return null;
	}
	
	/**
	 * Checks if the annotation references a local external resource.
	 * 
	 * @param annotation the annotation to inspect
	 * @return {@code true} if the annotation has a local external reference of
	 * type {@link ExternalReference#RESOURCE_URL}, a @{@code CVEntry} with
	 * such reference or annotation content that does or could reference a local
	 * resource, {@code false} otherwise
	 */
	public static boolean hasLocalExternalResourceRef(AbstractAnnotation annotation) {
		if (annotation == null) return false;

		String refResURL = annotation.getExtRefValue(ExternalReference.RESOURCE_URL);

		if (refResURL == null) {
			String cvName = annotation.getTier().getLinguisticType().getControlledVocabularyName();
			if (cvName != null) {
				ControlledVocabulary cv = annotation.getTier().getTranscription().getControlledVocabulary(cvName);
				String entryId = annotation.getCVEntryId();
				
				if (entryId != null && !entryId.isBlank()) {
					CVEntry entry = cv.getEntrybyId(entryId);
					if (entry != null) {
						ExternalReference extRef1 = entry.getExternalRef();
						if (extRef1 != null && extRef1.getReferenceType() == ExternalReference.RESOURCE_URL) {
							refResURL = extRef1.getValue();
						} else if (UrlOpener.maybeLink(entry.getDescription(cv.getDefaultLanguageIndex()), false)) {
							refResURL = entry.getDescription(cv.getDefaultLanguageIndex());
						} else if (UrlOpener.maybeLink(entry.getValue(cv.getDefaultLanguageIndex()), false)) {
							refResURL = entry.getValue(cv.getDefaultLanguageIndex());
						}
					}
				}
			}
		}
		
		if (refResURL == null) {
			if (annotation.getValue().indexOf('.') >= 0) {
				refResURL = annotation.getValue();
			}
		}
		
		if (refResURL != null) {
			try {
				// assume or try "file" protocol
				Path path = Path.of(refResURL);
				
				if (!path.isAbsolute()) {
					String docString = annotation.getTier().getTranscription().getFullPath();
					if (docString.startsWith("file:")) {
						docString = docString.substring(5);
					}
					
					if (docString.startsWith("///") && File.separatorChar != '/') {
						docString = docString.substring(3);
					}
					
					Path docPath = Path.of(docString);
					if (docPath.getParent() != null) {
						path = docPath.getParent().resolve(path);
					}
					path = path.normalize();
				}

				File f = path.toFile();
				return f.exists();
			} catch (Throwable t) {}
		}
		
		return false;
	}
	
	/**
	 * Checks if the annotation references a remote or local external 
	 * resource.
	 * 
	 * @param annotation the annotation to inspect
	 * @return {@code true} if the annotation has any type of external resource
	 * reference, {@code false} otherwise
	 */
	public static boolean hasAnyExternalResourceRef(AbstractAnnotation annotation) {
		if (annotation == null) return false;
		
		return hasBrowserLink(annotation) || hasLocalExternalResourceRef(annotation);
	}
	
	/**
	 * Checks whether the CV entry links in any known way to an Internet
	 * resource.
	 * 
	 * @param cvEntry the CV entry
	 * @return {@code true} if at least one such link is found, {@code false}
	 * otherwise
	 */
	public static boolean hasBrowserLink(CVEntry cvEntry) {
		if (cvEntry == null) return false;
		
		BasicControlledVocabulary cv = cvEntry.getParent();
		// 1 external resource property
		ExternalReference extRef = cvEntry.getExternalRef();
		if (extRef != null && extRef.getReferenceType() == ExternalReference.RESOURCE_URL) {
			String baseUrlString = extRef.getValue();
			if (baseUrlString != null && !baseUrlString.equals("")) {
				return true;
			} 
		}
		// 2 entry description
		String descString = cvEntry.getDescription(cv.getDefaultLanguageIndex());
		if (UrlOpener.maybeLink(descString, true)) {
			return true;
		}
		// 3 entry value (unlikely to occur)
		if (UrlOpener.maybeLink(cvEntry.getValue(cv.getDefaultLanguageIndex()), true)) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Checks if the CV entry references a local external resource.
	 * 
	 * @param cvEntry the CV entry to inspect
	 * @return {@code true} if the CV entry has a local external reference of
	 * type {@link ExternalReference#RESOURCE_URL} or a description or value 
	 * that does or could reference a local resource, {@code false} otherwise
	 */
	public static boolean hasLocalExternalResourceRef(CVEntry cvEntry) {
		if (cvEntry == null) return false;
		
		String refResURL = null;
		BasicControlledVocabulary cv = cvEntry.getParent();

		ExternalReference extRef1 = cvEntry.getExternalRef();
		if (extRef1 != null && extRef1.getReferenceType() == ExternalReference.RESOURCE_URL) {
			refResURL = extRef1.getValue();
		} else if (UrlOpener.maybeLink(cvEntry.getDescription(cv.getDefaultLanguageIndex()), false)) {
			refResURL = cvEntry.getDescription(cv.getDefaultLanguageIndex());
		} else if (UrlOpener.maybeLink(cvEntry.getValue(cv.getDefaultLanguageIndex()), false)) {
			refResURL = cvEntry.getValue(cv.getDefaultLanguageIndex());
		}
		
		try {
			// assume or try "file" protocol, assume absolute path
			Path path = Path.of(refResURL);
			File f = path.toFile();
			return f.exists();
		} catch (Throwable t) {}
		
		return false;
	}
	
	/**
	 * Get a {@code http(s)} URI from the CV entry's 
	 * external reference, its description or its value.
	 *  
	 * @param cvEntry the CV entry to check
	 * @return the first URI or {@code null}
	 */
	public static URI getBrowserURIFrom(CVEntry cvEntry) {
		if (cvEntry == null) return null;
		
		BasicControlledVocabulary cv = cvEntry.getParent();
		ExternalReference extRef1 = cvEntry.getExternalRef();
		if (extRef1 != null && extRef1.getReferenceType() == ExternalReference.RESOURCE_URL) {
			String extRefString = extRef1.getValue();
			if (extRefString != null && extRefString.startsWith("http")) {
				try {
					return new URI(extRefString);
				} catch (Throwable t) {
					//
				}
			}
		} 
		// get URI from value or description
		else if (UrlOpener.maybeLink(cvEntry.getDescription(cv.getDefaultLanguageIndex()), true)) {
			try {
				return new URI(cvEntry.getDescription(cv.getDefaultLanguageIndex()));
			} catch (Throwable t) {
				//
			}
		} else if (UrlOpener.maybeLink(cvEntry.getValue(cv.getDefaultLanguageIndex()), true)) {
			try {
				return new URI(cvEntry.getValue(cv.getDefaultLanguageIndex()));
			} catch (Throwable t) {
				//
			}
		}
		
		return null;
	}
	
	/**
	 * Get a local file URI from the CV entry's external reference, 
	 * its description or its value.
	 *  
	 * @param cvEntry the CV entry to check
	 * @return the first URI or {@code null}
	 */
	public static URI getLocalURIFrom(CVEntry cvEntry) {
		if (cvEntry == null) return null;
		
		String refResURL = null;
		BasicControlledVocabulary cv = cvEntry.getParent();

		ExternalReference extRef1 = cvEntry.getExternalRef();
		if (extRef1 != null && extRef1.getReferenceType() == ExternalReference.RESOURCE_URL) {
			refResURL = extRef1.getValue();
		} else if (UrlOpener.maybeLink(cvEntry.getDescription(cv.getDefaultLanguageIndex()), false)) {
			refResURL = cvEntry.getDescription(cv.getDefaultLanguageIndex());
		} else if (UrlOpener.maybeLink(cvEntry.getValue(cv.getDefaultLanguageIndex()), false)) {
			refResURL = cvEntry.getValue(cv.getDefaultLanguageIndex());
		}
		
		try {
			// assume or try "file" protocol, assume absolute path
			Path path = Path.of(refResURL);

			return path.toUri();
		} catch (Throwable t) {}
		
		return null;
	}
	
	private static boolean maybeLink(String linkString, boolean onlyRemote) {
		if (linkString == null || linkString.isBlank()) return false;
		
		if (linkString.startsWith("http")) {
			return true;
		}
		
		if (!onlyRemote && linkString.indexOf('.') >= 0) {
			Path path = null;
			try {
				// assume or try "file" protocol
				path = Path.of(linkString);
				@SuppressWarnings("unused")
				URI uri = path.toUri();
				return true;
			} catch (Throwable t) {}
		}
		return false;
	}
	

	
	/**
	 * Performs a naive test whether the specified string denotes a remote file
	 * (currently assumes http or https protocol) and whether is probably is 
	 * a video or audio file, based on its extension.
	 * 
	 * @param uriString a URL or path as a string
	 * @return {@code true} if the string indicates a remote media file, 
	 * {@code false} otherwise
	 */
	public static boolean isRemoteAVMediaLink(String uriString) {
		if (uriString == null || uriString.isBlank()) return false;
		// remote only checks for http/https
		uriString = uriString.toLowerCase();
		if (uriString.startsWith("http")) {
			for (String ext : FileExtension.MISC_VIDEO_EXT) {
				if (uriString.endsWith(ext)) {
					return true;
				}
			}
			for (String ext : FileExtension.MISC_AUDIO_EXT) {
				if (uriString.endsWith(ext)) {
					return true;
				}
			}
			
		}
		return false;
	}
	
	/**
	 * Performs a naive test whether the specified string denotes a remote file
	 * (currently assumes http or https protocol) and whether is probably is 
	 * a video file, based on its extension.
	 * 
	 * @param uriString a URL or path as a string
	 * @return {@code true} if the string indicates a remote video file, 
	 * {@code false} otherwise
	 */
	public static boolean isRemoteVideoLink(String uriString) {
		if (uriString == null || uriString.isBlank()) return false;
		// remote only checks for http/https
		uriString = uriString.toLowerCase();
		if (uriString.startsWith("http")) {
			for (String ext : FileExtension.MISC_VIDEO_EXT) {
				if (uriString.endsWith(ext)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * A runnable to open a {@code URI} in the system's default mail
	 * application or in the default browser.
	 */
	static class UrlOpenerRunnable implements Runnable {
		URI uri;

		public UrlOpenerRunnable(URI uri) {
			super();
			this.uri = uri;
		}

		@Override
		public void run() {
			if (uri.isOpaque() && uri.getScheme().equals("mailto:")) {
				try {
					Desktop.getDesktop().mail(uri);
				} catch (IOException e) {
					if (ClientLogger.LOG.isLoggable(Level.WARNING)) {
						ClientLogger.LOG.log(Level.WARNING, 
								"Error opening mail application: " + e.getMessage());
					}
					//e.printStackTrace();
				}
			} else {
				try {
					Desktop.getDesktop().browse(uri);
				} catch (IOException e) {
					if (ClientLogger.LOG.isLoggable(Level.WARNING)) {
						ClientLogger.LOG.log(Level.WARNING,
								"Error opening webpage: " + e.getMessage());
					}
					//e.printStackTrace();
				}
			}	
		}		
	}
	
	/**
	 * A runnable to open a local file in the system's default application.
	 */
	static class FileOpenerRunnable implements Runnable {
		File file;
		
		public FileOpenerRunnable(File file) {
			super();
			this.file = file;
		}

		@Override
		public void run() {
			try {
				Desktop.getDesktop().open(file);
			} catch (Exception ex) {
				if (ClientLogger.LOG.isLoggable(Level.WARNING)) {
					ClientLogger.LOG.log(Level.WARNING,
							"Error opening file in application: " + ex.getMessage());
				}
			} catch (Throwable t) {
				if (ClientLogger.LOG.isLoggable(Level.WARNING)) {
					ClientLogger.LOG.log(Level.WARNING,
							"Error opening file in application: " + t.getMessage());
				}
			}			
		}
	}
	
 }
