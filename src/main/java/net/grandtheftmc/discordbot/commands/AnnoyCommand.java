package net.grandtheftmc.discordbot.commands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.grandtheftmc.discordbot.utils.Utils;
import net.grandtheftmc.discordbot.utils.WebhookUtils;
import net.grandtheftmc.discordbot.utils.selfdata.AnnoyData;
import net.grandtheftmc.discordbot.utils.users.GTMUser;
import net.grandtheftmc.discordbot.utils.users.Rank;

import java.util.List;

public class AnnoyCommand extends Command {

    public AnnoyCommand() {
        super("annoy", "Useful admin utility command to be used against people you don't like", Rank.ADMIN, Type.DISCORD_ONLY);
    }

    @Override
    public void buildCommandData(SlashCommandData slashCommandData) {
        SubcommandData emoji = new SubcommandData("emoji", "Reacts to every message by the user with the given emoji.");
        emoji.addOption(OptionType.USER, "target", "The user you want to annoy.", true);
        emoji.addOption(OptionType.STRING, "emoji", "The emoji you want to annoy the user with", true);

        SubcommandData scrabble = new SubcommandData("scrabble", "Replaces the start of each word from the user with the selected character!");
        scrabble.addOption(OptionType.USER, "target", "The user you want to annoy.", true);
        scrabble.addOption(OptionType.STRING, "character", "The character you want to scrabble their message with.", true);


        SubcommandData educate = new SubcommandData("educate", "Sends the user a random wise quote every so many hours.");
        educate.addOption(OptionType.USER, "target", "The user you want to annoy.", true);
        educate.addOption(OptionType.NUMBER, "hours", "The frequency of how often to 'educate' the target in hours.", true);

        SubcommandData stop = new SubcommandData("stop", "Stops annoying the selected user.");
        stop.addOption(OptionType.USER, "target", "The user you want to stop annoying.", true);

        SubcommandData bot = new SubcommandData("bot", "Resends the player's messages as a webhook so everyone thinks they're a bot!");
        bot.addOption(OptionType.USER, "target", "The user you want to annoy.", true);

        SubcommandData sudo = new SubcommandData("sudo", "Sends the specified message as the selected user.");
        sudo.addOption(OptionType.USER, "target", "The user you want to sudo.", true);
        sudo.addOption(OptionType.STRING, "msg", "The message which to sudo.", true);



        slashCommandData.addSubcommands(emoji, scrabble, educate, stop, bot, sudo);
    }

