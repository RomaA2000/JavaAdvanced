package ru.ifmo.rain.ageev.bank.classes;

import ru.ifmo.rain.ageev.bank.interfaces.Account;

import java.io.Serializable;
import java.util.concurrent.ConcurrentMap;

public class LocalPerson extends AbstractPerson implements Serializable {
    public LocalPerson(final String firstName, final String lastName, final String passportId, final ConcurrentMap<String, Account> accounts) {
        super(firstName, lastName, passportId, accounts);
    }

    @Override
    public Account createNewAccountBySubId(final String subId) {
        return putIfAbsent(subId, new LocalAccount(getAccountId(subId)));
    }
}