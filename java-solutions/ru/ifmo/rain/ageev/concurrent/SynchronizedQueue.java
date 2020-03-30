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
    private Queue<Runnable> tasksQueue;

    /**
     * Constructor from {@link Queue}.
     * Creates an instance of this class.
     *
     * @param queue another {@link Queue}.
     */
    public SynchronizedQueue(Queue<Runnable> queue) {
        tasksQueue = queue;
    }

    /**
     * Default constructor from {@link Queue}.
     * Creates an instance of this class.
     */
    public SynchronizedQueue() {
        this(new ArrayDeque<>());
    }

    /**
     * Runs task from {@link Queue}.
     */
    public synchronized void runTask() throws InterruptedException {
        Runnable task;
        {
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
