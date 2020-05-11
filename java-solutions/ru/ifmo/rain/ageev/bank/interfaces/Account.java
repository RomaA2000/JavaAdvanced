package ru.ifmo.rain.ageev.bank.interfaces;

import java.rmi.*;

public interface Account extends Remote {
    /** Returns account identifier. */
    String getId() throws RemoteException;

    /** Returns amount of money at the account. */
    int getAmount() throws RemoteException;

    /** Increase amount of money at the account. */
    void increaseAmount(int amount) throws RemoteException;
}