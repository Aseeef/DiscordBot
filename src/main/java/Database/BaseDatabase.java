package Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * A generic database handler that acts as a singleton so we can reference it
 * anywhere.
 * 
 * @author sbahr
 */
public class BaseDatabase extends DatabaseHandler {

	/** Singleton instance for this class */
	private static BaseDatabase instance;

	/**
	 * Private constructor as singleton's cannot be instantiated.
	 */
	private BaseDatabase() {
		// Note: DatabaseHandler doesn't have a constructor for a reason
	}

	/**
	 * Get the singleton instance of this class.
	 * <p>
	 * This allows you to call {@link #getConnection()}.
	 * </p>
	 * 
	 * @return The instance of this database.
	 */
	public static BaseDatabase getInstance() {
		if (instance == null) {
			instance = new BaseDatabase();
		}

		return instance;
	}

	public static boolean runCustomQuery(String query) {
		try (Connection connection = getInstance().getConnection()) {
			try (PreparedStatement statement = connection.prepareStatement(query)) {
				statement.execute();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}
}