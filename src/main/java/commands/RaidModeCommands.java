package commands;

import Utils.Rank;
import Utils.SelfData;
import Utils.tools.GTools;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import static Utils.tools.GTools.*;
import static Utils.tools.RaidModeTools.*;

public class RaidModeCommands extends ListenerAdapter {

    public void onGuildMessageReceived(GuildMessageReceivedEvent e) {

        String msg = e.getMessage().getContentRaw();
        Member member = e.getMember();
        User user = e.getAuthor();
        assert member != null;

        if (GTools.isCommand(msg, user, Commands.RAIDMODE) &&
                hasRolePerms(member, Commands.RAIDMODE.rank())
        ) {

            String[] args = getArgs(msg);
            TextChannel channel = e.getChannel();

            if (args.length == 0) {
                // Send msg then delete after defined time in config
                sendThenDelete(channel, getRaidModeHelpMsg());
            }

            // RaidMode SetChannel Command
            else if (args[0].equalsIgnoreCase("setchannel")) {

                // Set as raid mode channel
                SelfData.get().setRaidAlertChannelId(channel.getIdLong());

                // Send success msg
                sendThenDelete(channel, raidChannelSet(channel));

            }

            else if (args[0].equalsIgnoreCase("enable")) {
                if (!raidMode[0])
                activateRaidMode(e.getAuthor());
                else sendThenDelete(channel, "**Raid mode is already enabled!**");
            }

            else if (args[0].equalsIgnoreCase("disable")) {
                if (raidMode[0])
                    disableRaidMode(e.getAuthor());
                else sendThenDelete(channel, "**Raid mode is already disabled!**");
            }

        }


    }

    private static Message raidChannelSet(TextChannel channel) {
        return new MessageBuilder()
                .append("**")
                .append(channel.getAsMention())
                .append(" has been set as the raid mode alerts channel!")
                .append("**")
        .build();
    }

    private Message getRaidModeHelpMsg() {
        return new MessageBuilder()
                .append("> **Please enter a valid command argument:**\n")
                .append("> `/RaidMode SetChannel <ID>` - *Sets current channel as the Raid mode alerts channel*\n")
                .append("> `/RaidMode Enable` - *Manually enables raid mode*\n")
                .append("> `/RaidMode Disable` - *Manually disables raid mode*\n")
                .build();
    }

}
