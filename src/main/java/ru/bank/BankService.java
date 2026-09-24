package ru.bank;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * Сервис банковских операций.
 * Публичные методы специально сделаны небольшими, чтобы их было удобно тестировать.
 */
public class BankService {
    private static final BigDecimal TRANSFER_FEE_RATE = new BigDecimal("0.01");
    private static final BigDecimal BONUS_RATE = new BigDecimal("0.02");

    private final AccountRepository repository;
    private long nextAccountId = 1;

    public BankService(AccountRepository repository) {
        this.repository = repository;
    }

    // 1. Открыть счет с нулевым балансом
    public long openAccount(String fullName) {
        return openAccount(fullName, BigDecimal.ZERO);
    }

    // 2. Открыть счет с начальным балансом
    public long openAccount(String fullName, BigDecimal initialBalance) {
        validateFullName(fullName);
        validateNonNegative(initialBalance, "Начальный баланс");
        long id = nextAccountId++;
        repository.save(id, fullName.trim(), money(initialBalance));
        return id;
    }

    // 3. Закрыть пустой счет
    public void closeAccount(long accountId) {
        requireZeroBalance(accountId);
        repository.updateActive(accountId, false);
    }

    // 4. Добавить деньги на счет
    public void deposit(long accountId, BigDecimal amount) {
        validatePositive(amount);
        requireAvailableAccount(accountId);
        BigDecimal newBalance = getBalance(accountId).add(money(amount));
        repository.updateBalance(accountId, newBalance);
    }

    // 5. Снять деньги со счета
    public void withdraw(long accountId, BigDecimal amount) {
        validatePositive(amount);
        requireAvailableAccount(accountId);
        if (!hasEnoughMoney(accountId, amount)) {
            throw new IllegalStateException("Недостаточно денег на счете");
        }
        BigDecimal newBalance = getBalance(accountId).subtract(money(amount));
        repository.updateBalance(accountId, newBalance);
    }

    // 6. Перевести деньги между счетами
    public void transfer(long fromAccountId, long toAccountId, BigDecimal amount) {
        if (fromAccountId == toAccountId) {
            throw new IllegalArgumentException("Нельзя переводить деньги на тот же счет");
        }
        validatePositive(amount);
        requireAvailableAccount(fromAccountId);
        requireAvailableAccount(toAccountId);
        if (!hasEnoughMoney(fromAccountId, amount)) {
            throw new IllegalStateException("Недостаточно денег для перевода");
        }
        withdraw(fromAccountId, amount);
        deposit(toAccountId, amount);
    }

    // 7. Получить баланс счета
    public BigDecimal getBalance(long accountId) {
        return (BigDecimal) findAccount(accountId).get("balance");
    }

    // 8. Получить ФИО владельца
    public String getFullName(long accountId) {
        return (String) findAccount(accountId).get("fullName");
    }

    // 9. Изменить ФИО владельца
    public void changeFullName(long accountId, String newFullName) {
        validateFullName(newFullName);
        findAccount(accountId);
        repository.updateFullName(accountId, newFullName.trim());
    }

    // 10. Заблокировать счет
    public void blockAccount(long accountId) {
        findAccount(accountId);
        repository.updateBlocked(accountId, true);
    }

    // 11. Разблокировать счет
    public void unblockAccount(long accountId) {
        findAccount(accountId);
        repository.updateBlocked(accountId, false);
    }

    // 12. Проверить блокировку счета
    public boolean isBlocked(long accountId) {
        return (boolean) findAccount(accountId).get("blocked");
    }

    // 13. Проверить активность счета
    public boolean isActive(long accountId) {
        return (boolean) findAccount(accountId).get("active");
    }

    // 14. Проверить существование счета
    public boolean accountExists(long accountId) {
        return repository.existsById(accountId);
    }

    // 15. Получить количество всех счетов
    public long getAccountCount() {
        return repository.count();
    }

    // 16. Получить количество активных счетов
    public long getActiveAccountCount() {
        long count = 0;
        List<HashMap<String, Object>> accounts = repository.findAll();
        for (HashMap<String, Object> account : accounts) {
            if ((boolean) account.get("active")) {
                count++;
            }
        }
        return count;
    }

