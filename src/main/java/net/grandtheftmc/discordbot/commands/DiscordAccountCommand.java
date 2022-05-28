package net.grandtheftmc.discordbot.commands;

import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.grandtheftmc.discordbot.utils.Data;
import net.grandtheftmc.discordbot.utils.Utils;
import net.grandtheftmc.discordbot.utils.threads.ThreadUtil;
import net.grandtheftmc.discordbot.utils.tools.Verification;
import net.grandtheftmc.discordbot.utils.users.GTMUser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.awt.*;

public class DiscordAccountCommand extends Command {

    public DiscordAccountCommand() {
        super("discord", "Manage your gtm-discord account link", null, Type.DMS_ONLY);
    }

    @Override
    public void onCommandUse(SlashCommandInteraction interaction, MessageChannel channel, Member member, GTMUser gtmUser, String[] args) {

        if (args.length < 1) {
            Utils.sendThenDelete(channel, getAccountHelpMsg());
            return;
        }

        switch (args[0].toLowerCase()) {
            case "verify":

                if (args.length < 2) {
                    Utils.sendThenDelete(channel, "`/Discord Verify <Code>` - *Verify your discord account with GTM*");
                    return;
                }

                if (Data.exists(Data.USER, member.getIdLong())) {
                    GTMUser user = (GTMUser) Data.obtainData(Data.USER, member.getIdLong());
                    channel.sendMessage("**Your discord account is already linked to the player `" + user.getUsername() + "`!**").queue();
                    return;
                }

                ThreadUtil.runAsync( () -> {
                    boolean success = Verification.verifyMember(member, args[1]);
                    System.out.println("[Debug] Discord verification status for " + member.getAsMention() + ": Success=" + success);

                    if (success) {
                        GTMUser user = (GTMUser) Data.obtainData(Data.USER, member.getIdLong());
                        channel.sendMessage("**Verification successful! Your discord account has now been linked to `" + user.getUsername() + "`!**").queue();
                    } else {
                        channel.sendMessage("**Verification failed!** Please make sure you provided to correct verification code from GTM. To get this verification code, log on to GTM with your account and use /discord verify.").queue();
                    }
                });

                break;

            case "unverify":

                if (gtmUser == null) {
                    channel.sendMessage("**Your discord account is already not linked to any player!**").queue();
                    return;
                }

                boolean deleted = GTMUser.removeGTMUser(member.getIdLong());

                Verification.unVerifyUser(gtmUser);

                channel.sendMessage(deleted ? "**You have successfully unlinked your account from the player `" + gtmUser.getUsername() + "`!**" : "**Unable to proccess request. It appears something went wrong...!**").queue();

                break;

            case "info":
                if (gtmUser == null) Utils.sendThenDelete(channel, "**Your discord account is not linked to any user!**");
                else Utils.sendThenDelete(channel, getInfo(gtmUser).build());
                break;

            case "update":
                if (gtmUser == null) Utils.sendThenDelete(channel, "**Your discord account is not linked to any user!**");
                else {
                    ThreadUtil.runAsync(gtmUser::updateUserDataNow);
                    Utils.sendThenDelete(channel, "**Your account information has been updated!**");
                }
                break;

            default:
                Utils.sendThenDelete(channel, getAccountHelpMsg());
        }

    }

    private EmbedBuilder getInfo(GTMUser gtmUser) {
        return new EmbedBuilder()
                .setThumbnail(Utils.getSkullSkin(gtmUser.getUuid()))
                .setTitle("**Discord Account Information**")
                .setDescription("Your discord account is linked to the following GTM player...")
                .addField("**UUID:**", gtmUser.getUuid().toString(), false)
                .addField("**Username:**", "`" + gtmUser.getUsername() + "`", false)
                .addField("**Rank:**", gtmUser.getRank().n(), false)
                .setColor(new Color(207,181,59)) //gold color
                ;
    }

    private Message getAccountHelpMsg() {
        return new MessageBuilder()
                .append("> **Please enter a valid command argument:**\n")
                .append("> `/Discord Verify <Code>` - *Verify your discord account with GTM*\n")
                .append("> `/Discord UnVerify` - *Un-link your discord account with GTM*\n")
                .append("> `/Discord Info` - *Displays your current account information*\n")
                .append("> `/Discord Update` - *Force update your account data*\n")
                .build();
    }

}
