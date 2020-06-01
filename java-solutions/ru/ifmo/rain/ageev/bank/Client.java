package ru.ifmo.rain.ageev.bank;

import ru.ifmo.rain.ageev.bank.interfaces.Bank;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Objects;

public class Client {
    public static void main(final String[] args) {
        if (args == null || args.length != 5 || Arrays.stream(args).anyMatch(Objects::isNull)) {
            System.err.println("BankClient usage: first-name last-name passport-id account-id amount");
            return;
        }
        final String firstName;
        final String lastName;
        final String passportId;
        final String accountSubId;
        final int amount;
        try {
            firstName = args[0];
            lastName = args[1];
            passportId = args[2];
            accountSubId = args[3];
            amount = Integer.parseInt(args[4]);
        } catch (NumberFormatException e) {
            System.err.println("amount and passport id need to be integers: " + e.getMessage());
            return;
        }
        final Bank bank;
        try {
            bank = Utils.getBank();
        } catch (final NotBoundException e) {
            System.err.println("Bank n-f: " + e.getMessage());
            return;
        } catch (final MalformedURLException e) {
            System.out.println("Bank URL is invalid");
            return;
        } catch (final RemoteException e) {
            System.err.println("Remote exception: " + e.getMessage());
            return;
        }
        try {
            var person = bank.getRemotePerson(passportId);
            if (person == null) {
                System.out.println("Making person");
                person = bank.addPerson(firstName, lastName, passportId);
            }
            if (person.firstName().equals(firstName) && person.lastName().equals(lastName) && person.passportId().equals(passportId)) {
                System.out.println("Person checked");
            } else {
                System.out.println("Person checking provided error");
                return;
            }
            var account = person.getAccount(accountSubId);
            if (account == null) {
                System.out.println("Adding new account");
                person.createNewAccountBySubId(accountSubId);
                account = person.getAccount(accountSubId);
            }
            System.out.println("Money before: " + account.getAmount());
            System.out.println("Increasing amount...");
            account.increaseAmount(amount);
            System.out.println("Money after: " + account.getAmount());
        } catch (RemoteException e) {
            System.err.println("Remote exception: " + e.getMessage());
        }
    }
}
