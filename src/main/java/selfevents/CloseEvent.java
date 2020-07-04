package selfevents;

import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import static utils.console.Logs.log;

public class CloseEvent extends ListenerAdapter {

    public void onShutdown (ShutdownEvent e) {
        log("Bot has now been disabled.");
    }

}
