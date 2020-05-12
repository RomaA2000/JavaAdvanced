package ru.ifmo.rain.ageev.bank.classes;

import ru.ifmo.rain.ageev.bank.interfaces.Account;
import ru.ifmo.rain.ageev.bank.interfaces.Bank;

import java.rmi.RemoteException;

public class RemotePerson extends AbstractPerson {
    private final Bank bank;
    public RemotePerson(String firstName, String secondName, int passportId, Bank bank) {
        super(firstName, secondName, passportId);
        this.bank = bank;
    }

    @Override
    public Account createNewAccountBySubId(final String subId) throws RemoteException {
        var accountId = getAccountId(subId);
        var account = bank.addAccount(accountId);
        var prevAccount = putIfAbsent(subId, account);
        if (prevAccount != null) {
            return prevAccount;
        }
        return account;
    }
}
