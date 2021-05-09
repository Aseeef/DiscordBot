package commands.stats.wrappers;

import commands.stats.PlanServer;
import org.jetbrains.annotations.Nullable;
import utils.Data;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Session {

    long startTime;
    long playtime;
    long afkTime;
    PlanServer server;

    public Session(long startTime, long playtime, long afkTime, PlanServer server) {
        this.startTime = startTime;
        this.playtime = playtime;
        this.afkTime = afkTime;
        this.server = server;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getActivePlaytime() {
        return playtime;
    }

    public long getPlaytime() {
        return playtime + afkTime;
    }

    public long getAfkTime() {
        return afkTime;
    }

    public PlanServer getServer() {
        return server;
    }

    public static long getActivePlaytime(List<Session> sessions) {
        return getActivePlaytime(sessions, null);
    }

    public static long getActivePlaytime(List<Session> sessions, @Nullable PlanServer server) {
        long activePlaytime = 0;
        for (Session session : sessions) {
            if (server != null && session.getServer() != server)
                continue;
            activePlaytime += session.getActivePlaytime();
        }
        return activePlaytime;
    }

    public static long getTotalPlaytime(List<Session> sessions) {
        return getTotalPlaytime(sessions, null);
    }

    public static long getTotalPlaytime(List<Session> sessions, @Nullable PlanServer server) {
        long playtime = 0;
        for (Session session : sessions) {
            if (server != null && session.getServer() != server)
                continue;
            playtime += session.getPlaytime();
        }
        return playtime;
    }

    public static long getTotalAFK(List<Session> sessions) {
        return getTotalAFK(sessions, null);
    }

    public static long getTotalAFK(List<Session> sessions, @Nullable PlanServer server) {
        long afk = 0;
        for (Session session : sessions) {
            if (server != null && session.getServer() != server)
                continue;
            afk += session.getAfkTime();
        }
        return afk;
    }

    public static PlanServer getFavoriteServer (List<Session> sessions) {
        HashMap<PlanServer, Long> serverPlaytime = new HashMap<>();
        for (PlanServer server : PlanServer.values()) {
            serverPlaytime.put(server, getActivePlaytime(sessions, server));
        }

        Map.Entry<PlanServer,Long> maxEntry = null;

        for (Map.Entry<PlanServer,Long> set : serverPlaytime.entrySet()) {
            if (maxEntry == null || set.getValue() > maxEntry.getValue()) {
                maxEntry = set;
            }
        }

        return maxEntry.getKey();
    }

}
