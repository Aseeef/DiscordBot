package commands;

import utils.confighelpers.Config;
import utils.tools.GTools;
import utils.users.GTMUser;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

public class HarryCommand extends Command {

    public HarryCommand() {
        super("harry", "Open DMs with Harry", null, Type.DISCORD_ONLY);
    }

    @Override
    public void onCommandUse(Message message, Member member, GTMUser gtmUser, MessageChannel channel, String[] args) {
        member.getUser().openPrivateChannel().queue(
                privateChannel -> privateChannel.sendMessage("**Hello! I am Harry. The discord bot. How can I help you?** \n Tip: Use `" + Config.get().getCommandPrefix() +"help` to see a list of commands you can use!").queue( pc ->
                        GTools.sendThenDelete(channel, "**I have opened a private channel conversation with you! Check your direct messages.**"),
                error -> {
                    if (error != null) {
                        GTools.sendThenDelete(channel, "**" + member.getAsMention() + " I was unable to DM you! Please make sure you have your messages from members of this this server enabled in your privacy settings as shown below!**", GTools.getAsset("whitelist.png"));
                    };
                }));
    }

}
