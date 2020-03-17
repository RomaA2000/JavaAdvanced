package ru.ifmo.ageev.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * List iterative parallelism support for operations from {@link ListIP}.
 *
 * @author ageev
 * @version 1.0.1
 */
public class IterativeParallelism implements ListIP {
    @Override
    /**
     * {@inheritDoc}
     */
    public String join(int threads, List<?> values) throws InterruptedException {
        return parallelWork(threads, values,
                s -> s.map(Object::toString).collect(Collectors.joining()),
                s -> s.collect(Collectors.joining()));
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public <T> List<T> filter(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return parallelWork(threads, values, s -> s.filter(predicate), this::merge);
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public <T, U> List<U> map(int threads, List<? extends T> values, Function<? super T, ? extends U> f) throws InterruptedException {
        return parallelWork(threads, values, s -> s.map(f), this::merge);
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return minMaxSamePart(threads, values, s -> s.max(comparator).orElse(null));
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return minMaxSamePart(threads, values, s -> s.min(comparator).orElse(null));
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return parallelWork(threads, values,
                s -> s.allMatch(predicate),
                s -> s.allMatch(Boolean::booleanValue));
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return parallelWork(threads, values,
                s -> s.anyMatch(predicate),
                s -> s.anyMatch(Boolean::booleanValue));
    }

    private <I> I minMaxSamePart(int threads, List<? extends I> values, Function<Stream<? extends I>, I> comparator) throws InterruptedException {
        return parallelWork(threads, values, comparator, comparator);
    }

    private <I, M, R> R parallelWork(int threads, List<? extends I> values, Function<Stream<? extends I>, M> work,
                                     Function<Stream<? extends M>, R> merger) throws InterruptedException {
        List<Stream<? extends I>> blocks = makeBlocks(threads, values);
        List<M> workersResults = new ArrayList<>(Collections.nCopies(blocks.size(), null));
        List<Thread> workers = new ArrayList<>();
        for (int i = 0; i < blocks.size(); ++i) {
            final int idx = i;
            Thread thread = new Thread(() -> workersResults.set(idx, work.apply(blocks.get(idx))));
            thread.start();
            workers.add(thread);
        }
        joinAll(workers);
        return merger.apply(workersResults.stream());
    }

    private void joinAll(List<Thread> workers) throws InterruptedException {
        List<InterruptedException> interruptedExceptions = new ArrayList<>();
        workers.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                interruptedExceptions.add(e);
            }
        });
        if (!interruptedExceptions.isEmpty()) {
            InterruptedException joinFail = new InterruptedException(interruptedExceptions.size() + " threads were interrupted");
            interruptedExceptions.forEach(joinFail::addSuppressed);
            throw joinFail;
        }
    }


    private <I> List<Stream<? extends I>> makeBlocks(int number, List<? extends I> values) {
        List<Stream<? extends I>> blocks = new ArrayList<>();
        int blockSize = values.size() / number;
        int r = values.size() % number;
        int pos = 0;
        for (int i = 0; i < number; ++i) {
            int currentBlock = (i < r ? 1 : 0) + blockSize;
            int last = pos;
            pos += currentBlock;
            if (currentBlock > 0) {
                blocks.add(values.subList(last, pos).stream());
            }
        }
        return blocks;
    }

    private <I> List<I> merge(Stream<? extends Stream<? extends I>> streams) {
        return streams.flatMap(Function.identity()).collect(Collectors.toList());
    }
}
