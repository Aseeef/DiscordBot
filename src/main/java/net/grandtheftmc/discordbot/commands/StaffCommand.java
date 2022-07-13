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

public class StaffCommand extends Command {

    public StaffCommand() {
        super("staff", "Manage staff related settings", Rank.MOD, Type.DISCORD_ONLY);
    }

    @Override
    public void buildCommandData(SlashCommandData slashCommandData) {
        SubcommandData modChannel = new SubcommandData("modchannel", "Set current channel to the mod channel for mod related alerts from the bot.");
        SubcommandData adminChannel = new SubcommandData("adminchannel", "Set current channel to the seniors channel for senior alerts from the bot.");
        slashCommandData.addSubcommands(modChannel, adminChannel);
    }

    @Override
    public void onCommandUse(SlashCommandInteraction interaction, MessageChannel channel, List<OptionMapping> arguments, Member member, GTMUser gtmUser, String[] path) {

        switch (path[0].toLowerCase()) {

            case "modchannel": {
                if (!Rank.hasRolePerms(member, Rank.ADMIN)) {
                    sendThenDelete(channel, "**Sorry but you must be an admin+ to execute this command!**");
                    return;
                }

                // Set as raid mode channel
                ChannelIdData.get().setModChannelId(channel.getIdLong());

                // Send success msg
                interaction.reply("**" + channel.getAsMention() + " has been set as the Moderator channel!**").setEphemeral(true).queue();
                break;
            }

            case "adminchannel": {
                if (!Rank.hasRolePerms(member, Rank.ADMIN)) {
                    sendThenDelete(channel, "**Sorry but you must be an admin+ to execute this command!**");
                    return;
                }

                // Set as raid mode channel
                ChannelIdData.get().setAdminChannelId(channel.getIdLong());

                // Send success msg
                interaction.reply("**" + channel.getAsMention() + " has been set as the Admin channel!**").setEphemeral(true).queue();
                break;
            }

            case "tickets": {

                // TODO: add a ticket lookup tool

            }

            default:
                sendThenDelete(channel, getHelpPage());
                break;

        }

    }

    private Message getHelpPage() {
        return new MessageBuilder()
                .append("> **Please enter a valid command argument:**\n")
                .append("> `/Staff SetChannel` - *Sets the current text channel has the Mod channel*\n")
                //.append("> `/Staff TLookup` - *Sets the current text channel has the Mod channel*\n")
                .build();
    }

}
