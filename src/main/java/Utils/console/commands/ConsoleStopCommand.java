package Utils.console.commands;

public class ConsoleStopCommand extends ConsoleCommand{

    public ConsoleStopCommand() {
        super("stop", "Gracefully stop the bot");
    }

    @Override
    public void onCommand(String[] args) {
        System.exit(0);
    }

}
