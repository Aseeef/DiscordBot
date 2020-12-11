package commands.stats;

import commands.Command;
import commands.stats.wrappers.PlanUser;
import commands.stats.wrappers.Session;
import commands.stats.wrappers.WrappedIPData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import utils.pagination.DiscordMenu;
import utils.tools.GTools;
import utils.users.GTMUser;
import utils.users.Rank;

import java.util.List;
import java.util.UUID;

public class StatsCommand extends Command {

    public StatsCommand() {
        super("stats", "View details statistics on players or staff", Rank.ADMIN, Type.ANYWHERE);
    }

    @Override
    public void onCommandUse(Message message, Member member, GTMUser gtmUser, MessageChannel channel, String[] args) {

        DiscordMenu.create(channel, getGeneralStats(args[0]), 1).thenAccept( (menu) -> {
           // todo
        });

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

        Gang:
        - Gang name
        - Whos in the gang

        Staff stats
        - Help questions answered
        - avg length of help question
        - Punishments given break down
        - % help questions answered while online
        - % of help questions no one answered while they were online

         */

    }

    private EmbedBuilder getGeneralStats(String username) {

        UUID uuid = GTools.getUUID(username).orElse(null);
        if (uuid == null) return null;

        PlanUser pu = StatsDAO.getPlanUser(uuid);
        if (pu == null) return null;

        List<Session> sessions = StatsDAO.getSessions(uuid,null, null);
        WrappedIPData lastIp = StatsDAO.getIpInfo(StatsDAO.getIPs(uuid).get(0));
        GTMUser targetUser = GTMUser.getGTMUser(uuid).orElse(null);

        // get name history minus current name
        List<String> usernames = GTools.getAllUsernames(uuid);
        if (usernames != null)
            usernames.remove(pu.getUsername());
        String prevUsernames = usernames == null || usernames.size() == 0 ? "None" : usernames.toString().replaceAll("\\[", "").replaceAll("]", "");
        if (prevUsernames.length() > MessageEmbed.VALUE_MAX_LENGTH) {
            prevUsernames = prevUsernames.substring(0, MessageEmbed.VALUE_MAX_LENGTH - 4);
            prevUsernames += "...";
        }
        // get all linked alts
        List<String> allAlts = StatsDAO.getAllAlts(uuid);
        String altString = allAlts.size() == 0 ? "None" : allAlts.toString().replaceAll("\\[", "").replaceAll("]", "");
        if (altString.length() > MessageEmbed.VALUE_MAX_LENGTH) {
            altString = altString.substring(0, MessageEmbed.VALUE_MAX_LENGTH - 4);
            altString += "...";
        }
        // get alts from current ip
        List<String> recentAlts = lastIp == null ? null : StatsDAO.getAlts(uuid, lastIp.getIp());
        String recentAltsString = recentAlts == null || recentAlts.size() == 0 ? "None" : recentAlts.toString().replaceAll("\\[", "").replaceAll("]", "");
        if (recentAltsString.length() > MessageEmbed.VALUE_MAX_LENGTH) {
            recentAltsString = recentAltsString.substring(0, MessageEmbed.VALUE_MAX_LENGTH - 4);
            recentAltsString += "...";
        }

        EmbedBuilder eb = new EmbedBuilder();

        eb.setTitle("**General Statistics for `" + pu.getUsername() + "`:**");

        eb.setThumbnail(GTools.getSkullSkin(uuid));

        eb.addField("Username", pu.getUsername(), true)
                .addField("Linked Discord", targetUser == null || !targetUser.getDiscordMember().isPresent() ? "None" : targetUser.getDiscordMember().get().getAsMention(), true)
                .addField("Previous Username(s)", prevUsernames, false)
                .addField("Current Alts", recentAltsString, false)
                .addField("All Linked Alts", altString, false)
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

}
