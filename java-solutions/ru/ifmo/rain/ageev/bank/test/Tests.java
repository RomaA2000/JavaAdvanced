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
    public void test0_simple_account() throws RemoteException {
        testDefaultAccountIncrease(new RemoteAccount(id), id);
        testDefaultAccountIncrease(new LocalAccount(id), id);
    }

    @Test
    public void test0_account_big_name() throws RemoteException {
        final var newId = "account_id_account_id_account_id_account_id_account_id_account_id_account_id_account_id_account_id_";
        testDefaultAccountIncrease(new RemoteAccount(newId), newId);
        testDefaultAccountIncrease(new LocalAccount(newId), newId);
    }

    @Test
    public void test0_account_getter() {
        addRemoteAccount(id);
        assertNotNull(getRemoteAccount(id));
    }

    @Test
    public void test0_account_creator() {
        addRemoteAccount(id);
        assertNotNull(addRemoteAccount(id));
    }

    @Test
    public void test0_account_create_and_increase() throws RemoteException {
        addRemoteAccount(id);
        testDefaultAccountIncrease(getRemoteAccount(id), id);
    }

    @Test
    public void test1_simple_increase() throws RemoteException {
        var account = addRemoteAccount(id);
        account.increaseAmount(1);
        checkAccount(account, id, 1);
        account.increaseAmount(1);
        checkAccount(account, id, 2);
    }

    @Test
    public void test1_many_increases_sync() throws RemoteException {
        var account = addRemoteAccount(id);
        final int number = 200;
        for (int i = 0; i < number; i++) {
            account.increaseAmount(1);
            checkAccount(account, id, i + 1);
        }
    }

    @Test
    public void test1_many_accounts_many_increases_sync() throws RemoteException {
        final int number = 200;
        final int count = 10;
        var strings = stringGenerator(count);
        strings.forEach(this::addRemoteAccount);
        List<Account> remoteAccounts = strings.stream().map(this::getRemoteAccount).collect(Collectors.toList());
        List<Account> localAccounts = remoteAccounts.stream().map(this::createLocalAccount).collect(Collectors.toList());

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
    public void test2_simple_increases_async() throws RemoteException, MalformedURLException, NotBoundException {
        app(1, 1, 10, 2);
    }

    @Test
    public void test2_simple_many_increases_async() throws RemoteException, MalformedURLException, NotBoundException {
        app(1, 1, 50, 2);
    }

    @Test
    public void test2_many_accounts_increases_async() throws RemoteException, MalformedURLException, NotBoundException {
        app(1, 10, 10, 2);
    }

    @Test
    public void test2_too_many_accounts_increases_async() throws RemoteException, MalformedURLException, NotBoundException {
        app(1, 50, 2, 4);
    }

    @Test
    public void test3_many_persons_increase_async() throws RemoteException, MalformedURLException, NotBoundException {
        app(10, 1, 2, 4);
    }

    @Test
    public void test3_many_persons_many_accounts_increase_async() throws RemoteException, MalformedURLException, NotBoundException {
        app(10, 10, 2,4);
    }

    @Test
    public void test3_too_many_persons_many_accounts_increase_async() throws RemoteException, MalformedURLException, NotBoundException {
        app(50, 10, 2, 4);
    }

    @Test
    public void test3_too_many_persons_too_many_accounts_increase_async() throws RemoteException, MalformedURLException, NotBoundException {
        app(20, 20, 2,4);
    }

    @Test
    public void test2_too_many_accounts_increases_many_threads_async() throws RemoteException, MalformedURLException, NotBoundException {
        app(1, 50, 2, 8);
    }

    @Test
    public void test3_too_many_persons_many_accounts_increase_many_threads_async() throws RemoteException, MalformedURLException, NotBoundException {
        app(50, 10, 2, 8);
    }

    @Test
    public void test3_too_many_persons_too_many_accounts_increase__many_threads_async() throws RemoteException, MalformedURLException, NotBoundException {
        app(15, 15, 2,8);
    }
}
