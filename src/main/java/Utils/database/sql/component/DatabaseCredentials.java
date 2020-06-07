package Utils.database.sql.component;

/**
 * Represents an immutable data object containing information about a connection
 * to the database.
 * 
 * @author sbahr
 */
public class DatabaseCredentials {

	/** The host of the db, ex: example.com */
	private final String host;
	/** The port associated, ex: 3306 */
	private final int port;
	/** The name of the database to use, ex: test_db */
	private final String dbName;
	/** The name of the user that has access to the db, ex: user123 */
	private final String dbUser;
	/** The password for the user, ex: pass123 */
	private final String dbPass;

	/**
	 * Construct a new DatabaseCredentials object.
	 * 
	 * @param host - the host of the db, ex: example.com
	 * @param port - the port for the db, ex: 3306
	 * @param dbName - the name of the db to use, ex: test_db
	 * @param dbUser - the user of the db, ex: user123
	 * @param dbPass - the pass for the user, ex: pass123
	 */
	public DatabaseCredentials(String host, int port, String dbName, String dbUser, String dbPass) {
		this.host = host;
		this.port = port;
		this.dbName = dbName;
		this.dbUser = dbUser;
		this.dbPass = dbPass;
	}

	/**
	 * Construct a new DatabaseCredentials object.
	 * <p>
	 * The port for the host is not defined as an argument and either should be
	 * supplied in the host argument or use a different constructor. If no port
	 * is defined, we'll use the default port.
	 * </p>
	 * 
	 * @param host - the host of the db, ex: example.com
	 * @param dbName - the name of the db to use, ex: test_db
	 * @param dbUser - the user of the db, ex: user123
	 * @param dbPass - the pass for the user, ex: pass123
	 */
	public DatabaseCredentials(String host, String dbName, String dbUser, String dbPass) {
		this(host, -1, dbName, dbUser, dbPass);
	}

	/**
	 * Get the host associated with this database credentials.
	 * <p>
	 * The host URL, ex: www.example.com
	 * </p>
	 * 
	 * @return The host URL for the database.
	 */
	public final String getHost() {
		return host;
	}

	/**
	 * Get the port number for the database.
	 * <p>
	 * The port number could be irrelevant if defined in {@link #getHost()}. If
	 * the port number is -1, use the default port.
	 * </p>
	 * 
	 * @return The port number for the database.
	 */
	public final int getPort() {
		return port;
	}

	/**
	 * Get the name of the database that we are using.
	 * <p>
	 * This is the name of the database, as there can be multiple databases
	 * within one database.
	 * </p>
	 * 
	 * @return The name of the database we are using.
	 */
	public final String getName() {
		return dbName;
	}

	/**
	 * Get the username of the user that has access to the database.
	 * 
	 * @return The name for the user that has access to this database.
	 */
	public final String getUser() {
		return dbUser;
	}

	/**
	 * Get the password of the user that has access to the database.
	 * 
	 * @return The password, associated with the user, that has access to this
	 *         database.
	 */
	public final String getPass() {
		return dbPass;
	}
}
