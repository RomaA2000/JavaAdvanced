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
        return putIfAbsent(subId, bank.addAccount(getAccountId(subId)));
    }
}
