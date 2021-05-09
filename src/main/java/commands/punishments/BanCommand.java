package commands.punishments;

import commands.Command;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import utils.users.GTMUser;
import utils.users.Rank;

public class BanCommand extends Command {

    public BanCommand() {
        super("ban", "Allows moderators to ip-ban target users from discord", Rank.MOD, Type.DISCORD_ONLY);
    }

    @Override
    public void onCommandUse(Message message, Member member, GTMUser gtmUser, MessageChannel channel, String[] args) {

    }

}
