package net.grandtheftmc.discordbot.commands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.grandtheftmc.discordbot.utils.users.GTMUser;
import net.grandtheftmc.discordbot.utils.users.Rank;

public class RebootCommand extends Command {

    public RebootCommand() {
        super("reboot", "Restart the discord bot", Rank.ADMIN, Type.ANYWHERE);
    }

    @Override
    public void onCommandUse(SlashCommandInteraction interaction, MessageChannel channel, Member member, GTMUser gtmUser, String[] args) {
        // Only need to open a private channel if this isn't already dms
        if (channel instanceof PrivateChannel)
            channel.sendMessage("**Executed reboot. Please allow up to 30 seconds for the bot to come back up!**").queue();
        else
        member.getUser().openPrivateChannel().queue( (privateChannel) ->
            privateChannel.sendMessage("**Executed reboot. Please allow up to 30 seconds for the bot to come back up!**")
                    .queue( (m) -> System.exit(1)));
    }

}
