import Database.BaseDatabase;
import Utils.tools.CommandsTools;
import Utils.Config;
import Utils.tools.MineStat;
import Utils.SelfData;
import Utils.tools.Logs;
import commands.*;
import events.LogCommands;
import events.OnJoin;
import events.OnReactRules;
import events.OnSuggestion;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import selfevents.CloseEvent;
import selfevents.ConsoleCommand;
import selfevents.ReadyEvents;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static Utils.tools.GTools.jda;
import static Utils.tools.GTools.gtm;
import static Utils.tools.Logs.log;
import static Utils.tools.WelcomeTools.getPost;

public class GTM extends ListenerAdapter {

    public static void main (String[] args) {

        // Reads input from console for console commands
        new Thread(new ConsoleCommand()).start();

        // Load config
        log("Loading bot configuration....");
        Config.load();

        // Load Self Data
        log("Loading bot data....");
        SelfData.load();

        // Load SQL Database
        log("Connecting to database...");
        loadMySQL();

        // Initialize GTM MineStat
        log("Loading MineStat data on GTM...");
        gtm = new MineStat(Config.get().getServerIp(), Config.get().getServerPort());

        // Set up JDA & set Settings
        log("Initializing JDA...");

        try {
            jda = JDABuilder.createDefault(Config.get().getBotToken())
                    .setEnabledIntents(
                            GatewayIntent.DIRECT_MESSAGE_REACTIONS,
                            GatewayIntent.DIRECT_MESSAGE_TYPING,
                            GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.GUILD_MESSAGE_REACTIONS,
                            GatewayIntent.GUILD_EMOJIS,
                            GatewayIntent.GUILD_PRESENCES,
                            GatewayIntent.GUILD_VOICE_STATES,
                            GatewayIntent.GUILD_MEMBERS,
                            GatewayIntent.GUILD_INVITES,
                            GatewayIntent.GUILD_BANS
                    )
                    .setMemberCachePolicy(MemberCachePolicy.ONLINE)
                    .build();

            // Set presence
            jda.getPresence().setStatus(OnlineStatus.ONLINE);
            jda.getPresence().setActivity(Activity.playing("mc-gtm.net"));
            jda.getPresence().setIdle(false);

            // JDA Events
            jda.addEventListener(new OnSuggestion());
            jda.addEventListener(new CommandsTools());
            jda.addEventListener(new ReadyEvents());
            jda.addEventListener(new LogCommands());
            jda.addEventListener(new CloseEvent());
            jda.addEventListener(new OnJoin());
            jda.addEventListener(new OnReactRules());

            // JDA Commands
            jda.addEventListener(new SuggestionCommand());
            jda.addEventListener(new PlayerCountCommand());
            jda.addEventListener(new RaidModeCommands());
            jda.addEventListener(new WelcomeCommand());
            jda.addEventListener(new SeniorsCommand());
            jda.addEventListener(new HelpCommand());

            // Self user settings functions to check if there was config change to prevent
            // unnecessary calls to the discord api which may result in us getting rate limited
            setBotName();
            setAvatar();

        } catch (LoginException | IllegalArgumentException e) {
            log(String.valueOf(e.initCause(e.getCause())), Logs.ERROR);
            for (StackTraceElement error : e.getStackTrace())
                log("        at " + error.toString(), Logs.ERROR);
        }

    }

    private static void setAvatar() {
        File avatar = new File("avatar.png");
        long avatarLastEdit = avatar.lastModified();
        if (avatarLastEdit != SelfData.get().getPreviousAvatarEdited()) {
            try {
                log("Detected avatar change! Updating bot avatar...");
                jda.getSelfUser().getManager().setAvatar(Icon.from(avatar)).queueAfter(5, TimeUnit.SECONDS);
                SelfData.get().setPreviousAvatarEdited(avatarLastEdit);
            } catch (IOException e) {
                log(String.valueOf(e.initCause(e.getCause())), Logs.ERROR);
                for (StackTraceElement error : e.getStackTrace())
                    log("        at " + error.toString(), Logs.ERROR);
            }
        }
    }

    private static void setBotName() {
        // If previous bot name doesn't match config bot name, update bot name
        if (!Config.get().getBotName().equals(SelfData.get().getPreviousBotName())) {
            log("Detected config name change! Updating bot name...");
            jda.getSelfUser().getManager().setName(Config.get().getBotName()).queueAfter(5, TimeUnit.SECONDS);
            SelfData.get().setPreviousBotName(Config.get().getBotName());
        }
    }

    private static void loadMySQL() {
        BaseDatabase.getInstance().init(
                Config.get().getSqlHostname(),
                Config.get().getSqlPort(),
                Config.get().getSqlDatabase(),
                Config.get().getSqlUsername(),
                Config.get().getSqlPassword()
        );
    }

}
