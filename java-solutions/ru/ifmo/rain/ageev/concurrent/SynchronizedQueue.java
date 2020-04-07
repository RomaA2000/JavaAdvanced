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
        final Collector<?, ?> task = tasksQueue.element();
        final var subTask = task.getNext();
        if (task.wasLast()) {
            tasksQueue.remove();
        }
        return subTask;
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