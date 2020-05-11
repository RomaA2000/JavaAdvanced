package ru.ifmo.rain.ageev.bank.classes;

import ru.ifmo.rain.ageev.bank.classes.AbstractAccount;
import ru.ifmo.rain.ageev.bank.interfaces.Account;

import java.rmi.RemoteException;

public class RemoteAccount extends AbstractAccount {
    public RemoteAccount(final String accountId) {
        super(accountId);
    }

    public RemoteAccount(final Account account) throws RemoteException {
        super(account.getId(), account.getAmount());
    }
}