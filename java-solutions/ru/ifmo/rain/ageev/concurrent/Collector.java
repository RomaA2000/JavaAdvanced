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
            } catch (final RuntimeException e) {
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
    public synchronized void setResult(final int position, final R element) {
        results.set(position, element);
        modified();
    }

    /**
     * Synchronized error setter.
     * Sets error to {@link List}.
     *
     * @param error error
     */
    public synchronized void setException(final RuntimeException error) {
        exceptions.add(error);
        modified();
    }

    private synchronized void modified() {
        if (results.size() == ++finished) {
            notify();
        }
    }

    public synchronized void finish() {
        finished = results.size();
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
        while (finished != results.size()) {
            wait();
        }
        if (!exceptions.isEmpty()) {
            final RuntimeException e = exceptions.get(0);
            exceptions.subList(1, exceptions.size()).forEach(e::addSuppressed);
            throw e;
        }
        return results;
    }

    public synchronized boolean wasLast() {
        return results.size() == started;
    }

    public synchronized Runnable getNext() {
        started++;
        return subCollectors.poll();
    }
}
