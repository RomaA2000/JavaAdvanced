package ru.ifmo.rain.ageev.crawler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Pools {
    private final ExecutorService extractorPool;
    private final ExecutorService downloaderPool;

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
    }
}
