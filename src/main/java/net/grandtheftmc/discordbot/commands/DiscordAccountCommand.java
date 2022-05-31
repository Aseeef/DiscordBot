package net.grandtheftmc.discordbot.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.grandtheftmc.discordbot.utils.Data;
import net.grandtheftmc.discordbot.utils.Utils;
import net.grandtheftmc.discordbot.utils.threads.ThreadUtil;
import net.grandtheftmc.discordbot.utils.tools.Verification;
import net.grandtheftmc.discordbot.utils.users.GTMUser;

import java.awt.*;
import java.util.List;

public class DiscordAccountCommand extends Command {

    public DiscordAccountCommand() {
        super("discord", "Manage your gtm-discord account link", null, Type.ANYWHERE);
    }

    @Override
    public void buildCommandData(SlashCommandData slashCommandData) {
        SubcommandData verify = new SubcommandData("verify", "Verify your discord account with GTM");
        verify.addOption(OptionType.STRING, "secret-code", "Your secret verification code (NEVER SHARE THIS)", true);

        SubcommandData unVerify = new SubcommandData("unverify", "Un-link your discord account with GTM");

        SubcommandData info = new SubcommandData("info", "Displays your current account information");

        SubcommandData update = new SubcommandData("update", "Force update your account data");

        slashCommandData.addSubcommands(verify, unVerify, info, update);
    }

    @Override
    public void onCommandUse(SlashCommandInteraction interaction, MessageChannel channel, List<OptionMapping> arguments, Member member, GTMUser gtmUser, String[] path) {

        switch (path[0].toLowerCase()) {
            case "verify":

                if (Data.exists(Data.USER, member.getIdLong())) {
                    GTMUser user = (GTMUser) Data.obtainData(Data.USER, member.getIdLong());
                    interaction.reply("**Your discord account is already linked to the player `" + user.getUsername() + "`!**").setEphemeral(true).queue();
                    return;
                }

                ThreadUtil.runAsync( () -> {
                    String verifyCode = interaction.getOption("secret-code").getAsString();
                    boolean success = Verification.verifyMember(member, verifyCode);
                    System.out.println("[Debug] Discord verification status for " + member.getAsMention() + ": Success=" + success);

                    if (success) {
                        GTMUser user = (GTMUser) Data.obtainData(Data.USER, member.getIdLong());
                        interaction.reply("**Verification successful! Your discord account has now been linked to `" + user.getUsername() + "`!**").setEphemeral(true).queue();
                    } else {
                        interaction.reply("**Verification failed!** Please make sure you provided to correct verification code from GTM. To get this verification code, log on to GTM with your account and use /discord verify.").setEphemeral(true).queue();
                    }
                });

                break;

            case "unverify":

                if (gtmUser == null) {
                    interaction.reply("**Your discord account is already not linked to any player!**").setEphemeral(true).queue();
                    return;
                }

                boolean deleted = GTMUser.removeGTMUser(member.getIdLong());

                Verification.unVerifyUser(gtmUser);

                interaction.reply(deleted ? "**You have successfully unlinked your account from the player `" + gtmUser.getUsername() + "`!**" : "**Unable to proccess request. It appears something went wrong...!**").setEphemeral(true).queue();

                break;

            case "info":
                if (gtmUser == null) interaction.reply("**Your discord account is not linked to any user!**").setEphemeral(true).queue();
                else interaction.replyEmbeds(getInfo(gtmUser).build()).setEphemeral(true).queue();
                break;

            case "update":
                if (gtmUser == null) interaction.reply( "**Your discord account is not linked to any user!**").setEphemeral(true).queue();
                else {
                    ThreadUtil.runAsync(gtmUser::updateUserDataNow);
                    interaction.reply("**Your account information has been updated!**").setEphemeral(true).queue();
                }
                break;

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

}
