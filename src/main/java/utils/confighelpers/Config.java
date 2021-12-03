package utils.confighelpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.Getter;
import utils.Utils;

import java.io.File;
import java.io.IOException;

@Getter
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
    private int clickUpWaitDuration;

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

}
