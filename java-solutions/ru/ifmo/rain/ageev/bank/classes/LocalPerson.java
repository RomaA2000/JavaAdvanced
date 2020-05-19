package ru.ifmo.rain.ageev.bank.classes;

import ru.ifmo.rain.ageev.bank.interfaces.Account;
import ru.ifmo.rain.ageev.bank.interfaces.Person;

import java.io.Serializable;
import java.rmi.RemoteException;

public class LocalPerson extends AbstractPerson implements Serializable {
    public LocalPerson(final Person person) throws RemoteException {
        super(person.firstName(), person.lastName(), person.passportId());
        for (final var entry : person.getEntrySet()) {
            put(entry);
        }
    }

    @Override
    public Account createNewAccountBySubId(final String subId) {
        final var accountId = getAccountId(subId);
        final var newLocalAccount = new LocalAccount(accountId);
        final var account = putIfAbsent(subId, newLocalAccount);
        if (account != null) {
            return account;
        }
        return newLocalAccount;
    }
}