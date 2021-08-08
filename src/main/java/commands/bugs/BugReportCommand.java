package commands.bugs;

import commands.Command;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.grandtheftmc.jedisnew.RedisEvent;
import org.json.JSONObject;
import utils.Data;
import utils.SelfData;
import utils.database.DiscordDAO;
import utils.selfdata.ChannelIdData;
import utils.tools.GTools;
import utils.users.GTMUser;
import utils.users.Rank;

import static utils.tools.GTools.guild;
import static utils.tools.GTools.sendThenDelete;

public class BugReportCommand extends Command {

    public BugReportCommand() {
        super("bugreports", "Manage bug reports", Rank.ADMIN, Type.DISCORD_ONLY);
    }

    @Override
    public void onCommandUse(Message message, Member member, GTMUser gtmUser, MessageChannel channel, String[] args) {

        if (!(channel instanceof TextChannel)) return;
        TextChannel textChannel = (TextChannel) channel;

        if (args.length < 1) {
            GTools.sendThenDelete(channel, getHelpMsg());
            return;
        }

        switch (args[0].toLowerCase()) {

            case "setchannel": {
                if (args.length < 2) {
                    GTools.sendThenDelete(channel, getHelpMsg());
                    return;
                }
                if (args[1].equalsIgnoreCase("report")) {
                    // Set settings
                    ChannelIdData.get().setBugReportChannelId(channel.getIdLong());
                    sendThenDelete(channel, "**" + textChannel.getAsMention() + " has been set as the bug reports channel!**");
                    // Delete previous msg
                    if (SelfData.get().getPrevBugEmbedId() != 0)
                        guild.getTextChannelById(ChannelIdData.getData().getBugReportChannelId()).retrieveMessageById(SelfData.get().getPrevBugEmbedId()).queue(m -> m.delete().queue());
                    if (SelfData.get().getPrevBugHelpMsgId() != 0)
                        guild.getTextChannelById(ChannelIdData.getData().getBugReportChannelId()).retrieveMessageById(SelfData.get().getPrevBugHelpMsgId()).queue(m -> m.delete().queue());
                    // Send how to make a bug report instruction
                    channel.sendMessage(ReportListener.createHowBugReportEmbed())
                            .flatMap(m -> {
                                SelfData.get().setPrevBugEmbedId(m.getIdLong());
                                return channel.sendMessage(ReportListener.getFormatMessage());
                            }).queue(m -> SelfData.get().setPrevBugHelpMsgId(m.getIdLong()));
                } else if (args[1].equalsIgnoreCase("receive")) {
                    // Set settings
                    ChannelIdData.get().setBugReceiveChannelId(channel.getIdLong());
                    sendThenDelete(channel, "**" + textChannel.getAsMention() + " has been set as the bug receive channel!**");
                } else GTools.sendThenDelete(channel, getHelpMsg());
                break;
            }
            case "deny": {
                if (!isValidArgs(textChannel, args)) return;
                BugReport report = (BugReport) Data.obtainData(Data.BUG_REPORTS, Integer.parseInt(args[1]));
                report.updateStatus(BugReport.ReportStatus.DENIED, args.length > 2 ? GTools.joinArgsAfter(args, 2) : null, report.isHidden());
                break;
            }
            case "complete": {
                if (!isValidArgs(textChannel, args)) return;
                BugReport report = (BugReport) Data.obtainData(Data.BUG_REPORTS, Integer.parseInt(args[1]));
                report.updateStatus(BugReport.ReportStatus.PATCHED, args.length > 2 ? GTools.joinArgsAfter(args, 2) : null, report.isHidden());
                break;
            }
            case "duplicate": {
                if (!isValidArgs(textChannel, args)) return;
                BugReport report = (BugReport) Data.obtainData(Data.BUG_REPORTS, Integer.parseInt(args[1]));
                report.updateStatus(BugReport.ReportStatus.DUPLICATE_REPORT, args.length > 2 ? GTools.joinArgsAfter(args, 2) : null, report.isHidden());
                break;
            }
            case "approve": {
                if (!isValidArgs(textChannel, args)) return;
                boolean b = false;
                if (args.length > 2) {
                    if (args[2].equalsIgnoreCase("true") || args[2].equalsIgnoreCase("false")) {
                        b = Boolean.parseBoolean(args[2]);
                    } else {
                        sendThenDelete(channel, "**The hide boolean '" + args[2] + "' is not a true or false.**");
                        return;
                    }
                }
                BugReport report = (BugReport) Data.obtainData(Data.BUG_REPORTS, Integer.parseInt(args[1]));

                report.updateStatus(BugReport.ReportStatus.CONFIRMED_BUG, args.length > 3 ? GTools.joinArgsAfter(args, 3) : null, b);
                report.setHidden(b);

                GTMUser.getGTMUser(report.getReporterId()).ifPresent(reporterGTMUser -> {
                    JSONObject data = new JSONObject()
                            .put("uuid", reporterGTMUser.getUuid());
                    DiscordDAO.sendToGTM("bug_reported", data);
                });
                break;
            }
            default: {
                GTools.sendThenDelete(channel, getHelpMsg());
                break;
            }

        }

    }

    private boolean isValidArgs(TextChannel channel, String[] args) {
        if (args.length < 2) {
            GTools.sendThenDelete(channel, getHelpMsg());
            return false;
        }
        boolean exists = false;
        try {
            exists = Data.doesNumberExist(Data.BUG_REPORTS, Integer.parseInt(args[1]));
        } catch (NumberFormatException ignored) {
        }

        if (!exists) {
            sendThenDelete(channel, "**No bug report with id " + args[1] + " exists!");
            return false;
        }
        return true;
    }

    private static Message getHelpMsg() {
        return new MessageBuilder()
                .append("> **Please enter a valid command argument:**\n")
                .append("> `/BugReports SetChannel (Report/Receive)` - *Set current channel to the bug Report or the bug Receive channel*\n")
                .append("> `/BugReports Approve <ID> (Hide-true/false) (Reason)` - *Approve this bug report. (Set 'Hide' to true to hide report from players)*\n")
                .append("> `/BugReports Deny <ID> (Reason)` - *Deny AND delete the bug report.*\n")
                .append("> `/BugReports Complete <ID> (Reason)` - *Set the status of the bug to fixed.*\n")
                .append("> `/BugReports Duplicate <ID> (Reason)` - *Deny the bug report because it is a duplicate.*\n")
                .build();
    }


}
