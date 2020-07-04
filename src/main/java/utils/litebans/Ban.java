package utils.litebans;

import java.sql.Timestamp;
import java.util.UUID;

public class Ban {

    private int id;
    private UUID uuid;
    private String ip;
    private String reason;
    private UUID banUuid;
    private String banName;
    private UUID unbanUuid;
    private String unbanName;
    private Timestamp unbanTime;
    private long banTime;
    private long expireTime;
    private String serverScope;
    private String serverOrigin;
    private boolean silent;
    private boolean ipBan;
    private boolean active;
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
