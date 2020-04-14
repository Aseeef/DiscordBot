package selfevents;

import Database.BaseDatabase;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import static Utils.tools.Logs.log;

public class CloseEvent extends ListenerAdapter {

    public void onShutdown (ShutdownEvent e) {
        // Close database pool connection
        log("Closing database connection...");
        BaseDatabase.getInstance().close();
        // Log finished
        log("Bot has now been disabled.");

        System.exit(0);

    }

}
