package ru.ifmo.rain.ageev.bank.classes;

import ru.ifmo.rain.ageev.bank.interfaces.Account;
import ru.ifmo.rain.ageev.bank.interfaces.Person;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class AbstractPerson implements Person, Serializable {
    private final String firstName;
    private final String lastName;
    private final int passportId;
    private final ConcurrentMap<String, Account> accounts;

    protected AbstractPerson(String firstName, String lastName, int passportId, final ConcurrentMap<String, Account> accounts) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.passportId = passportId;
        this.accounts = accounts;
    }

    protected AbstractPerson(String firstName, String lastName, int passportId) {
        this(firstName, lastName, passportId, new ConcurrentHashMap<>());
    }

    public String getAccountId(final String subId) {
        return passportId + ":" + subId;
    }

    @Override
    public Account getAccount(final String subId) {
        return accounts.get(subId);
    }


    protected void put(String subId, Account account) {
        accounts.put(subId, account);
    }

    protected Account putIfAbsent(final String subId, final Account newAccount) {
        return accounts.putIfAbsent(subId, newAccount);
    }

    protected Set<Map.Entry<String, Account>> getEntrySet() {
        return accounts.entrySet();
    }

    protected void put(Map.Entry<String, Account> pair) throws RemoteException {
        put(pair.getKey(), pair.getValue());
    }

    protected Account createNewAccountBySubId(final Account newAccount, final String subId) {
        final var account = putIfAbsent(subId, newAccount);
        if (account != null) {
            return account;
        }
        return newAccount;
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
    public int passportId() {
        return passportId;
    }
}