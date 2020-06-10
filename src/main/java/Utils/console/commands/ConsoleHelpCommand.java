package Utils.console.commands;

import Utils.console.Logs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConsoleHelpCommand extends ConsoleCommand{

    public ConsoleHelpCommand() {
        super("help", "Get a list of all commands and their description");
    }

    @Override
    public void onCommand(String[] args) {

        // List of all commands as string
        List<String> commands = new ArrayList<>();
        for (ConsoleCommand command : ConsoleCommand.getCommandsList()) {
            commands.add(command.getCommand());
        }

        // Sorts in alphabetical order
        Collections.sort(commands);

        StringBuilder help = new StringBuilder()
                .append("Console Commands Help:\n");

        for (String commandName : commands) {
            ConsoleCommand consoleCommand = ConsoleCommand.getByName(commandName);
                    help.append("\n            ")
                    .append(commandName)
                    .append(" - ")
                    .append(consoleCommand.getDescription());
        }

        Logs.log(help.toString());

    }

}
