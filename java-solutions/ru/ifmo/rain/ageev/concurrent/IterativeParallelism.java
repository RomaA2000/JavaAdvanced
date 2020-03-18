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
    // :NOTE: Неверное расположение Javadoc методов
    @Override
    /**
     * {@inheritDoc}
     */
    public String join(final int threads, final List<?> values) throws InterruptedException {
        return parallelWork(threads, values,
                s -> s.map(Object::toString).collect(Collectors.joining()),
                s -> s.collect(Collectors.joining()));
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public <T> List<T> filter(final int threads, final List<? extends T> values, final Predicate<? super T> predicate) throws InterruptedException {
        return parallelWork(threads, values, s -> s.filter(predicate), IterativeParallelism::merge);
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public <T, U> List<U> map(final int threads, final List<? extends T> values, final Function<? super T, ? extends U> f) throws InterruptedException {
        return parallelWork(threads, values, s -> s.map(f), IterativeParallelism::merge);
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public <T> T maximum(final int threads, final List<? extends T> values, final Comparator<? super T> comparator) throws InterruptedException {
        // :NOTE: Еще больше унифицировать с minimum
        return minMaxSamePart(threads, values, s -> s.max(comparator).orElse(null));
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public <T> T minimum(final int threads, final List<? extends T> values, final Comparator<? super T> comparator) throws InterruptedException {
        return minMaxSamePart(threads, values, s -> s.min(comparator).orElse(null));
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public <T> boolean all(final int threads, final List<? extends T> values, final Predicate<? super T> predicate) throws InterruptedException {
        return parallelWork(threads, values,
                s -> s.allMatch(predicate),
                s -> s.allMatch(Boolean::booleanValue));
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public <T> boolean any(final int threads, final List<? extends T> values, final Predicate<? super T> predicate) throws InterruptedException {
        return !all(threads, values, predicate.negate());
    }

    private <I> I minMaxSamePart(final int threads, final List<I> values, final Function<Stream<I>, I> comparator) throws InterruptedException {
        return parallelWork(threads, values, comparator, comparator);
    }

    private static <I, M, R> R parallelWork(final int threads, final List<I> values, final Function<Stream<I>, M> work,
                                            final Function<Stream<M>, R> merger) throws InterruptedException {
        final List<Stream<I>> blocks = makeBlocks(threads, values);
        final List<M> workersResults = new ArrayList<>(Collections.nCopies(blocks.size(), null));
        final List<Thread> workers = new ArrayList<>();
        for (int i = 0; i < blocks.size(); ++i) {
            final int idx = i;
            final Thread thread = new Thread(() -> workersResults.set(idx, work.apply(blocks.get(idx))));
            thread.start();
            workers.add(thread);
        }
        joinAll(workers);
        return merger.apply(workersResults.stream());
    }

    private static void joinAll(final List<Thread> workers) throws InterruptedException {
        for (final Iterator<Thread> i = workers.iterator(); i.hasNext(); ) {
            final Thread now = i.next();
            try {
                now.join();
            } catch (final InterruptedException e) {
                now.interrupt();
                final InterruptedException exception = new InterruptedException("Some threads were interrupted");
                exception.addSuppressed(e);
                // :NOTE: Надо дождаться окончания всех потоков
                for (; i.hasNext(); ) {
                    i.next().interrupt();
                }
                throw exception;
            }
        }
    }


    private static <I> List<Stream<I>> makeBlocks(final int number, final List<I> values) {
        final List<Stream<I>> blocks = new ArrayList<>();
        final int blockSize = values.size() / number;
        final int r = values.size() % number;
        int pos = 0;
        for (int i = 0; i < number; ++i) {
            final int currentBlock = (i < r ? 1 : 0) + blockSize;
            final int last = pos;
            pos += currentBlock;
            if (currentBlock > 0) {
                blocks.add(values.subList(last, pos).stream());
            }
        }
        return blocks;
    }

    private static <I> List<I> merge(final Stream<? extends Stream<? extends I>> streams) {
        return streams.flatMap(Function.identity()).collect(Collectors.toList());
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public <T> T reduce(final int threads, final List<T> values, final Monoid<T> monoid) throws InterruptedException {
        return minMaxSamePart(threads, values, s -> getReduce(s, monoid));
    }

    private static <T> T getReduce(final Stream<T> stream, final Monoid<T> monoid) {
        return stream.reduce(monoid.getIdentity(), monoid.getOperator());
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public <T, R> R mapReduce(final int threads, final List<T> values, final Function<T, R> lift, final Monoid<R> monoid) throws InterruptedException {
        final Function<Stream<T>, R> reducer = s -> s.map(lift).reduce(monoid.getIdentity(), monoid.getOperator());
        return parallelWork(threads, values, reducer, s -> getReduce(s, monoid));
    }
}
