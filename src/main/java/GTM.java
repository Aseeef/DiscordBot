import commands.*;
import commands.stats.StatsCommand;
import events.OnGuildMessage;
import events.OnJoin;
import events.GuildReaction;
import events.OnSuggestion;
import me.cadox8.xenapi.XenAPI;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.grandtheftmc.jedisnew.NewJedisManager;
import selfevents.CloseEvent;
import selfevents.ReadyEvents;
import utils.MembersCache;
import utils.SelfData;
import utils.confighelpers.Config;
import utils.console.Console;
import utils.console.Logs;
import utils.database.redis.OnRedisMessageReceive;
import utils.database.sql.BaseDatabase;
import utils.tools.GTools;
import utils.tools.MineStat;
import xenforo.Xenforo;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static utils.tools.GTools.*;

public class GTM extends ListenerAdapter {

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
        SelfData.load();

        // Load Databases
        System.out.println("Connecting to databases...");
        loadMySQL();
        loadJedis();

        // Load JDA & Xenforo and start bot
        loadJDA();
        loadXen();
    }


    private static void loadXen() {
        System.out.println("Initializing Xenforo API...");
        new XenAPI("c1230035-cf85-4e3d-add8-f4457b641d1e", "https://grandtheftmc.net/");
        //Xenforo.login();
        Xenforo.dbPollTickets();
    }

    private static void loadJDA() {
        // Set up JDA & set Settings
        System.out.println("Initializing JDA...");

        // Initialize GTM MineStat
        System.out.println("Loading MineStat data on GTM...");
        gtm = new MineStat(Config.get().getMineStatSettings().getServerIp(), Config.get().getMineStatSettings().getServerPort());

        try {
            jda = JDABuilder.createDefault(Config.get().getBotToken())
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
            jda.getPresence().setStatus(OnlineStatus.ONLINE);
            jda.getPresence().setActivity(Activity.playing("mc-gtm.net"));
            jda.getPresence().setIdle(false);

            // JDA Events
            jda.addEventListener(new OnSuggestion());
            jda.addEventListener(new Command.CommandsTools());
            jda.addEventListener(new ReadyEvents());
            jda.addEventListener(new CloseEvent());
            jda.addEventListener(new OnJoin());
            jda.addEventListener(new GuildReaction());
            jda.addEventListener(new OnGuildMessage());
            jda.addEventListener(new MembersCache());

            // JDA Commands
            jda.addEventListener(new SuggestionCommand());
            jda.addEventListener(new PlayerCountCommand());
            jda.addEventListener(new RaidModeCommand());
            //jda.addEventListener(new SeniorsCommand());
            jda.addEventListener(new HelpCommand());
            jda.addEventListener(new DiscordAccountCommand());
            jda.addEventListener(new StaffAccountCommand());
            jda.addEventListener(new RebootCommand());
            jda.addEventListener(new PingCommand());
            jda.addEventListener(new HarryCommand());
            jda.addEventListener(new StaffCommand());
            jda.addEventListener(new AnnoyCommand());
            jda.addEventListener(new ChannelCommand());
            jda.addEventListener(new StatsCommand());

            // Self user settings functions to check if there was utils.config change to prevent
            // unnecessary calls to the discord api which may result in us getting rate limited
            setBotName();
            setAvatar();

        } catch (LoginException | IllegalArgumentException e) {
            GTools.printStackError(e);
        }
    }

    private static void setAvatar() {
        File avatar = new File("avatar.png");
        long avatarLastEdit = avatar.lastModified();
        if (avatarLastEdit != SelfData.get().getPreviousAvatarEdited()) {
            try {
                System.out.println("Detected avatar change! Updating bot avatar...");
                jda.getSelfUser().getManager().setAvatar(Icon.from(avatar)).queueAfter(5, TimeUnit.SECONDS);
                SelfData.get().setPreviousAvatarEdited(avatarLastEdit);
            } catch (IOException e) {
                GTools.printStackError(e);
            }
        }
    }

    private static void setBotName() {
        // If previous bot name doesn't match utils.config bot name, update bot name
        if (!Config.get().getBotName().equals(SelfData.get().getPreviousBotName())) {
            System.out.println("Detected utils.config name change! Updating bot name...");
            jda.getSelfUser().getManager().setName(Config.get().getBotName()).queueAfter(5, TimeUnit.SECONDS);
            SelfData.get().setPreviousBotName(Config.get().getBotName());
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
