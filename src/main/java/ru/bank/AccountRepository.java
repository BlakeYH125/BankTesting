package ru.bank;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Простая имитация таблицы базы данных.
 * Список — это таблица, а каждая HashMap — одна строка таблицы.
 */
public class AccountRepository {
    private final List<HashMap<String, Object>> accounts = new ArrayList<>();

    public void save(long id, String fullName, BigDecimal balance) {
        HashMap<String, Object> row = new HashMap<>();
        row.put("id", id);
        row.put("fullName", fullName);
        row.put("balance", balance);
        row.put("active", true);
        row.put("blocked", false);
        accounts.add(row);
    }

    public Optional<HashMap<String, Object>> findById(long id) {
        for (HashMap<String, Object> row : accounts) {
            if ((long) row.get("id") == id) {
                return Optional.of(row);
            }
        }
        return Optional.empty();
    }

    public List<HashMap<String, Object>> findAll() {
        return new ArrayList<>(accounts);
    }

    public boolean existsById(long id) {
        return findById(id).isPresent();
    }

    public void updateBalance(long id, BigDecimal newBalance) {
        findRequired(id).put("balance", newBalance);
    }

    public void updateFullName(long id, String newFullName) {
        findRequired(id).put("fullName", newFullName);
    }

    public void updateActive(long id, boolean active) {
        findRequired(id).put("active", active);
    }

    public void updateBlocked(long id, boolean blocked) {
        findRequired(id).put("blocked", blocked);
    }

    public long count() {
        return accounts.size();
    }

    private Map<String, Object> findRequired(long id) {
        Optional<HashMap<String, Object>> account = findById(id);
        if (account.isEmpty()) {
            throw new IllegalArgumentException("Счет " + id + " не найден");
        }
        return account.get();
    }
}
