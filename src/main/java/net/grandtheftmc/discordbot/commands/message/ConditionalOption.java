package net.grandtheftmc.discordbot.commands.message;

import lombok.Getter;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.Arrays;

public enum ConditionalOption {

    RANK(OptionType.STRING, "rank", "Controls what donator rank a user must be to receive the message."),
    LEVEL(OptionType.INTEGER,"level", "Controls what game level a user must be to receive the message."),
    PLAYTIME(OptionType.INTEGER,"playtime", "Controls how much playtime the user must have to receive the message."),
    LAST_PLAYED(OptionType.STRING, "last-played", "Controls how long ago the user must have last played to receive the message."),
    MONEY(OptionType.INTEGER, "money", "Controls how much money (including from their bank) the user must have to receive the message."),
    ;

    @Getter
    private final OptionType optionType;
    @Getter
    private final String display;
    @Getter
    private final String description;

    ConditionalOption(OptionType optionType, String display, String description) {
        this.display = display;
        this.description = description;
        this.optionType = optionType;
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
