package Utils;

import Utils.tools.Logs;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;

import static Utils.tools.Logs.log;

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
    private String raidModePunishType;
    private String raidModeMessage;
    private String manager;
    private String developer;
    private String admin;
    private String builder;
    private String srMod;
    private String mod;
    private String helper;
    private String buildTeam;
    private String youtuber;
    private String supreme;
    private String sponsor;
    private String elite;
    private String premium;
    private String vip;
    private String noRank;
    private String unverified;

    private static Config config;


    public Config() {
    }

    public Config(String botToken, String botName, String commandPrefix, int deleteTime,
                  String sqlHostname, int sqlPort, String sqlUsername, String sqlPassword, String sqlDatabase,
                  String serverIp, int serverPort, int mineStatRefresh,
                  int raidModePlayers, int raidModeTime, int raidModeDisable, int raidModePunishTime, String raidModePunishType, String raidModeMessage,
                  String manager, String developer, String admin, String builder, String srMod, String mod, String helper, String buildTeam, String youtuber, String supreme, String sponsor, String elite, String premium, String vip, String noRank, String unverified) {
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
            log(String.valueOf(e.initCause(e.getCause())), Logs.ERROR);
            for (StackTraceElement error : e.getStackTrace())
                log("        at " + error.toString(), Logs.ERROR);
        }

        } catch (IOException e) {
            log(String.valueOf(e.initCause(e.getCause())), Logs.ERROR);
            for (StackTraceElement error : e.getStackTrace())
                log("        at " + error.toString(), Logs.ERROR);
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

    public String getRaidModePunishType() {
        return raidModePunishType;
    }

    public void setRaidModePunishType(String raidModePunishType) {
        this.raidModePunishType = raidModePunishType;
    }

    public String getRaidModeMessage() {
        return raidModeMessage;
    }

    public String getManager() {
        return manager;
    }

    public String getDeveloper() {
        return developer;
    }

    public String getAdmin() {
        return admin;
    }

    public String getBuilder() {
        return builder;
    }

    public String getSrMod() {
        return srMod;
    }

    public String getMod() {
        return mod;
    }

    public String getHelper() {
        return helper;
    }

    public String getBuildTeam() {
        return buildTeam;
    }

    public String getYoutuber() {
        return youtuber;
    }

    public String getSupreme() {
        return supreme;
    }

    public String getSponsor() {
        return sponsor;
    }

    public String getElite() {
        return elite;
    }

    public String getPremium() {
        return premium;
    }

    public String getVip() {
        return vip;
    }

    public String getNoRank() {
        return noRank;
    }

    public String getUnverified() {
        return unverified;
    }

}
