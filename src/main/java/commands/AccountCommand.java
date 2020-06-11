package commands;

import Utils.Data;
import Utils.database.DAO;
import Utils.tools.GTools;
import Utils.tools.Verification;
import Utils.users.GTMUser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.json.JSONObject;

import java.awt.*;

public class AccountCommand extends Command {

    public AccountCommand() {
        super("account", "Manage your gtm-discord account link", null, Type.DMS_ONLY);
    }

    @Override
    public void onCommandUse(Message message, Member member, GTMUser gtmUser, MessageChannel channel, String[] args) {

        if (args.length < 1) {
            GTools.sendThenDelete(channel, getAccountHelpMsg());
            return;
        }

        switch (args[0].toLowerCase()) {
            case "verify":

                if (args.length < 2) {
                    GTools.sendThenDelete(channel, "`/Account Verify <Code>` - *Verify your discord account with GTM*");
                    return;
                }

                if (Data.exists(Data.USER, member.getIdLong())) {
                    GTMUser user = (GTMUser) Data.obtainData(Data.USER, member.getIdLong());
                    channel.sendMessage("**Your discord account is already linked to the player `" + user.getUsername() + "`!**").queue();
                    return;
                }

                GTools.runAsync( () -> {
                    boolean success = Verification.verifyMember(member, args[1]);

                    if (success) {
                        GTMUser user = (GTMUser) Data.obtainData(Data.USER, member.getIdLong());
                        channel.sendMessage("**Verification successful! Your discord account has now been linked to `" + user.getUsername() + "`!**").queue();
                    } else {
                        channel.sendMessage("**Verification failed!** Please make sure you provided to correct verification code.").queue();
                    }
                });

                break;

            case "unverify":

                if (gtmUser == null) {
                    channel.sendMessage("**Your discord account is already not linked to any player!**").queue();
                    return;
                }

                GTMUser user = (GTMUser) Data.obtainData(Data.USER, member.getIdLong());
                boolean deleted = GTMUser.removeGTMUser(member.getIdLong());

                GTools.runAsync( () -> {
                    DAO.deleteDiscordProfile(user.getUuid());
                });

                DAO.sendToGTM("unverify", new JSONObject().put("uuid", user.getUuid()));

                channel.sendMessage(deleted ? "**You have successfully unlinked your account from the player `" + user.getUsername() + "`!**" : "**Unable to proccess request. It appears something went wrong...!**").queue();

                break;

            case "info":
                if (gtmUser == null) GTools.sendThenDelete(channel, "**Your discord account is not linked to any user!**");
                else GTools.sendThenDelete(channel, getInfo(gtmUser));
                break;

            case "update":
                if (gtmUser == null) GTools.sendThenDelete(channel, "**Your discord account is not linked to any user!**");
                else {
                    gtmUser.updateUserDataNow();
                    GTools.sendThenDelete(channel, "**Your account information has been updated!**");
                }

                break;

            default:
                GTools.sendThenDelete(channel, getAccountHelpMsg());
        }

    }

    private MessageEmbed getInfo(GTMUser gtmUser) {
        return new EmbedBuilder()
                .setThumbnail(DAO.getSkullSkin(gtmUser.getUuid()))
                .setTitle("**Discord Account Information**")
                .setDescription("You discord account is linked to the following GTM player...")
                .addField("**UUID:**", gtmUser.getUuid().toString(), false)
                .addField("**Username:**", gtmUser.getUsername(), false)
                .addField("**Rank:**", gtmUser.getRank().n(), false)
                .setColor(new Color(207,181,59)) //gold color
                .build();
    }

    private Message getAccountHelpMsg() {
        return new MessageBuilder()
                .append("> **Please enter a valid command argument:**\n")
                .append("> `/Account Verify <Code>` - *Verify your discord account with GTM*\n")
                .append("> `/Account UnVerify` - *Un-link your discord account with GTM*\n")
                .append("> `/Account Info` - *Displays your current account information*\n")
                .append("> `/Account Update` - *Force update your account data*\n")
                .build();
    }

}
