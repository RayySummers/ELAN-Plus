package mpi.eudico.server.corpora.clomimpl.json;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import mpi.eudico.client.annotator.Constants;
import static mpi.eudico.server.corpora.util.ServerLogger.LOG;


/**
 * Singleton class to get the Database connection object.
 * 
 */
public class DBConnection {
	private static Connection connection = null;
	/** The path to a default database file or folder */
	public static final String DB_FILEPATH = Constants.ELAN_DATA_DIR + Constants.FILESEPARATOR + Constants.DB_DIR
			+ Constants.FILESEPARATOR + "WA_MAPPER";

	// load the driver
	static {
		try {
			Class.forName("org.hsqldb.jdbc.JDBCDriver");
		} catch (ClassNotFoundException e) {
			LOG.severe("ERROR: failed to load HSQLDB JDBC driver.");
		}
	}

	/**
	 * Constructor, not to be used.
	 */
	private DBConnection() {
		super();
	}

	/**
	 * Static method which returns the connection object and if the connection
	 * object is closed or {@code null} then calls the method which creates the
	 * new connection.
	 * 
	 * @return the connection object
	 */
	public static Connection getDBConnection() {
		try {
			if (connection != null && !connection.isClosed()) {
				return connection;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return getConnection();
	}

	/**
	 * Method to create a new connection object from the Driver Manager.
	 * 
	 * @return a new connection object
	 */
	private static Connection getConnection() {

		try {
			connection = DriverManager.getConnection("jdbc:hsqldb:file:" + DB_FILEPATH, "SA", "");
		} catch (SQLException e) {
			LOG.severe("ERROR: SQL Exception while trying to get the connection object");
		}
		return connection;
	}

	
	
	/**
	 * Closes the database connection if it is not {@code null}.
	 */
	public static void closeDBConnection() {

		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException e) {
			LOG.severe("ERROR: SQL Exception while trying to close the connection object");
		}
	}

}
