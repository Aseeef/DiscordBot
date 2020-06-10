package Utils.console;

import Utils.console.commands.ConsoleHelpCommand;
import Utils.console.commands.ConsoleStopCommand;
import Utils.database.sql.BaseDatabase;
import Utils.tools.GTools;

import static Utils.console.Logs.log;
import static Utils.tools.GTools.jda;
import static Utils.tools.GTools.jedisManager;

public class Console {

    public static void loadConsoleCommands() {
        GTools.runAsync(new ConsoleStopCommand());
        GTools.runAsync(new ConsoleHelpCommand());
    }

    public static void loadShutdownHood() {
        Runtime.getRuntime().addShutdownHook(new Thread( () -> {
            // close jda
            if (jda != null) {
                // Shut down JDA
                log("Shutting down discord bot...");
                jda.shutdownNow();
            }
            // Close database pool connection
            log("Closing connections to databases...");
            for (BaseDatabase.Database database : BaseDatabase.Database.values())
                BaseDatabase.getInstance(database).close();
            jedisManager.close();
        }));
    }

}
