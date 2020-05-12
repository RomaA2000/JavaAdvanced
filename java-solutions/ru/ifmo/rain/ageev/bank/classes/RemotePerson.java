package ru.ifmo.rain.ageev.bank.classes;

import ru.ifmo.rain.ageev.bank.interfaces.Account;
import ru.ifmo.rain.ageev.bank.interfaces.Bank;

import java.rmi.RemoteException;

public class RemotePerson extends AbstractPerson {
    private final Bank bank;
    public RemotePerson(final String firstName, final String secondName, final int passportId, final Bank bank) {
        super(firstName, secondName, passportId);
        this.bank = bank;
    }

    @Override
    public Account createNewAccountBySubId(final String subId) throws RemoteException {
        final var accountId = getAccountId(subId);
        final var account = bank.addAccount(accountId);
        final var prevAccount = putIfAbsent(subId, account);
        if (prevAccount != null) {
            return prevAccount;
        }
        return account;
    }
}
