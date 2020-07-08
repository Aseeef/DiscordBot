package commands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import utils.channels.CustomChannel;
import utils.tools.GTools;
import utils.users.Rank;
import utils.users.GTMUser;

public class ChannelCommand extends Command {

    public ChannelCommand() {
        super("channel", "Create and manage your own private voice channels", Rank.VIP, Type.DISCORD_ONLY);
    }

    @Override
    public void onCommandUse(Message message, Member member, GTMUser gtmUser, MessageChannel channel, String[] args) {

        switch (args[0].toLowerCase()) {

            case "create": {

                if (CustomChannel.get(member).isPresent()) {
                    GTools.sendThenDelete(channel, "**You can only have `1` private channel at a time!**");
                    return;
                }

                break;
            }

            case "setname": {

            }

            case "setpublic": {

            }

            case "add": {

            }

            case "remove": {

            }

            case "addgang": {

            }

            case "delete": {

            }

        }

    }

}
