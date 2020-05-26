package ru.ifmo.rain.ageev.bank.classes;

import ru.ifmo.rain.ageev.bank.interfaces.Account;
import ru.ifmo.rain.ageev.bank.interfaces.Person;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class AbstractPerson implements Person, Serializable {
    private final String firstName;
    private final String lastName;
    private final int passportId;
    private final ConcurrentMap<String, Account> accounts;

    protected AbstractPerson(final String firstName, final String lastName, final int passportId, final ConcurrentMap<String, Account> accounts) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.passportId = passportId;
        this.accounts = accounts;
    }

    protected AbstractPerson(final String firstName, final String lastName, final int passportId) {
        this(firstName, lastName, passportId, new ConcurrentHashMap<>());
    }

    public String getAccountId(final String subId) {
        return passportId + ":" + subId;
    }

    @Override
    public Account getAccount(final String subId) {
        return accounts.get(subId);
    }

    protected Account putIfAbsent(final String subId, final Account newAccount) {
        return accounts.putIfAbsent(subId, newAccount);
    }

    protected ConcurrentMap<String, Account> getMapCopy() {
        return new ConcurrentHashMap<>(accounts);
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