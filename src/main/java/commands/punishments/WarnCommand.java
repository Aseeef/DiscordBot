package commands.punishments;

import commands.Command;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.Event;
import utils.users.GTMUser;
import utils.users.Rank;

public class WarnCommand extends Command {

    public WarnCommand(String name, String description, Rank rank, Type type) {
        super(name, description, rank, type);
    }

    @Override
    public void onCommandUse(Message message, Member member, GTMUser gtmUser, MessageChannel channel, String[] args) {

    }
}
