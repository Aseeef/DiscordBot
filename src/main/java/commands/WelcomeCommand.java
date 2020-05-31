package commands;

import Utils.Rank;
import Utils.SelfData;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import static Utils.tools.GTools.*;
import static Utils.tools.WelcomeTools.getRuleReactEmbed;

public class WelcomeCommand extends Command {

    public WelcomeCommand() {
        super("welcome", "Manage welcome message & settings", Rank.ADMIN, Type.DISCORD_ONLY);
    }

    @Override
    public void onCommandUse(Message message, Member member, TextChannel channel, String[] args) {
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

        // If no sub commands match
        else {
            sendThenDelete(channel, getWelcomeHelpMsg());
        }
    }

    private static Message getWelcomeHelpMsg() {
        return new MessageBuilder()
                .append("> **Please enter a valid command argument:**\n")
                .append("> `/Welcome SetChannel` - *Set current channel to the welcome channel*\n")
                .build();
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
