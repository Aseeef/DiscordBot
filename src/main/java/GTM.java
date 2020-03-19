import Database.BaseDatabase;
import Utils.Config;
import Utils.MineStat;
import Utils.tools.GTools;
import commands.PlayerCountCommand;
import events.LogCommands;
import events.OnSuggestion;
import commands.SuggestionsCommand;
import events.PlayerCountUpdater;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import Utils.AutoDeleter.AutoDelete;

import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.sql.Connection;

import static Utils.tools.GTools.jda;

public class GTM extends ListenerAdapter {

    public static void main (String[] args) throws LoginException, IOException {

        {

            // Load config
            GTools.log("Loading bot configuration....");
            Config.load();

            // Load SQL Database
            //loadMySQL();

            // Initialize GTM MineStat
            GTools.log("Loading MineStat data on GTM...");
            // 142.44.138.37 is the internal IP for the network
            GTools.gtm = new MineStat("142.44.138.37", 25565);

            // Set up JDA
            GTools.log("Initializing JDA...");

            jda = JDABuilder.createDefault(Config.get().getBotToken()).build();

            // Set presence
            jda.getPresence().setStatus(OnlineStatus.ONLINE);
            jda.getPresence().setActivity(Activity.playing("mc-gtm.net"));
            jda.getPresence().setIdle(false);

            // JDA Events
            jda.addEventListener(new OnSuggestion());
            jda.addEventListener(new AutoDelete());
            jda.addEventListener(new PlayerCountUpdater());
            jda.addEventListener(new ReadyEvents());
            jda.addEventListener(new LogCommands());

            // JDA Commands
            jda.addEventListener(new SuggestionsCommand());
            jda.addEventListener(new PlayerCountCommand());

        }

    }

    private static void loadMySQL() {
        BaseDatabase.getInstance().init(
                Config.get().getSqlHostname(),
                Integer.parseInt(Config.get().getSqlPort()),
                Config.get().getSqlDatabase(),
                Config.get().getSqlUsername(),
                Config.get().getSqlPassword()
        );
    }

}
