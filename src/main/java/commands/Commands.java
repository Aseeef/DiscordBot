package commands;

import Utils.Rank;

public enum Commands {

    HELP("help", Rank.NORANK, "View the help page"),
    PLAYERCOUNT("playercount", Rank.ADMIN, "Manage GTM player count display"),
    RAIDMODE("raidmode", Rank.ADMIN, "Manage raid mode settings"),
    SENIORS("seniors", Rank.ADMIN, "Manage senior settings"),
    SUGGESTION("suggestion", Rank.ADMIN, "Manage player suggestions"),
    WELCOME("welcome", Rank.ADMIN, "Manage welcome message & settings"),
    ;

    private String commandName;
    private Rank requiredRank;
    private String description;

    Commands(String commandName, Rank requiredRank, String description) {
        this.commandName = commandName;
        this.requiredRank = requiredRank;
        this.description = description;
    }

    public String desc() {
        return description;
    }

    public String command() {
        return commandName;
    }

    public Rank rank() {
        return requiredRank;
    }

}
