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
    private final Queue<Runnable> tasksQueue;
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
        tasksQueue = new ArrayDeque<>();
        workersList = new ArrayList<>();
        Runnable SIMPLE_TASK = () -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    runTaskSynchronised();
                }
            } catch (InterruptedException ignored) {
            }
        };
        range(0, number).forEach(i -> workersList.add(new Thread(SIMPLE_TASK)));
        workersList.forEach(Thread::start);
    }

    private void runTaskSynchronised() throws InterruptedException {
        Runnable task;
        synchronized (tasksQueue) {
            while (tasksQueue.isEmpty()) {
                tasksQueue.wait();
            }
            task = tasksQueue.poll();
        }
        task.run();
    }

    private void addTaskSynchronised(final Runnable task) {
        synchronized (tasksQueue) {
            tasksQueue.add(task);
            tasksQueue.notify();
        }
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
        Collector<R> collector = new Collector<>(list.size());
        List<RuntimeException> exceptions = new ArrayList<>();
        int idx = 0;
        for (final T value : list) {
            final int index = idx;
            addTaskSynchronised(() -> {
                R ans = null;
                try {
                    ans = function.apply(value);
                } catch (RuntimeException e) {
                    synchronized (exceptions) {
                        exceptions.add(e);
                    }
                }
                collector.setSynchronized(index, ans);
            });
            idx++;
        }
        if (!exceptions.isEmpty()) {
            RuntimeException e = new RuntimeException("Runtime Exceptions occurred while mapping");
            exceptions.forEach(e::addSuppressed);
            throw e;
        }
        return collector.getResultSynchronized();
    }

    /**
     * Joins all threads.
     */
    @Override
    public void close() {
        workersList.forEach(Thread::interrupt);
        ThreadJoiner.joinAllNothrow(workersList);
    }
}
