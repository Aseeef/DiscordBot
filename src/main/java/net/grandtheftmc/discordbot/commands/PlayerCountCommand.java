package net.grandtheftmc.discordbot.commands;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.grandtheftmc.discordbot.GTMBot;
import net.grandtheftmc.discordbot.utils.selfdata.ChannelIdData;
import net.grandtheftmc.discordbot.utils.users.GTMUser;
import net.grandtheftmc.discordbot.utils.users.Rank;

import java.util.List;

import static net.grandtheftmc.discordbot.utils.Utils.*;

public class PlayerCountCommand extends Command {

    public PlayerCountCommand() {
        super("playercount", "Manage GTM player count display", Rank.ADMIN, Type.DISCORD_ONLY);
    }

    @Override
    public void buildCommandData(SlashCommandData slashCommandData) {

    }

    @Override
    public void onCommandUse(SlashCommandInteraction interaction, MessageChannel channel, List<OptionMapping> arguments, Member member, GTMUser gtmUser, String[] path) {
        // If there are no command arguments send sub command help list
        if (path.length == 0) {
            // Send msg then delete after defined time in utils.config
            sendThenDelete(channel, getPlayerCountHelpMsg());
        }

        // Suggestions SetChannel Command
        else if (path[0].equalsIgnoreCase("setchannel")) {

            VoiceChannel playerCountChannel = GTMBot.getJDA().getVoiceChannelById(Long.parseLong(path[1]));

            // If its a valid voice channel id
            if (playerCountChannel != null) {

                // Set as player count channel
                ChannelIdData.get().setPlayerCountChannelId(Long.parseLong(path[1]));

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
            // Send msg then delete after defined time in utils.config
            sendThenDelete(channel, getPlayerCountHelpMsg());
        }
    }

    private static Message getPlayerCountHelpMsg() {
        return new MessageBuilder()
                .append("> **Please enter a valid command argument:**\n")
                .append("> `/PlayerCount SetChannel <ID>` - *Displays GTM's player count at the given channel*\n")
                .build();
    }

}
