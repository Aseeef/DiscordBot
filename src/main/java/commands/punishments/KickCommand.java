package commands.punishments;

import commands.Command;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.Event;
import utils.users.GTMUser;
import utils.users.Rank;

public class KickCommand extends Command {

    public KickCommand() {
        super("kick", "Kick a player from the discord", Rank.MOD, Type.DISCORD_ONLY);
    }

    @Override
    public void onCommandUse(Message message, Member member, GTMUser gtmUser, MessageChannel channel, String[] args) {

    }

}
