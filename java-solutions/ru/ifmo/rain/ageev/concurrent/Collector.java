package ru.ifmo.rain.ageev.concurrent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Class for collecting results from threads.
 *
 * @author ageev
 * @version 1.0
 */
public class Collector<Return, Exceptions> {
    private final List<Return> results;
    private final List<Exceptions> exceptions;
    private int readyThreads;

    /**
     * Default constructor.
     * Creates an Collector instance.
     *
     * @param size size of created collector
     */
    Collector(int size) {
        readyThreads = 0;
        results = new ArrayList<>(Collections.nCopies(size, null));
        exceptions = new ArrayList<>();
    }

    /**
     * Synchronized result setter.
     * Sets result to specified index.
     *
     * @param position index for result
     * @param element  result
     */
    public void setResult(final int position, Return element) {
        synchronized (results) {
            results.set(position, element);
        }
        modified();
    }

    /**
     * Synchronized error setter.
     * Sets error to {@link List}.
     *
     * @param error error
     */
    public void setException(Exceptions error) {
        synchronized (exceptions) {
            exceptions.add(error);
        }
        modified();
    }

    private synchronized void modified() {
        readyThreads++;
        if (results.size() == readyThreads) {
            notify();
        }
    }

    private synchronized void waitForFinish() throws InterruptedException {
        while (results.size() > readyThreads) {
            wait();
        }
    }

    /**
     * Synchronized result getter.
     * Gets result when is's ready.
     *
     * @return {@link List} of results
     * @throws InterruptedException if executing thread was interrupted.
     */
    public synchronized List<Return> getResult() throws InterruptedException {
        waitForFinish();
        return results;
    }

    /**
     * Synchronized exceptions checker.
     * Checks are there any exceptions.
     *
     * @return {@link List} of exceptions
     * @throws InterruptedException if executing thread was interrupted.
     */
    public synchronized boolean hasExceptions() throws InterruptedException {
        waitForFinish();
        return !exceptions.isEmpty();
    }

    /**
     * Synchronized exceptions getter.
     * Gets exceptions when is's ready.
     *
     * @return {@link List} of exceptions
     * @throws InterruptedException if executing thread was interrupted.
     */
    public synchronized List<Exceptions> getExceptions() throws InterruptedException {
        waitForFinish();
        return exceptions;
    }
}
