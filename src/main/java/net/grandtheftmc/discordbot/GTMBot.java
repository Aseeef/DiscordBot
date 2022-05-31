package net.grandtheftmc.discordbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.grandtheftmc.discordbot.commands.*;
import net.grandtheftmc.discordbot.commands.bugs.BugReportCommand;
import net.grandtheftmc.discordbot.commands.bugs.ReportListener;
import net.grandtheftmc.discordbot.commands.message.ConditionalMessageCommand;
import net.grandtheftmc.discordbot.commands.stats.StatsCommand;
import net.grandtheftmc.discordbot.commands.suggestions.SuggestAdminCommand;
import net.grandtheftmc.discordbot.commands.suggestions.SuggestionListener;
import net.grandtheftmc.discordbot.events.GuildReaction;
import net.grandtheftmc.discordbot.events.OnGuildMessage;
import net.grandtheftmc.discordbot.events.OnJoin;
import net.grandtheftmc.discordbot.utils.BotData;
import net.grandtheftmc.discordbot.utils.MembersCache;
import net.grandtheftmc.discordbot.utils.Utils;
import net.grandtheftmc.discordbot.utils.confighelpers.Config;
import net.grandtheftmc.discordbot.utils.console.Console;
import net.grandtheftmc.discordbot.utils.console.Logs;
import net.grandtheftmc.discordbot.utils.database.redis.OnRecieveMessageGTM;
import net.grandtheftmc.discordbot.utils.database.sql.BaseDatabase;
import net.grandtheftmc.discordbot.utils.selfdata.AnnoyData;
import net.grandtheftmc.discordbot.utils.selfdata.ChannelData;
import net.grandtheftmc.discordbot.utils.selfdata.ChannelIdData;
import net.grandtheftmc.discordbot.utils.threads.ThreadUtil;
import net.grandtheftmc.discordbot.utils.tools.MineStat;
import net.grandtheftmc.discordbot.utils.users.GTMUser;
import net.grandtheftmc.discordbot.utils.web.clickup.ClickUpPollTask;
import net.grandtheftmc.discordbot.xenforo.Xenforo;
import net.grandtheftmc.simplejedis.SimpleJedisManager;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static net.grandtheftmc.discordbot.utils.console.Logs.log;

public class GTMBot extends ListenerAdapter {

    private static JDA jda;
    private static Guild guild;
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
        System.out.println("Loading MineStat data for GTM...");
        //todo: mineStat = new MineStat(Config.get().getMineStatSettings().getServerIp(), Config.get().getMineStatSettings().getServerPort());

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
            jda.addEventListener(new GTMBot());
            jda.addEventListener(new OnJoin());
            jda.addEventListener(new GuildReaction());
            jda.addEventListener(new OnGuildMessage());
            jda.addEventListener(new MembersCache());
            jda.addEventListener(new ReportListener());
            jda.addEventListener(new SuggestionListener());

