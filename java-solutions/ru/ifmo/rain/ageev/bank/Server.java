package ru.ifmo.rain.ageev.bank;

import ru.ifmo.rain.ageev.bank.classes.RemoteBank;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;

public class Server {
    public static void main(final String[] args) {
        Utils.register();
        try {
            final var bank = new RemoteBank(Utils.PORT);
            Naming.rebind(Utils.getUrl(), bank);
        } catch (final RemoteException e) {
            System.out.println("Cannot export object: " + e.getMessage());
            e.printStackTrace();
        } catch (final MalformedURLException e) {
            System.out.println("Malformed URL");
        }
        System.out.println("Server started");
    }
}