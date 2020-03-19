import Utils.Config;
import Utils.tools.GTools;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.concurrent.TimeUnit;

import static Utils.tools.GTools.jda;
import static Utils.tools.GTools.onlinePlayersMsg;

public class ReadyEvents extends ListenerAdapter {

    public void onReady(ReadyEvent e) {

        // Print finished loading msg
        GTools.log("Bot is now online!");

        // If PlayerCount channel already exists, update it to start a self update loop in PlayerCountUpdater()
        VoiceChannel playerCountChannel = jda.getVoiceChannelById(Config.get().getPlayerCountChannelId());
        if (playerCountChannel != null) {
            playerCountChannel.getManager().setName(onlinePlayersMsg()).queueAfter(1, TimeUnit.SECONDS);
            GTools.log("Player Count Updater has been initialized.");
        } else {
            GTools.log("Player Count Channel has not been set yet!");
        }

    }

}