    @Override
    public void onCommandUse(SlashCommandInteraction interaction, MessageChannel channel, List<OptionMapping> arguments, Member member, GTMUser gtmUser, String[] path) {

        switch (path[0].toLowerCase()) {
            case "emoji": {
                if (alreadyAnnoying(channel, member))
                    return;

                Member target = interaction.getOption("target").getAsMember();
                if (target.getIdLong() == 218068237630439424L) {
                    interaction.reply("This person is immune to your diabolic schemes.").setEphemeral(true).queue();
                    return;
                }

                String emoji;
                String emojiTag;

                EmojiUnion emoteOptional;
                try {
                    emoteOptional = Emoji.fromFormatted(interaction.getOption("emoji").getAsString());
                } catch (IllegalArgumentException ex) {
                    interaction.reply("The string you entered is not a valid emoji!").setEphemeral(true).queue();
                    return;
                }

                try {
                    emoji = emoteOptional.asCustom().getId();
                    emojiTag = emoteOptional.asCustom().getAsMention();
                } catch (IllegalStateException ex) {
                    emoji = emoteOptional.getName();
                    emojiTag = emoteOptional.getAsReactionCode();
                }

                AnnoyData.get().getEmojiAnnoyMap().put(target.getIdLong(), emoji);
                AnnoyData.get().save();
                interaction.reply("**I will now give " + target.getEffectiveName() + " the care they deserve by reacting to all of their messages with a " + emojiTag + "!**").queue();

                break;
            }
            case "educate": {
                if (alreadyAnnoying(channel, member))
                    return;

                Member target = interaction.getOption("target").getAsMember();
                if (target.getIdLong() == 218068237630439424L) {
                    interaction.reply("This person is immune to your diabolic schemes.").setEphemeral(true).queue();
                    return;
                }

                double hours = interaction.getOption("hours").getAsDouble();
                // todo: add double support
                AnnoyData.get().getQuoteAnnoyMap().put(target.getIdLong(), new Long[]{(long) hours, 0L});
                AnnoyData.get().save();
                interaction.reply("**I will now begin sharing some of my great wisdom with " + target.getEffectiveName() + " every " + hours + " hour(s)!**").queue();

                break;
            }
            case "scrabble": {
                if (alreadyAnnoying(channel, member))
                    return;

                Member target = interaction.getOption("target").getAsMember();
                if (target.getIdLong() == 218068237630439424L) {
                    interaction.reply("This person is immune to your diabolic schemes.").setEphemeral(true).queue();
                    return;
                }

                String replacement = interaction.getOption("character").getAsString();
                if (replacement.length() > 1) {
                    interaction.reply("**You can only replace the start of the player's words with a single character!**").setEphemeral(true).queue();
                    return;
                }

                char character = replacement.charAt(0);

                AnnoyData.get().getScrabbleAnnoyMap().put(target.getIdLong(), character);
                AnnoyData.get().save();
                interaction.reply("**" + target.getEffectiveName() + " words will now be converted to match their IQ!**").queue();

                break;
            }
            case "bot": {
                if (alreadyAnnoying(channel, member))
                    return;

                Member target = interaction.getOption("target").getAsMember();
                if (target.getIdLong() == 218068237630439424L) {
                    interaction.reply("This person is immune to your diabolic schemes.").setEphemeral(true).queue();
                    return;
                }

                AnnoyData.get().getBotAnnoyList().add(target.getIdLong());
                AnnoyData.get().save();
                interaction.reply("**" + target.getEffectiveName() + " is now a bot! Beep bop!**").queue();

                break;
            }
            case "stop": {

                Member target = interaction.getOption("target").getAsMember();

                if (AnnoyData.get().getQuoteAnnoyMap().remove(target.getIdLong()) != null ||
                        AnnoyData.get().getEmojiAnnoyMap().remove(target.getIdLong()) != null ||
                        AnnoyData.get().getScrabbleAnnoyMap().remove(target.getIdLong()) != null ||
                        AnnoyData.get().getBotAnnoyList().remove(target.getIdLong())) {
                    interaction.reply("**Ok. I will now stop bothering " + target.getEffectiveName() + "... :(**").queue();
                    AnnoyData.get().save();
                }
                else
                    interaction.reply("**I am already not annoying " + target.getEffectiveName() + "!**").setEphemeral(true).queue();

                break;
            }
            case "sudo": {

                Member target = interaction.getOption("target").getAsMember();

                if (target.getIdLong() == 218068237630439424L) {
                    interaction.reply("This person is immune to your diabolic schemes.").setEphemeral(true).queue();
                    return;
                }

                String message = interaction.getOption("msg").getAsString();
                interaction.deferReply(true).setEphemeral(true).queue();

                // find appropriate webhook based on channel
                WebhookUtils.retrieveWebhookUrl((TextChannel) channel).thenAccept((hookUrl) -> {
                    if (hookUrl == null) {
                        interaction.getHook().editOriginal("**Sorry, but I can't find a webhooks for this channel. Please create a new webhook for this channel and try again.**").queue();
                        return;
                    }
                    WebhookUtils.sendMessageAs(message, target, hookUrl);
                    interaction.getHook().editOriginal("Message sent!").queue();
                });

                break;
            }
        }

    }

    private boolean alreadyAnnoying(MessageChannel channel, Member member) {
        if (AnnoyData.get().getBotAnnoyList().contains(member.getIdLong())) {
            Utils.sendThenDelete(channel, alreadyAnnoyingMessage(member, "bot annoy"));
            return true;
        }
        if (AnnoyData.get().getScrabbleAnnoyMap().containsKey(member.getIdLong())) {
            Utils.sendThenDelete(channel, alreadyAnnoyingMessage(member, "scrabble annoy"));
            return true;
        }
        if (AnnoyData.get().getEmojiAnnoyMap().containsKey(member.getIdLong())) {
            Utils.sendThenDelete(channel, alreadyAnnoyingMessage(member, "emoji annoy"));
            return true;
        }
        if (AnnoyData.get().getQuoteAnnoyMap().containsKey(member.getIdLong())) {
            Utils.sendThenDelete(channel, alreadyAnnoyingMessage(member, "education"));
            return true;
        }
        return false;
    }

    private String alreadyAnnoyingMessage(Member member, String annoy) {
        return "**I am already harassing " + member.getEffectiveName() + " with " + annoy + ". Reset the annoy status with `/Annoy stop <@User` command first!**";
    }
}
