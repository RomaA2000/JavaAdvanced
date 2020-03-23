package ru.ifmo.rain.ageev.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Class for collecting results from threads.
 *
 * @author ageev
 * @version 1.0
 */
public class Collector<Return> {
    private List<Return> results;
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
    }

    /**
     * Synchronized setter.
     * Sets result to specified index.
     *
     * @param position index for result
     * @param element  result
     */
    public synchronized void setSynchronized(final int position, Return element) {
        results.set(position, element);
        readyThreads++;
        if (results.size() == readyThreads) {
            notify();
        }
    }

    /**
     * Synchronized getter.
     * Gets result when is's ready.
     *
     * @return {@link List} of results
     * @throws InterruptedException if executing thread was interrupted.
     */
    public synchronized List<Return> getResultSynchronized() throws InterruptedException {
        while (results.size() > readyThreads) {
            wait();
        }
        return results;
    }
}
