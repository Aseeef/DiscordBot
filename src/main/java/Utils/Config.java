package Utils;

import Utils.tools.GTools;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;

public class Config {

    private String botToken;
    private String botName;
    private String commandPrefix;
    private int deleteTime;
    private String sqlHostname;
    private int sqlPort;
    private String sqlUsername;
    private String sqlPassword;
    private String sqlDatabase;
    private String serverIp;
    private int serverPort;
    private int mineStatRefresh;
    private int raidModePlayers;
    private int raidModeTime;
    private int raidModeDisable;
    private int raidModePunishTime;
    private int raidModeTimeToAccept;
    private String raidModePunishType;
    private String raidModeMessage;
    private long manager;
    private long developer;
    private long admin;
    private long builder;
    private long srMod;
    private long mod;
    private long helper;
    private long buildTeam;
    private long youtuber;
    private long supreme;
    private long sponsor;
    private long elite;
    private long premium;
    private long vip;
    private long noRank;
    private long unverified;

    private static Config config;


    public Config() {
    }

    public Config(String botToken, String botName, String commandPrefix, int deleteTime,
                  String sqlHostname, int sqlPort, String sqlUsername, String sqlPassword, String sqlDatabase,
                  String serverIp, int serverPort, int mineStatRefresh,
                  int raidModePlayers, int raidModeTime, int raidModeDisable, int raidModePunishTime, int raidModeTimeToAccept, String raidModePunishType, String raidModeMessage,
                  long manager, long developer, long admin, long builder, long srMod, long mod, long helper, long buildTeam, long youtuber, long supreme, long sponsor, long elite, long premium, long vip, long noRank, long unverified) {
        this.botToken = botToken;
        this.botName = botName;
        this.commandPrefix = commandPrefix;
        this.deleteTime = deleteTime;
        this.sqlHostname = sqlHostname;
        this.sqlPort = sqlPort;
        this.sqlUsername = sqlUsername;
        this.sqlPassword = sqlPassword;
        this.sqlDatabase = sqlDatabase;
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.mineStatRefresh = mineStatRefresh;
        this.raidModePlayers = raidModePlayers;
        this.raidModeTime = raidModeTime;
        this.raidModeTimeToAccept = raidModeTimeToAccept;
        this.raidModeDisable = raidModeDisable;
        this.raidModePunishTime = raidModePunishTime;
        this.raidModePunishType = raidModePunishType;
        this.raidModeMessage = raidModeMessage;
        this.manager = manager;
        this.developer = developer;
        this.admin = admin;
        this.builder = builder;
        this.srMod = srMod;
        this.mod = mod;
        this.helper = helper;
        this.buildTeam = buildTeam;
        this.youtuber = youtuber;
        this.supreme = supreme;
        this.sponsor = sponsor;
        this.elite = elite;
        this.premium = premium;
        this.vip = vip;
        this.noRank = noRank;
        this.unverified = unverified;
    }

    public static Config get() {
        return config;
    }

    public static void load() {
        try {
        File file = new File("config.yml");

        try {
            // Load the object back
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            config = mapper.readValue(file, Config.class);
        } catch (UnrecognizedPropertyException e) {
            GTools.printStackError(e);
        }

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

    public int getDeleteTime() {
        return deleteTime;
    }

    public String getSqlHostname() {
        return sqlHostname;
    }

    public int getSqlPort() {
        return sqlPort;
    }

    public String getSqlUsername() {
        return sqlUsername;
    }

    public String getSqlPassword() {
        return sqlPassword;
    }

    public String getSqlDatabase() {
        return sqlDatabase;
    }

    public String getServerIp() {
        return serverIp;
    }

    public int getServerPort() {
        return serverPort;
    }

    public int getMineStatRefresh() {
        return mineStatRefresh;
    }

    public int getRaidModePlayers() {
        return raidModePlayers;
    }

    public int getRaidModeTime() {
        return raidModeTime;
    }

    public int getRaidModeDisable() {
        return raidModeDisable;
    }

    public int getRaidModePunishTime() {
        return raidModePunishTime;
    }

    public int getRaidModeTimeToAccept() {
        return raidModeTimeToAccept;
    }

    public String getRaidModePunishType() {
        return raidModePunishType;
    }

    public void setRaidModePunishType(String raidModePunishType) {
        this.raidModePunishType = raidModePunishType;
    }

    public String getRaidModeMessage() {
        return raidModeMessage;
    }

    public long getManager() {
        return manager;
    }

    public long getDeveloper() {
        return developer;
    }

    public long getAdmin() {
        return admin;
    }

    public long getBuilder() {
        return builder;
    }

    public long getSrMod() {
        return srMod;
    }

    public long getMod() {
        return mod;
    }

    public long getHelper() {
        return helper;
    }

    public long getBuildTeam() {
        return buildTeam;
    }

    public long getYoutuber() {
        return youtuber;
    }

    public long getSupreme() {
        return supreme;
    }

    public long getSponsor() {
        return sponsor;
    }

    public long getElite() {
        return elite;
    }

    public long getPremium() {
        return premium;
    }

    public long getVip() {
        return vip;
    }

    public long getNoRank() {
        return noRank;
    }

    public long getUnverified() {
        return unverified;
    }

}
