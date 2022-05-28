package net.grandtheftmc.discordbot.selfevents;

import net.grandtheftmc.discordbot.utils.console.Logs;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import static net.grandtheftmc.discordbot.utils.console.Logs.log;

public class CloseEvent extends ListenerAdapter {

    public void onShutdown (ShutdownEvent e) {
        Logs.log("Bot has now been disabled.");
    }

}
