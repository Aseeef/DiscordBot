package Utils.database.sql;

import redis.clients.jedis.JedisPool;

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
	private BaseDatabase() {
		// Note: DatabaseHandler doesn't have a constructor for a reason
	}

	public static BaseDatabase getInstance(Database database) {
		return database.getInstance();
	}

	public enum Database {
		PLAN (new BaseDatabase()),
		USERS (new BaseDatabase()),
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