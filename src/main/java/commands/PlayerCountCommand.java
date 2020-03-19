package commands;

import Utils.AutoDeleter.DeleteMe;
import Utils.Config;
import Utils.tools.GTools;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.IOException;
import java.util.Objects;

import static Utils.tools.GTools.onlinePlayersMsg;

public class PlayerCountCommand extends ListenerAdapter {

    public void onGuildMessageReceived(GuildMessageReceivedEvent e) {
        String[] args = e.getMessage().getContentRaw().split(" ");
        if (GTools.isCommand(e, "playercount")
                && Objects.requireNonNull(e.getMember()).getPermissions().contains(Permission.ADMINISTRATOR)) {

            TextChannel channel = e.getChannel();

            // If there are no command arguments send sub command help list
            if (args.length == 1) {
                // Queue msg to be deleted
                DeleteMe.deleteQueue(getPlayerCountHelpMsg());
                // Send msg
                channel.sendMessage(getPlayerCountHelpMsg()).queue();
            }

            // Suggestions SetChannel Command
            else if (args[1].equalsIgnoreCase("setchannel")) {

                VoiceChannel playerCountChannel = GTools.jda.getVoiceChannelById(Long.parseLong(args[2]));

                // If its a valid voice channel id
                if (playerCountChannel !=null) {

                    // Set as player count channel
                    try {
                        Config.get().setPlayerCountChannelId(Long.parseLong(args[2]));
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                    // Update channel name to initialize Player Count Updater in PlayerCountUpdater()
                    playerCountChannel.getManager()
                            .setName(onlinePlayersMsg()).queue();
                    GTools.log("Player Count Updater has been initialized.");

                    // Queue success msg to be deleted
                    DeleteMe.deleteQueue(new MessageBuilder().setContent("**<`"+playerCountChannel.getIdLong()+"`>"+
                            " has been set as the Player Count channel!**").build());
                    // Send success msg
                    channel.sendMessage("**<`"+playerCountChannel.getIdLong()+"`>"+
                            " has been set as the Player Count channel!**").queue();

                }

                // If channel id is invalid
                else {
                    // Queue msg to be deleted
                    DeleteMe.deleteQueue(new MessageBuilder().setContent("**Invalid voice channel Id! Channel not set.**").build());
                    // Send command not found msg
                    channel.sendMessage("**Invalid voice channel Id! Channel not set.**").queue();
                }

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
