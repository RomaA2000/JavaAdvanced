package ru.ifmo.rain.ageev.bank.classes;

import ru.ifmo.rain.ageev.bank.classes.AbstractAccount;
import ru.ifmo.rain.ageev.bank.interfaces.Account;

import java.io.Serializable;
import java.rmi.RemoteException;

public class LocalAccount extends AbstractAccount implements Serializable {
    public LocalAccount(final String accountId) {
        super(accountId);
    }

    public LocalAccount(final Account account) throws RemoteException {
        super(account.getId(), account.getAmount());
    }
}