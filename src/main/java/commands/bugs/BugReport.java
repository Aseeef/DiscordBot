package commands.bugs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import utils.Data;
import utils.SelfData;
import utils.selfdata.ChannelIdData;
import utils.tools.GTools;

import java.awt.*;
import java.io.File;

import static utils.tools.GTools.*;

public class BugReport {

    private int number;
    private long receiveChannelId;
    private long reportChannelId;
    private String reportMessage;
    private boolean hidden;
    private long reporterId;
    private ReportStatus status;
    private String fileName;

    public BugReport() {
    }

    public BugReport(int number, long receiveChannelId, long reportChannelId, String reportMessage, boolean hidden, long reporterId, ReportStatus status, String fileName) {
        this.number = number;
        this.receiveChannelId = receiveChannelId;
        this.reportChannelId = reportChannelId;
        this.reportMessage = reportMessage;
        this.hidden = hidden;
        this.reporterId = reporterId;
        this.status = status;
        this.fileName = fileName;

        Data.storeData(Data.BUG_REPORTS, this, number);
    }

    public int getNumber() {
        return number;
    }

    public long getReceiveChannelId() {
        return receiveChannelId;
    }

    public void setReceiveChannelId(long receiveChannelId) {
        this.receiveChannelId = receiveChannelId;
        Data.storeData(Data.BUG_REPORTS, this, this.number);
    }

    public long getReportChannelId() {
        return reportChannelId;
    }

    public void setReportChannelId(long reportChannelId) {
        this.reportChannelId = reportChannelId;
        Data.storeData(Data.BUG_REPORTS, this, this.number);
    }

    public String getReportMessage() {
        return reportMessage;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
        Data.storeData(Data.BUG_REPORTS, this, this.number);
    }

    public long getReporterId() {
        return reporterId;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public String getFileName() {
        return fileName;
    }

    @JsonIgnore
    public void updateStatus(ReportStatus status, String statusReason, boolean warnCensor) {
        this.status = status;
        userById(this.reporterId).openPrivateChannel().queue(channel -> {
            MessageAction ma = channel.sendMessage("An update has been received on the following bug report:").embed(this.createEmbed(false));
            if (fileName != null) {
                ma = ma.addFile(new File(BugReport.getDownloadDir(), fileName));
            }
            ma.queue(success -> {
                if (statusReason != null) {
                    channel.sendMessage("**Additional info from an admin:** \n" + statusReason).queue();
                }
                if (warnCensor && status == ReportStatus.CONFIRMED_BUG)
                    channel.sendMessage("**Warning!** This bug report has been marked as 'hidden' (probably because this bug can be abused). Sharing this bug with anyone may result in a cancellation of any rewards and/or may result in a punishment!").queue();
            });
        });
        if (this.receiveChannelId != 0) {
            getBugReceiveChannel().retrieveMessageById(this.receiveChannelId).queue(msg ->
                    msg.editMessage(createEmbed(false)).queue());
        }
        if (this.reportChannelId != 0) {
            getBugReportChannel().retrieveMessageById(this.reportChannelId).queue(msg -> {
                    MessageAction ma = msg.editMessage(createEmbed(warnCensor));
                    if (warnCensor) ma.clearFiles();
                    ma.queue();
            });
        }
        else if (status == ReportStatus.CONFIRMED_BUG) {
            // Delete previous msg
            if (SelfData.get().getPrevBugEmbedId() != 0)
                guild.getTextChannelById(ChannelIdData.getData().getBugReportChannelId()).retrieveMessageById(SelfData.get().getPrevBugEmbedId()).queue(m -> m.delete().queue());
            if (SelfData.get().getPrevBugHelpMsgId() != 0)
                guild.getTextChannelById(ChannelIdData.getData().getBugReportChannelId()).retrieveMessageById(SelfData.get().getPrevBugHelpMsgId()).queue(m -> m.delete().queue());

            // set id
            GTools.runAsync(() -> {
                MessageAction ma = BugReport.getBugReportChannel().sendMessage(createEmbed(warnCensor));
                    if (!warnCensor && this.fileName != null) {
                        ma = ma.addFile(new File(BugReport.getDownloadDir(), this.fileName));
                    }
                    ma.queue(m1 -> {
                        this.setReportChannelId(m1.getIdLong());
                        getBugReportChannel().sendMessage(ReportListener.createHowBugReportEmbed())
                                .flatMap(m -> {
                                    SelfData.get().setPrevBugEmbedId(m.getIdLong());
                                    return getBugReportChannel().sendMessage(ReportListener.getFormatMessage());
                                }).queue(m -> SelfData.get().setPrevBugHelpMsgId(m.getIdLong()));
                    });
            });
        }

        if (status == ReportStatus.DENIED) {
            if (this.receiveChannelId != 0)
                getBugReceiveChannel().retrieveMessageById(this.receiveChannelId).queue(msg ->
                        msg.delete().queue());
            if (this.reportChannelId != 0)
                getBugReportChannel().retrieveMessageById(this.reportChannelId).queue(msg ->
                        msg.delete().queue());
        }

        Data.storeData(Data.BUG_REPORTS, this, this.number);
    }

    public enum ReportStatus {
        PENDING_REVIEW(Color.YELLOW),
        DENIED(Color.RED),
        CONFIRMED_BUG(Color.GREEN),
        PATCHED(Color.CYAN),
        DUPLICATE_REPORT(Color.BLACK)
        ;
        private Color embedColor;
        ReportStatus (Color embedColor) {
           this.embedColor = embedColor;
        }
    }

    @JsonIgnore
    public MessageEmbed createEmbed (boolean censor) {
        // Create suggestion embed
        EmbedBuilder embed = new EmbedBuilder();
        String gtmLearnablesEmoji = jda.getEmotesByName("gtmlearnables", true).get(0).getAsMention();
        embed.setTitle(gtmLearnablesEmoji + "  BUG REPORT ID: #" + this.number);
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

}
