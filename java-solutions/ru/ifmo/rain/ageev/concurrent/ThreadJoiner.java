package ru.ifmo.rain.ageev.concurrent;

import java.util.List;


/**
 * Class for joining threads.
 *
 * @author ageev
 * @version 1.0
 */
public class ThreadJoiner {
    private static void joinAllFlag(final List<Thread> workers, boolean nothrow) throws InterruptedException {
        for (int i = 0; i < workers.size(); i++) {
            try {
                workers.get(i).join();
            } catch (final InterruptedException e) {
                InterruptedException exception = new InterruptedException("Some threads were interrupted");
                exception.addSuppressed(e);
                for (int j = i; j < workers.size(); j++) {
                    workers.get(j).interrupt();
                }
                for (int j = i; j < workers.size(); j++) {
                    try {
                        workers.get(j).join();
                    } catch (InterruptedException er) {
                        if (!nothrow) {
                            exception.addSuppressed(er);
                        }
                        j--;
                    }
                }
                if (!nothrow) {
                    throw exception;
                }
            }
        }
    }


    /**
     * Joins treads from the {@link List}
     *
     * @param workers list of concurrent threads.
     * @throws InterruptedException if executing thread was interrupted.
     */
    public static void joinAll(final List<Thread> workers) throws InterruptedException {
        joinAllFlag(workers, false);
    }

    /**
     * Joins treads from the {@link List}. Ignores all exceptions.
     *
     * @param workers list of concurrent threads.
     */
    public static void joinAllNothrow(final List<Thread> workers) {
        try {
            joinAllFlag(workers, true);
        } catch (InterruptedException ignored) {
        }
    }
}
