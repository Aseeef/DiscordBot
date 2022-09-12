package net.grandtheftmc.discordbot.commands.suggestions;

import net.dv8tion.jda.api.entities.ThreadChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.grandtheftmc.discordbot.GTMBot;
import net.grandtheftmc.discordbot.utils.Data;
import net.grandtheftmc.discordbot.utils.Utils;
import net.grandtheftmc.discordbot.utils.console.Logs;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SuggestionListener extends ListenerAdapter {

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (event.getButton().getId() != null && event.getButton().getId().equals("suggest-btn")) {
            event.replyModal(SuggestionTools.getSuggestModal()).queue();
        }
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        if (event.getModalId().equals("suggest-create")) {

            String server = event.getValue("server").getAsString();
            String suggestionContent = event.getValue("suggestion").getAsString();
            String reason = event.getValue("reason").getAsString();

            // Create new suggestion object
            Suggestions suggestion = new Suggestions(Data.getNextNumber(Data.SUGGESTIONS),
                    server, suggestionContent, reason,
                    Objects.requireNonNull(event.getMember()).getId(),
                    "PENDING", "Awaiting response from staff.");

            event.replyEmbeds(SuggestionTools.createSuggestionEmbed(suggestion)).queue( (interactionHook) -> {
                // Set suggestion id
                interactionHook.retrieveOriginal().queue(msg -> {
                    suggestion.setId(msg.getIdLong());
                    // React to the message
                    Emoji gtmAgree = GTMBot.getJDA().getEmojisByName("gtmagree", true).get(0);
                    Emoji gtmDisagree = GTMBot.getJDA().getEmojisByName("gtmdisagree", true).get(0);
                    msg.addReaction(gtmAgree).queue();
                    msg.addReaction(gtmDisagree).queue();
                    msg.createThreadChannel("Suggestions #" + suggestion.getNumber()).setAutoArchiveDuration(ThreadChannel.AutoArchiveDuration.TIME_3_DAYS).queue( threadChannel ->
                            threadChannel.sendMessage("Got any feedback on this suggestion? How would you add to this suggestion to make it better? Write your thoughts about this suggestion here to help us decide whether to add this!").queue()
                    );
                });
            });

            // Notify user a success message & inform them that they will get alerts on status change
            event.getUser().openPrivateChannel().queue((userChannel) ->
            {
                // Send a private message to the user
                userChannel.sendMessage("**Hello! Thank you for your suggestion! " +
                        "I will notify you if there are any updates on the status of your suggestion. Have a great day!**").queue();
            });

            // Log suggestion
            Logs.log("User "+event.getUser().getAsTag()+
                    "("+event.getUser().getIdLong()+") just created a suggestion with ID #"+
                    suggestion.getNumber());

            // Send how to make a suggestion instructions for the next person
            SuggestionTools.suggestionInstruct(SuggestionTools.getSuggestionsChannel());

        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        if (!event.getAuthor().isBot() && SuggestionTools.getSuggestionsChannel().getIdLong() == event.getChannel().getIdLong()) {
            event.getMessage().delete().queue();
            Utils.sendThenDelete(event.getChannel(), "**Hey!** You can not send messages in this channel. If you want to create a suggestion, then use the **/suggest** command!");
        }

    }

}
