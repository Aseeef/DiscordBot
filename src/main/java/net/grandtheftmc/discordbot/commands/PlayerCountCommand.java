package net.grandtheftmc.discordbot.commands;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
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
        SubcommandData setChannel = new SubcommandData("setchannel", "Displays GTM's player count at the target channel");
        OptionData setChannelData = new OptionData(OptionType.CHANNEL, "channel", "The channel which we will display the player count in").setChannelTypes(ChannelType.VOICE);
        setChannel.addOptions(setChannelData);

        slashCommandData.addSubcommands(setChannel);
    }

    @Override
    public void onCommandUse(SlashCommandInteraction interaction, MessageChannel channel, List<OptionMapping> arguments, Member member, GTMUser gtmUser, String[] path) {

        // Suggestions SetChannel Command
        if (path[0].equalsIgnoreCase("setchannel")) {
            VoiceChannel playerCountChannel = interaction.getOption("channel").getAsVoiceChannel();

            // If its a valid voice channel id
            if (playerCountChannel != null) {

                // Set as player count channel
                ChannelIdData.get().setPlayerCountChannelId(Long.parseLong(path[1]));

                // Updates online players
                updateOnlinePlayers();

                // Send success msg
                interaction.reply("**<`"+playerCountChannel.getIdLong()+"`>"+
                        " has been set as the Player Count channel!**").setEphemeral(true).queue();

            }

            // If channel id is invalid
            else {
                interaction.reply("**Invalid voice channel Id! Channel not set.**").setEphemeral(true).queue();
            }

        }
    }

}
