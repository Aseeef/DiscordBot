package net.grandtheftmc.discordbot.commands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.grandtheftmc.discordbot.utils.users.GTMUser;
import net.grandtheftmc.discordbot.utils.users.Rank;

public class SeniorsCommand extends Command {

    public SeniorsCommand() {
        super("seniors", "Manage senior related settings", Rank.ADMIN, Type.DISCORD_ONLY);
    }

    @Override
    public void onCommandUse(SlashCommandInteraction interaction, MessageChannel channel, Member member, GTMUser gtmUser, String[] args) {
        if (args.length == 0) {

        }


        else if (args[0].equalsIgnoreCase("setchannel")) {


        }


    }

}
