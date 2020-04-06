package ru.ifmo.rain.ageev.concurrent;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Consumer;

/**
 * Class for storing tasks {@link Runnable}.
 *
 * @author ageev
 * @version 1.0
 */
class SynchronizedQueue {
    private final Queue<Collector<?, ?>> tasksQueue;

    /**
     * Default constructor from {@link Queue}.
     * Creates an instance of this class.
     */
    public SynchronizedQueue() {
        tasksQueue = new ArrayDeque<>();
    }

    /**
     * Runs task from {@link Queue}.
     */
    public synchronized Runnable getNext() throws InterruptedException {
        while (tasksQueue.isEmpty()) {
            wait();
        }
        var task = tasksQueue.element().getNext();
        if (tasksQueue.element().wasLast()) {
            remove();
        }
        return task;
    }

    public synchronized void remove() throws InterruptedException {
        while (tasksQueue.isEmpty()) {
            wait();
        }
        tasksQueue.remove();
    }

    public synchronized void get(Consumer<Collector<?, ?>> consumer) {
        tasksQueue.forEach(consumer);
    }

    /**
     * Adds task {@link Collector} to {@link Queue}.
     */
    public synchronized void addTask(final Collector<?, ?> task) {
        tasksQueue.add(task);
        notifyAll();
    }
}