package ru.ifmo.rain.ageev.concurrent;

import java.util.*;
import java.util.function.Function;

import static java.util.stream.IntStream.range;


/**
 * Class for collecting results from threads.
 *
 * @author ageev
 * @version 1.0
 */
class Collector<T, R> {
    private final List<R> results;
    private final List<RuntimeException> exceptions = new ArrayList<>();
    private final Queue<Runnable> subCollectors = new ArrayDeque<>();
    private int started = 0;
    private int finished = 0;
    private boolean finish;

    Collector(final Function<? super T, ? extends R> func, final List<? extends T> args) {
        results = new ArrayList<>(Collections.nCopies(args.size(), null));
        range(0, args.size()).forEach(i -> subCollectors.add(() -> {
            try {
                setResult(i, func.apply(args.get(i)));
            } catch (RuntimeException e) {
                setException(e);
            }
        }));
    }


    /**
     * Synchronized result setter.
     * Sets result to specified index.
     *
     * @param position index for result
     * @param element  result
     */
    public synchronized void setResult(final int position, R element) {
        results.set(position, element);
        modified();
    }

    /**
     * Synchronized error setter.
     * Sets error to {@link List}.
     *
     * @param error error
     */
    public synchronized void setException(RuntimeException error) {
        exceptions.add(error);
        modified();
    }

    private synchronized void modified() {
        finished++;
        if (results.size() == finished) {
            finish();
        }
    }

    public synchronized void finish() {
        finish = true;
        notify();
    }

    /**
     * Synchronized result getter.
     * Gets result when is's ready.
     *
     * @return {@link List} of results
     * @throws InterruptedException if executing thread was interrupted.
     */
    public synchronized List<R> getResult() throws InterruptedException {
        while (!finish) {
            wait();
        }
        if (hasExceptions()) {
            RuntimeException e = exceptions.get(0);
            exceptions.subList(1, exceptions.size()).forEach(e::addSuppressed);
            throw e;
        }
        return results;
    }

    public synchronized boolean wasLast() {
        return results.size() == started;
    }

    public synchronized Runnable getNext() {
        var subCollector = subCollectors.poll();
        started++;
        return subCollector;
    }

    /**
     * Synchronized exceptions checker.
     * Checks are there any exceptions.
     *
     * @return {@link List} of exceptions
     * @throws InterruptedException if executing thread was interrupted.
     */
    private synchronized boolean hasExceptions() {
        return !exceptions.isEmpty();
    }
}
