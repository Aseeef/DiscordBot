package commands.bugs;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.jetbrains.annotations.NotNull;
import utils.Data;
import utils.tools.GTools;
import utils.users.GTMUser;

import java.awt.*;
import java.io.File;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static utils.console.Logs.log;
import static utils.tools.GTools.jda;
import static utils.tools.GTools.sendThenDelete;

public class ReportListener extends ListenerAdapter {

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {

        TextChannel channel = event.getChannel();
        User user = event.getAuthor();
        Message msg = event.getMessage();
        String rawMsg = msg.getContentRaw();

        if (channel != BugReport.getBugReportChannel()) return;
        if (user.isBot()) return;
        if (GTools.isCommand(rawMsg, user)) return;
        if (!GTMUser.getGTMUser(user.getIdLong()).isPresent()) {
            sendThenDelete(channel, user.getAsMention() + " you are not allowed to post bug reports " +
                    "until you have verified your discord account to GTM by using the `/discord verify` command in game!");

            // DM user their deleted suggestion so it can be reposted
            user.openPrivateChannel().queue(
                    userChannel ->
                            userChannel.sendMessage("**Your Bug Report was deleted because you are not a verified user:**\n```" + rawMsg + "```\n" + "**Please link your account to GTM using the `/discord verify` command in game and repost this bug report.**")
                                    .queue());

            // Log failure
            log("Non Verified User: Deleted the following bug report from user " + user.getAsTag() + " (" + user.getId() + "):" +
                    "\n" + rawMsg);

            return;
        }

        if (match(rawMsg)) {
            GTools.runAsync(() -> {

                UUID fileUUID = msg.getAttachments().size() > 0 ? UUID.randomUUID() : null;
                Message.Attachment attachment;
                String fileName = null;
                File file = null;
                if (fileUUID != null) {
                    File dir = BugReport.getDownloadDir();
                    dir.mkdirs();
                    attachment = msg.getAttachments().get(0);
                    fileName = fileUUID + (attachment.getFileExtension() == null ? "" : "." + msg.getAttachments().get(0).getFileExtension());
                    try {
                        file = attachment.downloadToFile(new File(dir, fileName)).get();
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }

                BugReport report = new BugReport(Data.getNextNumber(Data.BUG_REPORTS), 0L, 0L, rawMsg, user.getIdLong(), BugReport.ReportStatus.PENDING_REVIEW, fileName);
                MessageAction ma = BugReport.getBugReceiveChannel().sendMessage(report.createEmbed(false));
                if (file != null) {
                    ma = ma.addFile(file);
                }
                // Send message
                ma.queue(success -> report.setReceiveChannelId(success.getIdLong()));
                // Delete original message & acknowledge report
                event.getMessage().delete().queue();
                File finalFile = file;
                user.openPrivateChannel().queue(userChannel -> {
                    MessageAction ma1 = userChannel.sendMessage("**Hey!** We received the following bug report from you:").embed(report.createEmbed(false));
                    if (report.getFileName() != null) {
                        ma1 = ma1.addFile(finalFile);
                    }
                    ma1.queue(success -> userChannel.sendMessage("I will notify you if there are any further updates on this report. Thank you!").queue());
                });
                String gtmAgree = jda.getEmotesByName("gtmagree", true).get(0).getAsMention();
                GTools.sendThenDelete(channel, gtmAgree + " **Success!** You submitted a bug report. Check your DMs for more info! (If you didn't receive a DM you may have your private messages turned off)");
            });
        } else {
            event.getMessage().delete().queue();
            GTools.sendThenDelete(channel, "**Hey!** " + user.getAsMention() + ", sorry but your message does not follow the bug reports format. I am sending the details in your PMs...");
            // DM user their deleted suggestion so it can be reposted
            user.openPrivateChannel().queue(userChannel ->
                    userChannel.sendMessage("**The following bug report from you was deleted because it did not follow the bug reports format:**\n```" + rawMsg + "```\n" + "**Please copy paste this exact format in to your message and repost your suggestion:**\n")
                            .queue( (success) -> userChannel.sendMessage(getFormatMessage()).queue(),
                                    (error) -> GTools.sendThenDelete(channel, "**Hey!** " + user.getAsMention() + ", sorry but I was unable to message you because you have your PMs disabled. Use /Harry for more info."))
            );

            // Log failure
            log("Incorrect Format: Deleted the following bug report from user "+user.getAsTag()+" ("+user.getId()+"):"+
                    "\n" + rawMsg);
        }

    }

    private static boolean match(String s) {
        Pattern pattern = Pattern.compile(
                "\\*\\*What server is this Bug Report for\\?\\*\\*" +
                ".+" +
                "\\*\\*What is happening / What is going wrong\\?\\*\\*" +
                ".+" +
                "\\*\\*What should be happening / What should be fixed\\?\\*\\*" +
                ".*" +
                "\\*\\*Video / Screenshot:\\*\\*" +
                ".*", Pattern.DOTALL
        );
        Matcher matcher = pattern.matcher(s);
        return matcher.matches();
    }

    public static MessageEmbed createHowBugReportEmbed () {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("How to Report Bugs");
        embed.setColor(Color.GRAY);
        embed.setDescription(
                "Welcome to the GTM Bug Reports channel! Please help us discover bugs and glitches so we can make " +
                        "GTM better and more fun for everyone! To post your bug report, **YOU MUST Copy and Paste** the " +
                        "below format in to chat, and then fill it out (otherwise the bot will delete it)! Please note, " +
                        "once sent, you **can NOT edit your report**, so please proof read your message before submitting it.\n" +
                        "Remember, a bug can be anything from a minor typo in a server message or something game breaking. If you " +
                        "aren't sure if something is a bug, you may still report it just in case, and an admin will verify the " +
                        "issue from there!\n\n" +
                "**\uD83C\uDF6C Rewards Program**\n" +
                "Submit bug reports to progress in the **Bug Catcher Achievement** on the GTM server. Once your report has been " +
                        "**reviewed & approved** by an Admin, you should see the progress bar on your achievement increase. Only " +
                        "players who report **unique** bugs will be credited in the achievement so please review the above bugs " +
                        "to see if your bug has already been reported.\n\n" +
                "**\uD83D\uDCA0 Additional Rewards**\n" +
                "In addition to the above, players who find and report **duplication bugs** involving any **currency** (including " +
                        "money, permits, crowbars...) OR **items** will be awarded a Store Gift Card of **up to $__100__ USD** depending " +
                        "on the severity and the scope of the duplication bug."
        );
        return embed.build();
    }

    public static Message getFormatMessage () {
        MessageBuilder msg = new MessageBuilder()
                .append("```")
                .append("**What server is this Bug Report for?**")
                .append("\n")
                .append("[Type what server here]")
                .append("\n\u200E\n")
                .append("**What is happening / What is going wrong?**")
                .append("\n")
                .append("[Explain the issue here (5+ words)]")
                .append("\n\u200E\n")
                .append("**What should be happening / What should be fixed?**")
                .append("\n")
                .append("[Explain the expected behavior here (If not applicable, type \"N/A\")]")
                .append("\n\u200E\n")
                .append("**Video / Screenshot:**")
                .append("\n")
                .append("[Upload or post the link to a video / screenshot of the bug (Required)]")
                .append("```");
        return msg.build();
    }

}
