package net.grandtheftmc.discordbot.utils.database.sql;

import net.grandtheftmc.discordbot.utils.Utils;
import net.grandtheftmc.discordbot.utils.database.sql.component.Database;
import net.grandtheftmc.discordbot.utils.database.sql.component.DatabaseCredentials;
import net.grandtheftmc.discordbot.utils.console.Logs;
import com.zaxxer.hikari.HikariDataSource;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import static net.grandtheftmc.discordbot.utils.console.Logs.log;

/**
 * A generic database handler that holds a HikariCP data source so we can have
 * multiple database connection.
 * 
 * Note: This should be init() with either the Plugin/Config path to load
 * settings, or can be init() with just database credentials, which uses default
 * HikariCP settings.
 * 
 * @author sbahr
 */
public class DatabaseHandler implements Database {

	/** The default MySQL driver */
	private static final String DEFAULT_MYSQL_DRIVER = "com.mysql.jdbc.Driver";
	/** The database credentials */
	private DatabaseCredentials dbCreds;
	/** Data source connection pool from HikariCP */
	private final HikariDataSource hikariSource = new HikariDataSource();

	// NOTE: HikariCP performs best at fixed pool size, minIdle=maxConns
	// https://github.com/brettwooldridge/HikariCP

	/** How many minimum idle connections should we always have (2) */
	protected int minIdle;
	/** How many max connections should exist in pool (2) */
	protected int maxPoolSize;
	/** How long, in millis, we stop waiting for new connection (15 secs) */
	protected int connectionTimeoutMs = 15 * 1000;
	/** How long, in millis, before connections timeout (45 secs) */
	protected int idleTimeoutMs = 45 * 1000;
	/** How long, in millis, this connection can be alive for (30 mins) */
	protected int maxLifetimeMs = 30 * 60 * 1000;
	/** How long, in millis, can a connection be gone from a pool (4 secs) */
	protected int leakDetectionThresholdMs = 4 * 1000;
	/** The ping alive query */
	protected String connectionTestQuery = "SELECT 1";
	/** Should the connection cache prepared statements */
	protected boolean cachePreparedStatements = true;
	/** Number of prepared statements to cache per connection */
	protected int preparedStatementCache = 250;
	/** Max number of prepared statements to have */
	protected int maxPreparedStatementCache = 2048;
	/** The log writer for Hikari */
	protected PrintWriter logWriter = new PrintWriter(System.out);

	public DatabaseHandler(int poolSize) {
		this.maxPoolSize = poolSize;
		this.minIdle = poolSize;
	}

	/**
	 * Initialize the handler with the specified database credentials.
	 * <p>
	 * Sets up the configuration for the connection pool and default settings.
	 * </p>
	 * 
	 * @param dbCreds - the credentials for the database
	 * @param driver - the driver class
	 */
	public void init(DatabaseCredentials dbCreds, String driver) {
		this.dbCreds = dbCreds;

		// set the driver name for the connection driver
		hikariSource.setDriverClassName(driver);

		// assume host/port combo together, or could just be without port
		String connURL = dbCreds.getHost();

		// if a port is defined
		if (dbCreds.getPort() > 0) {
			connURL = dbCreds.getHost() + ":" + dbCreds.getPort();
		}

		// set the jdbc url, note the character encoding
		// https://stackoverflow.com/questions/3040597/jdbc-character-encoding
		hikariSource.setJdbcUrl("jdbc:mysql://" + connURL + "/" + dbCreds.getName() + "?characterEncoding=UTF-8");

		// set user/pass
		hikariSource.setUsername(dbCreds.getUser());
		hikariSource.setPassword(dbCreds.getPass());

		/** General conf settings for hikari */
		// works best when minIdle=maxPoolSize
		hikariSource.setMinimumIdle(minIdle);
		hikariSource.setMaximumPoolSize(maxPoolSize);

		// how long to wait, for a new connection
		hikariSource.setConnectionTimeout(connectionTimeoutMs);
		// how long before idle connection is destroyed
		hikariSource.setIdleTimeout(idleTimeoutMs);
		// how long can a connection exist
		hikariSource.setMaxLifetime(maxLifetimeMs);
		// how long connection is away from a pool before saying uh oh
		hikariSource.setLeakDetectionThreshold(leakDetectionThresholdMs);
		// test query to confirm alive
		hikariSource.setConnectionTestQuery(connectionTestQuery);
		// should we cache prepared statements
		hikariSource.addDataSourceProperty("cachePrepStmts", "" + cachePreparedStatements);
		// the size of the prepared statement cache
		hikariSource.addDataSourceProperty("prepStmtCacheSize", "" + preparedStatementCache);
		// the maximum cache limit
		hikariSource.addDataSourceProperty("prepStmtCacheSqlLimit", "" + maxPreparedStatementCache);

		// MUST set log writer
		try {
			hikariSource.setLogWriter(new PrintWriter(System.out));
		}
		catch (SQLException e) {
			Utils.printStackError(e);
		}

		log("A connection to the database " + this.getCredentials().getName() + " has successfully been established!");

	}

	/**
	 * Initialize the database handler given the credentials.
	 * 
	 * @param credentials - the login details to this database
	 */
	protected void init(DatabaseCredentials credentials) {
		this.init(credentials, DEFAULT_MYSQL_DRIVER);
	}

	/**
	 * Load the settings for HikariCP from the yaml utils.config and stores them
	 * locally in the object, then initializes the database handler.
	 */
	public void init(String host, int port, String dbName, String user, String pass) {


		// create database credentials
		DatabaseCredentials creds = new DatabaseCredentials(host, port, dbName, user, pass);

		// initialize hikari cp
		init(creds);
	}

	/**
	 * Close HikariCP connection pool, and all the connections.
	 * <p>
	 * Note: This should be called whenever the plugin turns off!
	 * </p>
	 */
	public void close() {
		if (hikariSource != null && !hikariSource.isClosed()) {
			hikariSource.close();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DatabaseCredentials getCredentials() {
		return dbCreds;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Connection getConnection() {
		if (hikariSource != null) {
			try {
				Connection conn = hikariSource.getConnection();
				return conn;
			}
			catch (Exception e) {
				log("[DatabaseHandler] Unable to grab a connection from the connection pool!", Logs.ERROR);
				Utils.printStackError(e);
			}
		}

		return null;
	}
}
