package ru.ifmo.rain.ageev.bank.test;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import ru.ifmo.rain.ageev.bank.Utils;
import ru.ifmo.rain.ageev.bank.classes.LocalAccount;
import ru.ifmo.rain.ageev.bank.classes.RemoteBank;
import ru.ifmo.rain.ageev.bank.interfaces.Account;
import ru.ifmo.rain.ageev.bank.interfaces.Bank;
import ru.ifmo.rain.ageev.bank.interfaces.Person;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class BankTester extends BaseTests {
    public static Bank bank;

    @BeforeClass
    public static void register() {
        Utils.register();
    }


    @Before
    public void initBank() throws RemoteException, MalformedURLException {
        bank = new RemoteBank(Utils.PORT);
        Naming.rebind(Utils.getUrl(), bank);
    }

    @After
    public void freeBank() throws NoSuchObjectException {
        UnicastRemoteObject.unexportObject(bank, false);
    }

    public Person getLocalPerson(final int passportId) {
        try {
            return bank.getLocalPerson(passportId);
        } catch (final RemoteException e) {
            throw new AssertionError(e);
        }
    }

    public Person getRemotePerson(final int passportId) {
        try {
            return bank.getRemotePerson(passportId);
        } catch (final RemoteException e) {
            throw new AssertionError(e);
        }
    }

    public Account addRemoteAccount(final String name) {
        try {
            return bank.addAccount(name);
        } catch (final RemoteException e) {
            throw new AssertionError(e);
        }
    }

    public Account getRemoteAccount(final String name) {
        try {
            return bank.getAccount(name);
        } catch (final RemoteException e) {
            throw new AssertionError(e);
        }
    }

    public Account createLocalAccount(final Account account)  {
        try {
            return new LocalAccount(account);
        } catch (final RemoteException e) {
            throw new AssertionError(e);
        }
    }
}
