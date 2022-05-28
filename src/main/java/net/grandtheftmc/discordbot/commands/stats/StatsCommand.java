package net.grandtheftmc.discordbot.commands.stats;

import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.grandtheftmc.discordbot.commands.Command;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.grandtheftmc.discordbot.utils.Utils;
import net.grandtheftmc.discordbot.utils.users.GTMUser;
import net.grandtheftmc.discordbot.utils.users.Rank;

import java.util.List;

public class StatsCommand extends Command {

    public StatsCommand() {
        super("stats", "View details statistics on players or staff", Rank.SRMOD, Type.ANYWHERE);
    }

    @Override
    public void buildCommandData(SlashCommandData slashCommandData) {
        slashCommandData.addOption(OptionType.STRING, "player", "The in-game name of the player you want to generate statistics for.", true);
    }

    @Override
    public void onCommandUse(SlashCommandInteraction interaction, MessageChannel channel, List<OptionMapping> arguments, Member member, GTMUser gtmUser, String[] path) {

        StatsMenu menu = new StatsMenu(member.getUser(), channel, path[0]);
        boolean success = menu.load();

        if (!success) {
            Utils.sendThenDelete(channel, "**Invalid Player!** The player '" + path[0] + "' does not exist / has never played GTM!");
        }

    }



}
