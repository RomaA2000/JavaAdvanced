package ru.ifmo.rain.ageev.bank.test;


import org.junit.Assert;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

public class BaseTests extends Assert {
    public static <E extends Exception> void parallelCommands(final int threads, final List<BaseTests.Command<E>> commands) {
        final var executor = Executors.newFixedThreadPool(threads);
        try {
            for (final Future<Void> future : executor.invokeAll(commands)) {
                future.get();
            }
            executor.shutdown();
        } catch (final InterruptedException | ExecutionException e) {
            throw new AssertionError(e);
        }
    }

    public static <E extends Exception> void parallel(final int threads, final BaseTests.Command<E> command) {
        parallelCommands(threads, Collections.nCopies(threads, command));
    }

    public static void assertThrows(final Runnable r) {
        assertThrows(null, r);
    }

    public static void assertNotThrows(final Runnable r) {
        assertNotThrows(null, r);
    }

    public static void assertThrows(String message, final Runnable r) {
        assertFalse(message, notThrows(r));
    }

    public static void assertNotThrows(String message, final Runnable r) {
        assertTrue(message, notThrows(r));
    }

    private static boolean notThrows(Runnable r) {
        try {
            r.run();
        } catch (final Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @FunctionalInterface
    public interface Command<E extends Exception> extends Callable<Void> {
        @Override
        default Void call() throws E {
            run();
            return null;
        }

        void run() throws E;
    }
}
