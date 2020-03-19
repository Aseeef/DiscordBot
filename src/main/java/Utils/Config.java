package Utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;

public class Config {

    private String botToken;
    private String commandPrefix;
    private long suggestionChannelId;
    private long playerCountChannelId;
    private String sqlHostname;
    private String sqlPort;
    private String sqlUsername;
    private String sqlPassword;
    private String sqlDatabase;

    private static Config config;

    public Config() {
    }

    public Config(String botToken, String commandPrefix, long suggestionChannelId) throws IOException {
        this.botToken = botToken;
        this.commandPrefix = commandPrefix;
        this.suggestionChannelId = suggestionChannelId;

        // Update to file
        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        File file = new File("config.yml");
        om.writer().writeValue(file, config);

    }

    public static Config get() {
        return config;
    }

    public static void load() throws IOException {
        File file = new File("config.yml");
        // Load the object back
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        config = mapper.readValue(file, Config.class);
    }

    public String getBotToken() {
        return botToken;
    }

    public String getCommandPrefix() {
        return commandPrefix;
    }

    public void setCommandPrefix(String commandPrefix) {
        this.commandPrefix = commandPrefix;
    }

    public void setSuggestionChannelId(long suggestionChannelId) throws IOException {
        this.suggestionChannelId = suggestionChannelId;
        // Update to file
        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        File file = new File("config.yml");
        om.writerWithDefaultPrettyPrinter().writeValue(file, this);
    }

    public long getSuggestionChannelId() {
        return suggestionChannelId;
    }

    public void setPlayerCountChannelId(long playerCountChannelId) throws IOException {
        this.playerCountChannelId = playerCountChannelId;
        // Update to file
        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        File file = new File("config.yml");
        om.writerWithDefaultPrettyPrinter().writeValue(file, this);
    }

    public long getPlayerCountChannelId() {
        return playerCountChannelId;
    }

    public String getSqlHostname() {
        return sqlHostname;
    }

    public String getSqlPort() {
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

}
