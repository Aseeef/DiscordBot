package commands;

import Utils.Rank;
import Utils.SelfData;
import Utils.tools.GTools;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import static Utils.tools.GTools.*;
import static Utils.tools.WelcomeTools.getRuleReactEmbed;

public class WelcomeCommand extends ListenerAdapter {

    public void onGuildMessageReceived (GuildMessageReceivedEvent e) {

        String msg = e.getMessage().getContentRaw();
        Member member = e.getMember();
        User user = e.getAuthor();
        assert member != null;

        if (GTools.isCommand(msg, user, Commands.WELCOME) &&
                hasRolePerms(member, Commands.WELCOME.rank())
        ) {

            String[] args = getArgs(msg);
            TextChannel channel = e.getChannel();

            if (args[0].toLowerCase().equalsIgnoreCase("setchannel")) {

                // Delete previous rule embed (if any)
                TextChannel prevChannel = jda.getTextChannelById(SelfData.get().getRuleAgreeChannelId());
                long prevMsgId = SelfData.get().getRuleAgreeMessageId();
                if (prevChannel != null) {
                    prevChannel.retrieveMessageById(prevMsgId).queue( (prevMsg) -> {
                        if (prevMsg != null)
                                prevMsg.delete().queue();
                    });
                }

                // Set as player welcome channel
                SelfData.get().setRuleAgreeChannelId(channel.getIdLong());

                // Send success msg
                sendThenDelete(channel, welcomeChannelSet(channel));

                // Send rule embed & add reactions & save msg to SelfData
                channel.sendMessage(getRuleReactEmbed()).queue( (embed) -> {

                    Emote gtmAgree = jda.getEmotesByName("gtmagree", true).get(0);
                    Emote gtmDisagree = jda.getEmotesByName("gtmdisagree", true).get(0);
                    embed.addReaction(gtmAgree).queue();
                    embed.addReaction(gtmDisagree).queue();

                    SelfData.get().setRuleAgreeMessageId(embed.getIdLong());

                });

            }

            else if (args[0].toLowerCase().equalsIgnoreCase("setmessage")) {


            }


        }

    }

    private static Message welcomeChannelSet(TextChannel channel) {
        return new MessageBuilder()
                .append("**")
                .append(channel.getAsMention())
                .append(" has been set as the welcome channel!")
                .append("**")
                .build();
    }

}
