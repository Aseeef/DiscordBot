package commands;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import utils.users.Rank;
import utils.SelfData;
import utils.tools.GTools;
import utils.users.GTMUser;
import utils.webhooks.WebhookUtils;

import java.util.List;

public class AnnoyCommand extends Command {

    public AnnoyCommand() {
        super("annoy", "Useful admin utility command to be used against people you don't like", Rank.ADMIN, Type.DISCORD_ONLY);
    }

    @Override
    public void onCommandUse(Message message, Member member, GTMUser gtmUser, MessageChannel channel, String[] args) {

        if (args.length < 1) {
            GTools.sendThenDelete(channel, getCommandUsage());
            return;
        }

        switch (args[0].toLowerCase()) {
            case "emoji": {
                if (args.length < 3) {
                    GTools.sendThenDelete(channel, "Usage: `/Annoy Emoji <@User> <Emoji>`");
                    return;
                }
                if (alreadyAnnoying(channel, member))
                    return;

                Member target = message.getMentionedMembers().get(0);
                if (target == null) {
                    GTools.sendThenDelete(channel, "**User not found!**");
                    return;
                }

                List<Emote> emojis = message.getEmotes();
                String emoji;
                String emojiTag;

                if (emojis.size() >= 1) {
                    emoji = emojis.get(0).getId();
                    emojiTag = emojis.get(0).getAsMention();
                } else {
                    emoji = args[2];
                    emojiTag = emoji;
                }

                GTools.sendThenDelete(channel, "**I will now give " + target.getEffectiveName() + " the care they deserve by reacting to all of their messages with a " + emojiTag + "!**");
                SelfData.get().getEmojiAnnoyMap().put(target.getIdLong(), emoji);
                SelfData.get().update();

                break;
            }
            case "educate": {
                if (args.length < 3) {
                    GTools.sendThenDelete(channel, "Usage: `/Annoy Educate <@User> <Hours>`");
                    return;
                }
                if (alreadyAnnoying(channel, member))
                    return;

                Member target = message.getMentionedMembers().get(0);
                if (target == null) {
                    GTools.sendThenDelete(channel, "**User not found!**");
                    return;
                }
                try {
                    long hours = Long.parseLong(args[2]);
                    SelfData.get().getQuoteAnnoyMap().put(target.getIdLong(), new Long[] {hours, 0L});
                    SelfData.get().update();
                    GTools.sendThenDelete(channel, "**I will now begin sharing some of my great wisdom with " + target.getEffectiveName() + " every " + hours + " hour(s)!**");
                } catch (NumberFormatException e) {
                    GTools.sendThenDelete(channel, "**" + args[2] + " is not a number!**");
                }

                break;
            }
            case "scrabble": {
                if (args.length < 3) {
                    GTools.sendThenDelete(channel, "Usage: `/Annoy scrabble <@User> <Character>`");
                    return;
                }
                if (alreadyAnnoying(channel, member))
                    return;

                Member target = message.getMentionedMembers().get(0);
                if (target == null) {
                    GTools.sendThenDelete(channel, "**User not found!**");
                    return;
                }
                if (args[2].length() > 1) {
                    GTools.sendThenDelete(channel, "**You can only replace the start of the player's words with a single character!**");
                    return;
                }

                char character = args[2].charAt(0);

                SelfData.get().getScrabbleAnnoyMap().put(target.getIdLong(), character);
                SelfData.get().update();
                GTools.sendThenDelete(channel, "**" + target.getEffectiveName() + " words will now be converted to match their IQ!**");

                break;
            }
            case "bot": {
                if (args.length < 2) {
                    GTools.sendThenDelete(channel, "Usage: `/Annoy bot <@User>`");
                    return;
                }
                if (alreadyAnnoying(channel, member))
                    return;

                Member target = message.getMentionedMembers().get(0);
                if (target == null) {
                    GTools.sendThenDelete(channel, "**User not found!**");
                    return;
                }

                SelfData.get().getBotAnnoyList().add(target.getIdLong());
                SelfData.get().update();
                GTools.sendThenDelete(channel, "**" + target.getEffectiveName() + " is now a bot! Beep bop!**");

                break;
            }
            case "stop": {
                if (args.length < 2) {
                    GTools.sendThenDelete(channel, "Usage: `/Annoy Stop <@User>`");
                    return;
                }

                Member target = message.getMentionedMembers().get(0);
                if (target == null) {
                    GTools.sendThenDelete(channel, "**User not found!**");
                    return;
                }
                if (SelfData.get().getQuoteAnnoyMap().remove(target.getIdLong()) != null ||
                        SelfData.get().getEmojiAnnoyMap().remove(target.getIdLong()) != null ||
                        SelfData.get().getScrabbleAnnoyMap().remove(target.getIdLong()) != null ||
                        SelfData.get().getBotAnnoyList().remove(target.getIdLong())) {
                    GTools.sendThenDelete(channel, "**Ok. I will now stop bothering " + target.getEffectiveName() + "... :(**");
                    SelfData.get().update();
                }
                else
                    GTools.sendThenDelete(channel, "**I am already not annoying " + target.getEffectiveName() + "!**");

                break;
            }
            case "sudo": {
                if (args.length < 3) {
                    GTools.sendThenDelete(channel, "Usage: `/Annoy Impersonate <@User> <Message>`");
                    return;
                }

                Member target = message.getMentionedMembers().get(0);
                if (target == null) {
                    GTools.sendThenDelete(channel, "**User not found!**");
                    return;
                }
                StringBuilder sb = new StringBuilder();
                for (int i = 2; i < args.length; i++) {
                    if (i != 2) sb.append(" ");
                    sb.append(args[i]);
                }

                // find appropriate webhook based on channel
                WebhookUtils.retrieveWebhookUrl((TextChannel) channel).thenAcceptAsync((hookUrl) -> {
                    if (hookUrl == null) {
                        GTools.sendThenDelete(channel, "**Sorry, but I can't find a webhooks for this channel. Please create a new webhook for this channel and try again.**");
                        return;
                    }
                    WebhookUtils.sendMessageAs(sb.toString(), target, hookUrl);
                });

                break;
            }
            default: {
                GTools.sendThenDelete(channel, getCommandUsage());
                break;
            }
        }

    }

