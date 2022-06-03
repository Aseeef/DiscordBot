package net.grandtheftmc.discordbot.commands.message;

import lombok.Getter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.grandtheftmc.discordbot.utils.users.Rank;

import java.io.Serializable;
import java.time.Instant;
import java.util.Arrays;

public enum ConditionalOption {

    RANK(Rank.class, "rank", "What donator rank should receive this message?"),
    LEVEL(Integer.class,"level", "What game level a user must be to receive the message?"),
    PLAYTIME(Integer.class,"playtime", "How much playtime (in hours) the user must have to receive the message."),
    LAST_PLAYED(Instant.class, "last-played", "How long ago must the user have last played to receive the message. (format: yyyy-mm-dd)"),
    MONEY(Long.class, "money", "Controls how much money (including from their bank) the user must have to receive the message."),
    ;

    @Getter
    private final Class<? extends Serializable> optionType;
    @Getter
    private final String display;
    @Getter
    private final String description;

    ConditionalOption(Class<? extends Serializable> dataType, String display, String description) {
        this.display = display;
        this.description = description;
        this.optionType = dataType;
    }

    public static String[] displayValues() {
        return Arrays.stream(ConditionalOption.values()).map(mc -> mc.display).toArray(String[]::new);
    }

    public static ConditionalOption fromDisplay(String display) {
        for (ConditionalOption mc : ConditionalOption.values()) {
            if (mc.display.equalsIgnoreCase("display"))
                return mc;
        }
        return null;
    }

}
