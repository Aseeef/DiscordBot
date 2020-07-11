package utils.confighelpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import utils.tools.GTools;

import java.io.File;
import java.io.IOException;

public class Config {

    private String botToken;
    private String botName;
    private String commandPrefix;
    private int msgDeleteTime;
    private int customChannelDeleteTime;
    private DatabaseCreds usersDatabase;
    private DatabaseCreds planDatabase;
    private DatabaseCreds bansDatabase;
    private DatabaseCreds xenDatabase;
    private DatabaseCreds redisDatabase;
    private MineStatSettings mineStatSettings;
    private RaidmodeSettings raidmodeSettings;
    private RankSettings rankSettings;

    private static Config config;


    public Config() {
    }

    public Config(String botToken, String botName, String commandPrefix, int msgDeleteTime, int customChannelDeleteTime, DatabaseCreds usersDatabase, DatabaseCreds planDatabase, DatabaseCreds bansDatabase, DatabaseCreds xenDatabase, DatabaseCreds redisDatabase, MineStatSettings mineStatSettings, RaidmodeSettings raidmodeSettings, RankSettings rankSettings) {
        this.botToken = botToken;
        this.botName = botName;
        this.commandPrefix = commandPrefix;
        this.msgDeleteTime = msgDeleteTime;
        this.customChannelDeleteTime = customChannelDeleteTime;
        this.usersDatabase = usersDatabase;
        this.planDatabase = planDatabase;
        this.bansDatabase = bansDatabase;
        this.xenDatabase = xenDatabase;
        this.redisDatabase = redisDatabase;
        this.mineStatSettings = mineStatSettings;
        this.raidmodeSettings = raidmodeSettings;
        this.rankSettings = rankSettings;
    }

    public static Config get() {
        return config;
    }

    public static void load() {
        try {
            File file = new File("config.yml");
            // Load the object back
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            config = mapper.readValue(file, Config.class);

        } catch (IOException e) {
            GTools.printStackError(e);
        }
    }

    public String getBotToken() {
        return botToken;
    }

    public String getBotName() {
        return botName;
    }

    public String getCommandPrefix() {
        return commandPrefix;
    }

    public int getMsgDeleteTime() {
        return msgDeleteTime;
    }

    public int getCustomChannelDeleteTime() {
        return customChannelDeleteTime;
    }

    public DatabaseCreds getUsersDatabase() {
        return usersDatabase;
    }

    public DatabaseCreds getPlanDatabase() {
        return planDatabase;
    }

    public DatabaseCreds getBansDatabase() {
        return bansDatabase;
    }

    public DatabaseCreds getXenDatabase() {
        return xenDatabase;
    }

    public DatabaseCreds getRedisDatabase() {
        return redisDatabase;
    }

    public MineStatSettings getMineStatSettings() {
        return mineStatSettings;
    }

    public RaidmodeSettings getRaidmodeSettings() {
        return raidmodeSettings;
    }

    public RankSettings getRankSettings() {
        return rankSettings;
    }

}
