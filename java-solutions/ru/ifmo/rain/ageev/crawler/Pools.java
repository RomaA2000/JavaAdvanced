package ru.ifmo.rain.ageev.crawler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class Pools {
    private final static int AWAIT = 1;
    private final ExecutorService extractorPool;
    private final ExecutorService downloaderPool;

    public Pools(final int extractors, final int downloaders) {
        if (extractors <= 0 || downloaders <= 0) {
            throw new IllegalArgumentException("extractors and downloaders count must be greater than 0");
        }
        extractorPool = Executors.newFixedThreadPool(extractors);
        downloaderPool = Executors.newFixedThreadPool(downloaders);
    }

    private static void await(ExecutorService executorService) {
        try {
            executorService.awaitTermination(AWAIT, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException e) {
            System.err.println("Can't terminate pool: " + e.getMessage());
        }
    }

    public void submitExtractor(final Runnable runnable) {
        extractorPool.submit(runnable);
    }

    public void submitDownloader(final Runnable runnable) {
        downloaderPool.submit(runnable);
    }

    public void shutdown() {
        extractorPool.shutdown();
        downloaderPool.shutdown();
        await(extractorPool);
        await(downloaderPool);
    }
}
