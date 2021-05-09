package commands.stats;

import commands.stats.wrappers.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.AttachmentOption;
import utils.chart.PlaytimeChart;
import utils.database.XenforoDAO;
import utils.pagination.DiscordMenu;
import utils.pagination.MenuAction;
import utils.tools.GTools;
import utils.users.GTMUser;
import xenforo.objects.tickets.Department;
import xenforo.objects.tickets.SupportTicket;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static utils.tools.GTools.jda;

public class StatsMenu implements MenuAction {

    private DiscordMenu menu;
    private User creator;

    private EmbedBuilder generalStats;
    private EmbedBuilder punishmentStats;
    private EmbedBuilder playtimeStats;
    private EmbedBuilder gangStats;

    private String username;
    private MessageChannel channel;

    private UUID uuid;
    private PlanUser pu;
    private List<Session> sessions;
    private WrappedIPData lastIp;
    private List<String> ipHistory;
    private GTMUser targetUser;
    private List<String> recentAlts;
    private List<String> allAlts;
    private List<WrappedPunishment> bans;
    private List<WrappedPunishment> mutes;
    private List<WrappedPunishment> warns;
    private List<String> usernames;
    private String skullURL;
    private List<GTMGang> gangs;

    public StatsMenu(User user, MessageChannel channel, String username) {
        this.creator = user;
        this.channel = channel;
        this.username = username;
    }

