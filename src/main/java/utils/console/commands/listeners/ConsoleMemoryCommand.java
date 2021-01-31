package utils.console.commands.listeners;

import utils.console.Logs;
import utils.console.commands.ConsoleCommandListener;

public class ConsoleMemoryCommand implements ConsoleCommandListener {

    @Override
    public void onCommand(String[] args) {
        float absTotal = Math.round((Runtime.getRuntime().maxMemory() / (1024.0 * 1024.0)) * 10f) / 10f;
        float total = Math.round((Runtime.getRuntime().totalMemory() / (1024.0 * 1024.0)) * 10f) / 10f;
        float available = Math.round((Runtime.getRuntime().freeMemory() / (1024.0 * 1024.0)) * 10f) / 10f;
        float using = total - available;
        Logs.log("Current system memory usage: ");
        Logs.log("     Maximum Memory: " + absTotal + " M");
        Logs.log("     Total Memory: " + total + " M");
        Logs.log("     Available Memory: " + available + " M");
        Logs.log("     Using: " + (using) + " M");
    }

    @Override
    public String getCommand() {
        return "memory";
    }

    @Override
    public String getDescription() {
        return "View the memory usage for the bot.";
    }

}
