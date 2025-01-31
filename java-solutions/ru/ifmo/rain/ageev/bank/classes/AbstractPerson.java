package ru.ifmo.rain.ageev.bank.classes;

import ru.ifmo.rain.ageev.bank.interfaces.Account;
import ru.ifmo.rain.ageev.bank.interfaces.Person;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class AbstractPerson implements Person, Serializable {
    private final String firstName;
    private final String lastName;
    private final String passportId;
    private final ConcurrentMap<String, Account> accounts;

    AbstractPerson(final String firstName, final String lastName, final String passportId, final ConcurrentMap<String, Account> accounts) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.passportId = passportId;
        this.accounts = accounts;
    }

    AbstractPerson(final String firstName, final String lastName, final String passportId) {
        this(firstName, lastName, passportId, new ConcurrentHashMap<>());
    }

    public String getAccountId(final String subId) {
        return passportId + ":" + subId;
    }

    @Override
    public Account getAccount(final String subId) {
        return accounts.get(subId);
    }

    Account putIfAbsent(final String subId, final Account newAccount) {
        return accounts.putIfAbsent(subId, newAccount);
    }

    ConcurrentMap<String, Account> getMapCopy() throws RemoteException {
        var newAccounts = new ConcurrentHashMap<String, Account>();
        for (final Map.Entry<String, Account> entry : accounts.entrySet()) {
            final var val = entry.getValue();
            newAccounts.put(entry.getKey(), new LocalAccount(val.getId(), val.getAmount()));
        }
        return newAccounts;
    }

    @Override
    public String firstName() {
        return firstName;
    }

    @Override
    public String lastName() {
        return lastName;
    }

    @Override
    public String passportId() {
        return passportId;
    }
}