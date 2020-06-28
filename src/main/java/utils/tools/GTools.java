package utils.tools;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.grandtheftmc.jedisnew.NewJedisManager;
import org.json.JSONObject;
import utils.SelfData;
import utils.confighelpers.Config;
import utils.console.Logs;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static utils.console.Logs.log;

public class GTools {

    public static JDA jda;
    public static MineStat gtm;
    public static NewJedisManager jedisManager;
    public static final Random RANDOM = new Random();

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

    public static void sendThenDelete(MessageChannel channel, Message msg, File attachment) {
        // dont delete if private channel
        if (channel instanceof PrivateChannel) {
            MessageAction ma = channel.sendMessage(msg);
            if (attachment != null) ma = ma.addFile(attachment);
            ma.queue();
        }
        else {
            MessageAction ma = channel.sendMessage(msg);
            if (attachment != null) ma = ma.addFile(attachment);
            ma.queue((sentMsg) ->
                    sentMsg.delete().queueAfter(Config.get().getDeleteTime(), TimeUnit.SECONDS)
            );
        }
    }

    public static void sendThenDelete(MessageChannel channel, String msg, File file) {
        sendThenDelete(channel, new MessageBuilder(msg).build(), file);
    }

    public static void sendThenDelete(MessageChannel channel, Message msg) {
        sendThenDelete(channel, msg, null);
    }

    public static void sendThenDelete(MessageChannel channel, String msg) {
        sendThenDelete(channel, new MessageBuilder(msg).build());
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

    public static int randomNumber(int start, int end) {
        return RANDOM.nextInt(end - start + 1) + start;
    }

    public static void printStackError(Throwable e) {
        log(String.valueOf(e.initCause(e.getCause())), Logs.ERROR);
        for (StackTraceElement error : e.getStackTrace())
            log("        at " + error.toString(), Logs.ERROR);
    }

    public static void runAsync(Runnable target) {
        new Thread(target).start();
    }

    public static File getAsset(String name) {
        return new File("assets", name);
    }

    public static JSONObject getJsonFromApi(String url) {
        try (InputStream is = new URL(url).openStream()) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            int cp;
            while ((cp = rd.read()) != -1) {
                sb.append((char) cp);
            }
            return new JSONObject(sb.toString());
        } catch (IOException e) {
            GTools.printStackError(e);
            return null;
        }
    }


}
