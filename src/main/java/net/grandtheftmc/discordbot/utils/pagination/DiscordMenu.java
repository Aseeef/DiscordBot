package net.grandtheftmc.discordbot.utils.pagination;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageEmbedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveAllEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.grandtheftmc.discordbot.GTMBot;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class DiscordMenu extends ListenerAdapter {

    private static final String BACK = "◀️";
    private static final String FORWARD = "▶️";
    private static final String CLOSE = "❎";

    private Message message;
    private final User user;
    private final MessageChannel channel;
    private MenuAction menuAction;
    private final boolean allowChangeFromAll;
    private int page = 1;
    private int maxPages;

    /**
     * Initialize a new discord menu
     *
     * @param message -
     * @param maxPages
     * @param user
     * @param allowChangeFromAll
     */
    protected DiscordMenu (Message message, int maxPages, User user, boolean allowChangeFromAll) {
        GTMBot.getJDA().addEventListener(this);
        this.message = message;
        this.channel = message.getChannel();
        this.maxPages = maxPages;
        this.user = user;
        this.allowChangeFromAll = allowChangeFromAll;

        addReactions();
    }

    protected DiscordMenu (Message message, int maxPages) {
        GTMBot.getJDA().addEventListener(this);
        this.message = message;
        this.channel = message.getChannel();
        this.maxPages = maxPages;

        this.user = null;
        this.allowChangeFromAll = true;

        addReactions();
    }

    public static CompletableFuture<DiscordMenu> create(MessageChannel channel, EmbedBuilder embedBuilder, int maxPages) {
        return create(channel, embedBuilder, maxPages, null, true);
    }

    /**
     * @return - Create a discord menu. May take a while depending on discord. Returns a future completion
     */
    public static CompletableFuture<DiscordMenu> create(MessageChannel channel, EmbedBuilder embedBuilder, int maxPages, @Nullable User user, boolean allowChangeFromAll) {
        CompletableFuture<DiscordMenu> futureMenu = new CompletableFuture<>();
        MessageEmbed embed = buildEmbed(embedBuilder, 1, maxPages);
        channel.sendMessageEmbeds(embed).submit()
                .thenAcceptAsync( (msg) -> {
                    DiscordMenu menu = new DiscordMenu(msg, maxPages, user, allowChangeFromAll);
                    futureMenu.complete(menu);
                });
        return futureMenu;
    }

    public void setPageContents (EmbedBuilder embedBuilder) {
        MessageEmbed embed = buildEmbed(embedBuilder, this.page, this.maxPages);
        if (this.message != null && this.message.getChannel() instanceof TextChannel) {
            this.message.editMessageEmbeds(embed).queue();
        } else if (this.message != null && this.message.getChannel() instanceof PrivateChannel) {
            this.message.delete()
                    .flatMap(v -> this.channel.sendMessageEmbeds(embed))
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
        this.message.addReaction(BACK)
                .flatMap(v -> this.message.addReaction(CLOSE))
                .flatMap(v -> this.message.addReaction(FORWARD))
                .queue();
    }

    public final void onMessageEmbed (MessageEmbedEvent event) {
        if (event.getMessageEmbeds().size() == 0) {
            this.message.delete().queue();
            this.message = null;
            this.menuAction.onAction(MenuAction.Type.DELETE, null);
        }
    }

    public final void onMessageReactionAdd (MessageReactionAddEvent event) {
        onReactionAdd(event.getMessageIdLong(), event.getUser(), event.getReactionEmote());
    }

    private void onReactionAdd(long messageIdLong, User reactingUser, MessageReaction.ReactionEmote reactionEmote) {

        if (this.message != null && this.message.getIdLong() == messageIdLong && !(reactingUser == GTMBot.getJDA().getSelfUser())) {

            // remove the reaction
            if (this.channel == null || this.channel instanceof PrivateChannel || (reactionEmote.isEmoji() && reactionEmote.getEmoji().equals(CLOSE))) {}
            else if (reactionEmote.isEmoji())
                this.message.removeReaction(reactionEmote.getEmoji(), reactingUser).queue();
            else this.message.removeReaction(reactionEmote.getEmote(), reactingUser).queue();

            // fire appropriate event

            if (reactionEmote.isEmoji() && reactionEmote.getEmoji().equals(BACK) && this.page - 1 >= 1) {

                if (this.user != reactingUser && !this.allowChangeFromAll) return; // if this discord menu doesn't accept input from all users, return

                this.page--;
                this.menuAction.onAction(MenuAction.Type.PREVIOUS_PAGE, reactingUser);
            }

            else if (reactionEmote.isEmoji() && reactionEmote.getEmoji().equals(CLOSE)) {

                if (this.user != reactingUser && !this.allowChangeFromAll) return;

                this.message.delete().queue();
                this.message = null;
                this.menuAction.onAction(MenuAction.Type.DELETE, reactingUser);
                GTMBot.getJDA().getEventManager().unregister(this);
            }

            else if (reactionEmote.isEmoji() && reactionEmote.getEmoji().equals(FORWARD) && this.page + 1 <= this.maxPages) {

                if (this.user != reactingUser && !this.allowChangeFromAll) return; // if this discord menu doesn't accept input from all users, return

                this.page++;
                this.menuAction.onAction(MenuAction.Type.NEXT_PAGE, reactingUser);
            }

        }
    }

    public final void onMessageReactionRemove (@NotNull MessageReactionRemoveEvent event) {
        if (this.message != null && this.message.getIdLong() == event.getMessageIdLong())
            addReactions();
    }

    public final void onMessageReactionRemoveAll (@NotNull MessageReactionRemoveAllEvent event) {
        if (this.message != null && this.message.getIdLong() == event.getMessageIdLong())
            addReactions();
    }

    public void setMaxPages(int maxPages) {
        this.maxPages = maxPages;
    }
}
