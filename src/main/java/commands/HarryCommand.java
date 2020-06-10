package commands;

import Utils.Config;
import Utils.users.GTMUser;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

public class HarryCommand extends Command {

    public HarryCommand() {
        super("harry", "Open DMs with Harry", null, Type.DISCORD_ONLY);
    }

    @Override
    public void onCommandUse(Message message, Member member, GTMUser gtmUser, MessageChannel channel, String[] args) {
        member.getUser().openPrivateChannel().queue( (privateChannel -> {
           privateChannel.sendMessage("**Hello! I am Harry. The discord bot. How can I help you?** \n Tip: Use `" + Config.get().getCommandPrefix() +"help` to see a list of commands you can use!").queue();
        }));
    }

}
