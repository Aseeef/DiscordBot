package commands.stats;

import commands.Command;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import utils.Utils;
import utils.users.GTMUser;
import utils.users.Rank;

public class StatsCommand extends Command {

    public StatsCommand() {
        super("stats", "View details statistics on players or staff", Rank.SRMOD, Type.ANYWHERE);
    }

    @Override
    public void onCommandUse(Message message, Member member, GTMUser gtmUser, MessageChannel channel, String[] args) {

        if (args.length < 1) {
            Utils.sendThenDelete(channel, getCommandUsage());
            return;
        }

        StatsMenu menu = new StatsMenu(member.getUser(), channel, args[0]);
        boolean success = menu.load();

        if (!success) {
            Utils.sendThenDelete(channel, "**Invalid Player!** The player '" + args[0] + "' does not exist / has never played GTM!");
        }

    }

    private Message getCommandUsage() {
        return new MessageBuilder()
                .append("> **Please enter a valid command argument:**\n")
                .append("> `/Stats <Player>` - *Generates statistics on the specified user*\n")
                .build();
    }



}
