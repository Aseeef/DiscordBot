package net.grandtheftmc.discordbot.utils.litebans;

import java.sql.Timestamp;
import java.util.UUID;

public class Ban {

    private final int id;
    private final UUID uuid;
    private final String ip;
    private final String reason;
    private final UUID banUuid;
    private final String banName;
    private final UUID unbanUuid;
    private final String unbanName;
    private final Timestamp unbanTime;
    private final long banTime;
    private final long expireTime;
    private final String serverScope;
    private final String serverOrigin;
    private final boolean silent;
    private final boolean ipBan;
    private final boolean active;
    private boolean permanent = false;

    public Ban(int id, UUID uuid, String ip, String reason, UUID banUuid, String banName, UUID unbanUuid, String unbanName, Timestamp unbanTime, long banTime, long expireTime, String serverScope, String serverOrigin, boolean silent, boolean ipBan, boolean active) {
        this.id = id;
        this.uuid = uuid;
        this.ip = ip;
        this.reason = reason;
        this.banUuid = banUuid;
        this.banName = banName;
        this.unbanUuid = unbanUuid;
        this.unbanName = unbanName;
        this.unbanTime = unbanTime;
        this.banTime = banTime;
        this.expireTime = expireTime;
        this.serverScope = serverScope;
        this.serverOrigin = serverOrigin;
        this.silent = silent;
        this.ipBan = ipBan;
        this.active = active;

        if (expireTime == -1) this.permanent = true;
    }

    public int getId() {
        return id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getIp() {
        return ip;
    }

    public String getReason() {
        return reason;
    }

    public UUID getBanUuid() {
        return banUuid;
    }

    public String getBanName() {
        return banName;
    }

    public UUID getUnbanUuid() {
        return unbanUuid;
    }

    public String getUnbanName() {
        return unbanName;
    }

    public Timestamp getUnbanTime() {
        return unbanTime;
    }

    public long getBanTime() {
        return banTime;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public String getServerScope() {
        return serverScope;
    }

    public String getServerOrigin() {
        return serverOrigin;
    }

    public boolean isSilent() {
        return silent;
    }

    public boolean isIpBan() {
        return ipBan;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isPermanent() {
        return permanent;
    }
}
