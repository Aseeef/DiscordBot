package Utils.tools;

import Utils.Config;
import Utils.tools.GTools;
import commands.Commands;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;

public class CommandsTools extends ListenerAdapter {

    public void onGuildMessageReceived(GuildMessageReceivedEvent e) {

        String msg = e.getMessage().getContentRaw();
        String commandName = msg.toLowerCase().replaceFirst(Config.get().getCommandPrefix(), "")
                .split(" ")[0];
        TextChannel channel = e.getChannel();

        // List of all commands
        ArrayList<String> commands = new ArrayList<>();
        for (Commands command : Commands.values()) {
            commands.add(command.name().toLowerCase());
        }

        // If is any command, delete it
        if (GTools.isCommand(msg, e.getAuthor())) {
            e.getMessage().delete().queue();

            // If is unknown command, print msg
            if (!commands.contains(commandName)) {
                GTools.sendThenDelete(channel, "**Unknown command! Do /help for a list of all commands!**");
            }

        }

    }

}
