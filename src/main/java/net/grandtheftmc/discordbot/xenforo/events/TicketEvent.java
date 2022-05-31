package net.grandtheftmc.discordbot.xenforo.events;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.grandtheftmc.discordbot.GTMBot;
import net.grandtheftmc.discordbot.utils.MembersCache;
import net.grandtheftmc.discordbot.utils.Utils;
import net.grandtheftmc.discordbot.utils.console.Logs;
import net.grandtheftmc.discordbot.utils.database.DiscordDAO;
import net.grandtheftmc.discordbot.utils.database.LitebansDAO;
import net.grandtheftmc.discordbot.utils.database.XenforoDAO;
import net.grandtheftmc.discordbot.utils.database.sql.BaseDatabase;
import net.grandtheftmc.discordbot.utils.litebans.Ban;
import net.grandtheftmc.discordbot.utils.selfdata.ChannelIdData;
import net.grandtheftmc.discordbot.utils.threads.ThreadUtil;
import net.grandtheftmc.discordbot.utils.users.GTMUser;
import net.grandtheftmc.discordbot.utils.users.Rank;
import net.grandtheftmc.discordbot.xenforo.objects.Alert;
import net.grandtheftmc.discordbot.xenforo.objects.tickets.Department;
import net.grandtheftmc.discordbot.xenforo.objects.tickets.SupportTicket;

import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

@Deprecated
public class TicketEvent {

    // run async
    public void onTicketEvent(Alert alert) {
        ThreadUtil.runAsync( () -> {

            if (!alert.getAction().equals("new_ticket")) return;

            Department department = alert.getSupportTicket().getDepartment();
            if (department == null) return;

            Logs.log("[DEBUG] [EventType] Received a new support ticket with title '" + alert.getSupportTicket().getTitle() + "' in " + department.getDepartmentName() + "!");

            TextChannel channel = GTMBot.getJDA().getGuilds().get(0).getTextChannelById(ChannelIdData.get().getModChannelId());
            if (channel == null) return;

            switch (department) {

                case PUNISHMENT_APPEALS: {
                    try (Connection conn = BaseDatabase.getInstance(BaseDatabase.Database.USERS).getConnection()) {

                            String username = alert.getSupportTicket().getTicketFields().getString("username");
                            Ban ban = null;
                            try (Connection conn2 = BaseDatabase.getInstance(BaseDatabase.Database.BANS).getConnection()) {
                                ban = LitebansDAO.getBanByPlayer(conn2, username);
                            } catch (SQLException e) {
                                Utils.printStackError(e);
                            }

                            String banStaff;
                            if (ban == null || !ban.isActive()) {
                                banStaff = alert.getSupportTicket().getTicketFields().getString("staffban");
                            } else banStaff = ban.getBanName();

                            GTMUser gtmUser = GTMUser.getGTMUser(DiscordDAO.getDiscordIdFromName(conn, banStaff)).orElse(null);
                            MessageEmbed embed = generateAppealsEmbed(gtmUser, alert.getSupportTicket(), ban);
                            channel.sendMessageEmbeds(embed).queue();
                            if (gtmUser != null && gtmUser.getRank().isHigherOrEqualTo(Rank.MOD) && gtmUser.getUser().isPresent())
                                gtmUser.getUser().get().openPrivateChannel().queue((privateChannel) ->
                                        privateChannel.sendMessageEmbeds(embed).queue()
                                );
                    } catch (SQLException e) {
                        Utils.printStackError(e);
                    }
                    break;
                }

                case STAFF_REPORTS: {
                    try (Connection conn = BaseDatabase.getInstance(BaseDatabase.Database.USERS).getConnection()) {
                        List<Member> managers = MembersCache.getMembersWithRolePerms(Rank.MANAGER);

                        for (Member manager : managers) {
                            manager.getUser().openPrivateChannel().queue((privateChannel) -> {
                                privateChannel.sendMessageEmbeds(generateStaffReportEmbed(conn, alert.getSupportTicket())).queue();
                            });
                        }
                    } catch (SQLException e) {
                        Utils.printStackError(e);
                    }
                }

                case PURCHASES: {
                    // TODO
                }
                case OTHER_SUPPORT: {
                    // TODO
                }
                case PLAYER_REPORTS: {
                    // TODO
                }
                case BUY_AN_UNBAN: {
                    break;
                }

            }

        });
    }

