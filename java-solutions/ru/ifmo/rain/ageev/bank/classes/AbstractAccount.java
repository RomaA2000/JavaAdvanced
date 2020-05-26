package ru.ifmo.rain.ageev.bank.classes;

import ru.ifmo.rain.ageev.bank.interfaces.Account;

import java.io.Serializable;

public abstract class AbstractAccount implements Account, Serializable {
    private final String accountId;
    private int accountAmount;

    protected AbstractAccount(final String accountId) {
        this(accountId, 0);
    }

    protected AbstractAccount(final String accountId, final int accountAmount) {
        this.accountId = accountId;
        this.accountAmount = accountAmount;
    }

    @Override
    public String getId() {
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