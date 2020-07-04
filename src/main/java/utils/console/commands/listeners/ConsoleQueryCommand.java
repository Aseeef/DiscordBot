package utils.console.commands.listeners;

import utils.console.Logs;
import utils.console.commands.ConsoleCommandListener;
import utils.database.sql.BaseDatabase;
import utils.tools.GTools;

import java.sql.*;
import java.util.Arrays;

public class ConsoleQueryCommand implements ConsoleCommandListener {

    @Override
    public String getCommand() {
        return "query";
    }

    @Override
    public String getDescription() {
        return "Manually query data from a GTM database";
    }

    @Override
    public void onCommand(String[] args) {

        if (args.length < 2) {
            Logs.log("Usage: /query [Database] [Query]");
            return;
        }

        BaseDatabase.Database database = null;
        for (BaseDatabase.Database d : BaseDatabase.Database.values()) {
            if (args[1].equalsIgnoreCase(d.name())) {
                database = d;
                break;
            }
        }

        if (database == null) {
            Logs.log("Invalid database! Valid databases are: " + Arrays.toString(BaseDatabase.Database.values()));
            return;
        }

        StringBuilder string = new StringBuilder();
        for (int i = 2 ; i < args.length ; i++) {
            string.append(" ").append(args[i]);
        }

        runCustomQuery(string.toString(), database);

    }

    public void runCustomQuery(String query, BaseDatabase.Database database) {

        try (Connection conn = BaseDatabase.getInstance(database).getConnection()) {

            try (PreparedStatement statement = conn.prepareStatement(query)) {
                try (ResultSet result = statement.executeQuery()) {
                    ResultSetMetaData rsmd = result.getMetaData();
                    int columnsNumber = rsmd.getColumnCount();

                    boolean first = true;

                    while (result.next()) {
                        StringBuilder sb = new StringBuilder();
                        StringBuilder sb2 = new StringBuilder();

                        for (int i = 1; i <= columnsNumber; i++) {
                            if (first) {
                                sb.append("[").append(rsmd.getColumnName(i)).append("]  ");
                            }
                            sb2.append("[").append(result.getString(i)).append("]  ");
                        }

                        if (first) {
                            Logs.log(sb.toString());
                            Logs.log("");
                        }
                        Logs.log(sb2.toString());
                        Logs.log("");

                        first = false;
                    }

                }
            }

        } catch (SQLException e) {
            GTools.printStackError(e);
        }

    }

}
