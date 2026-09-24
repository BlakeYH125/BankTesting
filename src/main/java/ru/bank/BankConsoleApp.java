package ru.bank;

import java.math.BigDecimal;
import java.util.Scanner;

public class BankConsoleApp {
    private final BankService bankService;
    private final Scanner scanner;

    public BankConsoleApp(BankService bankService, Scanner scanner) {
        this.bankService = bankService;
        this.scanner = scanner;
    }

    public static void main(String[] args) {
        AccountRepository repository = new AccountRepository();
        BankService bankService = new BankService(repository);
        BankConsoleApp application = new BankConsoleApp(bankService, new Scanner(System.in));
        application.run();
    }

    public void run() {
        boolean running = true;
        while (running) {
            printMenu();
            String command = scanner.nextLine();
            try {
                switch (command) {
                    case "1" -> createAccount();
                    case "2" -> deposit();
                    case "3" -> withdraw();
                    case "4" -> transfer();
                    case "5" -> showAccount();
                    case "6" -> blockAccount();
                    case "7" -> unblockAccount();
                    case "0" -> running = false;
                    default -> System.out.println("Неизвестная команда");
                }
            } catch (IllegalArgumentException | IllegalStateException exception) {
                System.out.println("Ошибка: " + exception.getMessage());
            }
        }
        System.out.println("Работа программы завершена.");
    }

    private void printMenu() {
        System.out.println();
        System.out.println("1 — открыть счет");
        System.out.println("2 — пополнить счет");
        System.out.println("3 — снять деньги");
        System.out.println("4 — перевести деньги");
        System.out.println("5 — показать информацию о счете");
        System.out.println("6 — заблокировать счет");
        System.out.println("7 — разблокировать счет");
        System.out.println("0 — выход");
        System.out.println("Выберите команду:");
    }

    private void createAccount() {
        System.out.println("Введите ФИО владельца:");
        String fullName = scanner.nextLine();
        System.out.println("Введите начальный баланс:");
        BigDecimal balance = readMoney();
        long id = bankService.openAccount(fullName, balance);
        System.out.println("Счет открыт. Его номер: " + id);
    }

    private void deposit() {
        long id = readAccountId("Введите номер счета:");
        System.out.println("Введите сумму пополнения:");
        bankService.deposit(id, readMoney());
        System.out.println("Новый баланс: " + bankService.getBalance(id));
    }

    private void withdraw() {
        long id = readAccountId("Введите номер счета:");
        System.out.println("Введите сумму снятия:");
        bankService.withdraw(id, readMoney());
        System.out.println("Новый баланс: " + bankService.getBalance(id));
    }

    private void transfer() {
        long fromId = readAccountId("Введите номер счета отправителя:");
        long toId = readAccountId("Введите номер счета получателя:");
        System.out.println("Введите сумму перевода:");
        bankService.transfer(fromId, toId, readMoney());
        System.out.println("Перевод выполнен.");
    }

    private void showAccount() {
        long id = readAccountId("Введите номер счета:");
        System.out.println(bankService.getAccountInfo(id));
    }

    private void blockAccount() {
        long id = readAccountId("Введите номер счета:");
        bankService.blockAccount(id);
        System.out.println("Счет заблокирован.");
    }

    private void unblockAccount() {
        long id = readAccountId("Введите номер счета:");
        bankService.unblockAccount(id);
        System.out.println("Счет разблокирован.");
    }

    private long readAccountId(String message) {
        System.out.println(message);
        return Long.parseLong(scanner.nextLine());
    }

    private BigDecimal readMoney() {
        return new BigDecimal(scanner.nextLine().replace(',', '.'));
    }
}
