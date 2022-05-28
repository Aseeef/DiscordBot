package net.grandtheftmc.discordbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.grandtheftmc.discordbot.commands.*;
import net.grandtheftmc.discordbot.commands.bugs.BugReportCommand;
import net.grandtheftmc.discordbot.commands.bugs.ReportListener;
import net.grandtheftmc.discordbot.commands.stats.StatsCommand;
import net.grandtheftmc.discordbot.commands.suggestions.SuggestionCommand;
import net.grandtheftmc.discordbot.commands.suggestions.SuggestionListener;
import net.grandtheftmc.discordbot.events.GuildReaction;
import net.grandtheftmc.discordbot.events.OnGuildMessage;
import net.grandtheftmc.discordbot.events.OnJoin;
import net.grandtheftmc.discordbot.selfevents.CloseEvent;
import net.grandtheftmc.discordbot.selfevents.ReadyEvents;
import net.grandtheftmc.discordbot.utils.BotData;
import net.grandtheftmc.discordbot.utils.MembersCache;
import net.grandtheftmc.discordbot.utils.Utils;
import net.grandtheftmc.discordbot.utils.confighelpers.Config;
import net.grandtheftmc.discordbot.utils.console.Console;
import net.grandtheftmc.discordbot.utils.console.Logs;
import net.grandtheftmc.discordbot.utils.database.redis.OnRecieveMessageGTM;
import net.grandtheftmc.discordbot.utils.database.sql.BaseDatabase;
import net.grandtheftmc.discordbot.utils.tools.MineStat;
import net.grandtheftmc.discordbot.utils.web.clickup.ClickUpPollTask;
import net.grandtheftmc.discordbot.xenforo.Xenforo;
import net.grandtheftmc.simplejedis.SimpleJedisManager;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class GTMBot {

    private static JDA jda;
    private static MineStat mineStat;
    private static SimpleJedisManager jedisManager;

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
        new ClickUpPollTask();
        BotData.LAST_COMMIT_POLL.setValue(System.currentTimeMillis());
    }

    private static void loadJDA() {
        // Set up JDA & set Settings
        System.out.println("Initializing JDA...");

        // Initialize GTM MineStat
        System.out.println("Loading MineStat data on GTM...");
        mineStat = new MineStat(Config.get().getMineStatSettings().getServerIp(), Config.get().getMineStatSettings().getServerPort());

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
            jda.getPresence().setActivity(Activity.playing("gtm.network"));
            jda.getPresence().setIdle(false);

            // JDA Events
            jda.addEventListener(new SuggestionListener());
            jda.addEventListener(new ReadyEvents());
            jda.addEventListener(new CloseEvent());
            jda.addEventListener(new OnJoin());
            jda.addEventListener(new GuildReaction());
            jda.addEventListener(new OnGuildMessage());
            jda.addEventListener(new MembersCache());
            jda.addEventListener(new ReportListener());

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
            jda.addEventListener(new BugReportCommand());

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
                jda.getSelfUser().getManager().setAvatar(Icon.from(avatar)).queueAfter(5, TimeUnit.SECONDS);
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
            jda.getSelfUser().getManager().setName(Config.get().getBotName()).queueAfter(5, TimeUnit.SECONDS);
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
        jedisManager = new SimpleJedisManager(
                Config.get().getRedisDatabase().getHostname(),
                Config.get().getRedisDatabase().getPort(),
                Config.get().getRedisDatabase().getPassword(),
                "discord-bot"
        ).addRedisEventListener(new OnRecieveMessageGTM());
        jedisManager.init();

        System.out.println("Established connection to Redis!");
    }

    public static JDA getJDA() {
        return jda;
    }

    public static MineStat getMineStat() {
        return mineStat;
    }

    public static SimpleJedisManager getJedisManager() {
        return jedisManager;
    }
}
