package net.grandtheftmc.discordbot.utils.console.commands;

public interface ConsoleCommandListener {

    void onCommand(String[] args);

    /** This method should return the String console command */
    String getCommand();

    /** This method should return the String decription of the command that would print in the help command */
    String getDescription();

}
