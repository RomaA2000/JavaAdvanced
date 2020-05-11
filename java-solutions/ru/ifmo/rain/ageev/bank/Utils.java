package ru.ifmo.rain.ageev.bank;

import ru.ifmo.rain.ageev.bank.interfaces.Bank;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class Utils {
    public final static String HOST = "localhost";
    public final static int PORT = 8886;
    public final static String PATH = "bank";

    public static String getUrl() {
        return String.format("//%s:%d/%s", HOST, PORT, PATH);
    }

    public static void register() {
        try {
            LocateRegistry.createRegistry(PORT);
        } catch (RemoteException ignored) {}
    }

    public static Bank getBank() throws RemoteException, NotBoundException, MalformedURLException {
        return (Bank) Naming.lookup(getUrl());
    }
}
