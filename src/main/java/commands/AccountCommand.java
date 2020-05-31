package commands;

import Utils.Data;
import Utils.Rank;
import Utils.tools.GTools;
import Utils.tools.Verification;
import Utils.users.GTMUser;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class AccountCommand extends Command {

    public AccountCommand() {
        super("account", "Manage your gtm-discord account link", Rank.NORANK, Type.DMS_ONLY);
    }

    @Override
    public void onCommandUse(Message message, Member member, TextChannel channel, String[] args) {

        if (args.length < 1) {
            GTools.sendThenDelete(channel, getAccountHelpMsg());
        }

        switch (args[1].toLowerCase()) {
            case "verify":

                if (Data.exists(Data.USER, member.getIdLong())) {
                    GTMUser user = (GTMUser) Data.obtainData(Data.USER, member.getIdLong());
                    channel.sendMessage("**Your discord account is already linked to the player `" + user.getUsername() + "`!**").queue();
                    return;
                }

                boolean success = Verification.verifyMember(member, args[2]);

                if (success) {
                    GTMUser user = (GTMUser) Data.obtainData(Data.USER, member.getIdLong());
                    channel.sendMessage("**Verification successful! Your discord account has now been linked to `" + user.getUsername() + "`!").queue();
                } else {
                    channel.sendMessage("**Verification failed!** Please make sure you provided to correct verification code.").queue();
                }

                break;

            case "unverify":

                if (!Data.exists(Data.USER, member.getIdLong())) {
                    channel.sendMessage("**Your discord account is already not linked to any player!**").queue();
                    return;
                }

                GTMUser user = (GTMUser) Data.obtainData(Data.USER, member.getIdLong());
                GTMUser.removeGTMUser(member.getIdLong());
                boolean deleted = Data.deleteData(Data.USER, member.getIdLong());

                channel.sendMessage(deleted ? "**You have successfully unlinked your account from the player `" + user.getUsername() + "`!**" : "**Unable to proccess request. It appears something went wrong...!**").queue();

                break;

            case "info":
                //TODO
                break;
            default:
                GTools.sendThenDelete(channel, getAccountHelpMsg());
        }

    }

    private Message getAccountHelpMsg() {
        return new MessageBuilder()
                .append("> **Please enter a valid command argument:**\n")
                .append("> `/Account Verify <Code>` - *Verify your discord account with GTM*\n")
                .append("> `/Account UnVerify` - *Un-link your discord account with GTM*\n")
                .append("> `/Account Info` - *Displays your account information*\n")
                .build();
    }

}
