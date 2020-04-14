package commands;

import Utils.Rank;
import Utils.tools.GTools;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import static Utils.tools.GTools.getArgs;
import static Utils.tools.GTools.hasRolePerms;

public class SeniorsCommand extends ListenerAdapter {

    public void onGuildMessageReceived(GuildMessageReceivedEvent e) {

        String msg = e.getMessage().getContentRaw();
        Member member = e.getMember();
        User user = e.getAuthor();
        assert member != null;

        if (GTools.isCommand(msg, user, Commands.SENIORS) &&
                hasRolePerms(member, Commands.SENIORS.rank())
        ) {

            String[] args = getArgs(msg);
            TextChannel channel = e.getChannel();

            if (args.length == 0) {

            }

            // RaidMode SetChannel Command
            else if (args[0].equalsIgnoreCase("setchannel")) {



            }

        }


    }

}
