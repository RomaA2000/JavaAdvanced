package ru.ifmo.rain.ageev.concurrent;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Class for storing tasks {@link Runnable}.
 *
 * @author ageev
 * @version 1.0
 */
public class SynchronizedQueue {
    private final Queue<Runnable> tasksQueue;

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
    public void runTask() throws InterruptedException {
        Runnable task;
        synchronized (this) {
            while (tasksQueue.isEmpty()) {
                wait();
            }
            task = tasksQueue.poll();
        }
        task.run();
    }

    /**
     * Adds task {@link Runnable} to {@link Queue}.
     */
    public synchronized void addTask(final Runnable task) {
        tasksQueue.add(task);
        notify();
    }
}
