package ru.ifmo.rain.ageev.crawler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class Pools {
    private final ExecutorService extractorPool;
    private final ExecutorService downloaderPool;
    private final static int AWAIT = 1;

    Pools(final int extractors, final int downloaders) {
        if (extractors <= 0 || downloaders <= 0) {
            throw new IllegalArgumentException("extractors and downloaders count must be greater than 0");
        }
        extractorPool = Executors.newFixedThreadPool(extractors);
        downloaderPool = Executors.newFixedThreadPool(downloaders);
    }

    void submitExtractor(final Runnable runnable) {
        extractorPool.submit(runnable);
    }

    void submitDownloader(final Runnable runnable) {
        downloaderPool.submit(runnable);
    }

    void shutdown() {
        extractorPool.shutdown();
        downloaderPool.shutdown();
        try {
            extractorPool.awaitTermination(AWAIT, TimeUnit.MILLISECONDS);
            downloaderPool.awaitTermination(AWAIT, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException e) {
            System.err.println("Can't terminate pools: " + e.getMessage());
        }
    }
}
