package utils.confighelpers;

public class MineStatSettings {

    private String serverIp;
    private int serverPort;
    private int refreshFrequency;

    public MineStatSettings() {
    }

    public MineStatSettings(String serverIp, int serverPort, int refreshFrequency) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.refreshFrequency = refreshFrequency;
    }

    public String getServerIp() {
        return serverIp;
    }

    public int getServerPort() {
        return serverPort;
    }

    public int getRefreshFrequency() {
        return refreshFrequency;
    }
}
