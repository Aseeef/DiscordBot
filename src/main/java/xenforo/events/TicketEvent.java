package xenforo.events;

import Utils.Rank;
import Utils.SelfData;
import Utils.console.Logs;
import Utils.database.DAO;
import Utils.users.GTMUser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import xenforo.objects.Alert;
import xenforo.objects.tickets.SupportTicket;
import xenforo.objects.tickets.Department;

import java.awt.*;
import java.util.List;

import static Utils.tools.GTools.jda;

public class TicketEvent {

    public void onTicketEvent(Alert alert) {
        if (!alert.getAction().equals("new_ticket")) return;

        Logs.log("[DEBUG] [TicketEvent] Received a new support ticket with title '" + alert.getSupportTicket().getTitle() + "'!");

        TextChannel channel = jda.getGuilds().get(0).getTextChannelById(SelfData.get().getModChannelId());
        if (channel == null) return;
        Department department = Department.getDepartment(alert.getSupportTicket().getDepartmentId());
        if (department == null) return;

        switch (department) {

            case PUNISHMENT_APPEALS: {
                // TODO: Directly use litebans to get banning staff
                String banStaff = alert.getSupportTicket().getTicketFields().getString("staffban");
                GTMUser gtmUser = GTMUser.getGTMUser(DAO.getDiscordIdFromName(banStaff)).orElse(null);
                MessageEmbed embed = generateAppealsEmbed(gtmUser, alert.getSupportTicket());
                channel.sendMessage(embed).queue();
                if (gtmUser != null && gtmUser.getRank().isHigherOrEqualTo(Rank.MOD))
                    gtmUser.getDiscordMember().getUser().openPrivateChannel().queue( (privateChannel) ->
                        privateChannel.sendMessage(embed).queue()
                    );
                break;
            }

            case STAFF_REPORTS: {
                // TODO: Alternative for get a list of staff - Staff.class?
                List<GTMUser> managers = DAO.getAllWithRank(Rank.MANAGER);
                if (managers == null || managers.size() == 0) return;

                for (GTMUser manager : managers) {
                    manager.getDiscordMember().getUser().openPrivateChannel().queue( (privateChannel) -> {
                        privateChannel.sendMessage(generateStaffReportEmbed(alert.getSupportTicket())).queue();
                    });
                }
            }

            case PURCHASES:

            case OTHER_SUPPORT:

            case PLAYER_REPORTS:

            case BUY_AN_UNBAN: {
                break;
            }

        }

    }

    public MessageEmbed generateStaffReportEmbed(SupportTicket ticket) {
        String reportedStaff = ticket.getTicketFields().getString("i:1993");
        GTMUser gtmUser = GTMUser.getGTMUser(DAO.getDiscordIdFromName(reportedStaff)).orElse(null);

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Support Ticket Notification")
                .setColor(new Color(168, 50, 137))
                .setDescription("We have received a new staff report. You and other managers have been notified!")
                .addField("**Ticket Title:**", ticket.getTitle(), false)
                .addField("**Username:**", ticket.getTicketFields().getString("username"), false)
                .addField("**Reported Staff:**", reportedStaff, false)
                .addField("**Ticket Link**", generateTicketLink(ticket), false);
        if (gtmUser != null)
            embed.setThumbnail(gtmUser.getDiscordMember().getUser().getAvatarUrl());

        return embed.build();
    }


    public MessageEmbed generateAppealsEmbed(GTMUser gtmUser, SupportTicket ticket) {
        String staff;
        if (gtmUser == null)
            staff = "an unknown staff member";

        else if (!gtmUser.getRank().isHigherOrEqualTo(Rank.MOD))
            staff = gtmUser.getUsername() + " however they are no longer a moderator";

        else staff = gtmUser.getDiscordMember().getAsMention();

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Support Ticket Notification")
                .setColor(new Color(168, 50, 137))
                .setDescription("Received a new support ticket for " + staff + "!")
                .addField("**Ticket Title:**", ticket.getTitle(), false)
                .addField("**Username:**", ticket.getTicketFields().getString("username"), false)
                .addField("**Ban Reason:**", ticket.getTicketFields().getString("banReason"), false)
                .addField("**Banning Staff Member**", ticket.getTicketFields().getString("staffban"), false)
                .addField("**Ticket Link**", generateTicketLink(ticket), false);
        if (gtmUser != null)
            embed.setThumbnail(gtmUser.getDiscordMember().getUser().getAvatarUrl());

        return embed.build();
    }

    public String generateTicketLink(SupportTicket ticket) {
        return new StringBuilder()
                .append("https://grandtheftmc.net/support-tickets/")
                .append(ticket.getTitle().replace(" ", "-").replaceAll("[^a-zA-Z0-9\\-]", ""))
                .append(".")
                .append(ticket.getSupportTicketId())
                .append("/")
                .toString();
    }

}
