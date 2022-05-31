package net.grandtheftmc.discordbot.utils;

import com.google.common.base.Charsets;
import me.kbrewster.exceptions.APIException;
import me.kbrewster.exceptions.InvalidPlayerException;
import me.kbrewster.mojangapi.MojangAPI;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.grandtheftmc.discordbot.GTMBot;
import net.grandtheftmc.simplejedis.SimpleJedisManager;
import org.json.JSONObject;
import net.grandtheftmc.discordbot.utils.confighelpers.Config;
import net.grandtheftmc.discordbot.utils.console.Logs;
import net.grandtheftmc.discordbot.utils.selfdata.ChannelIdData;
import net.grandtheftmc.discordbot.utils.tools.MineStat;

import javax.annotation.Nullable;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.grandtheftmc.discordbot.utils.console.Logs.log;

public class Utils {

    public static final Random RANDOM = new Random();

    public static final String[] wiseQuotes = {
            "If we all work together we can make poor people come!",
            "If we all wash our hands, we can make nuclear missiles collapse under its own weight.",
            "Betrayal is in opposition to the World Health Organization.",
            "Civilization is a movie where the villain is insanity.",
            "When you're pretending to be somebody you're not, remember that you too will become an eternal flower.",
            "Aim lower. It's never too late to do it.",
            "A tattoo is all you need.",
            "Creating art can be like a fantastic commute that's hard to get around.",
            "Work in an office. Brush your teeth. Keep reminding yourself that everything happens for a reason. Ignore the inevitable.",
            "Boredom is a young woman dancing alone.",
            "Relying on an unemployed person to live like an arms dealer is pretty much as immortal as you get.",
            "How can you ensure yourself that an astronaut isn't a wife? A wife never picks up the check.",
            "Remember that you are hurting inside and remember to close your eyes when you get a turtle.",
            "Life is not a fairy tale. If you loose a shoe at midnight, you're drunk...",
            "Always be yourself. Unless you can be a unicorn in which case you should probably go ahead and be that.",
            "Use the strobe function to disorientate your attacker.",
            "Don't you think that a sperm whale can alter the way you see fear itself someday? Think about that one.",
            "If we pull ourselves together we can make stock photos mainstream! So what are you waiting for? Apply to your local McDonalds now!",
            "Always let the things you question get in the way of the things you hate.",
            "Before a donkey, comes a cake.",
            "Never let an whale tell you what to do.",
            "Understand that tomorrow is the first day of the rest of your life.",
            "If burger king burger, then is it not somebody else's foot fungus?",
            "How can mirrors be real if our eyes aren't real? Make sure to think about that one when you go to bed tonight.",
            "Between happiness and a finger is an insect.",
            "Psychology defines fearlessness as hurting oneself on purpose and still managing to surprise.",
            "It isn’t pollution that is hurting the environment, it’s the impurities in our air and water that are doing it.",
            "If a cricketer, for example, suddenly decided to go into a school and batter a lot of people to death with a cricket bat, which he could do very easily, I mean, are you going to ban cricket bats?",
            "Will the communist prevent the golden age of television? Think about that one before you eat.",
            "What criminalization that you bring when you actualize the sheep in the vicinity of the bisector?"
    };

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

    public static @Nullable User userById (String id) {
        return MembersCache.getUser(Long.parseLong(id)).orElse(null);
    }

    public static @Nullable User userById (long id) {
        return MembersCache.getUser(id).orElse(null);
    }

    public static void updateOnlinePlayers() {
        VoiceChannel channel = GTMBot.getJDA().getVoiceChannelById(ChannelIdData.get().getPlayerCountChannelId());

        if (channel == null) {
            log("Failed to updating online player count because Player count channel was not set", Logs.WARNING);
            return;
        }

        GTMBot.getMineStat().refresh();

        if (!GTMBot.getMineStat().isServerUp()) {
            String msg = new MessageBuilder()
                    .append("\uD83E\uDD3C Server is Offline!")
                    .build().getContentRaw();
            log("Failed to updating online player count server is offline", Logs.WARNING);
            channel.getManager().setName(msg).complete();
            return;
        }

        String msg = new MessageBuilder()
                .append("\uD83E\uDD3C Online Players: ")
                .append(GTMBot.getMineStat().getCurrentPlayers())
                .build().getContentRaw();
        log("Updating Online Player count to " + GTMBot.getMineStat().getCurrentPlayers() + "...");
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
            channel.sendMessageEmbeds(embed).queue();
        else
        channel.sendMessageEmbeds(embed).queue( (sentMsg) ->
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

    public static File getAsset(String name) {
        return new File("assets", name);
    }

    public static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
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
            Utils.printStackError(e);
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
            Utils.printStackError(e);
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
            Utils.printStackError(e);
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
            Utils.printStackError(e);
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
            return Optional.ofNullable(GTMBot.getJDA().getEmoteById(matcher.group(1)));
        }
        return Optional.empty();
    }

}
