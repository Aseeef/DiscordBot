package net.grandtheftmc.discordbot.utils.tools;

import net.grandtheftmc.discordbot.GTMBot;
import net.grandtheftmc.discordbot.utils.Utils;
import net.grandtheftmc.discordbot.utils.confighelpers.Config;
import net.grandtheftmc.discordbot.utils.selfdata.ChannelIdData;
import net.grandtheftmc.discordbot.utils.console.Logs;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import javax.annotation.Nullable;

import java.util.Timer;
import java.util.TimerTask;

import static net.grandtheftmc.discordbot.utils.console.Logs.log;

public class RaidModeTools {

    // raidMode[0] - Whether raid mode is active (true = yes)
    // raidMode[1] - Whether it was manually triggered (true = yes)
    public static boolean[] raidMode = {false, false};

    private static final String raidModePunishType = Config.get().getRaidmodeSettings().getRaidModePunishType();
    private static final String raidModeMessage = Config.get().getRaidmodeSettings().getRaidModeMessage();

    // Timer task to disable raid mode once it begins
    private static Timer task;


    public static void disableRaidMode(@Nullable User user) {
        if (raidMode[0] = true) {

            log("Raid mode has been disabled!");

            // Disable raid mode
            raidMode[0] = false;
            raidMode[1] = false;

            TextChannel alertChannel = GTMBot.getJDA().getTextChannelById(ChannelIdData.get().getRaidAlertChannelId());

            if (alertChannel != null) {
                // Notify staff members
                if (raidModePunishType.equalsIgnoreCase("KICK") ||
                        raidModePunishType.equalsIgnoreCase("BAN")) {
                    // If raid mode was disable by command
                    if (user != null)
                        alertChannel.sendMessage(
                                "[**ALERT**] " + user.getAsMention() + " has disabled raid mode! Resuming regular function..."
                        ).queue();
                    // Otherwise
                    else
                        alertChannel.sendMessage(
                                "[**ALERT**] As far as I am able to tell, the raid has concluded." +
                                        " Raid mode has been disabled. Resuming regular function..."
                        ).queue();
                }

            } else
                log("Failed to notify staff members of the raid end because the raid alerts channel " +
                        "has not been properly configured yet", Logs.WARNING);

        }
    }

    public static void activateRaidMode(@Nullable User user) {
        log("Server is being raided! Activating raid mode!");

        // Activate raid mode
        raidMode[0] = true;

        if (user == null) {
            // Start timer to disable raid mode automatically if it started automatically
            scheduleDisableTask();
            // Set raidMode[1] to false to signal this was automatically triggered
            raidMode[1] = false;
        } else {
            raidMode[1] = true;
        }

        TextChannel alertChannel = GTMBot.getJDA().getTextChannelById(ChannelIdData.get().getRaidAlertChannelId());

        if (alertChannel != null) {
            // Notify staff members
            if (raidModePunishType.equalsIgnoreCase("KICK") ||
                    raidModePunishType.equalsIgnoreCase("BAN")) {
                // If raid mode was triggered by a command
                if (user != null)
                    alertChannel.sendMessage(
                            "[**ALERT**] Attention! " + user.getAsMention() + " just enabled raid mode!\n\n" +
                                    "We might be getting **raided by bots**! " +
                                    "I have activated my Anti-Bot mechanisms, and will " + raidModePunishType + " " +
                                    "all bots as detected! Further action may or may not be needed. Note: Since " +
                                    "raid mode was activated manually, it must also be disabled manually!"
                    ).queue();

                // Otherwise
                else
                alertChannel.sendMessage(
                        "[**ALERT**] @everyone\n\n" +
                                "Attention! It appears we are currently being **raided by bots**! " +
                                "I have activated my Anti-Bot mechanisms, and will " + raidModePunishType + " " +
                                "all bots as detected! Further action may or may not be needed."
                ).queue();
            }

            else if (raidModePunishType.equalsIgnoreCase("NOTIFY")) {
                if (user != null)
                    alertChannel.sendMessage(
                            "[**ALERT**] Attention! " + user.getAsMention() + " just enabled raid mode!\n\n" +
                                    "We might be getting **raided by bots**! " +
                                    "I will alert staff members of the **names of any bots I detect**. However" +
                                    "staff will have to verify this info, and **punish the bots manually**. Note: Since " +
                                    "raid mode was activated manually, it must also be disabled manually!"
                    ).queue();

                // Otherwise
                else
                alertChannel.sendMessage(
                        "[**ALERT**] @everyone\n\n" +
                                "Attention! It appears we are currently being **raided by bots**! " +
                                "I will alert staff members of the **names of any bots I detect**. However" +
                                "staff will have to verify this info, and **punish the bots manually**."
                ).queue();
            }

        } else
            log("Failed to notify staff members of the raid because the raid alerts channel " +
                    "has not been properly configured yet", Logs.WARNING);
    }

    public static void punishBot(Member member) {

        User user = member.getUser();

        log("User " + user.getAsTag() + " (" + user.getId() + ") was detected as a bot!");


        // Uses callbacks to ensure that each task is ran sequentially
        if (raidModePunishType.equalsIgnoreCase("BAN")) {
            user.openPrivateChannel().queue( (userChannel) ->
                    userChannel.sendMessage(raidModeMessage).queue( (msg) ->
                    member.ban(1, "User detected as a Bot").queue()));
        }

        else if (raidModePunishType.equalsIgnoreCase("KICK")) {
            user.openPrivateChannel().queue( (userChannel) ->
                    userChannel.sendMessage(raidModeMessage).queue( (msg) ->
                    member.kick("User detected as a Bot").queue()));
        }

        else if (raidModePunishType.equalsIgnoreCase("NOTIFY")) {

            TextChannel alertChannel = GTMBot.getJDA().getTextChannelById(ChannelIdData.get().getRaidAlertChannelId());

            if (alertChannel != null)
                alertChannel.sendMessage("**BOTS>** User " + member.getAsMention() + " was detected as a bot!").queue();
            else
                log("Failed to report bot user " + member.getUser().getAsTag() + " (" +
                        member.getIdLong() + ") to staff because raid alert channel has not been set yet!", Logs.WARNING);

        }

    }

    public static void rescheduleDisableTask() {
        log("Rescheduling raid mode disable task...");
        task.cancel();
        scheduleDisableTask();
    }

    public static void scheduleDisableTask() {
        log("Scheduled a new disable raid mode task...");
        task = new Timer();
        task.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        disableRaidMode(null);
                    }
                },
                1000 * 60 * Config.get().getRaidmodeSettings().getRaidModeDisable()
        );
    }

}