    private Message getCommandUsage() {
        return new MessageBuilder()
                .append("> **Please enter a valid command argument:**\n")
                .append("> `/Annoy emoji <@User> <Emoji>` - *Reacts to every message by the user with the given emoji*\n")
                .append("> `/Annoy educate <@User> <Hours>` - *Sends the user a random wise quote every so many hours*\n")
                .append("> `/Annoy scrabble <@User> <Character>` - *Replaces the start of each word from the user with the selected character!*\n")
                .append("> `/Annoy bot <@User>` - *Resends the player's messages as a webhook so everyone thinks they're a bot!*\n")
                .append("> `/Annoy stop <@User>` - *Stops annoying the selected user*\n")
                .append("> `/Annoy sudo <@User> <Message>` - *Sends the specified message as the selected user*\n")
                .build();
    }

    private boolean alreadyAnnoying(MessageChannel channel, Member member) {
        if (SelfData.get().getBotAnnoyList().contains(member.getIdLong())) {
            GTools.sendThenDelete(channel, alreadyAnnoyingMessage(member, "bot annoy"));
            return true;
        }
        if (SelfData.get().getScrabbleAnnoyMap().containsKey(member.getIdLong())) {
            GTools.sendThenDelete(channel, alreadyAnnoyingMessage(member, "scrabble annoy"));
            return true;
        }
        if (SelfData.get().getEmojiAnnoyMap().containsKey(member.getIdLong())) {
            GTools.sendThenDelete(channel, alreadyAnnoyingMessage(member, "emoji annoy"));
            return true;
        }
        if (SelfData.get().getQuoteAnnoyMap().containsKey(member.getIdLong())) {
            GTools.sendThenDelete(channel, alreadyAnnoyingMessage(member, "education"));
            return true;
        }
        return false;
    }

    private String alreadyAnnoyingMessage(Member member, String annoy) {
        return "**I am already harassing " + member.getEffectiveName() + " with " + annoy + ". Reset the annoy status with `/Annoy stop <@User` command first!**";
    }
}
