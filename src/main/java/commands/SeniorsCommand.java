package commands;

import Utils.Rank;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class SeniorsCommand extends Command {

    public SeniorsCommand() {
        super("seniors", "Manage senior settings", Rank.ADMIN, Type.DISCORD_ONLY);
    }

    @Override
    public void onCommandUse(Message message, Member member, TextChannel channel, String[] args) {
        if (args.length == 0) {

        }

        // RaidMode SetChannel Command
        else if (args[0].equalsIgnoreCase("setchannel")) {


        }
    }

}
