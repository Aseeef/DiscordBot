package utils.tools;

import com.google.common.base.Charsets;
import me.kbrewster.exceptions.APIException;
import me.kbrewster.exceptions.InvalidPlayerException;
import me.kbrewster.mojangapi.MojangAPI;
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
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static utils.console.Logs.log;

public class GTools {

    public static JDA jda;
    public static Guild guild;
    public static MineStat gtm;
    public static NewJedisManager jedisManager;
    public static final Random RANDOM = new Random();
    private static final List<Member> members = new ArrayList<>();

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

    public static String joinArgsAfter(String[] args, int index) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0 ; i < args.length ; i++) {
            if (i < index) continue;
            if (i != index) sb.append(" ");
            sb.append(args[i]);
        }
        return sb.toString();
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

    public static List<String> getAllUsernames(UUID uuid) {
        try {
            List<String> nameHist = new ArrayList<>();
            MojangAPI.getNameHistory(uuid).forEach( (name) -> nameHist.add(name.getName()));
            return nameHist;
        } catch (InvalidPlayerException | NullPointerException e) {
            GTools.printStackError(e);
        }
        return null;
    }

    public static List<String> getAllUsernames(String name) {
        UUID uuid = getUUID(name).orElse(null);
        if (uuid == null) return null;
        else return getAllUsernames(uuid);
    }

    public static Optional<UUID> getUUID(String userName) {
        try {
            return Optional.of(MojangAPI.getUUID(userName));
        } catch (IOException | InvalidPlayerException | APIException | NullPointerException e) {
            GTools.printStackError(e);
        }
        return Optional.empty();
    }

    public static String getSkullSkin (UUID uuid) {
        String stringUUID = uuid.toString().replace("-", "");
        return "https://crafatar.com/avatars/" + stringUUID;
    }

    public static Optional<String> getUsername(UUID uuid) {
        try {
            return Optional.of(MojangAPI.getUsername(uuid));
        } catch (IOException | InvalidPlayerException | APIException | NullPointerException e) {
            GTools.printStackError(e);
        }
        return Optional.empty();
    }

    public static String epochToDate(long date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return sdf.format(new Date(date));
    }

    public static String epochToTime(long time) {
        double months = time / 30D / 24D / 60D / 60D / 1000D;
        double days = (months - Math.floor(months)) * 30;
        double hours = (days - Math.floor(days)) * 24;
        double minutes = (hours - Math.floor(hours)) * 60;
        double seconds = (minutes - Math.floor(minutes)) * 60;

        long monthR = (long) Math.floor(months);
        long daysR = (long) Math.floor(days);
        long hoursR = (long) Math.floor(hours);
        long minutesR = (long) Math.floor(minutes);
        long secondsR = Math.round(seconds);

        String timeFormatted = ((monthR == 0 ? "" : monthR + " month(s), ") + (daysR == 0 ? "" : daysR + " day(s), ") + (hoursR == 0 ? "" : hoursR + " hour(s), ") + (minutesR == 0 ? "" : minutesR + " minute(s), ") + (secondsR == 0 ? "" : secondsR + " second(s), "));

        return timeFormatted.length() != 0 ? timeFormatted.substring(0, timeFormatted.length() - 2) : timeFormatted;
    }

    public static Optional<Emote> getEmote(String s) {
        Pattern pattern = Pattern.compile("<:.{1,32}:([0-9]{18})>");
        Matcher matcher = pattern.matcher(s);
        if (matcher.find()) {
            return Optional.ofNullable(jda.getEmoteById(matcher.group(1)));
        }
        return Optional.empty();
    }

}
