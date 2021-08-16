package utils.confighelpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import utils.Utils;

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
    private int ticketPollingRate;
    private String dummyAccountUsername;
    private String dummyAccountPassword;
    private String clickUpKey;
    private int clickUpRefreshFrequency;

    private static Config config;


    public Config() {
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
            Utils.printStackError(e);
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

    public int getTicketPollingRate() {
        return ticketPollingRate;
    }

    public String getDummyAccountUsername() {
        return dummyAccountUsername;
    }

    public String getDummyAccountPassword() {
        return dummyAccountPassword;
    }

    public String getClickUpKey() {
        return clickUpKey;
    }

    public int getClickUpRefreshFrequency() {
        return clickUpRefreshFrequency;
    }
}
