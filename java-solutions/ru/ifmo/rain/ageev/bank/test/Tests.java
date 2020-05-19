package ru.ifmo.rain.ageev.bank.test;

import org.junit.Test;
import ru.ifmo.rain.ageev.bank.Client;
import ru.ifmo.rain.ageev.bank.Server;
import ru.ifmo.rain.ageev.bank.classes.LocalAccount;
import ru.ifmo.rain.ageev.bank.classes.RemoteAccount;
import ru.ifmo.rain.ageev.bank.interfaces.Account;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.stream.Collectors;

import static ru.ifmo.rain.ageev.bank.test.BankTesterUtils.*;

public class Tests extends BankTester {
    private static final String id = "account_id";

    @Test
    public void test0_simpleAccount() throws RemoteException {
        testDefaultAccountIncrease(new RemoteAccount(id), id);
        testDefaultAccountIncrease(new LocalAccount(id), id);
    }

    @Test
    public void test0_accountBigName() throws RemoteException {
        final var newId = "account_id_account_id_account_id_account_id_account_id_account_id_account_id_account_id_account_id_";
        testDefaultAccountIncrease(new RemoteAccount(newId), newId);
        testDefaultAccountIncrease(new LocalAccount(newId), newId);
    }

    @Test
    public void test0_accountGetter() {
        addRemoteAccount(id);
        assertNotNull(getRemoteAccount(id));
    }

    @Test
    public void test0_accountCreator() {
        addRemoteAccount(id);
        assertNotNull(addRemoteAccount(id));
    }

    @Test
    public void test0_accountCreateAndIncrease() throws RemoteException {
        addRemoteAccount(id);
        testDefaultAccountIncrease(getRemoteAccount(id), id);
    }

    @Test
    public void test1_simpleIncrease() throws RemoteException {
        var account = addRemoteAccount(id);
        account.increaseAmount(1);
        checkAccount(account, id, 1);
        account.increaseAmount(1);
        checkAccount(account, id, 2);
    }

    @Test
    public void test1_manyIncreasesSync() throws RemoteException {
        var account = addRemoteAccount(id);
        final int number = 200;
        for (int i = 0; i < number; i++) {
            account.increaseAmount(1);
            checkAccount(account, id, i + 1);
        }
    }

    @Test
    public void test1_manyAccountsManyIncreasesSync() throws RemoteException {
        final int number = 200;
        final int count = 10;
        var strings = stringGenerator(count);
        strings.forEach(this::addRemoteAccount);
        List<Account> remoteAccounts = strings.stream().map(this::getRemoteAccount).collect(Collectors.toList());
        List<Account> localAccounts = remoteAccounts.stream().map(this::createLocalAccount).collect(Collectors.toList());
        for (int i = 0; i < remoteAccounts.size(); i++) {
            checkAccount(remoteAccounts.get(i), localAccounts.get(i).getId(), localAccounts.get(i).getAmount());
        }
        for (int i = 0; i < number; i++) {
            for (var j : remoteAccounts) {
                j.increaseAmount(1);
            }
            for (var j : localAccounts) {
                j.increaseAmount(1);
                assertEquals(i + 1, j.getAmount());
            }
            for (var j : remoteAccounts) {
                assertEquals(i + 1, j.getAmount());
            }
        }
    }

    @Test
    public void test1_localPerson() throws RemoteException {
        for (int i = 0; i < 100; ++i) {
            final String person = id + i;
            bank.addPerson(person, person, person.hashCode());
            final var remotePerson = bank.getRemotePerson(person.hashCode());
            final var localPerson = bank.getLocalPerson(person.hashCode());
            checkPerson(localPerson, person, person, person.hashCode());
            checkPerson(localPerson, remotePerson);
            localPerson.createNewAccountBySubId(person + 10);
            assertNull(remotePerson.getAccount(person + 10));
        }
    }

    @Test
    public void test1_clientArgs() {
        Server.main(null);
        final String[] args = {"first_name", "last_name", "0", "subId", "1"};
        assertNotThrows(() -> Client.main(args));
        args[2] = "fail";
        assertNotThrows(() -> Client.main(args));
        args[4] = "fail";
        assertNotThrows(() -> Client.main(args));
    }

    @Test
    public void test2_simpleIncreasesAsync() throws RemoteException, MalformedURLException, NotBoundException {
        app(1, 1, 10, 2);
    }

    @Test
    public void test2_simpleManyIncreasesAsync() throws RemoteException, MalformedURLException, NotBoundException {
        app(1, 1, 50, 2);
    }

    @Test
    public void test2_manyAccountsIncreasesAsync() throws RemoteException, MalformedURLException, NotBoundException {
        app(1, 10, 10, 2);
    }

    @Test
    public void test2_tooManyAccountsIncreasesAsync() throws RemoteException, MalformedURLException, NotBoundException {
        app(1, 50, 2, 4);
    }

    @Test
    public void test3_manyPersonsIncreaseAsync() throws RemoteException, MalformedURLException, NotBoundException {
        app(10, 1, 2, 4);
    }

    @Test
    public void test3_manyPersonsManyAccountsIncreaseAsync() throws RemoteException, MalformedURLException, NotBoundException {
        app(10, 10, 2,4);
    }

    @Test
    public void test3_tooManyPersonsManyAccountsIncreaseAsync() throws RemoteException, MalformedURLException, NotBoundException {
        app(50, 10, 2, 4);
    }

    @Test
    public void test3_tooManyPersonsTooManyAccountsIncreaseAsync() throws RemoteException, MalformedURLException, NotBoundException {
        app(20, 20, 2,4);
    }

    @Test
    public void test3_tooManyAccountsIncreasesManyThreadsAsync() throws RemoteException, MalformedURLException, NotBoundException {
        app(1, 50, 2, 8);
    }

    @Test
    public void test3_tooManyPersonsManyAccountsIncreaseManyThreadsAsync() throws RemoteException, MalformedURLException, NotBoundException {
        app(50, 10, 2, 8);
    }

    @Test
    public void test3_tooManyPersonsTooManyAccountsIncreaseManyThreadsAsync() throws RemoteException, MalformedURLException, NotBoundException {
        app(15, 15, 2,8);
    }
}
