package ru.ifmo.rain.ageev.bank.classes;

import ru.ifmo.rain.ageev.bank.interfaces.Account;

import java.io.Serializable;

public class LocalAccount extends AbstractAccount implements Serializable {
    public LocalAccount(final String accountId) {
        super(accountId);
    }

    public LocalAccount(final String accountId, final int accountAmount) {
        super(accountId, accountAmount);
    }
}