package commands;

import Utils.Rank;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.ArrayList;

import static Utils.tools.GTools.hasRolePerms;
import static Utils.tools.GTools.sendThenDelete;

public class HelpCommand extends Command {

    public HelpCommand() {
        super("help", "View the help page", Rank.NORANK, Type.ANYWHERE);
    }

    @Override
    public void onCommandUse(Message message, Member member, TextChannel channel, String[] args) {
        // List of all commands that the member can use
        ArrayList<Command> commands = new ArrayList<>();
        for (Command command : Command.getCommands()) {
            if (hasRolePerms(member, command.getRank()))
                commands.add(command);
        }

        StringBuilder help = new StringBuilder();

        for (Command command : commands) {
            help.append("\n   **/")
                    .append(command.getName())
                    .append("** - *")
                    .append(command.getDescription())
                    .append("*");
        }

        MessageEmbed helpEmbed = new EmbedBuilder()
                .setTitle("List of All Available Commands:")
                .setDescription(help.toString())
                .build();

        // Send help embed and unless this is dms, delete later
        if (channel instanceof PrivateChannel) channel.sendMessage(helpEmbed).queue();
        else sendThenDelete(channel, helpEmbed);
    }

}
