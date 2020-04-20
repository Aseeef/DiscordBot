package events;

import Utils.Rank;
import Utils.SelfData;
import Utils.tools.Logs;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import static Utils.tools.GTools.hasRolePerms;
import static Utils.tools.GTools.jda;
import static Utils.tools.WelcomeTools.getWelcomeEmbed;

public class OnReactRules extends ListenerAdapter {

    public void onGuildMessageReactionAdd (GuildMessageReactionAddEvent e) {

        TextChannel channel = e.getChannel();
        User user = e.getUser();
        Member member = e.getMember();

        TextChannel ruleChannel = jda.getTextChannelById(SelfData.get().getRuleAgreeChannelId());
        long msgId = e.getMessageIdLong();

        if (!user.isBot() && ruleChannel != null && msgId == SelfData.get().getRuleAgreeMessageId()) {

            // Remove reaction
            e.getChannel().removeReactionById(msgId, e.getReactionEmote().getEmote(), e.getUser()).queue();

            Emote gtmAgree = jda.getEmotesByName("gtmagree", true).get(0);
            Emote gtmDisagree = jda.getEmotesByName("gtmdisagree", true).get(0);

            // If user agrees to rules & is unverified
            if (e.getReactionEmote().getEmote() == gtmAgree && hasRolePerms(member, Rank.UNVERIFIED)) {

                user.openPrivateChannel().queue( (privateChannel) ->
                        privateChannel.sendMessage(getWelcomeEmbed(user)).queue()
                );

                e.getGuild().removeRoleFromMember(member, Rank.UNVERIFIED.er()).queue();

                // Log
                Logs.log(user.getAsTag() + " (" + user.getId() + ") has agreed to rules and is now verified!");

            }

            // If user doesn't agree to rules & is unverified, msg them and kick
            else if (e.getReactionEmote().getEmote() == gtmDisagree && hasRolePerms(member, Rank.UNVERIFIED)) {

                user.openPrivateChannel().queue( (privateChannel) ->
                    privateChannel.sendMessage("**You have been kicked from the GTM Discord!** I am sorry but you have to agree to our rules in order to use the GTM discord. If you change your mind, you are free to rejoin us at http://grandtheftmc.net/discord!").queue( (msg) ->
                            member.kick("Rejected discord rules").queue()
                    ));

                // Log
                Logs.log(user.getAsTag() + " (" + user.getId() + ") has rejected the rules and was kicked from the server.");

            }


        }

    }

}
