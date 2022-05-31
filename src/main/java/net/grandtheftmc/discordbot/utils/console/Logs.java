package net.grandtheftmc.discordbot.utils.console;

import net.grandtheftmc.discordbot.utils.Utils;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public enum Logs {

    INFO("\u001B[32m", "INFO"), // Green Log color
    WARNING("\u001B[33m", "WARNING"), // Yellow Log color
    ERROR("\u001B[91m", "ERROR"); // Red Log color

    private final String c;
    private final String n;

    Logs (String c, String n) {
        this.c = c;
        this.n = n;
    }

    // Color
    public String c() {
        return this.c;
    }

    // Name
    public String n() {
        return this.n;
    }

    public static void log(String msg) {
        log(msg, Logs.INFO);
    }

    public static void log(String msg, Logs type) {
        msg = "["+type.n+"] " + msg;
        System.out.println(msg);
    }

    public static void logToFile(String output) {
        // Save logs to file
        String date = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDateTime.now());
        File dir = new File("logs");
        dir.mkdirs();
        File file = new File(dir, date+".txt");
        writeOutput(file, output);
    }

    public static void logToErrFile (String output) {
        // Save logs to file
        String date = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDateTime.now());
        File dir = new File("logs/error");
        dir.mkdirs();
        File file = new File(dir, date+".txt");
        writeOutput(file, output);
    }

    public static void writeOutput(File file, String output) {
        try {
            // Create new log file if one doesn't exist already
            if (file.createNewFile())
                log("No log file found. Creating new log file...");
            // Log
            BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
            bw.write(output);
            bw.newLine();
            bw.close();
        } catch (IOException e) {
            Utils.printStackError(e);
        }
    }

    //Log color codes
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_WHITE = "\u001B[37m";
    private static final String ANSI_CYAN = "\u001B[36m";

    public static class ErrorStream extends PrintStream {

        public ErrorStream(OutputStream out) {
            super(out);
        }

        @Override
        public void println(Object object) {
            println(object.toString());
        }

        @Override
        public void println(int i) {
            println(String.valueOf(i));
        }

        @Override
        public void println(long i) {
            println(String.valueOf(i));
        }

        @Override
        public void println(float i) {
            println(String.valueOf(i));
        }

        @Override
        public void println(double i) {
            println(String.valueOf(i));
        }

        @Override
        public void println(boolean b) {
            println(String.valueOf(b));
        }

        @Override
        public void println(@NotNull char[] chars) {
            println(Arrays.toString(chars));
        }

        @Override
        public void println(char c) {
            println(String.valueOf(c));
        }

        @Override
        public void println(String string) {
            super.println(generateAndLogCustomOutput(string));
        }

        private String generateAndLogCustomOutput(String string) {
            String time = DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now());

            // for file logging
            String output;
            // for console logging
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
            Logs.logToErrFile(output);

            return coloredOutput;
        }
    }

    public static class GeneralStream extends PrintStream {

        public GeneralStream(OutputStream out) {
            super(out);
        }

        @Override
        public void println(Object object) {
            println(object.toString());
        }

        @Override
        public void println(int i) {
            println(String.valueOf(i));
        }

        @Override
        public void println(long i) {
            println(String.valueOf(i));
        }

        @Override
        public void println(float i) {
            println(String.valueOf(i));
        }

        @Override
        public void println(double i) {
            println(String.valueOf(i));
        }

        @Override
        public void println(boolean b) {
            println(String.valueOf(b));
        }

        @Override
        public void println(@NotNull char[] chars) {
            println(Arrays.toString(chars));
        }

        @Override
        public void println(char c) {
            println(String.valueOf(c));
        }

        @Override
        public void println(String string) {
            super.println(generateAndLogCustomOutput(string));
        }

        private String generateAndLogCustomOutput(String string) {

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

            return coloredOutput;
        }
    }

}
