package commands.bugs;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.jetbrains.annotations.NotNull;
import utils.StringUtils;
import utils.Utils;
import utils.threads.ThreadUtil;
import utils.users.GTMUser;
import utils.web.ImgurUploader;
import utils.web.clickup.CUTask;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static utils.console.Logs.log;
import static utils.Utils.JDA;
import static utils.Utils.sendThenDelete;

public class ReportListener extends ListenerAdapter {

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {

        TextChannel channel = event.getChannel();
        User user = event.getAuthor();
        Message msg = event.getMessage();
        String rawMsg = msg.getContentRaw();

        if (channel != BugReport.getBugReportChannel()) return;
        if (user.isBot()) return;
        if (Utils.isCommand(rawMsg, user)) return;
        if (!GTMUser.getGTMUser(user.getIdLong()).isPresent()) {
            sendThenDelete(channel, user.getAsMention() + " you are not allowed to post bug reports " +
                    "until you have verified your discord account to GTM by using the `/discord verify` command in game!");

            // DM user their deleted bug so it can be reposted
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
            ThreadUtil.runAsync(() -> {

                String attachmentUrl = null;

                if (msg.getAttachments().size() > 0) {
                    Message.Attachment attachment = msg.getAttachments().get(0);
                    if (attachment == null || attachment.getFileExtension() == null) return;

                    if (ImgurUploader.isSupportedExtension(attachment.getFileExtension().toLowerCase())) {
                        if (ImgurUploader.checkAttachmentSize(attachment)) {
                            try {
                                attachmentUrl = ImgurUploader.uploadMedia(attachment.getFileName(), attachment.retrieveInputStream().get(), attachment.isVideo());
                                // mkv isn't properly support by imgur. API returns broken link with a . at the end.
                                if (attachment.getFileExtension().equalsIgnoreCase("mkv")) {
                                    if (attachmentUrl.endsWith("."))
                                        attachmentUrl = attachmentUrl.substring(0, attachmentUrl.length() - 1);
                                }
                            } catch (InterruptedException | ExecutionException | IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            sendThenDelete(channel, user.getAsMention() + ", sorry but the attachment file size is too large for me to process. Try upload a smaller file, or share using a media sharing service like Dropbox/Google Drive/Imgur/Youtube.");
                            PrivateChannel pc = user.openPrivateChannel().complete();
                            pc.sendMessage("**Your Bug Report was deleted because your attachment is too large. Please note that images must be less then 10 MB and videos must be less then 200 MB! Here is your deleted bug report so you can repost it:**\n```" + rawMsg + "```")
                                    .complete();
                            return;
                        }
                    } else {
                        sendThenDelete(channel, user.getAsMention() + ", you used an unsupported attachment for you bug report! Supported attachment formats are: " + Arrays.asList(ImgurUploader.IMAGE_FORMATS, ImgurUploader.VIDEO_FORMATS));
                        PrivateChannel pc = user.openPrivateChannel().complete();
                        pc.sendMessage("**Your Bug Report was deleted because you attached an unsupported file! Only include **video** or **image** files using one of the following formats: " + Arrays.asList(ImgurUploader.IMAGE_FORMATS, ImgurUploader.VIDEO_FORMATS) + "! Here is your deleted bug report so you can repost it:**\n```" + rawMsg + "```")
                                .complete();
                        return;
                    }
                }

                String updatedMsg = rawMsg;
                Matcher matcher = getFormatMatcher(rawMsg);
                matcher.find();
                String priority = matcher.group(4);
                int p = 2;
                for (char c : priority.toCharArray()) {
                    if (Character.isDigit(c)) {
                        p = Character.getNumericValue(c);
                    }
                }
                if (p > 4 || p < 1) p = 2;

                int cuPriority;
                if (p == 1) cuPriority = 4;
                else if (p == 2) cuPriority = 3;
                else if (p == 3) cuPriority = 2;
                else cuPriority = 1;

                if (attachmentUrl != null) {
                    updatedMsg = StringUtils.replaceLast(updatedMsg, matcher.group(5), "\n");
                    updatedMsg = updatedMsg + "\n(" + attachmentUrl + ")";
                }

                BugReport report = new BugReport(0L, 0L, updatedMsg, false, user.getIdLong(), BugReport.ReportStatus.AWAITING_REVIEW);

                new CUTask(GTMUser.getGTMUser(user.getIdLong()).get().getUsername(), user.getIdLong(), updatedMsg, BugReport.ReportStatus.AWAITING_REVIEW, cuPriority).createTask().thenAccept((id) -> {
                   report.setId(id);
                   report.save();

                   // send msg
                   Message bugMsg = BugReport.getBugReceiveChannel().sendMessageEmbeds(report.createEmbed(false)).complete();
                   report.setReceiveChannelId(bugMsg.getIdLong());
                   // delete original
                   event.getMessage().delete().complete();
                    PrivateChannel userChannel = user.openPrivateChannel().complete();
                    userChannel.sendMessage("**Hey!** We received the following bug report from you:").setEmbeds(report.createEmbed(false)).complete();
                    userChannel.sendMessage("I will notify you if there are any further updates on this report. Thank you!").complete();
                    String gtmAgree = JDA.getEmotesByName("gtmagree", true).get(0).getAsMention();
                    Utils.sendThenDelete(channel, gtmAgree + " **Success!** You submitted a bug report. Check your DMs for more info! (If you didn't receive a DM you may have your private messages turned off)");
                });
            });
        } else {
            event.getMessage().delete().queue();
            Utils.sendThenDelete(channel, "**Hey!** " + user.getAsMention() + ", sorry but your message does not follow the bug reports format. I am sending the details in your PMs...");
            // DM user their deleted bug so it can be reposted
            user.openPrivateChannel().queue(userChannel ->
                    userChannel.sendMessage("**The following bug report from you was deleted because it did not follow the bug reports format:**\n```" + rawMsg + "```\n" + "**Please copy paste this exact format in to your message and repost your bug report:**\n")
                            .queue( (success) -> userChannel.sendMessage(getFormatMessage()).queue(),
                                    (error) -> Utils.sendThenDelete(channel, "**Hey!** " + user.getAsMention() + ", sorry but I was unable to message you because you have your PMs disabled. Use /Harry for more info."))
            );

            // Log failure
            log("Incorrect Format: Deleted the following bug report from user "+user.getAsTag()+" ("+user.getId()+"):"+
                    "\n" + rawMsg);
        }

    }

    private static boolean match(String s) {
        Matcher matcher = getFormatMatcher(s);
        return matcher.matches();
    }

    private static Matcher getFormatMatcher(String s) {
        Pattern pattern = Pattern.compile(
                "\\*\\*What server is this Bug Report for\\?\\*\\*(" +
                        ".+)" +
                        "\\*\\*What is happening / What is going wrong\\?\\*\\*(" +
                        ".+)" +
                        "\\*\\*What should be happening / What should be fixed\\?\\*\\*(" +
                        ".*)" +
                        "\\*\\*On a scale of 1-4, urgent is this bug?\\?\\*\\*(" +
                        ".*)" +
                        "\\*\\*Video / Screenshot:\\*\\*(" +
                        ".*)", Pattern.DOTALL
        );
        return pattern.matcher(s);
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
                .append("**On a scale of 1-4, urgent is this bug?**")
                .append("\n")
                .append("[1=Not Urgent, 2=Normal, 3=Urgent 4=CRITIAL]")
                .append("\n\u200E\n")
                .append("**Video / Screenshot:**")
                .append("\n")
                .append("[Upload or post the link to a video / screenshot of the bug (Required)]")
                .append("```");
        return msg.build();
    }

}
