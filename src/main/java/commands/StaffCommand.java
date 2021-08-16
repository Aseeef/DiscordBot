package commands;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import utils.selfdata.ChannelIdData;
import utils.users.GTMUser;
import utils.users.Rank;

import static utils.Utils.sendThenDelete;

public class StaffCommand extends Command {

    public StaffCommand() {
        super("staff", "Manage staff related settings", Rank.MOD, Type.DISCORD_ONLY);
    }

    @Override
    public void onCommandUse(Message message, Member member, GTMUser gtmUser, MessageChannel channel, String[] args) {

        if (args.length < 1) {
            sendThenDelete(channel, getHelpPage());
            return;
        }

        switch (args[0].toLowerCase()) {

            case "setchannel": {
                if (!Rank.hasRolePerms(member, Rank.ADMIN)) {
                    sendThenDelete(channel, "**Sorry but you must be an admin+ to execute this command!**");
                    return;
                }

                // Set as raid mode channel
                ChannelIdData.get().setModChannelId(channel.getIdLong());

                // Send success msg
                sendThenDelete(channel, modChannelSet((TextChannel) channel));

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

    private String modChannelSet(TextChannel channel) {
        return "**" + channel.getAsMention() + " has been set as the Moderator channel!**";
    }

}
