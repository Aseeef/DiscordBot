package commands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import utils.users.GTMUser;
import utils.users.Rank;

public class SeniorsCommand extends Command {

    public SeniorsCommand() {
        super("seniors", "Manage senior related settings", Rank.ADMIN, Type.DISCORD_ONLY);
    }

    @Override
    public void onCommandUse(Message message, Member member, GTMUser gtmUser, MessageChannel channel, String[] args) {
        if (args.length == 0) {

        }


        else if (args[0].equalsIgnoreCase("setchannel")) {


        }


    }

}
