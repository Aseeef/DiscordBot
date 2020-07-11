package utils.pagination;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageEmbedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveAllEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.events.message.priv.react.PrivateMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.priv.react.PrivateMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import utils.console.Logs;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static utils.tools.GTools.jda;

public class DiscordMenu extends ListenerAdapter {

    private static ArrayList<MessageReaction.ReactionEmote> reactions = new ArrayList<MessageReaction.ReactionEmote>() {
        {
            add(MessageReaction.ReactionEmote.fromUnicode("◀️", jda));
            add(MessageReaction.ReactionEmote.fromUnicode("❎", jda));
            add(MessageReaction.ReactionEmote.fromUnicode("▶️", jda));
        }
    };

    private Message message;
    private User user;
    private MessageChannel channel;
    private MenuAction menuAction;
    private boolean allowChangeFromAll;
    private int page = 1;
    private int maxPages;

    private DiscordMenu (Message message, int maxPages, User user, boolean allowChangeFromAll) {
        jda.addEventListener(this);
        this.message = message;
        this.channel = message.getChannel();
        this.maxPages = maxPages;
        this.user = user;
        this.allowChangeFromAll = allowChangeFromAll;

        addReactions();
    }

    private DiscordMenu (Message message, int maxPages) {
        jda.addEventListener(this);
        this.message = message;
        this.channel = message.getChannel();
        this.maxPages = maxPages;

        this.user = null;
        this.allowChangeFromAll = true;

        addReactions();
    }

    public static CompletableFuture<DiscordMenu> create(MessageChannel channel, EmbedBuilder embedBuilder, int maxPages) {
        return create(channel, embedBuilder, maxPages, null, false);
    }

    public static CompletableFuture<DiscordMenu> create(MessageChannel channel, EmbedBuilder embedBuilder, int maxPages, @Nullable User user, boolean allowChangeFromAll) {
        CompletableFuture<DiscordMenu> futureMenu = new CompletableFuture<>();
        MessageEmbed embed = buildEmbed(embedBuilder, 1, maxPages);
        channel.sendMessage(embed).submit()
                .thenAcceptAsync( (msg) -> {
                    DiscordMenu menu = new DiscordMenu(msg, maxPages, user, allowChangeFromAll);
                    futureMenu.complete(menu);
                });
        return futureMenu;
    }

    public void setPageContents (EmbedBuilder embedBuilder) {
        MessageEmbed embed = buildEmbed(embedBuilder, this.page, this.maxPages);
        if (this.message != null && this.message.getChannel() instanceof TextChannel) {
            this.message.editMessage(embed).queue();
        } else if (this.message != null && this.message.getChannel() instanceof PrivateChannel) {
            this.message.delete()
                    .flatMap(v -> this.channel.sendMessage(embed))
                    .queue((msg) -> {
                        this.message = msg;
                        addReactions();
                    });
        }
    }

    private static MessageEmbed buildEmbed(EmbedBuilder embedBuilder, int page, int maxPages) {
        if (embedBuilder.build().getFooter() == null)
            embedBuilder.setFooter("[Page #" + page + " out of " + maxPages + "]");
        return embedBuilder.build();
    }

    public void onMenuAction(MenuAction menuAction) {
        this.menuAction = menuAction;
    }

    public int getPage() {
        return this.page;
    }

    public int getMaxPages() {
        return this.maxPages;
    }

    private void addReactions() {
        List<MessageReaction.ReactionEmote> messageEmotes = new ArrayList<>();
        this.message.getReactions().forEach( (reaction) -> messageEmotes.add(reaction.getReactionEmote()));

        reactions.forEach((button) -> {
            if (messageEmotes.contains(button)) return;

            if (button.isEmoji() && this.message != null)
                this.message.addReaction(button.getEmoji()).queue();
            else if (button.isEmote() && this.message != null)
                this.message.addReaction(button.getEmote()).queue();
        });

    }

    public final void onGuildMessageEmbed (GuildMessageEmbedEvent event) {
        if (event.getMessageEmbeds().size() == 0) {
            this.message.delete().queue();
            this.message = null;
            this.menuAction.onAction(MenuAction.Type.DELETE, null);
        }
    }

    public final void onGuildMessageReactionAdd (GuildMessageReactionAddEvent event) {
        onReactionAdd(event.getMessageIdLong(), event.getUser(), event.getReactionEmote());
    }

    private void onReactionAdd(long messageIdLong, User user2, MessageReaction.ReactionEmote reactionEmote) {
        if (this.message != null && this.message.getIdLong() == messageIdLong && !(user2 == jda.getSelfUser())) {

            boolean emoji = reactionEmote.isEmoji();

            if (emoji && reactionEmote.getEmoji().equals(reactions.get(0).getEmoji()) && this.page - 1 >= 1) {

                if (this.user != user2 && !this.allowChangeFromAll) return; // if this discord menu doesn't accept input from all users, return

                this.page--;
                this.menuAction.onAction(MenuAction.Type.PREVIOUS_PAGE, user2);
            }

            else if (emoji && reactionEmote.getEmoji().equals(reactions.get(1).getEmoji())) {

                if (this.user != user2 && !this.allowChangeFromAll) return;

                this.message.delete().queue();
                this.message = null;
                this.menuAction.onAction(MenuAction.Type.DELETE, user2);
                jda.getEventManager().unregister(this);
            }

            else if (emoji && reactionEmote.getEmoji().equals(reactions.get(2).getEmoji()) && this.page + 1 <= this.maxPages) {

                if (this.user != user2 && !this.allowChangeFromAll) return; // if this discord menu doesn't accept input from all users, return

                this.page++;
                this.menuAction.onAction(MenuAction.Type.NEXT_PAGE, user2);
            }

            if (this.channel == null || this.channel instanceof PrivateChannel || (emoji && reactionEmote.getEmoji().equals(reactions.get(1).getEmoji())))
                return;
            else if (emoji)
                this.message.removeReaction(reactionEmote.getEmoji(), user2).queue();
            else this.message.removeReaction(reactionEmote.getEmote(), user2).queue();

        }
    }

    public final void onGuildMessageReactionRemove (GuildMessageReactionRemoveEvent event) {
        if (this.message != null && this.message.getIdLong() == event.getMessageIdLong())
            addReactions();
    }

    public final void onGuildMessageReactionRemoveAll (GuildMessageReactionRemoveAllEvent event) {
        if (this.message != null && this.message.getIdLong() == event.getMessageIdLong())
            addReactions();
    }

    public final void onPrivateMessageReactionAdd (PrivateMessageReactionAddEvent event) {
        onReactionAdd(event.getMessageIdLong(), event.getUser(), event.getReactionEmote());
    }

    public final void onPrivateMessageReactionRemove (PrivateMessageReactionRemoveEvent event) {
        if (this.message != null && this.message.getIdLong() == event.getMessageIdLong())
            addReactions();
    }

}
