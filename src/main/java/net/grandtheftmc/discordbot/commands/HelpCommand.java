package net.grandtheftmc.discordbot.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.grandtheftmc.discordbot.utils.confighelpers.Config;
import net.grandtheftmc.discordbot.utils.users.GTMUser;
import net.grandtheftmc.discordbot.utils.users.Rank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.grandtheftmc.discordbot.utils.Utils.sendThenDelete;

public class HelpCommand extends Command {

    public HelpCommand() {
        super("help", "View the help page", null, Type.ANYWHERE);
    }

    @Override
    public void buildCommandData(SlashCommandData slashCommandData) {

    }

    @Override
    public void onCommandUse(SlashCommandInteraction interaction, MessageChannel channel, List<OptionMapping> arguments, Member member, GTMUser gtmUser, String[] path) {
        // List of all commands that the member can use
        List<String> commands = new ArrayList<>();
        for (Command command : getCommands()) {
            if (Rank.hasRolePerms(member, command.getRank()))
                commands.add(command.getName());
        }

        // Sorts in alphabetical order
        Collections.sort(commands);

        StringBuilder help = new StringBuilder();

        for (String commandName : commands) {
            Command command = getByName(commandName);
            help.append("\n   **")
                    .append(Config.get().getCommandPrefix())
                    .append(commandName)
                    .append("** - *").append(command.getDescription())
                    .append("* ")
                    .append("`")
                    .append(getChannelMessage(command))
                    .append("`")
                    ;
        }

        MessageEmbed helpEmbed = new EmbedBuilder()
                .setTitle("List of All Available Commands:")
                .setDescription(help.toString())
                .build();

        // Send help embed and unless this is dms, delete later
        if (channel instanceof PrivateChannel) channel.sendMessageEmbeds(helpEmbed).queue();
        else sendThenDelete(channel, helpEmbed);
    }

    private String getChannelMessage(Command c) {
        if (c.getType() == Type.DMS_ONLY)
            return "[DMS-ONLY]";
        else if (c.getType() == Type.DISCORD_ONLY)
            return "[DISCORD-ONLY]";
        else return "[ANYWHERE]";
    }

}
