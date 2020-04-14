package Utils.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public enum Logs {

    INFO("\u001B[32m", "INFO"), // Green Log color
    WARNING("\u001B[33m", "WARNING"), // Yellow Log color
    ERROR("\u001B[91m", "ERROR"); // Red Log color

    private String c;
    private String n;

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
        //Log color codes
        final String ANSI_RESET = "\u001B[0m";
        final String ANSI_WHITE = "\u001B[37m";
        final String ANSI_CYAN = "\u001B[36m";

        // Log with time stamp
        String time = DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now());

        String output = "["+time+"] ["+type+"] "+msg;
        String coloredOutput = ANSI_RESET+ANSI_WHITE+"["+ANSI_CYAN+time+ANSI_WHITE+"] "+
                ANSI_WHITE+"["+ANSI_RESET+type.c+type.n+ANSI_WHITE+"] "+type.c+msg+ANSI_RESET;
        System.out.println(coloredOutput);

        // Save logs to file
        String date = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDateTime.now());
        File file = new File("logs/", date+".txt");

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
            e.printStackTrace();
        }

    }

}
