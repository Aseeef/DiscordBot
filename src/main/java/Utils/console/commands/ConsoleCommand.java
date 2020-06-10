package Utils.console.commands;

import commands.Command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static Utils.tools.GTools.printStackError;

public abstract class ConsoleCommand implements Runnable {

    private String command;
    private String description;

    private static BufferedReader reader = null;

    private static List<ConsoleCommand> commandsList = new ArrayList<>();

    public ConsoleCommand(String command, String description) {
        this.command = command;
        this.description = description;
        commandsList.add(this);
    }

    public void run() {
        try {
            if (reader == null)
                reader = new BufferedReader(new InputStreamReader(System.in));

            String msg = null;
            String[] args = null;

            while (msg == null) {
                msg = reader.readLine();
                if (!msg.startsWith(this.command)) return;
                args = Command.getArgs(msg);
            }

            onCommand(args);

            run();

        } catch (IOException e) {
            printStackError(e);
        }
    }

    public abstract void onCommand(String[] args);

    public String getCommand() {
        return command;
    }

    public String getDescription() {
        return description;
    }

    public static List<ConsoleCommand> getCommandsList() {
        return commandsList;
    }

    public static ConsoleCommand getByName (String commandName) {
        return getCommandsList().stream().filter( (command) -> command.getCommand().equals(commandName)).findFirst().orElse(null);
    }

}
