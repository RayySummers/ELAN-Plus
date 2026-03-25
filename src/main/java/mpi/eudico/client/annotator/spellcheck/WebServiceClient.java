package mpi.eudico.client.annotator.spellcheck;

import org.apache.http.HttpHost;

/**
 * Defines a web service client based on Apache's {@code HttpClient}.
 */
public interface WebServiceClient {
	/**
	 * Sets the HttpHost.
	 * 
	 * @param host the {@code HttpHost}
	 */
	public void setHost(HttpHost host);
	
	/**
	 * Returns the HttpHost.
	 * 
	 * @return the {@code HttpHost}
	 */
	public HttpHost getHost();

	/**
	 * Sets the path component of the service {@code URL}.
	 * 
	 * @param path the web service path component
	 */
	public void setPath(String path);
	
	/**
	 * Returns the path component of the service {@code URL}.
	 * 
	 * @return the service path component
	 */
	public String getPath();

	/**
	 * Sets the login name of the user.
	 * 
	 * @param username the user name
	 */
	public void setUsername(String username);
	
	/**
	 * Returns the user's login name.
	 * 
	 * @return the user name
	 */
	public String getUsername();

	/**
	 * Sets the password of the user.
	 * 
	 * @param password the user's password
	 */
	public void setPassword(String password);

	/**
	 * Returns the password of the user.
	 * 
	 * @return the password of the user
	 */
	public String getPassword();
	
	/**
	 * Sets the secure connection flag.
	 * 
	 * @param ssl whether or not this service uses a secure connection
	 */
	public void setSecure(Boolean ssl);
	
	/**
	 * Returns the secure flag.
	 * 
	 * @return if {@code true} secure connections are use
	 */
	public Boolean getSecure();
	
	/**
	 * Supported authentication protocols.
	 */
	enum AuthenticationProtocol {
		/**
		 * Digest access authentication.
		 */
		DIGEST,
		/**
		 * OAuth access delegation.
		 */
		OAUTH
	}
	
	/**
	 * Sets the authentication protocol.
	 * 
	 * @param protocol the protocol to use for authentication
	 */
	public void setAuthenticationProtocol(AuthenticationProtocol protocol);
	
	/**
	 * Returns the protocol to use for authentication.
	 * 
	 * @return the protocol to use for authentication
	 */
	public AuthenticationProtocol getAuthenticationProtocol();
	
	/**
	 * Sets authentication information to be used by the client.
	 * 
	 * @param protocol the {@code AuthenticationProtocol}
	 * @param username the user name
	 * @param password the user's password
	 */
	public void setAuthentication(AuthenticationProtocol protocol, String username, String password);
}
