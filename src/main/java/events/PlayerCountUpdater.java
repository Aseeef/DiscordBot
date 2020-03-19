package events;

import Utils.Config;
import Utils.tools.GTools;
import net.dv8tion.jda.api.events.channel.voice.update.VoiceChannelUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.concurrent.TimeUnit;

import static Utils.tools.GTools.onlinePlayersMsg;

public class PlayerCountUpdater extends ListenerAdapter {

    public void onVoiceChannelUpdateName(VoiceChannelUpdateNameEvent e) {

        // If channel that is updated is player count channel, start a new task to re-update 1 minute later
        if (e.getChannel().getIdLong() == Config.get().getPlayerCountChannelId()) {
            GTools.log("Updating Online Player count to " + e.getNewName().replaceAll("\\D", "") + "...");
            GTools.gtm.refresh();
            e.getChannel().getManager().setName(onlinePlayersMsg()).queueAfter(2, TimeUnit.MINUTES);
        }

    }

}
