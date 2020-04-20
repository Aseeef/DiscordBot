package selfevents;

import Utils.Config;
import Utils.SelfData;
import Utils.tools.Logs;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Timer;
import java.util.TimerTask;

import static Utils.tools.GTools.jda;
import static Utils.tools.GTools.updateOnlinePlayers;
import static Utils.tools.Logs.log;
import static Utils.tools.RaidModeTools.raidMode;

public class ReadyEvents extends ListenerAdapter {

    public void onReady(ReadyEvent event) {

        // Print finished loading msg
        log("Bot is now online!");

        // Start updater repeating task to update player count
        Timer playerCountUpdater = new Timer();
        playerCountUpdater.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // If its a valid channel
                if (jda.getVoiceChannelById(SelfData.get().getPlayerCountChannelId()) != null)
                    updateOnlinePlayers();
                else log("Failed to update Player count channel because a valid channel isn't set!");
            }
        }, 5000, 1000 * Config.get().getMineStatRefresh());

        log("Player count channel updater task initialized!");

        // INVALID CONFIG OR DATA WARNINGS

        // Invalid raid mode punishment type settings
        String raidModePunishType = Config.get().getRaidModePunishType();
        if (!raidModePunishType.equalsIgnoreCase("BAN") &&
        !raidModePunishType.equalsIgnoreCase("KICK") &&
        !raidModePunishType.equalsIgnoreCase("NOTIFY")) {
            log("Invalid raid mode punishment type configured in the config!" +
                    " Valid punishment types are: NOTIFY, BAN, KICK", Logs.WARNING);
            log("Setting raid punishment type to \"KICK\" for now...", Logs.WARNING);
            Config.get().setRaidModePunishType("KICK");
        }

        // If any of the entered roles are invalid
        if (jda.getRolesByName(Config.get().getManager(), true).size() == 0)
            log("No role by the name of '" + Config.get().getManager() + "' was found!" +
                    " This will cause errors...", Logs.WARNING);
        else if (jda.getRolesByName(Config.get().getDeveloper(), true).size() == 0)
            log("No role by the name of '" + Config.get().getDeveloper() + "' was found!" +
                    " This will cause errors...", Logs.WARNING);
        else if (jda.getRolesByName(Config.get().getAdmin(), true).size() == 0)
            log("No role by the name of '" + Config.get().getAdmin() + "' was found!" +
                    " This will cause errors...", Logs.WARNING);
        else if (jda.getRolesByName(Config.get().getBuilder(), true).size() == 0)
            log("No role by the name of '" + Config.get().getBuilder() + "' was found!" +
                    " This will cause errors...", Logs.WARNING);
        else if (jda.getRolesByName(Config.get().getSrMod(), true).size() == 0)
            log("No role by the name of '" + Config.get().getManager() + "' was found!" +
                    " This will cause errors...", Logs.WARNING);
        else if (jda.getRolesByName(Config.get().getMod(), true).size() == 0)
            log("No role by the name of '" + Config.get().getMod() + "' was found!" +
                    " This will cause errors...", Logs.WARNING);
        else if (jda.getRolesByName(Config.get().getHelper(), true).size() == 0)
            log("No role by the name of '" + Config.get().getHelper() + "' was found!" +
                    " This will cause errors...", Logs.WARNING);
        else if (jda.getRolesByName(Config.get().getBuildTeam(), true).size() == 0)
            log("No role by the name of '" + Config.get().getBuildTeam() + "' was found!" +
                    " This will cause errors...", Logs.WARNING);
        else if (jda.getRolesByName(Config.get().getYoutuber(), true).size() == 0)
            log("No role by the name of '" + Config.get().getYoutuber() + "' was found!" +
                    " This will cause errors...", Logs.WARNING);
        else if (jda.getRolesByName(Config.get().getSupreme(), true).size() == 0)
            log("No role by the name of '" + Config.get().getSupreme() + "' was found!" +
                    " This will cause errors...", Logs.WARNING);
        else if (jda.getRolesByName(Config.get().getSponsor(), true).size() == 0)
            log("No role by the name of '" + Config.get().getSponsor() + "' was found!" +
                    " This will cause errors...", Logs.WARNING);
        else if (jda.getRolesByName(Config.get().getElite(), true).size() == 0)
            log("No role by the name of '" + Config.get().getElite() + "' was found!" +
                    " This will cause errors...", Logs.WARNING);
        else if (jda.getRolesByName(Config.get().getPremium(), true).size() == 0)
            log("No role by the name of '" + Config.get().getPremium() + "' was found!" +
                    " This will cause errors...", Logs.WARNING);
        else if (jda.getRolesByName(Config.get().getVip(), true).size() == 0)
            log("No role by the name of '" + Config.get().getVip() + "' was found!" +
                    " This will cause errors...", Logs.WARNING);
        else if (jda.getRolesByName(Config.get().getNoRank(), true).size() == 0)
            log("No role by the name of '" + Config.get().getNoRank() + "' was found!" +
                    " This will cause errors...", Logs.WARNING);
        else if (jda.getRolesByName(Config.get().getNoRank(), true).size() == 0)
            log("No role by the name of '" + Config.get().getUnverified() + "' was found!" +
                    " This will cause errors...", Logs.WARNING);


        // Suggestions channel not configured
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