package ru.ifmo.rain.ageev.bank.classes;

import ru.ifmo.rain.ageev.bank.interfaces.Account;
import ru.ifmo.rain.ageev.bank.interfaces.Bank;
import ru.ifmo.rain.ageev.bank.interfaces.Person;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RemoteBank extends UnicastRemoteObject implements Bank {
    private final int port;
    private final ConcurrentMap<String, Account> accounts = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, Person> persons = new ConcurrentHashMap<>();

    public RemoteBank(final int port) throws RemoteException {
        super(port);
        this.port = port;
    }

    @Override
    public Account addAccount(String id) throws RemoteException {
        final var newRemoteAccount = new RemoteAccount(id);
        UnicastRemoteObject.exportObject(newRemoteAccount, port);
        var realAccount = accounts.putIfAbsent(id, newRemoteAccount);
        if (realAccount != null) {
            return realAccount;
        }
        return newRemoteAccount;
    }

    @Override
    public Account getAccount(final String id) {
        return accounts.get(id);
    }

    @Override
    public Person addPerson(final String firstName, final String lastName, final int passportId) throws RemoteException {
        final var person = new RemotePerson(firstName, lastName, passportId, this);
        UnicastRemoteObject.exportObject(person, port);
        var realPerson = persons.putIfAbsent(passportId, person);
        if (realPerson != null) {
            return realPerson;
        }
        return person;
    }

    @Override
    public Person getLocalPerson(final int passportId) throws RemoteException {
        final var person = getRemotePerson(passportId);
        if (person == null) {
            return null;
        }
        return new LocalPerson((RemotePerson) person);
    }

    @Override
    public Person getRemotePerson(final int passportId) {
        return persons.get(passportId);
    }
}