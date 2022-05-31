package net.grandtheftmc.discordbot.utils.confighelpers;

public class RaidmodeSettings {

    private int raidModePlayers;
    private int raidModeTime;
    private int raidModeDisable;
    private int raidModePunishTime;
    private int raidModeTimeToAccept;
    private String raidModePunishType;
    private String raidModeMessage;

    public RaidmodeSettings() {
    }

    public RaidmodeSettings(int raidModePlayers, int raidModeTime, int raidModeDisable, int raidModePunishTime, int raidModeTimeToAccept, String raidModePunishType, String raidModeMessage) {
        this.raidModePlayers = raidModePlayers;
        this.raidModeTime = raidModeTime;
        this.raidModeDisable = raidModeDisable;
        this.raidModePunishTime = raidModePunishTime;
        this.raidModeTimeToAccept = raidModeTimeToAccept;
        this.raidModePunishType = raidModePunishType;
        this.raidModeMessage = raidModeMessage;
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
}
