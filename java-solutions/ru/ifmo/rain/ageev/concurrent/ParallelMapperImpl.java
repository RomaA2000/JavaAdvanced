package ru.ifmo.rain.ageev.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.IntStream.range;

/**
 * @author ageev
 * @version 1.0.1
 */
public class ParallelMapperImpl implements ParallelMapper, AutoCloseable {
    private final SynchronizedQueue tasksQueue = new SynchronizedQueue();
    private final List<Thread> workersList = new ArrayList<>();
    private boolean closed = false;
    private final Runnable SIMPLE_TASK = () -> {
        try {
            while (!Thread.interrupted()) {
                tasksQueue.getNext().run();
            }
        } catch (final InterruptedException ignored) {
        } finally {
            Thread.currentThread().interrupt();
        }
    };
    /**
     * Constructor with specified threads number.
     * Creates a ParallelMapperImpl instance operating with maximum of {@code threads}
     * threads of type {@link Thread}.
     *
     * @param number number of operable threads
     */
    public ParallelMapperImpl(final int number) {
        if (number <= 0) {
            throw new IllegalArgumentException("threads count must be greater than 0");
        }
        range(0, number).forEach(i -> workersList.add(new Thread(SIMPLE_TASK)));
        workersList.forEach(Thread::start);
    }

    /**
     * Maps function {@code f} over specified {@code args}.
     * Mapping for each element performs in parallel.
     *
     * @param function function for mapping
     * @param list     arguments {@link List}
     * @param <T>      type of arguments
     * @param <R>      type of resulting values
     * @return {@link List} of mapping results
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T, R> List<R> map(final Function<? super T, ? extends R> function, final List<? extends T> list) throws InterruptedException {
        final Collector<T, R> coll;
        synchronized (this) {
            if (closed) {
                return null;
            }
            coll = new Collector<>(function, list);
        }
        tasksQueue.addTask(coll);
        return coll.getResult();
    }

    /**
     * Joins all threads.
     */
    @Override
    public void close() {
        synchronized (this) {
            closed = true;
            workersList.forEach(Thread::interrupt);
            tasksQueue.shutdown();
        }
        ThreadJoiner.joinAllNothrow(workersList);
    }
}
