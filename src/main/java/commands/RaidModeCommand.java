package commands;

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

public class RaidModeCommand extends ListenerAdapter {

    public void onGuildMessageReceived(GuildMessageReceivedEvent e) {

        String msg = e.getMessage().getContentRaw();
        Member member = e.getMember();
        User user = e.getAuthor();
        assert member != null;

        if (GTools.isCommand(msg, user, Commands.RAIDMODE)) {

            String[] args = getArgs(msg);
            TextChannel channel = e.getChannel();

            // Check perms
            if (!hasRolePerms(member, Commands.RAIDMODE.rank())) {
                sendThenDelete(channel, getNoPermsLang());
                return;
            }

            // If there are no command arguments send sub command help list
            if (args.length == 0) {
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

            // If no sub commands match
            else {
                sendThenDelete(channel, getRaidModeHelpMsg());
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
                .append("> `/RaidMode SetChannel` - *Sets current channel as the Raid mode alerts channel*\n")
                .append("> `/RaidMode Enable` - *Manually enables raid mode*\n")
                .append("> `/RaidMode Disable` - *Manually disables raid mode*\n")
                .build();
    }

}
