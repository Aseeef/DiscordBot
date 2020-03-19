package Utils.tools;

import Utils.Config;
import Utils.MineStat;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GTools {

    public static JDA jda;
    public static MineStat gtm;

    public static boolean isCommand(GuildMessageReceivedEvent event, String command) {
        String[] args = event.getMessage().getContentRaw().toLowerCase().split(" ");
        return args[0].equals(Config.get().getCommandPrefix()+command.toLowerCase()) &&
                !event.getAuthor().isBot();
    }

    public static boolean isCommand(GuildMessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().toLowerCase().split(" ");
        String command = args[0].toLowerCase().replaceFirst(Config.get().getCommandPrefix(), "");
        // If author isn't a bot and command matches one of the commands, return true
        if (!event.getAuthor().isBot())
            return command.equals("suggestion") ||
                    command.equals("playercount");
        else return false;
    }

    public static User userById (String id) {
        return jda.retrieveUserById(id).complete();
    }

    public static String onlinePlayersMsg() {
        Message msg = new MessageBuilder()
        .append("\uD83E\uDD3C Online Players: ")
        .append(gtm.getCurrentPlayers())
        .build();
        return msg.getContentRaw();
    }

    public static void log(String msg) {
        //Log color codes
        final String ANSI_RESET = "\u001B[0m";
        final String ANSI_GREEN = "\u001B[32m";
        final String ANSI_BLUE = "\u001B[34m";
        final String ANSI_CYAN = "\u001B[36m";

        // Log with time stamp
        String time = DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now());
        String output = "["+time+"] "+msg;
        String coloredOutput = ANSI_RESET+ANSI_BLUE+"["+ANSI_CYAN+time+ANSI_BLUE+"] "+ANSI_GREEN+msg+ANSI_RESET;
        System.out.println(coloredOutput);

        // Save logs to file
        String date = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDateTime.now());
        File file = new File("logs/", date+".txt");

        try {
            // Create new log file if one doesn't exist already
            file.createNewFile();
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
