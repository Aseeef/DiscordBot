package commands.punishments;

import commands.Command;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.Event;
import utils.users.GTMUser;
import utils.users.Rank;

public class MuteCommand extends Command {

    public MuteCommand() {
        super("mute", "Allows moderators to mute target users in some/all channels", Rank.HELPER, Type.DISCORD_ONLY);
    }

    @Override
    public void onCommandUse(Message message, Member member, GTMUser gtmUser, MessageChannel channel, String[] args) {

    }
}
