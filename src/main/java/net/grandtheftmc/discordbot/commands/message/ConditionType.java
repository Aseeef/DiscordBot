package net.grandtheftmc.discordbot.commands.message;

import lombok.Getter;

public enum ConditionType {

    EQUAL_TO("="),
    GREATER_THAN(">"),
    GREATER_THAN_OR_EQUAL_TO(">="),
    LESS_THAN("<"),
    LESS_THAN_OR_EQUAL_TO("<="),
    ;

    @Getter
    private final String conditionString;
    ConditionType(String conditionString) {
        this.conditionString = conditionString;
    }

}
