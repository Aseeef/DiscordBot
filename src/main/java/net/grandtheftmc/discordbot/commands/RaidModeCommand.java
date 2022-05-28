package net.grandtheftmc.discordbot.commands;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.grandtheftmc.discordbot.utils.selfdata.ChannelIdData;
import net.grandtheftmc.discordbot.utils.users.GTMUser;
import net.grandtheftmc.discordbot.utils.users.Rank;

import static net.grandtheftmc.discordbot.utils.Utils.sendThenDelete;
import static net.grandtheftmc.discordbot.utils.tools.RaidModeTools.*;

public class RaidModeCommand extends Command {

    public RaidModeCommand() {
        super("raidmode", "Manage raid mode settings", Rank.ADMIN, Type.DISCORD_ONLY);
    }

    @Override
    public void onCommandUse(SlashCommandInteraction interaction, MessageChannel channel, Member member, GTMUser gtmUser, String[] args) {
        // If there are no command arguments send sub command help list
        if (args.length == 0) {
            sendThenDelete(channel, getRaidModeHelpMsg());
        }

        // RaidMode SetChannel Command
        else if (args[0].equalsIgnoreCase("setchannel")) {

            // Set as raid mode channel
            ChannelIdData.get().setRaidAlertChannelId(channel.getIdLong());

            // Send success msg
            sendThenDelete(channel, raidChannelSet((TextChannel) channel));

        }

        else if (args[0].equalsIgnoreCase("enable")) {
            if (!raidMode[0])
                activateRaidMode(member.getUser());
            else sendThenDelete(channel, "**Raid mode is already enabled!**");
        }

        else if (args[0].equalsIgnoreCase("disable")) {
            if (raidMode[0])
                disableRaidMode(member.getUser());
            else sendThenDelete(channel, "**Raid mode is already disabled!**");
        }

        // If no sub commands match
        else {
            sendThenDelete(channel, getRaidModeHelpMsg());
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
