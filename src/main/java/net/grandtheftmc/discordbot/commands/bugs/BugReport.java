package net.grandtheftmc.discordbot.commands.bugs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.grandtheftmc.discordbot.GTMBot;
import org.json.JSONObject;
import net.grandtheftmc.discordbot.utils.BotData;
import net.grandtheftmc.discordbot.utils.Data;
import net.grandtheftmc.discordbot.utils.database.DiscordDAO;
import net.grandtheftmc.discordbot.utils.selfdata.ChannelIdData;
import net.grandtheftmc.discordbot.utils.threads.ThreadUtil;
import net.grandtheftmc.discordbot.utils.users.GTMUser;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.File;

import static net.grandtheftmc.discordbot.utils.Utils.*;

@Getter
public class BugReport {

    private String id;
    private long receiveChannelId;
    private long reportChannelId;
    private String reportMessage;
    private boolean hidden;
    private long reporterId;
    private ReportStatus status;
    private boolean awarded = false;

    public BugReport() {
    }

    @JsonIgnore
    public BugReport(long receiveChannelId, long reportChannelId, String reportMessage, boolean hidden, long reporterId, ReportStatus status) {
        this.receiveChannelId = receiveChannelId;
        this.reportChannelId = reportChannelId;
        this.reportMessage = reportMessage;
        this.hidden = hidden;
        this.reporterId = reporterId;
        this.status = status;
    }

    public void save() {
        Data.storeData(Data.BUG_REPORTS, this, id);
    }

    public void setId(String id) {
        this.id = id;
        Data.storeData(Data.BUG_REPORTS, this, this.id);
    }

    public void setReceiveChannelId(long receiveChannelId) {
        this.receiveChannelId = receiveChannelId;
        Data.storeData(Data.BUG_REPORTS, this, this.id);
    }

    public void setReportChannelId(long reportChannelId) {
        this.reportChannelId = reportChannelId;
        Data.storeData(Data.BUG_REPORTS, this, this.id);
    }

    public void setReportMessage(String reportMessage) {
        this.reportMessage = reportMessage;
        Data.storeData(Data.BUG_REPORTS, this, this.id);
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
        Data.storeData(Data.BUG_REPORTS, this, this.id);
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
        Data.storeData(Data.BUG_REPORTS, this, this.id);
    }

    public void setAwarded(boolean awarded) {
        this.awarded = awarded;
        Data.storeData(Data.BUG_REPORTS, this, this.id);
    }

