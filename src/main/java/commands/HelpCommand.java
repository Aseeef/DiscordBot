package commands;

import Utils.tools.GTools;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;

import static Utils.tools.GTools.hasRolePerms;

public class HelpCommand extends ListenerAdapter {

    public void onGuildMessageReceived(GuildMessageReceivedEvent e) {

        String msg = e.getMessage().getContentRaw();
        Member member = e.getMember();
        User user = e.getAuthor();
        assert member != null;

        if (GTools.isCommand(msg, user, Commands.HELP) &&
                hasRolePerms(member, Commands.HELP.rank())
        ) {

            TextChannel channel = e.getChannel();

            channel.sendMessage(getCommandEmbed(member)).queue();

        }


    }

    private MessageEmbed getCommandEmbed(Member member) {

        // List of all commands that the member can use
        ArrayList<Commands> commands = new ArrayList<>();
        for (Commands command : Commands.values()) {
            if (hasRolePerms(member, command.rank()))
                commands.add(command);
        }

        StringBuilder help = new StringBuilder();

        for (Commands command : commands) {
            help.append("\n   **/")
                    .append(command.command())
                    .append("** - *")
                    .append(command.desc())
                    .append("*");
        }

        return new EmbedBuilder()
                .setTitle("List of All Available Commands:")
                .setDescription(help.toString())
                .build();
    }

}
