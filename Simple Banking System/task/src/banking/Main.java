package banking;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

//4000006343042197
public class Main {
    public static void main(String[] args) {
        String dbName = args[1];
        //String dbName = "test.db";
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbName)) {
            connection.setAutoCommit(true);

            //Create table
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("CREATE TABLE card (" +
                        "id INTEGER PRIMARY KEY," +
                        "number TEXT," +
                        "pin TEXT," +
                        "balance INTEGER DEFAULT 0)");
            } catch (SQLException e) {}

            new MainMenu(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}