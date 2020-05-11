package ru.ifmo.rain.ageev.bank.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Bank extends Remote {
    /**
     * Creates a new account with specified identifier if it is not already exists.
     * @param id account id
     * @return created or existing account.
     */
    Account addAccount(String id) throws RemoteException;

    /**
     * Returns account by identifier.
     * @param id account id
     * @return account with specified identifier or {@code null} if such account does not exists.
     */
    Account getAccount(String id) throws RemoteException;

    Person addPerson(String name, String surname, int passportId) throws RemoteException;

    Person getLocalPerson(int passportId) throws RemoteException;

    Person getRemotePerson(int passportId) throws RemoteException;
}