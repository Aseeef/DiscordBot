package Utils.AutoDeleter;

import Utils.tools.GTools;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.concurrent.TimeUnit;

public class AutoDelete extends ListenerAdapter {

    public void onGuildMessageReceived(GuildMessageReceivedEvent e) {

        if (DeleteMe.contains(e.getMessage().getContentRaw())) {
            e.getMessage().delete().queueAfter(15, TimeUnit.SECONDS);
        }

        // If is any command, delete
        else if (!e.getAuthor().isBot() && GTools.isCommand(e)) {
            e.getMessage().delete().queue();
        }

    }

}
