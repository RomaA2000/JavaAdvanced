package ru.ifmo.rain.ageev.bank.classes;

import ru.ifmo.rain.ageev.bank.interfaces.Account;
import ru.ifmo.rain.ageev.bank.interfaces.Bank;
import ru.ifmo.rain.ageev.bank.interfaces.Person;

import java.io.UncheckedIOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

public class RemoteBank extends UnicastRemoteObject implements Bank {
    private final int port;
    private final ConcurrentMap<String, Account> accounts = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, Person> persons = new ConcurrentHashMap<>();

    public RemoteBank(final int port) throws RemoteException {
        super(port);
        this.port = port;
    }

    private <T, E extends Remote> E add(final T id, final Map<T, E> map, final Function<? super T, ? extends E> adder) throws RemoteException {
        try {
            return map.computeIfAbsent(id, newId -> {
                final E element = adder.apply(newId);
                try {
                    UnicastRemoteObject.exportObject(element, port);
                } catch (final RemoteException remoteException) {
                    throw new UncheckedIOException(remoteException);
                }
                return element;
            });
        } catch (UncheckedIOException e) {
            throw (RemoteException) e.getCause();
        }
    }

    @Override
    public Account addAccount(String id) throws RemoteException {
        return add(id, accounts, RemoteAccount::new);
    }

    @Override
    public Account getAccount(final String id) {
        return accounts.get(id);
    }

    @Override
    public Person addPerson(final String firstName, final String lastName, final int passportId) throws RemoteException {
        return add(passportId, persons, lastId -> new RemotePerson(firstName, lastName, lastId, this));
    }

    @Override
    public Person getLocalPerson(final int passportId) throws RemoteException {
        final RemotePerson person = (RemotePerson) getRemotePerson(passportId);
        if (person == null) {
            return null;
        }
        return new LocalPerson(person.firstName(), person.lastName(), person.passportId(),  person.getMapCopy());
    }

    @Override
    public Person getRemotePerson(final int passportId) {
        return persons.get(passportId);
    }
}