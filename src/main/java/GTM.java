import commands.*;
import commands.bugs.BugReportCommand;
import commands.bugs.ReportListener;
import commands.stats.StatsCommand;
import commands.suggestions.SuggestionListener;
import commands.suggestions.SuggestionCommand;
import events.*;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.grandtheftmc.jedisnew.NewJedisManager;
import selfevents.CloseEvent;
import selfevents.ReadyEvents;
import utils.MembersCache;
import utils.confighelpers.Config;
import utils.console.Console;
import utils.console.Logs;
import utils.database.redis.OnRedisMessageReceive;
import utils.database.sql.BaseDatabase;
import utils.BotData;
import utils.Utils;
import utils.tools.MineStat;
import utils.web.clickup.ClickUpPollTask;
import xenforo.Xenforo;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static utils.Utils.*;

public class GTM {

    public static void main (String[] args) {

        // Set console output settings
        System.setOut(new Logs.GeneralStream(System.out));
        System.setErr(new Logs.ErrorStream(System.err));
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            System.err.println("Uncaught exception in thread \"" + thread.getName() + "\" caused by " + throwable.initCause(throwable.getCause()));
            throwable.printStackTrace();
        });

        // System/Console settings
        Console.loadShutdownHook();
        Console.loadConsoleCommands();

        // Load utils.config
        System.out.println("Loading bot configuration....");
        Config.load();

        // Load Self Data
        System.out.println("Loading bot data....");
        BotData.load();


        // Load Databases
        System.out.println("Connecting to databases...");
        loadMySQL();
        loadJedis();

        // Load JDA & Xenforo and start bot
        loadJDA();
        Xenforo.dbPollTickets();
        new ClickUpPollTask().run();
    }

    private static void loadJDA() {
        // Set up JDA & set Settings
        System.out.println("Initializing JDA...");

        // Initialize GTM MineStat
        System.out.println("Loading MineStat data on GTM...");
        gtm = new MineStat(Config.get().getMineStatSettings().getServerIp(), Config.get().getMineStatSettings().getServerPort());

        try {
            JDA = JDABuilder.createDefault(Config.get().getBotToken())
                    .setEnabledIntents(
                            GatewayIntent.DIRECT_MESSAGE_REACTIONS,
                            GatewayIntent.DIRECT_MESSAGE_TYPING,
                            GatewayIntent.DIRECT_MESSAGES,
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
            JDA.getPresence().setStatus(OnlineStatus.ONLINE);
            JDA.getPresence().setActivity(Activity.playing("play.mc-gtm.net"));
            JDA.getPresence().setIdle(false);

            // JDA Events
            JDA.addEventListener(new SuggestionListener());
            JDA.addEventListener(new Command.CommandsTools());
            JDA.addEventListener(new ReadyEvents());
            JDA.addEventListener(new CloseEvent());
            JDA.addEventListener(new OnJoin());
            JDA.addEventListener(new GuildReaction());
            JDA.addEventListener(new OnGuildMessage());
            JDA.addEventListener(new MembersCache());
            JDA.addEventListener(new GuildMessageStash());
            JDA.addEventListener(new ReportListener());

            // JDA Commands
            JDA.addEventListener(new SuggestionCommand());
            JDA.addEventListener(new PlayerCountCommand());
            JDA.addEventListener(new RaidModeCommand());
            //jda.addEventListener(new SeniorsCommand());
            JDA.addEventListener(new HelpCommand());
            JDA.addEventListener(new DiscordAccountCommand());
            JDA.addEventListener(new StaffAccountCommand());
            JDA.addEventListener(new RebootCommand());
            JDA.addEventListener(new PingCommand());
            JDA.addEventListener(new HarryCommand());
            JDA.addEventListener(new StaffCommand());
            JDA.addEventListener(new AnnoyCommand());
            JDA.addEventListener(new ChannelCommand());
            JDA.addEventListener(new StatsCommand());
            JDA.addEventListener(new BugReportCommand());

            // Self user settings functions to check if there was utils.config change to prevent
            // unnecessary calls to the discord api which may result in us getting rate limited
            setBotName();
            setAvatar();

        } catch (LoginException | IllegalArgumentException | IllegalStateException e) {
            Utils.printStackError(e);
        }
    }

    private static void setAvatar() {
        File avatar = new File("avatar.png");
        long avatarLastEdit = avatar.lastModified();
        if (avatarLastEdit != BotData.AVATAR_LAST_EDIT.getData(Long.TYPE)) {
            try {
                System.out.println("Detected avatar change! Updating bot avatar...");
                JDA.getSelfUser().getManager().setAvatar(Icon.from(avatar)).queueAfter(5, TimeUnit.SECONDS);
                BotData.AVATAR_LAST_EDIT.setValue(avatarLastEdit);
            } catch (IOException e) {
                Utils.printStackError(e);
            }
        }
    }

    private static void setBotName() {
        // If previous bot name doesn't match utils.config bot name, update bot name
        if (!Config.get().getBotName().equals(BotData.LAST_BOT_NAME)) {
            System.out.println("Detected utils.config name change! Updating bot name...");
            JDA.getSelfUser().getManager().setName(Config.get().getBotName()).queueAfter(5, TimeUnit.SECONDS);
            BotData.LAST_BOT_NAME.setValue(Config.get().getBotName());
        }
    }

    private static void loadMySQL() {
        BaseDatabase.getInstance(BaseDatabase.Database.USERS).init(
                Config.get().getUsersDatabase().getHostname(),
                Config.get().getUsersDatabase().getPort(),
                Config.get().getUsersDatabase().getDatabase(),
                Config.get().getUsersDatabase().getUsername(),
                Config.get().getUsersDatabase().getPassword()
        );
        BaseDatabase.getInstance(BaseDatabase.Database.PLAN).init(
                Config.get().getPlanDatabase().getHostname(),
                Config.get().getPlanDatabase().getPort(),
                Config.get().getPlanDatabase().getDatabase(),
                Config.get().getPlanDatabase().getUsername(),
                Config.get().getPlanDatabase().getPassword()
        );
        BaseDatabase.getInstance(BaseDatabase.Database.BANS).init(
                Config.get().getBansDatabase().getHostname(),
                Config.get().getBansDatabase().getPort(),
                Config.get().getBansDatabase().getDatabase(),
                Config.get().getBansDatabase().getUsername(),
                Config.get().getBansDatabase().getPassword()
        );
        BaseDatabase.getInstance(BaseDatabase.Database.XEN).init(
                Config.get().getXenDatabase().getHostname(),
                Config.get().getXenDatabase().getPort(),
                Config.get().getXenDatabase().getDatabase(),
                Config.get().getXenDatabase().getUsername(),
                Config.get().getXenDatabase().getPassword()
        );
    }

    private static void loadJedis() {
        jedisManager = new NewJedisManager(
                Config.get().getRedisDatabase().getHostname(),
                Config.get().getRedisDatabase().getPort(),
                Config.get().getRedisDatabase().getPassword()
        ).addRedisEventListener(new OnRedisMessageReceive());
        jedisManager.init();

        System.out.println("Established connection to Redis!");
    }

}
