package net.grandtheftmc.discordbot.commands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.grandtheftmc.discordbot.utils.confighelpers.Config;
import net.grandtheftmc.discordbot.utils.Utils;
import net.grandtheftmc.discordbot.utils.users.GTMUser;

import java.util.List;

public class HarryCommand extends Command {

    public HarryCommand() {
        super("harry", "Open DMs with Harry", null, Type.DISCORD_ONLY);
    }

    @Override
    public void buildCommandData(SlashCommandData slashCommandData) {

    }

    @Override
    public void onCommandUse(SlashCommandInteraction interaction, MessageChannel channel, List<OptionMapping> arguments, Member member, GTMUser gtmUser, String[] path) {
        member.getUser().openPrivateChannel().queue(
                privateChannel -> privateChannel.sendMessage("**Hello! I am Harry. The discord bot. How can I help you?** \n Tip: Use `" + Config.get().getCommandPrefix() +"help` to see a list of commands you can use!").queue( pc ->
                        Utils.sendThenDelete(channel, "** " + member.getAsMention() + " I have opened a private channel conversation with you! Check your direct messages.**"),
                error -> {
                    if (error != null) {
                        Utils.sendThenDelete(channel, "**" + member.getAsMention() + " I was unable to DM you! Please make sure you have your messages from members of this this server enabled in your privacy settings as shown below!**", Utils.getAsset("whitelist.png"));
                    }
                }));
    }

}
