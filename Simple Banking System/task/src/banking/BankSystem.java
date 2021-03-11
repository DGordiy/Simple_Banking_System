package banking;

import java.sql.*;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class BankSystem {

    private final Set<Card> cards;
    private final Random random;
    private final Connection connection;

    public BankSystem(Connection connection) {
        this.connection = connection;
        random = new Random();

        this.cards = new HashSet<>();

        //Reading data from db
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM card")) {
            while (resultSet.next()) {
                Card card = new Card();
                card.setId(resultSet.getInt("id"));
                card.setNumber(resultSet.getString("number"));
                card.setPin(resultSet.getString("pin"));
                card.setBalance(resultSet.getInt("balance"));
                cards.add(card);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Card createCard() throws SQLException {
        Card card = new Card();
        do {
            setCardNumber(card);
        } while (cards.stream().anyMatch(v -> v.equals(card)));

        setCardPin(card);

        //Saving data to db
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO card (number, pin) VALUES(?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, card.getNumber());
            statement.setString(2, card.getPin());
            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                generatedKeys.next();
                card.setId(generatedKeys.getInt(1));
            }
        }

        cards.add(card);
        return card;
    }

    private void setCardNumber(Card card) {
        int[] digits = new int[16];
        digits[0] = 4;

        for (int i = 6; i < 15; i++) {
            digits[i] = random.nextInt(10);
        }

        //Generate last digit using Lunh Algorithm
        int sum = 0;
        for (int i = 0; i < 15; i++) {
            int d = digits[i] * (i % 2 == 0 ? 2 : 1);
            sum += d > 9 ? d - 9 : d;
        }
        if (sum % 10 != 0){
            digits[15] = ((sum / 10) + 1) * 10 - sum;
        }

        StringBuilder sb = new StringBuilder();
        for (int v : digits) {
            sb.append((char) ('0' + v));
        }
        card.setNumber(sb.toString());
    }

    public static boolean checkCardNumber(String number) {
        char[] digits = number.toCharArray();
        int sum = 0;
        for (int i = 0; i < 15; i++) {
            int d = (digits[i] - '0') * (i % 2 == 0 ? 2 : 1);
            sum += d > 9 ? d - 9 : d;
        }
        return (sum + (digits[15] - '0')) % 10 == 0;
    }

    private void setCardPin(Card card) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            sb.append(random.nextInt(10));
        }
        card.setPin(sb.toString());
    }

    public Card getCardByNumberAndPin(String number, String pin) {
        Card card = getCardByNumber(number);
        if (card != null && card.getPin().equals(pin)) {
            return card;
        }

        return null;
    }

    public Card getCardByNumber(String number) {
        return cards.stream()
                .filter(v -> v.getNumber().equals(number))
                .findAny().orElse(null);
    }

    public int getBalance(Card card) {
        return card.getBalance();
    }

    public void addIncome(Card card, int amount) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("UPDATE card SET balance = ? WHERE id = ?")) {
            int newBalance = card.getBalance() + amount;

            statement.setInt(1, newBalance);
            statement.setInt(2, card.getId());
            statement.executeUpdate();

            card.setBalance(newBalance);
        }
    }

    public void transfer(Card fromCard, Card toCard, int amount) throws SQLException {
        if (connection.getAutoCommit()) {
            connection.setAutoCommit(false);
        }

        try {
            addIncome(fromCard, -amount);
            addIncome(toCard, amount);

            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
        }

        connection.setAutoCommit(true);
    }

    public void closeAccount(Card card) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM card WHERE id = ?")) {
            statement.setInt(1, card.getId());
            statement.executeUpdate();

            cards.remove(card);
        }
    }
}
