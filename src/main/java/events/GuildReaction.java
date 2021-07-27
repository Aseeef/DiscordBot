package events;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import utils.users.GTMUser;

public class GuildReaction extends ListenerAdapter {

    @Override
    public void onGuildMessageReactionAdd (GuildMessageReactionAddEvent e) {

        User user = e.getUser();
        Member member = e.getMember();
        TextChannel channel = e.getChannel();

        GTMUser gtmUser = GTMUser.getGTMUser(user.getIdLong()).orElse(null);

        /* TODO? Maybe make it so only verified users can react to suggestions...
        if (channel == SuggestionTools.getSuggestionsChannel()) {

            String msg = "**Hey!** I noticed your tried to react to a suggestion on discord. In order to leave your opinion on a suggestion, you have to"
            if (gtmUser == null) {
                user.openPrivateChannel()
                        .queue(privateChannel -> privateChannel.sendMessage("**Hey!** I noticed your tried to react to a suggestion on discord.")
                                .queue(pc -> GTools.sendThenDelete(channel, "** " + member.getAsMention() + " I have opened a private channel conversation with you! Check your direct messages.**"),
                                        error -> {
                                            if (error != null) {
                                                GTools.sendThenDelete(channel, "**" + member.getAsMention() + " I was unable to DM you! Please make sure you have your messages from members of this this server enabled in your privacy settings as shown below!**", GTools.getAsset("whitelist.png"));
                                            }
                                            ;
                                        }));
            }

        }

         */


    }

}
