package commands;

import Utils.Rank;
import net.dv8tion.jda.api.entities.*;

import static Utils.tools.GTools.jda;

public class RebootCommand extends Command {

    public RebootCommand() {
        super("reboot", "Restart the discord bot", Rank.ADMIN, Type.ANYWHERE);
    }

    @Override
    public void onCommandUse(Message message, Member member, MessageChannel channel, String[] args) {
        // Only need to open a private channel if this isn't already dms
        if (channel instanceof PrivateChannel)
            channel.sendMessage("**Executed reboot. Please allow up to 30 seconds for the bot to come back up!**").queue();
        else
        member.getUser().openPrivateChannel().queue( (privateChannel) ->
            privateChannel.sendMessage("**Executed reboot. Please allow up to 30 seconds for the bot to come back up!**")
                    .queue( (m) -> jda.shutdownNow()));
    }

}