    @JsonIgnore
    public void sendUpdate(@Nullable String adminComment) {
        ThreadUtil.runAsync(() -> {
            // dm user about an update
            User reporter = userById(this.reporterId);
            if (reporter != null) {
                try {
                    PrivateChannel privateChannel = reporter.openPrivateChannel().complete();
                    MessageAction ma = privateChannel.sendMessage("An update has been received on the following bug report:").setEmbeds(this.createEmbed(false));
                    ma.complete();
                    if (adminComment != null) {
                        StringBuilder comment = new StringBuilder("**Additional comments:**\n");
                        comment.append(adminComment);
                        privateChannel.sendMessage(comment).complete();
                    }
                    if (this.hidden)
                        privateChannel.sendMessage("**Warning!** This bug report was marked as 'hidden' (probably because this bug can be abused). Sharing this bug with anyone may result in a cancellation of any rewards and/or further punishment!").complete();
                } catch (ErrorResponseException ignored) {} // if user has dms closed
            }
            // update embed in the receive channel (if any and if not we create ones later if needed..)
            try {
                Message msg1 = getBugReceiveChannel().retrieveMessageById(this.receiveChannelId).complete();
                msg1.editMessageEmbeds(createEmbed(false)).complete();
            } catch (ErrorResponseException ignored) {
            }
            // update embed in the bug report public channel (if any and if not we create ones later if needed..)
            try {
                Message msg2 = getBugReportChannel().retrieveMessageById(this.reportChannelId).complete();
                msg2.editMessageEmbeds(createEmbed(this.hidden)).complete();
            } catch (ErrorResponseException ignored) {
            }

            if (status == ReportStatus.CONFIRMED_BUG || status == ReportStatus.PATCHED) {

                // Get text channel
                TextChannel bugChannel = guild.getTextChannelById(ChannelIdData.getData().getBugReportChannelId());
                // Get if an instance of this bug report has already been posted in public bug reports
                try {
                    bugChannel.retrieveMessageById(this.getReportChannelId()).complete();
                }
                // if not, we will get an error
                catch (ErrorResponseException ignored) {
                    try {
                        // so first delete the old bug report instruction msgs
                        bugChannel.retrieveMessageById(BotData.LAST_BUG_EMBED_ID.getData(Long.TYPE)).queue(m -> m.delete().queue());
                        bugChannel.retrieveMessageById(BotData.LAST_BUG_MSG_ID.getData(Long.TYPE)).queue(m -> m.delete().queue());

                        // now post this new bug report
                        Message msg = BugReport.getBugReportChannel().sendMessageEmbeds(createEmbed(this.hidden)).complete();
                        this.setReportChannelId(msg.getIdLong());

                        // and post back the instruction msgs and save there ids
                        Message msg1 = getBugReportChannel().sendMessageEmbeds(ReportListener.createHowBugReportEmbed()).complete();
                        BotData.LAST_BUG_EMBED_ID.setValue(msg1.getIdLong());
                        Message msg2 = getBugReportChannel().sendMessage(ReportListener.getFormatMessage()).complete();
                        BotData.LAST_BUG_MSG_ID.setValue(msg2.getIdLong());

                    } catch (ErrorResponseException ex) {
                        ex.printStackTrace(); //shouldn't get an error, but if we do...
                    }
                }

                // award player for reporting bug if not already rewarded
                if (!this.isAwarded()) {
                    GTMUser.getGTMUser(this.reporterId).ifPresent(reporterGTMUser -> {
                        JSONObject data = new JSONObject()
                                .put("uuid", reporterGTMUser.getUuid());
                        DiscordDAO.sendToGTM("bug_reported", data);
                    });
                    this.setAwarded(true);
                }
            }

            if (status == ReportStatus.REJECTED_REPORT) {
                if (this.reportChannelId != 0) {
                    try {
                        Message msg = getBugReportChannel().retrieveMessageById(this.reportChannelId).complete();
                        msg.delete().complete();
                        setReportChannelId(0);
                    } catch (ErrorResponseException ignored) {
                    }
                }
            }

            save();
        });
    }

    public enum ReportStatus {
        AWAITING_REVIEW(Color.YELLOW),
        REJECTED_REPORT(Color.RED),
        CONFIRMED_BUG(Color.GREEN),
        PATCHED(Color.CYAN),
        DUPLICATE_REPORT(Color.BLACK)
        ;
        private final Color embedColor;
        ReportStatus (Color embedColor) {
           this.embedColor = embedColor;
        }
    }

    @JsonIgnore
    public MessageEmbed createEmbed (boolean censor) {
        // Create suggestion embed
        EmbedBuilder embed = new EmbedBuilder();
        String gtmHelpfulEmoji = GTMBot.getJDA().getEmotesByName("gtmhelpful", true).get(0).getAsMention();
        embed.setTitle(gtmHelpfulEmoji + "  **BUG REPORT ID:** " + this.id);
        embed.setThumbnail(userById(this.reporterId).getAvatarUrl());
        if (censor) {
            embed.setDescription("\n\u200E||Sorry but the contents of this bug report are hidden from public view. This was likely done because this bug was considered 'exploitable' or contained other sensitive information.||\n\u200E");
        } else embed.setDescription("\n\u200E"+this.reportMessage+"\n\u200E");
        embed.addField("**Report Status:**", this.status.toString(), true);
        embed.setFooter("Submitted by " + userById(this.reporterId).getAsTag() +
                " (" + this.reporterId + ")");
        embed.setColor(status.embedColor);

        return embed.build();
    }

    public static TextChannel getBugReportChannel() {
        return guild.getTextChannelById(ChannelIdData.get().getBugReportChannelId());
    }

    public static TextChannel getBugReceiveChannel() {
        return guild.getTextChannelById(ChannelIdData.get().getBugReceiveChannelId());
    }

    public static File getDownloadDir() {
        return new File("data/" + Data.BUG_REPORTS.getDataName() + "/downloads/");
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof BugReport) {
            BugReport bugReport = (BugReport) o;
            return bugReport.id.equals(this.id);
        }
        return false;
    }
}
