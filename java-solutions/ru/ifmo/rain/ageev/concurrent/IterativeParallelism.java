package ru.ifmo.rain.ageev.concurrent;

import info.kgeorgiy.java.advanced.concurrent.AdvancedIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.ifmo.rain.ageev.concurrent.ThreadJoiner.joinAll;

/**
 * List iterative parallelism support for operations from {@link AdvancedIP}.
 *
 * @author ageev
 * @version 1.0.1
 */
public class IterativeParallelism implements AdvancedIP {
    private final ParallelMapper mapper;

    /**
     * Default constructor.
     * Creates an IterativeParallelism instance operating without {@link ParallelMapper}.
     */
    public IterativeParallelism() {
        mapper = null;
    }

    /**
     * Mapper constructor.
     * Creates an IterativeParallelism instance with {@link ParallelMapper} as a core mapper.
     *
     * @param map {@link ParallelMapper} instance
     */
    public IterativeParallelism(ParallelMapper map) {
        Objects.requireNonNull(map);
        mapper = map;
    }

    private static <I> List<Stream<I>> makeBlocks(final int number, final List<I> values) {
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

    private <I, M, R> R parallelWork(final int threads, final List<I> values, final Function<Stream<I>, M> work,
                                     final Function<Stream<M>, R> merger) throws InterruptedException {
        if (threads <= 0) {
            throw new IllegalArgumentException("threads count must be greater than 0");
        }
        final List<Stream<I>> blocks = makeBlocks(threads, values);
        final List<M> workersResults;
        if (mapper == null) {
            workersResults = new ArrayList<>(Collections.nCopies(blocks.size(), null));
            final List<Thread> workers = new ArrayList<>();
            for (int i = 0; i < blocks.size(); i++) {
                final int idx = i;
                Thread thread = new Thread(() -> workersResults.set(idx, work.apply(blocks.get(idx))));
                thread.start();
                workers.add(thread);
            }
            joinAll(workers);
        } else {
            workersResults = mapper.map(work, blocks);
        }
        return merger.apply(workersResults.stream());
    }

    private <T> Function<Stream<T>, T> getReduce(final Monoid<T> monoid) {
        return getMapReduce(monoid, Function.identity());
    }

    private <T, R> Function<Stream<T>, R> getMapReduce(final Monoid<R> monoid, final Function<T, R> lift) {
        return stream -> stream.map(lift).reduce(monoid.getIdentity(), monoid.getOperator());
    }

    private <I, M> List<M> filterMapSamePart(final int threads, final List<? extends I> values, final Function<Stream<? extends I>, Stream<? extends M>> work) throws InterruptedException {
        return parallelWork(threads, values, s -> work.apply(s).collect(Collectors.toList()), IterativeParallelism::merge);
    }

    @Override
    public String join(final int threads, final List<?> values) throws InterruptedException {
        return parallelWork(threads, values,
                s -> s.map(Object::toString).collect(Collectors.joining()),
                s -> s.collect(Collectors.joining()));
    }

    @Override
    public <T> List<T> filter(final int threads, final List<? extends T> values, final Predicate<? super T> predicate) throws InterruptedException {
        return filterMapSamePart(threads, values, s -> s.filter(predicate));
    }

    @Override
    public <T, U> List<U> map(final int threads, final List<? extends T> values, final Function<? super T, ? extends U> f) throws InterruptedException {
        return filterMapSamePart(threads, values, s -> s.map(f));
    }

    @Override
    public <T> T maximum(final int threads, final List<? extends T> values, final Comparator<? super T> comparator) throws InterruptedException {
        return minimum(threads, values, comparator.reversed());
    }

    @Override
    public <T> T minimum(final int threads, final List<? extends T> values, final Comparator<? super T> comparator) throws InterruptedException {
        return minMaxSameReducePart(threads, values, s -> s.min(comparator).orElse(null));
    }

    @Override
    public <T> boolean all(final int threads, final List<? extends T> values, final Predicate<? super T> predicate) throws InterruptedException {
        return parallelWork(threads, values,
                s -> s.allMatch(predicate),
                s -> s.allMatch(Boolean::booleanValue));
    }

    @Override
    public <T> boolean any(final int threads, final List<? extends T> values, final Predicate<? super T> predicate) throws InterruptedException {
        return !all(threads, values, predicate.negate());
    }

    private <I> I minMaxSameReducePart(final int threads, final List<I> values, final Function<Stream<I>, I> comparator) throws InterruptedException {
        return parallelWork(threads, values, comparator, comparator);
    }

    @Override
    public <T> T reduce(final int threads, final List<T> values, final Monoid<T> monoid) throws InterruptedException {
        return minMaxSameReducePart(threads, values, getReduce(monoid));
    }

    @Override
    public <T, R> R mapReduce(final int threads, final List<T> values, final Function<T, R> lift, final Monoid<R> monoid) throws InterruptedException {
        return parallelWork(threads, values, getMapReduce(monoid, lift), getReduce(monoid));
    }
}
