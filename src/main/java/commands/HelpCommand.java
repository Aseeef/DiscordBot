package commands;

import utils.confighelpers.Config;
import utils.Rank;
import utils.users.GTMUser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static utils.tools.GTools.sendThenDelete;

public class HelpCommand extends Command {

    public HelpCommand() {
        super("help", "View the help page", null, Type.ANYWHERE);
    }

    @Override
    public void onCommandUse(Message message, Member member, GTMUser gtmUser, MessageChannel channel, String[] args) {
        // List of all commands that the member can use
        List<String> commands = new ArrayList<>();
        for (Command command : Command.getCommands()) {
            if (Rank.hasRolePerms(member, command.getRank()))
                commands.add(command.getName());
        }

        // Sorts in alphabetical order
        Collections.sort(commands);

        StringBuilder help = new StringBuilder();

        for (String commandName : commands) {
            Command command = Command.getByName(commandName);
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
        if (channel instanceof PrivateChannel) channel.sendMessage(helpEmbed).queue();
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
