package commands;

import Utils.Rank;
import Utils.SelfData;
import Utils.tools.GTools;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import static Utils.tools.GTools.*;

public class PlayerCountCommand extends ListenerAdapter {

    public void onGuildMessageReceived(GuildMessageReceivedEvent e) {

        String msg = e.getMessage().getContentRaw();
        Member member = e.getMember();
        User user = e.getAuthor();
        assert member != null;

        if (GTools.isCommand(msg, user, Commands.PLAYERCOUNT) &&
                hasRolePerms(member, Commands.PLAYERCOUNT.rank())
            ) {

            String[] args = getArgs(msg);
            TextChannel channel = e.getChannel();

            // If there are no command arguments send sub command help list
            if (args.length == 0) {
                // Send msg then delete after defined time in config
                sendThenDelete(channel, getPlayerCountHelpMsg());
            }

            // Suggestions SetChannel Command
            else if (args[0].equalsIgnoreCase("setchannel")) {

                VoiceChannel playerCountChannel = GTools.jda.getVoiceChannelById(Long.parseLong(args[1]));

                // If its a valid voice channel id
                if (playerCountChannel != null) {

                    // Set as player count channel
                    SelfData.get().setPlayerCountChannelId(Long.parseLong(args[1]));

                    // Updates online players
                    updateOnlinePlayers();

                    // Send success msg
                    sendThenDelete(channel, "**<`"+playerCountChannel.getIdLong()+"`>"+
                            " has been set as the Player Count channel!**");

                }

                // If channel id is invalid
                else {
                    sendThenDelete(channel, "**Invalid voice channel Id! Channel not set.**");
                }

            }

            // If none of the sub arguments match
            else {
                // Send msg then delete after defined time in config
                sendThenDelete(channel, getPlayerCountHelpMsg());
            }

        }

    }

    private static Message getPlayerCountHelpMsg() {
        return new MessageBuilder()
                .append("> **Please enter a valid command argument:**\n")
                .append("> `/PlayerCount SetChannel <ID>` - *Displays GTM's player count at the given channel*\n")
                .build();
    }

}
