package ru.ifmo.rain.ageev.bank.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;


public interface Person extends Remote {
    String firstName() throws RemoteException;

    String lastName() throws RemoteException;

    int passportId() throws RemoteException;

    Account createNewAccountBySubId(final String subId) throws RemoteException;

    Account getAccount(final String subId) throws RemoteException;
}