            // Self user settings functions to check if there was utils.config change to prevent
            // unnecessary calls to the discord api which may result in us getting rate limited
            setBotName();
            setAvatar();

        } catch (LoginException | IllegalArgumentException | IllegalStateException e) {
            Utils.printStackError(e);
        }
    }

    @Override
    public void onReady(ReadyEvent event) {

        // Make sure bot is only on one server
        if (GTMBot.getJDA().getGuilds().size() > 1) {
            Logs.log("The GTM Discord bot may not be in more then one server at a time!", Logs.ERROR);
            Logs.log(GTMBot.getJDA().getGuilds().toString(), Logs.ERROR);
            System.exit(0);
        }

        // set static guild variable
        guild = GTMBot.getJDA().getGuilds().get(0);

        // JDA Commands - All Commands MUST BE REGISTERED HERE
        new SuggestAdminCommand();
        new PlayerCountCommand();
        new RaidModeCommand();
        //new SeniorsCommand();
        new HelpCommand();
        new DiscordAccountCommand();
        new StaffAccountCommand();
        new RebootCommand();
        new PingCommand();
        new HarryCommand();
        new StaffCommand();
        new AnnoyCommand();
        new ChannelCommand();
        new StatsCommand();
        new BugReportCommand();
        new ConditionalMessageCommand();

        List<CommandData> commandsData = Command.getCommands().stream().map(Command::getCommandData).collect(Collectors.toList());

        // update commands across guilds (quicker for debugging)
        List<net.dv8tion.jda.api.interactions.commands.Command> jdaCommandList = getGTMGuild().updateCommands().addCommands(commandsData).complete();
        // update commands across everything
        jdaCommandList = jda.updateCommands().addCommands(commandsData).complete();

        // cache all members and once done
        MembersCache.reloadMembersAsync().thenAccept(members -> {

            // load self datas
            ChannelIdData.load();
            AnnoyData.load();
            ChannelData.load();

            // Print finished loading msg
            log("Bot is now online!");

            startTasks();

            log("Player count channel updater task initialized!");

            // INVALID CONFIG OR DATA WARNINGS
            checkRaidModeSettings();
            checkSuggestionsSettings();

            // load all GTM users in to memory
            GTMUser.loadUsers();

        });

    }

    public void onShutdown (ShutdownEvent e) {
        Logs.log("Bot has now been disabled.");
    }

    private void startTasks() {
        // Start updater repeating task to update player count
        ThreadUtil.runTaskTimer(Utils::updateOnlinePlayers, 5000, 1000L * Config.get().getMineStatSettings().getRefreshFrequency());

        // Start wisdom annoy task
        Timer wisdomAnnoyTask = new Timer();
        wisdomAnnoyTask.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Map<Long, Long[]> map = AnnoyData.get().getQuoteAnnoyMap();
                map.forEach( (key, value) -> {
                    long hours = value[0];
                    long lastUpdated = value[1];
                    if (lastUpdated < System.currentTimeMillis() - 1000 * 60 * 60 * hours) {
                        int index = Utils.randomNumber(0, Utils.wiseQuotes.length - 1);
                        GTMBot.getJDA().retrieveUserById(key)
                                .flatMap(User::openPrivateChannel)
                                .flatMap(privateChannel -> privateChannel.sendMessage(Utils.wiseQuotes[index]))
                                .queue();
                        map.put(key, new Long[] {hours, System.currentTimeMillis()});
                        AnnoyData.get().save();
                    }

                });
            }
        }, 5000, 1000 * 60);
    }

    // Invalid raid mode punishment type settings
    private void checkRaidModeSettings() {
        String raidModePunishType = Config.get().getRaidmodeSettings().getRaidModePunishType();
        if (!raidModePunishType.equalsIgnoreCase("BAN") &&
                !raidModePunishType.equalsIgnoreCase("KICK") &&
                !raidModePunishType.equalsIgnoreCase("NOTIFY")) {
            log("Invalid raid mode punishment type configured in the utils.config!" +
                    " Valid punishment types are: NOTIFY, BAN, KICK", Logs.WARNING);
            log("Setting raid punishment type to \"KICK\" for now...", Logs.WARNING);
            Config.get().getRaidmodeSettings().setRaidModePunishType("KICK");
        }
    }

    // Check if suggestions channel not configured
    private void checkSuggestionsSettings() {
        if (GTMBot.getJDA().getTextChannelById(ChannelIdData.get().getSuggestionChannelId()) == null)
            log("The suggestion channel has not been properly configured yet!", Logs.WARNING);
        // Player count channel not configured
        if (GTMBot.getJDA().getVoiceChannelById(ChannelIdData.get().getPlayerCountChannelId()) == null)
            log("The player count display channel has not been properly configured yet!", Logs.WARNING);
        // Raid alerts channel not configured
        if (GTMBot.getJDA().getTextChannelById(ChannelIdData.get().getRaidAlertChannelId()) == null)
            log("The raid alerts channel has not been properly configured yet!", Logs.WARNING);
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

    public static Guild getGTMGuild() {
        return guild;
    }
}
