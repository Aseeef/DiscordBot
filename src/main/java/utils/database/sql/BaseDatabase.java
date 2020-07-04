package utils.database.sql;

/**
 * A generic database handler that acts as a singleton so we can reference it
 * anywhere.
 * 
 * @author sbahr
 */
public class BaseDatabase extends DatabaseHandler {

	/**
	 * Private constructor as singleton's cannot be instantiated.
	 */
	private BaseDatabase(int poolSize) {
		super(poolSize);
		// Note: DatabaseHandler doesn't have a constructor for a reason
	}

	public static BaseDatabase getInstance(Database database) {
		return database.getInstance();
	}

	public enum Database {
		/** Database for Plan */
		PLAN (new BaseDatabase(1)),
		/** Main GTM Database */
		USERS (new BaseDatabase(2)),
		/** Litebans database */
		BANS (new BaseDatabase(1)),
		/** Xenforo Database */
		XEN (new BaseDatabase(1))
		;

		BaseDatabase instance;
		Database(BaseDatabase instance) {
			this.instance = instance;
		}
		private BaseDatabase getInstance() {
			return instance;
		}
	}

}