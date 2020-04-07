package ru.ifmo.rain.ageev.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.*;

public class WebCrawler implements Crawler {

    private final Downloader downloader;
    private final int perHost;
    private final ExecutorService extractorPool;
    private final ExecutorService downloaderPool;
    private final ConcurrentHashMap<String, HostDownloadersControl> downloaderFromHost = new ConcurrentHashMap<>();

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        extractorPool = Executors.newFixedThreadPool(extractors);
        downloaderPool = Executors.newFixedThreadPool(downloaders);
        this.perHost = perHost;
    }

    @Override
    public Result download(String s, int i) {
        var worker = new Worker(s);
        worker.run(i);
        return worker.result();
    }

    @Override
    public void close() {
        extractorPool.shutdown();
        downloaderPool.shutdown();
    }

    private class HostDownloadersControl {
        private final Queue<Runnable> tasksQueue = new ArrayDeque<>();
        private int nowRunning = 0;

        public synchronized void run(Runnable task) {
            tasksQueue.add(task);
            runNext();
        }

        private synchronized void runNext() {
            if (perHost > nowRunning) {
                var task = tasksQueue.poll();
                if (task != null) {
                    nowRunning++;
                    downloaderPool.submit(() -> {
                        try {
                            task.run();
                        } finally {
                            nowRunning--;
                            runNext();
                        }
                    });
                }
            }
        }
    }

    private class Worker {
        private final Set<String> results = ConcurrentHashMap.newKeySet();
        private final Set<String> usedUrls = ConcurrentHashMap.newKeySet();
        private final ConcurrentMap<String, IOException> errors = new ConcurrentHashMap<>();
        private final ConcurrentLinkedQueue<String> level = new ConcurrentLinkedQueue<>();

        Worker(String url) {
            level.add(url);
        }

        private void run(int depth) {
            while (depth-- > 0) {
                final var levelPhaser = new Phaser(1);
                var processing = new ArrayList<>(level);
                level.clear();
                final int nowDepth = depth;
                processing.stream()
                        .filter(usedUrls::add)
                        .forEach(link -> queueDownload(link, nowDepth, levelPhaser));
                levelPhaser.arriveAndAwaitAdvance();
            }
        }

        private void queueExtraction(Document page, Phaser completionPhaser) {
            completionPhaser.register();
            extractorPool.submit(() -> {
                try {
                    var urls = page.extractLinks();
                    level.addAll(urls);
                } catch (IOException ignored) {
                } finally {
                    completionPhaser.arrive();
                }
            });
        }

        private void queueDownload(String link, int nowDepth, Phaser levelPhaser) {
            String newHost;
            try {
                newHost = URLUtils.getHost(link);
            } catch (MalformedURLException e) {
                errors.put(link, e);
                return;
            }
            var hostDownloader = downloaderFromHost
                    .computeIfAbsent(newHost, unused -> new HostDownloadersControl());
            levelPhaser.register();
            hostDownloader.run(() -> {
                try {
                    var page = downloader.download(link);
                    results.add(link);
                    if (nowDepth > 0) {
                        queueExtraction(page, levelPhaser);
                    }
                } catch (IOException e) {
                    errors.put(link, e);
                } finally {
                    levelPhaser.arrive();
                }
            });
        }

        public Result result() {
            return new Result(new ArrayList<>(results), errors);
        }
    }

    private static int checkedGet(String[] args, int index, int defaultValue) {
        return index < args.length ? Integer.parseInt(args[index]) : defaultValue;
    }

    /**
     * This method is used to call the {@link WebCrawler#download}.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        if (args == null || args.length == 0 || Arrays.stream(args).anyMatch(Objects::isNull)) {
            System.err.println("Url [depth [downloads [extractors [perHost]]]]");
            return;
        }
        try {
            int defaultValue = 1;
            int depth = checkedGet(args, 1, defaultValue);
            int downloaders = checkedGet(args, 2, defaultValue);
            int extractors = checkedGet(args, 3, defaultValue);
            int perHost = checkedGet(args, 4, defaultValue);
            try (Crawler crawler = new WebCrawler(new CachingDownloader(), downloaders, extractors, perHost)) {
                crawler.download(args[0], depth);
            }
        } catch (NumberFormatException e) {
            System.err.println("Arguments after first must be numbers: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Failed to download: " + e.getMessage());
        }
    }
}
