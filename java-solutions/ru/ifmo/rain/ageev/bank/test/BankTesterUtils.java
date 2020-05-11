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

    public static List<String> stringGenerator(int n) {
        return range(0, n).mapToObj(Integer::toString).collect(Collectors.toList());
    }

    public static void testDefaultAccountIncrease(final Account account, final String accountId) throws RemoteException {
        BankTesterUtils.checkAccount(account, accountId, 0);
        account.increaseAmount(accountId.hashCode());
        BankTesterUtils.checkAccount(account, accountId, accountId.hashCode());
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

    public static void checkPerson(final Person person, final String firstName, final String lastName, final int passportId) {
        try {
            assertNotNull(person);
            assertEquals(firstName, person.firstName());
            assertEquals(lastName, person.lastName());
            assertEquals(passportId, person.passportId());
        } catch (final RemoteException e) {
            throw new AssertionError(e);
        }
    }

    protected static void app(final int personNumber, final int accountNumber, final int counter) throws RemoteException, MalformedURLException, NotBoundException {
        Server.main(null);
        final Bank bank = Utils.getBank();
        if (bank == null) {
            throw new AssertionError();
        }
        List<BaseTests.Command<RemoteException>> commands = new ArrayList<>();
        for (int now_person = 0; now_person < personNumber; now_person++) {
            final var person = "intresting_person_id_" + now_person;
            bank.addPerson(person, person, person.hashCode());
            final var remotePerson = bank.getRemotePerson(person.hashCode());
            checkPerson(remotePerson, person, person, person.hashCode());
            commands.add(() -> {
                final var args = new String[]{person, person, Integer.toString(person.hashCode()), "not_used", "1"};
                assertNotNull(remotePerson);
                for (int now_account = 0; now_account < accountNumber; now_account++) {
                    args[3] = "intresting_account_id_" + now_account;
                    for (int inc_num = 0; inc_num < counter; inc_num++) {
                        Client.main(args);
                        var account = remotePerson.getAccount(args[3]);
                        assertEquals(inc_num + 1, account.getAmount());
                    }
                }
            });
        }
        parallelCommands(personNumber, commands);
    }
}
