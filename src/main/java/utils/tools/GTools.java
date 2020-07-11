package utils.tools;

import com.google.common.base.Charsets;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.grandtheftmc.jedisnew.NewJedisManager;
import org.json.JSONObject;
import utils.MembersCache;
import utils.confighelpers.Config;
import utils.console.Logs;
import utils.selfdata.ChannelIdData;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static utils.console.Logs.log;

public class GTools {

    public static JDA jda;
    public static Guild guild;
    public static MineStat gtm;
    public static NewJedisManager jedisManager;
    public static final Random RANDOM = new Random();
    private static List<Member> members = new ArrayList<>();

    public static List<Member> getMembers() {
        return members;
    }

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
        return MembersCache.getUser(Long.parseLong(id)).orElse(null);
    }

    public static User userById (long id) {
        return MembersCache.getUser(id).orElse(null);
    }

    public static void updateOnlinePlayers() {
        VoiceChannel channel = jda.getVoiceChannelById(ChannelIdData.get().getPlayerCountChannelId());

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
                    sentMsg.delete().queueAfter(Config.get().getMsgDeleteTime(), TimeUnit.SECONDS)
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
                sentMsg.delete().queueAfter(Config.get().getMsgDeleteTime(), TimeUnit.SECONDS)
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

    public static ScheduledFuture runTaskTimer(Runnable task, int startDelay, int period) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        return executor.scheduleAtFixedRate(task, startDelay, period, TimeUnit.MILLISECONDS);
    }

    public static ScheduledFuture runDelayedTask(Runnable task, int startDelay) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        return executor.schedule(task, startDelay, TimeUnit.MILLISECONDS);
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

    public static String replaceLast(String text, String regex, String replacement) {
        return text.replaceFirst("(?s)(.*)" + regex, "$1" + replacement);
    }

    public static String convertSpecialChar(String string) {
        return new String(string.getBytes(), Charsets.US_ASCII);
    }

    public static String stringFromArgsAfter (String[] args, int from) {
        StringBuilder sb = new StringBuilder();
        for (int i = from ; i < args.length ; i++) {
            sb.append(args[i]);
            if (args.length != i + 1) sb.append(" ");
        }
        return sb.toString();
    }

}
