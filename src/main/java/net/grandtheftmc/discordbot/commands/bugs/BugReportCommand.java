package net.grandtheftmc.discordbot.commands.bugs;

import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.grandtheftmc.discordbot.commands.Command;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.grandtheftmc.discordbot.utils.Data;
import net.grandtheftmc.discordbot.utils.BotData;
import net.grandtheftmc.discordbot.utils.selfdata.ChannelIdData;
import net.grandtheftmc.discordbot.utils.Utils;
import net.grandtheftmc.discordbot.utils.users.GTMUser;
import net.grandtheftmc.discordbot.utils.users.Rank;
import net.grandtheftmc.discordbot.utils.web.clickup.CUTask;

import static net.grandtheftmc.discordbot.utils.Utils.*;

public class BugReportCommand extends Command {

    public BugReportCommand() {
        super("BugReports", "Manage bug reports", Rank.ADMIN, Type.DISCORD_ONLY);

    }

    public void buildCommandData() {

        SubcommandData setChannel = new SubcommandData("SetChannel", "Set the bug reports channels.");
        OptionData optionData = new OptionData(OptionType.STRING, "Type", "Are you setting the report receive channel or the report send channel?");
        optionData.addChoice("Receive", "Send");
        setChannel.addOptions(optionData);

        SubcommandData deny = new SubcommandData("Deny", "Deny the selected bug report");
        deny.addOption(OptionType.STRING, "Bug Report ID", "Whats the bug report you want to deny?");

        SubcommandData approve = new SubcommandData("Deny", "Deny the selected bug report");
        deny.addOption(OptionType.STRING, "Bug Report ID", "Whats the bug report you want to approve?");
    }


    @Override
    public void onCommandUse(SlashCommandInteraction interaction, MessageChannel channel, Member member, GTMUser gtmUser, String[] args) {

        if (!(channel instanceof TextChannel)) return;
        TextChannel textChannel = (TextChannel) channel;

        if (args.length < 1) {
            Utils.sendThenDelete(channel, getHelpMsg());
            return;
        }

        switch (args[0].toLowerCase()) {

            case "setchannel": {
                if (args.length < 2) {
                    Utils.sendThenDelete(channel, getHelpMsg());
                    return;
                }
                if (args[1].equalsIgnoreCase("report")) {
                    // Set settings
                    ChannelIdData.get().setBugReportChannelId(channel.getIdLong());
                    sendThenDelete(channel, "**" + textChannel.getAsMention() + " has been set as the bug reports channel!**");
                    // Delete previous msg
                    try {
                        guild.getTextChannelById(ChannelIdData.getData().getBugReportChannelId()).retrieveMessageById(BotData.LAST_BUG_EMBED_ID.getData(Number.class).longValue()).queue(m -> m.delete().queue());
                        guild.getTextChannelById(ChannelIdData.getData().getBugReportChannelId()).retrieveMessageById(BotData.LAST_BUG_MSG_ID.getData(Number.class).longValue()).queue(m -> m.delete().queue());
                    } catch (ErrorResponseException ignored) {}

                    // Send how to make a bug report instruction
                    channel.sendMessageEmbeds(ReportListener.createHowBugReportEmbed())
                            .flatMap(m -> {
                                BotData.LAST_BUG_EMBED_ID.setValue(m.getIdLong());
                                return channel.sendMessage(ReportListener.getFormatMessage());
                            })
                            .queue(m -> BotData.LAST_BUG_MSG_ID.setValue(m.getIdLong()));

                } else if (args[1].equalsIgnoreCase("receive")) {
                    // Set settings
                    ChannelIdData.get().setBugReceiveChannelId(channel.getIdLong());
                    sendThenDelete(channel, "**" + textChannel.getAsMention() + " has been set as the bug receive channel!**");
                } else Utils.sendThenDelete(channel, getHelpMsg());
                break;
            }

            case "deny": {
                if (!isValidArgs(textChannel, args)) return;
                BugReport report = (BugReport) Data.obtainData(Data.BUG_REPORTS, args[1].toLowerCase());
                report.setStatus(BugReport.ReportStatus.REJECTED_REPORT);
                report.sendUpdate(args.length > 2 ? Utils.joinArgsAfter(args, 2) : null);
                CUTask.editTask(report.getId(), BugReport.ReportStatus.REJECTED_REPORT);
                sendThenDelete(channel, "**Success!** You set bug report id " + args[1] + " to " + BugReport.ReportStatus.REJECTED_REPORT + "!");
                break;
            }
            case "complete": {
                if (!isValidArgs(textChannel, args)) return;
                BugReport report = (BugReport) Data.obtainData(Data.BUG_REPORTS, args[1]);
                report.setStatus(BugReport.ReportStatus.PATCHED);
                report.sendUpdate(args.length > 2 ? Utils.joinArgsAfter(args, 2) : null);
                CUTask.editTask(report.getId(), BugReport.ReportStatus.PATCHED);
                sendThenDelete(channel, "**Success!** You set bug report id " + args[1] + " to " + BugReport.ReportStatus.PATCHED + "!");
                break;
            }
            case "duplicate": {
                if (!isValidArgs(textChannel, args)) return;
                BugReport report = (BugReport) Data.obtainData(Data.BUG_REPORTS, args[1]);
                report.setStatus(BugReport.ReportStatus.DUPLICATE_REPORT);
                report.sendUpdate(args.length > 2 ? Utils.joinArgsAfter(args, 2) : null);
                CUTask.editTask(report.getId(), BugReport.ReportStatus.DUPLICATE_REPORT);
                sendThenDelete(channel, "**Success!** You set bug report id " + args[1] + " to " + BugReport.ReportStatus.DUPLICATE_REPORT + "!");
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
                BugReport report = (BugReport) Data.obtainData(Data.BUG_REPORTS, args[1]);
                report.setStatus(BugReport.ReportStatus.CONFIRMED_BUG);
                report.setHidden(b);
                report.sendUpdate(args.length > 3 ? Utils.joinArgsAfter(args, 3) : null);
                CUTask.editTask(report.getId(), BugReport.ReportStatus.CONFIRMED_BUG);

                sendThenDelete(channel, "**Success!** You set bug report id " + args[1] + " to " + BugReport.ReportStatus.CONFIRMED_BUG + "!");
                break;
            }
            default: {
                Utils.sendThenDelete(channel, getHelpMsg());
                break;
            }

        }

    }

    private boolean isValidArgs(TextChannel channel, String[] args) {
        if (args.length < 2) {
            Utils.sendThenDelete(channel, getHelpMsg());
            return false;
        }

        boolean exists;
        exists = Data.doesDataExist(Data.BUG_REPORTS, args[1].toLowerCase());

        if (!exists) {
            sendThenDelete(channel, "**Error!** No bug report with id " + args[1] + " was found.");
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