    // 17. Получить общий баланс банка
    public BigDecimal getTotalBankBalance() {
        BigDecimal totalBalance = BigDecimal.ZERO.setScale(2);
        List<HashMap<String, Object>> accounts = repository.findAll();
        for (HashMap<String, Object> account : accounts) {
            BigDecimal balance = (BigDecimal) account.get("balance");
            totalBalance = totalBalance.add(balance);
        }
        return totalBalance;
    }

    // 18. Проверить достаточность денег
    public boolean hasEnoughMoney(long accountId, BigDecimal amount) {
        validateNonNegative(amount, "Сумма");
        return getBalance(accountId).compareTo(money(amount)) >= 0;
    }

    // 19. Найти счета по ФИО
    public List<Long> getAccountsByFullName(String fullName) {
        validateFullName(fullName);
        List<Long> accountIds = new ArrayList<>();
        List<HashMap<String, Object>> accounts = repository.findAll();
        for (HashMap<String, Object> account : accounts) {
            String accountFullName = (String) account.get("fullName");
            if (accountFullName.equalsIgnoreCase(fullName.trim())) {
                accountIds.add((long) account.get("id"));
            }
        }
        return accountIds;
    }

    // 20. Найти счет с наибольшим балансом
    public Optional<Long> getRichestAccountId() {
        List<HashMap<String, Object>> accounts = repository.findAll();
        if (accounts.isEmpty()) {
            return Optional.empty();
        }
        HashMap<String, Object> richestAccount = accounts.get(0);
        for (HashMap<String, Object> account : accounts) {
            BigDecimal balance = (BigDecimal) account.get("balance");
            BigDecimal richestBalance = (BigDecimal) richestAccount.get("balance");
            if (balance.compareTo(richestBalance) > 0) {
                richestAccount = account;
            }
        }
        return Optional.of((long) richestAccount.get("id"));
    }

    // 21. Рассчитать комиссию перевода
    public BigDecimal calculateTransferFee(BigDecimal amount) {
        validatePositive(amount);
        return money(amount.multiply(TRANSFER_FEE_RATE));
    }

    // 22. Пополнить счет с бонусом
    public BigDecimal depositWithBonus(long accountId, BigDecimal amount) {
        validatePositive(amount);
        BigDecimal bonus = money(amount.multiply(BONUS_RATE));
        deposit(accountId, amount.add(bonus));
        return bonus;
    }

    // 23. Снять все деньги со счета
    public BigDecimal withdrawAll(long accountId) {
        requireAvailableAccount(accountId);
        BigDecimal amount = getBalance(accountId);
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            withdraw(accountId, amount);
        }
        return amount;
    }

    // 24. Получить информацию о счете
    public String getAccountInfo(long accountId) {
        HashMap<String, Object> account = findAccount(accountId);
        return "Счет №" + account.get("id") + ", ФИО: " + account.get("fullName") + ", баланс: " + account.get("balance") + ", активен: " + account.get("active") + ", заблокирован: " + account.get("blocked");
    }

    private HashMap<String, Object> findAccount(long accountId) {
        Optional<HashMap<String, Object>> account = repository.findById(accountId);
        if (account.isEmpty()) {
            throw new IllegalArgumentException("Счет " + accountId + " не найден");
        }
        return account.get();
    }

    private void requireAvailableAccount(long accountId) {
        if (!isActive(accountId)) {
            throw new IllegalStateException("Счет закрыт");
        }
        if (isBlocked(accountId)) {
            throw new IllegalStateException("Счет заблокирован");
        }
    }

    private void requireZeroBalance(long accountId) {
        if (getBalance(accountId).compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalStateException("Нельзя закрыть счет с ненулевым балансом");
        }
    }

    private void validateFullName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("ФИО владельца не должно быть пустым");
        }
    }

    private void validatePositive(BigDecimal amount) {
        validateNonNegative(amount, "Сумма");
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Сумма должна быть больше нуля");
        }
    }

    private void validateNonNegative(BigDecimal amount, String fieldName) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(fieldName + " не может быть отрицательным");
        }
    }

    private BigDecimal money(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }
}
