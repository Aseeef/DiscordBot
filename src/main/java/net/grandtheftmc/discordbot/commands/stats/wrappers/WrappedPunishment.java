package net.grandtheftmc.discordbot.commands.stats.wrappers;

import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;
import java.util.UUID;

public class WrappedPunishment {

    private final int id;
    private final UUID uuid;
    private final PunishmentType punishmentType;
    private final String punishIp;
    private final String reason;
    private final UUID punisherUUID;
    private final String punisher;
    private final String remover;
    private final UUID removerUUID;
    private final Timestamp issueDate;
    private final Timestamp endDate;
    private final String serverScope;
    private final String serverOrigin;
    private final boolean silent;
    private final boolean ip;
    private final boolean active;

    public WrappedPunishment(int id, UUID uuid, PunishmentType punishmentType, String punishIp, String reason, @Nullable UUID punisherUUID, String punisher, @Nullable String remover, @Nullable UUID removerUUID, Timestamp issueDate, @Nullable Timestamp endDate, String serverScope, @Nullable String serverOrigin, boolean silent, boolean ip, boolean active) {
        this.id = id;
        this.uuid = uuid;
        this.punishmentType = punishmentType;
        this.punishIp = punishIp;
        this.reason = reason;
        this.punisherUUID = punisherUUID;
        this.punisher = punisher;
        this.remover = remover;
        this.removerUUID =removerUUID;
        this.issueDate = issueDate;
        this.endDate = endDate;
        this.serverScope = serverScope;
        this.serverOrigin = serverOrigin;
        this.silent = silent;
        this.ip = ip;
        this.active = active;
    }

    public int getId() {
        return id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public PunishmentType getPunishmentType() {
        return punishmentType;
    }

    public @Nullable String getPunishIp() {
        return punishIp;
    }

    public String getReason() {
        return reason;
    }

    public @Nullable UUID getPunisherUUID() {
        return punisherUUID;
    }

    public String getPunisher() {
        return punisher;
    }

    public @Nullable String getRemover() {
        return remover;
    }

    public @Nullable UUID getRemoverUUID() {
        return removerUUID;
    }

    public Timestamp getIssueDate() {
        return issueDate;
    }

    public @Nullable Timestamp getEndDate() {
        return endDate;
    }

    public String getServerScope() {
        return serverScope;
    }

    public @Nullable String getServerOrigin() {
        return serverOrigin;
    }

    public boolean isSilent() {
        return silent;
    }

    public boolean isIp() {
        return ip;
    }

    public boolean isActive() {
        return active;
    }

    public enum PunishmentType {
        BAN("litebans_bans"),
        MUTE("litebans_mutes"),
        WARN("litebans_warnings"),
        KICK("litebans_kicks")
        ;
        private final String table;
        PunishmentType(String table) {
            this.table = table;
        }
        public String getTable() {
            return this.table;
        }
    }

}
