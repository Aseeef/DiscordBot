package net.grandtheftmc.discordbot.utils.confighelpers;

public class DatabaseCreds {

    private String hostname;
    private int port;
    private String username;
    private String password;
    private String database;

    public DatabaseCreds() {}

    public DatabaseCreds(String hostname, int port, String username, String password, String database) {
        this.hostname = hostname;
        this.port = port;
        this.username = username;
        this.password = password;
        this.database = database;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDatabase() {
        return database;
    }
}
