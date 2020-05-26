package ru.ifmo.rain.ageev.bank.classes;

import ru.ifmo.rain.ageev.bank.interfaces.Account;

import java.io.Serializable;

public class LocalPerson extends AbstractPerson implements Serializable {
    public LocalPerson(final RemotePerson person) {
        super(person.firstName(), person.lastName(), person.passportId());
        for (final var entry : person.getEntrySet()) {
            put(entry);
        }
    }

    @Override
    public Account createNewAccountBySubId(final String subId) {
        return putIfAbsent(subId, new LocalAccount(getAccountId(subId)));
    }
}