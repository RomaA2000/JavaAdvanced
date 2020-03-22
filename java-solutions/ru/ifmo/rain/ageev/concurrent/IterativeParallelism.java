package ru.ifmo.rain.ageev.concurrent;

import info.kgeorgiy.java.advanced.concurrent.AdvancedIP;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * List iterative parallelism support for operations from {@link AdvancedIP}.
 *
 * @author ageev
 * @version 1.0.1
 */
public class IterativeParallelism implements AdvancedIP {
    private static <I, M, R> R parallelWork(final int threads, final List<I> values, final Function<Stream<I>, M> work,
                                            final Function<Stream<M>, R> merger) throws InterruptedException {
        final List<Stream<I>> blocks = makeBlocks(threads, values);
        final List<M> workersResults = new ArrayList<>(Collections.nCopies(blocks.size(), null));
        final List<Thread> workers = new ArrayList<>();
        for (int i = 0; i < blocks.size(); i++) {
            final int idx = i;
            final Thread thread = new Thread(() -> workersResults.set(idx, work.apply(blocks.get(idx))));
            thread.start();
            workers.add(thread);
        }
        joinAll(workers);
        return merger.apply(workersResults.stream());
    }

    private static void joinAll(final List<Thread> workers) throws InterruptedException {
        for (int i = 0; i < workers.size(); i++) {
            try {
                workers.get(i).join();
            } catch (final InterruptedException e) {
                final InterruptedException exception = new InterruptedException("Some threads were interrupted");
                exception.addSuppressed(e);
                for (int j = i; j < workers.size(); j++) {
                    workers.get(j).interrupt();
                }
                for (int j = i; j < workers.size(); j++) {
                    try {
                        workers.get(j).join();
                    } catch (InterruptedException er) {
                        exception.addSuppressed(er);
                        j--;
                    }
                }
                throw exception;
            }
        }
    }

    private static <I> List<Stream<I>> makeBlocks(final int number, final List<I> values) {
        if (number <= 0) {
            throw new NullPointerException("threads count must be greater than 0");
        }
        final List<Stream<I>> blocks = new ArrayList<>();
        final int blockSize = values.size() / number;
        final int r = values.size() % number;
        int pos = 0;
        for (int i = 0; i < number; i++) {
            final int currentBlock = (i < r ? 1 : 0) + blockSize;
            final int last = pos;
            pos += currentBlock;
            if (currentBlock > 0) {
                blocks.add(values.subList(last, pos).stream());
            }
        }
        return blocks;
    }

    private static <I> List<I> merge(final Stream<? extends List<? extends I>> streams) {
        return streams.flatMap(List::stream).collect(Collectors.toList());
    }

    private static <T> T getReduce(final Stream<T> stream, final Monoid<T> monoid) {
        return getMapReduce(stream, monoid, Function.identity());
    }

    private static <T, R> R getMapReduce(final Stream<T> stream, final Monoid<R> monoid, final Function<T, R> lift) {
        return stream.map(lift).reduce(monoid.getIdentity(), monoid.getOperator());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String join(final int threads, final List<?> values) throws InterruptedException {
        return parallelWork(threads, values,
                s -> s.map(Object::toString).collect(Collectors.joining()),
                s -> s.collect(Collectors.joining()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> List<T> filter(final int threads, final List<? extends T> values, final Predicate<? super T> predicate) throws InterruptedException {
        return parallelWork(threads, values, s -> s.filter(predicate).collect(Collectors.toList()), IterativeParallelism::merge);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T, U> List<U> map(final int threads, final List<? extends T> values, final Function<? super T, ? extends U> f) throws InterruptedException {
        return parallelWork(threads, values, s -> s.map(f).collect(Collectors.toList()), IterativeParallelism::merge);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T maximum(final int threads, final List<? extends T> values, final Comparator<? super T> comparator) throws InterruptedException {
        return minimum(threads, values, comparator.reversed());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T minimum(final int threads, final List<? extends T> values, final Comparator<? super T> comparator) throws InterruptedException {
        return minMaxSameReducePart(threads, values, s -> s.min(comparator).orElse(null));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> boolean all(final int threads, final List<? extends T> values, final Predicate<? super T> predicate) throws InterruptedException {
        return parallelWork(threads, values,
                s -> s.allMatch(predicate),
                s -> s.allMatch(Boolean::booleanValue));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> boolean any(final int threads, final List<? extends T> values, final Predicate<? super T> predicate) throws InterruptedException {
        return !all(threads, values, predicate.negate());
    }

    private <I> I minMaxSameReducePart(final int threads, final List<I> values, final Function<Stream<I>, I> comparator) throws InterruptedException {
        return parallelWork(threads, values, comparator, comparator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T reduce(final int threads, final List<T> values, final Monoid<T> monoid) throws InterruptedException {
        return minMaxSameReducePart(threads, values, s -> getReduce(s, monoid));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T, R> R mapReduce(final int threads, final List<T> values, final Function<T, R> lift, final Monoid<R> monoid) throws InterruptedException {
        return parallelWork(threads, values, s -> getMapReduce(s, monoid, lift), s -> getReduce(s, monoid));
    }
}
