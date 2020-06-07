package selfevents;

import Utils.Config;
import Utils.SelfData;
import Utils.database.BaseDatabase;
import Utils.database.redis.RedisDAO;
import Utils.tools.CommandsTools;
import Utils.tools.Logs;
import commands.*;
import events.LogCommands;
import events.OnJoin;
import events.OnReactRules;
import events.OnSuggestion;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import static Utils.tools.GTools.jda;
import static Utils.tools.GTools.updateOnlinePlayers;
import static Utils.tools.Logs.log;

public class ReadyEvents extends ListenerAdapter {

    public void onReady(ReadyEvent event) {

        // Print finished loading msg
        log("Bot is now online!");

        // Start updater repeating task to update player count
        Timer playerCountUpdater = new Timer();
        playerCountUpdater.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateOnlinePlayers();
            }
        }, 5000, 1000 * Config.get().getMineStatRefresh());

        log("Player count channel updater task initialized!");

        // INVALID CONFIG OR DATA WARNINGS
        checkRaidModeSettings();
        checkSuggestionsSettings();

    }

    // Invalid raid mode punishment type settings
    private void checkRaidModeSettings() {
        String raidModePunishType = Config.get().getRaidModePunishType();
        if (!raidModePunishType.equalsIgnoreCase("BAN") &&
                !raidModePunishType.equalsIgnoreCase("KICK") &&
                !raidModePunishType.equalsIgnoreCase("NOTIFY")) {
            log("Invalid raid mode punishment type configured in the config!" +
                    " Valid punishment types are: NOTIFY, BAN, KICK", Logs.WARNING);
            log("Setting raid punishment type to \"KICK\" for now...", Logs.WARNING);
            Config.get().setRaidModePunishType("KICK");
        }
    }

    // Chec if suggestions channel not configured
    private void checkSuggestionsSettings() {
        if (jda.getTextChannelById(SelfData.get().getSuggestionChannelId()) == null)
            log("The suggestion channel has not been properly configured yet!",
                    Logs.WARNING);
        // Player count channel not configured
        if (jda.getVoiceChannelById(SelfData.get().getPlayerCountChannelId()) == null)
            log("The player count display channel has not been properly configured yet!",
                    Logs.WARNING);
        // Raid alerts channel not configured
        if (jda.getTextChannelById(SelfData.get().getRaidAlertChannelId()) == null)
            log("The raid alerts channel has not been properly configured yet!",
                    Logs.WARNING);
    }

}