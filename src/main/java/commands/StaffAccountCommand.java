package commands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import utils.Rank;
import utils.users.GTMUser;

public class StaffAccountCommand extends Command {

    public StaffAccountCommand() {
        super("account", "Staff command to pull gtm account information for a verified user", Rank.HELPER, Type.ANYWHERE);
    }

    @Override
    public void onCommandUse(Message message, Member member, GTMUser gtmUser, MessageChannel channel, String[] args) {

    }

}
