package utils.console;

import utils.tools.GTools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static utils.console.Console.*;

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
        msg = "["+type.n+"] " + msg;
        System.out.println(msg);
    }

    public static void logToFile(String output) {
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
            GTools.printStackError(e);
        }
    }

}
