package net.grandtheftmc.discordbot.utils.console.commands.listeners;

import net.grandtheftmc.discordbot.utils.console.commands.ConsoleCommandListener;

public class ConsoleStopCommand implements ConsoleCommandListener {

    @Override
    public String getCommand() {
        return "stop";
    }

    @Override
    public String getDescription() {
        return "Gracefully stop the bot";
    }

    @Override
    public void onCommand(String[] args) {
        System.exit(0);
    }

}
