package ru.ifmo.rain.ageev.bank.classes;

import ru.ifmo.rain.ageev.bank.interfaces.Account;

public abstract class AbstractAccount implements Account {
    private String accountId;
    private int accountAmount;

    public AbstractAccount(final String accountId) {
        this(accountId, 0);
    }

    public AbstractAccount(final String accountId, final int accountAmount) {
        super();
        this.accountId = accountId;
        this.accountAmount = accountAmount;
    }

    @Override
    public synchronized String getId() {
        return accountId;
    }

    @Override
    public synchronized int getAmount() {
        return accountAmount;
    }

    @Override
    public synchronized void increaseAmount(int accountAmount) {
        this.accountAmount += accountAmount;
    }
}