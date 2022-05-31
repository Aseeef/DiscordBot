package net.grandtheftmc.discordbot.commands.message;

import lombok.Getter;

public enum MessagePlaceholder {

    DISCORD_ID("discord_id"),
    DISCORD_NAME("discord_name"),
    IN_GAME_NAME("in_game_name"),
    USER_RANK("rank"),
    ;

    @Getter
    private final String placeHolder;

    MessagePlaceholder(String placeHolder) {
        this.placeHolder = placeHolder;
    }

}
