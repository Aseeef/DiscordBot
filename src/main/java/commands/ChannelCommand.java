package commands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import utils.users.Rank;
import utils.users.GTMUser;

public class ChannelCommand extends Command {

    public ChannelCommand(String name, String description, Rank rank, Type type) {
        super("channel", "Create and manage your own private voice channels", Rank.VIP, Type.DISCORD_ONLY);
    }

    @Override
    public void onCommandUse(Message message, Member member, GTMUser gtmUser, MessageChannel channel, String[] args) {



    }

}
