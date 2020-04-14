package selfevents;

import Utils.tools.Logs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static Utils.tools.GTools.jda;
import static Utils.tools.Logs.log;

public class ConsoleCommand implements Runnable {

    public void run() {
        try {
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(System.in));

            String command = "null";

            while (!command.equals("stop")) {
                command = reader.readLine();
            }

            // Once command matches, close reader
            reader.close();

            if (jda != null) {
                // Shut down JDA
                jda.shutdownNow();
            }
            else
                System.exit(0);

        } catch (IOException e) {
            log(String.valueOf(e.initCause(e.getCause())), Logs.ERROR);
            for (StackTraceElement error : e.getStackTrace())
                log("        at " + error.toString(), Logs.ERROR);
        }
    }

}
