package utils.console;

import utils.console.commands.ConsoleCommand;
import utils.console.commands.listeners.ConsoleHelpCommand;
import utils.console.commands.listeners.ConsoleQueryCommand;
import utils.console.commands.listeners.ConsoleStopCommand;
import utils.database.sql.BaseDatabase;

import static utils.console.Logs.log;
import static utils.tools.GTools.jda;
import static utils.tools.GTools.jedisManager;

public class Console {

    public static void loadConsoleCommands() {
        new ConsoleCommand()
                .registerListener(new ConsoleHelpCommand())
                .registerListener(new ConsoleQueryCommand())
                .registerListener(new ConsoleStopCommand())
                .init();
    }

    public static void loadShutdownHook() {
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
