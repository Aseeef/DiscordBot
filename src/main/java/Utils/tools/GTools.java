package Utils.tools;

import Utils.Config;
import Utils.Rank;
import Utils.SelfData;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.grandtheftmc.jedisnew.NewJedisManager;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static Utils.tools.Logs.log;

public class GTools {

    public static JDA jda;
    public static MineStat gtm;
    public static NewJedisManager jedisManager;

    // Checks if its is a specific command
    public static boolean isCommand(String msg, User user, String command) {
        String[] args = msg.toLowerCase().split(" ");
        return args[0].equals(Config.get().getCommandPrefix() + command.toLowerCase()) &&
                !user.isBot();
    }

    // Checks if its any command
    public static boolean isCommand(String msg, User user) {
        // If the user is a bot, its not a command
        if (user.isBot())
            return false;
        else return msg.toLowerCase().startsWith(Config.get().getCommandPrefix());
    }

    public static User userById (String id) {
        return jda.retrieveUserById(id).complete();
    }

    public static void updateOnlinePlayers() {
        VoiceChannel channel = jda.getVoiceChannelById(SelfData.get().getPlayerCountChannelId());

        if (channel == null) {
            log("Failed to updating online player count because Player count channel was not set", Logs.WARNING);
            return;
        }

        GTools.gtm.refresh();

        if (!gtm.isServerUp()) {
            String msg = new MessageBuilder()
                    .append("\uD83E\uDD3C Server is Offline!")
                    .build().getContentRaw();
            log("Failed to updating online player count server is offline", Logs.WARNING);
            channel.getManager().setName(msg).queue();
            return;
        }

        String msg = new MessageBuilder()
                .append("\uD83E\uDD3C Online Players: ")
                .append(gtm.getCurrentPlayers())
                .build().getContentRaw();
        log("Updating Online Player count to " + gtm.getCurrentPlayers() + "...");
        channel.getManager().setName(msg).queue();
    }

    public static void sendThenDelete(MessageChannel channel, Message msg) {
        // dont delete if private channel
        if (channel instanceof PrivateChannel)
            channel.sendMessage(msg).queue();
        else
        channel.sendMessage(msg).queue( (sentMsg) ->
                sentMsg.delete().queueAfter(Config.get().getDeleteTime(), TimeUnit.SECONDS)
        );
    }

    public static void sendThenDelete(MessageChannel channel, String msg) {
        // dont delete if private channel
        if (channel instanceof PrivateChannel)
            channel.sendMessage(msg).queue();
        else
        channel.sendMessage(msg).queue( (sentMsg) ->
                sentMsg.delete().queueAfter(Config.get().getDeleteTime(), TimeUnit.SECONDS)
        );
    }

    public static void sendThenDelete(MessageChannel channel, MessageEmbed embed) {
        // dont delete if private channel
        if (channel instanceof PrivateChannel)
            channel.sendMessage(embed).queue();
        else
        channel.sendMessage(embed).queue( (sentMsg) ->
                sentMsg.delete().queueAfter(Config.get().getDeleteTime(), TimeUnit.SECONDS)
        );
    }

    public static void printStackError(Throwable e) {
        log(String.valueOf(e.initCause(e.getCause())), Logs.ERROR);
        for (StackTraceElement error : e.getStackTrace())
            log("        at " + error.toString(), Logs.ERROR);
    }

    public static void runAsync(Runnable target) {
        new Thread(target).start();
    }

}
