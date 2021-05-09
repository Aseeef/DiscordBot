package commands.stats;

import commands.Command;
import commands.stats.wrappers.PlanUser;
import commands.stats.wrappers.Session;
import commands.stats.wrappers.WrappedIPData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.Event;
import utils.pagination.DiscordMenu;
import utils.tools.GTools;
import utils.users.GTMUser;
import utils.users.Rank;

import java.util.List;
import java.util.UUID;

public class StatsCommand extends Command {

    public StatsCommand() {
        super("stats", "View details statistics on players or staff", Rank.SRMOD, Type.ANYWHERE);
    }

    @Override
    public void onCommandUse(Message message, Member member, GTMUser gtmUser, MessageChannel channel, String[] args) {

        if (args.length < 1) {
            GTools.sendThenDelete(channel, getCommandUsage());
            return;
        }

        StatsMenu menu = new StatsMenu(member.getUser(), channel, args[0]);
        boolean success = menu.load();

        if (!success) {
            GTools.sendThenDelete(channel, "**Invalid Player!** The player '" + args[0] + "' does not exist / has never played GTM!");
        }

    }

    private Message getCommandUsage() {
        return new MessageBuilder()
                .append("> **Please enter a valid command argument:**\n")
                .append("> `/Stats <Player>` - *Generates statistics on the specified user*\n")
                .build();
    }



}
