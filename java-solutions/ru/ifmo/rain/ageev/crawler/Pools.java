package ru.ifmo.rain.ageev.crawler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class Pools {
    private final ExecutorService extractorPool;
    private final ExecutorService downloaderPool;
    private final static int AWAIT = 1;
    Pools(int extractors, int downloaders) {
        if (extractors <= 0 || downloaders <= 0) {
            throw new IllegalArgumentException("extractors and downloaders count must be greater than 0");
        }
        extractorPool = Executors.newFixedThreadPool(extractors);
        downloaderPool = Executors.newFixedThreadPool(downloaders);
    }

    void submitExtractor(Runnable runnable) {
        extractorPool.submit(runnable);
    }

    void submitDownloader(Runnable runnable) {
        downloaderPool.submit(runnable);
    }

    void shutdown() {
        extractorPool.shutdown();
        downloaderPool.shutdown();
        try {
            extractorPool.awaitTermination(AWAIT, TimeUnit.MILLISECONDS);
            downloaderPool.awaitTermination(AWAIT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            System.err.println("Can't terminate pools: " + e.getMessage());
        }
    }
}
