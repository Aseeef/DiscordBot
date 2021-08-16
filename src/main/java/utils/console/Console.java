package utils.console;

import utils.console.commands.ConsoleCommand;
import utils.console.commands.listeners.ConsoleHelpCommand;
import utils.console.commands.listeners.ConsoleMemoryCommand;
import utils.console.commands.listeners.ConsoleQueryCommand;
import utils.console.commands.listeners.ConsoleStopCommand;
import utils.database.sql.BaseDatabase;

import java.io.OutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static utils.console.Logs.log;
import static utils.Utils.JDA;
import static utils.Utils.jedisManager;

public class Console {

    //Log color codes
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_WHITE = "\u001B[37m";
    public static final String ANSI_CYAN = "\u001B[36m";

    public static class ErrorStream extends PrintStream {

        public ErrorStream(OutputStream out) {
            super(out);
        }

        @Override
        public void println(String string) {
            String time = DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now());

            String output;
            String coloredOutput;

            // for some unknown reason some of JDA's non-error prints ges to System.err instead .out?
            if (string.contains("INFO JDA") || string.contains("INFO WebSocketClient")) {
                output = "["+time+"] ["+Logs.INFO.n()+"] "+string;
                coloredOutput = ANSI_RESET+ANSI_WHITE+"["+ANSI_CYAN+time+ANSI_WHITE+"] "+
                        ANSI_WHITE+"["+ANSI_RESET+Logs.INFO.c()+Logs.INFO.n()+ANSI_WHITE+"] "+Logs.INFO.c()+string+ANSI_RESET;
            }
            else {
                output = "[" + time + "] [" + Logs.ERROR.n() + "] " + string;
                coloredOutput = ANSI_RESET + ANSI_WHITE + "[" + ANSI_CYAN + time + ANSI_WHITE + "] " +
                        ANSI_WHITE + "[" + ANSI_RESET + Logs.ERROR.c() + Logs.ERROR.n() + ANSI_WHITE + "] " + Logs.ERROR.c() + string + ANSI_RESET;
            }

            Logs.logToFile(output);
            super.println(coloredOutput);
        }
    }

    public static class GeneralStream extends PrintStream {

        public GeneralStream(OutputStream out) {
            super(out);
        }

        @Override
        public void println(String string) {
            String time = DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now());

            Logs type;
            if (string.startsWith("["+Logs.WARNING.n()+"] ")) {
                type = Logs.WARNING;
                string = string.replaceFirst("\\["+Logs.WARNING.n()+"] ", "");
            }
            else if (string.startsWith("["+Logs.INFO.n()+"] ")) {
                type = Logs.INFO;
                string = string.replaceFirst("\\["+Logs.INFO.n()+"] ", "");
            }
            else if (string.startsWith("["+Logs.ERROR.n()+"] ")) {
                type = Logs.ERROR;
                string = string.replaceFirst("\\["+Logs.ERROR.n()+"] ", "");
            }
            else type = Logs.INFO;

            String output = "["+time+"] ["+type.n()+"] "+string;
            Logs.logToFile(output);
            
            String coloredOutput = ANSI_RESET+ANSI_WHITE+"["+ANSI_CYAN+time+ANSI_WHITE+"] "+
                    ANSI_WHITE+"["+ANSI_RESET+type.c()+type.n()+ANSI_WHITE+"] "+type.c()+string+ANSI_RESET;
            super.println(coloredOutput);
        }
    }

    public static void loadConsoleCommands() {
        new ConsoleCommand()
                .registerListener(new ConsoleHelpCommand())
                .registerListener(new ConsoleQueryCommand())
                .registerListener(new ConsoleStopCommand())
                .registerListener(new ConsoleMemoryCommand())
                .init();
    }

    public static void loadShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread( () -> {
            // close jda
            if (JDA != null) {
                // Shut down JDA
                log("Shutting down discord bot...");
                JDA.shutdownNow();
            }
            // Close database pool connection
            log("Closing connections to databases...");
            for (BaseDatabase.Database database : BaseDatabase.Database.values())
                BaseDatabase.getInstance(database).close();
            jedisManager.close();
        }));
    }

}
