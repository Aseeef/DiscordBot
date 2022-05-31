package net.grandtheftmc.discordbot.commands;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.grandtheftmc.discordbot.utils.selfdata.ChannelIdData;
import net.grandtheftmc.discordbot.utils.users.GTMUser;
import net.grandtheftmc.discordbot.utils.users.Rank;

import java.util.List;

import static net.grandtheftmc.discordbot.utils.Utils.sendThenDelete;
import static net.grandtheftmc.discordbot.utils.tools.RaidModeTools.*;

public class RaidModeCommand extends Command {

    public RaidModeCommand() {
        super("raidmode", "Manage raid mode settings", Rank.ADMIN, Type.DISCORD_ONLY);
    }

    @Override
    public void buildCommandData(SlashCommandData slashCommandData) {
        SubcommandData setChannel = new SubcommandData("setchannel", "Sets current channel as the Raid mode alerts channel");
        SubcommandData enable = new SubcommandData("enable", "Manually enables raid mode");
        SubcommandData disable = new SubcommandData("disable", "Manually disables raid mode");

        slashCommandData.addSubcommands(setChannel, enable, disable);
    }

    @Override
    public void onCommandUse(SlashCommandInteraction interaction, MessageChannel channel, List<OptionMapping> arguments, Member member, GTMUser gtmUser, String[] path) {

        // RaidMode SetChannel Command
        if (path[0].equalsIgnoreCase("setchannel")) {

            // Set as raid mode channel
            ChannelIdData.get().setRaidAlertChannelId(channel.getIdLong());

            // Send success msg
            interaction.reply(raidChannelSet((TextChannel) channel)).queue();

        }

        else if (path[0].equalsIgnoreCase("enable")) {
            if (!raidMode[0])
                activateRaidMode(member.getUser());
            else interaction.reply("**Raid mode is already enabled!**").queue();
        }

        else if (path[0].equalsIgnoreCase("disable")) {
            if (raidMode[0])
                disableRaidMode(member.getUser());
            else interaction.reply("**Raid mode is already disabled!**").queue();
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

}
