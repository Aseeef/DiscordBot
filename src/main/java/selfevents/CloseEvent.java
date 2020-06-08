package selfevents;

import Utils.database.sql.BaseDatabase;
import Utils.database.redis.OnRedisMessageReceive;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import static Utils.tools.GTools.jedisManager;
import static Utils.tools.Logs.log;

public class CloseEvent extends ListenerAdapter {

    public void onShutdown (ShutdownEvent e) {
        // Close database pool connection
        log("Closing connections to databases...");
        for (BaseDatabase.Database database : BaseDatabase.Database.values())
            BaseDatabase.getInstance(database).close();
        jedisManager.close();
        // Log finished
        log("Bot has now been disabled.");

        System.exit(0);

    }

}