    public boolean load() {
        uuid = GTools.getUUID(username).orElse(null);
        if (uuid == null) return false;

        pu = StatsDAO.getPlanUser(uuid);
        if (pu == null) return false;

        Thread thread = new Thread(() -> {

            CompletableFuture<DiscordMenu> futureMenu = DiscordMenu.create(channel, getPendingEmbed(1), 4);
            loadRawData();

            try {
                menu = futureMenu.get();
                menu.setPageContents(getPendingEmbed(2));

                generalStats = getGeneralStats();
                punishmentStats = getPunishmentStats();
                playtimeStats = getPlaytimeStats();
                gangStats = getGangStats();

                menu.setPageContents(getGeneralStats());
                menu.onMenuAction(this);

            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

        });
        thread.start();

        return true;
    }

    @Override
    public void onAction(Type actionType, User user) {
        if (actionType == Type.DELETE) return;

        int page = menu.getPage();
        if (page == 1) {
            menu.setPageContents(generalStats);
        } else if (page == 2) {
            menu.setPageContents(punishmentStats);
        } else if (page == 3) {
            menu.setPageContents(playtimeStats);
        } else if (page == 4) {
            menu.setPageContents(gangStats);
        }

        // /stats (

        /*

        General overview:
        - Username
        - First join
        - Total playtime (aft and active)
        - Favorite server
        - previous user names
        - alts (full alt hist + non full)
        - linked discord?
        - timezone


        Punishments:
        - Total warnings
        - Total mutes
        - Total bans
        - Last punishment
        - Ban appeals made

        Recent Playtime Stats:
        - Stats about playtime afk and non afk
        - Playtime stats break down by days like in a graph using JFreeChart
        - general time they are online around

        Recent PvP Stats:
        - Stats about kills
        - KDR
        - Kills per hour
        - recent / total
        - time spent in warzone?

        Gang:
        - Gang name
        - Whos in the gang

        Staff stats
        - Help questions answered
        - avg length of help question
        - Punishments given break down
        - % help questions answered while online
        - % of help questions no one answered while they were online
        - time spent spectating?

         */
    }

    private void loadRawData() {
        sessions = StatsDAO.getSessions(uuid,null, null);
        ipHistory = StatsDAO.getIPs(uuid);
        lastIp = StatsDAO.getIpInfo(ipHistory.get(0));
        allAlts = StatsDAO.getAllAlts(uuid, ipHistory);
        recentAlts = lastIp == null ? new ArrayList<>() : StatsDAO.getAlts(uuid, lastIp.getIp());
        recentAlts.remove(username);
        allAlts.remove(username);
        targetUser = GTMUser.getGTMUser(uuid).orElse(null);
        System.out.println(uuid);
        bans = StatsDAO.getPunishments(uuid, WrappedPunishment.PunishmentType.BAN);
        mutes = StatsDAO.getPunishments(uuid, WrappedPunishment.PunishmentType.MUTE);
        warns = StatsDAO.getPunishments(uuid, WrappedPunishment.PunishmentType.WARN);
        usernames = GTools.getAllUsernames(uuid);
        skullURL = GTools.getSkullSkin(uuid);
        gangs = StatsDAO.getGangs(uuid);
    }

    private EmbedBuilder getGangStats() {

        EmbedBuilder eb = new EmbedBuilder();

        eb.setTitle("**Gang Info for `" + pu.getUsername() + "`:**");
        eb.setDescription("*The following is the gang information for " + pu.getUsername() + " on all GTM servers.*");

        if (gangs != null) {
            eb.addBlankField(false);

            int j = 0;
            for (GTMGang gang : gangs) {

                StringBuilder sb = new StringBuilder();
                int i = 0;
                for (String member : gang.getMembers()) {
                    if (i != 0) sb.append(", ");
                    i++;
                    sb.append(member);
                }

                String members = sb.toString();
                if (members.length() >= MessageEmbed.VALUE_MAX_LENGTH) {
                    members = members.substring(0, MessageEmbed.VALUE_MAX_LENGTH - 6);
                    members += "...";
                }

                eb.addField(gang.getServer() + " Gang Name", gang.getName(), false)
                        .addField(gang.getServer() + " Gang Description", gang.getDescription(), false)
                        .addField(gang.getServer() + " Gang Members", "`" + members + "`", false);

                if (j != gangs.size() - 1) {
                    eb.addBlankField(false);
                }

                j++;
            }

        }

        return eb;
    }
    
    private EmbedBuilder getPlaytimeStats() {

        EmbedBuilder eb = new EmbedBuilder();

        eb.setTitle("**Playtime Statistics for `" + pu.getUsername() + "`:**");

        List<Session> sessions30 = new ArrayList<>(sessions);
        sessions30.removeIf(session ->
                session.getStartTime() < (System.currentTimeMillis() - (1000L * 60 * 60 * 24 * 30)));
        List<Session> sessions7 = new ArrayList<>(sessions);
        sessions7.removeIf(session ->
                session.getStartTime() < (System.currentTimeMillis() - (1000L * 60 * 60 * 24 * 7)));

        eb.addField("7d Playtime", GTools.epochToTime(Session.getTotalPlaytime(sessions7)), true)
                .addField("7d Active Playtime", GTools.epochToTime(Session.getActivePlaytime(sessions7)), true)
                .addField("7d AFK Playtime", GTools.epochToTime(Session.getTotalAFK(sessions7)), true)
                .addField("30d Playtime", GTools.epochToTime(Session.getTotalPlaytime(sessions30)), true)
                .addField("30d Active Playtime", GTools.epochToTime(Session.getActivePlaytime(sessions30)), true)
                .addField("30d AFK Playtime", GTools.epochToTime(Session.getTotalAFK(sessions30)), true);

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd-EEE");
        LinkedList<String> keys = new LinkedList<>();
        LinkedList<Double> totalPlaytime = new LinkedList<>();
        LinkedList<Double> activePlaytime = new LinkedList<>();
        LinkedList<Double> afkPlaytime = new LinkedList<>();

        for (int i = 0 ; i < 30 ; i++) {

            long rawLower = System.currentTimeMillis() - (1000L * 60 * 60 * 24 * (i + 1));

            ZoneId z = ZoneId.of("America/New_York");
            long lower = ZonedDateTime.ofInstant(Instant.ofEpochMilli(rawLower), z).toLocalDate().atStartOfDay(z).toEpochSecond() * 1000 ;
            long upper = lower - (1000L * 60 * 60 * 24);

            List<Session> clonedSessions2 = new ArrayList<>(sessions);
            clonedSessions2.removeIf(session ->
                    session.getStartTime() < upper ||
                            session.getStartTime() > lower);

            totalPlaytime.add(Session.getTotalPlaytime(clonedSessions2) / 1000 / 60 / 60D);
            activePlaytime.add(Session.getActivePlaytime(clonedSessions2) / 1000 / 60 / 60D);
            afkPlaytime.add(Session.getTotalAFK(clonedSessions2) / 1000 / 60 / 60D);

            keys.add(sdf.format(new Date(rawLower)));
        }

        try {
            PlaytimeChart pc = new PlaytimeChart(username + "'s Playtime", Color.GREEN, 2500, 1100);
            InputStream stream = pc.get(keys, totalPlaytime, activePlaytime, afkPlaytime);
            //TODO, pull id from a config - this channel is needed because we need a link to set the image
            Message msg = jda.getTextChannelById(631612007384350733L).sendFile(stream, username + "_PLAYTIME.png").complete();
            eb.setImage(msg.getAttachments().get(0).getUrl());
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return eb;

    }

    private EmbedBuilder getPunishmentStats() {

        EmbedBuilder eb = new EmbedBuilder();

        eb.setTitle("**Punishment Statistics for `" + pu.getUsername() + "`:**");

        LinkedList<WrappedPunishment> allPunishments = new LinkedList<>();
        allPunishments.addAll(bans);
        allPunishments.addAll(mutes);
        allPunishments.addAll(warns);
        allPunishments.sort(Comparator.comparing(WrappedPunishment::getIssueDate));

        eb.setDescription("*Please note that these punishment statistics are only for the account `" + pu.getUsername() + "`. They do not include any punishments on alts.*");
        eb.addField("Total Punishments", String.valueOf(allPunishments.size()), true)
                .addField("Total Bans", String.valueOf(bans.size()), true)
                .addField("Total Mutes", String.valueOf(mutes.size()), true)
                .addField("Total Warns", String.valueOf(warns.size()), true)
                .addField("LiteBans Dashboard", "https://grandtheftmc.net/bans/history.php?uuid=" + uuid.toString(), false);

        if (allPunishments.size() > 0) {
            WrappedPunishment lastPunishment = allPunishments.getLast();
            eb.addBlankField(false)
                    .addField("Recent Punishment Type", lastPunishment.getPunishmentType().toString(), true)
                    .addField("Recent Punishment Duration", lastPunishment.getEndDate() == null ? "Permanent" : GTools.epochToTime(lastPunishment.getEndDate().toInstant().toEpochMilli() - lastPunishment.getIssueDate().toInstant().toEpochMilli()), true)
                    .addField("Recent Punishment Issues", GTools.epochToTime(System.currentTimeMillis() - lastPunishment.getIssueDate().toInstant().toEpochMilli()), true)
                    .addField("Recent Punishment Issuer", lastPunishment.getPunisher() == null ? "CONSOLE" : lastPunishment.getPunisher(), true)
                    .addField("Recent Punishment Reason", lastPunishment.getReason(), true);
        }

        List<SupportTicket> tickets = new ArrayList<>(XenforoDAO.getAllTicketsFrom(username));
        tickets.removeIf( (ticket) -> ticket.getDepartment() != Department.PUNISHMENT_APPEALS);
        String appeals = null;
        if (tickets.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (SupportTicket ticket : tickets) {
                StringBuilder testSb = new StringBuilder(sb).append(ticket.getTicketLink());
                if (testSb.length() < 1000)
                    sb.append(ticket.getTicketLink()).append("\n");
                else {
                    sb.append("and more...");
                    break;
                }
            }
            appeals = sb.toString();
        }

        if (appeals != null) {
            eb.addField("Previous Appeals", appeals, false);
        }

        return eb;
    }

    private EmbedBuilder getGeneralStats() {
        if (usernames != null)
            usernames.remove(pu.getUsername());
        String prevUsernames = usernames == null || usernames.size() == 0 ? "None" : usernames.toString().replaceAll("\\[", "").replaceAll("]", "");
        if (prevUsernames.length() >= MessageEmbed.VALUE_MAX_LENGTH) {
            prevUsernames = prevUsernames.substring(0, MessageEmbed.VALUE_MAX_LENGTH - 6);
            prevUsernames += "...";
        }
        // get all linked alts
        String altString = allAlts.size() == 0 ? "None" : allAlts.toString().replaceAll("\\[", "").replaceAll("]", "");
        if (altString.length() >= MessageEmbed.VALUE_MAX_LENGTH) {
            altString = altString.substring(0, MessageEmbed.VALUE_MAX_LENGTH - 6);
            altString += "...";
        }
        // get alts from current ip
        String recentAltsString = recentAlts == null || recentAlts.size() == 0 ? "None" : recentAlts.toString().replaceAll("\\[", "").replaceAll("]", "");
        if (recentAltsString.length() >= MessageEmbed.VALUE_MAX_LENGTH) {
            recentAltsString = recentAltsString.substring(0, MessageEmbed.VALUE_MAX_LENGTH - 6);
            recentAltsString += "...";
        }

        EmbedBuilder eb = new EmbedBuilder();

        eb.setTitle("**General Statistics for `" + pu.getUsername() + "`:**");

        eb.setThumbnail(skullURL);

        eb.addField("Username", "`" + pu.getUsername() + "`", true)
                .addField("Linked Discord", targetUser == null || !targetUser.getDiscordMember().isPresent() ? "None" : targetUser.getDiscordMember().get().getAsMention(), true)
                .addField("Previous Username(s)", "`" + prevUsernames  + "`", false)
                .addField("Current Alts", "`" + recentAltsString + "`", false)
                .addField("All Linked Alts", "`" + altString + "`", false)
                .addField("First Join Date", GTools.epochToDate(pu.getRegistered()), true)
                .addField("Total Playtime", GTools.epochToTime(Session.getTotalPlaytime(sessions)), true)
                .addField("Active Playtime", GTools.epochToTime(Session.getActivePlaytime(sessions)), true)
                .addField("AFK Playtime", GTools.epochToTime(Session.getTotalAFK(sessions)), true)
                .addField("Favorite Server", Session.getFavoriteServer(sessions).name(), true)
                .addField("Country", lastIp == null ? "null" : lastIp.getCountry(), true)
                .addField("Time Zone", lastIp == null || lastIp.getTimezone() == null ? "null" : lastIp.getTimezone().getDisplayName(), true)
        ;

        return eb;
    }

    private EmbedBuilder getPendingEmbed (int stage) {

        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("**Compiling Statistics Data for `" + username + "`!**");

        String desc;

        if (stage == 1) {
            desc = "Loading raw data from the database...";
        } else {
            desc = "Processing data to compile statistics...";
        }

        eb.setDescription(desc);

        return eb;

    }
}
