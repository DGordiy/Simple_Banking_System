package banking;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

public class MainMenu {

    private final Scanner scanner;
    private final BankSystem bankSystem;

    MainMenu(Connection connection) {
        scanner = new Scanner(System.in);
        bankSystem = new BankSystem(connection);
        show();
    }

    private void show() {
        System.out.println("1. Create an account");
        System.out.println("2. Log into account");
        System.out.println("0. Exit");

        switch (scanner.nextLine()) {
            case "1":
                createAccount();
                break;
            case "2":
                logIn();
                break;
            case "0":
                exit();
            default:
                System.out.println("Incorrect selection");
        }

        System.out.println();
        show();
    }

    private void createAccount() {
        System.out.println();
        try {
            Card card = bankSystem.createCard();

            System.out.println("Your card has been created");
            System.out.println("Your card number is:");
            System.out.println(card.getNumber());
            System.out.println("Your card PIN is:");
            System.out.println(card.getPin());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void logIn() {
        System.out.println();
        System.out.println("Enter your card number:");
        String number = scanner.nextLine();
        System.out.println("Enter your PIN:");
        String pin = scanner.nextLine();

        Card card = bankSystem.getCardByNumberAndPin(number, pin);
        System.out.println();
        if (card == null) {
            System.out.println("Wrong card number or PIN!");
        } else {
            System.out.println("You have successfully logged in!");
            showCardMenu(card);
        }
    }

    private void showCardMenu(Card card) {
        System.out.println();
        System.out.println("1. Balance");
        System.out.println("2. Add income");
        System.out.println("3. Do transfer");
        System.out.println("4. Close account");
        System.out.println("5. Log out");
        System.out.println("0. Exit");

        switch (scanner.nextLine()) {
            case "1":
                showBalance(card);
                break;
            case "2":
                addIncome(card);
                break;
            case "3":
                doTransfer(card);
                break;
            case "4":
                closeAccount(card);
                show();
                return;
            case "5":
                return;
            case "0":
                exit();
            default:
                System.out.println("Incorrect selection");
        }

        showCardMenu(card);
    }

    private void showBalance(Card card) {
        System.out.println();
        System.out.println("Balance: " + bankSystem.getBalance(card));
    }

    private void exit() {
        System.out.println();
        System.out.println("Bye!");
        System.exit(0);
    }

    private void addIncome(Card card) {
        System.out.println();
        System.out.println("Enter income:");
        int amount = Integer.parseInt(scanner.nextLine());
        try {
            bankSystem.addIncome(card, amount);
            System.out.println("Income was added!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void doTransfer(Card card) {
        System.out.println();
        System.out.println("Transfer");
        System.out.println("Enter card number");
        String number2 = scanner.nextLine();

        boolean isError = false;
        if (!BankSystem.checkCardNumber(number2)) {
            System.out.println("Probably you made a mistake in the card number. Please try again!");
            isError = true;
        }

        if (number2.equals(card.getNumber())) {
            System.out.println("You can't transfer money to the same account!");
            isError = true;
        }

        Card card2 = bankSystem.getCardByNumber(number2);
        if (card2 == null) {
            System.out.println("Such a card does not exist.");
            isError = true;
        }

        if (isError) {
            return;
        }

        System.out.println("Enter how much money you want to transfer:");
        int amount = Integer.parseInt(scanner.nextLine());

        if (card.getBalance() < amount) {
            System.out.println("Not enough money!");
            return;
        }

        try {
            bankSystem.transfer(card, card2, amount);
            System.out.println("Success!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void closeAccount(Card card) {
        try {
            bankSystem.closeAccount(card);
            System.out.println();
            System.out.println("The account has been closed!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
