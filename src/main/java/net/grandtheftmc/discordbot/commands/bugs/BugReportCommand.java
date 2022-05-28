package net.grandtheftmc.discordbot.commands.bugs;

import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
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

import java.util.List;

import static net.grandtheftmc.discordbot.utils.Utils.*;

public class BugReportCommand extends Command {

    public BugReportCommand() {
        super("BugReports", "Manage bug reports", Rank.ADMIN, Type.DISCORD_ONLY);
    }


    @Override
    public void buildCommandData(SlashCommandData slashCommandData) {
        SubcommandData setChannel = new SubcommandData("setchannel", "Set the bug reports channels.");
        OptionData typeOption = new OptionData(OptionType.STRING, "type", "Are you setting the report receive channel or the report send channel?", true);
        typeOption.addChoice("receive", "receive");
        typeOption.addChoice("send", "send");
        OptionData channelOption = new OptionData(OptionType.CHANNEL, "channel-id", "The discord channel you want to assign for this purpose.");
        channelOption.setChannelTypes(ChannelType.TEXT);
        setChannel.addOptions(typeOption, channelOption);

        SubcommandData deny = new SubcommandData("deny", "Deny the selected bug report");
        deny.addOption(OptionType.STRING, "id", "The ID of the bug report you want to deny.");

        SubcommandData duplicate = new SubcommandData("duplicate", "Mark this bug report as a duplicate of an existing report.");
        duplicate.addOption(OptionType.STRING, "id", "The ID of the bug report you want to set to duplicate.");


        SubcommandData approve = new SubcommandData("approve", "Approve this bug report");
        approve.addOption(OptionType.STRING, "id", "The ID of the bug report you want to approve.", true);
        approve.addOption(OptionType.BOOLEAN, "hide", "Whether you want to hide the contents of this report from public.", true);

        SubcommandData complete = new SubcommandData("complete", "Mark this bug report as fully resolved.");
        complete.addOption(OptionType.STRING, "id", "The ID of the bug report you want to mark complete.", true);
        complete.addOption(OptionType.BOOLEAN, "hide", "Whether you want to hide the contents of this report from public.", false);


        slashCommandData.addSubcommands(setChannel, deny, duplicate, approve, complete);
    }

    @Override
    public void onCommandUse(SlashCommandInteraction interaction, MessageChannel channel, List<OptionMapping> arguments, Member member, GTMUser gtmUser, String[] path) {

        TextChannel textChannel = interaction.getTextChannel();

        switch (path[0].toLowerCase()) {

            case "setchannel": {
                if (arguments.get(0).getAsString().equalsIgnoreCase("send")) {
                    // Set settings
                    ChannelIdData.get().setBugReportChannelId(channel.getIdLong());
                    interaction.reply("**" + textChannel.getAsMention() + " has been set as the bug reports channel!**").queue();
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

                } else if (arguments.get(0).getAsString().equalsIgnoreCase("receive")) {
                    // Set settings
                    ChannelIdData.get().setBugReceiveChannelId(channel.getIdLong());
                    interaction.reply("**" + textChannel.getAsMention() + " has been set as the bug receive channel!**").queue();
                }
                break;
            }

            case "deny": {
                if (!isValidArgs(interaction, arguments))
                    return;
                BugReport report = (BugReport) Data.obtainData(Data.BUG_REPORTS, path[1].toLowerCase());
                report.setStatus(BugReport.ReportStatus.REJECTED_REPORT);
                report.sendUpdate(path.length > 2 ? Utils.joinArgsAfter(path, 2) : null);
                CUTask.editTask(report.getId(), BugReport.ReportStatus.REJECTED_REPORT);
                sendThenDelete(channel, "**Success!** You set bug report id " + path[1] + " to " + BugReport.ReportStatus.REJECTED_REPORT + "!");
                break;
            }
            case "complete": {
                if (!isValidArgs(interaction, arguments))
                    return;
                BugReport report = (BugReport) Data.obtainData(Data.BUG_REPORTS, path[1]);
                report.setStatus(BugReport.ReportStatus.PATCHED);
                report.sendUpdate(path.length > 2 ? Utils.joinArgsAfter(path, 2) : null);
                CUTask.editTask(report.getId(), BugReport.ReportStatus.PATCHED);
                interaction.reply("**Success!** You set bug report id " + path[1] + " to " + BugReport.ReportStatus.PATCHED + "!").queue();
                break;
            }
            case "duplicate": {
                if (!isValidArgs(interaction, arguments))
                    return;
                BugReport report = (BugReport) Data.obtainData(Data.BUG_REPORTS, path[1]);
                report.setStatus(BugReport.ReportStatus.DUPLICATE_REPORT);
                report.sendUpdate(path.length > 2 ? Utils.joinArgsAfter(path, 2) : null);
                CUTask.editTask(report.getId(), BugReport.ReportStatus.DUPLICATE_REPORT);
                interaction.reply("**Success!** You set bug report id " + path[1] + " to " + BugReport.ReportStatus.DUPLICATE_REPORT + "!").queue();
                break;
            }
            case "approve": {
                if (!isValidArgs(interaction, arguments))
                    return;
                boolean b = false;
                if (path.length > 2) {
                    if (path[2].equalsIgnoreCase("true") || path[2].equalsIgnoreCase("false")) {
                        b = Boolean.parseBoolean(path[2]);
                    } else {
                        interaction.reply("**The hide boolean '" + path[2] + "' is not a true or false.**").queue();
                        return;
                    }
                }
                BugReport report = (BugReport) Data.obtainData(Data.BUG_REPORTS, path[1]);
                report.setStatus(BugReport.ReportStatus.CONFIRMED_BUG);
                report.setHidden(b);
                report.sendUpdate(path.length > 3 ? Utils.joinArgsAfter(path, 3) : null);
                CUTask.editTask(report.getId(), BugReport.ReportStatus.CONFIRMED_BUG);

                break;
            }

        }

    }

    private boolean isValidArgs(SlashCommandInteraction interaction, List<OptionMapping> arguments) {
        boolean exists;
        exists = Data.doesDataExist(Data.BUG_REPORTS, arguments.get(0).getAsString().toLowerCase());

        if (!exists) {
            interaction.reply("**Error!** No bug report with id " + arguments.get(0).getAsString() + " was found.").queue();
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
