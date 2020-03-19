package events;

import Utils.tools.GTools;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import static Utils.tools.GTools.isCommand;

public class LogCommands extends ListenerAdapter {

    public void onGuildMessageReceived(GuildMessageReceivedEvent e) {

        if (isCommand(e)) {
            GTools.log("User "+ e.getAuthor().getAsTag()+
                    "("+e.getAuthor().getId()+") issued command: " + e.getMessage().getContentRaw());
        }

    }

}