    private MessageEmbed generateStaffReportEmbed(Connection conn, SupportTicket ticket) {
        String reportedStaff = ticket.getTicketFields().getString("1993");
        GTMUser gtmUser = GTMUser.getGTMUser(DiscordDAO.getDiscordIdFromName(conn, reportedStaff)).orElse(null);

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Support Ticket Notification")
                .setColor(new Color(168, 50, 137))
                .setDescription("We have received a new staff report. You and other managers have been notified!")
                .addField("**Ticket Title:**", ticket.getTitle(), false)
                .addField("**Username:**", ticket.getTicketFields().getString("username"), false)
                .addField("**Reported Staff:**", reportedStaff, false)
                .addField("**Ticket Link**", ticket.getTicketLink(), false);
        if (gtmUser != null && gtmUser.getUser().isPresent())
            embed.setThumbnail(gtmUser.getUser().get().getAvatarUrl());

        return embed.build();
    }


    private MessageEmbed generateAppealsEmbed(GTMUser gtmUser, SupportTicket ticket, Ban ban) {
        String staff;
        if (gtmUser == null)
            staff = "an unknown staff member";

        else if (!gtmUser.getRank().isHigherOrEqualTo(Rank.MOD))
            staff = gtmUser.getUsername() + ", however they are no longer a moderator+";

        else if (!gtmUser.getDiscordMember().isPresent())
            staff = gtmUser.getUsername() + ", however they are not in this discord";

        else staff = gtmUser.getDiscordMember().get().getAsMention();

        boolean plead = ticket.getTicketFields().getString("plead").equalsIgnoreCase("yes");

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Support Ticket Notification")
                .setColor(new Color(168, 50, 137))
                .setDescription("Received a new support ticket for " + staff + "!")
                .addField("**Ticket Title:**", ticket.getTitle(), false)
                .addField("**Username:**", ticket.getTicketFields().getString("username"), false)
                .addField("**Ban Reason:**", ticket.getTicketFields().getString("banReason"), false);

        if (ban != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd @ hh:mm a z");
            sdf.setTimeZone(TimeZone.getTimeZone("America/New_York"));
            embed.addField("**Banning Staff Member:**", ban.getBanName(), false)
                .addField("**Ban Timestamp:**", sdf.format(new Date(ban.getBanTime())), false);

        } else embed.addField("**Banning Staff Member:**", ticket.getTicketFields().getString("staffban"), false);
        embed.addField("**Guilty Plea:**", plead ? "Admits to Infraction" : "Denies Fault", false);
        embed.addField("**Ticket Link**", ticket.getTicketLink(), false);

        String prevAppealLinks = getPreviousAppeals(ticket.getTicketFields().getString("username"), ticket.getSupportTicketId());
        if (prevAppealLinks != null) embed.addField("**Previous Appeals**", prevAppealLinks, false);
        if (gtmUser != null && gtmUser.getUser().isPresent())
            embed.setThumbnail(gtmUser.getUser().get().getAvatarUrl());

        return embed.build();
    }

    private String getPreviousAppeals(String username, int currentTicketId) {
        List<SupportTicket> tickets = new ArrayList<>(XenforoDAO.getAllTicketsFrom(username));

        tickets.removeIf( (ticket) -> ticket.getDepartment() != Department.PUNISHMENT_APPEALS || ticket.getSupportTicketId() == currentTicketId);

        // return links
        if (tickets.size() == 0) return null;
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
        return sb.toString();
    }

}
