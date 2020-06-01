package ru.ifmo.rain.ageev.bank.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentMap;


public interface Person extends Remote {
    String firstName() throws RemoteException;

    String lastName() throws RemoteException;

    String passportId() throws RemoteException;

    Account createNewAccountBySubId(final String subId) throws RemoteException;

    Account getAccount(final String subId) throws RemoteException;
}