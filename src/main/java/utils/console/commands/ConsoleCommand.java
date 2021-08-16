package utils.console.commands;

import commands.Command;
import utils.console.Logs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static utils.Utils.printStackError;

public class ConsoleCommand implements Runnable {

    private List<ConsoleCommandListener> listeners = new ArrayList<>();

    private static ConsoleCommand instance;

    public ConsoleCommand registerListener(ConsoleCommandListener listener) {
        listeners.add(listener);
        return this;
    }

    public void init() {
        new Thread(this).start();
        instance = this;
    }

    public static ConsoleCommand getInstance() {
        return instance;
    }

    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            String msg = null;
            String[] args = null;

            while (msg == null) {
                msg = reader.readLine();
                args = Command.getArgs(msg);
            }

            boolean commandMatched = false;

            for (ConsoleCommandListener listener : listeners) {
                if (msg.startsWith(listener.getCommand())) {
                    listener.onCommand(args);
                    commandMatched = true;
                    break;
                }
            }

            if (!commandMatched) Logs.log("Unknown command! Please type 'help' for a list of all commands.");

            run();

        } catch (IOException e) {
            printStackError(e);
        }
    }

    public List<ConsoleCommandListener> getListeners() {
        return this.listeners;
    }

}
