package ru.ifmo.rain.ageev.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;

import static java.util.stream.IntStream.range;

/**
 * @author ageev
 * @version 1.0.1
 */
public class ParallelMapperImpl implements ParallelMapper, AutoCloseable {
    private final SynchronizedQueue tasksQueue;
    private final List<Thread> workersList;

    /**
     * Constructor with specified threads number.
     * Creates a ParallelMapperImpl instance operating with maximum of {@code threads}
     * threads of type {@link Thread}.
     *
     * @param number number of operable threads
     */
    public ParallelMapperImpl(int number) {
        if (number <= 0) {
            throw new IllegalArgumentException("threads count must be greater than 0");
        }
        tasksQueue = new SynchronizedQueue();
        workersList = new ArrayList<>();
        Runnable SIMPLE_TASK = () -> {
            try {
                while (!Thread.interrupted()) {
                    tasksQueue.getNext().run();
                }
            } catch (InterruptedException ignored) {
            } finally {
            Thread.currentThread().interrupt();
            }
        };
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
    public <T, R> List<R> map(Function<? super T, ? extends R> function, List<? extends T> list) throws InterruptedException {
        Collector<T, R> coll;
        synchronized (this) {
            coll = new Collector<>(function, list);
            tasksQueue.addTask(coll);
        }
        return coll.getResult();
    }

    /**
     * Joins all threads.
     */
    @Override
    public void close() {
        workersList.forEach(Thread::interrupt);
        synchronized (this) {
            tasksQueue.get(Collector::finish);
        }
        ThreadJoiner.joinAllNothrow(workersList);
    }
}
