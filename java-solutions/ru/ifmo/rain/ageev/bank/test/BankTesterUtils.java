package ru.ifmo.rain.ageev.bank.test;

import ru.ifmo.rain.ageev.bank.Client;
import ru.ifmo.rain.ageev.bank.Server;
import ru.ifmo.rain.ageev.bank.Utils;
import ru.ifmo.rain.ageev.bank.interfaces.Account;
import ru.ifmo.rain.ageev.bank.interfaces.Bank;
import ru.ifmo.rain.ageev.bank.interfaces.Person;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import static java.util.stream.IntStream.range;

public class BankTesterUtils extends BaseTests {
    public static final String id = "testing";

    public static List<String> stringGenerator(final int n) {
        return range(0, n).mapToObj(Integer::toString).collect(Collectors.toList());
    }

    public static void testDefaultAccountIncrease(final Account account, final String accountId) throws RemoteException {
        checkAccount(account, accountId, 0);
        account.increaseAmount(accountId.hashCode());
        checkAccount(account, accountId, accountId.hashCode());
    }

    public static void checkAccount(final Account account, final String accountId, final int amount) {
        try {
            assertNotNull(account);
            assertEquals(accountId, account.getId());
            assertEquals(amount, account.getAmount());
        } catch (final RemoteException e) {
            throw new AssertionError(e);
        }
    }

    public static void checkPerson(final Person person, final String firstName, final String lastName, final String passportId) {
        try {
            assertNotNull(person);
            assertEquals(firstName, person.firstName());
            assertEquals(lastName, person.lastName());
            assertEquals(passportId, person.passportId());
        } catch (final RemoteException e) {
            throw new AssertionError(e);
        }
    }

    public static void checkPerson(final Person person1, final Person person2) {
        try {
            assertNotNull(person2);
            checkPerson(person1, person2.firstName(), person2.lastName(), person2.passportId());
        } catch (final RemoteException e) {
            throw new AssertionError(e);
        }
    }

    protected static void app(final int personNumber, final int accountNumber, final int counter, final int threads) throws RemoteException, MalformedURLException, NotBoundException {
        Server.main(null);
        final var personId = "person_id_";
        final var accountId = "account_id_";
        final Bank bank = Utils.getBank();
        if (bank == null) {
            throw new AssertionError();
        }
        final List<BaseTests.Command<RemoteException>> commands = new ArrayList<>();
        for (int nowPerson = 0; nowPerson < personNumber; nowPerson++) {
            final var person = personId + nowPerson;
            final var hash = Integer.toString(person.hashCode());
            bank.addPerson(person, person, hash);
            final var remotePerson = bank.getRemotePerson(hash);
            checkPerson(remotePerson, person, person, hash);
            commands.add(() -> {
                final var args = new String[]{person, person, Integer.toString(person.hashCode()), "not_used", "1"};
                assertNotNull(remotePerson);
                for (int nowAccount = 0; nowAccount < accountNumber; nowAccount++) {
                    args[3] = accountId + nowAccount;
                    for (int incNum = 0; incNum < counter; incNum++) {
                        Client.main(args);
                        var account = remotePerson.getAccount(args[3]);
                        assertNotNull(account);
                        assertEquals(incNum + 1, account.getAmount());
                    }
                }
            });
        }
        parallelCommands(threads, commands);
    }
}
