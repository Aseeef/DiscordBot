package commands.stats;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import utils.pagination.DiscordMenu;

import java.util.concurrent.CompletableFuture;

public class StatsMenu extends DiscordMenu {

    private StatsMenu(Message msg, int maxPages, User user) {
        super(msg, maxPages, user, true);
    }

    public static CompletableFuture<Message> create(User user) {
        return null;
    }


}
