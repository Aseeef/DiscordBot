package commands.stats.wrappers;

import commands.stats.PlanServer;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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

    public static HashMap<String,Integer> getPlaytimeMapFromDailySessions(List<Session> dailySessions) {

        HashMap<String,Integer> map = new HashMap<>();
        String[] times = {"00:00", "00:30", "01:00", "01:30", "02:00", "02:30", "03:00", "03:30", "04:00", "04:30", "05:00", "05:30", "06:00", "06:30", "07:00", "07:30", "08:00", "08:30", "09:00", "09:30", "10:00", "10:30", "12:00", "12:30", "13:00", "13:30", "14:00", "14:30", "15:00", "15:30", "16:00", "16:30", "17:00", "17:30", "18:00", "18:30", "19:00", "19:30", "20:00", "20:30", "21:00", "21:30", "22:00", "22:30", "23:00", "23:30"};

        if (dailySessions.size() > 1) {

            ZoneId z = ZoneId.of("America/New_York");
            long hour = ZonedDateTime.ofInstant(Instant.ofEpochMilli(dailySessions.get(0).getStartTime()), z).toLocalDate().atStartOfDay(z).toEpochSecond() * 1000;

            for (int i = 0 ; i < 48 ; i++) {

                hour += (1000 * 60 * 30 * i);
                long time = hour + ((long) (1000 * 60 * (Math.random() * 30)));

                if (wasOnlineAt(time, dailySessions)) {
                    map.putIfAbsent(times[i], 0);
                    map.put(times[i], map.get(times[i]) + 1);
                }

            }

        }

        return map;
    }

    public static boolean wasOnlineAt (long time, List<Session> sessions) {

        for (Session session : sessions) {
            long start = session.getStartTime();
            long end = start + session.getPlaytime();

            if (time > start && time < end)
                return true;
        }

        return false;
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
