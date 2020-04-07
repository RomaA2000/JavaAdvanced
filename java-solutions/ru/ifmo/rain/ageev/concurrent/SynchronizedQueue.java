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
    private final Queue<Collector<?, ?>> tasksQueue = new ArrayDeque<>();

    /**
     * Runs task from {@link Queue}.
     */
    public synchronized Runnable getNext() throws InterruptedException {
        while (tasksQueue.isEmpty()) {
            wait();
        }
        var task = tasksQueue.element().getNext();
        if (tasksQueue.element().wasLast()) {
            while (tasksQueue.isEmpty()) {
                wait();
            }
            tasksQueue.remove();
        }
        return task;
    }

    public synchronized void shutdown() {
        tasksQueue.forEach(Collector::finish);
    }

    /**
     * Adds task {@link Collector} to {@link Queue}.
     */
    public synchronized void addTask(final Collector<?, ?> task) {
        tasksQueue.add(task);
        notifyAll();
    }
}