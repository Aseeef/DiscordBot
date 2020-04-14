package Utils.tools;

import Utils.Config;
import Utils.Rank;
import Utils.SelfData;
import commands.Commands;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static Utils.tools.Logs.log;

public class GTools {

    public static JDA jda;
    public static MineStat gtm;

    // Checks if its is a specific command
    public static boolean isCommand(String msg, User user, Commands command) {
        String[] args = msg.toLowerCase().split(" ");
        return args[0].equals(Config.get().getCommandPrefix()+command.name().toLowerCase()) &&
                !user.isBot();
    }

    // Checks if its any command
    public static boolean isCommand(String msg, User user) {
        // If the user is a bot, its not a command
        if (user.isBot())
            return false;
        else return msg.toLowerCase().startsWith(Config.get().getCommandPrefix());
    }

    public static boolean hasRolePerms(Member member, Rank role) {
        // Check perms
        boolean roleMatch = false;
        List<Role> memberRoles = member.getRoles();

        for (Role r : role.r()) {
            if (memberRoles.contains(r)) {
                roleMatch = true;
                break;
            }
        }

        return roleMatch;
    }

    public static String[] getArgs(String msg) {
        return msg.replaceFirst("/[^ ]+ ", "").split(" ");
    }

    public static User userById (String id) {
        return jda.retrieveUserById(id).complete();
    }

    public static void updateOnlinePlayers() {
        VoiceChannel channel = jda.getVoiceChannelById(SelfData.get().getPlayerCountChannelId());

        if (channel != null) {
            GTools.gtm.refresh();
            String msg = new MessageBuilder()
                    .append("\uD83E\uDD3C Online Players: ")
                    .append(gtm.getCurrentPlayers())
                    .build().getContentRaw();
            log("Updating Online Player count to " + gtm.getCurrentPlayers() + "...");
            channel.getManager().setName(msg).queue();
        } else {
            log("Failed to updating online player count because Player count channel was not set", Logs.WARNING);
        }
    }

    public static void sendThenDelete(TextChannel channel, Message msg) {
        channel.sendMessage(msg).queue( (sentMsg) ->
                sentMsg.delete().queueAfter(Config.get().getDeleteTime(), TimeUnit.SECONDS)
        );
    }

    public static void sendThenDelete(TextChannel channel, String msg) {
        channel.sendMessage(msg).queue( (sentMsg) ->
                sentMsg.delete().queueAfter(Config.get().getDeleteTime(), TimeUnit.SECONDS)
        );
    }

}
