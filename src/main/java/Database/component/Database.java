package Database.component;

import java.sql.Connection;

/**
 * An interface that represents a database (and it's credentials).
 * 
 * @author sbahr
 */
public interface Database {

	/**
	 * Get the credentials for the database.
	 * 
	 * @return The credentials for the database.
	 */
	DatabaseCredentials getCredentials();

	/**
	 * Get the connection for the database.
	 * 
	 * @return The connection for the database.
	 */
	Connection getConnection();

}
