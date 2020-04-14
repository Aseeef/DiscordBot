package events;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import static Utils.tools.GTools.isCommand;
import static Utils.tools.Logs.log;

public class LogCommands extends ListenerAdapter {

    public void onGuildMessageReceived(GuildMessageReceivedEvent e) {

        if (isCommand(e.getMessage().getContentRaw(), e.getAuthor())) {
            log("User "+ e.getAuthor().getAsTag()+
                    "("+e.getAuthor().getId()+") issued command: " + e.getMessage().getContentRaw());
        }

    }

}
