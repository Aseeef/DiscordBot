package net.grandtheftmc.discordbot.commands.punishments;

import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.grandtheftmc.discordbot.commands.Command;
import net.grandtheftmc.discordbot.utils.users.GTMUser;
import net.grandtheftmc.discordbot.utils.users.Rank;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

public class MuteCommand extends Command {

    public MuteCommand() {
        super("mute", "Allows moderators to mute target users in some/all channels", Rank.HELPER, Type.DISCORD_ONLY);
    }

    @Override
    public void onCommandUse(SlashCommandInteraction interaction, MessageChannel channel, Member member, GTMUser gtmUser, String[] args) {

    }
}
